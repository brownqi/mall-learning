package cn.brownqi.mall.search.controller;

import cn.brownqi.common.exception.BizCodeEnume;
import cn.brownqi.common.to.es.SkuEsModel;
import cn.brownqi.common.utils.R;
import cn.brownqi.mall.search.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@Slf4j
@RequestMapping("/search")
@RestController
public class ElasticSaveController {

    @Autowired
    ProductSaveService productSaveService;

    // 上架商品
    @PostMapping("/save/product")
    public R productStatusUp(@RequestBody List<SkuEsModel> skuEsModels){

        // TODO ?
        boolean b = true;
        try {
            b = productSaveService.productStatusUp(skuEsModels);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("ElasticSaveController商品上架错误：{}",e);
            return R.error(BizCodeEnume.PRODUCT_UP_EXCEPTION.getCode(),BizCodeEnume.PRODUCT_UP_EXCEPTION.getMsg());
        }

        if (!b){
            return R.ok();
        }else{
            return R.error(BizCodeEnume.PRODUCT_UP_EXCEPTION.getCode(),BizCodeEnume.PRODUCT_UP_EXCEPTION.getMsg());
        }


    }

}
