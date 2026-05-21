<template>
	<view class="safe-page">
		<view class="content-wrap page">
			<text class="title">物流跟踪</text>
			<view v-for="node in nodes" :key="node.time" class="node">
				<text class="dot"></text>
				<view>
					<text class="time">{{ node.time }}</text>
					<text class="text">{{ node.text }}</text>
				</view>
			</view>
		</view>
	</view>
</template>
<script>export default { data() { return { nodes: [{ time: '今天 10:30', text: '快件已到达同城配送站' }, { time: '昨天 18:20', text: '卖家已发货，平台担保生效' }, { time: '昨天 15:10', text: '订单支付成功' }] } } }</script>
<style lang="scss" scoped>
	.page { padding: 28rpx; }
	.title { display: block; font-size: 40rpx; font-weight: 900; color: #17231d; margin-bottom: 24rpx; }
	.node { display: flex; gap: 18rpx; background: #fff; border-radius: 20rpx; padding: 24rpx; margin-bottom: 14rpx; }
	.dot { width: 18rpx; height: 18rpx; border-radius: 50%; background: #d66a2c; margin-top: 10rpx; }
	.time, .text { display: block; }
	.time { color: #667085; font-size: 24rpx; }
	.text { margin-top: 6rpx; color: #17231d; font-size: 28rpx; font-weight: 800; }
</style>
