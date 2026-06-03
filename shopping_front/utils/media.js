import { buildRequestUrl } from '@/config/env.js'

/** 判断是否为可展示的图片地址（含本地上传 /files/） */
export function isImageUrl(url) {
	if (typeof url !== 'string') return false
	const value = url.trim()
	if (!value) return false
	if (value.startsWith('data:image/')) return true
	if (/^https?:\/\//i.test(value)) return true
	if (value.startsWith('/static/') || value.startsWith('static/')) return true
	if (value.startsWith('/files/') || value.startsWith('files/')) return true
	if (/^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}_/i.test(value)) return true
	return false
}

/** 将封面路径转为可加载的完整 URL */
export function resolveImageUrl(url) {
	if (typeof url !== 'string') return ''
	const value = url.trim()
	if (!value) return ''
	if (value.startsWith('data:') || /^https?:\/\//i.test(value)) return value
	if (value.startsWith('/static/') || value.startsWith('static/')) {
		return value.startsWith('/') ? value : '/' + value
	}
	if (value.startsWith('/files/')) return buildRequestUrl(value)
	if (value.startsWith('files/')) return buildRequestUrl('/' + value)
	if (/^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}_/i.test(value)) {
		return buildRequestUrl('/files/' + value)
	}
	if (value.startsWith('/')) return buildRequestUrl(value)
	return value
}
