package com.maike.mdm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("BASE_ROLE")
public class BaseRole {

    @TableId(value = "ID", type = IdType.INPUT)
    private String id;

    @TableField("ROLE_CODE")
    private String roleCode;

    @TableField("ROLE_NAME")
    private String roleName;

    @TableField("ORGID")
    private String orgId;

    @TableField("STATUS")
    private String status;

    @TableField("CREATE_TIME")
    private java.time.LocalDateTime createTime;

    @TableField("UPDATE_TIME")
    private java.time.LocalDateTime updateTime;
}