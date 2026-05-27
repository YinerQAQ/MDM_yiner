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
@TableName("BASE_USER_ROLE")
public class BaseUserRole {

    @TableId(value = "ID", type = IdType.INPUT)
    private String id;

    @TableField("USER_ID")
    private String userId;

    @TableField("ROLE_ID")
    private String roleId;

    @TableField("CREATE_TIME")
    private LocalDateTime createTime;
}
