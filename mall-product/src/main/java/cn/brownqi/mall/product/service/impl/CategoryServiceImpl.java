package cn.brownqi.mall.product.service.impl;

import cn.brownqi.mall.product.service.CategoryBrandRelationService;
import cn.brownqi.mall.product.vo.CateLog2VO;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.brownqi.common.utils.PageUtils;
import cn.brownqi.common.utils.Query;


import cn.brownqi.mall.product.dao.CategoryDao;
import cn.brownqi.mall.product.entity.CategoryEntity;
import cn.brownqi.mall.product.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        //1、查出所有分类
        List<CategoryEntity> entities = baseMapper.selectList(null);

        //2、组装成父子的树形结构
        //2.1、 找到所有一级分类
        List<CategoryEntity> level1Menus = entities.stream()
                .filter(categoryEntity -> categoryEntity.getParentCid() == 0)
                .map((menu) -> {
                    menu.setChildren(getChildren(menu, entities));
                    return menu;
                })
                .sorted((menu1, menu2) -> {
                    return (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort());
                })
                .collect(Collectors.toList());

        return level1Menus;
    }

    @Override
    public void removeMenusByIds(List<Long> asList) {
        //TODO 1、检查当前删除的菜单是否被其他地方引用

        //逻辑删除
        baseMapper.deleteBatchIds(asList);
    }

    @Override
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> paths = new ArrayList<>();

        List<Long> parentPath = findParentPath(catelogId, paths);

        return parentPath.toArray(new Long[parentPath.size()]);
    }

    /**
     * 级联更新所有关联的数据
     *
     * @param category
     */


//    @Caching(evict = {  // 同时进行多种缓存操作
//            @CacheEvict(value = "category",key = "'getLevel1Categorys'"), // 缓存失效模式,
//            @CacheEvict(value = "category",key="'getCatalogJson'")
//    })
    @CacheEvict(value = "category",allEntries = true)   // 指定删除某个分区下的所有数据
    @Transactional
    @Override
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);
        categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());
        // TODO 同时修改缓存中的数据（双写模式） / 删除缓存中的数据（失效模式）
    }

    // 每一个需要缓存的数据，我们都来指定要放到哪个名字的缓存。（缓存分区 - 按照业务类型分）
    @Cacheable(value = {"category"},key = "#root.method.name",sync = true)  // 代表当前方法的结果需要缓存，如果缓存中有，方法不用调用。如果缓存中没有，会调用方法，最后将方法的结果放入缓存
    @Override
    public List<CategoryEntity> getLevel1Categorys() {
        List<CategoryEntity> entities = baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
        return entities;
    }

    // TODO 可能产生堆外内存溢出：OutOfDirMemoryError （lettuce 5.1.8 发现有这个异常，5.2.2 没有发现这个异常）
    // 1、springboot2.0以后默认使用lettuce作为操作redis的客户端，使用netty进行网络通信
    // 2、lettuce的bug导致netty堆外内存溢出,-Xmx300m：netty如果没有指定对外内存，默认使用-Xmx300m
    //      可以通过 -Dio.netty.maxDirectMemory 进行设置
    // 解决方案：不能只去调大对外内存
    //      1)、升级lettuce客户端
    //      2）、切换使用jedis客户端


    @Cacheable(value = "category",key = "#root.methodName")
    @Override
    public Map<String, List<CateLog2VO>> getCatalogJson() {
        System.out.println("查询了数据库");
        /**
         *  1、将数据库的多次查询变为一次（本地缓存）
         */
        List<CategoryEntity> selectList = baseMapper.selectList(null);

        // 1、查出所有1级分类
        List<CategoryEntity> level1Categorys = getParent_cid(selectList, 0L);

        // 2、封装数据
        Map<String, List<CateLog2VO>> catalogJsonFromDB = level1Categorys.stream().collect(
                Collectors.toMap(
                        k -> k.getCatId().toString(),
                        v -> {
                            // 1、每一个的一级分类，查到这个一级分类的二级分类
                            List<CategoryEntity> categoryEntities =
                                    getParent_cid(selectList, v.getCatId());

                            // 2、封装上面的结果
                            List<CateLog2VO> cateLog2VOList = null;
                            if (categoryEntities != null) {
                                cateLog2VOList = categoryEntities.stream().map(l2 -> {
                                    CateLog2VO cateLog2VO = new CateLog2VO(v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName());
                                    // 1、找当前二级分类的三级分类封装成VO
                                    List<CategoryEntity> level3Categorys = getParent_cid(selectList, l2.getCatId());
                                    if (level3Categorys != null) {
                                        List<CateLog2VO.Catelog3VO> catalog3VOList = level3Categorys.stream().map(l3 -> {
                                            // 2、封装成指定格式
                                            CateLog2VO.Catelog3VO catalog3VO = new CateLog2VO.Catelog3VO(l2.getCatId().toString(), l3.getCatId().toString(), l3.getName());
                                            return catalog3VO;
                                        }).collect(Collectors.toList());
                                        cateLog2VO.setCatalog3List(catalog3VOList);
                                    }
                                    return cateLog2VO;
                                }).collect(Collectors.toList());
                            }
                            return cateLog2VOList;
                        }
                )
        );
        return catalogJsonFromDB;

    }

    //    @Override
    public Map<String, List<CateLog2VO>> getCatalogJsonCacheByRedis() {

        /**
         * 1、空结果缓存：解决缓存穿透
         * 2、设置过期时间（随机值）：解决缓存雪崩
         * 3、加锁：解决缓存击穿
         */

        // 1、加入缓存逻辑,缓存中存得数据value时 JSON 字符串，从缓存中拿出的 JSON 字符串，要逆转为相应的对象类型。（序列化与反序列化）
        // JSON 跨语言，跨平台
        String catalogJSON = stringRedisTemplate.opsForValue().get("catalogJSON");
        if (StringUtils.isEmpty(catalogJSON)) {
            // 2、缓存中没有，查询数据库
            System.out.println("缓存未命中，查询数据库");
            Map<String, List<CateLog2VO>> catalogJsonFromDB = getCatalogJsonFromDBWithRedissonLock();
            // 4、返回从数据库查到的结果
            return catalogJsonFromDB;
        }

        // 转为我们指定的对象。
        System.out.println("缓存命中，直接返回");
        Map<String, List<CateLog2VO>> result = JSON.parseObject(catalogJSON, new TypeReference<Map<String, List<CateLog2VO>>>() {
        });
        return result;
    }

    /**
     * 从数据库查询并封装分类数据，redisson 分布式锁
     *
     * 问题：缓存里面的数据如何和数据库保持一直
     * 缓存数据一致性：
     *  1）双写模式
     *  2）失效模式
     *
     * @return
     */
    private Map<String, List<CateLog2VO>> getCatalogJsonFromDBWithRedissonLock() {

        // 1、锁的名字 => 锁的粒度
        // 具体缓存的是某个数据，例：11号商品：product-11-lock
        RLock lock = redissonClient.getLock("catalogJson-lock");
        lock.lock();

        Map<String, List<CateLog2VO>> dataFromDB;
        try {
            // 加锁成功...执行业务
            dataFromDB = getDataFromDB();
        } finally {
            lock.unlock();
        }
        return dataFromDB;

    }

    /**
     * 从数据库查询并封装分类数据，redis 分布式锁
     *
     * @return
     */
    private Map<String, List<CateLog2VO>> getCatalogJsonFromDBWithRedisLock() {

        // 1、占分布式锁，setnx redis
        String uuid = UUID.randomUUID().toString();
        // 抢占 redis 锁，并设定过期时间（原子性）
        Boolean lock = stringRedisTemplate.opsForValue().setIfAbsent("lock", uuid, 30, TimeUnit.SECONDS);
        if (lock) {
            System.out.println("获取分布式锁成功");
            Map<String, List<CateLog2VO>> dataFromDB;
            try {
                // 加锁成功...执行业务
                dataFromDB = getDataFromDB();
            } finally {
                // 判断锁是否为当时的uuid，如果是，删除该锁（lua 脚本解锁）
                String script = "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
                stringRedisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class), Arrays.asList("lock"), uuid);
            }
            return dataFromDB;
        } else {
            System.out.println("获取分布式锁失败...等待重试");
            // 加锁失败...重试
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return getCatalogJsonFromDBWithRedisLock();// 自旋
        }
    }

    /**
     * 从数据库中查询并封装数据，无锁
     *
     * @return
     */
    private Map<String, List<CateLog2VO>> getDataFromDB() {
        String catalogJSON = stringRedisTemplate.opsForValue().get("catalogJSON");
        if (!StringUtils.isEmpty(catalogJSON)) {
            // 缓存不为null直接返回
            Map<String, List<CateLog2VO>> result = JSON.parseObject(catalogJSON, new TypeReference<Map<String, List<CateLog2VO>>>() {
            });
            return result;
        }
        System.out.println("查询了数据库");
        /**
         *  1、将数据库的多次查询变为一次（本地缓存）
         */
        List<CategoryEntity> selectList = baseMapper.selectList(null);

        // 1、查出所有1级分类
        List<CategoryEntity> level1Categorys = getParent_cid(selectList, 0L);

        // 2、封装数据
        Map<String, List<CateLog2VO>> catalogJsonFromDB = level1Categorys.stream().collect(
                Collectors.toMap(
                        k -> k.getCatId().toString(),
                        v -> {
                            // 1、每一个的一级分类，查到这个一级分类的二级分类
                            List<CategoryEntity> categoryEntities =
                                    getParent_cid(selectList, v.getCatId());

                            // 2、封装上面的结果
                            List<CateLog2VO> cateLog2VOList = null;
                            if (categoryEntities != null) {
                                cateLog2VOList = categoryEntities.stream().map(l2 -> {
                                    CateLog2VO cateLog2VO = new CateLog2VO(v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName());
                                    // 1、找当前二级分类的三级分类封装成VO
                                    List<CategoryEntity> level3Categorys = getParent_cid(selectList, l2.getCatId());
                                    if (level3Categorys != null) {
                                        List<CateLog2VO.Catelog3VO> catalog3VOList = level3Categorys.stream().map(l3 -> {
                                            // 2、封装成指定格式
                                            CateLog2VO.Catelog3VO catalog3VO = new CateLog2VO.Catelog3VO(l2.getCatId().toString(), l3.getCatId().toString(), l3.getName());
                                            return catalog3VO;
                                        }).collect(Collectors.toList());
                                        cateLog2VO.setCatalog3List(catalog3VOList);
                                    }
                                    return cateLog2VO;
                                }).collect(Collectors.toList());
                            }
                            return cateLog2VOList;
                        }
                )
        );
        // 3、将查到的数据放入缓存
        String jsonCatalogJsonFromDB = JSON.toJSONString(catalogJsonFromDB);
        stringRedisTemplate.opsForValue().set("catalogJSON", jsonCatalogJsonFromDB, 1, TimeUnit.DAYS);
        return catalogJsonFromDB;
    }


    /**
     * 从数据库查询并封装分类数据，本地锁
     *
     * @return
     */
    private Map<String, List<CateLog2VO>> getCatalogJsonFromDBWithLocalLock() {

        // TODO 本地锁只能锁住当前进程里面的资源，在分布式情况下，想要锁住所有，要是用分布式锁
        synchronized (this) {
            // 得到锁以后，应该再去缓存中确定一次，如果没有才需要继续查询
            return getDataFromDB();
        }

    }

    private List<CategoryEntity> getParent_cid(List<CategoryEntity> selectList, Long parent_cid) {
        List<CategoryEntity> collect = selectList.stream().filter(item -> item.getParentCid().equals(parent_cid)).collect(Collectors.toList());
        return collect;
    }

    private List<Long> findParentPath(Long catelogId, List<Long> paths) {
        paths.add(catelogId);
        CategoryEntity byId = this.getById(catelogId);
        if (byId.getParentCid() != 0) {
            findParentPath(byId.getParentCid(), paths);
        }
        Collections.reverse(paths);
        return paths;
    }


    /**
     * 递归查询所有菜单的子菜单
     *
     * @param root
     * @param all
     * @return
     */
    private List<CategoryEntity> getChildren(CategoryEntity root, List<CategoryEntity> all) {
        List<CategoryEntity> children = all.stream()
                .filter(categoryEntity -> categoryEntity.getParentCid().equals(root.getCatId()))
                .map(categoryEntity -> {
                    //1、递归找子菜单
                    categoryEntity.setChildren(getChildren(categoryEntity, all));
                    return categoryEntity;
                })
                //2、菜单排序
                .sorted((menu1, menu2) -> {
                    return (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort());
                })
                .collect(Collectors.toList());
        return children;
    }

}