package com.maike.mdm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("MDM_CODE_RULE")
public class MdmCodeRule {

    @TableId(value = "ID", type = IdType.INPUT)
    private String id;

    @TableField("RULECODE")
    private String ruleCode;

    @TableField("RULENAME")
    private String ruleName;

    @TableField("DESCRIPTION")
    private String description;

    @TableField("STATUS")
    private String status;
}