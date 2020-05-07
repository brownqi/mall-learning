package cn.brownqi.mall.coupon.dao;

import cn.brownqi.mall.coupon.entity.CouponHistoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券领取历史记录
 * 
 * @author brownqi
 * @email brownqi@foxmail.com
 * @date 2020-04-22 22:34:19
 */
@Mapper
public interface CouponHistoryDao extends BaseMapper<CouponHistoryEntity> {
	
}
