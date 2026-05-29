package com.maike.mdm.controller;

import com.maike.mdm.common.response.ApiResponse;
import com.maike.mdm.entity.BaseDataScope;
import com.maike.mdm.entity.BaseMenu;
import com.maike.mdm.entity.BaseUser;
import com.maike.mdm.service.PermissionService;
import com.maike.mdm.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;
    private final UserService userService;

    /**
     * 获取当前用户菜单树
     */
    @GetMapping("/menus")
    public ResponseEntity<ApiResponse<List<BaseMenu>>> getCurrentUserMenuTree() {
        String userId = getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.ok(ApiResponse.success(Collections.emptyList()));
        }
        List<BaseMenu> menuTree = permissionService.getUserMenuTree(userId);
        return ResponseEntity.ok(ApiResponse.success(menuTree));
    }

    /**
     * 获取当前用户权限列表
     */
    @GetMapping("/perms")
    public ResponseEntity<ApiResponse<Set<String>>> getCurrentUserPermissions() {
        String userId = getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.ok(ApiResponse.success(Collections.emptySet()));
        }
        Set<String> permissions = permissionService.getUserPermissions(userId);
        return ResponseEntity.ok(ApiResponse.success(permissions));
    }

    /**
     * 获取当前用户数据范围
     */
    @GetMapping("/data-scope")
    public ResponseEntity<ApiResponse<List<BaseDataScope>>> getCurrentUserDataScope() {
        String userId = getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.ok(ApiResponse.success(Collections.emptyList()));
        }
        List<BaseDataScope> dataScopes = permissionService.getDataScope(userId);
        return ResponseEntity.ok(ApiResponse.success(dataScopes));
    }

    private String getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            String username = auth.getName();
            BaseUser user = userService.getUserByUsername(username);
            if (user != null) {
                return user.getId();
            }
        }
        return null;
    }
}
