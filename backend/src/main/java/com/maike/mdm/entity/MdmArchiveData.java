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
@TableName("MDM_ARCHIVE_DATA")
public class MdmArchiveData {

    @TableId(value = "ID", type = IdType.INPUT)
    private String id;

    @TableField("APPLY_ID")
    private String applyId;

    @TableField("DATA_ID")
    private String dataId;

    @TableField("MODEL_CODE")
    private String modelCode;

    @TableField("DATA_CODE")
    private String dataCode;

    @TableField("DATA_CONTENT")
    private String dataContent;

    @TableField("VERSION")
    private Integer version;

    @TableField("ARCHIVE_TIME")
    private LocalDateTime archiveTime;

    @TableField("CREATE_TIME")
    private LocalDateTime createTime;
}
