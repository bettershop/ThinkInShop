import { getMchInfo, modifyMchInfo, getMchFl } from '@/api/plug_ins/stores'
import cascade from '@/api/publics/cascade'
import { getItuList } from '@/api/members/membersSet'
export default {
  name: 'editorStore',

  data() {
    return {
      ruleForm: {
        name: null,
        roomid: null,
        shop_information: null,
        confines: null,
        tel: null,
        sheng: '',
        shi: '',
        xian: '',
        address: null,
        shop_nature: null,
        mac_choose_fl: null,
        // user_name:null,
        realname: null,
        ID_number: null,
        business_licens: [],
        business_licens1: null,
        is_open: null,
        headImg: null,
        logo: null,
        posterImg: null
      },
      choose_fl: [
        {
          value: '0',
          label: '分类1'
        },
        {
          value: '1',
          label: '分类2'
        },
        {
          value: '2',
          label: '分类3'
        }
      ], // 店铺分类
      statusList: [
        // {
        //     value: 0,
        //     label: '未营业'
        // },
        {
          value: '1',
          label: '营业中'
        },
        {
          value: '2',
          label: '已打烊'
        }
      ],
      state: '',
      storeInfo: null,
      countryNum: '', // 选中的国家的num3值
      restaurants: [],
      //省市级联集
      shengList: {},
      shiList: {},
      xianList: {},
      isDomestic: true, // 是否国内
      belongList: [
        {
          value: 0,
          name: this.$t('stores.viewStore.gr')
        },
        {
          value: 1,
          name: this.$t('stores.viewStore.qy')
        }
      ]
    }
  },

  created() {
    this.getSheng()
    this.getMchInfos().then(() => {
      this.cascadeAddress()
      this.getMchFls()
    })

  },
  mounted() {
    if (sessionStorage.getItem('restaurants')) {
      this.restaurants = JSON.parse(sessionStorage.getItem('restaurants'))
    } else {
      this.queryAdd()
    }
  },
  beforeRouteLeave(to, from, next) {
    if (to.name == 'store') {
      to.params.dictionaryNum = this.$route.query.dictionaryNum
      to.params.pageSize = this.$route.query.pageSize
    }
    next()
  },

  methods: {
    queryAdd() {
      const data = {
        api: 'admin.user.getItuList',
        keyword: this.keyword
      }
      getItuList(data).then(res => {
        if (res.data.code == 200) {
          this.restaurants = res.data.data
          sessionStorage.setItem('restaurants', JSON.stringify(this.restaurants))
        }
      })
    },
    // 选择建议项时触发的方法
    handleSelect(item) {
      console.log('选中的项:', item);
      this.state = item.code2; // 可以根据需求更新输入框显示的值
      this.countryNum = item.code2;
      if (item.code2 == '86' || item.code2 == '852' || item.code2 == '853') {
        this.isDomestic = true;
      }else {
        this.isDomestic = false;
      }
    },
    // 异步查询建议列表的方法
    querySearchAsync(queryString, cb) {
      // 模拟异步请求
      setTimeout(() => {
        const results = queryString
          ? this.restaurants.filter(this.createFilter(queryString))
          : this.restaurants;
        // 调用回调函数，将查询结果传递给组件
        cb(results);
      }, 300);
    },
    createFilter(queryString) {
      return (country) => {
        const lowerCaseQuery = queryString.toLowerCase();
        return (
          country.name.toLowerCase().indexOf(lowerCaseQuery) > -1 ||
          country.code2.toLowerCase().indexOf(lowerCaseQuery) > -1 ||
          country.zh_name.toLowerCase().indexOf(lowerCaseQuery) > -1
        );
      };
    },
    //获取店铺分类
    async getMchFls() {
      const res = await getMchFl({
        api: 'mch.Admin.Mch.MchClassList',
        isDisPlay: 1
      })
      console.log('店铺分类res', res)
      if (res.data.code == 200) {
        this.choose_fl = res.data.data.list
        this.ruleForm.mac_choose_fl = this.choose_fl.find(
          item => item.name === this.storeInfo.className
        ).id
      } else {
        console.log(res.data.message)
      }
    },
    uploadChange() {
      // this.ruleForm.business_licens = []
    },
    async getMchInfos() {
      const res = await getMchInfo({
        api: 'mch.Admin.Mch.GetMchInfo',
        id: this.$route.query.id
      })

      console.log('res', res)
      this.storeInfo = res.data.data.list[0]

      let editorInfo = res.data.data.list[0]

      this.ruleForm.name = editorInfo.name
      this.ruleForm.roomid = editorInfo.roomid
      this.ruleForm.shop_information = editorInfo.shop_information
      this.ruleForm.confines = editorInfo.shop_range
      this.ruleForm.tel = editorInfo.tel
      this.ruleForm.sheng = editorInfo.sheng
      this.ruleForm.shi = editorInfo.shi
      this.ruleForm.xian = editorInfo.xian
      this.ruleForm.address = editorInfo.address
      this.ruleForm.shop_nature = editorInfo.shop_nature
      this.ruleForm.headImg = editorInfo.headimgurl
      this.ruleForm.posterImg = editorInfo.poster_img
      this.ruleForm.logo = editorInfo.logo
      // this.ruleForm.user_name = editorInfo.user_name
      this.ruleForm.realname = editorInfo.realname
      this.ruleForm.ID_number = editorInfo.ID_number
      this.ruleForm.is_open = editorInfo.is_open
      this.countryNum = editorInfo.cpc
      if (this.countryNum == '86' || this.countryNum == '852' || this.countryNum == '853') {
          this.isDomestic = true;
        }else {
          this.isDomestic = false;
      }
      if (this.ruleForm.shop_nature == 0) {
        this.ruleForm.business_licens = editorInfo.business_license
      } else {
        this.ruleForm.business_licens1 = editorInfo.business_license
      }
      console.log('editorInfo.is_open', editorInfo.is_open)

      console.log('business_licens', this.ruleForm.business_licens)
    },

    // 获取省级
    async getSheng() {
      const res = await cascade.getSheng()
      this.shengList = res.data.data
    },

    // 获取市级
    async getShi(sid, flag) {
      const res = await cascade.getShi(sid)
      this.shiList = res.data.data
      if (!flag) {
        this.ruleForm.shi = ''
        this.ruleForm.xian = ''
      }
    },

    // 获取县级
    async getXian(sid, flag) {
      const res = await cascade.getXian(sid)
      this.xianList = res.data.data
      if (!flag) {
        this.ruleForm.xian = ''
      }
    },

    //省市级联回显
    async cascadeAddress() {
      //省市级联
      for (const sheng of this.shengList) {
        if (sheng.districtName === this.ruleForm.sheng) {
          await this.getShi(sheng.id, true)
          for (const shi of this.shiList) {
            if (shi.districtName === this.ruleForm.shi) {
              await this.getXian(shi.id, true)
              break
            }
          }
          break
        }
      }
    },

    submitForm() {
      let is_cid = 0
      if (typeof this.ruleForm.mac_choose_fl == 'string') {
        this.choose_fl.forEach((item, index) => {
          if (item.name == this.ruleForm.mac_choose_fl) {
            is_cid = item.id
          }
        })
      } else {
        is_cid = this.ruleForm.mac_choose_fl
      }
      var business_license
      if (this.ruleForm.business_license != []) {
        if (this.ruleForm.shop_nature == 0) {
          business_license = this.ruleForm.business_licens.join(',')
        } else {
          business_license = this.ruleForm.business_licens1.toString()
        }
      }
      if (this.ruleForm.logo == '' || this.ruleForm.headImg == '') {
        this.errorMsg('店铺信息不完整！');
        return
      }
      let isCpc = this.restaurants.find((item) => [item.code2, item.name].includes(this.countryNum))

      if (!isCpc) {
        this.$message({
          message: '区号不正确！',
          type: 'error',
          offset: 102
        })
        return
      }

      if (this.countryNum == '') {
        this.$message({
          message: '请选择区号！',
          type: 'error',
          offset: 102
        })
        return
      }
      modifyMchInfo({
        api: 'mch.Admin.Mch.ModifyMchInfo',
        id: this.$route.query.id,
        roomid: this.ruleForm.roomid,
        mchInfo: this.ruleForm.shop_information,
        confines: this.ruleForm.confines,
        tel: this.ruleForm.tel,
        shen: this.ruleForm.sheng,
        shi: this.ruleForm.shi,
        xian: this.ruleForm.xian,
        address: this.ruleForm.address,
        nature: this.ruleForm.shop_nature,
        cid: is_cid,
        realName: this.ruleForm.realname,
        idNumber: this.ruleForm.ID_number,
        mchName: this.ruleForm.name,
        isOpen: this.ruleForm.is_open,
        logo: this.ruleForm.logo,
        headImg: this.ruleForm.headImg,
        posterImg: this.ruleForm.posterImg,
        license: business_license,
        cpc: isCpc.code2, // 国家代码

      }).then(res => {
        if (res.data.code == '200') {
          this.$message({
            message: this.$t('stores.bjcg'),
            type: 'success',
            offset: 102
          })
          this.$router.go(-1)
        }
        console.log(res)
      })
    }
  }
}
