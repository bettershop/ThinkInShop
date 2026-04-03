import * as echarts from 'echarts'
import { orderIndex} from '@/api/data'

export default {
  name: 'orde-report',
  data () {
    return {
      laikeCurrencySymbol:'￥',
      time_one: true,
      time_two: true,
      time_three: true,
      time_four: true,
      time_five: true,
      dep: 3,
      kep: 3,
      myChart:null,
      myChart2:null,
      orderStatus:[],//top
      obligationList:[],//待付款list
      waitList:[],//待付款List
      receList:[],//待收货List
      processedList:[],//处理中List
      appraiseList:[],//待评价List
      orderNumberList:[],//实时订单数list
      orderMoneyList:[],//订单金额
      returnList:[],//退款金额
      refundOrderData:[],//付款退款订单统计总list
      reOrderList:[],//付款/退款订单统计
      totalAmount:[],//订单总数统计总list
      allList:[],//订单总数统计list
    }
  },
  mounted () {
    this.myChart = echarts.init(document.getElementById('refundOrder')) //创建实例
    this.laikeCurrencySymbol = this.LaiKeCommon.getDefaultCurrencySymbol();
    this.myChart2 = echarts.init(document.getElementById('totalStatistics')) //创建实例
    this.getApi()
  },
  created() {
    this.laikeCurrencySymbol = this.LaiKeCommon.getDefaultCurrencySymbol();
  },
  methods: {
    refundOrder () {
      // var chartDom = document.getElementById('refundOrder')
      // var myChart = echarts.init(chartDom)
      // var option
      var echartsWrapper = this.myChart
      var option = {
        tooltip: {
          formatter: params => {
            var dotHtml =
              '<span style="display: inline-block;width: 15px;height: 1px;border: 2px solid #5B8FF9;background-color: #5B8FF9;margin-right: 8px;margin-bottom: 3px;"></span>'
            var dotHtml2 =
              '<span style="display: inline-block;width: 15px;height: 1px;border: 2px solid #E8684A;background-color: #E8684A;margin-right: 8px;margin-bottom: 3px;"></span>'
            if (params[0]) {
              if (params[0].seriesName == this.$t('ordereport.fk')) {
                var dotHtml3 = dotHtml
              } else if (params[0].seriesName == this.$t('ordereport.tk')) {
                var dotHtml3 = dotHtml2
              }
            }
            if (params[1]) {
              if (params[1].seriesName == this.$t('ordereport.fk')) {
                var dotHtml4 = dotHtml
              } else if (params[1].seriesName == this.$t('ordereport.tk')) {
                var dotHtml4 = dotHtml2
              }
            }
            switch (params.length) {
              case 1:
                return `<div style="min-width:133px;padding:10px;">
                <div><span style="color: rgba(0, 0, 0, 0.45)">${params[0].axisValue}</span></div>
                <div style="margin-top:10px;width:100%;">
                    <div style="display:flex;justify-content: space-between"><span>${dotHtml3}${params?.[0]?.seriesName}</span> <span> ${params?.[0]?.value}</span></div>
                  </div>
                </div>`
                break

              case 2:
                return `<div style="min-width:133px;padding:10px;">
                  <div><span style="color: rgba(0, 0, 0, 0.45)">${params[0].axisValue}</span></div>
                  <div style="margin-top:10px;width:100%;">
                      <div style="display:flex;justify-content: space-between"><span>${dotHtml3}${params?.[0]?.seriesName}</span> <span> ${params?.[0]?.value}</span></div>
                      <div style="display:flex;justify-content: space-between;margin:5px 0px"><span>${dotHtml4}${params?.[1]?.seriesName}</span> <span> ${params?.[1]?.value}</span></div>
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
        color: ['#5B8FF9', '#E16447'],
        legend: {
          data: [this.$t('ordereport.fk'), this.$t('ordereport.tk')],
          icon: 'roundRect',
          itemHeight: 15,
          itemWidth: 15,
          type: 'scroll',
          // orient: 'vertical',
          left: 'left',
          left: '2.5%',
          itemGap: 30 //间距
        },

        grid: {
          x: 20, //距离  左边的长度
          y: 60, //距离  头部的长度
          x2: 40, // 距离  右边的长度
          y2: 60, //距离  底部的长度
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
            data:this.reOrderList[0]
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
            name: this.$t('ordereport.fk'),
            type: 'line',
            // stack: 'Total',
            data: this.reOrderList[1]
          },
          {
            name: this.$t('ordereport.tk'),
            type: 'line',
            // stack: 'Total',
            data: this.reOrderList[2]
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
      // this.myChart.setOption(option)
    },
    totalStatistics () {
      // var chartDom = document.getElementById('totalStatistics')
      // var myChart2 = echarts.init(chartDom)
      var option = {
        tooltip: {
          formatter: params => {
            var dotHtml =
              '<span style="display: inline-block;width: 15px;height: 1px;border: 2px solid #5B8FF9;background-color: #5B8FF9;margin-right: 8px;margin-bottom: 3px;"></span>'
            return `<div style="min-width:133px;padding:10px;">
                <div><span style="color: rgba(0, 0, 0, 0.45)">${params[0].axisValue}</span></div>
                <div style="margin-top:10px;width:100%;">
                    <div style="display:flex;justify-content: space-between"><span>${dotHtml}${this.$t('ordereport.ddzs')}</span> <span> ${params?.[0]?.value}</span></div>
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
        color: ['#5B8FF9'],
        grid: {
          x: 20, //距离  左边的长度
          y: 15, //距离  头部的长度
          x2: 40, // 距离  右边的长度
          y2: 60, //距离  底部的长度
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
            data: this.allList[0]
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
            // stack: 'Total',
            data: this.allList[1]
          }
        ]
      }

      this.myChart2.setOption(option)
    },
    changeTime_one (row) {
      switch (row) {
        case 1:
          this.time_one = true
          this.obligationList =  this.orderStatus[0][0]
          break
        case 2:
          this.time_one = false
          this.obligationList =  this.orderStatus[0][1]
          break
      }
    },
    changeTime_two (row) {
      switch (row) {
        case 1:
          this.time_two = true
          this.waitList =  this.orderStatus[1][0]
          break
        case 2:
          this.time_two = false
          this.waitList =  this.orderStatus[1][1]
          break
      }
    },
    changeTime_three (row) {
      switch (row) {
        case 1:
          this.time_three = true
          this.receList =  this.orderStatus[2][0]
          break
        case 2:
          this.time_three = false
          this.receList =  this.orderStatus[2][1]
          break
      }
    },
    changeTime_four (row) {
      switch (row) {
        case 1:
          this.time_four = true
          this.appraiseList =  this.orderStatus[3][0]
          break
        case 2:
          this.time_four = false
          this.appraiseList =  this.orderStatus[3][1]
          break
      }
    },
    changeTime_five (row) {
      switch (row) {
        case 1:
          this.time_five = true
          this.processedList =  this.orderStatus[4][0]
          break
        case 2:
          this.time_five = false
          this.processedList =  this.orderStatus[4][1]
          break
      }
    },
    //切换付款/退款订单统计
    changeData (row) {
      this.dep = row
      switch (row) {
        case 1:
          this.reOrderList = this.refundOrderData.week
          break
        case 2:
          this.reOrderList = this.refundOrderData.month
          break
        case 3:
          this.reOrderList = this.refundOrderData.year
          break
      }
      this.refundOrder()
    },
    //订单总数统计
    changeData_two (row) {
      this.kep = row
      switch (row) {
        case 1:
          this.allList = this.totalAmount.week
          break
        case 2:
          this.allList = this.totalAmount.month
          break
        case 3:
          this.allList = this.totalAmount.year
          break
      }
      this.totalStatistics()
    },
    async getApi(){
      const res = await orderIndex({
        api:'admin.report.orderIndex'
      })
      console.log('res',res);
      //top
      this.orderStatus = res.data.data.orderStatus
      this.obligationList =  this.orderStatus[0][0]
      this.waitList = this.orderStatus[1][0]
      this.receList = this.orderStatus[2][0]
      this.appraiseList = this.orderStatus[3][0]
      this.processedList = this.orderStatus[4][0]

      //center
      this.orderNumberList = res.data.data.orderAmount[0]
      this.orderMoneyList = res.data.data.orderAmount[1]
      this.returnList = res.data.data.orderAmount[2]

      //bottom
      this.refundOrderData = res.data.data.refundOrderData
      this.reOrderList = this.refundOrderData.year
      this.totalAmount = res.data.data.totalAmount
      this.allList = this.totalAmount.year
      this.refundOrder()
      this.totalStatistics()


    },

  }
}
