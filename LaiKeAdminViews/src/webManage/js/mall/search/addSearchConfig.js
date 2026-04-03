import { index, save } from "@/api/mall/search/searchApi";
import { isEmpty } from "element-ui/src/utils/util";

export default {
  name: "addSearchConfig",

  data() {
    return {
      dataForm: {
        id: null,
        is_open: false,
        keyword: "",
        mch_keyword: "",
        num: "",
      },
      rules: {
        num: [
          {
            required: true,
            message: this.$t("searchConfig.qsrgjcsx"),
            trigger: "blur",
          },
        ],
        keyword: [
          {
            required: true,
            message: this.$t("searchConfig.qsrgjc"),
            trigger: "blur",
          },
        ],
      },
    };
  },

  created() {
    this.loadData();
  },

  methods: {
    async loadData() {
      let { entries } = Object;
      let data = {
        api: "admin.system.getSearchConfigIndex",
      };
      let formData = new FormData();
      for (let [key, value] of entries(data)) {
        formData.append(key, value);
      }
      const res = await index(formData);

      if (res.data.data) {
        let data = res.data.data.data;
        if (data) {
          this.dataForm = data;
        }

        this.dataForm.is_open = data.is_open === 1;
      }
    },

    submitForm(formName) {
      this.$refs[formName].validate(async (valid) => {
        // let text = "添加成功";
        // if (!isEmpty(this.dataForm.id)) {
        //   text = "修改成功";
        // }
        if (valid) {
          try {
            // if(this.dataForm.num == '') {
            //   this.$message({
            //     message: '关键词上限不能为空',
            //     type: 'error',
            //     showClose: true
            //   })
            //   return
            // }
            let { entries } = Object;
            let data = {
              api: "admin.system.addSearchConfig",
              isOpen: this.dataForm.is_open ? 1 : 0,
              limitNum: this.dataForm.num,
              keyword: this.dataForm.keyword,
            };
            let formData = new FormData();
            for (let [key, value] of entries(data)) {
              formData.append(key, value);
            }
            save(formData).then((res) => {
              if (res.data.code == 200) {
                this.$message({
                  message: this.$t("zdata.baccg"),
                  type: "success",
                  offset: 102,
                });
              }
              this.loadData();
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
  },
};
