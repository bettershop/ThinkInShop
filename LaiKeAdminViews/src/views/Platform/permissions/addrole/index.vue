<template>
  <div class="container">
    <div class="add-role">
      <el-form
        :model="ruleForm"
        :rules="rules"
        ref="ruleForm"
        class="picture-ruleForm"
        label-width="auto"
      >
        <el-form-item
          class="role-name"
          :label="$t('permissions.addrole.jsmc')"
          prop="rolename"
        >
          <el-input
            v-model="ruleForm.rolename"
            :placeholder="$t('permissions.addrole.qsrjsmc')"
          ></el-input>
        </el-form-item>
        <el-form-item :label="$t('yz')" prop="lang_code">
          <el-select
            class="select-input"
            v-model="lang_code"
            :placeholder="$t('qxzyz')"
             @change="selectLanguages"
          >
            <el-option
              v-for="item in languages"
              :key="item.lang_code"
              :label="item.lang_name"
              :value="item.lang_code"
            >
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item
          class="permissions"
          :label="$t('permissions.addrole.bdqx')"
          prop="rolelist"
        >
          <el-tree
            :data="treeData"
            show-checkbox
            node-key="id"
            ref="tree"
            v-model="ruleForm.rolelist"
            @check-change="handleCheckChange"
            :props="defaultProps"
          >
          </el-tree>
        </el-form-item>
        <el-form-item
          class="role-describe"
          :label="$t('permissions.addrole.jsms')"
        >
          <el-input
            v-model="ruleForm.roledescribe"
            :placeholder="$t('permissions.addrole.qsrjsms')"
            type="textarea"
          ></el-input>
        </el-form-item>

        <div class="form-footer">
          <el-form-item>
            <el-button
              :loading="btnloading"
              class="bgColor"
              type="primary"
              @click="submitForm('ruleForm')"
              >{{ $t("DemoPage.tableFromPage.save") }}</el-button
            >
            <el-button class="bdColor" @click="$router.go(-1)" plain>{{
              $t("DemoPage.tableFromPage.cancel")
            }}</el-button>
          </el-form-item>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script>
import { getUserRoleInfo } from "@/api/Platform/merchants";
import { addUserRoleMenu } from "@/api/Platform/permissions";
import { getLangs } from '@/api/goods/brandManagement'

export default {
  name: "addrole",

  data() {
    return {
      btnloading: false,
      ruleForm: {
        rolename: "",
        roledescribe: "",
        rolelist: [],
      },
      lang_code:this.LaiKeCommon.getUserLangVal(),
      languages:[],
      rules: {
        rolename: [
          {
            required: true,
            message: this.$t("permissions.addrole.qsrjsmc"),
            trigger: "blur",
          },
        ],
        roledescribe: [
          {
            required: true,
            message: this.$t("permissions.addrole.qsrjsms"),
            trigger: "blur",
          },
        ],
        rolelist: [
          {
            required: true,
            message: this.$t("permissions.addrole.qxzbdqx"),
            trigger: "change",
          },
        ],
      },

      treeData: [],
      defaultProps: {
        children: "children",
        label: "title",
      },
      idList: [],
      lang_code:this.LaiKeCommon.getUserLangVal(),
    };
  },

  created() {
    this.getUserRoleInfos();
    this.getLangs();
  },

  methods: {
        selectLanguages(){
      this.getUserRoleInfos();
    },
    async getUserRoleInfos() {
      const res = await getUserRoleInfo({
        api: "admin.role.platform.getUserRoleInfo",
        lang_code: this.lang_code
      });
      console.log(res);
      this.treeData = res.data.data.menuList;
    },

    // 获取语种列表
    async getLangs() {
      const res = await getLangs({
        api: "admin.goods.getLangs",
      });
      console.log(res);
      this.languages = res.data.data;
    },

    // handleCheckChange() {
    //   let res = this.$refs.tree.getCheckedNodes();
    //   let arr = [];
    //   res.forEach((item) => {
    //     arr.push(item.id);
    //   });
    //   this.idList = res.map((item) => {
    //     return item.id;
    //   });
    //   this.ruleForm.rolelist = this.idList;
    //   console.log(this.idList);
    // },

    handleCheckChange() {
      // 全选中的节点 id
      const checkedKeys = this.$refs.tree.getCheckedKeys()
      // 半选中的父节点 id
      const halfCheckedKeys = this.$refs.tree.getHalfCheckedKeys()
      // 合并去重，只保留 id
      this.idList = Array.from(new Set([...checkedKeys, ...halfCheckedKeys]))
      // 提交表单时就是纯 id 数组
      this.ruleForm.rolelist = this.idList
      console.log("最终提交的 ID 列表:", this.idList)
    }
    ,



    // 添加角色
    submitForm(formName) {
      this.btnloading = true;
      this.$refs[formName].validate(async (valid) => {
        console.log(this.ruleForm);
        if (valid) {
          try {
            addUserRoleMenu({
              api: "saas.role.addUserRoleMenu",
              permissions: this.idList.join(),
              roleName: this.ruleForm.rolename,
              describe: this.ruleForm.roledescribe,
              lang_code: this.lang_code,
              // status: 0,
              status:1,
            }).then((res) => {
              // debugger
              console.log(res);
              if (res.data.code == "200") {
                this.$message({
                  message: this.$t("zdata.tjcg"),
                  type: "success",
                  offset: 100,
                });
                this.btnloading = false;
                this.$router.go(-1);
              }
            });
          } catch (error) {
            this.$message({
              message: error.message,
              type: "error",
              showClose: true,
            });
            this.btnloading = false;
          }
        } else {
          console.log("error submit!!");
          this.btnloading = false;

          return false;
        }
      });
    },
  },
};
</script>

<style scoped lang="less">
.container {
  width: 100%;
  height: 737px;
  background-color: #fff;
  padding: 40px 0 0 0;
  color: #414658;
  position: relative;
  border-radius: 4px;
  /deep/.add-role {
    display: flex;
    justify-content: center;
    .el-form {
      .role-name {
        display: flex;
        .el-form-item__content {
          width: 580px;
          // height: 40px;
        }
        .select-input {
          width: 580px;
          height: 40px;
        }
      }

      .permissions {
        display: flex;
        height: 400px;
        /* overflow: hidden;
                overflow-y: auto; */
        .el-form-item__content {
          width: 580px;
          .el-tree {
            height: 400px;
            overflow: hidden;
            overflow-y: scroll;
            .el-tree-node {
              .el-tree-node__content {
                height: 40px;
              }
            }
          }
        }
      }

      .role-describe {
        .el-form-item__content {
          .el-textarea {
            width: 580px;
            height: 68px;
            textarea {
              width: 580px;
              height: 68px;
            }
          }
        }
        .el-form-item__error {
          left: 90px;
        }
      }

      .el-form-item {
        &:not(:last-child) {
          .el-form-item__content {
            margin-left: 0px !important;
          }
        }
      }
    }
  }

  /deep/.el-form-item__label {
    font-weight: normal;
  }
}
</style>
