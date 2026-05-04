<template>
	<view class="content">
		<view class="logo-mark">✓</view>
		<view class="text-area">
			<text class="title">{{ backendMsg }}</text>
		</view>
		<button type="primary" @click="getBackendData" style="margin-top: 40rpx;">点我测试联通</button>
	</view>
</template>

<script>
	import { fetchHello } from '@/services/hello.js'

	export default {
		data() {
			return {
				backendMsg: '等待连接后端...'
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
						this.backendMsg = body.data
						uni.showToast({ title: '连接成功！', icon: 'success' })
					} else {
						this.backendMsg = (body && body.message) || '业务返回异常'
					}
				} catch (e) {
					console.error('连接失败：', e)
					this.backendMsg = '连接失败：请确认后端已启动，且 H5 开发端口与 manifest 代理一致'
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
		border-radius: 50%;
		background: #1b4332;
		color: #fff;
		font-size: 72rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		font-weight: 300;
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
