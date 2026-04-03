import { modifyMchInfo } from '@/api/plug_ins/stores'
export default {
  name: 'addStoreFl',
  data () {
    return {
      ruleForm: {
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
      ],
      max: '3'
    }
  },
  created () {
    this.max = this.$route.query.max_sort + 1
  },
  methods: {
    //添加店铺分类
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
        name: this.ruleForm.name,
        img: this.ruleForm.img,
        sort: this.ruleForm.px,
        isDisplay: this.ruleForm.ishow
      }).then(res => {
        console.log('添加店铺分类-->', res)
        if (res.data.code == '200') {
          this.$message({
            message: this.$t('zdata.tjcg'),
            type: 'success',
            offset: 102
          })
          this.$router.go(-1)
        }
      })
    }
  }
}
