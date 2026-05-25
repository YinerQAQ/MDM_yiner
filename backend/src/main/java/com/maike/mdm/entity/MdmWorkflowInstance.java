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
@TableName("MDM_WORKFLOW_INSTANCE")
public class MdmWorkflowInstance {

    @TableId(value = "ID", type = IdType.INPUT)
    private String id;

    @TableField("WORKFLOW_ID")
    private String workflowId;

    @TableField("BUSINESS_ID")
    private String businessId;

    @TableField("BUSINESS_TYPE")
    private String businessType;

    @TableField("CURRENT_NODE")
    private String currentNode;

    @TableField("STATUS")
    private String status;

    @TableField("INITIATOR_ID")
    private String initiatorId;

    @TableField("CREATE_TIME")
    private LocalDateTime createTime;

    @TableField("UPDATE_TIME")
    private LocalDateTime updateTime;

    @TableField("IS_DELETED")
    private Integer isDeleted;
}
