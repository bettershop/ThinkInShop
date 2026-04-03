<template>
  <div class="container" id="hhhh">
    <el-form
      ref="ruleForm"
      class="form-search"
      :rules="rules"
      :model="ruleForm"
      label-width="auto"
    >
      <div class="basic-info">
        <div class="header">
          <span>{{ $t('increaseStore.dpxx') }}</span>
        </div>
        <div class="basic-block">
          <div class="basic-items">
            <el-form-item :label="$t('increaseStore.dpmc')" prop="name">
              <el-input
                maxlength="20"
                v-model="ruleForm.name"
                :placeholder="$t('increaseStore.qsrdpmc')"
              ></el-input>
            </el-form-item>
            <el-form-item :label="$t('increaseStore.dpfl')" prop="cid">
              <el-select
                class="select-input"
                v-model="ruleForm.cid"
                v-load-more.method="loadMore"
                filterable
                :placeholder="$t('increaseStore.qxzdpfl')"
              >
                <el-option
                  v-for="item in chooseList"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id"
                >
                </el-option>
              </el-select>
            </el-form-item>
            <el-form-item :label="$t('increaseStore.jyfw')" prop="shop_range">
              <el-input
                v-model="ruleForm.shop_range"
                :placeholder="$t('increaseStore.qsrjyfw')"
              ></el-input>
            </el-form-item>
          </div>
          <el-form-item :label="$t('increaseStore.dpxx')">
            <el-input
              maxlength="50"
              resize="none"
              type="textarea"
              :placeholder="$t('increaseStore.qsrdpjjxx')"
              v-model="ruleForm.shop_information"
            >
            </el-input>
          </el-form-item>
          <div class="basic-items">
            <el-form-item
              :label="$t('increaseStore.yhxm')"
              class="goods-class"
              prop="realname"
            >
              <el-input
                v-model="ruleForm.realname"
                :placeholder="$t('increaseStore.qsrzsxm')"
              ></el-input>
            </el-form-item>
            <el-form-item
              class="goods-brand"
              :label="$t('increaseStore.sfzh')"
              prop="ID_number"
            >
              <el-input
                oninput="value=value.replace(/[^\w]/g,'')"
                v-model="ruleForm.ID_number"
                :placeholder="$t('increaseStore.qsrsfzh')"
              ></el-input>
            </el-form-item>
            <el-form-item :label="$t('increaseStore.lxdh')" prop="tel">
             <div class="input-interval-block">
              <el-autocomplete
              class='auto custom-autocomplete'
                v-model="countryNum"
                :fetch-suggestions="querySearchAsync"
                :placeholder="$t('membersLists.qh')"
                @select="handleSelect"
                :popper-append-to-body="false"
              >
              <template slot-scope="{ item }">
                <div>
                  <span>{{ item.name }}</span>
                  <span style="color: #999; margin-left: 10px;">+({{ item.code2 }})</span>
                </div>
              </template>
              </el-autocomplete>
              <el-input
                @keyup.native="ruleForm.tel = oninput3(ruleForm.tel)"
                v-model="ruleForm.tel"
                :placeholder="$t('increaseStore.qsrlxdh')"
              ></el-input>
              </div>
            </el-form-item>
          </div>

          <div class="basic-items" >
            <el-form-item
              :label="$t('increaseStore.szdq')"
              class="goods-area"
              prop="xian"
              v-if="isDomestic"
            >
              <el-select
                class="select-input"
                v-model="ruleForm.sheng"
                :placeholder="$t('increaseStore.sheng')"
                @change="shengChange($event)"
              >
                <el-option
                  v-for="(item, index) in shengList"
                  :key="index"
                  :label="item.districtName"
                  :value="item.districtName"
                >
                  <div @click="getShi(item.id)">{{ item.districtName }}</div>
                </el-option>
              </el-select>
              <el-select
                class="select-input"
                v-model="ruleForm.shi"
                :placeholder="$t('increaseStore.shi')"
              >
                <el-option
                  v-for="(item, index) in shiList"
                  :key="index"
                  :label="item.districtName"
                  :value="item.districtName"
                >
                  <div @click="getXian(item.id)">{{ item.districtName }}</div>
                </el-option>
              </el-select>
              <el-select
                class="select-last"
                v-model="ruleForm.xian"
                :placeholder="$t('increaseStore.xian')"
              >
                <el-option
                  v-for="(item, index) in xianList"
                  :key="index"
                  :label="item.districtName"
                  :value="item.districtName"
                >
                  <div>{{ item.districtName }}</div>
                </el-option>
              </el-select>
            </el-form-item>
            <el-form-item :label="$t('increaseStore.xxdz')" prop="address">
              <el-input
                v-model="ruleForm.address"
                :placeholder="$t('increaseStore.qsrxxdz')"
              ></el-input>
            </el-form-item>
          </div>
          <el-form-item :label="$t('increaseStore.ssxz')">
            <el-radio-group
              v-model="ruleForm.shop_nature"
              @change="uploadChange($event)"
            >
              <el-radio
                v-for="item in natureList"
                :label="item.value"
                :key="item.value"
                >{{ item.name }}</el-radio
              >
            </el-radio-group>
          </el-form-item>
          <el-form-item
            v-show="ruleForm.shop_nature == 0"
            class="goods-img"
            required
            :label="$t('increaseStore.sfzzm')"
          >
            <l-upload ref="upload1" :limit="1" text="" v-model="ruleForm.img1">
            </l-upload>
          </el-form-item>
          <el-form-item
            v-show="ruleForm.shop_nature == 0"
            class="goods-img"
            required
            :label="$t('increaseStore.sfzfm')"
          >
            <l-upload ref="upload2" :limit="1" text="" v-model="ruleForm.img2">
            </l-upload>
          </el-form-item>
          <el-form-item
            v-show="ruleForm.shop_nature == 1"
            class="goods-img"
            required
            :label="$t('increaseStore.yyzz')"
            label-width="91.45px"
          >
            <l-upload ref="upload3" :limit="1" text="" v-model="ruleForm.img3">
            </l-upload>
          </el-form-item>
        </div>
      </div>

      <div class="goods-set">
        <div class="header">
          <span>{{ $t('increaseStore.dpzhsz') }}</span>
        </div>
        <div class="set-block">
          <el-form-item :label="$t('increaseStore.zh')" prop="account">
            <el-input
              v-model="ruleForm.account"
              maxlength="16"
              :placeholder="$t('increaseStore.qsrzh')"
            ></el-input>
          </el-form-item>
          <el-form-item :label="$t('increaseStore.mm')" prop="password">
            <el-input
              v-model="ruleForm.password"
              maxlength="16"
              :type="flagType"
              oninput="value=value.replace(/[^\w]/g,'')"
              :placeholder="$t('increaseStore.qsrmm')"
            >
              <i
                slot="suffix"
                :class="[
                  this.flag
                    ? 'laiketui laiketui-yanjing'
                    : 'laiketui laiketui-eye-close'
                ]"
                style="margin-top: 8px; font-size: 18px"
                @click="getFlag()"
              />
            </el-input>
          </el-form-item>
        </div>
      </div>

      <div class="footer-button">
        <el-button
          plain
          class="footer-cancel fontColor"
          @click="$router.go(-1)"
          >{{ $t('merchants.addmerchants.cancel') }}</el-button
        >
        <el-button
          type="primary"
          class="footer-save bgColor mgleft"
          @click="submitForm('ruleForm')"
          >{{ $t('merchants.addmerchants.save') }}</el-button
        >
      </div>
    </el-form>
  </div>
</template>

<script>
import { addStore } from '@/api/plug_ins/stores'
import cascade from '@/api/publics/cascade'
import { getMchFl } from '@/api/plug_ins/stores'
import { getStorage, setStorage } from '@/utils/storage'
import Config from "@/packages/apis/Config";
import { generateAccount} from '@/api/members/membersSet'
import { getItuList } from '@/api/members/membersSet'

export default {
  name: 'increaseStore',
  data () {
    return {
      //省市级联集
      shengList: {},
      shiList: {},
      xianList: {},
      chooseList: [], //店铺分类List
      ruleForm: {
        name: '', //店铺名称
        cid: '', //店铺分类
        shop_range: '', //经营范围
        shop_information: '', //店铺信息
        realname: '', //用户姓名
        ID_number: '', //身份证号
        tel: '', //联系电话
        sheng: '', //省
        shi: '', //市
        xian: '', //县/区
        address: '', //详细地址
        shop_nature: 0,
        img1: '', //身份证正面
        img2: '', //身份证反面
        img3: '', //营业执照
        account: '', //账号
        password: '', //密码
      },
      isDomestic: true, // 是否国内
      state: '',
      countryNum: '', // 选中的国家的num3值
      flag: false,
      flagType: 'password',
      goodsEditorBase: '',
      natureList: [
        {
          value: 0,
          name: this.$t('increaseStore.gr')
        },
        {
          value: 1,
          name: this.$t('increaseStore.qy')
        }
      ],
      rules: {
        // 基本信息
        name: [
          {
            required: true,
            message: this.$t('increaseStore.qsrdpmc'),
            trigger: 'blur'
          }
        ],
        cid: [
          {
            required: true,
            message: this.$t('increaseStore.qxzdpfl'),
            trigger: 'change'
          }
        ],
        shop_range: [
          {
            required: true,
            message: this.$t('increaseStore.qsrjyfw'),
            trigger: 'blur'
          }
        ],
        realname: [
          {
            required: true,
            message: this.$t('increaseStore.qsrzsxm'),
            trigger: 'blur'
          }
        ],
        ID_number: [
          {
            required: true,
            message: this.$t('increaseStore.qsrsfzh'),
            trigger: 'blur'
          }
        ],
        tel: [
          {
            required: true,
            message: this.$t('increaseStore.qsrlxdh'),
            trigger: 'blur'
          },
          {
            min: 11,
            max: 11,
            message: this.$t('increaseStore.llxhdyw'),
            trigger: 'blur'
          }
        ],
        xian: [
          {
            required: true,
            message: this.$t('increaseStore.qxzszdq'),
            trigger: 'blur'
          }
        ],
        address: [
          {
            required: true,
            message: this.$t('increaseStore.qsrxxdz'),
            trigger: 'blur'
          }
        ],
        account: [
          {
            required: true,
            message: this.$t('increaseStore.qsrzh'),
            trigger: 'blur'
          },
          {
            min: 6,
            max: 16,
            message: this.$t('increaseStore.text'),
            trigger: 'blur'
          }
        ],
        password: [
          {
            required: true,
            message: this.$t('increaseStore.qsrmm'),
            trigger: 'blur'
          },
          {
            min: 6,
            max: 16,
            message: this.$t('increaseStore.text'),
            trigger: 'blur'
          }
        ]
      },
      pages: 1,
      pageNo: 1,
      pageSize: 10,
    }
  },

  created () {
    this.getMchFls()
    this.getSheng()

    //自动生成账号
    generateAccount({
        api: 'admin.user.generateAccount'
    }).then(res=>{
        if(res.data.code == 200){
            this.ruleForm.account = res.data.data
        }
    })
  },
  watch: {
    'ruleForm.xian'() {
      if(this.ruleForm.xian) {
        this.$refs.ruleForm.clearValidate('xian')
      }
    },
  },
  mounted () {
    this.loadMore()
    this.getBase()
    this.storeIds = getStorage('rolesInfo').storeId
    if (sessionStorage.getItem('restaurants')) {
      this.restaurants = JSON.parse(sessionStorage.getItem('restaurants'))
    } else {
      this.queryAdd()
    }
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
    // 选择建议项时触发的方法
    handleSelect(item) {
      console.log('选中的项:', item);
      this.state = item.num3; // 可以根据需求更新输入框显示的值
      this.countryNum = item.code2;
      if (item.code2 == '86' || item.code2 == '852' || item.code2 == '853') {
        this.isDomestic = true;
      }else {
        this.isDomestic = false;
      }
    },
    uploadChange () {
      this.ruleForm.img1 = ''
      this.ruleForm.img2 = ''
      this.ruleForm.img3 = ''
      this.$nextTick(() => {
        this.$refs.upload1.fileList = []
        this.$refs.upload2.fileList = []
        this.$refs.upload3.fileList = []
      })
    },
    oninput3 (num) {
      var str = num
      str = str.replace(/[^\.\d]/g, '')
      str = str.replace('.', '')

      return str
    },
    shengChange () {
      this.shiList = []
      this.xianList = []
    },
    //获取店铺分类
    async getMchFls () {
      // const res = await getMchFl({
      //   api: 'mch.Admin.Mch.MchClassList',
      //   isDisPlay: 1,
      //   pageNo: this.pageNo,
      //   pageSize: this.pageSize
      // })
      // console.log('店铺分类res', res)
      // if (res.data.code == 200) {
      //   this.chooseList = res.data.data.list
      // } else {
      //   console.log(res.data.message)
      // }
    },
    loadMore () {
      if (this.pages >= this.pageNo) {
        getMchFl({
          api: 'mch.Admin.Mch.MchClassList',
          isDisPlay: 1,
          pageNo: this.pageNo++,
          pageSize: this.pageSize
        }).then(res => {
          this.pages = Math.ceil(res.data.data.total / this.pageSize)
          this.chooseList.push(...res.data.data.list)
        })
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
    //小眼睛切换状态
    getFlag () {
      this.flag = !this.flag
      this.flagType = this.flag ? 'text' : 'password' //text为显示密码；password为隐藏密码
    },
    //身份证号正则
    oninput (str) {
      // var reg = /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/;
      return str
    },
    //手机号正则
    oninput2 (mobile) {
      return mobile

      // return /^1[3-9]\d{9}$/.test(mobile)
    },
    getBase () {
      this.goodsEditorBase = Config.baseUrl
    },

    // 发布商品
    submitForm (formName) {
      console.log('countryNum', this.countryNum)

      if (this.countryNum == '') {
        this.$message({
          message: '请选择区号！',
          type: 'error',
          offset: 102
        })
        return
      }
      let isCpc = this.restaurants.find((item) => [item.code2,item.name].includes(this.countryNum))
      console.log('isCpcisCpc',isCpc)
      if (!isCpc) {
        this.$message({
          message: '区号不正确！',
          type: 'error',
          offset: 102
        })
        return
      }

      this.$refs[formName].validate(async valid => {
        console.log(this.ruleForm)
        if (valid) {
          try {
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
            let data = {
              api: 'mch.Admin.Mch.AddMchInfo',
              name: this.ruleForm.name, //店铺名称
              cid: this.ruleForm.cid, //店铺分类
              shop_range: this.ruleForm.shop_range, //经营范围
              shop_information: this.ruleForm.shop_information, //店铺信息
              realname: this.ruleForm.realname, //用户姓名
              ID_number: this.ruleForm.ID_number, //身份证号
              tel: this.ruleForm.tel, //联系电话
              city_all: this.ruleForm.sheng?`${this.ruleForm.sheng}-${this.ruleForm.shi}-${this.ruleForm.xian}`:'', //联系地址,省市区
              address: this.ruleForm.address, //详细地址
              shop_nature: this.ruleForm.shop_nature, //所属性质
              imgUrls: imgUrls, //身份证号/企业
              account: this.ruleForm.account, //账号
              password: this.ruleForm.password, //密码
              cpc: isCpc.code2, // 国家代码

            }

            // 853 852 86
            let { entries } = Object
            let formData = new FormData()
            for (let [key, value] of entries(data)) {
              formData.append(key, value)
            }

            addStore(formData).then(res => {
              console.log('res', res)

              if (res.data.code == '200') {
                this.$message({
                  message: this.$t('zdata.tjcg'),
                  type: 'success',
                  offset: 102
                })
                this.$router.go(-1)
              }
            })
          } catch (error) {
            this.$message({
              message: error.message,
              type: 'error',
              offset: 102
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
</script>

<style scoped lang="less">
/deep/.custom-autocomplete{
  margin-right: 24px !important;
  .el-input{
    .el-input__inner{
      width: 135px !important;
    }
  }
}


.container {
  width: 100%;
  /deep/.el-form {
    padding-bottom: 38px;
    .header {
      width: 100%;
      height: 60px;
      line-height: 60px;
      border-bottom: 1px solid #e9ecef;
      padding-left: 20px;
      span {
        font-weight: 400;
        font-size: 16px;
        color: #414658;
      }
    }
    .el-input {
      width: 400px;
      height: 40px;
      input {
        width: 400px;
        height: 40px;
      }
    }
    .el-textarea {
      width: 400px;
      height: 60px;
      .el-textarea__inner {
        height: 60px;
      }
      input {
        width: 400px;
        height: 60px;
      }
    }
    .composite {
      input {
        width: 345px !important;
      }
    }

    .basic-info {
      width: 100%;
      background-color: #fff;
      margin-bottom: 16px;
      border-radius: 4px;
      .basic-block {
        margin-top: 40px;
        padding: 0 20px 20px 20px;
        .goods-img {
          width: 100%;
        }

        .goods-class {
          .el-form-item__content {
            display: flex;
            .select-input {
              width: 294px !important;
              margin-right: 10px;
              .el-input {
                width: 294px !important;
                input {
                  width: 294px !important;
                }
              }
            }
            button {
              width: 96px;
              height: 38px;
              border: 1px solid #2890ff;
              border-radius: 4px;
              background-color: #fff;
              color: #2890ff;
              margin-left: 10px;
              margin: 0;
              padding: 0;
            }
          }
        }
        .goods-area {
          .el-form-item__content {
            display: flex;
            .select-input {
              width: 126.6px !important;
              margin-right: 10px;
              .el-input {
                width: 126.6px !important;
                input {
                  width: 126.6px !important;
                }
              }
            }
            .select-last {
              width: 126.6px !important;
              .el-input {
                width: 126.6px !important;
                input {
                  width: 126.6px !important;
                }
              }
            }
          }
        }
        .goods-brand {
          .el-form-item__content {
            display: flex;
            .select-input {
              width: 294px !important;
              margin-right: 10px;
              .el-input {
                width: 294px !important;
                input {
                  width: 294px !important;
                }
              }
            }
            button {
              width: 96px;
              height: 38px;
              border: 1px solid #2890ff;
              border-radius: 4px;
              background-color: #fff;
              color: #2890ff;
              margin-left: 10px;
              margin: 0;
              padding: 0;
            }
          }
        }

        .basic-items {
          width: 100%;
          display: flex;
          justify-content: flex-start;
          .el-form-item {
            width: 33.3%;
          }
        }
      }
    }

    .goods-set {
      width: 100%;
      background-color: #fff;
      margin-bottom: 16px;
      border-radius: 4px;
      .set-block {
        margin-top: 40px;
        padding: 0 20px 25px 20px;
        color: #414658;
        margin-bottom: 53px;

        .inventory-warning {
          .el-input {
            width: 140px;
            height: 40px;
            margin: 0 8px;
            input {
              width: 140px;
              height: 40px;
            }
          }
          .grey {
            color: rgb(151, 160, 180);
            margin-left: 14px;
          }
        }

        .freight-set {
          button {
            width: 96px;
            height: 38px;
            border: 1px solid #2890ff;
            border-radius: 4px;
            background-color: #fff;
            color: #2890ff;
            margin-left: 8px !important;
            margin: 0;
            padding: 0;
          }
        }

        .activity-class {
          .el-checkbox__inner {
            border-radius: 50px;
          }
        }

        .show-local {
          .el-form-item__content {
            display: flex;
          }
          .show-font {
            color: #97a0b4;
            margin-left: 20px;
          }
        }
      }
    }

    .footer-button {
      position: fixed;
      right: 0;
      bottom: 40px;
      display: flex;
      align-items: center;
      justify-content: flex-end;
      padding: 15px 20px;
      border-top: 1px solid #e9ecef;
      background: #ffffff;
      width: 300%;
      z-index: 10;
      button {
        width: 70px;
        height: 40px;
        padding: 0;
      }
      .bgColor {
        margin-left: 14px;
      }
      .bgColor:hover {
        opacity: 0.8;
      }

      .fontColor {
        color: #6a7076;
        border: 1px solid #d5dbe8;
        margin-left: 14px;
      }
      .fontColor:hover {
        color: #2890ff;
        border: 1px solid #2890ff;
      }
    }
    .el-form-item__label {
      font-weight: normal;
    }
  }

  .dialog-class {
    /deep/.el-dialog {
      width: 640px;
      // height: 490px;
      .el-dialog__header {
        width: 100%;
        height: 58px;
        line-height: 58px;
        font-size: 16px;
        margin-left: 19px;
        font-weight: bold;
        border-bottom: 1px solid #e9ecef;
        box-sizing: border-box;
        margin: 0;
        padding: 0 0 0 19px;
        .el-dialog__headerbtn {
          font-size: 18px;
          top: 0 !important;
        }
        .el-dialog__title {
          font-weight: normal;
        }
      }
      .el-dialog__body {
        display: flex;
        justify-content: center;
        padding: 40px 20px;
        .el-form-item {
          display: flex;
          .el-form-item__content {
            width: 400px;
            margin-left: 0px !important;
          }
          .select-input {
            width: 400px;
            height: 40px;
          }
        }

        .upload-img {
          margin-bottom: 38px;
        }

        .superior {
          width: 400px;
          display: flex;
          .el-select {
            flex: 1;
            &:not(:first-child) {
              margin-left: 8px;
            }
            .el-input {
              width: 100%;
              input {
                width: 100%;
              }
            }
          }
        }

        .upload-img {
          .el-form-item__content {
            display: flex;
            align-items: center;
            .avatar-uploader .el-upload {
              border: 1px dashed #d9d9d9;
              border-radius: 6px;
              cursor: pointer;
              position: relative;
              overflow: hidden;
            }
            .avatar-uploader .el-upload:hover {
              border-color: #409eff;
            }
            .avatar-uploader-icon {
              font-size: 28px;
              color: #8c939d;
              width: 80px;
              height: 80px;
              line-height: 80px;
              text-align: center;
            }
            .avatar {
              width: 80px;
              height: 80px;
              display: block;
            }
            .removeImg {
              position: absolute;
              right: 0;
              top: 0;
            }

            .text {
              margin-bottom: 10px;
              margin-left: 5px;
            }
          }
        }
        .form-footer {
          width: 100%;
          height: 72px;
          position: absolute;
          bottom: 0;
          right: 0;
          border-top: 1px solid #e9ecef;
          .el-form-item {
            width: 100%;
            padding: 0 !important;
            height: 100%;
            .el-form-item__content {
              height: 100%;
              width: 100%;
              line-height: 72px;
              margin: 0 !important;
              text-align: right;
              padding-right: 20px;
            }
          }
        }
      }
    }
  }

  .dialog-brand {
    /deep/.el-dialog {
      width: 640px;
      height: 550px;
      .el-dialog__header {
        width: 100%;
        height: 58px;
        line-height: 58px;
        font-size: 16px;
        margin-left: 19px;
        font-weight: bold;
        border-bottom: 1px solid #e9ecef;
        box-sizing: border-box;
        margin: 0;
        padding: 0 0 0 19px;
        .el-dialog__headerbtn {
          font-size: 18px;
          top: 0 !important;
        }
        .el-dialog__title {
          font-weight: normal;
        }
      }
      .el-dialog__body {
        display: flex;
        justify-content: center;
        padding: 40px 20px;
        .el-form {
          padding-bottom: 0;
        }
        .el-form-item {
          display: flex;
          .el-form-item__content {
            width: 400px;
            margin-left: 0px !important;
          }
          .select-input {
            width: 400px;
            height: 40px;
          }

          .belongClass {
            height: auto;
            .el-input {
              height: auto;
            }
            .el-tag.el-tag--info .el-tag__close {
              color: #ffffff;
            }
          }
        }

        .upload-img {
          .el-form-item__content {
            display: flex;
            align-items: center;
            .avatar-uploader .el-upload {
              border: 1px dashed #d9d9d9;
              border-radius: 6px;
              cursor: pointer;
              position: relative;
              overflow: hidden;
            }
            .avatar-uploader .el-upload:hover {
              border-color: #409eff;
            }
            .avatar-uploader-icon {
              font-size: 28px;
              color: #8c939d;
              width: 80px;
              height: 80px;
              line-height: 80px;
              text-align: center;
            }
            .avatar {
              width: 80px;
              height: 80px;
              display: block;
            }
            .removeImg {
              position: absolute;
              right: 0;
              top: 0;
            }

            .text {
              margin-bottom: 10px;
              margin-left: 5px;
            }
          }
        }

        .form-footer {
          width: 100%;
          height: 72px;
          position: absolute;
          bottom: 0;
          right: 0;
          border-top: 1px solid #e9ecef;
          .el-form-item {
            width: 100%;
            padding: 0 !important;
            height: 100%;
            .el-form-item__content {
              height: 100%;
              width: 100%;
              line-height: 72px;
              margin: 0 !important;
              text-align: right;
              padding-right: 20px;
            }
          }
        }
      }
    }
  }

  .dialog-freight {
    // 弹框样式
    /deep/.el-dialog {
      width: 724px;
      height: 680px;
      position: absolute;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      margin: 0 !important;
      .el-dialog__header {
        width: 100%;
        height: 58px;
        line-height: 58px;
        font-size: 16px;
        margin-left: 19px;
        font-weight: bold;
        border-bottom: 2px solid #e9ecef;
        box-sizing: border-box;
        margin: 0;
        padding: 0 0 0 19px;
        .el-dialog__headerbtn {
          font-size: 18px;
          top: 0 !important;
        }
        .el-dialog__title {
          font-weight: normal;
        }
      }

      .el-dialog__body {
        .el-form {
          padding-bottom: 0px !important;
        }
        .notice {
          padding: 40px 0 0 60px;
          display: flex;
          flex-direction: column;
          .title {
            .el-form-item__label {
              font-weight: normal;
            }
            .el-form-item__label {
              color: #414658;
            }
            .el-form-item__content {
              display: flex;
              input {
                width: 420px;
                height: 40px;
              }
            }
          }
        }
        .form-footer {
          width: 174px;
          height: 72px;
          position: absolute;
          bottom: 0;
          right: 0;
          .el-form-item {
            padding: 0 !important;
            height: 100%;
            .el-form-item__content {
              height: 100%;
              line-height: 72px;
              margin: 0 !important;
            }
          }
          .bgColor:hover {
            background-color: #fff;
            color: #2890ff;
            border: 1px solid #2890ff;
          }
        }

        .dictionary-list {
          width: 558px !important;
          border-radius: 4px;
          margin-left: 140px;
          .el-table {
            .el-table__header-wrapper {
              thead {
                tr {
                  background-color: #f4f7f9 !important;
                  th {
                    height: 61px;
                    text-align: center;
                    font-size: 14px;
                    font-weight: bold;
                    color: #414658;
                    background-color: #f4f7f9 !important;
                  }
                }
              }
            }
            .el-table__body-wrapper {
              background-color: #f4f7f9;
              tbody {
                tr {
                  td {
                    height: 92px;
                    text-align: center;
                    font-size: 14px;
                    color: #414658;
                    font-weight: 400;
                    padding: 0;
                  }
                }
              }
            }
            button {
              padding: 5px;
              height: 22px;
              background: #ffffff;
              border: 1px solid #d5dbe8;
              border-radius: 2px;
              font-size: 12px;
              font-weight: 400;
              color: #888f9e;
            }
            button:hover {
              border: 1px solid rgb(64, 158, 255);
              color: rgb(64, 158, 255);
            }
            button:hover i {
              color: rgb(64, 158, 255);
            }
          }
        }
      }
    }
  }
  /deep/ .input-interval-block{
          display: flex;
          .auto{
            margin-right: 8px;
            .el-input {
              width: 120px !important;
            }
          }
          >.el-input,input {
            width: 272px !important;
          }
        }

  .dialog-freightTemplate {
    // 弹框样式
    /deep/.el-dialog {
      width: 680px;
      height: 680px;
      position: absolute;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      margin: 0 !important;
      .el-dialog__header {
        width: 100%;
        height: 58px;
        line-height: 58px;
        font-size: 16px;
        margin-left: 19px;
        font-weight: bold;
        border-bottom: 2px solid #e9ecef;
        box-sizing: border-box;
        margin: 0;
        padding: 0 0 0 19px;
        .el-dialog__headerbtn {
          font-size: 18px;
          top: 0 !important;
        }
        .el-dialog__title {
          font-weight: normal;
        }
      }

      .el-dialog__body {
        padding: 41px 0px 0px 0px !important;
        // border-bottom: 1px solid #E9ECEF;
        .check-provinces {
          .el-checkbox {
            width: 120px;
            height: 30px;
            margin-right: 14px;
          }
          .el-form-item__content {
            padding-top: 5px;
          }
        }
        .el-form-item__content {
          line-height: 30px;
        }
        .el-form-item {
          margin-bottom: 12px;
        }
        .el-form-item__label {
          font-weight: normal;
        }
        .el-input__inner {
          width: 304px;
          height: 40px;
        }
        .form-footer {
          width: 100%;
          height: 72px;
          position: absolute;
          bottom: 0;
          right: 0;
          border-top: 1px solid #e9ecef;
          .el-form-item {
            padding: 0 !important;
            height: 100%;
            display: flex;
            justify-content: flex-end;
            margin-right: 17px;
            .el-form-item__content {
              height: 100%;
              line-height: 72px;
              margin: 0 !important;
            }
          }
          .bgColor:hover {
            background-color: #fff;
            color: #2890ff;
            border: 1px solid #2890ff;
          }
        }
      }
    }
  }

  /deep/.el-form-item__label {
    color: #414658;
  }
  /deep/.el-input__inner {
    border: 1px solid #d5dbe8;
  }
  /deep/.el-input__inner:hover {
    border: 1px solid #b2bcd4;
  }
  /deep/.el-input__inner:focus {
    border-color: #409eff;
  }
  /deep/.el-input__inner::-webkit-input-placeholder {
    color: #97a0b4;
  }

  .model {
    position: fixed;
    left: 0;
    top: 0;
    width: 100%;
    height: 100%;
    opacity: 0.5;
    background: #000;
    z-index: 2000;
  }
}
</style>

<style>
.zZindex {
  z-index: 99999999 !important;
}
.maskNew {
  position: fixed;
  z-index: 10000;
  top: 0;
  bottom: 0;
  left: 0;
  right: 0;
  background: rgba(0, 0, 0, 0.6);
  display: block;
}

/* 添加属性弹窗 */
.maskNew .mask-content {
  border-radius: 4px;
  position: relative;
  background-color: #fff;
  display: flex !important;
  flex-direction: column;
  padding-top: 0;
  width: 680px;
  height: 510px !important;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  margin: 0 !important;
}
.maskNew .mask-title {
  line-height: 58px;
  border-bottom: 1px solid #e9ecef;
  font-size: 16px;
  color: #414658;
  padding-left: 19px;
  margin-bottom: 0;
}
.maskNew .mask-content-data {
  padding: 40px 40px 0;
  overflow: hidden;
  flex: 1;
}
.maskNew .mask-content-data > div {
  display: flex;
}
.maskNew .shooser_attrDiv {
  width: 520px;
  height: 272px;
  background: #f4f7f9;
  margin-top: 10px;
  overflow-x: hidden;
  overflow-y: auto;
  padding: 15px;
  box-sizing: border-box;
  font-size: 14px;
}
.maskNew .shooser_attrDiv select {
  width: 240px;
  height: 36px;
  font-size: 14px;
  color: #414658 !important;
}
.maskNew .shooser_attrDiv > div,
#choose_attrDiv .custom_attr {
  display: flex;
  align-items: center;
}
.maskNew .shooser_attrDiv ul,
.shooser_attrDiv label {
  margin-bottom: 0;
}
.maskNew .shooser_attrDiv ul {
  width: 100%;
  max-height: 171px;
  position: absolute;
  background: #ffffff;
  border: 1px solid #d3dae3;
  border-top: 0;
  overflow-y: auto;
  background: #ffffff;
  z-index: 99;
}
.maskNew .shooser_attrDiv li {
  height: 34px;
}
.maskNew .shooser_attrDiv li:hover {
  background: #f4f7f9;
}
.maskNew .shooser_attrDiv li > label {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  padding: 10px;
  box-sizing: border-box;
}
.maskNew .attr_title {
  width: 90px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid #2890ff;
  border-radius: 2px;
  color: #2890ff;
  position: relative;
  margin: 20px 0;
  background: #ffffff;
}
.maskNew .attr_title img {
  position: absolute;
  right: 0;
  top: 0;
  width: 12px;
  height: 12px;
}
.maskNew .attr_content {
  display: flex;
  flex-wrap: wrap;
}
.maskNew .attr_content label {
  margin-right: 20px;
  margin-bottom: 10px;
}
.maskNew .custom_attrDiv .left_text {
  display: inline-block;
  width: 70px;
  text-align: right;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.maskNew .custom_attrDiv input[type='text'] {
  padding-left: 10px;
}
.maskNew .custom_attrDiv a.addBtn,
.custom_attrDiv a.removeBtn {
  display: inline-block;
  width: 88px;
  height: 38px;
  box-sizing: border-box;
  line-height: 38px;
  text-align: center;
  margin-left: 10px;
}
.maskNew .custom_attrDiv a.addBtn {
  color: #2890ff;
  border: 1px solid #2890ff;
}
.maskNew .custom_attrDiv a.removeBtn {
  color: #828b97;
  border: 1px solid #828b97;
}
.maskNew .custom_attrVlue {
  flex-wrap: wrap;
}
.maskNew .custom_attr {
  display: flex;
}
.maskNew .custom_attrVlue a {
  display: flex;
  align-items: center;
  height: 36px;
  padding: 0 10px;
  background: #ffffff;
  color: #414658;
  border: 1px dashed #b2bcd1;
  margin-bottom: 10px;
  margin-right: 10px;
}
.maskNew .custom_attrVlue a img {
  width: 12px;
  height: 12px;
  margin-left: 10px;
}

.maskNew .mask-bottom {
  display: flex;
  justify-content: flex-end;
  border-top: 1px solid #e9ecef;
  margin-top: auto;
}
.maskNew .mask-bottom input[type='button'] {
  width: 70px;
  height: 40px;
  line-height: 40px;
  text-align: center;
  margin: 16px 0;
  border: 0;
  outline: 0;
  cursor: pointer;
}
.maskNew .mask-bottom input[type='button']:first-child {
  border: 1px solid #d5dbe8;
  color: #6a7076;
  margin-right: 10px;
  background: #ffffff;
  border-radius: 4px;
}
.maskNew .mask-bottom input[type='button']:last-child {
  background: #2890ff;
  color: #ffffff;
  margin-right: 20px;
  border-radius: 4px;
}

.maskNew .maskContent1 input[type='submit'] {
  width: 100px;
  height: 40px;
  border: 1px solid #eee;
  border-radius: 5px;
  background: #008def;
  color: #fff;
  font-size: 16px;
  line-height: 40px;
  display: inline-block;
}

.maskNew .closeMaskBtn {
  width: 100px;
  height: 40px;
  border-radius: 5px;
  background: #fff;
  color: #008def;
  border: 1px solid #008def;
  font-size: 16px;
  line-height: 40px;
  display: inline-block;
  text-align: center;
  box-sizing: border-box;
  cursor: pointer;
  margin-right: 10px;
}

.maskNew .closeA {
  position: absolute;
  right: 10px;
  top: 15px;
  width: 30px;
  height: 30px;
  color: #eee;
}

.maskNew .checkbox {
  position: relative;
  display: inline-block;
  color: #414658;
  font-size: 14px;
  line-height: 14px;
  padding-left: 22px;
  -webkit-user-select: none;
  -moz-user-select: none;
  -ms-user-select: none;
  user-select: none;
}

.maskNew .checkbox i {
  position: absolute;
  display: block;
  left: 0;
  top: 0;
  width: 14px;
  height: 14px;
  border: 1px solid #b2bcd1;
  border-radius: 2px;
  box-sizing: border-box;
}

.attr_close_btn:hover {
  color: #2890ff !important;
  border: 1px solid #2890ff !important;
}

.attr_sava_btn:hover {
  background-color: #70aff3 !important;
}

.attr-input {
  height: 40px;
  border: 1px solid #d5dbe8;
  border-radius: 4px;
}

.attr-input::-webkit-input-placeholder {
  color: #97a0b4;
}

.attr-input:focus {
  outline: none;
  border: 1px solid #2890ff !important;
}

#searchAttrIpt {
  height: 40px;
  border: 1px solid #d5dbe8;
  border-radius: 4px;
}

#searchAttrIpt::-webkit-input-placeholder {
  color: #97a0b4;
}

#searchAttrIpt:focus {
  outline: none;
  border: 1px solid #2890ff !important;
}

#chooseAttr {
  border: 1px solid #d5dbe8;
  border-radius: 4px;
}

.addBtn,
.removeBtn {
  border-radius: 4px;
}
</style>

<style scoped lang="less">
/deep/.attribute-table {
  width: 90% !important;
  margin-bottom: 40px;
  .el-table__body-wrapper {
    border-bottom: 1px solid #edf1f5;
  }
  .el-form-item__content {
    width: 100%;
  }
  .el-table--scrollable-x .el-table__body-wrapper {
    overflow-x: auto;
  }
  .el-table .hidden-columns {
    visibility: hidden;
    position: absolute;
    z-index: -1;
  }
  .el-table--fit td.gutter,
  .el-table--fit th.gutter {
    border-right-width: 1px;
  }
  .el-table--scrollable-x .el-table__body-wrapper {
    overflow-x: auto;
  }
  .el-table--scrollable-y .el-table__body-wrapper {
    overflow-y: auto;
  }
  .el-table thead {
    color: #414658;
    font-weight: 500;
  }
  .el-table thead.is-group th {
    background: #f4f7f9;
  }
  .el-table td,
  .el-table th {
    padding: 12px 0;
    min-width: 0;
    -webkit-box-sizing: border-box;
    box-sizing: border-box;
    text-overflow: ellipsis;
    vertical-align: middle;
    position: relative;
    text-align: center;
  }
  thead th {
    padding: 0 !important;
    height: 50px;
  }

  .el-table {
    .el-input {
      text-align: center;
      width: 140px;
      input {
        width: 140px;
        text-align: center;
      }
    }
  }

  .el-table .l-upload {
    display: flex;
    justify-content: center;
  }

  .el-table th {
    background-color: #f4f7f9;
  }
}
/deep/ .el-autocomplete-suggestion{
    width: 280px !important;

  }
</style>
