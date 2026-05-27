package com.maike.mdm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.maike.mdm.entity.MdmCodeRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface MdmCodeRecordMapper extends BaseMapper<MdmCodeRecord> {

    @Select("SELECT * FROM MDM_CODE_RECORD WHERE SCHEME_ID = #{schemeId} AND SEGMENT_ID = #{segmentId} AND PREFIX = #{prefix} FOR UPDATE")
    MdmCodeRecord selectForUpdate(@Param("schemeId") String schemeId,
                                  @Param("segmentId") String segmentId,
                                  @Param("prefix") String prefix);

    @Update("UPDATE MDM_CODE_RECORD SET CURRENT_VALUE = CURRENT_VALUE + 1, UPDATE_TIME = NOW() WHERE SCHEME_ID = #{schemeId} AND SEGMENT_ID = #{segmentId} AND PREFIX = #{prefix}")
    int incrementValue(@Param("schemeId") String schemeId,
                       @Param("segmentId") String segmentId,
                       @Param("prefix") String prefix);
}
