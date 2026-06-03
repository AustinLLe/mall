<template>
  <view class="role-app">
    <view class="top">
      <view class="brand">
        <view class="mark admin-mark">&#31649;</view>
        <view>
          <text class="brand-title">&#31649;&#29702;&#21592;&#24037;&#20316;&#21488;</text>
          <text class="brand-sub">{{ user.username || 'admin' }} · {{ user.phoneMasked || '139****0000' }}</text>
        </view>
      </view>
      <button class="top-action admin-action" @click="openProductAudit">&#21830;&#21697;&#23457;&#26680;</button>
    </view>

    <view class="role-nav">
      <view v-for="item in tabs" :key="item.key" class="role-nav-item" :class="{ on: active === item.key }" @click="active = item.key">
        <text>{{ item.label }}</text>
      </view>
    </view>

    <view v-if="active === 'home'" class="content">
      <view class="hero admin-hero">
        <view>
          <text class="hero-kicker">ADMIN MODE</text>
          <text class="hero-title">&#24179;&#21488;&#27010;&#35272;</text>
          <text class="hero-copy">&#20320;&#24050;&#36827;&#20837;&#31649;&#29702;&#21592;&#31471;&#65292;&#21487;&#22788;&#29702;&#23457;&#26680;&#12289;&#29992;&#25143;&#29366;&#24577;&#12289;&#23454;&#21517;&#27169;&#25311;&#21644;&#20449;&#29992;&#39118;&#25511;&#12290;</text>
        </view>
      </view>
      <view class="stat-grid">
        <view v-for="item in stats" :key="item.title || item.label" class="stat-card">
          <text class="stat-value">{{ item.value }}</text>
          <text class="stat-label">{{ item.title || item.label }}</text>
        </view>
      </view>
      <view class="section">
        <text class="section-title">&#39118;&#25511;&#25552;&#37266;</text>
        <view v-for="item in alerts" :key="item" class="line">{{ item }}</view>
      </view>
      <view class="section subtle-section">
        <text class="section-title">&#24179;&#21488;&#21160;&#24577;</text>
        <view class="line">&#26032;&#27880;&#20876;&#20080;&#23478; 12 &#20154;&#65292;&#21334;&#23478; 3 &#20154;</view>
        <view class="line">&#20170;&#26085;&#23457;&#26680;&#36890;&#36807;&#29575; 87%&#65292;&#39118;&#38505;&#39033;&#30446;&#24050;&#26631;&#35760;</view>
      </view>
    </view>

    <view v-if="active === 'audit'" class="content">
      <view class="section-head">
        <text class="page-title">&#23457;&#26680;&#31649;&#29702;</text>
        <text class="section-more" @click="openProductAudit">&#21830;&#21697;&#23457;&#26680; {{ productAuditList.length }}</text>
      </view>
      <text class="audit-group-title">&#24453;&#23457;&#21830;&#21697;</text>
      <view v-if="!productAuditList.length" class="line muted">&#26242;&#26080;&#24453;&#23457;&#26680;&#21830;&#21697;</view>
      <view v-for="item in productAuditList" :key="'goods-' + item.id" class="list-card" @click="openProductAudit">
        <view>
          <text class="item-title">{{ item.title }}</text>
          <text class="item-desc">{{ item.publisherName || item.shopName || '卖家' }} · ¥{{ item.price }} · {{ item.category }}</text>
        </view>
        <text class="pill warn">&#24453;&#23457;&#26680;</text>
      </view>
      <text class="audit-group-title">&#23454;&#21517;&#35748;&#35777;</text>
      <view v-if="!auditList.length" class="line muted">&#26242;&#26080;&#24453;&#23457;&#26680;&#23454;&#21517;</view>
      <view v-for="item in auditList" :key="'real-' + item.id" class="list-card">
        <view>
          <text class="item-title">{{ item.username || item.title }}</text>
          <text class="item-desc">{{ item.realName || item.seller }} · {{ item.idCardMasked || item.risk }}</text>
        </view>
        <view class="inline-actions" v-if="item.id">
          <text class="pill" @click="approve(item.id)">通过</text>
          <text class="pill warn" @click="reject(item.id)">驳回</text>
        </view>
        <text v-else class="pill warn">&#24453;&#22788;&#29702;</text>
      </view>
    </view>

    <view v-if="active === 'users'" class="content">
      <text class="page-title">&#29992;&#25143;&#29366;&#24577;</text>
      <view v-for="item in users" :key="item.userId || item.name" class="list-card">
        <view>
          <text class="item-title">{{ item.username || item.name }}</text>
          <text class="item-desc">{{ item.role }} · {{ item.status }}</text>
        </view>
        <view class="inline-actions">
          <text class="pill">信用 {{ item.credit }}</text>
          <text v-if="canAdjustCredit(item)" class="pill credit-up" @click="adjustCredit(item, 5)">+5</text>
          <text v-if="canAdjustCredit(item)" class="pill credit-down" @click="adjustCredit(item, -5)">-5</text>
          <text v-if="item.userId" class="pill warn" @click="toggleStatus(item)">{{ item.status === 'disabled' ? '解冻' : '冻结' }}</text>
          <text v-if="canDeleteUser(item)" class="pill danger" @click="removeUser(item)">删除</text>
        </view>
      </view>
    </view>

    <view v-if="active === 'me'" class="content">
      <view class="profile-card">
        <view class="avatar admin-mark">A</view>
        <view>
          <text class="item-title">{{ user.username || 'admin' }}</text>
          <text class="item-desc">{{ user.phoneMasked || '139****0000' }} · Admin</text>
        </view>
      </view>
      <view class="menu-grid">
        <view v-for="item in stats" :key="item.title || item.label" class="menu-card">
          <text class="menu-title">{{ item.title || item.label }}</text>
          <text class="item-desc">{{ item.value }} · {{ item.desc || '数据库统计' }}</text>
        </view>
        <view class="menu-card" @click="logout"><text class="menu-title">&#36864;&#20986;&#30331;&#24405;</text><text class="item-desc">&#22238;&#21040;&#20080;&#23478;&#40664;&#35748;&#31471;</text></view>
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
import { adjustUserCredit, approveRealName, deleteUser, fetchAdminCenter, rejectRealName, updateUserStatus } from '@/services/center.js'
import { fetchAuditItems } from '@/services/shop.js'

export default {
  data() {
    return {
      active: 'home',
      user: {},
      center: {},
      tabs: [
        { key: 'home', label: '\u6982\u89c8' },
        { key: 'audit', label: '\u5ba1\u6838' },
        { key: 'users', label: '\u7528\u6237' },
        { key: 'me', label: '\u6211\u7684' }
      ],
      stats: [],
      alerts: ['9 \u4ef6\u5546\u54c1\u7b49\u5f85\u5ba1\u6838', '2 \u4e2a\u7528\u6237\u72b6\u6001\u9700\u590d\u6838', '1 \u6761\u552e\u540e\u7ea0\u7eb7\u8fdb\u5165\u5e73\u53f0\u534f\u5546'],
      auditList: [],
      productAuditList: [],
      users: [
        { name: 'demo', role: '\u4e70\u5bb6', status: '\u6b63\u5e38', credit: 100 },
        { name: 'seller', role: '\u5356\u5bb6', status: '\u5b9e\u540d\u6a21\u62df\u901a\u8fc7', credit: 100 },
        { name: 'admin', role: '\u7ba1\u7406\u5458', status: '\u7cfb\u7edf\u9884\u7f6e', credit: 100 }
      ]
    }
  },
  onShow() {
    this.user = getCachedUser() || {}
    this.loadCenter()
    this.loadProductAudits()
  },
  methods: {
    openProductAudit() {
      uni.navigateTo({ url: '/pages/admin/audit' })
    },
    async loadProductAudits() {
      try {
        const body = await fetchAuditItems()
        if (body && body.code === 0 && Array.isArray(body.data)) {
          this.productAuditList = body.data
        }
      } catch (e) {
        this.productAuditList = []
      }
    },
    async loadCenter() {
      try {
        const body = await fetchAdminCenter()
        if (body.code === 0 && body.data) {
          this.center = body.data
          this.stats = body.data.modules || []
          this.users = body.data.users || []
          this.auditList = body.data.realNameAudits || []
        }
      } catch (e) {
        this.stats = [
          { title: '\u5e73\u53f0\u603b\u89c8', value: '--', desc: '\u7b49\u5f85\u540e\u7aef\u8fde\u63a5' },
          { title: '\u7528\u6237\u7ba1\u7406', value: '--', desc: '\u6570\u636e\u5e93 users' },
          { title: '\u5b9e\u540d\u5ba1\u6838', value: '--', desc: '\u6570\u636e\u5e93 user_realname_auth' },
          { title: '\u5546\u54c1\u5e97\u94fa\u5ba1\u6838', value: '--', desc: '\u6570\u636e\u5e93 goods/store' }
        ]
      }
    },
    async toggleStatus(item) {
      const next = item.status === 'disabled' ? 'normal' : 'disabled'
      try {
        const body = await updateUserStatus(item.userId, next)
        if (body.code === 0 && body.data) {
          this.users = body.data.users || []
          this.stats = body.data.modules || this.stats
        }
      } catch (e) {
        uni.showToast({ title: '\u64cd\u4f5c\u5931\u8d25', icon: 'none' })
      }
    },
    canAdjustCredit(item) {
      return item.userId && ['buyer', 'seller', '\u4e70\u5bb6', '\u5356\u5bb6'].includes(item.role)
    },
    canDeleteUser(item) {
      const username = item.username || item.name
      return item.userId && !['demo', 'seller', 'admin'].includes(username)
    },
    async adjustCredit(item, value) {
      try {
        const reason = value > 0 ? '\u7ba1\u7406\u5458\u5956\u52b1\u4fe1\u7528\u5206' : '\u7ba1\u7406\u5458\u6263\u51cf\u4fe1\u7528\u5206'
        const body = await adjustUserCredit(item.userId, value, reason)
        if (body.code === 0 && body.data) {
          this.users = body.data.users || []
          this.stats = body.data.modules || this.stats
          uni.showToast({ title: value > 0 ? '\u5df2\u52a0\u5206' : '\u5df2\u6263\u5206', icon: 'none' })
        }
      } catch (e) {
        uni.showToast({ title: '\u4fe1\u7528\u5206\u8c03\u6574\u5931\u8d25', icon: 'none' })
      }
    },
    removeUser(item) {
      const username = item.username || item.name
      uni.showModal({
        title: '\u5220\u9664\u7528\u6237',
        content: `\u786e\u5b9a\u5220\u9664 ${username} \u5417\uff1f`,
        confirmText: '\u5220\u9664',
        confirmColor: '#b91c1c',
        success: async (res) => {
          if (!res.confirm) return
          try {
            const body = await deleteUser(item.userId)
            if (body.code === 0 && body.data) {
              this.users = body.data.users || []
              this.stats = body.data.modules || this.stats
              this.auditList = body.data.realNameAudits || this.auditList
              uni.showToast({ title: '\u5df2\u5220\u9664', icon: 'none' })
            }
          } catch (e) {
            uni.showToast({ title: '\u5220\u9664\u5931\u8d25', icon: 'none' })
          }
        }
      })
    },
    async approve(id) {
      const body = await approveRealName(id)
      if (body.code === 0 && body.data) {
        this.auditList = body.data.realNameAudits || []
        this.stats = body.data.modules || this.stats
      }
    },
    async reject(id) {
      const body = await rejectRealName(id, '\u4fe1\u606f\u4e0d\u5b8c\u6574\uff0c\u8bf7\u91cd\u65b0\u63d0\u4ea4')
      if (body.code === 0 && body.data) {
        this.auditList = body.data.realNameAudits || []
        this.stats = body.data.modules || this.stats
      }
    },
    logout() {
      clearSession()
      uni.reLaunch({ url: '/pages/home/home' })
    }
  }
}
</script>

<style scoped>
@import '../seller/role-mobile.css';
.admin-mark {
  background: #334155;
}
.admin-hero {
  background: #334155;
}
.admin-action {
  background: #334155;
}
.inline-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: flex-end;
}
.credit-up {
  background: rgba(22, 163, 74, 0.12);
  color: #15803d;
}
.credit-down {
  background: rgba(220, 38, 38, 0.10);
  color: #b91c1c;
}
.danger {
  background: rgba(185, 28, 28, 0.12);
  color: #991b1b;
}
.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}
.section-more {
  color: #1f5c43;
  font-size: 13px;
  font-weight: 700;
}
.audit-group-title {
  display: block;
  margin: 16px 0 10px;
  font-size: 14px;
  font-weight: 800;
  color: #17231d;
}
.line.muted {
  color: #667085;
  font-size: 13px;
  margin-bottom: 10px;
}
</style>
