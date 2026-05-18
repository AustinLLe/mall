<template>
	<view class="page">
		<text class="h">发布闲置</text>
		<text class="p">分类、图文、价格、审核状态将与后台商品模块对接。</text>
		<view class="card">
			<text class="li">· 图片上传与违禁检测（占位）</text>
			<text class="li">· 上架审核流（占位）</text>
			<text class="li">· 发布 Agent 自动生成文案（扩展功能）</text>
		</view>
		<button class="btn" @click="toast">暂不可提交</button>
	</view>
</template>

<script>
	export default {
		methods: {
			toast() {
				uni.showToast({ title: '商品模块上线后开放', icon: 'none' })
			}
		}
	}
</script>

<style lang="scss" scoped>
	.page {
		padding: 48rpx 40rpx;
		min-height: 60vh;
	}
	.h {
		font-size: 40rpx;
		font-weight: 700;
		color: #1b4332;
		display: block;
	}
	.p {
		font-size: 28rpx;
		color: #666;
		line-height: 1.6;
		margin-top: 16rpx;
		display: block;
	}
	.card {
		margin-top: 40rpx;
		background: #fff;
		border-radius: 20rpx;
		padding: 28rpx 32rpx;
		box-shadow: 0 8rpx 28rpx rgba(0, 0, 0, 0.06);
	}
	.li {
		display: block;
		font-size: 26rpx;
		color: #555;
		line-height: 2;
	}
	.btn {
		margin-top: 48rpx;
		background: #d8e2dc;
		color: #1b4332;
		border-radius: 16rpx;
		border: none;
		height: 88rpx;
		line-height: 88rpx;
	}
</style>
