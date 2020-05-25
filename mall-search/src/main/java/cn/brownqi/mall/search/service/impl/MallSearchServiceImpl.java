package cn.brownqi.mall.search.service.impl;

import cn.brownqi.common.to.es.SkuEsModel;
import cn.brownqi.mall.search.config.MallElasitcSearchConfig;
import cn.brownqi.mall.search.constant.EsConstant;
import cn.brownqi.mall.search.service.MallSearchService;
import cn.brownqi.mall.search.vo.SearchParamVO;
import cn.brownqi.mall.search.vo.SearchResponseVO;
import com.alibaba.fastjson.JSON;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MallSearchServiceImpl implements MallSearchService {

    @Autowired
    private RestHighLevelClient esRestClient;

    @Override
    public SearchResponseVO search(SearchParamVO param) {
        // 1、动态构建出查询需要的DSL语句
        SearchResponseVO responseVO = null;

        // 1、准备检索请求
        SearchRequest searchRequest = buildSearchRequest(param);

        try {
            // 2、执行检索请求
            SearchResponse response = esRestClient.search(searchRequest, MallElasitcSearchConfig.COMMON_OPTIONS);

            // 3、分析响应数据封装成我么你需要的格式
            responseVO = buildSearchResponseVO(response, param);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 准备检索请求
     * # 模糊匹配，过滤（按照属性、分类、品牌、价格区间、库存），排序，分页，高亮，聚合分析
     *
     * @return
     */
    private SearchRequest buildSearchRequest(SearchParamVO param) {

        // 用于构建 DSL 语句
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        /**
         * 所有查询条件
         * 模糊匹配，过滤（按照属性、分类、品牌、价格区间、库存）
         */
        // 1、构建 bool query
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        // 1.1 must 模糊匹配
        if (!StringUtils.isEmpty(param.getKeyword())) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("skuTitle", param.getKeyword()));
        }
        // 1.2.1 filter 按照三级分类
        if (param.getCatalog3Id() != null) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("catalogId", param.getCatalog3Id()));
        }
        // 1.2.2 filter 按照品牌 ID
        if (param.getBrandId() != null && param.getBrandId().size() > 0) {
            boolQueryBuilder.filter(QueryBuilders.termsQuery("brandId", param.getBrandId()));
        }
        // 1.2.3 filter 按照所有指定的属性进行查询
        if (param.getAttrs() != null && param.getAttrs().size() > 0) {

            for (String attrStr :
                    param.getAttrs()) {
                BoolQueryBuilder nestedBoolQuery = QueryBuilders.boolQuery();
                String[] splitAttrStr = attrStr.split("_");
                String attrId = splitAttrStr[0];    // 检索的属性id
                String[] attrValues = splitAttrStr[1].split(":");   // 属性检索的值
                nestedBoolQuery.must(QueryBuilders.termQuery("attrs.attrId", attrId));
                nestedBoolQuery.must(QueryBuilders.termsQuery("attrs.attrValue", attrValues));
                // 每一个必须都得生成一个 nested 查询
                NestedQueryBuilder nestedQuery = QueryBuilders.nestedQuery("attrs", nestedBoolQuery, ScoreMode.None);
                boolQueryBuilder.filter(nestedQuery);
            }

        }

        // 1.2.4 filter 按照是否有库存查询
        boolQueryBuilder.filter(QueryBuilders.termQuery("hasStock", param.getHasStock() == 1));
        // 1.2.5 filter 按照价格区间
        if (!StringUtils.isEmpty(param.getSkuPrice())) {
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("skuPrice");
            // 3000_5000/_5000/3000_
            String[] splitPrice = param.getSkuPrice().split("_");
            if (splitPrice.length == 2) {
                // 区间
                rangeQuery.gte(splitPrice[0]).lte(splitPrice[1]);
            } else if (splitPrice.length == 1) {
                if (param.getSkuPrice().startsWith("_")) {
                    rangeQuery.lte(splitPrice[0]);
                } else if (param.getSkuPrice().endsWith("_")) {
                    rangeQuery.gte(splitPrice[0]);
                }
            }
            boolQueryBuilder.filter(rangeQuery);
        }

        // 将所有条件都进行封装
        searchSourceBuilder.query(boolQueryBuilder);

        /**
         * 排序，分页，高亮
         */
        // 2.1 排序 sort=hotScore_asc/desc
        if (!StringUtils.isEmpty(param.getSort())) {
            String sort = param.getSort();
            String[] s = sort.split("_");
            SortOrder order = s[1].equalsIgnoreCase("asc") ? SortOrder.ASC : SortOrder.DESC;
            searchSourceBuilder.sort(s[0], order);
        }
        // 2.2 分页
        // from = (pageNum - 1) * size
        searchSourceBuilder.from((param.getPageNum() - 1) * EsConstant.PRODUCT_PAGESIZE);
        searchSourceBuilder.size(EsConstant.PRODUCT_PAGESIZE);

        // 2.3 高亮
        if (!StringUtils.isEmpty(param.getKeyword())) {
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("skuTitle");
            highlightBuilder.preTags("<b style='color:red'>");
            highlightBuilder.postTags("</b>");
            searchSourceBuilder.highlighter(highlightBuilder);
        }

        /**
         * 聚合分析
         */
        // 1、品牌聚合
        TermsAggregationBuilder brand_agg = AggregationBuilders.terms("brand_agg").field("brandId").size(50);
        // 1.1 品牌聚合的子聚合
        brand_agg.subAggregation(AggregationBuilders.terms("brand_name_agg").field("brandName").size(1));
        brand_agg.subAggregation(AggregationBuilders.terms("brand_img_agg").field("brandImg").size(1));
        searchSourceBuilder.aggregation(brand_agg);

        // 2、分类聚合
        TermsAggregationBuilder catalog_agg = AggregationBuilders.terms("catalog_agg").field("catalogId").size(20);
        catalog_agg.subAggregation(AggregationBuilders.terms("catalog_name_agg").field("catalogName").size(1));
        searchSourceBuilder.aggregation(catalog_agg);

        // 3、属性聚合
        NestedAggregationBuilder attr_agg = AggregationBuilders.nested("attr_agg", "attrs");
        // 聚合当前所有的attrId
        TermsAggregationBuilder attr_id_agg = AggregationBuilders.terms("attr_id_agg").field("attrs.attrId");
        // 聚合分析穿当前 attr_id 对应的名字
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_name_agg").field("attrs.attrName").size(1));
        // 聚合分析穿当前 attr_id 对应的所有可能的取值 attrValue
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_value_agg").field("attrs.attrValue").size(50));
        // 整合几种聚合
        attr_agg.subAggregation(attr_id_agg);
        searchSourceBuilder.aggregation(attr_agg);


        String s = searchSourceBuilder.toString();
        System.out.println("构建的 DSL :" + s);

        // 拼装 SearchRequest
        SearchRequest searchRequest = new SearchRequest(new String[]{EsConstant.PRODUCT_INDEX}, searchSourceBuilder);
        return searchRequest;
    }

    /**
     * 构建结果数据
     *
     * @return
     */
    private SearchResponseVO buildSearchResponseVO(SearchResponse response, SearchParamVO param) {
        SearchResponseVO responseVO = new SearchResponseVO();
        SearchHits hits = response.getHits();
        // 1、返回的所有查询到的商品
        List<SkuEsModel> skuEsModels = new ArrayList<>();
        if (hits.getHits() != null && hits.getHits().length > 0){
            for (SearchHit hit : hits.getHits()) {
                String sourceAsString = hit.getSourceAsString();
                SkuEsModel skuEsModel = JSON.parseObject(sourceAsString, SkuEsModel.class);
                skuEsModels.add(skuEsModel);
            }
        }
        responseVO.setProducts(skuEsModels);

        // 2、当前所有商品涉及到的所有属性信息
        List<SearchResponseVO.AttrVO> attrVOS = new ArrayList<>();
        ParsedNested attr_agg = response.getAggregations().get("attr_agg");
        ParsedLongTerms attr_id_agg = attr_agg.getAggregations().get("attr_id_agg");
        for (Terms.Bucket bucket : attr_id_agg.getBuckets()) {
            SearchResponseVO.AttrVO attrVO = new SearchResponseVO.AttrVO();
            // 1、得到属性的ID
            long attrId = bucket.getKeyAsNumber().longValue();
            // 2、得到属性的名字
            String attrName = ((ParsedStringTerms) bucket.getAggregations().get("attr_name_agg")).getBuckets().get(0).getKeyAsString();
            // 3、得到属性的所有值
            List<String> attrValues = ((ParsedStringTerms) bucket.getAggregations().get("attr_value_agg")).getBuckets().stream().map(item -> {
                String keyAsString = ((Terms.Bucket) item).getKeyAsString();
                return keyAsString;
            }).collect(Collectors.toList());

            attrVO.setAttrId(attrId);
            attrVO.setAttrName(attrName);
            attrVO.setAttrValue(attrValues);

            attrVOS.add(attrVO);
        }
//        responseVO.setAttrs();

        // 3、当前所有商品涉及到的所有品牌信息
        List<SearchResponseVO.BrandVO> brandVOS = new ArrayList<>();
        ParsedLongTerms brand_agg = response.getAggregations().get("brand_agg");
        for (Terms.Bucket bucket : brand_agg.getBuckets()) {
            SearchResponseVO.BrandVO brandVO = new SearchResponseVO.BrandVO();
            // 3.1 得到品牌的ID
            long brandId = bucket.getKeyAsNumber().longValue();
            // 3.2 得到品牌的名称
            String brandName = ((ParsedStringTerms) bucket.getAggregations().get("brand_name_agg")).getBuckets().get(0).getKeyAsString();
            // 3.3 得到品牌的图片
            String brandImg = ((ParsedStringTerms) bucket.getAggregations().get("brand_img_agg")).getBuckets().get(0).getKeyAsString();
            brandVO.setBrandId(brandId);
            brandVO.setBrandName(brandName);
            brandVO.setBrandImg(brandImg);
            brandVOS.add(brandVO);
        }

        responseVO.setBrands(brandVOS);

        // 4、当前所有商品涉及到的所有分类信息
        ParsedLongTerms catalog_agg = response.getAggregations().get("catalog_agg");
        List<SearchResponseVO.CatalogVO> catalogVOS = new ArrayList<>();
        List<? extends Terms.Bucket> buckets = catalog_agg.getBuckets();
        for (Terms.Bucket bucket : buckets) {
            SearchResponseVO.CatalogVO catalogVO = new SearchResponseVO.CatalogVO();
            // 得到分类ID
            String keyAsString = bucket.getKeyAsString();
            catalogVO.setCatalogId(Long.parseLong(keyAsString));

            // 得到分类名
            ParsedStringTerms catalog_name_agg = bucket.getAggregations().get("catalog_name_agg");
            String catalog_name = catalog_name_agg.getBuckets().get(0).getKeyAsString();
            catalogVO.setCatalogName(catalog_name);
            catalogVOS.add(catalogVO);
        }
        responseVO.setCatalogs(catalogVOS);

        // 5、分页信息-页码
        responseVO.setPageNum(param.getPageNum());
        // 5、分页信息-总记录数
        long total = hits.getTotalHits().value;
        responseVO.setTotal(total);
        // 5、分页信息-总页码
        int totalPages =
                (int) total % EsConstant.PRODUCT_PAGESIZE == 0 ?
                        ((int) total / EsConstant.PRODUCT_PAGESIZE) :
                        ((int) total / EsConstant.PRODUCT_PAGESIZE + 1);
        responseVO.setTotalPages(totalPages);

        return null;
    }

}
