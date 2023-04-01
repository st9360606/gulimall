package com.atguigu.gulimall.member.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.common.utils.HttpUtils;
import com.atguigu.gulimall.member.dao.MemberLevelDao;
import com.atguigu.gulimall.member.entity.MemberLevelEntity;
import com.atguigu.gulimall.member.exception.PhoneExistException;
import com.atguigu.gulimall.member.exception.UsernameExistException;
import com.atguigu.gulimall.member.service.MemberLevelService;
import com.atguigu.gulimall.member.vo.GiteeUser;
import com.atguigu.gulimall.member.vo.MemberLoginVo;
import com.atguigu.gulimall.member.vo.MemberRegistVo;
import com.atguigu.gulimall.member.vo.SocialUser;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.member.dao.MemberDao;
import com.atguigu.gulimall.member.entity.MemberEntity;
import com.atguigu.gulimall.member.service.MemberService;
import com.atguigu.gulimall.member.dao.MemberLevelDao;

@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {
    @Autowired
    MemberLevelDao memberLevelDao;

    @Autowired
    MemberLevelService memberLevelService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void regist(MemberRegistVo vo) {
        MemberDao memberDao = this.baseMapper;
        MemberEntity entity = new MemberEntity();

        //設置默認等級
        MemberEntity levelEntity = memberLevelDao.getDefaultLevel();
        entity.setLevelId(levelEntity.getId());

        //異常機制
        //檢查用戶名 和 手機號是否唯一 為了讓controller感知異常，異常機制
        checkPhoneUnique(vo.getPhone());
        checkUsernameUnique(vo.getUserName());

        entity.setUsername(vo.getUserName());
        entity.setMobile(vo.getPhone());

        //其他默認信息
        entity.setNickname(vo.getUserName());

        //密碼需要加密蹲存儲 MD5 密碼加密處理
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encode = passwordEncoder.encode(vo.getPassword());
        entity.setPassword(encode);

        //保存
        memberDao.insert(entity);
    }

    @Override
    public void checkPhoneUnique(String phone) throws PhoneExistException {
        Integer count = this.baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("mobile", phone));
        if (count > 0) {
            //說明數據庫有這個手機號，所以不能註冊
            throw new PhoneExistException();
        }
        //否則什麼都不做 檢查通過 業務繼續進行註冊
    }

    @Override
    public void checkUsernameUnique(String username) throws UsernameExistException {
        Integer count = this.baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("username", username));
        if (count > 0) {
            //說明數據庫有這個用戶名，所以不能註冊
            throw new UsernameExistException();
        }
        //否則什麼都不做 檢查通過 業務繼續進行註冊
    }

    @Override
    public MemberEntity login(MemberLoginVo vo) {
        String loginacct = vo.getLoginacct();
        String password = vo.getPassword();//123456

        //1 去數據庫查詢 根據登錄賬號查
        MemberEntity entity = this.baseMapper.selectOne(new QueryWrapper<MemberEntity>().eq("username", loginacct).or().eq("mobile", loginacct));
        if (entity == null) {
            //登錄失敗，數據庫沒有這個用戶
            return null;
        } else {
            //數據庫有這個用戶
            //1 獲取到數據庫中的password
            String passwordDb = entity.getPassword();
            //2 進行密碼比對
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            boolean matches = passwordEncoder.matches(password, passwordDb);
            if (matches) {
                //密碼比對成功，登錄成功
                return entity;
            } else {
                //用戶存在，密碼不對，登錄失敗
                return null;
            }
        }
    }

    @Override
    public MemberEntity login(SocialUser socialUser) {
        //登入和註冊合併邏輯
        String uid = socialUser.getUid();
        //1、判斷當前社交用戶是否已經登陸過系統
        MemberDao memberDao = this.baseMapper;
        MemberEntity memberEntity = memberDao.selectOne(new QueryWrapper<MemberEntity>().eq("social_uid", uid));
        if (memberEntity != null) {
            //這個用戶已經註冊
            MemberEntity update = new MemberEntity();
            update.setId(memberEntity.getId());
            update.setAccessToken(socialUser.getAccess_token());
            update.setExpiresIn(socialUser.getExpires_in());

            memberDao.updateById(update);
            //更新令牌、過期時間
            memberEntity.setAccessToken(socialUser.getAccess_token());
            memberEntity.setExpiresIn(socialUser.getExpires_in());
            return memberEntity;
        } else {
            //2、沒有查到當前社交用戶對應的紀錄我們就需要註冊一個
            System.out.println("weibo社交登錄中...首次登錄本站");
            MemberEntity regist = new MemberEntity();
            try {
                //3、查詢當前社交用戶的社交帳號信息(暱稱、性別等)
                HashMap<String, String> query = new HashMap<>();
                query.put("access_token", socialUser.getAccess_token());
                query.put("uid", socialUser.getUid());
                HttpResponse response = HttpUtils.doGet("https://api.weibo.com", "/2/users/show.json", "get", new HashMap<String, String>(), query);
                if (response.getStatusLine().getStatusCode() == 200) {
                    //查詢成功，取得json數據並轉換
                    String json = EntityUtils.toString(response.getEntity());
                    JSONObject jsonObject = JSON.parseObject(json);
                    String name = jsonObject.getString("name");//微博暱稱
                    String gender = jsonObject.getString("gender");//微博性別
                    //....
                    regist.setNickname(name);
                    regist.setGender("m".equals(gender) ? 1 : 0);
                    //....
                }
            } catch (Exception e) {
            }
            regist.setSocialUid(socialUser.getUid());//防止下一次登錄再次註冊
            regist.setAccessToken(socialUser.getAccess_token());
            regist.setExpiresIn(socialUser.getExpires_in());
            memberDao.insert(regist);
            return regist;
        }
    }

    @Override
    public MemberEntity login(GiteeUser giteeUser) throws Exception {
        Map<String, String> query = new HashMap<>();
        query.put("access_token", giteeUser.getAccessToken());
        HttpResponse response = HttpUtils.doGet("https://gitee.com", "/api/v5/user", "get", new HashMap<String, String>(), query);
        String json = EntityUtils.toString(response.getEntity());
        JSONObject jsonObject = JSON.parseObject(json);
        String id = jsonObject.getString("id");
        String name = jsonObject.getString("name");
        String gender = jsonObject.getString("gender");
        String profileImageUrl = jsonObject.getString("avatar_url");
        //具有登錄和註冊邏輯
        String uid = id;

        //1、判斷當前社交用戶是否已經登錄過系統
        MemberEntity memberEntity = baseMapper.selectOne(new QueryWrapper<MemberEntity>().eq("social_uid", uid));

        if (memberEntity != null) {
            //這個用戶已經註冊過
            //更新用戶的訪問令牌的時間和access_token
            MemberEntity update = new MemberEntity();
            update.setId(memberEntity.getId());
            update.setAccessToken(giteeUser.getAccessToken());
            update.setExpiresIn(giteeUser.getExpiresIn());
            baseMapper.updateById(update);

            memberEntity.setAccessToken(giteeUser.getAccessToken());
            memberEntity.setExpiresIn(giteeUser.getExpiresIn());
            return memberEntity;
        } else {
            //2、沒有查到當前社交用戶對應的記錄我們就需要註冊一個
            MemberEntity register = new MemberEntity();
            //3、查詢當前社交用戶的社交賬號信息（暱稱、性別等）
            // 遠程調用，不影響結果
            try {
//                Map<String, String> query = new HashMap<>();
//                query.put("access_token", socialUser.getAccessToken());
//                HttpResponse response = HttpUtils.doGet("https://gitee.com", "/api/v5/user", "get", new HashMap<String, String>(), query);

                if (response.getStatusLine().getStatusCode() == 200) {
                    //查詢成功
//                    String gender = jsonObject.getString("gender");
                    register.setUsername(name);
                    register.setNickname(name);
                    register.setCreateTime(new Date());
                    register.setGender("m".equals(gender) ? 1 : 0);
                    register.setHeader(profileImageUrl);
                }
            } catch (Exception e) {
            }
            register.setCreateTime(new Date());
            register.setSocialUid(uid);
            register.setAccessToken(giteeUser.getAccessToken());
            register.setExpiresIn(giteeUser.getExpiresIn());

            //把用戶信息插入到數據庫中
            baseMapper.insert(register);
            return register;
        }
    }
}
