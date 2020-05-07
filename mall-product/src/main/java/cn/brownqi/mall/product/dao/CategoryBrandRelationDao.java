package cn.brownqi.mall.product.dao;

import cn.brownqi.mall.product.entity.CategoryBrandRelationEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * 品牌分类关联
 * 
 * @author brownqi
 * @email brownqi@foxmail.com
 * @date 2020-04-21 00:40:57
 */
@Mapper
@Repository
public interface CategoryBrandRelationDao extends BaseMapper<CategoryBrandRelationEntity> {

    /**
     * 更新分类品牌关联表
     * @param catId 分类Id
     * @param name 分类名称
     */
    void updateCategory(@Param("catId") Long catId, @Param("name") String name);
}
