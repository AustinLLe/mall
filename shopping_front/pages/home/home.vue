<template>
	<view class="home-container">
		<view class="search-bar">
			<view class="search-input">
				<text class="icon">🔍</text>
				<input type="text" placeholder="搜宝贝、搜用户..." />
			</view>
		</view>

		<view class="nav-grid">
			<view class="nav-item" v-for="(item, index) in categories" :key="index">
				<view class="nav-icon">{{ item.icon }}</view>
				<text class="nav-text">{{ item.name }}</text>
			</view>
		</view>

		<view class="section-title">
			<text>新鲜好物</text>
			<text class="more">更多 ></text>
		</view>

		<view class="goods-list">
			<view class="goods-card" v-for="(goods, index) in dummyGoods" :key="index">
				<view class="goods-image">商品图展示</view>
				<view class="goods-detail">
					<text class="goods-name">{{ goods.title }}</text>
					<view class="goods-footer">
						<text class="price">￥{{ goods.price }}</text>
						<text class="user-info">来自: {{ goods.user }}</text>
					</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
export default {
	data() {
		return {
			categories: [
				{ name: '数码', icon: '📱' },
				{ name: '图书', icon: '📚' },
				{ name: '服饰', icon: '👕' },
				{ name: '运动', icon: '⚽' },
				{ name: '更多', icon: '✨' }
			],
			dummyGoods: [
				{ title: '二手 MacBook Air 95新', price: 3200, user: '学长A' },
				{ title: '考研英语红宝书', price: 15, user: '大四学姐' },
				{ title: '宿舍可用小火锅', price: 35, user: '研一学长' },
				{ title: '吉他 几乎全新', price: 400, user: '音乐社' }
			]
		}
	}
}
</script>

<style>
.home-container { background-color: #f5f5f5; min-height: 100vh; padding-bottom: 20rpx; }
/* 搜索栏样式 */
.search-bar { background-color: #fff; padding: 20rpx 30rpx; position: sticky; top: 0; z-index: 100; }
.search-input { background-color: #f1f1f1; height: 70rpx; border-radius: 35rpx; display: flex; align-items: center; padding: 0 30rpx; }
.icon { margin-right: 15rpx; }
/* 分类导航 */
.nav-grid { display: flex; justify-content: space-around; padding: 30rpx 0; background-color: #fff; margin-bottom: 20rpx; }
.nav-item { display: flex; flex-direction: column; align-items: center; }
.nav-icon { font-size: 50rpx; margin-bottom: 10rpx; }
.nav-text { font-size: 24rpx; color: #666; }
/* 标题 */
.section-title { padding: 20rpx 30rpx; display: flex; justify-content: space-between; align-items: center; }
.section-title text:first-child { font-size: 32rpx; font-weight: bold; }
.more { font-size: 24rpx; color: #999; }
/* 商品卡片布局 */
.goods-list { display: flex; flex-wrap: wrap; padding: 0 20rpx; justify-content: space-between; }
.goods-card { width: 48%; background-color: #fff; border-radius: 15rpx; margin-bottom: 20rpx; overflow: hidden; }
.goods-image { height: 300rpx; background-color: #e0e0e0; display: flex; align-items: center; justify-content: center; color: #999; }
.goods-detail { padding: 15rpx; }
.goods-name { font-size: 28rpx; color: #333; height: 80rpx; display: block; }
.goods-footer { display: flex; justify-content: space-between; align-items: center; margin-top: 10rpx; }
.price { color: #ff4400; font-size: 32rpx; font-weight: bold; }
.user-info { font-size: 20rpx; color: #999; }
</style>