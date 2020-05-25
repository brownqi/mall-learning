package cn.brownqi.mall.search.vo;

import cn.brownqi.common.to.es.SkuEsModel;
import lombok.Data;

import java.util.List;

@Data
public class SearchResponseVO {

    private List<SkuEsModel> products;  // 查询到的所有商品信息

    /**
     * 以下是分页信息
     */

    private Integer pageNum;    // 当前页码
    private Integer totalPages; // 总页码
    private Long total; // 总记录数

    private List<BrandVO> brands;   // 当前查询到的结果，所有涉及到的品牌
    private List<CatalogVO> catalogs;   // 当前查询到的结果，所有涉及到的所有分类
    private List<AttrVO> attrs;   // 当前查询到的


    @Data
    public static class BrandVO{
        private Long brandId;
        private String brandName;
        private String brandImg;
    }

    @Data
    public static class CatalogVO{
        private Long catalogId;
        private String catalogName;
    }

    @Data
    public static class AttrVO{
        private Long attrId;
        private String attrName;
        private List<String> attrValue;
    }

}
