<template>
  <div class="mobile-config" style="padding-top: 10px;">
    <div v-for="(item,key) in rCom" :key="key">
      <component :is="item.components.name" :configObj="configObj" ref="childData" :configNme="item.configNme" :numName="item.numName" :key="key" @getConfig="getConfig" :index="activeIndex" :num="item.num" :imgWidth="item.imgWidth" :imgHeight="item.imgHeight" :titleTextColor="item.titleTextColor"></component>
    </div>
    <rightBtn :activeIndex="activeIndex" :configObj="configObj"></rightBtn>
  </div>
</template>

<script>
import toolCom from '@/components/mobileConfigRight/index.js'
import rightBtn from '@/components/rightBtn/index.vue';

export default {
  name: 'c_home_store',
  componentsName: 'home_store',
  cname: '店铺',
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
          configNme: 'numberConfig',
          numName: '店铺数量'
        },
        {
          components: toolCom.c_input_item,
          configNme: 'titleConfig',
          titleTextColor: '#999'
        },
        // {
        //   components: toolCom.c_upload_img,
        //   configNme: 'imgConfig',
        //   imgWidth: 111,
        //   imgHeight: 19
        // },
        {
          components: toolCom.c_bg_color,
          configNme: 'titleTxtColor'
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
        console.log('handler2');
        this.$store.commit('admin/mobildConfig/UPDATEARR', { num: this.num, val: nVal });
      },
      deep: true
    }
  },
  mounted() {
    this.$nextTick(() => {
      const defaultArray = this.$store.state.admin.mobildConfig.defaultArray
      console.log(defaultArray, 'defaultArray', this.num);
      if (defaultArray[this.num]) {
        const value = JSON.parse(JSON.stringify(defaultArray[this.num]))
        this.configObj = value[this.num] ? value[this.num] : value;
      }
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
