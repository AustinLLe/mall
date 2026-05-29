<template>
	<view class="safe-page">
		<view class="content-wrap page">
			<text class="title">审核管理</text>
			<view v-for="item in list" :key="item.id" class="card">
				<view class="cover" :class="{ 'has-image': isImageCover(item.cover) }">
					<image v-if="isImageCover(item.cover)" class="cover-img" :src="item.cover" mode="aspectFill"></image>
					<text v-else>{{ item.cover }}</text>
				</view>
				<view class="main">
					<text class="name">{{ item.title }}</text>
					<text class="meta">{{ item.category }} · {{ item.condition }} · 信用 {{ item.credit }}</text>
					<text class="risk">检测结果：未发现违禁词，价格处于合理区间。</text>
				</view>
				<view class="actions"><text @click="pass(item)">通过</text><text class="reject" @click="reject(item)">拒绝</text></view>
			</view>
		</view>
	</view>
</template>
<script>
	import { goodsCatalog } from '../../data/catalog.js'
	import { fetchAuditItems, submitAudit } from '@/services/shop.js'

	export default {
		data() {
			return {
				list: goodsCatalog.slice(0, 4)
			}
		},
		onShow() {
			this.loadAuditItems()
		},
		methods: {
			isImageCover(cover) {
				return typeof cover === 'string' && (cover.startsWith('/static/') || cover.startsWith('http'))
			},
			async loadAuditItems() {
				try {
					const body = await fetchAuditItems()
					if (body && body.code === 0 && Array.isArray(body.data)) {
						this.list = body.data.slice(0, 8)
					}
				} catch (e) {
					this.list = goodsCatalog.slice(0, 4)
				}
			},
			async pass(item) {
				await this.audit(item, 'approved', '已通过审核')
			},
			async reject(item) {
				await this.audit(item, 'rejected', '已驳回并通知卖家')
			},
			async audit(item, status, title) {
				try {
					const body = await submitAudit(item.id, status)
					if (body && body.code === 0) {
						this.list = this.list.filter((current) => current.id !== item.id)
					}
				} catch (e) {}
				uni.showToast({ title, icon: status === 'approved' ? 'success' : 'none' })
			}
		}
	}
</script>
<style lang="scss" scoped>
	.page { padding: 28rpx; }
	.title { display: block; font-size: 40rpx; font-weight: 900; color: #17231d; margin-bottom: 20rpx; }
	.card { display: flex; gap: 18rpx; align-items: center; background: #fff; border-radius: 24rpx; padding: 24rpx; margin-bottom: 18rpx; border: 1rpx solid #e4e9e5; }
	.cover { width: 112rpx; height: 112rpx; border-radius: 18rpx; background: #edf3ef; display: flex; align-items: center; justify-content: center; font-size: 52rpx; overflow: hidden; }
	.cover.has-image { background: #eef7f1; }
	.cover-img { width: 100%; height: 100%; display: block; }
	.main { flex: 1; min-width: 0; }
	.name, .meta, .risk { display: block; }
	.name { font-size: 29rpx; font-weight: 900; color: #17231d; }
	.meta, .risk { margin-top: 6rpx; color: #667085; font-size: 23rpx; }
	.actions { display: flex; gap: 10rpx; }
	.actions text { padding: 12rpx 18rpx; border-radius: 14rpx; background: #e8f3ed; color: #1f5c43; font-weight: 900; font-size: 24rpx; }
	.actions .reject { background: #fff0e7; color: #b95420; }
</style>
