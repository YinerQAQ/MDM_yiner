package com.maike.mdm.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateRequest {

    @NotBlank(message = "用户编码不能为空")
    private String id;

    @NotBlank(message = "用户名称不能为空")
    private String username;

    private String password;

    private String nickname;

    private String sex;

    private String orgId;

    private String email;

    private String phone;

    private String securityLevel;
}