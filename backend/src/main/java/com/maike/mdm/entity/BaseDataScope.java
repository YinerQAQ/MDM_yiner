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
@TableName("BASE_DATA_SCOPE")
public class BaseDataScope {

    @TableId(value = "ID", type = IdType.INPUT)
    private String id;

    @TableField("GROUP_ID")
    private String groupId;

    @TableField("SCOPE_TYPE")
    private String scopeType;

    @TableField("ORG_CASCADE_FLAG")
    private String orgCascadeFlag;

    @TableField("CREATE_TIME")
    private LocalDateTime createTime;

    @TableField("UPDATE_TIME")
    private LocalDateTime updateTime;
}
