<template>
	<view :id="anchorId" class="wandering-timeline" :class="{ compact, playing: isPlaying, complete: isComplete }">
		<view class="ambient ambient-a"></view>
		<view class="ambient ambient-b"></view>

		<view class="timeline-header">
			<view class="header-main">
				<view class="header-copy">
					<text class="timeline-title">{{ title }}</text>
					<text class="timeline-sub">{{ subtitle }}</text>
				</view>
				<text v-if="badge" class="timeline-badge">{{ badge }}</text>
			</view>
			<view class="progress-row">
				<view class="progress-track">
					<view class="progress-fill" :style="{ width: progressPercent + '%' }"></view>
					<view class="progress-glow" :style="{ left: progressPercent + '%' }"></view>
				</view>
				<view class="progress-meta">
					<text class="step-label">{{ stepLabel }}</text>
					<view v-if="!compact" class="replay-btn" :class="{ disabled: isPlaying }" @click="replay">
						<text class="replay-icon">↻</text>
						<text>{{ isPlaying ? '展开中' : '重新播放' }}</text>
					</view>
				</view>
			</view>
		</view>

		<view class="timeline-track">
			<view class="connector">
				<view class="connector-fill" :style="{ height: connectorHeight + '%' }"></view>
			</view>

			<view
				v-for="(node, index) in nodes"
				:key="nodeKey(node, index)"
				class="timeline-node"
				:class="nodeStateClass(index)"
			>
				<view class="node-marker">
					<view class="marker-ring" v-if="index === activeIndex && isPlaying"></view>
					<view class="node-icon">
						<text>{{ node.icon || defaultIcon(index) }}</text>
					</view>
				</view>
				<view class="node-card">
					<view class="node-card-inner">
						<text class="node-date">{{ node.date || node.time }}</text>
						<text class="node-title">{{ node.title }}</text>
						<text class="node-text">{{ displayText(node, index) }}</text>
						<text v-if="showCursor(index)" class="type-cursor">|</text>
					</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
	const DEFAULT_ICONS = ['买', '用', '发', '审', '新']

	export default {
		name: 'WanderingTimeline',
		props: {
			nodes: {
				type: Array,
				default: () => []
			},
			title: {
				type: String,
				default: '物品流浪时间线'
			},
			subtitle: {
				type: String,
				default: '从首次购入到等待新主人，记录它被认真使用过的痕迹。'
			},
			badge: {
				type: String,
				default: 'Second Life'
			},
			compact: {
				type: Boolean,
				default: false
			},
			autoplay: {
				type: Boolean,
				default: true
			},
			staggerMs: {
				type: Number,
				default: 520
			},
			typeMs: {
				type: Number,
				default: 28
			}
		},
		data() {
			return {
				anchorId: 'wt-' + Math.random().toString(36).slice(2, 9),
				hasStarted: false,
				isPlaying: false,
				visibleCount: 0,
				activeIndex: -1,
				charCounts: {},
				staggerTimer: null,
				typeTimer: null,
				observer: null
			}
		},
		computed: {
			isComplete() {
				return this.nodes.length > 0 && this.visibleCount >= this.nodes.length && !this.isPlaying
			},
			progressPercent() {
				if (!this.nodes.length) return 0
				if (this.isComplete) return 100
				const base = (Math.max(this.visibleCount, 0) / this.nodes.length) * 100
				if (this.activeIndex < 0 || !this.isPlaying) return Math.min(base, 100)
				const node = this.nodes[this.activeIndex]
				const full = this.nodeFullText(node).length || 1
				const typed = this.charCounts[this.activeIndex] || 0
				const slice = ((typed / full) / this.nodes.length) * 100
				return Math.min(base + slice, 100)
			},
			connectorHeight() {
				if (!this.nodes.length) return 0
				if (this.visibleCount <= 1) return 0
				const done = Math.max(this.visibleCount - 1, 0)
				const ratio = done / Math.max(this.nodes.length - 1, 1)
				return Math.min(ratio * 100, 100)
			},
			stepLabel() {
				if (!this.nodes.length) return '暂无履历'
				if (this.isComplete) return `旅程 ${this.nodes.length}/${this.nodes.length} · 等待你续写`
				if (this.isPlaying) return `正在展开 ${Math.min(this.visibleCount, this.nodes.length)}/${this.nodes.length}`
				return `共 ${this.nodes.length} 段故事`
			}
		},
		watch: {
			nodes: {
				handler() {
					this.reset(false)
					this.$nextTick(() => this.bindObserver())
				},
				deep: true
			}
		},
		mounted() {
			this.$nextTick(() => this.bindObserver())
		},
		beforeDestroy() {
			this.clearTimers()
			if (this.observer) {
				this.observer.disconnect()
				this.observer = null
			}
		},
		methods: {
			defaultIcon(index) {
				return DEFAULT_ICONS[index] || '记'
			},
			nodeKey(node, index) {
				return (node.title || 'node') + '-' + index
			},
			nodeFullText(node) {
				return node.text || node.desc || ''
			},
			nodeStateClass(index) {
				if (this.isComplete || index < this.visibleCount - 1) {
					return { done: true, visible: true }
				}
				if (this.isPlaying && index === this.activeIndex) {
					return { active: true, visible: true }
				}
				if (index < this.visibleCount) {
					return { visible: true }
				}
				return { pending: true }
			},
			displayText(node, index) {
				const full = this.nodeFullText(node)
				if (index !== this.activeIndex) {
					return index < this.visibleCount ? full : ''
				}
				const count = this.charCounts[index] || 0
				return full.slice(0, count)
			},
			showCursor(index) {
				return this.isPlaying && index === this.activeIndex && (this.charCounts[index] || 0) < this.nodeFullText(this.nodes[index]).length
			},
			bindObserver() {
				if (!this.autoplay || this.compact || !this.nodes.length) {
					if (this.compact && this.nodes.length) this.revealAll()
					return
				}
				if (this.observer) {
					this.observer.disconnect()
					this.observer = null
				}
				try {
					this.observer = uni.createIntersectionObserver(this, { thresholds: [0, 0.15, 0.35] })
					this.observer.relativeToViewport({ bottom: 80 }).observe('#' + this.anchorId, (res) => {
						if (res.intersectionRatio >= 0.15 && !this.hasStarted) {
							this.hasStarted = true
							this.startReveal()
						}
					})
				} catch (e) {
					this.hasStarted = true
					this.startReveal()
				}
			},
			clearTimers() {
				if (this.staggerTimer) clearTimeout(this.staggerTimer)
				if (this.typeTimer) clearInterval(this.typeTimer)
				this.staggerTimer = null
				this.typeTimer = null
			},
			reset(keepStarted) {
				this.clearTimers()
				this.isPlaying = false
				this.visibleCount = 0
				this.activeIndex = -1
				this.charCounts = {}
				if (!keepStarted) this.hasStarted = false
			},
			revealAll() {
				this.reset(true)
				this.hasStarted = true
				this.visibleCount = this.nodes.length
				this.nodes.forEach((node, index) => {
					this.$set(this.charCounts, index, this.nodeFullText(node).length)
				})
			},
			replay() {
				if (this.isPlaying) return
				this.reset(true)
				this.hasStarted = true
				this.startReveal()
			},
			startReveal() {
				if (!this.nodes.length || this.isPlaying) return
				this.isPlaying = true
				this.visibleCount = 0
				this.activeIndex = -1
				this.charCounts = {}
				this.revealNext()
			},
			revealNext() {
				if (this.activeIndex >= 0) {
					const prev = this.nodes[this.activeIndex]
					this.$set(this.charCounts, this.activeIndex, this.nodeFullText(prev).length)
				}
				const next = this.activeIndex + 1
				if (next >= this.nodes.length) {
					this.finish()
					return
				}
				this.activeIndex = next
				this.visibleCount = next + 1
				this.$set(this.charCounts, next, 0)
				this.runTypewriter(next, () => {
					this.staggerTimer = setTimeout(() => this.revealNext(), this.staggerMs)
				})
			},
			runTypewriter(index, done) {
				const full = this.nodeFullText(this.nodes[index])
				if (!full.length) {
					done()
					return
				}
				if (this.typeTimer) clearInterval(this.typeTimer)
				this.typeTimer = setInterval(() => {
					const current = this.charCounts[index] || 0
					if (current >= full.length) {
						clearInterval(this.typeTimer)
						this.typeTimer = null
						done()
						return
					}
					this.$set(this.charCounts, index, current + 1)
				}, this.typeMs)
			},
			finish() {
				this.clearTimers()
				this.isPlaying = false
				this.activeIndex = -1
				this.visibleCount = this.nodes.length
			}
		}
	}
</script>

<style lang="scss" scoped>
	.wandering-timeline {
		position: relative;
		overflow: hidden;
	}

	.ambient {
		position: absolute;
		border-radius: 50%;
		filter: blur(40rpx);
		pointer-events: none;
		opacity: 0.55;
	}
	.ambient-a {
		width: 280rpx;
		height: 280rpx;
		top: -80rpx;
		right: -40rpx;
		background: rgba(240, 164, 92, 0.35);
		animation: drift-a 8s ease-in-out infinite;
	}
	.ambient-b {
		width: 220rpx;
		height: 220rpx;
		bottom: 40rpx;
		left: -60rpx;
		background: rgba(47, 111, 80, 0.18);
		animation: drift-b 10s ease-in-out infinite;
	}

	.timeline-header {
		position: relative;
		z-index: 1;
		margin-bottom: 28rpx;
	}

	.header-main {
		display: flex;
		align-items: flex-start;
		justify-content: space-between;
		gap: 18rpx;
	}

	.header-copy {
		flex: 1;
		min-width: 0;
	}

	.timeline-title {
		display: block;
		padding-left: 16rpx;
		border-left: 8rpx solid #d66a2c;
		font-size: 32rpx;
		font-weight: 900;
		color: #17231d;
		line-height: 1.3;
	}

	.timeline-sub {
		display: block;
		margin-top: 10rpx;
		font-size: 24rpx;
		color: #7b5a42;
		line-height: 1.65;
	}

	.timeline-badge {
		padding: 10rpx 16rpx;
		border-radius: 999rpx;
		background: #17231d;
		color: #fff;
		font-size: 21rpx;
		font-weight: 900;
		white-space: nowrap;
		flex-shrink: 0;
	}

	.progress-row {
		margin-top: 22rpx;
	}

	.progress-track {
		position: relative;
		height: 8rpx;
		border-radius: 999rpx;
		background: rgba(214, 106, 44, 0.12);
		overflow: hidden;
	}

	.progress-fill {
		height: 100%;
		border-radius: inherit;
		background: linear-gradient(90deg, #d66a2c, #f0a45c);
		transition: width 0.45s cubic-bezier(0.22, 1, 0.36, 1);
	}

	.progress-glow {
		position: absolute;
		top: 50%;
		width: 20rpx;
		height: 20rpx;
		margin: -10rpx 0 0 -10rpx;
		border-radius: 50%;
		background: #fff;
		box-shadow: 0 0 16rpx rgba(214, 106, 44, 0.8);
		transition: left 0.45s cubic-bezier(0.22, 1, 0.36, 1);
	}

	.progress-meta {
		display: flex;
		align-items: center;
		justify-content: space-between;
		margin-top: 12rpx;
		gap: 16rpx;
	}

	.step-label {
		font-size: 22rpx;
		color: #667085;
	}

	.replay-btn {
		display: flex;
		align-items: center;
		gap: 6rpx;
		padding: 8rpx 16rpx;
		border-radius: 999rpx;
		background: rgba(23, 35, 29, 0.06);
		color: #1f5c43;
		font-size: 22rpx;
		font-weight: 700;
	}
	.replay-btn.disabled {
		opacity: 0.55;
	}
	.replay-icon {
		font-size: 24rpx;
	}

	.timeline-track {
		position: relative;
		z-index: 1;
		padding: 8rpx 0 4rpx;
	}

	.connector {
		position: absolute;
		left: 27rpx;
		top: 36rpx;
		bottom: 36rpx;
		width: 4rpx;
		border-radius: 999rpx;
		background: rgba(231, 173, 114, 0.2);
		overflow: hidden;
	}

	.connector-fill {
		width: 100%;
		background: linear-gradient(180deg, #d66a2c, #f0a45c 70%, rgba(240, 164, 92, 0.2));
		transition: height 0.55s cubic-bezier(0.22, 1, 0.36, 1);
		border-radius: inherit;
	}

	.timeline-node {
		position: relative;
		display: flex;
		gap: 18rpx;
		padding-bottom: 28rpx;
		opacity: 0;
		transform: translateY(28rpx);
	}
	.timeline-node.visible {
		animation: node-enter 0.55s cubic-bezier(0.22, 1, 0.36, 1) forwards;
	}
	.timeline-node.pending {
		opacity: 0;
		transform: translateY(28rpx);
		pointer-events: none;
	}

	.node-marker {
		position: relative;
		width: 56rpx;
		flex-shrink: 0;
		display: flex;
		justify-content: center;
	}

	.marker-ring {
		position: absolute;
		top: 50%;
		left: 50%;
		width: 72rpx;
		height: 72rpx;
		margin: -36rpx 0 0 -36rpx;
		border-radius: 50%;
		border: 2rpx solid rgba(214, 106, 44, 0.45);
		animation: pulse-ring 1.6s ease-out infinite;
	}

	.node-icon {
		position: relative;
		z-index: 1;
		width: 56rpx;
		height: 56rpx;
		border-radius: 18rpx;
		background: linear-gradient(135deg, #d66a2c, #f0a45c);
		color: #fff;
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 24rpx;
		font-weight: 900;
		box-shadow: 0 10rpx 22rpx rgba(214, 106, 44, 0.24);
		transition: transform 0.35s ease, box-shadow 0.35s ease;
	}
	.timeline-node.active .node-icon {
		transform: scale(1.06);
		box-shadow: 0 14rpx 30rpx rgba(214, 106, 44, 0.36);
	}
	.timeline-node.done .node-icon {
		background: linear-gradient(135deg, #2f6f50, #4a8f6a);
		box-shadow: 0 8rpx 18rpx rgba(47, 111, 80, 0.22);
	}

	.node-card {
		flex: 1;
		min-width: 0;
	}

	.node-card-inner {
		padding: 20rpx 22rpx;
		border-radius: 20rpx;
		background: rgba(255, 255, 255, 0.88);
		border: 1rpx solid #f0dfce;
		box-shadow: 0 8rpx 24rpx rgba(23, 35, 29, 0.04);
		transition: border-color 0.35s ease, box-shadow 0.35s ease, transform 0.35s ease;
	}
	.timeline-node.active .node-card-inner {
		border-color: rgba(214, 106, 44, 0.45);
		box-shadow: 0 14rpx 32rpx rgba(214, 106, 44, 0.12);
		transform: translateX(4rpx);
	}
	.timeline-node.done .node-card-inner {
		border-color: #e2eee6;
		background: rgba(255, 255, 255, 0.72);
	}

	.node-date,
	.node-title,
	.node-text {
		display: block;
	}

	.node-date {
		font-size: 22rpx;
		color: #667085;
		letter-spacing: 0.02em;
	}

	.node-title {
		margin-top: 4rpx;
		font-size: 28rpx;
		font-weight: 900;
		color: #17231d;
	}

	.node-text {
		margin-top: 8rpx;
		font-size: 24rpx;
		color: #667085;
		line-height: 1.65;
		min-height: 1.65em;
	}

	.type-cursor {
		display: inline;
		margin-left: 4rpx;
		color: #d66a2c;
		font-weight: 300;
		animation: blink 0.9s step-end infinite;
	}

	.wandering-timeline.compact .timeline-sub,
	.wandering-timeline.compact .progress-row,
	.wandering-timeline.compact .timeline-badge {
		display: none;
	}
	.wandering-timeline.compact .timeline-node {
		padding-bottom: 16rpx;
	}
	.wandering-timeline.compact .node-text {
		display: none;
	}

	@keyframes node-enter {
		from {
			opacity: 0;
			transform: translateY(28rpx);
		}
		to {
			opacity: 1;
			transform: translateY(0);
		}
	}

	@keyframes pulse-ring {
		0% {
			transform: scale(0.72);
			opacity: 0.9;
		}
		100% {
			transform: scale(1.15);
			opacity: 0;
		}
	}

	@keyframes blink {
		50% {
			opacity: 0;
		}
	}

	@keyframes drift-a {
		0%,
		100% {
			transform: translate(0, 0);
		}
		50% {
			transform: translate(-16rpx, 12rpx);
		}
	}

	@keyframes drift-b {
		0%,
		100% {
			transform: translate(0, 0);
		}
		50% {
			transform: translate(12rpx, -10rpx);
		}
	}
</style>
