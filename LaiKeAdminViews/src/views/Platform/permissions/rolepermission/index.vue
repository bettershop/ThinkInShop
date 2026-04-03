<template>
  <div class="container">
    <div class="add-role">
        <el-form :model="ruleForm" :rules="rules" ref="ruleForm"  class="picture-ruleForm" label-width="auto">
            <el-form-item class="role-name" :label="$t('permissions.addrole.jsmc')" prop="rolename">
                <el-input v-model="ruleForm.rolename" :placeholder="$t('permissions.addrole.qsrjsmc')"></el-input>
            </el-form-item>
            <!-- 新增：语种选择 -->
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
              ></el-option>
            </el-select>
            </el-form-item>
            <el-form-item class="permissions" :label="$t('permissions.addrole.bdqx')" prop="rolelist">
                <el-tree
                    :data="treeData"
                    show-checkbox
                    node-key="id"
                    ref="tree"
                    v-model="ruleForm.rolelist"
                    :check-strictly="systemNodeFlag"
                    @check-change="handleCheckChange"
                    :props="defaultProps">
                </el-tree>
            </el-form-item>
            <el-form-item class="role-describe" :label="$t('permissions.addrole.jsms')" >
                <el-input v-model="ruleForm.roledescribe" :placeholder="$t('permissions.addrole.qsrjsms')" type="textarea"></el-input>
            </el-form-item>
        <div class="form-footer">
          <el-form-item>
            <el-button class="bgColor" :loading="btnLoading" type="primary" @click="submitForm('ruleForm')">{{ $t('DemoPage.tableFromPage.save') }}</el-button>
            <el-button class="bdColor" @click="$router.go(-1)" plain>{{ $t('DemoPage.tableFromPage.cancel') }}</el-button>
          </el-form-item>
        </div>
        </el-form>
      </div>
  </div>
</template>

<script>
import { getUserRoleInfo } from '@/api/Platform/merchants'
import { addUserRoleMenu } from '@/api/Platform/permissions'
import { getLangs } from '@/api/goods/brandManagement'

export default {
    name: 'rolepermission',

    data() {
        return {
            btnLoading:false,
            ruleForm: {
                rolename: '',
                roledescribe: '',
                rolelist: []
            },
            rules: {
                rolename: [
                    { required: true, message: this.$t('permissions.addrole.qsrjsmc'), trigger: 'blur' }
                ],
                roledescribe: [
                    { required: true, message: this.$t('permissions.addrole.qsrjsms'), trigger: 'blur' }
                ],
                rolelist: [
                    { required: true, message: this.$t('permissions.addrole.qxzbdqx'), trigger: 'change' }
                ],
            },

            treeData: [],
            defaultProps: {
                children: 'children',
                label: 'title'
            },

            lang_code: this.LaiKeCommon?.getUserLangVal() || 'zh', // 默认语种
            languages: [],

            // 每个语种独立的已选 ID 列表
            checkedList: [],

            idList: [],

            systemNodeFlag: false
        }
    },

    created() {

      // 先获取语种列表
      this.getLangs().then(() => {
        this.getUserRoleInfos(this.$route.params.id)
      })

      this.ruleForm.rolename = this.$route.params.name
      this.ruleForm.roledescribe = this.$route.params.role_describe
    },

    methods: {
      // 获取语种列表
      async getLangs() {
        try {
          const res = await getLangs({
            api: 'admin.goods.getLangs'
          })
          if (res.data.code == 200) {
            this.languages = res.data.data || []
            // 初始化每个语种的 checkedList
            this.checkedList = this.languages.map(item => ({
              lang_code: item.lang_code,
              idList: []
            }))
          }
        } catch (err) {
          console.error('获取语种列表失败', err)
        }
      },

      // 切换语种时重新加载菜单树，并恢复该语种的勾选状态
      async selectLanguages() {
        await this.getUserRoleInfos(this.$route.params.id)
      },

      // 加载菜单树 + 回显权限
      async getUserRoleInfos(id) {
        try {
          const res = await getUserRoleInfo({
            api: 'admin.role.platform.getUserRoleInfo',
            id: id,
            lang_code: this.lang_code  // 关键：传当前语种
          })

          if (res.data.code == '200') {
            this.treeData = res.data.data.menuList || []
            // 递归收集当前语种所有已选 ID（首次加载时用）
            this.idList = []
            this.recursionNodes(this.treeData)

            // 把首次加载的 idList 保存到对应语种
            const currentLangIndex = this.checkedList.findIndex(
              item => item.lang_code == this.lang_code
            )
            if (currentLangIndex !== -1) {
              // 如果是首次加载该语种，才覆盖；否则保持用户后续修改
              if (this.checkedList[currentLangIndex].idList.length == 0) {
                this.checkedList[currentLangIndex].idList = [...this.idList]
              }
            }

            // 延迟设置勾选（el-tree 需要 DOM 渲染完成）
            this.$nextTick(() => {
              this.$refs.tree?.setCheckedKeys(this.idList)
            })
          }
        } catch (err) {
          console.error('加载菜单失败', err)
        }
      },



        recursionNodes(childNodes) {
            const nodes = childNodes
            for (const item of nodes) {

              if (item.children) {
                this.recursionNodes(item.children)
              }
              if (item.checked && item.level !=1) {
                this.idList.push(item.id)
              }
            }
        },

      handleCheckChange() {
        const checkedKeys = this.$refs.tree.getCheckedKeys()
        const halfCheckedKeys = this.$refs.tree.getHalfCheckedKeys()
        this.idList = [...new Set([...checkedKeys, ...halfCheckedKeys])]

        // 更新当前语种的记录
        const currentLang = this.checkedList.find(
          item => item.lang_code == this.lang_code
        )
        if (currentLang) {
          currentLang.idList = [...this.idList]
        }

        // 合并所有语种的 ID 用于提交
        this.ruleForm.rolelist = []
        this.checkedList.forEach(item => {
          this.ruleForm.rolelist.push(...item.idList)
        })
        this.ruleForm.rolelist = [...new Set(this.ruleForm.rolelist)] // 去重

        console.log('当前语种已选:', this.idList)
        console.log('最终提交所有权限:', this.ruleForm.rolelist)
      },

        // 添加/修改字典表明细
      submitForm(formName) {
        this.btnLoading = true
        this.$refs[formName].validate(async valid => {
          if (!valid) {
            this.btnLoading = false
            return false
          }

          try {
            const res = await addUserRoleMenu({
              api: 'saas.role.addUserRoleMenu',
              id: this.$route.params.id,
              permissions: this.ruleForm.rolelist.join(','),
              roleName: this.ruleForm.rolename,
              describe: this.ruleForm.roledescribe,
              status: 1,
              lang_code: this.lang_code
            })

            if (res.data.code == '200') {
              this.$message({
                message: this.$t('zdata.bjcg'),
                type: 'success',
                offset: 100
              })
              this.$router.go(-1)
            }
          } catch (error) {
            this.$message({
              message: error.message || '修改失败',
              type: 'error',
              showClose: true
            })
          } finally {
            this.btnLoading = false
          }
        })
      }
    },
}
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
