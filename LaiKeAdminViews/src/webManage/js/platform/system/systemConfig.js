import {index, save} from '@/api/Platform/system'
import {isEmpty} from "element-ui/src/utils/util";

export default {
  name: 'systemConfig',

  //初始化数据
  data() {
    return {
      language:"",
      formMain: {
      },
      dialogVisible:false,
      ruleForm2:{
        id:'',
      },
      formRules : {
        logo: [{required: true, message: this.$t('systemConfig.qxzlogo'), trigger: 'change'}],
        copyright_information: [{required: true, message: this.$t('systemConfig.qsrbqxx'), trigger: 'change'}],
        record_information: [{required: true, message: this.$t('systemConfig.qsrbaxx'), trigger: 'blur'}],
        h5Domain: [{required: true, message: this.$t('systemConfig.srh5'), trigger: 'blur'}],
        domainPath: [{required: true, message: this.$t('systemConfig.qsrgmllj'), trigger: 'blur'}],
         adminDefaultPortrait:[{required: true,message: this.$t('systemConfig.qscmrtp'),trigger: 'change' }]
        // store_id_prefix: [{required: true, message: this.$t('systemConfig.text2'), trigger: 'blur'}],
      },
      rules2: {
        id: [
          {
            required: true,
            message: this.$t('systemConfig.text2'),
            trigger: 'blur'
          }
        ],
      },
    }
  },
  //组装模板
  created() {
    this.language = this.getCookit()
    this.loadData()
  },

  methods: {
    // 获取cookiet
    getCookit(){
      let myCookie = document.cookie.split(';').map(item=>{
         let arr = item.split('=')
         return {name:arr[0],value:arr[1]}
       })
       let strCookit = ''
       myCookie.forEach(item=>{
         if(item.name.indexOf('language')!==-1){
           strCookit = item.value
         }
       })
       return strCookit
    },
    addId(){
      this.dialogVisible = true
    },
    handleClose(){
      this.ruleForm2.id = ''
      this.$nextTick(() => {
        this.$refs['ruleForm2'].clearValidate()
      })
      this.dialogVisible = false

    },
    async loadData() {
      const res = await index({
        api: 'admin.system.getSetSystem',
        // storeId: 0

      }).then(data => {
        let obj = data.data.data.config;
        console.log(obj)
        if (isEmpty(obj)) {
          obj = {id: null, logoUrl: "", copyrightInformation: null, recordInformation: null, linkPage: null,adminDefaultPortrait:""};
        }
        if(!obj.link_to_landing_page || obj.link_to_landing_page == '[]') {
          obj.linkPage = [{name: null, url: null}];
        } else {
          obj.linkPage = JSON.parse(obj.link_to_landing_page);
        }
        obj.h5Domain=data.data.data.h5Domain
        obj.domainPath=data.data.data.domainPath
        obj.rules = {
          logo: [{required: true, message: this.$t('systemConfig.qxzlogo'), trigger: 'change'}],
          copyright_information: [{required: true, message: this.$t('systemConfig.qsrbqxx'), trigger: 'change'}],
          record_information: [{required: true, message: this.$t('systemConfig.qsrbaxx'), trigger: 'blur'}],
          h5Domain: [{required: true, message: this.$t('systemConfig.srh5'), trigger: 'blur'}],
          domainPath: [{required: true, message: this.$t('systemConfig.qsrgmllj'), trigger: 'blur'}],
          //  adminDefaultPortrait:[{required: true,message: this.$t('systemConfig.qscmrtp'),trigger: 'change' }]
          // store_id_prefix: [{required: true, message: this.$t('systemConfig.text2'), trigger: 'blur'}],
        }
        this.formMain = obj;
        let { entries } = Object;
        for (let [key, value] of entries(this.formMain)) {
          if(value!==null&&value!==""&&value!==undefined){
            this.formMain[key] = value
        }else{
          this.formMain[key] = ""
        }
      }
        if(!obj.adminDefaultPortrait){
          this.formMain.adminDefaultPortrait=''
          // obj.rules.adminDefaultPortrait=[{required: true,message: this.$t('systemConfig.qscmrtp'),trigger: 'change' }]
        }else{
          // obj.rules.adminDefaultPortrait=[{required: true,message: this.$t('systemConfig.qscmrtp'),trigger: 'change' }]
        }
        this.$refs.ruleForm.resetFields()
      });
    },
    addUrl() {
      this.formMain.linkPage.push({})
    },
    delUrl(index) {
      this.formMain.linkPage.splice(index, 1)
    },
    determine(formName){
      this.$refs[formName].validate(async (valid) => {
        if (valid) {
         this.formMain.store_id_prefix = this.ruleForm2.id
         this.handleClose()
        } else {
          return false;
        }
      })
    },
    //添加/编辑
    async Save(formName) {
      let text = this.$t('zdata.baccg');
      if (isEmpty(this.formMain.id)) {
        text = this.$t('zdata.tjcg');
      }
      // if(this.formMain.adminDefaultPortrait.length<=0){
      //   return this.$message({
      //     message:  this.$t('systemConfig.qscmrtp'),
      //     type: 'warning',
      //     offset: 102
      //   })
      // }

      this.$refs[formName].validate(async (valid) => {
        if (valid) {
          const res = await index({
            api: 'admin.system.setSystem',
            id: this.formMain.id?this.formMain.id:"",
            logoUrl: this.formMain.logo,
            copyrightInformation: this.formMain.copyright_information,
            recordInformation: this.formMain.record_information,
            linkPageJson: encodeURIComponent(JSON.stringify(this.formMain.linkPage)),
            // storeId: 0,
            h5Domain:this.formMain.h5Domain ,
            domainPath:this.formMain.domainPath,
            storeIdPrefix:this.formMain.store_id_prefix,
            adminDefaultPortrait:this.formMain.adminDefaultPortrait
          }).then(data => {
            if (data.data.code == '200' && !isEmpty(data)) {
              this.$message({
                message: text ,
                type: 'success',
                offset: 102
              })
            }
          })
        } else {
          return false;
        }
      })

    },

  }

}
