<template>
  <div class="chat-container">
    <div class="message-list" ref="msgList">
      <div v-if="messages.length === 0" class="empty-state">暂无消息，开始聊天吧</div>
      
      <div v-for="(msg, index) in messages" :key="msg.cmId">
        <div v-if="msg.showTime" class="time-divider">
          {{ formatTime(msg.createTime) }}
        </div>
        <div :class="['message-item', msg.senderId === myUserId ? 'my-msg' : 'other-msg']">
          <div class="message-content-wrapper">
            <div class="bubble">
              <p>{{ msg.content }}</p>
            </div>
            <div v-if="msg.senderId === myUserId" class="read-status">
              {{ msg.isRead ? '已读' : '未读' }}
            </div>
          </div>
        </div>
      </div>
    </div>


    <div v-if="showAiBargainTip" class="ai-tip-bar">
      <span>对方提到了价格，是否启用 AI 议价助手？</span>
      <button @click="triggerAiBargain" class="ai-btn">启用 AI 议价</button>
    </div>

    <div class="bottom-fixed-area">
      <div class="quick-reply-bar">
        <div v-for="text in quickOptions" :key="text" class="quick-item" @click="sendQuick(text)">{{ text }}</div>
      </div>
      <div class="input-area">
        <input v-model="inputContent" @keyup.enter="sendMessage" placeholder="说点什么..." />
        <button @click="sendMessage">发送</button>
      </div>
    </div>
  </div>
</template>

<script>
import SockJS from 'sockjs-client';
import Stomp from 'stompjs';
import { fetchMe } from '@/services/auth.js';
import { post } from '@/utils/request.js';
import { get } from '@/utils/request.js';
import { getToken } from '@/utils/auth.js';

const WS_URL = 'http://127.0.0.1:8080/ws';

export default {
  data() {
    return {
      covId: null,
      messages: [],
      inputContent: '',
      quickOptions: ["支持平台担保吗？", "是全新正品吗？", "是否包邮？", "最低多少钱？"],
      myUserId: null,
      stompClient: null,
      showAiBargainTip: false,
      connected: false
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
        this.connectWebSocket()
        setTimeout(() => this.sendReadReceipt(), 1000)
      } catch (error) {
        console.error('聊天初始化失败', error)
        uni.showToast({ title: '聊天启动失败，请重试', icon: 'none' })
      }
    },
    async fetchUserInfo() {
      const body = await fetchMe()
      if (body && body.code === 0 && body.data) {
        this.myUserId = body.data.userId
        console.warn("=== [调试] 成功赋值! 当前 myUserId 为:", this.myUserId);
      }
    },
    async loadHistory() {
      const res = await get(`/api/chat/conversations/${this.covId}/messages`)
      const rawList = (res.data && res.data.data) ? res.data.data : []
      this.messages = this.processMessages(rawList) 
      this.$nextTick(() => this.scrollToBottom())
    },
    connectWebSocket() {
      if (this.connected || !this.covId) {
        return
      }
      const token = getToken()
      const socket = new SockJS(WS_URL)
      this.stompClient = Stomp.over(socket)
      this.stompClient.debug = null

      this.stompClient.connect(
        { Authorization: token ? `Bearer ${token}` : '' },
        () => {
          this.connected = true
          console.log("【调试】WebSocket 连接成功，准备调用 subscribeTopic...");
          this.subscribeTopic()
        },
        (error) => {
          console.error('WebSocket 连接失败', error)
          uni.showToast({ title: '实时聊天连接失败', icon: 'none' })
        }
      )
    },
    sendReadReceipt() {
        if (this.stompClient && this.connected) {
            this.stompClient.send(
                `/app/chat/read`, 
                {},
                JSON.stringify({
                    covId: this.covId,
                    readerId: this.myUserId
                })
            );
        }
    },
    shouldShowTime(newMsg, prevMsg) {
      if (!prevMsg) return true; 
      const current = new Date(newMsg.createTime).getTime();
      const prev = new Date(prevMsg.createTime).getTime();
      return (current - prev) > 5 * 60 * 1000; 
    },
    processMessages(list) {
      return list.map((msg, index) => ({
        ...msg,
        showTime: index === 0 || this.shouldShowTime(msg, list[index - 1])
      }));
    },
    subscribeTopic() {
      if (!this.stompClient || !this.covId) return
      // 1. 公共频道：只负责处理聊天消息
      this.stompClient.subscribe(`/topic/chat/${this.covId}`, (message) => {
        if (!message.body) return
        try {
          const chatMessage = JSON.parse(message.body)
          console.log("【已读调试】收到状态更新通知:", message);
          if (chatMessage.type === 'CHAT_MESSAGE') {
            const lastMsg = this.messages[this.messages.length - 1]
            const showTime = this.shouldShowTime(chatMessage, lastMsg)
            this.messages.push({ ...chatMessage, showTime })
            this.$nextTick(() => {
              this.scrollToBottom()
              if (chatMessage.senderId !== this.myUserId) {
                this.sendReadReceipt()
              } 
            });
            const content = chatMessage.content || "";
            const keywords = ['价格', '多少', '便宜', '刀', '能不能少'];
            // 只要内容匹配关键词，就显示 AI 提示框
            const isMatch = keywords.some(k => content.includes(k));
            if (isMatch) {
              this.showAiBargainTip = true;
              console.log("检测到关键词，AI 提示框已强制触发");
            }
          }
        } catch (e) { console.error('解析消息失败', e); }
      });
      // 2. 私有频道：只负责处理“我发出的消息被已读”的更新
      this.stompClient.subscribe(`/user/queue/chat/read-status`, (message) => {
        const status = JSON.parse(message.body);
        console.log("调试2", status)
        if (status.type === 'STATUS_UPDATE') {
          this.messages.forEach(m => {
            if (m.senderId === this.myUserId && !m.isRead) {
              m.isRead = true;
            }
          });
        }
      });
    },
    disconnect() {
      if (this.stompClient && this.connected) {
        try {
          this.stompClient.disconnect(() => {
            this.connected = false
            this.stompClient = null
          })
        } catch (e) {
          console.warn('WebSocket 断开连接失败', e)
        }
      }
    },

    sendQuick(text) {
      this.inputContent = text; 
      this.sendMessage();    
    },
    
    async sendMessage() {
      const content = this.inputContent && this.inputContent.trim();
      if (!content || !this.covId) return
      const payload = { covId: this.covId, content: content, type: 'CHAT_MESSAGE'};
      if (this.stompClient && this.connected) {
        console.log("hahaha")
        const token = getToken()
        this.stompClient.send(
          `/app/chat/${this.covId}`,
          { 'Authorization': `Bearer ${token}` },
          JSON.stringify(payload)
        );
      } else {
        try {
          await post(`/api/chat/conversations/${this.covId}/messages`, payload)
        } catch (error) {
            console.error('消息发送失败', error)
            uni.showToast({ title: '消息发送失败，请检查网络', icon: 'none' })
            return; 
        }
      }
      this.inputContent = ''
    },

    async triggerAiBargain() {
      try {
        uni.showLoading({ title: 'AI 思考中...' })
        await post(`/api/chat/conversations/${this.covId}/ai-bargain`)
        this.showAiBargainTip = false
        uni.hideLoading()
      } catch (e) {
        uni.hideLoading()
        uni.showToast({ title: 'AI 议价请求失败', icon: 'none' })
      }
    },
    scrollToBottom() {
      const msgList = this.$refs.msgList
      if (msgList) {
        msgList.scrollTop = msgList.scrollHeight
      }
    },
    formatTime(value) {
      if (!value) {
        return ''
      }
      const date = new Date(value)
      return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
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
  overflow-y: auto;      
  padding: 16rpx 16rpx 180rpx;
  display: flex;
  flex-direction: column;
}

.input-area {
  display: flex;
  height: 140rpx;
  padding: 20rpx;
  gap: 12rpx;
  align-items: center;
}

.time-divider {
  text-align: center;
  font-size: 22rpx;
  color: #999;
  margin: 20rpx 0;
}

.message-item {
  display: flex;
  margin: 10px 0;
}

.message-content-wrapper {
  display: flex;
  flex-direction: column; 
  align-items: flex-end; 
  max-width: 70%;
}

.my-msg { justify-content: flex-end; }
.other-msg { justify-content: flex-start; }

.bubble {
  background: #ffffff; 
  padding: 8px 12px;
  border-radius: 8px;
}

.my-msg .bubble {
  background: #1f5c43;
  color: #fff;
}

.read-status {
  font-size: 10px;       
  color: #999;
  margin-top: 4px;       
  padding-right: 2px;
}

.input-area input {
  flex: 1;
  height: 80rpx;
  line-height: 80rpx;
  border-radius: 16rpx;
  border: 1rpx solid #eee;
  padding: 0 20rpx;
}
.input-area button {
  width: 140rpx;
  height: 75rpx;
  border: none;
  border-radius: 16rpx; 
  background: #1f5c43; 
  color: #ffffff;
  font-size: 28rpx;
  font-weight: 500;
}
.ai-tip-bar {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  background: #eef7f4;
  padding: 10rpx 20rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 24rpx;
  color: #1f5c43;
  border-bottom: 1rpx solid #d1e2dd;
}
.ai-btn {
  background: #1f5c43;
  color: #fff;
  padding: 5rpx 15rpx;
  font-size: 22rpx;
  border-radius: 10rpx;
}

.quick-reply-bar {
  display: flex;
  padding: 10px 15px;
  gap: 10px;
  background: #f8f9fa;
  overflow-x: auto; 
  white-space: nowrap;
  border-bottom: 1px solid #eee;
}

.quick-item {
  padding: 6px 14px;
  background: #fff;
  border: 1px solid #e4e9e5;
  border-radius: 16px;
  font-size: 13px;
  color: #333;
  cursor: pointer;
  transition: all 0.2s;
}

.quick-item:hover {
  border-color: #1f5c43;
  color: #1f5c43;
  background: #f0f7f4;
}

.bottom-fixed-area {
  flex-shrink: 0; 
  background: #fff;
  border-top: 1rpx solid #eee;
  z-index: 10;
}
</style>