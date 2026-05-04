<template>
	<view class="page">
		<view class="intro">
			<text class="h1">欢迎回来</text>
			<text class="h2">用手机号或账号登录轻市</text>
		</view>

		<view class="tabs">
			<view class="tab" :class="{ on: mode === 'account' }" @click="mode = 'account'">账号登录</view>
			<view class="tab" :class="{ on: mode === 'phone' }" @click="onPhoneTab">手机号登录</view>
		</view>

		<view v-if="mode === 'account'" class="card">
			<view class="field">
				<text class="label">用户名</text>
				<input v-model="username" class="input" placeholder="3～32 位字母或数字" />
			</view>
			<view class="field">
				<text class="label">密码</text>
				<input v-model="password" class="input" password placeholder="至少 6 位" />
			</view>
			<button class="submit" :loading="loading" @click="submit">登录</button>
			<view class="foot">
				<text class="link" @click="goReg">没有账号？去注册</text>
				<text class="link dim" @click="toastSoon">忘记密码</text>
			</view>
			<view class="demo">体验账号：<text class="mono">demo</text> / <text class="mono">demo123</text></view>
		</view>

		<view v-else class="card muted">
			<text class="soon">手机号验证码登录将在对接短信服务后启用。</text>
			<button class="submit ghost" @click="mode = 'account'">先用账号登录</button>
		</view>
	</view>
</template>

<script>
	import { loginByPassword } from '@/services/auth.js'
	import { setSession, pickErrorMessage } from '@/utils/auth.js'

	export default {
		data() {
			return {
				mode: 'account',
				username: '',
				password: '',
				loading: false
			}
		},
		methods: {
			onPhoneTab() {
				this.mode = 'phone'
			},
			toastSoon() {
				uni.showToast({ title: '功能开发中', icon: 'none' })
			},
			goReg() {
				uni.navigateTo({ url: '/pages/auth/register' })
			},
			async submit() {
				if (!this.username.trim() || !this.password) {
					uni.showToast({ title: '请填写用户名和密码', icon: 'none' })
					return
				}
				this.loading = true
				try {
					const body = await loginByPassword({
						username: this.username.trim(),
						password: this.password
					})
					if (body.code !== 0) {
						uni.showToast({ title: body.message || '登录失败', icon: 'none' })
						return
					}
					const { token, user } = body.data
					setSession(token, user)
					uni.showToast({ title: '登录成功', icon: 'success' })
					setTimeout(() => {
						uni.switchTab({ url: '/pages/user/index' })
					}, 400)
				} catch (e) {
					uni.showToast({ title: pickErrorMessage(e), icon: 'none' })
				} finally {
					this.loading = false
				}
			}
		}
	}
</script>

<style lang="scss" scoped>
	.page {
		min-height: 100vh;
		padding: 48rpx 40rpx 80rpx;
		box-sizing: border-box;
	}
	.intro {
		margin-bottom: 48rpx;
	}
	.h1 {
		font-size: 48rpx;
		font-weight: 700;
		color: #1b4332;
		display: block;
	}
	.h2 {
		font-size: 26rpx;
		color: #777;
		margin-top: 12rpx;
		display: block;
	}
	.tabs {
		display: flex;
		gap: 16rpx;
		margin-bottom: 28rpx;
	}
	.tab {
		flex: 1;
		text-align: center;
		padding: 20rpx 0;
		border-radius: 16rpx;
		background: #fff;
		color: #666;
		font-size: 28rpx;
	}
	.tab.on {
		background: #1b4332;
		color: #fff;
		font-weight: 600;
	}
	.card {
		background: #fff;
		border-radius: 24rpx;
		padding: 36rpx 32rpx 48rpx;
		box-shadow: 0 12rpx 40rpx rgba(0, 0, 0, 0.06);
	}
	.card.muted {
		text-align: center;
	}
	.field {
		margin-bottom: 28rpx;
	}
	.label {
		font-size: 24rpx;
		color: #888;
		display: block;
		margin-bottom: 12rpx;
	}
	.input {
		height: 88rpx;
		border-radius: 16rpx;
		background: #f5f4f1;
		padding: 0 24rpx;
		font-size: 30rpx;
	}
	.submit {
		margin-top: 16rpx;
		height: 92rpx;
		line-height: 92rpx;
		border-radius: 16rpx;
		background: #1b4332;
		color: #fff;
		font-size: 30rpx;
		font-weight: 600;
		border: none;
	}
	.submit.ghost {
		background: #e8ebe4;
		color: #1b4332;
		margin-top: 32rpx;
	}
	.foot {
		display: flex;
		justify-content: space-between;
		margin-top: 28rpx;
	}
	.link {
		font-size: 26rpx;
		color: #2d6a4f;
	}
	.link.dim {
		color: #aaa;
	}
	.demo {
		margin-top: 36rpx;
		font-size: 24rpx;
		color: #888;
		line-height: 1.6;
	}
	.mono {
		font-family: monospace;
		color: #333;
	}
	.soon {
		font-size: 28rpx;
		color: #666;
		line-height: 1.6;
		display: block;
		padding: 24rpx 0;
	}
</style>
