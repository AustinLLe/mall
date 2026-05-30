<template>
  <view class="me-page">
    <view class="topbar">
      <view class="me-wrap topbar-inner">
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
          <text class="nav-link" @click="navTo('/pages/browse/browse')">发现</text>
          <text class="nav-link" @click="navTo('/pages/cart/cart')">购物车</text>
          <text class="nav-link on">我的</text>
        </view>
      </view>
    </view>
    <view class="me-wrap">
    <view v-if="!loggedIn" class="login-card">
      <text class="login-title">&#30331;&#24405;&#26494;&#26524;&#38598;&#24066;</text>
      <text class="login-desc">&#30331;&#24405;&#21518;&#26597;&#30475;&#35746;&#21333;&#12289;&#25910;&#34255;&#12289;&#36275;&#36857;&#21644;&#20449;&#29992;&#20998;&#12290;</text>
      <view class="login-actions">
        <button class="primary" @click="goLogin">&#30331;&#24405;</button>
        <button class="ghost" @click="goRegister">&#27880;&#20876;</button>
      </view>
    </view>

    <view v-else-if="user.role === 'seller' || user.role === 'admin'" class="login-card">
      <text class="login-title">{{ user.roleLabel }}&#24037;&#20316;&#21488;</text>
      <text class="login-desc">&#20320;&#24403;&#21069;&#30331;&#24405;&#20026;{{ user.roleLabel }}&#65292;&#21487;&#36827;&#20837;&#23545;&#24212;&#31471;&#30340;&#22235;&#20837;&#21475;&#39029;&#38754;&#12290;</text>
      <view class="login-actions">
        <button class="primary" @click="enterRoleHome">&#36827;&#20837;&#24037;&#20316;&#21488;</button>
        <button class="ghost" @click="logout">&#36864;&#20986;</button>
      </view>
    </view>

    <view v-else>
      <view class="profile-card">
        <view class="avatar">{{ avatar }}</view>
        <view class="profile-main">
          <text class="name">{{ user.username || '松果用户' }}</text>
          <text class="meta">{{ center.phoneMasked || user.phoneMasked || '&#26410;&#32465;&#23450;&#25163;&#26426;' }} · {{ verifiedText }}</text>
          <view class="chips">
            <text class="chip">&#20080;&#23478;</text>
            <text class="chip">&#20449;&#29992; {{ user.credit || 100 }}</text>
            <text class="chip">{{ center.accountStatus || '正常' }}</text>
          </view>
        </view>
      </view>

      <view class="stat-grid">
        <view v-for="item in modules" :key="item.title" class="stat-card" :class="{ clickable: moduleType(item) }" @click="openModule(item)">
          <text class="stat-value">{{ item.value }}</text>
          <text class="stat-label">{{ item.title }}</text>
          <text class="stat-hint">{{ item.desc }}</text>
        </view>
      </view>

      <view v-if="activeModule === 'credit' || activeModule === 'favorite' || activeModule === 'history' || activeModule === 'follow'" class="section interaction-panel">
        <view class="section-head">
          <text class="section-title">{{ activePanelTitle }}</text>
          <text class="section-more">数据库同步</text>
        </view>
        <view v-if="activeModule !== 'credit'" class="panel-actions">
          <button class="ghost small" :loading="panelLoading" @click="clearInteraction">清空记录</button>
        </view>
        <view class="interaction-list">
          <view v-if="!interactionItems.length" class="empty-line">暂无记录，去首页收藏商品、进商品详情浏览，或进入店铺关注后会自动同步</view>
          <view v-for="item in interactionItems" :key="item.type + '-' + item.id" class="interaction-row">
            <view>
              <text class="interaction-title">{{ item.title }}</text>
              <text class="interaction-desc">{{ item.desc }} · {{ item.createdAt }}</text>
            </view>
            <text class="interaction-type">{{ item.type }}</text>
          </view>
        </view>
      </view>

      <view v-if="activeModule === 'realname'" class="section realname-panel">
        <view class="section-head">
          <text class="section-title">&#23454;&#21517;&#35748;&#35777;&#27169;&#25311;</text>
          <text class="section-more">{{ verifiedText }}</text>
        </view>
        <view class="realname-detail">
          <view class="detail-row">
            <text class="detail-label">认证状态</text>
            <text class="detail-value">{{ verifiedText }}</text>
          </view>
          <view class="detail-row">
            <text class="detail-label">脱敏姓名</text>
            <text class="detail-value">{{ realNameInfo.realName || '暂未提交' }}</text>
          </view>
          <view class="detail-row">
            <text class="detail-label">脱敏证件号</text>
            <text class="detail-value">{{ realNameInfo.idCardMasked || '暂未提交' }}</text>
          </view>
        </view>
        <view class="realname-form">
          <input v-model="realNameForm.realName" class="realname-input" placeholder="真实姓名" />
          <input v-model="realNameForm.idCard" class="realname-input" placeholder="身份证号，提交后只保存脱敏值" />
          <button class="primary small" :loading="submittingRealName" @click="submitRealNameForm">提交实名</button>
        </view>
        <view class="realname-actions">
          <button class="ghost small" :disabled="!hasRealName" :loading="cancelingRealName" @click="cancelRealNameForm">取消认证</button>
        </view>
      </view>

      <view class="menu-grid">
        <view v-for="item in menus" :key="item.title" class="menu-card" @click="openMenu(item)">
          <text class="menu-title">{{ item.title }}</text>
          <text class="menu-desc">{{ item.desc }}</text>
        </view>
      </view>
    </view>
    </view>
  </view>
</template>

<script>
import { getToken, getCachedUser, clearSession, goRoleHome } from '@/utils/auth.js'
import { fetchMe } from '@/services/auth.js'
import { cancelRealName, clearBuyerItems, fetchBuyerCenter, fetchBuyerItems, submitRealName } from '@/services/center.js'

export default {
  data() {
    return {
      loggedIn: false,
      user: {},
      center: {},
      activeModule: '',
      interactionItems: [],
      panelLoading: false,
      submittingRealName: false,
      cancelingRealName: false,
      realNameForm: {
        realName: '',
        idCard: ''
      }
    }
  },
  computed: {
    avatar() {
      return (this.user.username || 'M').slice(0, 1).toUpperCase()
    },
    verifiedText() {
      const status = this.center.realName && this.center.realName.status
      if (status === 'approved') return '\u5df2\u5b9e\u540d'
      if (status === 'pending') return '\u5f85\u5ba1\u6838'
      if (status === 'rejected') return '\u5df2\u9a73\u56de'
      return '\u672a\u5b9e\u540d'
    },
    realNameInfo() {
      return this.center.realName || {}
    },
    hasRealName() {
      return !!(this.realNameInfo && this.realNameInfo.id)
    },
    activePanelTitle() {
      const map = {
        credit: '\u4fe1\u7528\u5206\u8bb0\u5f55',
        favorite: '\u6536\u85cf\u5939',
        history: '\u6d4f\u89c8\u8db3\u8ff9',
        follow: '\u5173\u6ce8\u5e97\u94fa'
      }
      return map[this.activeModule] || '\u8bb0\u5f55'
    },
    modules() {
      if (this.center.modules && this.center.modules.length) {
        return this.center.modules
      }
      return [
        { title: '\u8d26\u53f7\u4fe1\u606f', value: this.user.phoneMasked || '--', desc: '\u7b49\u5f85\u6570\u636e\u5e93\u540c\u6b65' },
        { title: '\u4fe1\u7528\u5206', value: this.user.credit || 100, desc: '\u4ece users.credit \u8bfb\u53d6' },
        { title: '\u6211\u7684\u4e92\u52a8', value: '0 / 0 / 0', desc: '\u6536\u85cf / \u8db3\u8ff9 / \u5173\u6ce8' },
        { title: '\u5b9e\u540d\u8ba4\u8bc1', value: this.verifiedText, desc: '\u53ef\u63d0\u4ea4\u540e\u7ba1\u7406\u5458\u5ba1\u6838' }
      ]
    },
    menus() {
      return [
        { title: '\u6536\u85cf\u5939', desc: '\u8ffd\u8e2a\u5fc3\u4eea\u5546\u54c1', type: 'favorite' },
        { title: '\u6d4f\u89c8\u8db3\u8ff9', desc: '\u627e\u56de\u770b\u8fc7\u7684\u5546\u54c1', type: 'history' },
        { title: '\u5173\u6ce8\u5e97\u94fa', desc: '\u67e5\u770b\u5e97\u94fa\u4e0a\u65b0', type: 'follow' },
        { title: '\u8d26\u53f7\u8bbe\u7f6e', desc: '\u9000\u51fa\u767b\u5f55', action: 'logout' }
      ]
    }
  },
  onShow() {
    this.refresh()
  },
  methods: {
    async refresh() {
      const token = getToken()
      if (!token) {
        this.loggedIn = false
        this.user = {}
        return
      }
      const cached = getCachedUser()
      if (cached) {
        this.user = cached
        this.loggedIn = true
      }
      try {
        const body = await fetchMe()
        if (body.code === 0 && body.data) {
          this.user = body.data
          this.loggedIn = true
          uni.setStorageSync('auth_user', body.data)
          if (body.data.role === 'buyer') {
            await this.loadBuyerCenter()
          }
        }
      } catch (e) {
        clearSession()
        this.loggedIn = false
        this.user = {}
      }
    },
    async loadBuyerCenter() {
      try {
        const body = await fetchBuyerCenter()
        if (body.code === 0 && body.data) {
          this.center = body.data
        }
      } catch (e) {
        this.center = {}
      }
    },
    openModule(item) {
      const type = this.moduleType(item)
      if (!type) return
      this.activeModule = this.activeModule === type ? '' : type
      if (this.activeModule && this.activeModule !== 'realname') {
        this.loadInteractionItems()
      }
    },
    moduleType(item) {
      const title = item && item.title
      if (title === '\u4fe1\u7528\u5206') return 'credit'
      if (title === '\u6211\u7684\u4e92\u52a8') return 'favorite'
      if (title === '\u5b9e\u540d\u8ba4\u8bc1' || title === '实名认证') return 'realname'
      return ''
    },
    async loadInteractionItems() {
      if (!['credit', 'favorite', 'history', 'follow'].includes(this.activeModule)) return
      this.panelLoading = true
      try {
        const body = await fetchBuyerItems(this.activeModule)
        if (body.code === 0) {
          this.interactionItems = body.data || []
        }
      } finally {
        this.panelLoading = false
      }
    },
    async clearInteraction() {
      if (!['favorite', 'history', 'follow'].includes(this.activeModule)) return
      this.panelLoading = true
      try {
        const body = await clearBuyerItems(this.activeModule)
        if (body.code === 0 && body.data) {
          this.center = body.data
          this.interactionItems = []
          uni.showToast({ title: '\u5df2\u6e05\u7a7a', icon: 'none' })
        }
      } finally {
        this.panelLoading = false
      }
    },
    async submitRealNameForm() {
      if (!this.realNameForm.realName || !this.realNameForm.idCard) {
        uni.showToast({ title: '\u8bf7\u586b\u5199\u59d3\u540d\u548c\u8bc1\u4ef6\u53f7', icon: 'none' })
        return
      }
      this.submittingRealName = true
      try {
        const body = await submitRealName(this.realNameForm)
        if (body.code === 0 && body.data) {
          this.center = body.data
          this.realNameForm.realName = ''
          this.realNameForm.idCard = ''
          uni.showToast({ title: '\u5df2\u63d0\u4ea4\u5ba1\u6838', icon: 'success' })
        }
      } catch (e) {
          uni.showToast({ title: '\u63d0\u4ea4\u5931\u8d25\uff0c\u8bf7\u7a0d\u540e\u91cd\u8bd5', icon: 'none' })
      } finally {
        this.submittingRealName = false
      }
    },
    async cancelRealNameForm() {
      if (!this.hasRealName) {
        uni.showToast({ title: '\u6682\u65e0\u53ef\u53d6\u6d88\u7684\u8ba4\u8bc1', icon: 'none' })
        return
      }
      this.cancelingRealName = true
      try {
        const body = await cancelRealName()
        if (body.code === 0 && body.data) {
          this.center = body.data
          this.realNameForm.realName = ''
          this.realNameForm.idCard = ''
          uni.showToast({ title: '\u5df2\u53d6\u6d88\u8ba4\u8bc1', icon: 'none' })
        }
      } catch (e) {
        uni.showToast({ title: '\u53d6\u6d88\u5931\u8d25\uff0c\u8bf7\u7a0d\u540e\u91cd\u8bd5', icon: 'none' })
      } finally {
        this.cancelingRealName = false
      }
    },
    goLogin() {
      uni.navigateTo({ url: '/pages/auth/login' })
    },
    goRegister() {
      uni.navigateTo({ url: '/pages/auth/register' })
    },
    goOrders() {
      uni.navigateTo({ url: '/pages/order/list' })
    },
    navTo(url) {
      uni.switchTab({ url })
    },
    enterRoleHome() {
      goRoleHome(this.user, 'reLaunch')
    },
    openMenu(item) {
      if (item.action === 'logout') {
        this.logout()
        return
      }
      if (item.type) {
        this.activeModule = this.activeModule === item.type ? '' : item.type
        if (this.activeModule) this.loadInteractionItems()
        return
      }
      uni.showToast({ title: item.title, icon: 'none' })
    },
    logout() {
      clearSession()
      this.loggedIn = false
      this.user = {}
      uni.showToast({ title: '\u5df2\u9000\u51fa\u767b\u5f55', icon: 'none' })
    }
  }
}
</script>

<style scoped>
.me-page {
  min-height: 100vh;
  background:
    radial-gradient(circle at 18% 5%, rgba(66, 133, 244, .08), transparent 28%),
    radial-gradient(circle at 78% 14%, rgba(251, 188, 5, .06), transparent 26%),
    linear-gradient(180deg, #ffffff 0%, #f7f8fa 100%);
  padding: 22px;
  padding-bottom: 84px;
  box-sizing: border-box;
  color: #17231d;
}
.me-wrap {
  max-width: 1200px;
  margin: 0 auto;
}
.topbar {
  position: sticky;
  top: 0;
  z-index: 10;
  margin: -22px -22px 22px;
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
.brand-sub {
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
  display: flex;
  justify-self: center;
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
  font-size: 13px;
  font-weight: 750;
}
.nav-link.on,
.nav-link:hover {
  background: #fff;
  background: linear-gradient(135deg, #ffffff, #f5f7fa);
  color: #12372a;
  box-shadow: 0 10px 26px rgba(18, 55, 42, .14);
}
.login-card,
.profile-card,
.section,
.menu-card,
.stat-card {
  background: #fff;
  border: 1px solid rgba(203, 213, 225, .72);
  border-radius: 8px;
  box-shadow: 0 18px 55px rgba(60, 64, 67, 0.075);
}
.login-card {
  max-width: 760px;
  margin: 120px auto 0;
  padding: 36px;
  text-align: center;
}
.login-title,
.login-desc,
.name,
.meta,
.chip,
.stat-value,
.stat-label,
.section-title,
.section-more,
.order-icon,
.order-label,
.menu-title,
.menu-desc {
  display: block;
}
.login-title {
  font-size: 30px;
  font-weight: 900;
}
.login-desc {
  margin-top: 12px;
  color: #667085;
  font-size: 15px;
}
.login-actions {
  display: flex;
  justify-content: center;
  gap: 14px;
  margin-top: 26px;
}
.primary,
.ghost {
  width: 180px;
  height: 46px;
  line-height: 46px;
  border-radius: 8px;
  font-weight: 900;
}
.primary {
  background: #12372a;
  color: #fff;
}
.ghost {
  background: #fff;
  color: #12372a;
  border: 1px solid rgba(95, 99, 104, .22);
}
.profile-card {
  display: flex;
  align-items: center;
  gap: 18px;
  padding: 28px;
  background:
    radial-gradient(circle at 88% 12%, rgba(66, 133, 244, .08), transparent 30%),
    radial-gradient(circle at 12% 88%, rgba(52, 168, 83, .05), transparent 30%),
    linear-gradient(135deg, #ffffff 0%, #f8faff 100%);
}
.avatar {
  width: 72px;
  height: 72px;
  border-radius: 8px;
  background: linear-gradient(135deg, #12372a, #1f5c43);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 30px;
  font-weight: 900;
}
.profile-main {
  flex: 1;
  min-width: 0;
}
.name {
  font-size: 24px;
  font-weight: 900;
}
.meta {
  margin-top: 6px;
  color: #667085;
  font-size: 14px;
}
.chips {
  display: flex;
  gap: 8px;
  margin-top: 12px;
}
.chip {
  padding: 6px 11px;
  border-radius: 999px;
  background: #f5f7fa;
  color: #12372a;
  font-size: 13px;
  font-weight: 800;
}
.stat-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
  margin-top: 16px;
}
.stat-card {
  padding: 20px;
}
.stat-card.clickable {
  cursor: pointer;
  transition: transform .2s ease, box-shadow .2s ease, border-color .2s ease;
}
.stat-card.clickable:hover {
  transform: translateY(-2px);
  border-color: rgba(18, 55, 42, .24);
  box-shadow: 0 18px 48px rgba(18, 55, 42, .1);
}
.stat-value {
  font-size: 22px;
  font-weight: 900;
  color: #12372a;
}
.stat-label {
  margin-top: 6px;
  color: #667085;
  font-size: 13px;
}
.stat-hint {
  display: block;
  margin-top: 10px;
  color: #8a94a6;
  font-size: 12px;
  line-height: 1.5;
}
.section {
  margin-top: 16px;
  padding: 22px;
}
.realname-panel {
  border-color: rgba(18, 55, 42, .18);
}
.interaction-panel {
  border-color: rgba(18, 55, 42, .16);
}
.section-head {
  display: flex;
  justify-content: space-between;
}
.section-title {
  font-size: 19px;
  font-weight: 900;
}
.panel-actions {
  display: flex;
  gap: 12px;
  margin-top: 18px;
  align-items: center;
}
.interaction-list {
  display: grid;
  gap: 10px;
  margin-top: 16px;
}
.interaction-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  padding: 14px 16px;
  border-radius: 8px;
  background: #f8faf9;
  border: 1px solid rgba(203, 213, 225, .72);
}
.interaction-title,
.interaction-desc {
  display: block;
}
.interaction-title {
  color: #12372a;
  font-size: 15px;
  font-weight: 900;
}
.interaction-desc {
  margin-top: 6px;
  color: #667085;
  font-size: 12px;
}
.interaction-type {
  padding: 6px 10px;
  border-radius: 999px;
  background: #e8f3ed;
  color: #1f5c43;
  font-size: 12px;
  font-weight: 900;
}
.empty-line {
  padding: 16px;
  border-radius: 8px;
  background: #f8faf9;
  color: #667085;
  font-size: 14px;
}
.realname-detail {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  margin-top: 18px;
}
.detail-row {
  padding: 14px 16px;
  border-radius: 8px;
  background: #f8faf9;
  border: 1px solid rgba(203, 213, 225, .72);
}
.detail-label,
.detail-value {
  display: block;
}
.detail-label {
  color: #667085;
  font-size: 12px;
}
.detail-value {
  margin-top: 8px;
  color: #12372a;
  font-size: 16px;
  font-weight: 900;
}
.realname-form {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1.3fr) 120px;
  gap: 12px;
  margin-top: 18px;
  align-items: center;
}
.realname-input {
  height: 42px;
  padding: 0 14px;
  border-radius: 8px;
  border: 1px solid rgba(203, 213, 225, .9);
  background: #fff;
  box-sizing: border-box;
  font-size: 14px;
}
.primary.small {
  width: 120px;
  height: 42px;
  line-height: 42px;
  font-size: 13px;
}
.realname-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}
.ghost.small {
  width: 120px;
  height: 38px;
  line-height: 38px;
  font-size: 13px;
}
.ghost.small[disabled] {
  opacity: .45;
}
.section-more {
  color: #12372a;
  font-size: 14px;
  font-weight: 800;
}
.order-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
  margin-top: 18px;
}
.order-item {
  text-align: center;
}
.order-icon {
  color: #12372a;
  font-size: 22px;
  font-weight: 900;
}
.order-label {
  margin-top: 7px;
  color: #667085;
  font-size: 13px;
}
.menu-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
  margin-top: 16px;
}
.menu-card {
  padding: 18px;
}
.menu-title {
  font-size: 16px;
  font-weight: 900;
}
.menu-desc {
  margin-top: 7px;
  color: #667085;
  font-size: 13px;
}
@media screen and (max-width: 900px) {
  .topbar-inner {
    align-items: flex-start;
    flex-direction: column;
  }
  .web-nav,
  .brand-sub {
    display: none;
  }
  .stat-grid,
  .order-grid,
  .menu-grid,
  .realname-detail,
  .realname-form {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
  .realname-form .primary.small {
    width: 100%;
  }
}
</style>
