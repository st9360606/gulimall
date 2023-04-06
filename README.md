# 谷粒商城

![](https://img.shields.io/badge/building-passing-green.svg)![GitHub](https://img.shields.io/badge/license-MIT-yellow.svg)![jdk](https://img.shields.io/static/v1?label=oraclejdk&message=8&color=blue)

## 項目介紹

谷粒商城項目是一套電商項目，包括前台商城系統以及後台管理系統，基於 SpringBoot、SpringCloud Alibaba、MyBatis Plus實現。前台商城系統包括：用戶登錄、註冊、商品搜索、商品詳情、購物車、訂單、秒殺活動等模塊。後台管理系統包括：系統管理、商品系統、優惠營銷、庫存系統、訂單系統、用戶系統、內容管理等七大模塊。

## 項目資源

- 接口文檔：https://easydoc.net/s/78237135/ZUqEdvA4/HqQGp9TI

## 組織結構

```
gulimall
├── gulimall-common -- 工具類及通用代碼
├── renren-generator -- 人人開源項目的代碼生成器
├── gulimall-auth-server -- 認證中心（社交登錄、OAuth2.0）、微博、Gitee登入
├── gulimall-cart -- 購物車服務
├── gulimall-coupon -- 優惠券服務
├── gulimall-gateway -- 統一配置網關
├── gulimall-order -- 訂單服務(Alipay-pay 沙盒環境)
├── gulimall-product -- 商品服務
├── gulimall-search -- 檢索服務(ElasticSearch、Kibana)
├── gulimall-seckill -- 秒殺服務(RabbitMQ)
├── gulimall-third-party -- 第三方服務（對象oss存儲、sms短信）
├── gulimall-ware -- 倉儲服務
└── gulimall-member -- 會員服務
```

## 技術選型

### 後端技術

|        技術        |           說明           |                      官網                       |
| :----------------: | :----------------------: | :---------------------------------------------: |
|     SpringBoot     |       容器+MVC框架       |     https://spring.io/projects/spring-boot      |
|    SpringCloud     |        微服務架構        |     https://spring.io/projects/spring-cloud     |
|   SpringSession    |        分佈式緩存        |    https://projects.spring.io/spring-session    |
|   SpringSecurity   |         安全框架        |  https://projects.spring.io/spring-security     |
|   Spring-Amqp      |        消息傳遞框架      |    https://projects.spring.io/spring-amqp       |
| SpringCloudAlibaba |        一系列組件        | https://spring.io/projects/spring-cloud-alibaba |
|    MyBatis-Plus    |         ORM框架         |             https://mp.baomidou.com             |
|      Lombok        |      簡化對象封裝工具     |      https://github.com/projectlombok/lombok    |
|  renren-generator  |   人人開源項目的代碼生成器  |   https://gitee.com/renrenio/renren-generator   |
|   Elasticsearch    |         搜索引擎         |    https://github.com/elastic/elasticsearch     |
|      RabbitMQ      |         消息隊列         |            https://www.rabbitmq.com             |
|      Redisson      |         分佈式鎖         |      https://github.com/redisson/redisson       |
|       Docker       |       應用容器引擎       |             https://www.docker.com              |
|        OSS         |        對象雲存儲        |  https://github.com/aliyun/aliyun-oss-java-sdk  |
|      Kubernetes    |       容器化應用程式      |            https://kubernetes.io/               |

### 前端技術

|   技術    |    說明    |           官網            |
| :-------: | :--------: | :-----------------------: |
|    Vue    |  前端框架  |     https://vuejs.org     |
|  Element  | 前端UI框架 | https://element.eleme.io  |
| thymeleaf |  模板引擎  | https://www.thymeleaf.org |
|  node.js  | 服務端的js |   https://nodejs.org/en   |

## 架構圖

### 系統架構圖

![](https://s2.loli.net/2023/04/06/yOFpboSCLhE52fk.png)

### 業務架構圖

![](https://s2.loli.net/2023/04/06/aIlcs7LEjxmPMBR.png)

## 環境搭建

### 開發工具

|     工具      |        說明         |                      官網                       |
| :-----------: | :-----------------: | :---------------------------------------------: |
|    Intellij   |    開發Java程序     |     https://www.jetbrains.com/idea/download     |
|Visual Studio Code|    前端開發編譯器 |     https://code.visualstudio.com/              |
| RedisDesktop  | redis客戶端連接工具  |        https://redisdesktop.com/download        |
|  SwitchHosts  |    本地host管理     |       https://oldj.github.io/SwitchHosts        |
|    X-shell    |  Linux遠程連接工具   |       https://www.xshellcn.com/xshell.html      |
|    Xftp 7     |  Linux遠程文件傳輸   |       https://www.xshellcn.com/xftp.html        |
|    Navicat    |   數據庫連接工具     |       http://www.formysql.com/xiazai.html        |
|    Postman    |   API接口調試工具   |        https://www.postman.com                    |
|    Jmeter     |    性能壓測工具     |        https://jmeter.apache.org                  |
|    VisualVM   |   JDK可視化監控工具 |        https://visualvm.github.io/                |
|    Vargrant   |   虛擬機管理工具    |        https://www.vagrantup.com/                 |
|Oracle VM VirtualBox| 虛擬機器軟體   |        https://www.virtualbox.org/                |
|      Git      |      版控工具      |        https://git-scm.com/                       |
|    HackMD     |   Markdown編輯器   |        https://hackmd.io                          |
|    Kubesphere |   可視化容器管理平台 |        https://kubesphere.io/                     |
|    Sonarqube  |   開源程式碼檢測平台 |   https://www.sonarsource.com/products/sonarqube/ |
|    Jenkins    |   持續整合工具      |        https://www.jenkins.io/                    |
| ShardingSphere|   分庫分錶中間件    |        https://shardingsphere.apache.org/         |

### 開發環境

|     工具      | 版本號 |                             下載                               |
| :-----------: | :----: | :----------------------------------------------------------: |
|      JDK      |  1.8   | https://www.oracle.com/java/technologies/javase/javase-jdk8-downloads.html |
|     Linux     |CentOS 7|                 https://www.centos.org/download/             |
|     Mysql     |  5.7   |                    https://www.mysql.com                     |
|     Redis     | latest |                  https://redis.io/download                   |
| Elasticsearch | 7.4.2  |               https://www.elastic.co/downloads               |
|    Kibana     | 7.4.2  |               https://www.elastic.co/cn/kibana               |
|   RabbitMQ    | 3.11.10|            http://www.rabbitmq.com/download.html            |
|     Nginx     | 1.16.0 |              http://nginx.org/en/download.html              |
|     zipkin    | 2.1.7  |                  https://zipkin.io/                         |
|     Nacos     | 1.1.4  |  https://nacos.io/zh-cn/docs/what-is-nacos.html             |
|     Seata     | 2.1.0  |                  https://seata.io/zh-cn/                    |
|     Sleuth    | 2.1.7  | https://cloud.spring.io/spring-cloud-sleuth/reference/html/ |

注意：以上的除了JDK都是採用Docker方式進行安裝，詳細安裝步驟可參考Google!!!

### 搭建步驟

> Windows環境部署

- 修改本機的host文件，映射域名端口至Nginx地址

```
192.168.56.102	gulimall.com
192.168.56.102	search.gulimall.com
192.168.56.102  item.gulimall.com
192.168.56.102  auth.gulimall.com
192.168.56.102  cart.gulimall.com
192.168.56.102  order.gulimall.com
192.168.56.102  member.gulimall.com
192.168.56.102  seckill.gulimall.com
以上ip換成自己Linux的ip地址
```

- 修改Linux中Nginx的配置文件

```shell
1、在nginx.conf中添加負載均衡的配置   
upstream gulimall{
	# 網關的地址
	server 192.168.56.10:88;
}    
2、在gulimall.conf中添加如下配置
server {
	# 監聽以下域名地址的80端口
    listen       80;
    server_name  gulimall.com  *.gulimall.com kurtgulimall.hopto.org

    #charset koi8-r;
    #access_log  /var/log/nginx/log/host.access.log  main;

    #配置靜態資源分離
    location /static/ {
        root   /usr/share/nginx/html;
    }

    #支付異步回調的一個配置
    location /payed/ {
        proxy_set_header Host order.gulimall.com;        #不讓請求頭丟失
        proxy_pass http://gulimall;
    }

    location / {
        #root   /usr/share/nginx/html;
        #index  index.html index.htm;
        proxy_set_header Host $host;        #不讓請求頭丟失
        proxy_pass http://gulimall;
    }
```

或者直接用項目nginx模塊替換本機nginx配置目錄文件

- 克隆前端項目 `renren-fast-vue` 以 `npm run dev` 方式去運行
- 克隆整個後端項目 `gulimall` ，並導入 IDEA 中完成編譯



## 項目演示

### 前台商品系統

#### 首頁

![](https://s2.loli.net/2023/04/06/bzYiL3m9o1BJHpP.jpg)

#### 商品檢索

![](https://s2.loli.net/2023/04/06/WSZOPhTLtuD2jsq.png)

#### 認證

![](https://s2.loli.net/2023/04/06/OYCZfdEvsHeJMcb.jpg)

<img src="https://s2.loli.net/2023/04/06/TjhuFgA3XBV84Es.jpg"/>

#### 商品詳情

![](https://s2.loli.net/2023/04/06/iuYrsCUEytFJ23Q.jpg)

#### 購物車

![](https://s2.loli.net/2023/04/06/Cm3eAK1B5QcZfjO.jpg)

#### 結算頁

#### ![](https://s2.loli.net/2023/04/06/bLmhjEyov9CwU4H.png)支付

![](https://s2.loli.net/2023/04/06/1KVOZQ7nBaJrbsW.jpg)

![](https://s2.loli.net/2023/04/06/Cs9VUNYSQfTwoEq.jpg)

![](https://s2.loli.net/2023/04/06/bEexaN7cAf61XgD.png)



### 後台管理系統

#### 登錄

![](https://s2.loli.net/2023/04/06/U53tRazs8O6yTKQ.jpg)

#### 商品系統

**分類管理**

![](https://s2.loli.net/2023/04/06/zwex5SEWlIksXuq.jpg)

**品牌管理**

![](https://s2.loli.net/2023/04/06/sMmrTGw4B2NPlf1.jpg)

**平台屬性**

![ss](https://s2.loli.net/2023/04/06/gYaNpdRSiAjcGOL.jpg)

**商品管理**

![](https://s2.loli.net/2023/04/06/XsKOcW4RSvqQnFr.jpg)

**發布商品**

![](https://s2.loli.net/2023/04/06/T2wsvVxSA4eYuWt.jpg)

**採購單**

![](https://s2.loli.net/2023/04/06/POmT9x7n2DNJyVR.jpg)

**會員等級**

![](https://s2.loli.net/2023/04/06/sTkVbRnYmGJLEOe.jpg)


### ------------- Kubernetes（K8s）& KubeSphere --------------
![](https://s2.loli.net/2023/04/06/VT5rksJAO1oUyxv.jpg)
![](https://s2.loli.net/2023/04/06/l2TYqFbmpPuXy9W.jpg)
![](https://s2.loli.net/2023/04/06/t89HNRxbOEnoUK7.jpg)
![](https://s2.loli.net/2023/04/06/dbNjeyHIOfLk2SR.jpg)
![](https://s2.loli.net/2023/04/06/d2UMyihpnNAQRra.jpg)
![](https://s2.loli.net/2023/04/06/qDpRZAyc4bYas2X.jpg)

### ---------------- Jenkins ----------------
![](https://s2.loli.net/2023/04/06/gziZyFnON4MheUq.jpg)
![](https://s2.loli.net/2023/04/06/9rtKjvmHRBWEDfq.jpg)
![](https://s2.loli.net/2023/04/06/fMktRDN4IK5JeZ3.jpg)
![](https://s2.loli.net/2023/04/06/FXnOTKcLqBIPJ2x.jpg)
![](https://s2.loli.net/2023/04/06/rKRZJt1VTIsHDdC.jpg)
![](https://s2.loli.net/2023/04/06/KfnlASYoOLT8peD.jpg)

### ---------------- RabbitMQ ------------------
![](https://s2.loli.net/2023/04/06/HRmFU4ixjEs7bfG.jpg)
![](https://s2.loli.net/2023/04/06/ZuCKjJ5gbniQcR3.jpg)
![](https://s2.loli.net/2023/04/06/oMDZS9QVFYbynaJ.jpg)


### --------------- Nacos ---------------
![](https://s2.loli.net/2023/04/06/UlJjyVTp87SNEHR.jpg)
![](https://s2.loli.net/2023/04/06/idJU4ER7vZgQMzY.jpg)

### -------------- Sentinel ----------------
![](https://s2.loli.net/2023/04/06/RmNyFvkxnPDUbM7.jpg)
![](https://s2.loli.net/2023/04/06/yzmsQ1pBRM83cnk.jpg)

### --------------- Zipkin ------------------
![](https://s2.loli.net/2023/04/06/6QIefqyAKLGlrPc.jpg)
![](https://s2.loli.net/2023/04/06/2aw49DsZLiGxgSo.jpg)

### --------------- SonarQube -----------------
![](https://s2.loli.net/2023/04/06/Sl7uI5ZdFRWG4vB.jpg)
![](https://s2.loli.net/2023/04/06/ja9gFIGmcMbeZTt.jpg)
![](https://s2.loli.net/2023/04/06/2TpVkHuOrblKUWo.jpg)
![](https://s2.loli.net/2023/04/06/l6EtrezMK5HsBUN.jpg)

### ------------- Elasticsearch & kibana -----------------
![](https://s2.loli.net/2023/04/06/TWLPCEiKwH7XSrb.jpg)
![](https://s2.loli.net/2023/04/06/F7xuADH9semzrZ6.jpg)

### ---------------- Postman -------------------
![](https://s2.loli.net/2023/04/06/dks5UDJQ9xrG8Zh.jpg)

### ----------- Oracle VM VirtualBox --------------
![](https://s2.loli.net/2023/04/06/hnNEuzstm1WoP85.jpg)

### --------------- Jmter -----------------
![](https://s2.loli.net/2023/04/06/5tzflIJq7E4SW1a.jpg)

### --------------- VisualVM ------------------
![](https://s2.loli.net/2023/04/06/6DSyzfr9ReTWnUl.jpg)

### ---------------- Xshell 7 ------------------
![](https://s2.loli.net/2023/04/06/ojb45P796c1MWDJ.jpg)

### ----------------- Xftp 7 --------------------
![](https://s2.loli.net/2023/04/06/zILgT3Fu2Pe14QW.jpg)

### --------------- SwitchHosts -----------------
![](https://s2.loli.net/2023/04/06/fzBGDFMJhdUsOPg.jpg)



