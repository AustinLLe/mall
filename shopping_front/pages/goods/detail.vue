<template>
	<view class="page">
		<scroll-view scroll-y class="scroll-shell" :style="{ height: scrollHeight + 'px' }">
			<view class="container">
				<view class="gallery">
					<view class="gallery-badge-row">
						<text class="gallery-badge">{{ detail.scene === 'new' ? '新品馆' : '闲物集' }}</text>
						<text v-if="detail.tag" class="gallery-badge light">{{ detail.tag }}</text>
					</view>
					<view class="gallery-image">
    					<image v-if="detail.image" :src="detail.image" mode="aspectFill"></image>
					</view>
					<view class="gallery-foot">
						<text>{{ detail.location }}</text>
						<text>信用 {{ detail.credit }}</text>
					</view>
				</view>

				<view class="card price-card">
					<view class="price-row">
						<text class="price">¥{{ detail.price }}</text>
						<text class="origin">¥{{ detail.originPrice }}</text>
						<text class="discount">立省 {{ detail.originPrice - detail.price }}</text>
					</view>
					<text class="title">{{ detail.goodsName }}</text>
					<text class="subtitle">{{ detail.subtitle }}</text>
					<view class="service-list">
						<text v-for="item in detail.service" :key="item" class="service-pill">{{ item }}</text>
					</view>
				</view>

				<view class="card time-card">
					<view class="time-row">
						<text class="time-label">发布时间</text>
						<text class="time-text">{{ formatDate(detail.createTime) }}</text>
					</view>
				</view>

				<view class="card promo-card">
					<view class="promo-row">
						<text class="promo-label">活动</text>
						<text class="promo-text">新人券、学生价、同城自提红包都可在这里承接</text>
					</view>
					<view class="promo-row">
						<text class="promo-label">亮点</text>
						<text class="promo-text">{{ detail.highlights.join(' · ') }}</text>
					</view>
				</view>

				<view class="card selector-card">
					<view class="selector-row">
						<text class="selector-label">已选</text>
						<text class="selector-value">{{ detail.tag || '默认款' }} / 1 件</text>
					</view>
					<view class="selector-row">
						<text class="selector-label">配送</text>
						<text class="selector-value">{{ detail.delivery }}</text>
					</view>
				</view>

				<view class="card story-card">
					<text class="section-title">商品故事</text>
					<text class="story-text">{{ detail.goodsDesc }}</text>
				</view>

				<view class="card params-card">
					<text class="section-title">参数与说明</text>
					<view v-for="row in detail.params" :key="row[0]" class="param-row">
						<text class="param-key">{{ row[0] }}</text>
						<text class="param-value">{{ row[1] }}</text>
					</view>
				</view>

				<view class="card review-card">
					<view class="review-head">
						<text class="section-title">买家评价</text>
						<text class="review-summary">{{ detail.reviews.length }} 条精选</text>
					</view>
					<view v-for="review in detail.reviews" :key="review.user" class="review-item">
						<view class="review-top">
							<text class="review-user">{{ review.user }}</text>
							<text class="review-score">评分 {{ review.score }}</text>
						</view>
						<text class="review-text">{{ review.text }}</text>
					</view>
				</view>

				<view class="card store-card">
					<view class="store-top">
						<view class="store-info">
							<text class="section-title">{{ detail.shopName }}</text>
							<text class="store-note">{{ detail.scene === 'new' ? '官方/品牌店铺' : '闲置卖家主页' }}</text>
						</view>
						<view class="store-score">信用 {{ detail.credit }}</view>
					</view>
					<text class="store-desc">支持查看更多在售商品、历史交易评价和发货说明。</text>
				</view>

				<view class="section-head">
					<text class="section-title">猜你喜欢</text>
					<text class="review-summary">继续逛更多同类</text>
				</view>
				<scroll-view scroll-x class="recommend-line" :show-scrollbar="false">
					<view
						v-for="item in recommends"
						:key="item.id"
						class="recommend-card"
						@click="openRecommend(item)"
					>
						<view class="recommend-image">{{ item.image}}</view>
						<text class="recommend-title">{{ item.goodsName }}</text>
						<text class="recommend-price">¥{{ item.price }}</text>
					</view>
				</scroll-view>
			</view>
		</scroll-view>

		<view class="bottom-bar">
			<view class="mini-actions">
				<view class="mini-action" @click="goHome">
					<text class="mini-icon">⌂</text>
					<text class="mini-text">首页</text>
				</view>
				<view class="mini-action" @click="goCart">
					<text class="mini-icon">🛒</text>
					<text class="mini-text">购物车 {{ cartCount }}</text>
				</view>
			</view>
			<view class="cta-group">
				<button class="ghost-btn" @click="addToCart">加入购物车</button>
				<button class="buy-btn" @click="buyNow">立即购买</button>
			</view>
		</view>
	</view>
</template>

<script>
	import { goodsCatalog, findGoodsById, buildGoodsDetailUrl } from '../../data/catalog.js'
	import { addCartItem, getCartCount } from '../../utils/cart.js'

	function fallbackDetail(query = {}) {
		return {
			id: query.id || query.goodsName|| 'fallback',
			scene: 'used',
			category: '其他',
			goodsName: query.goodsName || '商品详情',
			subtitle: '这里展示商品的卖点、配送、服务和交易说明。',
			price: Number(query.price || 0),
			originPrice: Number(query.price || 0) + 80,
			image: query.image || '🛍',
			tag: query.tag || '',
			credit: query.credit || 95,
			location: '同城可见',
			shopName: '轻市卖家',
			delivery: '快递 / 面交',
			service: ['支持沟通', '支持验货', '支持加购'],
			highlights: ['页面结构已补齐', '后续可接接口', '支持多端展示'],
			goodsDesc: '当前商品来自前端演示数据，后续接后端后可替换为真实详情。',
			params: [
				['分类', '演示商品'],
				['来源', '前端传参'],
				['状态', '可购买'],
				['备注', '待接入真实接口']
			],
			reviews: [{ user: '体验用户', text: '详情页结构已经比占位版完整很多。', score: '4.8' }],
			createTime: query.createTime
		}
	}

	export default {
		data() {
			return {
				scrollHeight: 500,
				detail: fallbackDetail(),
				cartCount: 0
			}
		},
		computed: {
			recommends() {
				return goodsCatalog.filter((item) => item.id !== this.detail.id).slice(0, 4)
			}
		},
		onLoad(q) {
			const sys = uni.getWindowInfo()
			const windowHeight = sys.windowHeight || 667
			const bottomBar = 120
			this.scrollHeight = windowHeight - bottomBar
			const query = {
				id: q && q.id ? decodeURIComponent(q.id) : '',
				goodsName: q && q.goodsName ? decodeURIComponent(q.goodsName) : '',
				price: q && q.price ? decodeURIComponent(q.price) : '',
				image: q && q.image ? decodeURIComponent(q.image) : '',
				tag: q && q.tag ? decodeURIComponent(q.tag) : '',
				credit: q && q.credit ? decodeURIComponent(q.credit) : '',
				createTime: q && q.createTime ? decodeURIComponent(q.createTime) : ''
			}
			this.detail = findGoodsById(query.id) || fallbackDetail(query)
			this.refreshCartCount()
		},
		onShow() {
			this.refreshCartCount()
		},
		methods: {
			formatDate(timeStr) {
        		if (!timeStr) return '';
        		const date = new Date(timeStr);
        		if (isNaN(date.getTime())) return timeStr;
				const year = date.getFullYear();
        		const month = date.getMonth() + 1; 
        		const day = date.getDate();
        		return `${year}-${month < 10 ? '0' + month : month}-${day < 10 ? '0' + day : day}`;
    		},

			refreshCartCount() {
				this.cartCount = getCartCount()
			},
			addToCart() {
				addCartItem({
					id: this.detail.id,
					goodsName: this.detail.goodsName,
					price: this.detail.price,
					image: this.detail.image,
					tag: this.detail.tag,
					credit: this.detail.credit,
					qty: 1
				})
				this.refreshCartCount()
				uni.showToast({ title: '已加入购物车', icon: 'success' })
			},
			buyNow() {
				this.addToCart()
				uni.switchTab({ url: '/pages/cart/cart' })
			},
			goHome() {
				uni.switchTab({ url: '/pages/home/home' })
			},
			goCart() {
				uni.switchTab({ url: '/pages/cart/cart' })
			},
			openRecommend(item) {
				uni.navigateTo({ url: buildGoodsDetailUrl(item) })
			}
		}
	}
</script>

<style lang="scss" scoped>
	$page: #f5f4f1;
	$paper: #ffffff;
	$forest: #1b4332;
	$forest2: #2d6a4f;
	$muted: #6b7280;

	.page {
		min-height: 100vh;
		background: $page;
	}
	.scroll-shell {
		box-sizing: border-box;
	}
	.container {
		padding: 0 0 40rpx;
	}
	.gallery {
		padding: 26rpx 24rpx 32rpx;
		background: linear-gradient(160deg, #dfe8e0, #f7faf7);
	}
	.gallery-badge-row,
	.gallery-foot,
	.price-row,
	.review-head,
	.review-top,
	.store-top,
	.section-head {
		display: flex;
		flex-direction: row;
		align-items: center;
		justify-content: space-between;
	}
	.gallery-badge {
		font-size: 22rpx;
		padding: 8rpx 16rpx;
		border-radius: 999rpx;
		background: $forest;
		color: #fff;
	}
	.gallery-badge.light {
		background: rgba(255, 255, 255, 0.72);
		color: $forest;
	}
	.gallery-image {
		margin-top: 20rpx;
    	height: 420rpx;
    	border-radius: 30rpx;
    	background: rgba(255, 255, 255, 0.66);
    	overflow: hidden; 
    	position: relative;
    	display: flex;
    	align-items: center;
    	justify-content: center;
	}
	.gallery-image image {
    	width: 100%;
    	height: 100%;
	}
	.gallery-foot {
		margin-top: 18rpx;
		font-size: 24rpx;
		color: $muted;
	}
	.card {
		margin: 20rpx 24rpx 0;
		padding: 28rpx 26rpx;
		background: $paper;
		border-radius: 26rpx;
		box-shadow: 0 10rpx 34rpx rgba(0, 0, 0, 0.05);
	}
	.price {
		font-size: 46rpx;
		font-weight: 700;
		color: #c45c26;
	}
	.origin,
	.discount,
	.subtitle,
	.time-label,
	.time-text,
	.promo-label,
	.promo-text,
	.selector-label,
	.selector-value,
	.story-text,
	.param-key,
	.param-value,
	.review-summary,
	.review-user,
	.review-score,
	.review-text,
	.store-note,
	.store-desc,
	.recommend-title {
		font-size: 24rpx;
		color: $muted;
	}
	.origin {
		text-decoration: line-through;
	}
	.discount {
		padding: 6rpx 12rpx;
		border-radius: 999rpx;
		background: #fff1e8;
		color: #c45c26;
	}
	.title,
	.section-title {
		font-size: 34rpx;
		font-weight: 700;
		color: #222;
		line-height: 1.4;
	}
	.title {
		margin-top: 18rpx;
	}
	.subtitle {
		margin-top: 12rpx;
		line-height: 1.6;
	}
	.service-list {
		display: flex;
		flex-direction: row;
		flex-wrap: wrap;
		gap: 12rpx;
		margin-top: 18rpx;
	}
	.service-pill {
		font-size: 22rpx;
		padding: 8rpx 16rpx;
		border-radius: 999rpx;
		background: #eef6f1;
		color: $forest2;
	}
	.time-row,
	.promo-row,
	.selector-row,
	.param-row {
		display: flex;
		flex-direction: row;
		align-items: flex-start;
	}
	.promo-row + .promo-row,
	.selector-row + .selector-row,
	.param-row + .param-row {
		margin-top: 18rpx;
		padding-top: 18rpx;
		border-top: 1rpx solid #f0f0f0;
	}
	.time-label,
	.promo-label,
	.selector-label,
	.param-key {
		width: 112rpx;
		flex-shrink: 0;
	}
	.promo-text,
	.time-text,
	.selector-value,
	.param-value,
	.story-text,
	.review-text,
	.store-desc {
		flex: 1;
		line-height: 1.7;
	}
	.story-text {
		margin-top: 16rpx;
	}
	.review-item + .review-item {
		margin-top: 20rpx;
		padding-top: 20rpx;
		border-top: 1rpx solid #f0f0f0;
	}
	.review-score,
	.store-score {
		color: $forest2;
	}
	.store-info {
		display: flex;
		flex-direction: column;
	}
	.store-note,
	.store-desc {
		margin-top: 10rpx;
	}
	.section-head {
		margin: 28rpx 24rpx 0;
	}
	.recommend-line {
		white-space: nowrap;
		padding: 18rpx 12rpx 0 24rpx;
	}
	.recommend-card {
		display: inline-flex;
		flex-direction: column;
		width: 220rpx;
		margin-right: 16rpx;
		padding: 18rpx;
		border-radius: 22rpx;
		background: $paper;
		box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.04);
	}
	.recommend-image {
		height: 160rpx;
		border-radius: 18rpx;
		background: #eef2ef;
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 70rpx;
	}
	.recommend-title {
		margin-top: 12rpx;
		white-space: normal;
	}
	.recommend-price {
		margin-top: 10rpx;
		font-size: 30rpx;
		font-weight: 700;
		color: #c45c26;
	}
	.bottom-bar {
		position: fixed;
		left: 0;
		right: 0;
		bottom: 0;
		display: flex;
		flex-direction: row;
		align-items: center;
		padding: 18rpx 22rpx;
		background: rgba(255, 255, 255, 0.96);
		box-shadow: 0 -12rpx 30rpx rgba(0, 0, 0, 0.05);
	}
	.mini-actions {
		display: flex;
		flex-direction: row;
	}
	.mini-action {
		min-width: 110rpx;
		text-align: center;
	}
	.mini-icon,
	.mini-text {
		display: block;
	}
	.mini-icon {
		font-size: 28rpx;
	}
	.mini-text {
		margin-top: 6rpx;
		font-size: 20rpx;
		color: $muted;
	}
	.cta-group {
		flex: 1;
		display: flex;
		flex-direction: row;
		gap: 16rpx;
		margin-left: 16rpx;
	}
	.ghost-btn,
	.buy-btn {
		flex: 1;
		margin: 0;
		height: 88rpx;
		line-height: 88rpx;
		border-radius: 999rpx;
		font-size: 28rpx;
		border: none;
	}
	.ghost-btn {
		background: #eef2ef;
		color: $forest;
	}
	.buy-btn {
		background: $forest;
		color: #fff;
	}
</style>