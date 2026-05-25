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
public class LoginResponse {

    private String token;
    private String username;
    private String nickname;
    private String orgId;
    private String orgName;
    private List<String> roles;
    private List<String> permissions;
}