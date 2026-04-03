<template>
  <div class="mobile-config">
    <div v-for="(item,key) in rCom" :key="key">
      <component :is="item.components.name" :configObj="configObj" ref="childData" :configNme="item.configNme" :key="key" :index="activeIndex" :num="item.num" :numName="item.numName" @getConfig="getConfig" :placeholder="item.placeholder" :span="item.span"></component>
    </div>
    <rightBtn :activeIndex="activeIndex" :configObj="configObj"></rightBtn>
  </div>
</template>

<script>
import toolCom from '@/components/mobileConfigRight/index.js'
import rightBtn from '@/components/rightBtn/index.vue';
import { mapMutations } from 'vuex'

export default {
  name: 'c_banner',
  componentsName: 'home_banner',
  components: {
    ...toolCom,
    rightBtn
  },
  props: {
    activeIndex: {
      type: null
    },
    num: {
      type: null
    },
    index: {
      type: null
    }
  },
  data() {
    return {
      configObj: {},
      indicationType: 'circle',
      lockStatus: false,
      bgStatus: false,
      rCom: [
        {
          components: toolCom.c_tab,
          configNme: 'tabConfig'
        },
        {
          components: toolCom.c_menu_list,
          configNme: 'swiperConfig'
        },
        {
          components: toolCom.c_indication,
          configNme: 'c_indication'
        },
        {
          components: toolCom.c_bg_color,
          configNme: 'docColorConfig'
        },
        {
          components: toolCom.c_bg_color,
          configNme: 'docSelectColorConfig'
        },
        {
          components: toolCom.c_bg_color,
          configNme: 'docBackgroundColorConfig'
        },
        {
          components: toolCom.c_title,
          configNme: 'c_title'
        },
        {
          components: toolCom.c_txt_tab,
          configNme: 'imgConfig'
        },
        {
          components: toolCom.c_input_number,
          configNme: 'heightConfig',
          numName: '图片高度',
          placeholder: '请输入轮播图高度',
          span: 5
        },
        {
          components: toolCom.c_bg_color,
          configNme: 'bgColor2'
        },
        {
          components: toolCom.c_slider,
          configNme: 'mbConfig'
        },
        {
          components: toolCom.c_slider,
          configNme: 'paddingConfig'
        },
      ]
    }
  },
  watch: {
    num(nVal) {
      if (this.$store.state.admin.mobildConfig.defaultArray) {
        const value = JSON.parse(JSON.stringify(this.$store.state.admin.mobildConfig.defaultArray[nVal]))
        this.configObj = value[nVal] ? value[nVal] : value;
        this.$set(this.configObj, 'link_key', nVal)
        this.$set(this.configObj, 'num', nVal)
      }
    },
    configObj: {
      handler(nVal, oVal) {
        this.$store.commit('admin/mobildConfig/UPDATEARR', { num: this.num, val: nVal });
      },
      deep: true
    }
  },
  mounted() {
    this.$nextTick(() => {
      if (this.$store.state.admin.mobildConfig.defaultArray) {
        const value = JSON.parse(JSON.stringify(this.$store.state.admin.mobildConfig.defaultArray[this.num]))
        this.configObj = value[this.num] ? value[this.num] : value;
        this.$set(this.configObj, 'link_key', this.num)
        this.$set(this.configObj, 'num', this.num)

      }
    })
  },
  methods: {
    // 获取组件参数
    getConfig() {
      const data = this.configObj.tabConfig.tabVal
      console.log(data, 'tabVal')
      if (data == 1) {
        this.rCom.splice(this.rCom.length - 2, 0, {
          components: toolCom.c_bg_color,
          configNme: 'tabColor'
        })
      } else {
        this.rCom = this.rCom.filter(item => item.configNme !== 'tabColor')
      }
    },
    handleSubmit(name) {
      const obj = {}
      obj.activeIndex = this.activeIndex
      obj.data = this.configObj
      this.add(obj);
    },
    ...mapMutations({
      add: 'admin/mobildConfig/UPDATEARR'
    })
  }
}
</script>

<style scoped lang="stylus">
.title-tips {
  padding-bottom: 10px;
  font-size: 14px;
  color: #333;

  span {
    margin-right: 14px;
    color: #999;
  }
}
</style>
