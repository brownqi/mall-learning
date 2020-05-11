package cn.brownqi.mall.search.service.impl;

import cn.brownqi.common.to.es.SkuEsModel;
import cn.brownqi.mall.search.config.MallElasitcSearchConfig;
import cn.brownqi.mall.search.constant.EsConstant;
import cn.brownqi.mall.search.service.ProductSaveService;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ProductSaveServiceImpl implements ProductSaveService {

    @Autowired
    RestHighLevelClient restHighLevelClient;

    @Override
    public boolean productStatusUp(List<SkuEsModel> skuEsModels) throws IOException {
        // 保存到es
        // 1、给es中建立索引。product，建立好映射关系

        // 2、给es中保存这些数据
        BulkRequest bulkRequest = new BulkRequest();
        for (SkuEsModel skuEsModel : skuEsModels) {
            // 1、构造保存请求
            IndexRequest indexRequest = new IndexRequest(EsConstant.PRODUCT_INDEX);
            indexRequest.id(skuEsModel.getSkuId().toString());
            indexRequest.source(JSON.toJSONString(skuEsModel), XContentType.JSON);

            bulkRequest.add(indexRequest);
        }
        BulkResponse bulk = restHighLevelClient.bulk(bulkRequest, MallElasitcSearchConfig.COMMON_OPTIONS);

        // TODO 1、如果发生错误处理错误
        boolean hasFailures = bulk.hasFailures();
        List<String> collect = Arrays.stream(bulk.getItems()).map(item -> {
            return item.getId();
        }).collect(Collectors.toList());
        log.info("商品上架完成：{}，返回数据：{}", collect, bulk.toString());

        // TODO 2、如果上架错误重试

        return hasFailures;
    }
}
