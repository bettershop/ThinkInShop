<template>
  <div class="goods-box">
    <div>
      <div>
        <span class="title">初始化效果</span>
      </div>
      <div class="hied">
        <div style="display: flex;justify-content: space-between;">
          <p>添加图片(建议尺280px * 240px) </p>
          <div class="img-box" @click="modalPicTap('单选', index)" :style="{ height: `80px`, width: `80px` }">
            <img :src="imgVal" alt="" v-if="imgVal" />
            <div class="upload-box" v-else>
              <Icon type="ios-camera-outline" size="22" />
            </div>
            <div class="delect-btn" @click.stop="bindDelete">
              <span class="iconfont-diy icondel_1"></span>
            </div>
          </div>
        </div>
        <p>
          <span>文字描述</span>
          <Input class="my-input" v-model="imgConfig.description"></Input>
        </p>
        <p>
          <span>按钮显示/隐藏</span>
          <ASwitch @on-change="change" v-model="butConfig.isShow"></ASwitch>
        </p>
        <div class="but-box">
          <p><span>标题</span> <Input v-model="butConfig.title" class="but-input"></Input></p>
          <p><span>链接</span> <Input v-model="butConfig.toUrl" class="but-input"></Input></p>
          <p>
            <span>直角/圆角</span>
            <Col class="color-box">
            <RadioGroup v-model="butConfig.type" type="button">
              <Radio :label="key" v-for="(radio,key) in butConfig.list" :key="key">
                <span class="iconfont-diy" :class="radio.icon" v-if="radio.icon"></span>
                <span v-else>{{radio.val}}</span>
              </Radio>
            </RadioGroup>
            </Col>
          </p>
          <p>
            <span>圆角值</span>
            <Col span="18" class="slider-box">
            <Slider show-input :max='18' v-model="butConfig.roundedVal" :disabled="butConfig.type == 1 "></Slider>
            </Col>
          </p>
          <div style="display: flex;justify-content: space-between;">
            <span>背景颜色</span>
            <div>
              <div class="but-list color-list" @click="upBoxColor">
                <p data-type="0" :class="{ 'avt':butConfig.backgType == 0 }">纯色</p>
                <p data-type="1" :class="{ 'avt':butConfig.backgType == 1 }">渐变</p>
              </div>
              <div style="display: flex;margin-top: 10px;">
                <div class="color-item-title" v-if="butConfig.backgType == 1">
                  <div class="color-item-title-text" style="width: 75px;">{{ butConfig.backgColor1 }}</div>
                  <el-color-picker format="hex" size="mini" v-model="butConfig.backgColor1" alpha class="colorPicker-box" style="margin: 0 2px;"></el-color-picker>
                  <div class="reset-btn" style="margin: 0;" @click="resetBgB">重置</div>
                </div>
                <div class="color-item-title">
                  <div class="color-item-title-text" style="width: 75px;">{{ butConfig.backgColor2 }}</div>
                  <el-color-picker format="hex" size="mini" v-model="butConfig.backgColor2" alpha class="colorPicker-box" style="margin: 0 2px;"></el-color-picker>
                  <div class="reset-btn" style="margin: 0;" @click="resetBgC">重置</div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- <div>
          <span>直角/圆角</span>
          <Col class="color-box">
          <RadioGroup v-model="pageConfig.boxType" type="button">
            <Radio :label="key" v-for="(radio,key) in butConfig.list" :key="key">
              <span class="iconfont-diy" :class="radio.icon" v-if="radio.icon"></span>
              <span v-else>{{radio.val}}</span>
            </Radio>
          </RadioGroup>
          </Col>
        </div> -->
        <!-- <p>
          <span>圆角值</span>
          <Col span="18" class="slider-box">
          <Slider show-input :max='15' v-model="pageConfig.boxRoundedVal" :disabled="pageConfig.boxType == 1 "></Slider>
          </Col>
        </p> -->
        <!-- <div class="flex-buju">
          <span>背景颜色</span>
          <div class="color-item-title">
            <div class="color-item-title-text" style="width: 75px;">{{ pageConfig.boxBackColor }}</div>
            <el-color-picker format="hex" size="mini" v-model="pageConfig.boxBackColor" alpha class="colorPicker-box"></el-color-picker>
            <div class="reset-btn" @click="resetBgA">重置</div>
          </div>
        </div> -->
      </div>
    </div>
    <!-- -->
    <div style="margin-top: 10px;">
      <span class="title">推荐商品</span>
    </div>
    <div class="hied">
      <p>
        <span>商品显示/隐藏</span>
        <ASwitch @on-change="change" v-model="pageConfig.goodsConfig.goodsShow"></ASwitch>
      </p>
      <div class="flex-buju">
        <span>商品布局</span>
        <div class="but-list" @click="upType">
          <p :class="{'avt':layoutType == 1}" data-type="1">一排两个</p>
          <p :class="{'avt':layoutType == 2}" data-type="2">一排一个</p>
          <!-- <p :class="{'avt':layoutType == 3}" data-type="3">纵向显示</p> -->
        </div>
      </div>
    </div>

    <div>
      <Modal v-model="modalPic" :width="modalWidth" scrollable footer-hide closable title="上传商品图" :mask-closable="false" :z-index="1">
        <uploadPictures :isChoice="isChoice" @getPic="getPic" :gridBtn="gridBtn" :gridPic="gridPic" v-if="modalPic"></uploadPictures>
      </Modal>
    </div>
  </div>
</template>

  <script>
import uploadPictures from "@/components/uploadPictures";
import vuedraggable from 'vuedraggable'
import { Switch, Input } from 'iview'
export default {
  name: 'c_cart_Initialization',
  props: {
    configObj: {
      type: Object
    }
  },
  components: {
    uploadPictures,
    'ASwitch': Switch,
    'Input': Input,
    draggable: vuedraggable,
  },
  computed: {
    modalWidth() {
      if (window.outerWidth <= 1440) {
        console.log(1);
        return "70%";
      }
      console.log(1);
      return "60%";
    }
  },
  watch: {
    configObj: {
      handler(nVal, oVal) {
        if (nVal && Object.keys(nVal).length) {
          this.configData = this.defaults = nVal
          this.pageConfig = this.defaults.pageConfig
          this.butConfig = this.defaults.pageConfig.butConfig
          this.imgConfig = this.defaults.pageConfig.imgConfig
        }
      },
      immediate: true,
      deep: true
    },
    'butConfig.type': {
      handler(nVal, oVal) {
        if (nVal == 1) {
          this.butConfig.roundedVal = 0
        } else {
          this.butConfig.roundedVal = 15
        }

      }
    },
    'pageConfig.boxType': {
      handler(nVal, oVal) {
        console.log(nVal)
        if (nVal == 1) {
          this.pageConfig.boxRoundedVal = 0
        } else {
          this.pageConfig.boxRoundedVal = 15
        }

      }
    }
  },
  data() {
    return {
      dialogVisible: false,
      defaults: {},
      configData: {},
      imgConfig: {},
      butConfig: {},
      pageConfig: {},
      layoutType: 1,

      imgVal: '', // 图片
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
  },
  methods: {
    change(e) {
      console.log(e)
      this.dialogVisible = !this.dialogVisible
    },

    resetBgA() {
      this.pageConfig.boxBackColor = '#fff'
    },
    resetBgB() {
      this.butConfig.backgColor1 = '#fff'
    },
    resetBgC() {
      this.butConfig.backgColor2 = '#fff'
    },
    upType(event) {
      const type = Number(event.target.dataset.type);

      // 验证类型有效性（防止点击到父元素其他区域）
      if ([1, 2, 3].includes(type)) {
        this.layoutType = type;
        console.log("当前布局类型：", this.layoutType);
        this.pageConfig.goodsConfig.layoutType = type
      }
    },
    upBoxColor(event) {
      const type = Number(event.target.dataset.type);
      // 验证类型有效性（防止点击到父元素其他区域）
      if ([0, 1].includes(type)) {
        this.butConfig.backgType = type
        this.resetBgB()
        this.resetBgC()
      }
    },
    modalPicTap() {
      this.modalPic = true;
    },
    // 获取图片信息
    getPic(pc) {
      this.$nextTick(() => {
        console.log('pcpcpc', pc.att_dir)
        this.imgConfig.imgVal = pc.att_dir
        this.imgVal = pc.att_dir
        this.modalPic = false;
      });
    },
    bindDelete() {
      this.imgConfig.imgVal = ''
      this.imgVal = ''
    }
  }
}
  </script>

  <style scoped lang="less">
.upload-box {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
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

.flex-buju {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.title {
  font-family: MicrosoftYaHei;
  font-size: 14px;
  font-weight: normal;
  line-height: 14px;
  letter-spacing: 0em;

  color: #414658;
}
.iconfont-diy {
  font-size: 20px;
  line-height: 18px;
}
.hied {
  p {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin: 10px 0;
  }
  span {
    user-select: none;
    font-family: MicrosoftYaHei;
    font-size: 14px;
    font-weight: normal;
    line-height: 14px;
    letter-spacing: 0em;

    color: #7d869c;
  }
  .my-input {
    width: 176px;
    height: 32px;
    border-radius: 4px;
    opacity: 1;
  }
}
.but-box {
  background-color: #f4f8f9;
  padding: 15px;
  .but-input {
    width: 166px;
  }
}
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
.reset-btn {
  width: 30px;
  margin-left: 5px;
  color: #999;
  font-size: 13px;
  cursor: pointer;
}
.but-list {
  display: flex;
  align-items: center;
  justify-content: end;
  user-select: none;
  p {
    width: 80px;
    display: block;
    height: 32px;
    line-height: 32px;
    text-align: center;
    border: 1px solid #d5dbe8;
    border-radius: 4px;
    cursor: pointer;
    margin-left: 5px;
  }
}
.color-list {
  p {
    width: 50px;
    height: 24px;
    line-height: 24px;
    font-size: 12px;
    margin: 0;
    margin-left: 5px;
  }
}
.img-box {
  position: relative;
  width: 80px;
  height: 80px;

  img {
    width: 100%;
    height: 100%;
  }
}
</style>
