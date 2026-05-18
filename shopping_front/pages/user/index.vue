<template>
	<view class="page">
		<view class="head" :style="{ paddingTop: statusBarHeight + 'px' }">
			<view class="head-inner">
				<view class="avatar">{{ avatarLetter }}</view>
				<view class="info">
					<text class="name">{{ displayName }}</text>
					<text class="sub">{{ subLine }}</text>
				</view>
			</view>
			<view v-if="!loggedIn" class="auth-row">
				<button class="primary" @click="goLogin">登录</button>
				<button class="secondary" @click="goRegister">注册</button>
			</view>
			<view v-else class="stats">
				<view class="stat">
					<text class="n">{{ user.credit != null ? user.credit : '--' }}</text>
					<text class="l">信用分</text>
				</view>
				<view class="stat">
					<text class="n">--</text>
					<text class="l">卖出</text>
				</view>
				<view class="stat">
					<text class="n">--</text>
					<text class="l">买入</text>
				</view>
			</view>
		</view>

		<view class="menu">
			<view class="mi" @click="needLogin(goOrders)">
				<text class="mil">我的订单</text>
				<text class="mir">›</text>
			</view>
			<view class="mi" @click="needLogin(goAddr)">
				<text class="mil">收货地址</text>
				<text class="mir">›</text>
			</view>
			<view class="mi" @click="goMsg">
				<text class="mil">消息与聊天</text>
				<text class="mir">›</text>
			</view>
			<view class="mi" @click="needLogin(goFav)">
				<text class="mil">收藏与关注</text>
				<text class="mir">›</text>
			</view>
			<view class="mi" @click="needLogin(goFoot)">
				<text class="mil">足迹（最近 20 条）</text>
				<text class="mir">›</text>
			</view>
			<view class="mi" @click="goDev">
				<text class="mil">开发者 · 接口联通</text>
				<text class="mir">›</text>
			</view>
			<view v-if="loggedIn" class="mi danger" @click="logout">
				<text class="mil">退出登录</text>
				<text class="mir">›</text>
			</view>
		</view>
	</view>
</template>

<script>
	import { getToken, getCachedUser, clearSession } from '@/utils/auth.js'
	import { fetchMe } from '@/services/auth.js'

	export default {
		data() {
			return {
				statusBarHeight: 24,
				loggedIn: false,
				user: {}
			}
		},
		computed: {
			displayName() {
				if (!this.loggedIn) return '访客'
				return this.user.username || '用户'
			},
			subLine() {
				if (!this.loggedIn) return '登录后同步订单、地址与信用'
				return this.user.phoneMasked ? '手机 ' + this.user.phoneMasked : '已登录'
			},
			avatarLetter() {
				const n = this.displayName
				return n ? n.slice(0, 1) : '?'
			}
		},
		onShow() {
			const sys = uni.getWindowInfo()
			this.statusBarHeight = sys.statusBarHeight || 24
			this.refreshSession()
		},
		methods: {
			async refreshSession() {
				const t = getToken()
				if (!t) {
					this.loggedIn = false
					this.user = {}
					return
				}
				const cached = getCachedUser()
				if (cached && cached.username) {
					this.user = cached
					this.loggedIn = true
				}
				try {
					const body = await fetchMe()
					if (body && body.code === 0 && body.data) {
						this.user = body.data
						this.loggedIn = true
						uni.setStorageSync('auth_user', body.data)
					}
				} catch (e) {
					this.loggedIn = false
					this.user = {}
					clearSession()
				}
			},
			goLogin() {
				uni.navigateTo({ url: '/pages/auth/login' })
			},
			goRegister() {
				uni.navigateTo({ url: '/pages/auth/register' })
			},
			needLogin(fn) {
				if (!getToken()) {
					uni.showToast({ title: '请先登录', icon: 'none' })
					this.goLogin()
					return
				}
				fn()
			},
			goOrders() {
				uni.showToast({ title: '订单列表开发中', icon: 'none' })
			},
			goAddr() {
				uni.navigateTo({ url: '/pages/address/list' })
			},
			goFav() {
				uni.showToast({ title: '收藏夹开发中', icon: 'none' })
			},
			goFoot() {
				uni.showToast({ title: '足迹开发中', icon: 'none' })
			},
			goMsg() {
				uni.navigateTo({ url: '/pages/message/message' })
			},
			goDev() {
				uni.navigateTo({ url: '/pages/index/index' })
			},
			logout() {
				clearSession()
				this.loggedIn = false
				this.user = {}
				uni.showToast({ title: '已退出', icon: 'none' })
			}
		}
	}
</script>

<style lang="scss" scoped>
	.page {
		min-height: 100vh;
		padding-bottom: 40rpx;
	}
	.head {
		background: linear-gradient(145deg, #1b4332 0%, #2d6a4f 55%, #40916c 100%);
		color: #fff;
		padding: 24rpx 32rpx 40rpx;
		border-radius: 0 0 32rpx 32rpx;
	}
	.head-inner {
		display: flex;
		align-items: center;
		gap: 24rpx;
	}
	.avatar {
		width: 112rpx;
		height: 112rpx;
		border-radius: 50%;
		background: rgba(255, 255, 255, 0.2);
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 48rpx;
		font-weight: 600;
	}
	.name {
		font-size: 40rpx;
		font-weight: 700;
		display: block;
	}
	.sub {
		font-size: 24rpx;
		opacity: 0.9;
		margin-top: 8rpx;
		display: block;
	}
	.auth-row {
		display: flex;
		gap: 20rpx;
		margin-top: 36rpx;
	}
	.primary,
	.secondary {
		flex: 1;
		height: 80rpx;
		line-height: 80rpx;
		border-radius: 16rpx;
		font-size: 28rpx;
		border: none;
	}
	.primary {
		background: #fff;
		color: #1b4332;
		font-weight: 600;
	}
	.secondary {
		background: rgba(255, 255, 255, 0.15);
		color: #fff;
	}
	.stats {
		display: flex;
		margin-top: 36rpx;
		padding-top: 28rpx;
		border-top: 1rpx solid rgba(255, 255, 255, 0.2);
	}
	.stat {
		flex: 1;
		text-align: center;
	}
	.n {
		font-size: 36rpx;
		font-weight: 700;
		display: block;
	}
	.l {
		font-size: 22rpx;
		opacity: 0.85;
		margin-top: 6rpx;
		display: block;
	}
	.menu {
		margin: 28rpx 24rpx;
		background: #fff;
		border-radius: 24rpx;
		overflow: hidden;
		box-shadow: 0 8rpx 28rpx rgba(0, 0, 0, 0.06);
	}
	.mi {
		display: flex;
		align-items: center;
		justify-content: space-between;
		padding: 32rpx 28rpx;
		border-bottom: 1rpx solid #f0f0f0;
	}
	.mi:last-child {
		border-bottom: none;
	}
	.mi.danger .mil {
		color: #b42318;
	}
	.mil {
		font-size: 30rpx;
		color: #333;
	}
	.mir {
		font-size: 36rpx;
		color: #ccc;
	}
</style>
