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
@TableName("MDM_WORKFLOW_TASK")
public class MdmWorkflowTask {

    @TableId(value = "ID", type = IdType.INPUT)
    private String id;

    @TableField("INSTANCE_ID")
    private String instanceId;

    @TableField("NODE_ID")
    private String nodeId;

    @TableField("NODE_NAME")
    private String nodeName;

    @TableField("ASSIGNEE_ID")
    private String assigneeId;

    @TableField("ASSIGNEE_NAME")
    private String assigneeName;

    @TableField("TASK_STATUS")
    private String taskStatus;

    @TableField("OPINION")
    private String opinion;

    @TableField("CLAIM_TIME")
    private LocalDateTime claimTime;

    @TableField("COMPLETE_TIME")
    private LocalDateTime completeTime;

    @TableField("TRANSFER_TO_ID")
    private String transferToId;

    @TableField("TRANSFER_TO_NAME")
    private String transferToName;

    @TableField("CREATE_TIME")
    private LocalDateTime createTime;
}
