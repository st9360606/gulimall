package com.atguigu.gulimall.member.controller;

import java.util.Arrays;
import java.util.Map;


import com.atguigu.common.exception.BizCodeEnum;
import com.atguigu.gulimall.member.exception.PhoneExistException;
import com.atguigu.gulimall.member.exception.UsernameExistException;
import com.atguigu.gulimall.member.feign.CouponFeignService;
import com.atguigu.gulimall.member.vo.GiteeUser;
import com.atguigu.gulimall.member.vo.MemberLoginVo;
import com.atguigu.gulimall.member.vo.MemberRegistVo;
import com.atguigu.gulimall.member.vo.SocialUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.atguigu.gulimall.member.entity.MemberEntity;
import com.atguigu.gulimall.member.service.MemberService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;


/**
 * 会员
 *
 * @author kurt
 * @email st9360606@gmail.com
 * @date 2023-01-23 21:52:46
 */
@RestController
@RequestMapping("member/member")
public class MemberController {
    @Autowired
    private MemberService memberService;

    @Autowired
    CouponFeignService couponFeignService;

    //微博登入
    @PostMapping("/oauth2/weibo/login")
    public R oauthLogin(@RequestBody SocialUser socialUser) throws Exception {

        MemberEntity entity = memberService.login(socialUser);
        if (entity != null) {
            //TODO 1、登入成功處理
            return R.ok().setData(entity);
        } else {
            return R.error(BizCodeEnum.LOGIN_ACCOUNT_OR_PASSWORD_EXCEPTION.getCode(), BizCodeEnum.LOGIN_ACCOUNT_OR_PASSWORD_EXCEPTION.getMessage());
        }

    }

    //Gitee 登入
    @PostMapping("/oauth2/login")
    public R oauthLogin_Gitee(@RequestBody GiteeUser giteeUser) throws Exception {

        MemberEntity entity = memberService.login(giteeUser);
        if (entity != null) {
            //TODO 1、登入成功處理
            return R.ok().setData(entity);
        } else {
            return R.error(BizCodeEnum.LOGIN_ACCOUNT_OR_PASSWORD_EXCEPTION.getCode(), BizCodeEnum.LOGIN_ACCOUNT_OR_PASSWORD_EXCEPTION.getMessage());
        }

    }


    @RequestMapping("/coupons")
    public R test() {
        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setNickname("張三");

        //調用遠程服務查詢
        R membercoupons = couponFeignService.membercoupons();
        return R.ok().put("member", memberEntity).put("coupons", membercoupons.get("coupons"));
    }

    @PostMapping("/login")
    public R login(@RequestBody MemberLoginVo vo) {

        MemberEntity entity = memberService.login(vo);
        if (entity != null) {
            return R.ok().setData(entity);
        } else {
            return R.error(BizCodeEnum.LOGIN_ACCOUNT_OR_PASSWORD_EXCEPTION.getCode(), BizCodeEnum.LOGIN_ACCOUNT_OR_PASSWORD_EXCEPTION.getMessage());
        }

    }

    @PostMapping("/regist")
    public R regist(@RequestBody MemberRegistVo vo) {
        //嘗試註冊
        try {
            memberService.regist(vo);
        } catch (PhoneExistException e) {
            //捕獲了異常 返回失敗信息
            return R.error(BizCodeEnum.PHONE_REGISTERED_EXCEPTION.getCode(), BizCodeEnum.PHONE_REGISTERED_EXCEPTION.getMessage());
        } catch (UsernameExistException e) {
            return R.error(BizCodeEnum.USER_EXISTED_EXCEPTION.getCode(), BizCodeEnum.USER_EXISTED_EXCEPTION.getMessage());
        }

        //成功
        return R.ok();
    }


    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("member:member:list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = memberService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("member:member:info")
    public R info(@PathVariable("id") Long id) {
        MemberEntity member = memberService.getById(id);

        return R.ok().put("member", member);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("member:member:save")
    public R save(@RequestBody MemberEntity member) {
        memberService.save(member);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("member:member:update")
    public R update(@RequestBody MemberEntity member) {
        memberService.updateById(member);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("member:member:delete")
    public R delete(@RequestBody Long[] ids) {
        memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
