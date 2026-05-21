<template>
	<view class="safe-page cart-page">
		<view class="content-wrap page">
			<view class="page-head">
				<view>
					<text class="title">购物车</text>
					<text class="desc">按店铺合并结算，二手商品会保留信用与担保提示。</text>
				</view>
				<view class="head-action" @click="goBrowse">继续逛逛</view>
			</view>

			<view v-if="items.length" class="cart-layout">
				<view class="shop-list">
					<view v-for="group in groups" :key="group.shopName" class="shop-card">
						<view class="shop-head">
							<text class="shop-name">{{ group.shopName }}</text>
							<text class="shop-tip">平台担保 · 自动合并同店订单</text>
						</view>
						<view v-for="item in group.items" :key="item.id" class="cart-item">
							<view class="check" :class="{ on: item.checked }" @click="toggleChecked(item.id)">
								{{ item.checked ? '✓' : '' }}
							</view>
							<view class="cover">{{ item.cover }}</view>
							<view class="item-main">
								<view class="item-top">
									<text class="item-title">{{ item.title }}</text>
									<text class="scene-tag" :class="item.scene">{{ item.scene === 'new' ? '新品' : '二手' }}</text>
								</view>
								<view class="meta">
									<text v-if="item.tag" class="tag">{{ item.tag }}</text>
									<text v-if="item.credit" class="tag orange">信用 {{ item.credit }}</text>
								</view>
								<view class="bottom">
									<text class="price">¥{{ item.price }}</text>
									<view class="qty">
										<text class="op" @click="changeQty(item, -1)">-</text>
										<text class="num">{{ item.qty }}</text>
										<text class="op" @click="changeQty(item, 1)">+</text>
									</view>
								</view>
								<view class="item-actions">
									<text @click="findSimilar(item)">找相似</text>
									<text @click="remove(item.id)">删除</text>
								</view>
							</view>
						</view>
					</view>
				</view>

				<view class="summary">
					<text class="summary-title">结算摘要</text>
					<view class="row">
						<text>已选商品</text>
						<text>{{ selectedCount }} 件</text>
					</view>
					<view class="row">
						<text>商品金额</text>
						<text>¥{{ totalPrice }}</text>
					</view>
					<view class="row">
						<text>平台保障</text>
						<text>担保交易</text>
					</view>
					<view class="coupon">系统会在确认订单页模拟选择最优优惠。</view>
					<view class="total">
						<text>应付</text>
						<text>¥{{ totalPrice }}</text>
					</view>
					<view class="checkout" @click="checkout">去结算</view>
				</view>
			</view>

			<view v-else class="empty">
				<view class="empty-icon">🛒</view>
				<text class="empty-title">购物车还是空的</text>
				<text class="empty-desc">去看看新品严选，或淘一件有故事的闲置。</text>
				<view class="empty-btn" @click="goBrowse">去逛逛</view>
			</view>
		</view>
	</view>
</template>

<script>
	import { getCartItems, updateCartItem, removeCartItem, groupCartByShop } from '@/utils/cart.js'

	export default {
		data() {
			return {
				items: []
			}
		},
		computed: {
			groups() {
				return groupCartByShop(this.items)
			},
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
			},
			goBrowse() {
				uni.switchTab({ url: '/pages/browse/browse' })
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
				uni.showToast({ title: '已删除商品', icon: 'none' })
			},
			findSimilar(item) {
				uni.showToast({ title: '已为你筛选相似商品：' + item.title, icon: 'none' })
			},
			checkout() {
				if (!this.selectedItems.length) {
					uni.showToast({ title: '请先选择商品', icon: 'none' })
					return
				}
				uni.navigateTo({ url: '/pages/order/confirm' })
			}
		}
	}
</script>

<style lang="scss" scoped>
	.page {
		padding: 28rpx;
	}
	.page-head,
	.shop-head,
	.item-top,
	.bottom,
	.row,
	.total {
		display: flex;
		align-items: center;
		justify-content: space-between;
		gap: 18rpx;
	}
	.page-head {
		margin-bottom: 24rpx;
	}
	.title,
	.summary-title,
	.empty-title {
		display: block;
		font-size: 40rpx;
		font-weight: 900;
		color: #17231d;
	}
	.desc,
	.shop-tip,
	.empty-desc {
		display: block;
		margin-top: 8rpx;
		font-size: 25rpx;
		color: #667085;
	}
	.head-action,
	.checkout,
	.empty-btn {
		border-radius: 16rpx;
		background: #1f5c43;
		color: #fff;
		font-size: 26rpx;
		font-weight: 900;
		padding: 18rpx 28rpx;
	}
	.cart-layout {
		display: grid;
		grid-template-columns: minmax(0, 1fr) 330px;
		gap: 24rpx;
		align-items: start;
	}
	.shop-list {
		display: flex;
		flex-direction: column;
		gap: 20rpx;
	}
	.shop-card,
	.summary,
	.empty {
		background: #fff;
		border: 1rpx solid #e4e9e5;
		border-radius: 24rpx;
		box-shadow: 0 14rpx 36rpx rgba(15, 35, 26, 0.06);
	}
	.shop-card {
		padding: 24rpx;
	}
	.shop-name {
		font-size: 30rpx;
		font-weight: 900;
		color: #17231d;
	}
	.cart-item {
		display: flex;
		gap: 18rpx;
		padding: 22rpx 0;
		border-top: 1rpx solid #eef1ee;
	}
	.check {
		width: 42rpx;
		height: 42rpx;
		border-radius: 50%;
		border: 2rpx solid #cbd5d0;
		margin-top: 48rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		color: #fff;
		font-size: 24rpx;
		flex-shrink: 0;
	}
	.check.on {
		background: #1f5c43;
		border-color: #1f5c43;
	}
	.cover {
		width: 150rpx;
		height: 150rpx;
		border-radius: 20rpx;
		background: #edf3ef;
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 70rpx;
		flex-shrink: 0;
	}
	.item-main {
		flex: 1;
		min-width: 0;
	}
	.item-title {
		font-size: 30rpx;
		font-weight: 900;
		color: #17231d;
		line-height: 1.35;
	}
	.scene-tag,
	.tag {
		font-size: 22rpx;
		padding: 7rpx 13rpx;
		border-radius: 999rpx;
		flex-shrink: 0;
	}
	.scene-tag.new,
	.tag {
		background: #e8f3ed;
		color: #1f5c43;
	}
	.scene-tag.used,
	.tag.orange {
		background: #fff0e7;
		color: #b95420;
	}
	.meta {
		display: flex;
		flex-wrap: wrap;
		gap: 10rpx;
		margin-top: 12rpx;
	}
	.bottom {
		margin-top: 18rpx;
	}
	.price {
		font-size: 34rpx;
		font-weight: 900;
		color: #d66a2c;
	}
	.qty {
		display: flex;
		align-items: center;
		background: #f4f6f4;
		border-radius: 999rpx;
		overflow: hidden;
	}
	.op,
	.num {
		width: 58rpx;
		height: 58rpx;
		line-height: 58rpx;
		text-align: center;
		font-size: 28rpx;
	}
	.item-actions {
		display: flex;
		gap: 20rpx;
		margin-top: 16rpx;
		font-size: 24rpx;
		color: #667085;
	}
	.summary {
		padding: 28rpx;
		position: sticky;
		top: 24rpx;
	}
	.row {
		font-size: 26rpx;
		color: #4b5563;
		padding: 18rpx 0;
		border-bottom: 1rpx solid #eef1ee;
	}
	.coupon {
		margin-top: 18rpx;
		border-radius: 16rpx;
		background: #fff0e7;
		color: #b95420;
		font-size: 24rpx;
		line-height: 1.5;
		padding: 18rpx;
	}
	.total {
		margin-top: 22rpx;
		font-size: 32rpx;
		font-weight: 900;
		color: #17231d;
	}
	.total text:last-child {
		color: #d66a2c;
	}
	.checkout {
		margin-top: 24rpx;
		text-align: center;
	}
	.empty {
		text-align: center;
		padding: 90rpx 32rpx;
	}
	.empty-icon {
		font-size: 110rpx;
		margin-bottom: 18rpx;
	}
	.empty-btn {
		display: inline-flex;
		margin-top: 30rpx;
	}
	@media screen and (max-width: 900px) {
		.cart-layout {
			grid-template-columns: 1fr;
		}
		.summary {
			position: static;
		}
		.page-head {
			align-items: flex-start;
			flex-direction: column;
		}
	}
</style>
