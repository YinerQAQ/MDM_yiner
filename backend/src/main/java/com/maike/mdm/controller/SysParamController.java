package com.maike.mdm.controller;

import com.maike.mdm.common.response.ApiResponse;
import com.maike.mdm.entity.SysParam;
import com.maike.mdm.service.SysParamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sys-params")
@RequiredArgsConstructor
public class SysParamController {

    private final SysParamService sysParamService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<SysParam>>> listParams() {
        List<SysParam> params = sysParamService.listParams();
        return ResponseEntity.ok(ApiResponse.success(params));
    }

    @GetMapping("/value/{key}")
    public ResponseEntity<ApiResponse<String>> getParamValue(@PathVariable String key) {
        String value = sysParamService.getParamValue(key);
        return ResponseEntity.ok(ApiResponse.success(value));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> updateParam(@PathVariable String id, @RequestBody SysParam param) {
        param.setId(id);
        sysParamService.updateParam(param);
        return ResponseEntity.ok(ApiResponse.success("更新成功", null));
    }
}
