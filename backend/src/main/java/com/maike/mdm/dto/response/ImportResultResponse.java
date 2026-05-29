package com.maike.mdm.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportResultResponse {

    private int successCount;
    private int failCount;
    private List<ImportFailureDetail> failures;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImportFailureDetail {
        private int rowIndex;
        private String reason;
    }
}
