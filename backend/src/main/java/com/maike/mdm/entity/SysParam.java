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
@TableName("SYS_PARAM")
public class SysParam {

    @TableId(value = "ID", type = IdType.INPUT)
    private String id;

    @TableField("PARAM_KEY")
    private String paramKey;

    @TableField("PARAM_VALUE")
    private String paramValue;

    @TableField("PARAM_NAME")
    private String paramName;

    @TableField("PARAM_TYPE")
    private String paramType;

    @TableField("DESCRIPTION")
    private String description;

    @TableField("STATUS")
    private String status;

    @TableField("CREATE_TIME")
    private LocalDateTime createTime;

    @TableField("UPDATE_TIME")
    private LocalDateTime updateTime;
}
