<template>
  <div class="c_classification_info">
    <!-- <div class="title">默认样式</div>
    <div class="c_row-item">
      <Col span="8" class="c_label">样式布局</Col>
      <Col span="14" class="c_label">
      <div class="c_row-right">
        <div :class="{ active: configData.styleType == 2 }" @click="setStyleType(2)">样式一</div>
        <div :class="{ active: configData.styleType == 1 }" style="margin: 0 4px;" @click="setStyleType(1)">样式二</div>
      </div>
      </Col>
    </div> -->
    <div class="title">搜索栏</div>
    <div class="input-info">
      <div class="input-info-item">
        <div>显示/隐藏</div>
        <ASwitch @on-change="change" v-model="configData.isShow"></ASwitch>
      </div>
      <div class="input-info-item" style="margin: 10px 0;">
        <div>提示文字</div>
        <Input class="input" @on-blur="blurPlaceholder" v-model="configData.placeholder" :placeholder="configData.placeholder" type="text" />
      </div>
      <div class="input-info-item">
        <div>商品样式按钮</div>
        <ASwitch @on-change="styleChange" v-model="configData.isStyleShow"></ASwitch>
      </div>
    </div>
    <div class="title" v-if="configData.styleType==2">选中</div>
    <div>
      <div class="c_row-item" v-if="configData.styleType==2">
        <Col span="8" class="c_label">背景颜色</Col>
        <Col span="14" class="color-box">
        <div class="color-item" v-for="(color,key) in configData.bgColor" :key="key">
          <div class="color-item-title">
            <div class="color-item-title-text">{{ color.item }}</div>
            <el-color-picker v-model="color.item" format="hex" @on-change="changeColor($event,color)" alpha class="colorPicker-box"></el-color-picker>
            <div class="reset-btn" @click="resetBgA(color,index,key)">重置</div>
          </div>
        </div>
        </Col>
      </div>
      <div class="c_row-item" v-if="configData.styleType==2" style="margin-top: 10px;">
        <Col span="8" class="c_label">文字颜色</Col>
        <Col span="14" class="color-box">
        <div class="color-item" v-for="(color,key) in configData.color" :key="key">
          <div class="color-item-title">
            <div class="color-item-title-text">{{ color.item }}</div>
            <el-color-picker v-model="color.item" format="hex" @on-change="changeColor($event,color)" alpha class="colorPicker-box"></el-color-picker>
            <div class="reset-btn" @click="resetBgA(color,index,key)">重置</div>
          </div>
        </div>
        </Col>
      </div>
    </div>
    <!-- <template>
      <div class="title">设置</div>
      <div class="c_row-item">
        <Col span="8" class="c_label">背景颜色</Col>
        <Col span="14" class="c_label">
        <div class="c_row-right">
          <div :class="{ active: configData.type == 'pureColor' }" @click="setConfig('pureColor')">纯色</div>
          <div :class="{ active: configData.type == 'graduatedColor' }" style="margin: 0 4px;" @click="setConfig('graduatedColor')">渐变</div>
          <div :class="{ active: configData.type == 'img' }" @click="setConfig('img')">图片</div>
        </div>
        </Col>
      </div>
      <div class="c_row_tab_color">
        <div class="bg_color-box" v-if="configData && configData.type == 'pureColor'">
          <div class="color-item" v-for="(color,key) in configData.pureColor" :key="key">
            <div class="color-item-title">
              <div class="color-item-title-text">{{ color.item }}</div>
              <el-color-picker v-model="color.item" format="hex" @on-change="changePureColor($event,color)" alpha class="colorPicker-box"></el-color-picker>
              <div class="reset-btn" @click="resetBgA(color,index,key)">重置</div>
            </div>
          </div>
        </div>
        <div class="bg_color-box" v-if="configData && configData.type == 'graduatedColor'">
          <div class="color-item" v-for="(color,key) in configData.graduatedColor" :key="key">
            <div class="color-item-title">
              <div class="color-item-title-text">{{ color.item }}</div>
              <el-color-picker v-model="color.item" format="hex" @on-change="changeGraduatedColor($event,color)" alpha class="colorPicker-box"></el-color-picker>
              <div class="reset-btn" @click="color.item = '#dfc'">重置</div>
            </div>
          </div>
        </div>
        <Slider class="color-Slider" v-if="configData.type == 'graduatedColor'" v-model="configData.bgNum" show-input :max='360'></Slider>
      </div>
      <div class="c_row-item" v-if="configData.type == 'img'" style="margin-top: 24px;color: #999999;">
        <div span="14">添加图片 <span style="font-size: 10px;">(建议尺寸375px*812px)</span> </div>
        <div span="8" class="img-box" :style="{ height: `80px`, width: `80px` }" @click="modalPicTap('单选')">
          <img :src="bgImg" alt="" v-if="bgImg" />
          <div class="upload-box" v-else>
            <Icon type="ios-camera-outline" size="22" />
          </div>
          <div v-if="bgImg" class="delect-btn" @click.stop="bindDelete">
            <span class="iconfont-diy icondel_1"></span>
          </div>
        </div>
      </div>
    </template> -->

    <Modal v-model="modalPic" :width="modalWidth" scrollable footer-hide closable title="上传商品图" :mask-closable="false" :z-index="1">
      <uploadPictures :isChoice="isChoice" @getPic="getPic" :gridBtn="gridBtn" :gridPic="gridPic" v-if="modalPic"></uploadPictures>
    </Modal>
  </div>
</template>

  <script>
import uploadPictures from "@/components/uploadPictures";
import { Switch, Input } from 'iview'
export default {
  name: 'c_classification_info',
  props: {
    configObj: {
      type: Object
    },
    configNme: {
      type: String
    }
  },
  components: {
    uploadPictures,
    'ASwitch': Switch,
    'Input': Input,
  },
  computed: {
    modalWidth() {
      if (window.outerWidth <= 1440) {
        return "70%";
      }
      return "60%";
    }
  },
  watch: {
    configObj: {
      handler(nVal, oVal) {
        console.log(nVal, 'nVal---nVal')
        this.defaults = nVal
        if (Object.keys(this.defaults).length == 0) return
        this.configData = nVal[this.configNme]
        if (this.configObj[this.configNme]) {
          this.configData = nVal[this.configNme]
        } else {
          this.configData = nVal[nVal.link_key][this.configNme]
        }
      },
      immediate: true,
      deep: true
    }
  },
  data() {
    return {
      imgConfig: {},
      bgImg: "", // 图片
      configData: {
        type: 'pureColor', // 纯色 渐变 图片
        styleType: 2, // 样式布局
        isShow: true,
        isStyleShow: true,
        placeholder: '请输入内容',
        bgColor: [
          {
            item: '#ffffff'
          }
        ],
        color: [
          {
            item: '#FA5151'
          }
        ],
        pureColor: [
          {
            item: '#ffffff'
          }
        ],
        graduatedColor: [
          {
            item: '#ffffff'
          },
          {
            item: '#ffffff'
          }
        ],
        bgImg: '',
        bgNum: 0
      },
      isChoice: "单选",
      modalPic: false,
      gridBtn: {
        xl: 4,
        lg: 8,
        md: 8,
        sm: 8,
        xs: 8
      },
      gridPic: {
        xl: 6,
        lg: 8,
        md: 12,
        sm: 12,
        xs: 12
      },
    }
  },
  created() {
    this.defaults = this.configObj
    try {
      if (Object.keys(this.defaults).length == 0) return
      if (this.defaults[this.configNme]) {
        this.configData = this.defaults[this.configNme]
      } else {
        this.configData = this.defaults[this.defaults.link_key][this.configNme]
      }
    } catch (error) {
      console.error('c_bg_color::', error)
    }
  },

  methods: {
    setConfig(data) {
      this.configData.type = data
      console.log(this.configObj, 'data---data')
      this.configObj.c_classification_info.type = data

    },
    modalPicTap() {
      this.modalPic = true;
    },
    bindDelete() {
      this.imgConfig.bgImg = ''
      this.bgImg = ''
    },
    setStyleType(type) {
      this.configData.styleType = type
      console.log(this.configObj, 'data---data')

      this.configObj.c_classification_info.styleType = type
    },
    blurPlaceholder() {
      this.configObj.c_classification_info.styleType = this.configData.placeholder
    },
    // 获取图片信息
    getPic(pc) {
      this.$nextTick(() => {
        this.imgConfig.bgImg = pc.att_dir
        this.bgImg = pc.att_dir
        this.configObj.c_classification_info.bgImg = pc.att_dir
        this.modalPic = false;
      });
    },
    change(e) { },
    styleChange(e) { },
    changePureColor(e, color) {
      console.log(color, 'color---color')
      this.configObj.c_classification_info.pureColor = color
    },
    changeGraduatedColor(e, color) {
      this.configObj.c_classification_info.graduatedColor = color
    },
  },
}
  </script>

<style scoped lang="less">
.c_classification_info {
  .title {
    font-size: 14px;
    color: #414658;
    margin: 10px 0;
  }
  .input-info {
    border-radius: 4px;
    background: #f4f7f9;
    padding: 10px;
    .input-info-item {
      display: flex;
      align-items: center;
      justify-content: space-between;
      .input {
        width: 256px;
        height: 32px;
        background: #fff;
        border: 1px solid #d5dbe8 !important;
        border-radius: 4px;
      }
    }
  }
  .img-box {
    position: relative;
    display: flex;
    justify-content: flex-end;
    cursor: pointer;
    border: 1px solid #d5dbe8;
    img {
      width: 100%;
      height: 100%;
    }
    .upload-box {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 80px;
      height: 80px;
      border-radius: 4px;
      background: #f2f2f2;
    }
    .avt {
      border: 1px solid #2d8cf0;
      color: #2d8cf0;
    }
    .delect-btn {
      position: absolute;
      right: -5px;
      top: -5px;
      .iconfont-diy {
        font-size: 22px;
        color: #999;
      }
    }
  }

  .color-item {
    margin-left: 15px;
    .color-item-title {
      display: flex;
      align-items: center;
      .colorPicker-box {
        margin: 0 6px;
      }
      .color-item-title-text {
        width: 98px;
        height: 32px;
        display: flex;
        align-items: center;
        justify-content: center;
        padding: 8px 10px;
        background: #fcfcfc;
        border-radius: 4px 4px 4px 4px;
        border: 1px solid #d5dbe8;
      }
    }
  }
  .reset-btn {
    width: 30px;
    margin-left: 5px;
    color: #999;
    font-size: 13px;
    cursor: pointer;
  }
  .c_row-right {
    .active {
      color: #409eff;
      border: 1px solid #409eff;
    }
    display: flex;
    align-items: center;
    justify-content: end;
    div {
      text-align: center;
      cursor: pointer;
      border: 1px solid #dcdfe6;
      border-radius: 4px;
      padding: 2px 6px;
      color: #606266;
      &:hover {
        color: #409eff;
      }
    }
  }
  .bg_color-box {
    display: flex;
    align-items: center;
    justify-content: flex-end;
    margin: 24px 0;
    .color-item {
      .color-item-title {
        display: flex;
        align-items: center;

        .colorPicker-box {
          margin: 0 6px;
        }
        .color-item-title-text {
          width: 68px !important;
          height: 32px;
          display: flex;
          align-items: center;
          justify-content: center;
          padding: 8px 10px;
          background: #fcfcfc;
          border-radius: 4px 4px 4px 4px;
          border: 1px solid #d5dbe8;
        }
      }
      .reset-btn {
        width: 30px;
        margin-left: 5px;
        color: #999;
        font-size: 13px;
        cursor: pointer;
      }
    }
  }
}
/deep/.el-color-picker {
  width: 28px !important;
  height: 28px !important;
  .el-color-picker__trigger {
    width: 28px !important;
    height: 28px !important;
  }
}
</style>
