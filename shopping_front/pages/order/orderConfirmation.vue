<template>
    <view class="order-confirmation-page">
        <view class="address-section" @click="goAddress">
            <view class="address-info" v-if="defaultAddress">
                <view class="addr-top">
                    <text class="addr-name">{{ defaultAddress.name }}</text>
                    <text class="addr-phone">{{ defaultAddress.phone }}</text>
                    <text v-if="defaultAddress.isDefault" class="addr-badge">默认</text>
                </view>
                <text class="addr-detail">{{ defaultAddress.region }} {{ defaultAddress.detail }}</text>
            </view>
            <view class="no-address" v-else>
                <text>请选择或添加收货地址</text>
            </view>
			<text class="addr-link">更换地址 ›</text>
        </view>

        <view class="goods-section">
            <view class="seller-header">
                <text class="seller-avatar">👤</text>
                <text class="seller-name">卖家: {{ goodsInfo.sellerName }}</text>
                <text class="credit-tag" v-if="goodsInfo.sellerCredit">信用 {{ goodsInfo.sellerCredit }}</text>
            </view>
            
            <view class="goods-card">
                <image :src="goodsInfo.image" mode="aspectFill" class="goods-img" />
                <view class="goods-detail">
                    <text class="goods-title">{{ goodsInfo.goodsName }}</text>
                    <view class="price-row">
                        <text class="price">¥{{ goodsInfo.price }}</text>
                        <text class="count">x{{ quantity }}</text>
                    </view>
                </view>
            </view>
            
            <view class="remark-row">
                <text class="label">买家备注：</text>
                <input type="text" v-model="buyerRemark" placeholder="选填: 可约定面交时间和地点" />
            </view>
        </view>

        <view class="payment-section">
            <view class="section-title">交易/支付方式</view>
            <radio-group @change="handlePayMethodChange">
                <label class="pay-item">
                    <view class="pay-left">🤝 <text>校内线下当面交易（面交现金/微信）</text></view>
                    <radio value="offline" :checked="payMethod === 'offline'" color="#1b4332" />
                </label>
                <label class="pay-item">
                    <view class="pay-left">💳 <text>线上直接支付（模拟钱包扣款）</text></view>
                    <radio value="online" :checked="payMethod === 'online'" color="#1b4332" />
                </label>
            </radio-group>
        </view>

        <view class="bottom-bar">
            <view class="total-box">
                <text class="label">合计: </text>
                <text class="total-price">¥{{ totalPrice }}</text>
            </view>
            <button class="submit-btn" @click="handlePlaceOrder">提交订单</button>
        </view>
    </view>
</template>

<script>

</script>
<style scoped>
.order-confirmation-page {
    min-height: 100vh;
    background-color: #f7f8fa;
    padding: 20rpx 24rpx 140rpx;
    box-sizing: border-box;
    font-family: -apple-system, BlinkMacSystemFont, 'Helvetica Neue', Helvetica, Segoe UI, Arial, Roboto, sans-serif;
}

.address-info {
    flex: 1;
    display: flex;
    flex-direction: column;
    padding-right: 20rpx;
}

.addr-top {
    display: flex;
    align-items: center;
    margin-bottom: 12rpx;
}

.addr-name {
    font-size: 30rpx;
    font-weight: 600;
    color: #1f2937;
    margin-right: 16rpx; 
}

.addr-phone {
    font-size: 28rpx;
    color: #4b5563;
}

.addr-link {
	font-size: 24rpx;
	color: #666;
	display: block;
    margin-top: 12rpx;
}

.addr-badge {
    margin-left: 16rpx;
    font-size: 20rpx;
    color: #1b4332;
    background-color: #ecfdf5;
    padding: 2rpx 10rpx;
    border-radius: 6rpx;
    font-weight: 500;
}

.addr-detail {
    font-size: 26rpx;
    color: #4b5563;
    line-height: 1.4;
}

.address-section {
    background-color: #ffffff;
    border-radius: 20rpx;
    padding: 30rpx 24rpx;
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 20rpx;
    box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.03);
}

.address-section:active {
    background-color: #fafafa;
}

.address-info {
    flex: 1;
    display: flex;
    flex-direction: column;
    padding-right: 20rpx;
}

.user-row {
    font-size: 30rpx;
    font-weight: 600;
    color: #1f2937;
    margin-bottom: 12rpx;
}

.address-detail {
    font-size: 26rpx;
    color: #4b5563;
    line-height: 1.4;
}

.no-address {
    flex: 1;
    font-size: 28rpx;
    color: #1b4332;
    font-weight: 500;
}

.arrow-right {
    font-size: 28rpx;
    color: #9ca3af;
    font-weight: bold;
}

.goods-section {
    background-color: #ffffff;
    border-radius: 20rpx;
    padding: 24rpx;
    margin-bottom: 20rpx;
    box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.03);
}

.seller-header {
    display: flex;
    align-items: center;
    margin-bottom: 24rpx;
}

.seller-avatar {
    font-size: 28rpx;
    margin-right: 12rpx;
}

.seller-name {
    font-size: 26rpx;
    font-weight: 500;
    color: #374151;
}

.credit-tag {
    margin-left: 16rpx;
    font-size: 20rpx;
    color: #10b981;
    background-color: #ecfdf5;
    padding: 2rpx 12rpx;
    border-radius: 6rpx;
    font-weight: 500;
}

.goods-card {
    display: flex;
    margin-bottom: 30rpx;
}

.goods-img {
    width: 160rpx;
    height: 160rpx;
    border-radius: 12rpx;
    background-color: #f3f4f6;
    flex-shrink: 0;
}

.goods-detail {
    flex: 1;
    padding-left: 20rpx;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
}

.goods-title {
    font-size: 28rpx;
    color: #1f2937;
    line-height: 1.4;
    display: -webkit-box;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 2;
    overflow: hidden;
}

.price-row {
    display: flex;
    align-items: baseline;
    justify-content: space-between;
}

.price {
    font-size: 32rpx;
    font-weight: bold;
    color: #1b4332;
}

.count {
    font-size: 24rpx;
    color: #9ca3af;
}

.remark-row {
    display: flex;
    align-items: center;
    border-top: 1rpx solid #f3f4f6;
    padding-top: 24rpx;
}

.remark-row .label {
    font-size: 26rpx;
    color: #4b5563;
    width: 140rpx;
    flex-shrink: 0;
}

.remark-row input {
    flex: 1;
    font-size: 26rpx;
    color: #1f2937;
}

.payment-section {
    background-color: #ffffff;
    border-radius: 20rpx;
    padding: 24rpx;
    box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.03);
}

.section-title {
    font-size: 28rpx;
    font-weight: 600;
    color: #1f2937;
    margin-bottom: 20rpx;
}

.pay-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 24rpx 0;
    border-bottom: 1rpx solid #f3f4f6;
}

.pay-item:last-child {
    border-bottom: none;
    padding-bottom: 4rpx;
}

.pay-left {
    font-size: 28rpx;
    color: #374151;
    display: flex;
    align-items: center;
}

.pay-left text {
    margin-left: 12rpx;
}

.pay-item radio {
    transform: scale(0.9);
}

.bottom-bar {
    position: fixed;
    bottom: 0;
    left: 0;
    right: 0;
    height: 110rpx;
    background-color: #ffffff;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 32rpx;
    box-shadow: 0 -4rpx 16rpx rgba(0, 0, 0, 0.05);
    z-index: 100;
    padding-bottom: env(safe-area-inset-bottom);
}

.total-box {
    display: flex;
    align-items: baseline;
}

.total-box .label {
    font-size: 26rpx;
    color: #4b5563;
    margin-right: 8rpx;
}

.total-price {
    font-size: 38rpx;
    font-weight: bold;
    color: #1b4332;
}

.submit-btn {
    width: 240rpx;
    height: 76rpx;
    line-height: 76rpx;
    background: linear-gradient(135deg, #1b4332, #1b4332);
    color: #ffffff;
    font-size: 28rpx;
    font-weight: 600;
    border-radius: 38rpx;
    border: none;
    margin: 0; 
    padding: 0;
}

.submit-btn::after {
    border: none;
}

.submit-btn:active {
    opacity: 0.9;
}
</style>