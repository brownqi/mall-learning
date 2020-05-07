package cn.brownqi.mall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import cn.brownqi.common.utils.PageUtils;
import cn.brownqi.mall.member.entity.MemberLoginLogEntity;

import java.util.Map;

/**
 * 会员登录记录
 *
 * @author brownqi
 * @email brownqi@foxmail.com
 * @date 2020-04-22 22:45:13
 */
public interface MemberLoginLogService extends IService<MemberLoginLogEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

