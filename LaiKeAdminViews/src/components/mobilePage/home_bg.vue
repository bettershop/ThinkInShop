<template>
  <div class="mobile-page"  >
    背景组件
  </div>
</template>

<script>
    import { mapState } from 'vuex';
    export default {
        name: 'home_bg',
        cname: '背景',
        icon: 'icon-background',
        configName: 'c_home_bg',
        defaultName: 'homeBg',
        type: 2,
        props: {
            index: {
                type: null
            },
            num: {
                type: null
            }
        },
        computed: {
            ...mapState('admin/mobildConfig', ['defaultArray'])
        },
        watch: {
            pageData: {
                handler (nVal, oVal) {
                    this.setConfig(nVal)
                },
                deep: true
            },
            num: {
                handler (nVal, oVal) {
                    const data = this.$store.state.admin.mobildConfig.defaultArray[nVal]
                    this.setConfig(data)
                },
                deep: true
            },
            defaultArray: {
                handler (nVal, oVal) {
                    const data = this.$store.state.admin.mobildConfig.defaultArray[this.num]
                    if(!data.pointName){
                        this.$set(data,'pointName','home_bg')
                    }
                    this.setConfig(data);
                },
                deep: true
            }
        },
        data () {
            return {
                defaultConfig: {
                    name: 'homeBg',
                    configName: 'c_home_bg',
                    pointName:'home_bg',
                    cname: '背景',
                    icon: 'icon-background',
                    timestamp: this.num,
                    bgConfig: {
                        title: '添加的背景将位于图层的最下方，请注意调整其他组件的背景色'
                    },
                    // 背景颜色
                    bgColor: {
                        colorOrimg:true,
                        title: '背景颜色',
                        type :1, // 1 渐变 2 图片
                        imgUrl: '',
                        default: [
                            {
                                item: '#FFFFFF'
                            },
                            {
                                item: '#FFFFFF'
                            }
                        ],
                        color: [
                            {
                                item: '#FFFFFF'
                            },
                            {
                                item: '#FFFFFF'
                            }
                        ]
                    }
                },
                bgColor: [
                    {
                        item: 'rgba(207,230,230,1)'
                    },
                    {
                        item: 'rgba(207,230,230,1)'
                    }
                ],
                pageData: {},
                aTop: 0 // 绝对定位距离顶部的距离
            }
        },
        mounted () {
            this.$nextTick(() => {
                console.log(this.num)
                this.pageData = this.$store.state.admin.mobildConfig.defaultArray[this.num]
                this.setConfig(this.pageData)
            })
        },
        methods: {
            setConfig (data) {
                if (data) {
                    if(!data.bgColor){
                        data = data[this.num] 
                    }
                    console.log('home_bg data:::',data)
                    
                    this.bgColor = data.bgColor.color;
                } else {
                    // this.bgColor = [
                    //     {
                    //         item: '#FFFFFF'
                    //     },
                    //     {
                    //         item: '#FFFFFF'
                    //     }
                    // ]
                }
            }
        }
    }
</script>

<style scoped lang="stylus">
  .mobile-page
    height: 50px;
    text-align: center
    line-height: 50px
  /*.mobile-page*/
  /*  background red*/
  /*  height 100px*/
  /*  position absolute*/
  /*  top: 0*/
</style>
