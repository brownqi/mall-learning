package cn.brownqi.mall.product.service.impl;

import cn.brownqi.mall.product.service.CategoryBrandRelationService;
import cn.brownqi.mall.product.vo.CateLog2VO;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
    @Transactional
    @Override
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);
        categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());
    }

    @Override
    public List<CategoryEntity> getLevel1Categorys() {
        List<CategoryEntity> entities = baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
        return entities;
    }

    @Override
    public Map<String, List<CateLog2VO>> getCatalogJson() {
        // 1、加入缓存逻辑,缓存中存得数据value时 JSON 字符串，从缓存中拿出的 JSON 字符串，要逆转为相应的对象类型。（序列化与反序列化）
        // JSON 跨语言，跨平台
        String catalogJSON = stringRedisTemplate.opsForValue().get("catalogJSON");
        if (StringUtils.isEmpty(catalogJSON)){
            // 2、缓存中没有，查询数据库
            Map<String, List<CateLog2VO>> catalogJsonFromDB = getCatalogJsonFromDB();
            // 3、将查到的数据放入缓存
            String jsonCatalogJsonFromDB = JSON.toJSONString(catalogJsonFromDB);
            stringRedisTemplate.opsForValue().set("catalogJSON",jsonCatalogJsonFromDB);
            // 4、返回从数据库查到的结果
            return catalogJsonFromDB;
        }

        // 转为我们指定的对象。
        Map<String, List<CateLog2VO>> result = JSON.parseObject(catalogJSON, new TypeReference<Map<String, List<CateLog2VO>>>() {
        });
        return result;
    }

    /**
     * 从数据库查询并封装分类数据
     * @return
     */
    private Map<String, List<CateLog2VO>> getCatalogJsonFromDB() {

        /**
         *  1、将数据库的多次查询变为一次（本地缓存）
         */
        List<CategoryEntity> selectList = baseMapper.selectList(null);

        // 1、查出所有1级分类
        List<CategoryEntity> level1Categorys = getParent_cid(selectList, 0L);

        // 2、封装数据
        Map<String, List<CateLog2VO>> parent_cid = level1Categorys.stream().collect(
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

        return parent_cid;
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