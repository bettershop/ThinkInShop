<template>
  <div class="mobile-config">
    <div v-for="(item,key) in rCom" :key="key">
      <component :is="item.components.name" :configObj="configObj" ref="childData" :configNme="item.configNme" :key="key" @getConfig="getConfig" :index="activeIndex" :num="item.num" :canDelete="false" :imgHeight="item.imgHeight" :imgWidth="item.imgWidth"></component>
    </div>
    <rightBtn :activeIndex="activeIndex" :configObj="configObj"></rightBtn>
  </div>
</template>

<script>
import toolCom from '@/components/mobileConfigRight/index.js'
import rightBtn from '@/components/rightBtn/index.vue';
import { mapMutations } from 'vuex'
export default {
  name: 'c_home_adv',
  componentsName: 'home_adv',
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
      rCom: [
        {
          components: toolCom.c_menu_list,
          configNme: 'advConfig',
          // imgHeight: 64,
          // imgWidth: 128
        },
        {
          components: toolCom.c_bg_color,
          configNme: 'bgColor'
        },
        {
          components: toolCom.c_bg_color,
          configNme: 'bgColor2'
        },
        {
          components: toolCom.c_txt_tab,
          configNme: 'imgConfig'
        },
        {
          components: toolCom.c_txt_tab,
          configNme: 'bgTopLeftConfig'
        },
        {
          components: toolCom.c_txt_tab,
          configNme: 'bgTopRightConfig'
        },
        {
          components: toolCom.c_txt_tab,
          configNme: 'bgBottomLeftConfig'
        },
        {
          components: toolCom.c_txt_tab,
          configNme: 'bgBottomRightConfig'
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
      this.configObj = value;
      this.$set(this.configObj, 'link_key', nVal)
      this.$set(this.configObj, 'num', nVal)

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
      this.configObj = value;
      this.$set(this.configObj, 'link_key', this.num)
      this.$set(this.configObj, 'num', this.num)

    })
  },
  methods: {
    ...mapMutations({
      add: 'admin/mobildConfig/UPDATEARR'
    }),
    getConfig(data) {

    }
  }
}
</script>

<style scoped>
</style>
