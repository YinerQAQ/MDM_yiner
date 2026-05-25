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
@TableName("MDM_MODEL_ATTRIBUTE")
public class MdmModelAttribute {

    @TableId(value = "ID", type = IdType.INPUT)
    private String id;

    @TableField("MODELID")
    private String modelId;

    @TableField("ATTRCODE")
    private String attrCode;

    @TableField("ATTRNAME")
    private String attrName;

    @TableField("DATATYPE")
    private String dataType;

    @TableField("LENGTH")
    private Integer length;

    @TableField("ISREQUIRED")
    private String isRequired;

    @TableField("DEFAULTVALUE")
    private String defaultValue;

    @TableField("DISPLAYORDER")
    private Integer displayOrder;

    @TableField("STATUS")
    private String status;
}