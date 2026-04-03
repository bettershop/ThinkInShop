import * as echarts from 'echarts'
import { userIndex } from '@/api/data'
import { mixinstest } from '@/mixins/index'

export default {
  name: 'addvit-report',
  mixins: [mixinstest],
  data () {
    return {
      dep: 1,
      kep: 2,
      yep: 3,
      oneday: 1,
      dayList_one: [
        {
          value: 1,
          label: this.$t('home.homeReport.zj7t')
        },
        {
          value: 2,
          label: this.$t('home.homeReport.zj1y')
        },
        {
          value: 3,
          label: this.$t('home.homeReport.zj1n')
        },
        {
          value: 4,
          label: this.$t('home.homeReport.zjqb')
        }
      ],
      myChart: null, //活跃用户
      myChart2: null, //活跃用户
      myChart3: null, //新增用户
      myChart4: null, //会员统计
      myChart5: null, //用户消费排行
      arrList_two: [],
      arrList: [], //数据总览list
      activeuserData: [], //活跃用户list
      userCount: [], //用户统计list
      countNum: [], //用户统计数量List
      allNum: 0, //用户总数量
      moneyTop: [], //用户消费排行总list
      storeList: [], //用户消费排行
      additiondata: [], //新增用户
      addList: [], //新增用户list
      Membership:[],//会员统计
      MembershipList:[],//会员统计list
    }
  },
  mounted () {
    this.myChart = echarts.init(document.getElementById('activeUser')) //创建实例

    //用户统计 饼图
    this.myChart2 = echarts.init(document.getElementById('userStatistics')) //创建实例

    //新增用户
    this.myChart3 = echarts.init(document.getElementById('newUsers')) //创建实例

    //会员统计
    this.myChart4 = echarts.init(document.getElementById('memberStatistics')) //创建实例

    //用户消费排行
    this.myChart5 = echarts.init(document.getElementById('Store1')) //创建实例

    this.getApi()
  },
  methods: {
    //活跃用户
    activeUser () {
      // var chartDom = document.getElementById('activeUser')
      // var myChart = echarts.init(chartDom)
      var option = {
        tooltip: {
          formatter: params => {
            return `<div style="min-width:121px;padding:10px;">
                            <div><span style="color: rgba(0, 0, 0, 0.45)">${params[0].axisValue}</span><div>

                            <div style="margin-top:10px;width:100%;display:flex;justify-content: space-between">
                            <span> ${params[0].value}`+this.$t('home.homeReport.ren')+`</span></div> 
                            </div>

                    </div>`
          },
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          },
          color: '#000000',
          padding: 0,
          border: 0,
          backgroundColor: '#FFFFFF', //通过设置rgba调节背景颜色与透明
          borderWidth: 0,
          borderColor: 'rgba(255, 255, 255,0.3)',
          textStyle: {
            color: '#000000'
          }
        },
        dataZoom: [
          {
            type: 'inside',
            show: true,
            realtime: true,
            start: 30,
            end: 70,
            textStyle: {
              color: '#000000' //滚动条两边字体样式
            },
            handleIcon: 'image://' + require('@/assets/imgs/bf.png'),
            handleSize: '100%',
            handleStyle: {
              color: '#D9D9D9',
              borderColor: '#D9D9D9'
            },
            // 高度
            width: '100%',
            left: 'center',
            right: 'center',
            moveHandleSize: 0, //取消头部的横条
            showDetail: true, // 拖拽时候是否显示详细数值信息
            showDataShadow: true // 组件中是否显示数据阴影
          },
          {
            show: true,
            start: 0,
            end: 100 - 1500 / 31,
            textStyle: {
              color: '#000000' //滚动条两边字体样式
            },
            fillerColor: 'rgba(0, 0, 0, 0)',
            backgroundColor: '#f0f2f5',
            handleIcon: 'image://' + require('@/assets/imgs/bf.png'),
            dataBackground: {
              areaStyle: {
                color: '#ced4d9',
                opacity: 0.8
              }
            },

            handleStyle: {
              color: '#D9D9D9',
              borderColor: '#D9D9D9'
            },
            width: '95%',
            left: 'center',
            right: 'center',
            moveHandleSize: 0, //取消头部的横条
            showDetail: true, // 拖拽时候是否显示详细数值信息
            showDataShadow: true // 组件中是否显示数据阴
          }
        ],
        toolbox: {
          feature: {
            // saveAsImage: {}
          }
        },

        grid: {
          x: 40, //距离  左边的长度
          y: 50, //距离  头部的长度
          x2: 30, // 距离  右边的长度
          y2: 80, //距离  底部的长度
          top: 10
        },
        xAxis: [
          {
            type: 'category',
            boundaryGap: false,
            axisLabel: {
              show: true,
              textStyle: {
                color: 'rgba(0, 0, 0, 0.45)'
              }
            },
            data: this.activeList?.[0]
          }
        ],
        yAxis: [
          {
            type: 'value',
            axisLabel: {
              show: true,
              textStyle: {
                color: 'rgba(0, 0, 0, 0.45)'
              }
            },
            splitLine: {
              lineStyle: {
                type: 'dashed',
                color: '#cccccc'
              },
              show: true //隐藏
            }
          }
        ],
        series: [
          {
            type: 'line',
            stack: 'Total',
            areaStyle: {},
            emphasis: {
              focus: 'series'
            },
            symbol: 'circle',
            color: '#c9e3ff',
            itemStyle: {
              //折线拐点标志的样式
              borderColor: '#2890ff', //拐点的边框颜色
              borderWidth: 3.5
            },
            lineStyle: {
              //折线的样式
              color: '#2890ff'
            },
            areaStyle: {
              //渐变色
              normal: {
                color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                  { offset: 0, color: '#c9e3ff' },
                  { offset: 0.3, color: '#c9e3ff' },
                  { offset: 1, color: '#ffffff' }
                ])
              }
            },
            data: this.activeList?.[1]
          }
        ]
      }

      this.myChart.setOption(option)
    },
    //用户统计
    userStatistics () {
      // var chartDom = document.getElementById('userStatistics')
      // var myChart1 = echarts.init(chartDom)
      var echartsWrapper = this.myChart2
      var option={}
      console.log(this.allNum,'allNum')
      if(this.allNum>0){
        option = {
          tooltip: {
            formatter: params => {
              // console.log('params', params)
  
              return `<div style="min-width:170px;padding:10px;">
                              <div><span style="color: rgba(0, 0, 0, 0.45)">${params.name}</span><div>
  
                              <div style="margin-top:10px;width:100%;display:flex;justify-content: space-between">
                              <span>${params.marker}&nbsp;`+this.$t('home.homeReport.rszb')+`</span> <span>${params.percent}%</span></div> 
                              </div>
  
                      </div>`
            },
            trigger: 'item',
            axisPointer: {
              type: 'shadow'
            },
            color: '#000000',
            padding: 0,
            border: 0,
            backgroundColor: '#FFFFFF', //通过设置rgba调节背景颜色与透明
            borderWidth: 0,
            borderColor: 'rgba(255, 255, 255,0.3)',
            textStyle: {
              color: '#000000'
            }
          },
          legend: {
            icon: 'roundRect',
            itemHeight: 15,
            itemWidth: 15,
            type: 'scroll',
            left: 'left',
            left: '5.5%',
            itemGap: 30 //间距
          },
          color: ['#2890FF', '#5AD8A6', '#F6BD16', '#E8684A'],
          series: [
            {
              type: 'pie',
              radius: '82%',
              center: ['50%', '55%'], // 控制饼图位置 1 左右 2上下
              data: this.userCount,
              label: {
                normal: {
                  show: false
                }
              },
              emphasis: {
                itemStyle: {
                  shadowBlur: 10,
                  shadowOffsetX: 0,
                  shadowColor: 'rgba(0, 0, 0, 0.5)'
                }
              }
            }
          ]
        }
      }else{
        option = {
          tooltip: {
            formatter: params => {
              // console.log('params', params)
  
              return `<div style="min-width:170px;padding:10px;">
                              <div><span style="color: rgba(0, 0, 0, 0.45)">${params.name}</span><div>
  
                              <div style="margin-top:10px;width:100%;display:flex;justify-content: space-between">
                              <span>${params.marker}&nbsp;`+this.$t('home.homeReport.rszb')+`</span> <span>${params.percent}%</span></div> 
                              </div>
  
                      </div>`
            },
            trigger: 'item',
            axisPointer: {
              type: 'shadow'
            },
            color: '#000000',
            padding: 0,
            border: 0,
            backgroundColor: '#FFFFFF', //通过设置rgba调节背景颜色与透明
            borderWidth: 0,
            borderColor: 'rgba(255, 255, 255,0.3)',
            textStyle: {
              color: '#000000'
            }
          },
          title: {
            text: this.$t('zdata.zwsj'),

            top: "50%",
            right: "center",
            // textAlign:"center",
            // top: '310',
            // 主标题样式
            textStyle: {
              fontSize: "12.8",
              color: "rgba(255, 255, 255, 1)",
              fontFamily: "PingFangSC-Regular",
            },

          },
          legend: {
            icon: 'roundRect',
            itemHeight: 15,
            itemWidth: 15,
            type: 'scroll',
            left: 'left',
            left: '5.5%',
            itemGap: 30 //间距
          },
          color: ['#B2BCD1', '#B2BCD1', '#B2BCD1', '#B2BCD1'],
          series: [
            {
              type: 'pie',
              radius: '82%',
              center: ['50%', '55%'], // 控制饼图位置 1 左右 2上下
              data: this.userCount,
              label: {
                normal: {
                  show: false
                }
              },
              emphasis: {
                itemStyle: {
                  shadowBlur: 10,
                  shadowOffsetX: 0,
                  shadowColor: 'rgba(0, 0, 0, 0.5)'
                }
              }
            }
          ]
        }
      }
      
      echartsWrapper.clear();
      echartsWrapper.setOption(option);
      
      // 至少保留一个图例
      echartsWrapper.on('legendselectchanged', function (params) { 
          var optionLegend = echartsWrapper.getOption(); 
          var selectValue = Object.values(params.selected); 
          var n = 0; 
          selectValue.map(function (res) {
               if (!res) { 
                   n++; 
                } 
          });
           if (n == selectValue.length) {
               optionLegend.legend[0].selected[params.name] = true;
          }
          echartsWrapper.setOption(optionLegend)
      });      
      // this.myChart2.setOption(option)
    },
    //新增用户
    newUsers () {
      // var chartDom = document.getElementById('newUsers')
      // var myChart2 = echarts.init(chartDom)
      var echartsWrapper = this.myChart3
      var option = {
        tooltip: {
          formatter: params => {
            var dotHtml =
              '<span style="display: inline-block;width: 15px;height: 1px;border: 2px solid #2890FF;background-color: #2890FF;margin-right: 8px;margin-bottom: 3px;"></span>'
            var dotHtml2 =
              '<span style="display: inline-block;width: 15px;height: 1px;border: 2px solid #5AD8A6;background-color: #5AD8A6;margin-right: 8px;margin-bottom: 3px;"></span>'
            var dotHtml3 =
              '<span style="display: inline-block;width: 15px;height: 1px;border: 2px solid #F6BD16;background-color: #F6BD16;margin-right: 8px;margin-bottom: 3px;"></span>'
            var dotHtml4 =
              '<span style="display: inline-block;width: 15px;height: 1px;border: 2px solid #E8684A;background-color: #E8684A;margin-right: 8px;margin-bottom: 3px;"></span>'
            if (params[0]) {
              if (params[0].seriesName == this.$t('home.homeReport.appd')) {
                var dotHtml5 = dotHtml
              } else if (params[0].seriesName == this.$t('home.homeReport.xcxd')) {
                var dotHtml5 = dotHtml2
              } else if (params[0].seriesName == this.$t('home.homeReport.H5d')) {
                var dotHtml5 = dotHtml3
              } else if (params[0].seriesName == this.$t('home.homeReport.pcsc')) {
                var dotHtml5 = dotHtml4
              }
            }
            if (params[1]) {
              if (params[1].seriesName == this.$t('home.homeReport.appd')) {
                var dotHtml6 = dotHtml
              } else if (params[1].seriesName == this.$t('home.homeReport.xcxd')) {
                var dotHtml6 = dotHtml2
              } else if (params[1].seriesName == this.$t('home.homeReport.H5d')) {
                var dotHtml6 = dotHtml3
              } else if (params[1].seriesName == this.$t('home.homeReport.pcsc')) {
                var dotHtml6 = dotHtml4
              }
            }
            if (params[2]) {
              if (params[2].seriesName == this.$t('home.homeReport.appd')) {
                var dotHtml7 = dotHtml
              } else if (params[2].seriesName == this.$t('home.homeReport.xcxd')) {
                var dotHtml7 = dotHtml2
              } else if (params[2].seriesName == this.$t('home.homeReport.H5d')) {
                var dotHtml7 = dotHtml3
              } else if (params[2].seriesName == this.$t('home.homeReport.pcsc')) {
                var dotHtml7 = dotHtml4
              }
            }
            if (params[3]) {
              if (params[3].seriesName == this.$t('home.homeReport.appd')) {
                var dotHtml8 = dotHtml
              } else if (params[3].seriesName == this.$t('home.homeReport.xcxd')) {
                var dotHtml8 = dotHtml2
              } else if (params[3].seriesName == this.$t('home.homeReport.H5d')) {
                var dotHtml8 = dotHtml3
              } else if (params[3].seriesName == this.$t('home.homeReport.pcsc')) {
                var dotHtml8 = dotHtml4
              }
            }
            switch (params.length) {
              case 1:
                return `<div style="min-width:145px;padding:10px;">
                <div><span style="color: rgba(0, 0, 0, 0.45)">${params[0].axisValue} &nbsp;`+this.$t('home.homeReport.yhsl')+`</span></div>
                <div style="margin-top:10px;width:100%;">
                    <div style="display:flex;justify-content: space-between"><span>${dotHtml5}${params?.[0]?.seriesName}</span> <span> ${params?.[0]?.value}</span></div> 
                  </div>
                </div>`
                break

              case 2:
                return `<div style="min-width:145px;padding:10px;">
                  <div><span style="color: rgba(0, 0, 0, 0.45)">${params[0].axisValue} &nbsp;`+this.$t('home.homeReport.yhsl')+`</span></div>
                  <div style="margin-top:10px;width:100%;">
                      <div style="display:flex;justify-content: space-between"><span>${dotHtml5}${params?.[0]?.seriesName}</span> <span> ${params?.[0]?.value}</span></div> 
                      <div style="display:flex;justify-content: space-between;margin:5px 0px"><span>${dotHtml6}${params?.[1]?.seriesName}</span> <span> ${params?.[1]?.value}</span></div> 
                    </div>
                </div>`
                break

              case 3:
                return `<div style="min-width:145px;padding:10px;">
                  <div><span style="color: rgba(0, 0, 0, 0.45)">${params[0].axisValue} &nbsp;`+this.$t('home.homeReport.yhsl')+`</span></div>
                  <div style="margin-top:10px;width:100%;">
                      <div style="display:flex;justify-content: space-between"><span>${dotHtml5}${params?.[0]?.seriesName}</span> <span> ${params?.[0]?.value}</span></div> 
                      <div style="display:flex;justify-content: space-between;margin:5px 0px"><span>${dotHtml6}${params?.[1]?.seriesName}</span> <span> ${params?.[1]?.value}</span></div> 
                      <div style="display:flex;justify-content: space-between"><span>${dotHtml7}${params?.[2]?.seriesName}</span> <span> ${params?.[2]?.value}</span></div> 
                    </div>
                </div>`
                break

              case 4:
                return `<div style="min-width:145px;padding:10px;">
                      <div><span style="color: rgba(0, 0, 0, 0.45)">${params[0].axisValue} &nbsp;`+this.$t('home.homeReport.yhsl')+`</span></div>
                      <div style="margin-top:10px;width:100%;">
                          <div style="display:flex;justify-content: space-between"><span>${dotHtml5}${params?.[0]?.seriesName}</span> <span> ${params?.[0]?.value}</span></div> 
                          <div style="display:flex;justify-content: space-between;margin:5px 0px"><span>${dotHtml6}${params?.[1]?.seriesName}</span> <span> ${params?.[1]?.value}</span></div> 
                          <div style="display:flex;justify-content: space-between"><span>${dotHtml7}${params?.[2]?.seriesName}</span> <span> ${params?.[2]?.value}</span></div> 
                          <div style="display:flex;justify-content: space-between;margin:5px 0px"><span>${dotHtml8}${params?.[3]?.seriesName}</span> <span> ${params?.[3]?.value}</span></div> 
                        </div>
                    </div>`
                break
            }
          },
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          },
          color: '#000000',
          padding: 0,
          border: 0,
          backgroundColor: '#FFFFFF', //通过设置rgba调节背景颜色与透明
          borderWidth: 0,
          borderColor: 'rgba(255, 255, 255,0.3)',
          textStyle: {
            color: '#000000'
          }
        },
        dataZoom: [
          {
            type: 'inside',
            show: true,
            realtime: true,
            start: 30,
            end: 70,
            textStyle: {
              color: '#000000' //滚动条两边字体样式
            },
            handleIcon: 'image://' + require('@/assets/imgs/bf.png'),
            handleSize: '100%',
            handleStyle: {
              color: '#D9D9D9',
              borderColor: '#D9D9D9'
            },
            // 高度
            width: '100%',
            left: 'center',
            right: 'center',
            moveHandleSize: 0, //取消头部的横条
            showDetail: true, // 拖拽时候是否显示详细数值信息
            showDataShadow: true // 组件中是否显示数据阴影
          },
          {
            show: true,
            start: 0,
            end: 100 - 1500 / 31,
            textStyle: {
              color: '#000000' //滚动条两边字体样式
            },
            fillerColor: 'rgba(0, 0, 0, 0)',
            backgroundColor: '#f0f2f5',
            handleIcon: 'image://' + require('@/assets/imgs/bf.png'),
            dataBackground: {
              areaStyle: {
                color: '#ced4d9',
                opacity: 0.8
              }
            },

            handleStyle: {
              color: '#D9D9D9',
              borderColor: '#D9D9D9'
            },
            width: '95%',
            left: 'center',
            right: 'center',
            moveHandleSize: 0, //取消头部的横条
            showDetail: true, // 拖拽时候是否显示详细数值信息
            showDataShadow: true // 组件中是否显示数据阴
          }
        ],
        color: ['#2890FF', '#5AD8A6', '#F6BD16', '#E8684A'],
        legend: {
          data: [this.$t('home.homeReport.appd'), this.$t('home.homeReport.xcxd'), this.$t('home.homeReport.H5d'), this.$t('home.homeReport.pcsc')],
          icon: 'roundRect',
          itemHeight: 15,
          itemWidth: 15,
          type: 'scroll',
          // orient: 'vertical',
          left: 'left',
          left: '2.5%',
          itemGap: 30 //间距
        },
        toolbox: {
          feature: {
            // saveAsImage: {}
          }
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '20%',
          containLabel: true
        },
        xAxis: [
          {
            type: 'category',
            boundaryGap: false,
            axisLabel: {
              show: true,
              textStyle: {
                color: 'rgba(0, 0, 0, 0.45)'
              }
            },
            data: this.addList?.[0]
          }
        ],
        yAxis: [
          {
            type: 'value',
            minInterval:1,
            axisLabel: {
              show: true,
              textStyle: {
                color: 'rgba(0, 0, 0, 0.45)'
              },
            },
            splitLine: {
              lineStyle: {
                type: 'dashed',
                color: '#cccccc'
              },
              show: true //隐藏
            }
          }
        ],
        series: [
          {
            name: this.$t('home.homeReport.appd'),
            type: 'line',
            // stack: 'Total',
            data: this.addList?.[1]
          },
          {
            name: this.$t('home.homeReport.xcxd'),
            type: 'line',
            // stack: 'Total',
            data: this.addList?.[2]
          },
          {
            name: this.$t('home.homeReport.H5d'),
            type: 'line',
            // stack: 'Total',
            data: this.addList?.[3]
          },
          {
            name: this.$t('home.homeReport.pcsc'),
            type: 'line',
            // stack: 'Total',
            data: this.addList?.[4]
          }
        ]
      }
      echartsWrapper.clear();
      // 设置配置项
      echartsWrapper.setOption(option);
      
      // 至少保留一个图例
      echartsWrapper.on('legendselectchanged', function (params) { 
          var optionLegend = echartsWrapper.getOption(); 
          var selectValue = Object.values(params.selected); 
          var n = 0; 
          selectValue.map(function (res) {
               if (!res) { 
                   n++; 
                } 
          });
           if (n == selectValue.length) {
               optionLegend.legend[0].selected[params.name] = true;
          }
          echartsWrapper.setOption(optionLegend)
      });      
      // this.myChart3.setOption(option)
    },
    //会员统计
    memberStatistics () {
      // var chartDom = document.getElementById('memberStatistics')
      // var myChart = echarts.init(chartDom)
      var echartsWrapper = this.myChart4
      var option = {
        tooltip: {
          formatter: params => {
            // console.log('params', params)
            var dotHtml =
              '<span style="display: inline-block;width: 10px;height: 10px;border: 2px solid #2890FF;background-color: #2890FF;margin-right: 8px;margin-bottom: 1px;"></span>'
            var dotHtml2 =
              '<span style="display: inline-block;width: 10px;height: 10px;border: 2px solid #E8684A;background-color: #E8684A;margin-right: 8px;margin-bottom: 1px;"></span>'
            var dotHtml3 =
              '<span style="display: inline-block;width: 10px;height: 10px;border: 2px solid #F6BD16;background-color: #F6BD16;margin-right: 8px;margin-bottom: 1px;"></span>'
            if (params[0]) {
              if (params[0].seriesName == this.$t('home.homeReport.kt')) {
                var dotHtml4 = dotHtml
              } else if (params[0].seriesName == this.$t('home.homeReport.gq')) {
                var dotHtml4 = dotHtml2
              } else if (params[0].seriesName == this.$t('home.homeReport.xf')) {
                var dotHtml4 = dotHtml3
              }
            }
            if (params[1]) {
              if (params[1].seriesName == this.$t('home.homeReport.kt')) {
                var dotHtml5 = dotHtml
              } else if (params[1].seriesName == this.$t('home.homeReport.gq')) {
                var dotHtml5 = dotHtml2
              } else if (params[1].seriesName == this.$t('home.homeReport.xf')) {
                var dotHtml5 = dotHtml3
              }
            }
            if (params[2]) {
              if (params[2].seriesName == this.$t('home.homeReport.kt')) {
                var dotHtml6 = dotHtml
              } else if (params[2].seriesName == this.$t('home.homeReport.gq')) {
                var dotHtml6 = dotHtml2
              } else if (params[2].seriesName == this.$t('home.homeReport.xf')) {
                var dotHtml6 = dotHtml3
              }
            }
            switch (params.length) {
              case 1:
                return `<div style="min-width:127px;padding:10px">
                  <div style="color: rgba(0, 0, 0, 0.45)">`+this.$t('home.homeReport.hybb')+`</div>

                  <div style="margin-top:10px;width:100%;">
                      <div style="display:flex;justify-content: space-between"><span>${dotHtml4}${params?.[0]?.seriesName}</span> <span> ${params?.[0]?.value}</span></div> 
                  <div>
                </div>`
                break

              case 2:
                return `<div style="min-width:127px;padding:10px">
                  <div style="color: rgba(0, 0, 0, 0.45)">`+this.$t('home.homeReport.hybb')+`</div>

                  <div style="margin-top:10px;width:100%;">
                      <div style="display:flex;justify-content: space-between"><span>${dotHtml4}${params?.[0]?.seriesName}</span> <span> ${params?.[0]?.value}</span></div> 
                      <div style="display:flex;justify-content: space-between"><span>${dotHtml5}${params?.[1]?.seriesName}</span> <span> ${params?.[1]?.value}</span></div> 
                  <div>
                </div>`
                break

              case 3:
                return `<div style="min-width:127px;padding:10px">
                  <div style="color: rgba(0, 0, 0, 0.45)">`+this.$t('home.homeReport.hybb')+`</div>

                  <div style="margin-top:5px;width:100%;">
                      <div style="display:flex;justify-content: space-between"><span>${dotHtml4}${params?.[0]?.seriesName}</span> <span> ${params?.[0]?.value}</span></div> 
                      <div style="display:flex;justify-content: space-between"><span>${dotHtml5}${params?.[1]?.seriesName}</span> <span> ${params?.[1]?.value}</span></div> 
                      <div style="display:flex;justify-content: space-between"><span>${dotHtml6}${params?.[2]?.seriesName}</span> <span> ${params?.[2]?.value}</span></div> 

                  <div>
                </div>`
                break
            }
          },
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          },
          color: '#000000',
          padding: 0,
          border: 0,
          backgroundColor: '#FFFFFF', //通过设置rgba调节背景颜色与透明
          borderWidth: 0,
          borderColor: 'rgba(255, 255, 255,0.3)',
          textStyle: {
            color: '#000000'
          }
        },
        legend: {
          icon: 'roundRect',
          itemHeight: 15,
          itemWidth: 15,
          type: 'scroll',
          left: 'left',
          left: '2.5%',
          itemGap: 30 //间距
        },
        grid: {
          x: 40, //距离  左边的长度
          y: 60, //距离  头部的长度
          x2: 20, // 距离  右边的长度
          y2: 75 //距离  底部的长度
          // containLabel: true //是否为虚线
        },
        dataZoom: [
          {
            type: 'inside',
            show: true,
            realtime: true,
            start: 30,
            end: 70,
            textStyle: {
              color: '#000000' //滚动条两边字体样式
            },
            handleIcon: 'image://' + require('@/assets/imgs/bf.png'),
            handleSize: '100%',
            handleStyle: {
              color: '#D9D9D9',
              borderColor: '#D9D9D9'
            },
            // 高度
            width: '100%',
            left: 'center',
            right: 'center',
            moveHandleSize: 0, //取消头部的横条
            showDetail: true, // 拖拽时候是否显示详细数值信息
            showDataShadow: true // 组件中是否显示数据阴影
          },
          {
            show: true,
            start: 0,
            end: 100 - 1500 / 31,
            textStyle: {
              color: '#000000' //滚动条两边字体样式
            },
            fillerColor: 'rgba(0, 0, 0, 0)',
            backgroundColor: '#f0f2f5',
            handleIcon: 'image://' + require('@/assets/imgs/bf.png'),
            dataBackground: {
              areaStyle: {
                color: '#ced4d9',
                opacity: 0.8
              }
            },

            handleStyle: {
              color: '#D9D9D9',
              borderColor: '#D9D9D9'
            },
            width: '95%',
            left: 'center',
            right: 'center',
            moveHandleSize: 0, //取消头部的横条
            showDetail: true, // 拖拽时候是否显示详细数值信息
            showDataShadow: true // 组件中是否显示数据阴
          }
        ],
        xAxis: {
          // 坐标字体的颜色
          axisLabel: {
            color: '#647AAA'
          },
          type: 'category',
          data: this.MembershipList?.[0],
          axisTick: {
            show: false
          }
        },
        yAxis: {
          minInterval:1,
          splitLine: {
            show: true,
            lineStyle: {
              type: 'dashed',
              color: '#cccccc'
            }
          },
          axisLabel: {
            show: true,
            textStyle: {
              color: 'rgba(0, 0, 0, 0.45)'
            },
          },
          axisLine: {
            show: false
          },
          type: 'value',
          axisTick: {
            show: false
          }
        },
        series: [
          {
            name: this.$t('home.homeReport.kt'),
            data: this.MembershipList?.[1],
            type: 'bar',
            barGap: '5%',
            barWidth: 35,
            color: '#2890FF'
          },
          {
            name: this.$t('home.homeReport.gq'),
            data: this.MembershipList?.[2],
            type: 'bar',
            barGap: '5%',
            barWidth: 35,
            color: '#E8684A'
          },
          {
            name: this.$t('home.homeReport.xf'),
            data: this.MembershipList?.[3],
            type: 'bar',
            barGap: '5%',
            barWidth: 35,
            color: '#F6BD16'
          }
        ]
      }
      echartsWrapper .clear();
      // 设置配置项
      echartsWrapper.setOption(option);
      
      // 至少保留一个图例
      echartsWrapper.on('legendselectchanged', function (params) { 
          var optionLegend = echartsWrapper.getOption(); 
          var selectValue = Object.values(params.selected); 
          var n = 0; 
          selectValue.map(function (res) {
               if (!res) { 
                   n++; 
                } 
          });
           if (n == selectValue.length) {
               optionLegend.legend[0].selected[params.name] = true;
          }
          echartsWrapper.setOption(optionLegend)
      });      
      // this.myChart4.setOption(option)
    },
    //用户消费排行
    getStore () {
      // var chartDom = document.getElementById('Store1')
      // var myChart3 = echarts.init(chartDom)
      var option={}
      console.log(this.storeList,'this.storeList')
      if(this.storeList&&this.storeList[0]&&this.storeList[0].length>0){
        option = {
          tooltip: {
            formatter: params => {
              return `<div style="min-width:124px;min-height:60px;padding:12px;">
              <div><span style="color: rgba(0, 0, 0, 0.45)">${params.name}</span><div>

              <div style="margin-top:10px;width:100%;display:flex;justify-content: space-between">
              <span>`+this.$t('home.homeReport.cjje')+`</span> <span> ￥${params.value}</span></div> 
              </div>

          </div>`
            },
            trigger: 'item',
            axisPointer: {
              type: 'shadow'
            },
            color: '#000000',
            padding: 0,
            border: 0,
            backgroundColor: '#FFFFFF', //通过设置rgba调节背景颜色与透明
            borderWidth: 0,
            borderColor: 'rgba(255, 255, 255,0.3)',
            textStyle: {
              color: '#000000'
            }
          },
          xAxis: {
            type: 'category',
            data: this.storeList?.[0],
            axisTick: {
              show: false
            },
            // 坐标字体的颜色
            axisLabel: {
              interval:0,
              rotate:20,
              // interval: 0, //强制显示过多数据
              fontSize: 12,
              color: 'rgba(0, 0, 0, 0.45)'
            }
          },
          grid: {
            x: 50, //距离  左边的长度
            y: 10, //距离  头部的长度
            x2: 20, // 距离  右边的长度
            y2: 50 //距离  底部的长度
            // containLabel: true //是否为虚线
          },
          yAxis: {
            splitLine: {
              show: true,
              lineStyle: {
                type: 'dashed',
                color: '#cccccc'
              }
            },
            axisLabel: {
              color: 'rgba(0, 0, 0, 0.45)'
              // formatter: params => {
              //   if (params >= 10000) {
              //     return params + 'w'
              //   } else if (params >= 1000) {
              //     return params + 'k'
              //   } else {
              //     return params
              //   }
              // }
            },
            type: 'value',
            axisTick: {
              show: false
            },
            axisLine: {
              show: false
            }
          },
          series: [
            {
              data: this.storeList?.[1],
              type: 'bar',
              barWidth: 44,
              color: '#2890FF'
            }
          ]
        }
      }else{
        option = {
          
          xAxis: {
            type: 'category',
            data: ['虚位以待','虚位以待','虚位以待','虚位以待','虚位以待',],
            axisTick: {
              show: false
            },
            // 坐标字体的颜色
            axisLabel: {
              interval:0,
              rotate:20,
              // interval: 0, //强制显示过多数据
              fontSize: 12,
              color: 'rgba(0, 0, 0, 0.45)'
            }
          },
          grid: {
            x: 50, //距离  左边的长度
            y: 10, //距离  头部的长度
            x2: 20, // 距离  右边的长度
            y2: 50 //距离  底部的长度
            // containLabel: true //是否为虚线
          },
          yAxis: {
            splitLine: {
              show: true,
              lineStyle: {
                type: 'dashed',
                color: '#cccccc'
              }
            },
            axisLabel: {
              color: 'rgba(0, 0, 0, 0.45)'

            },
            type: 'value',
            axisTick: {
              show: false
            },
            axisLine: {
              show: false
            }
          },
          series: [
            {
              data: [0],
              type: 'bar',
              barWidth: 44,
              color: '#2890FF'
            }
          ]
        }
      }
      

      this.myChart5.setOption(option)
    },
    //用户消费排行切换时间w
    changeTime (row) {
      switch (row) {
        case 1:
          this.storeList = this.moneyTop.week
          this.arrList_two = this.moneyTop.weekdata
          break
        case 2:
          this.storeList = this.moneyTop.month
          this.arrList_two = this.moneyTop.monthdata
          break
        case 3:
          this.storeList = this.moneyTop.year
          this.arrList_two = this.moneyTop.yeardata
          break
        case 4:
          this.storeList = this.moneyTop.all
          this.arrList_two = this.moneyTop.alldata
          break
      }
      let arr1 = new Array(10).fill({ amount: 0, name: this.$t('home.homeReport.xwyd') })
      if (this.arrList_two.length < 10) {
        arr1.forEach(item => {
          if (this.arrList_two.length < 10) {
            this.arrList_two.push(item)
          }
        })
      }
      this.getStore()
    },
    //切换活跃用户
    changeData_one (row) {
      this.dep = row
      switch (row) {
        case 1:
          this.activeList = this.activeuserData.today
          break
        case 2:
          this.activeList = this.activeuserData.yesterday
          break
        case 3:
          this.activeList = this.activeuserData.thisweek
          break
        case 4:
          this.activeList = this.activeuserData.thismonth
          break
      }
      this.activeUser()
    },
    changeData_two (row) {
      this.kep = row
      switch (row) {
        case 1:
          this.addList = this.additiondata.week
          break
        case 2:
          this.addList = this.additiondata.month
          break
        case 3:
          this.addList = this.additiondata.year
          break
      }
      this.newUsers()
    },
    //切换会员统计
    changeData_three (row) {
      this.yep = row
      switch (row) {
        case 1:
          this.MembershipList = this.Membership.week
          break
        case 2:
          this.MembershipList = this.Membership.month
          break
        case 3:
          this.MembershipList = this.Membership.year
          break
      }
      this.memberStatistics()
    },
    async getApi () {
      const res = await userIndex({
        api: 'admin.report.userIndex'
      })
      console.log('res', res)
      this.arrList = res.data.data
      this.activeuserData = res.data.data.activeuserData //活跃用户
      // this.userCount = [
      //   { name: this.$t('home.homeReport.appd'), value: res.data.data.userCount[1]?res.data.data.userCount[1].value:0 },
      //   { name: this.$t('home.homeReport.xcxd'), value: res.data.data.userCount[0]?res.data.data.userCount[0].value:0 },
      //   { name: this.$t('home.homeReport.H5d'), value: res.data.data.userCount[3]?res.data.data.userCount[3].value:0 },
      //   { name: this.$t('home.homeReport.pcsc'), value: res.data.data.userCount[2]?res.data.data.userCount[2].value:0 }
      // ]
      this.userCount = res.data.data.userCount
      this.userCount.forEach(item => {
        this.countNum.push(item.value)
      })
      for (var i = 0; i < this.countNum.length; i++) {
        if (!isNaN(this.countNum[i])) {
          this.allNum += this.countNum[i] * 1
        }
      } //获取用户总数量
      this.activeList = this.activeuserData.today
      this.moneyTop = res.data.data.moneyTop
      this.storeList = this.moneyTop.week
      this.arrList_two = this.moneyTop.weekdata
      let arr1 = new Array(10).fill({ amount: 0, name: this.$t('home.homeReport.xwyd') })
      if (this.arrList_two.length < 10) {
        arr1.forEach(item => {
          if (this.arrList_two.length < 10) {
            this.arrList_two.push(item)
          }
        })
      }
      this.additiondata = res.data.data.additiondata
      this.addList = this.additiondata.month
      this.Membership = res.data.data.Membership
      this.MembershipList = this.Membership.year
      // this.storeList[0].map((item, index) => {
      //   this.arrList_two.push({ value: this.storeList[1][index], name: item })
      // })
      this.activeUser() //活跃用户
      this.userStatistics() //用户统计
      this.newUsers() //新增用户
      this.memberStatistics() //会员统计
      this.getStore() //用户消费排行
    },
  }
}
