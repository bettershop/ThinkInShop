<template>
  <div class="slider-box">
    <!-- 样式一 -->
    <div class="c_row-item" v-if="configData && configData.type == 'padding'">
      <Col class="label" span="4" v-if="configData.title">
        {{configData.title}}
      </Col>
      <Col span="2" v-if="configData.title">
        <div class="slider-icon-box">
          <img v-if="direction == 'top'"  class="slider-active-top" src="../../assets/images/diy/right.png" alt=""></img>
          <img v-else @click="setDirection('top')" class="slider-top" src="../../assets/images/diy/top.png" alt="">

          <img v-if="direction == 'bottom'" class="slider-active-bottom" src="../../assets/images/diy/right.png" alt=""></img>
          <img v-else @click="setDirection('bottom')" class="slider-bottom" src="../../assets/images/diy/top.png" alt="">

          <img v-if="direction == 'left'" class="slider-active-left" src="../../assets/images/diy/right.png" alt=""></img>
          <img v-else @click="setDirection('left')" class="slider-left" src="../../assets/images/diy/top.png" alt="">

          <img v-if="direction == 'right'" class="slider-active-right" src="../../assets/images/diy/right.png" alt=""></img>
          <img v-else @click="setDirection('right')" class="slider-right" src="../../assets/images/diy/top.png" alt="">
        </div>
      </Col>
      <Col span="16">
        <Slider class="padding-slider" v-model="configData.val" show-input @input="onSliderChange" :max='20' :min="configData.min"></Slider>
      </Col>
    </div>

    <!-- 样式二 -->
    <div class="c_row-item" v-else>
      <Col class="label" span="4" v-if="configData.title">
        {{configData.title}}
      </Col>
      <Col span="18" class="slider-box">
        <Slider v-model="configData.val" show-input :max='configData.max || 120' :min="configData.min"></Slider>
      </Col>
    </div>
  </div>

</template>

<script>
    export default {
        name: 'c_slider',
        props: {
            configObj: {
                type: Object
            },
            configNme: {
                type: String
            }
        },
        data () {
            return {
                defaults: {},
                sliderWidth: 0,
                configData: {},
                direction: 'right', // 默认方向
                sliderVal: 10,
                TOTAL: 20, // 总值
            }
        },
        mounted () {
            this.$nextTick(() => {
                this.defaults = this.configObj
                if(Object.keys(this.defaults).length === 0 || !this.configObj) {
                  return
                }
                try {
                    if( this.configObj[this.configNme]){
                      this.configData = this.configObj[this.configNme]
                    }else{
                      this.configData = this.configObj[this.configObj.link_key][this.configNme]
                    }
                } catch (error) {
                  console.error('defaults1111',this.defaults)
                  console.error('c_slider mounted error::',error)
                }

            })
        },
        watch: {
            configObj: {
                handler (nVal, oVal) {
                    this.defaults = nVal
                    // this.configData = nVal[this.configNme]
                    if( this.configObj[this.configNme]){
                      this.configData = nVal[this.configNme]
                    }else{
                      this.configData = nVal[nVal.link_key][this.configNme]
                    }

                },
                deep: true
            }
        },
        methods: {

            setDirection(e){
                this.direction = e
                let name = e == 'left'?'左':e == 'right'?'右':e == 'top'?'上':'下'
                this.$el.style.setProperty('--prefix-text', `'${name}'`);
            },
            onSliderChange(newVal) {
              const dir = this.direction;          // 当前正在调节的方向
              // 配对映射：top<->bottom，left<->right
              const pair = { top: 'bottom', bottom: 'top', left: 'right', right: 'left' };
              const reverse = pair[dir];

              // 当前方向
              this.configObj.paddingConfig.padding[dir] = newVal;
              // 反方向自动补足
              this.configObj.paddingConfig.padding[reverse] = Math.max(0, this.TOTAL - newVal);
            }

        }
    }
</script>

<style scoped lang="stylus">
  .c_row-item
    margin-bottom 20px

    .label
      color #999
</style>
<style lang="less" scoped>
.padding-slider{
  width: auto !important;
  background:none !important;
  box-shadow:none !important;
  /deep/.ivu-input-number-input-wrap{
      display: flex;
      &::before {
          content: var(--prefix-text, '右');
          color: #85859e;
          width: 38px;
          display: inline-block;
          background: #f3f5f7;
          display: flex;
          align-items: center;
          justify-content: center;
          border-right: #d5dbe8 1px solid;
      }
  }
}
  .slider-icon-box{
    position: relative;
    width: 100%;
    height: 30px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    border: 1px solid #ddd;
    border-radius: 5px;
    cursor: pointer;
    .slider-top{
      position: absolute;
      top: 2px;
      left: 50%;
      transform: translateX(-50%);
    }
    .slider-bottom{
      position: absolute;
      bottom: 2px;
      left: 50%;
      transform: translateX(-50%) rotate(180deg);
    }
    .slider-left{
      position: absolute;
      left: 2px;
      top: 36%;
      transform: rotate(-90deg);
    }
    .slider-right{
      position: absolute;
      right: 2px;
      top: 36%;
      transform:rotate(90deg);
    }
    .slider-active-top{
      position: absolute;
      top: 1px;
      left: 50%;
      transform: translateX(-50%) rotate(-90deg);
    }
    .slider-active-right{
      position: absolute;
      right: 3px;
      top: 32%;
    }

    .slider-active-bottom{
      position: absolute;
      bottom: 1px;
      left: 50%;
      transform: translateX(-50%) rotate(90deg);
    }
    .slider-active-left{
      position: absolute;
      left: 3px;
      top: 32%;
      transform: rotate(180deg);
    }
  }
</style>
