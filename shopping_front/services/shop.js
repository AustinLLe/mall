import { buildRequestUrl } from '@/config/env.js'
import { getToken } from '@/utils/auth.js'
import { del, get, post, put } from '@/utils/request.js'

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

export function fetchMyProducts() {
	return get('/api/products/mine').then(unwrap)
}

export function fetchStores() {
	return get('/api/stores').then(unwrap)
}

export function fetchMyStore() {
	return get('/api/stores/mine').then(unwrap)
}

export function updateMyStore(payload) {
	return put('/api/stores/mine', payload).then(unwrap)
}

export function fetchStore(id) {
	return get('/api/stores/' + encodeURIComponent(id)).then(unwrap)
}

export function fetchStoreProducts(id) {
	return get('/api/stores/' + encodeURIComponent(id) + '/products').then(unwrap)
}

export function followStore(id) {
	return post('/api/stores/' + encodeURIComponent(id) + '/follow', {}).then(unwrap)
}

export function unfollowStore(id) {
	return del('/api/stores/' + encodeURIComponent(id) + '/follow').then(unwrap)
}

export function fetchTopics(params = {}) {
	const query = []
	if (params.tag) query.push('tag=' + encodeURIComponent(params.tag))
	if (params.keyword) query.push('keyword=' + encodeURIComponent(params.keyword))
	const suffix = query.length ? '?' + query.join('&') : ''
	return get('/api/topics' + suffix).then(unwrap)
}

export function createTopic(payload) {
	return post('/api/topics', payload).then(unwrap)
}

export function uploadImage(filePath) {
	return new Promise((resolve, reject) => {
		const token = getToken()
		uni.uploadFile({
			url: buildRequestUrl('/api/upload/image'),
			filePath,
			name: 'file',
			header: token ? { Authorization: `Bearer ${token}` } : {},
			success: (res) => {
				if (res.statusCode < 200 || res.statusCode >= 300) {
					reject(new Error(`HTTP ${res.statusCode}`))
					return
				}
				try {
					resolve(typeof res.data === 'string' ? JSON.parse(res.data) : res.data)
				} catch (e) {
					reject(e)
				}
			},
			fail: reject
		})
	})
}

export function fetchTopic(id) {
	return get('/api/topics/' + encodeURIComponent(id)).then(unwrap)
}

export function followTopic(id) {
	return post('/api/topics/' + encodeURIComponent(id) + '/follow', {}).then(unwrap)
}

export function unfollowTopic(id) {
	return del('/api/topics/' + encodeURIComponent(id) + '/follow').then(unwrap)
}

export function fetchTopicPosts(id) {
	return get('/api/topics/' + encodeURIComponent(id) + '/posts').then(unwrap)
}

export function createTopicPost(id, payload) {
	return post('/api/topics/' + encodeURIComponent(id) + '/posts', payload).then(unwrap)
}

export function createTopicComment(postId, payload) {
	return post('/api/topic-posts/' + encodeURIComponent(postId) + '/comments', payload).then(unwrap)
}

export function toggleTopicPostLike(postId) {
	return post('/api/topic-posts/' + encodeURIComponent(postId) + '/like', {}).then(unwrap)
}

export function toggleTopicPostAction(postId, actionType) {
	return post('/api/topic-posts/' + encodeURIComponent(postId) + '/action', { actionType }).then(unwrap)
}

export function fetchOrders() {
	return get('/api/orders').then(unwrap)
}

export function createOrders(items = []) {
	return post('/api/orders', { items }).then(unwrap)
}

export function submitOrderReview(orderId, payload) {
	return post('/api/orders/' + encodeURIComponent(orderId) + '/review', payload).then(unwrap)
}

export function publishProduct(payload) {
	return post('/api/products', payload).then(unwrap)
}

export function fetchAuditItems() {
	return get('/api/admin/audit').then(unwrap)
}

export function submitAudit(id, action, reason = '') {
	return post('/api/admin/audit/' + encodeURIComponent(id), { action, reason }).then(unwrap)
}

export function requestAiAssist(payload) {
	return post('/api/ai/assist', payload).then(unwrap)
}

export function requestPublishSuggestion(payload) {
	return post('/api/ai/publish-suggestion', payload).then(unwrap)
}
