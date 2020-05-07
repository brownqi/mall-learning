package cn.brownqi.mall.coupon.dao;

import cn.brownqi.mall.coupon.entity.CouponEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券信息
 * 
 * @author brownqi
 * @email brownqi@foxmail.com
 * @date 2020-04-22 22:34:20
 */
@Mapper
public interface CouponDao extends BaseMapper<CouponEntity> {
	
}
