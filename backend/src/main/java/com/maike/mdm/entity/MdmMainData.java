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
@TableName("MDM_MAIN_DATA")
public class MdmMainData {

    @TableId(value = "ID", type = IdType.INPUT)
    private String id;

    @TableField("MODEL_ID")
    private String modelId;

    @TableField("CODE")
    private String code;

    @TableField("DATA_STATUS")
    private String dataStatus;

    @TableField("FLOW_STATUS")
    private String flowStatus;

    @TableField("JSON_DATA")
    private String jsonData;

    @TableField("VERSION")
    private Integer version;

    @TableField("CREATED_BY_ID")
    private String createdById;

    @TableField("CREATED_BY_NAME")
    private String createdByName;

    @TableField("CREATED_BY_ORGID")
    private String createdByOrgId;

    @TableField("SUBMITTED_BY_ORGID")
    private String submittedByOrgId;

    @TableField("MODIFIED_BY_ID")
    private String modifiedById;

    @TableField("CREATE_TIME")
    private LocalDateTime createTime;

    @TableField("MODIFY_TIME")
    private LocalDateTime modifyTime;

    @TableField("IS_MODIFY")
    private Integer isModify;

    @TableField("SECURITY_LEVEL")
    private String securityLevel;

    @TableField("IS_DELETED")
    private Integer isDeleted;
}
