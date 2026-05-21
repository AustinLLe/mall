const ADDRESS_KEY = 'shopping_address_list'

function readAddressList() {
	try {
		const list = uni.getStorageSync(ADDRESS_KEY)
		return Array.isArray(list) ? list : []
	} catch (e) {
		return []
	}
}

function writeAddressList(list) {
	uni.setStorageSync(ADDRESS_KEY, list)
}

function buildId() {
	return 'addr_' + Date.now() + '_' + Math.random().toString(36).slice(2, 8)
}

export function getAddressList() {
	return readAddressList()
}

export function getDefaultAddress() {
	const list = readAddressList()
	return list.find((item) => item.isDefault) || list[0] || null
}

export function saveAddress(payload = {}) {
	const list = readAddressList()
	const name = (payload.name || '').trim()
	const phone = (payload.phone || '').trim()
	const region = (payload.region || '').trim()
	const detail = (payload.detail || '').trim()
	if (!name || !phone || !region || !detail) {
		throw new Error('Address fields are required')
	}
	const next = {
		id: payload.id || buildId(),
		name,
		phone,
		region,
		detail,
		tag: (payload.tag || '').trim(),
		isDefault: !!payload.isDefault
	}
	const index = list.findIndex((item) => item.id === next.id)
	if (next.isDefault) {
		list.forEach((item) => {
			item.isDefault = false
		})
	}
	if (index >= 0) {
		list.splice(index, 1, next)
	} else {
		list.unshift(next)
	}
	if (!list.some((item) => item.isDefault) && list[0]) {
		list[0].isDefault = true
	}
	writeAddressList(list)
	return list
}

export function removeAddress(id) {
	const list = readAddressList().filter((item) => item.id !== id)
	if (list.length && !list.some((item) => item.isDefault)) {
		list[0].isDefault = true
	}
	writeAddressList(list)
	return list
}

export function setDefaultAddress(id) {
	const list = readAddressList().map((item) => ({ ...item, isDefault: item.id === id }))
	writeAddressList(list)
	return list
}
