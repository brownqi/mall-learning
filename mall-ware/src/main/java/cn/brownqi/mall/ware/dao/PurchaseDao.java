package cn.brownqi.mall.ware.dao;

import cn.brownqi.mall.ware.entity.PurchaseEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 采购信息
 * 
 * @author brownqi
 * @email brownqi@foxmail.com
 * @date 2020-04-22 23:06:17
 */
@Mapper
public interface PurchaseDao extends BaseMapper<PurchaseEntity> {
	
}
