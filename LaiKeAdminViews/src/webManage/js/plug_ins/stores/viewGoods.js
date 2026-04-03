import { getGoodsDetailInfo } from '@/api/plug_ins/stores'
import { dictionaryList } from '@/api/Platform/numerical'
import { label } from '@/api/goods/goodsList'
import { VueEditor } from 'vue2-editor'

export default {
  name: 'viewGoods',
  components: {
    VueEditor
  },

  data () {
    return {
      ruleForm: {
        // 商品设置
        checkedLabel: [],
        checkedActivity: [],
        checked: '',

        content: '',
        attrList: []
      },
      showAdrList: [], // 商品展示位置
      goodsInfo: {},

      header: {
        'background-color': '#F4F7F9',
        'font-weight': 'bold',
        'border-bottom': '1px solid #E9ECEF'
      },
      goodsDate: [],

      labelList: [],

      activityList: [
        {
          value: '1',
          name: '正价'
        }
      ],

      goodsImageUrls: [],
      attrTitle: JSON.parse('[]', true),
      videoList: [],
      videoList2: [],
      videoSrc: '',
      videoSrc2: '',
      dialogVisible_video: false,
      dialogVisible_video2: false,
      IntroList: [],
      drawingList: []
    }
  },

  created () {
    this.labels()
    this.getDictionaryList()
    this.getGoodsDetailInfos()
  },

  computed: {
    filterTable () {
      let arr = []
      if (this.attrTitle.length > 0) {
        this.attrTitle.filter((items, indexs) => {
          arr.push([])
          items.attr_list.filter(item => {
            arr[indexs].push({
              text: item.attr_name,
              value: item.attr_name
            })
          })
        })
      }
      return arr
    }
  },

  beforeRouteLeave (to, from, next) {
    if (to.name == 'goodsAudit') {
      to.params.dictionaryNum = this.$route.query.dictionaryNum
      to.params.pageSize = this.$route.query.pageSize
    }
    next()
  },

  methods: {
    async getDictionaryList () {
      const res = await dictionaryList({
        api: 'saas.dic.getDictionaryInfo',
        key: '商品展示位置',
        status: 1
      })

      this.showAdrList = res.data.data.list
      console.log('显示位置',this.showAdrList)
    },
    onEditorFocus (event) {
      event.enable(false)
    },
    // 预览视频
    previewVideo (data) {
      this.videoSrc = data.url
      this.dialogVisible_video = true
    },
    handleClose () {
      const video = document.getElementsByTagName('video')[1]
      if (!video.paused) {
        video.currentTime = 0
        video.pause()
      }
      this.dialogVisible_video = false
    },
    handleClose5 () {
      const video = document.getElementsByTagName('video')[1]
      if (!video.paused) {
        video.currentTime = 0
        video.pause()
      }
      this.dialogVisible_video2 = false
    },
    // 预览视频
    previewVideo2 (data) {
      this.videoSrc2 = data.url
      this.dialogVisible_video2 = true
    },
    // 获取商品标签
    async labels () {
      const res = await label({
        api: 'admin.label.index',
        pageSize: 9999
      })
      this.labelList = res.data.data.list
    },
    filterHandler (value, row) {
      let flag = row.attr_list.some(item => {
        return item.attr_name == value
      })
      return flag
    },
    async getGoodsDetailInfos () {
      const res = await getGoodsDetailInfo({
        api: 'mch.Admin.Mch.GetGoodsDetailInfo',
        goodsId: this.$route.query.id
      })

      console.log(res)
      this.goodsDate = res.data.data.checked_attr_list
      this.goodsInfo = res.data.data.goodsInfo[0]
      if (this.goodsInfo.video) {
        this.videoList.push({
          videoFile: this.goodsInfo.video,
          url: this.goodsInfo.video,
          isShowPopup: false
        })
      }
      if (this.goodsInfo.proVideo) {
        this.videoList2.push({
          videoFile: this.goodsInfo.proVideo,
          url: this.goodsInfo.proVideo,
          isShowPopup: false
        })
      }
      this.ruleForm.checkedLabel = res.data.data.goodsInfo[0].s_type
        .split(',')
        .map(item => {
          return parseInt(item)
        })
      this.ruleForm.checkedLabel = this.ruleForm.checkedLabel.filter(item => {
        if (item) {
          return item
        }
      })
      // this.ruleForm.checkedLabel = res.data.data.goodsInfo[0].s_type.split(',').filter(item => {
      //     if(item !== '') {
      //         return Number(item)
      //     }
      // })
      this.ruleForm.checkedActivity = res.data.data.goodsInfo[0].active
      res.data.data.goodsInfo[0].show_adr.split(',').map(item => {
        if (item) {
          this.ruleForm.checked = item.toString()
          // this.ruleForm.checked.push(item)
        }
      })
      // this.ruleForm.content = res.data.data.goodsInfo[0].content
      this.IntroList = JSON.parse(res.data.data.goodsInfo[0].content)
      // if(res.data.data.goodsInfo[0].content){
      //   try {
      //   this.IntroList=JSON.parse(res.data.data.goodsInfo[0].content)
      //   let list = []
      //   console.log('335335335',);
      //   if(JSON.parse(res.data.data.goodsInfo[0].content) instanceof Array){
      //       JSON.parse(res.data.data.goodsInfo[0].content).forEach((item,index)=>{
      //           list.push({content:""})
      //           item.content.forEach(item1=>{
      //               if(item1.tagType=='image'){
      //                   list[index].content = list[index]?.content.length<=0?`<img src='${item1.value}'></img>`:list[index].content+`<img src='${item1.value}'></img>`
      //               }else{
      //                 // style='text-align: left;color:#999999;font-size:24rpx;margin-bottom:16px'
      //                   list[index].content = list[index]?.content.length<=0?`<${item1.tagType} >${item1.value}</${item1.tagType}>`:list[index].content+`<${item1.tagType}>${item1.value}</${item1.tagType}>`
      //               }
      //           })
      //       })
      //   }
      //  this.drawingList = list
      //   } catch (error) {
      //      this.IntroList = [{name:'商品详情',content:res.data.data.goodsInfo[0].content}]
      //       try {
      //         this.drawingList = [{name:'商品详情',content:JSON.parse(res.data.data.goodsInfo[0].content).content}]
      //       } catch (error) {
      //         this.drawingList = [{name:'商品详情',content:res.data.data.goodsInfo[0].content}]
      //       }
      //   }
      // }

      ;(this.attrTitle = res.data.data.attr_group_list),
        (this.goodsImageUrls = res.data.data.goodsImageUrls)
    }
  }
}
