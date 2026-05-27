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
@TableName("SYS_LOGIN_LOG")
public class SysLoginLog {

    @TableId(value = "ID", type = IdType.AUTO)
    private Long id;

    @TableField("USER_ID")
    private String userId;

    @TableField("USERNAME")
    private String username;

    @TableField("IP")
    private String ip;

    @TableField("LOCATION")
    private String location;

    @TableField("BROWSER")
    private String browser;

    @TableField("OS")
    private String os;

    @TableField("STATUS")
    private String status;

    @TableField("MESSAGE")
    private String message;

    @TableField("CREATE_TIME")
    private LocalDateTime createTime;
}
