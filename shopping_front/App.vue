<script>
	export default {
		onLaunch() {
			// #ifdef MP-WEIXIN
			const tabIcons = [
				['static/tabbar/home.png', 'static/tabbar/home-active.png'],
				['static/tabbar/browse.png', 'static/tabbar/browse-active.png'],
				['static/tabbar/cart.png', 'static/tabbar/cart-active.png'],
				['static/tabbar/user.png', 'static/tabbar/user-active.png']
			]
			tabIcons.forEach(([iconPath, selectedIconPath], index) => {
				uni.setTabBarItem({ index, iconPath, selectedIconPath })
			})
			// #endif
		},
		onShow() {},
		onHide() {}
	}
</script>

<style lang="scss">
	page {
		background:
			radial-gradient(circle at 18% 6%, rgba(66, 133, 244, 0.08), transparent 28%),
			radial-gradient(circle at 78% 12%, rgba(251, 188, 5, 0.06), transparent 26%),
			radial-gradient(circle at 50% 92%, rgba(52, 168, 83, 0.05), transparent 30%),
			linear-gradient(180deg, #ffffff 0%, #f7f8fa 48%, #f1f4f7 100%);
		color: #161616;
		font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Hiragino Sans GB',
			'Microsoft YaHei', sans-serif;
		-webkit-font-smoothing: antialiased;
	}

	@media screen and (min-width: 961px) {
		uni-tabbar,
		.uni-tabbar,
		uni-tabbar .uni-tabbar {
			display: none !important;
		}
	}

	@keyframes softIn {
		from {
			opacity: 0;
			transform: translateY(14px);
		}
		to {
			opacity: 1;
			transform: translateY(0);
		}
	}
</style>
