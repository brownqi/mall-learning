package cn.brownqi.mall.product.service;

import cn.brownqi.mall.product.vo.AttrGroupRelationVO;
import cn.brownqi.mall.product.vo.AttrRespVO;
import cn.brownqi.mall.product.vo.AttrVO;
import com.baomidou.mybatisplus.extension.service.IService;
import cn.brownqi.common.utils.PageUtils;
import cn.brownqi.mall.product.entity.AttrEntity;

import java.util.List;
import java.util.Map;

/**
 * 商品属性
 *
 * @author brownqi
 * @email brownqi@foxmail.com
 * @date 2020-04-21 00:40:58
 */
public interface AttrService extends IService<AttrEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveAttr(AttrVO attr);

    PageUtils queryBaseAttrPage(Map<String, Object> params, Long catelogId, String type);

    AttrRespVO getAttrInfo(Long attrId);

    void updateAttr(AttrVO attr);

    List<AttrEntity> getRelationAttr(Long attrgroupId);

    void deleteRelation(AttrGroupRelationVO[] vos);

    PageUtils getNoRelationAttr(Map<String, Object> params, Long attrgroupId);

    /**
     * 在指定的所有属性集合里面，跳出检索属性
     * @param attrIds
     * @return
     */
    List<Long> selectSearchAttrIds(List<Long> attrIds);
}

