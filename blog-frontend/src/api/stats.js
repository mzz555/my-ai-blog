import request from './request'

export const getOverview = () => request.get('/stats/overview')
export const getTrend   = () => request.get('/stats/trend')
