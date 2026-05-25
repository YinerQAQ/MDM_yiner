package com.maike.mdm.controller;

import com.maike.mdm.common.response.ApiResponse;
import com.maike.mdm.entity.BaseOrg;
import com.maike.mdm.service.OrgService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orgs")
@RequiredArgsConstructor
public class OrgController {

    private final OrgService orgService;

    @PostMapping
    public ResponseEntity<ApiResponse<BaseOrg>> createOrg(@RequestBody BaseOrg org) {
        BaseOrg createdOrg = orgService.createOrg(org);
        return ResponseEntity.ok(ApiResponse.success("创建成功", createdOrg));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BaseOrg>> getOrgById(@PathVariable String id) {
        BaseOrg org = orgService.getOrgById(id);
        return ResponseEntity.ok(ApiResponse.success(org));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<BaseOrg>>> getAllOrgs() {
        List<BaseOrg> orgs = orgService.getAllOrgs();
        return ResponseEntity.ok(ApiResponse.success(orgs));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BaseOrg>> updateOrg(@PathVariable String id, @RequestBody BaseOrg org) {
        BaseOrg updatedOrg = orgService.updateOrg(id, org);
        return ResponseEntity.ok(ApiResponse.success("更新成功", updatedOrg));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteOrg(@PathVariable String id) {
        orgService.deleteOrg(id);
        return ResponseEntity.ok(ApiResponse.success("删除成功", null));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Void>> changeStatus(@PathVariable String id, @RequestParam String status) {
        orgService.changeOrgStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("状态已更新", null));
    }

    @GetMapping("/tree/{parentId}")
    public ResponseEntity<ApiResponse<List<BaseOrg>>> getOrgTree(@PathVariable String parentId) {
        List<BaseOrg> orgs = orgService.getOrgTree(parentId);
        return ResponseEntity.ok(ApiResponse.success(orgs));
    }
}