package com.maike.mdm.service;

import com.maike.mdm.dto.request.UserCreateRequest;
import com.maike.mdm.entity.BaseUser;

import java.util.List;

public interface UserService {

    BaseUser createUser(UserCreateRequest request);

    BaseUser getUserById(String id);

    BaseUser getUserByUsername(String username);

    List<BaseUser> getAllUsers();

    BaseUser updateUser(String id, UserCreateRequest request);

    void deleteUser(String id);

    void changeUserStatus(String id, String status);

    void resetPassword(String id, String newPassword);

    void updateUserPassword(String id, String encodedPassword);
}