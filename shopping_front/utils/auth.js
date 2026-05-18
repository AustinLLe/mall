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
	if (!err) return '请求失败'
	const b = err.body
	if (b && typeof b === 'object' && b.message) return b.message
	if (typeof b === 'string' && b.length) return b
	if (err.errMsg) return err.errMsg
	return String(err.message || err)
}
