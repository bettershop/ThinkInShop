import { getMchFl, modifyMchInfo } from '@/api/plug_ins/stores'
export default {
  name: 'addStoreFlSet',
  data () {
    return {
      ruleForm: {
        id: '',
        name: '',
        img: '',
        px: '',
        ishow: 1
      },
      belongList: [
        {
          value: 0,
          name: this.$t('stores.addStoreFl.fou')
        },
        {
          value: 1,
          name: this.$t('stores.addStoreFl.shi')
        }
      ]
    }
  },
  created () {
    this.getMchExamineInfos()
  },
  methods: {
    //获取分类店铺详情
    async getMchExamineInfos () {
      const res = await getMchFl({
        api: 'mch.Admin.Mch.MchClassList',
        id: this.$route.query.id
      })
      console.log('获取分类店铺详情res--》', res)
      if (res.data.code == 200) {
        this.ruleForm.id = res.data.data.list[0].id
        this.ruleForm.name = res.data.data.list[0].name
        this.ruleForm.img = res.data.data.list[0].img
        this.ruleForm.px = res.data.data.list[0].sort
        this.ruleForm.ishow = res.data.data.list[0].is_display
      } else {
        console.log(res.data.message)
      }
    },
    //修改店铺分类
    submitForm () {
      if (this.ruleForm.name.length > 20) {
        this.$message({
          message: this.$t('stores.addStoreFl.flmcc'),
          type: 'error',
          offset: 102
        })
        return
      }
      modifyMchInfo({
        api: 'mch.Admin.Mch.AddMchClass',
        id: this.ruleForm.id,
        name: this.ruleForm.name,
        img: this.ruleForm.img,
        sort: this.ruleForm.px,
        isDisplay: this.ruleForm.ishow
      }).then(res => {
        console.log('修改店铺分类-->', res)
        if (res.data.code == '200') {
          this.$message({
            message: this.$t('zdata.bjcg'),
            type: 'success',
            offset: 102
          })
          this.$router.go(-1)
        }
      })
    }
  }
}
