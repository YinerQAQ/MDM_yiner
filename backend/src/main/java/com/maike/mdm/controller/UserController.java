package com.maike.mdm.controller;

import com.maike.mdm.common.annotation.AuditLog;
import com.maike.mdm.common.response.ApiResponse;
import com.maike.mdm.dto.request.UserCreateRequest;
import com.maike.mdm.entity.BaseUser;
import com.maike.mdm.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PreAuthorize("hasAuthority('menu:system:user')")
    @AuditLog(operation = "创建用户")
    @PostMapping
    public ResponseEntity<ApiResponse<BaseUser>> createUser(@Valid @RequestBody UserCreateRequest request) {
        BaseUser user = userService.createUser(request);
        return ResponseEntity.ok(ApiResponse.success("创建成功", user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BaseUser>> getUserById(@PathVariable String id) {
        BaseUser user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<BaseUser>>> getAllUsers() {
        List<BaseUser> users = userService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @PreAuthorize("hasAuthority('menu:system:user')")
    @AuditLog(operation = "更新用户")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BaseUser>> updateUser(@PathVariable String id, @RequestBody UserCreateRequest request) {
        BaseUser user = userService.updateUser(id, request);
        return ResponseEntity.ok(ApiResponse.success("更新成功", user));
    }

    @PreAuthorize("hasAuthority('menu:system:user')")
    @AuditLog(operation = "删除用户")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("删除成功", null));
    }

    @PreAuthorize("hasAuthority('menu:system:user')")
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Void>> changeStatus(@PathVariable String id, @RequestParam String status) {
        userService.changeUserStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("状态已更新", null));
    }

    @PreAuthorize("hasAuthority('menu:system:user')")
    @PutMapping("/{id}/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@PathVariable String id, @RequestParam String password) {
        userService.resetPassword(id, password);
        return ResponseEntity.ok(ApiResponse.success("密码已重置", null));
    }

    @GetMapping("/{id}/roles")
    public ResponseEntity<ApiResponse<List<String>>> getUserRoleIds(@PathVariable String id) {
        List<String> roleIds = userService.getUserRoleIds(id);
        return ResponseEntity.ok(ApiResponse.success(roleIds));
    }

    @PreAuthorize("hasAuthority('menu:system:user')")
    @AuditLog(operation = "分配用户角色")
    @PutMapping("/{id}/roles")
    public ResponseEntity<ApiResponse<Void>> assignRoles(@PathVariable String id, @RequestBody List<String> roleIds) {
        userService.assignRoles(id, roleIds);
        return ResponseEntity.ok(ApiResponse.success("角色分配成功", null));
    }
}