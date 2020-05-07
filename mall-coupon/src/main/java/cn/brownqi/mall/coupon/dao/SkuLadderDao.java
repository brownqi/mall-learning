package cn.brownqi.mall.coupon.dao;

import cn.brownqi.mall.coupon.entity.SkuLadderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品阶梯价格
 * 
 * @author brownqi
 * @email brownqi@foxmail.com
 * @date 2020-04-22 22:34:19
 */
@Mapper
public interface SkuLadderDao extends BaseMapper<SkuLadderEntity> {
	
}
