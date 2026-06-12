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
      <view v-for="item in tabs" :key="item.key" class="role-nav-item" :class="{ on: active === item.key }" @click="selectTab(item)">
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

    <view v-if="active === 'store'" class="content">
      <view class="section-head">
        <text class="page-title">店铺管理</text>
        <text class="link" @click="openPublicStore">买家视角预览</text>
      </view>
      <view class="store-manage-grid">
        <view class="section store-form-card">
          <text class="section-title">店铺资料</text>
          <view class="store-form">
            <view class="form-field">
              <text class="form-label">店铺名称</text>
              <input v-model="storeForm.name" class="form-input" placeholder="请输入店铺名称" />
            </view>
            <view class="form-field">
              <text class="form-label">店铺简介</text>
              <textarea v-model="storeForm.desc" class="form-textarea" maxlength="300" placeholder="介绍店铺主营、服务和交易方式" />
            </view>
            <view class="form-field">
              <text class="form-label">店铺标识</text>
              <input v-model="storeForm.badge" class="form-input" placeholder="例如 信用卖家 / 官方严选" />
            </view>
            <view class="form-field">
              <text class="form-label">服务标签</text>
              <input v-model="storeForm.serviceText" class="form-input" placeholder="用逗号分隔，例如 平台担保,真实商品" />
            </view>
            <button class="top-action form-action" :loading="savingStore" @click="saveStore">保存店铺</button>
          </view>
        </view>
        <view class="section store-summary-card">
          <text class="section-title">买家侧展示</text>
          <view class="store-preview">
            <text class="preview-name">{{ myStore.name || '我的店铺' }}</text>
            <text class="preview-desc">{{ myStore.desc || '卖家暂未填写店铺介绍。' }}</text>
            <view class="preview-tags">
              <text v-for="item in storeServices" :key="item">{{ item }}</text>
            </view>
          </view>
          <view class="store-stats">
            <view class="store-stat"><text>{{ myStore.productCount || myProducts.length }}</text><text>在售商品</text></view>
            <view class="store-stat"><text>{{ myStore.newCount || newProductCount }}</text><text>新品</text></view>
            <view class="store-stat"><text>{{ myStore.usedCount || usedProductCount }}</text><text>二手闲置</text></view>
            <view class="store-stat"><text>{{ myStore.creditScore || 100 }}</text><text>店铺信用</text></view>
          </view>
        </view>
      </view>
      <view class="section">
        <view class="section-head">
          <text class="section-title">店铺商品</text>
          <text class="link" @click="goPublish">新增发布</text>
        </view>
        <view v-if="!myProducts.length" class="line">暂无商品，发布后会同步出现在买家店铺页。</view>
        <view v-for="item in myProducts" :key="item.id" class="list-card">
          <view>
            <text class="item-title">{{ item.title }}</text>
            <text class="item-desc">{{ item.scene === 'new' ? '新品' : '二手' }} · {{ item.category }} · ¥{{ item.price }}</text>
          </view>
          <text class="pill" :class="{ warn: item.status !== 'approved' }">{{ statusText(item) }}</text>
        </view>
      </view>
    </view>

    <view v-if="active === 'messages'" class="content message-content">
      <view class="seller-message-workspace">
        <view class="seller-conversation-pane">
          <view class="seller-pane-head">
            <view>
              <text class="seller-pane-kicker">Messages</text>
              <text class="seller-pane-title">卖家消息</text>
            </view>
            <view class="seller-total-pill">{{ filteredConversations.length }}</view>
          </view>
          <view class="seller-search-box">
            <text class="seller-search-icon">⌕</text>
            <input v-model="messageKeyword" class="seller-search-input" placeholder="搜索买家或商品" />
          </view>
          <scroll-view class="seller-conversation-list" scroll-y :show-scrollbar="false">
            <view
              v-for="item in filteredConversations"
              :key="item.covId"
              class="seller-conversation-item"
              :class="{ active: item.covId === activeCovId }"
              @click="selectConversation(item)"
            >
              <view class="seller-avatar-wrap">
                <image v-if="item.goodsImageUrl" class="seller-avatar-img" :src="item.goodsImageUrl" mode="aspectFill"></image>
                <view v-else class="seller-avatar">{{ item.icon }}</view>
                <text v-if="item.unreadCount > 0" class="seller-badge">{{ item.unreadCount }}</text>
              </view>
              <view class="seller-conversation-main">
                <view class="seller-conversation-head">
                  <text class="seller-conversation-name">{{ item.title }}</text>
                  <text class="seller-conversation-time">{{ item.time }}</text>
                </view>
                <text class="seller-conversation-sub">{{ item.sub }}</text>
                <text v-if="item.goodsName" class="seller-goods-line">{{ item.goodsName }}</text>
              </view>
            </view>
            <view v-if="!messageLoadingList && filteredConversations.length === 0" class="seller-empty-list">
              <text class="seller-empty-title">暂无会话</text>
              <text class="seller-empty-sub">买家咨询你的店铺商品后，会话会出现在这里。</text>
            </view>
          </scroll-view>
        </view>

        <view class="seller-chat-pane">
          <template v-if="activeConversation">
            <view class="seller-chat-header">
              <view class="seller-chat-contact">
                <view class="seller-chat-avatar">{{ activeConversation.icon }}</view>
                <view>
                  <text class="seller-chat-title">{{ activeConversation.title }}</text>
                  <text class="seller-chat-subtitle">{{ activeConversation.goodsName || '正在咨询商品' }}</text>
                </view>
              </view>
              <button class="seller-ghost-btn" @click="refreshConversation">刷新</button>
            </view>

            <scroll-view class="seller-messages" scroll-y :scroll-top="messageScrollTop" :show-scrollbar="false">
              <view class="seller-session-tip">
                <view class="seller-tip-line"></view>
                <text>上次聊到这里</text>
                <view class="seller-tip-line"></view>
              </view>
              <view
                v-for="msg in parsedMessages"
                :key="msg.cmId || `${msg.senderId}-${msg.createTime}`"
                class="seller-message-row"
                :class="{ mine: msg.senderId === messageUserId }"
              >
                <view v-if="msg.senderId !== messageUserId" class="seller-mini-avatar">{{ activeConversation.icon }}</view>
                <view class="seller-bubble-wrap">
                  <text class="seller-message-time">{{ formatTime(msg.createTime) }}</text>
                  <view v-if="msg.card" class="seller-product-card" @click="openProductById(msg.card.id)">
                    <image v-if="msg.card.cover" class="seller-card-thumb" :src="msg.card.cover" mode="aspectFill"></image>
                    <view class="seller-card-body">
                      <text class="seller-card-label">商品卡片</text>
                      <text class="seller-card-title">{{ msg.card.title }}</text>
                      <view class="seller-card-meta">
                        <text class="seller-card-price">{{ priceLabel(msg.card.price) }}</text>
                        <text v-if="msg.card.scene" class="seller-card-tag">{{ sceneLabel(msg.card.scene) }}</text>
                      </view>
                    </view>
                  </view>
                  <view v-else class="seller-bubble">
                    <text>{{ msg.content }}</text>
                  </view>
                  <text v-if="msg.senderId === messageUserId" class="seller-read-receipt" :class="{ read: msg.isRead }">{{ msg.isRead ? '已读' : '未读' }}</text>
                </view>
              </view>
              <view v-if="!messageLoadingMessages && parsedMessages.length === 0" class="seller-empty-chat">
                <text class="seller-empty-title">没有聊天记录</text>
                <text class="seller-empty-sub">选择会话后，可以直接回复买家的商品咨询。</text>
              </view>
            </scroll-view>

            <scroll-view class="seller-quick-row" scroll-x :show-scrollbar="false">
              <view class="seller-quick-track">
                <button v-for="item in sellerQuickOptions" :key="item" class="seller-quick-btn" @click="sendQuick(item)">{{ item }}</button>
              </view>
            </scroll-view>
            <view class="seller-composer">
              <view v-if="showEmojiPicker" class="seller-emoji-panel">
                <view class="seller-emoji-title">{{ currentEmojiGroupName }}</view>
                <scroll-view class="seller-emoji-grid" scroll-y :show-scrollbar="false">
                  <view class="seller-emoji-grid-inner">
                    <button
                      v-for="emoji in currentEmojiOptions"
                      :key="emoji"
                      class="seller-emoji-item"
                      @click="chooseEmoji(emoji)"
                    >{{ emoji }}</button>
                  </view>
                </scroll-view>
                <view class="seller-emoji-tabs">
                  <button
                    v-for="group in emojiGroups"
                    :key="group.key"
                    class="seller-emoji-tab"
                    :class="{ on: activeEmojiGroup === group.key }"
                    @click="activeEmojiGroup = group.key"
                  >{{ group.name }}</button>
                </view>
              </view>
              <view class="seller-tool-row">
                <button class="seller-tool-btn" :class="{ active: showEmojiPicker }" @click="toggleEmojiPicker">☺</button>
                <text class="seller-counter">{{ messageInput.length }} / 500</text>
              </view>
              <textarea v-model="messageInput" class="seller-message-input" maxlength="500" placeholder="请输入回复买家的内容..." />
              <view class="seller-composer-actions">
                <button class="seller-send-btn secondary" @click="sendProductCard">发送商品</button>
                <button class="seller-send-btn" :disabled="messageSending || !messageInput.trim()" @click="sendMessage">发送</button>
              </view>
            </view>
          </template>
          <view v-else class="seller-chat-empty-state">
            <text class="seller-empty-title">请选择一个会话</text>
            <text class="seller-empty-sub">左侧会展示买家对你店铺商品的咨询。</text>
          </view>
        </view>

        <view class="seller-tools-pane">
          <template v-if="activeConversation">
            <view class="seller-side-card">
              <view class="seller-side-avatar">{{ activeConversation.icon }}</view>
              <view class="seller-side-main">
                <text class="seller-side-name">{{ activeConversation.title }}</text>
                <text class="seller-side-desc">{{ activeConversation.goodsName || '商品咨询' }}</text>
              </view>
            </view>
            <view class="seller-side-section">
              <text class="seller-block-title">卖家处理</text>
              <view class="seller-action-grid">
                <button class="seller-fn-btn" @click="appendText('您好，这件商品目前还在，可以继续沟通细节。')">回复在售</button>
                <button class="seller-fn-btn" @click="appendText('支持平台担保交易，您可以放心下单。')">担保说明</button>
                <button class="seller-fn-btn" @click="appendText('我可以补充商品细节图和配件说明。')">补充细节</button>
                <button class="seller-fn-btn" @click="sendProductCard">发商品卡</button>
              </view>
            </view>
            <view class="seller-side-section">
              <text class="seller-block-title">会话状态</text>
              <view class="seller-status-row">
                <button class="seller-status-btn" :class="{ on: activeConversation.status === 'pending' }" @click="updateConversationStatus('pending')">待跟进</button>
                <button class="seller-status-btn" :class="{ on: activeConversation.status === 'dealing' }" @click="updateConversationStatus('dealing')">沟通中</button>
                <button class="seller-status-btn" :class="{ on: activeConversation.status === 'closed' }" @click="updateConversationStatus('closed')">已结束</button>
              </view>
            </view>
            <view class="seller-side-section">
              <view class="seller-section-head">
                <text class="seller-block-title">咨询商品</text>
                <button class="seller-text-btn" @click="openProduct">查看</button>
              </view>
              <view class="seller-focus-product" @click="openProduct">
                <image v-if="focusProduct.cover" class="seller-focus-img" :src="focusProduct.cover" mode="aspectFill"></image>
                <view v-else class="seller-focus-placeholder">商品</view>
                <view class="seller-focus-main">
                  <text class="seller-focus-title">{{ focusProduct.title || activeConversation.goodsName || '商品信息加载中' }}</text>
                  <text class="seller-focus-price">{{ priceLabel(focusProduct.price || activeConversation.goodsPrice) }}</text>
                  <view class="seller-tag-row">
                    <text v-if="focusProduct.scene || activeConversation.goodsScene" class="seller-card-tag">{{ sceneLabel(focusProduct.scene || activeConversation.goodsScene) }}</text>
                    <text v-if="focusProduct.category || activeConversation.goodsCategory" class="seller-card-tag muted">{{ focusProduct.category || activeConversation.goodsCategory }}</text>
                  </view>
                </view>
              </view>
            </view>
            <view class="seller-side-section">
              <text class="seller-block-title">经营入口</text>
              <button class="seller-link-btn" @click="active = 'store'">店铺管理</button>
              <button class="seller-link-btn" @click="active = 'products'">我的发布</button>
            </view>
          </template>
          <view v-else class="seller-side-empty">
            <text class="seller-empty-title">没有选中的会话</text>
            <text class="seller-empty-sub">选择会话后，这里会显示处理工具。</text>
          </view>
        </view>
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
      <view v-for="item in tabs" :key="item.key" class="tab" :class="{ on: active === item.key }" @click="selectTab(item)">
        <text>{{ item.label }}</text>
      </view>
    </view>
  </view>
</template>

<script>
import { clearSession, getCachedUser } from '@/utils/auth.js'
import { cancelSellerRealName, fetchSellerCenter, submitSellerRealName } from '@/services/center.js'
import { fetchMyProducts, fetchMyStore, updateMyStore } from '@/services/shop.js'
import { get, post, put } from '@/utils/request.js'

const PRODUCT_CARD_PREFIX = '__PRODUCT_CARD__'

export default {
  data() {
    return {
      active: 'home',
      user: {},
      center: {},
      showRealNamePanel: false,
      submittingRealName: false,
      cancelingRealName: false,
      savingStore: false,
      myStore: {},
      myProducts: [],
      messageKeyword: '',
      conversationList: [],
      activeCovId: null,
      messages: [],
      messageInput: '',
      messageUserId: null,
      messageLoadingList: false,
      messageLoadingMessages: false,
      messageSending: false,
      messagePollTimer: null,
      messageScrollTop: 0,
      showEmojiPicker: false,
      activeEmojiGroup: 'face',
      emojiGroups: [
        { key: 'face', name: '经典', items: ['😀', '😁', '😂', '🤣', '😊', '😇', '🙂', '😉', '😍', '😘', '😋', '😜', '😎', '🤓', '🤔', '🤗', '😳', '🥺', '😭', '😤', '😡', '😱', '😴', '🤒', '😵', '🤯', '🥳', '😅', '😆', '😬', '🙄', '😏', '😌', '😔', '😮', '🤭', '🤫', '🤐', '😷', '🤧', '🥰', '😚', '😛', '😝', '🤤', '😪', '😫', '😈'] },
        { key: 'hand', name: '手势', items: ['👍', '👎', '👌', '✌️', '🤞', '🤟', '🤙', '👋', '👏', '🙌', '🙏', '💪', '🤝', '🫶', '☝️', '👉', '👈', '👇', '👆', '✋', '🤚', '🖐️', '🫡', '🤲'] },
        { key: 'trade', name: '交易', items: ['💬', '💰', '💸', '🧾', '📦', '🚚', '🎁', '🏷️', '💳', '✅', '❌', '⚠️', '📌', '🔍', '🛒', '⭐', '🔥', '💡', '📮', '⏰', '🧡', '💯', '📱', '💻', '🎧', '📚', '🏠', '☕'] },
        { key: 'heart', name: '心情', items: ['❤️', '🧡', '💛', '💚', '💙', '💜', '🤍', '🤎', '🖤', '💔', '💕', '💞', '💓', '💗', '💖', '💘', '💝', '✨', '🌟', '🎉', '🌈', '☀️', '🌙', '🍀'] }
      ],
      storeForm: {
        name: '',
        desc: '',
        badge: '',
        serviceText: ''
      },
      realNameForm: {
        realName: '',
        idCard: ''
      },
      tabs: [
        { key: 'home', label: '\u5de5\u4f5c\u53f0' },
        { key: 'store', label: '\u5e97\u94fa' },
        { key: 'products', label: '\u5546\u54c1' },
        { key: 'orders', label: '\u8ba2\u5355' },
        { key: 'messages', label: '\u6d88\u606f' },
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
    },
    storeServices() {
      return this.myStore.service && this.myStore.service.length ? this.myStore.service : ['平台担保', '真实商品', '信用卖家']
    },
    newProductCount() {
      return this.myProducts.filter(item => item.scene === 'new').length
    },
    usedProductCount() {
      return this.myProducts.filter(item => item.scene === 'used').length
    },
    filteredConversations() {
      const word = this.messageKeyword.trim().toLowerCase()
      if (!word) return this.conversationList
      return this.conversationList.filter(item => [item.title, item.sub, item.goodsName].some(value => String(value || '').toLowerCase().includes(word)))
    },
    activeConversation() {
      return this.conversationList.find(item => item.covId === this.activeCovId) || null
    },
    currentEmojiOptions() {
      const group = this.emojiGroups.find(item => item.key === this.activeEmojiGroup)
      return group ? group.items : []
    },
    currentEmojiGroupName() {
      const group = this.emojiGroups.find(item => item.key === this.activeEmojiGroup)
      return group ? group.name : '经典'
    },
    parsedMessages() {
      return this.messages.map(item => ({
        ...item,
        card: this.parseProductCard(item.content)
      }))
    },
    focusProduct() {
      const active = this.activeConversation
      if (!active) return {}
      const found = this.myProducts.find(item => String(item.id) === String(active.goodsId))
      if (found) return found
      return {
        id: active.goodsId,
        title: active.goodsName,
        price: active.goodsPrice,
        cover: active.goodsImageUrl,
        category: active.goodsCategory,
        scene: active.goodsScene,
        storeId: active.storeId
      }
    },
    sellerQuickOptions() {
      return ['您好，商品还在', '支持平台担保', '可以补充细节图', '今天可以发货', '配件信息如下', '价格可以小幅协商']
    }
  },
  onLoad(query) {
    if (query && query.tab && this.tabs.some(item => item.key === query.tab)) {
      this.active = query.tab
    }
  },
  onShow() {
    this.user = getCachedUser() || {}
    this.messageUserId = this.user.userId || this.user.id || null
    this.loadCenter()
    this.loadStoreManage()
    if (this.active === 'messages') {
      this.loadSellerMessagesArea()
    }
  },
  onHide() {
    this.stopMessagePoll()
  },
  onUnload() {
    this.stopMessagePoll()
  },
  methods: {
    selectTab(item) {
      if (this.active === 'messages' && item.key !== 'messages') this.stopMessagePoll()
      this.active = item.key
      if (item.key === 'messages') this.loadSellerMessagesArea()
    },
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
    async loadStoreManage() {
      await Promise.all([this.loadMyStore(), this.loadMyProducts()])
    },
    async loadMyStore() {
      try {
        const body = await fetchMyStore()
        if (body.code === 0 && body.data) {
          this.myStore = body.data
          this.storeForm = {
            name: body.data.name || '',
            desc: body.data.desc || '',
            badge: body.data.badge || '',
            serviceText: (body.data.service || []).join(',')
          }
        }
      } catch (e) {
        console.error('加载店铺失败', e)
      }
    },
    async loadMyProducts() {
      try {
        const body = await fetchMyProducts()
        this.myProducts = body.code === 0 && Array.isArray(body.data) ? body.data : []
        if (this.myProducts.length) {
          this.products = this.myProducts.map(item => ({
            title: item.title,
            desc: `${item.category || '未分类'} · ¥${item.price}`,
            status: this.statusText(item)
          }))
        }
      } catch (e) {
        this.myProducts = []
      }
    },
    async saveStore() {
      if (!this.storeForm.name.trim()) {
        uni.showToast({ title: '请填写店铺名称', icon: 'none' })
        return
      }
      this.savingStore = true
      try {
        const body = await updateMyStore({
          name: this.storeForm.name.trim(),
          desc: this.storeForm.desc.trim(),
          badge: this.storeForm.badge.trim(),
          service: this.storeForm.serviceText.split(/[,，\s]+/).map(item => item.trim()).filter(Boolean)
        })
        if (body.code === 0 && body.data) {
          this.myStore = body.data
          uni.showToast({ title: '店铺已更新', icon: 'success' })
        }
      } catch (e) {
        uni.showToast({ title: '保存失败，请稍后重试', icon: 'none' })
      } finally {
        this.savingStore = false
      }
    },
    openPublicStore() {
      const id = this.myStore.id || ''
      if (!id) {
        uni.showToast({ title: '店铺信息加载中', icon: 'none' })
        return
      }
      uni.navigateTo({ url: '/pages/store/store?id=' + encodeURIComponent(id) })
    },
    statusText(item) {
      if (item.status === 'pending') return '待审核'
      if (item.status === 'rejected') return '已拒绝'
      if (item.status === 'offline') return '已下架'
      return '买家可见'
    },
    async loadSellerMessagesArea() {
      this.messageUserId = this.user.userId || this.user.id || this.messageUserId
      await Promise.all([this.loadMyProducts(), this.fetchConversationList(true)])
      this.startMessagePoll()
    },
    async fetchConversationList(autoSelect = false) {
      this.messageLoadingList = true
      try {
        const res = await get('/api/chat/conversations')
        const body = res.data || res
        const list = body.code === 0 && Array.isArray(body.data) ? body.data : []
        this.conversationList = list.map(item => ({
          ...item,
          title: item.peerName || item.buyerName || item.sellerName || '买家',
          sub: this.formatConversationLastMessage(item.lastMessage),
          time: this.formatTime(item.lastTime),
          icon: this.avatarText(item.peerName || item.buyerName || item.sellerName || '买家')
        }))
        if (autoSelect && !this.activeCovId && this.conversationList.length) {
          await this.selectConversation(this.conversationList[0])
        } else if (this.activeCovId && !this.conversationList.some(item => item.covId === this.activeCovId)) {
          this.activeCovId = null
          this.messages = []
        }
      } catch (e) {
        this.conversationList = []
      } finally {
        this.messageLoadingList = false
      }
    },
    async selectConversation(item) {
      this.activeCovId = item.covId
      await this.loadConversationMessages(item.covId)
      this.markConversationRead(item.covId)
    },
    async loadConversationMessages(covId) {
      if (!covId) return
      this.messageLoadingMessages = true
      try {
        const res = await get(`/api/chat/conversations/${covId}/messages`)
        const body = res.data || res
        this.messages = body.code === 0 && Array.isArray(body.data) ? body.data : []
        this.scrollToLatest()
      } catch (e) {
        this.messages = []
      } finally {
        this.messageLoadingMessages = false
      }
    },
    async markConversationRead(covId) {
      if (!covId) return
      try {
        await post(`/api/chat/${covId}/read`, {})
        const current = this.conversationList.find(item => item.covId === covId)
        if (current) current.unreadCount = 0
      } catch (e) {}
    },
    async refreshConversation() {
      await this.fetchConversationList(false)
      if (this.activeCovId) await this.loadConversationMessages(this.activeCovId)
    },
    startMessagePoll() {
      this.stopMessagePoll()
      this.messagePollTimer = setInterval(() => {
        if (this.active === 'messages') this.refreshConversation()
      }, 8000)
    },
    stopMessagePoll() {
      if (this.messagePollTimer) {
        clearInterval(this.messagePollTimer)
        this.messagePollTimer = null
      }
    },
    async sendMessage() {
      const content = this.messageInput.trim()
      if (!content || !this.activeCovId || this.messageSending) return
      this.messageSending = true
      try {
        const res = await post(`/api/chat/conversations/${this.activeCovId}/messages`, {
          covId: this.activeCovId,
          content,
          type: 'CHAT_MESSAGE'
        })
        const body = res.data || res
        if (body.code === 0) {
          this.messageInput = ''
          this.showEmojiPicker = false
          await this.refreshConversation()
        }
      } catch (e) {
        uni.showToast({ title: '发送失败，请稍后重试', icon: 'none' })
      } finally {
        this.messageSending = false
      }
    },
    sendQuick(text) {
      this.messageInput = text
      this.sendMessage()
    },
    async sendProductCard() {
      const product = this.focusProduct
      if (!product || !product.id || !this.activeCovId) {
        uni.showToast({ title: '暂无可发送的商品', icon: 'none' })
        return
      }
      this.messageInput = PRODUCT_CARD_PREFIX + JSON.stringify({
        id: product.id,
        title: product.title,
        price: product.price,
        cover: product.cover,
        scene: product.scene
      })
      await this.sendMessage()
    },
    appendText(text) {
      this.messageInput = this.messageInput ? `${this.messageInput}\n${text}` : text
    },
    toggleEmojiPicker() {
      this.showEmojiPicker = !this.showEmojiPicker
    },
    chooseEmoji(emoji) {
      this.messageInput = `${this.messageInput}${emoji}`
    },
    async updateConversationStatus(status) {
      if (!this.activeCovId) return
      try {
        const res = await put(`/api/chat/conversations/${this.activeCovId}/status`, { status })
        const body = res.data || res
        if (body.code === 0) {
          const current = this.conversationList.find(item => item.covId === this.activeCovId)
          if (current) current.status = status
        }
      } catch (e) {
        uni.showToast({ title: '状态更新失败', icon: 'none' })
      }
    },
    openProduct() {
      const id = (this.focusProduct && this.focusProduct.id) || (this.activeConversation && this.activeConversation.goodsId)
      if (id) this.openProductById(id)
    },
    openProductById(id) {
      if (!id) return
      uni.navigateTo({ url: '/pages/goods/detail?id=' + encodeURIComponent(id) })
    },
    parseProductCard(content) {
      if (!content || typeof content !== 'string' || !content.startsWith(PRODUCT_CARD_PREFIX)) return null
      try {
        return JSON.parse(content.slice(PRODUCT_CARD_PREFIX.length))
      } catch (e) {
        return null
      }
    },
    formatConversationLastMessage(content) {
      const card = this.parseProductCard(content)
      if (card) return `[商品卡片] ${card.title || '商品'}`
      return content || '暂无消息'
    },
    formatTime(value) {
      if (!value) return ''
      const date = new Date(value)
      if (Number.isNaN(date.getTime())) return ''
      const now = new Date()
      const hh = String(date.getHours()).padStart(2, '0')
      const mm = String(date.getMinutes()).padStart(2, '0')
      if (date.toDateString() === now.toDateString()) return `${hh}:${mm}`
      return `${date.getMonth() + 1}-${date.getDate()}`
    },
    avatarText(name) {
      return String(name || '买').slice(0, 1).toUpperCase()
    },
    scrollToLatest() {
      this.$nextTick(() => {
        this.messageScrollTop = this.messageScrollTop === 999999 ? 999998 : 999999
      })
    },
    priceLabel(price) {
      if (price === null || price === undefined || price === '') return '价格待确认'
      return `¥${Number(price).toFixed(2)}`
    },
    sceneLabel(scene) {
      if (scene === 'new') return '新品'
      if (scene === 'used') return '二手'
      return scene || ''
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
