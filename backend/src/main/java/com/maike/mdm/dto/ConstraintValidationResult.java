package com.maike.mdm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConstraintValidationResult {

    private String constraintName;
    private String constraintType;
    private String severity;
    private boolean passed;
    private String message;
}
