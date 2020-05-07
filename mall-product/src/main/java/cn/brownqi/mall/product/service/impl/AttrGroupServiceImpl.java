package cn.brownqi.mall.product.service.impl;

import cn.brownqi.mall.product.entity.AttrEntity;
import cn.brownqi.mall.product.service.AttrService;
import cn.brownqi.mall.product.vo.AttrGroupWithAttrsVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.brownqi.common.utils.PageUtils;
import cn.brownqi.common.utils.Query;


import cn.brownqi.mall.product.dao.AttrGroupDao;
import cn.brownqi.mall.product.entity.AttrGroupEntity;
import cn.brownqi.mall.product.service.AttrGroupService;
import org.springframework.util.StringUtils;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Autowired
    private AttrService attrService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, Long catelogId) {

        String key = (String) params.get("key");
        // select * from pms_attr_group where catelog_id = ? and (attr_group_id = key or attr_group_name like %key%)
        QueryWrapper<AttrGroupEntity> wrapper = new QueryWrapper<AttrGroupEntity>();
        if (!StringUtils.isEmpty(key)){
            wrapper.and((obj)->{
                obj.eq("attr_group_id",key).or().like("attr_group_name",key);
            });
        }

        if (catelogId != 0) {
            wrapper.eq("catelog_id",catelogId);
        }
        IPage<AttrGroupEntity> page =
                this.page(
                        new Query<AttrGroupEntity>().getPage(params),
                        wrapper);
        return new PageUtils(page);
    }

    /**
     * 根据分类id查出所有的分组以及这些组里面的属性
     * @param catelogId
     * @return
     */
    @Override
    public List<AttrGroupWithAttrsVO> getAttrGroupWithAttrsByCatelogId(Long catelogId) {
        //1、查询分组信息
        List<AttrGroupEntity> attrGroupEntities = this.list(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));

        //2、查询所有属性
        List<AttrGroupWithAttrsVO> collect = attrGroupEntities.stream().map(group -> {
            AttrGroupWithAttrsVO attrsVO = new AttrGroupWithAttrsVO();
            BeanUtils.copyProperties(group,attrsVO);
            List<AttrEntity> attrs = attrService.getRelationAttr(attrsVO.getAttrGroupId());
            attrsVO.setAttrs(attrs);
            return attrsVO;
        }).collect(Collectors.toList());
        return collect;
    }

}

//        if (catelogId == 0) {
//            IPage<AttrGroupEntity> page =
//                    this.page(
//                            new Query<AttrGroupEntity>().getPage(params),
//                            wrapper);
//            return new PageUtils(page);
//        } else {
//            wrapper.eq("catelog_id",catelogId);
//            IPage<AttrGroupEntity> page =
//                    this.page(
//                            new Query<AttrGroupEntity>().getPage(params),
//                            wrapper);
//            return new PageUtils(page);
//        }