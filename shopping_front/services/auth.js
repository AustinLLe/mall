import { post, get } from '@/utils/request.js'

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
