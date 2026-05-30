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
	import { fetchHello } from '@/services/hello.js'

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
					const body = await fetchHello()
					if (body && body.code === 0) {
						this.backendMsg = body.data || '平台服务运行正常'
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
