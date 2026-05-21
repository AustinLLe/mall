const TOKEN_KEY = 'auth_token'
const USER_KEY = 'auth_user'

export function getToken() {
	try {
		return uni.getStorageSync(TOKEN_KEY) || ''
	} catch (e) {
		return ''
	}
}

export function setSession(token, user) {
	uni.setStorageSync(TOKEN_KEY, token)
	uni.setStorageSync(USER_KEY, user || {})
}

export function clearSession() {
	uni.removeStorageSync(TOKEN_KEY)
	uni.removeStorageSync(USER_KEY)
}

export function getCachedUser() {
	try {
		return uni.getStorageSync(USER_KEY) || null
	} catch (e) {
		return null
	}
}

export function pickErrorMessage(err) {
	if (!err) return 'Request failed'
	const body = err.body
	if (body && typeof body === 'object' && body.message) return body.message
	if (typeof body === 'string' && body.length) return body
	if (err.errMsg) return err.errMsg
	return String(err.message || err)
}
