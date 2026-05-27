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
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("BASE_MENU")
public class BaseMenu {

    @TableId(value = "ID", type = IdType.INPUT)
    private String id;

    @TableField("MENU_CODE")
    private String menuCode;

    @TableField("MENU_NAME")
    private String menuName;

    @TableField("PARENT_ID")
    private String parentId;

    @TableField("MENU_TYPE")
    private String menuType;

    @TableField("PATH")
    private String path;

    @TableField("COMPONENT")
    private String component;

    @TableField("ICON")
    private String icon;

    @TableField("SORT_ORDER")
    private Integer sortOrder;

    @TableField("STATUS")
    private String status;

    @TableField("VISIBLE")
    private String visible;

    @TableField("CREATE_TIME")
    private LocalDateTime createTime;

    @TableField("UPDATE_TIME")
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private List<BaseMenu> children;
}
