package com.maike.mdm.controller;

import com.maike.mdm.common.response.ApiResponse;
import com.maike.mdm.common.util.PasswordUtil;
import com.maike.mdm.dto.request.LoginRequest;
import com.maike.mdm.dto.response.LoginResponse;
import com.maike.mdm.entity.BaseUser;
import com.maike.mdm.mapper.BaseUserMapper;
import com.maike.mdm.service.AuthService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final BaseUserMapper userMapper;
    private final PasswordUtil passwordUtil;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("登录成功", response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestHeader("Authorization") String token) {
        authService.logout(token);
        return ResponseEntity.ok(ApiResponse.success("登出成功", null));
    }

    /** 获取当前登录用户信息 */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<BaseUser>> getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        BaseUser user = userMapper.selectOne(
                new LambdaQueryWrapper<BaseUser>().eq(BaseUser::getUsername, username));
        if (user != null) {
            user.setPassword(null); // 不返回密码
        }
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    /** 修改个人信息（昵称、邮箱、手机） */
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<Void>> updateProfile(@RequestBody Map<String, String> body) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        BaseUser user = userMapper.selectOne(
                new LambdaQueryWrapper<BaseUser>().eq(BaseUser::getUsername, username));
        if (user == null) {
            return ResponseEntity.status(404).body(ApiResponse.error("404", "用户不存在"));
        }
        if (body.containsKey("nickname")) user.setNickname(body.get("nickname"));
        if (body.containsKey("email")) user.setEmail(body.get("email"));
        if (body.containsKey("phone")) user.setPhone(body.get("phone"));
        userMapper.updateById(user);
        return ResponseEntity.ok(ApiResponse.success("更新成功", null));
    }

    /** 修改密码（需验证旧密码） */
    @PutMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(@RequestBody Map<String, String> body) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        BaseUser user = userMapper.selectOne(
                new LambdaQueryWrapper<BaseUser>().eq(BaseUser::getUsername, username));
        if (user == null) {
            return ResponseEntity.status(404).body(ApiResponse.error("404", "用户不存在"));
        }

        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");

        if (oldPassword == null || newPassword == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("400", "参数不完整"));
        }

        // 验证旧密码（支持BCrypt密文和明文两种格式）
        boolean passwordMatch = passwordUtil.matches(oldPassword, user.getPassword())
                || oldPassword.equals(user.getPassword());
        if (!passwordMatch) {
            return ResponseEntity.badRequest().body(ApiResponse.error("400", "原密码错误"));
        }

        // 新密码使用BCrypt加密存储
        user.setPassword(passwordUtil.encode(newPassword));
        userMapper.updateById(user);
        return ResponseEntity.ok(ApiResponse.success("密码修改成功", null));
    }
}