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
@TableName("MDM_ESB_MODEL_DIST_DATA")
public class MdmEsbModelDistData {

    @TableId(value = "ID", type = IdType.INPUT)
    private String id;

    @TableField("DIST_ID")
    private String distId;

    @TableField("DATA_ID")
    private String dataId;

    @TableField("MODEL_CODE")
    private String modelCode;

    @TableField("DATA_CODE")
    private String dataCode;

    @TableField("SYNC_STATUS")
    private String syncStatus;

    @TableField("RETRY_COUNT")
    private Integer retryCount;

    @TableField("LAST_SYNC_TIME")
    private LocalDateTime lastSyncTime;

    @TableField("ERROR_MSG")
    private String errorMsg;

    @TableField("CREATE_TIME")
    private LocalDateTime createTime;
}
