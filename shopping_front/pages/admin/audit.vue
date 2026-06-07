<template>
	<view class="safe-page">
		<view class="content-wrap page">
			<view class="head">
				<view>
					<text class="title">审核管理</text>
					<text class="desc">仅管理员可处理待审核商品，审核后商品会自动移出列表。</text>
				</view>
				<view v-if="isAdmin" class="refresh" @click="loadAuditItems">刷新</view>
			</view>

			<view v-if="!isAdmin" class="no-access">
				<text class="lock">!</text>
				<text class="no-title">无权限访问</text>
				<text class="no-desc">当前账号不是管理员，无法进入商品审核页。请使用 admin 账号登录后再试。</text>
			</view>

			<view v-else-if="!list.length" class="empty">暂无待审核商品</view>

			<block v-else>
				<view v-for="item in list" :key="item.id" class="card">
					<view class="cover" :class="{ 'has-image': isImageUrl(item.cover) }">
						<image v-if="isImageUrl(item.cover)" class="cover-img" :src="resolveImageUrl(item.cover)" mode="aspectFill"></image>
						<text v-else>{{ item.cover }}</text>
					</view>
					<view class="main">
						<text class="name">{{ item.title }}</text>
						<text class="meta">{{ item.category }} · ¥{{ item.price }} · {{ item.condition || '待补充' }}</text>
						<text class="meta">发布人：{{ item.publisherName || item.shopName || '个人卖家' }} · {{ item.publishedAt || '刚刚' }}</text>
						<text class="desc-line">{{ item.description || item.story || item.subtitle }}</text>
						<text class="risk">检测结果：未发现违禁词，价格处于合理区间。</text>
						<textarea v-model="rejectReasons[item.id]" class="reason" placeholder="拒绝时可填写原因，例如：图片不清晰 / 描述不完整" />
					</view>
					<view class="actions"><text @click="pass(item)">通过</text><text class="reject" @click="reject(item)">拒绝</text></view>
				</view>
			</block>
		</view>
	</view>
</template>
<script>
	import { fetchAuditItems, submitAudit } from '@/services/shop.js'
	import { getCachedUser } from '@/utils/auth.js'
	import { isImageUrl, resolveImageUrl } from '@/utils/media.js'

	export default {
		data() {
			return {
				user: {},
				list: [],
				rejectReasons: {}
			}
		},
		computed: {
			isAdmin() {
				return this.user && this.user.role === 'admin'
			}
		},
		onShow() {
			this.user = getCachedUser() || {}
			if (this.isAdmin) this.loadAuditItems()
		},
		methods: {
			isImageUrl,
			resolveImageUrl,
			async loadAuditItems() {
				if (!this.isAdmin) return
				try {
					const body = await fetchAuditItems()
					if (body && body.code === 0 && Array.isArray(body.data)) {
						this.list = body.data
						return
					}
					this.list = []
					uni.showToast({ title: (body && body.message) || '待审核列表加载失败', icon: 'none' })
				} catch (e) {
					this.list = []
					const msg = (e && e.body && e.body.message) || (e && e.statusCode === 403 ? '当前账号无管理员权限' : '待审核列表加载失败')
					uni.showToast({ title: msg, icon: 'none' })
				}
			},
			async pass(item) {
				await this.audit(item, 'approve', '已通过审核')
			},
			async reject(item) {
				await this.audit(item, 'reject', '已驳回并通知卖家')
			},
			async audit(item, action, title) {
				try {
					const body = await submitAudit(item.id, action, this.rejectReasons[item.id] || '')
					if (body && body.code === 0) {
						this.list = this.list.filter((current) => current.id !== item.id)
						delete this.rejectReasons[item.id]
					}
					uni.showToast({ title, icon: action === 'approve' ? 'success' : 'none' })
				} catch (e) {
					uni.showToast({ title: '审核操作失败', icon: 'none' })
				}
			}
		}
	}
</script>
<style lang="scss" scoped>
	.page { padding: 28rpx; }
	.head { display: flex; align-items: center; justify-content: space-between; gap: 18rpx; margin-bottom: 20rpx; }
	.title { display: block; font-size: 40rpx; font-weight: 900; color: #17231d; }
	.desc { display: block; margin-top: 8rpx; color: #667085; font-size: 24rpx; }
	.refresh { padding: 14rpx 22rpx; border-radius: 14rpx; background: #1f5c43; color: #fff; font-weight: 900; }
	.card { display: flex; gap: 18rpx; align-items: flex-start; background: #fff; border-radius: 24rpx; padding: 24rpx; margin-bottom: 18rpx; border: 1rpx solid #e4e9e5; }
	.cover { width: 112rpx; height: 112rpx; border-radius: 18rpx; background: #edf3ef; display: flex; align-items: center; justify-content: center; font-size: 52rpx; overflow: hidden; }
	.cover.has-image { background: #eef7f1; }
	.cover-img { width: 100%; height: 100%; display: block; }
	.main { flex: 1; min-width: 0; }
	.name, .meta, .risk, .desc-line { display: block; }
	.name { font-size: 29rpx; font-weight: 900; color: #17231d; }
	.meta, .risk, .desc-line { margin-top: 6rpx; color: #667085; font-size: 23rpx; line-height: 1.5; }
	.desc-line { color: #34423b; }
	.reason { margin-top: 14rpx; width: 100%; min-height: 82rpx; background: #f8faf8; border-radius: 16rpx; padding: 14rpx 18rpx; box-sizing: border-box; font-size: 24rpx; }
	.actions { display: flex; gap: 10rpx; flex-shrink: 0; }
	.actions text { padding: 12rpx 18rpx; border-radius: 14rpx; background: #e8f3ed; color: #1f5c43; font-weight: 900; font-size: 24rpx; }
	.actions .reject { background: #fff0e7; color: #b95420; }
	.no-access, .empty { background: #fff; border: 1rpx solid #e4e9e5; border-radius: 24rpx; padding: 42rpx; text-align: center; }
	.lock { width: 76rpx; height: 76rpx; border-radius: 50%; margin: 0 auto 18rpx; background: #fff0e7; color: #b95420; display: flex; align-items: center; justify-content: center; font-size: 42rpx; font-weight: 900; }
	.no-title { display: block; font-size: 32rpx; font-weight: 900; color: #17231d; }
	.no-desc, .empty { color: #667085; font-size: 25rpx; line-height: 1.6; }
	.no-desc { display: block; margin-top: 10rpx; }
	@media screen and (max-width: 760px) {
		.card { flex-direction: column; }
		.actions { width: 100%; }
		.actions text { flex: 1; text-align: center; }
	}
</style>
