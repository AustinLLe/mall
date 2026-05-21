<template>
	<view class="safe-page center">
		<view class="box">
			<view class="icon">✓</view>
			<text class="title">支付成功</text>
			<text class="desc">平台已担保资金，卖家发货后可在物流页跟踪状态。</text>
			<view class="btn" @click="goOrders">查看订单</view>
		</view>
	</view>
</template>
<script>export default { methods: { goOrders() { uni.navigateTo({ url: '/pages/order/list' }) } } }</script>
<style lang="scss" scoped>
	.center { display: flex; align-items: center; justify-content: center; padding: 40rpx; }
	.box { background: #fff; border-radius: 28rpx; padding: 60rpx 44rpx; text-align: center; max-width: 640rpx; }
	.icon { width: 100rpx; height: 100rpx; margin: 0 auto 24rpx; border-radius: 50%; background: #e8f3ed; color: #1f5c43; display: flex; align-items: center; justify-content: center; font-size: 54rpx; font-weight: 900; }
	.title { display: block; font-size: 38rpx; font-weight: 900; color: #17231d; }
	.desc { display: block; margin-top: 12rpx; font-size: 26rpx; color: #667085; line-height: 1.6; }
	.btn { margin-top: 32rpx; height: 82rpx; border-radius: 16rpx; background: #1f5c43; color: #fff; display: flex; align-items: center; justify-content: center; font-size: 28rpx; font-weight: 900; }
</style>
