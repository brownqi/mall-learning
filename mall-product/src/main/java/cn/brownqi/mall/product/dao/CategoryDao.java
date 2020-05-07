package cn.brownqi.mall.product.dao;

import cn.brownqi.mall.product.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * 商品三级分类
 * 
 * @author brownqi
 * @email brownqi@foxmail.com
 * @date 2020-04-21 00:40:57
 */
@Mapper
@Repository
public interface CategoryDao extends BaseMapper<CategoryEntity> {
	
}
