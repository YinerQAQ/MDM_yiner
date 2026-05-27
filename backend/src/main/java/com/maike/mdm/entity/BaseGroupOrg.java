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
@TableName("BASE_GROUP_ORG")
public class BaseGroupOrg {

    @TableId(value = "ID", type = IdType.INPUT)
    private String id;

    @TableField("GROUP_ID")
    private String groupId;

    @TableField("ORG_ID")
    private String orgId;

    @TableField("CASCADE_FLAG")
    private String cascadeFlag;

    @TableField("CREATE_TIME")
    private LocalDateTime createTime;
}
