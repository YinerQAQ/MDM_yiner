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
@TableName("MDM_WORKFLOW")
public class MdmWorkflow {

    @TableId(value = "ID", type = IdType.INPUT)
    private String id;

    @TableField("WORKFLOW_CODE")
    private String workflowCode;

    @TableField("WORKFLOW_NAME")
    private String workflowName;

    @TableField("WORKFLOW_TYPE")
    private String workflowType;

    @TableField("ORGID")
    private String orgId;

    @TableField("STATUS")
    private String status;

    @TableField("DEFINITION")
    private String definition;

    @TableField("CREATE_BY")
    private String createBy;

    @TableField("CREATE_TIME")
    private LocalDateTime createTime;

    @TableField("UPDATE_TIME")
    private LocalDateTime updateTime;

    @TableField("IS_DELETED")
    private Integer isDeleted;
}
