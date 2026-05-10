import request from './request'

export const getUsers = (params) => request.get('/admin/users', { params })
export const updateUserStatus = (id, status) => request.put(`/admin/users/${id}/status`, null, { params: { status } })
export const updateUser = (id, data) => request.put(`/admin/users/${id}`, data)
export const createUser = (data) => request.post('/admin/users', data)
