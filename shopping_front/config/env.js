/**
 * 多端 API 地址策略（与 manifest.json 里 H5 devServer.proxy 配合使用）
 *
 * - H5：使用相对路径（如 /api/hello）。开发时由 devServer 代理到本机 Spring Boot；
 *   上线由 Nginx 将同域 /api 反代到后端。
 * - 小程序 / App：将下方常量改为已备案的 HTTPS API 根地址（无尾斜杠）。
 */
// #ifdef H5
const RUNTIME_API_BASE = ''
// #endif

// #ifndef H5
const RUNTIME_API_BASE = 'http://10.136.46.7:8080'
// #endif

export function buildRequestUrl(path) {
	const p = path.startsWith('/') ? path : '/' + path
	return RUNTIME_API_BASE + p
}
