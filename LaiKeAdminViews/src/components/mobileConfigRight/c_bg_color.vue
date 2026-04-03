<template>
  <div> 
    <div class="c_row-item" v-if="configData && configData.type != 'tabColor' && !configData.colorOrimg">
      <Col span="8" class="c_label">{{configData.title}} </Col>
      <Col span="14" class="color-box">

      <div class="color-item" v-for="(color,key) in configData.color" :key="key">
        <div class="color-item-title">
          <div class="color-item-title-text" v-if="defaults.name == 'swiperBg'">{{ color.item }}</div>
          <el-colorPicker v-model="color.item" format="hex" @on-change="changeColor($event,color)" alpha class="colorPicker-box" size="mini"></el-colorPicker>
          <div class="reset-btn" @click="resetBgA(color,index,key)">重置</div>
        </div>

      </div>
      </Col>
    </div>
    <div class="c_row_tab_color" v-if="configData && configData.type == 'tabColor' && !configData.colorOrimg">
      <div class="c_row_title">
        <div class="c_row-left">{{configData.title}}</div>
        <div class="c_row-right">
          <div class="c_row-right-but" :class="{ active: configData.tabType == 1 }" @click="setTabType(1)">纯色</div>
          <div class="c_row-right-but" :class="{ active: configData.tabType == 2 }" style="margin: 0 4px;" @click="setTabType(2)">渐变</div>
        </div>
      </div>

      <div class="color-box">
        <div class="color-item" v-for="(color,key) in configData.color" :key="key">
          <div class="color-item-title">
            <div class="color-item-title-text" v-if="defaults.name == 'swiperBg'">{{ color.item }}</div>
            <ColorPicker v-model="color.item" format="hex" @on-change="changeColor($event,color)" alpha class="colorPicker-box"></ColorPicker>
            <div class="reset-btn" @click="resetBgA(color,index,key)">重置</div>
          </div>

        </div>
      </div>
      <Slider class="color-Slider" v-if="configData.tabType == 2" v-model="configData.val" show-input @on-change="sliderChange($event)" :max='360'></Slider>
    </div>

    <div  v-if="configData && configData.colorOrimg ">
       <div class="c_row-item" >
          <Col span="8" class="c_label">{{configData.title}} </Col>
          <Col span="14" class="color-box" style="flex-direction: column;align-items: end;">

            <div style="display:flex;margin-bottom: 10px;">
              <div class="but" :class="configData.type==1? 'but-active':''" @click="configData.type = 1" >颜色</div>
              <div class="but" :class="configData.type==2? 'but-active':''" @click="configData.type = 2">图片</div>
            </div>
            <div style="display:flex" v-show="configData.type == 1">
              <div class="color-item" v-for="(color,key) in configData.color" :key="key">
                <div class="color-item-title">
                  <div class="color-item-title-text" v-if="defaults.name == 'swiperBg'">{{ color.item }}</div>
                  <el-colorPicker v-model="color.item" format="hex" @on-change="changeColor($event,color)" alpha class="colorPicker-box" size="mini"></el-colorPicker>
                  <div class="reset-btn" @click="resetBgA(color,index,key)">重置</div>
                </div>
              </div> 
            </div>
             <div style="display:flex" v-show="configData.type == 2">
              <div class="color-item"  >
                <div class="color-item-title">
                    <div
                          class="img-box"
                          @click="modalPicTap('单选', index)"
                          :style="{ height: `80px`, width: `80px` }"
                        >
                            <img :src="configData.imgUrl" alt="" v-if="configData.imgUrl" />
                            <div class="upload-box" v-else>
                                <Icon type="ios-camera-outline" size="22" />
                            </div>
                            <div
                            class="delect-btn"
                            @click.stop="bindDelete" 
                            >
                            <span class="iconfont-diy icondel_1"></span>
                            </div>
                        </div>
                </div>
              </div> 
            </div>
          </Col>
        </div>  

        <Modal
            v-model="modalPic"
            :width="modalWidth"
            scrollable
            footer-hide
            closable
            title="上传商品图"
            :mask-closable="false"
            :z-index="1"
            >
            <uploadPictures
                :isChoice="isChoice"
                @getPic="getPic"
                :gridBtn="gridBtn"
                :gridPic="gridPic"
                v-if="modalPic"
            ></uploadPictures>
            </Modal>
    </div>
  </div>

</template>

<script>
    import uploadPictures from "@/components/uploadPictures";
export default {
  name: 'c_bg_color',
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
          },
  data() {
    return { 
      defaults: {},
      configData: {},
      bgColor: {
        bgStar: '',
        bgEnd: ''
      },
      oldColor: {
        bgStar: '',
        bgEnd: ''
      },
      index: 0,
      isType: false,
      tabType: 1, // 1 纯色 2 渐变


      imgVal:'', // 图片
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
    // this.configData = this.configObj[this.configNme]

    try {
      if (Object.keys(this.defaults).length == 0) return

      if (this.defaults[this.configNme]) {
        this.configData = this.defaults[this.configNme]
      } else {
        this.configData = this.defaults[this.defaults.link_key][this.configNme]
      }
      if (this.configData.color && this.configData.color.length > 1) {
        this.isType = true
      } else {
        this.isType = false
      }
      console.log('isType', this.isType)

    } catch (error) {
      console.error('c_bg_color::', error)
    }

  },
  watch: {
    configObj: {
      handler(nVal, oVal) {
        this.defaults = nVal

        if (Object.keys(this.defaults).length == 0) return

        this.configData = nVal[this.configNme]
        if (this.configObj[this.configNme]) {
          this.configData = nVal[this.configNme]
        } else {
          this.configData = nVal[nVal.link_key][this.configNme]
        }
        if (this.configData.color && this.configData.color.length > 1) {
          this.isType = true
        } else {
          this.isType = false
        }
        console.log('isType', this.isType)
      },
      immediate: true,
      deep: true
    }
  },
  methods: {
    changeColor(e, color) {
      if (!e) {
        color.item = 'transparent'
      }
      // this.$emit('getConfig', this.defaults)
    },
    // 重置
    resetBgA(color, index, key) {
      // console.log(color)
      color.item = this.configData.default[key].item
    },
    setTabType(type) {
      this.configData.tabType = type
      if (type == 1) {
        this.configData.color = [this.configData.color[0]]
      } else if (type == 2) {
        if (this.configData.color.length < 2) {
          this.configData.color.push({ item: '#ffffff' })
        }
      }
    },
      modalPicTap( ) { 
          this.modalPic = true;
      },
      getPic(pc) {
          this.$nextTick(() => {
              // this.configData.list[this.activeIndex].img = pc.att_dir;
              console.log('pcpcpc',pc.att_dir)
              this.configData.imgUrl = pc.att_dir
              this.imgVal = pc.att_dir

              this.modalPic = false;
          });
      },
      bindDelete(){
        console.log('删除图片',this.configData)
          this.configData.imgUrl = ''
          this.imgVal = ''
      }
  }
}
</script>
<style lang="less" scoped>
.but{
  min-width: 55px;
  height: 25px;
  line-height: 23px;
  font-size: 13px;
  text-align: center;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  margin-right: 10px;
  cursor: pointer;
}
.but-active{
  background-color: #409eff;
  color: #fff;
}
.img-box{

        position: relative;
        width: 80px;
        height: 80px;

        img{
            width: 100%;
            height: 100%;
        }
    }
        .upload-box{
        display: flex;
        align-items: center;
        justify-content: center;
        width: 100%;
        height: 100%;
        border-radius: 4px;
        background: #f2f2f2;
    }
      .delect-btn{
        position: absolute;
        right: -5px;
        top: -5px;

        .iconfont-diy{
          font-size: 22px;
          color: #999;
          cursor: pointer;
        }
    }
</style>

<style scoped lang="stylus">
.c_row_tab_color {
  margin-bottom: 12px;

  .c_row_title {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 12px;
  }

  .c_row-left {
    color: #999;
  }

  .c_row-right {
    display: flex;
    align-items: center;

    .c_row-right-but {
      text-align: center;
      cursor: pointer;
      border: 1px solid #dcdfe6;
      border-radius: 4px;
      padding: 2px 4px;
      color: #606266;

      &:hover {
        color: #409eff;
      }
    }
  }

  .color-item-title-text {
    width: 70px !important;
    font-size: 12px;
  }
}

.c_row-item {
  margin-top: 10px;
  margin-bottom: 10px;
}

.color-Slider {
  margin: 24px 0;
}

.color-box {
  display: flex;
  align-items: center;
  justify-content: flex-end;

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
        background: #FCFCFC;
        border-radius: 4px 4px 4px 4px;
        border: 1px solid #D5DBE8;
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
</style>
