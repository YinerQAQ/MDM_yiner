package com.maike.mdm.controller;

import com.maike.mdm.common.response.ApiResponse;
import com.maike.mdm.entity.BaseDict;
import com.maike.mdm.entity.BaseDictItem;
import com.maike.mdm.service.DictService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/dicts")
@RequiredArgsConstructor
public class DictController {

    private final DictService dictService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<BaseDict>>> listDicts() {
        List<BaseDict> dicts = dictService.listDicts();
        return ResponseEntity.ok(ApiResponse.success(dicts));
    }

    @GetMapping("/{dictCode}/items")
    public ResponseEntity<ApiResponse<List<BaseDictItem>>> getDictItems(@PathVariable String dictCode) {
        List<BaseDictItem> items = dictService.getItemsByDictCode(dictCode);
        return ResponseEntity.ok(ApiResponse.success(items));
    }

    @PreAuthorize("hasAuthority('menu:system:dict')")
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createDict(@RequestBody BaseDict dict) {
        dictService.createDict(dict);
        return ResponseEntity.ok(ApiResponse.success("创建成功", null));
    }

    @PreAuthorize("hasAuthority('menu:system:dict')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> updateDict(@PathVariable String id, @RequestBody BaseDict dict) {
        dict.setId(id);
        dictService.updateDict(dict);
        return ResponseEntity.ok(ApiResponse.success("更新成功", null));
    }

    @PreAuthorize("hasAuthority('menu:system:dict')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDict(@PathVariable String id) {
        dictService.deleteDict(id);
        return ResponseEntity.ok(ApiResponse.success("删除成功", null));
    }

    @PreAuthorize("hasAuthority('menu:system:dict')")
    @PostMapping("/{dictId}/items")
    public ResponseEntity<ApiResponse<Void>> createDictItem(@PathVariable String dictId, @RequestBody BaseDictItem item) {
        item.setDictId(dictId);
        dictService.createDictItem(item);
        return ResponseEntity.ok(ApiResponse.success("创建成功", null));
    }

    @PreAuthorize("hasAuthority('menu:system:dict')")
    @PutMapping("/items/{id}")
    public ResponseEntity<ApiResponse<Void>> updateDictItem(@PathVariable String id, @RequestBody BaseDictItem item) {
        item.setId(id);
        dictService.updateDictItem(item);
        return ResponseEntity.ok(ApiResponse.success("更新成功", null));
    }

    @PreAuthorize("hasAuthority('menu:system:dict')")
    @DeleteMapping("/items/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDictItem(@PathVariable String id) {
        dictService.deleteDictItem(id);
        return ResponseEntity.ok(ApiResponse.success("删除成功", null));
    }
}
