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

    @TableField("ROLECODE")
    private String roleCode;

    @TableField("ROLENAME")
    private String roleName;

    @TableField("ORGID")
    private String orgId;

    @TableField("STATUS")
    private String status;
}