<template>
	<view class="safe-page">
		<view class="content-wrap page">
			<text class="title">确认订单</text>
			<view class="card">
				<text class="label">收货地址</text>
				<text class="value">{{ addressText }}</text>
			</view>
			<view class="card">
				<text class="label">订单商品</text>
				<view v-for="item in selectedItems" :key="item.id" class="item">
					<text>{{ item.cover }} {{ item.title }} x{{ item.qty }}</text>
					<text>¥{{ item.price * item.qty }}</text>
				</view>
			</view>
			<view class="card">
				<view class="item"><text>商品合计</text><text>¥{{ total }}</text></view>
				<view class="item"><text>运费</text><text>¥0</text></view>
				<view class="item total"><text>应付</text><text>¥{{ total }}</text></view>
			</view>
			<view class="pay" @click="pay">模拟支付</view>
		</view>
	</view>
</template>
<script>
	import { getCartItems, clearCheckedCartItems } from '@/utils/cart.js'
	import { getDefaultAddress } from '@/utils/address.js'
	export default {
		data() { return { items: [], address: null } },
		computed: {
			selectedItems() { return this.items.filter((item) => item.checked) },
			total() { return this.selectedItems.reduce((sum, item) => sum + item.price * item.qty, 0).toFixed(2) },
			addressText() { return this.address ? `${this.address.name} ${this.address.phone} ${this.address.region} ${this.address.detail}` : '未设置地址，可在个人中心维护' }
		},
		onShow() { this.items = getCartItems(); this.address = getDefaultAddress() },
		methods: {
			pay() { clearCheckedCartItems(); uni.navigateTo({ url: '/pages/order/pay-result' }) }
		}
	}
</script>
<style lang="scss" scoped>
	.page { padding: 28rpx; }
	.title { display: block; font-size: 40rpx; font-weight: 900; color: #17231d; margin-bottom: 22rpx; }
	.card { background: #fff; border: 1rpx solid #e4e9e5; border-radius: 24rpx; padding: 26rpx; margin-bottom: 18rpx; }
	.label, .value { display: block; }
	.label { font-size: 24rpx; color: #667085; margin-bottom: 10rpx; }
	.value { font-size: 28rpx; color: #17231d; line-height: 1.6; }
	.item { display: flex; justify-content: space-between; gap: 18rpx; padding: 12rpx 0; font-size: 26rpx; color: #17231d; }
	.total { font-weight: 900; color: #d66a2c; }
	.pay { margin-top: 24rpx; height: 88rpx; border-radius: 18rpx; background: #1f5c43; color: #fff; display: flex; align-items: center; justify-content: center; font-size: 30rpx; font-weight: 900; }
</style>
