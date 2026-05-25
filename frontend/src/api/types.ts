export interface ApiResponse<T> {
  code: string
  message: string
  data: T
  timestamp: string
}

export interface BaseUser {
  id: string
  username: string
  password?: string
  nickname: string
  sex: string
  orgId: string
  orgName: string
  birthday?: string
  email: string
  phone: string
  status: string
  securityLevel: string
  createTime?: string
  updateTime?: string
}

export interface BaseOrg {
  id: string
  orgName: string
  parentId: string
  status: string
  createTime?: string
  updateTime?: string
}

export interface MdmDataModel {
  id: string
  modelCode: string
  modelName: string
  modelType: string
  status: string
  version: number
  orgId?: string
  description?: string
  createBy?: string
  createTime?: string
  updateTime?: string
  isDeleted?: number
}

export interface MdmMainData {
  id: string
  modelId: string
  code: string
  dataStatus: string
  flowStatus: string
  version: number
  jsonData?: string
  createdById?: string
  createdByName?: string
  createdByOrgId?: string
  submittedByOrgId?: string
  modifiedById?: string
  createTime?: string
  modifyTime?: string
  isModify?: number
  securityLevel?: string
  isDeleted?: number
}

export interface MdmWorkflow {
  id: string
  workflowCode: string
  workflowName: string
  workflowType?: string
  orgId?: string
  status: string
  definition?: string
  createBy?: string
  createTime?: string
  updateTime?: string
  isDeleted?: number
}

export interface MdmModelAttribute {
  id: string
  modelId: string
  attributeCode: string
  attributeName: string
  dataType: string
  isRequired: number
  maxLength?: number
  defaultValue?: string
  displayType?: string
  sortOrder?: number
  createTime?: string
  updateTime?: string
  isDeleted?: number
}

export interface MdmWorkflowInstance {
  id: string
  workflowId: string
  businessId: string
  businessType?: string
  currentNode?: string
  status?: string
  initiatorId?: string
  createTime?: string
  updateTime?: string
  isDeleted?: number
}
