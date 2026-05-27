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
@TableName("MDM_CODE_RECORD")
public class MdmCodeRecord {

    @TableId(value = "ID", type = IdType.INPUT)
    private String id;

    @TableField("SCHEME_ID")
    private String schemeId;

    @TableField("SEGMENT_ID")
    private String segmentId;

    @TableField("PREFIX")
    private String prefix;

    @TableField("CURRENT_VALUE")
    private Long currentValue;

    @TableField("UPDATE_TIME")
    private LocalDateTime updateTime;
}
