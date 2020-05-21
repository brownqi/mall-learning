package cn.brownqi.mall.product.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// 2级分类VO
@NoArgsConstructor
@AllArgsConstructor
@Data
public class CateLog2VO {
    private String catalog1Id; // 1级父分类id
    private List<Catelog3VO> catalog3List; // 三级子分类
    private String id;
    private String name;

    /**
     * 3级分类VO
     */
    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class Catelog3VO{
        private String catalog2Id;
        private String id;
        private String name;

    }

}
