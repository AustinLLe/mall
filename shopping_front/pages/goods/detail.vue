<template>
	<view class="safe-page detail-page">
		<scroll-view scroll-y class="scroll">
			<view class="content-wrap main">
				<view class="detail-layout">
					<view class="gallery">
						<view class="cover" :class="{ 'has-image': isImageCover(detail.cover) }">
							<image v-if="isImageCover(detail.cover)" class="cover-img" :src="detail.cover" mode="aspectFill"></image>
							<text v-else>{{ detail.cover }}</text>
						</view>
						<view class="gallery-foot">
							<text>{{ detail.scene === 'new' ? '新品正品' : '二手闲置' }}</text>
							<text>{{ detail.location }}</text>
						</view>
					</view>

					<view class="summary">
						<view class="tag-row">
							<text class="scene-tag" :class="detail.scene">{{ detail.scene === 'new' ? '新品' : '二手' }}</text>
							<text class="soft-tag">{{ detail.condition }}</text>
							<text class="soft-tag">信用 {{ detail.credit }}</text>
						</view>
						<view class="price-row">
							<text class="price">¥{{ detail.price }}</text>
							<text class="origin">¥{{ detail.originPrice }}</text>
							<text class="save">省 ¥{{ detail.originPrice - detail.price }}</text>
						</view>
						<text class="title">{{ detail.title }}</text>
						<text class="subtitle">{{ detail.subtitle }}</text>
						<view class="service-list">
							<text v-for="item in detail.service" :key="item" class="service">{{ item }}</text>
						</view>
						<view class="seller-card" @click="openStore">
							<view>
								<text class="seller-name">{{ detail.shopName }}</text>
								<text class="seller-desc">{{ detail.scene === 'new' ? '官方/严选店铺' : '个人信用卖家' }} · {{ detail.location }}</text>
							</view>
							<text class="seller-score">{{ detail.credit }} 分</text>
						</view>
						<view class="ai-box">
							<view>
								<text class="ai-title">AI 交易助手</text>
								<text class="ai-desc">{{ detail.aiTips.join(' · ') }}</text>
							</view>
							<view class="ai-btn" @click="goMessage">去问问</view>
						</view>
					</view>
				</view>

				<view class="info-grid">
					<view class="card">
						<text class="card-title">商品亮点</text>
						<view class="highlight-list">
							<text v-for="item in detail.highlights" :key="item" class="highlight">✓ {{ item }}</text>
						</view>
					</view>
					<view class="card">
						<text class="card-title">配送与保障</text>
						<text class="plain">{{ detail.delivery }}</text>
						<text class="plain">资金流：买家付款 → 平台担保 → 确认收货 → 卖家收款。</text>
					</view>
				</view>

				<view class="content-layout">
					<view class="left">
						<view class="card">
							<text class="card-title">{{ detail.scene === 'used' ? '物品故事' : '商品说明' }}</text>
							<text class="story">{{ detail.story }}</text>
						</view>

						<view v-if="detail.timeline && detail.timeline.length" class="card">
							<text class="card-title">二手物品流浪时间线</text>
							<view v-for="node in detail.timeline" :key="node.date" class="timeline-node">
								<text class="dot"></text>
								<view>
									<text class="node-date">{{ node.date }}</text>
									<text class="node-title">{{ node.title }}</text>
									<text class="node-text">{{ node.text }}</text>
								</view>
							</view>
						</view>

						<view class="card">
							<text class="card-title">参数信息</text>
							<view class="params">
								<view v-for="row in normalizedParams" :key="row.key" class="param">
									<text class="param-key">{{ row.key }}</text>
									<text class="param-value">{{ row.value }}</text>
								</view>
							</view>
						</view>

						<view class="card">
							<view class="review-head">
								<text class="card-title">评价与信用</text>
								<text class="review-count">{{ detail.reviews.length }} 条评价</text>
							</view>
							<view v-for="review in detail.reviews" :key="review.user" class="review">
								<view class="review-top">
									<text class="review-user">{{ review.user }}</text>
									<text class="review-score">{{ review.score }} 分</text>
								</view>
								<text class="review-text">{{ review.text }}</text>
								<view class="review-tags">
									<text v-for="tag in review.tags" :key="tag" class="review-tag">{{ tag }}</text>
								</view>
							</view>
						</view>
					</view>

					<view class="right">
						<view class="card sticky-card">
							<text class="card-title">猜你喜欢</text>
							<view v-for="item in recommends" :key="item.id" class="recommend" @click="openRecommend(item)">
								<view class="recommend-cover" :class="{ 'has-image': isImageCover(item.cover) }">
									<image v-if="isImageCover(item.cover)" class="cover-img" :src="item.cover" mode="aspectFill"></image>
									<text v-else>{{ item.cover }}</text>
								</view>
								<view>
									<text class="recommend-title">{{ item.title }}</text>
									<text class="recommend-price">¥{{ item.price }}</text>
								</view>
							</view>
						</view>
					</view>
				</view>
			</view>
		</scroll-view>

		<view class="bottom-bar">
			<view class="bottom-bar-inner">
				<view class="mini-action" @click="goHome">首页</view>
				<view class="mini-action" @click="goCart">购物车 {{ cartCount }}</view>
				<view class="cta ghost" @click="favoriteCurrent">收藏</view>
				<view class="cta ghost" @click="addToCart">加入购物车</view>
				<view class="cta buy" @click="buyNow">{{ detail.scene === 'used' ? '担保下单' : '立即购买' }}</view>
			</view>
		</view>
	</view>
</template>

<script>
	import { goodsCatalog, findGoodsById, buildGoodsDetailUrl } from '../../data/catalog.js'
	import { addCartItem, getCartCount } from '../../utils/cart.js'
	import { addBuyerItem } from '@/services/center.js'
	import { fetchProduct } from '@/services/shop.js'

	export default {
		data() {
			return {
				detail: goodsCatalog[0],
				cartCount: 0
			}
		},
		computed: {
			normalizedParams() {
				return (this.detail.params || []).map((row) => {
					if (Array.isArray(row)) return { key: row[0], value: row[1] }
					return { key: row.key, value: row.value }
				}).filter((row) => row.key)
			},
			recommends() {
				const picked = []
				const append = (items) => {
					items.forEach((item) => {
						if (item.id !== this.detail.id && !picked.some((current) => current.id === item.id)) picked.push(item)
					})
				}
				append(goodsCatalog.filter((item) => item.category === this.detail.category))
				append(goodsCatalog)
				return picked.slice(0, 4)
			}
		},
		onLoad(q) {
			const id = q && q.id ? decodeURIComponent(q.id) : ''
			this.detail = findGoodsById(id) || goodsCatalog[0]
			this.loadDetail(id)
			this.refreshCartCount()
		},
		onShow() {
			this.refreshCartCount()
		},
		methods: {
			isImageCover(cover) {
				return typeof cover === 'string' && (cover.startsWith('/static/') || cover.startsWith('http'))
			},
			async loadDetail(id) {
				if (!id) {
					this.recordBrowse()
					return
				}
				try {
					const body = await fetchProduct(id)
					if (body && body.code === 0 && body.data && body.data.id === id) {
						this.detail = body.data
					}
				} catch (e) {}
				this.recordBrowse()
			},
			refreshCartCount() {
				this.cartCount = getCartCount()
			},
			async recordBrowse() {
				try {
					await addBuyerItem('history', { itemId: this.detail.id, title: this.detail.title, storeName: this.detail.shopName })
				} catch (e) {}
			},
			async favoriteCurrent() {
				try {
					await addBuyerItem('favorite', { itemId: this.detail.id, title: this.detail.title, storeName: this.detail.shopName })
					uni.showToast({ title: '已收藏', icon: 'success' })
				} catch (e) {
					uni.showToast({ title: '请先登录买家账号', icon: 'none' })
				}
			},
			addToCart() {
				addCartItem({
					id: this.detail.id,
					title: this.detail.title,
					price: this.detail.price,
					cover: this.detail.cover,
					tag: this.detail.tag,
					credit: this.detail.credit,
					shopName: this.detail.shopName,
					scene: this.detail.scene,
					qty: 1
				})
				this.refreshCartCount()
				uni.showToast({ title: '已加入购物车', icon: 'success' })
			},
			buyNow() {
				this.addToCart()
				uni.navigateTo({ url: '/pages/order/confirm' })
			},
			goHome() {
				uni.switchTab({ url: '/pages/home/home' })
			},
			goCart() {
				uni.switchTab({ url: '/pages/cart/cart' })
			},
			goMessage() {
				uni.navigateTo({ url: '/pages/message/message?goods=' + encodeURIComponent(this.detail.title) })
			},
			openStore() {
				uni.navigateTo({ url: '/pages/store/store?name=' + encodeURIComponent(this.detail.shopName) })
			},
			openRecommend(item) {
				uni.navigateTo({ url: buildGoodsDetailUrl(item) })
			}
		}
	}
</script>

<style lang="scss" scoped>
	.detail-page {
		padding-bottom: 128rpx;
		background: linear-gradient(180deg, #f3f8f5 0%, #f6f7f8 260rpx, #f6f7f8 100%);
	}
	.scroll {
		height: calc(100vh - 120rpx);
	}
	.main {
		padding: 28rpx;
	}
	.detail-layout {
		display: grid;
		grid-template-columns: minmax(320px, 0.9fr) minmax(0, 1.1fr);
		gap: 24rpx;
	}
	.gallery,
	.summary,
	.card {
		background: #fff;
		border: 1rpx solid #e4e9e5;
		border-radius: 26rpx;
		box-shadow: 0 14rpx 36rpx rgba(15, 35, 26, 0.06);
	}
	.summary {
		background: linear-gradient(180deg, #ffffff 0%, #f5fbf7 100%);
	}
	.gallery {
		padding: 28rpx;
	}
	.cover {
		height: 520rpx;
		border-radius: 24rpx;
		background: radial-gradient(circle at 50% 42%, #ffffff 0%, #eef7f1 52%, #dcebe2 100%);
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 170rpx;
		overflow: hidden;
	}
	.cover.has-image {
		background: #eef7f1;
	}
	.cover-img {
		width: 100%;
		height: 100%;
		display: block;
	}
	.gallery-foot,
	.tag-row,
	.price-row,
	.seller-card,
	.ai-box,
	.review-head,
	.review-top {
		display: flex;
		align-items: center;
		justify-content: space-between;
		gap: 12rpx;
	}
	.gallery-foot {
		margin-top: 18rpx;
		font-size: 24rpx;
		color: #667085;
	}
	.summary {
		padding: 34rpx;
	}
	.tag-row {
		justify-content: flex-start;
		flex-wrap: wrap;
	}
	.scene-tag,
	.soft-tag,
	.service,
	.save,
	.review-tag {
		font-size: 22rpx;
		padding: 7rpx 14rpx;
		border-radius: 999rpx;
	}
	.scene-tag.new {
		background: #e8f3ed;
		color: #1f5c43;
	}
	.scene-tag.used,
	.save {
		background: #fff0e7;
		color: #b95420;
	}
	.soft-tag,
	.review-tag {
		background: #f4f6f4;
		color: #667085;
	}
	.price-row {
		justify-content: flex-start;
		margin-top: 24rpx;
	}
	.price {
		font-size: 54rpx;
		font-weight: 900;
		color: #d66a2c;
	}
	.origin {
		font-size: 26rpx;
		color: #9ca3af;
		text-decoration: line-through;
	}
	.title {
		display: block;
		margin-top: 18rpx;
		font-size: 42rpx;
		line-height: 1.25;
		font-weight: 900;
		color: #17231d;
	}
	.subtitle {
		display: block;
		margin-top: 12rpx;
		font-size: 26rpx;
		color: #667085;
		line-height: 1.6;
	}
	.service-list {
		display: flex;
		flex-wrap: wrap;
		gap: 12rpx;
		margin-top: 22rpx;
	}
	.service {
		background: #e8f3ed;
		color: #1f5c43;
	}
	.seller-card,
	.ai-box {
		margin-top: 24rpx;
		padding: 22rpx;
		border-radius: 20rpx;
		background: #eef7f1;
		border: 1rpx solid #dcebe2;
	}
	.seller-name,
	.seller-desc,
	.seller-score,
	.ai-title,
	.ai-desc {
		display: block;
	}
	.seller-name,
	.ai-title {
		font-size: 28rpx;
		font-weight: 900;
		color: #17231d;
	}
	.seller-desc,
	.ai-desc {
		margin-top: 6rpx;
		font-size: 23rpx;
		color: #667085;
		line-height: 1.5;
	}
	.seller-score {
		font-size: 26rpx;
		font-weight: 900;
		color: #1f5c43;
	}
	.ai-box {
		background: #17231d;
		color: #fff;
	}
	.ai-box .ai-title,
	.ai-box .ai-desc {
		color: #fff;
	}
	.ai-btn {
		padding: 14rpx 20rpx;
		border-radius: 14rpx;
		background: #fff;
		color: #1f5c43;
		font-size: 24rpx;
		font-weight: 900;
		flex-shrink: 0;
	}
	.info-grid,
	.content-layout {
		display: grid;
		gap: 24rpx;
		margin-top: 24rpx;
	}
	.info-grid {
		grid-template-columns: repeat(2, minmax(0, 1fr));
	}
	.content-layout {
		grid-template-columns: minmax(0, 1fr) 320px;
	}
	.card {
		padding: 28rpx;
	}
	.card-title {
		display: block;
		padding-left: 16rpx;
		border-left: 8rpx solid #2f6f50;
		font-size: 32rpx;
		font-weight: 900;
		color: #17231d;
		margin-bottom: 18rpx;
	}
	.highlight-list {
		display: flex;
		flex-wrap: wrap;
		gap: 12rpx;
	}
	.highlight,
	.plain,
	.story {
		display: block;
		font-size: 25rpx;
		color: #4b5563;
		line-height: 1.7;
	}
	.highlight {
		background: #f4f6f4;
		border-radius: 14rpx;
		padding: 12rpx 16rpx;
	}
	.timeline-node {
		display: flex;
		gap: 16rpx;
		padding: 18rpx 0;
		border-top: 1rpx solid #eef1ee;
	}
	.dot {
		width: 16rpx;
		height: 16rpx;
		border-radius: 50%;
		background: #d66a2c;
		margin-top: 10rpx;
		flex-shrink: 0;
	}
	.node-date,
	.node-title,
	.node-text {
		display: block;
	}
	.node-date {
		font-size: 22rpx;
		color: #667085;
	}
	.node-title {
		margin-top: 3rpx;
		font-size: 27rpx;
		font-weight: 900;
		color: #17231d;
	}
	.node-text {
		margin-top: 6rpx;
		font-size: 24rpx;
		color: #667085;
		line-height: 1.6;
	}
	.params {
		display: grid;
		grid-template-columns: repeat(2, minmax(0, 1fr));
		gap: 14rpx;
	}
	.param {
		background: #f2faf5;
		border: 1rpx solid #e2eee6;
		border-radius: 16rpx;
		padding: 18rpx;
	}
	.param-key,
	.param-value {
		display: block;
	}
	.param-key {
		font-size: 22rpx;
		color: #667085;
	}
	.param-value {
		margin-top: 8rpx;
		font-size: 26rpx;
		font-weight: 800;
		color: #17231d;
	}
	.review {
		padding: 18rpx 0;
		border-top: 1rpx solid #eef1ee;
	}
	.review-count,
	.review-score {
		color: #1f5c43;
		font-size: 24rpx;
	}
	.review-user {
		font-size: 26rpx;
		font-weight: 800;
		color: #17231d;
	}
	.review-text {
		display: block;
		margin-top: 10rpx;
		font-size: 25rpx;
		color: #4b5563;
		line-height: 1.6;
	}
	.review-tags {
		display: flex;
		gap: 10rpx;
		margin-top: 12rpx;
	}
	.sticky-card {
		position: sticky;
		top: 24rpx;
	}
	.recommend {
		display: flex;
		gap: 14rpx;
		padding: 16rpx 0;
		border-top: 1rpx solid #eef1ee;
	}
	.recommend-cover {
		width: 92rpx;
		height: 92rpx;
		border-radius: 16rpx;
		background: #eef7f1;
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 42rpx;
		flex-shrink: 0;
		overflow: hidden;
	}
	.recommend-cover.has-image {
		background: #eef7f1;
	}
	.recommend-title,
	.recommend-price {
		display: block;
	}
	.recommend-title {
		font-size: 24rpx;
		color: #17231d;
		line-height: 1.35;
	}
	.recommend-price {
		margin-top: 8rpx;
		font-size: 27rpx;
		font-weight: 900;
		color: #d66a2c;
	}
	.bottom-bar {
		position: fixed;
		left: 0;
		right: 0;
		bottom: 0;
		z-index: 20;
		padding: 18rpx 28rpx calc(18rpx + env(safe-area-inset-bottom));
		background: rgba(247, 250, 248, 0.88);
		backdrop-filter: blur(18rpx);
		box-shadow: 0 -18rpx 36rpx rgba(15, 35, 26, 0.1);
	}
	.bottom-bar-inner {
		width: min(100%, 1680rpx);
		margin: 0 auto;
		display: grid;
		grid-template-columns: repeat(5, minmax(0, 1fr));
		align-items: center;
		gap: 10rpx;
		padding: 10rpx;
		border: 1rpx solid #dfe9e3;
		border-radius: 26rpx;
		background: #ffffff;
		box-shadow: 0 16rpx 48rpx rgba(15, 35, 26, 0.12);
	}
	.mini-action,
	.cta {
		height: 78rpx;
		border-radius: 18rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 25rpx;
		font-weight: 800;
		white-space: nowrap;
	}
	.mini-action {
		background: #f1f7f3;
		color: #1f5c43;
	}
	.cta.ghost {
		background: #e8f3ed;
		color: #1f5c43;
	}
	.cta.buy {
		background: linear-gradient(135deg, #2f6f50, #1f5c43);
		color: #fff;
		box-shadow: 0 12rpx 28rpx rgba(31, 92, 67, 0.24);
	}
	@media screen and (max-width: 900px) {
		.detail-layout,
		.info-grid,
		.content-layout,
		.params {
			grid-template-columns: 1fr;
		}
		.right {
			display: none;
		}
		.cover {
			height: 420rpx;
		}
		.bottom-bar {
			padding-left: 14rpx;
			padding-right: 14rpx;
		}
		.bottom-bar-inner {
			gap: 10rpx;
			padding: 8rpx;
		}
		.mini-action,
		.cta {
			height: 70rpx;
			font-size: 22rpx;
		}
	}
</style>
