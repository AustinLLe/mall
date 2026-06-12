const CART_KEY = 'shopping_cart_items'

function normalizeCartItem(item = {}) {
	const next = Object.assign({}, item)
	next.id = String(next.id || next.title || '').trim()
	next.title = String(next.title || '').trim()
	next.price = normalizePrice(next.price)
	next.qty = normalizeQty(next.qty)
	next.cover = next.cover || ''
	next.tag = next.tag || ''
	next.credit = next.credit || ''
	next.shopName = next.shopName || '松果集市卖家'
	next.scene = next.scene || 'used'
	next.checked = next.checked !== false
	next.valid = next.valid !== false
	return next
}

function readCart() {
	try {
		const list = uni.getStorageSync(CART_KEY)
		if (!Array.isArray(list)) return []
		const normalized = list.map((item) => normalizeCartItem(item)).filter((item) => item.id && item.title)
		if (JSON.stringify(normalized) !== JSON.stringify(list)) {
			writeCart(normalized)
		}
		return normalized
	} catch (e) {
		return []
	}
}

function writeCart(list) {
	uni.setStorageSync(CART_KEY, list)
}

function normalizePrice(value) {
	const n = Number(value)
	return Number.isFinite(n) ? Number(n.toFixed(2)) : 0
}

function normalizeQty(value) {
	const n = parseInt(value, 10)
	return Number.isFinite(n) && n > 0 ? n : 1
}

export function getCartItems() {
	return readCart()
}

export function addCartItem(payload = {}) {
	const item = normalizeCartItem(payload)
	if (!item.title) {
		throw new Error('商品标题不能为空')
	}
	const list = readCart()
	const found = list.find((current) => current.id === item.id)
	if (found) {
		found.qty = normalizeQty(found.qty + normalizeQty(item.qty || 1))
		found.checked = true
	} else {
		list.unshift(item)
	}
	writeCart(list)
	return list
}

export function updateCartItem(id, patch = {}) {
	const list = readCart()
	const index = list.findIndex((item) => item.id === id)
	if (index < 0) return list
	const next = normalizeCartItem(Object.assign({}, list[index], patch))
	list.splice(index, 1, next)
	writeCart(list)
	return list
}

export function removeCartItem(id) {
	const list = readCart().filter((item) => item.id !== id)
	writeCart(list)
	return list
}

export function clearCheckedCartItems() {
	const list = readCart().filter((item) => !item.checked)
	writeCart(list)
	return list
}

export function getCartCount() {
	return readCart().reduce((sum, item) => sum + normalizeQty(item.qty), 0)
}

export function groupCartByShop(items = []) {
	const map = {}
	items.forEach((item) => {
		const key = item.shopName || '松果集市卖家'
		if (!map[key]) {
			map[key] = []
		}
		map[key].push(item)
	})
	return Object.keys(map).map((shopName) => ({ shopName, items: map[shopName] }))
}
