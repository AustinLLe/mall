<template>
	<view class="page">
		<view class="intro">
			<text class="h1">创建账号</text>
			<text class="h2">支持手机号/账户体系（当前为账号注册）</text>
		</view>

		<view class="card">
			<view class="field">
				<text class="label">用户名</text>
				<input v-model="username" class="input" placeholder="3～32 位" />
			</view>
			<view class="field">
				<text class="label">密码</text>
				<input v-model="password" class="input" password placeholder="至少 6 位" />
			</view>
			<view class="field">
				<text class="label">确认密码</text>
				<input v-model="password2" class="input" password placeholder="再输入一次" />
			</view>
			<view class="field">
				<text class="label">手机号（可选）</text>
				<input v-model="phone" class="input" type="number" maxlength="11" placeholder="选填，11 位" />
			</view>
			<button class="submit" :loading="loading" @click="submit">注册并登录</button>
			<view class="foot">
				<text class="link" @click="goLogin">已有账号？去登录</text>
			</view>
		</view>
	</view>
</template>

<script>
	import { register } from '@/services/auth.js'
	import { setSession, pickErrorMessage } from '@/utils/auth.js'

	export default {
		data() {
			return {
				username: '',
				password: '',
				password2: '',
				phone: '',
				loading: false
			}
		},
		methods: {
			goLogin() {
				uni.navigateBack()
			},
			async submit() {
				if (!this.username.trim() || this.password.length < 6) {
					uni.showToast({ title: '用户名或密码不符合要求', icon: 'none' })
					return
				}
				if (this.password !== this.password2) {
					uni.showToast({ title: '两次密码不一致', icon: 'none' })
					return
				}
				const payload = {
					username: this.username.trim(),
					password: this.password
				}
				const ph = this.phone.trim()
				if (ph) {
					payload.phone = ph
				}
				this.loading = true
				try {
					const body = await register(payload)
					if (body.code !== 0) {
						uni.showToast({ title: body.message || '注册失败', icon: 'none' })
						return
					}
					const { token, user } = body.data
					setSession(token, user)
					uni.showToast({ title: '注册成功', icon: 'success' })
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
	}
	.intro {
		margin-bottom: 40rpx;
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
	.card {
		background: #fff;
		border-radius: 24rpx;
		padding: 36rpx 32rpx 48rpx;
		box-shadow: 0 12rpx 40rpx rgba(0, 0, 0, 0.06);
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
	.foot {
		margin-top: 28rpx;
		text-align: center;
	}
	.link {
		font-size: 26rpx;
		color: #2d6a4f;
	}
</style>
