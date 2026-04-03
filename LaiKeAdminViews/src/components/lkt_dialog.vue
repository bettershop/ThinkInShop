<!-- <Ldialog :title="'拒绝'" 
:dialogVisible="dialogVisible"
:myForm="myForm"
:determine="determine"
>

</Ldialog>
dialogVisible:true,//显示弹框
myForm:[
    {
        label:'拒绝理由',//名称
        prop:'reason',//参数名称
        type:'textarea',//type
        placeholder:'请输入拒绝理由（限200字内）'//占位符
    },
    {
        label:'其他理由',
        prop:'remake',
        placeholder:'请输入其他理由'
    },
] -->


<template>
    <div class="dialog-refuse">
      <!-- 弹框组件 -->
      <el-dialog
        :title="title"
        :visible.sync="dialogVisible"
        :before-close="handleClose"
      >
        <el-form
          :model="ruleForm"
          :rules="rules"
          ref="ruleForm"
          label-width="auto"
          class="demo-ruleForm"
        >
          <div class="pass-input">
            <el-form-item v-for="(item,index) in myForm" :key="index" :label="item.label" :prop="item.prop">
              <el-input
                :type="item.type"
                v-model="ruleForm[item.prop]"
                :placeholder="item.placeholder"
              ></el-input>
            </el-form-item>
          </div>
          <div class="form-footer">
            <el-form-item>
              <el-button @click="handleClose" class="qxcolor">{{$t('goodsclassifyExamine.ccel')}}</el-button>
              <el-button
                type="primary"
                @click="determine('ruleForm')"
                class="qdcolor"
                >{{$t('goodsclassifyExamine.okk')}}</el-button
              >
            </el-form-item>
          </div>
        </el-form>
      </el-dialog>
    </div>
</template>
<script>
export default {
  name: "export",
  props: {
    // 显示
    dialogVisible: {
      type: Boolean,
      default: false,
    },
    //请求接口
    api: {
      type: String,
      default: '',
    },
    //标题
    title: {
      type: String,
      default: '',
    },
    //渲染参数
    myForm:{
        type: Array,
        default: [],
    },
    //确认请求方法
    determine: {
      type: Function,
      default: ()=>{},
    },
  },
  //初始化数据
  data() {
    return {
        ruleForm: {
            
        },
        rules: {
            
        },

    };
  },
  watch:{

  },
  //组装模板
  created() {

  },
  
  methods: {
    
    handleClose(done) {
      this.dialogVisible = false;
      this.$refs.ruleForm.resetFields();
    },
  },
};
</script>
<style scoped lang="less">
.dialog-refuse {
    /* 弹框样式 */
    /deep/.el-dialog {
      width: 580px;
      height: 400px;
      position: absolute;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      margin: 0 !important;
      .el-dialog__header {
        width: 100%;
        height: 58px;
        line-height: 58px;
        font-size: 16px;
        margin-left: 19px;
        font-weight: bold;
        border-bottom: 1px solid #e9ecef;
        box-sizing: border-box;
        margin: 0;
        padding: 0 0 0 19px;
        .el-dialog__headerbtn {
          font-size: 18px;
          top: 0 !important;
        }
        .el-dialog__title {
          font-weight: normal;
          font-size: 16px;
          color: #414658;
        }
      }

      .el-dialog__body {
        padding: 41px 60px 16px 60px !important;
        .pass-input {
          /deep/.demo-ruleForm {
            width: 340px;
          }
          .el-textarea {
            width: 340px;
            height: 193px;
            border-radius: 4px;
            textarea {
              width: 340px;
              height: 193px !important;
              border-radius: 4px;
              padding-top: 9px;
            }
          }
        }
        .form-footer {
          width: 100%;
          height: 72px;
          position: absolute;
          bottom: 0;
          right: 0;
          display: flex;
          justify-content: flex-end;
          padding-right: 20px;
          border-top: 1px solid #e9ecef;
          .el-form-item {
            padding: 0 !important;
            height: 100%;
            .el-form-item__content {
              height: 100%;
              line-height: 72px;
              margin: 0 !important;
            }
          }

          .qxcolor {
            color: #6a7076;
            border: 1px solid #d5dbc6;
          }
          .qdcolor {
            background-color: #2890ff;
          }
          .qdcolor {
            background-color: #2890ff;
          }
          .qdcolor:hover {
            opacity: 0.8;
          }
          .qxcolor {
            color: #6a7076;
            border: 1px solid #d5dbe8;
            // margin-left: 14px;
          }
          .qxcolor:hover {
            color: #2890ff;
            border: 1px solid #2890ff;
            background-color: #fff;
          }
        }
      }
    }
    /deep/.el-form-item__label {
      font-weight: normal;
      color: #414658;
    }
  }
</style>