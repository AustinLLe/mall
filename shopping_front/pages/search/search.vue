<template>
    <view class="search-page">
        <view class="search-header">
            <view class="search-input-box">
                <text class="search-icon">⌕</text>
                <input 
                    type="text" 
                    v-model="keyword" 
                    :focus="true" 
                    placeholder="搜索商品、店铺、话题灵感" 
                    confirm-type="search"
                    @confirm="handleSearch(keyword)"
                />
                <text v-if="keyword" class="clear-icon" @click="clearKeyword">✕</text>
            </view>
            <text class="search-btn" @click="handleSearch(keyword)">搜索</text>
        </view>

        <scroll-view scroll-y class="search-content">
            <view class="section-box" v-if="historyList.length > 0">
                <view class="section-title-row">
                    <text class="section-title">历史搜索</text>
                    <text class="delete-icon" @click="clearHistory">🗑</text>
                </view>
                <view class="tag-container">
                    <view 
                        class="tag-item" 
                        v-for="(item, index) in historyList" 
                        :key="index"
                        @click="handleSearch(item)"
                    >
                        {{ item }}
                    </view>
                </view>
            </view>

            <view class="section-box">
                <view class="section-title-row">
                    <text class="section-title">热门搜索</text>
                </view>
                <view class="tag-container">
                    <view 
                        class="tag-item hot" 
                        v-for="(item, index) in hotList" 
                        :key="index"
                        @click="handleSearch(item)"
                    >
                        {{ item }}
                    </view>
                </view>
            </view>
        </scroll-view>
    </view>
</template>

<script>
export default {
    data() {
        return {
            keyword: '',       
            historyList: [],    
            hotList: ['考研数学', '四六级真题', 'AirPods', '北航寝室神器', '考研政治'] 
        }
    },
    onLoad() {
        this.loadSearchHistory();
    },
    methods: {
        handleSearch(searchWord) {
            if (!searchWord || !searchWord.trim()) {
                return uni.showToast({ title: '请输入搜索关键词', icon: 'none' });
            }
            const kw = searchWord.trim();
            this.saveSearchHistory(kw);
            this.keyword = '';
            uni.navigateTo({
                url: `/pages/search/searchList?keyword=${encodeURIComponent(kw)}`
            });
        },

        saveSearchHistory(word) {
            let history = uni.getStorageSync('search_history_cache') || [];
            const index = history.indexOf(word);
            if (index > -1) history.splice(index, 1);
            history.unshift(word);
            if (history.length > 10) history.pop();
            this.historyList = history;
            uni.setStorageSync('search_history_cache', history);
        },

        loadSearchHistory() {
            this.historyList = uni.getStorageSync('search_history_cache') || [];
        },

        clearHistory() {
            uni.showModal({
                title: '提示',
                content: '确定要清空搜索历史吗？',
                success: (res) => {
                    if (res.confirm) {
                        this.historyList = [];
                        uni.removeStorageSync('search_history_cache');
                    }
                }
            });
        },

        clearKeyword() {
            this.keyword = '';
        }
    }
}
</script>

<style scoped>
.search-page {
    min-height: 100vh;
    background-color: #f8f8f8;
    display: flex;
    flex-direction: column;
}


.search-header {
    background-color: #ffffff;
    padding: 20rpx 30rpx;
    display: flex;
    align-items: center;
    border-bottom: 1rpx solid #eeeeee;
}

.search-input-box {
    flex: 1;
    height: 72rpx;
    background-color: #f1f3f2;
    border-radius: 36rpx;
    display: flex;
    align-items: center;
    padding: 0 24rpx;
    position: relative;
}

.search-icon {
    font-size: 38rpx;
    color: #999999;
    margin-right: 12rpx;
    margin-top: -4rpx;
}

.search-input-box input {
    flex: 1;
    font-size: 28rpx;
    color: #333333;
}

.clear-icon {
    font-size: 28rpx;
    color: #999999;
    padding: 10rpx;
    position: absolute;
    right: 15rpx;
    z-index: 10;
}

.search-btn {
    font-size: 30rpx;
    color: #333333;
    font-weight: bold;
    margin-left: 24rpx;
    padding: 10rpx 0;
}

.search-content, .result-content {
    flex: 1;
    overflow: hidden;
}

.section-box {
    background-color: #ffffff;
    margin-top: 16rpx;
    padding: 30rpx;
}

.section-title-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24rpx;
}

.section-title {
    font-size: 28rpx;
    font-weight: bold;
    color: #333333;
}

.delete-icon {
    font-size: 32rpx;
    color: #999999;
    padding: 10rpx;
}

.tag-container {
    display: flex;
    flex-wrap: wrap;
}

.tag-item {
    font-size: 26rpx;
    color: #666666;
    background-color: #f5f5f5;
    padding: 12rpx 24rpx;
    border-radius: 28rpx;
    margin-right: 20rpx;
    margin-bottom: 20rpx;
}

.tag-item.hot {
    color: #fff;
    background-color: #1b4332;
}

.empty-box {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding-top: 200rpx;
}

.empty-icon {
    font-size: 100rpx;
    margin-bottom: 30rpx;
}

.empty-text {
    font-size: 28rpx;
    color: #999999;
}

</style>