package cn.brownqi.mall.product.feign;

import cn.brownqi.common.to.SkuHasStockVO;
import cn.brownqi.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient("mall-ware")
public interface WareFeignService {
    @PostMapping("/ware/waresku/hasstock")
    public R getSkuHasStock(@RequestBody List<Long> skuIds);
}
