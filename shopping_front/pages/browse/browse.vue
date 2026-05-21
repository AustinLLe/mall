<template>
	<view class="safe-page">
		<view class="content-wrap page">
			<view class="hero">
				<view>
					<text class="kicker">社区发现</text>
					<text class="title">看清单、避坑经验，也发现有故事的闲置。</text>
					<text class="desc">把淘宝式商品效率和闲鱼式社区氛围放在一起，让交易前的信息更透明。</text>
				</view>
				<view class="hero-action" @click="goPublish">发布我的经验</view>
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

			<view v-if="activeTab === 'topics'" class="feed-grid">
				<view v-for="item in topicFeed" :key="item.id" class="topic-card">
					<view class="topic-cover">{{ item.cover }}</view>
					<view class="topic-main">
						<view class="topic-line">
							<text class="badge">{{ item.type }}</text>
							<text class="heat">{{ item.heat }}</text>
						</view>
						<text class="topic-title">{{ item.title }}</text>
						<text class="topic-desc">{{ item.desc }}</text>
						<view class="tags">
							<text v-for="tag in item.tags" :key="tag" class="tag">{{ tag }}</text>
						</view>
						<text class="author">来自 {{ item.author }}</text>
					</view>
				</view>
			</view>

			<view v-else-if="activeTab === 'stores'" class="store-grid">
				<view v-for="store in hotStores" :key="store.id" class="store-card" @click="openStore(store)">
					<view class="store-top">
						<view>
							<text class="store-name">{{ store.name }}</text>
							<text class="store-desc">{{ store.desc }}</text>
						</view>
						<text class="store-badge">{{ store.badge }}</text>
					</view>
					<view class="store-meta">
						<text>评分 {{ store.score }}</text>
						<text>{{ store.fans }} 关注</text>
					</view>
					<scroll-view scroll-x class="mini-line" :show-scrollbar="false">
						<view v-for="item in recommendByStore(store.name)" :key="item.id" class="mini" @click.stop="openGoods(item)">
							<view class="mini-cover">{{ item.cover }}</view>
							<text class="mini-title">{{ item.title }}</text>
							<text class="mini-price">¥{{ item.price }}</text>
						</view>
					</scroll-view>
				</view>
			</view>

			<view v-else class="story-grid">
				<view v-for="item in storyGoods" :key="item.id" class="story-card" @click="openGoods(item)">
					<view class="story-cover">{{ item.cover }}</view>
					<view class="story-main">
						<view class="story-head">
							<text class="badge orange">物品护照</text>
							<text class="heat">{{ item.location }}</text>
						</view>
						<text class="story-title">{{ item.title }}</text>
						<text class="story-desc">{{ item.story }}</text>
						<view class="timeline">
							<view v-for="node in item.timeline.slice(0, 3)" :key="node.date" class="node">
								<text class="dot"></text>
								<text class="node-text">{{ node.date }} · {{ node.title }}</text>
							</view>
						</view>
					</view>
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
					{ key: 'topics', label: '话题清单' },
					{ key: 'stories', label: '二手故事' },
					{ key: 'stores', label: '热门店铺' }
				]
			},
			storyGoods() {
				return goodsCatalog.filter((item) => item.timeline && item.timeline.length)
			}
		},
		methods: {
			recommendByStore(name) {
				return goodsCatalog.filter((item) => item.shopName === name).slice(0, 4)
			},
			openGoods(item) {
				uni.navigateTo({ url: buildGoodsDetailUrl(item) })
			},
			openStore(store) {
				uni.navigateTo({ url: '/pages/store/store?name=' + encodeURIComponent(store.name) })
			},
			goPublish() {
				uni.navigateTo({ url: '/pages/publish/publish' })
			}
		}
	}
</script>

<style lang="scss" scoped>
	.page {
		padding: 28rpx;
	}
	.hero {
		display: flex;
		justify-content: space-between;
		gap: 24rpx;
		align-items: flex-end;
		background: #fff;
		border: 1rpx solid #e4e9e5;
		border-radius: 28rpx;
		padding: 38rpx;
		box-shadow: 0 14rpx 36rpx rgba(15, 35, 26, 0.06);
	}
	.kicker,
	.desc,
	.heat,
	.author,
	.store-desc,
	.store-meta,
	.story-desc,
	.node-text {
		color: #667085;
	}
	.kicker {
		font-size: 24rpx;
		font-weight: 800;
		color: #1f5c43;
	}
	.title {
		display: block;
		margin-top: 12rpx;
		font-size: 44rpx;
		line-height: 1.2;
		font-weight: 900;
		color: #17231d;
		max-width: 780rpx;
	}
	.desc {
		display: block;
		margin-top: 14rpx;
		font-size: 26rpx;
		line-height: 1.6;
	}
	.hero-action {
		padding: 18rpx 28rpx;
		border-radius: 16rpx;
		background: #1f5c43;
		color: #fff;
		font-size: 26rpx;
		font-weight: 800;
		flex-shrink: 0;
	}
	.tabs {
		display: flex;
		gap: 14rpx;
		margin-top: 24rpx;
		background: #e8f0eb;
		border-radius: 18rpx;
		padding: 6rpx;
		width: fit-content;
	}
	.tab {
		min-width: 150rpx;
		text-align: center;
		padding: 16rpx 20rpx;
		border-radius: 14rpx;
		font-size: 25rpx;
		color: #667085;
	}
	.tab.on {
		background: #fff;
		color: #1f5c43;
		font-weight: 800;
	}
	.feed-grid,
	.store-grid,
	.story-grid {
		display: grid;
		grid-template-columns: repeat(3, minmax(0, 1fr));
		gap: 20rpx;
		margin-top: 24rpx;
	}
	.topic-card,
	.store-card,
	.story-card {
		background: #fff;
		border: 1rpx solid #e4e9e5;
		border-radius: 24rpx;
		padding: 24rpx;
		box-shadow: 0 12rpx 32rpx rgba(15, 35, 26, 0.05);
	}
	.topic-cover,
	.story-cover {
		height: 160rpx;
		border-radius: 20rpx;
		background: #edf3ef;
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 70rpx;
		margin-bottom: 20rpx;
	}
	.topic-line,
	.store-top,
	.store-meta,
	.story-head {
		display: flex;
		justify-content: space-between;
		align-items: center;
		gap: 12rpx;
	}
	.badge,
	.tag,
	.store-badge {
		font-size: 22rpx;
		padding: 7rpx 14rpx;
		border-radius: 999rpx;
		background: #e8f3ed;
		color: #1f5c43;
	}
	.badge.orange,
	.store-badge {
		background: #fff0e7;
		color: #b95420;
	}
	.heat {
		font-size: 22rpx;
	}
	.topic-title,
	.store-name,
	.story-title {
		display: block;
		margin-top: 16rpx;
		font-size: 31rpx;
		line-height: 1.35;
		font-weight: 900;
		color: #17231d;
	}
	.topic-desc,
	.store-desc,
	.story-desc {
		display: block;
		margin-top: 12rpx;
		font-size: 24rpx;
		line-height: 1.6;
	}
	.tags {
		display: flex;
		flex-wrap: wrap;
		gap: 10rpx;
		margin-top: 16rpx;
	}
	.tag {
		background: #f4f6f4;
		color: #667085;
	}
	.author {
		display: block;
		margin-top: 18rpx;
		font-size: 23rpx;
	}
	.store-meta {
		margin-top: 18rpx;
		font-size: 24rpx;
	}
	.mini-line {
		white-space: nowrap;
		margin-top: 22rpx;
	}
	.mini {
		display: inline-flex;
		flex-direction: column;
		width: 188rpx;
		margin-right: 14rpx;
	}
	.mini-cover {
		height: 130rpx;
		border-radius: 18rpx;
		background: #edf3ef;
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 52rpx;
	}
	.mini-title,
	.mini-price {
		display: block;
		margin-top: 8rpx;
	}
	.mini-title {
		font-size: 23rpx;
		color: #17231d;
		white-space: normal;
		line-height: 1.35;
	}
	.mini-price {
		font-size: 27rpx;
		font-weight: 900;
		color: #d66a2c;
	}
	.timeline {
		margin-top: 18rpx;
	}
	.node {
		display: flex;
		align-items: center;
		gap: 10rpx;
		padding: 8rpx 0;
	}
	.dot {
		width: 12rpx;
		height: 12rpx;
		border-radius: 50%;
		background: #d66a2c;
		flex-shrink: 0;
	}
	.node-text {
		font-size: 23rpx;
	}
	@media screen and (max-width: 900px) {
		.hero {
			align-items: flex-start;
			flex-direction: column;
		}
		.feed-grid,
		.store-grid,
		.story-grid {
			grid-template-columns: 1fr;
		}
		.tabs {
			width: auto;
		}
		.tab {
			flex: 1;
			min-width: 0;
		}
	}
</style>
