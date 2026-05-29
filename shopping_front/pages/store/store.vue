<template>
	<view class="safe-page">
		<view class="content-wrap page">
			<view class="store-head">
				<view class="avatar">{{ store.name.slice(0, 1) }}</view>
				<view class="store-main">
					<text class="eyebrow">Verified Store</text>
					<text class="name">{{ store.name }}</text>
					<text class="desc">{{ store.desc }}</text>
					<view class="meta"><text>评分 {{ store.score }}</text><text>{{ store.fans }} 关注</text><text>{{ store.badge }}</text></view>
				</view>
				<button class="follow-btn" @click="followCurrent">关注</button>
			</view>
			<view class="store-stats">
				<view v-for="item in stats" :key="item.label" class="stat">
					<text class="stat-value">{{ item.value }}</text>
					<text class="stat-label">{{ item.label }}</text>
				</view>
			</view>
			<view class="grid">
				<view v-for="item in goods" :key="item.id" class="goods" @click="open(item)">
					<view class="cover" :class="{ 'has-image': isImageCover(item.cover) }">
						<image v-if="isImageCover(item.cover)" class="cover-img" :src="item.cover" mode="aspectFill"></image>
						<text v-else>{{ item.cover }}</text>
					</view>
					<view class="tags"><text>{{ item.scene === 'new' ? '新品' : '二手' }}</text><text>信用 {{ item.credit }}</text></view>
					<text class="title">{{ item.title }}</text>
					<text class="price">¥{{ item.price }}</text>
				</view>
			</view>
			<view v-if="!goods.length" class="empty">该店铺暂无在售商品，已为你保留关注入口。</view>
		</view>
	</view>
</template>
<script>
	import { hotStores, findStoreByName, productsByStore, buildGoodsDetailUrl } from '../../data/catalog.js'
	import { addBuyerItem } from '@/services/center.js'
	import { fetchProducts, fetchStores } from '@/services/shop.js'

	export default {
		data() {
			return {
				store: hotStores[0],
				goodsList: productsByStore(hotStores[0].name),
				queryName: ''
			}
		},
		computed: {
			goods() {
				return this.goodsList.length ? this.goodsList : productsByStore(this.store.name)
			},
			stats() {
				return [
					{ label: '在售商品', value: this.goods.length },
					{ label: '店铺评分', value: this.store.score },
					{ label: '服务标签', value: this.store.badge }
				]
			}
		},
		onLoad(q) {
			this.queryName = q && q.name ? decodeURIComponent(q.name) : ''
			this.store = findStoreByName(this.queryName) || hotStores[0]
			this.goodsList = productsByStore(this.store.name)
			this.loadStore()
		},
		methods: {
			isImageCover(cover) {
				return typeof cover === 'string' && (cover.startsWith('/static/') || cover.startsWith('http'))
			},
			async loadStore() {
				try {
					const storesBody = await fetchStores()
					if (storesBody && storesBody.code === 0 && Array.isArray(storesBody.data)) {
						this.store = storesBody.data.find((item) => item.name === this.queryName) || storesBody.data[0] || this.store
					}
					const productsBody = await fetchProducts({ keyword: this.store.name })
					if (productsBody && productsBody.code === 0 && Array.isArray(productsBody.data)) {
						this.goodsList = productsBody.data.filter((item) => item.shopName === this.store.name)
					}
				} catch (e) {
					this.goodsList = productsByStore(this.store.name)
				}
			},
			open(item) { uni.navigateTo({ url: buildGoodsDetailUrl(item) }) },
			async followCurrent() {
				try {
					await addBuyerItem('follow', { storeName: this.store.name })
					uni.showToast({ title: '已关注店铺', icon: 'success' })
				} catch (e) {
					uni.showToast({ title: '请先登录买家账号', icon: 'none' })
				}
			}
		}
	}
</script>
<style lang="scss" scoped>
	.page { padding: 28rpx; }
	.store-head { display: flex; gap: 24rpx; background: #fff; border-radius: 8px; padding: 34rpx; border: 1rpx solid #e4e9e5; align-items: center; box-shadow: 0 18rpx 52rpx rgba(17, 38, 28, 0.08); }
	.avatar { width: 104rpx; height: 104rpx; border-radius: 8px; background: linear-gradient(135deg, #12372a, #2563eb); color: #fff; display: flex; align-items: center; justify-content: center; font-size: 42rpx; font-weight: 900; }
	.store-main { flex: 1; min-width: 0; }
	.eyebrow, .name, .desc, .stat-value, .stat-label { display: block; }
	.eyebrow { margin-bottom: 8rpx; color: #2563eb; font-size: 22rpx; font-weight: 900; }
	.name { font-size: 40rpx; font-weight: 900; color: #17231d; }
	.desc { margin-top: 8rpx; font-size: 25rpx; color: #667085; line-height: 1.6; }
	.meta { display: flex; gap: 12rpx; flex-wrap: wrap; margin-top: 14rpx; color: #1f5c43; font-size: 24rpx; }
	.meta text { padding: 8rpx 14rpx; border-radius: 999rpx; background: #f5f7fa; }
	.follow-btn { margin-left: auto; width: 146rpx; height: 70rpx; line-height: 70rpx; border-radius: 8px; background: #12372a; color: #fff; font-size: 24rpx; font-weight: 900; }
	.store-stats { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 16rpx; margin-top: 20rpx; }
	.stat { background: #fff; border: 1rpx solid #e4e9e5; border-radius: 8px; padding: 24rpx; box-shadow: 0 12rpx 34rpx rgba(17, 38, 28, 0.05); }
	.stat-value { font-size: 32rpx; font-weight: 900; color: #17231d; }
	.stat-label { margin-top: 6rpx; font-size: 23rpx; color: #667085; }
	.grid { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 18rpx; margin-top: 22rpx; }
	.goods { background: #fff; border-radius: 8px; padding: 20rpx; border: 1rpx solid #e4e9e5; box-shadow: 0 14rpx 40rpx rgba(17, 38, 28, 0.06); }
	.cover { height: 160rpx; border-radius: 8px; background: linear-gradient(135deg, #f5f7fa, #edf3ef); display: flex; align-items: center; justify-content: center; font-size: 64rpx; overflow: hidden; }
	.cover.has-image { background: #eef7f1; }
	.cover-img { width: 100%; height: 100%; display: block; }
	.tags { display: flex; gap: 8rpx; margin-top: 14rpx; flex-wrap: wrap; }
	.tags text { padding: 6rpx 10rpx; border-radius: 999rpx; background: #eaf1ff; color: #2563eb; font-size: 20rpx; font-weight: 800; }
	.title, .price { display: block; margin-top: 12rpx; }
	.title { font-size: 26rpx; font-weight: 900; color: #17231d; line-height: 1.35; }
	.price { color: #d66a2c; font-size: 30rpx; font-weight: 900; }
	.empty { margin-top: 20rpx; padding: 28rpx; border-radius: 8px; background: #fff; color: #667085; border: 1rpx solid #e4e9e5; }
	@media screen and (max-width: 900px) { .store-head { align-items: flex-start; } .grid, .store-stats { grid-template-columns: repeat(2, minmax(0, 1fr)); } }
</style>
