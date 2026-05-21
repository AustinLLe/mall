<template>
	<view class="safe-page">
		<view class="content-wrap page">
			<view class="form-card">
				<text class="title">{{ editingId ? '编辑地址' : '新增地址' }}</text>
				<view class="grid">
					<view class="field"><text class="label">收货人</text><input v-model="form.name" class="input" placeholder="姓名" /></view>
					<view class="field"><text class="label">手机号</text><input v-model="form.phone" class="input" type="number" maxlength="11" placeholder="11 位手机号" /></view>
					<view class="field"><text class="label">地区</text><input v-model="form.region" class="input" placeholder="省市区 / 学校" /></view>
					<view class="field"><text class="label">标签</text><input v-model="form.tag" class="input" placeholder="家 / 学校 / 公司" /></view>
				</view>
				<view class="field"><text class="label">详细地址</text><input v-model="form.detail" class="input" placeholder="楼栋、门牌号等" /></view>
				<view class="default-row" @click="form.isDefault = !form.isDefault">
					<view class="check" :class="{ on: form.isDefault }">{{ form.isDefault ? '✓' : '' }}</view>
					<text>设为默认地址</text>
				</view>
				<view class="actions">
					<view class="primary" @click="submit">{{ editingId ? '保存修改' : '新增地址' }}</view>
					<view v-if="editingId" class="plain" @click="resetForm">取消编辑</view>
				</view>
			</view>

			<view class="section-head">
				<text class="section-title">地址簿</text>
				<text class="section-sub">共 {{ list.length }} 条</text>
			</view>

			<view v-if="list.length" class="list">
				<view v-for="item in list" :key="item.id" class="card">
					<view class="top">
						<view>
							<text class="name">{{ item.name }}</text>
							<text class="phone">{{ item.phone }}</text>
						</view>
						<view class="badges">
							<text v-if="item.tag" class="tag">{{ item.tag }}</text>
							<text v-if="item.isDefault" class="default-badge">默认</text>
						</view>
					</view>
					<text class="detail">{{ item.region }} {{ item.detail }}</text>
					<view class="ops">
						<text @click="useDefault(item.id)">设为默认</text>
						<text @click="edit(item)">编辑</text>
						<text class="danger" @click="remove(item.id)">删除</text>
					</view>
				</view>
			</view>
			<view v-else class="empty">还没有地址，请先新增一个收货地址。</view>
		</view>
	</view>
</template>

<script>
	import { getAddressList, saveAddress, removeAddress, setDefaultAddress } from '@/utils/address.js'

	function emptyForm() {
		return { name: '', phone: '', region: '', detail: '', tag: '', isDefault: false }
	}

	export default {
		data() {
			return { list: [], editingId: '', form: emptyForm() }
		},
		onShow() {
			this.loadData()
		},
		methods: {
			loadData() {
				this.list = getAddressList()
			},
			resetForm() {
				this.editingId = ''
				this.form = emptyForm()
			},
			submit() {
				if (!/^1\d{10}$/.test(this.form.phone.trim())) {
					uni.showToast({ title: '请输入正确手机号', icon: 'none' })
					return
				}
				try {
					saveAddress({ id: this.editingId, ...this.form })
					this.loadData()
					this.resetForm()
					uni.showToast({ title: '地址已保存', icon: 'success' })
				} catch (e) {
					uni.showToast({ title: e.message || '保存失败', icon: 'none' })
				}
			},
			edit(item) {
				this.editingId = item.id
				this.form = { name: item.name, phone: item.phone, region: item.region, detail: item.detail, tag: item.tag || '', isDefault: !!item.isDefault }
			},
			useDefault(id) {
				setDefaultAddress(id)
				this.loadData()
				uni.showToast({ title: '已设为默认', icon: 'none' })
			},
			remove(id) {
				removeAddress(id)
				this.loadData()
			}
		}
	}
</script>

<style lang="scss" scoped>
	.page { padding: 28rpx; }
	.form-card, .card, .empty { background: #fff; border: 1rpx solid #e4e9e5; border-radius: 24rpx; padding: 26rpx; box-shadow: 0 14rpx 36rpx rgba(15, 35, 26, 0.06); }
	.title, .section-title, .name { font-weight: 900; color: #17231d; }
	.title { display: block; font-size: 36rpx; }
	.grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 18rpx; }
	.field { margin-top: 22rpx; }
	.label { display: block; margin-bottom: 10rpx; color: #667085; font-size: 24rpx; }
	.input { height: 82rpx; border-radius: 16rpx; background: #f8faf8; padding: 0 20rpx; font-size: 28rpx; }
	.default-row, .actions, .top, .badges, .ops, .section-head { display: flex; align-items: center; gap: 14rpx; }
	.default-row { margin-top: 24rpx; color: #4b5563; font-size: 25rpx; }
	.check { width: 40rpx; height: 40rpx; border-radius: 50%; border: 2rpx solid #cbd5d0; display: flex; align-items: center; justify-content: center; color: #fff; }
	.check.on { background: #1f5c43; border-color: #1f5c43; }
	.actions { justify-content: flex-end; margin-top: 24rpx; }
	.primary, .plain { padding: 16rpx 24rpx; border-radius: 16rpx; font-weight: 900; }
	.primary { background: #1f5c43; color: #fff; }
	.plain { background: #e8f3ed; color: #1f5c43; }
	.section-head { justify-content: space-between; margin: 28rpx 0 16rpx; }
	.section-title { font-size: 30rpx; }
	.section-sub, .phone, .detail, .ops { color: #667085; font-size: 24rpx; }
	.card { margin-bottom: 16rpx; }
	.top { justify-content: space-between; }
	.name, .phone { display: inline-block; margin-right: 14rpx; }
	.tag, .default-badge { padding: 7rpx 14rpx; border-radius: 999rpx; font-size: 22rpx; }
	.tag { background: #f4f6f4; color: #667085; }
	.default-badge { background: #e8f3ed; color: #1f5c43; }
	.detail { display: block; margin-top: 14rpx; line-height: 1.6; }
	.ops { margin-top: 16rpx; }
	.danger { color: #b95420; }
	.empty { text-align: center; color: #667085; }
	@media screen and (max-width: 900px) { .grid { grid-template-columns: 1fr; } }
</style>
