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
@TableName("MDM_CODE_SEGMENT")
public class MdmCodeSegment {

    @TableId(value = "ID", type = IdType.INPUT)
    private String id;

    @TableField("SCHEME_ID")
    private String schemeId;

    @TableField("SEGMENT_CODE")
    private String segmentCode;

    @TableField("SEGMENT_NAME")
    private String segmentName;

    @TableField("SEGMENT_TYPE")
    private String segmentType;

    @TableField("SEGMENT_FORMAT")
    private String segmentFormat;

    @TableField("SEGMENT_VALUE")
    private String segmentValue;

    @TableField("FIXED_VALUE")
    private String fixedValue;

    @TableField("SEGMENT_LENGTH")
    private Integer segmentLength;

    @TableField("EXPRESSION")
    private String expression;

    @TableField("REFERENCE_FIELD")
    private String referenceField;

    @TableField("RELATED_SEGMENT_ID")
    private String relatedSegmentId;

    @TableField("SORT_ORDER")
    private Integer sortOrder;

    @TableField("CREATE_TIME")
    private LocalDateTime createTime;

    @TableField("UPDATE_TIME")
    private LocalDateTime updateTime;

    @TableField("IS_DELETED")
    private Integer isDeleted;
}
