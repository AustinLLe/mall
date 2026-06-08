<template>
  <view class="chat-container">
    <scroll-view class="message-list" scroll-y :scroll-top="scrollTop">
      <view v-if="messages.length === 0" class="empty-state">暂无消息，开始聊天吧</view>

      <view v-for="(msg, index) in messages" :key="msg.cmId || `${msg.senderId}-${msg.createTime}-${index}`">
        <view v-if="msg.showTime" class="time-divider">
          {{ formatTime(msg.createTime) }}
        </view>
        <view class="message-item" :class="msg.senderId === myUserId ? 'my-msg' : 'other-msg'">
          <view class="message-content-wrapper">
            <view class="bubble">
              <text>{{ msg.content }}</text>
            </view>
            <text v-if="msg.senderId === myUserId" class="read-status">{{ msg.isRead ? '已读' : '未读' }}</text>
          </view>
        </view>
      </view>
    </scroll-view>

    <view v-if="showAiBargainTip" class="ai-tip-bar">
      <text>对方提到了价格，是否启用 AI 议价助手？</text>
      <button class="ai-btn" @click="triggerAiBargain">启用 AI 议价</button>
    </view>

    <view class="bottom-fixed-area">
      <scroll-view class="quick-reply-bar" scroll-x>
        <view v-for="text in quickOptions" :key="text" class="quick-item" @click="sendQuick(text)">{{ text }}</view>
      </scroll-view>
      <view class="input-area">
        <input v-model="inputContent" confirm-type="send" placeholder="说点什么..." @confirm="sendMessage" />
        <button class="send-btn" :disabled="sending || !inputContent.trim()" @click="sendMessage">发送</button>
      </view>
    </view>
  </view>
</template>

<script>
import { fetchMe } from '@/services/auth.js'
import { get, post } from '@/utils/request.js'

export default {
  data() {
    return {
      covId: null,
      messages: [],
      inputContent: '',
      quickOptions: ['支持平台担保吗？', '是全新正品吗？', '是否包邮？', '最低多少钱？'],
      myUserId: null,
      pollTimer: null,
      showAiBargainTip: false,
      sending: false,
      scrollTop: 0
    }
  },
  onLoad(options) {
    this.covId = options && options.covId ? Number(options.covId) : null
    if (!this.covId) {
      uni.showToast({ title: '会话 ID 缺失', icon: 'none' })
      return
    }
    this.initChat()
  },
  onUnload() {
    this.disconnect()
  },
  methods: {
    async initChat() {
      try {
        await this.fetchUserInfo()
        await this.loadHistory()
        this.startPolling()
        setTimeout(() => this.sendReadReceipt(), 600)
      } catch (error) {
        console.error('聊天初始化失败', error)
        uni.showToast({ title: '聊天启动失败，请重试', icon: 'none' })
      }
    },
    async fetchUserInfo() {
      const res = await fetchMe()
      this.myUserId = res && res.data ? res.data.userId : null
    },
    async loadHistory() {
      const res = await get(`/api/chat/conversations/${this.covId}/messages`)
      const rawList = (res.data && res.data.data) ? res.data.data : []
      this.messages = this.processMessages(rawList)
      this.$nextTick(() => this.scrollToBottom())
    },
    async refreshHistory() {
      try {
        const before = this.messages.length
        const res = await get(`/api/chat/conversations/${this.covId}/messages`)
        const rawList = (res.data && res.data.data) ? res.data.data : []
        this.messages = this.processMessages(rawList)
        if (this.messages.length !== before) {
          this.$nextTick(() => this.scrollToBottom())
          this.detectBargainTip(this.messages[this.messages.length - 1])
        }
      } catch (e) {
        console.warn('刷新聊天记录失败', e)
      }
    },
    startPolling() {
      this.disconnect()
      this.pollTimer = setInterval(() => {
        this.refreshHistory()
      }, 3000)
    },
    disconnect() {
      if (this.pollTimer) {
        clearInterval(this.pollTimer)
        this.pollTimer = null
      }
    },
    sendReadReceipt() {
      if (!this.covId) return
      post(`/api/chat/${this.covId}/read`).catch((e) => {
        console.warn('标记已读失败', e)
      })
    },
    shouldShowTime(newMsg, prevMsg) {
      if (!prevMsg) return true
      const current = new Date(newMsg.createTime).getTime()
      const prev = new Date(prevMsg.createTime).getTime()
      return (current - prev) > 5 * 60 * 1000
    },
    processMessages(list) {
      return list.map((msg, index) => ({
        ...msg,
        showTime: index === 0 || this.shouldShowTime(msg, list[index - 1])
      }))
    },
    sendQuick(text) {
      this.inputContent = text
      this.sendMessage()
    },
    async sendMessage() {
      const content = this.inputContent.trim()
      if (!content || !this.covId || this.sending) return
      this.sending = true
      try {
        await post(`/api/chat/conversations/${this.covId}/messages`, {
          covId: this.covId,
          content,
          type: 'CHAT_MESSAGE'
        })
        this.inputContent = ''
        await this.refreshHistory()
        this.sendReadReceipt()
      } catch (error) {
        console.error('消息发送失败', error)
        uni.showToast({ title: '消息发送失败，请检查网络', icon: 'none' })
      } finally {
        this.sending = false
      }
    },
    async triggerAiBargain() {
      try {
        uni.showLoading({ title: 'AI 思考中...' })
        await post(`/api/chat/conversations/${this.covId}/ai-bargain`)
        this.showAiBargainTip = false
        await this.refreshHistory()
      } catch (e) {
        uni.showToast({ title: 'AI 议价请求失败', icon: 'none' })
      } finally {
        uni.hideLoading()
      }
    },
    scrollToBottom() {
      this.scrollTop = Date.now()
    },
    formatTime(value) {
      if (!value) return ''
      const date = new Date(value)
      if (Number.isNaN(date.getTime())) return ''
      return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
    },
    detectBargainTip(msg) {
      if (!msg || msg.senderId === this.myUserId) return
      const content = msg.content || ''
      const keywords = ['价格', '多少', '便宜', '刀', '能不能少', '最低']
      if (keywords.some((keyword) => content.includes(keyword))) {
        this.showAiBargainTip = true
      }
    }
  }
}
</script>

<style scoped>
.chat-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #f4f7fb;
  position: relative;
  overflow: hidden;
}
.message-list {
  flex: 1;
  min-height: 0;
  padding: 16rpx 16rpx 180rpx;
  box-sizing: border-box;
}
.empty-state {
  text-align: center;
  color: #9ca3af;
  padding-top: 120rpx;
  font-size: 28rpx;
}
.time-divider {
  text-align: center;
  font-size: 22rpx;
  color: #999;
  margin: 20rpx 0;
}
.message-item {
  display: flex;
  margin-bottom: 18rpx;
}
.my-msg {
  justify-content: flex-end;
}
.other-msg {
  justify-content: flex-start;
}
.message-content-wrapper {
  max-width: 72%;
}
.bubble {
  padding: 18rpx 22rpx;
  border-radius: 12rpx;
  font-size: 28rpx;
  line-height: 1.55;
  word-break: break-word;
}
.other-msg .bubble {
  background: #ffffff;
  color: #1f2937;
}
.my-msg .bubble {
  background: #2563eb;
  color: #ffffff;
}
.read-status {
  display: block;
  margin-top: 6rpx;
  text-align: right;
  color: #9ca3af;
  font-size: 22rpx;
}
.ai-tip-bar {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 16rpx 20rpx;
  background: #fff7ed;
  border-top: 1rpx solid #fed7aa;
  color: #9a3412;
  font-size: 24rpx;
}
.ai-btn {
  height: 52rpx;
  padding: 0 18rpx;
  border-radius: 8rpx;
  background: #f97316;
  color: #ffffff;
  font-size: 24rpx;
  line-height: 52rpx;
}
.bottom-fixed-area {
  background: #ffffff;
  border-top: 1rpx solid #e5e7eb;
  padding: 14rpx;
  box-sizing: border-box;
}
.quick-reply-bar {
  white-space: nowrap;
  margin-bottom: 12rpx;
}
.quick-item {
  display: inline-flex;
  align-items: center;
  height: 50rpx;
  padding: 0 18rpx;
  margin-right: 12rpx;
  border-radius: 999rpx;
  background: #f3f4f6;
  color: #374151;
  font-size: 24rpx;
}
.input-area {
  display: flex;
  align-items: center;
  gap: 12rpx;
}
.input-area input {
  flex: 1;
  height: 72rpx;
  padding: 0 22rpx;
  border-radius: 10rpx;
  background: #f3f4f6;
  color: #111827;
  font-size: 28rpx;
  box-sizing: border-box;
}
.send-btn {
  width: 110rpx;
  height: 72rpx;
  border-radius: 10rpx;
  background: #ef1f2d;
  color: #ffffff;
  font-size: 28rpx;
  line-height: 72rpx;
}
.send-btn[disabled] {
  background: #cbd5e1;
}
button {
  margin: 0;
  padding: 0;
  border: none;
}
button::after {
  border: none;
}
</style>
