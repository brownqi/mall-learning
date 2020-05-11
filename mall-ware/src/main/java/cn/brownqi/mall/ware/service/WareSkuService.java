package cn.brownqi.mall.ware.service;

import cn.brownqi.mall.ware.vo.SkuHasStockVO;
import com.baomidou.mybatisplus.extension.service.IService;
import cn.brownqi.common.utils.PageUtils;
import cn.brownqi.mall.ware.entity.WareSkuEntity;

import java.util.List;
import java.util.Map;

/**
 * 商品库存
 *
 * @author brownqi
 * @email brownqi@foxmail.com
 * @date 2020-04-22 23:06:17
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void addStock(Long skuId, Long wareId, Integer skuNum);

    List<SkuHasStockVO> getSkuHasStock(List<Long> skuIds);
}

