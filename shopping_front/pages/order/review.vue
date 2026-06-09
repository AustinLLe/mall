<template>
	<view class="safe-page">
		<view class="content-wrap page">
			<text class="title">发表评价</text>
			<view class="card">
				<text class="label">商品符合程度</text>
				<view class="stars">
					<text v-for="n in 5" :key="'p' + n" class="star" :class="{ on: n <= productScore }" @click="productScore = n">★</text>
				</view>

				<text class="label">卖家信用评分</text>
				<view class="stars">
					<text v-for="n in 5" :key="'s' + n" class="star" :class="{ on: n <= sellerScore }" @click="sellerScore = n">★</text>
				</view>

				<text class="label">评价内容</text>
				<textarea v-model="content" class="textarea" maxlength="500" placeholder="说说商品是否符合描述、卖家沟通和交易体验" />
				<text class="count">{{ content.length }}/500</text>

				<button class="btn" :disabled="submitting" @click="submit">{{ submitting ? '提交中...' : '提交评价' }}</button>
			</view>
		</view>
	</view>
</template>

<script>
	import { submitOrderReview } from '@/services/shop.js'
	import { pickErrorMessage } from '@/utils/auth.js'

	export default {
		data() {
			return {
				orderId: '',
				productScore: 5,
				sellerScore: 5,
				content: '',
				submitting: false
			}
		},
		onLoad(query) {
			this.orderId = query && query.id ? decodeURIComponent(query.id) : ''
		},
		methods: {
			async submit() {
				if (!this.orderId) {
					uni.showToast({ title: '订单 ID 缺失', icon: 'none' })
					return
				}
				if (!this.content.trim()) {
					uni.showToast({ title: '请填写评价内容', icon: 'none' })
					return
				}
				this.submitting = true
				try {
					await submitOrderReview(this.orderId, {
						productScore: this.productScore,
						sellerScore: this.sellerScore,
						content: this.content.trim()
					})
					uni.showToast({ title: '评价已提交', icon: 'success' })
					setTimeout(() => uni.navigateBack(), 450)
				} catch (e) {
					uni.showToast({ title: pickErrorMessage(e) || '评价提交失败', icon: 'none' })
				} finally {
					this.submitting = false
				}
			}
		}
	}
</script>

<style lang="scss" scoped>
	.page { padding: 28rpx; }
	.title { display: block; font-size: 40rpx; font-weight: 900; color: #17231d; margin-bottom: 24rpx; }
	.card { background: #fff; border-radius: 24rpx; padding: 28rpx; border: 1rpx solid #e4e9e5; }
	.label { display: block; font-size: 25rpx; color: #667085; margin: 18rpx 0 10rpx; }
	.stars { display: flex; gap: 10rpx; margin-bottom: 8rpx; }
	.star { font-size: 48rpx; color: #d7dce0; line-height: 1; }
	.star.on { color: #f5a524; }
	.textarea { width: 100%; height: 210rpx; background: #f8faf8; border-radius: 16rpx; padding: 18rpx; box-sizing: border-box; font-size: 28rpx; line-height: 1.5; }
	.count { display: block; text-align: right; margin-top: 8rpx; color: #98a2b3; font-size: 22rpx; }
	.btn { margin-top: 24rpx; width: 100%; height: 86rpx; border-radius: 16rpx; background: #1f5c43; color: #fff; display: flex; align-items: center; justify-content: center; font-weight: 900; font-size: 28rpx; }
	.btn[disabled] { opacity: .68; }
	button::after { border: 0; }
</style>
