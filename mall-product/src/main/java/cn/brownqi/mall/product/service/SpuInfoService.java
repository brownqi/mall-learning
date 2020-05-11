package cn.brownqi.mall.product.service;

import cn.brownqi.mall.product.vo.SpuSaveVO;
import com.baomidou.mybatisplus.extension.service.IService;
import cn.brownqi.common.utils.PageUtils;
import cn.brownqi.mall.product.entity.SpuInfoEntity;

import java.util.Map;

/**
 * spu信息
 *
 * @author brownqi
 * @email brownqi@foxmail.com
 * @date 2020-04-21 00:40:58
 */
public interface SpuInfoService extends IService<SpuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveSpuInfo(SpuSaveVO vo);

    void saveBaseSpuInfo(SpuInfoEntity infoEntity);

    PageUtils queryPageByCondition(Map<String, Object> params);

    /**
     * 商品上架
     * @param spuId
     */
    void up(Long spuId);
}

