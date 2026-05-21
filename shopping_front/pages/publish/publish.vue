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
                            <view v-if="form.image" class="upload" @click="uploadImage">
                                <image :src="form.image" mode="aspectFill" style="width: 100%; height: 100%; border-radius: 20rpx;"></image>
                            </view>
                            <view v-else class="upload add" @click="uploadImage">+</view>
                        </view>
                    </view>

                    <view class="grid">
                        <view class="field">
                            <text class="label">标题</text>
                            <input v-model="form.goods_name" class="input" placeholder="例如：[北航] 考研数学资料" />
                        </view>
                        <view class="field">
                            <text class="label">分类</text>
                            <input v-model="form.category" class="input" placeholder="数码影音 / 学习资料" />
                        </view>
                        <view class="field">
                            <text class="label">价格</text>
                            <input v-model="form.price" class="input" type="digit" placeholder="0.00" />
                        </view>
                        <view class="field">
                            <text class="label">成色/状态</text>
                            <input v-model="form.condition" class="input" placeholder="全新 / 9成新 / 有瑕疵" />
                        </view>
                    </view>

                    <view class="field">
                        <text class="label">商品描述</text>
                        <textarea v-model="form.goods_desc" class="textarea" placeholder="详细描述：说明新旧程度、使用情况、转手原因等..." />
                    </view>

                    <view class="field">
                        <text class="label">二手故事 / 新品卖点</text>
                        <textarea v-model="form.story" class="textarea small" placeholder="二手商品可以写它的前世今生，新品可以写核心卖点..." />
                    </view>

                    <view class="grid">
                        <view class="field">
                            <text class="label">所在位置</text>
                            <input v-model="form.address" class="input" placeholder="城市或学校" />
                        </view>
                        <view class="field">
                            <text class="label">最低可接受价</text>
                            <input v-model="form.floor_price" class="input" type="digit" placeholder="用于 AI 议价保底" />
                        </view>
                    </view>

                    <view class="actions">
                        <button class="submit" :disabled="loading" @click="handlePublish">发布商品</button>
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
    import { post } from '@/utils/request.js' 
    import { buildRequestUrl } from '../../config/env.js'

    export default {
        data() {
            return {
                loading: false,
                form: {
                    goods_name: '',
                    goods_desc: '',
                    price: '',
                    image: '',
                    address: '北京航空航天大学(学院路校区)',
                    scene: 'used',       
                    category: '',        
                    condition: '',       
                    story: '',           
                    floor_price: ''      
                },
                aiSuggestions: [
                    { title: '标题建议', desc: '突出品牌、型号、成色和关键卖点，控制在 20 字左右。' },
                    { title: '估价建议', desc: '参考同类成交价、成色、配件和信用分，给出合理区间。' },
                    { title: '瑕疵说明', desc: '二手商品建议主动说明划痕、维修史和验货方式。' }
                ]
            }
        },

        methods: {
            uploadImage() {
                uni.chooseImage({
                    count: 1,
                    success: (res) => {
                        const tempFilePath = res.tempFilePaths[0];
                        uni.showLoading({ title: '上传中...' });
            
                        uni.uploadFile({
                            url: buildRequestUrl('/api/upload/image'),
                            filePath: tempFilePath,
                            name: 'file', 
                            success: (uploadRes) => {
                                let data = uploadRes.data;
                                if (typeof data === 'string') {
                                    data = JSON.parse(data);
                                }
                                if (data.code === 200 || data.code === 0) {
                                    this.form.image = data.data; 
                                    uni.showToast({ title: '上传成功' });
                                }
                            },
                            fail: () => {
                                uni.showToast({ title: '网络连接失败', icon: 'none' });
                            },
                            complete: () => uni.hideLoading()
                        });
                    }
                });
            },

            handlePublish() {
                if (!this.form.goods_name || !this.form.price) {
                    return uni.showToast({ title: '标题和价格不能为空', icon: 'none' });
                }

                uni.showLoading({ title: '发布中...', mask: true });
                this.loading = true;

                post('/api/goods/publish', {
                    goodsName: this.form.goods_name,
                    goodsDesc: this.form.goods_desc,
                    price: parseFloat(this.form.price),
                    address: this.form.address,
                    image: this.form.image,
                    scene: this.form.scene,                                                     
                    category: this.form.category,                                               
                    goodsCondition: this.form.condition,                                        
                    story: this.form.story,                                                     
                    floorPrice: this.form.floor_price ? parseFloat(this.form.floor_price) : null 
                })
                .then(res => {
                    uni.showToast({ title: '提交审核成功', icon: 'success' });
                    setTimeout(() => {
                        uni.navigateBack();
                    }, 1500);
                })
                .catch(err => {
                    console.error('发布失败错误日志：', err);
                    uni.showToast({ title: '发布失败，请重试', icon: 'none' });
                })
                .finally(() => {
                    this.loading = false;
                    uni.hideLoading();
                });
            },

            // 预留的 AI 模拟快速填表函数，方便日常开发和演示测试
            fillByAi() {
                this.form.goods_name = '[北航] 27 英寸 2K 显示器';
                this.form.category = '数码影音';
                this.form.price = '680';
                this.form.condition = '9 成新';
                this.form.address = '北京航空航天大学(学院路校区)';
                this.form.floor_price = '620';
                this.form.goods_desc = '补充说明：接口齐全，屏幕无坏点，日常写代码和看论文体验极佳，寝室当面验货。';
                this.form.story = '这台显示器陪我熬过了好几个写系统内核实验和论文的夜晚，现在准备升级桌面，转给需要的同学。';
                this.form.image = 'https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?w=500';
                uni.showToast({ title: '已生成发布建议', icon: 'success' });
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
