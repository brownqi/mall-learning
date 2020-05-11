package cn.brownqi.mall.product.dao;

import cn.brownqi.mall.product.controller.CategoryController;
import cn.brownqi.mall.product.entity.CategoryEntity;
import cn.brownqi.mall.product.service.CategoryBrandRelationService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
class CategoryBrandRelationDaoTest {

    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;

    @Autowired
    private CategoryController categoryController;

    @Test
    void updateCategory() {
//        System.out.println("===");
//        categoryBrandRelationService.updateCategory(2L,"华为000");
//        System.out.println("===");
        System.out.println("===");
        CategoryEntity categoryEntity = new CategoryEntity();
        categoryEntity.setCatId(2L);
        categoryEntity.setName("华为");
        categoryController.update(categoryEntity);
        System.out.println("===");
    }
}