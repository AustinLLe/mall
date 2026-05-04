<template>
	<view class="page">
		<view v-for="(m, i) in list" :key="i" class="row" @click="toast">
			<view class="dot" />
			<view class="body">
				<text class="t">{{ m.title }}</text>
				<text class="s">{{ m.sub }}</text>
			</view>
			<text class="time">{{ m.time }}</text>
		</view>
		<view class="hint">买家卖家会话、官方客服、快捷发商品/订单等后续接入</view>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				list: [
					{ title: '系统通知', sub: '欢迎使用轻市交易平台', time: '今天' },
					{ title: '客服小轻', sub: '有问题随时找我', time: '昨天' },
					{ title: '交易协商（占位）', sub: '与卖家的聊天将出现在这里', time: '' }
				]
			}
		},
		methods: {
			toast() {
				uni.showToast({ title: '聊天功能开发中', icon: 'none' })
			}
		}
	}
</script>

<style lang="scss" scoped>
	.page {
		padding: 16rpx 0 40rpx;
	}
	.row {
		display: flex;
		align-items: center;
		padding: 28rpx 32rpx;
		background: #fff;
		border-bottom: 1rpx solid #f0f0f0;
	}
	.dot {
		width: 16rpx;
		height: 16rpx;
		border-radius: 50%;
		background: #40916c;
		margin-right: 20rpx;
	}
	.body {
		flex: 1;
		min-width: 0;
	}
	.t {
		font-size: 30rpx;
		color: #222;
		display: block;
	}
	.s {
		font-size: 24rpx;
		color: #888;
		margin-top: 8rpx;
		display: block;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}
	.time {
		font-size: 22rpx;
		color: #aaa;
		margin-left: 16rpx;
	}
	.hint {
		padding: 40rpx 48rpx;
		font-size: 24rpx;
		color: #999;
		line-height: 1.6;
		text-align: center;
	}
</style>
