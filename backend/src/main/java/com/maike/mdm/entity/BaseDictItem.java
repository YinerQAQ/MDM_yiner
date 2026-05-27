package com.maike.mdm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("BASE_DICT_ITEM")
public class BaseDictItem {

    @TableId(value = "ID", type = IdType.INPUT)
    private String id;

    @TableField("DICT_ID")
    private String dictId;

    @TableField("DICT_CODE")
    private String dictCode;

    @TableField("ITEM_VALUE")
    private String itemValue;

    @TableField("ITEM_LABEL")
    private String itemLabel;

    @TableField("SORT_ORDER")
    private Integer sortOrder;

    @TableField("STATUS")
    private String status;

    @TableField("CREATE_TIME")
    private LocalDateTime createTime;

    @TableField("UPDATE_TIME")
    private LocalDateTime updateTime;
}
