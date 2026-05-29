package com.maike.mdm.config;

import com.maike.mdm.common.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtUtil jwtUtil;

    /**
     * 超级管理员（permissions 包含 "all" 或 roles 包含 "admin"/"ADMIN"）应当拥有的全部权限码。
     * 该列表覆盖代码中所有 @PreAuthorize(hasAuthority('...')) 检查项，避免 500 Access Denied。
     */
    private static final Set<String> ALL_AUTHORITIES = Set.of(
            // 菜单级权限
            "menu:dashboard:view", "menu:model:view", "menu:codeRule:view",
            "menu:modelAudit:view", "menu:categoryConfig:view",
            "menu:mainData:view", "menu:dataApply:view", "menu:dataReview:view",
            "menu:dataArchive:view", "menu:dataDistribute:view", "menu:distMonitor:view",
            "menu:dataExchange:view", "menu:dataReceive:view", "menu:dataQueryService:view",
            "menu:workflow:view",
            "menu:org:view", "menu:group:view", "menu:menu:view",
            "menu:system:user", "menu:system:role", "menu:system:org",
            "menu:system:dict", "menu:system:param", "menu:system:settings",
            "menu:system:log", "menu:securityLevel:view", "menu:systemMonitor:view",
            // 模型按钮权限
            "btn:model:create", "btn:model:edit", "btn:model:delete", "btn:model:publish",
            // 模型审核按钮
            "btn:modelAudit:approve", "btn:modelAudit:reject",
            // 主数据按钮权限
            "btn:mainData:create", "btn:mainData:add", "btn:mainData:edit",
            "btn:mainData:batchEdit", "btn:mainData:delete", "btn:mainData:submit",
            "btn:mainData:withdraw", "btn:mainData:approve", "btn:mainData:reject",
            "btn:mainData:archive", "btn:mainData:change", "btn:mainData:export",
            "btn:mainData:import",
            // 数据评审按钮
            "btn:dataReview:approve", "btn:dataReview:reject",
            "btn:dataReview:transfer", "btn:dataReview:claim",
            // 流程按钮
            "btn:workflow:create", "btn:workflow:edit", "btn:workflow:delete",
            // ESB 按钮
            "btn:esb:dist:create", "btn:esb:dist:execute",
            "btn:esb:distCreate", "btn:esb:distExecute",
            // 数据接收/查询服务按钮
            "btn:dataReceive:create", "btn:dataReceive:toggle",
            "btn:queryService:create", "btn:queryService:toggle",
            // 用户管理按钮
            "btn:user:create", "btn:user:update", "btn:user:delete",
            "btn:user:enable", "btn:user:disable", "btn:user:resetPassword",
            // 单位管理按钮
            "btn:org:create", "btn:org:update", "btn:org:delete", "btn:org:enable",
            // 角色管理按钮
            "btn:role:create", "btn:role:update", "btn:role:delete", "btn:role:assign",
            // 菜单管理按钮
            "btn:menu:create", "btn:menu:update", "btn:menu:delete",
            // 密级配置按钮
            "btn:securityLevel:create", "btn:securityLevel:update", "btn:securityLevel:delete"
    );

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setContentType("application/json;charset=UTF-8");
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.getWriter().write("{\"code\":\"401\",\"message\":\"未登录或登录已过期\",\"data\":null}");
                        })
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/login", "/api/v1/auth/logout").permitAll()
                        .requestMatchers("/swagger-ui/**", "/api-docs/**").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173", "http://localhost:8080"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    private jakarta.servlet.Filter jwtAuthenticationFilter() {
        return new jakarta.servlet.Filter() {
            @Override
            public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
                HttpServletRequest httpRequest = (HttpServletRequest) request;
                String authHeader = httpRequest.getHeader("Authorization");

                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    String token = authHeader.substring(7);
                    if (jwtUtil.validateToken(token)) {
                        try {
                            Claims claims = jwtUtil.extractAllClaims(token);
                            String username = claims.getSubject();

                            // 从Claims提取roles和permissions
                            List<String> roles = claims.get("roles", List.class);
                            List<String> permissions = claims.get("permissions", List.class);

                            // 构建GrantedAuthority列表
                            List<GrantedAuthority> authorities = new ArrayList<>();
                            boolean isSuperAdmin = false;
                            if (roles != null) {
                                for (String r : roles) {
                                    if (r == null || r.isEmpty()) continue;
                                    // 数据库中角色编码已带 ROLE_ 前缀（如 ROLE_ADMIN），避免重复前缀
                                    String roleAuthority = r.startsWith("ROLE_") ? r : "ROLE_" + r;
                                    authorities.add(new SimpleGrantedAuthority(roleAuthority));
                                    // 仅 ROLE_ADMIN（运维管理员）视为超级管理员
                                    if ("ROLE_ADMIN".equalsIgnoreCase(r) || "admin".equalsIgnoreCase(r)) {
                                        isSuperAdmin = true;
                                    }
                                }
                            }
                            if (permissions != null) {
                                for (String p : permissions) {
                                    if (p == null || p.isEmpty()) continue;
                                    authorities.add(new SimpleGrantedAuthority(p));
                                    if ("all".equalsIgnoreCase(p)) {
                                        isSuperAdmin = true;
                                    }
                                }
                            }
                            // 仅超级管理员（ROLE_ADMIN）补全 ALL_AUTHORITIES，
                            // 普通角色（DATA_ADMIN / AUDITOR / APPLICANT 等）只持有自己角色对应的 PERMS
                            if (isSuperAdmin) {
                                for (String auth : ALL_AUTHORITIES) {
                                    authorities.add(new SimpleGrantedAuthority(auth));
                                }
                            }

                            org.springframework.security.core.userdetails.User user =
                                new org.springframework.security.core.userdetails.User(username, "", authorities);
                            org.springframework.security.authentication.UsernamePasswordAuthenticationToken auth =
                                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                                    user, null, authorities);
                            org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);
                        } catch (Exception e) {
                            // Token解析失败，不带权限继续
                            org.springframework.security.core.userdetails.User user =
                                new org.springframework.security.core.userdetails.User(
                                    jwtUtil.extractUsername(token), "",
                                    java.util.Collections.emptyList());
                            org.springframework.security.authentication.UsernamePasswordAuthenticationToken auth =
                                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                                    user, null, java.util.Collections.emptyList());
                            org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);
                        }
                    }
                }
                chain.doFilter(request, response);
            }
        };
    }
}