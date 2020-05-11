package cn.brownqi.mall.product.service.impl;

import cn.brownqi.common.constant.ProductConstant;
import cn.brownqi.mall.product.dao.AttrAttrgroupRelationDao;
import cn.brownqi.mall.product.dao.AttrGroupDao;
import cn.brownqi.mall.product.dao.CategoryDao;
import cn.brownqi.mall.product.entity.AttrAttrgroupRelationEntity;
import cn.brownqi.mall.product.entity.AttrGroupEntity;
import cn.brownqi.mall.product.entity.CategoryEntity;
import cn.brownqi.mall.product.service.CategoryService;
import cn.brownqi.mall.product.vo.AttrGroupRelationVO;
import cn.brownqi.mall.product.vo.AttrRespVO;
import cn.brownqi.mall.product.vo.AttrVO;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.brownqi.common.utils.PageUtils;
import cn.brownqi.common.utils.Query;


import cn.brownqi.mall.product.dao.AttrDao;
import cn.brownqi.mall.product.entity.AttrEntity;
import cn.brownqi.mall.product.service.AttrService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


/**
 * @author BrownQi
 */
@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {

    @Autowired
    private AttrAttrgroupRelationDao attrAttrgroupRelationDao;

    @Autowired
    private AttrGroupDao attrGroupDao;

    @Autowired
    private CategoryDao categoryDao;

    @Autowired
    private CategoryService categoryService;

    @Transactional
    @Override
    public void saveAttr(AttrVO attr) {
        AttrEntity attrEntity = new AttrEntity();
        // 将AttrVO中AttrEntity类拥有的数据封装到AttrEntity对象中
        BeanUtils.copyProperties(attr, attrEntity);
        // 1、保存基本数据
        this.save(attrEntity);
        // 2、保存关联关系
        if (attr.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode() && attr.getAttrGroupId() != null) {
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
            relationEntity.setAttrGroupId(attr.getAttrGroupId());
            relationEntity.setAttrId(attrEntity.getAttrId());
            attrAttrgroupRelationDao.insert(relationEntity);
        }
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                new QueryWrapper<AttrEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryBaseAttrPage(Map<String, Object> params, Long catelogId, String type) {
        QueryWrapper<AttrEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("attr_type", "base".equalsIgnoreCase(type) ? ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode() : ProductConstant.AttrEnum.ATTR_TYPE_SALE.getCode());

        if (catelogId != 0) {
            queryWrapper.eq("catelog_id", catelogId);
        }

        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            queryWrapper.and((wrapper) -> {
                wrapper.eq("attr_id", key).or().like("attr_name", key);
            });
        }

        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                queryWrapper
        );
        PageUtils pageUtils = new PageUtils(page);
        List<AttrEntity> records = page.getRecords();

        List<AttrRespVO> attrRespVOS = records.stream().map((attrEntity) -> {
            AttrRespVO attrRespVO = new AttrRespVO();
            BeanUtils.copyProperties(attrEntity, attrRespVO);

            // 1、设置分类和分组的名字
            if ("base".equalsIgnoreCase(type)) {
                AttrAttrgroupRelationEntity selectByAttrIdEntity = attrAttrgroupRelationDao.selectOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrEntity.getAttrId()));
                if (selectByAttrIdEntity != null && selectByAttrIdEntity.getAttrGroupId() != null) {
                    Long attrGroupId = selectByAttrIdEntity.getAttrGroupId();
                    AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrGroupId);
                    attrRespVO.setGroupName(attrGroupEntity.getAttrGroupName());
                }
            }

            CategoryEntity categoryEntity = categoryDao.selectById(attrEntity.getCatelogId());
            if (categoryEntity != null) {
                attrRespVO.setCatelogName(categoryEntity.getName());
            }
            return attrRespVO;
        }).collect(Collectors.toList());

        pageUtils.setList(attrRespVOS);
        return pageUtils;
    }

    @Override
    public AttrRespVO getAttrInfo(Long attrId) {
        AttrRespVO respVO = new AttrRespVO();
        AttrEntity attrEntity = this.getById(attrId);
        BeanUtils.copyProperties(attrEntity, respVO);

        if (attrEntity.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()) {
            // 设置分组信息
            AttrAttrgroupRelationEntity relationEntity =
                    attrAttrgroupRelationDao.selectOne(
                            new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrId));
            if (relationEntity != null) {
                respVO.setAttrGroupId(relationEntity.getAttrGroupId());
                AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(relationEntity.getAttrGroupId());
                if (attrGroupEntity != null) {
                    respVO.setGroupName(attrGroupEntity.getAttrGroupName());
                }
            }
        }

        // 设置分类信息
        Long catelogId = attrEntity.getCatelogId();
        Long[] catelogPath = categoryService.findCatelogPath(catelogId);
        if (catelogPath != null) {
            respVO.setCatelogPath(catelogPath);
        }

        CategoryEntity categoryEntity = categoryDao.selectById(catelogId);
        if (categoryEntity != null) {
            respVO.setCatelogName(categoryEntity.getName());
        }


        return respVO;
    }

    @Transactional
    @Override
    public void updateAttr(AttrVO attr) {
        // 基本修改
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attr, attrEntity);
        this.updateById(attrEntity);

        if (attrEntity.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()) {
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
            relationEntity.setAttrGroupId(attr.getAttrGroupId());
            relationEntity.setAttrId(attr.getAttrId());

            Integer count = attrAttrgroupRelationDao.selectCount(new UpdateWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attr.getAttrId()));
            if (count > 0) {
                // 修改分组关联
                attrAttrgroupRelationDao.update(relationEntity, new UpdateWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attr.getAttrId()));
            } else {
                attrAttrgroupRelationDao.insert(relationEntity);
            }
        }

    }

    /**
     * 根据分组id查找关联的所有基本属性
     *
     * @param attrgroupId
     * @return
     */
    @Override
    public List<AttrEntity> getRelationAttr(Long attrgroupId) {
        List<AttrAttrgroupRelationEntity> entities = attrAttrgroupRelationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id", attrgroupId));
        List<Long> attrIds = entities.stream().map((attr) -> {
            return attr.getAttrId();
        }).collect(Collectors.toList());

        if (attrIds == null || attrIds.size() == 0) {
            return null;
        }
        List<AttrEntity> attrEntities = this.listByIds(attrIds);
        return attrEntities;
    }

    @Transactional
    @Override
    public void deleteRelation(AttrGroupRelationVO[] vos) {
        // 批量删除
        List<AttrAttrgroupRelationEntity> entities = Arrays.asList(vos).stream().map((vo) -> {
            AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrgroupRelationEntity();
            BeanUtils.copyProperties(vo, attrAttrgroupRelationEntity);
            return attrAttrgroupRelationEntity;
        }).collect(Collectors.toList());
        attrAttrgroupRelationDao.deleteBatchRelation(entities);
    }

    /**
     * 获取当前分组没有关联的所有属性
     *
     * @param params
     * @param attrgroupId
     * @return
     */
    @Override
    public PageUtils getNoRelationAttr(Map<String, Object> params, Long attrgroupId) {
        //1、当前分组只能关联自己所属的分类里面的所有属性
        AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrgroupId);
        Long catelogId = attrGroupEntity.getCatelogId();
        //2、当前分组只能关联别的分组没有引用的属性
        //2.1)、当前分类下的其他分组
        List<AttrGroupEntity> group = attrGroupDao.selectList(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));
        List<Long> collect = group.stream().map(item -> {
            return item.getAttrGroupId();
        }).collect(Collectors.toList());

        //2.2)、这些分组关联的属性
        List<AttrAttrgroupRelationEntity> groupId = attrAttrgroupRelationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().in("attr_group_id", collect));
        List<Long> attrIds = groupId.stream().map(item -> {
            return item.getAttrId();
        }).collect(Collectors.toList());

        //2.3)、从当前分类的所有属性中移除这些属性；
        QueryWrapper<AttrEntity> wrapper = new QueryWrapper<AttrEntity>().eq("catelog_id", catelogId).eq("attr_type", ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode());
        if (attrIds != null && attrIds.size() > 0) {
            wrapper.notIn("attr_id", attrIds);
        }
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            wrapper.and((w) -> {
                w.eq("attr_id", key).or().like("attr_name", key);
            });
        }
        IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(params), wrapper);

        PageUtils pageUtils = new PageUtils(page);

        return pageUtils;
    }

    @Override
    public List<Long> selectSearchAttrIds(List<Long> attrIds) {

        return this.baseMapper.selectSearchAttrIds(attrIds);

    }

}