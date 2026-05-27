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
@TableName("MDM_ESB_MODEL_DIST_RECORD")
public class MdmEsbModelDistRecord {

    @TableId(value = "ID", type = IdType.INPUT)
    private String id;

    @TableField("DIST_ID")
    private String distId;

    @TableField("DATA_ID")
    private String dataId;

    @TableField("BATCH_ID")
    private String batchId;

    @TableField("REQUEST_BODY")
    private String requestBody;

    @TableField("RESPONSE_BODY")
    private String responseBody;

    @TableField("STATUS")
    private String status;

    @TableField("ERROR_MSG")
    private String errorMsg;

    @TableField("DURATION")
    private Long duration;

    @TableField("CREATE_TIME")
    private LocalDateTime createTime;
}
