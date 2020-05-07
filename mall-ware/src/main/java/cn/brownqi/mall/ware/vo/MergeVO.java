package cn.brownqi.mall.ware.vo;

import lombok.Data;

import java.util.List;

@Data
public class MergeVO {
    //整单id
    private Long purchaseId;
    //合并项集合
    private List<Long> items;
}
