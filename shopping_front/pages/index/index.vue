<template>
	<view class="content">
		<image class="logo" src="/static/logo.png"></image>
		<view class="text-area">
			<text class="title">{{ backendMsg }}</text>
		</view>
		<button type="primary" @click="getBackendData" style="margin-top: 40rpx;">点我测试联通</button>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				// 初始文字
				backendMsg: '等待连接后端...'
			}
		},
		onLoad() {
			// 页面加载时自动调一次
			this.getBackendData();
		},
		methods: {
			getBackendData() {
				const that = this;
				uni.request({
					// 注意：如果你是在电脑浏览器运行，用 localhost 没问题
					// 如果之后用手机真机调试，记得换成你电脑的局域网 IP
					url: 'http://localhost:8080/api/hello', 
					method: 'GET',
					success: (res) => {
						console.log('收到后端回复：', res.data);
						that.backendMsg = res.data;
						uni.showToast({
							title: '连接成功！',
							icon: 'success'
						});
					},
					fail: (err) => {
						console.error('连接失败：', err);
						that.backendMsg = '连接失败，请检查后端是否启动';
					}
				});
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

	.logo {
		height: 200rpx;
		width: 200rpx;
		margin-top: 200rpx;
		margin-bottom: 50rpx;
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