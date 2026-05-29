<template>
	<view class="desk-page">
		<view class="topbar">
			<view>
				<text class="eyebrow">Admin Console</text>
				<text class="title">管理员后台</text>
			</view>
			<button class="logout" @click="logout">退出</button>
		</view>

		<view class="hero admin">
			<view class="avatar">{{ avatar }}</view>
			<view class="profile">
				<text class="name">{{ user.username || '管理员' }}</text>
				<text class="meta">{{ user.phoneMasked || '系统预置账号' }} · 管理员不开放注册</text>
				<view class="chips">
					<text class="chip">管理员</text>
					<text class="chip">用户状态维护</text>
					<text class="chip">实名模拟审核</text>
				</view>
			</view>
			<view class="score-card">
				<text class="score">Root</text>
				<text class="score-label">权限级别</text>
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
						<text class="section-title">管理模块</text>
						<text class="section-more" @click="goAudit">进入审核</text>
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
					<text class="section-title">风控摘要</text>
					<text class="side-copy">此页为管理员演示控制台，先展示用户状态、实名模拟、商品审核和信用分分布入口。</text>
					<view class="bar"><view class="fill" style="width: 72%"></view></view>
				</view>
				<view class="section">
					<text class="section-title">今日提醒</text>
					<view v-for="item in alerts" :key="item" class="line">{{ item }}</view>
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
				return (this.user.username || '管').slice(0, 1).toUpperCase()
			},
			stats() {
				return [
					{ label: '总用户', value: 128 },
					{ label: '卖家账号', value: 36 },
					{ label: '待审核商品', value: 9 },
					{ label: '异常状态', value: 2 }
				]
			},
			services() {
				return [
					{ title: '用户状态维护', desc: '冻结、恢复、角色检查' },
					{ title: '实名认证模拟', desc: '手机号格式与资料完整度' },
					{ title: '商品审核', desc: '发布内容、图片和价格风险' },
					{ title: '信用分看板', desc: '信用分分布与异常用户' }
				]
			},
			alerts() {
				return ['9 件商品等待审核', '2 个用户状态需复核', '1 条售后纠纷进入平台协商']
			}
		},
		onShow() {
			this.user = getCachedUser() || {}
			if (this.user.role !== 'admin') {
				uni.showToast({ title: '仅管理员可访问', icon: 'none' })
				setTimeout(() => uni.reLaunch({ url: '/pages/home/home' }), 500)
			}
		},
		methods: {
			goAudit() {
				uni.navigateTo({ url: '/pages/admin/audit' })
			},
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
