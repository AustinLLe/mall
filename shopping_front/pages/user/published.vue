<template>
	<view class="safe-page">
		<view class="content-wrap page">
			<view class="head">
				<text class="title">我的发布</text>
				<view class="btn" @click="goPublish">继续发布</view>
			</view>
			<view v-if="!goods.length" class="empty">暂无发布记录，去发布一件商品试试。</view>
			<view v-for="item in goods" :key="item.id" class="card" @click="open(item)">
				<view class="cover" :class="{ 'has-image': isImageUrl(item.cover) }">
					<image v-if="isImageUrl(item.cover)" class="cover-img" :src="resolveImageUrl(item.cover)" mode="aspectFill"></image>
					<text v-else>{{ item.cover || '物' }}</text>
				</view>
				<view class="main">
					<text class="name">{{ item.title }}</text>
					<text class="meta">{{ item.scene === 'new' ? '新品' : '二手' }} · {{ item.condition }} · {{ item.location }}</text>
					<text class="price">¥{{ item.price }}</text>
					<text v-if="item.rejectReason" class="reason">拒绝原因：{{ item.rejectReason }}</text>
				</view>
				<text class="status" :class="statusClass(item)">{{ statusText(item) }}</text>
			</view>
		</view>
	</view>
</template>
<script>
	import { buildGoodsDetailUrl } from '../../data/catalog.js'
	import { fetchMyProducts } from '@/services/shop.js'
	import { isImageUrl, resolveImageUrl } from '@/utils/media.js'

	export default {
		data() {
			return {
				goods: []
			}
		},
		onShow() {
			this.loadGoods()
		},
		methods: {
			async loadGoods() {
				try {
					const body = await fetchMyProducts()
					if (body && body.code === 0 && Array.isArray(body.data)) {
						this.goods = body.data
						return
					}
				} catch (e) {}
				this.goods = []
			},
			isImageUrl,
			resolveImageUrl,
			statusText(item) {
				if (item.status === 'pending') return '待审核'
				if (item.status === 'rejected') return '已拒绝'
				return '审核通过'
			},
			statusClass(item) {
				return item.status || 'approved'
			},
			goPublish() { uni.navigateTo({ url: '/pages/publish/publish' }) },
			open(item) { uni.navigateTo({ url: buildGoodsDetailUrl(item) }) }
		}
	}
</script>
<style lang="scss" scoped>
	.page { padding: 28rpx; }
	.head, .card { display: flex; align-items: center; justify-content: space-between; gap: 18rpx; }
	.title { font-size: 40rpx; font-weight: 900; color: #17231d; }
	.btn { padding: 16rpx 24rpx; border-radius: 16rpx; background: #1f5c43; color: #fff; font-weight: 900; }
	.card { margin-top: 18rpx; background: #fff; border-radius: 24rpx; padding: 24rpx; border: 1rpx solid #e4e9e5; }
	.cover { width: 120rpx; height: 120rpx; border-radius: 18rpx; background: #edf3ef; display: flex; align-items: center; justify-content: center; font-size: 56rpx; overflow: hidden; }
	.cover.has-image { background: #eef7f1; }
	.cover-img { width: 100%; height: 100%; display: block; }
	.main { flex: 1; min-width: 0; }
	.name, .meta, .price, .reason { display: block; }
	.name { font-size: 30rpx; font-weight: 900; color: #17231d; }
	.meta { margin-top: 8rpx; color: #667085; font-size: 24rpx; }
	.price { margin-top: 8rpx; color: #d66a2c; font-size: 30rpx; font-weight: 900; }
	.reason { margin-top: 6rpx; color: #b95420; font-size: 23rpx; }
	.status { color: #1f5c43; background: #e8f3ed; padding: 8rpx 14rpx; border-radius: 999rpx; font-size: 22rpx; }
	.status.pending { color: #9a5b12; background: #fff6da; }
	.status.rejected { color: #b95420; background: #fff0e7; }
	.empty { margin-top: 22rpx; padding: 36rpx; text-align: center; background: #fff; border: 1rpx solid #e4e9e5; border-radius: 24rpx; color: #667085; }
</style>
