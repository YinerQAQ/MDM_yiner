package com.maike.mdm.dto.response;

import com.maike.mdm.dto.ConstraintValidationResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConstraintWarningResponse {

    private List<ConstraintValidationResult> warnings;
    private String dataId;
}
