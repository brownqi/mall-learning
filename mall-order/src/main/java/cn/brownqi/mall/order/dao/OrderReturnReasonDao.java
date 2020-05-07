package cn.brownqi.mall.order.dao;

import cn.brownqi.mall.order.entity.OrderReturnReasonEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 退货原因
 * 
 * @author brownqi
 * @email brownqi@foxmail.com
 * @date 2020-04-22 23:00:39
 */
@Mapper
public interface OrderReturnReasonDao extends BaseMapper<OrderReturnReasonEntity> {
	
}
