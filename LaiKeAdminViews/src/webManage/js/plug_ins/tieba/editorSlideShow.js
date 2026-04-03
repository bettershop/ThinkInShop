import { bannerPathList, bannerSave } from '@/api/plug_ins/template'
export default {
    name: 'editorSlideShow',
    data() {
        return {
            ruleForm: {
                img: '',
                class1: '',
                url: ''
            },

            classList1: [
                {
                    value: 2,
                    label: this.$t('template.addSlideShow.sp')
                },
                {
                    value: 1,
                    label: this.$t('template.addSlideShow.fl')
                },
                {
                    value: 3,
                    label: this.$t('template.addSlideShow.dp')
                },
                {
                    value: 0,
                    label: this.$t('template.addSlideShow.zdy')
                }
            ],

            classList2: [],
            baseUrl: ''
        }
    },

    watch: {
        'ruleForm.class1'(newVal,oldVal) {
            if(this.ruleForm.class1 == 1) {
                this.baseUrl = '/pagesC/goods/goods?cid='
                this.ruleForm.url = ''
                this.bannerPathLists(this.ruleForm.class1)
            } else if(this.ruleForm.class1 == 3) {
                this.baseUrl = '/pagesB/store/store?shop_id='
                this.ruleForm.url = ''
                this.bannerPathLists(this.ruleForm.class1)
            } else if(this.ruleForm.class1 == 0) {
                if(oldVal) {
                    this.ruleForm.url = ''
                }
                this.baseUrl = ''
                this.classList2 = []
            } else {
                this.baseUrl = '/pagesC/goods/goodsDetailed?productId='
                this.ruleForm.url = ''
                this.bannerPathLists(this.ruleForm.class1)
            }
        }
    },

    created() {
        this.ruleForm.class1 = this.$route.params.open_type ? parseInt(this.$route.params.open_type) : 0
        if(this.ruleForm.class1 !== 0) {
            this.bannerPathLists(this.ruleForm.class1).then(() => {
                this.ruleForm.url = this.$route.params.url.split('?')[1]
            })
        } else {
            this.ruleForm.url = this.$route.params.url
        }

        this.ruleForm.img = this.$route.params.image
    },

    beforeRouteLeave (to, from, next) {
        if (to.name == 'playlist') {
          to.params.dictionaryNum = this.$route.query.dictionaryNum
          to.params.pageSize = this.$route.query.pageSize
        }
        next();
    },

    methods: {
        async bannerPathLists(value) {
            const res = await bannerPathList({
                // api: 'admin.diy.bannerPathList',
                api: 'plugin.template.AdminDiy.BannerPathList',
                type: value
            })
            console.log(res);
            this.classList2 = res.data.data.list
        },

        submitForm() {
            if(this.ruleForm.class1 == 0 && this.ruleForm.url.length > 100) {
                this.$message({
                    message: this.$t('template.addSlideShow.zdyurl'),
                    type: 'error',
                    offset:100
                })
                return
            }
            bannerSave({
                // api: 'admin.diy.bannerSave',
                api: 'plugin.template.AdminDiy.BannerSave',
                id: this.$route.params.id,
                type0: this.ruleForm.class1,
                url: this.getUrl(),
                picUrl: this.ruleForm.img
            }).then(res => {
                if(res.data.code == '200') {
                    this.$message({
                        message: this.$t('template.cg'),
                        type: 'success',
                        offset:100
                    })
                    this.$router.go(-1)
                }
                console.log(res);
            })
        },

        getUrl() {
            if(this.ruleForm.class1 == 0) {
                return this.ruleForm.url
            } else {
                if(this.ruleForm.url) {
                    return this.baseUrl + this.ruleForm.url.split('=')[1]
                } else {
                    return ''
                }
            }
        }
    }

}
