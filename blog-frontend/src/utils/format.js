import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

export const formatDate = (date) => dayjs(date).format('YYYY-MM-DD')
export const formatDateTime = (date) => dayjs(date).format('YYYY-MM-DD HH:mm')
export const fromNow = (date) => dayjs(date).fromNow()
