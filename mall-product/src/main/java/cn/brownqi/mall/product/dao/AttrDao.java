package cn.brownqi.mall.product.dao;

import cn.brownqi.mall.product.entity.AttrEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品属性
 * 
 * @author brownqi
 * @email brownqi@foxmail.com
 * @date 2020-04-21 00:40:58
 */
@Mapper
public interface AttrDao extends BaseMapper<AttrEntity> {

    /** TODO 优化：将查询语句 in (?,?...,?) 改为 between min and max ，mysql中需要添加 attr_id 索引 ，java代码需要将 attrIds 查出最大最小值
     * select attr_id from mall_pms.pms_attr where attr_id in (?,?...,?) and search_type = 1
     * @param attrIds
     * @return
     */
    List<Long> selectSearchAttrIds(@Param("attrIds") List<Long> attrIds);
}
