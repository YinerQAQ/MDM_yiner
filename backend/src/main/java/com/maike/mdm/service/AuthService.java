package com.maike.mdm.service;

import com.maike.mdm.dto.request.LoginRequest;
import com.maike.mdm.dto.response.LoginResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    void logout(String token);
}