<template>
  <view class="page">
    <view class="shell">
      <view class="showcase">
        <view class="brand">
          <view class="logo">S</view>
          <view>
            <text class="brand-title">&#26494;&#26524;&#38598;&#24066;</text>
            <text class="brand-subtitle">&#21487;&#20449;&#20108;&#25163;&#20132;&#26131;&#24037;&#20316;&#21488;</text>
          </view>
        </view>

        <view class="hero">
          <text class="eyebrow">&#20998;&#35282;&#33394;&#30331;&#24405;</text>
          <text class="hero-title">&#19968;&#20010;&#20837;&#21475;&#65292;&#36827;&#20837;&#20320;&#30340;&#19987;&#23646;&#24037;&#20316;&#21488;</text>
          <text class="hero-copy">
            &#20080;&#23478;&#12289;&#21334;&#23478;&#21644;&#31649;&#29702;&#21592;&#20849;&#29992;&#19968;&#22871;&#30331;&#24405;&#20837;&#21475;&#12290;&#31995;&#32479;&#20250;&#26681;&#25454;&#36134;&#21495;&#35282;&#33394;&#33258;&#21160;&#36339;&#36716;&#21040;&#23545;&#24212;&#39029;&#38754;&#12290;
          </text>
        </view>

        <view class="proof-grid">
          <view class="proof">
            <text class="proof-value">&#20080;&#23478;</text>
            <text class="proof-label">&#25910;&#34255;&#12289;&#36275;&#36857;&#12289;&#20449;&#29992;&#20998;</text>
          </view>
          <view class="proof">
            <text class="proof-value">&#21334;&#23478;</text>
            <text class="proof-label">&#21830;&#21697;&#12289;&#35746;&#21333;&#12289;&#21806;&#21518;</text>
          </view>
          <view class="proof">
            <text class="proof-value">&#31649;&#29702;&#21592;</text>
            <text class="proof-label">&#23457;&#26680;&#12289;&#29992;&#25143;&#12289;&#39118;&#25511;</text>
          </view>
        </view>
      </view>

      <view class="card">
        <view class="card-head">
          <text class="card-kicker">&#27426;&#36814;&#22238;&#26469;</text>
          <text class="card-title">&#36134;&#21495;&#30331;&#24405;</text>
          <text class="card-copy">&#20808;&#36873;&#25321;&#30331;&#24405;&#36523;&#20221;&#65292;&#20877;&#36755;&#20837;&#36134;&#21495;&#23494;&#30721;&#12290;&#28857;&#20987;&#35282;&#33394;&#21345;&#29255;&#21487;&#24555;&#36895;&#22635;&#20837;&#28436;&#31034;&#36134;&#21495;&#12290;</text>
        </view>

        <view class="role-grid">
          <view
            v-for="item in roles"
            :key="item.role"
            class="role-card"
            :class="{ selected: selectedRole === item.role }"
            @click="selectRole(item)"
          >
            <text class="role-title">{{ item.label }}</text>
            <text class="role-text">{{ item.text }}</text>
          </view>
        </view>

        <view class="field">
          <text class="label">&#36134;&#21495;</text>
          <input v-model="username" class="input" :placeholder="usernamePlaceholder" />
        </view>

        <view class="field">
          <text class="label">&#23494;&#30721;</text>
          <input v-model="password" class="input" password :placeholder="passwordPlaceholder" />
        </view>

        <button class="submit" :loading="loading" @click="submit">
          &#30331;&#24405;&#24182;&#36827;&#20837;{{ selectedRoleLabel }}
        </button>

        <view class="card-actions">
          <text @click="goReg">&#27880;&#20876;&#20080;&#23478; / &#21334;&#23478;&#36134;&#21495;</text>
          <text @click="fillCurrentPreset">&#22635;&#20837;&#28436;&#31034;&#36134;&#21495;</text>
        </view>

        <view class="demo-box">
          <text>&#20080;&#23478;&#65306;demo / demo123</text>
          <text>&#21334;&#23478;&#65306;seller / seller123</text>
          <text>&#31649;&#29702;&#21592;&#65306;admin / admin123</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { loginByPassword } from '@/services/auth.js'
import { setSession, pickErrorMessage, goRoleHome } from '@/utils/auth.js'

export default {
  data() {
    return {
      username: '',
      password: '',
      selectedRole: 'buyer',
      loading: false,
      roles: [
        { role: 'buyer', label: '\u4e70\u5bb6', text: '\u4e2a\u4eba\u8d2d\u7269\u4e2d\u5fc3', username: 'demo', password: 'demo123' },
        { role: 'seller', label: '\u5356\u5bb6', text: '\u5e97\u94fa\u7ecf\u8425\u5de5\u4f5c\u53f0', username: 'seller', password: 'seller123' },
        { role: 'admin', label: '\u7ba1\u7406\u5458', text: '\u5e73\u53f0\u7ba1\u7406\u540e\u53f0', username: 'admin', password: 'admin123' }
      ]
    }
  },
  computed: {
    usernamePlaceholder() {
      return '\u8bf7\u8f93\u5165\u7528\u6237\u540d'
    },
    passwordPlaceholder() {
      return '\u8bf7\u8f93\u5165\u5bc6\u7801'
    },
    selectedRoleLabel() {
      const found = this.roles.find(item => item.role === this.selectedRole)
      return found ? found.label : 'Workspace'
    }
  },
  mounted() {
    this.fillCurrentPreset()
  },
  methods: {
    selectRole(item) {
      this.selectedRole = item.role
      if (item.username) {
        this.username = item.username
        this.password = item.password
        return
      }
      this.username = ''
      this.password = ''
    },
    fillCurrentPreset() {
      const found = this.roles.find(item => item.role === this.selectedRole)
      if (found && found.username) {
        this.username = found.username
        this.password = found.password
        return
      }
      uni.showToast({ title: '\u5df2\u586b\u5165\u6f14\u793a\u8d26\u53f7', icon: 'none' })
    },
    goReg() {
      uni.navigateTo({ url: '/pages/auth/register' })
    },
    async submit() {
      if (!this.username.trim() || !this.password) {
        uni.showToast({ title: '\u8bf7\u8f93\u5165\u8d26\u53f7\u548c\u5bc6\u7801', icon: 'none' })
        return
      }
      this.loading = true
      try {
        const body = await loginByPassword({ username: this.username.trim(), password: this.password })
        if (body.code !== 0) {
          uni.showToast({ title: body.message || '\u767b\u5f55\u5931\u8d25', icon: 'none' })
          return
        }
        const { token, user } = body.data
        setSession(token, user)
        uni.showToast({ title: `${user.roleLabel || '\u7528\u6237'}\u767b\u5f55\u6210\u529f`, icon: 'success' })
        setTimeout(() => goRoleHome(user, 'reLaunch'), 350)
      } catch (e) {
        uni.showToast({ title: pickErrorMessage(e), icon: 'none' })
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: linear-gradient(180deg, #f7faf8 0%, #eef3ef 100%);
  padding: 48px;
  box-sizing: border-box;
  color: #111d17;
}
.shell {
  max-width: 1180px;
  min-height: 680px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: minmax(0, 1fr) 440px;
  gap: 24px;
}
.showcase,
.card {
  border-radius: 8px;
  box-sizing: border-box;
  box-shadow: 0 24px 80px rgba(17, 38, 28, 0.12);
}
.showcase {
  background: linear-gradient(135deg, #12231a 0%, #1f5c43 100%);
  color: #fff;
  padding: 44px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}
.card {
  background: #fff;
  border: 1px solid #dfe7e1;
  padding: 40px;
}
.brand {
  display: flex;
  align-items: center;
  gap: 14px;
}
.logo {
  width: 52px;
  height: 52px;
  border-radius: 8px;
  background: #fff;
  color: #123629;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  font-weight: 900;
}
.brand-title,
.brand-subtitle,
.eyebrow,
.hero-title,
.hero-copy,
.proof-value,
.proof-label,
.card-kicker,
.card-title,
.card-copy,
.role-title,
.role-text,
.label,
.demo-box text {
  display: block;
}
.brand-title {
  font-size: 20px;
  font-weight: 900;
}
.brand-subtitle {
  margin-top: 3px;
  font-size: 13px;
  opacity: 0.72;
}
.eyebrow {
  font-size: 13px;
  font-weight: 900;
  opacity: 0.72;
}
.hero-title {
  margin-top: 18px;
  max-width: 620px;
  font-size: 54px;
  font-weight: 900;
  line-height: 1.08;
}
.hero-copy {
  margin-top: 20px;
  max-width: 620px;
  font-size: 17px;
  line-height: 1.8;
  opacity: 0.82;
}
.proof-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}
.proof {
  padding: 18px;
  border-radius: 8px;
  background: rgba(255,255,255,0.1);
  border: 1px solid rgba(255,255,255,0.14);
}
.proof-value {
  font-size: 18px;
  font-weight: 900;
}
.proof-label {
  margin-top: 8px;
  font-size: 13px;
  line-height: 1.45;
  opacity: 0.74;
}
.card-kicker {
  color: #1f5c43;
  font-size: 13px;
  font-weight: 900;
}
.card-title {
  margin-top: 8px;
  font-size: 34px;
  font-weight: 900;
}
.card-copy {
  margin-top: 10px;
  color: #667085;
  font-size: 14px;
  line-height: 1.7;
}
.role-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
  margin-top: 26px;
}
.role-card {
  min-height: 92px;
  padding: 14px 12px;
  border: 1px solid #dce6df;
  border-radius: 8px;
  background: #f8faf8;
  box-sizing: border-box;
  cursor: pointer;
}
.role-card.selected {
  background: #123629;
  color: #fff;
  border-color: #123629;
}
.role-title {
  font-size: 17px;
  font-weight: 900;
}
.role-text {
  margin-top: 8px;
  color: #667085;
  font-size: 12px;
  line-height: 1.4;
}
.role-card.selected .role-text {
  color: rgba(255,255,255,0.76);
}
.field {
  margin-top: 22px;
}
.label {
  margin-bottom: 9px;
  color: #34453b;
  font-size: 14px;
  font-weight: 900;
}
.input {
  width: 100%;
  height: 52px;
  padding: 0 15px;
  border: 1px solid #dce6df;
  border-radius: 8px;
  background: #f8faf8;
  box-sizing: border-box;
  font-size: 16px;
}
.submit {
  width: 100%;
  height: 54px;
  line-height: 54px;
  margin-top: 28px;
  border-radius: 8px;
  background: #1f5c43;
  color: #fff;
  font-size: 16px;
  font-weight: 900;
}
.card-actions {
  display: flex;
  justify-content: space-between;
  margin-top: 18px;
  color: #1f5c43;
  font-size: 14px;
}
.demo-box {
  margin-top: 24px;
  padding-top: 18px;
  border-top: 1px solid #edf1ee;
  color: #667085;
  font-size: 13px;
  line-height: 1.8;
}
@media screen and (max-width: 980px) {
  .page {
    padding: 18px;
  }
  .shell {
    display: block;
  }
  .showcase,
  .card {
    padding: 28px;
  }
  .card {
    margin-top: 18px;
  }
  .hero-title {
    font-size: 38px;
  }
  .proof-grid,
  .role-grid {
    grid-template-columns: 1fr;
  }
}
</style>
