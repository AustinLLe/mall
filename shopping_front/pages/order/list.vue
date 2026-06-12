<template>
	<view class="safe-page">
		<view class="content-wrap page">
			<text class="title">我的订单</text>
			<view class="tabs">
				<view v-for="tab in tabs" :key="tab" class="tab" :class="{ on: active === tab }" @click="active = tab">{{ tab }}</view>
			</view>

			<view v-for="order in filteredOrders" :key="order.id" class="card">
				<view class="top">
					<text class="shop">{{ order.shop || '卖家' }}</text>
					<text class="status">{{ order.status }}</text>
				</view>
				<view class="body" @click="openProduct(order)">
					<view class="cover" :class="{ 'has-image': isImageUrl(order.cover) }">
						<image v-if="isImageUrl(order.cover)" class="cover-img" :src="resolveImageUrl(order.cover)" mode="aspectFill"></image>
						<text v-else>{{ order.type || '商品' }}</text>
					</view>
					<view class="main">
						<text class="name">{{ order.title }}</text>
						<text class="meta">{{ order.type }} · {{ order.service || '平台担保' }}</text>
						<text class="price">¥{{ order.amount }}</text>
					</view>
				</view>

				<view v-if="order.reviewed" class="reviewed-box">
					<text>商品 {{ order.productScore || 5 }} 星 · 卖家 {{ order.sellerScore || 5 }} 星</text>
					<text class="review-text">{{ order.reviewContent || '已完成评价' }}</text>
				</view>

				<view class="actions">
					<view class="ghost" @click="goLogistics">物流跟踪</view>
					<view v-if="order.reviewable" class="primary" @click="goReview(order)">去评价</view>
					<view v-else-if="order.reviewed" class="done">已评价</view>
				</view>
			</view>

			<view v-if="filteredOrders.length === 0" class="empty">
				<text>暂无订单</text>
			</view>
		</view>
	</view>
</template>

<script>
	import { fetchOrders } from '@/services/shop.js'
	import { isImageUrl, resolveImageUrl } from '@/utils/media.js'
	import { buildGoodsDetailUrl } from '../../data/catalog.js'

	export default {
		data() {
			return {
				tabs: ['全部', '已完成', '待评价', '已评价'],
				active: '全部',
				orders: []
			}
		},
		computed: {
			filteredOrders() {
				if (this.active === '全部') return this.orders
				if (this.active === '待评价') return this.orders.filter((item) => item.reviewable)
				return this.orders.filter((item) => item.status === this.active)
			}
		},
		onShow() {
			this.loadOrders()
		},
		onLoad(query) {
			if (query && query.tab) this.active = decodeURIComponent(query.tab)
		},
		methods: {
			isImageUrl,
			resolveImageUrl,
			async loadOrders() {
				try {
					const body = await fetchOrders()
					this.orders = body && body.code === 0 && Array.isArray(body.data) ? body.data : []
				} catch (e) {
					if (e && e.statusCode === 401) {
						uni.showToast({ title: '请先登录后查看订单', icon: 'none' })
						uni.navigateTo({ url: '/pages/auth/login' })
					}
					this.orders = []
				}
			},
			goLogistics() {
				uni.navigateTo({ url: '/pages/order/logistics' })
			},
			goReview(order) {
				uni.navigateTo({ url: '/pages/order/review?id=' + encodeURIComponent(order.id) })
			},
			openProduct(order) {
				if (order.goodsId) uni.navigateTo({ url: buildGoodsDetailUrl({ id: order.goodsId }) })
			}
		}
	}
</script>

<style lang="scss" scoped>
	.page { padding: 28rpx; }
	.title { display: block; font-size: 40rpx; font-weight: 900; color: #17231d; }
	.tabs { display: flex; gap: 12rpx; margin: 24rpx 0; overflow-x: auto; }
	.tab { flex-shrink: 0; padding: 14rpx 22rpx; border-radius: 999rpx; background: #fff; color: #667085; font-size: 24rpx; }
	.tab.on { background: #1f5c43; color: #fff; }
	.card, .empty { background: #fff; border-radius: 24rpx; padding: 24rpx; margin-bottom: 18rpx; border: 1rpx solid #e4e9e5; }
	.top, .body, .actions { display: flex; align-items: center; justify-content: space-between; gap: 18rpx; }
	.shop, .name { font-size: 28rpx; font-weight: 900; color: #17231d; }
	.status, .price { color: #d66a2c; font-weight: 900; }
	.body { justify-content: flex-start; margin-top: 20rpx; }
	.cover { width: 120rpx; height: 120rpx; border-radius: 18rpx; background: #edf3ef; display: flex; align-items: center; justify-content: center; font-size: 28rpx; overflow: hidden; flex-shrink: 0; color: #667085; }
	.cover.has-image { background: #eef7f1; }
	.cover-img { width: 100%; height: 100%; display: block; }
	.main { flex: 1; min-width: 0; }
	.name, .meta, .price { display: block; }
	.meta { margin-top: 8rpx; color: #667085; font-size: 24rpx; }
	.price { margin-top: 10rpx; font-size: 30rpx; }
	.reviewed-box { margin-top: 18rpx; padding: 18rpx; border-radius: 16rpx; background: #f7faf8; color: #315d49; font-size: 24rpx; line-height: 1.6; }
	.review-text { display: block; margin-top: 6rpx; color: #667085; }
	.actions { justify-content: flex-end; margin-top: 20rpx; }
	.ghost, .primary, .done { min-width: 128rpx; height: 58rpx; padding: 0 22rpx; border-radius: 14rpx; font-size: 24rpx; font-weight: 900; display: flex; align-items: center; justify-content: center; box-sizing: border-box; }
	.ghost { background: #e8f3ed; color: #1f5c43; }
	.primary { background: #1f5c43; color: #fff; }
	.done { background: #f1f3f1; color: #8a94a1; }
	.empty { text-align: center; color: #98a2b3; }
</style>
