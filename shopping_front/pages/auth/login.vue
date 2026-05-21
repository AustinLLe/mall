<template>
	<view class="safe-page auth-page">
		<view class="card">
			<text class="title">欢迎回到松果集市</text>
			<text class="desc">登录后可查看订单、信用分、发布管理和议价消息。</text>
			<view class="field">
				<text class="label">账号</text>
				<input v-model="username" class="input" placeholder="请输入账号，演示账号 demo" />
			</view>
			<view class="field">
				<text class="label">密码</text>
				<input v-model="password" class="input" password placeholder="演示密码 demo123" />
			</view>
			<button class="submit" :loading="loading" @click="submit">登录</button>
			<view class="links">
				<text @click="goReg">注册新账号</text>
				<text @click="fillDemo">填入演示账号</text>
			</view>
		</view>
	</view>
</template>

<script>
	import { loginByPassword } from '@/services/auth.js'
	import { setSession, pickErrorMessage } from '@/utils/auth.js'

	export default {
		data() {
			return { username: '', password: '', loading: false }
		},
		methods: {
			fillDemo() {
				this.username = 'demo'
				this.password = 'demo123'
			},
			goReg() {
				uni.navigateTo({ url: '/pages/auth/register' })
			},
			async submit() {
				if (!this.username.trim() || !this.password) {
					uni.showToast({ title: '请输入账号和密码', icon: 'none' })
					return
				}
				this.loading = true
				try {
					const body = await loginByPassword({ username: this.username.trim(), password: this.password })
					if (body.code !== 0) {
						uni.showToast({ title: body.message || '登录失败', icon: 'none' })
						return
					}
					const { token, user } = body.data
					setSession(token, user)
					uni.showToast({ title: '登录成功', icon: 'success' })
					setTimeout(() => uni.switchTab({ url: '/pages/user/index' }), 400)
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
	.auth-page { display: flex; align-items: center; justify-content: center; padding: 48rpx 28rpx; box-sizing: border-box; }
	.card { width: 100%; max-width: 620rpx; background: #fff; border-radius: 28rpx; padding: 42rpx 34rpx; box-shadow: 0 18rpx 42rpx rgba(15, 35, 26, 0.08); }
	.title { display: block; font-size: 42rpx; font-weight: 900; color: #17231d; }
	.desc { display: block; margin-top: 12rpx; color: #667085; font-size: 25rpx; line-height: 1.6; }
	.field { margin-top: 26rpx; }
	.label { display: block; margin-bottom: 10rpx; color: #4b5563; font-size: 24rpx; font-weight: 800; }
	.input { height: 86rpx; border-radius: 16rpx; background: #f8faf8; padding: 0 22rpx; font-size: 28rpx; }
	.submit { margin-top: 30rpx; height: 88rpx; line-height: 88rpx; border-radius: 16rpx; background: #1f5c43; color: #fff; font-weight: 900; border: none; }
	.links { display: flex; justify-content: space-between; margin-top: 24rpx; color: #1f5c43; font-size: 25rpx; }
</style>
