package com.atguigu.gulimall.product.entity;

import com.atguigu.common.valid.AddGroup;
import com.atguigu.common.valid.ListValue;
import com.atguigu.common.valid.UpdateGroup;
import com.atguigu.common.valid.UpdateStatusGroup;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.*;

/**
 * 品牌
 * 
 * @author kurt
 * @email st9360606@gmail.com
 * @date 2023-01-22 22:45:47
 */
@Data
@TableName("pms_brand")
public class BrandEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 品牌id
	 */
	@NotNull(message = "修改必須指定品牌id",groups = {UpdateGroup.class})
	@Null(message = "新增不能指定id",groups = AddGroup.class)
	@TableId
	private Long brandId;
	/**
	 * 品牌名 @NotBlank: 只能作用在String上，不能为null，而且调用trim()后，长度必须大于0
	 */

	@NotBlank(message = "品牌名必須提交",groups = {AddGroup.class,UpdateGroup.class})
	private String name;
	/**
	 * 品牌logo地址
	 */
	@NotBlank(groups = {AddGroup.class})
	@URL(message = "logo必須是一個合法的url地址",groups = {AddGroup.class,UpdateGroup.class})
	private String logo;


	/**
	 * 介绍
	 */
	private String descript;

	/**
	 * 显示状态[0-不显示；1-显示]
	 */
	@NotNull(groups = {AddGroup.class,UpdateStatusGroup.class})
	@ListValue(vals = {0,1}, groups = {AddGroup.class, UpdateStatusGroup.class})
	private Integer showStatus;


	/**
	 * 检索首字母 @Pattern 自定義校驗
	 */
	@NotEmpty(message = "不能為空",groups = {AddGroup.class})
	@Pattern(regexp = "^[a-zA-Z]$",message = "檢索首字母必須是一個字母",groups = {AddGroup.class,UpdateGroup.class})
	private String firstLetter;
	/**
	 * 排序
	 */
	@NotNull(message = "不能為null",groups = {AddGroup.class})
	@Min(value = 0,message = "排序必須大於等於0",groups = {AddGroup.class,UpdateGroup.class})
	private Integer sort;

}
