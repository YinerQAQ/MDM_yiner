package com.maike.mdm.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VersionCompareResult {

    private String fieldName;
    private String oldValue;
    private String newValue;
}
