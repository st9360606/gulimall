package com.atguigu.gulimall.product.app;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


import com.atguigu.common.valid.AddGroup;
import com.atguigu.common.valid.UpdateGroup;
import com.atguigu.common.valid.UpdateStatusGroup;
import com.atguigu.gulimall.product.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.atguigu.gulimall.product.entity.BrandEntity;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;


/**
 * 品牌
 *
 * @author kurt
 * @email st9360606@gmail.com
 * @date 2023-01-23 12:38:53
 */
@RestController
@RequestMapping("product/brand")
public class BrandController {
    @Autowired
    private BrandService brandService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("product:brand:list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = brandService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{brandId}")
    //@RequiresPermissions("product:brand:info")
    public R info(@PathVariable("brandId") Long brandId) {
        BrandEntity brand = brandService.getById(brandId);

        return R.ok().put("brand", brand);
    }

    @GetMapping("/infos")
    public R info(@RequestParam("brandIds") List<Long> brandIds) {
        List<BrandEntity> brand = brandService.getBrandsByIds(brandIds);
        return R.ok().put("brand", brand);
    }

    /**
     * 保存
     * 標@Valid，校驗才會啟動
     */
    @RequestMapping("/save")
    //@RequiresPermissions("product:brand:save")
    public R save(@Validated({AddGroup.class}) @RequestBody BrandEntity brand/*, BindingResult result*/) {
//        if (result.hasErrors()) {
//            Map<String, String> map = new HashMap<>();
//            //獲取校驗的錯誤結果
//            result.getFieldErrors().forEach((item) -> {
//                        //FieldError 獲取到錯誤提示
//                        String message = item.getDefaultMessage();
//                        //獲取錯誤屬性的名字
//                        String field = item.getField();
//                        map.put(field, message);
//                    }
//            );
//            return R.error(400, "提交的數據不合法").put("data",map);
//        } else {
//        }

        //所有異常處理在GulimallExceptionControllerAdvice集中處理就好
        brandService.save(brand);
        return R.ok();
    }

    /**
     * 修改
     * //@Validated(UpdateGroup.class):分組校驗
     */
    @RequestMapping("/update")
    //@RequiresPermissions("product:brand:update")
    public R update(@Validated(UpdateGroup.class)@RequestBody BrandEntity brand) {
        brandService.updateDetail(brand);

        return R.ok();
    }

    /**
     * 修改狀態
     * //@Validated(UpdateGroup.class):分組校驗
     */
    @RequestMapping("/update/status")
    //@RequiresPermissions("product:brand:update")
    public R updateStatus(@Validated(UpdateStatusGroup.class)@RequestBody BrandEntity brand) {
        brandService.updateById(brand);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:brand:delete")
    public R delete(@RequestBody Long[] brandIds) {
        brandService.removeByIds(Arrays.asList(brandIds));

        return R.ok();
    }

}
