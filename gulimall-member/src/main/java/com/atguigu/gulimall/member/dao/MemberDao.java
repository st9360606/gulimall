package com.atguigu.gulimall.member.dao;

import com.atguigu.gulimall.member.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 * 
 * @author kurt
 * @email st9360606@gmail.com
 * @date 2023-01-23 21:52:46
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {
	
}
