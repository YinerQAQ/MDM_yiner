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
@TableName("MDM_CODE_SCHEME")
public class MdmCodeScheme {

    @TableId(value = "ID", type = IdType.INPUT)
    private String id;

    @TableField("RULEID")
    private String ruleId;

    @TableField("SCHEMECODE")
    private String schemeCode;

    @TableField("SCHEMENAME")
    private String schemeName;

    @TableField("PRECONDITION")
    private String precondition;

    @TableField("SORTORDER")
    private Integer sortOrder;
}