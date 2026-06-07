import { buildRequestUrl } from '@/config/env.js'
import { getToken } from '@/utils/auth.js'

/**
 * 统一封装 uni.request，后续可加 token、超时、错误码处理等。
 */
export function request(options = {}) {
	const { url, header, ...rest } = options
	if (!url) {
		return Promise.reject(new Error('request: url 不能为空'))
	}
	const token = getToken()
	const mergedHeader = Object.assign({}, header || {})
	if (token) {
		mergedHeader.Authorization = `Bearer ${token}`
	}
	if (rest.method && rest.method !== 'GET' && !mergedHeader['Content-Type']) {
		mergedHeader['Content-Type'] = 'application/json'
	}
	return new Promise((resolve, reject) => {
		uni.request({
			url: buildRequestUrl(url),
			header: mergedHeader,
			...rest,
			success: (res) => {
				if (res.statusCode >= 200 && res.statusCode < 300) {
					resolve(res)
				} else {
					const err = new Error(`HTTP ${res.statusCode}`)
					err.statusCode = res.statusCode
					err.body = res.data
					reject(err)
				}
			},
			fail: (err) => reject(err)
		})
	})
}

export function get(url, options = {}) {
	return request({ ...options, url, method: 'GET' })
}

export function post(url, data, options = {}) {
	return request({ ...options, url, method: 'POST', data })
}

export function put(url, data, options = {}) {
	return request({ ...options, url, method: 'PUT', data })
}

export function del(url, options = {}) {
	return request({ ...options, url, method: 'DELETE' })
}
