<template>
	<view class="content">
		<image class="logo-mark" src="/static/logo.png" mode="aspectFit"></image>
		<view class="text-area">
			<text class="title">{{ backendMsg }}</text>
		</view>
		<button type="primary" @click="getBackendData" style="margin-top: 40rpx;">刷新平台状态</button>
	</view>
</template>

<script>
	import { fetchProducts } from '@/services/shop.js'

	export default {
		data() {
			return {
				backendMsg: '正在获取平台状态...'
			}
		},
		onLoad() {
			this.getBackendData()
		},
		methods: {
			async getBackendData() {
				try {
					const body = await fetchProducts()
					if (body && body.code === 0) {
						const count = Array.isArray(body.data) ? body.data.length : 0
						this.backendMsg = `平台服务运行正常，当前可浏览商品 ${count} 件`
						uni.showToast({ title: '状态已刷新', icon: 'success' })
					} else {
						this.backendMsg = (body && body.message) || '平台服务暂未返回最新状态'
					}
				} catch (e) {
					this.backendMsg = '平台状态暂时不可用，请稍后刷新'
				}
			}
		}
	}
</script>

<style>
	.content {
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
	}

	.logo-mark {
		height: 160rpx;
		width: 160rpx;
		margin-top: 160rpx;
		margin-bottom: 40rpx;
		border-radius: 24rpx;
	}

	.text-area {
		display: flex;
		justify-content: center;
	}

	.title {
		font-size: 36rpx;
		color: #333;
		font-weight: bold;
	}
</style>
