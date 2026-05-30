<template>
  <view class="role-app">
    <view class="top">
      <view class="brand">
        <view class="mark">&#21334;</view>
        <view>
          <text class="brand-title">&#21334;&#23478;&#24037;&#20316;&#21488;</text>
          <text class="brand-sub">{{ user.username || 'seller' }} · {{ user.phoneMasked || '137****0000' }}</text>
        </view>
      </view>
      <button class="top-action" @click="goPublish">&#21457;&#24067;&#21830;&#21697;</button>
    </view>

    <view class="role-nav">
      <view v-for="item in tabs" :key="item.key" class="role-nav-item" :class="{ on: active === item.key }" @click="active = item.key">
        <text>{{ item.label }}</text>
      </view>
    </view>

    <view v-if="active === 'home'" class="content">
      <view class="hero">
        <view>
          <text class="hero-kicker">SELLER MODE</text>
          <text class="hero-title">&#20170;&#26085;&#24215;&#38138;&#27010;&#35272;</text>
          <text class="hero-copy">&#20320;&#24050;&#36827;&#20837;&#21334;&#23478;&#31471;&#65292;&#21487;&#31649;&#29702;&#21830;&#21697;&#12289;&#35746;&#21333;&#12289;&#21806;&#21518;&#21644;&#24215;&#38138;&#20449;&#29992;&#12290;</text>
        </view>
      </view>
      <view class="stat-grid">
        <view v-for="item in stats" :key="item.title || item.label" class="stat-card">
          <text class="stat-value">{{ item.value }}</text>
          <text class="stat-label">{{ item.title || item.label }}</text>
        </view>
      </view>
      <view class="section">
        <text class="section-title">&#24453;&#22788;&#29702;</text>
        <view v-for="item in todos" :key="item" class="line">{{ item }}</view>
      </view>
      <view class="section subtle-section">
        <text class="section-title">&#36817;&#26399;&#21160;&#24577;</text>
        <view class="line">&#20170;&#26085;&#26032;&#22686; 14 &#26465;&#21672;&#35810;&#65292;&#25968;&#30721;&#24433;&#38899;&#31867;&#30446;&#26368;&#27963;&#36291;</view>
        <view class="line">&#24314;&#35758;&#32473; ViewTop 27 &#34917;&#20805;&#25509;&#21475;&#21644;&#23631;&#24149;&#28857;&#20142;&#29031;&#29255;</view>
      </view>
    </view>

    <view v-if="active === 'products'" class="content">
      <view class="section-head">
        <text class="page-title">&#21830;&#21697;&#31649;&#29702;</text>
        <text class="link" @click="goPublish">&#26032;&#22686;&#21457;&#24067;</text>
      </view>
      <view v-for="item in products" :key="item.title" class="list-card">
        <view>
          <text class="item-title">{{ item.title }}</text>
          <text class="item-desc">{{ item.desc }}</text>
        </view>
        <text class="pill">{{ item.status }}</text>
      </view>
    </view>

    <view v-if="active === 'orders'" class="content">
      <text class="page-title">&#35746;&#21333;&#21806;&#21518;</text>
      <view v-for="item in orders" :key="item.title" class="list-card">
        <view>
          <text class="item-title">{{ item.title }}</text>
          <text class="item-desc">{{ item.buyer }} · {{ item.price }}</text>
        </view>
        <text class="pill warn">{{ item.status }}</text>
      </view>
    </view>

    <view v-if="active === 'me'" class="content">
      <view class="profile-card">
        <view class="avatar">S</view>
        <view>
          <text class="item-title">{{ user.username || 'seller' }}</text>
          <text class="item-desc">{{ user.phoneMasked || '137****0000' }} · {{ verifiedText }}</text>
        </view>
      </view>
      <view class="menu-grid">
        <view v-for="item in centerModules" :key="item.title" class="menu-card">
          <text class="menu-title">{{ item.title }}</text>
          <text class="item-desc">{{ item.value }} · {{ item.desc }}</text>
        </view>
        <view class="menu-card clickable" @click="toggleRealNamePanel">
          <text class="menu-title">实名认证</text>
          <text class="item-desc">{{ verifiedText }} · 点开查看脱敏资料</text>
        </view>
        <view class="menu-card" @click="logout"><text class="menu-title">&#36864;&#20986;&#30331;&#24405;</text><text class="item-desc">&#22238;&#21040;&#20080;&#23478;&#40664;&#35748;&#31471;</text></view>
      </view>
      <view v-if="showRealNamePanel" class="section realname-panel">
        <view class="section-head">
          <text class="section-title">实名认证模拟</text>
          <text class="link">{{ verifiedText }}</text>
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
          <button class="top-action form-action" :loading="submittingRealName" @click="submitRealNameForm">提交实名</button>
        </view>
        <view class="realname-actions">
          <button class="cancel-action" :disabled="!hasRealName" :loading="cancelingRealName" @click="cancelRealNameForm">取消认证</button>
        </view>
      </view>
    </view>

    <view class="role-tabbar">
      <view v-for="item in tabs" :key="item.key" class="tab" :class="{ on: active === item.key }" @click="active = item.key">
        <text>{{ item.label }}</text>
      </view>
    </view>
  </view>
</template>

<script>
import { clearSession, getCachedUser } from '@/utils/auth.js'
import { cancelSellerRealName, fetchSellerCenter, submitSellerRealName } from '@/services/center.js'

export default {
  data() {
    return {
      active: 'home',
      user: {},
      center: {},
      showRealNamePanel: false,
      submittingRealName: false,
      cancelingRealName: false,
      realNameForm: {
        realName: '',
        idCard: ''
      },
      tabs: [
        { key: 'home', label: '\u5de5\u4f5c\u53f0' },
        { key: 'products', label: '\u5546\u54c1' },
        { key: 'orders', label: '\u8ba2\u5355' },
        { key: 'me', label: '\u6211\u7684' }
      ],
      stats: [],
      todos: ['3 \u4e2a\u8ba2\u5355\u5f85\u53d1\u8d27', '1 \u4e2a\u552e\u540e\u5f85\u56de\u590d', '2 \u4ef6\u5546\u54c1\u5efa\u8bae\u8865\u5145\u56fe\u7247'],
      products: [
        { title: 'AirWave Pro', desc: '\u6570\u7801\u5f71\u97f3 · 699', status: '\u5728\u552e' },
        { title: 'ViewTop 27', desc: '\u6570\u7801\u5f71\u97f3 · 680', status: '\u5f85\u5ba1\u6838' },
        { title: 'Songuo Pad 11', desc: '\u56fe\u4e66\u6587\u521b · 2299', status: '\u5728\u552e' }
      ],
      orders: [
        { title: 'AirWave Pro', buyer: '\u4e70\u5bb6 A', price: '699', status: '\u5f85\u53d1\u8d27' },
        { title: 'ViewTop 27', buyer: '\u4e70\u5bb6 B', price: '680', status: '\u552e\u540e' }
      ]
    }
  },
  computed: {
    centerModules() {
      return this.center.modules || []
    },
    realNameInfo() {
      return this.center.realName || {}
    },
    hasRealName() {
      return !!(this.realNameInfo && this.realNameInfo.id)
    },
    verifiedText() {
      const status = this.realNameInfo.status
      if (status === 'approved') return '已实名'
      if (status === 'pending') return '待审核'
      if (status === 'rejected') return '已驳回'
      return '未实名'
    }
  },
  onShow() {
    this.user = getCachedUser() || {}
    this.loadCenter()
  },
  methods: {
    async loadCenter() {
      try {
        const body = await fetchSellerCenter()
        if (body.code === 0 && body.data) {
          this.center = body.data
          this.stats = body.data.modules || []
        }
      } catch (e) {
        this.stats = [
          { title: '\u5e97\u94fa\u4fe1\u606f', value: '--', desc: '\u7b49\u5f85\u540e\u7aef\u8fde\u63a5' },
          { title: '\u5546\u54c1\u6982\u89c8', value: '--', desc: '\u6570\u636e\u5e93 goods' },
          { title: '\u8ba2\u5355\u6982\u89c8', value: '--', desc: '\u6570\u636e\u5e93 orders' },
          { title: '\u5e97\u94fa\u4fe1\u7528', value: this.user.credit || 100, desc: '\u5e97\u94fa\u7ecf\u8425\u4fe1\u7528' }
        ]
      }
    },
    toggleRealNamePanel() {
      this.showRealNamePanel = !this.showRealNamePanel
    },
    async submitRealNameForm() {
      if (!this.realNameForm.realName || !this.realNameForm.idCard) {
        uni.showToast({ title: '请填写姓名和证件号', icon: 'none' })
        return
      }
      this.submittingRealName = true
      try {
        const body = await submitSellerRealName(this.realNameForm)
        if (body.code === 0 && body.data) {
          this.center = body.data
          this.realNameForm.realName = ''
          this.realNameForm.idCard = ''
          uni.showToast({ title: '已提交审核', icon: 'success' })
        }
      } catch (e) {
        uni.showToast({ title: '提交失败，请稍后重试', icon: 'none' })
      } finally {
        this.submittingRealName = false
      }
    },
    async cancelRealNameForm() {
      if (!this.hasRealName) {
        uni.showToast({ title: '暂无可取消的认证', icon: 'none' })
        return
      }
      this.cancelingRealName = true
      try {
        const body = await cancelSellerRealName()
        if (body.code === 0 && body.data) {
          this.center = body.data
          this.realNameForm.realName = ''
          this.realNameForm.idCard = ''
          uni.showToast({ title: '已取消认证', icon: 'none' })
        }
      } catch (e) {
        uni.showToast({ title: '取消失败，请稍后重试', icon: 'none' })
      } finally {
        this.cancelingRealName = false
      }
    },
    goPublish() {
      uni.navigateTo({ url: '/pages/publish/publish' })
    },
    logout() {
      clearSession()
      uni.reLaunch({ url: '/pages/home/home' })
    }
  }
}
</script>

<style scoped>
@import './role-mobile.css';
</style>
