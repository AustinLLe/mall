<template>
	<view class="desk-page">
		<view class="topbar">
			<view>
				<text class="eyebrow">Buyer Center</text>
				<text class="title">我的松果</text>
			</view>
			<button class="logout" @click="logout">退出</button>
		</view>

		<view class="hero">
			<view class="avatar">{{ avatar }}</view>
			<view class="profile">
				<text class="name">{{ user.username || '买家用户' }}</text>
				<text class="meta">{{ user.phoneMasked || '未绑定手机号' }} · {{ verifiedText }}</text>
				<view class="chips">
					<text class="chip">买家</text>
					<text class="chip">信用分 {{ user.credit || 100 }}</text>
					<text class="chip">{{ verifiedText }}</text>
				</view>
			</view>
			<view class="score-card">
				<text class="score">{{ user.credit || 100 }}</text>
				<text class="score-label">信用分</text>
			</view>
		</view>

		<view class="layout">
			<view class="main">
				<view class="grid">
					<view v-for="item in stats" :key="item.label" class="stat-card">
						<text class="stat-value">{{ item.value }}</text>
						<text class="stat-label">{{ item.label }}</text>
					</view>
				</view>
				<view class="section">
					<view class="section-head">
						<text class="section-title">买家服务</text>
						<text class="section-more">全部</text>
					</view>
					<view class="service-grid">
						<view v-for="item in services" :key="item.title" class="service">
							<text class="service-title">{{ item.title }}</text>
							<text class="service-desc">{{ item.desc }}</text>
						</view>
					</view>
				</view>
			</view>
			<view class="side">
				<view class="section">
					<text class="section-title">信用说明</text>
					<text class="side-copy">信用分综合交易完成率、评价、纠纷记录与资料完整度。手机号通过格式校验后视为实名认证模拟通过。</text>
					<view class="bar"><view class="fill" :style="{ width: (user.credit || 100) + '%' }"></view></view>
				</view>
				<view class="section">
					<text class="section-title">最近足迹</text>
					<view v-for="item in footprints" :key="item" class="line">{{ item }}</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
	import { clearSession, getCachedUser } from '@/utils/auth.js'

	export default {
		data() {
			return { user: {} }
		},
		computed: {
			avatar() {
				return (this.user.username || '买').slice(0, 1).toUpperCase()
			},
			verifiedText() {
				return this.user.realNameVerified ? '实名认证已模拟通过' : '未完成实名模拟'
			},
			stats() {
				return [
					{ label: '收藏', value: 12 },
					{ label: '足迹', value: 28 },
					{ label: '关注店铺', value: 6 },
					{ label: '待评价', value: 3 }
				]
			},
			services() {
				return [
					{ title: '我的订单', desc: '待付款、待收货、退款售后' },
					{ title: '收藏夹', desc: '持续跟踪心仪商品' },
					{ title: '浏览足迹', desc: '按时间找回看过的商品' },
					{ title: '关注店铺', desc: '查看店铺动态与上新' }
				]
			},
			footprints() {
				return ['AirWave Pro 降噪耳机', '松果 Pad 11 学习平板', 'ViewTop 27 英寸显示器']
			}
		},
		onShow() {
			this.user = getCachedUser() || {}
		},
		methods: {
			logout() {
				clearSession()
				uni.reLaunch({ url: '/pages/home/home' })
			}
		}
	}
</script>

<style lang="scss" scoped>
	@import './role-dashboard.scss';
</style>
