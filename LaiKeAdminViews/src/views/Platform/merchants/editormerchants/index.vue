<template>
  <div class="container">
    <el-form
      ref="ruleForm"
      class="form-search"
      :rules="rules"
      :model="ruleForm"
      label-width="100px"
    >
      <div class="basic-info">
        <div class="header">
          <span>{{ $t("merchants.addmerchants.jcxx") }}</span>
        </div>
        <div class="basic-block">
          <el-form-item
            :label="$t('merchants.addmerchants.scmc')"
            prop="storeName"
          >
            <el-input
              v-model="ruleForm.storeName"
              :placeholder="$t('merchants.addmerchants.qsrscmc')"
            ></el-input>
          </el-form-item>
          <el-form-item
            :label="$t('merchants.addmerchants.scbh')"
            prop="storeNo"
          >
            <el-input
              v-model="ruleForm.storeNo"
              :placeholder="$t('merchants.addmerchants.qsrscbh')"
            ></el-input>
          </el-form-item>
          <el-form-item
            :label="$t('merchants.addmerchants.gsmc')"
            prop="company"
          >
            <el-input
              v-model="ruleForm.company"
              :placeholder="$t('merchants.addmerchants.qsrgsmc')"
            ></el-input>
          </el-form-item>
          <el-form-item :label="$t('merchants.addmerchants.sjh')" prop="mobile">
            <el-input
              v-model="ruleForm.mobile"
              :placeholder="$t('merchants.addmerchants.qsrsjh')"
            ></el-input>
          </el-form-item>
          <el-form-item :label="$t('merchants.addmerchants.jg')" prop="price">
            <el-input
              class="composite"
              v-model="ruleForm.price"
              :placeholder="$t('merchants.addmerchants.qsrjg')"
              @keyup.native="ruleForm.price = oninput(ruleForm.price, 2)"
            >
              <template slot="append">{{
                $t("merchants.addmerchants.yuan")
              }}</template>
            </el-input>
          </el-form-item>
          <el-form-item :label="$t('merchants.addmerchants.yx')">
            <el-input
              v-model="ruleForm.email"
              :placeholder="$t('merchants.addmerchants.qsryx')"
            ></el-input>
          </el-form-item>
          <el-form-item
            :label="$t('merchants.addmerchants.sczh')"
            prop="adminAccount"
          >
            <el-input
              v-model="ruleForm.adminAccount"
              :placeholder="$t('merchants.addmerchants.qsrglyzh')"
            ></el-input>
          </el-form-item>
          <el-form-item :label="$t('merchants.addmerchants.scmm')" prop="adminPwd">
            <el-input
              show-password
              v-model="ruleForm.adminPwd"
              disabled
              @keyup.native="ruleForm.adminPwd = oninput3(ruleForm.adminPwd)"
              :placeholder="$t('merchants.addmerchants.qsrglymm')"
            ></el-input>
          </el-form-item>
          <el-form-item
            :label="$t('merchants.addmerchants.dqsj')"
            prop="endDate"
          >
            <el-date-picker
              v-model="ruleForm.endDate"
              type="datetime"
              value-format="yyyy-MM-dd HH:mm:ss"
              :placeholder="$t('merchants.addmerchants.qxzdqsj')"
            >
            </el-date-picker>
          </el-form-item>
          <div style="width: 100%">
            <el-form-item
              :label="$t('merchants.addmerchants.sfqy')"
              prop="isOpen"
            >
              <el-switch
                v-model="ruleForm.isOpen"
                :active-value="0"
                :inactive-value="2"
                active-color="#00ce6d"
                inactive-color="#d4dbe8"
              >
              </el-switch>
            </el-form-item>
          </div>
        </div>
      </div>
      <div class="configuration-info">
        <div class="header">
          <span>{{ $t("merchants.addmerchants.pzxx") }}</span>
        </div>
        <div class="configuration-block">
          <el-form-item :label="$t('yz')" prop="store_langs">
            <el-select disabled class="select-input" multiple filterable v-model="ruleForm.store_langs" :placeholder="$t('qxzyz')">
              <el-option v-for="(item,index) in languages" :key="index" :label="item.lang_name" :value="item.id">
                <div >{{ item.lang_name }}</div>
              </el-option>
            </el-select>
          </el-form-item>

          <!-- 设置默认语言（从已选中中选择） -->
          <el-form-item :label="$t('mryz')" prop="default_lang">
            <el-select
              class="select-input"
              disabled
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
          </el-form-item>

          <el-form-item :label="$t('bz')" prop="store_currencys">
            <el-select disabled class="select-input" multiple filterable v-model="ruleForm.store_currencys" :placeholder="$t('qxzbz')">
              <el-option v-for="(item,index) in currencys" :key="index" :label="item.currency_name" :value="item.id">
                <div >{{ item.currency_name }}</div>
              </el-option>
            </el-select>
          </el-form-item>
          <!-- 设置默认币种（从已选中中选择） -->
          <el-form-item :label="$t('mrbz')" prop="default_currency">
            <el-select
              class="select-input"
              disabled
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
          </el-form-item>


        </div>
      </div>

      <div class="footer-button">
        <el-button
          plain
          class="footer-cancel fontColor"
          @click="$router.go(-1)"
          >{{ $t("merchants.addmerchants.cancel") }}</el-button
        >
        <el-button
          type="primary"
          class="footer-save bgColor mgleft"
          @click="submitForm('ruleForm')"
          >{{ $t("merchants.addmerchants.save") }}</el-button
        >
      </div>
    </el-form>
    <div class="role-authorization">
      <el-form :rules="rules2" :model="ruleForm2" label-width="100px">
        <div class="header">
          <span>{{ $t("merchants.addmerchants.jsqx") }}</span>
        </div>
        <div class="role-block">
          <el-form-item
            :label="$t('merchants.addmerchants.bdjs')"
            prop="region"
          >
            <el-select
              disabled
              class="select-input"
              v-model="ruleForm2.region"
              :placeholder="$t('merchants.addmerchants.qxzjs')"
            >
              <el-option
                v-for="item in Dictionary"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              >
                <div @click="change(item.id)">{{ item.name }}</div>
              </el-option>
            </el-select>
            <span class="prompt">{{
              $t("merchants.addmerchants.bdjsbz")
            }}</span>
          </el-form-item>
          <el-form-item :label="$t('merchants.addmerchants.bdqx')" prop="">
            <el-tree
              :data="treeData"
              :show-checkbox="false"
              node-key="id"
              :props="defaultProps"
            >
            </el-tree>
          </el-form-item>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script>
import {
  addStore,
  getUserRoleInfo,
  getShopInfo,
} from "@/api/Platform/merchants";
import { getRoleListInfo } from "@/api/Platform/permissions";
import { getTime } from "@/utils/utils";
import { getStorage, setStorage, removeStorage } from "@/utils/storage";
import { getItuList } from '@/api/members/membersSet'
export default {
  inject: ["reload"],
  name: "editormerchants",

  data() {
    // 只能输入数字和字母
    var validatePass3 = (rule, value, callback) => {
      console.log("8148148148", /^[\da-z]+$/i.test(value));
      if (/^[\da-z]+$/i.test(value)) {
        callback();
      } else {
        callback(new Error(this.$t("merchants.znsrszhzm")));
      }
    };
    return {
      treeData: [],
      languages: [],
      currencys:[],
      store_langs:"",
      store_currencys:"",
      default_lang: null,       // 默认语言ID
      default_currency: null,   // 默认币种ID
      defaultProps: {
        children: "children",
        label: "title",
        disabled: true,
      },
      ruleForm: {
        // 基础信息
        storeName: "",
        storeNo: "",
        company: "",
        mobile: "",
        price: "",
        email: "",
        adminAccount: "",
        adminPwd: "",
        endDate: "",
        isOpen: 0,

        // 配置信息
        storeDomain: "",
        contactAddress: "",
        contactNumber: "",
        copyrightInformation: "",
        recordInformation: "",
        website: "",
        logoUrl: "",
        adminDefaultPortrait: '',
        default_lang: null,       // 默认语言ID
        default_currency: null,   // 默认币种ID
        store_langs:"",
        store_currencys:"",
      },

      Dictionary: [],
      id: "",

      rules: {
        storeName: [
          {
            required: true,
            message: this.$t("merchants.addmerchants.qsrscmc"),
            trigger: "blur",
          },
        ],
        storeNo: [
          {
            required: true,
            message: this.$t("merchants.addmerchants.qsrscbh"),
            trigger: "blur",
          },
          { validator: validatePass3, trigger: "change" },
        ],
        company: [
          {
            required: true,
            message: this.$t("merchants.addmerchants.qsrgsmc"),
            trigger: "blur",
          },
        ],
        mobile: [
          {
            required: true,
            message: this.$t("merchants.addmerchants.qsrsjh"),
            trigger: "blur",
          },
        ],
        price: [
          {
            required: true,
            message: this.$t("merchants.addmerchants.qsrjg"),
            trigger: "blur",
          },
        ],
        email: [
          {
            required: true,
            message: this.$t("merchants.addmerchants.qsryx"),
            trigger: "blur",
          },
        ],
        adminAccount: [
          {
            required: true,
            message: this.$t("merchants.addmerchants.qsrglyzh"),
            trigger: "blur",
          },
        ],
        adminPwd: [
          {
            required: true,
            message: this.$t("merchants.addmerchants.qsrglymm"),
            trigger: "blur",
          },
        ],
        endDate: [
          {
            required: true,
            message: this.$t("merchants.addmerchants.qxzdqsj"),
            trigger: "change",
          },
        ],
        storeDomain: [
          {
            required: true,
            message: this.$t("merchants.addmerchants.qsrscgml"),
            trigger: "blur",
          },
        ],
        copyrightInformation: [
          {
            required: true,
            message: this.$t("merchants.addmerchants.qsrbqxx"),
            trigger: "blur",
          },
        ],
        recordInformation: [
          {
            required: true,
            message: this.$t("merchants.addmerchants.qsrbaxx"),
            trigger: "blur",
          },
        ],
        store_langs:[
          {required: true, message: this.$t('qxzyz'), trigger: 'blur'}
        ],
        store_currencys:[
          {required: true, message: this.$t('qxzbz'), trigger: 'blur'}
        ]
      },

      ruleForm2: {
        // 角色权限
        region: "系统管理员",
      },

      rules2: {
        region: [{ required: true, message: "请选择角色", trigger: "blur" }],
      },
      cpc: '', //国家区号
      restaurants: [], //异步查询建议列表
    };
  },

  created() {
    this.getShopInfos();
    this.getRoleListInfos();
    this.getUserRoleInfos();

    this.getAllCurrencys().then(() => {
      this.ruleForm.store_currencys = this.currencys.filter(item => {
        let curs = this.$route.params.store_currencys.split(",");
        // includes 区分数据类型 数字和字符串 比较返回 false ..
        if(curs.includes(item.id+"")) {
          return item.id
        }
      }).map(item => {
        return item.id
      })
      this.ruleForm.default_currency = this.$route.params.default_currency;
      this.default_currency = this.ruleForm.default_currency;
      this.store_currencys  = this.ruleForm.store_currencys;
      console.log(this.ruleForm.store_currencys)
    });

    this.getAllLanguages().then(() => {
      this.ruleForm.store_langs = this.languages.filter(item => {
        let langs = this.$route.params.store_langs.split(",");
        // includes 区分数据类型 数字和字符串 比较返回 false ..
        if(langs.includes(item.id+"")) {
          return item.id
        }
      }).map(item => {
        return item.id
      })

      const langCode = this.$route.params.default_lang_code;
      const langItem = this.languages.find(item => item.lang_code === langCode);
      this.ruleForm.default_lang = langItem ? langItem.id : null;

      this.default_lang = this.ruleForm.default_lang;
      console.log(langCode)
      console.log(this.ruleForm.default_lang)
      // this.ruleForm.default_lang = this.$route.params.default_lang_code;
      this.store_langs  = this.ruleForm.store_langs;
      console.log(this.ruleForm.store_langs)
    })
    // this.ruleForm.storeName = this.$route.params.name
    // this.ruleForm.storeNo = this.$route.params.customer_number
    // this.ruleForm.company = this.$route.params.company
    // this.ruleForm.mobile = this.$route.params.mobile
    // this.ruleForm.price = this.$route.params.price
    // this.ruleForm.email = this.$route.params.email
    // this.ruleForm.adminAccount = this.$route.params.adminName
    // this.ruleForm.endDate = getTime(this.$route.params.end_date)
    // this.ruleForm.isOpen = this.$route.params.status
    // this.ruleForm.storeDomain = this.$route.params.official_website
    // this.ruleForm.contactAddress = this.$route.params.contact_address
    // this.ruleForm.contactNumber = this.$route.params.contact_number
    // this.ruleForm.copyrightInformation = this.$route.params.copyright_information
    // this.ruleForm.recordInformation = this.$route.params.record_information
    // this.ruleForm.website = this.$route.params.official_website
    // this.ruleForm.logoUrl = this.$route.params.merchant_logo

    // this.Dictionary.push({
    //   id: this.$route.params.roleId,
    //   name: this.$route.params.roleName
    // })
    this.ruleForm2.region = this.$route.params.roleId
      ? Number(this.$route.params.roleId)
      : "";
  },

  beforeRouteLeave(to, from, next) {
    if (to.name == "merchantslist") {
      to.params.dictionaryNum = this.$route.query.dictionaryNum;
      to.params.pageSize = this.$route.query.pageSize;
      to.params.inputInfo = this.$route.query.inputInfo;
    }
    next();
  },

  watch: {
    id() {
      this.getChangeUserRoleInfos();
    },
  },

  methods: {
    getLangName(id) {
      const item = this.languages.find(i => i.id === id);
      return item ? item.lang_name : id;
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
    async getAllCurrencys(){
      const res = await this.LaiKeCommon.select({
        api: 'admin.currency.currencyList',
        pageNo: 1,
        pageSize: 300
      })
      this.currencys = res.data.data.list
    },

    async getAllLanguages(){
      const res = await this.LaiKeCommon.select({
        api: 'admin.lang.index',
        pageNo: 1,
        pageSize: 300
      })
      this.languages = res.data.data.list
    },
    // 获取商城列表
    async getShopInfos() {
      const res = await getShopInfo({
        api: "saas.shop.getShopInfo",
        storeType: 8,
        storeId: this.$route.params.id,
      });
      console.log(res);
      let info = res.data.data.dataList[0];
      this.ruleForm.storeName = info.name;
      this.ruleForm.storeNo = info.customer_number;
      this.ruleForm.company = info.company;
      this.ruleForm.mobile = info.mobile;
      this.ruleForm.price = info.price;
      this.ruleForm.email = info.email;
      this.ruleForm.adminAccount = info.adminName;
      this.ruleForm.endDate = getTime(info.end_date);
      this.ruleForm.isOpen = info.status;
      this.ruleForm.storeDomain = info.storeDomain;
      this.ruleForm.contactAddress = info.contact_address;
      this.ruleForm.contactNumber = info.contact_number;
      this.ruleForm.copyrightInformation = info.copyright_information;
      this.ruleForm.recordInformation = info.record_information;
      this.ruleForm.website = info.official_website;
      this.ruleForm.logoUrl = info.merchant_logo;
      this.ruleForm.adminDefaultPortrait= info.portrait,
      this.ruleForm.adminPwd = info.adminPwd || '· · · · · ·';
      this.cpc = info.cpc;
    },

    // 获取角色列表
    async getRoleListInfos() {
      const res = await getRoleListInfo({
        api: "saas.role.getRoleListInfo",
      });
      console.log(res);
      this.Dictionary = res.data.data.list;
    },

    // 根据角色获取权限
    async getUserRoleInfos() {
      if (!this.$route.params.roleId) {
        return;
      }
      const res = await getUserRoleInfo({
        api: "admin.role.getUserRoleInfo",
        id: this.$route.params.roleId,
      });
      console.log('xxxxxxx494',this.filterFalsyValues(res.data.data.menuList));

      this.treeData = res.data.data.menuList.filter((item) => {
        if (item.checked) {
          return item;
        }
      });
    },

    async getChangeUserRoleInfos() {
      const res = await getUserRoleInfo({
        api: "admin.role.getUserRoleInfo",
        id: this.id,
      });
      console.log('xxxxxxx506',this.filterFalsyValues(res.data.data.menuList));

      this.treeData = res.data.data.menuList.filter((item) => {
        if (item.checked) {
          // this.filterChildren(item)
          return item;
        }
      });
    },
    // 递归过滤数据

    filterFalsyValues(arr) {
      return arr.filter((item) => {
        if (item.checked) {
          if(item.children.length>0){
            return item.children = this.filterFalsyValues(item.children);
          }else{
            return item
          }
        }
      });
    },

    change(value) {
      this.id = value;
    },

    submitForm(formName) {
      this.$refs[formName].validate(async (valid) => {
        console.log(this.ruleForm);
        if (valid) {
          try {
            if (this.ruleForm.storeName.length > 50) {
              this.$message({
                message: "商城名称不能大于50个字符",
                type: "error",
                offset: 100,
              });
              return;
            }
            addStore({
              api: "saas.shop.addStore",
              store_langs: this.ruleForm.store_langs.join(','),
              store_currencys: this.ruleForm.store_currencys.join(','),
              id: this.$route.params.id, // 覆盖问题
              storeName: this.ruleForm.storeName,
              storeNo: this.ruleForm.storeNo,
              company: this.ruleForm.company,
              mobile: this.ruleForm.mobile,
              price: this.ruleForm.price,
              email: this.ruleForm.email,
              adminAccount: this.ruleForm.adminAccount,
              /// adminPwd: this.ruleForm.adminPwd ? this.ruleForm.adminPwd : null,
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
            }).then((res) => {
              if (res.data.code == "200") {
                this.$message({
                  message: "编辑成功",
                  type: "success",
                  offset: 100,
                });
                if (this.$route.params.id == getStorage("rolesInfo").storeId) {
                  this.$store.commit(
                    "user/SET_MERCHANTSLOGO",
                    this.ruleForm.logoUrl
                  );
                  setStorage("laike_head_img", this.ruleForm.logoUrl);
                  //删除现有缓存
                  removeStorage("website_information");
                  let website_information = {
                    contact_address: this.ruleForm.contactAddress,
                    contact_number: this.ruleForm.contactNumber,
                    copyright_information: this.ruleForm.copyrightInformation,
                    record_information: this.ruleForm.recordInformation,
                    official_website: this.ruleForm.website,
                  };
                  //存入新缓存
                  setStorage("website_information", website_information);
                  this.reload();
                }
                this.$router.go(-1);
              }
            });
          } catch (error) {
            this.$message({
              message: error.message,
              type: "error",
              showClose: true,
            });
          }
        } else {
          console.log("error submit!!");
          return false;
        }
      });
    },

    oninput(num, limit) {
      var str = num;
      var len1 = str.substr(0, 1);
      var len2 = str.substr(1, 1);
      //如果第一位是0，第二位不是点，就用数字把点替换掉
      if (str.length > 1 && len1 == 0 && len2 != ".") {
        str = str.substr(1, 1);
      }
      //第一位不能是.
      if (len1 == ".") {
        str = "";
      }
      //限制只能输入一个小数点
      if (str.indexOf(".") != -1) {
        var str_ = str.substr(str.indexOf(".") + 1);
        if (str_.indexOf(".") != -1) {
          str = str.substr(0, str.indexOf(".") + str_.indexOf(".") + 1);
        }
      }
      //正则替换
      str = str.replace(/[^\d^\.]+/g, ""); // 保留数字和小数点
      if (limit / 1 === 1) {
        str = str.replace(/^\D*([0-9]\d*\.?\d{0,1})?.*$/, "$1"); // 小数点后只能输 1 位
      } else {
        str = str.replace(/^\D*([0-9]\d*\.?\d{0,2})?.*$/, "$1"); // 小数点后只能输 2 位
      }

      if (
        this.ruleForm.price_type == 1 &&
        this.totlePrice !== 0 &&
        parseInt(this.ruleForm.price) > this.totlePrice
      ) {
        str = this.totlePrice;
      }
      return str;
    },
  },
};
</script>

<style scoped lang="less">
.container {
  width: 100%;
  /deep/.el-form {
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

    .basic-info {
      width: 100%;
      background-color: #fff;
      margin-bottom: 16px;
      border-radius: 4px;
      .basic-block {
        margin-top: 40px;
        display: flex;
        flex-wrap: wrap;
        justify-content: space-between;
        padding: 0 20px 20px 52px;
        .password {
          .el-input {
            input::-webkit-input-placeholder {
              color: #414658;
              position: relative;
              top: 3px;
            }
          }
        }

        .is_switch {
          width: 100%;
        }
      }
    }

    .configuration-info {
      width: 100%;
      // height: 340px;
      background-color: #fff;
      margin-bottom: 16px;
      border-radius: 4px;
      .configuration-block {
        margin-top: 40px;
        display: flex;
        flex-wrap: wrap;
        justify-content: space-between;
        padding: 0 20px 20px 23px;
        .el-form-item {
          display: flex;
          .el-form-item__label {
            width: 130px !important;
          }
          .el-form-item__content {
            margin-left: 0 !important;
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
      button {
        width: 70px;
        height: 40px;
      }

      .bgColor {
        background-color: #2890ff;
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
        background-color: #fff;
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

    .el-checkbox__input.is-checked .el-checkbox__inner,
    .el-checkbox__input.is-indeterminate .el-checkbox__inner {
      background-color: #b2bcd1;
      border-color: #b2bcd1;
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
