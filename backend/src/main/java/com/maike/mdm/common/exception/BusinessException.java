package com.maike.mdm.common.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final String code;

    public BusinessException(String message) {
        super(message);
        this.code = "500";
    }

    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
    }

    public static BusinessException of(String message) {
        return new BusinessException(message);
    }

    public static BusinessException of(String code, String message) {
        return new BusinessException(code, message);
    }
}