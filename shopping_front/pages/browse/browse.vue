<template>
	<view class="page">
		<view class="container">
			<view class="hero">
				<text class="hero-kicker">内容社区</text>
				<text class="hero-title">话题、讨论帖和热门店铺</text>
				<text class="hero-desc">首页负责商品浏览，逛逛负责内容种草、交易经验和店铺发现</text>
			</view>

			<view class="tabs">
				<view
					v-for="tab in tabs"
					:key="tab.key"
					class="tab"
					:class="{ on: activeTab === tab.key }"
					@click="activeTab = tab.key"
				>
					{{ tab.label }}
				</view>
			</view>

			<view v-if="activeTab === 'topics'" class="stack">
				<view v-for="item in topicFeed" :key="item.id" class="card article">
					<view class="article-image">{{ item.image }}</view>
					<view class="article-main">
						<view class="article-badges">
							<text class="badge">{{ item.type }}</text>
							<text class="heat">{{ item.heat }}</text>
						</view>
						<text class="article-title">{{ item.goodsName }}</text>
						<text class="article-desc">{{ item.goodsDesc }}</text>
						<view class="tags">
							<text v-for="tag in item.tags" :key="tag" class="tag">{{ tag }}</text>
						</view>
						<text class="article-author">作者：{{ item.author }}</text>
					</view>
				</view>
			</view>

			<view v-else-if="activeTab === 'stores'" class="stack">
				<view v-for="store in hotStores" :key="store.id" class="card store">
					<view class="store-top">
						<view class="store-info">
							<text class="store-name">{{ store.name }}</text>
							<text class="store-desc">{{ store.desc }}</text>
						</view>
						<text class="store-badge">{{ store.badge }}</text>
					</view>
					<view class="store-meta">
						<text>评分 {{ store.score }}</text>
						<text>粉丝 {{ store.fans }}</text>
					</view>
					<scroll-view scroll-x class="goods-line" :show-scrollbar="false">
						<view
							v-for="item in recommendByStore(store.name)"
							:key="item.id"
							class="mini-goods"
							@click="open(item)"
						>
							<view class="mini-image">{{ item.image }}</view>
							<text class="mini-title">{{ item.goodsName }}</text>
							<text class="mini-price">¥{{ item.price }}</text>
						</view>
					</scroll-view>
				</view>
			</view>

			<view v-else class="stack">
				<view v-for="item in showcase" :key="item.id" class="card showcase" @click="open(item)">
					<view class="showcase-main">
						<text class="showcase-title">{{ item.goodsName }}</text>
						<text class="showcase-desc">{{ item.goodsDesc }}</text>
						<view class="showcase-meta">
							<text class="showcase-price">¥{{ item.price }}</text>
							<text class="showcase-tag">{{ item.tag }}</text>
						</view>
					</view>
					<view class="showcase-image">{{ item.image }}</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
	import { goodsCatalog, topicFeed, hotStores, buildGoodsDetailUrl } from '../../data/catalog.js'

	export default {
		data() {
			return {
				activeTab: 'topics',
				topicFeed,
				hotStores
			}
		},
		computed: {
			tabs() {
				return [
					{ key: 'topics', label: '热门话题' },
					{ key: 'stores', label: '热门店铺' },
					{ key: 'showcase', label: '晒单广场' }
				]
			},
			showcase() {
				return goodsCatalog.slice(0, 4)
			}
		},
		methods: {
			recommendByStore(name) {
				return goodsCatalog.filter((item) => item.shopName === name).slice(0, 3)
			},
			open(item) {
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
	.container {
		padding: 24rpx 24rpx 50rpx;
	}
	.hero {
		background: linear-gradient(135deg, #14382c, #2d6a4f);
		color: #fff;
		border-radius: 30rpx;
		padding: 34rpx 28rpx;
	}
	.hero-kicker,
	.hero-desc {
		font-size: 24rpx;
	}
	.hero-title {
		font-size: 38rpx;
		font-weight: 700;
		margin-top: 12rpx;
		line-height: 1.35;
	}
	.hero-desc {
		margin-top: 18rpx;
		line-height: 1.6;
		opacity: 0.92;
	}
	.tabs {
		display: flex;
		flex-direction: row;
		gap: 16rpx;
		margin-top: 24rpx;
	}
	.tab {
		flex: 1;
		background: #edf2ef;
		color: $muted;
		text-align: center;
		padding: 18rpx 16rpx;
		border-radius: 18rpx;
		font-size: 25rpx;
	}
	.tab.on {
		background: $forest;
		color: #fff;
	}
	.stack {
		display: flex;
		flex-direction: column;
		gap: 20rpx;
		margin-top: 24rpx;
	}
	.card {
		background: $paper;
		border-radius: 24rpx;
		padding: 24rpx;
		box-shadow: 0 8rpx 28rpx rgba(0, 0, 0, 0.05);
	}
	.article {
		display: flex;
		flex-direction: row;
		gap: 20rpx;
	}
	.article-image {
		width: 120rpx;
		height: 120rpx;
		border-radius: 24rpx;
		background: #eef2ef;
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 58rpx;
		flex-shrink: 0;
	}
	.article-main {
		flex: 1;
		min-width: 0;
	}
	.article-badges,
	.store-top,
	.store-meta,
	.showcase-meta {
		display: flex;
		flex-direction: row;
		align-items: center;
		justify-content: space-between;
	}
	.store-info {
		display: flex;
		flex-direction: column;
	}
	.badge,
	.store-badge,
	.tag,
	.showcase-tag {
		font-size: 22rpx;
		padding: 6rpx 14rpx;
		border-radius: 999rpx;
	}
	.badge,
	.showcase-tag {
		background: rgba(45, 106, 79, 0.12);
		color: $forest2;
	}
	.heat,
	.article-author,
	.store-meta text,
	.store-desc,
	.showcase-desc {
		font-size: 23rpx;
		color: $muted;
	}
	.article-title,
	.store-name,
	.showcase-title {
		font-size: 30rpx;
		font-weight: 700;
		color: #222;
		margin-top: 12rpx;
		line-height: 1.35;
	}
	.article-desc,
	.store-desc,
	.showcase-desc {
		margin-top: 12rpx;
		line-height: 1.6;
	}
	.tags {
		display: flex;
		flex-direction: row;
		flex-wrap: wrap;
		gap: 10rpx;
		margin-top: 14rpx;
	}
	.tag {
		background: #f3f4f6;
		color: #6b7280;
	}
	.article-author {
		margin-top: 14rpx;
	}
	.store-badge {
		background: #fff1e8;
		color: #c45c26;
	}
	.goods-line {
		white-space: nowrap;
		margin-top: 20rpx;
	}
	.mini-goods {
		display: inline-flex;
		flex-direction: column;
		width: 190rpx;
		margin-right: 16rpx;
	}
	.mini-image {
		height: 140rpx;
		border-radius: 18rpx;
		background: #eef2ef;
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 54rpx;
	}
	.mini-title,
	.mini-price {
		margin-top: 10rpx;
	}
	.mini-title {
		font-size: 24rpx;
		color: #222;
		line-height: 1.4;
		white-space: normal;
	}
	.mini-price,
	.showcase-price {
		font-size: 28rpx;
		font-weight: 700;
		color: #c45c26;
	}
	.showcase {
		display: flex;
		flex-direction: row;
		align-items: center;
		gap: 18rpx;
	}
	.showcase-main {
		flex: 1;
		min-width: 0;
	}
	.showcase-image {
		width: 120rpx;
		height: 120rpx;
		border-radius: 24rpx;
		background: #eef2ef;
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 56rpx;
		flex-shrink: 0;
	}
</style>