import cascade from "@/api/publics/cascade";
import { index, save } from "@/api/mall/aftersaleAddress/aftersaleAddress";
import { isEmpty } from "element-ui/src/utils/util";
import { getItuList } from '@/api/members/membersSet'
export default {
  name: "addressSave",

  data() {
    var validateCode = (rule, value, callback) => {
      if (value === "") {
        callback(new Error("请输入邮政编码"));
      } else if (value.length && value.length != 6) {
        callback(new Error("邮政编码长度为6字符"));
      } else {
        callback();
      }
    };
    return {
      ruleForm: {
        name: "",
        tel: "",
        sheng: "",
        shi: "",
        xian: "",
        address: "",
        is_default: 1,
        code: "",
        country_num: '', // 默认中国
        province:"",
        city:""
      },
      rules: {
        name: [
          {
            required: true,
            message: this.$t("aftersaleAddress.add.qsrlxr"),
            trigger: "blur",
          },
        ],
        tel: [
          {
            required: true,
            message: this.$t("aftersaleAddress.add.qsrlxdh"),
            trigger: "blur",
          },
          {
            validator: (rule, value, callback) => {
              // 核心逻辑：判断cpc是否为空（空字符串/undefined/null都算空）
              if (!this.cpc || this.cpc.trim() === '') {
                // 校验失败：通过callback返回错误提示
                callback(new Error(this.$t("addMembers.qsrqh")));
              } else {
                // 校验通过：必须调用空的callback，否则表单会一直处于校验中状态
                callback();
              }
            },
            trigger: "blur", // 失焦时触发
          },
        ],
        sheng: [
          {
            required: true,
            message: this.$t("aftersaleAddress.add.qxzs"),
            trigger: "change",
          },
        ],
        shi: [
          {
            required: true,
            message: this.$t("aftersaleAddress.add.qxzss"),
            trigger: "change",
          },
        ],
        xian: [
          {
            required: true,
            message: this.$t("aftersaleAddress.add.qxzx"),
            trigger: "change",
          },
        ],
        address: [
          {
            required: true,
            message: this.$t("aftersaleAddress.add.qsrxxdz"),
            trigger: "blur",
          },
        ],
        code: [{ validator: validateCode, trigger: "blur", required: true }],
      },
      //省市级联集
      shengList: {},
      shiList: {},
      xianList: {},
      cpc: '', //国家区号
      restaurants: [], //异步查询建议列表
      isDomestic: true, // 是否国内
      countriesList: [],
    };
  },

  created() {
    this.getSheng();
    this.loadData(this.$route.params.id);
    this.queryAdd()
    this.getSelectCountrys()
  },

  beforeRouteLeave(to, from, next) {
    if (to.name == "list" && this.$route.name == "edit") {
      to.params.dictionaryNum = this.$route.query.dictionaryNum;
      to.params.pageSize = this.$route.query.pageSize;
    }
    next();
  },

  methods: {
    // 获取国家列表
    async getSelectCountrys() {
      const res = await this.LaiKeCommon.getCountries()
      this.countriesList = res.data.data
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
      this.state = item.code2; // 可以根据需求更新输入框显示的值
      this.cpc = item.code2;
      if (item.code2 == '86' || item.code2 == '852' || item.code2 == '853') {
        this.isDomestic = true;
      }else {
        this.isDomestic = false;
      }
    },
    async loadData(id) {
      if (!isEmpty(id)) {
        let { entries } = Object;
        let data1 = {
          api: "admin.system.getAddressInfo",
          type: 2,
          id: id,
        };
        let formData = new FormData();
        for (let [key, value] of entries(data1)) {
          formData.append(key, value);
        }
        const res = await index(formData);
        if (!isEmpty(res)) {
          let data = res.data.data.list[0];
          this.ruleForm = data;
          data.is_default = data.is_default;
          this.cpc = data.cpc
          if (this.cpc == '86' || this.cpc == '852' || this.cpc == '853') {
            this.isDomestic = true;
            await this.cascadeAddress();
          }else {
            this.isDomestic = false;
          }

        }
      }
    },
    // 获取省级
    async getSheng() {
      const res = await cascade.getSheng();
      this.shengList = res.data.data;
    },
    // 获取市级
    async getShi(sid, flag) {
      const res = await cascade.getShi(sid);
      this.shiList = res.data.data;
      if (!flag) {
        this.ruleForm.shi = "";
        this.ruleForm.xian = "";
      }
    },
    // 获取县级
    async getXian(sid, flag) {
      const res = await cascade.getXian(sid);
      this.xianList = res.data.data;
      if (!flag) {
        this.ruleForm.xian = "";
      }
    },
    //省市级联回显
    async cascadeAddress() {
      //省市级联
      for (const sheng of this.shengList) {
        if (sheng.districtName === this.ruleForm.sheng) {
          await this.getShi(sheng.id, true);
          for (const shi of this.shiList) {
            if (shi.districtName === this.ruleForm.shi) {
              await this.getXian(shi.id, true);
              break;
            }
          }
          break;
        }
      }
    },

    submitForm(formName) {
      this.$refs[formName].validate(async (valid) => {
        // let text = "添加成功";
        // if (!isEmpty(this.ruleForm.id)) {
        //   text = "修改成功";
        // }
        if (valid) {
          try {
            if (this.ruleForm.name.length > 20) {
              this.$message({
                message: "联系人不能超过20个字符",
                type: "error",
                offset: 102,
              });
              return;
            }
            if (this.ruleForm.address.length > 50) {
              this.$message({
                message: "详细地址不能超过50个字符",
                type: "error",
                offset: 102,
              });
              return;
            }
            // 更换api传值方式
            let { entries } = Object;
            let data = {
              api: "admin.system.addAddressInfo",
              name: this.ruleForm.name,
              tel: this.ruleForm.tel,
              type: 2,
              code: this.ruleForm.code,
              address: this.ruleForm.address,
              shen: this.ruleForm.sheng,
              shi: this.ruleForm.shi,
              xian: this.ruleForm.xian,
              isDefault: this.ruleForm.is_default,
              country_num: this.ruleForm.country_num,
              cpc:this.cpc
            };
            if (this.ruleForm.id){
              data.id = this.ruleForm.id
            }
            if (this.ruleForm.province){
              data.province = this.ruleForm.province
            }
            if (this.ruleForm.city){
              data.city = this.ruleForm.city
            }
            let formData = new FormData();
            for (let [key, value] of entries(data)) {
              formData.append(key, value);
            }
            save(formData).then((res) => {
              if (res.data.code == "200" && !isEmpty(res)) {
                this.$message({
                  message:this.ruleForm.id?this.$t("zdata.bjcg"):this.$t("zdata.tjcg"),
                  type: "success",
                  offset: 102,
                });
                this.$router.go(-1);
              }
            });
          } catch (e) {
            this.$message({
              message: e.message,
              type: "error",
              showClose: true,
              offset: 102,

            });
          }
        } else {
          return false;
        }
      });
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
  },
};
