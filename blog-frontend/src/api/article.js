import request from './request'

export const getArticles = (params) => request.get('/articles', { params })
export const getAdminArticles = (params) => request.get('/articles/admin/list', { params })
export const getArticleBySlug = (slug) => request.get(`/articles/${slug}`)
export const createArticle = (data) => request.post('/articles', data)
export const updateArticle = (id, data) => request.put(`/articles/${id}`, data)
export const togglePublish = (id) => request.put(`/articles/${id}/publish`)
export const deleteArticle = (id) => request.delete(`/articles/${id}`)
export const recordView = (id) => request.post(`/stats/articles/${id}/view`)
export const searchArticles = (params) => request.get('/articles/search', { params })
export const likeArticle = (id) => request.post(`/articles/${id}/like`)
export const getArticleNeighbors = (slug) => request.get(`/articles/${slug}/neighbors`)
export const getArticleById = (id) => request.get(`/articles/admin/${id}`)
