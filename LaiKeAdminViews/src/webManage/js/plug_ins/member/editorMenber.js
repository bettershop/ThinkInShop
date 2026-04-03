import { getMemberInfo, updateMember } from '@/api/plug_ins/members'
import ErrorImg from "@/assets/images/default_picture.png";

export default {
  name: 'editorStore',

  data () {
    return {
      forbiddenTime: {
        disabledDate (time) {
          return time.getTime() < Date.now() - 8.64e7
        }
      },
      ruleForm: {
        grade_end: '',
        grade_m: '3',
        headimgurl: '',
        id: 1429,
        is_out: '0',
        phone: null,
        user_id: 'user1430',
        user_name: '用户同步003',
        zhanghao: '25658629',
        gradeMDesc:'月卡'
      },

      memberInfo: null
    }
  },

  created () {
    this.getMemberInfo()
  },

  beforeRouteLeave (to, from, next) {
    if (to.name == 'store') {
      to.params.dictionaryNum = this.$route.query.dictionaryNum
      to.params.pageSize = this.$route.query.pageSize
    }
    next()
  },

  methods: {
     // 图片错误处理
     handleErrorImg(e) {
      console.log("图片报错了", e.target.src);
      e.target.src = ErrorImg;
    },
    async getMemberInfo () {
      const res = await getMemberInfo({
        api: 'plugin.member.AdminMember.GetMemberInfo',
        userId: this.$route.query.id
      })

      console.log(res)
      this.memberInfo = res.data.data

      let editorInfo = res.data.data

      this.ruleForm.grade_end = editorInfo.grade_end
      this.ruleForm.grade_m = editorInfo.grade_m
      this.ruleForm.headimgurl = editorInfo.headimgurl+'?a=xxxxx'
      this.ruleForm.id = editorInfo.id
      this.ruleForm.is_out = editorInfo.is_out
      this.ruleForm.phone = editorInfo.phone
      this.ruleForm.cpc = editorInfo.cpc
      this.ruleForm.user_id = editorInfo.user_id
      this.ruleForm.user_name = editorInfo.user_name
      this.ruleForm.zhanghao = editorInfo.zhanghao
      // this.ruleForm.gradeMDesc = editorInfo.gradeMDesc
    },

    submitForm () {
      updateMember({
        api: 'plugin.member.AdminMember.UpdateMember',
        userId: this.$route.query.id,
        overTime: this.ruleForm.grade_end
      }).then(res => {
        if (res.data.code == '200') {
          this.$message({
            message: this.$t('member.cg'),
            type: 'success',
            offset: 100
          })
          this.$router.go(-1)
        }
        console.log(res)
      })
    }
  }
}
