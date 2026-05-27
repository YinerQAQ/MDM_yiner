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
@TableName("MDM_ESB_INFO_SYSTEM")
public class MdmEsbInfoSystem {

    @TableId(value = "ID", type = IdType.INPUT)
    private String id;

    @TableField("SYSTEM_CODE")
    private String systemCode;

    @TableField("SYSTEM_NAME")
    private String systemName;

    @TableField("SYSTEM_URL")
    private String systemUrl;

    @TableField("AUTH_TYPE")
    private String authType;

    @TableField("AUTH_CONFIG")
    private String authConfig;

    @TableField("CONTACT_PERSON")
    private String contactPerson;

    @TableField("CONTACT_PHONE")
    private String contactPhone;

    @TableField("STATUS")
    private String status;

    @TableField("REMARK")
    private String remark;

    @TableField("CREATE_TIME")
    private LocalDateTime createTime;

    @TableField("UPDATE_TIME")
    private LocalDateTime updateTime;
}
