<template>
  <div class="mobile-config">
    <div v-for="(item,key) in rCom" :key="key">
      <component :is="item.components.name" :configObj="configObj" ref="childData" :configNme="item.configNme"
                 :key="key" @getConfig="getConfig" :index="activeIndex" :num="item.num" :fieldName="item.fieldName"></component>
    </div>
    <rightBtn :activeIndex="activeIndex" :configObj="configObj"></rightBtn>
  </div>
</template>

<script>
    import toolCom from '@/components/mobileConfigRight/index.js'
    import rightBtn from '@/components/rightBtn/index.vue';

    export default {
        name: 'c_comp_title',
        componentsName: 'comp_title',
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
                        components: toolCom.c_input_item,
                        configNme: 'titleConfig'
                    }, 
                    {
                        components: toolCom.c_slider,
                        configNme: 'titleSize'
                    }, 
                    {
                        components: toolCom.c_bg_color,
                        configNme: 'bgColor'
                    },
                    {
                        components: toolCom.c_switch,
                        configNme: 'linkStyle', 
                        fieldName:'是否显示返回键'
                    }, 
                    {
                        components: toolCom.c_bg_color,
                        configNme: 'color'
                    },     
                ]
            }
        },
        watch: {
            num (nVal) {
                // debugger;
                const value = JSON.parse(JSON.stringify(this.$store.state.admin.mobildConfig.defaultArray[nVal]))
                this.configObj = value;
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
                this.configObj = value;
            })
        },
        methods: {
            // 获取组件参数
            getConfig (data) {
            }
        }
    }
</script>

<style scoped>

</style>
