<template>
  <div class="slider-box c_input_link">
    <div class="c_row-item">

      <Col class="label" span="4" :style="{color: '#999'}">
        {{configData.title}}
      </Col>

      <Col class="label" span="19">
        <div style="display:flex;">
          <Select
            class="select-link-type"
            v-model="link_type" 
          >
            <Option value="product" key="product">普通商品</Option>
            <Option value="jifen" key="jifen">积分商品</Option> 
            <Option value="fenlei" key="fenlei">分类</Option> 
            <Option value="diy" key="diy">自定义</Option>
          </Select>

          <Input
            v-model="configData.value"
            v-if="link_type === 'diy'"
          />

          <Select
            class="select-box"
            v-model="configData.value"
            filterable
            clearable
            v-if="link_type === 'product'"
			 placeholder="请选择"
          >
            <Option
              v-for="item in product_links"
              :value="`/pagesC/goods/goodsDetailed?toback=true&pro_id=${item.sid}`"
              :key="item.id"
            >{{ item.name }}</Option
            >
          </Select>

          <Select
            class="select-box"
            v-model="configData.value"
            filterable
            clearable
            v-if="link_type === 'jifen'"
			 placeholder="请选择"
          >
            <Option
              v-for="item in jifen_links"
               :value="`/pagesC/goods/goods?cid=${item.sid}`"
              :key="item.id"
            >{{ item.name }}</Option
            >
          </Select>
 

          <Select
            class="select-box"
            v-model="configData.value"
            filterable
            clearable
            v-if="link_type === 'course'"
			 placeholder="请选择"
          >
            <Option
              v-for="item in course_links"
              :value="item.url"
              :key="item.id"
            >{{ item.name }}</Option
            >
          </Select>

          <Select
            class="select-box"
            v-model="configData.value"
            filterable
            clearable
            v-if="link_type === 'fenlei'"
			 placeholder="请选择"
          >
            <Option
              v-for="item in feilei_links"
              :value="item.url"
              :key="item.id"
            >{{ item.name }}</Option
            >
          </Select>
 
        </div>
      </Col>
    </div>
  </div>
</template>

<script>
    import { mapState } from 'vuex';

    export default {
        name: 'c_input_link',
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
            }
        },
        computed: {
            ...mapState({
                product_links: state => state.admin.mobildConfig.product_links,
                jifen_links: state => state.admin.mobildConfig.jifen_links, 
                feilei_links: state => state.admin.mobildConfig.feilei_links, 
                course_links: state => state.admin.mobildConfig.course_links
            })
        },
        data () {
            return {
                defaults: {},
                configData: {},
                link_type: 'product'
            }
        },

        mounted () {
            this.$nextTick(() => {
                this.defaults = this.configObj
                if (this.configObj[this.configNme]) {
                    this.configData = this.configObj[this.configNme]
                }
            })
        },
        watch: {
            configObj: {
                handler (nVal, oVal) {
                    this.defaults = nVal
                    if (nVal[this.configNme]) {
                        this.configData = nVal[this.configNme]
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

<style lang="stylus" scoped>
.c_input_link {
  >>> .ivu-select-dropdown {
    top auto !important;
    left auto !important
  }

  .select-link-type {
    width 100px;
    margin-right: 6px
  }

}
</style>
