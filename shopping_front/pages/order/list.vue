<template>
	<view class="safe-page">
		<view class="content-wrap page">
			<text class="title">我的订单</text>
			<view class="tabs">
				<view v-for="tab in tabs" :key="tab" class="tab" :class="{ on: active === tab }" @click="active = tab">{{ tab }}</view>
			</view>
			<view v-for="order in filteredOrders" :key="order.id" class="card">
				<view class="top">
					<text class="shop">{{ order.shop }}</text>
					<text class="status">{{ order.status }}</text>
				</view>
				<view class="body">
					<view class="cover" :class="{ 'has-image': isImageUrl(order.cover) }">
						<image v-if="isImageUrl(order.cover)" class="cover-img" :src="resolveImageUrl(order.cover)" mode="aspectFill"></image>
						<text v-else>{{ order.cover }}</text>
					</view>
					<view class="main">
						<text class="name">{{ order.title }}</text>
						<text class="meta">{{ order.type }} · {{ order.service }}</text>
						<text class="price">¥{{ order.amount }}</text>
					</view>
				</view>
				<view class="actions">
					<view class="ghost" @click="goLogistics">物流跟踪</view>
					<view class="primary" @click="goReview">去评价</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
	import { orderTabs } from '../../data/catalog.js'
	import { fetchOrders } from '@/services/shop.js'
	import { isImageUrl, resolveImageUrl } from '@/utils/media.js'

	const fallbackOrders = [
		{ id: 'o1', shop: '松果严选数码', status: '待收货', title: 'AirWave Pro 降噪耳机', cover: '/static/goods/airwave-pro.jpg', type: '新品', service: '平台担保', amount: 699 },
		{ id: 'o2', shop: '阿洛的桌面仓库', status: '待评价', title: 'ViewTop 27 英寸 2K 显示器', cover: '/static/goods/viewtop-monitor.jpg', type: '二手', service: '同城验货', amount: 680 }
	]

	export default {
		data() {
			return {
				tabs: orderTabs,
				active: '全部',
				orders: fallbackOrders
			}
		},
		computed: {
			filteredOrders() {
				return this.active === '全部' ? this.orders : this.orders.filter((item) => item.status === this.active)
			}
		},
		onShow() {
			this.loadOrders()
		},
		methods: {
			isImageUrl,
			resolveImageUrl,
			async loadOrders() {
				try {
					const body = await fetchOrders()
					if (body && body.code === 0 && Array.isArray(body.data)) {
						this.orders = body.data
					}
				} catch (e) {
					this.orders = fallbackOrders
				}
			},
			goLogistics() {
				uni.navigateTo({ url: '/pages/order/logistics' })
			},
			goReview() {
				uni.navigateTo({ url: '/pages/order/review' })
			}
		}
	}
</script>

<style lang="scss" scoped>
	.page { padding: 28rpx; }
	.title { display: block; font-size: 40rpx; font-weight: 900; color: #17231d; }
	.tabs { display: flex; gap: 12rpx; margin: 24rpx 0; overflow: hidden; }
	.tab { padding: 14rpx 22rpx; border-radius: 999rpx; background: #fff; color: #667085; font-size: 24rpx; }
	.tab.on { background: #1f5c43; color: #fff; }
	.card { background: #fff; border-radius: 24rpx; padding: 24rpx; margin-bottom: 18rpx; border: 1rpx solid #e4e9e5; }
	.top, .body, .actions { display: flex; align-items: center; justify-content: space-between; gap: 18rpx; }
	.shop, .name { font-size: 28rpx; font-weight: 900; color: #17231d; }
	.status, .price { color: #d66a2c; font-weight: 900; }
	.body { justify-content: flex-start; margin-top: 20rpx; }
	.cover { width: 120rpx; height: 120rpx; border-radius: 18rpx; background: #edf3ef; display: flex; align-items: center; justify-content: center; font-size: 56rpx; overflow: hidden; }
	.cover.has-image { background: #eef7f1; }
	.cover-img { width: 100%; height: 100%; display: block; }
	.main { flex: 1; min-width: 0; }
	.name, .meta, .price { display: block; }
	.meta { margin-top: 8rpx; color: #667085; font-size: 24rpx; }
	.price { margin-top: 10rpx; font-size: 30rpx; }
	.actions { justify-content: flex-end; margin-top: 20rpx; }
	.ghost, .primary { padding: 13rpx 22rpx; border-radius: 14rpx; font-size: 24rpx; font-weight: 900; }
	.ghost { background: #e8f3ed; color: #1f5c43; }
	.primary { background: #1f5c43; color: #fff; }
</style>
