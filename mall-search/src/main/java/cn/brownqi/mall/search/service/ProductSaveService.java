package cn.brownqi.mall.search.service;

import cn.brownqi.common.to.es.SkuEsModel;

import java.io.IOException;
import java.util.List;

public interface ProductSaveService {
    boolean productStatusUp(List<SkuEsModel> skuEsModels) throws IOException;
}
