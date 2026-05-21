<template>
	<view class="desk-page">
		<view class="topbar">
			<view>
				<text class="eyebrow">Seller Studio</text>
				<text class="title">卖家工作台</text>
			</view>
			<button class="logout" @click="logout">退出</button>
		</view>

		<view class="hero seller">
			<view class="avatar">{{ avatar }}</view>
			<view class="profile">
				<text class="name">{{ user.username || '卖家用户' }}</text>
				<text class="meta">{{ user.phoneMasked || '未绑定手机号' }} · {{ verifiedText }}</text>
				<view class="chips">
					<text class="chip">卖家</text>
					<text class="chip">信用分 {{ user.credit || 100 }}</text>
					<text class="chip">店铺状态正常</text>
				</view>
			</view>
			<view class="score-card">
				<text class="score">A</text>
				<text class="score-label">经营评级</text>
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
						<text class="section-title">经营动作</text>
						<text class="section-more" @click="goPublish">发布商品</text>
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
					<text class="section-title">店铺健康度</text>
					<text class="side-copy">模拟展示卖家发布、成交、售后协商与信用表现，后续可接入真实订单统计。</text>
					<view class="bar"><view class="fill" style="width: 88%"></view></view>
				</view>
				<view class="section">
					<text class="section-title">待处理</text>
					<view v-for="item in todos" :key="item" class="line">{{ item }}</view>
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
				return (this.user.username || '卖').slice(0, 1).toUpperCase()
			},
			verifiedText() {
				return this.user.realNameVerified ? '实名认证已模拟通过' : '未完成实名模拟'
			},
			stats() {
				return [
					{ label: '在售商品', value: 8 },
					{ label: '今日咨询', value: 14 },
					{ label: '待发货', value: 3 },
					{ label: '售后协商', value: 1 }
				]
			},
			services() {
				return [
					{ title: '商品管理', desc: '编辑价格、库存与上下架' },
					{ title: '订单履约', desc: '发货、物流与收款状态' },
					{ title: '议价消息', desc: '集中处理买家报价' },
					{ title: '店铺资料', desc: '头像、简介、认证状态' }
				]
			},
			todos() {
				return ['3 个订单待发货', '1 个售后待回复', '2 件商品建议补充图片']
			}
		},
		onShow() {
			this.user = getCachedUser() || {}
		},
		methods: {
			goPublish() {
				uni.navigateTo({ url: '/pages/publish/publish' })
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
