import instance from './axios'

export const getCurrentUser = () => instance.get('/auth/me')
export const updateProfile = (data: { nickname?: string; email?: string; phone?: string }) =>
  instance.put('/auth/profile', data)
export const changePassword = (data: { oldPassword: string; newPassword: string }) =>
  instance.put('/auth/change-password', data)
