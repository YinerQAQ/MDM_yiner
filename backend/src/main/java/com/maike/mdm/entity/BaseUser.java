package com.maike.mdm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("BASE_USER")
public class BaseUser {

    @TableId(value = "ID", type = IdType.INPUT)
    private String id;

    @TableField("USERNAME")
    private String username;

    @TableField("PASSWORD")
    private String password;

    @TableField("NICKNAME")
    private String nickname;

    @TableField("SEX")
    private String sex;

    @TableField("ORGID")
    private String orgId;

    @TableField("ORGNAME")
    private String orgName;

    @TableField("BIRTHDAY")
    private LocalDate birthday;

    @TableField("EMAIL")
    private String email;

    @TableField("PHONE")
    private String phone;

    @TableField("STATUS")
    private String status;

    @TableField("SECURITY_LEVEL")
    private String securityLevel;

    @TableField("CREATE_TIME")
    private java.time.LocalDateTime createTime;

    @TableField("UPDATE_TIME")
    private java.time.LocalDateTime updateTime;
}