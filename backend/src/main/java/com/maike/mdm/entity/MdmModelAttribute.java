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
@TableName("MDM_MODEL_ATTRIBUTE")
public class MdmModelAttribute {

    @TableId(value = "ID", type = IdType.INPUT)
    private String id;

    @TableField("MODEL_ID")
    private String modelId;

    @TableField("ATTRIBUTE_CODE")
    private String attributeCode;

    @TableField("ATTRIBUTE_NAME")
    private String attributeName;

    @TableField("DATA_TYPE")
    private String dataType;

    @TableField("IS_REQUIRED")
    private Integer isRequired;

    @TableField("MAX_LENGTH")
    private Integer maxLength;

    @TableField("DEFAULT_VALUE")
    private String defaultValue;

    @TableField("DISPLAY_TYPE")
    private String displayType;

    @TableField("SORT_ORDER")
    private Integer sortOrder;

    @TableField("CREATE_TIME")
    private LocalDateTime createTime;

    @TableField("UPDATE_TIME")
    private LocalDateTime updateTime;

    @TableField("IS_DELETED")
    private Integer isDeleted;
}
