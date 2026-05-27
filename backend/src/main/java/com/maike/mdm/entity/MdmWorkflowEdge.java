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
@TableName("MDM_WORKFLOW_EDGE")
public class MdmWorkflowEdge {

    @TableId(value = "ID", type = IdType.INPUT)
    private String id;

    @TableField("WORKFLOW_ID")
    private String workflowId;

    @TableField("SOURCE_NODE_ID")
    private String sourceNodeId;

    @TableField("TARGET_NODE_ID")
    private String targetNodeId;

    @TableField("CONDITION_EXPR")
    private String conditionExpr;

    @TableField("IS_DEFAULT")
    private String isDefault;

    @TableField("LABEL")
    private String label;

    @TableField("SORT_ORDER")
    private Integer sortOrder;

    @TableField("CREATE_TIME")
    private LocalDateTime createTime;
}
