<template>
	<view class="safe-page" @click="closeFloaters">
		<view class="topbar" @click.stop>
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
					<text class="nav-link" @click="navTo('/pages/browse/browse')">发现</text>
					<text class="nav-link" @click="navTo('/pages/cart/cart')">购物车</text>
					<text class="nav-link on">消息</text>
					<text class="nav-link" @click="navTo('/pages/user/index')">我的</text>
				</view>
			</view>
		</view>

		<view class="content-wrap page-shell">
			<view class="message-workspace">
				<view class="conversation-pane" @click.stop>
					<view class="pane-head">
						<view>
							<text class="pane-kicker">Messages</text>
							<text class="pane-title">{{ messageTitle }}</text>
						</view>
						<view class="total-pill">{{ filteredList.length }}</view>
					</view>

					<view class="search-box">
						<text class="search-icon">⌕</text>
						<input v-model="keyword" class="search-input" placeholder="搜索联系人或商品" />
					</view>

					<scroll-view class="conversation-list" scroll-y :show-scrollbar="false">
						<view
							v-for="item in filteredList"
							:key="item.covId"
							class="conversation-item"
							:class="{ active: item.covId === activeCovId }"
							@click="selectConversation(item)"
						>
							<view class="avatar-wrap">
								<image v-if="item.goodsImageUrl" class="avatar-img" :src="item.goodsImageUrl" mode="aspectFill"></image>
								<view v-else class="avatar">{{ item.icon }}</view>
								<text v-if="item.unreadCount > 0" class="badge">{{ item.unreadCount }}</text>
							</view>
							<view class="conversation-main">
								<view class="conversation-head">
									<text class="conversation-name">{{ item.title }}</text>
									<text class="conversation-time">{{ item.time }}</text>
								</view>
								<text class="conversation-sub">{{ item.sub }}</text>
								<text v-if="item.goodsName" class="goods-line">{{ item.goodsName }}</text>
							</view>
						</view>

						<view v-if="!loadingList && filteredList.length === 0" class="empty-list">
							<text class="empty-title">暂无会话</text>
							<text class="empty-sub">{{ emptyConversationText }}</text>
						</view>
					</scroll-view>
				</view>

				<view class="chat-pane" @click="closeFloaters">
					<template v-if="activeConversation">
						<view class="chat-header" @click.stop>
							<view class="chat-contact">
								<view class="chat-avatar">{{ activeConversation.icon }}</view>
								<view>
									<text class="chat-title">{{ activeConversation.title }}</text>
									<text class="chat-subtitle">{{ activeConversation.goodsName || '正在咨询商品' }}</text>
								</view>
							</view>
							<view class="header-actions">
								<button v-if="!isSeller" class="ghost-btn" :disabled="aiBargaining" @click="triggerAiBargain">{{ aiBargaining ? '生成中' : 'AI 议价' }}</button>
								<button class="ghost-btn" @click="refreshCurrent">刷新</button>
							</view>
						</view>

						<scroll-view class="messages" scroll-y :scroll-top="scrollTop" :show-scrollbar="false">
							<view class="session-tip">
								<view class="line"></view>
								<text>上次聊到这里</text>
								<view class="line"></view>
							</view>

							<view
								v-for="msg in messages"
								:key="msg.cmId || `${msg.senderId}-${msg.createTime}`"
								class="message-row"
								:class="{ mine: msg.senderId === myUserId }"
							>
								<view v-if="msg.senderId !== myUserId" class="mini-avatar">{{ activeConversation.icon }}</view>
								<view class="bubble-wrap">
									<text class="message-time">{{ formatTime(msg.createTime) }}</text>
									<view v-if="msg.card" class="message-product-card" @click.stop="openProductById(msg.card.id)">
										<image v-if="msg.card.cover" class="card-thumb" :src="msg.card.cover" mode="aspectFill"></image>
										<view class="card-body">
											<text class="card-label">商品卡片</text>
											<text class="card-title">{{ msg.card.title }}</text>
											<view class="card-meta">
												<text class="card-price">{{ priceLabel(msg.card.price) }}</text>
												<text v-if="msg.card.scene" class="card-tag">{{ sceneLabel(msg.card.scene) }}</text>
											</view>
										</view>
									</view>
									<view v-else class="bubble">
										<text>{{ msg.content }}</text>
									</view>
									<text v-if="msg.senderId === myUserId" class="read-receipt" :class="{ read: msg.isRead }">{{ msg.isRead ? '已读' : '未读' }}</text>
								</view>
							</view>

							<view v-if="!loadingMessages && messages.length === 0" class="empty-chat">
								<text class="empty-title">没有聊天记录</text>
								<text class="empty-sub">可以先发一句问候，或者发送当前商品卡片。</text>
							</view>
						</scroll-view>

						<scroll-view class="quick-row" scroll-x :show-scrollbar="false" @click.stop>
							<view class="quick-track">
								<button v-for="item in quickOptions" :key="item" class="quick-btn" @click="sendQuick(item)">{{ item }}</button>
							</view>
						</scroll-view>

						<view class="composer" @click.stop>
							<view v-if="showEmojiPicker" class="emoji-panel">
								<view class="emoji-title">{{ currentEmojiGroupName }}</view>
								<scroll-view class="emoji-grid" scroll-y :show-scrollbar="false">
									<view class="emoji-grid-inner">
										<button
											v-for="emoji in currentEmojiOptions"
											:key="emoji"
											class="emoji-item"
											@click="chooseEmoji(emoji)"
										>{{ emoji }}</button>
									</view>
								</scroll-view>
								<view class="emoji-tabs">
									<button
										v-for="group in emojiGroups"
										:key="group.key"
										class="emoji-tab"
										:class="{ on: activeEmojiGroup === group.key }"
										@click="activeEmojiGroup = group.key"
									>{{ group.name }}</button>
								</view>
							</view>

							<view class="tool-row">
								<button class="tool-btn" :class="{ active: showEmojiPicker }" @click="toggleEmojiPicker">☺</button>
								<button class="tool-btn" @click="sendProductCard">▧</button>
								<button class="tool-btn" @click="appendText(defaultAppendText)">♡</button>
								<text class="counter">{{ messageInput.length }} / 500</text>
							</view>
							<textarea
								v-model="messageInput"
								class="message-input"
								maxlength="500"
								:placeholder="inputPlaceholder"
								@confirm="sendMessage"
							/>
							<view class="composer-actions">
								<button class="send-btn secondary" @click="sendProductCard">{{ productCardButtonText }}</button>
								<button class="send-btn" :disabled="sending || !messageInput.trim()" @click="sendMessage">发送</button>
							</view>
						</view>
					</template>

					<view v-else class="chat-empty-state">
						<text class="empty-title">请选择一个会话</text>
						<text class="empty-sub">{{ emptyChatText }}</text>
					</view>
				</view>

				<view class="info-pane" @click.stop>
					<template v-if="activeConversation">
						<view class="seller-card">
							<view class="seller-avatar">{{ activeConversation.icon }}</view>
							<view class="seller-main">
								<text class="seller-name">{{ sidePrimaryName }}</text>
								<text class="seller-desc">{{ sideSecondaryText }}</text>
							</view>
						</view>

						<template v-if="isSeller">
							<view class="side-section">
								<text class="block-title">卖家处理</text>
								<view class="seller-action-grid">
									<button class="seller-fn-btn" @click="appendText('您好，这件商品目前还在，可以继续沟通细节。')">回复在售</button>
									<button class="seller-fn-btn" @click="appendText('支持平台担保交易，您可以放心下单。')">担保说明</button>
									<button class="seller-fn-btn" @click="appendText('我可以补充商品细节图和配件说明。')">补充细节</button>
									<button class="seller-fn-btn" @click="sendProductCard">发商品卡</button>
								</view>
							</view>
							<view class="side-section">
								<text class="block-title">会话状态</text>
								<view class="status-row">
									<button class="status-btn" :class="{ on: activeConversation.status === 'pending' }" @click="updateConversationStatus('pending')">待跟进</button>
									<button class="status-btn" :class="{ on: activeConversation.status === 'dealing' }" @click="updateConversationStatus('dealing')">沟通中</button>
									<button class="status-btn" :class="{ on: activeConversation.status === 'closed' }" @click="updateConversationStatus('closed')">已结束</button>
								</view>
							</view>
							<view class="side-section">
								<view class="section-head">
									<text class="block-title">咨询商品</text>
									<button class="text-btn" @click="openProduct">查看</button>
								</view>
								<view class="focus-product" @click="openProduct">
									<image v-if="focusProduct.cover" class="focus-img" :src="focusProduct.cover" mode="aspectFill"></image>
									<view v-else class="focus-placeholder">商品</view>
									<view class="focus-main">
										<text class="focus-title">{{ focusProduct.title || activeConversation.goodsName || '商品信息加载中' }}</text>
										<text class="focus-price">{{ priceLabel(focusProduct.price || activeConversation.goodsPrice) }}</text>
										<view class="tag-row">
											<text v-if="focusProduct.scene || activeConversation.goodsScene" class="tag">{{ sceneLabel(focusProduct.scene || activeConversation.goodsScene) }}</text>
											<text v-if="focusProduct.category || activeConversation.goodsCategory" class="tag">{{ focusProduct.category || activeConversation.goodsCategory }}</text>
										</view>
									</view>
								</view>
							</view>
							<view class="side-section">
								<text class="block-title">经营入口</text>
								<button class="link-btn" @click="openSellerStore">店铺管理</button>
								<button class="link-btn" @click="openSellerProducts">我的发布</button>
							</view>
						</template>

						<template v-else>
						<view class="side-section">
							<view class="section-head">
								<text class="block-title">正在咨询</text>
								<button class="text-btn" @click="openProduct">查看</button>
							</view>
							<view class="focus-product" @click="openProduct">
								<image v-if="focusProduct.cover" class="focus-img" :src="focusProduct.cover" mode="aspectFill"></image>
								<view v-else class="focus-placeholder">商品</view>
								<view class="focus-main">
									<text class="focus-title">{{ focusProduct.title || activeConversation.goodsName || '商品信息加载中' }}</text>
									<text class="focus-price">{{ priceLabel(focusProduct.price || activeConversation.goodsPrice) }}</text>
									<view class="tag-row">
										<text v-if="focusProduct.scene || activeConversation.goodsScene" class="tag">{{ sceneLabel(focusProduct.scene || activeConversation.goodsScene) }}</text>
										<text v-if="focusProduct.category || activeConversation.goodsCategory" class="tag">{{ focusProduct.category || activeConversation.goodsCategory }}</text>
									</view>
								</view>
							</view>
							<button class="link-btn" @click="sendProductCard">发送商品卡片</button>
						</view>

						<view class="side-section">
							<text class="block-title">刚刚浏览过的本店商品</text>
							<scroll-view class="side-product-list" scroll-y :show-scrollbar="false">
								<view
									v-for="item in recentStoreProducts"
									:key="item.id"
									class="mini-product"
									@click="openProductById(item.id)"
								>
									<image class="mini-product-img" :src="item.cover" mode="aspectFill"></image>
									<view class="mini-product-main">
										<text class="mini-product-title">{{ item.title }}</text>
										<text class="mini-product-price">{{ priceLabel(item.price) }}</text>
									</view>
								</view>
								<view v-if="recentStoreProducts.length === 0" class="side-empty-mini">暂无更多本店商品</view>
							</scroll-view>
						</view>

						<view class="side-section">
							<text class="block-title">同类相似商品</text>
							<scroll-view class="side-product-list compact" scroll-y :show-scrollbar="false">
								<view
									v-for="item in similarProducts"
									:key="item.id"
									class="mini-product"
									@click="openProductById(item.id)"
								>
									<image class="mini-product-img" :src="item.cover" mode="aspectFill"></image>
									<view class="mini-product-main">
										<text class="mini-product-title">{{ item.title }}</text>
										<view class="mini-product-tags">
											<text class="mini-tag">{{ sceneLabel(item.scene) }}</text>
											<text class="mini-product-price">{{ priceLabel(item.price) }}</text>
										</view>
									</view>
								</view>
								<view v-if="similarProducts.length === 0" class="side-empty-mini">暂无同类商品</view>
							</scroll-view>
						</view>
						</template>
					</template>

					<view v-else class="side-empty">
						<text class="empty-title">没有选中的会话</text>
						<text class="empty-sub">{{ sideEmptyText }}</text>
					</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
	import { fetchMe } from '@/services/auth.js'
	import { get, post, put } from '@/utils/request.js'

	const PRODUCT_CARD_PREFIX = '__PRODUCT_CARD__'

	export default {
		data() {
			return {
				keyword: '',
				list: [],
				allProducts: [],
				activeCovId: null,
				messages: [],
				messageInput: '',
				myUserId: null,
				currentUser: {},
				loadingList: false,
				loadingMessages: false,
				sending: false,
				aiBargaining: false,
				pollTimer: null,
				scrollTop: 0,
				pendingFocus: null,
				showEmojiPicker: false,
				activeEmojiGroup: 'face',
				emojiGroups: [
					{ key: 'face', name: '经典', items: ['😀', '😁', '😂', '🤣', '😊', '😇', '🙂', '😉', '😍', '😘', '😋', '😜', '😎', '🤓', '🤔', '🤗', '😳', '🥺', '😭', '😤', '😡', '😱', '😴', '🤒', '😵', '🤯', '🥳', '😅', '😆', '😬', '🙄', '😏', '😌', '😔', '😮', '🤭', '🤫', '🤐', '😷', '🤧', '🥰', '😚', '😛', '😝', '🤤', '😪', '😫', '😈'] },
					{ key: 'hand', name: '手势', items: ['👍', '👎', '👌', '✌️', '🤞', '🤟', '🤙', '👋', '👏', '🙌', '🙏', '💪', '🤝', '🫶', '☝️', '👉', '👈', '👇', '👆', '✋', '🤚', '🖐️', '🫡', '🤲'] },
					{ key: 'trade', name: '交易', items: ['💬', '💰', '💸', '🧾', '📦', '🚚', '🎁', '🏷️', '💳', '✅', '❌', '⚠️', '📌', '🔍', '🛒', '⭐', '🔥', '💡', '📮', '⏰', '🧡', '💯', '📱', '💻', '🎧', '📚', '🏠', '☕'] },
					{ key: 'heart', name: '心情', items: ['❤️', '🧡', '💛', '💚', '💙', '💜', '🤍', '🤎', '🖤', '💔', '💕', '💞', '💓', '💗', '💖', '💘', '💝', '✨', '🌟', '🎉', '🌈', '☀️', '🌙', '🍀'] }
				]
			}
		},
		computed: {
			isSeller() {
				return this.currentUser && this.currentUser.role === 'seller'
			},
			messageTitle() {
				return this.isSeller ? '卖家消息' : '消息中心'
			},
			emptyConversationText() {
				return this.isSeller ? '买家咨询你的商品后，会话会出现在这里。' : '从商品详情页点击“去问问”后，会话会出现在这里。'
			},
			emptyChatText() {
				return this.isSeller ? '左侧会展示买家对你店铺商品的咨询。' : '左侧会展示你和卖家的所有商品咨询。'
			},
			sideEmptyText() {
				return this.isSeller ? '选择会话后，这里会显示卖家处理功能。' : '商品推荐会在这里展示。'
			},
			inputPlaceholder() {
				return this.isSeller ? '请输入回复买家的内容...' : '请输入您想要咨询的内容...'
			},
			productCardButtonText() {
				return this.isSeller ? '发送商品' : '发送宝贝'
			},
			defaultAppendText() {
				return this.isSeller ? '您好，我来为您补充一下商品细节。' : '我想再了解一下商品成色和配件。'
			},
			quickOptions() {
				if (this.isSeller) {
					return ['您好，商品还在', '支持平台担保', '可以补充细节图', '今天可以发货', '配件信息如下', '价格可以小幅协商']
				}
				return ['可以便宜一点吗', '支持平台担保吗', '成色细节能发我看看吗', '今天可以发货吗', '配件齐全吗', '最低多少钱']
			},
			filteredList() {
				const word = this.keyword.trim().toLowerCase()
				if (!word) return this.list
				return this.list.filter(item => [item.title, item.sub, item.goodsName].some(value => String(value || '').toLowerCase().includes(word)))
			},
			activeConversation() {
				return this.list.find(item => item.covId === this.activeCovId) || null
			},
			currentEmojiOptions() {
				const group = this.emojiGroups.find(item => item.key === this.activeEmojiGroup)
				return group ? group.items : []
			},
			currentEmojiGroupName() {
				const group = this.emojiGroups.find(item => item.key === this.activeEmojiGroup)
				return group ? group.name : '经典'
			},
			focusProduct() {
				const active = this.activeConversation
				if (!active) return {}
				const found = this.allProducts.find(item => String(item.id) === String(active.goodsId))
				if (found) return found
				return {
					id: active.goodsId,
					title: active.goodsName,
					price: active.goodsPrice,
					cover: active.goodsImageUrl,
					category: active.goodsCategory,
					scene: active.goodsScene,
					storeId: active.storeId,
					shopName: active.title
				}
			},
			currentShopName() {
				return this.focusProduct.shopName || this.focusProduct.publisherName || (this.activeConversation && this.activeConversation.title) || '店铺'
			},
			sidePrimaryName() {
				if (!this.activeConversation) return ''
				return this.isSeller ? this.activeConversation.title : this.currentShopName
			},
			sideSecondaryText() {
				if (!this.activeConversation) return ''
				return this.isSeller
					? `买家咨询 · ${this.activeConversation.goodsCategory || '商品咨询'}`
					: `${this.activeConversation.title} · ${this.activeConversation.goodsCategory || '商品咨询'}`
			},
			recentStoreProducts() {
				const focus = this.focusProduct
				if (!focus || !focus.id) return []
				return this.allProducts
					.filter(item => {
						if (focus.storeId && item.storeId) return String(item.storeId) === String(focus.storeId)
						if (focus.publisherId && item.publisherId) return item.publisherId === focus.publisherId
						return item.shopName && focus.shopName && item.shopName === focus.shopName
					})
					.sort((a, b) => (String(a.id) === String(focus.id) ? -1 : String(b.id) === String(focus.id) ? 1 : 0))
					.slice(0, 4)
			},
			similarProducts() {
				const focus = this.focusProduct
				if (!focus || !focus.id) return []
				return this.allProducts
					.filter(item => String(item.id) !== String(focus.id))
					.filter(item => item.category === focus.category)
					.slice(0, 5)
			}
		},
		async onShow() {
			await this.loadMe()
			this.pendingFocus = this.consumePendingFocus()
			await Promise.all([this.fetchProducts(), this.fetchConversationList(!this.pendingFocus)])
			if (this.pendingFocus) await this.applyPendingFocus()
			this.startPolling()
		},
		onHide() {
			this.stopPolling()
		},
		onUnload() {
			this.stopPolling()
		},
		methods: {
			closeFloaters() {
				this.showEmojiPicker = false
			},
			consumePendingFocus() {
				try {
					const focus = uni.getStorageSync('pending_message_focus')
					uni.removeStorageSync('pending_message_focus')
					if (!focus || !focus.covId) return null
					if (focus.time && Date.now() - focus.time > 5 * 60 * 1000) return null
					return focus
				} catch (e) {
					return null
				}
			},
			async applyPendingFocus() {
				const focus = this.pendingFocus
				if (!focus) return
				if (focus.product && focus.product.id && !this.allProducts.some(item => String(item.id) === String(focus.product.id))) {
					this.allProducts.unshift(focus.product)
				}
				const target = this.list.find(item => String(item.covId) === String(focus.covId))
					|| this.list.find(item => String(item.goodsId) === String(focus.goodsId))
				if (target) {
					await this.selectConversation(target)
				} else if (this.list.length > 0 && !this.activeCovId) {
					await this.selectConversation(this.list[0])
				}
				this.pendingFocus = null
			},
			async loadMe() {
				try {
					const body = await fetchMe()
					this.currentUser = body && body.data ? body.data : {}
					this.myUserId = this.currentUser.userId || null
				} catch (e) {
					if (e.statusCode === 401) uni.showToast({ title: '请先登录', icon: 'none' })
				}
			},
			async fetchProducts() {
				try {
					const res = await get('/api/products')
					if (this.isSeller) {
						const mine = await get('/api/products/mine')
						this.allProducts = (mine.data && mine.data.data) ? mine.data.data : []
						return
					}
					this.allProducts = (res.data && res.data.data) ? res.data.data : []
				} catch (e) {
					console.error('加载商品失败', e)
				}
			},
			async fetchConversationList(autoSelect = false) {
				this.loadingList = true
				try {
					const res = await get('/api/chat/conversations')
					const conversationList = (res.data && res.data.data) ? res.data.data : []
					this.list = conversationList.map(item => ({
						icon: this.avatarText(item.targetName),
						title: item.targetName || '用户',
						sub: this.formatConversationLastMessage(item.lastMessage),
						time: this.formatTime(item.lastTime),
						covId: item.covId,
						unreadCount: item.unreadCount || 0,
						goodsId: item.goodsId,
						goodsName: item.goodsName,
						goodsPrice: item.goodsPrice,
						goodsCategory: item.goodsCategory,
						goodsScene: item.goodsScene,
						goodsImageUrl: item.goodsImageUrl,
						storeId: item.storeId,
						status: item.status || 'pending'
					}))
					if (autoSelect && !this.activeCovId && this.list.length > 0) await this.selectConversation(this.list[0])
				} catch (e) {
					console.error('加载会话列表失败', e)
					uni.showToast({ title: e.statusCode === 401 ? '请先登录后查看消息' : '加载会话失败', icon: 'none' })
				} finally {
					this.loadingList = false
				}
			},
			async selectConversation(item) {
				this.activeCovId = item.covId
				item.unreadCount = 0
				this.closeFloaters()
				await this.loadMessages(true)
				this.markAsRead()
			},
			async loadMessages(scrollToBottom = false) {
				if (!this.activeCovId) return
				this.loadingMessages = true
				try {
					const res = await get(`/api/chat/conversations/${this.activeCovId}/messages`)
					const raw = (res.data && res.data.data) ? res.data.data : []
					this.messages = raw.map((msg, index) => this.normalizeMessage(msg, raw[index - 1]))
					if (scrollToBottom) this.scrollToLatest()
				} catch (e) {
					console.error('加载聊天记录失败', e)
					uni.showToast({ title: '聊天启动失败，请重试', icon: 'none' })
				} finally {
					this.loadingMessages = false
				}
			},
			async refreshCurrent() {
				await this.fetchConversationList(false)
				await this.loadMessages(true)
			},
			startPolling() {
				this.stopPolling()
				this.pollTimer = setInterval(() => {
					if (this.activeCovId) {
						this.loadMessages(false)
						this.fetchConversationList(false)
					}
				}, 3500)
			},
			stopPolling() {
				if (this.pollTimer) {
					clearInterval(this.pollTimer)
					this.pollTimer = null
				}
			},
			async markAsRead() {
				if (!this.activeCovId) return
				try {
					await post(`/api/chat/${this.activeCovId}/read`)
				} catch (e) {
					console.error('标记已读失败', e)
				}
			},
			async sendMessage() {
				const content = this.messageInput.trim()
				if (!content || !this.activeCovId || this.sending) return
				await this.sendContent(content)
				this.messageInput = ''
				this.closeFloaters()
			},
			async sendContent(content) {
				this.sending = true
				try {
					const res = await post(`/api/chat/conversations/${this.activeCovId}/messages`, {
						covId: this.activeCovId,
						content,
						type: 'CHAT_MESSAGE'
					})
					const savedMessage = res.data && res.data.data ? res.data.data : null
					if (savedMessage) {
						const lastMsg = this.messages[this.messages.length - 1]
						this.messages.push(this.normalizeMessage(savedMessage, lastMsg))
					}
					this.scrollToLatest()
					await this.fetchConversationList(false)
				} catch (e) {
					console.error('发送消息失败', e)
					uni.showToast({ title: '发送失败，请重试', icon: 'none' })
				} finally {
					this.sending = false
				}
			},
			sendQuick(text) {
				this.messageInput = text
				this.sendMessage()
			},
			appendText(text) {
				this.messageInput = `${this.messageInput}${text}`.slice(0, 500)
			},
			toggleEmojiPicker() {
				this.showEmojiPicker = !this.showEmojiPicker
			},
			chooseEmoji(emoji) {
				this.appendText(emoji)
			},
			sendProductCard() {
				const product = this.focusProduct
				if (!product || !product.id) {
					uni.showToast({ title: '暂无商品信息', icon: 'none' })
					return
				}
				const card = {
					id: product.id,
					title: product.title || product.goodsName,
					price: product.price,
					cover: product.cover || product.goodsImageUrl,
					scene: product.scene,
					category: product.category,
					storeId: product.storeId,
					shopName: product.shopName || this.currentShopName
				}
				this.sendContent(`${PRODUCT_CARD_PREFIX}${JSON.stringify(card)}`)
				this.closeFloaters()
			},
			async triggerAiBargain() {
				if (!this.activeCovId || this.aiBargaining) return
				this.aiBargaining = true
				uni.showLoading({ title: 'AI 生成中' })
				try {
					const res = await post(`/api/chat/conversations/${this.activeCovId}/ai-bargain`)
					const data = res && res.data && res.data.data ? res.data.data : {}
					await this.refreshCurrent()
					uni.showToast({
						title: data.source === 'ai' ? 'AI 模型已生成' : 'AI 不可用，已用兜底建议',
						icon: 'none'
					})
				} catch (e) {
					console.error('AI 议价失败', e)
					uni.showToast({ title: 'AI 议价失败', icon: 'none' })
				} finally {
					this.aiBargaining = false
					uni.hideLoading()
				}
			},
			async updateConversationStatus(status) {
				if (!this.activeCovId) return
				try {
					await put(`/api/chat/conversations/${this.activeCovId}/status`, { status })
					const active = this.activeConversation
					if (active) active.status = status
					uni.showToast({ title: '状态已更新', icon: 'none' })
				} catch (e) {
					uni.showToast({ title: '状态更新失败', icon: 'none' })
				}
			},
			openSellerStore() {
				uni.navigateTo({ url: '/pages/seller/dashboard?tab=store' })
			},
			openSellerProducts() {
				uni.navigateTo({ url: '/pages/user/published' })
			},
			openProduct() {
				if (this.focusProduct && this.focusProduct.id) this.openProductById(this.focusProduct.id)
			},
			openProductById(id) {
				if (!id) return
				uni.navigateTo({ url: `/pages/goods/detail?id=${id}` })
			},
			navTo(url) {
				if (['/pages/home/home', '/pages/browse/browse', '/pages/cart/cart', '/pages/message/message', '/pages/user/index'].includes(url)) {
					uni.switchTab({ url })
					return
				}
				uni.navigateTo({ url })
			},
			normalizeMessage(msg, prevMsg) {
				return {
					...msg,
					card: this.parseProductCard(msg.content),
					showTime: this.shouldShowTime(msg, prevMsg)
				}
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
				return String(name || '聊').slice(0, 1).toUpperCase()
			},
			shouldShowTime(newMsg, prevMsg) {
				if (!prevMsg) return true
				const current = new Date(newMsg.createTime).getTime()
				const prev = new Date(prevMsg.createTime).getTime()
				if (Number.isNaN(current) || Number.isNaN(prev)) return false
				return current - prev > 5 * 60 * 1000
			},
			scrollToLatest() {
				this.$nextTick(() => {
					this.scrollTop = this.scrollTop === 999999 ? 999998 : 999999
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
			}
		}
	}
</script>

<style lang="scss" scoped>
	.safe-page {
		height: 100vh;
		overflow: hidden;
		background:
			radial-gradient(circle at 18% 8%, rgba(232, 243, 237, .72), transparent 34%),
			linear-gradient(135deg, #f7f8fb 0%, #eef3ef 100%);
	}
	.topbar {
		position: sticky;
		top: 0;
		z-index: 10;
		background: rgba(255,255,255,.88);
		backdrop-filter: blur(20px);
		border-bottom: 1px solid rgba(226, 232, 240, .8);
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
		width: 42px;
		height: 42px;
		border-radius: 8px;
		overflow: hidden;
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
		color: #121826;
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
		height: 50px;
		border-radius: 999px;
		background: rgba(255,255,255,.72);
		border: 1px solid rgba(203, 213, 225, .72);
		box-sizing: border-box;
	}
	.nav-link {
		width: 82px;
		height: 38px;
		display: flex;
		align-items: center;
		justify-content: center;
		border-radius: 999px;
		color: #5f6b85;
		font-size: 13px;
		font-weight: 800;
	}
	.nav-link.on,
	.nav-link:hover {
		background: linear-gradient(135deg, #ffffff, #f5f7fa);
		color: #12372a;
		box-shadow: 0 10px 26px rgba(18, 55, 42, .14);
	}
	.page-shell {
		height: calc(100vh - 82px);
		padding: 14px;
		box-sizing: border-box;
		overflow: hidden;
	}
	.message-workspace {
		height: 100%;
		display: grid;
		grid-template-columns: 286px minmax(520px, 1fr) 318px;
		gap: 12px;
	}
	.conversation-pane,
	.chat-pane,
	.info-pane {
		min-height: 0;
		border: 1px solid rgba(226, 232, 240, .95);
		background: rgba(255, 255, 255, .94);
		box-shadow: 0 18px 50px rgba(15, 23, 42, .08);
		overflow: hidden;
	}
	.conversation-pane,
	.info-pane {
		border-radius: 10px;
	}
	.chat-pane {
		border-radius: 12px;
		display: flex;
		flex-direction: column;
		background: #f7f8fb;
	}
	.conversation-pane {
		display: flex;
		flex-direction: column;
	}
	.pane-head {
		height: 74px;
		padding: 16px;
		display: flex;
		align-items: center;
		justify-content: space-between;
		box-sizing: border-box;
	}
	.pane-kicker,
	.pane-title {
		display: block;
	}
	.pane-kicker {
		font-size: 11px;
		color: #94a3b8;
		text-transform: uppercase;
	}
	.pane-title {
		margin-top: 3px;
		font-size: 22px;
		font-weight: 950;
		color: #111827;
	}
	.total-pill {
		min-width: 30px;
		height: 30px;
		padding: 0 8px;
		border-radius: 999px;
		display: flex;
		align-items: center;
		justify-content: center;
		background: #e8f3ed;
		color: #12372a;
		font-size: 13px;
		font-weight: 900;
		box-sizing: border-box;
	}
	.search-box {
		height: 40px;
		margin: 0 14px 12px;
		padding: 0 12px;
		display: flex;
		align-items: center;
		gap: 8px;
		border-radius: 8px;
		background: #f3f5f8;
		box-sizing: border-box;
	}
	.search-icon {
		font-size: 18px;
		color: #8a93a3;
	}
	.search-input {
		flex: 1;
		height: 100%;
		font-size: 14px;
		color: #111827;
	}
	.conversation-list {
		flex: 1;
		min-height: 0;
		padding: 0 8px 12px;
		box-sizing: border-box;
	}
	.conversation-item {
		display: flex;
		gap: 10px;
		padding: 12px 10px;
		margin-bottom: 6px;
		border-radius: 8px;
		border: 1px solid transparent;
		box-sizing: border-box;
		transition: background .18s ease, border-color .18s ease, transform .18s ease;
	}
	.conversation-item:hover,
	.conversation-item.active {
		background: linear-gradient(135deg, #f8fbf9, #fff);
		border-color: #cfe6d8;
	}
	.conversation-item.active {
		transform: translateX(2px);
	}
	.avatar-wrap {
		position: relative;
		width: 46px;
		height: 46px;
		flex-shrink: 0;
	}
	.avatar,
	.avatar-img,
	.chat-avatar,
	.seller-avatar,
	.mini-avatar {
		display: flex;
		align-items: center;
		justify-content: center;
		background: linear-gradient(135deg, #e8f3ed, #f8fbf9);
		color: #12372a;
		font-weight: 950;
	}
	.avatar,
	.avatar-img {
		width: 46px;
		height: 46px;
		border-radius: 8px;
	}
	.badge {
		position: absolute;
		top: -5px;
		right: -5px;
		min-width: 18px;
		height: 18px;
		padding: 0 5px;
		border-radius: 999px;
		background: #d66a2c;
		color: #fff;
		font-size: 12px;
		line-height: 18px;
		text-align: center;
		box-sizing: border-box;
	}
	.conversation-main {
		flex: 1;
		min-width: 0;
	}
	.conversation-head {
		display: flex;
		align-items: center;
		gap: 8px;
	}
	.conversation-name {
		flex: 1;
		min-width: 0;
		font-size: 15px;
		font-weight: 900;
		color: #111827;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}
	.conversation-time {
		font-size: 12px;
		color: #9aa3af;
	}
	.conversation-sub,
	.goods-line {
		display: block;
		margin-top: 5px;
		font-size: 12px;
		color: #8792a2;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}
	.goods-line {
		color: #12372a;
	}
	.chat-header {
		height: 72px;
		padding: 0 22px;
		display: flex;
		align-items: center;
		justify-content: space-between;
		background: rgba(255,255,255,.96);
		border-bottom: 1px solid #e5e7eb;
		box-sizing: border-box;
	}
	.chat-contact {
		display: flex;
		align-items: center;
		gap: 12px;
		min-width: 0;
	}
	.chat-avatar {
		width: 42px;
		height: 42px;
		border-radius: 10px;
		flex-shrink: 0;
	}
	.chat-title,
	.chat-subtitle {
		display: block;
	}
	.chat-title {
		font-size: 20px;
		font-weight: 950;
		color: #111827;
	}
	.chat-subtitle {
		margin-top: 4px;
		max-width: 520px;
		font-size: 13px;
		color: #8792a2;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}
	.header-actions {
		display: flex;
		gap: 8px;
	}
	button {
		display: flex;
		align-items: center;
		justify-content: center;
		margin: 0;
		padding: 0;
		border: 0;
		line-height: normal;
		text-align: center;
		box-sizing: border-box;
		vertical-align: middle;
	}
	button::after {
		border: none;
	}
	.ghost-btn,
	.quick-btn,
	.text-btn,
	.link-btn {
		height: 32px;
		padding: 0 13px;
		align-items: center;
		justify-content: center;
		border-radius: 8px;
		background: #fff;
		border: 1px solid #e5e7eb;
		color: #334155;
		font-size: 13px;
	}
	.ghost-btn:hover,
	.quick-btn:hover,
	.link-btn:hover {
		border-color: #cfe6d8;
		color: #12372a;
	}
	.messages {
		flex: 1;
		height: 0;
		min-height: 0;
		padding: 18px 24px;
		box-sizing: border-box;
		background:
			linear-gradient(90deg, rgba(226,232,240,.55) 1px, transparent 1px),
			linear-gradient(180deg, #f7f8fb, #f4f6fa);
		background-size: 28px 28px, 100% 100%;
	}
	.session-tip {
		display: flex;
		align-items: center;
		justify-content: center;
		gap: 12px;
		margin: 0 auto 16px;
		color: #9aa3af;
		font-size: 12px;
	}
	.line {
		width: 84px;
		height: 1px;
		background: #d7dce5;
	}
	.message-row {
		display: flex;
		align-items: flex-start;
		gap: 10px;
		margin-bottom: 16px;
	}
	.message-row.mine {
		justify-content: flex-end;
	}
	.mini-avatar {
		width: 32px;
		height: 32px;
		border-radius: 8px;
		font-size: 14px;
		flex-shrink: 0;
	}
	.bubble-wrap {
		max-width: min(72%, 680px);
	}
	.message-row.mine .bubble-wrap {
		display: flex;
		flex-direction: column;
		align-items: flex-end;
	}
	.message-time {
		display: block;
		margin-bottom: 5px;
		font-size: 11px;
		color: #9aa3af;
	}
	.bubble {
		padding: 11px 14px;
		border-radius: 10px;
		background: #fff;
		color: #1f2937;
		font-size: 15px;
		line-height: 1.6;
		box-shadow: 0 8px 24px rgba(15, 23, 42, .06);
		word-break: break-word;
	}
	.message-row.mine .bubble {
		background: linear-gradient(135deg, #12372a, #1f5c43);
		color: #fff;
	}
	.read-receipt {
		display: block;
		margin-top: 5px;
		font-size: 11px;
		color: #94a3b8;
		text-align: right;
	}
	.read-receipt.read {
		color: #12372a;
		font-weight: 850;
	}
	.message-product-card {
		width: 360px;
		max-width: 100%;
		display: flex;
		gap: 12px;
		padding: 12px;
		border-radius: 12px;
		background: #fff;
		border: 1px solid #e5e7eb;
		box-shadow: 0 12px 30px rgba(15, 23, 42, .08);
		box-sizing: border-box;
	}
	.message-row.mine .message-product-card {
		border-color: #cfe6d8;
	}
	.card-thumb {
		width: 84px;
		height: 84px;
		border-radius: 8px;
		flex-shrink: 0;
	}
	.card-body {
		min-width: 0;
		flex: 1;
	}
	.card-label {
		display: block;
		font-size: 12px;
		color: #12372a;
		font-weight: 900;
	}
	.card-title {
		display: -webkit-box;
		-webkit-line-clamp: 2;
		-webkit-box-orient: vertical;
		overflow: hidden;
		margin-top: 6px;
		font-size: 15px;
		font-weight: 900;
		color: #111827;
		line-height: 1.45;
	}
	.card-meta {
		display: flex;
		align-items: center;
		gap: 8px;
		margin-top: 10px;
	}
	.card-price {
		color: #12372a;
		font-size: 18px;
		font-weight: 950;
	}
	.card-tag,
	.tag,
	.mini-tag {
		padding: 3px 7px;
		border-radius: 999px;
		background: #e8f3ed;
		color: #12372a;
		font-size: 11px;
		font-weight: 800;
	}
	.quick-row {
		width: 100%;
		height: 50px;
		white-space: nowrap;
		padding: 7px 18px;
		background: #fff;
		border-top: 1px solid #e5e7eb;
		box-sizing: border-box;
	}
	.quick-track {
		display: inline-block;
		height: 36px;
		white-space: nowrap;
	}
	.quick-track::after {
		content: "";
		display: inline-block;
		width: 18px;
		height: 1px;
	}
	.quick-btn {
		display: inline-flex;
		align-items: center;
		justify-content: center;
		width: auto !important;
		min-width: 0;
		height: 34px;
		padding: 0 15px;
		margin-right: 8px;
		border-radius: 999px;
		white-space: nowrap;
		line-height: normal;
		background: #f8fafc;
		color: #334155;
		font-size: 13px;
		font-weight: 750;
		box-shadow: inset 0 0 0 1px #e5e7eb;
	}
	.composer {
		position: relative;
		min-height: 142px;
		padding: 10px 18px 12px;
		background: #fff;
		border-top: 1px solid #e5e7eb;
		box-sizing: border-box;
	}
	.tool-row {
		display: flex;
		align-items: center;
		gap: 10px;
	}
	.tool-btn {
		display: flex;
		align-items: center;
		justify-content: center;
		width: 30px;
		height: 30px;
		border-radius: 8px;
		background: #f8fafc;
		color: #111827;
		font-size: 19px;
	}
	.tool-btn.active,
	.tool-btn:hover {
		background: #e8f3ed;
		color: #12372a;
	}
	.counter {
		margin-left: auto;
		color: #9aa3af;
		font-size: 12px;
	}
	.emoji-panel {
		position: absolute;
		left: 14px;
		bottom: 122px;
		width: 520px;
		height: 362px;
		border-radius: 12px;
		background: #fff;
		border: 1px solid #e5e7eb;
		box-shadow: 0 18px 60px rgba(15, 23, 42, .18);
		z-index: 12;
		overflow: hidden;
	}
	.emoji-title {
		height: 44px;
		padding: 0 16px;
		display: flex;
		align-items: center;
		color: #6b7280;
		font-size: 15px;
		font-weight: 850;
		box-sizing: border-box;
	}
	.emoji-tabs {
		height: 54px;
		padding: 6px 12px;
		display: flex;
		gap: 6px;
		border-top: 1px solid #eef2f7;
		box-sizing: border-box;
	}
	.emoji-tab {
		display: flex;
		align-items: center;
		justify-content: center;
		min-width: 76px;
		height: 42px;
		padding: 0 16px;
		border-radius: 8px;
		background: #f8fafc;
		color: #64748b;
		font-size: 15px;
		line-height: normal;
	}
	.emoji-tab.on {
		background: #e8f3ed;
		color: #12372a;
		font-weight: 900;
	}
	.emoji-grid {
		height: 264px;
		padding: 4px 14px 12px;
		box-sizing: border-box;
	}
	.emoji-grid-inner {
		display: grid;
		grid-template-columns: repeat(12, 34px);
		justify-content: space-between;
		gap: 8px 0;
		align-items: center;
		padding-bottom: 10px;
		box-sizing: border-box;
	}
	.emoji-item {
		display: flex;
		align-items: center;
		justify-content: center;
		width: 34px;
		height: 34px;
		margin: 0;
		border-radius: 9px;
		background: #fff;
		font-size: 26px;
		line-height: normal;
	}
	.emoji-item:hover {
		background: #f1f5f9;
	}
	.message-input {
		width: 100%;
		height: 58px;
		margin-top: 7px;
		padding: 0;
		color: #111827;
		font-size: 15px;
		line-height: 1.6;
		box-sizing: border-box;
	}
	.composer-actions {
		display: flex;
		justify-content: flex-end;
		gap: 8px;
	}
	.send-btn {
		display: flex;
		align-items: center;
		justify-content: center;
		width: 72px;
		height: 34px;
		border-radius: 8px;
		background: #12372a;
		color: #fff;
		font-size: 14px;
		font-weight: 950;
	}
	.send-btn.secondary {
		width: 104px;
		background: #f8fafc;
		color: #475569;
		border: 1px solid #e2e8f0;
		box-shadow: none;
	}
	.send-btn.secondary:hover {
		background: #f8fbf9;
		color: #12372a;
		border-color: #cfe6d8;
	}
	.send-btn[disabled] {
		background: #cbd5e1;
	}
	.info-pane {
		padding: 16px;
		box-sizing: border-box;
		overflow: hidden;
	}
	.seller-card {
		display: flex;
		gap: 12px;
		align-items: center;
		padding: 12px;
		border-radius: 10px;
		background: linear-gradient(135deg, #f8fbf9, #fff);
		border: 1px solid #cfe6d8;
	}
	.seller-avatar {
		width: 54px;
		height: 54px;
		border-radius: 10px;
		font-size: 20px;
		flex-shrink: 0;
	}
	.seller-main {
		min-width: 0;
	}
	.seller-name,
	.seller-desc,
	.block-title {
		display: block;
	}
	.seller-name {
		font-size: 17px;
		font-weight: 950;
		color: #111827;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}
	.seller-desc {
		margin-top: 5px;
		font-size: 12px;
		color: #8792a2;
	}
	.side-section {
		margin-top: 14px;
	}
	.section-head {
		display: flex;
		align-items: center;
		justify-content: space-between;
		margin-bottom: 8px;
	}
	.block-title {
		margin-bottom: 8px;
		font-size: 14px;
		font-weight: 950;
		color: #111827;
	}
	.text-btn {
		height: 28px;
		padding: 0 12px;
		border-radius: 999px;
		background: #f8fafc;
		color: #64748b;
		border: 1px solid #e2e8f0;
		font-size: 12px;
		font-weight: 850;
	}
	.text-btn:hover {
		background: #f8fbf9;
		color: #12372a;
		border-color: #cfe6d8;
	}
	.focus-product {
		display: flex;
		gap: 10px;
		padding: 10px;
		border-radius: 10px;
		background: #f8fafc;
		border: 1px solid #eef2f7;
	}
	.focus-img,
	.focus-placeholder {
		width: 86px;
		height: 86px;
		border-radius: 8px;
		flex-shrink: 0;
	}
	.focus-placeholder {
		display: flex;
		align-items: center;
		justify-content: center;
		background: #e5e7eb;
		color: #64748b;
		font-size: 13px;
	}
	.focus-main {
		flex: 1;
		min-width: 0;
	}
	.focus-title {
		display: -webkit-box;
		-webkit-line-clamp: 2;
		-webkit-box-orient: vertical;
		overflow: hidden;
		font-size: 14px;
		font-weight: 850;
		color: #111827;
		line-height: 1.45;
	}
	.focus-price {
		display: block;
		margin-top: 8px;
		color: #12372a;
		font-size: 18px;
		font-weight: 950;
	}
	.tag-row {
		display: flex;
		flex-wrap: wrap;
		gap: 6px;
		margin-top: 7px;
	}
	.link-btn {
		width: 100%;
		height: 36px;
		margin-top: 10px;
		border-radius: 9px;
		background: #fff;
		color: #334155;
		border: 1px solid #e2e8f0;
		font-weight: 900;
		box-shadow: 0 8px 20px rgba(15, 23, 42, .05);
	}
	.link-btn:hover {
		background: #f8fbf9;
		color: #12372a;
		border-color: #cfe6d8;
	}
	.seller-action-grid,
	.status-row {
		display: grid;
		gap: 8px;
	}
	.seller-action-grid {
		grid-template-columns: repeat(2, minmax(0, 1fr));
	}
	.seller-fn-btn,
	.status-btn {
		min-height: 36px;
		padding: 0 10px;
		border-radius: 8px;
		background: #f8fafc;
		border: 1px solid #e2e8f0;
		color: #334155;
		font-size: 12px;
		font-weight: 850;
	}
	.seller-fn-btn:hover,
	.status-btn:hover,
	.status-btn.on {
		background: #f8fbf9;
		border-color: #cfe6d8;
		color: #12372a;
	}
	.status-row {
		grid-template-columns: 1fr;
	}
	.side-product-list {
		max-height: 170px;
	}
	.side-product-list.compact {
		max-height: 190px;
	}
	.mini-product {
		display: flex;
		gap: 9px;
		padding: 8px;
		margin-bottom: 8px;
		border-radius: 9px;
		background: #fff;
		border: 1px solid #eef2f7;
	}
	.mini-product:hover {
		border-color: #cfe6d8;
		background: #f8fbf9;
	}
	.mini-product-img {
		width: 52px;
		height: 52px;
		border-radius: 7px;
		flex-shrink: 0;
	}
	.mini-product-main {
		flex: 1;
		min-width: 0;
	}
	.mini-product-title {
		display: -webkit-box;
		-webkit-line-clamp: 2;
		-webkit-box-orient: vertical;
		overflow: hidden;
		color: #111827;
		font-size: 12px;
		line-height: 1.35;
	}
	.mini-product-price {
		display: block;
		margin-top: 5px;
		color: #12372a;
		font-size: 13px;
		font-weight: 950;
	}
	.mini-product-tags {
		display: flex;
		align-items: center;
		gap: 6px;
	}
	.mini-tag {
		margin-top: 5px;
		background: #f1f5f9;
		color: #64748b;
	}
	.side-empty-mini {
		padding: 16px 8px;
		text-align: center;
		color: #9aa3af;
		font-size: 12px;
	}
	.empty-list,
	.empty-chat,
	.chat-empty-state,
	.side-empty {
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		padding: 44px 20px;
		text-align: center;
		color: #94a3b8;
	}
	.chat-empty-state {
		height: 100%;
		box-sizing: border-box;
	}
	.side-empty {
		height: 360px;
	}
	.empty-title {
		font-size: 17px;
		font-weight: 950;
		color: #475569;
	}
	.empty-sub {
		margin-top: 8px;
		font-size: 13px;
		color: #94a3b8;
		line-height: 1.6;
	}
	@media (max-width: 1120px) {
		.message-workspace {
			grid-template-columns: 260px minmax(440px, 1fr) 280px;
		}
	}
	@media (max-width: 920px) {
		.safe-page {
			height: auto;
			overflow: auto;
		}
		.topbar-inner {
			grid-template-columns: 1fr;
			height: auto;
			padding: 12px;
		}
		.web-nav {
			justify-self: stretch;
			overflow-x: auto;
		}
		.page-shell {
			height: auto;
		}
		.message-workspace {
			height: auto;
			grid-template-columns: 1fr;
		}
		.conversation-list {
			max-height: 330px;
		}
		.chat-pane {
			height: 680px;
		}
		.emoji-panel {
			width: calc(100vw - 52px);
		}
	}
</style>
