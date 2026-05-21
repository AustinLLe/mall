<template>
	<view class="safe-page">
		<view class="content-wrap page">
			<view class="page-head">
				<view>
					<text class="title">发布商品</text>
					<text class="desc">支持新品店铺发布，也支持二手闲置的成色、故事和 AI 估价。</text>
				</view>
				<view class="ai-pill" @click="fillByAi">AI 生成建议</view>
			</view>

			<view class="layout">
				<view class="form-card">
					<view class="section">
						<text class="section-title">商品类型</text>
						<view class="segmented">
							<view class="seg" :class="{ on: form.scene === 'new' }" @click="form.scene = 'new'">新品</view>
							<view class="seg" :class="{ on: form.scene === 'used' }" @click="form.scene = 'used'">二手闲置</view>
						</view>
					</view>

					<view class="section">
						<text class="section-title">图片</text>
						<view class="upload-row">
							<view v-for="(img, index) in form.images" :key="index" class="upload">{{ img }}</view>
							<view class="upload add" @click="addImage">+</view>
						</view>
					</view>

					<view class="grid">
						<view class="field">
							<text class="label">标题</text>
							<input v-model="form.title" class="input" placeholder="例如：27 英寸 2K 显示器" />
						</view>
						<view class="field">
							<text class="label">分类</text>
							<input v-model="form.category" class="input" placeholder="数码影音 / 家居生活" />
						</view>
						<view class="field">
							<text class="label">价格</text>
							<input v-model="form.price" class="input" type="number" placeholder="请输入价格" />
						</view>
						<view class="field">
							<text class="label">成色/状态</text>
							<input v-model="form.condition" class="input" placeholder="全新 / 9 成新 / 有瑕疵" />
						</view>
					</view>

					<view class="field">
						<text class="label">商品描述</text>
						<textarea v-model="form.desc" class="textarea" placeholder="描述规格、购买时间、使用感受、瑕疵和售后约定" />
					</view>

					<view class="field">
						<text class="label">二手故事 / 新品卖点</text>
						<textarea v-model="form.story" class="textarea small" placeholder="二手商品可以写它的前世今生，新品可以写核心卖点" />
					</view>

					<view class="grid">
						<view class="field">
							<text class="label">所在地区</text>
							<input v-model="form.location" class="input" placeholder="城市或学校" />
						</view>
						<view class="field">
							<text class="label">最低可接受价</text>
							<input v-model="form.floorPrice" class="input" type="number" placeholder="用于 AI 议价" />
						</view>
					</view>

					<view class="actions">
						<view class="draft" @click="saveDraft">保存草稿</view>
						<view class="submit" @click="submit">提交审核</view>
					</view>
				</view>

				<view class="side-card">
					<text class="side-title">AI 发布助手</text>
					<view v-for="item in aiSuggestions" :key="item.title" class="suggestion">
						<text class="suggest-title">{{ item.title }}</text>
						<text class="suggest-desc">{{ item.desc }}</text>
					</view>
					<view class="audit-box">
						<text class="audit-title">审核规则</text>
						<text class="audit-desc">发布后会模拟检测违禁词、图片合规和价格异常；通过后自动上架。</text>
					</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				form: {
					scene: 'used',
					images: ['📷'],
					title: '',
					category: '',
					price: '',
					condition: '',
					desc: '',
					story: '',
					location: '',
					floorPrice: ''
				},
				aiSuggestions: [
					{ title: '标题建议', desc: '突出品牌、型号、成色和关键卖点，控制在 20 字左右。' },
					{ title: '估价建议', desc: '参考同类成交价、成色、配件和信用分，给出合理区间。' },
					{ title: '瑕疵说明', desc: '二手商品建议主动说明划痕、维修史和验货方式。' }
				]
			}
		},
		methods: {
			addImage() {
				this.form.images.push(['📷', '🧾', '🔍', '✨'][this.form.images.length % 4])
			},
			fillByAi() {
				if (!this.form.title) this.form.title = '27 英寸 2K 显示器'
				if (!this.form.category) this.form.category = '数码影音'
				if (!this.form.price) this.form.price = '680'
				if (!this.form.condition) this.form.condition = '9 成新'
				if (!this.form.location) this.form.location = '广州大学城'
				if (!this.form.floorPrice) this.form.floorPrice = '620'
				this.form.desc = 'AI 建议：补充品牌型号、接口、购买时间、是否有坏点，并说明支持当面验货。'
				this.form.story = '这台显示器陪我完成了毕业设计，现在桌面升级，希望交给下一位继续使用。'
				uni.showToast({ title: '已生成发布建议', icon: 'success' })
			},
			saveDraft() {
				uni.showToast({ title: '草稿已保存到本地', icon: 'none' })
			},
			submit() {
				if (!this.form.title || !this.form.price) {
					uni.showToast({ title: '请填写标题和价格', icon: 'none' })
					return
				}
				uni.showToast({ title: '已提交审核', icon: 'success' })
				setTimeout(() => {
					uni.navigateTo({ url: '/pages/user/published' })
				}, 500)
			}
		}
	}
</script>

<style lang="scss" scoped>
	.page {
		padding: 28rpx;
	}
	.page-head,
	.actions {
		display: flex;
		align-items: center;
		justify-content: space-between;
		gap: 18rpx;
	}
	.title {
		display: block;
		font-size: 40rpx;
		font-weight: 900;
		color: #17231d;
	}
	.desc {
		display: block;
		margin-top: 8rpx;
		font-size: 25rpx;
		color: #667085;
	}
	.ai-pill,
	.submit,
	.draft {
		border-radius: 16rpx;
		padding: 18rpx 28rpx;
		font-size: 26rpx;
		font-weight: 900;
	}
	.ai-pill,
	.submit {
		background: #1f5c43;
		color: #fff;
	}
	.layout {
		display: grid;
		grid-template-columns: minmax(0, 1fr) 330px;
		gap: 24rpx;
		margin-top: 24rpx;
		align-items: start;
	}
	.form-card,
	.side-card {
		background: #fff;
		border: 1rpx solid #e4e9e5;
		border-radius: 24rpx;
		box-shadow: 0 14rpx 36rpx rgba(15, 35, 26, 0.06);
		padding: 28rpx;
	}
	.section + .section,
	.field {
		margin-top: 24rpx;
	}
	.section-title,
	.side-title,
	.label,
	.suggest-title,
	.audit-title {
		display: block;
		font-weight: 900;
		color: #17231d;
	}
	.section-title,
	.side-title {
		font-size: 30rpx;
		margin-bottom: 16rpx;
	}
	.segmented {
		display: flex;
		background: #e8f0eb;
		border-radius: 18rpx;
		padding: 6rpx;
		width: fit-content;
	}
	.seg {
		min-width: 150rpx;
		text-align: center;
		padding: 16rpx 22rpx;
		border-radius: 14rpx;
		font-size: 25rpx;
		color: #667085;
	}
	.seg.on {
		background: #fff;
		color: #1f5c43;
		font-weight: 900;
	}
	.upload-row {
		display: flex;
		flex-wrap: wrap;
		gap: 14rpx;
	}
	.upload {
		width: 132rpx;
		height: 132rpx;
		border-radius: 20rpx;
		background: #edf3ef;
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 48rpx;
		color: #1f5c43;
	}
	.upload.add {
		border: 2rpx dashed #cbd5d0;
		background: #fff;
	}
	.grid {
		display: grid;
		grid-template-columns: repeat(2, minmax(0, 1fr));
		gap: 20rpx;
	}
	.label {
		font-size: 24rpx;
		margin-bottom: 10rpx;
	}
	.input,
	.textarea {
		background: #f8faf8;
		border-radius: 16rpx;
		padding: 0 20rpx;
		font-size: 28rpx;
		box-sizing: border-box;
		width: 100%;
	}
	.input {
		height: 82rpx;
	}
	.textarea {
		height: 180rpx;
		padding-top: 18rpx;
		line-height: 1.6;
	}
	.textarea.small {
		height: 130rpx;
	}
	.actions {
		margin-top: 30rpx;
		justify-content: flex-end;
	}
	.draft {
		background: #e8f3ed;
		color: #1f5c43;
	}
	.suggestion {
		padding: 18rpx 0;
		border-top: 1rpx solid #eef1ee;
	}
	.suggest-title {
		font-size: 26rpx;
	}
	.suggest-desc,
	.audit-desc {
		display: block;
		margin-top: 8rpx;
		font-size: 24rpx;
		color: #667085;
		line-height: 1.6;
	}
	.audit-box {
		margin-top: 24rpx;
		background: #fff0e7;
		border-radius: 18rpx;
		padding: 20rpx;
	}
	.audit-title {
		font-size: 26rpx;
		color: #b95420;
	}
	@media screen and (max-width: 900px) {
		.layout,
		.grid {
			grid-template-columns: 1fr;
		}
		.page-head {
			align-items: flex-start;
			flex-direction: column;
		}
	}
</style>
