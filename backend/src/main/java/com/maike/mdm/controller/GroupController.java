package com.maike.mdm.controller;

import com.maike.mdm.common.response.ApiResponse;
import com.maike.mdm.dto.request.OrgPermDTO;
import com.maike.mdm.entity.BaseGroup;
import com.maike.mdm.entity.BaseUser;
import com.maike.mdm.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<BaseGroup>>> listGroups() {
        List<BaseGroup> groups = groupService.listGroups();
        return ResponseEntity.ok(ApiResponse.success(groups));
    }

    @PreAuthorize("hasAuthority('menu:system:user') or hasAuthority('menu:system:role')")
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createGroup(@RequestBody BaseGroup group) {
        groupService.createGroup(group);
        return ResponseEntity.ok(ApiResponse.success("创建成功", null));
    }

    @PreAuthorize("hasAuthority('menu:system:user') or hasAuthority('menu:system:role')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> updateGroup(@PathVariable String id, @RequestBody BaseGroup group) {
        group.setId(id);
        groupService.updateGroup(group);
        return ResponseEntity.ok(ApiResponse.success("更新成功", null));
    }

    @PreAuthorize("hasAuthority('menu:system:user') or hasAuthority('menu:system:role')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteGroup(@PathVariable String id) {
        groupService.deleteGroup(id);
        return ResponseEntity.ok(ApiResponse.success("删除成功", null));
    }

    @GetMapping("/{id}/orgs")
    public ResponseEntity<ApiResponse<List<String>>> getGroupOrgIds(@PathVariable String id) {
        List<String> orgIds = groupService.getGroupOrgIds(id);
        return ResponseEntity.ok(ApiResponse.success(orgIds));
    }

    @PreAuthorize("hasAuthority('menu:system:user') or hasAuthority('menu:system:role')")
    @PutMapping("/{id}/orgs")
    public ResponseEntity<ApiResponse<Void>> assignOrgs(@PathVariable String id, @RequestBody List<OrgPermDTO> orgPerms) {
        groupService.assignOrgs(id, orgPerms);
        return ResponseEntity.ok(ApiResponse.success("分配单位权限成功", null));
    }

    @GetMapping("/{id}/users")
    public ResponseEntity<ApiResponse<List<BaseUser>>> getGroupUsers(@PathVariable String id) {
        List<BaseUser> users = groupService.getGroupUsers(id);
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @PostMapping("/{id}/users")
    public ResponseEntity<ApiResponse<Void>> addUsersToGroup(@PathVariable String id, @RequestBody List<String> userIds) {
        groupService.addUsersToGroup(id, userIds);
        return ResponseEntity.ok(ApiResponse.success("添加用户成功", null));
    }

    @DeleteMapping("/{id}/users/{userId}")
    public ResponseEntity<ApiResponse<Void>> removeUserFromGroup(@PathVariable String id, @PathVariable String userId) {
        groupService.removeUserFromGroup(id, userId);
        return ResponseEntity.ok(ApiResponse.success("移除用户成功", null));
    }
}
