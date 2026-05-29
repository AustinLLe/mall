/**
 * 多端 API 地址策略（与 manifest.json 里 H5 devServer.proxy 配合使用）
 *
 * - H5：使用相对路径（如 /api/hello）。开发时由 devServer 代理到本机 Spring Boot；
 *   上线由 Nginx 将同域 /api 反代到后端。
 * - 小程序 / App：默认走本机 http://127.0.0.1:8080 便于开发者工具联调；
 *   如果需要真机调试或局域网联调，可通过 storage 中的 runtime_api_base 覆盖。
 */
// #ifdef H5
const RUNTIME_API_BASE = ''
// #endif

// #ifndef H5
const DEFAULT_NON_H5_API_BASE = 'http://127.0.0.1:8080'

function readRuntimeApiBase() {
	try {
		const saved = uni.getStorageSync('runtime_api_base')
		return typeof saved === 'string' ? saved.trim() : ''
	} catch (e) {
		return ''
	}
}

const RUNTIME_API_BASE = readRuntimeApiBase() || DEFAULT_NON_H5_API_BASE
// #endif

export function buildRequestUrl(path) {
	const p = path.startsWith('/') ? path : '/' + path
	return RUNTIME_API_BASE + p
}
