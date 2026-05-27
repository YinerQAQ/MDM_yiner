package com.maike.mdm.dto.response;

import com.maike.mdm.entity.BaseMenu;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoResponse {

    private String id;
    private String username;
    private String nickname;
    private String orgId;
    private String orgName;
    private String status;
    private List<String> roles;
    private List<String> permissions;
    private List<BaseMenu> menus;
}
