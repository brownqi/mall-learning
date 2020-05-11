package cn.brownqi.mall.ware.dao;

import cn.brownqi.mall.ware.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * 商品库存
 * 
 * @author brownqi
 * @email brownqi@foxmail.com
 * @date 2020-04-22 23:06:17
 */
@Mapper
@Repository
public interface WareSkuDao extends BaseMapper<WareSkuEntity> {

    void addStock(@Param("skuId") Long skuId, @Param("wareId") Long wareId, @Param("skuNum") Integer skuNum);

    Long getSkuStock(@Param("skuId") Long skuId);
}
