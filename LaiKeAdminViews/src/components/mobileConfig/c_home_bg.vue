<template>
    <div class="mobile-config">
      <div class="title">
        修改页面背景
      </div>

      <div v-for="(item,key) in rCom" :key="key">
        <component :is="item.components.name" :configObj="configObj" ref="childData" :configNme="item.configNme"
                   :key="key" @getConfig="getConfig" :index="activeIndex" :num="item.num" :canDelete="false"></component>
      </div>
      <rightBtn :activeIndex="activeIndex" :configObj="configObj"></rightBtn>
    </div>
</template>

<script>
    import toolCom from '@/components/mobileConfigRight/index.js'
    import rightBtn from '@/components/rightBtn/index.vue';
    import { mapMutations } from 'vuex'
    export default {
        name: 'c_home_bg',
        componentsName: 'home_bg',
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
        data () {
            return {
                configObj: {},
                rCom: [
                    {
                        components: toolCom.c_bg_color,
                        configNme: 'bgColor'
                    }
                ]
            }
        },
        watch: {
            num (nVal) {
                // debugger;
                const value = JSON.parse(JSON.stringify(this.$store.state.admin.mobildConfig.defaultArray[nVal]))
                this.configObj = value[this.num]?value[this.num]:value;
            },
            configObj: {
                handler (nVal, oVal) {
                    this.$store.commit('admin/mobildConfig/UPDATEARR', { num: this.num, val: nVal });
                },
                deep: true
            }
        },
        mounted () {
            this.$nextTick(() => {
                const value = JSON.parse(JSON.stringify(this.$store.state.admin.mobildConfig.defaultArray[this.num]))
                this.configObj = value[this.num]?value[this.num]:value;
            })
        },
        methods: {
            ...mapMutations({
                add: 'admin/mobildConfig/UPDATEARR'
            }),
            getConfig (data) {

            }
        }

    }
</script>

<style scoped lang="stylus">
.title {
  padding: 13px 0;
  color: #999;
  font-size: 12px;
  border-bottom: 1px solid rgba(0,0,0,0.05);
}
</style>
