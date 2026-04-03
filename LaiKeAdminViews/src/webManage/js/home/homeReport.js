import * as echarts from "echarts";
import { homeIndex } from "@/api/data";
// import { isEmpty } from 'element-ui/src/utils/util'
// import { color } from 'echarts/lib/export'

export default {
  name: "addvit-report",
  data() {
    return {
      oneday: 1,
      dayList_one: [
        {
          value: 1,
          label: this.$t("home.homeReport.zj7t"),
        },
        {
          value: 2,
          label: this.$t("home.homeReport.zj1y"),
        },
        {
          value: 3,
          label: this.$t("home.homeReport.zj1n"),
        },
        {
          value: 4,
          label: this.$t("home.homeReport.zjqb"),
        },
      ],
      twoday: 1,
      dayList_two: [
        {
          value: 1,
          label: this.$t("home.homeReport.zj7t"),
        },
        {
          value: 2,
          label: this.$t("home.homeReport.zj1y"),
        },
        {
          value: 3,
          label: this.$t("home.homeReport.zj1n"),
        },
        {
          value: 4,
          label: this.$t("home.homeReport.zjqb"),
        },
      ],
      kep: 3,
      myChart: null,
      myChart2: null,
      myChart3: null,
      myChart4: null,
      userList: [
        { value: 1048, name: "男生数量" },
        { value: 735, name: "女生数量" },
        { value: 580, name: "其他数量" },
      ],
      // arrList_one: [
      //   { value: 1048, name: 'iPhone 14 pro mlx' },
      //   { value: 735, name: '雷神911Plus' },
      //   { value: 580, name: '荣耀Magic4 至臻版手机' },
      //   { value: 484, name: '海鸥 锋芒系列' },
      //   { value: 300, name: '拉链夹克外套' },
      //   { value: 1048, name: 'Apple watch SE' },
      //   { value: 735, name: '香布蕾满印 经典老花' },
      //   { value: 580, name: '川久保玲爱心联名款' },
      //   { value: 484, name: '防滑耐磨高帮休闲帆布鞋' },
      //   { value: 300, name: '高帮帆布鞋 经典男鞋' }
      // ],
      arrList_two: [
        { value: 323234, name: "高冲冲" },
        { value: 323234, name: "熊猫" },
        { value: 323234, name: "Aftttter" },
        { value: 323234, name: "馒头" },
        { value: 323234, name: "静电" },
        { value: 323234, name: "纪希" },
        { value: 323234, name: "羽灵" },
        { value: 323234, name: "Gray" },
        { value: 323234, name: "VVv" },
        { value: 323234, name: "Shaco" },
      ],
      goodsData: [],
      volumeData: [],
      mchData: [],
      supplierCountData: [],
      topGoods: [],
      chargeMoneyData: [],
      chargeData: [],
      chargeDataList: [],
      supplierData: [],
      supplierList: [],
      supplierArr: [],
      topGoodNum: "",
      moneyInfo: [],
      moneyInfoList: [],
    };
  },
  created(){
    this.$store.dispatch("user/getSystemIcon") 
  },
  mounted() {
    this.myChart = echarts.init(document.getElementById("incomeSummary")); //创建实例

    this.myChart2 = echarts.init(document.getElementById("Market")); //创建实例

    this.myChart3 = echarts.init(document.getElementById("Store")); //创建实例

    this.myChart4 = echarts.init(document.getElementById("Supplier")); //创建实例

    this.getApi();

  
  
    // this.incomeSummary() //收益汇总
    // this.Market() //商品销量前10排行
    // this.getStore() //店铺成交金额排行
    // this.getSupplier() //供应商供货排行
  },
  methods: {
    async getApi() {
      let { entries } = Object;
      let data = {
        api: "admin.report.storeIndex",
      }
      let formData = new FormData();
      for (let [key, value] of entries(data)) {
        formData.append(key, value);
      }
      const res = await homeIndex(formData);
      console.log("res", res);
      if(res){
        let reportData = res.data.data.reportData;
        this.goodsData = reportData.goodsData;
        this.volumeData = reportData.volumeData;
        this.mchData = reportData.mchData;
        this.supplierCountData = reportData.supplierCountData;
        this.topGoods = reportData.topGoodsData.topGoods;
        this.topGoodNum = reportData.topGoodsData.total;
        this.chargeMoneyData = reportData.chargeMoneyData;
        this.chargeData = this.chargeMoneyData.weekData;
        this.chargeDataList = this.chargeMoneyData.weekDataList;
        let arr1 = new Array(10).fill({ amount: 0, name: this.$t('home.xwyd') });
        if (this.chargeDataList.length < 10) {
          arr1.forEach((item) => {
            if (this.chargeDataList.length < 10) {
              this.chargeDataList.push(item);
            }
          });
        }
        this.supplierData = reportData.supplierData;
        this.supplierList = this.supplierData.weekSupplierData;
        this.supplierArr = this.supplierData.weekSupplierDataList;
        let arr2 = new Array(10).fill({ num: 0, supplier_name: this.$t('home.xwyd') });
        if (this.supplierArr.length < 10) {
          arr2.forEach((item) => {
            if (this.supplierArr.length < 10) {
              this.supplierArr.push(item);
            }
          });
        }
        this.moneyInfo = reportData.moneyInfo;
        this.moneyInfoList = this.moneyInfo[2];
      }

      this.incomeSummary(); //收益汇总
      this.Market(); //商品销量前10排行
      this.getStore(); //店铺成交金额排行
      this.getSupplier(); //供应商供货排行
    },
    // 收益汇总 
    incomeSummary() {
      var echartsWrapper = this.myChart;

      var option = {
        tooltip: {
          formatter: (params) => {
            var dotHtml =
              '<span style="display: inline-block;width: 15px;height: 1px;border: 2px solid #2890FF;background-color: #2890FF;margin-right: 8px;margin-bottom: 3px;"></span>';
            var dotHtml2 =
              '<span style="display: inline-block;width: 15px;height: 1px;border: 2px solid #5AD8A6;background-color: #5AD8A6;margin-right: 8px;margin-bottom: 3px;"></span>';
            switch (params.length) {
              case 1:
                if (params[0].seriesName == this.$t('home.txsxf')) { // 替换硬编码文字为国际化
                  var dotHtml3 = dotHtml;
                } else {
                  var dotHtml3 = dotHtml2;
                }
                // 移除固定宽度，改为max-width自适应
                return `<div style="min-width:186px;padding:10px;max-width:100%;box-sizing:border-box;">
                        <div><span style="color: rgba(0, 0, 0, 0.45)">${params[0].name} &nbsp;` + this.$t('home.scjysj') + `</span></div>
                        <div style="margin-top:10px;width:100%;display:flex;justify-content: space-between">
                        <span>${dotHtml3}${params[0].seriesName}</span> <span> ${params[0].value}</span></div>
                    </div>`;
                break;

              case 2:
                // 移除固定宽度，改为max-width自适应
                return `<div style="min-width:186px;padding:10px;max-width:100%;box-sizing:border-box;">
                  <div><span style="color: rgba(0, 0, 0, 0.45)">${params[0].name} &nbsp;` + this.$t('home.scjysj') + `</span></div>
                  <div style="margin-top:10px;width:100%;display:flex;justify-content: space-between">
                  <span>${dotHtml}${params[0].seriesName}</span> <span> ${params[0].value}</span></div>
                  <div style="margin-top:10px;width:100%;display:flex;justify-content: space-between">
                  <span>${dotHtml2}${params[1].seriesName}</span> <span> ${params[1].value}</span></div>
                </div>`;
                break;
            }
          },
          trigger: "axis",
          axisPointer: {
            type: "shadow",
          },
          color: "#000000",
          padding: 0,
          border: 0,
          backgroundColor: "#FFFFFF",
          borderWidth: 0,
          borderColor: "rgba(255, 255, 255,0.3)",
          textStyle: {
            color: "#000000",
          },
        },

        color: ["#2db7f5", "#ff6600"],
        legend: {
          selectedMode: false,
          data: [
            {
              name: this.$t('home.txsxf'),
              itemStyle: {
                color: "#2890ff",
                borderColor: "#2890ff",
              },
            },
          ],
          icon: "roundRect",
          itemHeight: 15,
          itemWidth: 15,
          type: "scroll",
          left: "left",
          itemGap: 30,
        },

        grid: {
          left: "3%",
          right: "4%",
          bottom: "3%",
          containLabel: true,
        },
        xAxis: [
          {
            type: "category",
            boundaryGap: false,
            axisLabel: {
              show: true,
              textStyle: {
                color: "rgba(0, 0, 0, 0.45)",
              },
            },
            data: this.moneyInfoList[0],
          },
        ],
        yAxis: [
          {
            type: "value",
            axisLabel: {
              show: true,
              textStyle: {
                color: "rgba(0, 0, 0, 0.45)",
              },
            },
            splitLine: {
              lineStyle: {
                type: "dashed",
                color: "#cccccc",
              },
              show: true,
            },
          },
        ],
        series: [
          {
            name: this.$t('home.txsxf'),
            type: "line",
            stack: "Total",
            areaStyle: {},
            emphasis: {
              focus: "series",
            },
            symbol: "circle",
            color: "#c9e3ff",
            itemStyle: {
              borderColor: "#2890ff",
              borderWidth: 3.5,
            },
            lineStyle: {
              color: "#2890ff",
            },
            data: this.moneyInfoList[1],
          },
          {
            name: this.$t('home.flyj收益'), // 替换硬编码文字为国际化（建议补充对应国际化字段）
            type: "line",
            stack: "Total",
            areaStyle: {},
            emphasis: {
              focus: "series",
            },
            symbol: "circle",
            itemStyle: {
              borderColor: "#5AD8A6",
              borderWidth: 3.5,
            },
            lineStyle: {
              color: "#5AD8A6",
            },
            color: "#d6f5E9",
            data: [],
          },
        ],
      };

      // 清除并设置配置项
      echartsWrapper.clear();
      echartsWrapper.setOption(option);

      // 图例选择逻辑保留
      echartsWrapper.on("legendselectchanged", function (params) {
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
        echartsWrapper.setOption(optionLegend);
      });

      // ========== 核心：添加自适应逻辑 ==========
      // 1. 先执行一次resize确保初始化时适配
      echartsWrapper.resize();

      // 2. 监听窗口大小变化，自动调整图表尺寸
      // 防抖处理，避免频繁触发
      let resizeTimer = null;
      window.addEventListener('resize', () => {
        clearTimeout(resizeTimer);
        resizeTimer = setTimeout(() => {
          if (echartsWrapper && !echartsWrapper.isDisposed()) {
            echartsWrapper.resize(); // ECharts内置的自适应方法
          }
        }, 100);
      });

      // 3. 组件销毁时移除监听（防止内存泄漏）
      this.$once('hook:beforeDestroy', () => {
        window.removeEventListener('resize', () => { });
        clearTimeout(resizeTimer);
      });
    },
    // 商品销量前10排行 
    Market() {
      var echartsWrapper = this.myChart2;
      var option = {};

      if (this.topGoodNum > 0) {
        option = {
          title: {
            text: this.$t('home.zxssl'),
            subtext: this.topGoodNum,
            itemGap: 20,
            top: "63%", // 使用百分比适配不同容器高度
            right: "center",
            textStyle: {
              fontSize: "16.8",
              color: "rgba(0, 0, 0, 0.45)",
              fontFamily: "PingFangSC-Regular",
            },
            subtextStyle: {
              fontSize: "30",
              fontWeight: "800",
              color: "rgba(0,0,0,0.85)",
              fontFamily: "HelveticaNeue",
            },
          },
          tooltip: {
            formatter: (params) => {
              // 移除固定宽度，改用min-width + max-width自适应
              return `<div style="min-width:121px;max-width:100%;padding:10px;box-sizing:border-box;">
            <div><span style="color: rgba(0, 0, 0, 0.45)">${params.name}</span></div>
            <div style="margin-top:10px;width:100%;display:flex;justify-content: space-between">
              <span>${this.$t('home.xssl')}</span> <span> ${params.value}</span>
            </div>
          </div>`;
            },
            trigger: "item",
            axisPointer: {
              type: "shadow",
            },
            color: "#000000",
            padding: 0,
            border: 0,
            backgroundColor: "#FFFFFF",
            borderWidth: 0,
            borderColor: "rgba(255, 255, 255,0.3)",
            textStyle: {
              color: "#000000",
            },
          },
          legend: [
            {
              right: "2%", // 改用百分比适配容器宽度
              icon: "roundRect",
              orient: "vertical",
              data: this.topGoods.slice(5, 10),
              itemGap: 20,
              itemHeight: 15,
              itemWidth: 15,
              textStyle: {
                color: "rgba(0,0,0,0.85)",
                fontSize: "14",
                width: 11,
              },
              formatter: function (name) {
                if (!name) return "";
                if (name.length > 10) {
                  name = name.slice(0, 10) + "...";
                }
                return name;
              },
            },
            {
              left: "2%", // 改用百分比适配容器宽度
              icon: "roundRect",
              orient: "vertical",
              data: this.topGoods.slice(0, 5),
              itemGap: 20,
              itemHeight: 15,
              itemWidth: 15,
              textStyle: {
                color: "rgba(0,0,0,0.85)",
                fontSize: "14",
                width: 11,
              },
              formatter: function (name) {
                if (!name) return "";
                if (name.length > 10) {
                  name = name.slice(0, 10) + "...";
                }
                return name;
              },
            },
          ],
          color: [
            "#5b8ff9", "#5ad8a6", "#5d7092", "#f6bd16", "#e8684a",
            "#6dc8ec", "#9270CA", "#ff9d4d", "#269a99", "#ff99c3",
          ],
          series: [
            {
              type: "pie",
              radius: ["35%", "54%"], // 百分比半径，适配容器尺寸
              center: ["50%", "70%"],
              label: {
                show: false,
                position: "center",
              },
              itemStyle: {
                borderRadius: 0,
                borderWidth: 3,
              },
              emphasis: {
                label: {
                  show: false,
                  fontSize: "14",
                },
              },
              labelLine: {
                show: false,
              },
              data: this.topGoods,
            },
          ],
        };
      } else {
        let mydata = [
          { name: this.$t('home.xwyd'), value: 0 },
          { name: this.$t('home.xwyd'), value: 0 },
          { name: this.$t('home.xwyd'), value: 0 },
          { name: this.$t('home.xwyd'), value: 0 },
          { name: this.$t('home.xwyd'), value: 0 },
          { name: this.$t('home.xwyd'), value: 0 },
          { name: this.$t('home.xwyd'), value: 0 },
          { name: this.$t('home.xwyd'), value: 0 },
          { name: this.$t('home.xwyd'), value: 0 },
          { name: this.$t('home.xwyd'), value: 0 },
        ];
        option = {
          legend: [
            {
              right: "2%", // 改用百分比适配容器宽度
              icon: "roundRect",
              orient: "vertical",
              data: mydata.slice(5, 10),
              itemGap: 20,
              itemHeight: 15,
              itemWidth: 15,
              textStyle: {
                color: "rgba(0,0,0,0.85)",
                fontSize: "14",
                width: 11,
              },
            },
            {
              left: "2%", // 改用百分比适配容器宽度
              icon: "roundRect",
              orient: "vertical",
              data: mydata.slice(0, 5),
              itemGap: 20,
              itemHeight: 15,
              itemWidth: 15,
              textStyle: {
                color: "rgba(0,0,0,0.85)",
                fontSize: "14",
                width: 11,
              },
            },
          ],
          title: {
            text: this.$t('home.zxssl'),
            subtext: '0',
            itemGap: 20,
            top: "63%", // 百分比适配容器高度
            right: "center",
            textStyle: {
              fontSize: "16.8",
              color: "rgba(0, 0, 0, 0.45)",
              fontFamily: "PingFangSC-Regular",
            },
            subtextStyle: {
              fontSize: "30",
              fontWeight: "800",
              color: "rgba(0,0,0,0.85)",
              fontFamily: "HelveticaNeue",
            },
          },
          color: Array(10).fill("#B2BCD1"), // 简化重复颜色赋值
          series: [
            {
              type: "pie",
              radius: ["35%", "54%"], // 百分比半径
              center: ["50%", "70%"],
              label: {
                show: false,
                position: "center",
              },
              itemStyle: {
                borderRadius: 0,
                borderWidth: 3,
              },
              emphasis: {
                label: {
                  show: false,
                  fontSize: "14",
                },
              },
              labelLine: {
                show: false,
              },
              data: mydata
            }
          ]
        };
      }

      // 清除并设置配置项
      echartsWrapper.clear();
      echartsWrapper.setOption(option);

      // 至少保留一个图例的逻辑（原有逻辑保留）
      echartsWrapper.on("legendselectchanged", function (params) {
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
        echartsWrapper.setOption(optionLegend);
      });

      // ========== 核心：添加自适应逻辑 ==========
      // 1. 初始化时先执行一次resize，适配当前容器尺寸
      echartsWrapper.resize();

      // 2. 防抖处理：监听窗口大小变化，避免频繁重绘
      let resizeTimer = null;
      const resizeChart = () => {
        clearTimeout(resizeTimer);
        resizeTimer = setTimeout(() => {
          // 检查图表实例是否存在且未被销毁
          if (echartsWrapper && !echartsWrapper.isDisposed()) {
            echartsWrapper.resize(); // ECharts内置自适应方法
          }
        }, 100); // 100ms防抖延迟，平衡响应速度和性能
      };

      // 添加窗口resize监听
      window.addEventListener('resize', resizeChart);

      // 3. 组件销毁时移除监听，防止内存泄漏
      this.$once('hook:beforeDestroy', () => {
        window.removeEventListener('resize', resizeChart);
        clearTimeout(resizeTimer);
        // 销毁图表实例（可选，根据项目需求）
        if (echartsWrapper && !echartsWrapper.isDisposed()) {
          echartsWrapper.dispose();
        }
      });
    },
    // 店铺成交金额排行 
    getStore() {
      var option = {};
      // 定义自适应的grid边距（百分比）和柱形宽度（动态计算）
      const adaptiveGrid = {
        left: "5%",   // 替换固定x:70为百分比
        right: "3%",  // 替换固定x2:20为百分比
        top: "5%",    // 替换固定y:20为百分比
        bottom: "5%", // 替换固定y2:20为百分比
        containLabel: true // 确保标签不会超出grid范围
      };
      const emptyGrid = {
        left: "3%",   // 空数据时的grid百分比适配
        right: "3%",
        top: "5%",
        bottom: "5%",
        containLabel: true
      };

      if (this.chargeData && this.chargeData[0] && this.chargeData[0].length > 0) {
        option = {
          tooltip: {
            formatter: (params) => {
              // 移除固定min-width/min-height，改用自适应 + 最小尺寸兜底
              return `<div style="min-width:124px;padding:12px;max-width:100%;box-sizing:border-box;">
            <div><span style="color: rgba(0, 0, 0, 0.45)">${params.name}</span></div>
            <div style="margin-top:10px;width:100%;display:flex;justify-content: space-between">
              <span>${this.$t('home.cjje')}</span> <span> ￥${params.value}</span>
            </div>
          </div>`;
            },
            trigger: "item",
            axisPointer: {
              type: "shadow",
            },
            color: "#000000",
            padding: 0,
            border: 0,
            backgroundColor: "#FFFFFF",
            borderWidth: 0,
            borderColor: "rgba(255, 255, 255, 0.3)",
            textStyle: {
              color: "#000000",
            },
          },
          xAxis: {
            type: "category",
            data: this.chargeData[0],
            axisTick: {
              show: false,
            },
            axisLabel: {
              interval: 0,
              fontSize: 12,
              color: "rgba(0, 0, 0, 0.45)",
              // 自适应：文字过长时自动换行
              formatter: function (value) {
                return value.length > 6 ? value.slice(0, 6) + '...' : value;
              }
            },
          },
          grid: adaptiveGrid, // 使用百分比grid适配
          yAxis: {
            splitLine: {
              show: true,
              lineStyle: {
                type: "dashed",
                color: "#cccccc",
              },
            },
            axisLabel: {
              color: "rgba(0, 0, 0, 0.45)",
            },
            type: "value",
            axisTick: {
              show: false,
            },
            axisLine: {
              show: false,
            },
          },
          series: [
            {
              data: this.chargeData[1],
              type: "bar",
              // 柱形宽度改为百分比，适配容器宽度（避免固定44px在小容器溢出）
              barWidth: "20%",
              color: "#2890FF",
            },
          ],
        };
      } else {
        option = {
          xAxis: {
            type: "category",
            data: [
              this.$t('home.xwyd'),
              this.$t('home.xwyd'),
              this.$t('home.xwyd'),
              this.$t('home.xwyd'),
              this.$t('home.xwyd')
            ],
            axisTick: {
              show: false,
            },
            axisLabel: {
              interval: 0,
              fontSize: 12,
              color: "rgba(0, 0, 0, 0.45)",
              formatter: function (value) {
                return value.length > 6 ? value.slice(0, 6) + '...' : value;
              }
            },
          },
          grid: emptyGrid, // 空数据时的百分比grid适配
          yAxis: {
            splitLine: {
              show: true,
              lineStyle: {
                type: "dashed",
                color: "#cccccc",
              },
            },
            axisLabel: {
              color: "rgba(0, 0, 0, 0.45)",
            },
            type: "value",
            axisTick: {
              show: false,
            },
            axisLine: {
              show: false,
            },
          },
          series: [
            {
              data: [0, 0, 0, 0, 0], // 补全空数据，避免图表显示异常
              type: "bar",
              barWidth: "20%", // 百分比柱形宽度
              color: "#2890FF",
            },
          ],
        };
      }

      // 设置配置项
      this.myChart3.setOption(option);

      // ========== 核心：添加自适应逻辑 ==========
      const echartsWrapper = this.myChart3;

      // 1. 初始化时执行resize，适配当前容器尺寸
      if (echartsWrapper) {
        echartsWrapper.resize();
      }

      // 2. 防抖处理：监听窗口大小变化
      let resizeTimer = null;
      const resizeChart = () => {
        clearTimeout(resizeTimer);
        resizeTimer = setTimeout(() => {
          if (echartsWrapper && !echartsWrapper.isDisposed()) {
            echartsWrapper.resize(); // ECharts原生自适应方法
          }
        }, 100); // 100ms防抖，平衡性能和响应速度
      };

      // 添加窗口resize监听
      window.addEventListener('resize', resizeChart);

      // 3. 组件销毁时清理监听和定时器，防止内存泄漏
      this.$once('hook:beforeDestroy', () => {
        window.removeEventListener('resize', resizeChart);
        clearTimeout(resizeTimer);
        // 可选：销毁图表实例
        if (echartsWrapper && !echartsWrapper.isDisposed()) {
          echartsWrapper.dispose();
        }
      });
    },
    // 供应商供货排行 
    getSupplier() {
      var option = {};
      // 定义自适应的grid配置（百分比边距，适配不同容器）
      const adaptiveGrid = {
        left: "5%",   // 替换原固定x:70
        right: "3%",  // 替换原固定x2:20
        top: "5%",    // 替换原固定y:20
        bottom: "5%", // 替换原固定y2:20
        containLabel: true // 确保标签不超出grid范围
      };
      const emptyGrid = {
        left: "3%",   // 空数据时的grid适配
        right: "3%",
        top: "5%",
        bottom: "5%",
        containLabel: true
      };

      if (this.supplierList && this.supplierList[0] && this.supplierList[0].length > 0) {
        option = {
          tooltip: {
            formatter: (params) => {
              // 移除固定min-height，保留min-width兜底，添加max-width自适应
              return `<div style="min-width:124px;padding:12px;max-width:100%;box-sizing:border-box;">
            <div><span style="color: rgba(0, 0, 0, 0.45)">${params.name}</span></div>
            <div style="margin-top:10px;width:100%;display:flex;justify-content: space-between">
              <span>${this.$t('home.cjje')}</span> <span> ￥${params.value}</span>
            </div>
          </div>`;
            },
            trigger: "item",
            axisPointer: {
              type: "shadow",
            },
            color: "#000000",
            padding: 0,
            border: 0,
            backgroundColor: "#FFFFFF",
            borderWidth: 0,
            borderColor: "rgba(255, 255, 255, 0.3)",
            textStyle: {
              color: "#000000",
            },
          },
          xAxis: {
            type: "category",
            data: this.supplierList[0],
            axisTick: {
              show: false,
            },
            axisLabel: {
              interval: 0,
              fontSize: 12,
              color: "rgba(0, 0, 0, 0.45)",
              // 自适应优化：文字过长时截断，避免X轴标签重叠
              formatter: function (value) {
                return value.length > 6 ? value.slice(0, 6) + '...' : value;
              }
            },
          },
          grid: adaptiveGrid, // 使用百分比grid适配
          yAxis: {
            splitLine: {
              show: true,
              lineStyle: {
                type: "dashed",
                color: "#cccccc",
              },
            },
            axisLabel: {
              color: "rgba(0, 0, 0, 0.45)",
            },
            type: "value",
            axisTick: {
              show: false,
            },
            axisLine: {
              show: false,
            },
          },
          series: [
            {
              data: this.supplierList[1],
              type: "bar",
              barWidth: "20%", // 替换固定44px为百分比，适配容器宽度
              color: "#5AD8A6",
            },
          ],
        };
      } else {
        option = {
          xAxis: {
            type: "category",
            data: [
              this.$t('home.xwyd'),
              this.$t('home.xwyd'),
              this.$t('home.xwyd'),
              this.$t('home.xwyd'),
              this.$t('home.xwyd')
            ],
            axisTick: {
              show: false,
            },
            axisLabel: {
              interval: 0,
              fontSize: 12,
              color: "rgba(0, 0, 0, 0.45)",
              formatter: function (value) {
                return value.length > 6 ? value.slice(0, 6) + '...' : value;
              }
            },
          },
          grid: emptyGrid, // 空数据时的百分比grid适配
          yAxis: {
            splitLine: {
              show: true,
              lineStyle: {
                type: "dashed",
                color: "#cccccc",
              },
            },
            axisLabel: {
              color: "rgba(0, 0, 0, 0.45)",
            },
            type: "value",
            axisTick: {
              show: false,
            },
            axisLine: {
              show: false,
            },
          },
          series: [
            {
              data: [0, 0, 0, 0, 0], // 补全空数据，避免图表显示异常
              type: "bar",
              barWidth: "20%", // 百分比柱形宽度
              color: "#5AD8A6",
            },
          ],
        };
      }

      // 设置图表配置项
      this.myChart4.setOption(option);

      // ========== 核心：添加自适应逻辑 ==========
      const echartsWrapper = this.myChart4;

      // 1. 初始化时执行resize，适配当前容器尺寸
      if (echartsWrapper) {
        echartsWrapper.resize();
      }

      // 2. 防抖处理：监听窗口大小变化，避免频繁重绘
      let resizeTimer = null;
      const resizeChart = () => {
        clearTimeout(resizeTimer);
        resizeTimer = setTimeout(() => {
          // 检查图表实例是否有效
          if (echartsWrapper && !echartsWrapper.isDisposed()) {
            echartsWrapper.resize(); // ECharts原生自适应方法
          }
        }, 100); // 100ms防抖延迟，平衡性能和响应速度
      };

      // 添加窗口resize监听
      window.addEventListener('resize', resizeChart);

      // 3. 组件销毁时清理监听和定时器，防止内存泄漏
      this.$once('hook:beforeDestroy', () => {
        window.removeEventListener('resize', resizeChart);
        clearTimeout(resizeTimer);
        // 可选：销毁图表实例（根据项目需求选择）
        if (echartsWrapper && !echartsWrapper.isDisposed()) {
          echartsWrapper.dispose();
        }
      });
    },
    changeData(row) {
      this.kep = row;
      switch (row) {
        case 1:
          this.moneyInfoList = this.moneyInfo[0];
          break;
        case 2:
          this.moneyInfoList = this.moneyInfo[1];
          break;
        case 3:
          this.moneyInfoList = this.moneyInfo[2];
          break;
      }
      this.incomeSummary();
    },
    timeChange_one(row) {
      switch (row) {
        case 1:
          this.chargeData = this.chargeMoneyData.weekData;
          this.chargeDataList = this.chargeMoneyData.weekDataList;
          break;
        case 2:
          this.chargeData = this.chargeMoneyData.monthData;
          this.chargeDataList = this.chargeMoneyData.monthDataList;
          break;
        case 3:
          this.chargeData = this.chargeMoneyData.yearData;
          this.chargeDataList = this.chargeMoneyData.yearDataList;
          break;
        case 4:
          this.chargeData = this.chargeMoneyData.allData;
          this.chargeDataList = this.chargeMoneyData.allDataList;
          break;
      }
      let arr1 = new Array(10).fill({ amount: 0, name: this.$t('home.xwyd') });
      if (this.chargeDataList.length < 10) {
        arr1.forEach((item) => {
          if (this.chargeDataList.length < 10) {
            this.chargeDataList.push(item);
          }
        });
      }
      this.getStore();
    },
    timeChange_two(row) {
      switch (row) {
        case 1:
          this.supplierList = this.supplierData.weekSupplierData;
          this.supplierArr = this.supplierData.weekSupplierDataList;
          break;
        case 2:
          this.supplierList = this.supplierData.monthSupplierData;
          this.supplierArr = this.supplierData.monthSupplierDataList;
          break;
        case 3:
          this.supplierList = this.supplierData.yearSupplierData;
          this.supplierArr = this.supplierData.yearSupplierDataList;
          break;
        case 4:
          this.supplierList = this.supplierData.allSupplierData;
          this.supplierArr = this.supplierData.allSupplierDataList;
          break;
      }
      let arr2 = new Array(10).fill({ num: 0, supplier_name: this.$t('home.xwyd') });
      if (this.supplierArr.length < 10) {
        arr2.forEach((item) => {
          if (this.supplierArr.length < 10) {
            this.supplierArr.push(item);
          }
        });
      }
      this.getSupplier();
    },
  },
};
