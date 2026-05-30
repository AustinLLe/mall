import { goodsCatalog } from '../data/catalog.js'

const CART_KEY = 'shopping_cart_items'

function isImageCover(cover) {
	return typeof cover === 'string' && (cover.startsWith('/static/') || cover.startsWith('http'))
}

function findCatalogItem(item = {}) {
	const id = String(item.id || '').trim()
	const title = String(item.title || '').trim()
	if (!id && !title) return null
	return goodsCatalog.find((goods) => {
		return goods.id === id || goods.title === title || goods.title === id || (title && title.includes(goods.title)) || (title && goods.title.includes(title))
	}) || null
}

function normalizeCartItem(item = {}) {
	const source = findCatalogItem(item)
	if (!source) return item
	const next = Object.assign({}, item)
	if (!isImageCover(next.cover) && source.cover) next.cover = source.cover
	if (!next.id || next.id === next.title) next.id = source.id
	if (!next.title) next.title = source.title
	if (!next.tag) next.tag = source.tag || ''
	if (!next.credit) next.credit = source.credit || ''
	if (!next.shopName) next.shopName = source.shopName || '松果集市卖家'
	if (!next.scene) next.scene = source.scene || 'used'
	return next
}

function readCart() {
	try {
		const list = uni.getStorageSync(CART_KEY)
		if (!Array.isArray(list)) return []
		const normalized = list.map((item) => normalizeCartItem(item))
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
	const title = (payload.title || '').trim()
	if (!title) {
		throw new Error('商品标题不能为空')
	}
	const source = findCatalogItem(payload)
	const id = (payload.id || (source && source.id) || title).trim()
	const list = readCart()
	const found = list.find((item) => item.id === id)
	if (found) {
		found.qty = normalizeQty(found.qty + normalizeQty(payload.qty || 1))
		found.checked = true
	} else {
		list.unshift({
			id,
			title: title || (source && source.title) || '',
			price: normalizePrice(payload.price),
			cover: payload.cover || (source && source.cover) || '📦',
			tag: payload.tag || (source && source.tag) || '',
			credit: payload.credit || (source && source.credit) || '',
			shopName: payload.shopName || (source && source.shopName) || '松果集市卖家',
			scene: payload.scene || (source && source.scene) || 'used',
			qty: normalizeQty(payload.qty),
			checked: true,
			valid: payload.valid !== false
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
