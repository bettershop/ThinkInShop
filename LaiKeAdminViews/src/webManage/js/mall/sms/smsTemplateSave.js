import { index, save,saveMessage } from "@/api/mall/sms/smsManage";
import { isEmpty } from "element-ui/src/utils/util";

export default {
  name: "smsTemplateSave",

  //初始化数据
  data() {
    return {
      mainForm: {
        type: null,
        category: null,
        smsTemplateId: null,
        templateStr: null,
        content: "",
        country_num: "",
        cpc:"86"
      },
      smsTypeList: [],
      categoryList: [],
      countriesList: [],
      cate_type: "",
      shaco: "",
      international: "",
    };
  },
  //组装模板
  created() {
    this.shaco = this.$route.query.shaco;
    this.international = this.$route.query.international;
    this.loadData();
    this.rules();
    this.getSelectCountrys();
  },

  watch: {
    "mainForm.type": {
      handler() {
        if (this.mainForm.type) {
          this.cate_type = "";
        }
      },
    },
  },

  methods: {
    // 获取国家列表
    async getSelectCountrys() {
      const res = await this.LaiKeCommon.getCountries()
      this.countriesList = res.data.data
    },
    rules() {
      this.mainForm.rules = {
        type: [
          {
            required: true,
            message: this.$t("smsList.save.qxzlx"),
            trigger: "change",
          },
        ],
        category: [
          {
            required: true,
            message: this.$t("smsList.save.qxzlb"),
            trigger: "change",
          },
        ],
        SignName: [
          {
            required: true,
            message: this.$t("smsList.save.qsrdxqm"),
            trigger: "change",
          },
        ],
        name: [
          {
            required: true,
            message: this.$t("smsList.save.qsrdxmb"),
            trigger: "change",
          },
        ],
        TemplateCode: [
          {
            required: true,
            message: this.$t("smsList.save.qsrdxco"),
            trigger: "change",
          },
        ],
        phone: [
          {
            required: true,
            message: this.$t("smsList.save.qsrdxcs"),
            trigger: "change",
          },
        ],
        content: [
          {
            required: true,
            message: this.$t("smsList.save.qsrfsnr"),
            trigger: "change",
          },
        ],
      };
    },
    async loadData() {
      let id = this.$route.query.id;
      await this.getType();

      if (!isEmpty(id)) {
        let { entries } = Object;
        let data1 = { api: "admin.system.getSmsTemplateInfo", id: id, international: this.international };
        let formData1 = new FormData();
        for (let [key, value] of entries(data1)) {
          formData1.append(key, value);
        }
        const res = await index(formData1).then((data) => {
          data = data.data.data.list[0];
          this.mainForm = data;
          this.mainForm.cpc = "86";
          this.mainForm.id = data.id;
          this.mainForm.type = data.type.toString();
          this.getSelectCountrys().then(() => {
              if(result.country_num) {
                this.mainForm.country_num = parseInt(result.country_num)
              }
          })
          for (let i = 0; i < this.smsTypeList.length; i++) {
            let item = this.smsTypeList[i];

            if (item.value === data.type.toString()) {
              let data2 = {
                api: "admin.system.getSmsTypeList",
                superName: item.text,
              };
              let formData2 = new FormData();
              for (let [key, value] of entries(data2)) {
                formData2.append(key, value);
              }
              index(formData2).then((obj) => {
                obj = obj.data.data;
                this.cate_type = data.type1.toString();
                this.categoryList = obj.list;
                this.smsTemplateList = obj.templateList;
              });
              break;
            }
          }
        });
      }
    },
    change_type() {
      this.mainForm.category = this.cate_type;
    },
    //添加/编辑
    async Save(formName) {
      this.$refs[formName].validate(async (valid) => {
        if (valid) {
          try {
            let { entries } = Object;
            let data = {
              api: "admin.system.addMessage",
              id: this.mainForm.id,
              SignName: this.mainForm.SignName,
              name: this.mainForm.name,
              code: this.mainForm.TemplateCode,
              content: this.mainForm.content,
              type: this.mainForm.type,
              category: this.cate_type,
              type1: this.cate_type,
              cpc:this.mainForm.cpc,
              // category: this.mainForm.type,
              phone: this.mainForm.phone,
              international: this.international
            };
            // let formData = new FormData();
            // for (let [key, value] of entries(data)) {
            //   formData.append(key, value);
            // }
            saveMessage(data).then((data) => {
              if (data.data.code == "200") {
                if (!isEmpty(data)) {
                  this.$message({
                    message:this.mainForm.id ? this.$t("zdata.bjcg"):this.$t("zdata.tjcg"),
                    type: "success",
                    offset: 102,
                  });
                  this.$router.go(-1);
                }
              }
            });
          } catch (e) {
            this.$message({
              message: e.message,
              type: "error",
              showClose: true,
            });
          }
        } else {
          return false;
        }
      });
    },
    //获取类别
    async getType() {
      let { entries } = Object;
      let data = { api: "admin.system.getSmsTypeList" };
      let formData = new FormData();
      for (let [key, value] of entries(data)) {
        formData.append(key, value);
      }
      const res = await index(formData).then((data) => {
        data = data.data.data;
        this.smsTypeList = data.list[0].value;
        this.categoryList = null;
        this.smsTemplateList = null;
      });
    },
    //获取类型+短信模板
    async getCategory(name) {
      let { entries } = Object;
      let data = {
        api: "admin.system.getSmsTypeList",
        superName: name,
      };
      let formData = new FormData();
      for (let [key, value] of entries(data)) {
        formData.append(key, value);
      }
      const res = await index(formData).then((data) => {
        data = data.data.data;
        this.categoryList = data.list;
        this.mainForm.category = null;
      });
    },
  },
};
