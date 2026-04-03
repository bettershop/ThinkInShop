import cascade from '@/api/publics/cascade'
import { addMch } from '@/api/goods/goodsList'
import { setStorage, getStorage } from '@/utils/storage'
import { getMchFl, modifyMchInfo } from '@/api/plug_ins/stores'
import { getItuList } from '@/api/members/membersSet'
export default {
  name: 'addStore',

  data () {
    return {
      ruleForm: {
        logo: '',
        headImg: '',
        posterImg: '',
        name: '',
        shop_information: '',
        shop_range: '',
        // community_id: '',
        // adminid: '',
        realname: '',
        ID_number: '',
        tel: '',
        sheng: '',
        shi: '',
        xian: '',
        address: '',
        shop_nature: 0,
        // business_licens: [],
        img1: '', //身份证正面
        img2: '', //身份证反面
        img3: '', //营业执照
        mac_choose_fl: ''
      },
      ruleForm2: {
        name: '',
        img: '',
        px: '',
        ishow: 1
      },
      dialogVisible: false,
      choose_fl: [], // 店铺分类
      rules: {
        logo: [
          {
            required: true,
            message: '请选择店铺logo',
            trigger: 'change'
          }
        ],
        headImg: [
          {
            required: true,
            message: '请选择店铺头像',
            trigger: 'change'
          }
        ],
        posterImg: [
          {
            required: true,
            message: '请选择店铺宣传图',
            trigger: 'change'
          }
        ],
        name: [
          {
            required: true,
            message: '请输入店铺名称',
            trigger: 'blur'
          }
        ],
        shop_information: [
          {
            required: true,
            message: '请输入店铺简介',
            trigger: 'blur'
          }
        ],
        shop_range: [
          {
            required: true,
            message: '请输入店铺经营范围',
            trigger: 'blur'
          }
        ],
        mac_choose_fl: [
          {
            required: true,
            message: '请选择店铺分类',
            trigger: 'change'
          }
        ], 
        realname: [
          {
            required: true,
            message: '请输入用户姓名',
            trigger: 'blur'
          }
        ],
        ID_number: [
          {
            required: true,
            message: '请输入身份证号',
            trigger: 'blur'
          }
        ],
        tel: [
          {
            required: true,
            message: '请输入联系电话',
            trigger: 'blur'
          }
        ],
        sheng: [
          {
            required: true,
            message: '请选择省或直辖市',
            trigger: 'change'
          }
        ],
        address: [
          {
            required: true,
            message: '请输入详细地址',
            trigger: 'blur'
          }
        ]
      },
      rules2: {
        name: [
          {
            required: true,
            message: this.$t('stores.addStoreFl.qsrfl'),
            trigger: 'blur'
          }
        ],
        px: [
          {
            required: true,
            message: '请输入排序号',
            trigger: 'blur'
          }
        ]
      },
      belongList: [
        {
          value: 0,
          name: this.$t('stores.viewStore.gr')
        },
        {
          value: 1,
          name: this.$t('stores.viewStore.qy')
        }
      ],
      classList: [
        {
          value: 1,
          name: this.$t('stores.addStoreFl.shi')
        },
        {
          value: 0,
          name: this.$t('stores.addStoreFl.fou')
        }
      ],
      max: '3',
      //省市级联集
      shengList: {},
      shiList: {},
      xianList: {},
      state:'',
      storeInfo: null,
      countryNum: '', // 选中的国家的num3值
      restaurants:[],
      isDomestic: true, // 是否国内
    }
  },

  created () {
    this.getSheng()
    this.getMchFls()

  },
  mounted() {
    if(sessionStorage.getItem('restaurants')){
      this.restaurants =JSON.parse( sessionStorage.getItem('restaurants'))
    }else{
        this.queryAdd()
    }
  },
  methods: {
    queryAdd(){
      const data ={
          api:'admin.user.getItuList',
          keyword:this.keyword
      }
      getItuList(data).then(res=>{
        if(res.data.code == 200){
            this.restaurants = res.data.data
            sessionStorage.setItem('restaurants',JSON.stringify(this.restaurants))
        }
      })
    },
    // 选择建议项时触发的方法
    handleSelect(item) {
      console.log('选中的项:', item);
      this.state = item.code2; // 可以根据需求更新输入框显示的值
      this.countryNum = item.num3;
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
    handleClose () {
      this.dialogVisible = false
      this.ruleForm2.name = ''
      this.ruleForm2.img = ''
      this.$refs.imgUpload.fileList = []
      this.ruleForm2.px = ''
      this.ruleForm2.ishow = 1
      this.$refs['ruleForm2'].clearValidate()
    },
    submitForm2 (formName) {
      this.$refs[formName].validate(async valid => {
        if (valid) {
          try {
            modifyMchInfo({
              api: 'mch.Admin.Mch.AddMchClass',
              name: this.ruleForm2.name,
              img: this.ruleForm2.img,
              sort: this.ruleForm2.px,
              isDisplay: this.ruleForm2.ishow
            }).then(res => {
              if (res.data.code == '200') {
                this.$message({
                  message: this.$t('zdata.tjcg'),
                  type: 'success',
                  offset: 102
                })
                this.handleClose()
                this.getMchFls(true)
              }
            })
          } catch (error) {
            this.$message({
              message: error.message,
              type: 'error',
              showClose: true
            })
          }
        } else {
          console.log('error submit!!')
          return false
        }
      })
    },
    natureChange () {
      // this.ruleForm.business_licens = []
      this.ruleForm.img1 = ''
      this.ruleForm.img2 = ''
      this.ruleForm.img3 = ''
      this.$nextTick(() => {
        this.$refs.upload1.fileList = []
        this.$refs.upload2.fileList = []
        this.$refs.upload3.fileList = []
      })
    },
    //获取店铺分类
    async getMchFls (isAdd) {
      const res = await getMchFl({
        api: 'mch.Admin.Mch.MchClassList',
        isDisPlay: 1
      })
      console.log('店铺分类res', res)
      if (res.data.code == 200) {
        this.choose_fl = res.data.data.list
         if (isAdd) {
          this.ruleForm.mac_choose_fl = this.choose_fl[0].id
        }
      } else {
        console.log(res.data.message)
      }
    },

    // 获取省级
    async getSheng () {
      const res = await cascade.getSheng()
      this.shengList = res.data.data
    },

    // 获取市级
    async getShi (sid, flag) {
      const res = await cascade.getShi(sid)
      this.shiList = res.data.data
      if (!flag) {
        this.ruleForm.shi = ''
        this.ruleForm.xian = ''
      }
    },

    // 获取县级
    async getXian (sid, flag) {
      const res = await cascade.getXian(sid)
      this.xianList = res.data.data
      if (!flag) {
        this.ruleForm.xian = ''
      }
    },

    //省市级联回显
    async cascadeAddress () {
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

    submitForm (formName) {
      this.$refs[formName].validate(async valid => {
        if (valid) {
          try {
            // if (this.ruleForm.name.length > 20) {
            //   this.$message({
            //     message: '店铺名称长度不能超过20个字符',
            //     type: 'error',
            //     offset: 102
            //   })
            //   return
            // }
            // if (this.ruleForm.shop_information.length > 200) {
            //   this.$message({
            //     message: '店铺信息长度不能超过200个字符',
            //     type: 'error',
            //     offset: 102
            //   })
            //   return
            // }
            // if (this.ruleForm.shop_range.length > 200) {
            //   this.$message({
            //     message: '经营范围长度不能超过200个字符',
            //     type: 'error',
            //     offset: 102
            //   })
            //   return
            // }
            // if (this.ruleForm.realname.length > 20) {
            //   this.$message({
            //     message: '用户姓名长度不能超过20个字符',
            //     type: 'error',
            //     offset: 102
            //   })
            //   return
            // }
            // if (this.ruleForm.address.length > 20) {
            //   this.$message({
            //     message: '详细地址长度不能超过20个字符',
            //     type: 'error',
            //     offset: 102
            //   })
            //   return
            // }
            // if (this.ruleForm.business_licens.length <= 0) {
            //   if(this.ruleForm.shop_nature == 0){
            //     this.$message({
            //       message: '请上传身份证件',
            //       type: 'error',
            //       offset: 102
            //     })
            //     return
            //   }else{
            //     this.$message({
            //       message: '请上传营业执照',
            //       type: 'error',
            //       offset: 102
            //     })
            //     return
            //   }
            // }
            if (this.ruleForm.shop_nature == 0) {
              if (this.ruleForm.img1 == '') {
                this.$message({
                  message: this.$t('increaseStore.qscsfzzm'),
                  type: 'error',
                  offset: 102
                })
                return
              }
              if (this.ruleForm.img2 == '') {
                this.$message({
                  message: this.$t('increaseStore.qscsfzfm'),
                  type: 'error',
                  offset: 102
                })
                return
              }
            } else {
              if (this.ruleForm.img3 == '') {
                this.$message({
                  message: this.$t('increaseStore.qscyyzz'),
                  type: 'error',
                  offset: 102
                })
                return
              }
            }
            if (this.ruleForm.shop_nature == 1) {
              var imgUrls = this.ruleForm.img3
            } else if (this.ruleForm.shop_nature == 0) {
              var imgUrls = this.ruleForm.img1 + ',' + this.ruleForm.img2
            }
            console.log('imgUrls', imgUrls)
            let { entries } = Object
            // var business_license
            // if(this.ruleForm.business_license !=[] ){
            //     if(this.ruleForm.shop_nature == 0){
            //         business_license = this.ruleForm.business_licens.join(',')
            //     }else{
            //         business_license = this.ruleForm.business_licens.toString()
            //     }

            // }
            let data = {
              api: 'admin.goods.addMch',
              logo: this.ruleForm.logo,
              headImg: this.ruleForm.headImg,
              posterImg: this.ruleForm.posterImg,
              name: this.ruleForm.name,
              shop_information: this.ruleForm.shop_information,
              shop_range: this.ruleForm.shop_range,
              realname: this.ruleForm.realname,
              ID_number: this.ruleForm.ID_number,
              tel: this.ruleForm.tel,
              city_all: `${this.ruleForm.sheng}-${this.ruleForm.shi}-${this.ruleForm.xian}`,
              address: this.ruleForm.address,
              shop_nature: this.ruleForm.shop_nature,
              // business_license: this.ruleForm.business_license,
              imgUrls: imgUrls,
              cid: this.ruleForm.mac_choose_fl,
              country_num : this.countryNum, // 国家代码
            }

            let formData = new FormData()
            for (let [key, value] of entries(data)) {
              formData.append(key, value)
            }
            addMch(formData).then(res => {
              console.log(res)
              if (res.data.code == '200') {
                this.$message({
                  message: '添加成功',
                  type: 'success',
                  offset: 102
                })
                const laike_admin_userInfo = getStorage('laike_admin_userInfo')
                laike_admin_userInfo.mchId = res.data.data.mchId
                setStorage('laike_admin_userInfo', laike_admin_userInfo)
                this.$router.go(-1)
              }
            })
          } catch (error) {
            this.$message({
              message: error.message,
              type: 'error',
              showClose: true
            })
          }
        } else {
          console.log('error submit!!')
          return false
        }
      })
    }
  }
}
