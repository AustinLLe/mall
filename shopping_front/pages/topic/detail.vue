<template>
	<view class="safe-page topic-page">
		<view class="topbar">
			<view class="content-wrap topbar-inner">
				<view class="brand" @click="navTo('/pages/home/home')">
					<view class="brand-mark">
						<image class="brand-logo" src="/static/logo.png" mode="aspectFit"></image>
					</view>
					<view>
						<text class="brand-name">松果集市</text>
						<text class="brand-sub">话题、经验和真实商品讨论</text>
					</view>
				</view>
				<view class="web-nav">
					<text class="nav-link" @click="navTo('/pages/home/home')">首页</text>
					<text class="nav-link on" @click="navTo('/pages/browse/browse')">发现</text>
					<text class="nav-link" @click="navTo('/pages/cart/cart')">购物车</text>
					<text class="nav-link" @click="navTo('/pages/message/message')">消息</text>
					<text class="nav-link" @click="navTo('/pages/user/index')">我的</text>
				</view>
				<view class="top-actions">
					<view class="back-topic" @click="navTo('/pages/browse/browse')">返回发现</view>
				</view>
			</view>
		</view>
		<scroll-view scroll-y class="scroll">
			<view class="content-wrap page">
				<view class="hero">
					<view class="cover" :class="{ 'has-image': isImageUrl(topic.cover) }">
						<image v-if="isImageUrl(topic.cover)" class="cover-img" :src="resolveImageUrl(topic.cover)" mode="aspectFill"></image>
						<text v-else>{{ topic.type || '话题' }}</text>
					</view>
					<view class="hero-main">
						<view class="hero-line">
							<text class="badge">{{ topic.type || '话题' }}</text>
							<text class="heat">{{ topic.heat || '0 帖 · 0 赞' }}</text>
						</view>
						<text class="title">{{ topic.title || '话题讨论' }}</text>
						<text class="desc">{{ topic.desc || '一起讨论这个话题。' }}</text>
						<view class="tags">
							<text v-for="tag in topic.tags || []" :key="tag" class="tag">{{ tag }}</text>
						</view>
					</view>
				</view>

				<view class="layout">
					<view class="feed">
						<view class="composer">
							<view class="composer-head">
								<view class="avatar">我</view>
								<view>
									<text class="composer-title">发布讨论</text>
									<text class="composer-sub">可以添加图片、商品卡片或店铺卡片</text>
								</view>
							</view>
							<textarea v-model="postContent" class="post-input" maxlength="500" placeholder="分享真实体验、避坑点、购买建议或者晒单心得..." />

							<view v-if="postImages.length" class="draft-images">
								<view v-for="(img, index) in postImages" :key="img" class="draft-image-wrap">
									<image class="draft-image" :src="resolveImageUrl(img)" mode="aspectFill"></image>
									<text class="remove-image" @click="removeImage(index)">×</text>
								</view>
							</view>

							<view v-if="selectedProduct" class="attach-preview">
								<view class="attach-cover" :class="{ 'has-image': isImageUrl(selectedProduct.cover) }">
									<image v-if="isImageUrl(selectedProduct.cover)" class="cover-img" :src="resolveImageUrl(selectedProduct.cover)" mode="aspectFill"></image>
									<text v-else>商品</text>
								</view>
								<view class="attach-main">
									<text class="attach-type">推荐商品</text>
									<text class="attach-title">{{ selectedProduct.title }}</text>
									<text class="attach-meta">¥{{ selectedProduct.price }} · {{ selectedProduct.scene === 'new' ? '新品' : '二手' }}</text>
								</view>
								<text class="clear-attach" @click="selectedProductId = ''">移除</text>
							</view>

							<view v-if="selectedStore" class="attach-preview store-preview">
								<view class="store-mark">{{ (selectedStore.name || '店').slice(0, 1) }}</view>
								<view class="attach-main">
									<text class="attach-type">推荐店铺</text>
									<text class="attach-title">{{ selectedStore.name }}</text>
									<text class="attach-meta">评分 {{ selectedStore.score }} · {{ selectedStore.badge }}</text>
								</view>
								<text class="clear-attach" @click="selectedStoreId = ''">移除</text>
							</view>

							<view class="composer-tools">
								<button class="tool-btn" :disabled="uploading" @click="chooseImages">{{ uploading ? '上传中' : '图片' }}</button>
								<button class="tool-btn" @click="focusProductPanel">商品卡片</button>
								<button class="tool-btn" @click="focusStorePanel">店铺卡片</button>
								<text class="count">{{ postContent.length }}/500</text>
								<button class="post-btn" :disabled="posting" @click="publishPost">{{ posting ? '发布中...' : '发布' }}</button>
							</view>
						</view>

						<view v-for="post in posts" :key="post.id" class="post-card">
							<view class="post-head">
								<view class="avatar">{{ (post.author || '松').slice(0, 1) }}</view>
								<view class="post-user">
									<text class="post-author">{{ post.author }}</text>
									<text class="post-time">{{ post.createdAt }}</text>
								</view>
							</view>
							<text class="post-text">{{ post.content }}</text>

							<view v-if="post.images && post.images.length" class="post-images" :class="'cols-' + Math.min(post.images.length, 3)">
								<image v-for="img in post.images" :key="img" class="post-image" :src="resolveImageUrl(img)" mode="aspectFill"></image>
							</view>

							<view v-if="post.product" class="post-attach" @click="openGoods(post.product)">
								<view class="attach-cover" :class="{ 'has-image': isImageUrl(post.product.cover) }">
									<image v-if="isImageUrl(post.product.cover)" class="cover-img" :src="resolveImageUrl(post.product.cover)" mode="aspectFill"></image>
									<text v-else>商品</text>
								</view>
								<view class="attach-main">
									<text class="attach-type">帖子推荐商品</text>
									<text class="attach-title">{{ post.product.title }}</text>
									<text class="attach-meta">¥{{ post.product.price }} · {{ post.product.category }}</text>
								</view>
								<text class="open-link">查看</text>
							</view>

							<view v-if="post.store" class="post-attach store-preview" @click="openStore(post.store)">
								<view class="store-mark">{{ (post.store.name || '店').slice(0, 1) }}</view>
								<view class="attach-main">
									<text class="attach-type">帖子推荐店铺</text>
									<text class="attach-title">{{ post.store.name }}</text>
									<text class="attach-meta">评分 {{ post.store.score }} · {{ post.store.fans }} 关注</text>
								</view>
								<text class="open-link">进店</text>
							</view>

							<view class="post-actions">
								<button class="action" :class="{ on: post.liked }" @click="likePost(post)">赞 {{ post.likeCount || 0 }}</button>
								<button class="action" :class="{ on: post.wanted }" @click="toggleAction(post, 'want')">种草 {{ post.wantCount || 0 }}</button>
								<button class="action" :class="{ on: post.collected }" @click="toggleAction(post, 'collect')">收藏 {{ post.collectCount || 0 }}</button>
								<button class="action" @click="toggleComment(post)">评论 {{ post.commentCount || 0 }}</button>
							</view>

							<view v-if="post.comments && post.comments.length" class="comment-list">
								<view v-for="comment in post.comments" :key="comment.id" class="comment">
									<text class="comment-author">{{ comment.author }}：</text>
									<text class="comment-text">{{ comment.content }}</text>
								</view>
							</view>
							<view v-if="commentingPostId === post.id" class="comment-box">
								<input v-model="commentText" class="comment-input" placeholder="写下你的评论" />
								<button class="comment-btn" @click="sendComment(post)">发送</button>
							</view>
						</view>

						<view v-if="posts.length === 0" class="empty">暂无帖子，来发布第一条讨论吧。</view>
					</view>

					<view class="side">
						<view class="side-card">
							<text class="side-title">选择商品卡片</text>
							<scroll-view scroll-y class="pick-list" id="productPicker">
								<view v-for="item in relatedGoods" :key="item.id" class="pick-item" :class="{ on: selectedProductId === item.id }" @click="selectedProductId = selectedProductId === item.id ? '' : item.id">
									<view class="pick-cover" :class="{ 'has-image': isImageUrl(item.cover) }">
										<image v-if="isImageUrl(item.cover)" class="cover-img" :src="resolveImageUrl(item.cover)" mode="aspectFill"></image>
										<text v-else>{{ item.category || '商品' }}</text>
									</view>
									<view>
										<text class="pick-title">{{ item.title }}</text>
										<text class="pick-meta">¥{{ item.price }} · {{ item.scene === 'new' ? '新品' : '二手' }}</text>
									</view>
								</view>
							</scroll-view>
						</view>

						<view class="side-card">
							<text class="side-title">选择店铺卡片</text>
							<view v-for="store in stores" :key="store.id" class="pick-item store-pick" :class="{ on: selectedStoreId === store.id }" @click="selectedStoreId = selectedStoreId === store.id ? '' : store.id">
								<view class="store-mark">{{ (store.name || '店').slice(0, 1) }}</view>
								<view>
									<text class="pick-title">{{ store.name }}</text>
									<text class="pick-meta">评分 {{ store.score }} · {{ store.badge }}</text>
								</view>
							</view>
						</view>
					</view>
				</view>
			</view>
		</scroll-view>
	</view>
</template>

<script>
	import { buildGoodsDetailUrl } from '../../data/catalog.js'
	import { buildRequestUrl } from '@/config/env.js'
	import { createTopicComment, createTopicPost, fetchProducts, fetchStores, fetchTopic, fetchTopicPosts, toggleTopicPostAction, toggleTopicPostLike } from '@/services/shop.js'
	import { isImageUrl, resolveImageUrl } from '@/utils/media.js'
	import { pickErrorMessage } from '@/utils/auth.js'

	export default {
		data() {
			return {
				topicId: '',
				topic: {},
				posts: [],
				relatedGoodsList: [],
				stores: [],
				postContent: '',
				postImages: [],
				selectedProductId: '',
				selectedStoreId: '',
				posting: false,
				uploading: false,
				commentingPostId: '',
				commentText: ''
			}
		},
		computed: {
			relatedGoods() {
				const tags = this.topic.tags || []
				const matched = this.relatedGoodsList.filter(item => tags.some(tag => [item.category, item.scene, item.title, item.subtitle].join(' ').includes(tag)))
				return (matched.length ? matched : this.relatedGoodsList).slice(0, 8)
			},
			selectedProduct() {
				return this.relatedGoodsList.find(item => item.id === this.selectedProductId) || null
			},
			selectedStore() {
				return this.stores.find(item => item.id === this.selectedStoreId) || null
			}
		},
		onLoad(query) {
			this.topicId = query && query.id ? decodeURIComponent(query.id) : ''
			this.loadAll()
		},
		methods: {
			isImageUrl,
			resolveImageUrl,
			async loadAll() {
				await Promise.all([this.loadTopic(), this.loadPosts(), this.loadRelatedGoods(), this.loadStores()])
			},
			async loadTopic() {
				try {
					const body = await fetchTopic(this.topicId)
					this.topic = body && body.code === 0 ? body.data : {}
				} catch (e) {
					uni.showToast({ title: pickErrorMessage(e) || '话题加载失败', icon: 'none' })
				}
			},
			async loadPosts() {
				try {
					const body = await fetchTopicPosts(this.topicId)
					this.posts = body && body.code === 0 && Array.isArray(body.data) ? body.data : []
				} catch (e) {
					this.posts = []
				}
			},
			async loadRelatedGoods() {
				try {
					const body = await fetchProducts()
					this.relatedGoodsList = body && body.code === 0 && Array.isArray(body.data) ? body.data : []
				} catch (e) {
					this.relatedGoodsList = []
				}
			},
			async loadStores() {
				try {
					const body = await fetchStores()
					this.stores = body && body.code === 0 && Array.isArray(body.data) ? body.data : []
				} catch (e) {
					this.stores = []
				}
			},
			chooseImages() {
				if (this.uploading) return
				if (this.postImages.length >= 6) {
					uni.showToast({ title: '最多上传 6 张图片', icon: 'none' })
					return
				}
				uni.chooseImage({
					count: 6 - this.postImages.length,
					success: async (res) => {
						const files = res.tempFilePaths || []
						if (!files.length) return
						this.uploading = true
						uni.showLoading({ title: '上传中...' })
						try {
							for (const filePath of files) {
								const url = await this.uploadOne(filePath)
								if (url) this.postImages.push(url)
							}
							uni.showToast({ title: '上传成功' })
						} catch (e) {
							uni.showToast({ title: '图片上传失败', icon: 'none' })
						} finally {
							this.uploading = false
							uni.hideLoading()
						}
					}
				})
			},
			uploadOne(filePath) {
				return new Promise((resolve, reject) => {
					uni.uploadFile({
						url: buildRequestUrl('/api/upload/image'),
						filePath,
						name: 'file',
						success: (uploadRes) => {
							let data = {}
							try {
								data = typeof uploadRes.data === 'string' ? JSON.parse(uploadRes.data) : uploadRes.data
							} catch (e) {
								data = {}
							}
							if (uploadRes.statusCode >= 200 && uploadRes.statusCode < 300 && (data.code === 200 || data.code === 0) && data.data) {
								resolve(data.data)
								return
							}
							reject(new Error(data.message || '上传失败'))
						},
						fail: reject
					})
				})
			},
			removeImage(index) {
				this.postImages.splice(index, 1)
			},
			focusProductPanel() {
				uni.showToast({ title: '在右侧选择商品卡片', icon: 'none' })
			},
			focusStorePanel() {
				uni.showToast({ title: '在右侧选择店铺卡片', icon: 'none' })
			},
			async publishPost() {
				if (!this.postContent.trim()) {
					uni.showToast({ title: '请先写点内容', icon: 'none' })
					return
				}
				this.posting = true
				try {
					const body = await createTopicPost(this.topicId, {
						content: this.postContent.trim(),
						images: this.postImages,
						productId: this.selectedProductId,
						storeId: this.selectedStoreId
					})
					this.posts = body.data || []
					this.postContent = ''
					this.postImages = []
					this.selectedProductId = ''
					this.selectedStoreId = ''
					this.loadTopic()
					uni.showToast({ title: '已发布', icon: 'success' })
				} catch (e) {
					if (e && e.statusCode === 401) uni.navigateTo({ url: '/pages/auth/login' })
					uni.showToast({ title: pickErrorMessage(e) || '发布失败', icon: 'none' })
				} finally {
					this.posting = false
				}
			},
			async likePost(post) {
				try {
					const body = await toggleTopicPostLike(post.id)
					this.posts = body.data || []
					this.loadTopic()
				} catch (e) {
					if (e && e.statusCode === 401) uni.navigateTo({ url: '/pages/auth/login' })
					uni.showToast({ title: pickErrorMessage(e) || '点赞失败', icon: 'none' })
				}
			},
			async toggleAction(post, actionType) {
				try {
					const body = await toggleTopicPostAction(post.id, actionType)
					this.posts = body.data || []
				} catch (e) {
					if (e && e.statusCode === 401) uni.navigateTo({ url: '/pages/auth/login' })
					uni.showToast({ title: pickErrorMessage(e) || '操作失败', icon: 'none' })
				}
			},
			toggleComment(post) {
				this.commentingPostId = this.commentingPostId === post.id ? '' : post.id
				this.commentText = ''
			},
			async sendComment(post) {
				if (!this.commentText.trim()) {
					uni.showToast({ title: '请输入评论', icon: 'none' })
					return
				}
				try {
					const body = await createTopicComment(post.id, { content: this.commentText.trim() })
					this.posts = body.data || []
					this.commentText = ''
					this.commentingPostId = ''
					this.loadTopic()
				} catch (e) {
					if (e && e.statusCode === 401) uni.navigateTo({ url: '/pages/auth/login' })
					uni.showToast({ title: pickErrorMessage(e) || '评论失败', icon: 'none' })
				}
			},
			openGoods(item) {
				uni.navigateTo({ url: buildGoodsDetailUrl(item) })
			},
			openStore(store) {
				uni.navigateTo({ url: '/pages/store/store?name=' + encodeURIComponent(store.name) })
			},
			navTo(url) {
				if (['/pages/home/home', '/pages/browse/browse', '/pages/cart/cart', '/pages/message/message', '/pages/user/index'].includes(url)) {
					uni.switchTab({ url })
					return
				}
				uni.navigateTo({ url })
			}
		}
	}
</script>

<style lang="scss" scoped>
	.topic-page { min-height: 100vh; background: #f3f5f7; }
	.topbar { position: sticky; top: 0; z-index: 10; background: rgba(255,255,255,.9); backdrop-filter: blur(22px); border-bottom: 1px solid rgba(203,213,225,.55); box-shadow: 0 10px 40px rgba(60,64,67,.06); }
	.topbar-inner { display: grid; grid-template-columns: 300px 320px minmax(0, 1fr); align-items: center; gap: 18px; height: 82px; padding: 0 22px; }
	.brand { display: flex; align-items: center; gap: 12px; }
	.brand-mark { width: 42px; height: 42px; border-radius: 8px; overflow: hidden; flex-shrink: 0; }
	.brand-logo { width: 100%; height: 100%; }
	.brand-name, .brand-sub { display: block; }
	.brand-name { font-size: 20px; font-weight: 900; color: #202124; }
	.brand-sub { margin-top: 2px; font-size: 12px; color: #667085; }
	.web-nav { justify-self: center; display: flex; align-items: center; gap: 4px; padding: 5px; height: 50px; border-radius: 999px; background: rgba(255,255,255,.72); border: 1px solid rgba(203,213,225,.72); box-sizing: border-box; box-shadow: 0 14px 38px rgba(60,64,67,.08); }
	.nav-link { width: 82px; height: 38px; border-radius: 999px; display: flex; align-items: center; justify-content: center; color: #5f6b85; font-size: 13px; font-weight: 800; }
	.nav-link.on, .nav-link:hover { background: #12372a; color: #fff; }
	.top-actions { justify-self: end; }
	.back-topic { height: 44px; padding: 0 18px; border-radius: 999px; background: #12372a; color: #fff; display: flex; align-items: center; justify-content: center; font-size: 13px; font-weight: 900; }
	.scroll { height: calc(100vh - 82px); }
	.page { padding: 28rpx; }
	.hero, .composer, .post-card, .side-card { background: #fff; border: 1rpx solid #e5e9ef; border-radius: 8px; box-shadow: 0 14rpx 34rpx rgba(18, 32, 46, .06); }
	.hero { display: grid; grid-template-columns: 360rpx minmax(0, 1fr); gap: 30rpx; padding: 28rpx; align-items: center; }
	.cover { height: 260rpx; border-radius: 8px; overflow: hidden; background: linear-gradient(135deg, #eaf3ef, #f7efe4); display: flex; align-items: center; justify-content: center; color: #12372a; font-size: 38rpx; font-weight: 900; }
	.cover-img, .draft-image { width: 100%; height: 100%; display: block; }
	.hero-line, .tags, .composer-head, .composer-tools, .post-head, .post-actions { display: flex; align-items: center; gap: 12rpx; flex-wrap: wrap; }
	.hero-line { justify-content: space-between; }
	.badge, .tag { display: inline-flex; align-items: center; justify-content: center; min-height: 42rpx; padding: 0 16rpx; border-radius: 999rpx; background: #eef6f1; color: #12372a; font-size: 22rpx; font-weight: 900; }
	.heat, .desc, .composer-sub, .post-time, .attach-type, .attach-meta, .pick-meta, .count { color: #667085; }
	.title, .desc, .composer-title, .composer-sub, .post-author, .post-time, .post-text, .attach-type, .attach-title, .attach-meta, .side-title, .pick-title, .pick-meta { display: block; }
	.title { margin-top: 16rpx; font-size: 42rpx; line-height: 1.22; font-weight: 900; color: #111827; }
	.desc { margin-top: 12rpx; font-size: 26rpx; line-height: 1.7; }
	.tags { margin-top: 18rpx; }
	.layout { display: grid; grid-template-columns: minmax(0, 1fr) 360rpx; gap: 20rpx; margin-top: 20rpx; align-items: start; }
	.feed, .side { display: grid; gap: 18rpx; }
	.composer, .post-card, .side-card { padding: 24rpx; }
	.avatar { width: 70rpx; height: 70rpx; border-radius: 8px; background: #12372a; color: #fff; display: flex; align-items: center; justify-content: center; font-size: 28rpx; font-weight: 900; flex: 0 0 auto; }
	.composer-title, .post-author { color: #17231d; font-size: 28rpx; font-weight: 900; }
	.composer-sub, .post-time { margin-top: 4rpx; font-size: 22rpx; }
	.post-input { width: 100%; min-height: 170rpx; margin-top: 18rpx; padding: 20rpx; border-radius: 8px; background: #f8faf9; box-sizing: border-box; font-size: 27rpx; line-height: 1.6; }
	.draft-images, .post-images { display: grid; grid-template-columns: repeat(3, 1fr); gap: 10rpx; margin-top: 18rpx; }
	.draft-image-wrap { position: relative; height: 150rpx; border-radius: 8px; overflow: hidden; background: #edf3ef; }
	.remove-image { position: absolute; top: 8rpx; right: 8rpx; width: 36rpx; height: 36rpx; border-radius: 999rpx; background: rgba(17, 24, 39, .72); color: #fff; display: flex; align-items: center; justify-content: center; font-size: 28rpx; }
	.attach-preview, .post-attach { display: grid; grid-template-columns: 112rpx minmax(0, 1fr) 72rpx; gap: 14rpx; align-items: center; margin-top: 16rpx; padding: 14rpx; border-radius: 8px; background: #f8faf9; border: 1rpx solid #e7ece8; }
	.attach-cover, .store-mark, .pick-cover { border-radius: 8px; background: #edf3ef; overflow: hidden; display: flex; align-items: center; justify-content: center; color: #12372a; font-weight: 900; }
	.attach-cover, .store-mark { width: 112rpx; height: 112rpx; }
	.store-mark { background: #12372a; color: #fff; font-size: 34rpx; }
	.attach-title { margin-top: 5rpx; color: #17231d; font-size: 26rpx; font-weight: 900; line-height: 1.35; }
	.attach-meta { margin-top: 4rpx; font-size: 22rpx; }
	.clear-attach, .open-link { height: 54rpx; border-radius: 8px; background: #fff; color: #12372a; display: flex; align-items: center; justify-content: center; font-size: 22rpx; font-weight: 900; border: 1rpx solid #dbe5df; }
	.composer-tools { margin-top: 18rpx; }
	button { margin: 0; padding: 0; border: 0; }
	button::after { border: 0; }
	.tool-btn, .post-btn, .action, .comment-btn { height: 60rpx; border-radius: 8px; display: flex; align-items: center; justify-content: center; box-sizing: border-box; font-weight: 900; }
	.tool-btn { min-width: 116rpx; padding: 0 18rpx; background: #eef3f0; color: #12372a; font-size: 24rpx; }
	.count { margin-left: auto; font-size: 22rpx; }
	.post-btn { width: 116rpx; background: #12372a; color: #fff; font-size: 25rpx; }
	.post-text { margin-top: 18rpx; color: #2f3a35; font-size: 29rpx; line-height: 1.78; }
	.post-images.cols-1 { grid-template-columns: minmax(0, 460rpx); }
	.post-images.cols-2 { grid-template-columns: repeat(2, minmax(0, 240rpx)); }
	.post-image { width: 100%; height: 200rpx; border-radius: 8px; background: #edf3ef; }
	.post-actions { margin-top: 18rpx; padding-top: 16rpx; border-top: 1rpx solid #edf0f3; }
	.action { min-width: 128rpx; padding: 0 18rpx; background: #f3f6f4; color: #475467; font-size: 24rpx; }
	.action.on { background: #12372a; color: #fff; }
	.comment-list { margin-top: 16rpx; padding: 16rpx; border-radius: 8px; background: #f8faf9; display: grid; gap: 10rpx; }
	.comment { font-size: 24rpx; line-height: 1.5; }
	.comment-author { color: #12372a; font-weight: 900; }
	.comment-text { color: #475467; }
	.comment-box { display: grid; grid-template-columns: minmax(0, 1fr) 116rpx; gap: 10rpx; margin-top: 14rpx; }
	.comment-input { height: 60rpx; padding: 0 18rpx; border-radius: 8px; background: #f8faf9; font-size: 25rpx; }
	.comment-btn { background: #12372a; color: #fff; }
	.empty { padding: 42rpx; text-align: center; color: #98a2b3; background: #fff; border-radius: 8px; border: 1rpx solid #e5e9ef; }
	.side-title { color: #17231d; font-size: 28rpx; font-weight: 900; }
	.pick-list { max-height: 620rpx; margin-top: 12rpx; }
	.pick-item { display: grid; grid-template-columns: 88rpx minmax(0, 1fr); gap: 12rpx; align-items: center; padding: 14rpx; margin-top: 12rpx; border-radius: 8px; border: 1rpx solid #e7ece8; background: #fff; }
	.pick-item.on { border-color: #12372a; background: #f1f7f3; }
	.pick-cover { width: 88rpx; height: 88rpx; font-size: 20rpx; }
	.pick-title { color: #17231d; font-size: 24rpx; font-weight: 900; line-height: 1.35; }
	.pick-meta { margin-top: 4rpx; font-size: 21rpx; }
	.store-pick .store-mark { width: 88rpx; height: 88rpx; font-size: 28rpx; }
	@media screen and (max-width: 900px) {
		.topbar-inner, .hero, .layout { display: flex; flex-direction: column; }
		.web-nav, .brand-sub, .top-actions { display: none; }
		.cover { width: 100%; }
		.side { order: -1; }
	}
</style>
