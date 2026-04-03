import { index, save } from "@/api/mall/sms/smsManage";
import { isEmpty } from "element-ui/src/utils/util";

export default {
  name: "smsSave",

  //初始化数据
  data() {
    return {
      mainForm: {
        type: null,
        category: null,
        smsTemplateId: null,
        templateStr: null,
        content: "",
        rules: {
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
          smsTemplateId: [
            {
              required: true,
              message: this.$t("smsList.save.qxzdxmb"),
              trigger: "blur",
            },
          ],
          content: [
            {
              required: true,
              message: this.$t("smsList.save.qsrnr"),
              trigger: "change",
            },
          ],
        },
      },
      smsTypeList: [],
      categoryList: [],
      smsTemplateList: [],

      smsTemplateType: "",
    };
  },
  //组装模板
  created() {
    this.loadData();
    this.international = this.$route.query.international;
  },

  methods: {
    rules() {
      this.mainForm.rules = {
        type: [
          {
            required: true,
            message: $t("smsList.save.qxzlx"),
            trigger: "change",
          },
        ],
        category: [
          {
            required: true,
            message: $t("smsList.save.qxzlb"),
            trigger: "change",
          },
        ],
      };
    },
    change_id() {
      this.mainForm.smsTemplateId = this.smsTemplateType;
    },
    async loadData() {
      let id = this.$route.query.id;
      await this.getType();
      let { entries } = Object;

      let formData1 = new FormData();
      let formData2 = new FormData();
      let formData3 = new FormData();

      if (!isEmpty(id)) {
        let data1 = {
          api: "admin.system.getSmsInfo",
          id: id,
        };
        for (let [key, value] of entries(data1)) {
          formData1.append(key, value);
        }
        const res = await index(formData1).then((data) => {
          data = data.data.data.list[0];
          this.mainForm = data;

          this.mainForm.id = data.id;
          this.mainForm.type = data.type.toString();
          for (let i = 0; i < this.smsTypeList.length; i++) {
            let item = this.smsTypeList[i];
            if (item.value === data.type.toString()) {
              let data2 = {
                api: "admin.system.getSmsTypeList",
                superName: item.text,
              };
              for (let [key, value] of entries(data2)) {
                formData2.append(key, value);
              }
              // 数据三保存
              let data3 = {
                api: "admin.system.getSmsTemplateInfo",
                id: data.Template_id,
                international: this.international
              };
              for (let [key, value] of entries(data3)) {
                formData3.append(key, value);
              }
              index(formData2).then((obj) => {
                obj = obj.data.data;
                this.categoryList = obj.list;
                this.smsTemplateList = obj.templateList;
                this.mainForm.category = data.type1.toString();

                index(formData3).then((obj) => {
                  this.mainForm.content = obj.data.data.list[0].content;
                  this.mainForm.smsTemplateId = data.Template_id;
                  this.smsTemplateType = data.Template_id;
                  this.mainForm.content = data.content1;
                  this.smsTemplateList = obj.data.data.list;
                });
              });
              break;
            }
          }
        });
      }
    },
    //添加/编辑
    async Save(formName) {
      this.$refs[formName].validate(async (valid) => {
        if (valid) {
          try {
            let { entries } = Object;
            let data = {
              api: "admin.system.addMessageList",
              id: this.mainForm.id?this.mainForm.id:"",
              type: this.mainForm.type,
              category: this.mainForm.category,
              smsTemplateId: this.mainForm.smsTemplateId,
              templateStr: this.mainForm.content,
              international: this.international
            };
            // let formData = new FormData();
            // for (let [key, value] of entries(data)) {
            //   formData.append(key, value);
            // }
            save(data).then((data) => {
              if (data.data.code == "200") {
                if (this.$route.query.id) {
                  this.$message({
                    message: this.$t("zdata.bjcg"),
                    type: "success",
                    offset: 102,
                  });
                } else {
                  this.$message({
                    message: this.$t("zdata.tjcg"),
                    type: "success",
                    offset: 102,
                  });
                }
                this.$router.go(-1);
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
        // formData.append(key, value);
        if(value!==null&&value!==""&&value!==undefined){
          formData.append(key, value);
      }
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
        // formData.append(key, value);
        if(value!==null&&value!==""&&value!==undefined){
          formData.append(key, value);
      }

      }
      const res = await index(formData).then((data) => {
        data = data.data.data;
        this.categoryList = data.list;
        this.mainForm.category = null;
        this.getSmsTemplateList();
      });
    },
    //获取短信模板
    async getSmsTemplateList(id) {
      let { entries } = Object;
      let data = {
        api: "admin.system.getSmsTemplateList",
        type: this.mainForm.type,
        id: id,
      };
      let formData = new FormData();
      for (let [key, value] of entries(data)) {
        if(value!==null&&value!==""&&value!==undefined){
          formData.append(key, value);
      }
        // formData.append(key, value);
      }
      const res = await index(formData).then((data) => {
        data = data.data.data.templateList;
        this.smsTemplateList = data;
        this.smsTemplateType = data.name;
        this.mainForm.smsTemplateId = data.id;
        this.mainForm.content = null;
      });
    },
    //获取短信模板内容
    async getTemplate(typeId) {
      let { entries } = Object;
      let data = {
        api: "admin.system.getSmsTemplateInfo",
        id: typeId,
        international: this.international
      };
      let formData = new FormData();
      for (let [key, value] of entries(data)) {
        // formData.append(key, value);
        if(value!==null&&value!==""&&value!==undefined){
          formData.append(key, value);
      }
      }
      const res = await index(formData).then((data) => {
        this.mainForm.content = data.data.data.list[0].content;
      });
    },
  },
};
