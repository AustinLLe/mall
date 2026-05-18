<template>
    <view class="page">
        <view class="header">
            <text class="h">发布闲置</text>
            <button class="publish-btn" :disabled="loading" @click="handlePublish">发布</button>
        </view>

        <view class="upload-card" @click="uploadImage">
            <image v-if="form.image" :src="form.image" mode="aspectFill" class="preview-img" />
            <view v-else class="upload-placeholder">
                <text class="plus">+</text>
                <text>点击添加商品图片</text>
            </view>
        </view>

        <view class="input-card">
            <input 
                class="title-input" 
                v-model="form.goods_name" 
                placeholder="标题：例如[北航] 考研数学资料" 
            />
            <view class="divider"></view>
            <textarea 
                class="desc-input" 
                v-model="form.goods_desc" 
                placeholder="详细描述：说明新旧程度、使用情况、转手原因等..." 
            />
        </view>

        <view class="list-card">
            <view class="item">
                <text class="label">价格</text>
                <view class="right">
                    <text class="currency">¥</text>
                    <input 
                        type="digit" 
                        v-model="form.price" 
                        placeholder="0.00" 
                        class="price-input" 
                    />
                </view>
            </view>
            <view class="item">
                <text class="label">所在位置</text>
                <text class="value">{{ form.address }}</text>
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
                }
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
                })
                .then(res => {
                    uni.showToast({ title: '发布成功', icon: 'success' });
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
            }
        }
    }
</script>

<style lang="scss" scoped>
    .page {
        padding: 30rpx;
        background-color: #f7f7f7;
        min-height: 100vh;
    }
    .header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 30rpx;
        .h { font-size: 38rpx; font-weight: bold; color: #1b4332; }
        .publish-btn {
            margin: 0; padding: 0 44rpx; height: 68rpx; line-height: 68rpx;
            background: #1b4332; color: #fff; font-size: 28rpx; border-radius: 34rpx;
        }
    }
    .upload-card {
        height: 300rpx; background: #fff; border-radius: 20rpx;
        display: flex; justify-content: center; align-items: center;
        overflow: hidden; margin-bottom: 24rpx;
        .upload-placeholder {
            display: flex; flex-direction: column; align-items: center;
            color: #ccc; font-size: 26rpx;
            .plus { font-size: 80rpx; line-height: 1; margin-bottom: 10rpx; }
        }
        .preview-img { width: 100%; height: 100%; }
    }
    .input-card {
        background: #fff; border-radius: 20rpx; padding: 24rpx; margin-bottom: 24rpx;
        .title-input { font-size: 34rpx; font-weight: bold; padding: 10rpx 0; color: #333; }
        .divider { height: 1rpx; background: #f0f0f0; margin: 16rpx 0; }
        .desc-input { width: 100%; height: 240rpx; font-size: 30rpx; color: #444; }
    }
    .list-card {
        background: #fff; border-radius: 20rpx; padding: 0 24rpx;
        .item {
            display: flex; justify-content: space-between; align-items: center;
            padding: 32rpx 0; border-bottom: 1rpx solid #f8f8f8;
            &:last-child { border-bottom: none; }
            .label { font-size: 30rpx; color: #333; }
            .right { display: flex; align-items: center; }
            .price-input { text-align: right; font-size: 34rpx; color: #1b4332; font-weight: bold; width: 220rpx; }
            .currency { color: #1b4332; font-weight: bold; font-size: 34rpx; margin-right: 4rpx; }
            .value { font-size: 28rpx; color: #666; }
        }
    }
</style>


