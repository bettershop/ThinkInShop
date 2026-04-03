<template>
  <div>
    <div class="btn-nav">
      <el-radio-group fill="#2890ff" text-color="#fff" v-model="radio1">
        <el-radio-button label="2" @click.native.prevent="$router.push('/Platform/system/graphics')">{{ $t('archive.tpsz') }}</el-radio-button>
        <el-radio-button label="3" @click.native.prevent="$router.push('/Platform/system/archive')">{{ $t('archive.sjkbf') }}</el-radio-button>
        <el-radio-button v-if="isJava" label="4" @click.native.prevent="openScheduledTask">{{ $t('archive.dsrw') }}</el-radio-button>

      </el-radio-group>
    </div>
    <div class="container">

      <div class="picture-info">
        <div class="ali-cloud">
          <el-form :model="ruleForm3" :rules="rules3" ref="ruleForm3"  class="aliCloud-ruleForm" :label-width="language=='en'?'auto':'187px'">
            <!-- <el-form-item class="head-title" :label="$t('graphics.alypz')">
            </el-form-item> -->
            <el-form-item  :label="$t('graphics.cclx')">
              <el-radio-group v-model="ruleForm3.type" @change="typeChange()">
                <el-radio label="5">{{$t('graphics.minio')}}</el-radio>
                <el-radio label="2">{{$t('graphics.aly')}}</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item :label="$t('graphics.bucket')" prop="ossbucket">
              <el-input v-model="ruleForm3.ossbucket" :placeholder="$t('graphics.qsrbucket')"></el-input>
              <span class="fixed">{{$t('graphics.qszcc')}}</span>
            </el-form-item>
            <el-form-item label="Endpoint" prop="ossendpoint">
              <el-input v-model="ruleForm3.ossendpoint" :placeholder="$t('graphics.qsrend')"></el-input>
              <span class="fixed">{{$t('graphics.lzoss')}}</span>
            </el-form-item>
            <!-- <el-form-item label="MyEndpoint(自定义域名)" prop="myEndpoint">
              <el-input v-model="ruleForm3.myEndpoint" placeholder="请输入MyEndpoint(自定义域名)"></el-input>
              <span class="fixed">例子：aaa.bbb.com，结尾不需要/</span>
            </el-form-item>
            <el-form-item class="isOpenDiyDomain" label="是否开启自定义域名">
              <el-radio-group v-model="ruleForm3.isOpenDiyDomain" style="margin-left:-20Px">
                <el-radio v-for="item in isOpenDiyDomainList" :label="item.value" :key="item.value">{{item.name}}</el-radio>
              </el-radio-group>
            </el-form-item> -->
            <el-form-item label="Access Key ID" prop="ossaccesskey">
              <el-input v-model="ruleForm3.ossaccesskey" :placeholder="$t('graphics.qsracc')"></el-input>
              <span class="fixed" v-if="ruleForm3.type == 5">{{$t('graphics.miniozh')}}</span>
              <span class="fixed" v-if="ruleForm3.type == 2">{{$t('graphics.alyzh')}}</span>
            </el-form-item>
            <el-form-item label="Access Key Secret" prop="ossaccesssecret">
              <el-input v-model="ruleForm3.ossaccesssecret" :placeholder="$t('graphics.qsraccs')" show-password></el-input>
              <span class="fixed" v-if="ruleForm3.type == 5">{{$t('graphics.miniomm')}}</span>
              <span class="fixed" v-if="ruleForm3.type == 2">{{$t('graphics.alymm')}}</span>
            </el-form-item>
            <el-form-item label="Server URI" prop="serveruri" v-if="ruleForm3.type == 5">
              <el-input v-model="ruleForm3.serveruri" :placeholder="$t('graphics.qsrseruri')"></el-input>
              <span class="fixed" v-if="ruleForm3.type == 5">{{$t('graphics.qsrseruri')}}</span>
            </el-form-item>
            <!-- <el-form-item label="图片样式接口（选填）">
              <el-input v-model="ruleForm3.ossimgstyleapi" placeholder="请输入图片样式接口（选填）"></el-input>
            </el-form-item> -->
            <div class="form-footer">
              <el-form-item>
                <el-button class="bgColor" type="primary" @click="submitForm3('ruleForm3')">{{ $t('DemoPage.tableFromPage.save') }}</el-button>
                <!-- <el-button class="bdColor" @click="$router.go(-1)" plain>{{ $t('DemoPage.tableFromPage.cancel') }}</el-button> -->
              </el-form-item>
            </div>
          </el-form>
        </div>

      </div>
    </div>
  </div>
</template>

<script>
import { addImageConfigInfo, getImageConfigInfo } from '@/api/Platform/graphics'
export default {
  name: 'graphics',

  data() {
    return {
      radio1:2,
      language:"",
      // 上传方式数据
      ruleForm1: {
        type: '1'
      },
      radio: '本地',
      rules1: {
        type: [
          { required: true, message: '是', trigger: 'change' }
        ],
      },

      //阿里云数据
      ruleForm3: {
        ossbucket: '',
        ossendpoint: '',
        type:'',
        // myEndpoint: '',
        // isOpenDiyDomain: 0,
        ossaccesskey: '',
        ossaccesssecret	: '',
        serveruri	: '',
        // ossimgstyleapi: ''
      },
      rules3: {
        ossbucket: [
          { required: true, message: this.$t('graphics.qsrbucket'), trigger: 'blur' }
        ],
        ossendpoint: [
          { required: true, message: this.$t('graphics.qsrend'), trigger: 'blur' }
        ],
        ossaccesskey: [
          { required: true, message: this.$t('graphics.qsracc'), trigger: 'blur' }
        ],
        ossaccesssecret: [
          { required: true, message: this.$t('graphics.qsraccs'), trigger: 'blur' }
        ],
        serveruri: [
          { required: true, message: this.$t('graphics.qsrseruri'), trigger: 'blur' }
        ]
      },

      isOpenDiyDomainList: [
        {
          value: 0,
          name: this.$t('graphics.No')
        },
        {
          value: 1,
          name: this.$t('graphics.Yes')
        },
      ],
    }
  },

  computed: {
    isJava() {
      return process.env.VUE_APP_LANG_TYPE == 1
    }
  },

  watch: {

  },

  created() {
    this.language = this.getCookit()
    this.getImageConfigInfos2()
  },

  methods: {
      // 打开定时任务的外部链接
      openScheduledTask() {
        window.open(process.env.VUE_APP_XXL_JOB_URL, '_blank');
      },
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
    typeChange(){
      // 去除表单校验结果
      this.$refs.ruleForm3.resetFields()
      this.getImageConfigInfos2()
    },
    async getImageConfigInfos2() {
      const res = await getImageConfigInfo({
        api: 'saas.image.getImageConfigInfo',
        type: this.ruleForm3.type
      })
      console.log('res',res);
      if(res.data.code == 200 && res.data.data.data.length>0) {
        let info = res.data.data.data
        for(let i=0; i<info.length; i++) {
          this.getValue(info[i])
        }
      }else{
        this.ruleForm3.ossbucket=""
        this.ruleForm3.ossendpoint =""
        this.ruleForm3.ossaccesskey=""
        this.ruleForm3.ossaccesssecret=""
        this.ruleForm3.serveruri =""
        // this.ruleForm3.type=value.upserver
      }
    },

    getValue(value) {
      console.log('value',value);

      if(value.attr == 'Bucket') {
        this.ruleForm3.ossbucket = value.attrvalue
      } else if(value.attr == 'Endpoint') {
        this.ruleForm3.ossendpoint = value.attrvalue
      } else if(value.attr == 'isopenzdy') {
        // this.ruleForm3.isOpenDiyDomain = Number(value.attrvalue)
      } else if(value.attr == 'AccessKeyID') {
        this.ruleForm3.ossaccesskey = value.attrvalue
      } else if(value.attr == 'AccessKeySecret') {
        this.ruleForm3.ossaccesssecret = value.attrvalue
      } else if(value.attr == 'serveruri') {
        this.ruleForm3.serveruri = value.attrvalue
      } else if(value.attr == 'MyEndpoint') {
        // this.ruleForm3.myEndpoint = value.attrvalue
      }
      this.ruleForm3.type = value.upserver
    },

    submitForm3(formName) {
      this.$refs[formName].validate(async (valid) => {
        if (valid) {
          try {
            addImageConfigInfo({
              api: 'saas.image.addImageConfigInfo',
              type: this.ruleForm3.type,
              ossbucket: this.ruleForm3.ossbucket,
              ossendpoint: this.ruleForm3.ossendpoint,
              myEndpoint: '',
              isOpenDiyDomain: 0,
              ossaccesskey: this.ruleForm3.ossaccesskey,
              ossaccesssecret: this.ruleForm3.ossaccesssecret,
              serveruri: this.ruleForm3.serveruri,
              // ossimgstyleapi: this.ruleForm3.ossimgstyleapi
            }).then(res => {
              console.log(res);
              if(res.data.code == '200') {
                this.$message({
                  message: this.$t('graphics.bccg'),
                  type: 'success',
                  offset:102,
                })
              }
            })
          } catch (e) {
            this.$message({
              message: e.message,
              type: 'error',
              showClose: true,
              offset:102,
            })
          }
        } else {
          return false;
        }
      });
    },
  }
}
</script>

<style scoped lang="less">
.fixed{
  position: fixed;
  color: #97a0b4 !important;
  margin-left: 14px;
}
.container {
  width: 100%;
  height: 607px;
  background-color: #fff;
  padding: 40px 0 0 0;
  color: #414658;
  border-radius: 4px;
  /deep/.header-radio {
    padding-left: 446px;

    .el-radio {
      margin-right: 20px;
    }
  }

  /deep/.picture-info {
    .local {
      padding-left: 431px;
      .picture-ruleForm {
        .el-form-item {
          display: flex;
          .el-form-item__content {
            width: 580px;
            height: 40px;
            .el-radio-group{
              margin-left: -20Px;
            }
          }
        }

        .form-footer {
          padding-left: 106px;
        }
      }

    }

    .ali-cloud {
      // padding-left: 348px;
      display: flex;
      justify-content: center;
      align-items: center;
      .aliCloud-ruleForm {
        .el-form-item {
          display: flex;
          &:not(:last-child) {
            .el-form-item__content {
              margin-left: 0px !important;
            }
          }
          .el-form-item__content {
            .el-radio {
              // margin-right: 0px;
            //   .el-radio__label{
            //   margin-left: 0;
            // }
            }
            .el-input {
              width: 580px;
              height: 40px;
            }
            span {
              // margin-left: 14px;
              color: #414658;
            }
          }
        }

        .head-title {
          .el-form-item__label {
            font-size: 16px;
            color: #414658 !important;
          }
        }

        .form-footer {
          .bgColor {
            width: 70px;
            height: 40px;
            text-align: center;
            background-color: #2890ff;
            color: #fff;
            span {
              color: #fff;
              margin-left: 0;
            }
          }
          .bgColor:hover {
            opacity: 0.8;
          }
          .bdColor {
            width: 70px;
            height: 40px;
            text-align: center;
            color: #6a7076;
            border: 1px solid #d5dbe8;
            margin-left: 14px;
            span {
              margin-left: 0;
            }
          }
          .bdColor:hover {
            color: #2890ff;
            border: 1px solid #2890ff;
          }
        }
      }
    }

  }
  .btn-nav{
    margin-bottom:20px
  }
  // /deep/.el-form-item__label {
  //   font-weight: normal;
  // }
}
</style>
