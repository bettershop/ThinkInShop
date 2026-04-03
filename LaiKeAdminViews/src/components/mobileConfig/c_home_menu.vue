<template>
  <div class="mobile-config pro">
    <div v-for="(item,key) in rCom" :key="key">
      <component :is="item.components.name" :configObj="configObj" ref="childData" :configNme="item.configNme" :key="key" @getConfig="getConfig" :index="activeIndex" :num="item.num"></component>
    </div>
    <rightBtn :activeIndex="activeIndex" :configObj="configObj"></rightBtn>
  </div>
</template>

<script>
import toolCom from '@/components/mobileConfigRight/index.js'
import rightBtn from '@/components/rightBtn/index.vue';

export default {
  name: 'c_home_menu',
  cname: '导航组',
  componentsName: 'home_menu',
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
          components: toolCom.c_menu_list,
          configNme: 'menuConfig'
        },
        {
          components: toolCom.c_txt_tab,
          configNme: 'rowStyle'
        },
        {
          components: toolCom.c_bg_color,
          configNme: 'titleColor'
        },
        {
          components: toolCom.c_bg_color,
          configNme: 'bgColor'
        },
        {
          components: toolCom.c_slider,
          configNme: 'mbConfig'
        }
        // toolCom.c_menu_list,
        // toolCom.c_txt_tab,
        // toolCom.c_bg_color,
        // toolCom.c_slider
      ]
    }
  },
  watch: {
    num(nVal) {
      // debugger;
      const value = JSON.parse(JSON.stringify(this.$store.state.admin.mobildConfig.defaultArray[nVal]))
      this.configObj = (value[nVal] ? value[nVal] : value) || {};

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
      // this.configObj = value;
      this.configObj = (value[this.num] ? value[this.num] : value) || {};
      console.log('c_home_menu mounted:', this.configObj);
      this.$set(this.configObj, 'link_key', this.num)
      this.$set(this.configObj, 'num', this.num)

    })
  },
  methods: {
    getConfig(data) {

    }
  }
}
</script>

<style scoped>
</style>
