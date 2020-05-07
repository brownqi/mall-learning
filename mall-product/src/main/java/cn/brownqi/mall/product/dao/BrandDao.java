package cn.brownqi.mall.product.dao;

import cn.brownqi.mall.product.entity.BrandEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * 品牌
 * 
 * @author brownqi
 * @email brownqi@foxmail.com
 * @date 2020-04-21 00:40:58
 */
@Mapper
@Repository
public interface BrandDao extends BaseMapper<BrandEntity> {
	
}
