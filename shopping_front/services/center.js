import { del, get, post, put } from '@/utils/request.js'

export function fetchBuyerCenter() {
	return get('/api/center/buyer').then((res) => res.data)
}

export function fetchSellerCenter() {
	return get('/api/center/seller').then((res) => res.data)
}

export function fetchAdminCenter() {
	return get('/api/center/admin').then((res) => res.data)
}

export function submitRealName(payload) {
	return post('/api/center/buyer/realname', payload).then((res) => res.data)
}

export function cancelRealName() {
	return put('/api/center/buyer/realname/cancel', {}).then((res) => res.data)
}

export function submitSellerRealName(payload) {
	return post('/api/center/seller/realname', payload).then((res) => res.data)
}

export function cancelSellerRealName() {
	return put('/api/center/seller/realname/cancel', {}).then((res) => res.data)
}

export function fetchBuyerItems(type) {
	return get(`/api/center/buyer/items/${type}`).then((res) => res.data)
}

export function addBuyerItem(type, payload = {}) {
	return post(`/api/center/buyer/items/${type}`, payload).then((res) => res.data)
}

export function clearBuyerItems(type) {
	return put(`/api/center/buyer/items/${type}/clear`, {}).then((res) => res.data)
}

export function updateUserStatus(userId, status) {
	return put(`/api/center/admin/users/${userId}/status`, { status }).then((res) => res.data)
}

export function adjustUserCredit(userId, changeValue, reason) {
	return put(`/api/center/admin/users/${userId}/credit`, { changeValue, reason }).then((res) => res.data)
}

export function deleteUser(userId) {
	return del(`/api/center/admin/users/${userId}`).then((res) => res.data)
}

export function approveRealName(id) {
	return put(`/api/center/admin/realname/${id}/approve`, {}).then((res) => res.data)
}

export function rejectRealName(id, reason) {
	return put(`/api/center/admin/realname/${id}/reject`, { reason }).then((res) => res.data)
}
