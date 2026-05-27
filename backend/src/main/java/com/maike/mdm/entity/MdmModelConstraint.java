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
@TableName("MDM_MODEL_CONSTRAINT")
public class MdmModelConstraint {

    @TableId(value = "ID", type = IdType.INPUT)
    private String id;

    @TableField("MODEL_ID")
    private String modelId;

    @TableField("CONSTRAINT_TYPE")
    private String constraintType;

    @TableField("CONSTRAINT_NAME")
    private String constraintName;

    @TableField("CONSTRAINT_CODE")
    private String constraintCode;

    @TableField("SEVERITY")
    private String severity;

    @TableField("SCOPE")
    private String scope;

    @TableField("CONDITION_EXPR")
    private String conditionExpr;

    @TableField("CONFIG_JSON")
    private String configJson;

    @TableField("STATUS")
    private String status;

    @TableField("SORT_ORDER")
    private Integer sortOrder;

    @TableField("CREATE_TIME")
    private LocalDateTime createTime;

    @TableField("UPDATE_TIME")
    private LocalDateTime updateTime;

    @TableField("IS_DELETED")
    private Integer isDeleted;
}
