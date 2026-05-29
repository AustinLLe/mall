<template>
  <view class="page">
    <view class="shell">
      <view class="intro">
        <text class="eyebrow">&#21019;&#24314;&#36134;&#21495;</text>
        <text class="title">&#36873;&#25321;&#36523;&#20221;&#65292;&#24320;&#22987;&#20351;&#29992;&#26494;&#26524;&#38598;&#24066;</text>
        <text class="copy">&#27492;&#22788;&#21482;&#24320;&#25918;&#20080;&#23478;&#21644;&#21334;&#23478;&#27880;&#20876;&#12290;&#31649;&#29702;&#21592;&#36134;&#21495;&#30001;&#31995;&#32479;&#39044;&#32622;&#12290;</text>
        <view class="admin-note">
          <text class="note-title">&#31649;&#29702;&#21592;&#20837;&#21475;</text>
          <text class="note-copy">&#35831;&#22312;&#30331;&#24405;&#39029;&#20351;&#29992; admin / admin123&#12290;&#31649;&#29702;&#21592;&#19981;&#25903;&#25345;&#33258;&#21161;&#27880;&#20876;&#12290;</text>
        </view>
      </view>

      <view class="form-card">
        <view class="role-grid">
          <view class="role-card" :class="{ selected: role === 'buyer' }" @click="role = 'buyer'">
            <text class="role-title">&#20080;&#23478;&#36134;&#21495;</text>
            <text class="role-copy">&#31649;&#29702;&#35746;&#21333;&#12289;&#25910;&#34255;&#12289;&#36275;&#36857;&#21644;&#20449;&#29992;&#20998;&#12290;</text>
          </view>
          <view class="role-card" :class="{ selected: role === 'seller' }" @click="role = 'seller'">
            <text class="role-title">&#21334;&#23478;&#36134;&#21495;</text>
            <text class="role-copy">&#21457;&#24067;&#21830;&#21697;&#12289;&#32463;&#33829;&#24215;&#38138;&#12289;&#22788;&#29702;&#35758;&#20215;&#19982;&#21806;&#21518;&#12290;</text>
          </view>
        </view>

        <view class="field">
          <text class="label">&#36134;&#21495;</text>
          <input v-model="username" class="input" :placeholder="usernamePlaceholder" />
        </view>
        <view class="field">
          <text class="label">&#25163;&#26426;&#21495;</text>
          <input v-model="phone" class="input" :placeholder="phonePlaceholder" />
        </view>
        <view class="field">
          <text class="label">&#23494;&#30721;</text>
          <input v-model="password" class="input" password :placeholder="passwordPlaceholder" />
        </view>

        <button class="submit" :loading="loading" @click="submit">&#21019;&#24314;{{ roleLabel }}&#36134;&#21495;</button>

        <view class="footer">
          <text @click="goLogin">&#36820;&#22238;&#30331;&#24405;</text>
          <text>&#24403;&#21069;&#36873;&#25321;&#65306;{{ roleLabel }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { registerByPassword } from '@/services/auth.js'
import { setSession, pickErrorMessage, goRoleHome } from '@/utils/auth.js'

export default {
  data() {
    return {
      username: '',
      phone: '',
      password: '',
      role: 'buyer',
      loading: false
    }
  },
  computed: {
    roleLabel() {
      return this.role === 'seller' ? '\u5356\u5bb6' : '\u4e70\u5bb6'
    },
    usernamePlaceholder() {
      return '\u81f3\u5c11 3 \u4f4d'
    },
    phonePlaceholder() {
      return '\u7528\u4e8e\u5b9e\u540d\u6a21\u62df\uff0c\u4f8b\u5982 18800001111'
    },
    passwordPlaceholder() {
      return '\u81f3\u5c11 6 \u4f4d'
    }
  },
  methods: {
    goLogin() {
      uni.navigateTo({ url: '/pages/auth/login' })
    },
    async submit() {
      if (!this.username.trim() || !this.phone.trim() || !this.password) {
        uni.showToast({ title: '\u8bf7\u586b\u5199\u5b8c\u6574\u4fe1\u606f', icon: 'none' })
        return
      }
      this.loading = true
      try {
        const body = await registerByPassword({
          username: this.username.trim(),
          phone: this.phone.trim(),
          password: this.password,
          role: this.role
        })
        if (body.code !== 0) {
          uni.showToast({ title: body.message || '\u6ce8\u518c\u5931\u8d25', icon: 'none' })
          return
        }
        const { token, user } = body.data
        setSession(token, user)
        uni.showToast({ title: '\u6ce8\u518c\u6210\u529f', icon: 'success' })
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
  max-width: 1080px;
  min-height: 620px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: 390px minmax(0, 1fr);
  gap: 24px;
}
.intro,
.form-card {
  border-radius: 8px;
  box-sizing: border-box;
  box-shadow: 0 24px 80px rgba(17, 38, 28, 0.12);
}
.intro {
  background: linear-gradient(135deg, #12231a 0%, #1f5c43 100%);
  color: #fff;
  padding: 42px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}
.form-card {
  background: #fff;
  border: 1px solid #dfe7e1;
  padding: 42px;
}
.eyebrow,
.title,
.copy,
.note-title,
.note-copy,
.role-title,
.role-copy,
.label {
  display: block;
}
.eyebrow {
  font-size: 13px;
  font-weight: 900;
  opacity: 0.72;
}
.title {
  margin-top: 18px;
  font-size: 42px;
  font-weight: 900;
  line-height: 1.12;
}
.copy {
  margin-top: 18px;
  font-size: 16px;
  line-height: 1.75;
  opacity: 0.82;
}
.admin-note {
  margin-top: 32px;
  padding: 20px;
  border: 1px solid rgba(255,255,255,0.14);
  border-radius: 8px;
  background: rgba(255,255,255,0.1);
}
.note-title {
  font-size: 17px;
  font-weight: 900;
}
.note-copy {
  margin-top: 8px;
  font-size: 13px;
  line-height: 1.6;
  opacity: 0.78;
}
.role-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}
.role-card {
  min-height: 140px;
  padding: 24px;
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
  font-size: 24px;
  font-weight: 900;
}
.role-copy {
  margin-top: 12px;
  color: #667085;
  font-size: 14px;
  line-height: 1.65;
}
.role-card.selected .role-copy {
  color: rgba(255,255,255,0.78);
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
.footer {
  display: flex;
  justify-content: space-between;
  margin-top: 18px;
  color: #1f5c43;
  font-size: 14px;
}
@media screen and (max-width: 900px) {
  .page {
    padding: 18px;
  }
  .shell {
    display: block;
  }
  .intro,
  .form-card {
    padding: 28px;
  }
  .form-card {
    margin-top: 18px;
  }
  .role-grid {
    grid-template-columns: 1fr;
  }
}
</style>
