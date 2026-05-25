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
@TableName("BASE_GROUP")
public class BaseGroup {

    @TableId(value = "ID", type = IdType.INPUT)
    private String id;

    @TableField("GROUPCODE")
    private String groupCode;

    @TableField("GROUPNAME")
    private String groupName;

    @TableField("ORGID")
    private String orgId;

    @TableField("STATUS")
    private String status;
}