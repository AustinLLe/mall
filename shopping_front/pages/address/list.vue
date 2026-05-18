<template>
	<view class="page">
		<view class="form-card">
			<text class="title">{{ editingId ? '编辑地址' : '新增地址' }}</text>
			<view class="field">
				<text class="label">收货人</text>
				<input v-model="form.name" class="input" placeholder="请输入收货人姓名" />
			</view>
			<view class="field">
				<text class="label">手机号</text>
				<input v-model="form.phone" class="input" type="number" maxlength="11" placeholder="请输入手机号" />
			</view>
			<view class="field">
				<text class="label">地区</text>
				<input v-model="form.region" class="input" placeholder="如：广东省 深圳市 南山区" />
			</view>
			<view class="field">
				<text class="label">详细地址</text>
				<input v-model="form.detail" class="input" placeholder="楼栋、门牌号等" />
			</view>
			<view class="field">
				<text class="label">标签</text>
				<input v-model="form.tag" class="input" placeholder="如：宿舍 / 家 / 公司" />
			</view>
			<view class="default-row" @click="form.isDefault = !form.isDefault">
				<view class="check" :class="{ on: form.isDefault }">{{ form.isDefault ? '✓' : '' }}</view>
				<text class="default-text">设为默认地址</text>
			</view>
			<view class="actions">
				<button class="primary" @click="submit">{{ editingId ? '保存修改' : '新增地址' }}</button>
				<button v-if="editingId" class="plain" @click="resetForm">取消编辑</button>
			</view>
		</view>

		<view class="section-head">
			<text class="section-title">地址列表</text>
			<text class="section-sub">共 {{ list.length }} 条</text>
		</view>

		<view v-if="list.length" class="list">
			<view v-for="item in list" :key="item.id" class="card" @click="selectAddress(item)">
				<view class="top">
					<view class="person">
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
					<text class="op" @click="useDefault(item.id)">{{ item.isDefault ? '当前默认' : '设为默认' }}</text>
					<text class="op" @click="edit(item)">编辑</text>
					<text class="op danger" @click="remove(item.id)">删除</text>
				</view>
			</view>
		</view>

		<view v-else class="empty">
			<text class="empty-title">还没有收货地址</text>
			<text class="empty-sub">填写上方表单后即可保存，购物车下单会优先使用默认地址。</text>
		</view>
	</view>
</template>

<script>
	import { getAddressList, saveAddress, removeAddress, setDefaultAddress } from '@/utils/address.js'

	function emptyForm() {
		return {
			name: '',
			phone: '',
			region: '',
			detail: '',
			tag: '',
			isDefault: false
		}
	}

	export default {
		data() {
			return {
				list: [],
				editingId: '',
				form: emptyForm()
			}
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
					uni.showToast({ title: '手机号格式不正确', icon: 'none' })
					return
				}
				try {
					saveAddress({
						id: this.editingId,
						...this.form
					})
					this.loadData()
					this.resetForm()
					uni.showToast({ title: '地址已保存', icon: 'success' })
				} catch (e) {
					uni.showToast({ title: e.message || '保存失败', icon: 'none' })
				}
			},
			edit(item) {
				this.editingId = item.id
				this.form = {
					name: item.name,
					phone: item.phone,
					region: item.region,
					detail: item.detail,
					tag: item.tag || '',
					isDefault: !!item.isDefault
				}
				uni.pageScrollTo({ scrollTop: 0, duration: 200 })
			},
			useDefault(id) {
				setDefaultAddress(id)
				this.loadData()
				uni.showToast({ title: '默认地址已更新', icon: 'none' })
			},
			remove(id) {
				removeAddress(id)
				this.loadData()
				if (this.editingId === id) {
					this.resetForm()
				}
				uni.showToast({ title: '已删除', icon: 'none' })
			},
			selectAddress(item) {
        		uni.$emit('API_SELECT_ADDRESS', item);
        		uni.navigateBack();
    		}
		}
	}
</script>

<style lang="scss" scoped>
	.page {
		min-height: 100vh;
		padding: 24rpx;
	}
	.form-card,
	.card {
		background: #fff;
		border-radius: 24rpx;
		box-shadow: 0 8rpx 28rpx rgba(0, 0, 0, 0.05);
	}
	.form-card {
		padding: 30rpx 28rpx;
	}
	.title,
	.section-title,
	.name {
		color: #222;
		font-weight: 700;
	}
	.title {
		font-size: 34rpx;
		display: block;
	}
	.field {
		margin-top: 24rpx;
	}
	.label {
		display: block;
		font-size: 24rpx;
		color: #777;
		margin-bottom: 12rpx;
	}
	.input {
		height: 84rpx;
		background: #f6f6f6;
		border-radius: 16rpx;
		padding: 0 24rpx;
		font-size: 28rpx;
	}
	.default-row {
		display: flex;
		align-items: center;
		gap: 16rpx;
		margin-top: 28rpx;
	}
	.check {
		width: 40rpx;
		height: 40rpx;
		border-radius: 50%;
		border: 2rpx solid #c9d6cf;
		display: flex;
		align-items: center;
		justify-content: center;
		color: #fff;
		font-size: 22rpx;
	}
	.check.on {
		background: #1b4332;
		border-color: #1b4332;
	}
	.default-text {
		font-size: 26rpx;
		color: #444;
	}
	.actions {
		display: flex;
		gap: 20rpx;
		margin-top: 28rpx;
	}
	.primary,
	.plain {
		flex: 1;
		height: 84rpx;
		line-height: 84rpx;
		border-radius: 16rpx;
		font-size: 28rpx;
		border: none;
	}
	.primary {
		background: #1b4332;
		color: #fff;
	}
	.plain {
		background: #edf2ef;
		color: #1b4332;
	}
	.section-head {
		display: flex;
		align-items: baseline;
		justify-content: space-between;
		margin: 32rpx 4rpx 18rpx;
	}
	.section-title {
		font-size: 30rpx;
	}
	.section-sub,
	.phone,
	.detail,
	.empty-title,
	.empty-sub,
	.op {
		font-size: 24rpx;
		color: #666;
	}
	.list {
		display: flex;
		flex-direction: column;
		gap: 20rpx;
	}
	.card {
		padding: 26rpx;
	}
	.top,
	.person,
	.badges,
	.ops {
		display: flex;
		align-items: center;
	}
	.top {
		justify-content: space-between;
		gap: 16rpx;
	}
	.person,
	.badges,
	.ops {
		flex-wrap: wrap;
		gap: 14rpx;
	}
	.name {
		font-size: 30rpx;
	}
	.tag,
	.default-badge {
		font-size: 22rpx;
		border-radius: 999rpx;
		padding: 6rpx 14rpx;
	}
	.tag {
		background: #f3f4f6;
		color: #666;
	}
	.default-badge {
		background: #edf7f1;
		color: #2d6a4f;
	}
	.detail {
		display: block;
		margin-top: 16rpx;
		line-height: 1.6;
	}
	.ops {
		margin-top: 20rpx;
	}
	.op.danger {
		color: #b42318;
	}
	.empty {
		background: #fff;
		border-radius: 24rpx;
		padding: 60rpx 36rpx;
		text-align: center;
		box-shadow: 0 8rpx 28rpx rgba(0, 0, 0, 0.05);
	}
	.empty-title {
		display: block;
		font-size: 30rpx;
		font-weight: 600;
		color: #222;
	}
	.empty-sub {
		display: block;
		margin-top: 12rpx;
		line-height: 1.6;
	}
</style>
