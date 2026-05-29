<template>
	<view class="safe-page">
		<view class="content-wrap page">
			<view class="hero">
				<text class="title">消息与 AI 议价</text>
				<text class="desc">买家、卖家和 AI 助手在这里确认商品细节、价格和交易共识。</text>
			</view>
			<view v-for="item in list" :key="item.title" class="row" @click="open(item)">
				<view class="avatar">{{ item.icon }}</view>
				<view class="body">
					<text class="name">{{ item.title }}</text>
					<text class="sub">{{ item.sub }}</text>
				</view>
				<text class="time">{{ item.time }}</text>
			</view>
			<view class="consensus">
				<text class="consensus-title">交易共识清单示例</text>
				<text class="consensus-text">{{ assist.consensus }}</text>
				<view class="checklist">
					<text v-for="item in assist.checklist" :key="item" class="check-item">{{ item }}</text>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
	import { requestAiAssist } from '@/services/shop.js'

	export default {
		data() {
			return {
				assist: {
					consensus: '价格 650 元，平台担保下单；卖家承诺无坏点，买家收货 48 小时内完成验货。',
					checklist: ['确认商品实拍图', '确认是否支持平台担保', '确认瑕疵和售后约定']
				},
				list: [
					{ icon: '🤖', title: 'AI 议价助手', sub: '建议先确认瑕疵、配件、发票和最低可接受价。', time: '刚刚' },
					{ icon: '🖥️', title: '阿洛的桌面仓库', sub: '显示器支持同城验货，今晚 7 点后方便。', time: '10:24' },
					{ icon: '🛡️', title: '官方客服', sub: '平台担保交易已开启，资金将在确认收货后结算。', time: '昨天' }
				]
			}
		},
		onShow() {
			this.loadAssist()
		},
		methods: {
			async loadAssist() {
				try {
					const body = await requestAiAssist({ productId: 'used-monitor', question: '能便宜一点吗？有没有坏点？', offer: 620 })
					if (body && body.code === 0 && body.data) {
						this.assist = body.data
						this.list[0].sub = body.data.answer
					}
				} catch (e) {}
			},
			open(item) {
				uni.showToast({ title: item.title + ' 功能演示中', icon: 'none' })
			}
		}
	}
</script>

<style lang="scss" scoped>
	.page { padding: 28rpx; }
	.hero, .row, .consensus { background: #fff; border: 1rpx solid #e4e9e5; border-radius: 24rpx; box-shadow: 0 14rpx 36rpx rgba(15, 35, 26, 0.06); }
	.hero { padding: 30rpx; margin-bottom: 20rpx; }
	.title { display: block; font-size: 40rpx; font-weight: 900; color: #17231d; }
	.desc { display: block; margin-top: 10rpx; font-size: 25rpx; color: #667085; line-height: 1.6; }
	.row { display: flex; align-items: center; gap: 18rpx; padding: 24rpx; margin-bottom: 16rpx; }
	.avatar { width: 76rpx; height: 76rpx; border-radius: 20rpx; background: #edf3ef; display: flex; align-items: center; justify-content: center; font-size: 36rpx; }
	.body { flex: 1; min-width: 0; }
	.name, .sub { display: block; }
	.name { font-size: 29rpx; font-weight: 900; color: #17231d; }
	.sub { margin-top: 6rpx; font-size: 24rpx; color: #667085; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
	.time { font-size: 22rpx; color: #9ca3af; }
	.consensus { padding: 26rpx; margin-top: 22rpx; background: #fff0e7; border-color: #ffe0cd; }
	.consensus-title, .consensus-text { display: block; }
	.consensus-title { font-size: 28rpx; font-weight: 900; color: #b95420; }
	.consensus-text { margin-top: 10rpx; font-size: 25rpx; color: #70401f; line-height: 1.6; }
	.checklist { display: flex; flex-direction: column; gap: 8rpx; margin-top: 16rpx; }
	.check-item { display: block; font-size: 24rpx; color: #70401f; }
</style>
