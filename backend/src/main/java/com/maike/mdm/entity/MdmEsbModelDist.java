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
@TableName("MDM_ESB_MODEL_DIST")
public class MdmEsbModelDist {

    @TableId(value = "ID", type = IdType.INPUT)
    private String id;

    @TableField("MODEL_ID")
    private String modelId;

    @TableField("DIST_NAME")
    private String distName;

    @TableField("DIST_CODE")
    private String distCode;

    @TableField("INTERFACE_TYPE")
    private String interfaceType;

    @TableField("SYNC_TYPE")
    private String syncType;

    @TableField("DATA_FORMAT")
    private String dataFormat;

    @TableField("CRON_EXPR")
    private String cronExpr;

    @TableField("BATCH_SIZE")
    private Integer batchSize;

    @TableField("RETRY_COUNT")
    private Integer retryCount;

    @TableField("RETRY_INTERVAL")
    private Integer retryInterval;

    @TableField("TIMEOUT")
    private Integer timeout;

    @TableField("TARGET_SYSTEM_ID")
    private String targetSystemId;

    @TableField("TARGET_URL")
    private String targetUrl;

    @TableField("STATUS")
    private String status;

    @TableField("CREATE_TIME")
    private LocalDateTime createTime;

    @TableField("UPDATE_TIME")
    private LocalDateTime updateTime;
}
