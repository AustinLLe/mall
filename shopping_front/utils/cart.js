const CART_KEY = 'shopping_cart_items'

function readCart() {
	try {
		const list = uni.getStorageSync(CART_KEY)
		return Array.isArray(list) ? list : []
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
	const title = (payload.goodsName || '').trim()
	if (!title) {
		throw new Error('商品标题不能为空')
	}
	const id = (payload.id || title).trim()
	const list = readCart()
	const found = list.find((item) => item.id === id)
	if (found) {
		found.qty = normalizeQty(found.qty + normalizeQty(payload.qty || 1))
		found.checked = true
	} else {
		list.unshift({
			id,
			goodsName: title,
			price: normalizePrice(payload.price),
			image: payload.image || '🛍',
			tag: payload.tag || '',
			credit: payload.credit || '',
			qty: normalizeQty(payload.qty),
			checked: true
		})
	}
	writeCart(list)
	return list
}

export function updateCartItem(id, patch = {}) {
	const list = readCart()
	const index = list.findIndex((item) => item.id === id)
	if (index < 0) return list
	const next = Object.assign({}, list[index], patch)
	if (patch.price != null) next.price = normalizePrice(patch.price)
	if (patch.qty != null) next.qty = normalizeQty(patch.qty)
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
