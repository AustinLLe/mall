<template>
  <view class="safe-page browse-page">
    <view class="topbar">
      <view class="content-wrap topbar-inner">
        <view class="brand" @click="navTo('/pages/home/home')">
          <image class="brand-logo" src="/static/logo.png" mode="aspectFit"></image>
          <view>
            <text class="brand-name">松果集市</text>
            <text class="brand-sub">话题、经验和真实商品讨论</text>
          </view>
        </view>
        <view class="web-nav">
          <text class="nav-link" @click="navTo('/pages/home/home')">首页</text>
          <text class="nav-link on">发现</text>
          <text class="nav-link" @click="navTo('/pages/cart/cart')">购物车</text>
          <text class="nav-link" @click="navTo('/pages/message/message')">消息</text>
          <text class="nav-link" @click="navTo('/pages/user/index')">我的</text>
        </view>
      </view>
    </view>

    <view class="content-wrap page">
      <view class="intro">
        <view>
          <text class="kicker">Community Topics</text>
          <text class="title">发现值得参与的话题合集</text>
          <text class="desc">新品推荐、宿舍好物、防晒避雷、学习资料等话题都来自数据库。点进话题后可以像朋友圈一样发帖、评论和点赞。</text>
        </view>
        <view class="intro-side">
          <text class="side-value">{{ topics.length }}</text>
          <text class="side-desc">个话题合集</text>
        </view>
      </view>

      <view class="browse-layout">
        <view class="main-feed">
          <view class="tabs">
            <view v-for="tab in tabs" :key="tab.key" class="tab" :class="{ on: activeTab === tab.key }" @click="activeTab = tab.key">
              {{ tab.label }}
            </view>
          </view>

          <view v-if="activeTab === 'topics'" class="topic-grid">
            <view v-for="item in filteredTopics" :key="item.id" class="topic-card" @click="openTopic(item)">
              <view class="topic-cover" :class="{ 'has-image': isImageUrl(item.cover) }">
                <image v-if="isImageUrl(item.cover)" class="cover-img" :src="resolveImageUrl(item.cover)" mode="aspectFill"></image>
                <text v-else>{{ item.type }}</text>
              </view>
              <view class="topic-main">
                <view class="article-line">
                  <text class="badge">{{ item.type }}</text>
                  <text class="heat">{{ item.heat }}</text>
                </view>
                <text class="article-title">{{ item.title }}</text>
                <text class="article-desc">{{ item.desc }}</text>
                <view class="tags">
                  <text v-for="tag in item.tags || []" :key="tag" class="tag">{{ tag }}</text>
                </view>
              </view>
            </view>
          </view>

          <view v-else-if="activeTab === 'stores'" class="feed-list">
            <view v-for="store in hotStores" :key="store.id" class="article-card" @click="openStore(store)">
              <view class="store-avatar">{{ (store.name || '店').slice(0, 1) }}</view>
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
              <view class="article-cover" :class="{ 'has-image': isImageUrl(item.cover) }">
                <image v-if="isImageUrl(item.cover)" class="cover-img" :src="resolveImageUrl(item.cover)" mode="aspectFill"></image>
                <text v-else>{{ item.category || '商品' }}</text>
              </view>
              <view class="article-main">
                <view class="article-line">
                  <text class="badge orange">物品故事</text>
                  <text class="heat">{{ item.location }}</text>
                </view>
                <text class="article-title">{{ item.title }}</text>
                <text class="article-desc">{{ item.story || item.description }}</text>
              </view>
            </view>
          </view>
        </view>

        <view class="aside">
          <view class="aside-card">
            <text class="aside-title">数据库标签</text>
            <view class="tag-cloud">
              <text class="cloud-tag" :class="{ on: activeTag === '' }" @click="activeTag = ''">全部</text>
              <text v-for="tag in hotTags" :key="tag" class="cloud-tag" :class="{ on: activeTag === tag }" @click="activeTag = tag">{{ tag }}</text>
            </view>
          </view>
          <view class="aside-card">
            <text class="aside-title">话题玩法</text>
            <view class="mission-line">
              <text class="mission-index">1</text>
              <text class="mission-text">选择一个合集进入讨论页</text>
            </view>
            <view class="mission-line">
              <text class="mission-index">2</text>
              <text class="mission-text">发布自己的购买经验或避坑记录</text>
            </view>
            <view class="mission-line">
              <text class="mission-index">3</text>
              <text class="mission-text">给有用帖子点赞或评论补充</text>
            </view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
  import { buildGoodsDetailUrl, buildTopicDetailUrl } from '../../data/catalog.js'
  import { fetchProducts, fetchStores, fetchTopics } from '@/services/shop.js'
  import { isImageUrl, resolveImageUrl } from '@/utils/media.js'

  export default {
    data() {
      return {
        activeTab: 'topics',
        activeTag: '',
        topics: [],
        hotStores: [],
        storyGoodsList: []
      }
    },
    computed: {
      tabs() {
        return [
          { key: 'topics', label: '话题' },
          { key: 'stories', label: '故事' },
          { key: 'stores', label: '店铺' }
        ]
      },
      filteredTopics() {
        if (!this.activeTag) return this.topics
        return this.topics.filter(item => (item.tags || []).includes(this.activeTag))
      },
      hotTags() {
        const set = new Set()
        this.topics.forEach(item => (item.tags || []).forEach(tag => set.add(tag)))
        return Array.from(set)
      },
      storyGoods() {
        return this.storyGoodsList.filter((item) => item.scene === 'used').slice(0, 8)
      }
    },
    onLoad() {
      this.loadAll()
    },
    methods: {
      isImageUrl,
      resolveImageUrl,
      async loadAll() {
        await Promise.all([this.loadTopics(), this.loadStores(), this.loadStoryGoods()])
      },
      async loadTopics() {
        try {
          const body = await fetchTopics()
          this.topics = body && body.code === 0 && Array.isArray(body.data) ? body.data : []
        } catch (e) {
          this.topics = []
        }
      },
      async loadStores() {
        try {
          const body = await fetchStores()
          this.hotStores = body && body.code === 0 && Array.isArray(body.data) ? body.data : []
        } catch (e) {
          this.hotStores = []
        }
      },
      async loadStoryGoods() {
        try {
          const body = await fetchProducts({ scene: 'used' })
          this.storyGoodsList = body && body.code === 0 && Array.isArray(body.data) ? body.data : []
        } catch (e) {
          this.storyGoodsList = []
        }
      },
      navTo(url) {
        if (['/pages/home/home', '/pages/browse/browse', '/pages/cart/cart', '/pages/message/message', '/pages/user/index'].includes(url)) {
          uni.switchTab({ url })
          return
        }
        uni.navigateTo({ url })
      },
      openGoods(item) {
        uni.navigateTo({ url: buildGoodsDetailUrl(item) })
      },
      openTopic(item) {
        uni.navigateTo({ url: buildTopicDetailUrl(item) })
      },
      openStore(store) {
        uni.navigateTo({ url: '/pages/store/store?name=' + encodeURIComponent(store.name) })
      }
    }
  }
</script>

<style lang="scss" scoped>
  .browse-page { padding-bottom: 48px; background: #f5f6f8; }
  .topbar { position: sticky; top: 0; z-index: 10; background: rgba(255,255,255,.94); backdrop-filter: blur(18px); border-bottom: 1px solid rgba(203,213,225,.55); }
  .topbar-inner { display: grid; grid-template-columns: 300px minmax(0, 1fr); align-items: center; gap: 18px; height: 82px; padding: 0 22px; }
  .brand { display: flex; align-items: center; gap: 12px; }
  .brand-logo { width: 42px; height: 42px; border-radius: 8px; }
  .brand-name, .brand-sub, .kicker, .title, .desc, .side-value, .side-desc, .article-title, .article-desc, .store-meta, .aside-title, .mission-text { display: block; }
  .brand-name { font-size: 20px; font-weight: 900; color: #202124; }
  .brand-sub { margin-top: 2px; font-size: 12px; color: #667085; }
  .web-nav { justify-self: end; display: flex; align-items: center; gap: 4px; padding: 5px; height: 50px; border-radius: 999px; background: #fff; border: 1px solid rgba(203,213,225,.72); box-sizing: border-box; }
  .nav-link { width: 82px; height: 38px; border-radius: 999px; display: flex; align-items: center; justify-content: center; color: #5f6b85; font-size: 13px; font-weight: 800; }
  .nav-link.on, .nav-link:hover { background: #12372a; color: #fff; }
  .page { padding: 26px 22px 0; }
  .intro, .topic-card, .article-card, .aside-card { background: #fff; border: 1px solid #e4e9e5; border-radius: 8px; box-shadow: 0 14px 38px rgba(17,38,28,.055); }
  .intro { display: grid; grid-template-columns: minmax(0, 1fr) 180px; gap: 20px; align-items: end; padding: 34px 38px; }
  .kicker { color: #1f5c43; font-size: 13px; font-weight: 900; }
  .title { margin-top: 10px; color: #12372a; font-size: 32px; line-height: 1.22; font-weight: 900; }
  .desc { margin-top: 12px; max-width: 760px; color: #667085; font-size: 14px; line-height: 1.8; }
  .intro-side { padding: 18px; border-radius: 8px; background: #f4f8f5; }
  .side-value { color: #12372a; font-size: 34px; font-weight: 900; }
  .side-desc { margin-top: 4px; color: #667085; font-size: 12px; }
  .browse-layout { display: grid; grid-template-columns: minmax(0, 1fr) 300px; gap: 18px; margin-top: 18px; }
  .tabs { display: flex; gap: 4px; width: fit-content; padding: 4px; border-radius: 999px; background: #e8f0eb; }
  .tab { min-width: 82px; height: 36px; border-radius: 999px; display: flex; align-items: center; justify-content: center; color: #667085; font-size: 13px; }
  .tab.on { background: #fff; color: #12372a; font-weight: 900; box-shadow: 0 8px 22px rgba(17,38,28,.08); }
  .topic-grid, .feed-list { display: grid; gap: 14px; margin-top: 14px; }
  .topic-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .topic-card { overflow: hidden; }
  .topic-cover { height: 190px; background: #edf3ef; display: flex; align-items: center; justify-content: center; color: #12372a; font-size: 28px; font-weight: 900; overflow: hidden; }
  .cover-img { width: 100%; height: 100%; display: block; }
  .topic-main, .article-main { padding: 18px; }
  .article-card { display: grid; grid-template-columns: 150px minmax(0, 1fr); gap: 18px; padding: 18px; }
  .article-cover, .store-avatar { height: 128px; border-radius: 8px; background: #edf3ef; display: flex; align-items: center; justify-content: center; overflow: hidden; color: #12372a; font-size: 34px; font-weight: 900; }
  .article-line { display: flex; align-items: center; justify-content: space-between; gap: 10px; }
  .badge, .tag, .cloud-tag { display: inline-flex; align-items: center; border-radius: 999px; background: #f5f7fa; color: #12372a; font-size: 12px; font-weight: 800; padding: 5px 10px; }
  .badge.orange { background: #fff0e7; color: #b95420; }
  .heat { color: #667085; font-size: 12px; }
  .article-title { margin-top: 12px; color: #12372a; font-size: 19px; line-height: 1.38; font-weight: 900; }
  .article-desc { margin-top: 8px; color: #667085; font-size: 13px; line-height: 1.7; }
  .tags, .tag-cloud { display: flex; flex-wrap: wrap; gap: 8px; margin-top: 12px; }
  .tag { background: #f4f6f4; color: #667085; }
  .aside { display: grid; gap: 14px; align-content: start; margin-top: 52px; }
  .aside-card { padding: 18px; }
  .aside-title { color: #12372a; font-size: 17px; font-weight: 900; }
  .cloud-tag.on { background: #12372a; color: #fff; }
  .mission-line { display: grid; grid-template-columns: 26px minmax(0, 1fr); gap: 10px; align-items: start; padding: 12px 0; border-top: 1px solid #eef1ee; }
  .mission-line:first-of-type { margin-top: 8px; }
  .mission-index { width: 24px; height: 24px; border-radius: 8px; background: #f4f8f5; color: #12372a; display: flex; align-items: center; justify-content: center; font-size: 12px; font-weight: 900; }
  .mission-text { color: #475467; font-size: 13px; line-height: 1.65; }
  @media screen and (max-width: 900px) {
    .topbar-inner, .intro, .browse-layout, .article-card { display: flex; flex-direction: column; }
    .web-nav, .brand-sub, .aside { display: none; }
    .topic-grid { grid-template-columns: 1fr; }
    .title { font-size: 26px; }
  }
</style>
