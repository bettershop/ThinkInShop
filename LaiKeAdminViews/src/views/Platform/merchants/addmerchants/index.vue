  <template>
  <div class="container">
    <el-form ref="ruleForm" class="form-search" :rules="rules" :model="ruleForm" label-width="100px">
      <div class="basic-info">
        <div class="header">
          <span>{{$t('merchants.addmerchants.jcxx')}}</span>
        </div>
        <div class="basic-block">
          <el-form-item :label="$t('merchants.addmerchants.scmc')" prop="storeName">
						<el-input v-model="ruleForm.storeName" :placeholder="$t('merchants.addmerchants.qsrscmc')"></el-input>
					</el-form-item>
          <el-form-item :label="$t('merchants.addmerchants.scbh')" prop="storeNo">
						<el-input v-model="ruleForm.storeNo" :placeholder="$t('merchants.addmerchants.qsrscbh')"></el-input>
					</el-form-item>
          <el-form-item :label="$t('merchants.addmerchants.gsmc')" prop="company">
						<el-input v-model="ruleForm.company" :placeholder="$t('merchants.addmerchants.qsrgsmc')"></el-input>
					</el-form-item>
          <el-form-item :label="$t('merchants.addmerchants.sjh')" prop="mobile">
						<el-input v-model="ruleForm.mobile" :placeholder="$t('merchants.addmerchants.qsrsjh')"></el-input>
					</el-form-item>
          <el-form-item :label="$t('merchants.addmerchants.jg')" prop="price">
						<el-input class="composite" v-model="ruleForm.price" :placeholder="$t('merchants.addmerchants.qsrjg')" @keyup.native="ruleForm.price = oninput(ruleForm.price,2)">
              <template slot="append">{{$t('merchants.addmerchants.yuan')}}</template>
            </el-input>
					</el-form-item>
          <el-form-item :label="$t('merchants.addmerchants.yx')" >
						<el-input v-model="ruleForm.email" :placeholder="$t('merchants.addmerchants.qsryx')"></el-input>
					</el-form-item>
          <el-form-item :label="$t('merchants.addmerchants.sczh')" prop="adminAccount">
						<el-input v-model="ruleForm.adminAccount" :placeholder="$t('merchants.addmerchants.qsrglyzh')"></el-input>
					</el-form-item>
          <el-form-item :label="$t('merchants.addmerchants.scmm')" prop="adminPwd">
						<el-input show-password v-model="ruleForm.adminPwd" @keyup.native="ruleForm.adminPwd = oninput3(ruleForm.adminPwd)" :placeholder="$t('merchants.addmerchants.qsrglymm')"></el-input>
					</el-form-item>
          <el-form-item :label="$t('merchants.addmerchants.dqsj')" prop="endDate">
						<el-date-picker
              v-model="ruleForm.endDate"
              type="datetime"
              value-format='yyyy-MM-dd HH:mm:ss'
              :placeholder="$t('merchants.addmerchants.qxzdqsj')">
            </el-date-picker>
					</el-form-item>
          <div style="width: 100%;">
            <el-form-item :label="$t('merchants.addmerchants.sfqy')" prop="isOpen">
              <el-switch v-model="ruleForm.isOpen" :active-value="0" :inactive-value="2" active-color="#00ce6d" inactive-color="#d4dbe8">
              </el-switch>
            </el-form-item>

          </div>
        </div>
      </div>
      <div class="configuration-info">
        <div class="header">
          <span>{{$t('merchants.addmerchants.pzxx')}}</span>
        </div>
        <div class="configuration-block">
          <!-- <el-form-item :label="$t('merchants.addmerchants.scgml')" prop="storeDomain">
						<el-input v-model="ruleForm.storeDomain" oninput="if(value.length > 100)value = value.slice(0, 100)" :placeholder="$t('merchants.addmerchants.qsrscgml')"></el-input>
					</el-form-item> -->
          <el-form-item :label="$t('yz')" prop="store_langs">
            <el-select class="select-input" multiple filterable v-model="ruleForm.store_langs" :placeholder="$t('qxzyz')">
              <el-option v-for="(item,index) in languages" :key="index" :label="item.lang_name" :value="item.id">
                <div >{{ item.lang_name }}</div>
              </el-option>
            </el-select>
          </el-form-item>

          <!-- 设置默认语言（从已选中中选择） -->
          <!-- <el-form-item :label="$t('mryz')" prop="default_lang">
            <el-select
              class="select-input"
              filterable
              v-model="ruleForm.default_lang"
              :placeholder="$t('qxzmryz')"
            >
              <el-option
                v-for="langId in ruleForm.store_langs"
                :key="langId"
                :label="getLangName(langId)"
                :value="langId"
              />
            </el-select>
          </el-form-item> -->

          <el-form-item :label="$t('bz')" prop="store_currencys">
            <el-select class="select-input" multiple filterable v-model="ruleForm.store_currencys" :placeholder="$t('qxzbz')">
              <el-option v-for="(item,index) in currencys" :key="index" :label="item.currency_name" :value="item.id">
                <div >{{ item.currency_name }}</div>
              </el-option>
            </el-select>
          </el-form-item>

          <!-- 设置默认币种（从已选中中选择） -->
          <!-- <el-form-item :label="$t('mrbz')" prop="default_currency">
            <el-select
              class="select-input"
              filterable
              v-model="ruleForm.default_currency"
              :placeholder="$t('qxzmrbz')"
            >
              <el-option
                v-for="currencyId in ruleForm.store_currencys"
                :key="currencyId"
                :label="getCurrencyName(currencyId)"
                :value="currencyId"
              />
            </el-select>
          </el-form-item> -->

        </div>
      </div>

      <div class="footer-button">
        <el-button plain class="footer-cancel fontColor" @click="$router.go(-1)">{{$t('merchants.addmerchants.cancel')}}</el-button>
        <el-button type="primary" class="footer-save bgColor mgleft" @click="submitForm('ruleForm')">{{$t('merchants.addmerchants.save')}}</el-button>
      </div>
    </el-form>
    <div class="role-authorization">
      <el-form :rules="rules2" :model="ruleForm2" label-width="100px">
        <div class="header">
          <span>{{$t('merchants.addmerchants.jsqx')}}</span>
        </div>
        <div class="role-block">
          <el-form-item :label="$t('merchants.addmerchants.bdjs')" prop="region">
						<el-select class="select-input" v-model="ruleForm2.region" :placeholder="$t('merchants.addmerchants.qxzjs')">
              <el-option v-for="item in Dictionary" :key="item.id" :label="item.name" :value="item.name">
                <div @click="change(item.id)">{{ item.name }}</div>
              </el-option>
            </el-select>
            <span class="prompt">{{$t('merchants.addmerchants.bdjsbz')}}</span>
					</el-form-item>
          <el-form-item :label="$t('merchants.addmerchants.bdqx')" prop="">
            <el-tree
            v-if="treeData.length>0"
              :data="treeData"
              :show-checkbox="false"
              node-key="id"
              :props="defaultProps">
            </el-tree>
            <div v-else style="color:#999;width:402px;height:60px;line-height:60px;text-align:center">
              {{ $t('zdata.zwsj') }}
            </div>
          </el-form-item>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script>
import { addStore, getUserRoleInfo } from '@/api/Platform/merchants'
import { getRoleListInfo } from '@/api/Platform/permissions'
import { getItuList } from '@/api/members/membersSet'
export default {
  name: 'addmerchants',
  data() {
    // 只能输入数字和字母
    var validatePass3 = (rule,value,callback) => {
      console.log('8148148148',/^[\da-z]+$/i.test(value));
      if(/^[\da-z]+$/i.test(value)){
        callback();
      }else{
        callback(new Error(this.$t('merchants.znsrszhzm')));
      }
    };
    return {
      treeData: [],
      defaultProps: {
        children: 'children',
        label: 'title',
        disabled: true
      },
      languages: [],
      currencys: [],
      store_langs:"",
      store_currencys:"",
      default_lang: null,       // 默认语言ID
      default_currency: null,   // 默认币种ID
      ruleForm: {
        // 基础信息

        storeName: '',
        storeNo: '',
        company: '',
        mobile: '',
        price: '',
        email: '',
        adminAccount: '',
        adminPwd: '',
        endDate: '',
        isOpen: 0,

        // 配置信息
        storeDomain: '',
        contactAddress: '',
        contactNumber: '',
        copyrightInformation: '',
        recordInformation: '',
        website: '',
        logoUrl: '',
        adminDefaultPortrait: '',
        default_lang: null,       // 默认语言ID
        default_currency: null,   // 默认币种ID
        store_langs:'',
        store_currencys:'',

      },

      Dictionary: [],
      id: '',
      cpc: '', //国家区号
      restaurants: [], //异步查询建议列表
      rules: {
        storeName: [
          {required: true, message: this.$t('merchants.addmerchants.qsrscmc'), trigger: 'blur'}
        ],
        storeNo: [
          {required: true, message: this.$t('merchants.addmerchants.qsrscbh'), trigger: 'blur'},
          {validator:validatePass3,trigger: "change" }
        ],
        company: [
          {required: true, message: this.$t('merchants.addmerchants.qsrgsmc'), trigger: 'blur'}
        ],
        mobile: [
          {required: true, message: this.$t('merchants.addmerchants.qsrsjh'), trigger: 'blur'}
        ],
        price: [
          {required: true, message: this.$t('merchants.addmerchants.qsrjg'), trigger: 'blur'}
        ],
        email: [
          {required: true, message: this.$t('merchants.addmerchants.qsryx'), trigger: 'blur'}
        ],
        adminAccount: [
          {required: true, message: this.$t('merchants.addmerchants.qsrglyzh'), trigger: 'blur'}
        ],
        adminPwd: [
          {required: true, message: this.$t('merchants.addmerchants.qsrglymm'), trigger: 'blur'}
        ],
        endDate: [
          {required: true, message: this.$t('merchants.addmerchants.qxzdqsj'), trigger: 'change'}
        ],
        storeDomain: [
          {required: true, message: this.$t('merchants.addmerchants.qsrscgml'), trigger: 'blur'}
        ],
        copyrightInformation: [
          {required: true, message: this.$t('merchants.addmerchants.qsrbqxx'), trigger: 'blur'}
        ],
        recordInformation: [
          {required: true, message: this.$t('merchants.addmerchants.qsrbaxx'), trigger: 'blur'}
        ],
        store_langs:[
          {required: true, message: this.$t('qxzyz'), trigger: ['blur', 'change'] }
        ],
        store_currencys:[
          {required: true, message: this.$t('qxzbz'),trigger: ['blur', 'change'] }
        ],

        default_lang:[
          {required: true, message: this.$t('qxzmryz'), trigger: ['blur', 'change'] }
        ],
        default_currency:[
          {required: true, message: this.$t('qxzmrbz'), trigger: ['blur', 'change'] }
        ]

      },

      ruleForm2: {
        // 角色权限
        region: ''
      },

      rules2: {
        region: [
          {required: true, message: this.$t('merchants.addmerchants.qxzjs'), trigger: 'blur'}
        ],
      }
    }
  },

  created() {
    this.getRoleListInfos()
    this.getAllLanguages();
    this.getAllCurrencys();
    this.queryAdd()
  },

  watch: {
    'ruleForm.logoUrl'(val){
      this.$refs.ruleForm.validateField('logoUrl')
    },
    'ruleForm.adminDefaultPortrait'(){
      this.$refs.ruleForm.validateField('adminDefaultPortrait')
    },
    id() {
      this.getUserRoleInfos();
    },
    'ruleForm.store_currencys'(val) {
      if (!val || val.length === 0) {
        this.ruleForm.default_currency = [];
      } else if (!val.includes(this.default_currency)) {
        // 如果当前默认币种已不在可选项中，自动清空
        this.ruleForm.default_currency = [];
      }
    },
    'ruleForm.store_langs'(val) {
      if (!val || val.length === 0) {
        this.ruleForm.default_lang = [];
      } else if (!val.includes(this.default_lang)) {
        // 如果当前默认语种已不在可选项中，自动清空
        this.ruleForm.default_lang = [];
      }
    }
  },

  methods: {
        getLangName(id) {
          const item = this.languages.find(i => i.id === id);
          return item ? item.lang_name: id;
        },
        getLangCode(id) {
          const item = this.languages.find(i => i.id === id);
          return item ? item.lang_code: id;
        },
        getCurrencyName(id) {
          const item = this.currencys.find(i => i.id === id);
          return item ? item.currency_name : id;
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
        queryAdd() {
          const data = {
            api: 'admin.user.getItuList',
            keyword: this.keyword
          }
          getItuList(data).then(res => {
            if (res.data.code == 200) {
              this.restaurants = res.data.data
              console.log('this.restaurants', this.restaurants)
              sessionStorage.setItem('restaurants', JSON.stringify(this.restaurants))
            }
          })
        },
        // 选择建议项时触发的方法
        handleSelect(item) {
          console.log('选中的项:', item);
          this.state = item.code2; // 可以根据需求更新输入框显示的值
          this.cpc = item.code2;
        },
    disabledFn() {
      // if(this.operate === 'view') {
      //   return true
      // }
      return false
    },

    // 获取角色列表
    async getRoleListInfos() {
      const res = await getRoleListInfo({
        api: 'saas.role.getRoleListInfo',
      })
      console.log(res);
      this.Dictionary = res.data.data.list
    },

    async getAllLanguages(){
      const res = await this.LaiKeCommon.select({
        api: 'admin.lang.index',
        pageNo: 1,
        pageSize: 300
      })
      this.languages = res.data.data.list
      if(this.languages.length>0){
        this.ruleForm.store_langs = this.languages.map(v=>v.id)
      }
    },

    async getAllCurrencys(){
      const res = await this.LaiKeCommon.select({
        api: 'admin.currency.currencyList',
        pageNo: 1,
        pageSize: 300
      })
      this.currencys = res.data.data.list
      if(this.currencys.length>0){
        this.ruleForm.store_currencys = this.currencys.map(v=>v.id)
      }
    },

    // 根据角色获取权限
    async getUserRoleInfos() {
      const res = await getUserRoleInfo({
        api: 'admin.role.getUserRoleInfo',
        id: this.id
      })
      this.treeData = res.data.data.menuList.filter(item => {
        if(item.checked) {
          // this.filterChildren(item)
          let list
          list = item.children.filter(item => {
            if(item.checked) {

              return item
            }
          })
          item.children=list
          return item
        }
      })
      console.log(res);
    },

    change(value) {
      this.id = value
    },

    submitForm(formName) {
      this.$refs[formName].validate(async (valid) => {
        console.log(this.ruleForm,this.id);

        if (valid) {
          try {
            if(this.ruleForm.storeName.length > 50) {
              this.$message({
                message: '商城名称不能大于50个字符',
                type: 'error',
                offset: 100
              })
              return
            }
            if(!this.ruleForm2.region){
              this.$message({
                message: '请选择绑定角色',
                type: 'error',
                offset: 100
              })
              return
            }
            addStore({
              api: 'saas.shop.addStore',
              id: "",
              store_langs: this.ruleForm.store_langs.join(','),
              store_currencys: this.ruleForm.store_currencys.join(','),
              default_lang: this.getLangCode(this.ruleForm.default_lang),
              default_currency: this.ruleForm.default_currency,
              storeName: this.ruleForm.storeName,
              storeNo: this.ruleForm.storeNo,
              company: this.ruleForm.company,
              mobile: this.ruleForm.mobile,
              price: this.ruleForm.price,
              email: this.ruleForm.email,
              adminAccount: this.ruleForm.adminAccount,
              adminPwd: this.ruleForm.adminPwd,
              endDate: this.ruleForm.endDate,
              isOpen: this.ruleForm.isOpen,
              storeDomain: this.ruleForm.storeDomain,
              logUrl: this.ruleForm.logoUrl,
              adminDefaultPortrait: this.ruleForm.adminDefaultPortrait,

              website: this.ruleForm.website,
              recordInformation: this.ruleForm.recordInformation,
              copyrightInformation: this.ruleForm.copyrightInformation,
              contactNumber: this.ruleForm.contactNumber,
              contactAddress: this.ruleForm.contactAddress,
              roleId: this.id,
              cpc:this.cpc
            }).then(res => {
              if(res.data.code == '200') {
                this.$message({
                  message: '添加成功',
                  type: 'success',
                  offset: 100
                })
                location.reload();
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
            console.log('error submit!!');
            return false;
        }
      });
    },

    oninput(num, limit) {
      var str = num
      var len1 = str.substr(0, 1)
      var len2 = str.substr(1, 1)
      //如果第一位是0，第二位不是点，就用数字把点替换掉
      if (str.length > 1 && len1 == 0 && len2 != ".") {
        str = str.substr(1, 1)
      }
      //第一位不能是.
      if (len1 == ".") {
        str = ""
      }
      //限制只能输入一个小数点
      if (str.indexOf(".") != -1) {
        var str_ = str.substr(str.indexOf(".") + 1)
        if (str_.indexOf(".") != -1) {
          str = str.substr(0, str.indexOf(".") + str_.indexOf(".") + 1)
        }
      }
      //正则替换
      str = str.replace(/[^\d^\.]+/g, '') // 保留数字和小数点
      if (limit / 1 === 1) {
        str = str.replace(/^\D*([0-9]\d*\.?\d{0,1})?.*$/,'$1') // 小数点后只能输 1 位
      } else {
        str = str.replace(/^\D*([0-9]\d*\.?\d{0,2})?.*$/,'$1') // 小数点后只能输 2 位
      }

      if(this.ruleForm.price_type == 1 && this.totlePrice !== 0 && parseInt(this.ruleForm.price) > this.totlePrice) {
          str = this.totlePrice
      }
      return str
    },
  }
}
</script>

<style scoped  lang="less">
/deep/ .el-select__tags {
  max-width: none !important;
}
.container {
  width: 100%;
  /deep/.el-form {
    .header {
      width: 100%;
      height: 60px;
      line-height: 60px;
      border-bottom: 1px solid #E9ECEF;
      padding-left: 20px;
      span {
        font-weight: 400;
        font-size: 16px;
        color: #414658;
      }
    }
    .el-input {
      width: auto;
      height: 40px;
      input {
        width: 400px;
        height: 40px;
      }
    }
    .composite {
      input {
        width: 345px !important;
      }
    }


    //.basic-info {
    //  width: 100%;
    //  background-color: #fff;
    //  margin-bottom: 16px;
    //  border-radius: 4px;
    //  .basic-block {
    //    margin-top: 40px;
    //    display: flex;
    //    flex-wrap: wrap;
    //    justify-content: space-between;
    //    padding: 0 20px 20px 52px;
    //  }
    //}

    .basic-info {
      width: 100%;
      background-color: #fff;
      margin-bottom: 16px;
      border-radius: 4px;
    }

    .basic-info .basic-block {
      margin-top: 40px;
      display: grid;
      grid-template-columns: repeat(3, 1fr); /* 三列布局 */
      gap: 20px 24px; /* 行间距 / 列间距 */
      padding: 0 20px 20px 52px;

      /* 响应式：屏幕 < 1024px 自动两列 */
      @media (max-width: 1024px) {
        grid-template-columns: repeat(2, 1fr);
      }

      /* 响应式：屏幕 < 768px 自动一列 */
      @media (max-width: 768px) {
        grid-template-columns: 1fr;
      }

      .el-form-item {
        display: flex;
        align-items: flex-start;
        margin-bottom: 0; /* grid 控制间距 */
        max-width: 100%;
        box-sizing: border-box;
      }

      .el-form-item__label {
        width: 130px !important;
        white-space: nowrap;
        text-align: right;
        padding-right: 10px;
      }

      .el-form-item__content {
        flex: 1;
        margin-left: 0 !important;
        display: flex;
        align-items: center;
        justify-content: flex-start;
        min-width: 0;
        max-width: 100%;
        box-sizing: border-box;
      }
    }

    .configuration-info {
      width: 100%;
      background-color: #fff;
      margin-bottom: 16px;
      border-radius: 4px;

      .configuration-block {
        margin-top: 40px;
        display: grid;
        grid-template-columns: repeat(3, 1fr); /* 三列布局 */
        gap: 20px 24px; /* 行间距 / 列间距 */
        padding: 0 20px 20px 23px;

        /* 响应式：屏幕 < 1024px 自动两列 */
        @media (max-width: 1024px) {
          grid-template-columns: repeat(2, 1fr);
        }

        /* 响应式：屏幕 < 768px 自动一列 */
        @media (max-width: 768px) {
          grid-template-columns: 1fr;
        }

        .el-form-item {
          display: flex;
          align-items: flex-start;
          margin-bottom: 0; /* grid 已经控制间距 */
          max-width: 100%;
          box-sizing: border-box;
        }

        .el-form-item__label {
          width: 130px !important;
          white-space: nowrap;
          text-align: right;
          padding-right: 10px;
        }

        .el-form-item__content {
          flex: 1;
          margin-left: 0 !important;
          display: flex;
          align-items: center;
          justify-content: flex-start;
          min-width: 0; /* 防止 flex 子项溢出 */
          max-width: 100%; /* 防止内容超出容器 */
          box-sizing: border-box;
        }

        /* 联系电话 */
        .phone {
          display: flex;
          width: 100%;
          box-sizing: border-box;

          .custom-autocomplete {
            flex: 0 0 120px; /* 固定区号宽度 */
            box-sizing: border-box;
            margin-right: 5px; /* 内部间距 */
          }

          .contactNumber {
            flex: 1 1 auto; /* 自动占满剩余空间 */
            width: 100%;
            min-width: 0; /* 防止溢出 */
            max-width: 100%;
            box-sizing: border-box;
            /deep/.el-input  {
              width: auto !important;
            }
          }
        }
      }
    }

    .basic-info .basic-block .el-form-item__label,
    .configuration-info .configuration-block .el-form-item__label {
      width: 120px; /* 固定宽度，确保所有label宽度一致 */
      text-align: right;
      padding-right: 10px;
      box-sizing: border-box;
      white-space: nowrap; /* 防止label换行 */
    }

    .basic-info .basic-block .el-form-item__content,
    .configuration-info .configuration-block .el-form-item__content {
      flex: 1;
      min-width: 0; /* 允许缩小防止溢出 */
    }

    .basic-info .basic-block,
    .configuration-info .configuration-block {
      display: grid;
      grid-template-columns: repeat(3, 1fr); /* 三列 */
      gap: 20px 16px; /* 行间距20px，列间距16px，可根据需要调整 */
      padding: 20px 32px 20px 52px; /* 内边距，右边留足空间防止溢出 */
      box-sizing: border-box;
      width: 100%;
      max-width: 1200px; /* 容器最大宽度限制，防止超宽 */
      padding-left: 0px;
    }

    @media (max-width: 1024px) {
      .basic-info .basic-block,
      .configuration-info .configuration-block {
        grid-template-columns: repeat(2, 1fr);
      }
    }

    @media (max-width: 768px) {
      .basic-info .basic-block,
      .configuration-info .configuration-block {
        grid-template-columns: 1fr;
      }
    }

    .basic-info .basic-block .el-form-item,
    .configuration-info .configuration-block .el-form-item {
      display: flex;
      align-items: center;
    }

    .basic-info .basic-block .el-form-item .l-upload,
    .configuration-info .configuration-block .el-form-item .l-upload {
      // width: 80px; /* 或 122px，根据设计调整 */
      height: auto;
      flex-shrink: 0;
    }


    .footer-button {
      position: fixed;
      right: 0;
      bottom: 40px;
      display: flex;
      align-items: center;
      justify-content: flex-end;
      padding: 15px 20px;
      border-top: 1px solid #E9ECEF;
      background: #FFFFFF;
      width: 300%;
      z-index: 10;
      button {
        width: 70px;
        height: 40px;
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
  /deep/.role-authorization {
    width: 100%;
    height: 633px;
    padding-bottom: 71px;
    background-color: #fff;
    border-radius: 4px;
    .role-block {
      margin-top: 40px;
      padding-left: 52px;
      .el-tree {
        left: -10px;
        top: 5px;
        max-height: 260px;
        overflow: hidden;
        overflow-y: scroll;
        width: 412px;
        .el-tree-node {
          width: 412px;
        }
      }
    }

    .prompt {
      margin-left: 20px;
      color: #97a0b4;
    }

    .el-checkbox__input.is-checked .el-checkbox__inner, .el-checkbox__input.is-indeterminate .el-checkbox__inner {
      background-color: #B2BCD1;
      border-color: #B2BCD1;
    }
    .el-form-item__label {
      font-weight: normal;
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
}
/deep/.custom-autocomplete{
  margin-right: 12px !important;
  .el-input{
      width: 135px !important;
    .el-input__inner{
      width: 135px !important;
    }
  }
}
/deep/.contactNumber{
  .el-input__inner{
    width: 253px !important;
  }
}
</style>
