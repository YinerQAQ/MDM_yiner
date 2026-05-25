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
@TableName("MDM_CODE_SEGMENT")
public class MdmCodeSegment {

    @TableId(value = "ID", type = IdType.INPUT)
    private String id;

    @TableField("SCHEMEID")
    private String schemeId;

    @TableField("SEGMENTTYPE")
    private String segmentType;

    @TableField("SEGMENTNAME")
    private String segmentName;

    @TableField("GENERATERULE")
    private String generateRule;

    @TableField("LENGTH")
    private Integer length;

    @TableField("SORTORDER")
    private Integer sortOrder;
}