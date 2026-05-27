package com.maike.mdm.service;

import com.maike.mdm.dto.request.OrgPermDTO;
import com.maike.mdm.entity.BaseGroup;
import com.maike.mdm.entity.BaseUser;

import java.util.List;

public interface GroupService {

    List<BaseGroup> listGroups();

    void createGroup(BaseGroup group);

    void updateGroup(BaseGroup group);

    void deleteGroup(String id);

    List<String> getGroupOrgIds(String groupId);

    void assignOrgs(String groupId, List<OrgPermDTO> orgPerms);

    List<BaseUser> getGroupUsers(String groupId);

    void addUsersToGroup(String groupId, List<String> userIds);

    void removeUserFromGroup(String groupId, String userId);
}
