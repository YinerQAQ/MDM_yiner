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
@TableName("SYS_AUDIT_LOG")
public class SysAuditLog {

    @TableId(value = "ID", type = IdType.AUTO)
    private Long id;

    @TableField("USER_ID")
    private String userId;

    @TableField("USERNAME")
    private String username;

    @TableField("OPERATION")
    private String operation;

    @TableField("METHOD")
    private String method;

    @TableField("PARAMS")
    private String params;

    @TableField("IP")
    private String ip;

    @TableField("RESULT")
    private String result;

    @TableField("DURATION")
    private Long duration;

    @TableField("CREATE_TIME")
    private LocalDateTime createTime;
}
