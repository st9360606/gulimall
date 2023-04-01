package com.atguigu.gulimall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.exception.NoStockException;
import com.atguigu.common.to.mq.OrderTo;
import com.atguigu.common.to.mq.StockDetailTo;
import com.atguigu.common.to.mq.StockLockedTo;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.ware.dao.WareSkuDao;

import com.atguigu.gulimall.ware.entity.WareOrderTaskDetailEntity;
import com.atguigu.gulimall.ware.entity.WareOrderTaskEntity;
import com.atguigu.gulimall.ware.feign.OrderFeignService;
import com.atguigu.gulimall.ware.feign.ProductFeignService;
import com.atguigu.gulimall.ware.service.WareOrderTaskDetailService;
import com.atguigu.gulimall.ware.service.WareOrderTaskService;
import com.atguigu.gulimall.ware.vo.*;
import com.rabbitmq.client.Channel;
import lombok.Data;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;


import com.atguigu.gulimall.ware.entity.WareSkuEntity;
import com.atguigu.gulimall.ware.service.WareSkuService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Autowired
    WareSkuDao wareSkuDao;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    WareOrderTaskService orderTaskService;

    @Autowired
    OrderFeignService orderFeignService;

    @Autowired
    WareOrderTaskDetailService orderTaskDetailService;


    private void unLockStock(Long skuId, Long wareId, Integer num, Long taskDetailId) {
        //庫存解鎖
        wareSkuDao.unlockStock(skuId, wareId, num);
        //更新庫存工作單的狀態
        WareOrderTaskDetailEntity entity = new WareOrderTaskDetailEntity();
        entity.setId(taskDetailId);
        entity.setLockStatus(2); //狀態更新為已解鎖
        orderTaskDetailService.updateById(entity);
    }


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        /**
         * skuId: 1
         * wareId: 1
         */
        QueryWrapper<WareSkuEntity> queryWrapper = new QueryWrapper<>();
        String skuId = (String) params.get("skuId");
        if (!StringUtils.isEmpty(skuId)) {
            queryWrapper.eq("sku_id", skuId);
        }

        String wareId = (String) params.get("wareId");
        if (!StringUtils.isEmpty(wareId)) {
            queryWrapper.eq("ware_id", wareId);
        }

        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        //1、判断如果还没有这个库存记录新增
        List<WareSkuEntity> entities = wareSkuDao.selectList(new QueryWrapper<WareSkuEntity>().eq("sku_id", skuId).eq("ware_id", wareId));
        if (entities == null || entities.size() == 0) {
            WareSkuEntity skuEntity = new WareSkuEntity();
            skuEntity.setSkuId(skuId);
            skuEntity.setStock(skuNum);
            skuEntity.setWareId(wareId);
            skuEntity.setStockLocked(0);   //默認鎖定庫存
            //TODO 远程查询sku的名字，如果失败，整个事务无需回滚
            //1、自己catch异常
            //TODO 还可以用什么办法让异常出现以后不回滚？高级
            try {
                R info = productFeignService.info(skuId);
                Map<String, Object> data = (Map<String, Object>) info.get("skuInfo");

                /**
                 *      {
                 *     "msg": "success",
                 *     "code": 0
                 *      }
                 */
                if (info.getCode() == 0) {
                    //獲取skuName並設定
                    skuEntity.setSkuName((String) data.get("skuName"));
                }
            } catch (Exception e) {

            }

            wareSkuDao.insert(skuEntity);
        } else {
            /**
             * <update id="addStock">
             *         UPDATE `wms_ware_sku` SET stock=stock+#{skuNum} WHERE sku_id=#{skuId} AND ware_id=#{wareId}
             * </update>
             */
            wareSkuDao.addStock(skuId, wareId, skuNum);
        }

    }


    @Override
    public List<SkuHasStockVo> getSkusHasStock(List<Long> skuIds) {

        List<SkuHasStockVo> collect = skuIds.stream().map(skuId -> {
            SkuHasStockVo vo = new SkuHasStockVo();
            //查詢當前sku的總庫存量
            Long count = baseMapper.getSkuStock(skuId);
            vo.setSkuId(skuId);
            vo.setHasStock(count == null ? false : count > 0);
            return vo;
        }).collect(Collectors.toList());
        return collect;
    }

    /**
     * 為某個訂單鎖定庫存
     * (rollbackFor = NoStockException.class)
     * 默認只要是運行時異常都會回滾
     *
     * @param vo
     * @return 庫存解鎖的場景
     * 1 下订单成功，订单过期，没有支付被系统自动取消、被用户手动取消
     * 2 下订单成功，库存锁定成功，接下来的业务调用失败，导致订单回滚。之前锁定的库存就要自动解锁
     */
    @Transactional
    @Override
    public Boolean orderLockStock(WareSkuLockVo vo) {

        /**
         * 保存库存工作单详情信息
         * 追溯
         */
        WareOrderTaskEntity taskEntity = new WareOrderTaskEntity();
        taskEntity.setOrderSn(vo.getOrderSn());
        orderTaskService.save(taskEntity);

        //1、按照下單的收貨地址，找到一個就近倉庫，鎖定庫存。
        //1、找到每個商品在哪個倉庫都有庫存
        List<OrderItemVo> locks = vo.getLocks();
        List<SkuWareHsaStock> collect = locks.stream().map(item -> {
            SkuWareHsaStock stock = new SkuWareHsaStock();
            Long skuId = item.getSkuId();
            stock.setSkuId(skuId);
            stock.setNum(item.getCount());
            //查詢這個商品在哪裡有庫存
            List<Long> wareIds = wareSkuDao.listWareIdHasSkuStock(skuId);
            stock.setWareId(wareIds);
            return stock;
        }).collect(Collectors.toList());

        Boolean allLock = true;
        //2、鎖定庫存
        for (SkuWareHsaStock hsaStock : collect) {
            Boolean skuStocked = false;
            Long skuId = hsaStock.getSkuId();
            List<Long> wareIds = hsaStock.getWareId();
            if (wareIds == null || wareIds.size() == 0) {
                //沒有任何倉庫有這個商品的庫存
                throw new NoStockException(skuId);
            }

            //有库存
            //1 如果每一个商品都锁定成功，将当前商品锁定了几件的工作单记录发给MQ
            //2 如果鎖定失败，前面保存的工作单的信息就回滚了。发送出去的消息，即使要解锁记录，由于去数据库查不到ID，所以不用解锁
            for (Long wareId : wareIds) {
                //成功就返回 1，否則就是 0
                Long count = wareSkuDao.lockSkuStock(skuId, wareId, hsaStock.getNum());

                if (count == 1) {
                    //鎖定成功
                    skuStocked = true;
                    //TODO 告訴MQ庫存鎖定成功
                    WareOrderTaskDetailEntity entity = new WareOrderTaskDetailEntity(null, skuId, "", hsaStock.getNum(), taskEntity.getId(), wareId, 1);
                    orderTaskDetailService.save(entity);

                    StockLockedTo lockedTo = new StockLockedTo();
                    lockedTo.setId(taskEntity.getId());

                    //對拷
                    StockDetailTo stockDetailTo = new StockDetailTo();
                    BeanUtils.copyProperties(entity, stockDetailTo);
                    //只发id不行，防止回滚以后找不到数据
                    lockedTo.setDetail(stockDetailTo);

                    //TODO 發消息給MQ
                    rabbitTemplate.convertAndSend("stock-event-exchange", "stock.locked", lockedTo);
                    break;
                } else {
                    //當前倉庫鎖失敗了，重試下一個倉庫
                }
            }
            if (skuStocked == false) {
                //說明當前商品所有倉庫都沒有鎖住
                throw new NoStockException(skuId);
            }
        }
        //3 代码能够到这里，代表全部商品都锁定成功
        return true;
    }

    @Override
    public void unlockStock(StockLockedTo to) {
        StockDetailTo detail = to.getDetail();
        Long detailId = detail.getId();
        //解鎖
        //1、查詢數據庫關於這個訂單的鎖定庫存信息
        //有: 證明庫存鎖定成功了
        //    解鎖:訂單情況。
        //          1、沒有這個訂單，必須解鎖
        //          2、有這個訂單，不是解鎖庫存。
        //              訂單狀態: 已取消:可以解鎖庫存
        //                      未取消:不可以解鎖庫存
        //沒有: 庫存鎖定失敗了，庫存回滾了，這種情況無須解鎖。
        WareOrderTaskDetailEntity byId = orderTaskDetailService.getById(detailId);
        if (byId != null) {
            //解鎖，代表 庫存訂單任務明細表中，有鎖定紀錄
            Long id = to.getId();
            WareOrderTaskEntity taskEntity = orderTaskService.getById(id);
            String orderSn = taskEntity.getOrderSn();//根據訂單號查詢訂單的狀態
            R r = orderFeignService.getOrderStatus(orderSn);
            if (r.getCode() == 0) {
                //訂單數據返回成功
                OrderVo data = r.getData(new TypeReference<OrderVo>() {
                });
                if (data == null || data.getStatus() == 4) {
                    //訂單不存在
                    //訂單狀態為已取消了，才能解鎖庫存。
                    //OrderStatusEnum  CANCLED(4,"已取消")
                    if (byId.getLockStatus() == 1) {
                        //當前庫存工作單詳情，狀態為1，已鎖定但是未解鎖才可以解鎖
                        unLockStock(detail.getSkuId(), detail.getWareId(), detail.getSkuNum(), detailId);
                    }
                }
            } else {
                //消息拒絕以後重新放到隊列裡面，讓別人繼續消費解鎖。
                throw new RuntimeException("遠程服務失敗...");
            }
        } else {
            //無須解鎖
        }
    }

    /**
     * 防止訂單服務卡頓，導致訂單狀態一值改不了，庫存消息優先到期。查訂單狀態新建狀態，什麼都不做就走了，導致卡頓的訂單，永遠不能解鎖庫存。
     *
     * @param orderTo
     */
    @Transactional
    @Override
    public void unlockStock(OrderTo orderTo) {
        String orderSn = orderTo.getOrderSn();
        //查一下最新庫存的狀態，防止重複解鎖庫存。
        WareOrderTaskEntity task = orderTaskService.getOrderTaskByOrderSn(orderSn);
        Long id = task.getId();
        //按照工作單找到所有沒有解鎖的庫存，進行解鎖。
        List<WareOrderTaskDetailEntity> entities = orderTaskDetailService.list(
                new QueryWrapper<WareOrderTaskDetailEntity>()
                        .eq("task_id", id)
                        .eq("lock_status", 1));
        for (WareOrderTaskDetailEntity entity : entities) {
            //解鎖
            unLockStock(entity.getSkuId(), entity.getWareId(), entity.getSkuNum(), entity.getId());
        }

    }

    @Data
    class SkuWareHsaStock {
        private Long skuId;         //商品的id
        private Integer num;        //商品數量
        private List<Long> wareId;  //倉庫的id
    }
}