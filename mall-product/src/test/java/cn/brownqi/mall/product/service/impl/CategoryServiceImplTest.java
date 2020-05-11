package cn.brownqi.mall.product.service.impl;

import cn.brownqi.mall.product.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
class CategoryServiceImplTest {
    @Autowired
    private CategoryService categoryService;

    @Test
    void findCatelogPath() {
        Long[] catelogPath = categoryService.findCatelogPath(225L);
        log.info("完整路径：{}", Arrays.asList(catelogPath));
    }
}