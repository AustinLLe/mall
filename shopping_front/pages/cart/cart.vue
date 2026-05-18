<template>
	<view class="page">
		<view v-if="items.length" class="addr-card" @click="goAddress">
			<view v-if="defaultAddress">
				<view class="addr-top">
					<text class="addr-name">{{ defaultAddress.name }}</text>
					<text class="addr-phone">{{ defaultAddress.phone }}</text>
					<text v-if="defaultAddress.isDefault" class="addr-badge">默认</text>
				</view>
				<text class="addr-detail">{{ defaultAddress.region }} {{ defaultAddress.detail }}</text>
			</view>
			<view v-else>
				<text class="addr-empty">请先添加收货地址</text>
			</view>
			<text class="addr-link">管理地址 ›</text>
		</view>

		<view v-if="items.length" class="list">
			<view v-for="item in items" :key="item.id" class="card">
				<view class="check" :class="{ on: item.checked }" @click="toggleChecked(item.id)">
					{{ item.checked ? '✓' : '' }}
				</view>
				<view class="image">
   					<image v-if="item.image" :src="item.image" mode="aspectFill"></image>
				</view>
				<view class="main">
					<text class="title">{{ item.goodsName }}</text>
					<view class="meta">
						<text v-if="item.tag" class="tag">{{ item.tag }}</text>
						<text v-if="item.credit" class="credit">信用 {{ item.credit }}</text>
					</view>
					<view class="bottom">
						<text class="price">¥{{ item.price }}</text>
						<view class="qty">
							<text class="op" @click="changeQty(item, -1)">-</text>
							<text class="num">{{ item.qty }}</text>
							<text class="op" @click="changeQty(item, 1)">+</text>
						</view>
					</view>
					<text class="remove" @click="remove(item.id)">删除</text>
				</view>
			</view>
		</view>

		<view v-else class="empty">
			<view class="illu">🛒</view>
			<text class="t1">购物车还是空的</text>
			<text class="t2">先去逛逛，把心动的好物装进来</text>
			<button class="btn" @click="go">去逛逛</button>
		</view>

		<view v-if="items.length" class="settle">
			<view class="sum">
				<text class="sum-label">已选 {{ selectedCount }} 件</text>
				<text class="sum-price">合计 ¥{{ totalPrice }}</text>
			</view>
			<button class="settle-btn" @click="goCheckout">去结算</button>
		</view>
	</view>
</template>

<script>
	import { getCartItems, updateCartItem, removeCartItem, clearCheckedCartItems } from '@/utils/cart.js'
	import { getDefaultAddress } from '@/utils/address.js'

	export default {
		data() {
			return {
				items: [],
				defaultAddress: null
			}
		},
		computed: {
			selectedItems() {
				return this.items.filter((item) => item.checked)
			},
			selectedCount() {
				return this.selectedItems.reduce((sum, item) => sum + item.qty, 0)
			},
			totalPrice() {
				const total = this.selectedItems.reduce((sum, item) => sum + item.price * item.qty, 0)
				return total.toFixed(2)
			}
		},
		onShow() {
        	this.loadData()
    	},
		methods: {
			loadData() {
            	this.items = getCartItems()
            	this.defaultAddress = getDefaultAddress()
        	},
			go() {
				uni.switchTab({ url: '/pages/browse/browse' })
			},
			goAddress() {
				uni.navigateTo({ url: '/pages/address/list' })
			},
			toggleChecked(id) {
				const current = this.items.find((item) => item.id === id)
				if (!current) return
				updateCartItem(id, { checked: !current.checked })
				this.loadData()
			},
			changeQty(item, delta) {
				const nextQty = item.qty + delta
				if (nextQty < 1) {
					this.remove(item.id)
					return
				}
				updateCartItem(item.id, { qty: nextQty })
				this.loadData()
			},
			remove(id) {
				removeCartItem(id)
				this.loadData()
				uni.showToast({ title: '已删除', icon: 'none' })
			},
			goCheckout() {
                if (!this.selectedItems.length) {
                    uni.showToast({ title: '请先选择商品', icon: 'none' })
                    return
                }
                if (!this.defaultAddress) {
                    uni.showToast({ title: '请先添加收货地址', icon: 'none' })
                    this.goAddress()
                }
				uni.navigateTo({ url: '/pages/order/orderConfirmation' })
			}
		}
	}
</script>

<style lang="scss" scoped>
	.page {
		min-height: 100vh;
		padding: 24rpx 24rpx 180rpx;
	}
	.addr-card,
	.card {
		background: #fff;
		border-radius: 24rpx;
		box-shadow: 0 8rpx 28rpx rgba(0, 0, 0, 0.05);
	}
	.addr-card {
		padding: 28rpx;
		margin-bottom: 20rpx;
	}
	.addr-top {
		display: flex;
		align-items: center;
		gap: 16rpx;
		flex-wrap: wrap;
	}
	.addr-name {
		font-size: 30rpx;
		font-weight: 700;
		color: #222;
	}
	.addr-phone,
	.addr-detail,
	.addr-empty,
	.addr-link {
		font-size: 24rpx;
		color: #666;
		display: block;
	}
	.addr-detail,
	.addr-link {
		margin-top: 12rpx;
	}
	.addr-badge {
		font-size: 22rpx;
		color: #2d6a4f;
		background: #edf7f1;
		padding: 6rpx 14rpx;
		border-radius: 999rpx;
	}
	.list {
		display: flex;
		flex-direction: column;
		gap: 20rpx;
	}
	.card {
		padding: 24rpx;
		display: flex;
		gap: 20rpx;
	}
	.check {
		width: 40rpx;
		height: 40rpx;
		border-radius: 50%;
		border: 2rpx solid #c9d6cf;
		margin-top: 40rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		color: #fff;
		font-size: 22rpx;
		flex-shrink: 0;
	}
	.check.on {
		background: #1b4332;
		border-color: #1b4332;
	}
	.image {
    	width: 132rpx;
    	height: 132rpx;
    	border-radius: 20rpx;
    	background: #eef2ef;
    	display: flex;
    	align-items: center;
    	justify-content: center;
    	font-size: 72rpx;
    	flex-shrink: 0;
    	overflow: hidden; 
	}
	.image image {
    	width: 100%;
    	height: 100%;
    	display: block;
	}
	.main {
		flex: 1;
		min-width: 0;
	}
	.title {
		font-size: 30rpx;
		color: #222;
		font-weight: 600;
		display: block;
	}
	.meta {
		display: flex;
		gap: 12rpx;
		flex-wrap: wrap;
		margin-top: 12rpx;
	}
	.tag,
	.credit {
		font-size: 22rpx;
		border-radius: 999rpx;
		padding: 6rpx 14rpx;
	}
	.tag {
		background: #eef6f1;
		color: #2d6a4f;
	}
	.credit {
		background: #fff1e8;
		color: #c45c26;
	}
	.bottom {
		display: flex;
		align-items: center;
		justify-content: space-between;
		margin-top: 18rpx;
	}
	.price {
		font-size: 34rpx;
		font-weight: 700;
		color: #c45c26;
	}
	.qty {
		display: flex;
		align-items: center;
		background: #f5f5f5;
		border-radius: 999rpx;
		overflow: hidden;
	}
	.op,
	.num {
		width: 56rpx;
		height: 56rpx;
		line-height: 56rpx;
		text-align: center;
		font-size: 28rpx;
	}
	.remove {
		font-size: 24rpx;
		color: #999;
		display: inline-block;
		margin-top: 18rpx;
	}
	.empty {
		text-align: center;
		padding-top: 120rpx;
	}
	.illu {
		font-size: 120rpx;
		margin-bottom: 32rpx;
		opacity: 0.85;
	}
	.t1 {
		display: block;
		font-size: 34rpx;
		font-weight: 600;
		color: #222;
	}
	.t2 {
		display: block;
		margin-top: 12rpx;
		font-size: 26rpx;
		color: #888;
	}
	.btn {
		margin-top: 48rpx;
		background: #1b4332;
		color: #fff;
		font-size: 28rpx;
		border-radius: 999rpx;
		padding: 0 56rpx;
		height: 80rpx;
		line-height: 80rpx;
		border: none;
	}
	.settle {
		position: fixed;
		left: 0;
		right: 0;
		bottom: 0;
		background: #fff;
		padding: 20rpx 24rpx calc(20rpx + env(safe-area-inset-bottom));
		box-shadow: 0 -8rpx 28rpx rgba(0, 0, 0, 0.06);
		display: flex;
		align-items: center;
		justify-content: space-between;
		gap: 24rpx;
	}
	.sum {
		display: flex;
		flex-direction: column;
	}
	.sum-label {
		font-size: 24rpx;
		color: #666;
	}
	.sum-price {
		font-size: 34rpx;
		font-weight: 700;
		color: #c45c26;
		margin-top: 8rpx;
	}
	.settle-btn {
		margin: 0;
		width: 240rpx;
		height: 88rpx;
		line-height: 88rpx;
		border-radius: 999rpx;
		background: #1b4332;
		color: #fff;
		font-size: 30rpx;
		border: none;
	}
</style>
