<template>
	<view class="page">
		<view class="nav-shell">
			<view class="nav" :style="{ paddingTop: statusBarHeight + 'px' }">
				<view class="nav-inner">
					<view class="brand">
						<text class="brand-name">轻市</text>
						<text class="brand-sub">校园闲置交易平台</text>
					</view>
					<view class="nav-actions">
						<view class="ghost-btn" @click="goPublish">发布闲置</view>
					</view>
				</view>
				<view class="search" @click="scrollToGoods">
					<text class="search-icon">⌕</text>
					<text class="search-placeholder">搜索商品、店铺、话题灵感</text>
				</view>
			</view>
		</view>

		<scroll-view scroll-y class="scroll-shell" :style="{ height: scrollHeight + 'px' }">
			<view class="container">
				<view class="hero">
					<view
						class="hero-card new"
						:class="{ active: activeScene === 'new' }"
						@click="setScene('new')"
					>
						<text class="hero-kicker">官方仓发</text>
						<text class="hero-title">新品馆</text>
						<text class="hero-desc">新品、学生价、售后保障</text>
						<view class="hero-action">查看新品流</view>
					</view>
					<view
						class="hero-card used"
						:class="{ active: activeScene === 'used' }"
						@click="setScene('used')"
					>
						<text class="hero-kicker">校友闲置</text>
						<text class="hero-title">闲物集</text>
						<text class="hero-desc">二手转卖、同城面交</text>
						<view class="hero-action">查看闲置流</view>
					</view>
				</view>

				<view class="section-head">
					<text class="section-title">浏览商品</text>
					<text class="section-tip">新品馆和闲物集直接在首页切换</text>
				</view>

				<view class="toolbar">
					<scroll-view scroll-x class="chips" :show-scrollbar="false">
						<view
							v-for="tab in sceneTabs"
							:key="tab.key"
							class="chip"
							:class="{ on: activeScene === tab.key }"
							@click="setScene(tab.key)"
						>
							{{ tab.label }}
						</view>
					</scroll-view>
					<view class="mode-switch">
						<view
							v-for="mode in viewModes"
							:key="mode.key"
							class="mode-item"
							:class="{ on: viewMode === mode.key }"
							@click="viewMode = mode.key"
						>
							{{ mode.label }}
						</view>
					</view>
				</view>

				<scroll-view scroll-x class="chips categories" :show-scrollbar="false">
					<view
						v-for="category in categories"
						:key="category"
						class="chip soft"
						:class="{ on: activeCategory === category }"
						@click="activeCategory = category"
					>
						{{ category }}
					</view>
				</scroll-view>

				<view class="goods-grid" :class="viewMode">
					<view
						v-for="item in displayGoods"
						:key="item.id"
						class="goods-card"
						@click="openDetail(item)"
					>
						<view class="goods-cover">{{ item.cover }}</view>
						<view class="goods-main">
							<view class="goods-topline">
								<text class="goods-scene">{{ item.scene === 'new' ? '新品馆' : '闲物集' }}</text>
								<text class="goods-tag">{{ item.tag }}</text>
							</view>
							<text class="goods-title">{{ item.title }}</text>
							<text class="goods-subtitle">{{ item.subtitle }}</text>
							<view class="goods-meta">
								<text class="goods-price">¥{{ item.price }}</text>
								<text class="goods-origin">¥{{ item.originPrice }}</text>
							</view>
							<view class="goods-extra">
								<text class="goods-credit">信用 {{ item.credit }}</text>
								<text class="goods-location">{{ item.location }}</text>
							</view>
						</view>
					</view>
				</view>

				<view class="section-head">
					<text class="section-title">馆内精选</text>
					<text class="section-more" @click="setScene('all')">回到全量</text>
				</view>

				<view class="spotlight">
					<view class="spot-card" @click="openDetail(newGoods[0])">
						<text class="spot-kicker">新品馆主推</text>
						<text class="spot-title">{{ newGoods[0].title }}</text>
						<text class="spot-desc">{{ newGoods[0].highlights.join(' · ') }}</text>
						<text class="spot-price">¥{{ newGoods[0].price }}</text>
					</view>
					<view class="spot-card alt" @click="openDetail(usedGoods[0])">
						<text class="spot-kicker">闲物集捡漏</text>
						<text class="spot-title">{{ usedGoods[0].title }}</text>
						<text class="spot-desc">{{ usedGoods[0].story }}</text>
						<text class="spot-price">¥{{ usedGoods[0].price }}</text>
					</view>
				</view>
			</view>
		</scroll-view>

		<view class="fab" @click="goPublish">
			<text class="fab-text">卖闲置</text>
		</view>
	</view>
</template>

<script>
	import { goodsCatalog, buildGoodsDetailUrl } from '../../data/catalog.js'

	export default {
		data() {
			return {
				statusBarHeight: 24,
				scrollHeight: 500,
				activeScene: 'all',
				activeCategory: '全部',
				viewMode: 'double',
				scrollTarget: ''
			}
		},
		computed: {
			sceneTabs() {
				return [
					{ key: 'all', label: '全部商品' },
					{ key: 'new', label: '新品馆' },
					{ key: 'used', label: '闲物集' }
				]
			},
			viewModes() {
				return [
					{ key: 'double', label: '双列' },
					{ key: 'single', label: '单列' }
				]
			},
			categories() {
				const values = Array.from(new Set(goodsCatalog.map((item) => item.category)))
				return ['全部'].concat(values)
			},
			newGoods() {
				return goodsCatalog.filter((item) => item.scene === 'new')
			},
			usedGoods() {
				return goodsCatalog.filter((item) => item.scene === 'used')
			},
			displayGoods() {
				return goodsCatalog.filter((item) => {
					const sceneOk = this.activeScene === 'all' || item.scene === this.activeScene
					const categoryOk = this.activeCategory === '全部' || item.category === this.activeCategory
					return sceneOk && categoryOk
				})
			}
		},
		onLoad() {
			const sys = uni.getWindowInfo()
			this.statusBarHeight = sys.statusBarHeight || 24
			const windowHeight = sys.windowHeight || 667
			const navHeight = this.statusBarHeight + 120
			const tabHeight = 56
			this.scrollHeight = windowHeight - navHeight - tabHeight
		},
		methods: {
			setScene(scene) {
				this.activeScene = scene
			},
			scrollToGoods() {
			},
			goPublish() {
				uni.navigateTo({ url: '/pages/publish/publish' })
			},
			openDetail(item) {
				uni.navigateTo({ url: buildGoodsDetailUrl(item) })
			}
		}
	}
</script>

<style lang="scss" scoped>
	$page: #f5f4f1;
	$forest: #1b4332;
	$forest2: #2d6a4f;
	$paper: #ffffff;
	$muted: #6b6b6b;
	$border: rgba(27, 67, 50, 0.08);

	.page {
		min-height: 100vh;
		background: $page;
		position: relative;
	}
	.nav-shell {
		background: linear-gradient(180deg, #e8ebe4 0%, $page 100%);
	}
	.nav {
		padding-bottom: 20rpx;
	}
	.nav-inner {
		display: flex;
		flex-direction: row;
		align-items: center;
		justify-content: space-between;
		padding: 12rpx 32rpx 0;
	}
	.brand {
		display: flex;
		flex-direction: column;
	}
	.brand-name {
		font-size: 44rpx;
		font-weight: 700;
		letter-spacing: 4rpx;
		color: $forest;
	}
	.brand-sub {
		font-size: 22rpx;
		color: $muted;
		margin-top: 6rpx;
	}
	.ghost-btn {
		padding: 14rpx 28rpx;
		border-radius: 999rpx;
		border: 2rpx solid rgba(27, 67, 50, 0.24);
		font-size: 24rpx;
		color: $forest;
		background: rgba(255, 255, 255, 0.72);
	}
	.search {
		margin: 24rpx 32rpx 0;
		height: 76rpx;
		border-radius: 38rpx;
		background: $paper;
		box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.06);
		display: flex;
		flex-direction: row;
		align-items: center;
		padding: 0 28rpx;
	}
	.search-icon {
		font-size: 32rpx;
		margin-right: 16rpx;
		opacity: 0.45;
	}
	.search-placeholder {
		font-size: 28rpx;
		color: #9a9a9a;
	}
	.scroll-shell {
		box-sizing: border-box;
	}
	.container {
		padding: 28rpx 24rpx 120rpx;
	}
	.hero {
		display: flex;
		flex-direction: row;
		gap: 20rpx;
	}
	.hero-card {
		flex: 1;
		border-radius: 28rpx;
		padding: 30rpx 26rpx;
		min-height: 220rpx;
		box-sizing: border-box;
		position: relative;
		overflow: hidden;
	}
	.hero-card.active {
		transform: translateY(-4rpx);
		box-shadow: 0 20rpx 46rpx rgba(0, 0, 0, 0.08);
	}
	.hero-card.new {
		background: linear-gradient(135deg, #1b4332 0%, #2d6a4f 100%);
		color: #fff;
	}
	.hero-card.used {
		background: $paper;
		color: $forest;
		border: 2rpx solid $border;
	}
	.hero-kicker,
	.hero-desc {
		font-size: 22rpx;
	}
	.hero-kicker {
		opacity: 0.85;
	}
	.hero-title {
		font-size: 38rpx;
		font-weight: 700;
		margin-top: 10rpx;
	}
	.hero-desc {
		margin-top: 16rpx;
		line-height: 1.5;
	}
	.hero-action {
		display: flex;
		flex-direction: row;
		align-items: center;
		justify-content: center;
		margin-top: 24rpx;
		padding: 10rpx 20rpx;
		border-radius: 999rpx;
		font-size: 22rpx;
		background: rgba(255, 255, 255, 0.14);
	}
	.hero-card.used .hero-action {
		background: #edf7f1;
		color: $forest2;
	}
	.section-head {
		display: flex;
		flex-direction: row;
		align-items: center;
		justify-content: space-between;
		margin-top: 34rpx;
	}
	.section-title {
		font-size: 32rpx;
		font-weight: 700;
		color: #222;
	}
	.section-tip,
	.section-more {
		font-size: 24rpx;
		color: $forest2;
	}
	.toolbar {
		margin-top: 18rpx;
		display: flex;
		flex-direction: column;
		gap: 18rpx;
	}
	.chips {
		white-space: nowrap;
	}
	.chip {
		display: inline-flex;
		align-items: center;
		justify-content: center;
		padding: 14rpx 26rpx;
		border-radius: 999rpx;
		margin-right: 14rpx;
		font-size: 24rpx;
		background: $paper;
		color: #555;
	}
	.chip.soft {
		background: #eef1ee;
	}
	.chip.on {
		background: $forest;
		color: #fff;
	}
	.mode-switch {
		display: flex;
		flex-direction: row;
		align-self: flex-start;
		background: #edf2ef;
		border-radius: 999rpx;
		padding: 6rpx;
	}
	.mode-item {
		min-width: 110rpx;
		padding: 12rpx 16rpx;
		border-radius: 999rpx;
		text-align: center;
		font-size: 24rpx;
		color: $muted;
	}
	.mode-item.on {
		background: $paper;
		color: $forest;
		box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);
	}
	.categories {
		margin-top: 16rpx;
	}
	.goods-grid {
		margin-top: 24rpx;
		display: flex;
		flex-direction: row;
		flex-wrap: wrap;
		gap: 20rpx;
	}
	.goods-grid.double .goods-card {
		width: calc(50% - 10rpx);
	}
	.goods-grid.single .goods-card {
		width: 100%;
	}
	.goods-card {
		background: $paper;
		border-radius: 24rpx;
		padding: 20rpx;
		box-shadow: 0 8rpx 28rpx rgba(0, 0, 0, 0.05);
		box-sizing: border-box;
	}
	.goods-grid.single .goods-card {
		display: flex;
		flex-direction: row;
		gap: 20rpx;
		align-items: center;
	}
	.goods-cover {
		height: 220rpx;
		border-radius: 18rpx;
		background: #eef2ef;
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 76rpx;
	}
	.goods-grid.single .goods-cover {
		width: 220rpx;
		height: 220rpx;
		flex-shrink: 0;
	}
	.goods-main {
		flex: 1;
		min-width: 0;
		margin-top: 16rpx;
	}
	.goods-grid.single .goods-main {
		margin-top: 0;
	}
	.goods-topline,
	.goods-extra {
		display: flex;
		flex-direction: row;
		align-items: center;
		justify-content: space-between;
	}
	.goods-scene,
	.goods-tag,
	.goods-credit,
	.goods-location {
		font-size: 22rpx;
	}
	.goods-scene {
		color: $forest2;
	}
	.goods-tag {
		padding: 6rpx 12rpx;
		border-radius: 10rpx;
		background: rgba(45, 106, 79, 0.12);
		color: $forest2;
	}
	.goods-title {
		margin-top: 12rpx;
		font-size: 30rpx;
		font-weight: 700;
		color: #222;
		line-height: 1.35;
	}
	.goods-subtitle {
		margin-top: 10rpx;
		font-size: 24rpx;
		color: $muted;
		line-height: 1.5;
	}
	.goods-meta {
		display: flex;
		flex-direction: row;
		align-items: baseline;
		gap: 12rpx;
		margin-top: 16rpx;
	}
	.goods-price {
		font-size: 34rpx;
		font-weight: 700;
		color: #c45c26;
	}
	.goods-origin {
		font-size: 22rpx;
		color: #aaa;
		text-decoration: line-through;
	}
	.goods-extra {
		margin-top: 14rpx;
		color: $muted;
	}
	.spotlight {
		margin-top: 18rpx;
		display: flex;
		flex-direction: row;
		gap: 20rpx;
	}
	.spot-card {
		flex: 1;
		border-radius: 24rpx;
		padding: 28rpx 24rpx;
		background: linear-gradient(140deg, #113428, #1b4332);
		color: #fff;
	}
	.spot-card.alt {
		background: $paper;
		color: #1f2937;
		border: 2rpx solid $border;
	}
	.spot-kicker,
	.spot-desc {
		font-size: 22rpx;
	}
	.spot-title {
		margin-top: 12rpx;
		font-size: 32rpx;
		font-weight: 700;
		line-height: 1.35;
	}
	.spot-desc {
		margin-top: 16rpx;
		line-height: 1.6;
	}
	.spot-price {
		margin-top: 20rpx;
		font-size: 34rpx;
		font-weight: 700;
	}
	.fab {
		position: fixed;
		right: 28rpx;
		bottom: 140rpx;
		background: $forest;
		color: #fff;
		padding: 20rpx 32rpx;
		border-radius: 999rpx;
		box-shadow: 0 12rpx 40rpx rgba(27, 67, 50, 0.35);
		z-index: 20;
	}
	.fab-text {
		font-size: 26rpx;
		font-weight: 600;
	}
</style>