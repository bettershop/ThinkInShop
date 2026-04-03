import * as echarts from 'echarts'
import { mixinstest } from '@/mixins/index'
import { income } from "@/api/plug_ins/supplier";

export default {
  name: 'operatingIncome',
  mixins: [mixinstest],
  data() {
    return {
      routerList:JSON.parse(sessionStorage.getItem('tabRouter')),
      radio1:this.$t('supplier.jysy'),
      tbl: "新增用户报表",
      inputInfo: {
        date: ''
      },
      tag: 1,

      tableData: [],
      cumulative: [], // 累计排行榜
      thisWeek: [] // 本周排行榜
    }
  },
  mounted() {
    this.myChartOne = echarts.init(document.getElementById('myChart'))
    this.incomes()    
  },
  watch: {
    tag() {
      if(this.tag ==1) {
        this.tableData = this.cumulative
      } else {
        this.tableData = this.thisWeek
      }
    }
  },
  methods: {
    async incomes() {
      const res = await income({
        api: 'supplier.Admin.Supplier.Income',
        startTime: this.inputInfo.date[0] ? this.inputInfo.date[0] : '',
        endTime: this.inputInfo.date[1] ? this.inputInfo.date[1] : ''
      })
      console.log(res)
      let orderList = res.data.data.daySettlement
      //处理数据
      let report = {};
      let dateList = [];
      let orderNumList = [];
      for (let i = 0; i < orderList.length; i++) {
        dateList.push(orderList[i].date);
        orderNumList.push(orderList[i].settlementPrice);
      }
      report.dateList = dateList;
      report.orderNumList = orderNumList;
      this.cumulative = res.data.data.cumulative
      this.thisWeek = res.data.data.thisWeek
      if(this.tag == 1) {
        this.tableData = this.cumulative
      }
      this.loadLine(report);
    },

    tags(value) {
      this.tag = value
    },

    tableHeaderColor({ row, column, rowIndex, columnIndex }) {
      if (rowIndex === 0) {
        return 'background-color: #f4f7f9;'
      }
    },

    reset() {
      this.inputInfo.date = ''
    },

    // 查询
    demand() {
      this.incomes()
    },

    loadLine(data) {
      // let option = {
      //   xAxis: {
      //     type: 'category',
      //     data: data.dateList,
      //     lineStyle: {
      //       color: "#EDF1F5",
      //     },
      //   },
      //   yAxis: {
      //     type: 'value',
      //     splitLine: {
      //       lineStyle: {
      //         // 设置背景横线
      //         color: "#E9ECEF",
      //      }
      //     },
      //   },
      //   tooltip: {
      //       //鼠标悬停提示内容
      //       trigger: 'axis', // 触发类型，默认数据触发，可选为：'axis' item
      //       axisPointer: {
      //         // 坐标轴指示器，坐标轴触发有效
      //         type: "line", // 默认为直线，可选为：'line' | 'shadow'
      //         label:'cross',
      //         // show:true
      //       }, 
      //   },
      //   series: [
      //     {
      //       name: '订单数',
      //       itemStyle: {
      //           normal: {
      //               color: "#5B8FF9", //改变折线点的颜色
      //               lineStyle: {
      //                   color: "#5B8FF9", //改变折线颜色
      //               },
      //           },
      //       },
      //       data: data.orderNumList,
      //       type: 'line'
      //     }
      //   ]
      // };


      var option
      option = {
        tooltip: {
          formatter: params => {
            console.log(params)
            return `<div style="min-width:121px;padding:10px;">
                            <div><span style="color: rgba(0, 0, 0, 0.45)">${params[0].name}</span><div>

                            <div style="margin-top:10px;width:100%;display:flex;justify-content: space-between">
                            <span>${this.$t('supplier.operatingIncome.jssy')} ${params[0].value}</span></div> 
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
        toolbox: {
          feature: {
            // saveAsImage: {}
          }
        },

        grid: {
          x: 80, //距离  左边的长度
          y: 50, //距离  头部的长度
          x2: 80, // 距离  右边的长度
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
            data: data.dateList
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
            // areaStyle: {
            //   //渐变色
            //   normal: {
            //     color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            //       { offset: 0, color: '#c9e3ff' },
            //       { offset: 0.3, color: '#c9e3ff' },
            //       { offset: 1, color: '#ffffff' }
            //     ])
            //   }
            // },
            data: data.orderNumList
          }
        ]
      }
      
      this.myChartOne.setOption(option)
    },
  }
}
