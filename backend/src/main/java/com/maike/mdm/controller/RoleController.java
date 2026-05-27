package com.maike.mdm.controller;

import com.maike.mdm.common.response.ApiResponse;
import com.maike.mdm.entity.BaseRole;
import com.maike.mdm.entity.BaseUser;
import com.maike.mdm.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<BaseRole>>> listRoles() {
        List<BaseRole> roles = roleService.listRoles();
        return ResponseEntity.ok(ApiResponse.success(roles));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createRole(@RequestBody BaseRole role) {
        roleService.createRole(role);
        return ResponseEntity.ok(ApiResponse.success("创建成功", null));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> updateRole(@PathVariable String id, @RequestBody BaseRole role) {
        role.setId(id);
        roleService.updateRole(role);
        return ResponseEntity.ok(ApiResponse.success("更新成功", null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRole(@PathVariable String id) {
        roleService.deleteRole(id);
        return ResponseEntity.ok(ApiResponse.success("删除成功", null));
    }

    @GetMapping("/{id}/menus")
    public ResponseEntity<ApiResponse<List<String>>> getRoleMenuIds(@PathVariable String id) {
        List<String> menuIds = roleService.getRoleMenuIds(id);
        return ResponseEntity.ok(ApiResponse.success(menuIds));
    }

    @PutMapping("/{id}/menus")
    public ResponseEntity<ApiResponse<Void>> assignMenus(@PathVariable String id, @RequestBody List<String> menuIds) {
        roleService.assignMenus(id, menuIds);
        return ResponseEntity.ok(ApiResponse.success("分配菜单权限成功", null));
    }

    @GetMapping("/{id}/users")
    public ResponseEntity<ApiResponse<List<BaseUser>>> getRoleUsers(@PathVariable String id) {
        List<BaseUser> users = roleService.getRoleUsers(id);
        return ResponseEntity.ok(ApiResponse.success(users));
    }
}
