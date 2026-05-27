package com.maike.mdm.service;

import com.maike.mdm.entity.BaseRole;
import com.maike.mdm.entity.BaseUser;

import java.util.List;

public interface RoleService {

    List<BaseRole> listRoles();

    void createRole(BaseRole role);

    void updateRole(BaseRole role);

    void deleteRole(String id);

    List<String> getRoleMenuIds(String roleId);

    void assignMenus(String roleId, List<String> menuIds);

    List<BaseUser> getRoleUsers(String roleId);
}
