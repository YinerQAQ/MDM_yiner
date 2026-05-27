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
@TableName("MDM_ESB_MODEL_DIST_CONTENT")
public class MdmEsbModelDistContent {

    @TableId(value = "ID", type = IdType.INPUT)
    private String id;

    @TableField("DIST_ID")
    private String distId;

    @TableField("ATTRIBUTE_ID")
    private String attributeId;

    @TableField("ATTRIBUTE_CODE")
    private String attributeCode;

    @TableField("ATTRIBUTE_NAME")
    private String attributeName;

    @TableField("FIELD_MAPPING")
    private String fieldMapping;

    @TableField("SORT_ORDER")
    private Integer sortOrder;

    @TableField("INCLUDE_FLAG")
    private String includeFlag;
}
