import { get, post } from '@/utils/request.js'

function unwrap(res) {
	return res.data
}

export function fetchProducts(params = {}) {
	const query = []
	if (params.scene) query.push('scene=' + encodeURIComponent(params.scene))
	if (params.keyword) query.push('keyword=' + encodeURIComponent(params.keyword))
	const suffix = query.length ? '?' + query.join('&') : ''
	return get('/api/products' + suffix).then(unwrap)
}

export function fetchProduct(id) {
	return get('/api/products/' + encodeURIComponent(id)).then(unwrap)
}

export function fetchStores() {
	return get('/api/stores').then(unwrap)
}

export function fetchTopics() {
	return get('/api/topics').then(unwrap)
}

export function fetchOrders() {
	return get('/api/orders').then(unwrap)
}

export function publishProduct(payload) {
	return post('/api/products', payload).then(unwrap)
}

export function fetchAuditItems() {
	return get('/api/admin/audit').then(unwrap)
}

export function submitAudit(id, status) {
	return post('/api/admin/audit/' + encodeURIComponent(id), { status }).then(unwrap)
}

export function requestAiAssist(payload) {
	return post('/api/ai/assist', payload).then(unwrap)
}
