<template>
    <div class="goods-box" >
            <div>
                <div>
                    <span class="title">内容</span> 
                </div>
                <div style="display: flex;justify-content: space-between;align-items: center;">
                        <p>添加图片(建议尺280px * 240px) </p> 
                        <div
                            class="img-box"
                            @click="modalPicTap('单选', index)"
                            :style="{ height: `80px`, width: `80px` }"
                        >
                            <img :src="supendedConfig.imgVal" alt="" v-if="supendedConfig.imgVal" />
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
                <div>
                    <span class="title">设置</span> 
                </div>
                <div class="hied"> 
                    <p>
                        <span>图标大小</span> 
                        <Col span="18" class="slider-box">
                            <Slider show-input :max='55' :min="40" v-model="supendedConfig.imgsiez" ></Slider>
                        </Col>
                    </p>
                    <div style="display: flex;justify-content: space-between;">
                         <span >图片背景</span>
                        <div > 
                            <div class="but-list color-list" @click="upBoxColor">
                                <p data-type="0" :class="{ 'avt':supendedConfig.backgType == 0 }" >纯色</p> 
                                <p data-type="1" :class="{ 'avt':supendedConfig.backgType == 1 }" >渐变</p>
                            </div>
                            <div style="display: flex;margin-top: 10px;">
                                <div class="color-item-title" v-if="supendedConfig.backgType == 1"> 
                                    <div class="color-item-title-text" style="width: 75px;" >{{ supendedConfig.backgColor1 }}</div>
                                    <el-color-picker format="hex" size="mini"  v-model="supendedConfig.backgColor1" alpha class="colorPicker-box" style="margin: 0 2px;"></el-color-picker> 
                                    <div class="reset-btn" style="margin: 0;" @click="resetBgB">重置</div> 
                                </div>
                                <div class="color-item-title"> 
                                    <div class="color-item-title-text" style="width: 75px;">{{ supendedConfig.backgColor2 }}</div>
                                    <el-color-picker format="hex" size="mini" v-model="supendedConfig.backgColor2" alpha class="colorPicker-box" style="margin: 0 2px;"></el-color-picker> 
                                    <div class="reset-btn" style="margin: 0;" @click="resetBgC">重置</div> 
                                </div>
                            </div>
                        </div>
                    </div > 
                    <p>
                        <span>直角/圆角</span>  
                        <Col class="color-box"> 
                            <RadioGroup v-model="supendedConfig.type" type="button" >
                            <Radio :label="key" v-for="(radio,key) in supendedConfig.list" :key="key">
                                <span class="iconfont-diy" :class="radio.icon" v-if="radio.icon"></span>
                                <span v-else>{{radio.val}}</span>
                            </Radio>
                            </RadioGroup>
                        </Col>
                    </p>
                    <p>
                        <span>圆角值</span> 
                        <Col span="18" class="slider-box">
                            <Slider show-input :max='18' v-model="supendedConfig.roundedVal" :disabled="supendedConfig.type == 1 " ></Slider>
                        </Col>
                    </p>
                    <p>
                        <span>开启商品计计数</span>  
                        <ASwitch v-model="supendedConfig.sunShow"></ASwitch>
                        
                    </p>
                </div>
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
  </template>
  
  <script>
  import uploadPictures from "@/components/uploadPictures";

      import vuedraggable from 'vuedraggable'
      import goodsList from '@/components/goodsList'
        import { Switch,Input } from 'iview'
      export default {
          name: 'c_cart_suspended',
          props: {
              configObj: {
                  type: Object
              }
          }, 
          components: {
                uploadPictures,
              goodsList,
              'ASwitch':Switch,
              'Input':Input,
              draggable: vuedraggable,
          },
          watch: {
              configObj: {
                  handler (nVal, oVal) {
                    this.defConfig = nVal
                      this.supendedConfig = nVal.pageConfig.supendedConfig 
                    console.log(this.supendedConfig)
                  },
                  immediate: true,
                  deep: true
              }
          },
          data () {
              return {
                  modals: false,  
                  supendedConfig: {},
                  supendedConfig: {},

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
          created () {
            //   this.supendedConfig = this.configObj.pageConfig.supendedConfig
          },
          computed: { 
                modalWidth() {
                    console.log("window.outerWidth", window.outerWidth);
                    if (window.outerWidth <= 1440) {
                        console.log(1);
                        return "70%";
                    }
                    console.log(1);
                    return "60%";
                }
            },
          methods: {
            upBoxColor(event){
                const type = event.target.dataset.type
                if([0,1].includes(Number(type))){
                    this.supendedConfig.backgType = Number(type)
                } 
            },
            resetBgB(){
                this.supendedConfig.backgColor1 = '#ffffff'
            },
            resetBgC(){
                this.supendedConfig.backgColor2 = '#ffffff'
            },
            modalPicTap( ) { 
                this.modalPic = true;
            },
            getPic(pc) {
                this.$nextTick(() => {
                    // this.configData.list[this.activeIndex].img = pc.att_dir;
                    console.log('pcpcpc',pc.att_dir)
                    this.supendedConfig.imgVal = pc.att_dir
                    this.imgVal = pc.att_dir

                    this.modalPic = false;
                });
            },
            bindDelete(){
                this.supendedConfig.imgVal = ''
                this.imgVal = ''
            }
          }
      }
  </script>
  
  <style scoped lang="less">
    .avt {
        border:1px solid #2d8cf0 !important;
        color: #2d8cf0 !important;
    }
     .title{
        font-family: MicrosoftYaHei;
        font-size: 14px;
        font-weight: normal;
        line-height: 14px;
        letter-spacing: 0em;

        color: #414658;
     }
     .hied{
        p{
            display: flex;
            align-items: center;
            justify-content: space-between;
            margin: 10px 0;
        }
        span{
            user-select: none;
            font-family: MicrosoftYaHei;
            font-size: 14px;
            font-weight: normal;
            line-height: 14px;
            letter-spacing: 0em;
            
            color: #7D869C;
        }
        .my-input{
            width: 176px;
            height: 32px;
            border-radius: 4px;
            opacity: 1;
        }
     }
     .color-item-title{
        display: flex;
        align-items: center;
        .colorPicker-box{
          margin: 0 6px;
        }
        .color-item-title-text{ 
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
      .but-list{
        display: flex;
        align-items: center; 
        justify-content: end;
        user-select: none;
        p{ 
            width: 80px;
            display: block;
            height: 32px;
            line-height: 32px;
            text-align: center;
            border: 1px solid #D5DBE8;
            border-radius: 4px;
            cursor: pointer;
            margin-left: 5px;
        } 
    }
    .color-list{
        p{
            width: 50px;
            height: 24px;
            line-height: 24px;
            font-size: 12px;
            margin: 0;
            margin-left: 5px;
        }
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
    .iconfont-diy{
        font-size: 20px;
        line-height: 18px
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
  