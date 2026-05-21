<template>
	<view class="safe-page">
		<view class="head" :style="{ paddingTop: statusBarHeight + 'px' }">
			<view class="content-wrap">
				<view class="profile">
					<view class="avatar">{{ avatarLetter }}</view>
					<view class="info">
						<text class="name">{{ displayName }}</text>
						<text class="sub">{{ subLine }}</text>
						<view class="badges">
							<text class="badge">信用 {{ loggedIn ? creditScore : '--' }}</text>
							<text class="badge">实名认证模拟</text>
						</view>
					</view>
					<view v-if="!loggedIn" class="auth-actions">
						<view class="login" @click="goLogin">登录</view>
						<view class="register" @click="goRegister">注册</view>
					</view>
				</view>
				<view class="stats">
					<view v-for="stat in stats" :key="stat.label" class="stat">
						<text class="n">{{ stat.value }}</text>
						<text class="l">{{ stat.label }}</text>
					</view>
				</view>
			</view>
		</view>

		<view class="content-wrap page">
			<view class="order-card">
				<view class="card-head">
					<text class="card-title">我的订单</text>
					<text class="more" @click="goOrders">查看全部</text>
				</view>
				<view class="order-grid">
					<view v-for="item in orderEntrances" :key="item.label" class="order-item" @click="goOrders">
						<text class="order-icon">{{ item.icon }}</text>
						<text class="order-label">{{ item.label }}</text>
					</view>
				</view>
			</view>

			<view class="menu-grid">
				<view v-for="item in menus" :key="item.title" class="menu-card" @click="openMenu(item)">
					<text class="menu-icon">{{ item.icon }}</text>
					<text class="menu-title">{{ item.title }}</text>
					<text class="menu-desc">{{ item.desc }}</text>
				</view>
			</view>

			<view class="credit-card">
				<text class="card-title">信用分说明</text>
				<text class="credit-desc">信用分由交易评价、成交率、退款纠纷和资料完整度综合计算。高信用卖家会在商品列表和详情页展示标识。</text>
				<view class="credit-bar">
					<view class="credit-fill" :style="{ width: creditScore + '%' }"></view>
				</view>
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
				if (!this.loggedIn) return '未登录用户'
				return this.user.nickname || this.user.username || '松果用户'
			},
			subLine() {
				if (!this.loggedIn) return '登录后查看订单、发布、收藏和信用体系'
				return this.user.phoneMasked ? '手机号 ' + this.user.phoneMasked : '已登录'
			},
			avatarLetter() {
				return this.displayName ? this.displayName.slice(0, 1) : '松'
			},
			creditScore() {
				return this.user.creditScore != null ? this.user.creditScore : 96
			},
			stats() {
				return [
					{ label: '信用分', value: this.loggedIn ? this.creditScore : '--' },
					{ label: '收藏', value: '12' },
					{ label: '足迹', value: '20' },
					{ label: '关注店铺', value: '6' }
				]
			},
			orderEntrances() {
				return [
					{ icon: '💳', label: '待付款' },
					{ icon: '📦', label: '待发货' },
					{ icon: '🚚', label: '待收货' },
					{ icon: '⭐', label: '待评价' }
				]
			},
			menus() {
				return [
					{ icon: '🛍️', title: '我的发布', desc: '管理闲置和新品', url: '/pages/user/published' },
					{ icon: '📍', title: '收货地址', desc: '地址簿管理', url: '/pages/address/list' },
					{ icon: '💬', title: '消息与议价', desc: 'AI 问答记录', url: '/pages/message/message' },
					{ icon: '🛡️', title: '审核管理', desc: '管理员模拟入口', url: '/pages/admin/audit' },
					{ icon: '🧾', title: '浏览足迹', desc: '最近 20 条商品', toast: '足迹功能演示中' },
					{ icon: '↩️', title: '退出登录', desc: '清除本地会话', action: 'logout' }
				]
			}
		},
		onShow() {
			const sys = uni.getWindowInfo()
			this.statusBarHeight = sys.statusBarHeight || 24
			this.refreshSession()
		},
		methods: {
			async refreshSession() {
				const token = getToken()
				if (!token) {
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
			goOrders() {
				uni.navigateTo({ url: '/pages/order/list' })
			},
			openMenu(item) {
				if (item.action === 'logout') {
					clearSession()
					this.loggedIn = false
					this.user = {}
					uni.showToast({ title: '已退出登录', icon: 'none' })
					return
				}
				if (item.url) {
					uni.navigateTo({ url: item.url })
					return
				}
				uni.showToast({ title: item.toast || '功能演示中', icon: 'none' })
			}
		}
	}
</script>

<style lang="scss" scoped>
	.head {
		background: linear-gradient(135deg, #163528, #1f5c43);
		color: #fff;
		padding: 28rpx 28rpx 44rpx;
		border-radius: 0 0 34rpx 34rpx;
	}
	.profile,
	.stats,
	.card-head,
	.order-grid,
	.menu-grid {
		display: flex;
	}
	.profile {
		align-items: center;
		gap: 24rpx;
	}
	.avatar {
		width: 112rpx;
		height: 112rpx;
		border-radius: 30rpx;
		background: rgba(255, 255, 255, 0.18);
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 46rpx;
		font-weight: 900;
	}
	.info {
		flex: 1;
		min-width: 0;
	}
	.name {
		display: block;
		font-size: 40rpx;
		font-weight: 900;
	}
	.sub {
		display: block;
		margin-top: 8rpx;
		font-size: 24rpx;
		opacity: 0.86;
	}
	.badges {
		display: flex;
		flex-wrap: wrap;
		gap: 10rpx;
		margin-top: 14rpx;
	}
	.badge {
		font-size: 22rpx;
		border-radius: 999rpx;
		background: rgba(255, 255, 255, 0.16);
		padding: 7rpx 14rpx;
	}
	.auth-actions {
		display: flex;
		gap: 12rpx;
	}
	.login,
	.register {
		border-radius: 16rpx;
		padding: 16rpx 24rpx;
		font-size: 25rpx;
		font-weight: 900;
	}
	.login {
		background: #fff;
		color: #1f5c43;
	}
	.register {
		background: rgba(255, 255, 255, 0.16);
		color: #fff;
	}
	.stats {
		margin-top: 32rpx;
		background: rgba(255, 255, 255, 0.12);
		border-radius: 22rpx;
		padding: 22rpx 10rpx;
	}
	.stat {
		flex: 1;
		text-align: center;
	}
	.n,
	.l {
		display: block;
	}
	.n {
		font-size: 34rpx;
		font-weight: 900;
	}
	.l {
		margin-top: 6rpx;
		font-size: 22rpx;
		opacity: 0.82;
	}
	.page {
		padding: 28rpx;
	}
	.order-card,
	.menu-card,
	.credit-card {
		background: #fff;
		border: 1rpx solid #e4e9e5;
		border-radius: 24rpx;
		box-shadow: 0 14rpx 36rpx rgba(15, 35, 26, 0.06);
	}
	.order-card,
	.credit-card {
		padding: 28rpx;
	}
	.card-head {
		align-items: center;
		justify-content: space-between;
	}
	.card-title {
		font-size: 32rpx;
		font-weight: 900;
		color: #17231d;
	}
	.more {
		font-size: 24rpx;
		color: #1f5c43;
	}
	.order-grid {
		margin-top: 22rpx;
	}
	.order-item {
		flex: 1;
		text-align: center;
	}
	.order-icon {
		display: block;
		font-size: 42rpx;
	}
	.order-label {
		display: block;
		margin-top: 8rpx;
		font-size: 24rpx;
		color: #4b5563;
	}
	.menu-grid {
		display: grid;
		grid-template-columns: repeat(3, minmax(0, 1fr));
		gap: 18rpx;
		margin-top: 22rpx;
	}
	.menu-card {
		padding: 24rpx;
	}
	.menu-icon,
	.menu-title,
	.menu-desc {
		display: block;
	}
	.menu-icon {
		font-size: 42rpx;
	}
	.menu-title {
		margin-top: 12rpx;
		font-size: 28rpx;
		font-weight: 900;
		color: #17231d;
	}
	.menu-desc {
		margin-top: 6rpx;
		font-size: 23rpx;
		color: #667085;
	}
	.credit-card {
		margin-top: 22rpx;
	}
	.credit-desc {
		display: block;
		margin-top: 14rpx;
		font-size: 25rpx;
		color: #667085;
		line-height: 1.7;
	}
	.credit-bar {
		margin-top: 22rpx;
		height: 18rpx;
		border-radius: 999rpx;
		background: #edf3ef;
		overflow: hidden;
	}
	.credit-fill {
		height: 100%;
		border-radius: 999rpx;
		background: #1f5c43;
	}
	@media screen and (max-width: 900px) {
		.profile {
			align-items: flex-start;
		}
		.auth-actions {
			flex-direction: column;
		}
		.menu-grid {
			grid-template-columns: repeat(2, minmax(0, 1fr));
		}
	}
</style>
