import {
  getMemberConfig,
  addOrUpdate,
  saveMember
} from '@/api/plug_ins/members'
import { getStorage, setStorage } from '@/utils/storage'
import Config from '@/packages/apis/Config'
import $ from 'jquery'
export default {
  name: 'editorMenber',

  data () {
    return {
      radio1: '2',

      ruleForm: {
        isOpen: 1,
        open_config: [
          {
            day: '',
            openMethod: this.$t('member.memberSet.yk'),
            openMethodName: '',
            price: '',
            priceForDay: ''
          },
          {
            day: '',
            openMethod: this.$t('member.memberSet.jk'),
            openMethodName: '',
            price: '',
            priceForDay: ''
          },
          {
            day: '',
            openMethod: this.$t('member.memberSet.nk'),
            openMethodName: '',
            price: '',
            priceForDay: ''
          }
        ],
        birthdayOpen: 1,
        pointsMultiple: '',
        bonusPointsOpen: '',
        bonusPointsConfig: [
          {
            openMethod: this.$t('member.memberSet.yk'),
            points: ''
          },
          {
            openMethod: this.$t('member.memberSet.jk'),
            points: ''
          },
          {
            openMethod: this.$t('member.memberSet.nk'),
            points: ''
          }
        ],
        memberDiscount: '',
        renewOpen: 1,
        renewDay: '',
        memberEquity: []
      },

      rules: {
        memberDiscount: [
          {
            required: true,
            message: this.$t('member.memberSet.qtxhy'),
            trigger: 'blur'
          }
        ]
      },

      sequenceList: [
        {
          value: '1',
          label: this.$t('member.memberSet.yk')
        },
        {
          value: '2',
          label: this.$t('member.memberSet.jk')
        },
        {
          value: '3',
          label: this.$t('member.memberSet.nk')
        }
      ],

      equity: {
        equityName: '', //权益名称
        englishName: '', //英文名称
        icon: '' //图标
      }
    }
  },

  created () {
    this.getBase()
    this.getMemberConfig()
  },

  mounted () {
    this.$nextTick(function () {
      this.getHeight()
    })
    window.addEventListener('resize', this.getHeight(), false)
  },

  methods: {
    getBase () {
      this.goodsEditorBase = Config.baseUrl
    },
    async getMemberConfig () {
      const res = await getMemberConfig({
        api: 'plugin.member.AdminMember.getMemberConfig'
      })
      console.log(res)
      const Config = res.data.data
      if (!Config) {
        return
      }
      this.ruleForm.isOpen = Config.is_open
      if (Config.open_config && Config.open_config.length) {
        this.ruleForm.open_config = Config.open_config
      }
      this.ruleForm.birthdayOpen = Config.birthday_open
      this.ruleForm.pointsMultiple = Config.points_multiple
      this.ruleForm.bonusPointsOpen = Config.bonus_points_open
      if (Config.bonus_points_config && Config.bonus_points_config.length) {
        this.ruleForm.bonusPointsConfig = Config.bonus_points_config
      }
      this.ruleForm.memberDiscount = Config.member_discount
      this.ruleForm.renewOpen = Config.renew_open
      this.ruleForm.renewDay = Config.renew_day
      this.ruleForm.memberEquity = Config.member_equity
    },

    delEquity (index) {
      this.ruleForm.memberEquity.splice(index, 1)
    },

    add () {
      if (this.ruleForm.memberEquity.length == 4) {
        this.$message({
          message: this.$t('member.memberSet.hyqyzd'),
          type: 'error',
          showClose: true
        })
        return
      }
      if (
        !this.equity.equityName ||
        !this.equity.englishName ||
        !this.equity.icon
      ) {
        this.$message({
          message: this.$t('member.memberSet.qwsqy'),
          type: 'error',
          showClose: true
        })
        return
      }
      this.ruleForm.memberEquity.push(this.equity)
      this.equity = {
        equityName: '', //权益名称
        englishName: '', //英文名称
        icon: '' //图标
      }
      this.$refs.upload.fileList = []
    },

    reset () {
      this.equity = {
        equityName: '', //权益名称
        englishName: '', //英文名称
        icon: '' //图标
      }
      this.$refs.upload.fileList = []
    },

    submitForm (formName) {
      this.$refs[formName].validate(async valid => {
        console.log(this.ruleForm)
        if (valid) {
          try {
            saveMember({
                api: 'plugin.member.AdminMember.addOrUpdate',
                isOpen: this.ruleForm.isOpen,
                openConfig: JSON.stringify(this.ruleForm.open_config),
                birthdayOpen: this.ruleForm.birthdayOpen,
                pointsMultiple: this.ruleForm.pointsMultiple,
                bonusPointsOpen: this.ruleForm.bonusPointsOpen,
                bonusPointsConfig: JSON.stringify(
                  this.ruleForm.bonusPointsConfig
                ),
                memberDiscount: this.ruleForm.memberDiscount,
                renewOpen: this.ruleForm.renewOpen,
                renewDay: this.ruleForm.renewDay,
                memberEquity: JSON.stringify(this.ruleForm.memberEquity)
            }).then(res => {
                if(res.data.code == '200') {
                    this.$message({
                        message: this.$t('member.cg'),
                        type: 'success',
                        offset: 100
                      })
                      this.getMemberConfig()
                  }
            })
          } catch (error) {
            this.$message({
              message: error.message,
              type: 'error',
              showClose: true
            })
          }
        } else {
          console.log('error submit!!')
          return false
        }
      })
    }
  }
}
