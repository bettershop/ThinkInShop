<template>
  <div class="box" v-if="configData">
    <div class="c_row-item">
      <Col class="label" span="4" :style="{color: titleTextColor}">
        {{configData.title}}
      </Col>
      <Col span="19" class="slider-box">
        <Select v-model="configData.reduction_id" multiple >
            <Option v-for="item in configData.cityList" :value="item.id" :key="item.id">{{ item.title }}</Option>
        </Select>
      </Col>
    </div>
  </div>

</template>

<script>
    export default {
        name: 'c_select_multiple',
        props: {
            configObj: {
                type: Object
            },
            configNme: {
                type: String
            },
            titleTextColor: {
                type: String,
                default: '#515A6E'
            },
            cityList: {
              type: Array,
              default: () => []
            }
        },
        data () {
            return {
                defaults: {},
                configData: {},
                // cityList: [
                //     {
                //         value: 'New York',
                //         label: 'New York'
                //     },
                //     {
                //         value: 'London',
                //         label: 'London'
                //     },
                //     {
                //         value: 'Sydney',
                //         label: 'Sydney'
                //     },
                //     {
                //         value: 'Ottawa',
                //         label: 'Ottawa'
                //     },
                //     {
                //         value: 'Paris',
                //         label: 'Paris'
                //     },
                //     {
                //         value: 'Canberra',
                //         label: 'Canberra'
                //     }
                // ],
                reduction_id: []
            }
        },
        created () {
            this.defaults = this.configObj
            // this.configData = this.configObj[this.configNme]
            if( this.configObj[this.configNme]){
                  this.configData = this.configObj[this.configNme]
                }else{
                  this.configData = this.defaults[this.defaults.link_key][this.configNme]
                }
            console.log(this.configData)
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
                immediate: true,
                deep: true
            }
        }
    }
</script>

<style scoped lang="stylus">
  .c_row-item
    margin-bottom 13px

    /deep/.ivu-select .ivu-select-dropdown
        top: auto!important;
        left: auto!important;
</style>
