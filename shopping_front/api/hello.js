import { get } from '@/utils/request.js'

/** 联通测试：GET /api/hello */
export function fetchHello() {
	return get('/api/hello').then((res) => res.data)
}
