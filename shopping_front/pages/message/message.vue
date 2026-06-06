<template>
	<view class="safe-page">
		<view class="topbar">
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
		<view class="content-wrap page">
			<view class="hero">
				<text class="title">消息与 AI 议价</text>
				<text class="desc">买家、卖家和 AI 助手在这里确认商品细节、价格和交易共识。</text>
			</view>
			<view v-for="item in list" :key="item.covId" class="row" @click="open(item)">
				<view class="avatar-wrapper">
        			<view class="avatar">{{ item.icon }}</view>
       				<text v-if="item.unreadCount > 0" class="badge">{{ item.unreadCount }}</text>
    			</view>
    			<view class="body">
        			<text class="name">{{ item.title }}</text>
        			<text class="sub">{{ item.sub }}</text>
					<text class="time">{{ item.time }}</text>
    			</view>
				<view class="goods-thumb" v-if="item.goodsImageUrl">
        			<image :src="item.goodsImageUrl" mode="aspectFill"></image>
    			</view>
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
	import { get } from '@/utils/request.js';

	export default {
		data() {
			return {
				assist: {
					consensus: '价格 650 元，平台担保下单；卖家承诺无坏点，买家收货 48 小时内完成验货。',
					checklist: ['确认商品实拍图', '确认是否支持平台担保', '确认瑕疵和售后约定']
				},
				list: [
					/*{ icon: '🤖', title: 'AI 议价助手', sub: '建议先确认瑕疵、配件、发票和最低可接受价。', time: '刚刚' },
					{ icon: '🖥️', title: '阿洛的桌面仓库', sub: '显示器支持同城验货，今晚 7 点后方便。', time: '10:24' },
					{ icon: '🛡️', title: '官方客服', sub: '平台担保交易已开启，资金将在确认收货后结算。', time: '昨天' }*/
				]
			}
		},
		onShow() {
			this.loadAssist()
			this.fetchConversationList()
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
			async fetchConversationList() {
  				try {
    				const res = await get('/api/chat/conversations')
					console.log("=== [调试] 后端返回的数据:", res);
					const conversationList = (res.data && res.data.data) ? res.data.data : [];
                    this.list = conversationList.map(item => ({
                        icon: '💬',
                        title: item.targetName || '用户', 
                        sub: item.lastMessage || '暂无消息',
                        time: this.formatTime(item.lastTime),
                        covId: item.covId,
						unreadCount: item.unreadCount || 0,
						goodsImageUrl: item.goodsImageUrl
                    }))
  				} catch (e) {
    				console.error("加载会话列表失败", e);
  				}
			},
			async open(item) {
    			if (item.unreadCount > 0) {
        			item.unreadCount = 0; 
    			}
    			try {
        			await post(`/api/chat/${item.covId}/read`);
    			} catch (e) {
        			console.error("标记已读接口调用失败", e);
    			}
    			uni.navigateTo({ 
        			url: `/pages/chat/chat?covId=${item.covId}` 
    			});
			},
			navTo(url) {
				if (['/pages/home/home', '/pages/browse/browse', '/pages/cart/cart', '/pages/message/message', '/pages/user/index'].includes(url)) {
					uni.switchTab({ url })
					return
				}
				uni.reLaunch({ url })
			},
		 	formatTime(dateStr) {
      			if (!dateStr) return ''
    			const timePart = dateStr.split('T')[1].split('.')[0]
    			const [h, m] = timePart.split(':')
    			return `${h}:${m}`
    		}
		}
	}
</script>

<style lang="scss" scoped>
	.topbar {
		position: sticky;
		top: 0;
		z-index: 10;
		background: rgba(255,255,255,.88);
		backdrop-filter: blur(22px);
		border-bottom: 1px solid rgba(203, 213, 225, .55);
		box-shadow: 0 10px 40px rgba(60, 64, 67, .06);
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
		width: 40px;
		height: 40px;
		border-radius: 8px;
		display: flex;
		align-items: center;
		justify-content: center;
		overflow: hidden;
		flex-shrink: 0;
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
		color: #202124;
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
		box-sizing: border-box;
		height: 50px;
		border-radius: 999px;
		background: rgba(255,255,255,.72);
		border: 1px solid rgba(203, 213, 225, .72);
		box-shadow: 0 14px 38px rgba(60, 64, 67, .08);
	}
	.nav-link {
		width: 82px;
		height: 38px;
		padding: 0;
		border-radius: 999px;
		display: flex;
		align-items: center;
		justify-content: center;
		box-sizing: border-box;
		line-height: 1;
		text-align: center;
		color: #5f6b85;
		font-size: 13px;
		font-weight: 750;
	}
	.nav-link.on,
	.nav-link:hover {
		background: #fff;
		background: linear-gradient(135deg, #ffffff, #f5f7fa);
		color: #12372a;
		box-shadow: 0 10px 26px rgba(18, 55, 42, .14);
	}
	.page { 
		padding: 28rpx; 
		animation: softIn .45s ease both;
	}
	.hero, .consensus { 
		background: #fff; 
		border: 1rpx solid #e4e9e5; 
		border-radius: 24rpx; 
		box-shadow: 0 14rpx 36rpx rgba(15, 35, 26, 0.06); 
	}
	.row {
		display: flex;
  		align-items: center;
  		padding: 18px;           
  		background: #fff;
  		border: 1px solid #e4e9e5; 
  		border-radius: 12px;     
  		box-shadow: 0 16px 48px rgba(17, 38, 28, 0.065);
  		transition: transform .22s ease, box-shadow .22s ease, border-color .22s ease;
  		margin-bottom: 14px;     
	}
	.row:hover {
  		transform: translateY(-2px);
  		box-shadow: 0 22px 64px rgba(17, 38, 28, 0.1);
 	 	border-color: rgba(31, 92, 67, .16);
	}
	.hero { 
		padding: 30rpx; 
		margin-bottom: 20rpx; 
	}
	.title { 
		display: block; 
		font-size: 40rpx; 
		font-weight: 900; 
		color: #17231d; 
	}
	.desc { 
		display: block; 
		margin-top: 10rpx; 
		font-size: 25rpx; 
		color: #667085; 
		line-height: 1.6; 
	}
	.avatar { 
		width: 76rpx; 
		height: 76rpx; 
		border-radius: 20rpx; 
		background: #edf3ef; 
		display: flex; 
		align-items: center; 
		justify-content: center; 
		font-size: 36rpx; 
	}
	.body {
    	flex: 1;
    	display: flex;
    	flex-direction: column;
    	justify-content: center;
    	margin: 0 24rpx;
    	overflow: hidden;
	}
	.name, .sub { 
		display: block; 
	}
	.name { 
		font-size: 29rpx; 
		font-weight: 900; 
		color: #17231d; 
	}
	.sub { 
		margin-top: 6rpx; 
		font-size: 24rpx; 
		color: #667085; 
		overflow: hidden; 
		text-overflow: ellipsis; 
		white-space: nowrap; 
	}
	.time { 
		font-size: 20rpx; 
    	color: #9ca3af;
    	margin-top: 6rpx;
	}
	.consensus { 
		padding: 26rpx; 
		margin-top: 22rpx; 
		background: #fff0e7; 
		border-color: #ffe0cd; 
	}
	.consensus-title, .consensus-text { 
		display: block; 
	}
	.consensus-title { 
		font-size: 28rpx; 
		font-weight: 900; 
		color: #b95420; 
	}
	.consensus-text { 
		margin-top: 10rpx; 
		font-size: 25rpx; 
		color: #70401f; 
		line-height: 1.6; 
	}
	.checklist { 
		display: flex; 
		flex-direction: 
		column; gap: 8rpx; 
		margin-top: 16rpx; 
	}
	.check-item { 
		display: block; 
		font-size: 24rpx; 
		color: #70401f; 
	}
	.avatar-wrapper {
    	position: relative;
    	width: 80rpx; 
    	height: 80rpx;
    	margin-right: 20rpx;
	}
	.badge {
    	position: absolute;
    	top: -5rpx;
    	right: -5rpx;
    	background-color: #12372a;
    	color: #ffffff;
    	font-size: 20rpx;
    	padding: 0 10rpx;
    	min-width: 32rpx;
    	height: 32rpx;
    	line-height: 32rpx;
    	border-radius: 16rpx;
    	text-align: center;
    	font-weight: bold;
    	z-index: 10;
	}
	.goods-thumb {
    	width: 50px;
    	height: 50px;
    	margin-right: 10px;
    	border-radius: 4px;
    	overflow: hidden;
    	flex-shrink: 0;
	}

	.goods-thumb image {
    	width: 100%;
    	height: 100%;
	}

	.pill {
    	padding: 4rpx 16rpx;
    	background: #f0f0f0;
    	border-radius: 20rpx;
    	font-size: 22rpx;
    	color: #666;
	}
</style>
