<template>
  <div class="c_row">
    <div class="c_row-left">指示点</div>
    <div class="c_row-right">
      <div :class="{ active: configData.type == 'circle' }" @click="setConfig('circle')">圆形</div>
      <div :class="{ active: configData.type == 'line' }" style="margin: 0 4px;" @click="setConfig('line')">线形</div>
      <div :class="{ active: configData.type == 'number' }" @click="setConfig('number')">数字</div>
    </div>
  </div>

</template>

<script>
    export default {
        name: 'c_slider_padding',
        props: {
            configObj: {
                type: Object
            },
            configNme: {
                type: String
            }
        },
        created () {
            this.defaults = this.configObj
            try {
              if(Object.keys(this.defaults).length ==0 )return

              if( this.defaults[this.configNme]){
                this.configData = this.defaults[this.configNme]
              }else{
                this.configData = this.defaults[this.defaults.link_key][this.configNme]
              }
            } catch (error) {
              console.error('c_bg_color::',error)
            }

        },
        watch: {
            configObj: {
                handler (nVal, oVal) {
                    this.defaults = nVal
                    if(Object.keys(this.defaults).length ==0 )return
                    this.configData = nVal[this.configNme]
                    if( this.configObj[this.configNme]){
                      this.configData = nVal[this.configNme]
                    }else{
                      this.configData = nVal[nVal.link_key][this.configNme]
                    }
                },
                immediate: true,
                deep: true
            }
        },
        methods: {
            setConfig (data) {
              this.configData.type = data
              this.configObj.c_indication.type = data
              console.log(data,this.defaults,this.configNme, 'defaults')

            }
        },
    }
</script>

<style scoped lang="stylus">
  .c_row{
    display: flex;
    align-items: center;
    justify-content: space-between;
    .c_row-left{

    }
    .c_row-right{
      .active{
        color: #409eff;
        border: 1px solid #409eff;
      }
      display: flex;
      align-items: center;
      justify-content: space-between;
      div{
        width :50px;
        text-align: center;
        cursor: pointer;
        border: 1px solid #dcdfe6;
        border-radius: 4px;
        padding: 2px 0;
        color: #606266;
        &:hover{
          color: #409eff;
        }
      }
    }
  }
</style>
