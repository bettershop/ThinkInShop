<template>
  <view class="upload-container">

    <!-- 编辑凭证提示栏 -->
    <view class="edit-tips"> 
      <view class="nav-bar">
      <view class="nav-left">
        <text class="laiketui laiketui-zuo" @click="goBack"></text>
      </view>
      <text class="nav-title">{{language.voucher.zfpc}}</text>
      <view class="nav-right"></view>
      </view>
      <view class="edit-tips-content">
        <text class="edit-title">{{language.voucher.pjpz}}</text>
        <text class="edit-desc">{{language.voucher.cjtj}}</text>
      </view>
    </view>

    <!-- 图片上传区域 -->
    <view class="upload-wrap">
      <!-- 已上传的图片 -->
      <view class="img-container">

          <view class="label"><span class="required">*</span>{{language.voucher.zfpz}}</view>
          <view 
          class="img-item" 
          v-for="(img, index) in form.voucher" 
          :key="index"
          >
           <image :src="img" mode="aspectFill" class="img-preview"></image>
           <view class="del-btn" @click="removeMedia(index)">×</view>
          </view>          
          <view 
            class="upload-btn" 
            @click="chooseImage"
            v-if="form.voucher.length < 3"
          >
            <text style="font-size: 45rpx;" class="laiketui laiketui-zhaoxiangji"></text>
            <text style="text-align: center;">{{language.voucher.sctp}}</text> 
          </view>

      </view>
      <view v-if="form.reason_for_rejection != ''">
        <view style="margin-top: 36rpx;" class="label"><span class="required">*</span>{{language.voucher.wtg}}：</view>
        <view>{{ form.reason_for_rejection }}</view>
      </view>
    </view>
    <div  class="submit-offline" >
        <div class="sub-button" @click="submitAudit()">{{language.voucher.tjst}}</div>
    </div>
  </view>
</template>
<script>
export default {
  data() {
    return {
        form: {
          voucher: [],
          reason_for_rejection: '',
          sNo: ''
        }
    };
  },
  onLoad() {
    //拿到本地存储voucher
    const voucherData = uni.getStorageSync('voucher');
    if (voucherData && voucherData.voucher) {
      this.form.voucher = voucherData.voucher.split(',') || [];
      this.form.reason_for_rejection = voucherData?.reason_for_rejection || '';
    }
    this.form.sNo = voucherData.sNo;
  },

  methods: {
    // 选择图片（最多选2张，和界面保持一致）
    chooseImage() {
      const maxCount = 2;
      // 允许的图片后缀（覆盖常见格式） 
      uni.chooseImage({
        count: maxCount - this.form.voucher.length, // 计算剩余可上传数量
        sourceType: ['album'],
        success: async (res) => {
             const allowImageExts = ['jpg', 'jpeg', 'png', 'gif', 'webp', 'bmp'];
                // 允许的图片 MIME 类型（核心：拦截 MP4 的 video/mp4）
                const allowImageMimes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp', 'image/bmp'];
                for (let i = 0; i < res.tempFilePaths.length; i++) {
                  const tempPath = res.tempFilePaths[i];
                  const tempFile = res.tempFiles[i]; // 获取文件完整信息（含 size、type 等）
                  let isImage = false;
                  // 场景1：有文件 MIME 类型（优先校验，最准确）
                  if (tempFile.type) {
                    isImage = allowImageMimes.includes(tempFile.type);
                  } 
                  // 场景2：无 MIME 类型，但有后缀（校验后缀）
                  else if (tempPath.includes('.')) {
                    const ext = tempPath.split('.').pop().toLowerCase();
                    isImage = allowImageExts.includes(ext);
                  } 
                  // 场景3：无 MIME + 无后缀（H5 端通过 FileReader 校验，小程序/APP 兜底拦截）
                  else {
                    // H5 端：读取文件头判断是否为图片（拦截 MP4）
                    if (uni.getSystemInfoSync().platform === 'h5' && tempFile.file) {
                      isImage = await this.checkFileIsImageByReader(tempFile.file);
                    } else {
                      // 小程序/APP 端无后缀+无 MIME，兜底视为非图片（避免 MP4 漏判）
                      isImage = false;
                    }
                  }
            
                  // 最终判断：非图片则提示
                  if (!isImage) {
                    uni.showToast({
                      title: this.language.voucher.imgTypeError || '仅支持上传jpg、png、gif等图片格式！',
                      icon: 'none'
                    });
                    continue; // 跳过当前文件
                  }
            
                  // 是图片，执行上传
                  this.uploadSingleImage(tempPath);
                  }
          // 批量上传选中的图片
          // res.tempFilePaths.forEach((tempPath) => {
          //       const ext = tempPath.split('.').pop().toLowerCase(); 
          //       if (!allowImageExts.includes(ext)) {
          //           // 非图片格式，抛出提示
          //           uni.showToast({
          //             title:  this.language.voucher.imgTypeError,
          //             icon: 'none',
          //             duration: 2000
          //           });
          //           return; // 跳过当前文件上传
          //         }
                  
          //   this.uploadSingleImage(tempPath);
          // });
        },
        fail: () => {
          uni.showToast({ title: this.language.voucher.tpsb, icon: 'none' });
        }
      });
    },
        checkFileIsImageByReader(file) {
          return new Promise((resolve) => {
            const reader = new FileReader();
            // 读取文件前 8 个字节（图片/视频魔数不同）
            reader.readAsArrayBuffer(file.slice(0, 8));
            reader.onload = (e) => {
              const arr = new Uint8Array(e.target.result);
              const magicNumber = arr.reduce((prev, curr) => prev + curr.toString(16).padStart(2, '0'), '').toUpperCase();
              // 图片魔数（MP4 魔数是 0000001866747970 等，不会匹配）
              const isJpg = magicNumber.startsWith('FFD8FF');
              const isPng = magicNumber.startsWith('89504E47');
              const isGif = magicNumber.startsWith('47494638');
              const isWebp = magicNumber.startsWith('52494646');
              resolve(isJpg || isPng || isGif || isWebp);
            };
            reader.onerror = () => resolve(false);
          });
        },
    // 上传单张图片（调用你的接口）
    uploadSingleImage(tempPath) {
      // 显示加载中
      uni.showLoading({ title: '上传中...' });
      
      this.$req.upLoadl(tempPath) // 替换为你的实际上传接口
        .then((res) => {
          // 假设接口返回的图片链接在 res.data.imgUrls[0]
          if (res.data?.imgUrls?.[0]) {
            this.form.voucher.push(res.data.imgUrls[0]);
          } else {
            uni.showToast({ title: this.language.voucher.tpError, icon: 'none' });
          }
        })
        .catch((err) => { 
          uni.showToast({ title:  this.language.voucher.tpcs, icon: 'none' });
        })
        .finally(() => {
          uni.hideLoading();
        });
    },

    // 移除图片
    removeMedia(index) {
      this.form.voucher.splice(index, 1);
    },

    // 提交审核（可补充表单校验）
    submitAudit() {
        if (this.form.voucher.length === 0) {
          return uni.showToast({ title:  this.language.voucher.cxzfpz, icon: 'none' });
        }
        let data = {
            api: 'app.order.upload_credentials',
            sNo: this.form.sNo,
            voucher: this.form.voucher
        };
        this.$req.post({ data }).then(res => {
            if (res.code == 200) {    
                uni.showToast({
                    title:  this.language.voucher.dsh,
                    duration: 1500,
                    icon: 'none'
                });
                setTimeout(() => {
                    uni.navigateBack();
                }, 1500);
            } else {
                uni.showToast({
                    title: res.message,
                    duration: 1500,
                    icon: 'none'
                });                  
            }
            this.fastTap = true;
        });      
    },
    // 导航返回
    goBack() {
        uni.navigateTo({
            url: '/pagesB/order/myOrder?status=' + 1
        });
    },
  }
};
</script>
<style>
page {
    background: #f4f5f6;
}
</style>
<style lang="less" scoped>
.upload-container {
  .edit-tips {
    background: linear-gradient(90deg, #FA5151 0%, #D73B48 100%);
    color: #fff;

    margin-bottom: 15px;
    border-bottom-left-radius: 50rpx;
    border-bottom-right-radius: 50rpx;
       /* 顶部导航栏 */
   .nav-bar {
     display: flex;
     align-items: center;
     justify-content: space-between;
     padding: 20rpx 30rpx;
     flex-shrink: 0;
   }
   
   .nav-title {
     font-size: 32rpx;
     font-weight: 500;
     color: #fff;
   }
    .edit-tips-content{
        padding:62rpx 36rpx;
       .edit-title {
         font-size: 16px;
         font-weight: bold;
         display: block;
         margin-bottom: 5px;
       }
       .edit-desc {
         font-size: 14px;
       }
    }
  }

  .upload-wrap {
    background: #fff;
    border-radius: 16rpx;
    padding: 60rpx 30rpx;
    margin-bottom: 20px;
    .img-container { 
        display: flex;
    }
    .label{
        padding-top: 16rpx;
    }
    .required {
      color: #f56c6c;
    }
    .img-item {
        width: 80px;
        height: 80px;
        position: relative;
        margin-left: 12rpx;
        border-radius: 4px;  
        overflow: hidden; 
      
        .img-preview {
            width: 100%;
            height: 100%;
            display: block;  
        }

      .del-btn {
        position: absolute;
        top: -5px;
        right: -5px;
        width: 20px;
        height: 20px;
        background: #fff;
        border-radius: 50%;
        text-align: center;
        line-height: 20px;
        color: #f53f3f;
        font-size: 16px;
        border: 1px solid #f53f3f;
        z-index: 10;
      }
    }

    .upload-btn {
      width: 80px;
      height: 80px;
      border: 1px dashed #ccc;
      border-radius: 4px;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      font-size: 12px;
      margin-left: 36rpx;
      color: #999;

      .upload-icon {
        width: 20px;
        height: 20px;
        margin-top: 5px;
      }
    }
  }

.submit-offline{
    width: 100%;
    padding: 24rpx 32rpx;
    z-index: 1000;
    display: flex;
    justify-content: center;
    align-items: center;
    .sub-button{
       width: 100%;
       height: 100rpx;
       display: flex;
       justify-content: center;
       align-items: center;
       background: linear-gradient(270deg, #ff6f6f 0, #fa5151 100%);
       border-radius: 50rpx;
       color: #fff;
    }   
} 
}
</style>
