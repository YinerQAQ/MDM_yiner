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
@TableName("MDM_WORKFLOW_NODE")
public class MdmWorkflowNode {

    @TableId(value = "ID", type = IdType.INPUT)
    private String id;

    @TableField("WORKFLOW_ID")
    private String workflowId;

    @TableField("NODE_CODE")
    private String nodeCode;

    @TableField("NODE_NAME")
    private String nodeName;

    @TableField("NODE_TYPE")
    private String nodeType;

    @TableField("POSITION_X")
    private Integer positionX;

    @TableField("POSITION_Y")
    private Integer positionY;

    @TableField("ASSIGNEE_TYPE")
    private String assigneeType;

    @TableField("ASSIGNEE_VALUE")
    private String assigneeValue;

    @TableField("AUTO_APPROVE_EXPR")
    private String autoApproveExpr;

    @TableField("TIMEOUT_HOURS")
    private Integer timeoutHours;

    @TableField("TIMEOUT_ACTION")
    private String timeoutAction;

    @TableField("SIGN_TYPE")
    private String signType;

    @TableField("SIGN_THRESHOLD")
    private Integer signThreshold;

    @TableField("CLAIM_HOURS")
    private Integer claimHours;

    @TableField("REQUIRE_OPINION")
    private String requireOpinion;

    @TableField("REQUIRE_ATTACHMENT")
    private String requireAttachment;

    @TableField("CALLBACK_SQL")
    private String callbackSql;

    @TableField("SORT_ORDER")
    private Integer sortOrder;

    @TableField("CREATE_TIME")
    private LocalDateTime createTime;
}
