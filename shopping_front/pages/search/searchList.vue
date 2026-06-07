<template>
    <view class="search-list-page">
        <view class="top-tips">
            <text class="kw-label">关于“{{ currentKeyword }}”的搜索结果：</text>
        </view>

        <view class="tabs-bar">
            <view class="tab-item" :class="{ active: activeTab === 'goods' }" @click="switchTab('goods')">
                <text>商品</text>
            </view>
            <view class="tab-item" :class="{ active: activeTab === 'user' }" @click="switchTab('user')">
                <text>用户</text>
            </view>
        </view>

        <view class="goods-panel-section" v-if="activeTab === 'goods'">
            <view class="filter-bar">
                <view class="filter-item" :class="{ active: activeScene === 'all' }" @click="activeScene = 'all'">
                    <text>全部</text>
                </view>
                <view class="filter-item" :class="{ active: activeScene === 'new' }" @click="activeScene = 'new'">
                    <text>新品</text>
                </view>
                <view class="filter-item" :class="{ active: activeScene === 'used' }" @click="activeScene = 'used'">
                    <text>闲置</text>
                </view>
            </view>

            <view class="sort-bar">
                <view class="sort-item" :class="{ active: currentSort === 'createTime' }" @click="changeSort('createTime')">
                    <text>时间最新</text>
                </view>
                <view class="sort-item" :class="{ active: currentSort === 'price' }" @click="changeSort('price')">
                    <text>价格</text>
                    <text class="arrow" v-if="currentSort === 'price'">{{ currentOrder === 'ASC' ? '▲' : '▼' }}</text>
                </view>
            </view>

            <scroll-view scroll-y class="list-container">
                <view v-if="goodsList.length === 0" class="empty-box">
                    <text class="empty-icon">🤣</text>
                    <text class="empty-text">没有找到相关商品~</text>
                </view>

                <view class="goods-grid" v-else>
                    <view class="goods-card" v-for="item in displayGoods" :key="item.id" @click="goToDetail(item)">
                        <image :src="goodsCover(item)" mode="aspectFill" class="goods-img" />
                        <view class="goods-info">
                            <text class="goods-title">{{ item.title }}</text>
                            <view class="goods-foot">
                                <text class="goods-price">¥{{ item.price }}</text>
                                <text class="goods-address" v-if="item.location">{{ item.location }}</text>
                            </view>
                        </view>
                    </view>
                </view>
            </scroll-view>
        </view>
        <view class="user-panel-section" v-if="activeTab === 'user'">
            <scroll-view scroll-y class="list-container">
                <view v-if="userList.length === 0" class="empty-box">
                    <text class="empty-icon">👥</text>
                    <text class="empty-text">没有找到相关的同学/卖家~</text>
                </view>

                <view class="user-list" v-else>
                    <view class="user-row-card" v-for="user in userList" :key="user.userId" @click="goToUserSpace(user)">
                        <view class="user-avatar">👤</view>
                        <view class="user-meta">
                            <text class="username-text">{{ user.username }}</text>
                            <text class="user-credit-tag">诚信信用分: {{ user.credit || 100 }}</text>
                        </view>
                        <view class="go-space-btn"><text>去主页 →</text></view>
                    </view>
                </view>
            </scroll-view>
        </view>
        </view>
</template>

<script>
    import { get } from '@/utils/request.js' 
    import { fetchProducts } from '@/services/shop.js'
    import { buildGoodsDetailUrl } from '../../data/catalog.js'
    import { isImageUrl, resolveImageUrl } from '@/utils/media.js'
    
    export default {
        data() {
            return {
                currentKeyword: '', 
                activeTab: 'goods',   
                goodsList: [],        
                userList: [],          
                activeScene: 'all',     
                currentSort: 'createTime', 
                currentOrder: 'DESC'       
            }
        },
        watch: {
            activeScene() {
                if (this.activeTab === 'goods') {
                    this.fetchGoodsResult();
                }
            }
        },
        computed: {
            displayGoods() {
                const list = this.activeScene === 'all' ? this.goodsList.slice() : this.goodsList.filter(item => item.scene === this.activeScene);
                if (this.currentSort === 'price') {
                    return list.sort((a, b) => {
                        const diff = Number(a.price || 0) - Number(b.price || 0);
                        return this.currentOrder === 'ASC' ? diff : -diff;
                    });
                }
                return list;
            }
        },
        onLoad(options) {
            if (options.keyword) {
                this.currentKeyword = decodeURIComponent(options.keyword);
                this.fetchGoodsResult();
            }
        },
        methods: {
            goodsCover(item) {
                const cover = item && item.cover
                if (isImageUrl(cover)) return resolveImageUrl(cover)
                return '/static/goods/viewtop-monitor.jpg'
            },
            switchTab(tabName) {
                this.activeTab = tabName;
                if (tabName === 'goods' && this.goodsList.length === 0) {
                    this.fetchGoodsResult();
                } else if (tabName === 'user' && this.userList.length === 0) {
                    this.fetchUserResult();
                }
            },

            fetchGoodsResult() {
                uni.showLoading({ title: '正在检索商品...' });
                fetchProducts({
                    keyword: this.currentKeyword,
                    scene: this.activeScene === 'all' ? '' : this.activeScene
                }).then(body => {
                    if (body && body.code === 0) {
                        this.goodsList = body.data || [];
                    }
                }).finally(() => { uni.hideLoading(); });
            },

            fetchUserResult() {
                uni.showLoading({ title: '正在寻找用户...' });
                const url = `/api/auth/search-users?keyword=${encodeURIComponent(this.currentKeyword)}`;
                get(url).then(res => {
                    if (res.data && res.data.code === 0) {
                        this.userList = res.data.data || [];
                    }
                }).finally(() => { uni.hideLoading(); });
            },

            changeSort(type) {
                if (this.currentSort === type) {
                    this.currentOrder = this.currentOrder === 'DESC' ? 'ASC' : 'DESC';
                } else {
                    this.currentSort = type;
                    this.currentOrder = 'DESC';
                }
                this.fetchGoodsResult();
            },
            goToDetail(item) {
                uni.navigateTo({ url: buildGoodsDetailUrl(item) });
            },
            goToUserSpace(user) {
                uni.navigateTo({
                    url: `/pages/user/space?userId=${user.userId}&username=${user.username}`
                });
            }
        }
    }
</script>

<style scoped>
.search-list-page {
    display: flex;
    flex-direction: column;
    height: 100vh;
    background-color: #f7f8fa;
    box-sizing: border-box;
}

.top-tips {
    padding: 20rpx 30rpx 10rpx 30rpx;
    background-color: #ffffff;
}

.top-tips .kw-label {
    font-size: 26rpx;
    color: #909399;
}

.tabs-bar {
    display: flex;
    background-color: #ffffff;
    border-bottom: 1rpx solid #f2f2f2;
    padding: 10rpx 0;
}

.tabs-bar .tab-item {
    flex: 1;
    display: flex;
    justify-content: center;
    align-items: center;
    padding: 20rpx 0;
    position: relative;
}

.tabs-bar .tab-item text {
    font-size: 30rpx;
    color: #606266;
    font-weight: 500;
    transition: all 0.2s;
}

.tabs-bar .tab-item.active text {
    color: #1b4332;
    font-weight: bold;
    font-size: 32rpx;
}

.tabs-bar .tab-item.active::after {
    content: '';
    position: absolute;
    bottom: 0;
    width: 60rpx;
    height: 6rpx;
    background-color: #1b4332;
    border-radius: 4rpx;
}

.filter-bar {
    display: flex;
    padding: 20rpx 30rpx;
    background-color: #ffffff;
    gap: 20rpx;
}

.filter-bar .filter-item {
    padding: 10rpx 30rpx;
    background-color: #f4f4f5;
    border-radius: 30rpx;
}

.filter-bar .filter-item text {
    font-size: 26rpx;
    color: #606266;
}

.filter-bar .filter-item.active {
    background-color: #1b4332;
}

.filter-bar .filter-item.active text {
    color: #fff;
    font-weight: bold;
}

.sort-bar {
    display: flex;
    padding: 15rpx 40rpx;
    background-color: #ffffff;
    border-bottom: 1rpx solid #eee;
    gap: 50rpx;
}

.sort-bar .sort-item {
    display: flex;
    align-items: center;
    font-size: 28rpx;
    color: #909399;
}

.sort-bar .sort-item .arrow {
    font-size: 20rpx;
    margin-left: 6rpx;
}

.sort-bar .sort-item.active {
    color: #111827;
    font-weight: bold;
}

.list-container {
    flex: 1;
    overflow: hidden;
}

.empty-box {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding-top: 200rpx;
}

.empty-box .empty-icon {
    font-size: 100rpx;
    margin-bottom: 20rpx;
}

.empty-box .empty-text {
    font-size: 28rpx;
    color: #909399;
}

.goods-grid {
    display: flex;
    flex-wrap: wrap;
    padding: 20rpx;
    gap: 20rpx;
}

.goods-grid .goods-card {
    width: calc(50% - 10rpx);
    background-color: #ffffff;
    border-radius: 16rpx;
    overflow: hidden;
    display: flex;
    flex-direction: column;
    box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
}

.goods-grid .goods-card .goods-img {
    width: 100%;
    height: 340rpx;
    background-color: #eaeaea;
}

.goods-grid .goods-card .goods-info {
    padding: 16rpx;
    display: flex;
    flex-direction: column;
}

.goods-grid .goods-card .goods-info .goods-title {
    font-size: 28rpx;
    color: #1f2937;
    font-weight: 500;
    line-height: 40rpx;
    height: 80rpx;
    display: -webkit-box;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 2;
    overflow: hidden;
}

.goods-grid .goods-card .goods-info .goods-foot {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 16rpx;
}

.goods-grid .goods-card .goods-info .goods-foot .goods-price {
    font-size: 32rpx;
    color: #1b4332;
    font-weight: bold;
}

.goods-grid .goods-card .goods-info .goods-foot .goods-address {
    font-size: 22rpx;
    color: #9ca3af;
    max-width: 140rpx;
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
}

.user-list {
    padding: 20rpx;
    display: flex;
    flex-direction: column;
    gap: 20rpx;
}

.user-list .user-row-card {
    display: flex;
    align-items: center;
    background-color: #ffffff;
    padding: 30rpx;
    border-radius: 16rpx;
    box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.03);
}

.user-list .user-row-card .user-avatar {
    width: 90rpx;
    height: 90rpx;
    background-color: #f3f4f6;
    border-radius: 50%;
    display: flex;
    justify-content: center;
    align-items: center;
    font-size: 45rpx;
    margin-right: 24rpx;
    border: 1rpx solid #e5e7eb;
}

.user-list .user-row-card .user-meta {
    display: flex;
    flex-direction: column;
    flex: 1;
    gap: 8rpx;
}

.user-list .user-row-card .user-meta .username-text {
    font-size: 30rpx;
    color: #111827;
    font-weight: bold;
}

.user-list .user-row-card .user-meta .user-credit-tag {
    font-size: 22rpx;
    color: #10b981;
    background-color: #ecfdf5;
    padding: 4rpx 12rpx;
    border-radius: 6rpx;
    align-self: flex-start;
}

.user-list .user-row-card .go-space-btn {
    font-size: 26rpx;
    color: #fff;
    font-weight: 500;
    background-color:#1b4332;
    padding: 12rpx 24rpx;
    border-radius: 30rpx;
}
</style>
