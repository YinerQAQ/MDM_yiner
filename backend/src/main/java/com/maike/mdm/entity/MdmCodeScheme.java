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
@TableName("MDM_CODE_SCHEME")
public class MdmCodeScheme {

    @TableId(value = "ID", type = IdType.INPUT)
    private String id;

    @TableField("RULE_ID")
    private String ruleId;

    @TableField("SCHEME_CODE")
    private String schemeCode;

    @TableField("SCHEME_NAME")
    private String schemeName;

    @TableField("CONDITION_EXPRESSION")
    private String conditionExpression;

    @TableField("CREATE_TIME")
    private LocalDateTime createTime;

    @TableField("UPDATE_TIME")
    private LocalDateTime updateTime;

    @TableField("IS_DELETED")
    private Integer isDeleted;
}
