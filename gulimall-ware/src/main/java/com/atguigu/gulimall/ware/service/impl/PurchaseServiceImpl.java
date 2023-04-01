package com.atguigu.gulimall.ware.service.impl;


import com.atguigu.common.constant.WareConstant;
import com.atguigu.gulimall.ware.service.WareSkuService;
import com.atguigu.gulimall.ware.vo.MergeVo;
import com.atguigu.gulimall.ware.entity.PurchaseDetailEntity;
import com.atguigu.gulimall.ware.service.PurchaseDetailService;
import com.atguigu.gulimall.ware.vo.PurchaseDoneVo;
import com.atguigu.gulimall.ware.vo.PurchaseItemDoneVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.ware.dao.PurchaseDao;
import com.atguigu.gulimall.ware.entity.PurchaseEntity;
import com.atguigu.gulimall.ware.service.PurchaseService;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;

@Slf4j
@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {

    @Autowired
    PurchaseDetailService detailService;

    @Autowired
    WareSkuService wareSkuService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageUnreceivePurchase(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                //状态[0新建，1已分配，2正在采购，3已完成，4采购失败]
                new QueryWrapper<PurchaseEntity>().eq("status", 0).or().eq("status", 1)
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void mergePurchase(MergeVo mergeVo) {
        Long purchaseId = mergeVo.getPurchaseId();
        if (purchaseId == null) {
            //1、新建一个採購單
            PurchaseEntity purchaseEntity = new PurchaseEntity();

            purchaseEntity.setStatus(WareConstant.PurchaseStatusEnum.CREATED.getCode());
            purchaseEntity.setCreateTime(new Date());
            purchaseEntity.setUpdateTime(new Date());
            this.save(purchaseEntity);
            //儲存後才會有id
            purchaseId = purchaseEntity.getId();
        }

        //TODO 确认采购单状态是0,1才可以合并

        List<Long> items = mergeVo.getItems();
        Long finalPurchaseId = purchaseId;
        List<PurchaseDetailEntity> collect = items.stream().map(id -> {
                    PurchaseDetailEntity byId = detailService.getById(id);
                    return byId;
                }).filter(item -> {
                    if (item.getStatus() == WareConstant.PurchaseDetailStatusEnum.CREATED.getCode() ||
                            item.getStatus() == WareConstant.PurchaseDetailStatusEnum.ASSIGNED.getCode()) {
                        return true;
                    }
                    log.error("錯誤:采购单状态是0,1才可以合并.....");
                    return false;
                })
                .map(i -> {
                    PurchaseDetailEntity detailEntity = new PurchaseDetailEntity();

                    detailEntity.setId(i.getId());
                    detailEntity.setPurchaseId(finalPurchaseId);
                    detailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.ASSIGNED.getCode());
                    return detailEntity;
                }).collect(Collectors.toList());

        detailService.updateBatchById(collect);

        //合併完要修改採購單的采购单id跟更新時間
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(purchaseId);
        purchaseEntity.setUpdateTime(new Date());
        //this: 代表Mybatis 原生 baseMapper
        this.updateById(purchaseEntity);
    }

    /**
     * @param ids 采购单id
     */
    @Override
    public void received(List<Long> ids) {
        //1、确认当前采购单是新建或者已分配状态
        List<PurchaseEntity> collect = ids.stream().map(id -> {
            PurchaseEntity byId = this.getById(id);
            return byId;
        }).filter(item -> {
            if (item.getStatus() == WareConstant.PurchaseStatusEnum.CREATED.getCode() ||
                    item.getStatus() == WareConstant.PurchaseStatusEnum.ASSIGNED.getCode()) {
                return true;
            }
            return false;
        }).map(item -> {
            item.setStatus(WareConstant.PurchaseStatusEnum.RECEIVE.getCode());
            item.setUpdateTime(new Date());
            return item;
        }).collect(Collectors.toList());

        //2、改变采购单的状态
        this.updateBatchById(collect);


        //3、改变采购项的状态
        collect.forEach((item) -> {
            //拿到所有採購項
            List<PurchaseDetailEntity> entities = detailService.listDetailByPurchaseId(item.getId());
            //批量更新
            List<PurchaseDetailEntity> detailEntities = entities.stream().map(entity -> {
                PurchaseDetailEntity entity1 = new PurchaseDetailEntity();
                entity1.setId(entity.getId());
                entity1.setStatus(WareConstant.PurchaseDetailStatusEnum.BUYING.getCode());
                return entity1;
            }).collect(Collectors.toList());
            detailService.updateBatchById(detailEntities);
        });
    }

    @Transactional
    @Override
    public void done(PurchaseDoneVo doneVo) {

        Long id = doneVo.getId();


        //2、改变采购项的状态
        Boolean flag = true;
        List<PurchaseItemDoneVo> items = doneVo.getItems();

        List<PurchaseDetailEntity> updates = new ArrayList<>();
        for (PurchaseItemDoneVo item : items) {
            PurchaseDetailEntity detailEntity = new PurchaseDetailEntity();
            if (item.getStatus() == WareConstant.PurchaseDetailStatusEnum.HASERROR.getCode()) {
                flag = false;
                detailEntity.setStatus(item.getStatus());
            } else {
                detailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.FINISH.getCode());
                ////3、将成功采购的进行入库
                PurchaseDetailEntity entity = detailService.getById(item.getItemId());
                wareSkuService.addStock(entity.getSkuId(), entity.getWareId(), entity.getSkuNum());

            }
            detailEntity.setId(item.getItemId());
            //把detailEntity 放進 List<PurchaseDetailEntity>
            updates.add(detailEntity);
        }

        detailService.updateBatchById(updates);

        //1、改变采购单状态
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(id);
        purchaseEntity.setStatus(flag ? WareConstant.PurchaseStatusEnum.FINISH.getCode() : WareConstant.PurchaseStatusEnum.HASERROR.getCode());
        purchaseEntity.setUpdateTime(new Date());
        this.updateById(purchaseEntity);


    }

}