import { buildRequestUrl } from '@/config/env.js'
import { getToken } from '@/utils/auth.js'
import { post, get, put } from '@/utils/request.js'

export function loginByPassword(payload) {
	return post('/api/auth/login', payload).then((res) => res.data)
}

export function register(payload) {
	return post('/api/auth/register', payload).then((res) => res.data)
}

export function registerByPassword(payload) {
	return register(payload)
}

export function fetchMe() {
	return get('/api/auth/me').then((res) => res.data)
}

export function updateProfile(payload) {
	return put('/api/auth/me/profile', payload).then((res) => res.data)
}

export function uploadAvatarImage(filePath) {
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
