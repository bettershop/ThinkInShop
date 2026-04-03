import { getUserRoleInfo, addUserRoleMenu } from '@/api/authority/roleManagement'
export default {
    name: 'addRoles',

    data() {
        return {
            ruleForm: {
                rolename: '',
                roledescribe: '',
                rolelist: []
            },
            rules: {
                rolename: [
                    { required: true, message: this.$t('addRoles.qsrjsmc'), trigger: 'blur' }
                ],
                roledescribe: [
                    { required: true, message: this.$t('addRoles.qsrjsms'), trigger: 'blur' }
                ],
            },

            treeData: [],
            defaultProps: {
                children: 'children',
                label: 'title'
            },
            idList: [],
            fangdou:true,
        }
    },

    created() {
        this.getUserRoleInfos()
    },

    methods: {
        async getUserRoleInfos() {
            const res = await getUserRoleInfo({
                api: 'admin.role.getUserRoleInfo'
            })
            console.log(res);
            this.treeData = res.data.data.menuList
            console.log(res);
        },

        filterNode(value) {
            if(value && value[0]) {
                filterNode(value.children)
            } else {
                value.children = []
            }
        },

        handleCheckChange () {
            let res = this.$refs.tree.getCheckedNodes().concat(this.$refs.tree.getHalfCheckedNodes())
            let arr = []
            res.forEach((item) => {
                arr.push(item.id)
            })
            this.idList = res.map(item => {
                return item.id
            })
            this.ruleForm.rolelist = this.idList
            console.log(this.idList);
        },

        submitForm(formName) {
            this.$refs[formName].validate(async (valid) => {
            console.log(this.ruleForm);
            if (valid) {
                try {
                    let { entries } = Object
                    let data = {
                        api: "admin.role.addUserRoleMenu",
                        permissions: this.idList.join(),
                        roleName: this.ruleForm.rolename,
                        describe: this.ruleForm.roledescribe,
                    }
              
                    let formData = new FormData()
                    for (let [key, value] of entries(data)) {
                        formData.append(key, value)
                    }
                    if(!this.fangdou){
                        return
                    }
                    this.fangdou=false
                    addUserRoleMenu(formData).then(res => {
                        if(res.data.code == '200') {
                            this.$message({
                                message: this.$t('zdata.tjcg'),
                                type: 'success',
                                offset: 100
                            })
                            this.$router.go(-1)
                        }
                        
                    })
                } catch (error) {
                    this.fangdou=true
                    this.$message({
                        message: error.message,
                        type: 'error',
                        showClose: true
                    })
                }
            } else {
                this.fangdou=true
                console.log('error submit!!');
                return false;
            }
            });
        },
    }
}
