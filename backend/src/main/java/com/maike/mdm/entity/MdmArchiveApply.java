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
@TableName("MDM_ARCHIVE_APPLY")
public class MdmArchiveApply {

    @TableId(value = "ID", type = IdType.INPUT)
    private String id;

    @TableField("MODEL_ID")
    private String modelId;

    @TableField("MODEL_CODE")
    private String modelCode;

    @TableField("APPLY_USER_ID")
    private String applyUserId;

    @TableField("APPLY_USER_NAME")
    private String applyUserName;

    @TableField("APPLY_ORG_ID")
    private String applyOrgId;

    @TableField("ARCHIVE_REASON")
    private String archiveReason;

    @TableField("STATUS")
    private String status;

    @TableField("REVIEWER_ID")
    private String reviewerId;

    @TableField("REVIEW_OPINION")
    private String reviewOpinion;

    @TableField("REVIEW_TIME")
    private LocalDateTime reviewTime;

    @TableField("CREATE_TIME")
    private LocalDateTime createTime;
}
