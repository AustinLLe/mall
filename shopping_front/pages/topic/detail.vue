<template>
	<view class="safe-page topic-page">
		<scroll-view scroll-y class="scroll">
			<view class="content-wrap page">
				<view class="hero">
					<view class="cover">
						<image class="cover-img" :src="topicCover" mode="aspectFill" @error="coverFailed = true"></image>
					</view>
					<view class="hero-main">
						<view class="article-line">
							<text class="badge">{{ topic.type }}</text>
							<text class="heat">{{ topic.heat }}</text>
						</view>
						<text class="title">{{ topic.title }}</text>
						<text class="desc">{{ topic.desc }}</text>
						<view class="tags">
							<text v-for="tag in topic.tags" :key="tag" class="tag">{{ tag }}</text>
						</view>
						<text class="author">{{ topic.author }}</text>
					</view>
				</view>

				<view class="layout">
					<view class="article">
						<view v-for="section in topic.sections" :key="section.title" class="section">
							<text class="section-title">{{ section.title }}</text>
							<text class="section-text">{{ section.text }}</text>
						</view>
					</view>

					<view class="side">
						<text class="side-title">相关商品</text>
						<view v-for="item in relatedGoods" :key="item.id" class="goods" @click="openGoods(item)">
							<view class="goods-cover">
								<image class="cover-img" :src="item.cover" mode="aspectFill"></image>
							</view>
							<view>
								<text class="goods-title">{{ item.title }}</text>
								<text class="goods-price">¥{{ item.price }}</text>
							</view>
						</view>
					</view>
				</view>
			</view>
		</scroll-view>
	</view>
</template>

<script>
	import { topicFeed, goodsCatalog, findTopicById, buildGoodsDetailUrl } from '../../data/catalog.js'

	export default {
		data() {
			return {
				topic: topicFeed[0],
				coverFailed: false
			}
		},
		computed: {
			topicCover() {
				return this.coverFailed ? this.topic.fallbackCover : this.topic.cover
			},
			relatedGoods() {
				const ids = this.topic.relatedGoods || []
				const picked = ids.map((id) => goodsCatalog.find((item) => item.id === id)).filter(Boolean)
				return picked.length ? picked : goodsCatalog.slice(0, 3)
			}
		},
		onLoad(query) {
			const id = query && query.id ? decodeURIComponent(query.id) : ''
			this.topic = findTopicById(id) || topicFeed[0]
			this.coverFailed = false
		},
		methods: {
			openGoods(item) {
				uni.navigateTo({ url: buildGoodsDetailUrl(item) })
			}
		}
	}
</script>

<style lang="scss" scoped>
	.topic-page {
		min-height: 100vh;
		background: linear-gradient(180deg, #f3f8f5 0%, #f6f7f8 240rpx, #f6f7f8 100%);
	}
	.scroll {
		height: 100vh;
	}
	.page {
		padding: 28rpx;
	}
	.hero,
	.section,
	.side {
		background: #fff;
		border: 1rpx solid #e4e9e5;
		border-radius: 8px;
		box-shadow: 0 14rpx 38rpx rgba(17, 38, 28, .06);
	}
	.hero {
		display: grid;
		grid-template-columns: minmax(260rpx, 420rpx) minmax(0, 1fr);
		gap: 30rpx;
		padding: 28rpx;
		align-items: center;
	}
	.cover {
		height: 300rpx;
		border-radius: 8px;
		overflow: hidden;
		background: #edf3ef;
	}
	.cover-img {
		width: 100%;
		height: 100%;
		display: block;
	}
	.article-line,
	.tags {
		display: flex;
		align-items: center;
		gap: 12rpx;
		flex-wrap: wrap;
	}
	.article-line {
		justify-content: space-between;
	}
	.badge,
	.tag {
		display: inline-flex;
		align-items: center;
		border-radius: 999rpx;
		background: #f5f7fa;
		color: #12372a;
		font-size: 22rpx;
		font-weight: 800;
		padding: 8rpx 16rpx;
	}
	.heat,
	.desc,
	.author,
	.section-text {
		color: #667085;
	}
	.title,
	.desc,
	.author,
	.section-title,
	.section-text,
	.side-title,
	.goods-title,
	.goods-price {
		display: block;
	}
	.title {
		margin-top: 18rpx;
		font-size: 42rpx;
		line-height: 1.22;
		font-weight: 900;
		color: #12372a;
	}
	.desc {
		margin-top: 14rpx;
		font-size: 26rpx;
		line-height: 1.7;
	}
	.tags {
		margin-top: 18rpx;
	}
	.author {
		margin-top: 18rpx;
		font-size: 24rpx;
	}
	.layout {
		display: grid;
		grid-template-columns: minmax(0, 1fr) 330rpx;
		gap: 20rpx;
		margin-top: 20rpx;
	}
	.article {
		display: grid;
		gap: 18rpx;
	}
	.section,
	.side {
		padding: 26rpx;
	}
	.section-title,
	.side-title {
		font-size: 30rpx;
		font-weight: 900;
		color: #17231d;
	}
	.section-text {
		margin-top: 12rpx;
		font-size: 26rpx;
		line-height: 1.8;
	}
	.goods {
		display: grid;
		grid-template-columns: 96rpx minmax(0, 1fr);
		gap: 14rpx;
		align-items: center;
		padding: 18rpx 0;
		border-top: 1rpx solid #eef1ee;
	}
	.goods:first-of-type {
		margin-top: 12rpx;
	}
	.goods-cover {
		width: 96rpx;
		height: 96rpx;
		border-radius: 8px;
		overflow: hidden;
		background: #edf3ef;
	}
	.goods-title {
		font-size: 24rpx;
		font-weight: 850;
		color: #17231d;
		line-height: 1.35;
	}
	.goods-price {
		margin-top: 6rpx;
		font-size: 24rpx;
		font-weight: 900;
		color: #d66a2c;
	}
	@media screen and (max-width: 900px) {
		.hero,
		.layout {
			display: flex;
			flex-direction: column;
		}
		.cover {
			width: 100%;
		}
	}
</style>