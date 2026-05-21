<template>
	<view class="safe-page">
		<view class="content-wrap page">
			<text class="title">发表评价</text>
			<view class="card">
				<text class="label">评分</text>
				<view class="stars">★★★★★</view>
				<text class="label">评价内容</text>
				<textarea v-model="text" class="textarea" placeholder="说说商品是否符合描述、卖家沟通和物流体验" />
				<view class="btn" @click="submit">提交评价</view>
			</view>
		</view>
	</view>
</template>
<script>export default { data() { return { text: '' } }, methods: { submit() { uni.showToast({ title: '评价已提交', icon: 'success' }) } } }</script>
<style lang="scss" scoped>
	.page { padding: 28rpx; }
	.title { display: block; font-size: 40rpx; font-weight: 900; color: #17231d; margin-bottom: 24rpx; }
	.card { background: #fff; border-radius: 24rpx; padding: 28rpx; }
	.label { display: block; font-size: 25rpx; color: #667085; margin: 18rpx 0 10rpx; }
	.stars { color: #d66a2c; font-size: 42rpx; letter-spacing: 6rpx; }
	.textarea { width: 100%; height: 180rpx; background: #f8faf8; border-radius: 16rpx; padding: 18rpx; box-sizing: border-box; font-size: 28rpx; }
	.btn { margin-top: 24rpx; height: 86rpx; border-radius: 16rpx; background: #1f5c43; color: #fff; display: flex; align-items: center; justify-content: center; font-weight: 900; }
</style>
