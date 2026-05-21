<template>
	<view class="safe-page home-page">
		<view class="topbar" :style="{ paddingTop: statusBarHeight + 'px' }">
			<view class="content-wrap topbar-inner">
				<view class="brand" @click="setScene('all')">
					<text class="brand-mark">松果</text>
					<view class="brand-copy">
						<text class="brand-name">松果集市</text>
						<text class="brand-sub">买新品，也淘有故事的闲置</text>
					</view>
				</view>
				<view class="search">
					<text class="search-icon">⌕</text>
					<input v-model="keyword" class="search-input" placeholder="搜索商品、店铺、求购意图" confirm-type="search" @confirm="applySearch" />
					<view class="search-action" @click="applySearch">搜索</view>
				</view>
				<view class="publish-btn" @click="goPublish">发布闲置</view>
			</view>
		</view>

		<scroll-view scroll-y class="scroll-shell">
			<view class="content-wrap main">
				<view class="hero">
					<view class="hero-main">
						<text class="eyebrow">安全交易 · 信用可见 · AI 辅助议价</text>
						<text class="hero-title">在一个平台里，放心买新，也轻松淘旧。</text>
						<text class="hero-desc">新品购物有标准保障，二手交易有信用分、验货清单、物品流转故事和平台担保。</text>
						<view class="hero-actions">
							<view class="primary-btn" @click="setScene('new')">逛新品</view>
							<view class="secondary-btn" @click="setScene('used')">淘二手</view>
						</view>
					</view>
					<view class="hero-panel">
						<view class="panel-head">
							<text>今日可信交易</text>
							<text class="panel-badge">模拟数据</text>
						</view>
						<view class="metric-grid">
							<view v-for="metric in metrics" :key="metric.label" class="metric">
								<text class="metric-value">{{ metric.value }}</text>
								<text class="metric-label">{{ metric.label }}</text>
							</view>
						</view>
						<view class="ai-card">
							<text class="ai-title">AI 议价助手</text>
							<text class="ai-text">帮买家询问瑕疵、建议报价，并生成交易共识清单。</text>
						</view>
					</view>
				</view>

				<view class="quick-grid">
					<view v-for="item in quickLinks" :key="item.title" class="quick-card" @click="quickOpen(item)">
						<text class="quick-icon">{{ item.icon }}</text>
						<text class="quick-title">{{ item.title }}</text>
						<text class="quick-desc">{{ item.desc }}</text>
					</view>
				</view>

				<view class="section-row">
					<view>
						<text class="section-title">精选商品</text>
						<text class="section-desc">按新品、二手、分类与信用筛选</text>
					</view>
					<view class="mode-switch">
						<view
							v-for="tab in sceneTabs"
							:key="tab.key"
							class="mode-item"
							:class="{ on: activeScene === tab.key }"
							@click="setScene(tab.key)"
						>
							{{ tab.label }}
						</view>
					</view>
				</view>

				<scroll-view scroll-x class="category-line" :show-scrollbar="false">
					<view
						v-for="category in categories"
						:key="category"
						class="category-chip"
						:class="{ on: activeCategory === category }"
						@click="activeCategory = category"
					>
						{{ category }}
					</view>
				</scroll-view>

				<view class="layout">
					<view class="goods-grid">
						<view v-for="item in displayGoods" :key="item.id" class="goods-card" @click="openDetail(item)">
							<view class="cover">{{ item.cover }}</view>
							<view class="goods-body">
								<view class="goods-tags">
									<text class="scene-tag" :class="item.scene">{{ item.scene === 'new' ? '新品' : '二手' }}</text>
									<text class="light-tag">{{ item.condition }}</text>
								</view>
								<text class="goods-title">{{ item.title }}</text>
								<text class="goods-sub">{{ item.subtitle }}</text>
								<view class="price-row">
									<text class="price">¥{{ item.price }}</text>
									<text class="origin">¥{{ item.originPrice }}</text>
								</view>
								<view class="goods-foot">
									<text>信用 {{ item.credit }}</text>
									<text>{{ item.location }}</text>
								</view>
							</view>
						</view>
					</view>

					<view class="side">
						<view class="side-card">
							<text class="side-title">交易保障</text>
							<view v-for="item in guardrails" :key="item.title" class="guard">
								<text class="guard-icon">{{ item.icon }}</text>
								<view>
									<text class="guard-title">{{ item.title }}</text>
									<text class="guard-desc">{{ item.desc }}</text>
								</view>
							</view>
						</view>
						<view class="side-card story">
							<text class="side-title">二手流浪时间线</text>
							<text class="story-title">{{ usedSpot.title }}</text>
							<view v-for="node in usedSpot.timeline" :key="node.date" class="timeline-node">
								<text class="dot"></text>
								<view>
									<text class="node-date">{{ node.date }}</text>
									<text class="node-text">{{ node.title }}</text>
								</view>
							</view>
							<view class="story-link" @click="openDetail(usedSpot)">查看物品护照</view>
						</view>
					</view>
				</view>
			</view>
		</scroll-view>
	</view>
</template>

<script>
	import { goodsCatalog, buildGoodsDetailUrl } from '../../data/catalog.js'

	export default {
		data() {
			return {
				statusBarHeight: 24,
				activeScene: 'all',
				activeCategory: '全部',
				keyword: ''
			}
		},
		computed: {
			sceneTabs() {
				return [
					{ key: 'all', label: '全部' },
					{ key: 'new', label: '买新品' },
					{ key: 'used', label: '淘二手' }
				]
			},
			categories() {
				return ['全部'].concat(Array.from(new Set(goodsCatalog.map((item) => item.category))))
			},
			metrics() {
				return [
					{ label: '信用卖家', value: '128' },
					{ label: '担保订单', value: '2.4k' },
					{ label: '二手故事', value: '86' }
				]
			},
			quickLinks() {
				return [
					{ icon: '🛒', title: '新品严选', desc: '正品保障', scene: 'new' },
					{ icon: '♻️', title: '闲置好物', desc: '信用可见', scene: 'used' },
					{ icon: '🤖', title: 'AI 议价', desc: '自动问答', path: '/pages/message/message' },
					{ icon: '📦', title: '发布商品', desc: 'AI 估价', path: '/pages/publish/publish' }
				]
			},
			guardrails() {
				return [
					{ icon: '✓', title: '平台担保', desc: '买家确认收货后卖家收款' },
					{ icon: '★', title: '信用评分', desc: '成交率、评价和纠纷记录综合计算' },
					{ icon: '↺', title: '售后协商', desc: '退款、证据上传和平台介入流程' }
				]
			},
			usedSpot() {
				return goodsCatalog.find((item) => item.scene === 'used' && item.timeline.length) || goodsCatalog[0]
			},
			displayGoods() {
				const kw = this.keyword.trim().toLowerCase()
				return goodsCatalog.filter((item) => {
					const sceneOk = this.activeScene === 'all' || item.scene === this.activeScene
					const categoryOk = this.activeCategory === '全部' || item.category === this.activeCategory
					const keywordOk = !kw || [item.title, item.subtitle, item.category, item.shopName].join(' ').toLowerCase().includes(kw)
					return sceneOk && categoryOk && keywordOk
				})
			}
		},
		onLoad() {
			const sys = uni.getWindowInfo()
			this.statusBarHeight = sys.statusBarHeight || 24
		},
		methods: {
			setScene(scene) {
				this.activeScene = scene
			},
			applySearch() {
				uni.showToast({ title: this.keyword ? '已筛选相关商品' : '请输入搜索关键词', icon: 'none' })
			},
			quickOpen(item) {
				if (item.scene) {
					this.setScene(item.scene)
					return
				}
				uni.navigateTo({ url: item.path })
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
	.home-page {
		padding-bottom: 40rpx;
	}
	.topbar {
		position: sticky;
		top: 0;
		z-index: 10;
		background: rgba(244, 246, 244, 0.96);
		border-bottom: 1rpx solid #e4e9e5;
	}
	.topbar-inner {
		display: flex;
		align-items: center;
		gap: 24rpx;
		padding: 18rpx 28rpx;
	}
	.brand {
		display: flex;
		align-items: center;
		gap: 14rpx;
		flex-shrink: 0;
	}
	.brand-mark {
		width: 72rpx;
		height: 72rpx;
		border-radius: 20rpx;
		background: #1f5c43;
		color: #fff;
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 24rpx;
		font-weight: 700;
	}
	.brand-copy {
		display: flex;
		flex-direction: column;
	}
	.brand-name {
		font-size: 34rpx;
		font-weight: 800;
		color: #163528;
	}
	.brand-sub {
		font-size: 22rpx;
		color: #667085;
		margin-top: 4rpx;
	}
	.search {
		flex: 1;
		height: 76rpx;
		border-radius: 18rpx;
		background: #fff;
		display: flex;
		align-items: center;
		padding: 0 14rpx 0 24rpx;
		box-shadow: 0 8rpx 24rpx rgba(15, 35, 26, 0.06);
		min-width: 0;
	}
	.search-icon {
		font-size: 30rpx;
		color: #667085;
		margin-right: 12rpx;
	}
	.search-input {
		flex: 1;
		font-size: 26rpx;
		min-width: 0;
	}
	.search-action,
	.publish-btn,
	.primary-btn,
	.secondary-btn,
	.story-link {
		display: flex;
		align-items: center;
		justify-content: center;
		border-radius: 16rpx;
		font-size: 26rpx;
		font-weight: 700;
	}
	.search-action {
		width: 104rpx;
		height: 56rpx;
		background: #1f5c43;
		color: #fff;
	}
	.publish-btn {
		height: 76rpx;
		padding: 0 26rpx;
		background: #fff0e7;
		color: #b95420;
		flex-shrink: 0;
	}
	.scroll-shell {
		height: calc(100vh - 104rpx);
	}
	.main {
		padding: 28rpx;
	}
	.hero {
		display: grid;
		grid-template-columns: minmax(0, 1.6fr) minmax(280px, 0.8fr);
		gap: 24rpx;
	}
	.hero-main,
	.hero-panel,
	.quick-card,
	.goods-card,
	.side-card {
		background: #fff;
		border: 1rpx solid #e4e9e5;
		box-shadow: 0 14rpx 36rpx rgba(15, 35, 26, 0.06);
	}
	.hero-main {
		border-radius: 28rpx;
		padding: 54rpx;
		background: linear-gradient(135deg, #fdfefe, #edf7f1);
	}
	.eyebrow {
		font-size: 24rpx;
		color: #1f5c43;
		font-weight: 700;
	}
	.hero-title {
		display: block;
		margin-top: 20rpx;
		font-size: 58rpx;
		line-height: 1.15;
		font-weight: 800;
		color: #163528;
		max-width: 760rpx;
	}
	.hero-desc {
		display: block;
		margin-top: 22rpx;
		font-size: 28rpx;
		line-height: 1.7;
		color: #4b5563;
		max-width: 760rpx;
	}
	.hero-actions {
		display: flex;
		gap: 18rpx;
		margin-top: 34rpx;
	}
	.primary-btn,
	.secondary-btn {
		height: 78rpx;
		padding: 0 34rpx;
	}
	.primary-btn {
		background: #1f5c43;
		color: #fff;
	}
	.secondary-btn {
		background: #fff;
		color: #1f5c43;
		border: 1rpx solid #cfe1d6;
	}
	.hero-panel {
		border-radius: 28rpx;
		padding: 30rpx;
	}
	.panel-head,
	.section-row,
	.price-row,
	.goods-foot {
		display: flex;
		align-items: center;
		justify-content: space-between;
	}
	.panel-head {
		font-size: 30rpx;
		font-weight: 800;
		color: #163528;
	}
	.panel-badge {
		font-size: 22rpx;
		color: #d66a2c;
		background: #fff0e7;
		padding: 8rpx 14rpx;
		border-radius: 999rpx;
	}
	.metric-grid {
		display: grid;
		grid-template-columns: repeat(3, 1fr);
		gap: 14rpx;
		margin-top: 24rpx;
	}
	.metric {
		background: #f4f6f4;
		border-radius: 18rpx;
		padding: 20rpx 10rpx;
		text-align: center;
	}
	.metric-value {
		display: block;
		font-size: 32rpx;
		font-weight: 800;
		color: #1f5c43;
	}
	.metric-label {
		display: block;
		margin-top: 6rpx;
		font-size: 22rpx;
		color: #667085;
	}
	.ai-card {
		margin-top: 22rpx;
		border-radius: 20rpx;
		padding: 24rpx;
		background: #163528;
		color: #fff;
	}
	.ai-title {
		display: block;
		font-size: 30rpx;
		font-weight: 800;
	}
	.ai-text {
		display: block;
		margin-top: 12rpx;
		font-size: 24rpx;
		line-height: 1.6;
		opacity: 0.9;
	}
	.quick-grid {
		display: grid;
		grid-template-columns: repeat(4, 1fr);
		gap: 18rpx;
		margin-top: 24rpx;
	}
	.quick-card {
		border-radius: 22rpx;
		padding: 24rpx;
	}
	.quick-icon,
	.quick-title,
	.quick-desc {
		display: block;
	}
	.quick-icon {
		font-size: 44rpx;
	}
	.quick-title {
		margin-top: 12rpx;
		font-size: 28rpx;
		font-weight: 800;
		color: #17231d;
	}
	.quick-desc {
		margin-top: 6rpx;
		font-size: 23rpx;
		color: #667085;
	}
	.section-row {
		margin-top: 38rpx;
		gap: 18rpx;
	}
	.section-title {
		display: block;
		font-size: 36rpx;
		font-weight: 800;
		color: #17231d;
	}
	.section-desc {
		display: block;
		margin-top: 6rpx;
		font-size: 24rpx;
		color: #667085;
	}
	.mode-switch {
		display: flex;
		background: #e8f0eb;
		border-radius: 18rpx;
		padding: 6rpx;
	}
	.mode-item {
		min-width: 112rpx;
		text-align: center;
		padding: 14rpx 18rpx;
		border-radius: 14rpx;
		font-size: 25rpx;
		color: #667085;
	}
	.mode-item.on {
		background: #fff;
		color: #1f5c43;
		font-weight: 800;
		box-shadow: 0 6rpx 18rpx rgba(15, 35, 26, 0.06);
	}
	.category-line {
		white-space: nowrap;
		margin-top: 20rpx;
	}
	.category-chip {
		display: inline-flex;
		padding: 14rpx 24rpx;
		margin-right: 12rpx;
		border-radius: 999rpx;
		background: #fff;
		color: #667085;
		font-size: 24rpx;
		border: 1rpx solid #e4e9e5;
	}
	.category-chip.on {
		background: #1f5c43;
		color: #fff;
		border-color: #1f5c43;
	}
	.layout {
		display: grid;
		grid-template-columns: minmax(0, 1fr) 320px;
		gap: 24rpx;
		margin-top: 22rpx;
	}
	.goods-grid {
		display: grid;
		grid-template-columns: repeat(3, minmax(0, 1fr));
		gap: 20rpx;
	}
	.goods-card {
		border-radius: 22rpx;
		overflow: hidden;
	}
	.cover {
		height: 220rpx;
		background: #edf3ef;
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 88rpx;
	}
	.goods-body {
		padding: 22rpx;
	}
	.goods-tags {
		display: flex;
		gap: 10rpx;
		flex-wrap: wrap;
	}
	.scene-tag,
	.light-tag {
		font-size: 21rpx;
		padding: 6rpx 12rpx;
		border-radius: 999rpx;
	}
	.scene-tag.new {
		background: #e8f3ed;
		color: #1f5c43;
	}
	.scene-tag.used {
		background: #fff0e7;
		color: #b95420;
	}
	.light-tag {
		background: #f4f6f4;
		color: #667085;
	}
	.goods-title {
		display: block;
		margin-top: 14rpx;
		font-size: 29rpx;
		font-weight: 800;
		color: #17231d;
		line-height: 1.35;
	}
	.goods-sub {
		display: block;
		margin-top: 8rpx;
		font-size: 23rpx;
		color: #667085;
		line-height: 1.5;
	}
	.price-row {
		margin-top: 16rpx;
		justify-content: flex-start;
		gap: 12rpx;
	}
	.price {
		font-size: 34rpx;
		font-weight: 900;
		color: #d66a2c;
	}
	.origin {
		font-size: 22rpx;
		color: #9ca3af;
		text-decoration: line-through;
	}
	.goods-foot {
		margin-top: 14rpx;
		font-size: 22rpx;
		color: #667085;
	}
	.side {
		display: flex;
		flex-direction: column;
		gap: 20rpx;
	}
	.side-card {
		border-radius: 22rpx;
		padding: 24rpx;
	}
	.side-title {
		display: block;
		font-size: 30rpx;
		font-weight: 800;
		color: #17231d;
		margin-bottom: 18rpx;
	}
	.guard {
		display: flex;
		gap: 14rpx;
		padding: 16rpx 0;
		border-top: 1rpx solid #eef1ee;
	}
	.guard-icon {
		width: 42rpx;
		height: 42rpx;
		border-radius: 50%;
		background: #e8f3ed;
		color: #1f5c43;
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 24rpx;
		font-weight: 800;
		flex-shrink: 0;
	}
	.guard-title,
	.guard-desc,
	.story-title,
	.node-date,
	.node-text {
		display: block;
	}
	.guard-title {
		font-size: 26rpx;
		font-weight: 800;
		color: #17231d;
	}
	.guard-desc {
		margin-top: 4rpx;
		font-size: 22rpx;
		color: #667085;
		line-height: 1.5;
	}
	.story-title {
		font-size: 28rpx;
		font-weight: 800;
		color: #1f5c43;
		margin-bottom: 16rpx;
	}
	.timeline-node {
		display: flex;
		gap: 12rpx;
		padding: 12rpx 0;
	}
	.dot {
		width: 14rpx;
		height: 14rpx;
		border-radius: 50%;
		background: #d66a2c;
		margin-top: 10rpx;
		flex-shrink: 0;
	}
	.node-date {
		font-size: 21rpx;
		color: #667085;
	}
	.node-text {
		margin-top: 2rpx;
		font-size: 24rpx;
		color: #17231d;
	}
	.story-link {
		margin-top: 18rpx;
		height: 64rpx;
		background: #e8f3ed;
		color: #1f5c43;
	}

	@media screen and (max-width: 900px) {
		.topbar-inner,
		.hero,
		.layout,
		.quick-grid,
		.goods-grid {
			display: flex;
			flex-direction: column;
		}
		.brand-sub,
		.publish-btn,
		.side {
			display: none;
		}
		.hero-main {
			padding: 38rpx 30rpx;
		}
		.hero-title {
			font-size: 44rpx;
		}
		.quick-grid {
			gap: 14rpx;
		}
		.quick-card,
		.goods-card {
			width: 100%;
			box-sizing: border-box;
		}
		.section-row {
			align-items: flex-start;
			flex-direction: column;
		}
	}
</style>
