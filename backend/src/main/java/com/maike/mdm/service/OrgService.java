package com.maike.mdm.service;

import com.maike.mdm.entity.BaseOrg;

import java.util.List;

public interface OrgService {

    BaseOrg createOrg(BaseOrg org);

    BaseOrg getOrgById(String id);

    List<BaseOrg> getAllOrgs();

    BaseOrg updateOrg(String id, BaseOrg org);

    void deleteOrg(String id);

    void changeOrgStatus(String id, String status);

    List<BaseOrg> getOrgTree(String parentId);
}