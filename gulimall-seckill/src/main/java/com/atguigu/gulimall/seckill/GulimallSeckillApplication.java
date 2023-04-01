package com.atguigu.gulimall.seckill;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;


/**
 * 秒殺（高並發）系統關注的問題
 *
 * 01服務單一職責+獨立部署
 *      秒殺服務即使自己扛不住壓力，掛掉。不要影別人
 *
 * 02秒殺鏈接加密
 *      防止惡意攻擊，模擬秒殺請求。 1000次/s攻擊。防止鏈接暴露，自己工作人員，提前秒殺商品
 *
 * 03庫存預熱+快速扣減
 *
 * 04動靜分離
 *
 * 05惡意請求攔截
 *
 * 06流量錯峰 (驗證碼、加入購物車)
 *
 * 07限流、熔斷、降級
 *
 * 08隊列削峰 : 秒殺成功的請求，進入(MQ隊列)快速創建訂單
 *
 * 1 整合Sentinel
 *  1) 引入依賴spring-cloud-starter-alibaba-sentinel
 *  2) 下載sentinel控制台 sentinel-dashboard-1.6.3.jar
 *  3) 配置sentinel控制台地址信息
 *  4) 在控制台調整參數 [默認所有的留空設置保存在內存,重啟服務就會失效]
 *
 * 2 每一個微服務都引入統計審計信息spring-boot-starter-actuator
 *     並配置management.endpoints.web.exposure.include=*
 *
 * 3 自定義sentinel流控返回數據
 *
 * 4 流控模式&效果 全服務引入
 *
 * 5 使用Sentinel來保護Feign遠程調用：熔斷機制
 *     1) 調用方的熔斷保護 feign.sentinel.enabled=true
 *     2) 調用方手動指定遠程服務的降級策略。遠程服務被降級處理，就會觸發我們的熔斷回調方法 fallback @FeignClient(value = "gulimall-seckill",fallback = SeckillFeignServiceFallBack.class)
 *     3) 超大流量的時候，必須犧牲一些遠程服務。在服務的提供方（遠程服務）來指定降級策略，
 *         提供方是在運行中的，但是他不運行自己的業務邏輯，然後返回的是默認的降級數據（限流的數據）
 *
 * 6 自定義受保護的資源
 *    1) try(Entry entry = SphU.entry("seckillSkus")) {
 *        ///業務邏輯
 *     } catch (BlockException e) {
 *        log.error("資源被限流,{}",e.getMessage());
 *     }
 *
 *    2）基於註解
 *       @SentinelResource(value = "getCurrentSeckillSkusResource", blockHandler = "blockHandler")
 *    無論是 1 或 2 的方式一定要配置被限流以後的默認返回
 *     url請求可以設置統一返回:webCallbackManager
 */



//@EnableRabbit
@EnableFeignClients
@EnableDiscoveryClient
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class GulimallSeckillApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallSeckillApplication.class, args);
    }

}
