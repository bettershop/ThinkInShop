import { getUserInfo } from '@/api/members/membersList'

export default {
    name: 'viewMembers',

    data() {
        return {
            laikeCurrencySymbol:'￥',
            userInfo: null
        }
    },

    created() {
        this.laikeCurrencySymbol = this.LaiKeCommon.getDefaultCurrencySymbol();
        this.getUserInfos()
    },

    beforeRouteLeave (to, from, next) {
        if (to.name == 'membersLists') {
          to.params.dictionaryNum = this.$route.query.dictionaryNum
          to.params.pageSize = this.$route.query.pageSize
          to.params.inputInfo = this.$route.query.inputInfo
        }
        next();
    },

    methods: {
        async getUserInfos() {
            const res = await getUserInfo({
                api: 'admin.user.getUserInfo',
                uid: this.$route.query.id
            })
            this.userInfo = res.data.data.list[0]
            //因为账号来源用的是公共方法，那在公共方法中增加一个类型判断 12 为PC端；
            //所以此处增加数据处理，如果是PC开头的来源都改为PC端。
            if(this.userInfo.source == 6 || this.userInfo.source == 7 || this.userInfo.source == 8 || this.userInfo.source == 9){
              this.userInfo.source = 12
            }
            console.log(this.userInfo);
        },


    }
}
