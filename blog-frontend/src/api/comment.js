import request from './request'

export const getComments = (articleId) => request.get(`/articles/${articleId}/comments`)
export const createComment = (articleId, data) =>
  request.post(`/articles/${articleId}/comments`, data)
export const getAdminComments = (params) => request.get('/comments/admin', { params })
export const updateCommentStatus = (id, status) =>
  request.put(`/comments/${id}/status`, null, { params: { status } })
export const deleteComment = (id) => request.delete(`/comments/${id}`)
