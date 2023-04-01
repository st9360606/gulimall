package com.atguigu.gulimall.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.exception.NoStockException;
import com.atguigu.common.to.mq.OrderTo;
import com.atguigu.common.to.mq.SeckillOrderTo;
import com.atguigu.common.utils.R;
import com.atguigu.common.vo.MemberRespVo;
import com.atguigu.gulimall.order.constant.OrderConstant;
import com.atguigu.gulimall.order.dao.OrderItemDao;
import com.atguigu.gulimall.order.entity.OrderItemEntity;
import com.atguigu.gulimall.order.entity.PaymentInfoEntity;
import com.atguigu.gulimall.order.enume.OrderStatusEnum;
import com.atguigu.gulimall.order.feign.CartFeignService;
import com.atguigu.gulimall.order.feign.MemberFeignService;
import com.atguigu.gulimall.order.feign.ProductFeignService;
import com.atguigu.gulimall.order.feign.WareFeignService;
import com.atguigu.gulimall.order.interceptor.LoginUserInterceptor;
import com.atguigu.gulimall.order.service.OrderItemService;
import com.atguigu.gulimall.order.service.PaymentInfoService;
import com.atguigu.gulimall.order.to.OrderCreateTo;
import com.atguigu.gulimall.order.vo.*;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.order.dao.OrderDao;
import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.service.OrderService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import javax.swing.*;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    private ThreadLocal<OrderSubmitVo> confirmVoThreadLocal = new ThreadLocal<>();

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    PaymentInfoService paymentInfoService;

    @Autowired
    OrderItemService orderItemService;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    MemberFeignService memberFeignService;

    @Autowired
    CartFeignService cartFeignService;

    @Autowired
    WareFeignService wareFeignService;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    ThreadPoolExecutor executor;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException {
        OrderConfirmVo confirmVo = new OrderConfirmVo();
        MemberRespVo memberRespVo = LoginUserInterceptor.loginUser.get();
        System.out.println("主線程...." + Thread.currentThread().getId());

        //獲取之前的請求
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();


        CompletableFuture<Void> getAddressFuture = CompletableFuture.runAsync(() -> {
            //1、遠程查詢所有的收貨地址列表
            System.out.println("member線程...." + Thread.currentThread().getId());
            //每一個線程都來共享之前的請求數據
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<MemberAddressVo> address = memberFeignService.getAddress(memberRespVo.getId());
            confirmVo.setAddress(address);
        }, executor);


        CompletableFuture<Void> cartFuture = CompletableFuture.runAsync(() -> {
            //2、遠程查詢購物車所有選中的購物項
            System.out.println("cart線程...." + Thread.currentThread().getId());
            //每一個線程都來共享之前的請求數據
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<OrderItemVo> items = cartFeignService.currentUserItems();
            confirmVo.setItems(items);
            //Feign在远程调用之前要构造请求，调用很多的拦截器
        }, executor).thenRunAsync(() -> {
            List<OrderItemVo> items = confirmVo.getItems();
            List<Long> collect = items.stream().map(item -> item.getSkuId()).collect(Collectors.toList());

            //TODO 一定要啟動庫存服務，否則庫存查不出。
            R hasStock = wareFeignService.getSkusHasStock(collect);
            List<SkuStockVo> data = hasStock.getData(new TypeReference<List<SkuStockVo>>() {
            });
            if (data != null) {
                Map<Long, Boolean> map = data.stream().collect(Collectors.toMap(SkuStockVo::getSkuId, SkuStockVo::getHasStock));
                confirmVo.setStocks(map);
            }
        }, executor);


        //3、查詢用戶積分
        Integer integration = memberRespVo.getIntegration();
        confirmVo.setIntegration(integration);

        //4、其他數據自動計算

        //TODO 5、防重令牌

        String token = UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue().set(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberRespVo.getId(), token, 30, TimeUnit.MINUTES);
        confirmVo.setOrderToken(token);

        CompletableFuture.allOf(getAddressFuture, cartFuture).get();

        return confirmVo;

    }

    /**
     * 提交订单 去支付
     *
     * @Transactional 是一种本地事物，在分布式系统中，只能控制住自己的回滚，控制不了其他服务的回滚
     * 分布式事物 最大的原因是 网络问题+分布式机器。
     * (isolation = Isolation.REPEATABLE_READ) MySql默认隔离级别 - 可重复读
     */
    // @Transactional(isolation = Isolation.READ_COMMITTED) 设置事务的隔离级别
    // @Transactional(propagation = Propagation.REQUIRED)   设置事务的传播级别
//    @GlobalTransactional  //高併發場景
    @Transactional
    @Override
    public SubmitOrderResponseVo submitOrder(OrderSubmitVo vo) {

        confirmVoThreadLocal.set(vo);

        SubmitOrderResponseVo response = new SubmitOrderResponseVo();

        //從攔截器獲取當前用戶
        MemberRespVo memberRespVo = LoginUserInterceptor.loginUser.get();
        response.setCode(0);
        //1、首先验证令牌
        //0令牌失败 - 1刪除成功 ｜ 不存在0 存在 删除？1：0
        // lua腳本
        //KEYS[]：传入的Redis键参数。
        //ARGV[]：传入的脚本参数。KEYS[]与ARGV[]的索引均从1开始。

        String script = "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
        String orderToken = vo.getOrderToken();

        //原子驗證令牌和刪除令牌
        Long result = redisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class)
                , Arrays.asList(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberRespVo.getId()), orderToken);
        if (result == 0L) {
            //令牌驗證失敗
            response.setCode(1);
            return response;
        } else {
            //令牌驗證成功
            //下單: 去創建訂單、驗令牌、驗價格、驗庫存....
            //1、創建訂單，訂單項等信息
            OrderCreateTo order = createOrder();
            //2、驗價
            //後台金額
            BigDecimal payAmount = order.getOrder().getPayAmount();
            //頁面金額
            BigDecimal payPrice = vo.getPayPrice();

            //金额对比
            if (Math.abs(payAmount.subtract(payPrice).doubleValue()) < 0.01) {
                //對比成功
                //TODO 3、保存訂單
                saveOrder(order);

                //4、庫存鎖定，只要有異常回滾訂單數據。
                //訂單號，所有訂單項(skuId，skuName，num)
                WareSkuLockVo lockVo = new WareSkuLockVo();

                //訂單號
                lockVo.setOrderSn(order.getOrder().getOrderSn());

                //訂單項
                List<OrderItemVo> locks = order.getOrderItems().stream().map(item -> {
                    OrderItemVo itemVo = new OrderItemVo();
                    itemVo.setSkuId(item.getSkuId());
                    itemVo.setCount(item.getSkuQuantity());
                    itemVo.setTitle(item.getSkuName());
                    return itemVo;
                }).collect(Collectors.toList());

                lockVo.setLocks(locks);

                //TODO 4、 遠程鎖庫存
                //為了保證高併發，庫存服務自己回滾。可以發消息給庫存服務。
                //庫存服務本身也可以使用自動解鎖模式，要參與消息隊列
                R r = wareFeignService.orderLockStock(lockVo);
                if (r.getCode() == 0) {
                    //鎖成功了
                    response.setOrder(order.getOrder());

                    //TODO 5、遠程扣減積分
                    //如果库存成功了，但是网络原因超时了，就會產生订单服務回滚，但库存服務不回滚
//                    int i = 10 / 0;//模拟积分系统异常，订单服務回滚，但库存服務不回滚
                    //TODO 订单创建成功，发消息给MQ
                    rabbitTemplate.convertAndSend("order-event-exchange", "order.create.order", order.getOrder());
                    return response;
                } else {
                    //鎖失敗了
                    String msg = (String) r.get("msg");
                    throw new NoStockException(msg);
                }


            } else {
                //對比失敗
                response.setCode(2);
                return response;
            }
        }
    }

    @Override
    public OrderEntity getOrderByOrderSn(String orderSn) {
        OrderEntity order_sn = this.getOne(new QueryWrapper<OrderEntity>().eq("order_sn", orderSn));
        return order_sn;
    }

    @Override
    public void closeOrder(OrderEntity entity) {
        //查詢當前這個訂單的最新狀態
        OrderEntity orderEntity = this.getById(entity.getId());
        if (orderEntity.getStatus() == OrderStatusEnum.CREATE_NEW.getCode()) {
            //關單 超時未支付
            OrderEntity updateOrder = new OrderEntity();
            updateOrder.setId(entity.getId());
            updateOrder.setStatus(OrderStatusEnum.CANCLED.getCode());
            this.updateById(updateOrder);

            //TODO 發給MQ一個
            OrderTo orderTo = new OrderTo();
            BeanUtils.copyProperties(orderEntity, orderTo);

            try {
                //TODO 保证消息100%发送出去，每一个消息都做好日志记录 (给数据库保存每一个消息的详细信息)
                //TODO 定期扫描数据库 将失败的消息再发送一遍
                rabbitTemplate.convertAndSend("order-event-exchange", "order.release.other", orderTo);
            } catch (Exception e) {
                //TODO 将没发送出去的想消息进行重复发送 while
            }

        }
    }

    /**
     * 获取当前订单的支付信息 PayVo
     *
     * @param orderSn
     * @return
     */
    @Override
    public PayVo getOrderPay(String orderSn) {
        //要返回的大对象
        PayVo payVo = new PayVo();

        OrderEntity order = this.getOrderByOrderSn(orderSn);
        List<OrderItemEntity> order_sn = orderItemService.list(new QueryWrapper<OrderItemEntity>().eq("order_sn", orderSn));

        //大对象属性1：设置订单的备注
        payVo.setBody(order_sn.get(0).getSkuAttrsVals());

        //大对象属性2：订单号
        payVo.setOut_trade_no(order.getOrderSn());

        //大对象属性3：订单的主题
        payVo.setSubject("谷粒商城" + order_sn.get(0).getSkuName());

        //大对象属性4：订单的金额 小数点后2位+向上取值
        BigDecimal payNum = order.getPayAmount().setScale(2, BigDecimal.ROUND_UP);
        payVo.setTotal_amount(payNum.toString());

        //返回给前端这个大对象
        return payVo;


    }

    @Override
    public PageUtils queryPageWithItem(Map<String, Object> params) {
        MemberRespVo memberRespVo = LoginUserInterceptor.loginUser.get();
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>().eq("member_id", memberRespVo.getId()).orderByDesc("id")
        );
        List<OrderEntity> order_sn = page.getRecords().stream().map(order -> {
            List<OrderItemEntity> itemEntities = orderItemService.list(new QueryWrapper<OrderItemEntity>().eq("order_sn", order.getOrderSn()));
            order.setItemEntities(itemEntities);
            return order;
        }).collect(Collectors.toList());

        page.setRecords(order_sn);

        return new PageUtils(page);
    }

    /**
     * 處理支付寶的返回數據
     * @param vo
     * @return
     */
    @Override
    public String handlePayResult(PayAsyncVo vo) {
        //保存交易流水
        PaymentInfoEntity infoEntity = new PaymentInfoEntity();
        infoEntity.setAlipayTradeNo(vo.getTrade_no());
        infoEntity.setOrderSn(vo.getOut_trade_no());//修改数据库为唯一属性
        infoEntity.setPaymentStatus(vo.getTrade_status());
        infoEntity.setCallbackTime(vo.getNotify_time());

        paymentInfoService.save(infoEntity);

        //2。修改订单状态
        if (vo.getTrade_status().equals("TRADE_SUCCESS") || vo.getTrade_status().equals("TRADE_FINISHED")) {
            //支付成功
            String outTradeNo = vo.getOut_trade_no();
            this.baseMapper.updateOrderStatus(outTradeNo, OrderStatusEnum.PAYED.getCode());
        }
        return "success";


    }

    /**
     * 秒殺單信息
     * @param seckillOrder
     */
    @Override
    public void creatSeckillOrder(SeckillOrderTo seckillOrder) {
        //TODO 保存订单信息
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderSn(seckillOrder.getOrderSn());
        orderEntity.setMemberId(seckillOrder.getMemberId());

        orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());

        BigDecimal multiply = seckillOrder.getSeckillPrice().multiply(new BigDecimal("" + seckillOrder.getNum()));
        orderEntity.setPayAmount(multiply);

        this.save(orderEntity);

        //TODO 保存订单项信息
        OrderItemEntity orderItemEntity = new OrderItemEntity();
        orderItemEntity.setOrderSn(seckillOrder.getOrderSn());
        orderItemEntity.setRealAmount(multiply);

        //TODO 获取当前Sku的詳細信息進行配置
        orderItemEntity.setSkuQuantity(seckillOrder.getNum());

        orderItemService.save(orderItemEntity);

    }

    /**
     * 保存訂單數據
     *
     * @param order
     */
    private void saveOrder(OrderCreateTo order) {
        OrderEntity orderEntity = order.getOrder();
        orderEntity.setModifyTime(new Date()); //添加修改時間

        //保存訂單
        this.save(orderEntity);

        //保存所有訂單項數據
        List<OrderItemEntity> orderItems = order.getOrderItems();
        orderItemService.saveBatch(orderItems);

    }

    /**
     * 創建訂單
     *
     * @return
     */
    private OrderCreateTo createOrder() {
        OrderCreateTo createTo = new OrderCreateTo();
        //1、生成訂單號
        //時間 ID 可用於商品訂單 ID
        String orderSn = IdWorker.getTimeId();
        OrderEntity orderEntity = buildOrder(orderSn);

        //2、獲取到所有的訂單項
        List<OrderItemEntity> itemEntities = buildOrderItems(orderSn);

        //3、驗價 計算價格、積分等相關信息
        computePrice(orderEntity, itemEntities);

        createTo.setOrder(orderEntity);
        createTo.setOrderItems(itemEntities);

        return createTo;
    }

    private void computePrice(OrderEntity orderEntity, List<OrderItemEntity> itemEntities) {
        //总价格
        BigDecimal total = new BigDecimal("0.0");
        //优惠卷
        BigDecimal coupon = new BigDecimal("0.0");
        //积分
        BigDecimal interation = new BigDecimal("0.0");
        //打折
        BigDecimal promotion = new BigDecimal("0.0");
        //赠送积分
        BigDecimal gift = new BigDecimal("0.0");
        //赠送成长值
        BigDecimal growth = new BigDecimal("0.0");

        //訂單的總額，疊加每一個訂單項的總額信息
        for (OrderItemEntity entity : itemEntities) {
            coupon = coupon.add(entity.getCouponAmount());
            interation = interation.add(entity.getIntegrationAmount());
            promotion = promotion.add(entity.getPromotionAmount());
            total = total.add(entity.getRealAmount());
            gift = gift.add(new BigDecimal(entity.getGiftIntegration().toString()));
            growth = growth.add(new BigDecimal(entity.getGiftGrowth().toString()));
        }
        //1、订单价格相关
        orderEntity.setTotalAmount(total);
        //应付金额 + 运费金额
        orderEntity.setPayAmount(total.add(orderEntity.getFreightAmount()));
        //优惠信息
        orderEntity.setPromotionAmount(promotion);
        orderEntity.setIntegrationAmount(interation);
        orderEntity.setCouponAmount(coupon);
        //设置积分和成長值信息
        orderEntity.setGrowth(growth.intValue());
        orderEntity.setIntegration(gift.intValue());
        //設置刪除狀態  0:代表未删除
        orderEntity.setDeleteStatus(0);


    }

    private OrderEntity buildOrder(String orderSn) {
        MemberRespVo respVo = LoginUserInterceptor.loginUser.get();
        OrderEntity entity = new OrderEntity();
        entity.setOrderSn(orderSn);
        entity.setMemberId(respVo.getId());

        //獲取收貨地址信息
        OrderSubmitVo submitVo = confirmVoThreadLocal.get();

        //獲取運費信息
        R fare = wareFeignService.getFare(submitVo.getAddrId());
        FareVo fareResp = fare.getData(new TypeReference<FareVo>() {
        });
        //設置運費
        entity.setFreightAmount(fareResp.getFare());

        //設置收貨人信息
        entity.setReceiverCity(fareResp.getAddress().getCity());
        entity.setReceiverDetailAddress(fareResp.getAddress().getDetailAddress());
        entity.setReceiverName(fareResp.getAddress().getName());
        entity.setReceiverPhone(fareResp.getAddress().getPhone());
        entity.setReceiverPostCode(fareResp.getAddress().getPostCode());
        entity.setReceiverProvince(fareResp.getAddress().getProvince());
        entity.setReceiverRegion(fareResp.getAddress().getRegion());

        //設定訂單的相關狀態信息
        entity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());

        //自动确认时间（天）
        entity.setAutoConfirmDay(7);

        return entity;
    }

    /**
     * 構建所有訂單項數據
     *
     * @return
     */
    private List<OrderItemEntity> buildOrderItems(String orderSn) {
        //重要!!! 最後確定每個購物項的價格
        List<OrderItemVo> currentUserItems = cartFeignService.currentUserItems();
        if (currentUserItems != null && currentUserItems.size() > 0) {
            List<OrderItemEntity> itemEntities = currentUserItems.stream().map(cartItem -> {
                OrderItemEntity itemEntity = buildOrderItem(cartItem);
                itemEntity.setOrderSn(orderSn);

                return itemEntity;
            }).collect(Collectors.toList());
            return itemEntities;
        }
        return null;
    }

    /**
     * 構建一個訂單項
     *
     * @param cartItem
     * @return
     */
    private OrderItemEntity buildOrderItem(OrderItemVo cartItem) {
        OrderItemEntity itemEntity = new OrderItemEntity();
        //1、訂單信息，訂單號
        //2、商品的spu信息
        Long skuId = cartItem.getSkuId();
        R r = productFeignService.getSpuInfoBySkuId(skuId);
        SpuInfoVo data = r.getData(new TypeReference<SpuInfoVo>() {
        });
        itemEntity.setSpuId(data.getId());
        itemEntity.setSpuBrand(data.getBrandId().toString());
        itemEntity.setSpuName(data.getSpuName());
        itemEntity.setCategoryId(data.getCatalogId());

        //3、商品的sku信息
        itemEntity.setSkuId(cartItem.getSkuId());
        itemEntity.setSkuName(cartItem.getTitle());
        itemEntity.setSkuPic(cartItem.getImage());
        itemEntity.setSkuPrice(cartItem.getPrice());
        //集合轉數組 ，數組用;隔開
        String skuAttr = StringUtils.collectionToDelimitedString(cartItem.getSkuAttr(), ";");
        itemEntity.setSkuAttrsVals(skuAttr);
        itemEntity.setSkuQuantity(cartItem.getCount());
        //4、優惠信息
        //5、積分信息
        itemEntity.setGiftGrowth(cartItem.getPrice().multiply(new BigDecimal(cartItem.getCount().toString())).intValue());
        itemEntity.setGiftIntegration(cartItem.getPrice().multiply(new BigDecimal(cartItem.getCount().toString())).intValue());

        //6、訂單項的價格信息
        //promotion_amount` decimal(18,4) DEFAULT NULL COMMENT '促销优化金额（促销价、满减、阶梯价）',
        //integration_amount` decimal(18,4) DEFAULT NULL COMMENT '积分抵扣金额',
        //coupon_amount` decimal(18,4) DEFAULT NULL COMMENT '优惠券抵扣金额',
        //real_amount` decimal(18,4) DEFAULT NULL COMMENT '该商品经过优惠后的分解金额',
        itemEntity.setPromotionAmount(new BigDecimal("0"));
        itemEntity.setCouponAmount(new BigDecimal("0"));
        itemEntity.setIntegrationAmount(new BigDecimal("0"));
        //當前訂單項的實際金額
        BigDecimal orign = itemEntity.getSkuPrice().multiply(new BigDecimal(itemEntity.getSkuQuantity().toString()));

        //subtract (減法)
        //實際金額 - 优惠券抵扣金额 - 促销优化金额 - 积分抵扣金额 = subtract
        BigDecimal subtract = orign.subtract(itemEntity.getCouponAmount())
                .subtract(itemEntity.getPromotionAmount())
                .subtract(itemEntity.getIntegrationAmount());

        itemEntity.setRealAmount(subtract);


        return itemEntity;
    }

}