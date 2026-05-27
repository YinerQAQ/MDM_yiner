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
@TableName("BASE_ROLE_MENU")
public class BaseRoleMenu {

    @TableId(value = "ID", type = IdType.INPUT)
    private String id;

    @TableField("ROLE_ID")
    private String roleId;

    @TableField("MENU_ID")
    private String menuId;

    @TableField("CREATE_TIME")
    private LocalDateTime createTime;
}
