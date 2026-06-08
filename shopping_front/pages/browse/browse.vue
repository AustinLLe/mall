<template>
  <view class="safe-page browse-page">
    <view class="topbar">
      <view class="content-wrap topbar-inner">
        <view class="brand" @click="navTo('/pages/home/home')">
          <view class="brand-mark">
            <image class="brand-logo" src="/static/logo.png" mode="aspectFit"></image>
          </view>
          <view>
            <text class="brand-name">松果集市</text>
            <text class="brand-sub">可信的新旧商品流转平台</text>
          </view>
        </view>
        <view class="web-nav">
          <text class="nav-link" @click="navTo('/pages/home/home')">首页</text>
          <text class="nav-link on">发现</text>
          <text class="nav-link" @click="navTo('/pages/cart/cart')">购物车</text>
          <text class="nav-link" @click="navTo('/pages/message/message')">消息</text>
          <text class="nav-link" @click="navTo('/pages/user/index')">我的</text>
        </view>
        <view class="publish-btn" @click="goPublish">发布经验</view>
      </view>
    </view>

    <view class="content-wrap page">
      <view class="intro">
        <view>
          <text class="kicker">Discover</text>
          <text class="title">发现值得收藏的清单、故事和信用店铺。</text>
          <text class="desc">这里汇总选购建议、真实物品履历和高信用店铺，帮你更快判断商品是否适合自己。</text>
        </view>
        <view class="intro-side">
          <text class="side-label">今日内容</text>
          <text class="side-value">3</text>
          <text class="side-desc">精选清单</text>
        </view>
      </view>

      <view class="browse-layout">
        <view class="main-feed js-browse-main">
          <view class="tabs">
            <view v-for="tab in tabs" :key="tab.key" class="tab" :class="{ on: activeTab === tab.key }" @click="activeTab = tab.key">
              {{ tab.label }}
            </view>
          </view>

          <view v-if="activeTab === 'topics'" class="feed-list">
              <view v-for="item in topicFeed" :key="item.id" class="article-card" @click="openTopic(item)">
              <view class="article-cover topic-visual has-image">
                <image class="cover-img" :src="topicCover(item)" mode="aspectFill" @error="markTopicCoverFailed(item)"></image>
              </view>
              <view class="article-main">
                <view class="article-line">
                  <text class="badge">{{ item.type }}</text>
                  <text class="heat">{{ item.heat }}</text>
                </view>
                <text class="article-title">{{ item.title }}</text>
                <text class="article-desc">{{ item.desc }}</text>
                <view class="tags">
                  <text v-for="tag in item.tags" :key="tag" class="tag">{{ tag }}</text>
                </view>
              </view>
            </view>
          </view>

          <view v-else-if="activeTab === 'stores'" class="feed-list">
            <view v-for="store in hotStores" :key="store.id" class="article-card" @click="openStore(store)">
              <view class="store-avatar has-image">
                <image class="cover-img" :src="storeCover(store)" mode="aspectFill" @error="markStoreCoverFailed(store)"></image>
              </view>
              <view class="article-main">
                <view class="article-line">
                  <text class="badge orange">{{ store.badge }}</text>
                  <text class="heat">评分 {{ store.score }}</text>
                </view>
                <text class="article-title">{{ store.name }}</text>
                <text class="article-desc">{{ store.desc }}</text>
                <text class="store-meta">{{ store.fans }} 关注</text>
              </view>
            </view>
          </view>

          <view v-else class="feed-list">
            <view v-for="item in storyGoods" :key="item.id" class="article-card" @click="openGoods(item)">
              <view class="article-cover soft story-visual has-image">
                <image class="cover-img" :src="resolveImageUrl(item.cover)" mode="aspectFill"></image>
              </view>
              <view class="article-main">
                <view class="article-line">
                  <text class="badge orange">物品履历</text>
                  <text class="heat">{{ item.location }}</text>
                </view>
                <text class="article-title">{{ item.title }}</text>
                <text class="article-desc">{{ item.story }}</text>
                <view class="mini-timeline">
                  <text v-for="node in item.timeline.slice(0, 2)" :key="node.date">{{ node.date }} · {{ node.title }}</text>
                </view>
              </view>
            </view>
          </view>
        </view>

        <view class="aside js-browse-aside">
          <view class="aside-card">
            <text class="aside-title">热门标签</text>
            <view class="tag-cloud">
              <text v-for="tag in hotTags" :key="tag" class="cloud-tag">{{ tag }}</text>
            </view>
          </view>
          <view class="aside-card">
            <text class="aside-title">推荐店铺</text>
            <view v-for="store in hotStores" :key="store.id" class="store-line" @click="openStore(store)">
              <text class="store-name">{{ store.name }}</text>
              <text class="store-score">{{ store.score }} · {{ store.badge }}</text>
            </view>
          </view>
          <view class="aside-card mission-card">
            <text class="aside-title">逛逛任务</text>
            <view v-for="(item, index) in visibleMissions" :key="item + '-' + index" class="mission-line">
              <text class="mission-index">{{ index + 1 }}</text>
              <text class="mission-text">{{ item }}</text>
            </view>
          </view>
          <view v-if="browseAsideSpacer > 0" class="aside-spacer" :style="{ height: browseAsideSpacer + 'px' }"></view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
  import { topicFeed, hotStores, buildGoodsDetailUrl, buildTopicDetailUrl, getTopicCover, getStoreCover } from '../../data/catalog.js'
  import { fetchProducts } from '@/services/shop.js'
  import { resolveImageUrl } from '@/utils/media.js'

  export default {
    data() {
      return {
        statusBarHeight: 24,
        activeTab: 'topics',
        topicFeed,
        hotStores,
        storyGoodsList: [],
        failedTopicCovers: {},
        failedStoreCovers: {},
        browseMissionCount: 3,
        browseAsideSpacer: 0
      }
    },
    computed: {
      tabs() {
        return [
          { key: 'topics', label: '清单' },
          { key: 'stories', label: '故事' },
          { key: 'stores', label: '店铺' }
        ]
      },
      storyGoods() {
        return this.storyGoodsList.filter((item) => item.scene === 'used' && item.timeline && item.timeline.length)
      },
      hotTags() {
        return ['宿舍桌搭', '二手数码', '同城自提', '交易保障', '新品', '验货清单']
      },
      missionPool() {
        if (this.activeTab === 'stories') {
          return ['先看物品履历与时间线', '优先选择支持当面验货的商品', '收藏 2 个可对比故事商品', '确认交付方式与售后协商', '查看同城距离再决定下单']
        }
        if (this.activeTab === 'stores') {
          return ['对比 3 家店铺评分与服务标签', '优先浏览公告写得清楚的店铺', '关注 1 家信用店铺便于后续上新提醒', '进店后先看在售商品数量', '检查是否支持担保交易']
        }
        return ['按预算筛出 3 件候选商品', '查看每件商品的保障与信用信息', '优先收藏可当面验货的清单商品', '对比同类商品参数和价格', '下单前先看店铺评分与近期评价']
      },
      visibleMissions() {
        return this.missionPool.slice(0, this.browseMissionCount)
      }
    },
    watch: {
      activeTab() {
        this.$nextTick(() => this.syncBrowseAsideLength())
      }
    },
    onLoad() {
      const sys = uni.getWindowInfo()
      this.statusBarHeight = sys.statusBarHeight || 24
      this.loadStoryGoods()
      this.$nextTick(() => this.syncBrowseAsideLength())
    },
    methods: {
      async loadStoryGoods() {
        try {
          const body = await fetchProducts({ scene: 'used' })
          if (body && body.code === 0 && Array.isArray(body.data)) {
            this.storyGoodsList = body.data
          }
        } catch (e) {
          this.storyGoodsList = []
        } finally {
          this.$nextTick(() => this.syncBrowseAsideLength())
        }
      },
      navTo(url) {
        	if (['/pages/home/home', '/pages/browse/browse', '/pages/cart/cart', '/pages/message/message', '/pages/user/index'].includes(url)) {
					  uni.switchTab({ url })
					  return
				  }
				  uni.reLaunch({ url })
      },
      openGoods(item) {
        uni.navigateTo({ url: buildGoodsDetailUrl(item) })
      },
      openTopic(item) {
        uni.navigateTo({ url: buildTopicDetailUrl(item) })
      },
      openStore(store) {
        uni.navigateTo({ url: '/pages/store/store?name=' + encodeURIComponent(store.name) })
      },
      resolveImageUrl,
      topicCover(item) {
        return this.failedTopicCovers[item.id] ? item.fallbackCover : getTopicCover(item)
      },
      storeCover(store) {
        return this.failedStoreCovers[store.id] ? store.fallbackCover : getStoreCover(store)
      },
      markTopicCoverFailed(item) {
        this.failedTopicCovers[item.id] = true
      },
      markStoreCoverFailed(store) {
        this.failedStoreCovers[store.id] = true
      },
      syncBrowseAsideLength() {
        const query = uni.createSelectorQuery().in(this)
        query.select('.js-browse-main').boundingClientRect()
        query.select('.js-browse-aside').boundingClientRect()
        query.exec((res) => {
          if (!Array.isArray(res) || !res[0] || !res[1]) return
          const diff = Math.max(0, Math.round((res[0].height || 0) - (res[1].height || 0)))
          if (diff <= 8) {
            this.browseMissionCount = 3
            this.browseAsideSpacer = 0
            return
          }
          const estimateCount = Math.ceil(diff / 72)
          this.browseMissionCount = Math.max(2, Math.min(6, estimateCount))
          const residual = diff - this.browseMissionCount * 58
          this.browseAsideSpacer = Math.max(0, residual)
        })
      },
      goPublish() {
        uni.navigateTo({ url: '/pages/publish/publish' })
      }
    }
  }
</script>

<style lang="scss" scoped>
  .browse-page {
    padding-bottom: 48px;
  }
  .topbar {
    position: sticky;
    top: 0;
    z-index: 10;
    background: rgba(255,255,255,.88);
    backdrop-filter: blur(22px);
    border-bottom: 1px solid rgba(203, 213, 225, .55);
    box-shadow: 0 10px 40px rgba(60, 64, 67, .06);
  }
  .topbar-inner {
    display: grid;
    grid-template-columns: 300px 320px minmax(0, 1fr);
    align-items: center;
    gap: 18px;
    height: 82px;
    padding: 0 22px;
  }
  .brand {
    display: flex;
    align-items: center;
    gap: 12px;
    flex-shrink: 0;
  }
  .brand-mark {
    width: 40px;
    height: 40px;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    overflow: hidden;
    flex-shrink: 0;
  }
  .brand-logo {
    width: 100%;
    height: 100%;
  }
  .brand-name,
  .brand-sub,
  .kicker,
  .title,
  .desc,
  .side-label,
  .side-value,
  .side-desc,
  .article-title,
  .article-desc,
  .store-meta,
  .aside-title,
  .store-name,
  .store-score,
  .mini-timeline text {
    display: block;
  }
  .brand-name {
    font-size: 18px;
    font-weight: 900;
    color: #202124;
  }
  .brand-sub {
    margin-top: 2px;
    font-size: 12px;
    color: #667085;
  }
  .web-nav {
    justify-self: center;
    display: flex;
    align-items: center;
    gap: 4px;
    padding: 5px;
    box-sizing: border-box;
    height: 50px;
    border-radius: 999px;
    background: rgba(255,255,255,.72);
    border: 1px solid rgba(203, 213, 225, .72);
    box-shadow: 0 14px 38px rgba(60, 64, 67, .08);
  }
  .nav-link {
    width: 82px;
    height: 38px;
    padding: 0;
    border-radius: 999px;
    display: flex;
    align-items: center;
    justify-content: center;
    box-sizing: border-box;
    line-height: 1;
    text-align: center;
    color: #5f6b85;
    font-weight: 750;
    font-size: 13px;
    transition: all .22s ease;
  }
  .nav-link.on,
  .nav-link:hover {
    background: #fff;
    background: linear-gradient(135deg, #ffffff, #f5f7fa);
    color: #12372a;
    box-shadow: 0 10px 26px rgba(18, 55, 42, .14);
  }
  .publish-btn {
    justify-self: end;
    height: 42px;
    padding: 0 18px;
    border-radius: 8px;
    background: #12372a;
    color: #fff;
    display: flex;
    align-items: center;
    font-size: 14px;
    font-weight: 900;
    box-shadow: 0 14px 34px rgba(18, 55, 42, .16);
  }
  .page {
    padding: 26px 22px 0;
    animation: softIn .42s ease both;
  }
  .intro {
    display: grid;
    grid-template-columns: minmax(0, 1fr) 180px;
    gap: 20px;
    align-items: end;
    padding: 34px 38px;
    border: 1px solid #e4e9e5;
    border-radius: 8px;
    background:
      radial-gradient(circle at 88% 18%, rgba(214, 106, 44, .07), transparent 26%),
      #fff;
    box-shadow: 0 16px 48px rgba(17, 38, 28, .06);
  }
  .kicker {
    color: #1f5c43;
    font-size: 13px;
    font-weight: 900;
  }
  .title {
    margin-top: 10px;
    max-width: 760px;
    color: #12372a;
    font-size: 31px;
    line-height: 1.22;
    font-weight: 850;
  }
  .desc {
    margin-top: 12px;
    max-width: 760px;
    color: #667085;
    font-size: 14px;
    line-height: 1.8;
  }
  .intro-side {
    padding: 18px;
    border-radius: 8px;
    background: #f4f8f5;
  }
  .side-label,
  .side-desc {
    color: #667085;
    font-size: 12px;
  }
  .side-value {
    margin-top: 8px;
    color: #12372a;
    font-size: 32px;
    font-weight: 900;
  }
  .browse-layout {
    display: grid;
    grid-template-columns: minmax(0, 1fr) 300px;
    gap: 18px;
    margin-top: 18px;
  }
  .tabs {
    display: flex;
    gap: 4px;
    width: fit-content;
    padding: 4px;
    border-radius: 999px;
    background: #e8f0eb;
  }
  .tab {
    min-width: 82px;
    height: 36px;
    border-radius: 999px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #667085;
    font-size: 13px;
  }
  .tab.on {
    background: #fff;
    color: #12372a;
    font-weight: 900;
    box-shadow: 0 8px 22px rgba(17, 38, 28, .08);
  }
  .feed-list {
    display: flex;
    flex-direction: column;
    gap: 12px;
    margin-top: 14px;
  }
  .article-card,
  .aside-card {
    border: 1px solid #e4e9e5;
    border-radius: 8px;
    background: #fff;
    box-shadow: 0 14px 38px rgba(17, 38, 28, .055);
    transition: transform .22s ease, box-shadow .22s ease;
  }
  .article-card {
    display: grid;
    grid-template-columns: 156px minmax(0, 1fr);
    gap: 18px;
    padding: 18px;
  }
  .article-card:hover,
  .aside-card:hover {
    transform: translateY(-2px);
    box-shadow: 0 22px 62px rgba(17, 38, 28, .095);
  }
  .article-card {
    cursor: pointer;
  }
  .article-cover,
  .store-avatar {
    height: 128px;
    border-radius: 8px;
    background:
      radial-gradient(circle at 50% 42%, rgba(255,255,255,.9), transparent 22%),
      linear-gradient(135deg, #edf5f0 0%, #eaf1ff 100%);
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 0;
  }
  .article-cover::before {
    content: "";
    width: 64px;
    height: 64px;
    border-radius: 18px;
    background:
      radial-gradient(circle at 28% 28%, #ffffff 0 18%, transparent 19%),
      linear-gradient(135deg, #d9eadf, #eaf1ff);
    box-shadow: 0 18px 40px rgba(31, 92, 67, .13);
  }
  .article-cover.has-image::before,
  .store-avatar.has-image::before {
    display: none;
  }
  .cover-img {
    width: 100%;
    height: 100%;
    display: block;
  }
  .topic-visual::before {
    border-radius: 22px 10px 22px 10px;
    background:
      radial-gradient(circle at 28% 28%, #ffffff 0 14%, transparent 15%),
      linear-gradient(135deg, #d9eadf, #f7d278);
  }
  .story-visual::before {
    width: 72px;
    height: 54px;
    border-radius: 14px;
    border: 5px solid rgba(18, 55, 42, .42);
    background: linear-gradient(135deg, #ffffff, #eaf4ee);
  }
  .article-cover.soft {
    background:
      radial-gradient(circle at 50% 42%, rgba(255,255,255,.88), transparent 22%),
      linear-gradient(135deg, #fff6ec 0%, #edf3ef 100%);
  }
  .store-avatar {
    color: #12372a;
    font-size: 34px;
    font-weight: 900;
  }
  .article-line {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 10px;
  }
  .badge,
  .tag,
  .cloud-tag {
    display: inline-flex;
    align-items: center;
    border-radius: 999px;
    background: #f5f7fa;
    color: #12372a;
    font-size: 12px;
    font-weight: 800;
    padding: 5px 10px;
  }
  .badge.orange {
    background: #fff0e7;
    color: #b95420;
  }
  .heat {
    color: #667085;
    font-size: 12px;
  }
  .article-title {
    margin-top: 12px;
    color: #12372a;
    font-size: 19px;
    line-height: 1.38;
    font-weight: 850;
  }
  .article-desc {
    margin-top: 8px;
    color: #667085;
    font-size: 13px;
    line-height: 1.7;
  }
  .tags {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    margin-top: 12px;
  }
  .tag {
    background: #f4f6f4;
    color: #667085;
  }
  .store-meta {
    margin-top: 12px;
    color: #667085;
    font-size: 12px;
  }
  .mini-timeline {
    display: grid;
    gap: 5px;
    margin-top: 12px;
    color: #667085;
    font-size: 12px;
  }
  .aside {
    display: flex;
    flex-direction: column;
    gap: 14px;
    margin-top: 52px;
  }
  .aside-card {
    padding: 18px;
  }
  .aside-title {
    color: #12372a;
    font-size: 17px;
    font-weight: 850;
  }
  .tag-cloud {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    margin-top: 14px;
  }
  .cloud-tag {
    background: #f4f6f4;
    color: #667085;
  }
  .store-line {
    padding: 13px 0;
    border-top: 1px solid #eef1ee;
  }
  .store-line:first-of-type {
    margin-top: 10px;
  }
  .store-name {
    color: #202124;
    font-size: 14px;
    font-weight: 850;
  }
  .store-score {
    margin-top: 5px;
    color: #667085;
    font-size: 12px;
  }
  .mission-card {
    padding-bottom: 12px;
  }
  .mission-line {
    display: grid;
    grid-template-columns: 26px minmax(0, 1fr);
    gap: 10px;
    align-items: start;
    padding: 10px 0;
    border-top: 1px solid #eef1ee;
  }
  .mission-line:first-of-type {
    margin-top: 8px;
  }
  .mission-index {
    width: 24px;
    height: 24px;
    border-radius: 8px;
    background: #f4f8f5;
    color: #12372a;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 12px;
    font-weight: 900;
  }
  .mission-text {
    color: #475467;
    font-size: 13px;
    line-height: 1.65;
  }
  .aside-spacer {
    width: 100%;
    border-radius: 8px;
    background: transparent;
  }
  @media screen and (max-width: 900px) {
    .topbar-inner,
    .intro,
    .browse-layout,
    .article-card {
      display: flex;
      flex-direction: column;
    }
    .web-nav,
    .brand-sub,
    .publish-btn,
    .aside {
      display: none;
    }
    .title {
      font-size: 26px;
    }
  }
</style>
