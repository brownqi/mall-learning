package cn.brownqi.mall.member.dao;

import cn.brownqi.mall.member.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 * 
 * @author brownqi
 * @email brownqi@foxmail.com
 * @date 2020-04-22 22:45:13
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {
	
}
