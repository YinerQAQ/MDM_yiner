package com.maike.mdm.controller;

import com.maike.mdm.common.response.ApiResponse;
import com.maike.mdm.dto.request.MenuSortDTO;
import com.maike.mdm.entity.BaseMenu;
import com.maike.mdm.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/menus")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @GetMapping("/tree")
    public ResponseEntity<ApiResponse<List<BaseMenu>>> getMenuTreeByCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        // 这里简化处理，实际可通过username查询userId
        // 但为了解耦，直接返回所有菜单树
        List<BaseMenu> tree = menuService.getMenuTree();
        return ResponseEntity.ok(ApiResponse.success(tree));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<BaseMenu>>> getAllMenuTree() {
        List<BaseMenu> tree = menuService.getMenuTree();
        return ResponseEntity.ok(ApiResponse.success(tree));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createMenu(@RequestBody BaseMenu menu) {
        menuService.createMenu(menu);
        return ResponseEntity.ok(ApiResponse.success("创建成功", null));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> updateMenu(@PathVariable String id, @RequestBody BaseMenu menu) {
        menu.setId(id);
        menuService.updateMenu(menu);
        return ResponseEntity.ok(ApiResponse.success("更新成功", null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMenu(@PathVariable String id) {
        menuService.deleteMenu(id);
        return ResponseEntity.ok(ApiResponse.success("删除成功", null));
    }

    @PutMapping("/sort")
    public ResponseEntity<ApiResponse<Void>> updateSort(@RequestBody List<MenuSortDTO> sortList) {
        menuService.updateSort(sortList);
        return ResponseEntity.ok(ApiResponse.success("排序更新成功", null));
    }
}
