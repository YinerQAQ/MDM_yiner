package com.maike.mdm.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArchiveApplyRequest {

    private List<String> dataIds;
    private String modelCode;
    private String archiveReason;
}
