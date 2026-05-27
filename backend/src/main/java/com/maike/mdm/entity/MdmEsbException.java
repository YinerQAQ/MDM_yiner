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
@TableName("MDM_ESB_EXCEPTION")
public class MdmEsbException {

    @TableId(value = "ID", type = IdType.INPUT)
    private String id;

    @TableField("DIST_ID")
    private String distId;

    @TableField("EXCEPTION_TYPE")
    private String exceptionType;

    @TableField("EXCEPTION_MSG")
    private String exceptionMsg;

    @TableField("NOTIFY_TYPE")
    private String notifyType;

    @TableField("NOTIFY_STATUS")
    private String notifyStatus;

    @TableField("HANDLE_STATUS")
    private String handleStatus;

    @TableField("HANDLER_ID")
    private String handlerId;

    @TableField("HANDLE_TIME")
    private LocalDateTime handleTime;

    @TableField("CREATE_TIME")
    private LocalDateTime createTime;
}
