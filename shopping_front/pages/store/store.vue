<template>
	<view class="safe-page">
		<view class="content-wrap page">
			<view class="store-head">
				<view class="avatar">店</view>
				<view>
					<text class="name">{{ store.name }}</text>
					<text class="desc">{{ store.desc }}</text>
					<view class="meta"><text>评分 {{ store.score }}</text><text>{{ store.fans }} 关注</text><text>{{ store.badge }}</text></view>
				</view>
			</view>
			<view class="grid">
				<view v-for="item in goods" :key="item.id" class="goods" @click="open(item)">
					<view class="cover">{{ item.cover }}</view>
					<text class="title">{{ item.title }}</text>
					<text class="price">¥{{ item.price }}</text>
				</view>
			</view>
		</view>
	</view>
</template>
<script>
	import { hotStores, findStoreByName, productsByStore, buildGoodsDetailUrl } from '../../data/catalog.js'
	export default {
		data() { return { store: hotStores[0] } },
		computed: { goods() { return productsByStore(this.store.name) } },
		onLoad(q) { this.store = findStoreByName(q && q.name ? decodeURIComponent(q.name) : '') || hotStores[0] },
		methods: { open(item) { uni.navigateTo({ url: buildGoodsDetailUrl(item) }) } }
	}
</script>
<style lang="scss" scoped>
	.page { padding: 28rpx; }
	.store-head { display: flex; gap: 22rpx; background: #fff; border-radius: 26rpx; padding: 30rpx; border: 1rpx solid #e4e9e5; }
	.avatar { width: 100rpx; height: 100rpx; border-radius: 26rpx; background: #1f5c43; color: #fff; display: flex; align-items: center; justify-content: center; font-weight: 900; }
	.name, .desc { display: block; }
	.name { font-size: 38rpx; font-weight: 900; color: #17231d; }
	.desc { margin-top: 8rpx; font-size: 25rpx; color: #667085; line-height: 1.6; }
	.meta { display: flex; gap: 12rpx; flex-wrap: wrap; margin-top: 14rpx; color: #1f5c43; font-size: 24rpx; }
	.grid { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 18rpx; margin-top: 22rpx; }
	.goods { background: #fff; border-radius: 22rpx; padding: 20rpx; border: 1rpx solid #e4e9e5; }
	.cover { height: 160rpx; border-radius: 18rpx; background: #edf3ef; display: flex; align-items: center; justify-content: center; font-size: 64rpx; }
	.title, .price { display: block; margin-top: 12rpx; }
	.title { font-size: 26rpx; font-weight: 900; color: #17231d; line-height: 1.35; }
	.price { color: #d66a2c; font-size: 30rpx; font-weight: 900; }
	@media screen and (max-width: 900px) { .grid { grid-template-columns: repeat(2, minmax(0, 1fr)); } }
</style>
