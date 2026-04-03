<template>
  <div style="margin-bottom: 20px">
    <!-- 轮播图组件 -->
    <template v-if='defaults.name == "swiperBg"'>
      <div class="title-tips swiperBg-style" v-if="configData.tabList">
        <div class='swiperBg-mrmb' style="text-align:left" >样式</div>
         <RadioGroup v-model="configData.tabVal" type="button" size="large" @on-change="radioChange($event)">
            <Radio :label="index" v-for="(item,index) in configData.tabList" :key="index">
              <span class="iconfont-diy" :class="item.icon" v-if="!item.icon"></span>
              <span v-else>{{item.name}}</span>
            </Radio>
          </RadioGroup>
      </div>
    </template>
    <!-- 搜索组件 继续使用原来的 显示布局 -->
    <template v-else>
        <div class="title-tips" v-if="configData.tabList">
          <span>选择模板</span>
          {{configData.tabList[configData.tabVal].name}}
        </div>
        <div class="radio-box" :class="{on:configData.type == 1}">
            <RadioGroup v-model="configData.tabVal" type="button" size="large" @on-change="radioChange($event)">
              <Radio :label="index" v-for="(item,index) in configData.tabList" :key="index">
                <span class="iconfont-diy" :class="item.icon" v-if="item.icon"></span>
                <span v-else>{{item.name}}</span>
              </Radio>
            </RadioGroup>
        </div>
    </template>
  </div>
</template>

<script>
    export default {
        name: 'c_tab',
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
                formData: {
                    type: 0
                },
                defaults: {},
                configData: {}
            }
        },
        watch: {
            configObj: {
                handler (nVal, oVal) {
                    this.defaults = nVal 
                    console.log(this.configData,this.configNme,'this.configNme')
                    if( this.configObj[this.configNme]){
                      this.configData = nVal[this.configNme]
                    }else{
                      this.configData = nVal[nVal.link_key][this.configNme]
                    }
                },
                deep: true
            }
        },
        mounted () {
            this.$nextTick(() => {
                this.defaults = this.configObj
                if(Object.keys(this.configObj).length ==0 )return
                // this.configData = this.configObj[this.configNme]
                if( this.configObj[this.configNme]){
                  this.configData = this.configObj[this.configNme]
                }else{
                  this.configData = this.defaults[this.defaults.link_key][this.configNme]
                }
            })
        },
        methods: {
            radioChange (e) {
                this.$emit('getConfig', e);
            }
        }
    }
</script>

<style scoped lang="stylus">
  .radio-box
    /deep/ .ivu-radio-group-button
      display flex
      width 100%

      .ivu-radio-wrapper
        flex 1
        display flex
        align-items center
        justify-content center

    &.on
      /deep/ .ivu-radio-group-button
        .ivu-radio-wrapper
          flex 1

  .title-tips
    padding-bottom 10px
    font-size 14px
    color #333

    span
      margin-right 14px
      color #999

  .iconfont-diy
    font-size 20px
    line-height 18px

  .swiperBg-style
      padding-bottom 0px
      .swiperBg-mrmb
        margin: 20px 0 16px 0;
        font-family: MicrosoftYaHei, MicrosoftYaHei;
        font-weight: normal;
        font-size: 14px;
        color: #414658;
        font-weight: bold;
        text-align: left;
        font-style: normal;
        text-transform: none;

      .ivu-radio-wrapper
         width: 104px
         height: 32px
         text-align: center
         line-height: 32px
         border-radius: 4px;
        span
          font-family: MicrosoftYaHei, MicrosoftYaHei;
          font-weight: normal;
          font-size: 12px;
          color: #414658;
          line-height: 12px;
          text-align: center;
          font-style: normal;
          text-transform: none;

      .ivu-radio-wrapper
        margin-right: 8px

      span
        width: 104px;
        height: 32px;
        margin: 0;
        line-height: 32px
  .ivu-radio-wrapper-checked
      span
        color: #2890ff
</style>
