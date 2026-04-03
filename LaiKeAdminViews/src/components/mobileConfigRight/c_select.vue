<template>
  <div class="slider-box">
    <div class="c_row-item">
      <Col class="label" span="4" v-if="configData.title">
        {{configData.title}}
      </Col>
      <Col span="19" class="slider-box">
        <Select v-model="configData.activeValue" @on-change="sliderChange">
          <Option v-for="(item,index) in configData.list" :value="item.activeValue" :key="index">{{ item.title }}
          </Option>
        </Select>
      </Col>
    </div>
  </div>
</template>

<script>
    export default {
        name: 'c_select',
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
                configData: {}
            }
        },
        mounted () {
            this.$nextTick(() => {
                this.defaults = this.configObj
                // this.configData = this.configObj[this.configNme]
                if( this.configObj[this.configNme]){
                  this.configData = this.configObj[this.configNme]
                }else{
                  this.configData = this.defaults[this.defaults.link_key][this.configNme]
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
            sliderChange (e) {
                console.log(e, 'item')
                this.$emit('getConfig', { name: 'select', values: e })
            }
        }
    }
</script>

<style scoped>

</style>
