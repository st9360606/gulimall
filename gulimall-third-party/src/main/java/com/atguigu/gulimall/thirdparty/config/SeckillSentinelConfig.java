package com.atguigu.gulimall.thirdparty.config;

import com.alibaba.csp.sentinel.adapter.servlet.callback.UrlBlockHandler;
import com.alibaba.csp.sentinel.adapter.servlet.callback.WebCallbackManager;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.fastjson.JSON;
import com.atguigu.common.exception.BizCodeEnum;
import com.atguigu.common.utils.R;
import org.springframework.context.annotation.Configuration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * ClassName: SeckillSentinelConfig
 * Package: com.atguigu.gulimall.seckill.config
 * Description:
 *
 * @Author kurt
 * @Create 2023/3/24 下午 01:20
 * @Version 1.0
 */
@Configuration
public class SeckillSentinelConfig {

    //構造器
    public SeckillSentinelConfig(){
        WebCallbackManager.setUrlBlockHandler(new UrlBlockHandler() {
            @Override
            public void blocked(HttpServletRequest request, HttpServletResponse response, BlockException e) throws IOException {
                R error = R.error(BizCodeEnum.TOO_MANY_REQUEST.getCode(), BizCodeEnum.TOO_MANY_REQUEST.getMessage());
                //解决response乱码
                response.setCharacterEncoding("UTF-8");
                response.setContentType("application/json");
                response.getWriter().write(JSON.toJSONString(error));
            }
        });
    }
}
