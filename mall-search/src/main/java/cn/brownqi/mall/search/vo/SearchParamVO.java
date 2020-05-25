package cn.brownqi.mall.search.vo;

import lombok.Data;

import java.util.List;

/**
 * 封装页面所有可能传递过来的查询条件
 * keyword=
 * catalog3Id=
 */
@Data
public class SearchParamVO {
    private String keyword; // 页面传递过来的全文匹配关键字
    private Long catalog3Id;    // 三级分类ID


    /**
     * sort= [saleCount_asc/desc;skuPrice_asc/desc;hotScore_asc/desc]
     */
    private String sort;    // 排序条件

    /**
     * 过滤条件
     * hasStock（是否有货）、skuPrice、brandId、catalog3Id、attrs
     * hasStock=0/1
     * skuPrice=3000_5000/_5000/5000_
     * brandId=
     * attrs=2_5寸:6寸
     */
    private Integer hasStock = 1;   // 是否只显示有货 0 无库存 ，1 有库存
    private String skuPrice;    // 价格区间查询
    private List<Long> brandId; // 品牌Id可多选
    private List<String> attrs; // 按照属性进行筛选
    private Integer pageNum = 1;    // 页码
}
