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
@TableName("MDM_DATA_MODEL")
public class MdmDataModel {

    @TableId(value = "ID", type = IdType.INPUT)
    private String id;

    @TableField("MODEL_CODE")
    private String modelCode;

    @TableField("MODEL_NAME")
    private String modelName;

    @TableField("MODEL_TYPE")
    private String modelType;

    @TableField("STATUS")
    private String status;

    @TableField("VERSION")
    private Integer version;

    @TableField("DESCRIPTION")
    private String description;

    @TableField("CREATE_BY")
    private String createBy;

    @TableField("ORGID")
    private String orgId;

    @TableField("CREATE_TIME")
    private LocalDateTime createTime;

    @TableField("UPDATE_TIME")
    private LocalDateTime updateTime;

    @TableField("IS_DELETED")
    private Integer isDeleted;
}