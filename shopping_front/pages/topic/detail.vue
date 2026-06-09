<template>
	<view class="safe-page topic-page">
		<scroll-view scroll-y class="scroll">
			<view class="content-wrap page">
				<view class="hero">
					<view class="cover" :class="{ 'has-image': isImageUrl(topic.cover) }">
						<image v-if="isImageUrl(topic.cover)" class="cover-img" :src="resolveImageUrl(topic.cover)" mode="aspectFill"></image>
						<text v-else>{{ topic.type || '话题' }}</text>
					</view>
					<view class="hero-main">
						<view class="article-line">
							<text class="badge">{{ topic.type || '话题' }}</text>
							<text class="heat">{{ topic.heat || '0 帖 · 0 赞' }}</text>
						</view>
						<text class="title">{{ topic.title || '话题讨论' }}</text>
						<text class="desc">{{ topic.desc || '一起讨论这个话题。' }}</text>
						<view class="tags">
							<text v-for="tag in topic.tags || []" :key="tag" class="tag">{{ tag }}</text>
						</view>
						<text class="author">发起人：{{ topic.author || '松果社区' }}</text>
					</view>
				</view>

				<view class="layout">
					<view class="feed">
						<view class="composer">
							<textarea v-model="postContent" class="post-input" maxlength="500" placeholder="分享你的经验、避坑点、购买建议或者晒图心得..." />
							<view class="composer-bottom">
								<text class="count">{{ postContent.length }}/500</text>
								<button class="post-btn" :disabled="posting" @click="publishPost">{{ posting ? '发布中...' : '发布帖子' }}</button>
							</view>
						</view>

						<view v-for="post in posts" :key="post.id" class="post-card">
							<view class="post-head">
								<view class="avatar">{{ (post.author || '松').slice(0, 1) }}</view>
								<view>
									<text class="post-author">{{ post.author }}</text>
									<text class="post-time">{{ post.createdAt }}</text>
								</view>
							</view>
							<text class="post-text">{{ post.content }}</text>
							<view v-if="post.images && post.images.length" class="post-images">
								<image v-for="img in post.images" :key="img" class="post-image" :src="resolveImageUrl(img)" mode="aspectFill"></image>
							</view>
							<view class="post-actions">
								<button class="action" :class="{ on: post.liked }" @click="likePost(post)">{{ post.liked ? '已赞' : '点赞' }} {{ post.likeCount || 0 }}</button>
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
						<text class="side-title">相关商品</text>
						<view v-for="item in relatedGoods" :key="item.id" class="goods" @click="openGoods(item)">
							<view class="goods-cover" :class="{ 'has-image': isImageUrl(item.cover) }">
								<image v-if="isImageUrl(item.cover)" class="cover-img" :src="resolveImageUrl(item.cover)" mode="aspectFill"></image>
								<text v-else>{{ item.category || '商品' }}</text>
							</view>
							<view>
								<text class="goods-title">{{ item.title }}</text>
								<text class="goods-price">¥{{ item.price }}</text>
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
	import { createTopicComment, createTopicPost, fetchProducts, fetchTopic, fetchTopicPosts, toggleTopicPostLike } from '@/services/shop.js'
	import { isImageUrl, resolveImageUrl } from '@/utils/media.js'
	import { pickErrorMessage } from '@/utils/auth.js'

	export default {
		data() {
			return {
				topicId: '',
				topic: {},
				posts: [],
				relatedGoodsList: [],
				postContent: '',
				posting: false,
				commentingPostId: '',
				commentText: ''
			}
		},
		computed: {
			relatedGoods() {
				const tags = this.topic.tags || []
				const matched = this.relatedGoodsList.filter(item => tags.some(tag => [item.category, item.scene, item.title, item.subtitle].join(' ').includes(tag)))
				return (matched.length ? matched : this.relatedGoodsList).slice(0, 4)
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
				await Promise.all([this.loadTopic(), this.loadPosts(), this.loadRelatedGoods()])
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
			async publishPost() {
				if (!this.postContent.trim()) {
					uni.showToast({ title: '请先写点内容', icon: 'none' })
					return
				}
				this.posting = true
				try {
					const body = await createTopicPost(this.topicId, { content: this.postContent.trim(), images: [] })
					this.posts = body.data || []
					this.postContent = ''
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
			}
		}
	}
</script>

<style lang="scss" scoped>
	.topic-page { min-height: 100vh; background: #f5f6f8; }
	.scroll { height: 100vh; }
	.page { padding: 28rpx; }
	.hero, .composer, .post-card, .side { background: #fff; border: 1rpx solid #e4e9e5; border-radius: 8px; box-shadow: 0 14rpx 38rpx rgba(17,38,28,.06); }
	.hero { display: grid; grid-template-columns: minmax(260rpx, 420rpx) minmax(0, 1fr); gap: 30rpx; padding: 28rpx; align-items: center; }
	.cover { height: 300rpx; border-radius: 8px; overflow: hidden; background: #edf3ef; display: flex; align-items: center; justify-content: center; color: #12372a; font-size: 38rpx; font-weight: 900; }
	.cover-img { width: 100%; height: 100%; display: block; }
	.article-line, .tags, .post-actions { display: flex; align-items: center; gap: 12rpx; flex-wrap: wrap; }
	.article-line { justify-content: space-between; }
	.badge, .tag { display: inline-flex; align-items: center; border-radius: 999rpx; background: #f5f7fa; color: #12372a; font-size: 22rpx; font-weight: 800; padding: 8rpx 16rpx; }
	.heat, .desc, .author, .post-time { color: #667085; }
	.title, .desc, .author, .side-title, .goods-title, .goods-price, .post-author, .post-time, .post-text { display: block; }
	.title { margin-top: 18rpx; font-size: 42rpx; line-height: 1.22; font-weight: 900; color: #12372a; }
	.desc { margin-top: 14rpx; font-size: 26rpx; line-height: 1.7; }
	.tags { margin-top: 18rpx; }
	.author { margin-top: 18rpx; font-size: 24rpx; }
	.layout { display: grid; grid-template-columns: minmax(0, 1fr) 330rpx; gap: 20rpx; margin-top: 20rpx; align-items: start; }
	.feed { display: grid; gap: 18rpx; }
	.composer, .post-card, .side { padding: 26rpx; }
	.post-input { width: 100%; min-height: 150rpx; padding: 18rpx; border-radius: 8px; background: #f8faf9; box-sizing: border-box; font-size: 27rpx; line-height: 1.6; }
	.composer-bottom { display: flex; align-items: center; justify-content: space-between; margin-top: 14rpx; }
	.count { color: #98a2b3; font-size: 22rpx; }
	button { margin: 0; padding: 0; border: 0; }
	button::after { border: 0; }
	.post-btn, .action, .comment-btn { height: 62rpx; border-radius: 8px; display: flex; align-items: center; justify-content: center; font-weight: 900; box-sizing: border-box; }
	.post-btn { width: 170rpx; background: #12372a; color: #fff; }
	.post-head { display: flex; align-items: center; gap: 14rpx; }
	.avatar { width: 72rpx; height: 72rpx; border-radius: 8px; background: #12372a; color: #fff; display: flex; align-items: center; justify-content: center; font-size: 30rpx; font-weight: 900; }
	.post-author { color: #17231d; font-size: 27rpx; font-weight: 900; }
	.post-time { margin-top: 4rpx; font-size: 22rpx; }
	.post-text { margin-top: 18rpx; color: #344054; font-size: 28rpx; line-height: 1.75; }
	.post-images { display: grid; grid-template-columns: repeat(3, 1fr); gap: 10rpx; margin-top: 18rpx; }
	.post-image { width: 100%; height: 180rpx; border-radius: 8px; }
	.post-actions { margin-top: 18rpx; }
	.action { min-width: 132rpx; padding: 0 18rpx; background: #eef5f0; color: #12372a; font-size: 24rpx; }
	.action.on { background: #12372a; color: #fff; }
	.comment-list { margin-top: 16rpx; padding: 16rpx; border-radius: 8px; background: #f8faf9; display: grid; gap: 10rpx; }
	.comment { font-size: 24rpx; line-height: 1.5; }
	.comment-author { color: #12372a; font-weight: 900; }
	.comment-text { color: #475467; }
	.comment-box { display: grid; grid-template-columns: minmax(0, 1fr) 120rpx; gap: 10rpx; margin-top: 14rpx; }
	.comment-input { height: 62rpx; padding: 0 18rpx; border-radius: 8px; background: #f8faf9; font-size: 25rpx; }
	.comment-btn { background: #12372a; color: #fff; }
	.side-title { font-size: 30rpx; font-weight: 900; color: #17231d; }
	.goods { display: grid; grid-template-columns: 96rpx minmax(0, 1fr); gap: 14rpx; align-items: center; padding: 18rpx 0; border-top: 1rpx solid #eef1ee; }
	.goods:first-of-type { margin-top: 12rpx; }
	.goods-cover { width: 96rpx; height: 96rpx; border-radius: 8px; overflow: hidden; background: #edf3ef; display: flex; align-items: center; justify-content: center; color: #667085; font-size: 22rpx; }
	.goods-title { font-size: 24rpx; font-weight: 900; color: #17231d; line-height: 1.35; }
	.goods-price { margin-top: 6rpx; font-size: 24rpx; font-weight: 900; color: #d66a2c; }
	.empty { padding: 40rpx; text-align: center; color: #98a2b3; background: #fff; border-radius: 8px; border: 1rpx solid #e4e9e5; }
	@media screen and (max-width: 900px) {
		.hero, .layout { display: flex; flex-direction: column; }
		.cover { width: 100%; }
	}
</style>
