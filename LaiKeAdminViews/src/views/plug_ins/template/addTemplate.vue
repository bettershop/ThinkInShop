<template>
  <div id="app" v-loading="okLoading" element-loading-text="请稍后">
    <!-- <div @click="delbut">buttt</div> -->
    <!-- 组件 View-UI  Card卡片 -->
    <Card :bordered="false" dis-hover class="ivu-mt" style="width: 100%">
      <div class="diy-wrapper">
        <!-- 左侧 -->

        <div class="left" :style="{ marginRight: marginRight }">
          <div class="fanghui">
            <span @click="toUp"><i class="el-icon-arrow-left" style="text-indent: 0px;"></i> 返回</span>
          </div>
          <!-- 组件 View-UI Tabs标签页 Tabs+TabPane -->
          <Tabs ref="Tabs" @on-click="handleChange">
            <!-- 三大基础组件：基础组件、营销组件、工具组件 --> 
            <TabPane v-for="(item, index) in leftMenu" :key="index" :label="item.title" @click="handleChange2(index)">
              <!-- Vue 拖拽组件(Draggable) -->
              <draggable v-show="!displayComponentIcons" class="dragArea list-group list-group-config left-list-group" :list="item.list" :group="{ name: 'people', pull: 'clone', put: false }" :clone="cloneDog" :ref="'listGroupConfig' + index" @change="log">
                <!-- 三大分类组件对应的组件内容 如：基础组件中有轮播图、导航组、商品分类... -->
                <div class="list-group-item" v-for="(element, index) in item.list" :key="element.id" @click="addDom(element, index)">
                  <div class="list-group-item-content" :class="{ active: active_key == index }">
                    <!-- 组件小图标 -->
                    <span class="iconfont-diy" :class="element.icon"></span>
                    <!-- 组件名称 -->
                    <p>{{ element.cname }}</p> 
                  </div>
                </div>
              </draggable>
              <!-- 对应组件详细数据设置 -->
              <div class="right-box" id="rightBox">
                <div class="mConfig-item" v-for="(item, key) in rConfig" :key="key">
                  <!-- 设置组件的名称：如轮播图组件 -->
                  <div class="title-bar titleBar">{{ item.cname }}</div>
                  <div ref="formConfig" class="title-bar-config" style="overflow: scroll; box-sizing: border-box; " :style="{'height': formConfigHeigth}">
                    <!-- component vue中一个用于渲染动态组件或元素的“元组件” 和 <slot>、<template>具有类似组件的特性，也是模板语法的一部分 -->
                    <!-- 左侧设置组件数据 -->
                    <component :is="item.configName" :activeIndex="activeIndex" :num="item.num" :index="key" @config="config">
                    </component>
                  </div>
                </div>
              </div>
            </TabPane>
          </Tabs>
        </div>  
             
        <!-- 中间手机中显示的内容 -->
        <div class="diy-box-style" style="flex:2;justify-content: center;display: flex">
          <!-- 关联列表 -->
          <div class='correlation-box'  v-if="correlationList && correlationList.length >0">
                <span class="iconfont-diy icondel_1" @click="hiedList" style=" position: absolute; right: 0px;top: -15px;"></span>

                <div class="gllb" style="margin-top:10px">
                  <span >关联列表</span>
                </div>
                <div class='correlation-body'>
                  <div class="list-group-item" v-for="(pageItem, index) in correlationList" :key="index" @click="goLook(pageItem, index)">
                    <el-tooltip open-delay="1000" class="tooltip-item" effect="dark" :content="pageItem.pageName" placement="right">
                      <div class="list-group-item-content item  " :style="{border:groupActive === index?'2px solid #2890FF':''}">
                        <!-- 组件小图标 -->
                        <span class="iconfont-diy" :class="pageItem.icon"></span>
                        <!-- 组件名称 -->
                        <p :style="{color:groupActive === index?'#2890FF':''}">{{getCompName(pageItem.pageComName)}}</p>
                      </div>
                    </el-tooltip>
                  </div>
                </div>
              </div> 

          <div :class="isIOS()?'content1': 'content'" style=" position: relative;
              border-radius:  30px;
              border: 4px solid rgb(51, 51, 51);
              overflow: hidden;
              ">

            <!-- 手机框里面的 DIY内容 -->
            <div :style="{ 
                position: 'relative'
              }">
              <!-- 组件 View-UI Scroll无限滚动（超出手机框内容可以上下滚动） -->
              <Scroll ref="scroll" height="calc(100vh - 40px * 2)" style="box-sizing: border-box; overflow: hidden" :style="{
                  borderTopLeftRadius: borderRadiusTop + 'px',
                  borderTopRightRadius: borderRadiusTop + 'px',
                  borderBottomLeftRadius: borderRadiusBottom + 'px',
                  borderBottomRightRadius: borderRadiusBottom + 'px'
                }">
                <div ref="scroll2" class="scroll2" :class="tabberinfo.backType == 1 ? 'backgImg':'backgColor'">
                  <!-- Vue 拖拽组件(Draggable) --> 
                  <draggable group="people" class="dragArea list-group pre" :list="mConfig" @change="log" :style="bgColor">
                    <!-- 在左侧选中的组件可以在这里显示出来 -->
                    <div class="mConfig-item" :class="{
                        on: activeIndex == key,
                        header: headerNum == item.num
                      }" :num="item.num" v-for="(item, key) in defaultArrayVal" :key="key" @click="bindconfig(item, key)">

                      <!-- component vue中一个用于渲染动态组件或元素的“元组件” 和 <slot>、<template>具有类似组件的特性，也是模板语法的一部分 -->
                      <!-- 右侧显示组件数据 -->
                      <component ref="getComponentData" :is="item.pointName" :configData="propsObj" :index="key" :num="item.timestamp">
                      </component>
                      <!-- 右侧当前添加组件的删除按钮（可以删除已经显示在右侧的组件） -->
                      <div class="delete-box">
                        <span @click.stop="bindDelete(item, key)" class="del-but" v-if="!['classification', 'shopping_cart', 'personal_center'].includes(item.name)">
                          {{$t('template.shanchu')}}</span>
                      </div>
                    </div>
                  </draggable>
                  <!-- tabbar 组件 -->
                  <tabber v-if="tabbarShow" />
                </div>
              </Scroll>
            </div>
          </div>
        </div>
        <!-- 右侧tabBar栏组件以及页面设置 -->
        <div class="diy-box-style">
          <diy-right-info @ok='ok' @upAddLoin=" upAddLoin " @getPageImg="getPageImg" @page-selected="handlePageSelected" @changeColor="setBackColor" :tabbar="tabbar" ref="diyIntroduction"></diy-right-info>
        </div>
      </div>
    </Card>

    <!-- 组件 View-UI Modal对话框 -->
    <Modal v-model="modal1" class="add-template-model" :title="$t('template.addTemplate.txmb')" :loading="loading">
      <Form ref="formItem" :model="formItem" :rules="ruleValidate" :label-width="80">
        <!-- 模版名称 -->
        <FormItem :label="$t('template.addTemplate.mbmc')" prop="name">
          <el-input v-model="formItem.name" :placeholder="$t('template.addTemplate.qsrmbmc')"></el-input>
        </FormItem>
      </Form>
    </Modal>
  </div>
</template>

<script> 
import { diyGetInfo, addDiyInfo } from '@/api/diy/index'
import vuedraggable from 'vuedraggable'
import mPage from '@/components/mobilePage/index.js'
import mConfig from '@/components/mobileConfig/index.js'
import $ from 'jquery'
import html2canvas from 'html2canvas'
import { bannerPathList } from '@/api/plug_ins/template'
import Config from '@/packages/apis/Config'
import diyRightInfo from './diyRightInfo'
import tabber from './tabber'
import IconSwitch from '@/views/plug_ins/template/IconSwitch.vue'

import { mapState, mapMutations } from 'vuex';

export default {
  name: 'App',
  components: {
    draggable: vuedraggable,
    diyRightInfo,
    tabber,
    IconSwitch,
    ...mPage,
    ...mConfig
  },
  data() {
    return {
      displayComponentIcons: false,
      okLoading: false,
      tabbarShow: true,
      modal1: false,
      formItem: {
        name: ''
      },
      deliverList: [],
      loading: true,
      ruleValidate: {
        name: [
          {
            required: true,
            message: this.$t('template.addTemplate.qsrmbmc'),
            trigger: 'change'
          }
        ]
      },
      formConfigHeigth: '',
      isLkt: false,
      groupActive: '',
      tabbar: [],
      leftMenu: [], // 左侧菜单
      lConfig: [], // 左侧组件
      mConfig: [], // 中间组件渲染
      rConfig: [], // 右侧组件配置
      activeConfigName: '',
      propsObj: {}, // 组件传递的数据,
      activeIndex: -99, // 选中的下标
      number: 0,
      pageId: '1',
      pageName: '首页',
      pageType: '1',
      active: 0, // 0-andriod 1-ios
      ios: require('../../../assets/images/diy/ios.png'),
      android: require('../../../assets/images/diy/Android.png'),
      myUni: require('../../../assets/images/diy/myUni.png'),
      liuhai: require('../../../assets/images/liuhai.png'),
      azw: 'images/diy/az-w.png',
      iosw: 'images/diy/ios-w.png',
      xcxw: 'images/diy/xcx-w.png',
      azcur: 'images/diy/az-cur.png',
      ioscur: 'images/diy/ios-cur.png',
      xcxcur: 'images/diy/xcx-cur.png',
      // android: require('../../../assets/images/android.png'),
      bgColor: [
        {
          item: 'rgba(255, 255, 255, 0)'
        },
        {
          item: 'rgba(255, 255, 255, 0)'
        }
      ],
      height: 0,
      keyName: 'listGroupConfig0', //当前选中的大分类名称 用以区分 显示对应的组件内容
      key: 0, //选中的三大分类下标
      scaleNum: 1,
      marginRight: '0px',
      active_key: '-1', // 左侧选中的索引下标
      isConfig: false,
      tempNum: '',
      hasSearchBox: false,
      barStatusColor: [
        {
          item: 'rgba(255, 255, 255, 0)'
        },
        {
          item: 'rgba(255, 255, 255, 0)'
        }
      ],
      headerNum: '',
      dataURL: '',

      goodsEditorBase: '',

      ylsHeigth: '',
      defaultArrayVal: []
    }
  },
  beforeRouteLeave(to, from, next) {
    // 检查表单是否有未保存的修改 
    this.$store.commit('admin/mobildConfig/DELDATA')
    next()
  },
  created() {
    //左侧选择的 所有数据出处
    
    this.lConfig = this.objToArr(mPage)
    //
    this.pageId = Number(this.$route.query.id)

  },
  mounted() {

    this.isLkt = process.env.VUE_APP_IS_LKT || true
    const { query } = this.$route


    console.log('mounted~组件挂载后（页面html创建后）')

    this.getBase()
    this.scaleNum = (document.documentElement.clientWidth / 1920).toFixed(2)
    this.scaleNum = (document.documentElement.clientWidth / 1920).toFixed(2)

    this.$nextTick(async () => {
      this.okLoading = true
      try {
        await this.arraySort()
        this.rConfig = []
        if (query.status == 'pageDetail') {
          let data1 = {
            api: 'plugin.template.adminDiy.getDiyPageById',
            id: query.pageId, //页面id
            link: query.link //页面链接
          }
          const res = await this.LaiKeCommon.select(data1)
          
          if (res.data.code == 200) {
            const { data } = res
            const model = data.data.model
            const bindDiyList = data.data.bindDiyList
            if (model.page_context) {
              const page_context = JSON.parse(model.page_context)
              console.log('page_context', page_context)
              this.mConfig = page_context.mConfig

              this.$store.state.admin.mobildConfig.defaultArray = page_context.defaultArray
            }
            this.$refs.diyIntroduction.createdFrom.name = model.page_name
            this.$refs.diyIntroduction.createdFrom.link = model.link

            this.$refs.diyIntroduction.bindDiyList = bindDiyList || []

          } else {
            this.errorMsg(res.data.message)
          }
          return
        }

        if (query.status == 'editor') {
          await this.getDefaultConfig()
        } else {
          this.tabbar = JSON.parse(JSON.stringify([]))
        } 
        // 渲染自定义页面列表
        setTimeout(async () => {
          await this.$refs.diyIntroduction.getBindPageList()
          this.okLoading = false
        }, 1500)


        setTimeout(() => {
          // this.setHandleChangeSelect(0)
        }, 300)

        setTimeout(() => {
          console.log('getHeight')
          this.height = this.getHeight()
        }, 200)

        const rightBox = document.getElementById('rightBox');
        this.formConfigHeigth = rightBox.offsetHeight - 50 + 'px'
        this.ylsHeigth = this.formConfigHeigth
        console.log('formConfigHeigth', this.formConfigHeigth)
      } catch (e) {
        this.okLoading = false
      }
    })
  },
  watch: {
    mConfig: {
      handler(val) {
        console.log('监听到mConfig发生变化', val)
        this.SET_MCONFIG(val)
      },
      deep: true,
      immediate: true
    }, 
    '$store.state.admin.mobildConfig.defaultArray'(val) {
      console.log(
        '监听到$store.state.admin.mobildConfig.defaultArray发生变化',
        JSON.parse(JSON.stringify(val))
      )
    
      let keys = Object.keys(val)

     

      keys.map(key => {
        let item = val[key] 
        // 背景组件 逻辑
        if (item.name === 'homeBg') { 
          if(item.bgColor.type == 1){
            const bgColor =  item.bgColor.color
              this.bgColor =  `background: linear-gradient(90deg,${bgColor[0].item} 0%,${bgColor[1].item} 100%)`
            }else{
              const bgImgUrl =  item.bgColor.imgUrl
              if(bgImgUrl){ 
               this.bgColor = {
                backgroundImage: `url(${bgImgUrl})`, // 改用 bgImgUrl，而非 bgColor
                backgroundSize: '100% 100%',
                backgroundRepeat: 'no-repeat',
                backgroundPosition: 'center'
              };
              }else{
                  this.bgColor =''
              }
            }  
        }

       
      })
 
      

        this.defaultArrayVal = val 

        for (let i = 0; i < keys.length; i++) {
          let item = val[keys[i]]
          if (item.name === 'headerSerch') {
            this.barStatusColor = item.bgColor.color
            break
          }
        }
      /**
       * 作用：用于获取当前 选中的页面 判断是否显示 tabbar 组件
       * 只有系统页面才会显示 tabbar预览组件
       */

      const pageList = this.$store.state.admin.mobildConfig.pageList
      const pageskey = this.$store.state.admin.mobildConfig.pageskey

      if (pageList && pageList.length > 0) {
        const pageItem = pageList.find(v => {
          return pageskey == v.key
        });
        this.tabbarShow = pageItem ? pageItem.type : false
      } else {
        this.tabbarShow = false
      }

    },

    active(val) {
      console.log('监听到active发生变化', val)
      this.hasSearch()
    },

    newActive: {
      handler(val) {
        console.log('监听到newActive发生变化', val)
        this.mConfig = val
      },
      deep: true,
      immediate: true
    },
    /**
     * 作用：用于获取当前 选中的页面 判断是否显示 tabbar 组件
     * 只有系统页面才会显示 tabbar预览组件
     */
    'this.$store.state.admin.mobildConfig.pagesKey'(val) {
      const arr = this.$store.state.admin.mobildConfig.pageList
      const pageItem = arr.find(v => {
        return val == v.key
      });
      console.log('pagesKeypagesKeypagesKey', pageItem)
    },
  },
  computed: {
    ...mapState("admin/mobildConfig", ["tabBarList", 'tabberinfo', 'newActive', 'correlationList', 'labelList', 'delLinkList']),
    //手机框内容左右边距
    diyPadding() {
      //如果是安卓设置12px iOS设置13px
      if (this.active == 0) {
        return 12
      }
      return 13
    },
    //手机框内容上边距
    marginTop() {
      //如果是安卓设置40px iOS设置12px
      if (this.active == 0) {
        return 50
      }
      return 50
    },
    //手机框内容左上右上圆角
    borderRadiusTop() {
      //如果是安卓设置20px iOS设置24px
      if (this.active == 0) {
        return 20
      }
      return 24
    },
    //手机框内容右下左下圆角
    borderRadiusBottom() {
      //如果是安卓设置20px iOS设置38px
      if (this.active == 0) {
        return 20
      }
      return 38
    },
    //是否已经添加了背景组件
    isAddBg() {
      let val = this.$store.state.admin.mobildConfig.defaultArray
      let arr = Object.keys(val)
      for (let i = 0; i < arr.length; i++) {
        let item = val[arr[i]]
        if (item.name === 'homeBg') {
          return true
        }
      }
      return false
    },
    //是否已经添加了背景组件
    isAddPageTile() {
      let val = this.$store.state.admin.mobildConfig.defaultArray
      let arr = Object.keys(val) 
      for (let i = 0; i < arr.length; i++) {
        let item = val[arr[i]]
        if (item.name === 'page_title') {
          return true
        }
      }
      return false
    }
  },
  methods: {
    ...mapMutations("admin/mobildConfig", ["SET_MCONFIG", 'SET_CORRELATIONLIST', 'SET_DELLINKLIST']),
    isIOS() {
      // navigator.userAgent 包含设备和浏览器信息
      const userAgent = window.navigator.userAgent.toLowerCase(); 
      // 匹配 iPhone、iPad、iPod（覆盖 iOS 全设备）
      console.log(!/windows/.test(userAgent), '我是IOS环境')

      return !/windows/.test(userAgent);
    },
    setBackColor(obj) {

      const { color } = obj

      const bgColor = [
        {
          item: color || 'rgba(255, 255, 255, 0)'
        },
        {
          item: color || 'rgba(255, 255, 255, 0)'
        }
      ]
       this.bgColor =  `background: linear-gradient(90deg,${bgColor[0].item} 0%,${bgColor[1].item} 100%)`
    },
    // 图片压缩
    async captureUltraSmall(elSelector, targetKB = 8) {
      const element = document.querySelector(elSelector);
      console.log('element', element)
      if (!element) return null;

      // 先截取高分辨率
      const canvas = await html2canvas(element, { scale: 1 });

      // 缩小分辨率
      const scaleFactor = 0.4; // 缩小到 40%
      const tmpCanvas = document.createElement("canvas");
      tmpCanvas.width = canvas.width * scaleFactor;
      tmpCanvas.height = canvas.height * scaleFactor;
      const ctx = tmpCanvas.getContext("2d");
      ctx.drawImage(canvas, 0, 0, tmpCanvas.width, tmpCanvas.height);

      // 自动压缩到目标大小
      let quality = 0.2;
      let dataUrl = tmpCanvas.toDataURL("image/jpeg", quality);

      // 如果仍大于 targetKB，可继续降低 quality（极限 0.05）
      while ((dataUrl.length / 1024) > targetKB && quality > 0.05) {
        quality -= 0.05;
        dataUrl = tmpCanvas.toDataURL("image/jpeg", quality);
      }

      console.log("最终大小(KB)：", (dataUrl.length / 1024).toFixed(2));
      return dataUrl;
    },
    // 组件key 的生成规则
    generateKey() {
      const timestamp = new Date().getTime() * 10; // 原始时间戳（数字）
      const timestampStr = `${timestamp}`;

      // 关键：在时间戳中间插入 "_"，彻底打断连续数字
      const splitIndex = 6; // 拆分位置（可自定义，只要在中间即可）
      const brokenTimestamp = `${timestampStr.slice(0, splitIndex)}_${timestampStr.slice(splitIndex)}`;

      // 生成随机后缀（可选，确保 key 唯一）
      const randomLetter = String.fromCharCode(97 + Math.floor(Math.random() * 26));

      // 最终 key：前缀 + 打断后的时间戳 + 后缀（无连续数字主体）
      return `L_${brokenTimestamp}_${randomLetter}`;
    },
    handlePageSelected(item) {
      const tabsNav = document.querySelector('.ivu-tabs-nav-scroll');
      // // 判断是否显示组件图标
      if (item.page_name === '我的' || item.page_name === '分类' || item.page_name === '购物车') {
        Object.keys(this.$store.state.admin.mobildConfig.defaultArray).forEach(key => {
          const currentAttr = this.$store.state.admin.mobildConfig.defaultArray[key];
          if (['classification', 'shopping_cart', 'personal_center'].includes(currentAttr.pointName)) {
            console.log(currentAttr)
            currentAttr.num = currentAttr.timestamp
            this.bindconfig(currentAttr, 0)

          }
        })

        this.displayComponentIcons = true;

        this.formConfigHeigth = (Number(this.ylsHeigth.slice(0, this.ylsHeigth.length - 2)) + Number(170)) + 'px'

        //触发对应组件的点击事件 下方展示对应的组件配置项目
      } else {

        const currentAttr = this.$store.state.admin.mobildConfig.defaultArray;

        if (Object.keys(currentAttr).length > 0) {
          const key = Object.keys(currentAttr)[0]
          const Obj = JSON.parse(JSON.stringify(currentAttr[key]))
          Obj.num = Obj.timestamp
          Obj.name = Obj.pointName
          this.setLeftSelect(Obj.pointName)
          this.bindconfig(Obj, 0)
          console.log('')
        }

        this.displayComponentIcons = false;
        this.formConfigHeigth = this.ylsHeigth
      }

      // 隐藏 '基础组件'、‘营销组件’、‘工具组件’
      if (tabsNav) {
        tabsNav.style.display = this.displayComponentIcons ? 'none' : 'block';
      }
    },

    hiedList() {
      this.groupActive = ''
      this.SET_CORRELATIONLIST([])
    },
    getCompName(compType) {
      return this.$enums.DIY.compName.getCompNameLabel(compType, this)

    },
    goLook(pageItem, index) {
      console.log(pageItem)
      this.groupActive = index
      this.$refs.diyIntroduction.queryPage(pageItem, false)

      const mConfig = this.$store.state.admin.mobildConfig.mConfig 

      let pageIndex = -1;
      let currentIndex = 0;
      let obj = {}
      mConfig.forEach((item) => {
        if (item.num == pageItem.pageComKey) {
          pageIndex = currentIndex
          obj = JSON.parse(JSON.stringify(item))

        }
        currentIndex++
      })
      if (pageIndex >= 0) {
        console.log('obj', obj)
        obj.name = obj.pointName
        this.bindconfig(obj, pageIndex)
      }
    },
    toUp() {
      this.$store.commit('admin/mobildConfig/DELDATA')
      this.$router.push('/plug_ins/template/divTemplate')
    },
    getImageUrl(path) {
      try {
        return require(`@/assets/${path}`) // 根据你的项目结构调整前缀
      } catch (e) {
        console.error('图片加载失败:', path, e)
        // 可增加默认的图片
        return ''
      }
    },
    /**
     * 三大分类组件 切换
     * @param {Number} index 选择的下标 0基础组件 1营销组件 2工具组件
     */
    handleChange2(index) {
      //选中的三大分类下标
      this.key = index
      //当前选中的大分类名称 用以区分 显示对应的组件内容
      this.keyName = 'listGroupConfig' + index
      //动态设置左侧组件详细数据内容高度
      setTimeout(() => {
        this.height = this.getHeight()
      }, 200)
    },
    /**
     * 动态计算左侧组件详细数据内容高度
     */
    getHeight() {
      //获取左侧模块总高度
      let wrapHeight = $('.diy-wrapper').height()
      //三大分类nav高度
      let navHeight = 44
      //大分类对应的组件列表高度
      let height = $(this.$refs[this.keyName][0].$el).height()
      //组件标题高度
      let titleHeight = 50
      //计算左侧剩余可显示高度（用于动态设置组件详细数据内容高度）
      let mobileConfigHeight =
        wrapHeight - height - titleHeight - 20 - navHeight

      return mobileConfigHeight + 'px'
    },
    getBase() {
      this.goodsEditorBase = Config.baseUrl
    },
    upAddLoin() {
      this.okLoading = true
    },
    // 主题保存接口
    async ok() {
      try {
        this.okLoading = true
        let val = this.$refs['diyIntroduction'].pageList
        const arr = JSON.parse(JSON.stringify(val))
        let pageList = []
        arr.forEach((f, ind) => {
          if (true) {
            let attr = []
            if (f.defaultArray) {
              attr = JSON.parse(JSON.stringify(f.defaultArray))
            } else if (f.page_context) {
              let { defaultArray } = JSON.parse(f.page_context)
              attr = JSON.parse(JSON.stringify(defaultArray))
            }
            for (let key in attr) {
              const currentAttr = attr[key];
              // 确定要访问的配置（swiper/menu/adv）
              let targetConfig;
              if (currentAttr.swiperConfig) {
                targetConfig = currentAttr.swiperConfig;
              } else if (currentAttr.menuConfig) {
                targetConfig = currentAttr.menuConfig;
              } else if (currentAttr.advConfig) {
                targetConfig = currentAttr.advConfig;
              }
              if (!targetConfig || !Array.isArray(targetConfig.list)) {
                continue; // 跳过无效配置
              }
              targetConfig.list.forEach(listItem => {
                listItem.info.forEach(v => {
                  if (v.unit) {
                    pageList.push({
                      link: key,
                      pageId: f.pagesId,
                      unit: v.unit,
                      lodValue: v.lodValue,
                      value: v.value
                    })
                  }
                })
              })
            }
          }
        })

        pageList.push(...this.delLinkList)

        this.dataURL = await this.captureUltraSmall('.scroll2')

        if (this.tabBarList.length == 0) {
          this.warnMsg('至少需要一条导航条页面信息')
          return
        }
        const pageIndex = this.tabBarList.findIndex(v => !v.page_name || (!v.selectedIconPath || !v.iconPath))
        console.log(pageIndex, 'pageIndex')
        if (pageIndex >= 0) {
          this.warnMsg('导航图标或导航名称不能为空！')
          this.$nextTick(() => {
            this.$refs.diyIntroduction.getTabbarElement(pageIndex, 'tabbarBox')
          })
          return
        }

        let theme_type = ''
        if (this.isLkt) {
          theme_type = this.$route.query.typeIndex
        } else {
          theme_type = 2
        }

        addDiyInfo({
          api: 'plugin.template.AdminDiy.AddOrUpdateDiy',
          id: this.$route.query.id, //主题id
          theme_type: theme_type, // 主题类型 1 系统主题  2 自定义主题
          theme_type_code: this.$refs.diyIntroduction.createdFrom.theme_type_code, //主题类别id
          // name: this.formItem.name,
          name: this.$refs.diyIntroduction.createdFrom.name,
          cover: this.dataURL,
          value: JSON.stringify(val),
          tabBar: JSON.stringify(this.tabBarList), //tabber数组
          tabberinfo: JSON.stringify(this.tabberinfo), //tabber 数组信息数据配置
          createdFrom: JSON.stringify(this.createdFrom || ''), // diy 简介
          pageInfo: JSON.stringify(pageList) // 用于绑定和解绑
        }).then(res => {
          this.okLoading = false
          if (res.data.code == '200') {
            this.$message({
              message: this.$t('template.cg'),
              type: 'success',
              offset: 100
            })
          } else {
            this.warnMsg(res.data.message)
          }
        })
      } catch (e) {
        console.error('保存异常:', error)
      } finally {
        this.okLoading = false
      }

    },
    hasSearch() {
      setTimeout(() => {
        let val = this.getMobileConfigData()
        let arr = Object.keys(val)
        if (!arr.length) {
          return (this.hasSearchBox = false)
        }
        let item = val[arr[0]]
        if (item.name === 'headerSerch') {
          this.headerNum = item.timestamp
          this.barStatusColor = item.bgColor.color
          if (!this.active) {
            return (this.hasSearchBox = false)
          }
          return (this.hasSearchBox = true)
        } else {
          return (this.hasSearchBox = false)
        }
      }, 50)
    },
    changeMobile(index) {
      this.active = index
    },
    onSettingClick() {
      window.parent.postMessage(1)
    },
    handleChange(name) {

      this.active_key = '-1'

      console.log(222222, name, this.tempNum, this.isConfig)

      this.key = name
      
      this.keyName = 'listGroupConfig' + name

      setTimeout(() => {
        this.height = this.getHeight()
      }, 200)
      this.rConfig = []

      if (this.tempNum) {
        this.$store.commit('admin/mobildConfig/DELETEARRAY', {
          num: this.tempNum
        })
      }

      // if (!this.isConfig) {
      // this.setHandleChangeSelect(name)
      // }
      // this.isConfig = false
    },
    // tab 切换是选中左侧标签和右边的组件
    setHandleChangeSelect(key) {
      this.active_key = 0 
      let item = this.leftMenu[key].list[0]
      this.activeConfigName = item.name
      for (let i = 0; i < this.mConfig.length; i++) {
        if ( (this.mConfig[i] &&  item) && this.mConfig[i].name == item.name) {
          this.activeIndex = i
          let tempItem = JSON.parse(JSON.stringify(this.mConfig[i]))
          this.rConfig = []
          this.rConfig.push(tempItem)
          this.$store.commit('admin/mobildConfig/SETCONFIGNAME', item.name)
          return false
        }
      }

      let timestamp = this.generateKey()
      this.tempNum = item.num = `${timestamp}`
      let tempItem = JSON.parse(JSON.stringify(item))
      this.rConfig = []
      this.rConfig.push(tempItem)
      this.$store.commit('admin/mobildConfig/SETCONFIGNAME', item.name)
      this.$store.commit('admin/mobildConfig/ADDARRAY', {
        num: item.num,
        val: item.data().defaultConfig
      })
    },
    // 点击显示相应的配置
    bindconfig(item, index) { 
      // 增加补丁  不知道哪里 出了问题  有时候 item.num 会没有值
      if(!item.num){
          item.num = item.timestamp
      }
      console.log('bindconfig', item, index)
      this.rConfig = []
      let tempItem = JSON.parse(JSON.stringify(item))
      this.rConfig.push(tempItem)
      this.activeIndex = index
      this.$store.commit('admin/mobildConfig/SETCONFIGNAME', item.pointName) 
      this.isConfig = true
      this.setLeftSelect(item.pointName)
      this.rConfig = []
      this.rConfig.push(tempItem)
    },
    // 选中左侧标签
    setLeftSelect(name) {
      for (let i = 0; i < this.leftMenu.length; i++) {
        for (let j = 0; i < this.leftMenu[i].list.length; j++) {
          let item = this.leftMenu[i].list[j]
          if (item) {
            if (name === item.name) { 
              if (i != this.key) {
                this.$refs.Tabs.handleChange(i)
              }
              this.active_key = j
              break
            }
          } else {
            break
          }
        }
      }
    },
    // 组件删除
    bindDelete(item, key) { 
      console.log('bindDelete', item, key)
      if (item.name === 'homeBg') {
        const bgColor = [
          {
            item: 'rgba(255, 255, 255, 0)'
          },
          {
            item: 'rgba(255, 255, 255, 0)'
          }
        ]
         this.bgColor = `background: linear-gradient(90deg,${bgColor[0].item} 0%,${bgColor[1].item} 100%)`
      } 

      this.rConfig.splice(0, 1)
 
      this.deliverList.push({ dleLink: item.timestamp })
      
      console.log('this.deliverList', this.deliverList)
      this.SET_DELLINKLIST(this.deliverList)
      
     const keys = Object.keys(JSON.parse(JSON.stringify(this.defaultArrayVal)) || {})
      // 找谁被删除了,并保留当前索引位置
      let  index = keys.findIndex(k => k == item.timestamp )
      
      // 删除的最后一个
      if (keys.length == index+1) {
        index--
      }

      // 删除手机区域显示组件
      this.$store.commit('admin/mobildConfig/DELETEARRAY', { num: item.timestamp }) 
      delete this.defaultArrayVal[item.timestamp]
       
      this.rConfig = []
      this.isConfig = true

      let nowItem = this.defaultArrayVal[key]

      // 选中左侧标签
      if (nowItem && nowItem.pointName) {
        this.setLeftSelect(nowItem.pointName || '')
      }
      let tempItem = JSON.parse(JSON.stringify(nowItem || {}))
      this.rConfig.push(tempItem)

      
      const values = Object.values(this.defaultArrayVal);  
      // 步骤2：获取要选中的下一个对象，如果没有则选中上一个对象
      const fifthObj = values[index];   
      // 触发 对象点击事件
      console.log('fifthObj', fifthObj)
      if(Object.keys(fifthObj || {}).length){
         this.bindconfig(fifthObj,   fifthObj.num)
      }
      
      this.hasSearch()
    },
    // 组件返回
    config(data) {
      let propsObj = this.propsObj
      propsObj.data = data
      propsObj.name = this.activeConfigName
    },
    // 对象转数组
    objToArr(data) {
      let obj = Object.keys(data)
      let m = obj.map(key => data[key])
      return m
    },
    // 组件添加
    addDom(item, key = 0) {
      this.active_key = key
      let timestamp = this.generateKey()
      item.num = `${timestamp}`
      this.activeConfigName = item.name

      if (this.tempNum) {
        this.$store.commit('admin/mobildConfig/DELETEARRAY', {
          num: this.tempNum
        })
      }  

      if (this.isAddPageTile && item.defaultName === 'c_page_title') {
        return this.warnMsg('页面标题组件只能添加一个，请勿重复添加')
      } else if (this.isAddBg && item.defaultName === 'homeBg') {
        return this.warnMsg('背景组件只能添加一个，请勿重复添加')
      } else {
        let tempItem = JSON.parse(JSON.stringify(item))
        this.rConfig = []
        if (!Array.isArray(this.mConfig) || !this.mConfig) {
          this.mConfig = []
        }
        this.mConfig.push(tempItem)
        this.rConfig.push(tempItem)
        this.activeIndex = this.mConfig.length - 1
        console.log('this.rConfig', this.rConfig)
        // 保存组件名称
        this.$store.commit('admin/mobildConfig/SETCONFIGNAME', item.name)
        // 保存默认组件配置
        this.$store.commit('admin/mobildConfig/ADDARRAY', {
          num: item.num,
          val: item.data().defaultConfig
        })

        setTimeout(() => {
          let dom = $(this.$refs.scroll.$el).find('.pre')
          console.log(dom)
          let scrollHeight = dom.prop('scrollHeight')
          let height = dom.height()

          dom.scrollTop(scrollHeight - height)
        }, 100)
      }
      this.hasSearch()
    },
    log: function (evt) {
      // 中间拖拽排序
      if (evt.moved) {
        console.log(evt.moved)
        this.$store.commit('admin/mobildConfig/defaultArraySort', evt.moved)
        this.hasSearch()
      }
      // 从左向右拖拽排序
      if (evt.added) {
        if (this.tempNum) {
          this.$store.commit('admin/mobildConfig/DELETEARRAY', {
            num: this.tempNum
          })
        }
        let data = evt.added.element
        let obj = {}
        let timestamp = this.generateKey()
        data.num = `${timestamp}`
        this.activeConfigName = data.name
        if ( this.isAddPageTile &&  data.defaultName === 'c_page_title') {
          return this.warnMsg('页面标题组件只能添加一个，请勿重复添加')
        } else if (this.isAddBg && data.defaultName === 'homeBg') {
          return this.warnMsg('背景组件只能添加一个，请勿重复添加')
        } else {
          try {
            let tempItem = JSON.parse(JSON.stringify(data))
            this.rConfig = []

            this.rConfig.push(tempItem)
            // 保存组件名称
            console.log('保存组件名称')
            this.$store.commit('admin/mobildConfig/SETCONFIGNAME', data.name)
            this.$store.commit('admin/mobildConfig/defaultArraySort', evt.added)
          } catch (e) {
            console.log(e)
          }
        }
        this.hasSearch()
      }
    },
    // 数组排序
    async arraySort() {
      let tempArr = []
      // 基础组件
      let basis = {
        title: this.$t('diy.jczj.title'),
        list: []
      }
      // 营销组件
      let marketing = {
        title: this.$t('diy.yxzj.title'),
        list: []
      }
      // 工具组件
      let tool = {
        title: this.$t('diy.gzzj.title'),
        list: []
      }
      let info = null
      info = {
        mch: 1,
        go_group: 1,
        seconds: 1
      }
      const url_list1 = await bannerPathList({ 
        api: 'plugin.template.AdminDiy.BannerPathList',
        type: 2
      })
      const url_list2 = await bannerPathList({ 
        api: 'plugin.template.AdminDiy.BannerPathList',
        type: 1
      })
      const url_list3 = await bannerPathList({ 
        api: 'plugin.template.AdminDiy.BannerPathList',
        type: 3
      })
      this.$store.commit(
        'admin/mobildConfig/SET_PRODUCT_LINKS',
        url_list1.data.data.list
      )
      this.$store.commit(
        'admin/mobildConfig/SET_FENLEI_LINKS',
        url_list2.data.data.list
      )
      this.$store.commit(
        'admin/mobildConfig/SET_DIANPU_LINKS',
        url_list3.data.data.list
      )
      console.log('this.lConfig', this.lConfig.map(v=>v.type == 2?v:''))
      this.lConfig.map((el, index) => {
        if (el.type == 0) {
          basis.list.push(el)
        }
        if (el.type == 1) {
          // 插件全部都放影响组件里面
          if (el.defaultName === 'homeStore') {
            if (info.mch && info.mch == 1) {
              marketing.list.push(el)
            }
            return false
          }

          if (el.defaultName === 'combination') {
            if (info.go_group && info.go_group == 1) {
              marketing.list.push(el)
            }
            return false
          }

          if (el.defaultName === 'seckill') {
            if (info.seconds && info.seconds == 1) {
              marketing.list.push(el)
            }
            return false
          }

          marketing.list.push(el)
        }

        if (el.type == 2 && ![ '广告', '全部分类', '购物车', '个人中心'].includes(el.cname)) {
          console.log('工具组件', el)
          tool.list.push(el)
        }
      }) 
      tempArr.push(basis, marketing, tool)
      this.leftMenu = tempArr
      console.log('this.leftMenu', this.leftMenu)
    },
    cloneDog(data) {
      if (this.tempNum) {
        this.$store.commit('admin/mobildConfig/DELETEARRAY', {
          num: this.tempNum
        })
      }
      
      if (this.isAddPageTile &&   data.defaultName === 'c_page_title') {
        return this.warnMsg('页面标题组件只能添加一个，请勿重复添加')
      } else if (this.isAddBg && data.defaultName === 'homeBg') {
        return this.warnMsg('背景组件只能添加一个，请勿重复添加')
      }
     
      return {
        ...data
      }
    },

    async getPageImg(callback) {

      let dataURL = await this.captureUltraSmall('.scroll2')

      callback(dataURL)

    },
    // 通过页面的方式来获取提交的数据, 曲线救国
    getMobileConfigData() {
      let obj = {}
      let val = this.$store.state.admin.mobildConfig.defaultArray
      let isTop = true
      $('.mConfig-item').each((index, element) => {
        let num = $(element).attr('num')
        if (num) {
          obj[num] = val[num]
          if (obj && obj[num].name !== 'homeBg' && obj[num].name !== 'headerSerch') {
            isTop = false
          }
          if (obj[num].name === 'headerSerch') {
            obj[num]['isTop'] = isTop
            isTop = false
          }
        }
      })
      return obj
    },
    delbut() {
      this.$store.commit('admin/mobildConfig/DELETEARRAY', {
        num: 'L_176162_21786580_t'
      })
    },
    returnDefaultArray(defaultArray, num) { 
      let obj = {}
      for (let key in defaultArray) {
        if (key == num) {
          obj = defaultArray[key]
        }
      } 
      return obj || defaultArray[num]
    },
    // 获取默认配置
    async getDefaultConfig() {
      let { data } = await diyGetInfo({
        // api: 'plugin.adminDiy.getDiyList',
        api: 'plugin.template.AdminDiy.GetDiyList',
        id: this.pageId,

      })

      // 第一个页面
      this.formItem.name = data.data.list[0].name
      let obj = {}
      let tempARR = []
      let newArr = this.objToArr(JSON.parse(data.data.list[0].value || '[]'))
      console.log('newArr', newArr)
      console.log('lConfig', this.lConfig)
      // 渲染 tabbar组件

      this.tabbar = JSON.parse(data.data.list[0].tab_bar || '[]').map((v, i) => {
        this.$set(v, 'page_name', v.page_name || v.text)
        this.$set(v, 'defaultArray', newArr.find(item => item.key === v.key).defaultArray || {})
        this.$set(v, 'mConfig', newArr.find(item => item.key === v.key).mConfig || {})
        return v
      })
      if (data.data.list[0].tabber_info) {
        this.$refs.diyIntroduction.tabberInfo = JSON.parse(data.data.list[0].tabber_info)
      }
      console.log('this.tabbar', this.tabbar)
      let arr = [newArr[0]]
       
      try{ 
            arr.map((el, index) => {
              if (el && el.mConfig) {
                el.mConfig.forEach(v => {
                  this.lConfig.map((item, j) => { 
                    if ((v && item) && v.name == item.name) {
                      item.num = v.num
                      let tempItem = JSON.parse(JSON.stringify(item))
                      tempARR.push(tempItem)
                      obj[v.num] = v
                      // 保存默认组件配置
                      this.$store.commit('admin/mobildConfig/ADDARRAY', {
                        num: v.num,
                        val: this.returnDefaultArray(el.defaultArray, v.num)
                      })
                    }
                  })
                })
 
                 if (el.name === 'homeBg') { 
                    if(el.bgColor.type == 1){
                      const bgColor =  el.bgColor.color
                        this.bgColor =  `background: linear-gradient(90deg,${bgColor[0].item} 0%,${bgColor[1].item} 100%)`
                      }else{
                        const bgImgUrl =  el.bgColor.imgUrl
                        if(bgImgUrl){ 
                        this.bgColor = {
                          backgroundImage: `url(${bgImgUrl})`, // 改用 bgImgUrl，而非 bgColor
                          backgroundSize: '100% 100%',
                          backgroundRepeat: 'no-repeat',
                          backgroundPosition: 'center'
                        };
                        }else{
                            this.bgColor =''
                        }
                      }  
                   }
              }
            })
      }catch(e){
        console.error(e)
      }

      this.$nextTick(() => {
        console.log('父节点钩子')

        const { name, remark, theme_type, theme_dict_code } = data.data.list[0]
        const obj = this.labelList.find(v => v.id == theme_dict_code)

        this.$refs.diyIntroduction.createdFrom = {
          name: name,
          remark: remark,
          theme_type_code: theme_dict_code,
          theme_type_name: obj?.name || ''
        }
        this.$refs.diyIntroduction.themeType = theme_type
      })

      setTimeout(() => {
        console.log('getHeight')
        this.height = this.getHeight()
      }, 200)
    },

  }
}
</script>
<style scoped lang="less">
     /deep/ .box {
        width: 100%;
        word-wrap: break-word;        /* 兼容旧浏览器，允许长单词内换行 */
        word-break: break-all;        /* 允许任意位置换行（适合中英文混合） */
        white-space: normal;          /* 恢复默认换行（取消 nowrap 强制不换行） */
        overflow-wrap: break-word;  
        img {
         width: 100%;
        }
    }
   
    /deep/.ql-syntax {
        width: 100%;
        word-wrap: break-word;        /* 兼容旧浏览器，允许长单词内换行 */
        word-break: break-all;        /* 允许任意位置换行（适合中英文混合） */
        white-space: normal;          /* 恢复默认换行（取消 nowrap 强制不换行） */
        overflow-wrap: break-word;  
    }
    /deep/ .ql-align-right{
        text-align: right;
    }
     /deep/ .ql-align-center{
      text-align: center;
    }
    /deep/.box img {
        display: block;
    }
    /deep/ .box li:not(.ql-direction-rtl)::before {
          margin-left: -1.5em;
          margin-right: .3em;
          text-align: right;
      }
</style>

<style scoped lang="stylus">
.backgImg {
  background-image: var(--imgurl) !important;
  background-size: 100% 100%;
  background-repeat: 'no-repeat'; // 不重复
  background-position: 'center'; // 居中显示
  background-color: 'transparent'; // 背景色设为透明
}

.backgColor {
  background-color: var(--colorTwo) !important;
}

.del-but {
  font-size: 13px !important;
  padding: 3px 5px !important;
  width: auto !important;
  height: auto !important;
}

.choose {
  width: 56px;
  height: 40px;
  background: #FFFFFF;
  box-shadow: 0px 5px 10px 1px rgba(65, 70, 88, 0.3);
  border-radius: 23px 23px 23px 23px;
  padding: 8px 16px;
}

.diy-box-style {
  flex: 1;
  height: 100%;
  background-color: white;
  // border-radius: 4px;
  border: 0.5px solid #edf1f4;
  border-bottom: 0;
  position: relative;
  align-items: center;
  border-radius: 4px 4px 0px 0px;

  .content {
    width: 320px !important;
    height: 690px !important;
    background-size: 320px 690px;
    box-sizing: border-box;
  }

  /* 针对 iOS 设备 */
}

.content1 {
  width: 393px !important;
  height: 705px !important;
  background-size: 393px 852px;
}

.qx_bt:hover {
  border: 1px solid #2890ff;
  background-color: #FFFFFF;
}

.qd_bt {
  margin-left: 14px;
}

#app {
  background: #EDF1F5;
  height: 100%;
  overflow: hidden;

  .nav {
    height: 50px;
    display: flex;
    align-items: center;
    font-size: 16px;
    font-weight: 400;
    padding-left: 10px;

    .jiantou {
      margin: 0 5px;
    }
  }
}

.link {
  cursor: pointer;
}

.ivu-mt {
  display: flex;
  justify-content: space-between;
  height: 100% !important;
}

.iconfont-diy {
  font-size: 24px;
   
}

.diy-wrapper {
  display: flex;
  align-items: center;
  height: 100%;

  .left {
    width: 450px;
    display: flex;
    flex-direction: column;
    height: 100%;
    background: white;
    margin-right: 31vw;
    border-radius: 4px 4px 0px 0px;

    .ivu-tabs {
      height: 100%;
      flex: 1;
      display: flex;
      flex-direction: column;

      /deep/ .ivu-tabs-content {
        height: calc(100% - 43px);
        flex: 1;
      }

      /deep/ .ivu-tabs-tabpane {
        display: flex;
        flex-direction: column;

        .left-list-group {
          padding: 24px 0px 20px 20px;
          display: flex;
          overflow: auto;
          scrollbar-width: thin;
          border-bottom: 1px solid #E9ECEF;

          .list-group-item {
            display: flex;
            align-items: center;
            justify-content: center;
            margin-right: 8px !important;

            .list-group-item-active {
              border: 1px solid #2890FF !important;
            }

            .list-group-item-content {
              display: flex;
              flex-direction: column;
              align-items: center;
              justify-content: center;
              background: #fff;
              font-size: 12px;
              color: #666;
              cursor: pointer;
              width: 76px;
              height: 76px;
              border-radius: 8px;
              border: 1px solid #E9ECEF;
            }

            &:nth-child(3n) {
              margin-right: 0;
            }
          }
        }

        .right-box {
          flex: 1;
          overflow: hidden;

          .title-bar {
            // width: 100%;
            height: 50px;
            line-height: 50px;
            // background: #F4F7F9;
            margin: 0px 19px;
            padding: 0px;
            color: #333;
            font-family: MicrosoftYaHei-Bold, MicrosoftYaHei-Bold;
            font-weight: bold;
            font-size: 16px;
            color: rgba(0, 0, 0, 0.8);
            text-align: left;
            font-style: normal;
            text-transform: none;
          }
        }
      }
    }

    .title-bar {
      height: 38px;
      color: #333;
      line-height: 38px;
      padding-left: 12px;
      border-bottom: 1px solid #eee;
      border-radius: 4px;
    }

    .wrapper {
      padding: 15px;

      .tips {
        padding-bottom: 15px;
        font-size: 12px;
        color: #999999;
      }
    }
  }
    
    .correlation-box {
      position: absolute;
      height: 588px;
      top: 20px;
      right: 50px;
      width: 110px;
      background: linear-gradient(180deg, rgba(213, 219, 232, 0.12) 0%, rgba(213, 219, 232, 0.08) 100%);
      border: 1px solid rgba(213, 219, 232, 0.3);
      border-radius: 8px;
      text-align: center;

      .correlation-body {
        display: flex;
        flex-direction: column;
        justify-content: center;
        max-height: 500px;
        overflow-y: auto;
        overflow-x: hidden;
        -ms-overflow-style: scrollbar;
        scrollbar-width: thin;
      }

      .list-group-item {
        display: flex;
        align-items: center;
        justify-content: center;
        cursor: pointer;

        :hover {
          background-color: #f5f5f5;
        }
      }

      .list-group-item-content {
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        background: #fff;
        font-size: 8px;
        color: #666;
        cursor: pointer;
        width: 70px;
        height: 70px;
        border-radius: 8px;
        border: 1px solid #e9ecef;
        margin-bottom: 10px;
      }

      .item {
        text-align: center;
        user-select: none;
        cursor: pointer;
      }
    }


  .content, .content1 {
    position: relative;
    // width: 35% !important;
    // height: 82.5% !important;
    // width: 319px !important;
    // height: auto !important;
    background-position: top center;
    background-repeat: no-repeat;
    background-size: 320px 690px;

    // background-size 100% 100%;
    // border-radius: 50px;
    // transform scale(1);
    .bar-status {
      position: absolute;
      top: 11px;
      right: 0;
      width: 93.4%;
      left: 3.2%;
      border-top-left-radius: 24px;
      border-top-right-radius: 24px;
    }

    .liuhai {
      position: absolute;
      top: 9px;
      left: 50%;
      transform: translateX(-50%);
      z-index: 999;
    }

    .shadow {
      display: none;
      box-shadow: -11px 19px 100px 0px rgba(22, 68, 117, 0.24);
      width: 98%;
      height: 100%;
      position: absolute;
      border-radius: 48px;
      /* top: 1% */
      left: 1%;
      z-index: 0;
    }

    .gllb {
      font-weight: 400;
      font-size: 14px;
      color: #414658;
      border-bottom: 1px solid rgba(213, 219, 232, 0.3);
      padding: 12px 0px;
      margin-bottom: 12px;
    }

    .icondel_1 {
      position: absolute;
      top: -12px;
      right: 0px;
      width: 12px;
      height: 12px;
      color: #97A0B4;
      cursor: pointer;
    }

    .changeBtn {
      // width: 40px;
      // height: 96px;
      // background: #343B4A;
      user-select: none;
      // width: 64px;
      height: 152px;
      background: #F4F5F6;
      border-radius: 23px 23px 23px 23px;
      position: absolute;
      top: 260px;
      left: -100px;
      padding: 4px;
      cursor: pointer;

      p {
        width: 56px;
        height: 40px;
        text-align: center;
        display: flex;
        justify-content: center;
        align-items: center;
        margin-bottom: 12px;

        img {
          width: 24px;
          height: 24px;
        }
      }
    }

    .dragArea.list-group {
      width: 100%;

      &.pre {
        overflow-x: hidden;
        overflow-y: scroll;
        scrollbar-width: none;
        // height: 540px;
        height: 685px;
      }

      .mConfig-item {
        position: relative;

        .delete-box {
          display: none;
          position: absolute;
          left: 0;
          top: 0;
          width: 100%;
          height: 100%;
          border: 2px dashed #00a0e9;
          padding: 10px 0;

          span {
            position: absolute;
            right: 0;
            bottom: 0;
            width: 32px;
            height: 16px;
            line-height: 16px;
            display: inline-block;
            text-align: center;
            font-size: 10px;
            color: #fff;
            background: rgba(0, 0, 0, 0.4);
            margin-left: 2px;
            cursor: pointer;
            z-index: 11;
          }
        }

        &:hover, &.on {
          cursor: move;

          .delete-box {
            display: block;
          }
        }

        &.header {
          position: sticky;
          top: 0;
          z-index: 99;
        }
      }

      >.mConfig-item:last-child {
        margin-bottom: 60px;
      }
    }
  }

  ::-webkit-scrollbar {
    width: 0px;
    background-color: transparent;
  }

  ::-webkit-scrollbar-track {
    border-radius: 10px;
  }

  ::-webkit-scrollbar-thumb {
    background-color: #bfc1c4;
  }
}

.fanghui {
  width: 402px;
  height: 41px;
  margin-top: 20px;
  text-indent: 23px;
  border-bottom: 1px solid #E9ECEF;

  span {
    font-family: MicrosoftYaHei, MicrosoftYaHei;
    font-weight: normal;
    font-size: 16px;
    cursor: pointer;
  }
}

/deep/ .ivu-tabs-bar {
  margin-bottom: 0;
  // height 43px;
  box-sizing: border-box overflow hidden border-top-left-radius 10px;
  border-top-right-radius: 10px;
  // border-bottom: 3px solid #E9ECEF;
  display: flex;
  justify-content: center;
  border: none;
  margin-top: 20px;
}

/deep/ .ivu-scroll-loader {
  display: none;
}

/deep/ .ivu-card-body {
  width: 100%;
  height: 100%;
  background: #EDF1F5;
  padding: 0px;
}

/deep/ .ivu-tabs-nav {
  width: 100%;
  display: flex;
  background: #EDF1F5;

  .ivu-tabs-tab {
    background: #f4f5f6;
  }
}

/deep/ .ivu-tabs-nav-scroll {
  border-right: #edf1f5;
  width: 338px;
  border-radius: 4px;
  // height: 32px;
}

/deep/ .ivu-tabs-nav .ivu-tabs-tab {
  padding: 11px 22px 8px 22px;
  margin-right: 0;
  flex: 1;
  text-align: center;
  border-right: 1px solid #D5DBE8;
  width: 112px;
  height: 42px;
  font-weight: normal;
  font-size: 16px;
  line-height: 22px;
}

/deep/ .ivu-tabs-nav div.ivu-tabs-tab:last-of-type {
  border-right: none;
}

/deep/ .ivu-tabs-nav .ivu-tabs-tab-active:hover {
  color: #fff;
}

/deep/ .ivu-tabs-ink-bar .ivu-tabs-ink-bar-animated {
  visibility: hidden !important;
  background: #fff;
}

/deep/ .ivu-tabs-ink-bar {
  width: 95px !important;
}

/deep/ .ivu-tabs-nav .ivu-tabs-tab-active {
  color: #fff;
  background: #2d8cf0;
}

/deep/ .ivu-tabs-nav-container {
  line-height: 15px;
  margin-bottom: 0px;
}

/deep/ .ivu-mt, .ivu-mt-16 {
  margin-top: 0px !important;
  // 页面高度 - 导航栏高度 - 距离底部的高度
  height: calc(100% - 50px - 11px);
}

/deep/ .delete-box {
  display: flex;
  flex-direction: row-reverse;
}

/deep/.ivu-scroll-container {
  overflow-y: scroll;
  scrollbar-width: none;
  background-image: url('../../../assets/images/diy/grbj.png');
  background-size: cover;
  // background-color: var(--colorTwo);
}

/deep/.ivu-modal-body .ivu-form-item-label {
  color: #414658 !important;
  font-size: 14px !important;
}

/deep/.ivu-input {
  border: 1px solid #d5dbe8;
}

/deep/.ivu-input::input-placeholder {
  color: #97a0b4;
  font-size: 14px !important;
}

/deep/.ivu-modal .ivu-form {
  margin-right: 40px;
}

.saveBtn {
  width: 80px;
  height: 80px;
  text-align: center;
  font-size: 18px;
  color: #fff;
  border-radius: 50%;
  background-image: linear-gradient(140deg, #007DFF, #00BEFF);
  position: fixed;
  bottom: 5%;
  right: 4%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  cursor: pointer;

  p:nth-of-type(1) {
    line-height: 26px;
    font-size: 28px;
  }

  &:hover {
    filter: brightness(115%);
  }
}

.add-template-model {
  /deep/.ivu-modal-wrap .ivu-modal {
    top: 50% !important;
    transform: translateY(-50%);
  }

  /deep/.ivu-btn-text {
    width: 112px;
    height: 36px;
    border: 1px solid #2890FF;
    border-radius: 2px;
    background: white;
    font-size: 16px !important;
    font-weight: 400;
    color: #2890FF;
  }

  /deep/.ivu-btn-primary {
    width: 112px;
    height: 36px;
    background: #2890FF;
    border-radius: 2px;
    font-size: 16px !important;
    font-weight: 400;
    color: #FEFEFE;
  }

  /deep/.ivu-modal-wrap .ivu-modal {
    width: 460px !important;
  }

  /deep/.ivu-modal-content .ivu-modal-body {
    padding-top: 40px !important;
    padding-bottom: 40px !important;
  }
}

/deep/ .ivu-tabs-tab {
  font-size: 16px;
}

/deep/ .list-group-item-content p {
  font-size: 14px;
  color: #7D869C;
}

/deep/ .right-box .title-bar {
  font-size: 16px;
}

/deep/.ivu-modal-header p {
  color: #414658 !important;
}

/deep/ .ivu-modal-header {
  background: center !important;
  border-bottom: 1px solid #e8eaec !important;
}

/deep/ .ivu-modal-header-inner {
  color: #414658 !important;
  font-size: 16px;
}

/deep/ .active {
  border: 1px solid #2890FF !important;

  span, p {
    color: #2890FF !important;
  }
}

/deep/.colorPicker-box {
  .ivu-select-dropdown {
    left: -40px !important;
  }
}
/deep/.ql-toolbar {
  display: block !important;
}
</style>
