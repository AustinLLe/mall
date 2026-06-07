<template>
	<view class="safe-page home-page">
		<view class="topbar">
			<view class="content-wrap topbar-inner">
				<view class="brand" @click="setScene('all')">
					<view class="brand-mark">
						<image class="brand-logo" src="/static/logo.png" mode="aspectFit"></image>
					</view>
					<view class="brand-copy">
						<text class="brand-name">松果集市</text>
						<text class="brand-sub">可信的新旧商品流转平台</text>
					</view>
				</view>
				<view class="web-nav">
					<text class="nav-link on" @click="navTo('/pages/home/home')">首页</text>
					<text class="nav-link" @click="navTo('/pages/browse/browse')">发现</text>
					<text class="nav-link" @click="navTo('/pages/cart/cart')">购物车</text>
					<text class="nav-link" @click="navTo('/pages/message/message')">消息</text>
					<text class="nav-link" @click="navTo('/pages/user/index')">我的</text>
				</view>
				<view class="top-actions">
					<view class="search">
						<text class="search-icon">⌕</text>
						<input v-model="keyword" class="search-input" placeholder="搜索商品、店铺、关键词" confirm-type="search" @confirm="applySearch" />
						<view class="search-action" @click="applySearch">搜索</view>
					</view>
					<view class="publish-btn" @click="goPublish">发布闲置</view>
				</view>
			</view>
		</view>

		<scroll-view scroll-y class="scroll-shell">
			<view class="content-wrap main">
				<view class="hero">
					<view class="hero-copy">
						<text class="eyebrow">New goods · Second life · Credit first</text>
						<text class="hero-title">精选新品与优质闲置，一站式安心交易平台。</text>
						<text class="hero-desc">买家可以放心选购与下单，卖家可以发布商品、管理订单，管理员负责审核商品与维护交易秩序。</text>
						<view class="hero-actions">
							<view class="primary-btn" @click="setScene('new')">看新品</view>
							<view class="secondary-btn" @click="setScene('used')">淘二手</view>
						</view>
					</view>
					<view class="hero-board">
						<view class="board-head">
							<text>今日可信交易</text>
							<text class="board-pill">今日推荐</text>
						</view>
						<view class="metric-grid">
							<view v-for="metric in metrics" :key="metric.label" class="metric">
								<text class="metric-value">{{ metric.value }}</text>
								<text class="metric-label">{{ metric.label }}</text>
							</view>
						</view>
						<view class="focus-item" @click="openDetail(featured)">
							<view class="focus-cover" :class="{ 'has-image': isImageUrl(featured.cover) }">
								<image v-if="isImageUrl(featured.cover)" class="cover-img" :src="resolveImageUrl(featured.cover)" mode="aspectFill"></image>
								<text v-else>{{ featured.cover }}</text>
							</view>
							<view>
								<text class="focus-label">编辑推荐</text>
								<text class="focus-title">{{ featured.title }}</text>
								<text class="focus-price">¥{{ featured.price }}</text>
							</view>
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
						<text class="section-desc">按新旧、分类、信用与关键词筛选</text>
					</view>
					<view class="toolbar-group">
						<view class="mode-switch">
							<view v-for="tab in sceneTabs" :key="tab.key" class="mode-item" :class="{ on: activeScene === tab.key }" @click="setScene(tab.key)">
								{{ tab.label }}
							</view>
						</view>
						<view class="sort-switch">
							<text v-for="item in sortTabs" :key="item.key" class="sort-item" :class="{ on: activeSort === item.key }" @click="activeSort = item.key">{{ item.label }}</text>
						</view>
					</view>
				</view>

				<view v-if="loadError" class="sync-tip">当前先为你展示精选商品，稍后会自动刷新更多库存。</view>

				<scroll-view scroll-x class="category-line" :show-scrollbar="false">
					<view v-for="category in categories" :key="category" class="category-chip" :class="{ on: activeCategory === category }" @click="activeCategory = category">
						{{ category }}
					</view>
				</scroll-view>

				<view class="product-layout">
					<view class="goods-grid js-home-grid">
						<view v-for="item in displayGoods" :key="item.id" class="goods-card" @click="openDetail(item)">
							<view class="cover" :class="[visualClass(item), { 'has-image': isImageUrl(item.cover) }]">
								<image v-if="isImageUrl(item.cover)" class="cover-img" :src="resolveImageUrl(item.cover)" mode="aspectFill"></image>
								<text v-else>{{ item.cover }}</text>
							</view>
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
								<view class="goods-actions">
									<text class="mini-link" @click.stop="favoriteItem(item)">收藏</text>
									<text class="mini-link" @click.stop="openStore(item)">进店</text>
								</view>
							</view>
						</view>
					</view>

					<view class="side js-home-side">
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
						<view class="side-card timeline-side-card">
							<text class="side-title">物品时间线</text>
							<text class="story-title">{{ usedSpot.title }}</text>
							<wandering-timeline
								v-if="usedSpotTimeline.length"
								compact
								:nodes="usedSpotTimeline"
								title="流浪预览"
								subtitle=""
								badge=""
							/>
							<view class="story-link" @click="openDetail(usedSpot)">查看完整故事</view>
						</view>
						<view class="side-card recommend-card" v-if="sideRecommendations.length">
							<text class="side-title">猜你喜欢</text>
							<view v-for="item in sideRecommendations" :key="item.id" class="recommend-item" @click="openDetail(item)">
								<view class="recommend-cover" :class="{ 'has-image': isImageUrl(item.cover) }">
									<image v-if="isImageUrl(item.cover)" class="cover-img" :src="resolveImageUrl(item.cover)" mode="aspectFill"></image>
									<text v-else>{{ item.cover }}</text>
								</view>
								<view>
									<text class="recommend-title">{{ item.title }}</text>
									<text class="recommend-price">¥{{ item.price }}</text>
								</view>
							</view>
						</view>
						<view v-if="homeSideSpacer > 0" class="side-spacer" :style="{ height: homeSideSpacer + 'px' }"></view>
					</view>
				</view>
			</view>
		</scroll-view>
	</view>
</template>

<script>
	import { goodsCatalog, buildGoodsDetailUrl } from '../../data/catalog.js'
	import { addBuyerItem } from '@/services/center.js'
	import { fetchProducts } from '@/services/shop.js'
	import WanderingTimeline from '@/components/wandering-timeline/wandering-timeline.vue'
	import { isImageUrl, resolveImageUrl } from '@/utils/media.js'

	function mergeProducts(localList, remoteList) {
		const merged = localList.slice()
		remoteList.forEach((remote) => {
			const index = merged.findIndex((item) => item.id === remote.id || item.title === remote.title)
			if (index >= 0) {
				merged.splice(index, 1, Object.assign({}, merged[index], remote))
			} else {
				merged.push(remote)
			}
		})
		return merged
	}

	export default {
		components: { WanderingTimeline },
		data() {
			return {
				statusBarHeight: 24,
				activeScene: 'all',
				activeSort: 'recommend',
				activeCategory: '全部',
				keyword: '',
				goodsList: goodsCatalog,
				loadError: false,
				homeRecommendCount: 0,
				homeSideSpacer: 0
			}
		},
		computed: {
			sceneTabs() {
				return [
					{ key: 'all', label: '全部' },
					{ key: 'new', label: '新品' },
					{ key: 'used', label: '二手' }
				]
			},
			sortTabs() {
				return [
					{ key: 'recommend', label: '推荐' },
					{ key: 'credit', label: '信用优先' },
					{ key: 'price', label: '价格优先' }
				]
			},
			categories() {
				return ['全部'].concat(Array.from(new Set(this.goodsList.map((item) => item.category))))
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
					{ icon: '✓', title: '新品严选', desc: '正品与售后', scene: 'new' },
					{ icon: '↻', title: '闲置好物', desc: '信用可见', scene: 'used' },
					{ icon: 'AI', title: '议价助手', desc: '生成验货清单', path: '/pages/message/message' },
					{ icon: '+', title: '发布商品', desc: '快速上架', path: '/pages/publish/publish' }
				]
			},
			guardrails() {
				return [
					{ icon: '1', title: '平台担保', desc: '买家确认收货后，卖家再收到款项。' },
					{ icon: '2', title: '信用评分', desc: '成交率、评价和纠纷记录综合计算。' },
					{ icon: '3', title: '售后协商', desc: '支持证据上传、退款沟通和平台介入。' }
				]
			},
			featured() {
				return this.goodsList[0] || goodsCatalog[0]
			},
			usedSpot() {
				return this.goodsList.find((item) => item.scene === 'used' && item.timeline && item.timeline.length) || this.featured
			},
			usedSpotTimeline() {
				return (this.usedSpot.timeline || []).slice(0, 3).map((node, index) => ({
					date: node.date || node.time,
					title: node.title,
					text: node.text || node.desc || '',
					icon: node.icon || ['买', '用', '发'][index] || '记'
				}))
			},
			displayGoods() {
				const kw = this.keyword.trim().toLowerCase()
				const list = this.goodsList.filter((item) => {
					const sceneOk = this.activeScene === 'all' || item.scene === this.activeScene
					const categoryOk = this.activeCategory === '全部' || item.category === this.activeCategory
					const keywordOk = !kw || [item.title, item.subtitle, item.category, item.shopName].join(' ').toLowerCase().includes(kw)
					return sceneOk && categoryOk && keywordOk
				})
				if (this.activeSort === 'credit') return list.slice().sort((a, b) => (b.credit || 0) - (a.credit || 0))
				if (this.activeSort === 'price') return list.slice().sort((a, b) => Number(a.price || 0) - Number(b.price || 0))
				return list
			},
			sideRecommendations() {
				const picked = []
				const append = (items) => {
					items.forEach((item) => {
						if (item.id !== this.featured.id && item.id !== this.usedSpot.id && !picked.some((current) => current.id === item.id)) {
							picked.push(item)
						}
					})
				}
				append(this.displayGoods)
				append(this.goodsList)
				append(goodsCatalog)
				return picked.slice(0, this.homeRecommendCount)
			}
		},
		watch: {
			displayGoods() {
				this.$nextTick(() => this.syncHomeSideLength())
			}
		},
		onLoad() {
			const sys = uni.getWindowInfo()
			this.statusBarHeight = sys.statusBarHeight || 24
			this.loadProducts()
			this.$nextTick(() => this.syncHomeSideLength())
		},
		methods: {
			isImageUrl,
			resolveImageUrl,
			async loadProducts() {
				try {
					const body = await fetchProducts({ scene: this.activeScene === 'all' ? '' : this.activeScene, keyword: this.keyword })
					if (body && body.code === 0 && Array.isArray(body.data) && body.data.length) {
						this.goodsList = mergeProducts(goodsCatalog, body.data)
						this.loadError = false
					}
				} catch (e) {
					this.goodsList = goodsCatalog
					this.loadError = true
				} finally {
					this.$nextTick(() => this.syncHomeSideLength())
				}
			},
			syncHomeSideLength() {
				// Reset to 0 first so we measure the baseline without the recommend card.
				this.homeRecommendCount = 0
				this.homeSideSpacer = 0
				this.$nextTick(() => {
					const query = uni.createSelectorQuery().in(this)
					query.select('.js-home-grid').boundingClientRect()
					query.select('.js-home-side').boundingClientRect()
					query.exec((res) => {
						if (!Array.isArray(res) || !res[0] || !res[1]) return
						const diff = Math.round((res[0].height || 0) - (res[1].height || 0))
						if (diff <= 0) return
						// Constants (px):
						// CARD_OVERHEAD = gap(14) + padTop(20) + padBottom(14) + title(18+10=28) + firstItemMarginTop(4) = 80
						// ITEM_H        = padTop(10) + cover(62) + padBottom(10) = 82
						const CARD_OVERHEAD = 80
						const ITEM_H = 82
						if (diff < CARD_OVERHEAD + ITEM_H) {
							// Not enough room for even one item — use only spacer
							this.homeRecommendCount = 0
							this.homeSideSpacer = diff
						} else {
							const count = Math.min(2, Math.floor((diff - CARD_OVERHEAD) / ITEM_H))
							this.homeRecommendCount = count
							this.homeSideSpacer = Math.max(0, diff - CARD_OVERHEAD - count * ITEM_H)
						}
					})
				})
			},
			setScene(scene) {
				this.activeScene = scene
				this.loadProducts()
			},
			applySearch() {
				this.loadProducts()
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
			navTo(url) {
				if (['/pages/home/home', '/pages/browse/browse', '/pages/cart/cart', '/pages/message/message', '/pages/user/index'].includes(url)) {
					uni.switchTab({ url })
					return
				}
				uni.reLaunch({ url })
			},
			visualClass(item) {
				if (item.category === '数码影音') return 'digital'
				if (item.category === '图书文创') return 'book'
				return 'life'
			},
			openDetail(item) {
				uni.navigateTo({ url: buildGoodsDetailUrl(item) })
			},
			async favoriteItem(item) {
				try {
					await addBuyerItem('favorite', { itemId: item.id, title: item.title, storeName: item.shopName })
					uni.showToast({ title: '已收藏', icon: 'success' })
				} catch (e) {
					uni.showToast({ title: '请先登录买家账号', icon: 'none' })
				}
			},
			openStore(item) {
				uni.navigateTo({ url: '/pages/store/store?name=' + encodeURIComponent(item.shopName) })
			}
		}
	}
</script>

<style lang="scss" scoped>
	.home-page {
		padding-bottom: 36px;
	}
	.topbar {
		position: sticky;
		top: 0;
		z-index: 10;
		background: rgba(255, 255, 255, .88);
		backdrop-filter: blur(22px);
		border-bottom: 1px solid rgba(203, 213, 225, .55);
		box-shadow: 0 10px 40px rgba(60, 64, 67, .06);
	}
	.topbar-inner {
		display: grid;
		grid-template-columns: 360px 320px minmax(0, 1fr);
		align-items: center;
		gap: 18px;
		height: 96px;
		padding: 0 22px;
	}
	.brand {
		display: flex;
		align-items: center;
		gap: 12px;
		flex-shrink: 0;
	}
	.brand-mark {
		width: 64px;
		height: 64px;
		border-radius: 8px;
		display: flex;
		align-items: center;
		justify-content: center;
		overflow: hidden;
		flex-shrink: 0;
	}
	.brand-logo {
		width: 100%;
		height: 100%;
	}
	.brand-copy,
	.hero-title,
	.hero-desc,
	.eyebrow,
	.board-head,
	.metric-value,
	.metric-label,
	.focus-label,
	.focus-title,
	.focus-price,
	.quick-title,
	.quick-desc,
	.section-title,
	.section-desc,
	.goods-title,
	.goods-sub,
	.side-title,
	.guard-title,
	.guard-desc,
	.story-title,
	.node-date,
	.node-text {
		display: block;
	}
	.brand-name {
		display: block;
		font-size: 28px;
		line-height: 1.1;
		font-weight: 900;
		color: #202124;
	}
	.toolbar-group {
		display: flex;
		align-items: center;
		justify-content: flex-end;
		gap: 12px;
		flex-wrap: wrap;
	}
	.brand-sub {
		display: block;
		margin-top: 8px;
		font-size: 14px;
		line-height: 1.25;
		color: #667085;
	}
	.web-nav {
		justify-self: center;
		display: flex;
		align-items: center;
		gap: 4px;
		padding: 5px;
		box-sizing: border-box;
		height: 50px;
		border-radius: 999px;
		background: rgba(255,255,255,.72);
		border: 1px solid rgba(203, 213, 225, .72);
		box-shadow: 0 14px 38px rgba(60, 64, 67, .08);
		flex-shrink: 0;
	}
	.sort-switch {
		display: flex;
		align-items: center;
		gap: 6px;
		padding: 5px;
		border-radius: 999px;
		background: #e8f0eb;
		border: 1px solid #dfe6e2;
	}
	.sort-item {
		display: flex;
		align-items: center;
		justify-content: center;
		width: 86px;
		height: 38px;
		padding: 0;
		border-radius: 999px;
		font-size: 13px;
		font-weight: 850;
		color: #667085;
		white-space: nowrap;
	}
	.sort-item.on {
		background: #fff;
		color: #12372a;
		box-shadow: 0 6px 18px rgba(17, 38, 28, 0.08);
	}
	.sync-tip {
		margin-top: 14px;
		padding: 12px 16px;
		border-radius: 8px;
		background: #fff8ed;
		border: 1px solid #ffe0b8;
		color: #9a5b15;
		font-size: 13px;
		font-weight: 750;
	}
	.nav-link {
		width: 82px;
		height: 38px;
		padding: 0;
		border-radius: 999px;
		display: flex;
		align-items: center;
		justify-content: center;
		box-sizing: border-box;
		line-height: 1;
		text-align: center;
		font-size: 13px;
		font-weight: 750;
		color: #5f6b85;
		transition: all .22s ease;
	}
	.nav-link.on,
	.nav-link:hover {
		background: linear-gradient(135deg, #ffffff, #f5f7fa);
		color: #12372a;
		box-shadow: 0 10px 26px rgba(18, 55, 42, .14);
	}
	.top-actions {
		justify-self: end;
		display: flex;
		align-items: center;
		gap: 12px;
		min-width: 0;
	}
	.search {
		width: clamp(220px, 20vw, 300px);
		height: 44px;
		border-radius: 12px;
		background: rgba(255,255,255,.82);
		border: 1px solid rgba(203, 213, 225, .78);
		display: flex;
		align-items: center;
		padding: 0 8px 0 14px;
		min-width: 0;
		box-shadow: 0 12px 30px rgba(60, 64, 67, .06);
		transition: box-shadow .22s ease, border-color .22s ease;
	}
	.search:focus-within {
		border-color: rgba(66, 133, 244, .32);
		box-shadow: 0 16px 38px rgba(60, 64, 67, .1);
	}
	.search-icon {
		font-size: 22px;
		color: #667085;
		margin-right: 8px;
	}
	.search-input {
		flex: 1;
		min-width: 0;
		font-size: 14px;
	}
	.search-action,
	.publish-btn,
	.primary-btn,
	.secondary-btn,
	.story-link {
		display: flex;
		align-items: center;
		justify-content: center;
		border-radius: 8px;
		font-size: 14px;
		font-weight: 900;
	}
	.search-action {
		width: 70px;
		height: 34px;
		background: #12372a;
		color: #fff;
	}
	.publish-btn {
		height: 44px;
		padding: 0 18px;
		background: #12372a;
		color: #fff;
		flex-shrink: 0;
		box-shadow: 0 14px 34px rgba(18, 55, 42, .16);
	}
	.scroll-shell {
		height: calc(100vh - 74px);
	}
	.main {
		padding: 24px 22px;
		animation: softIn .45s ease both;
	}
	.hero {
		display: grid;
		grid-template-columns: minmax(0, 1.45fr) minmax(360px, .85fr);
		gap: 20px;
	}
	.hero-copy,
	.hero-board,
	.quick-card,
	.goods-card,
	.side-card {
		background: #fff;
		border: 1px solid #e4e9e5;
		border-radius: 8px;
		box-shadow: 0 16px 48px rgba(17, 38, 28, 0.065);
		transition: transform .22s ease, box-shadow .22s ease, border-color .22s ease;
	}
	.hero-copy {
		padding: 48px 50px;
		background:
			radial-gradient(circle at 85% 12%, rgba(66, 133, 244, .08), transparent 30%),
			radial-gradient(circle at 18% 80%, rgba(52, 168, 83, .05), transparent 32%),
			linear-gradient(135deg, #ffffff 0%, #f8faff 100%);
	}
	.eyebrow {
		font-size: 13px;
		color: #1f5c43;
		font-weight: 900;
	}
	.hero-title {
		margin-top: 16px;
		max-width: 760px;
		font-size: 36px;
		line-height: 1.22;
		font-weight: 850;
		color: #202124;
	}
	.hero-desc {
		margin-top: 18px;
		max-width: 720px;
		font-size: 15px;
		line-height: 1.8;
		color: #4b5563;
	}
	.hero-actions {
		display: flex;
		gap: 12px;
		margin-top: 28px;
	}
	.primary-btn,
	.secondary-btn {
		height: 44px;
		padding: 0 24px;
	}
	.primary-btn {
		background: #12372a;
		color: #fff;
		box-shadow: 0 16px 34px rgba(60, 64, 67, .14);
	}
	.secondary-btn {
		background: #fff;
		color: #12372a;
		border: 1px solid rgba(95, 99, 104, .22);
	}
	.hero-board {
		padding: 24px;
		display: flex;
		flex-direction: column;
		justify-content: space-between;
		min-height: 270px;
	}
	.board-head,
	.price-row,
	.goods-foot {
		display: flex;
		align-items: center;
		justify-content: space-between;
	}
	.board-head {
		font-size: 18px;
		font-weight: 900;
		color: #17231d;
	}
	.board-pill {
		padding: 5px 9px;
		border-radius: 999px;
		background: #fff0e7;
		color: #b95420;
		font-size: 12px;
	}
	.metric-grid {
		display: grid;
		grid-template-columns: repeat(3, 1fr);
		gap: 10px;
		margin-top: 18px;
	}
	.metric {
		background: linear-gradient(180deg, #f7faf8, #eef5f0);
		border-radius: 8px;
		padding: 14px 8px;
		text-align: center;
	}
	.metric-value {
		font-size: 22px;
		font-weight: 900;
		color: #1f5c43;
	}
	.metric-label {
		margin-top: 4px;
		font-size: 12px;
		color: #667085;
	}
	.focus-item {
		margin-top: 18px;
		padding: 18px;
		border-radius: 8px;
		background:
			radial-gradient(circle at 16% 20%, rgba(255,255,255,.16), transparent 26%),
			linear-gradient(135deg, #12372a 0%, #1f5c43 100%);
		color: #fff;
		display: flex;
		gap: 14px;
		align-items: center;
		box-shadow: inset 0 1px 0 rgba(255,255,255,.08);
		transition: transform .22s ease;
	}
	.focus-item:hover {
		transform: translateY(-2px);
	}
	.focus-cover {
		width: 76px;
		height: 76px;
		border-radius: 8px;
		background:
			radial-gradient(circle at 34% 28%, rgba(255,255,255,.34), transparent 24%),
			linear-gradient(135deg, rgba(255,255,255,.16), rgba(255,255,255,.06));
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 0;
		flex-shrink: 0;
	}
	.focus-cover::before {
		content: "";
		width: 42px;
		height: 34px;
		border-radius: 18px;
		border: 5px solid rgba(255,255,255,.72);
		border-bottom-width: 8px;
		box-shadow: inset 0 -8px 0 rgba(255,255,255,.18), 0 10px 26px rgba(0,0,0,.18);
	}
	.focus-cover.has-image,
	.cover.has-image {
		background: #eef7f1;
		overflow: hidden;
	}
	.focus-cover.has-image::before,
	.cover.has-image::before,
	.cover.has-image::after {
		display: none;
	}
	.cover-img {
		width: 100%;
		height: 100%;
		display: block;
	}
	.focus-label {
		font-size: 12px;
		opacity: 0.72;
	}
	.focus-title {
		margin-top: 6px;
		font-size: 17px;
		font-weight: 900;
	}
	.focus-price {
		margin-top: 8px;
		font-size: 20px;
		font-weight: 900;
		color: #f3c98b;
	}
	.quick-grid {
		display: grid;
		grid-template-columns: repeat(4, 1fr);
		gap: 12px;
		margin-top: 16px;
	}
	.quick-card {
		padding: 18px;
		animation: softIn .5s ease both;
	}
	.quick-card:nth-child(2) {
		animation-delay: .04s;
	}
	.quick-card:nth-child(3) {
		animation-delay: .08s;
	}
	.quick-card:nth-child(4) {
		animation-delay: .12s;
	}
	.quick-card:hover,
	.goods-card:hover,
	.side-card:hover {
		transform: translateY(-3px);
		box-shadow: 0 24px 70px rgba(17, 38, 28, 0.11);
		border-color: rgba(31, 92, 67, .18);
	}
	.quick-icon {
		width: 34px;
		height: 34px;
		border-radius: 8px;
		background: linear-gradient(135deg, #f5f7fa, #ffffff);
		color: #12372a;
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 15px;
		font-weight: 900;
	}
	.quick-title {
		margin-top: 13px;
		font-size: 16px;
		font-weight: 850;
		color: #17231d;
	}
	.quick-desc {
		margin-top: 5px;
		font-size: 13px;
		color: #667085;
	}
	.section-row {
		display: flex;
		align-items: center;
		justify-content: space-between;
		gap: 16px;
		margin-top: 30px;
	}
	.section-title {
		font-size: 25px;
		font-weight: 850;
		color: #17231d;
	}
	.section-desc {
		margin-top: 6px;
		font-size: 14px;
		color: #667085;
	}
	.mode-switch {
		display: flex;
		background: #e8f0eb;
		border-radius: 999px;
		padding: 5px;
		border: 1px solid #dfe6e2;
	}
	.mode-item {
		display: flex;
		align-items: center;
		justify-content: center;
		width: 86px;
		height: 38px;
		text-align: center;
		padding: 0;
		border-radius: 999px;
		font-size: 13px;
		color: #667085;
	}
	.mode-item.on {
		background: #fff;
		color: #12372a;
		font-weight: 900;
		box-shadow: 0 6px 18px rgba(17, 38, 28, 0.08);
	}
	.category-line {
		white-space: nowrap;
		margin-top: 16px;
	}
	.category-chip {
		display: inline-flex;
		padding: 9px 15px;
		margin-right: 10px;
		border-radius: 999px;
		background: #fff;
		color: #667085;
		font-size: 13px;
		border: 1px solid #e4e9e5;
	}
	.category-chip.on {
		background: #12372a;
		color: #fff;
		border-color: transparent;
	}
	.product-layout {
		display: grid;
		grid-template-columns: minmax(0, 1fr) 310px;
		gap: 18px;
		margin-top: 18px;
		align-items: start;
	}
	.goods-grid {
		display: grid;
		grid-template-columns: repeat(3, minmax(0, 1fr));
		gap: 14px;
		align-items: start;
	}
	.goods-card {
		overflow: hidden;
		align-self: start;
	}
	.cover {
		height: 150px;
		background:
			radial-gradient(circle at 50% 42%, rgba(255,255,255,.88), transparent 20%),
			linear-gradient(135deg, #edf5f0 0%, #e4eee8 100%);
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 0;
		position: relative;
	}
	.cover::before {
		content: "";
		width: 76px;
		height: 58px;
		border-radius: 14px;
		background: linear-gradient(135deg, #ffffff, #dfe8e3);
		box-shadow: 0 18px 40px rgba(31, 92, 67, .14);
	}
	.cover::after {
		content: "";
		position: absolute;
		width: 42px;
		height: 6px;
		border-radius: 999px;
		background: rgba(18, 55, 42, .18);
		bottom: 42px;
	}
	.cover.digital::before {
		width: 86px;
		height: 54px;
		border-radius: 10px;
		background: linear-gradient(135deg, #dff2ff, #b9d9ea);
		border: 5px solid #3c5260;
	}
	.cover.book::before {
		width: 66px;
		height: 76px;
		border-radius: 8px 14px 14px 8px;
		background: linear-gradient(90deg, #d9eadf 0 34%, #f7d278 34% 68%, #dbeafe 68%);
		box-shadow: 12px 12px 0 rgba(18, 55, 42, .08), 0 18px 40px rgba(31, 92, 67, .12);
	}
	.cover.life::before {
		width: 64px;
		height: 78px;
		border-radius: 18px 18px 10px 10px;
		background: linear-gradient(135deg, #f6e6ce, #b98f72);
		clip-path: polygon(18% 0, 82% 0, 72% 42%, 86% 100%, 68% 100%, 55% 54%, 45% 54%, 32% 100%, 14% 100%, 28% 42%);
	}
	.cover.digital {
		background:
			radial-gradient(circle at 50% 42%, rgba(255,255,255,.9), transparent 20%),
			linear-gradient(135deg, #edf5f0 0%, #eaf1ff 100%);
	}
	.cover.book {
		background:
			radial-gradient(circle at 50% 42%, rgba(255,255,255,.9), transparent 20%),
			linear-gradient(135deg, #fff6ec 0%, #edf3ef 100%);
	}
	.cover.life {
		background:
			radial-gradient(circle at 50% 42%, rgba(255,255,255,.9), transparent 20%),
			linear-gradient(135deg, #f3f7f4 0%, #f7efe6 100%);
	}
	.goods-body {
		padding: 16px;
	}
	.goods-tags {
		display: flex;
		gap: 8px;
		flex-wrap: wrap;
	}
	.scene-tag,
	.light-tag {
		font-size: 12px;
		padding: 5px 9px;
		border-radius: 999px;
	}
	.scene-tag.new {
		background: #f5f7fa;
		color: #12372a;
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
		margin-top: 12px;
		font-size: 16px;
		font-weight: 850;
		color: #17231d;
		line-height: 1.35;
	}
	.goods-sub {
		margin-top: 7px;
		font-size: 13px;
		color: #667085;
		line-height: 1.5;
	}
	.price-row {
		justify-content: flex-start;
		gap: 10px;
		margin-top: 14px;
	}
	.price {
		font-size: 20px;
		font-weight: 900;
		color: #d66a2c;
	}
	.origin {
		font-size: 13px;
		color: #9ca3af;
		text-decoration: line-through;
	}
	.goods-foot {
		margin-top: 12px;
		font-size: 12px;
		color: #667085;
	}
	.goods-actions {
		display: flex;
		gap: 10px;
		margin-top: 12px;
	}
	.mini-link {
		padding: 7px 12px;
		border-radius: 999px;
		background: #e8f3ed;
		color: #12372a;
		font-size: 12px;
		font-weight: 900;
	}
	.side {
		display: flex;
		flex-direction: column;
		gap: 14px;
	}
	.side-card {
		padding: 20px;
	}
	.side-title {
		font-size: 18px;
		font-weight: 900;
		color: #17231d;
		margin-bottom: 10px;
	}
	.guard {
		display: flex;
		gap: 12px;
		padding: 9px 0;
		border-top: 1px solid #eef1ee;
	}
	.guard-icon {
		width: 30px;
		height: 30px;
		border-radius: 8px;
		background: #f5f7fa;
		color: #12372a;
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 13px;
		font-weight: 900;
		flex-shrink: 0;
	}
	.guard-title {
		font-size: 14px;
		font-weight: 900;
		color: #17231d;
	}
	.guard-desc {
		margin-top: 4px;
		font-size: 12px;
		color: #667085;
		line-height: 1.55;
	}
	.story-title {
		font-size: 15px;
		font-weight: 900;
		color: #12372a;
		margin-bottom: 8px;
	}
	.timeline-side-card .wandering-timeline {
		margin-top: 6px;
	}
	.story-link {
		margin-top: 10px;
		height: 38px;
		background: #f5f7fa;
		color: #202124;
	}
	.recommend-card {
		padding-bottom: 14px;
	}
	.recommend-item {
		display: grid;
		grid-template-columns: 62px minmax(0, 1fr);
		gap: 10px;
		padding: 10px 0;
		border-top: 1px solid #eef1ee;
		cursor: pointer;
	}
	.recommend-item:first-of-type {
		margin-top: 4px;
	}
	.recommend-cover {
		width: 62px;
		height: 62px;
		border-radius: 8px;
		background: linear-gradient(135deg, #edf5f0 0%, #eaf1ff 100%);
		overflow: hidden;
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 12px;
		color: #667085;
	}
	.recommend-title,
	.recommend-price {
		display: block;
	}
	.recommend-title {
		font-size: 13px;
		font-weight: 850;
		color: #17231d;
		line-height: 1.35;
	}
	.recommend-price {
		margin-top: 6px;
		font-size: 14px;
		font-weight: 900;
		color: #d66a2c;
	}
	.side-spacer {
		width: 100%;
		border-radius: 8px;
		background: transparent;
	}
	@media screen and (max-width: 960px) {
		.topbar-inner,
		.hero,
		.product-layout,
		.quick-grid,
		.goods-grid {
			display: flex;
			flex-direction: column;
		}
		.brand-sub,
		.web-nav,
		.publish-btn,
		.side {
			display: none;
		}
		.hero-copy {
			padding: 32px 24px;
		}
		.hero-title {
			font-size: 34px;
		}
		.section-row {
			align-items: flex-start;
			flex-direction: column;
		}
		.quick-card,
		.goods-card {
			width: 100%;
			box-sizing: border-box;
		}
	}

	/* #ifdef MP-WEIXIN */
	.topbar {
		position: relative;
		padding-top: 72px;
		z-index: 20;
	}
	.topbar-inner {
		height: auto;
		padding: 10px 18px 12px;
		gap: 10px;
	}
	.brand {
		width: 100%;
		justify-content: center;
	}
	.brand-mark {
		width: 48px;
		height: 48px;
	}
	.brand-name {
		font-size: 24px;
		line-height: 1.15;
	}
	.top-actions {
		width: 100%;
		justify-content: center;
	}
	.search {
		width: calc(100vw - 68px);
		height: 44px;
		padding: 0 7px 0 12px;
		box-sizing: border-box;
	}
	.search-icon {
		font-size: 18px;
		margin-right: 5px;
	}
	.search-input {
		height: 32px;
		font-size: 13px;
		line-height: 20px;
	}
	.search-action {
		width: 64px;
		height: 36px;
		font-size: 13px;
		flex-shrink: 0;
	}
	.scroll-shell {
		height: auto;
		min-height: calc(100vh - 178px);
	}
	.main {
		padding: 20px 18px 112px;
	}
	.hero-copy {
		padding: 28px 24px;
	}
	.hero-title {
		font-size: 31px;
		line-height: 1.26;
	}
	.toolbar-group {
		width: 100%;
		align-items: stretch;
		justify-content: flex-start;
	}
	.mode-switch,
	.sort-switch {
		width: 100%;
		box-sizing: border-box;
		display: grid;
		grid-template-columns: repeat(3, minmax(0, 1fr));
		gap: 6px;
	}
	.mode-item,
	.sort-item {
		width: auto;
		min-width: 0;
	}
	.product-layout,
	.goods-grid {
		width: 100%;
	}
	.goods-grid {
		align-items: center;
		gap: 18px;
	}
	.goods-card {
		width: 100%;
		max-width: 390px;
		margin: 0 auto;
	}
	/* #endif */
</style>
