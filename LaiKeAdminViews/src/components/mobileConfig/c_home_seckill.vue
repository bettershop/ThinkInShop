<template>
  <div class="mobile-config" style="padding-top: 10px;">
    <div v-for="(item,key) in rCom" :key="key">
      <component :is="item.components.name" :configObj="configObj" ref="childData" :configNme="item.configNme" :key="key" @getConfig="getConfig" :index="activeIndex" :num="item.num" :imgHeight="item.imgHeight" :imgWidth="item.imgWidth"></component>
    </div>
    <rightBtn :activeIndex="activeIndex" :configObj="configObj"></rightBtn>
  </div>
</template>

<script>
import toolCom from '@/components/mobileConfigRight/index.js'
import rightBtn from '@/components/rightBtn/index.vue';

export default {
  name: 'c_home_seckill',
  componentsName: 'home_seckill',
  cname: '秒杀',
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
  components: {
    ...toolCom,
    rightBtn
  },
  data() {
    return {
      configObj: {},
      rCom: [
        {
          components: toolCom.c_input_number,
          configNme: 'numberConfig'
        },
        // {
        //   components: toolCom.c_upload_img,
        //   configNme: 'imgConfig',
        //   imgWidth: 103,
        //   imgHeight: 25

        // },
        // {
        //   components: toolCom.c_upload_img,
        //   configNme: 'seckillBtnImgConfig',
        //   imgWidth: 25,
        //   imgHeight: 19

        // },
        {
          components: toolCom.c_input_item,
          configNme: 'titleConfig',
          titleTextColor: '#999'
        },
        {
          components: toolCom.c_bg_color,
          configNme: 'countDownColor'
        },
        {
          components: toolCom.c_bg_color,
          configNme: 'themeColor'
        },
        {
          components: toolCom.c_bg_color,
          configNme: 'bgColor'
        },
        {
          components: toolCom.c_slider,
          configNme: 'lrConfig'
        },
        {
          components: toolCom.c_slider,
          configNme: 'mbConfig'
        }
        // toolCom.c_input_number,
        // toolCom.c_bg_color,
        // toolCom.c_slider
      ]
    }
  },
  watch: {
    num(nVal) {
      // debugger;
      const value = JSON.parse(JSON.stringify(this.$store.state.admin.mobildConfig.defaultArray[nVal]))
      this.configObj = value[this.num] ? value[this.num] : value;
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
      const value = JSON.parse(JSON.stringify(this.$store.state.admin.mobildConfig.defaultArray[this.num]))
      this.configObj = value[this.num] ? value[this.num] : value;
    })
  },
  methods: {
    // 获取组件参数
    getConfig(data) {
    }

  }
}
</script>

<style scoped>
</style>
