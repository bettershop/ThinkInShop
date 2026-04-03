import * as echarts from 'echarts'
import pageData from '@/api/constant/page'
import { goodIndex } from '@/api/data'
import { mixinstest } from '@/mixins/index'

export default {
  name: 'goods-report',
  mixins: [mixinstest],
  data () {
    return {
      page: pageData.data(),
      mainData: { product_num: 0, customer_num: 0 },
      kep: 2,
      kep2: 0,
      dep: 1,
      yep: 2,
      rep: 2,
      oneday: 1,
      twoday: 1,
      myChart: null,
      myChart2: null,
      myChart3: null,
      myChart4: null,
      myChart5: null,
      commodityNum_List: [
        {
          value: 1,
          label: this.$t('goodsreport.sc')
        },
        {
          value: 2,
          label: this.$t('goodsreport.dp')
        },
        {
          value: 3,
          label: this.$t('goodsreport.gys')
        }
      ],
      commodityType_List: [
        {
          value: 1,
          label: this.$t('goodsreport.zs')
        },
        {
          value: 2,
          label: this.$t('goodsreport.yxj')
        },
        {
          value: 3,
          label: this.$t('goodsreport.qb')
        }
      ],
      tableHeight: null,
      tableData: [],
      loading: true,
      countList: [],
      goodsSalesInfo: [],
      goodsSalesList: [],
      goodsStockInfo: [],
      goodsStockInfoList: [],
      StatusList: [],
      goodsSalesInfoWithStatus: [],
      skuInfo: [],
      skuInfoList: [],
      stocksType:'month',
      goodsNumInfo:[],
      goodsNumInfoList:[],
      goodsNumInfoArr:[],
    }
  },
  created () {
    if (this.$route.params.pageSize) {
      this.pagination.page = this.$route.params.dictionaryNum
      this.dictionaryNum = this.$route.params.dictionaryNum
      this.pageSize = this.$route.params.pageSize
    }
  },
  mounted () {
    this.myChart = echarts.init(document.getElementById('commodity_num')) //创建实例

    this.myChart2 = echarts.init(document.getElementById('commodity_type')) //创建实例

    this.myChart3 = echarts.init(document.getElementById('SKU')) //创建实例

    this.myChart4 = echarts.init(document.getElementById('commodity_all')) //创建实例

    this.myChart5 = echarts.init(document.getElementById('inventory')) //创建实例

    this.getApi()
    this.getStocks()
    this.$nextTick(function () {
      this.getHeight()
    })
    window.addEventListener('resize', this.getHeight(), false)
  },
  methods: {
    async getApi () {
      const res = await goodIndex({
        api: 'admin.report.goodsIndex'
      })
      console.log('res', res)
      let arr = res.data.data
      this.countList = arr.countList
      this.goodsSalesInfo = arr.goodsSalesInfo
      this.goodsSalesList = this.goodsSalesInfo[1]
      this.goodsStockInfo = arr.goodsStockInfo
      this.goodsSalesInfoWithStatus = arr.goodsSalesInfoWithStatus
      this.StatusList = this.goodsSalesInfoWithStatus[0]
      this.skuInfo = arr.skuInfo
      this.skuInfoList = this.skuInfo[0]
      this.goodsNumInfo = arr.goodsNumInfo
      this.goodsNumInfoList = this.goodsNumInfo[1]//按月
      this.goodsNumInfoArr = this.goodsNumInfoList[0]//商城
      console.log('goodsNumInfoList',this.goodsNumInfoList);
      console.log('goodsNumInfoArr',this.goodsNumInfoArr);
      
      this.commodity_num() // 商品数量
      this.commodity_type() // 商品状态
      this.getSKU() // 商品规格
      this.commodity_all() // 商量销量汇总
    },
    async getStocks () {
      this.loading = true
      const res = await goodIndex({
        api: 'admin.report.stockRecord',
        type: this.stocksType,
        pageNo: this.dictionaryNum,
        pageSize: this.pageSize
      })
      let arr = res.data.data
      console.log('arr',arr);
      
      this.goodsStockInfoList = arr.stockInfo
      this.total = arr.stockRecord.total
      this.tableData = arr.stockRecord.data
      this.loading = false
      if (res.data.data.total < 10) {
        this.current_num = this.total
      }
      this.inventory() // 商品库存
    },
    commodity_num () {
      // var chartDom = document.getElementById('commodity_num')
      // var myChart = echarts.init(chartDom)
      // var option
      var echartsWrapper = this.myChart
      var option = {
        tooltip: {
          formatter: params => {
            var dotHtml =
              '<span style="display: inline-block;width: 8px;height: 8px;border: 2px solid #2890FF;background-color: #2890FF;margin-right: 8px;margin-bottom: 1px;"></span>'
            var dotHtml2 =
              '<span style="display: inline-block;width: 8px;height: 8px;border: 2px solid #E16447;background-color: #E16447;margin-right: 8px;margin-bottom: 1px;"></span>'
            // var dotHtml3 =
            //   '<span style="display: inline-block;width: 8px;height: 8px;border: 2px solid #F6BD16;background-color: #F6BD16;margin-right: 8px;margin-bottom: 1px;"></span>'
            // var dotHtml4 =
            //   '<span style="display: inline-block;width: 8px;height: 8px;border: 2px solid #5AD8A6;background-color: #5AD8A6;margin-right: 8px;margin-bottom: 1px;"></span>'
            var dotHtml5 =
              '<span style="display: inline-block;width: 8px;height: 8px;border: 2px solid #945FB9;background-color: #945FB9;margin-right: 8px;margin-bottom: 1px;"></span>'
            if (params[0]) {
              if (params[0].seriesName == this.$t('goodsreport.ys')) {
                var dotHtml6 = dotHtml
              } else if (params[0].seriesName == this.$t('goodsreport.th')) {
                var dotHtml6 = dotHtml2
              } else if (params[0].seriesName == this.$t('goodsreport.xz')) {
                var dotHtml6 = dotHtml5
              }
            }
            if (params[1]) {
              if (params[1].seriesName == this.$t('goodsreport.ys')) {
                var dotHtml7 = dotHtml
              } else if (params[1].seriesName == this.$t('goodsreport.th')) {
                var dotHtml7 = dotHtml2
              } else if (params[1].seriesName == this.$t('goodsreport.xz')) {
                var dotHtml7 = dotHtml5
              }
            }
            if (params[2]) {
              if (params[2].seriesName == this.$t('goodsreport.ys')) {
                var dotHtml8 = dotHtml
              } else if (params[2].seriesName == this.$t('goodsreport.th')) {
                var dotHtml8 = dotHtml2
              } else if (params[2].seriesName == this.$t('goodsreport.xz')) {
                var dotHtml8 = dotHtml5
              }
            }
            
            switch (params.length) {
              case 1:
                return `<div style="min-width:127px;padding:10px">
                  <div style="color: rgba(0, 0, 0, 0.45)">${params[0].name}&nbsp;`+this.$t('goodsreport.spsl')+`</div>

                  <div style="margin-top:10px;width:100%;">
                    <div style="display:flex;justify-content: space-between"><span>${dotHtml6}${params[0].seriesName}</span> <span> ${params[0].value}</span></div> 
                  <div>
                </div>`
                break

              case 2:
                return `<div style="min-width:127px;padding:10px">
                  <div style="color: rgba(0, 0, 0, 0.45)">${params[0].name}&nbsp;`+this.$t('goodsreport.spsl')+`</div>

                  <div style="margin-top:10px;width:100%;">
                    <div style="display:flex;justify-content: space-between"><span>${dotHtml6}${params[0].seriesName}</span> <span> ${params[0].value}</span></div> 
                    <div style="display:flex;justify-content: space-between"><span>${dotHtml7}${params[1].seriesName}</span> <span> ${params[1].value}</span></div> 
                  <div>
                </div>`
                break

              case 3:
                return `<div style="min-width:127px;padding:10px">
                  <div style="color: rgba(0, 0, 0, 0.45)">${params[0].name}&nbsp;`+this.$t('goodsreport.spsl')+`</div>

                  <div style="margin-top:5px;width:100%;">
                    <div style="display:flex;justify-content: space-between"><span>${dotHtml6}${params[0].seriesName}</span> <span> ${params[0].value}</span></div> 
                    <div style="display:flex;justify-content: space-between"><span>${dotHtml7}${params[1].seriesName}</span> <span> ${params[1].value}</span></div> 
                    <div style="display:flex;justify-content: space-between"><span>${dotHtml8}${params[2].seriesName}</span> <span> ${params[2].value}</span></div> 
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
          left: '1%',
          itemGap: 30 //间距
        },
        grid: {
          x: 85, //距离  左边的长度
          y: 50, //距离  头部的长度
          x2: 40, // 距离  右边的长度
          y2: 40 //距离  底部的长度
        },

        xAxis: {
          // 坐标字体的颜色
          axisLabel: {
            textStyle: {
              color: 'rgba(0, 0, 0, 0.45)'
            }
          },
          axisTick: {
            show: false
          },
          type: 'value',
          splitLine: {
            show: false
          }
        },
        yAxis: {
          type: 'category',
          minInterval:1,
          data:this.goodsNumInfoArr[0],
          splitLine: {
            show: false
          },
          axisLabel: {
            // 坐标字体的颜色
            // textStyle: {
            //   color: 'rgba(0, 0, 0, 0.45)'
            // }
            // formatter: params => {
            //     return params / 10000  + 'w'
            // }
          },
          axisLine: {
            show: false
          },
          axisTick: {
            show: false
          }
        },
        dataZoom: [
          {
            type: 'slider', //滑动条
            show: true,      //开启
            yAxisIndex: [0],
            left: '96%',  //滑动条位置
            // start: 30,
            // start:this.kep == 2?30:1,
            // end: this.kep == 2?60:100,
            start:this.kep == 2?30:this.kep == 1?1:90,
            end: this.kep == 2?60:this.kep == 1?100:10,
            textStyle: {
              color: '#000000', //滚动条两边字体样式

            },
            handleIcon: 'image://' + require('@/assets/imgs/bf.png'),
            handleSize: '100%',
            handleStyle: {
              color: '#D9D9D9',
              borderColor: '#D9D9D9'
            },
            moveHandleSize: 0, //取消头部的横条
            // showDetail: true, // 拖拽时候是否显示详细数值信息
            showDataShadow: true // 组件中是否显示数据阴影
          },
          {
            type: 'inside',  //内置滑动，随鼠标滚轮展示
            yAxisIndex: [0],
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
            moveHandleSize: 0, //取消头部的横条
            // showDetail: true, // 拖拽时候是否显示详细数值信息
            showDataShadow: true // 组件中是否显示数据阴
          }
        ],
        series: [
          {
            name: this.$t('goodsreport.ys'),
            type: 'bar',
            stack: 'total',
            label: {
              show: true,
              color: '#FFFFFF',
              normal: {
                show: true,
                formatter: this.zero_format()
            }
            },
            emphasis: {
              focus: 'series'
            },
            data: this.goodsNumInfoArr[1],
            color: '#2890FF',
            barWidth: 33
          },
          {
            name: this.$t('goodsreport.th'),
            type: 'bar',
            stack: 'total',
            label: {
              show: true,
              color: '#FFFFFF',
              normal: {
                show: true,
                color: '#FFFFFF',
                formatter: this.zero_format()
            }
            },
            emphasis: {
              focus: 'series'
            },
            data: this.goodsNumInfoArr[2],
            color: '#E16447',
            barWidth: 33
          },
          // {
          //   name: '下架',
          //   type: 'bar',
          //   stack: 'total',
          //   label: {
          //     show: true
          //   },
          //   emphasis: {
          //     focus: 'series'
          //   },
          //   data: [137, 132, 102, 118, 92, 168, 153],
          //   color: '#F6BD16',
          //   barWidth: 33
          // },
          // {
          //   name: '待上架',
          //   type: 'bar',
          //   stack: 'total',
          //   label: {
          //     show: true
          //   },
          //   emphasis: {
          //     focus: 'series'
          //   },
          //   data: [176, 158, 267, 147, 189, 159, 128],
          //   color: '#5AD8A6',
          //   barWidth: 33
          // },
          {
            name: this.$t('goodsreport.xz'),
            type: 'bar',
            stack: 'total',
            label: {
              show: true,
              normal: {
                show: true,
                color: '#FFFFFF',
                formatter: this.zero_format()
            }
            },
            emphasis: {
              focus: 'series'
            },
            data: this.goodsNumInfoArr[3],
            color: '#945FB9',
            barWidth: 33
          }
        ]
      }
      echartsWrapper.clear();
      // 设置配置项
      echartsWrapper.setOption(option);
      
      // 至少保留一个图例
      echartsWrapper .on('legendselectchanged', function (params) { 
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
    commodity_type () {
      // var chartDom = document.getElementById('commodity_type')
      // var myChart1 = echarts.init(chartDom)
      // var option
      var echartsWrapper = this.myChart2
      var option = {
        tooltip: {
          formatter: params => {
            switch (params.data.status) {
              case '在售':
                return `<div style="min-width:170px;padding:10px;">
                            <div><span style="color: rgba(0, 0, 0, 0.45)">${params.name}</span><div>
                            <div style="margin-top:10px;width:100%;display:flex;justify-content: space-between">
                            <span>${params.marker}&nbsp;`+this.$t('goodsreport.zssp')+`</span> <span>${params.value}</span></div> 
                            </div>

                    </div>`
                break
              case '已下架':
                return `<div style="min-width:170px;padding:10px;">
                        <div><span style="color: rgba(0, 0, 0, 0.45)">${params.name}</span><div>
                        <div style="margin-top:10px;width:100%;display:flex;justify-content: space-between">
                        <span>${params.marker}&nbsp;`+this.$t('goodsreport.yxjsp')+`</span> <span>${params.value}</span></div> 
                        </div>

                </div>`
                break
              case '所有':
                return `<div style="min-width:170px;padding:10px;">
                        <div><span style="color: rgba(0, 0, 0, 0.45)">${params.name}</span><div>
                        <div style="margin-top:10px;width:100%;display:flex;justify-content: space-between">
                        <span>${params.marker}&nbsp;`+this.$t('goodsreport.qbsp')+`</span> <span>${params.value}</span></div> 
                        </div>

                </div>`
                break
            }
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
          type: 'scroll',
          orient: 'vertical',
          left: 'left',
          itemHeight: 15,
          itemWidth: 15,
          width: 'auto',
          itemGap: 30, //间距
          textStyle: {
            color: '#647AAA',

            rich: {
              // 这里更改 字体颜色
              name: {
                fontSize: 14,
                color: '#000000'
              },
              num: {
                fontSize: 14,
                color: '#000000'
              }
            }
          },
          formatter: name => {
            let tarValue
            for (let i = 0; i < this.StatusList.length; i++) {
              if (this.StatusList[i].name == name) {
                tarValue = this.StatusList[i].value
              }
            }
            // return [`{name|${name}}: {num|${tarValue}}`].join('\n')
            // console.log('tarValue',tarValue);

            return [
              `{name|${name}} \u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0{num|${tarValue}}`
            ].join('\n')
          }
        },
        color: ['#4d71c2', '#96cb72', '#ffc55b'],
        series: [
          {
            type: 'pie',
            radius: [30, 140],
            center: ['50%', '67%'], // 控制饼图位置 1 左右 2上下
            roseType: 'area',
            itemStyle: {
              borderRadius: 8
            },

            label: {
              normal: {
                show: false
              }
            },
            data: this.StatusList
          }
        ]
      }
      if(this.StatusList[0].value==0&&this.StatusList[1].value==0&&this.StatusList[2].value==0){
        option.color=["#B2BCD1","#B2BCD1","#B2BCD1"]
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
      // this.myChart2.setOption(option)
    },
    getSKU () {
      // var chartDom = document.getElementById('SKU')
      // var myChart3 = echarts.init(chartDom)
      var option = {}
      if(this.skuInfoList&&this.skuInfoList[0]&&this.skuInfoList[0].length>0){
        option = {
          tooltip: {
            formatter: params => {
              return `<div style="min-width:124px;min-height:60px;padding:12px;">
              <div><span style="color: rgba(0, 0, 0, 0.45)">`+this.$t('goodsreport.skusl')+`</span><div>

              <div style="margin-top:10px;width:100%;display:flex;justify-content: space-between">
              <span>${params.name}</span> <span> ${params.value}</span></div> 
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
            data: this.skuInfoList[0],
            axisTick: {
              show: false
            },
            // 坐标字体的颜色
            axisLabel: {
              interval: 0, //强制显示过多数据
              fontSize: 12,
              color: 'rgba(0, 0, 0, 0.45)'
            }
          },
          // dataZoom: [
          //   {
          //     type: 'inside',
          //     show: true,
          //     realtime: true,
          //     start: 30,
          //     end: 70,
          //     textStyle: {
          //       color: '#000000' //滚动条两边字体样式
          //     },
          //     handleIcon: 'image://' + require('@/assets/imgs/bf.png'),
          //     handleSize: '100%',
          //     handleStyle: {
          //       color: '#D9D9D9',
          //       borderColor: '#D9D9D9'
          //     },
          //     // 高度
          //     width: '100%',
          //     left: 'center',
          //     right: 'center',
          //     moveHandleSize: 0, //取消头部的横条
          //     showDetail: true, // 拖拽时候是否显示详细数值信息
          //     showDataShadow: true // 组件中是否显示数据阴影
          //   },
          //   {
          //     show: true,
          //     start: 0,
          //     end: 100 - 1500 / 31,
          //     textStyle: {
          //       color: '#000000' //滚动条两边字体样式
          //     },
          //     fillerColor: 'rgba(0, 0, 0, 0)',
          //     backgroundColor: '#f0f2f5',
          //     handleIcon: 'image://' + require('@/assets/imgs/bf.png'),
          //     dataBackground: {
          //       areaStyle: {
          //         color: '#ced4d9',
          //         opacity: 0.8
          //       }
          //     },

          //     handleStyle: {
          //       color: '#D9D9D9',
          //       borderColor: '#D9D9D9'
          //     },
          //     width: '95%',
          //     left: 'center',
          //     right: 'center',
          //     moveHandleSize: 0, //取消头部的横条
          //     showDetail: true, // 拖拽时候是否显示详细数值信息
          //     showDataShadow: true // 组件中是否显示数据阴
          //   }
          // ],
          grid: {
            x: 40, //距离  左边的长度
            y: 10, //距离  头部的长度
            x2: 0, // 距离  右边的长度
            y2: 20 //距离  底部的长度
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
              //     return params/1000 + 'w'
              //   } else if (params >= 1000) {
              //     return params/1000 + 'k'
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
              data: this.skuInfoList[1],
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
            data: ['虚位以待','虚位以待','虚位以待','虚位以待','虚位以待'],
            axisTick: {
              show: false
            },
            // 坐标字体的颜色
            axisLabel: {
              interval: 0, //强制显示过多数据
              fontSize: 12,
              color: 'rgba(0, 0, 0, 0.45)'
            }
          },
          
          grid: {
            x: 40, //距离  左边的长度
            y: 10, //距离  头部的长度
            x2: 0, // 距离  右边的长度
            y2: 20 //距离  底部的长度
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
              //     return params/1000 + 'w'
              //   } else if (params >= 1000) {
              //     return params/1000 + 'k'
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
              data: [0],
              type: 'bar',
              barWidth: 44,
              color: '#2890FF'
            }
          ]
        }
      }
      

      this.myChart3.setOption(option)
    },
    //商品销量汇总
    commodity_all () {
      // var chartDom = document.getElementById('commodity_all')
      // var myChart = echarts.init(chartDom)
      // var option
      var echartsWrapper = this.myChart4
      var option = {
        tooltip: {
          formatter: params => {
            var dotHtml =
              '<span style="display: inline-block;width: 15px;height: 1px;border: 2px solid #9A67BD;background-color: #9A67BD;margin-right: 8px;margin-bottom: 3px;"></span>'
            var dotHtml2 =
              '<span style="display: inline-block;width: 15px;height: 1px;border: 2px solid #F7C122;background-color: #F7C122;margin-right: 8px;margin-bottom: 3px;"></span>'
            switch (params.length) {
              case 1:
                if (params[0].seriesName == '提现手续费') {
                  var dotHtml3 = dotHtml
                } else {
                  var dotHtml3 = dotHtml2
                }
                return `<div style="min-width:186px;padding:10px;width:186px;">
                            <div><span style="color: rgba(0, 0, 0, 0.45)">${params[0].name} &nbsp;`+this.$t('goodsreport.spxlzh')+`</span><div>

                            <div style="margin-top:10px;width:100%;display:flex;justify-content: space-between">
                            <span>${dotHtml3}${params[0].seriesName}</span> <span> ${params[0].value}</span></div> 
                            </div>

                    </div>`
                break

              case 2:
                return `<div style="min-width:186px;padding:10px;width:186px;">
                      <div><span style="color: rgba(0, 0, 0, 0.45)">${params[0].name} &nbsp;`+this.$t('goodsreport.spxlzh')+`</span><div>

                      <div style="margin-top:10px;width:100%;display:flex;justify-content: space-between">
                      <span>${dotHtml}${params[0].seriesName}</span> <span> ${params[0].value}</span></div> 
                      </div>
                      <div style="margin-top:10px;width:100%;display:flex;justify-content: space-between">
                      <span>${dotHtml2}${params[1].seriesName}</span> <span> ${params[1].value}</span></div> 
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
        legend: {
          data: [
            {
              name: this.$t('goodsreport.sczy'),
              itemStyle: {
                color: '#9A67BD',
                borderColor: '#9A67BD'
              }
            },
            {
              name: this.$t('goodsreport.qtdp'),
              itemStyle: {
                color: '#F7C122',
                borderColor: '#F7C122'
              }
            }
          ],
          icon: 'roundRect',
          itemHeight: 15,
          itemWidth: 15,
          type: 'scroll',
          left: 'left',
          left: '5%',
          itemGap: 30 //间距
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
        grid: {
          x: 20, //距离  左边的长度
          y: 60, //距离  头部的长度
          x2: 30, // 距离  右边的长度
          y2: 60, //距离  底部的长度
          containLabel: true //是否为虚线
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
            data: this.goodsSalesList[0]
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
            name: this.$t('goodsreport.sczy'),
            type: 'line',
            stack: 'Total',
            areaStyle: {},
            emphasis: {
              focus: 'series'
            },
            symbol: 'circle',
            color: '#EBE0F2',
            itemStyle: {
              //折线拐点标志的样式
              borderColor: '#9A67BD', //拐点的边框颜色
              borderWidth: 3.5
            },
            lineStyle: {
              //折线的样式
              color: '#9A67BD'
            },
            data: this.goodsSalesList[1]
          },
          {
            name: this.$t('goodsreport.qtdp'),
            type: 'line',
            stack: 'Total',
            areaStyle: {},
            emphasis: {
              focus: 'series'
            },
            symbol: 'circle',
            itemStyle: {
              //折线拐点标志的样式
              borderColor: '#F7C122', //拐点的边框颜色
              borderWidth: 3.5
            },
            lineStyle: {
              color: '#F7C122'
            },
            color: '#FEF3D2',
            data: this.goodsSalesList[2]
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
    //商品库存
    inventory () {
      // var chartDom = document.getElementById('inventory')
      // var myChart = echarts.init(chartDom)
      // var option
      var echartsWrapper = this.myChart5
      var option = {
        tooltip: {
          formatter: params => {
            var dotHtml =
              '<span style="display: inline-block;width: 10px;height: 10px;border: 2px solid #2890FF;background-color: #2890FF;margin-right: 8px;margin-bottom: 1px;"></span>'
            var dotHtml2 =
              '<span style="display: inline-block;width: 10px;height: 10px;border: 2px solid #E8684A;background-color: #E8684A;margin-right: 8px;margin-bottom: 1px;"></span>'
            var dotHtml3 =
              '<span style="display: inline-block;width: 10px;height: 10px;border: 2px solid #9A67BD;background-color: #9A67BD;margin-right: 8px;margin-bottom: 1px;"></span>'
            if (params[0]) {
              if (params[0].seriesName == this.$t('goodsreport.rksl')) {
                var dotHtml4 = dotHtml
              } else if (params[0].seriesName == this.$t('goodsreport.cksl')) {
                var dotHtml4 = dotHtml2
              } else if (params[0].seriesName == this.$t('goodsreport.yjcs')) {
                var dotHtml4 = dotHtml3
              }
            }
            if (params[1]) {
              if (params[1].seriesName == this.$t('goodsreport.rksl')) {
                var dotHtml5 = dotHtml
              } else if (params[1].seriesName == this.$t('goodsreport.cksl')) {
                var dotHtml5 = dotHtml2
              } else if (params[1].seriesName == this.$t('goodsreport.yjcs')) {
                var dotHtml5 = dotHtml3
              }
            }
            if (params[2]) {
              if (params[2].seriesName == this.$t('goodsreport.rksl')) {
                var dotHtml6 = dotHtml
              } else if (params[2].seriesName == this.$t('goodsreport.cksl')) {
                var dotHtml6 = dotHtml2
              } else if (params[2].seriesName == this.$t('goodsreport.yjcs')) {
                var dotHtml6 = dotHtml3
              }
            }
            switch (params.length) {
              case 1:
                return `<div style="min-width:139px;padding:10px">
                  <div style="color: rgba(0, 0, 0, 0.45)">`+this.$t('goodsreport.spkc')+`</div>

                  <div style="margin-top:10px;width:100%;">
                      <div style="display:flex;justify-content: space-between"><span>${dotHtml4}${params[0].seriesName}</span> <span> ${params[0].value}</span></div> 
                  <div>
                </div>`
                break

              case 2:
                return `<div style="min-width:139px;padding:10px">
                  <div style="color: rgba(0, 0, 0, 0.45)">`+this.$t('goodsreport.spkc')+`</div>

                  <div style="margin-top:10px;width:100%;">
                      <div style="display:flex;justify-content: space-between"><span>${dotHtml4}${params[0].seriesName}</span> <span> ${params[0].value}</span></div> 
                      <div style="display:flex;justify-content: space-between"><span>${dotHtml5}${params[1].seriesName}</span> <span> ${params[1].value}</span></div> 
                  <div>
                </div>`
                break

              case 3:
                return `<div style="min-width:139px;padding:10px">
                  <div style="color: rgba(0, 0, 0, 0.45)">`+this.$t('goodsreport.spkc')+`</div>

                  <div style="margin-top:5px;width:100%;">
                      <div style="display:flex;justify-content: space-between"><span>${dotHtml4}${params[0].seriesName}</span> <span> ${params[0].value}</span></div> 
                      <div style="display:flex;justify-content: space-between"><span>${dotHtml5}${params[1].seriesName}</span> <span> ${params[1].value}</span></div> 
                      <div style="display:flex;justify-content: space-between"><span>${dotHtml6}${params[2].seriesName}</span> <span> ${params[2].value}</span></div> 

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
          x: 45, //距离  左边的长度
          y: 60, //距离  头部的长度
          x2: 30, // 距离  右边的长度
          y2: 20 //距离  底部的长度
          // containLabel: true //是否为虚线
        },
        // dataZoom: [
        //   {
        //     type: 'inside',
        //     show: true,
        //     realtime: true,
        //     start: 30,
        //     end: 70,
        //     textStyle: {
        //       color: '#000000' //滚动条两边字体样式
        //     },
        //     handleIcon: 'image://' + require('@/assets/imgs/bf.png'),
        //     handleSize: '100%',
        //     handleStyle: {
        //       color: '#D9D9D9',
        //       borderColor: '#D9D9D9'
        //     },
        //     // 高度
        //     width: '100%',
        //     left: 'center',
        //     right: 'center',
        //     moveHandleSize: 0, //取消头部的横条
        //     showDetail: true, // 拖拽时候是否显示详细数值信息
        //     showDataShadow: true // 组件中是否显示数据阴影
        //   },
        //   {
        //     show: true,
        //     start: 0,
        //     end: 100 - 1500 / 31,
        //     textStyle: {
        //       color: '#000000' //滚动条两边字体样式
        //     },
        //     fillerColor: 'rgba(0, 0, 0, 0)',
        //     backgroundColor: '#f0f2f5',
        //     handleIcon: 'image://' + require('@/assets/imgs/bf.png'),
        //     dataBackground: {
        //       areaStyle: {
        //         color: '#ced4d9',
        //         opacity: 0.8
        //       }
        //     },

        //     handleStyle: {
        //       color: '#D9D9D9',
        //       borderColor: '#D9D9D9'
        //     },
        //     width: '95%',
        //     left: 'center',
        //     right: 'center',
        //     bottom: '0%',
        //     moveHandleSize: 0, //取消头部的横条
        //     showDetail: true, // 拖拽时候是否显示详细数值信息
        //     showDataShadow: true // 组件中是否显示数据阴
        //   }
        // ],
        xAxis: {
          // 坐标字体的颜色
          axisLabel: {
            color: '#647AAA'
          },
          type: 'category',
          data: [this.goodsStockInfoList],
          axisTick: {
            show: false
          },
          axisLabel: {
            show: false
          }
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
            // 坐标字体的颜色
            color: '#647AAA',
            formatter: params => {
              if (params >= 10000) {
                return params / 10000 + 'w'
              } else if (params >= 1000) {
                return params / 1000 + 'k'
              } else {
                return params
              }
            }
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
            name: this.$t('goodsreport.rksl'),
            data: [this.goodsStockInfoList[0]],
            type: 'bar',
            barGap: '5%',
            barWidth: 35,
            color: '#2890FF'
          },
          {
            name: this.$t('goodsreport.cksl'),
            data: [this.goodsStockInfoList[1]],
            type: 'bar',
            barGap: '5%',
            barWidth: 35,
            color: '#E8684A'
          },
          {
            name: this.$t('goodsreport.yjcs'),
            data: [this.goodsStockInfoList[2]],
            type: 'bar',
            barGap: '5%',
            barWidth: 35,
            color: '#9A67BD'
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
      // this.myChart5.setOption(option)
    },
    changeStatus (row) {
      switch (row) {
        case 1:
          this.StatusList = this.goodsSalesInfoWithStatus[0]
          break
        case 2:
          this.StatusList = this.goodsSalesInfoWithStatus[1]
          break
        case 3:
          this.StatusList = this.goodsSalesInfoWithStatus[2]
          break
      }
      this.commodity_type()
    },
    changeShop(row){
      this.kep2=Number(row)-1
      switch (row) {
        case 1:
          this.goodsNumInfoArr = this.goodsNumInfoList[0]//商城
          console.log('goodsNumInfoArr',this.goodsNumInfoArr);
          
          break
        case 2:
          this.goodsNumInfoArr = this.goodsNumInfoList[1]//店铺
          console.log('goodsNumInfoArr',this.goodsNumInfoArr);

          break
        case 3:
          this.goodsNumInfoArr = this.goodsNumInfoList[2]//供应商
          console.log('goodsNumInfoArr',this.goodsNumInfoArr);

          break
      }
      this.commodity_num()
    },
    changeData (row) {
      this.kep = row
      switch (row) {
        case 1:
          this.goodsNumInfoList = this.goodsNumInfo[0]//按周
          this.goodsNumInfoArr = this.goodsNumInfoList[this.kep2]//商城
          break
        case 2:
          this.goodsNumInfoList = this.goodsNumInfo[1]//按月
          this.goodsNumInfoArr = this.goodsNumInfoList[this.kep2]//商城
          break
        case 3:
          this.goodsNumInfoList = this.goodsNumInfo[2]//按年
          this.goodsNumInfoArr = this.goodsNumInfoList[this.kep2]//商城
          break
      }
      this.commodity_num()
    },
    changeData_two (row) {
      this.dep = row
      switch (row) {
        case 1:
          this.skuInfoList = this.skuInfo[0]
          break
        case 2:
          this.skuInfoList = this.skuInfo[1]
          break
        case 3:
          this.skuInfoList = this.skuInfo[2]
          break
      }
      this.getSKU()
    },
    changeData_three (row) {
      this.yep = row
      switch (row) {
        case 1:
          this.goodsSalesList = this.goodsSalesInfo[0]
          break
        case 2:
          this.goodsSalesList = this.goodsSalesInfo[1]
          break
        case 3:
          this.goodsSalesList = this.goodsSalesInfo[2]
          break
      }
      this.commodity_all()
    },
    changeData_four (row) {
      this.rep = row
      switch (row) {
        case 1:
          this.stocksType = 'week'
          this.getStocks()
          break
        case 2:
          this.stocksType = 'month'
          this.getStocks()
          break
        case 3:
          this.stocksType = 'year'
          this.getStocks()
          break
      }
      this.inventory()
    },
    getHeight () {
      this.tableHeight =
        this.$refs.tableFather.clientHeight - this.$refs.pageBox.clientHeight
    },
    //选择一页多少条
    handleSizeChange (e) {
      this.loading = true
      console.log('e', e)
      // this.current_num = e
      this.pageSize = e
      this.getStocks().then(() => {
        this.currpage = (this.dictionaryNum - 1) * this.pageSize + 1
        this.current_num =
          this.tableData.length === this.pageSize
            ? this.dictionaryNum * this.pageSize
            : this.total
        this.loading = false
      })
    },

    //点击上一页，下一页
    handleCurrentChange (e) {
      this.loading = true
      this.dictionaryNum = e
      this.currpage = (e - 1) * this.pageSize + 1
      this.getStocks().then(() => {
        this.current_num =
          this.tableData.length === this.pageSize
            ? e * this.pageSize
            : this.total
        this.loading = false
      })
    },
    zero_format(){
      return function (params) {
          if (params.value > 0) {
              return params.value;
          } else {
              return '';
          }
      }
  }
  }
}
