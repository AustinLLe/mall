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

export function normalizeRole(role) {
	const value = String(role || '').trim().toLowerCase()
	if (value === 'seller' || value === '卖家') return 'seller'
	if (value === 'admin' || value === '管理员') return 'admin'
	return 'buyer'
}

export function roleHomePath(role) {
	const normalized = normalizeRole(role)
	if (normalized === 'seller') return '/pages/seller/dashboard'
	if (normalized === 'admin') return '/pages/admin/dashboard'
	return '/pages/home/home'
}

export function goRoleHome(user, mode = 'navigateTo') {
	const url = roleHomePath(user && user.role)
	if (mode === 'redirectTo') {
		uni.redirectTo({ url })
		return
	}
	if (mode === 'reLaunch') {
		uni.reLaunch({ url })
		return
	}
	uni.navigateTo({ url })
}

export function pickErrorMessage(err) {
	if (!err) return 'Request failed'
	const body = err.body
	if (body && typeof body === 'object' && body.message) return body.message
	if (typeof body === 'string' && body.length) return body
	if (err.errMsg) return err.errMsg
	return String(err.message || err)
}
