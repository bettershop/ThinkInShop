<!--
    插件简介：
    xuxiong
    基本逻辑都是在组件内处理
    visible.sync的参数用来控制组件是否显示，v-if是为了重新渲染
    data-source是回显的数据来源
    attr-key是需要回显的key数组，绑定在el-transfer的v-model上
    @submit是父子交互的提交事件，可以获取到商品所需参数
    以下是使用实例

    <LktTransfer
      v-if="transferDialog"
      :visible.sync="transferDialog"
      :data-source="dataSource"
      :attr-key="attrKey"
      @submit="handleSubmit"
    />
 -->
<template>
  <div class="Transfer">
    <el-dialog
      :close-on-click-modal="false"
      :visible.sync="dialogVisible"
      :title="$t('transferDialog.szsx')"
    >
      <el-input
        class="filter"
        v-model="keyword"
        :placeholder="$t('transferDialog.qsrsxmchsxz')"
        @input="filterMethod"
        @clear="clearMethod"
        clearable
      ></el-input>
      <el-transfer
        target-order="unshift"
        v-loading="loading"
        :element-loading-text="$t('DemoPage.tableExamplePage.loading_text')"
        ref="transfer"
        :props="{ key: 'sid', label: 'label' }"
        v-model="selectedList"
        :data="transferData"
        :titles="[$t('transferDialog.xzsx'), $t('transferDialog.yx')]"
        @change="transferChange"
      >
        <el-button
          class="transfer-footer"
          type="text"
          slot="right-footer"
          size="small"
          @click="openChild"
          >{{ $t("transferDialog.tjxsx") }}</el-button
        >
        <span slot="right-footer" style="color: #d5dbe8"> | </span>
        <el-button
          type="text"
          slot="right-footer"
          size="small"
          @click="clearRight"
          >{{ $t("transferDialog.qk") }}</el-button
        >
      </el-transfer>
      <div class="left-footer" v-show="loadingShow && isLoading">
        {{ $t("transferDialog.loding") }}
      </div>
      <span slot="footer" class="Transfer-footer">
        <el-button class="cancel" @click="dialogVisible = false">{{
          $t("transferDialog.qx")
        }}</el-button>
        <el-button type="primary" @click="submit">{{
          $t("transferDialog.qd")
        }}</el-button>
      </span>
      <el-dialog
        class="child"
        :title="$t('transferDialog.tjsxz')"
        :visible.sync="childDialogVisible"
        :before-close="childClose"
        append-to-body
      >
        <el-form
          :model="ruleForm"
          ref="ruleForm"
          label-width="auto"
          class="ruleForm"
        >
          <el-form-item :label="$t('transferDialog.tjfs')">
            <el-radio-group v-model="ruleForm.radio" @change="clearBute">
              <el-radio :label="1">{{ $t("transferDialog.tjsxmc") }}</el-radio>
              <el-radio :label="2">{{ $t("transferDialog.tjsxz") }}</el-radio>
            </el-radio-group>
            <div class="box_add">
              <el-form-item :label="$t('transferDialog.sxmc')">
                <div class="box_center" v-if="ruleForm.radio == 1">
                  <el-input
                    class="buteInput"
                    oninput="value=value.replace(/\[|\]/g, '')"
                    v-model="ruleForm.name"
                    :placeholder="$t('transferDialog.qsrsxmc')"
                  ></el-input>
                  <el-button class="add_bt" @click="addBute">{{
                    $t("transferDialog.tjsx")
                  }}</el-button>
                </div>
                <div class="box_center" v-if="ruleForm.radio == 2">
                  <!-- collapse-tags -->
                  <el-select
                    class="buteSelect"
                    v-model="ruleForm.buteName"
                    multiple
                    filterable
                    default-first-option
                    value-key="id"
                    :placeholder="$t('transferDialog.qxzsxmc')"
                    @change="addBute"
                  >
                    <el-option
                      v-for="item in options"
                      :key="item.id"
                      :label="item.name"
                      :value="item"
                    >
                    </el-option>
                  </el-select>
                </div>
              </el-form-item>
              <el-form-item
                v-for="(item, index) in buteList"
                :label="item.head"
                :key="index"
              >
                <div>
                  <div class="box_center">
                    <el-input
                      class="add_input"
                      v-model="item.name"
                      oninput="value=value.replace(/\[|\]/g, '')"
                      :placeholder="$t('transferDialog.qsrsxz')"
                    ></el-input>
                    <el-button class="add_bt" @click="addValue(item, index)">{{
                      $t("transferDialog.tjsxz")
                    }}</el-button>
                    <el-button class="delete_bt" @click="delBute(index)">{{
                      $t("transferDialog.scsx")
                    }}</el-button>
                  </div>
                  <div class="attrValue">
                    <span v-for="(itm, ind) in item.valueList" :key="ind"
                      >{{ itm.sname }}
                      <img
                        @click="delValue(index, ind)"
                        src="../../public/bounced/img/gb.png"
                      />
                    </span>
                  </div>
                </div>
              </el-form-item>
            </div>
          </el-form-item>
        </el-form>
        <span slot="footer" class="Transfer-footer">
          <el-button class="cancel" @click="childClose">{{
            $t("transferDialog.qx")
          }}</el-button>
          <el-button type="primary" @click="submitChild">{{
            $t("transferDialog.tj")
          }}</el-button>
        </span>
      </el-dialog>
    </el-dialog>
  </div>
</template>

<script>
import { getSkuList } from "@/api/goods/goodsList";
export default {
  props: {
    // 控制弹窗显示
    visible: {
      type: Boolean,
      default: false,
    },
    // 回显数据
    dataSource: {
      type: Array,
      default: () => [],
    },
    //回显数据的key
    attrKey: {
      type: Array,
      default: () => [],
    },
    lang_code:{
      type: String,
      default: 'zh_CN',
    },
    country_num:{
      type: Number,
      default: 156,
    }
  },
  data() {
    return {
      loading: true, //数据loading
      pop_lang_code: 'zh_CN', //用户所选语种默认 zh_CN
      pop_country_num: 156, //功能数据中用户所选国家 默认中国
      selectedList: [], // 已选择的列表
      //transferData: this.dataSource.map(item => ({ label: item, key: item })),
      transferData: [], //数据来源
      page: 1, //分页参数
      isLoading: false, //判断是否继续下拉加载
      keyword: "", //搜索条件
      bottomOfWindow: "", //用来判断是否显示正在加载
      loadingShow: true, //用来判断是否显示正在加载
      total: "", //总数
      childDialogVisible: false, //添加新属性弹窗
      //添加新属性弹窗参数 start
      ruleForm: {
        radio: 1, //添加方式 1:添加属性名称 2：添加属性值
        name: "", //属性名称字段
        buteName: "", //属性值数据
      },
      buteList: [], //得到的所有参数数组
      options: [], //属性值列表
      //添加新属性弹窗参数 end
      zcList: [], //左侧数据暂存数组
      arr: [], //用来去重赋值给transferData
    };
  },
  computed: {
    dialogVisible: {
      get() {
        return this.visible;
      },
      set(val) {
        this.$emit("update:visible", val);
      },
    },
  },
  watch: {
    // transferData () {
    //   if (this.transferData.length <= 9 && this.isLoading == false) {
    //     this.page += 1
    //     this.getList()
    //   }
    // },
    zcList() {
      if (this.isLoading == false) {
        this.page += 1;
        this.getList();
      }
    },
  },
  mounted() {
    console.log("组件执行mounted");
    //给穿梭框v-model赋初始值
    if (this.attrKey.length > 0) {
      this.selectedList = this.attrKey;
    }
    //必须把sid定义为number类型，不然无法在穿梭框回显，会导致出现多个重复数据
    if (this.dataSource.length > 0) {
      this.zcList = this.dataSource.map(({ id, sid, ...rest }) => {
        return { id: Number(id), sid: Number(sid), ...rest };
      });
    }
    console.log("mounted-zcList", this.zcList);
    console.log("mounted-selectedList", this.selectedList);
    //获取dom元素来监听滚动
    this.$nextTick(() => {
      let scrollBox = this.$refs.transfer.$el.getElementsByClassName(
        "el-transfer-panel__body"
      )[0];
      scrollBox.addEventListener(
        "scroll",
        (res) => {
          this.lazyLoading(res);
        },
        true
      );
    });
    this.pop_lang_code = this.lang_code;
    this.pop_country_num = this.country_num;
    //数据初始化
    this.getList();
  },
  methods: {
    //初始化数据
    async getList() {
      const res = await getSkuList({
        api: "saas.dic.getSkuList",
        keyword: this.keyword,
        pageNo: this.page,
        lang_code: this.pop_lang_code,
        strArr: JSON.stringify(this.zcList),
      });
      // console.log('res', res)
      //用来判断是否还可以下拉，是否继续展示正在加载
      if (res.data.data.list.length < 10) {
        this.isLoading = true;
        this.loadingShow = false;
      } else {
        this.isLoading = false;
      }
      this.total = res.data.data.total;
      res.data.data.list.filter((item) => {
        item.label = item.name + ":" + item.sname;
      });
      this.arr.push(...res.data.data.list);
      this.arr.push(...this.zcList); //把右边暂存的数据push进来，在下面去重，防止sid重复
      //去重，防止sid重复
      this.arr = this.arr.reduce((accumulator, current) => {
        // 非空判断
         if(!current || !current.sid){
          return accumulator
        }
        const existingItem = accumulator.find(
          (item) => item.sid == current.sid
        );
        if (!existingItem) {
          return accumulator.concat([current]);
        } else {
          return accumulator;
        }
      }, []);
      console.log("去重后的结果", this.arr);

      this.transferData = this.arr;
      this.loading = false;
    },
    //输入框搜索方法
    filterMethod() {
      this.isLoading = false;
      this.page = 1;
      this.arr = [];
      this.transferData = [];
      this.getList();
    },
    //输入框清空事件
    clearMethod() {
      this.keyword = "";
    },
    //下拉加载分页方法
    lazyLoading(res) {
      //滚动时判断是否触碰到底部
      this.bottomOfWindow =
        res.target.scrollTop + res.target.clientHeight >=
        res.target.scrollHeight;

      if (this.bottomOfWindow && this.isLoading == false) {
        this.isLoading = true;
        this.page += 1;
        this.getList();
      }
    },
    //清空选中数据
    clearRight() {
      this.zcList = []; //清空暂存数据
      this.selectedList = []; //清空选中数组
       // 再判断是否根据查询条件重新查询左边规格框
       if(this.keyword.toString.length>0){
        this.filterMethod()
      }
    },
    //打开添加新属性弹窗
    async openChild() {
      const res = await getSkuList({
        api: "saas.dic.getSkuAttributeList",
        pageSize: 100,
        lang_code:this.lang_code
      });
      this.options = res.data.data.list;
      this.childDialogVisible = true;
    },
    //切换radio,切换清空数据
    clearBute() {
      this.ruleForm.name = "";
      this.ruleForm.buteName = [];
      this.buteList = [];
    },
    //添加属性
    addBute() {
      //添加属性名称
      if (this.ruleForm.radio == 1) {
        if (this.ruleForm.name == "") {
          this.errorMsg(this.$t("transferDialog.qsrsxmc"));
          return;
        }
        if (
          /[\x21-\x2F\x3A-\x40\x5B-\x60\x7B-\x7E]+/g.test(this.ruleForm.name)
        ) {
          this.errorMsg(this.$t("transferDialog.sxbnbhtszf"));
          return;
        }
        let rule = this.buteList.find((row) => {
          return row.label == this.ruleForm.name;
        });
        if (rule) {
          this.errorMsg(this.$t("transferDialog.sxmcycz"));
          return;
        }
        getSkuList({
          api: "saas.dic.getSkuInfo",
          key: this.ruleForm.name,
          pageSize: 100,
        }).then((res) => {
          if (res.data.code == 200) {
            let arrList = {
              head: this.ruleForm.name + "：",
              label: this.ruleForm.name,
              valueList: [],
            };
            this.buteList.push(arrList);
            this.ruleForm.name = "";
          }
        });
      } else {
        //添加属性值
        this.buteList = this.ruleForm.buteName.map((item) => ({
          head: item.name + "：",
          label: item.name,
          id: item.id,
          valueList: [],
        }));
      }
    },
    //删除属性
    delBute(index) {
      this.buteList.splice(index, 1);
      if (this.ruleForm.radio == 2) {
        this.ruleForm.buteName.splice(index, 1);
      }
    },
    //添加属性值
    addValue(item, index) {
      if (!item.name || item.name == "") {
        this.errorMsg(this.$t("transferDialog.qsrsxz"));
        return;
      }
      if (/[\x21-\x2F\x3A-\x40\x5B-\x60\x7B-\x7E]+/g.test(item.name)) {
        this.errorMsg(this.$t("transferDialog.sxbnbhtszf"));
        return;
      }
      let rule = this.buteList[index].valueList.find((row) => {
        return row.sname == item.name;
      });
      if (rule) {
        this.errorMsg(this.$t("transferDialog.sxzycz"));
        return;
      }
      let arrList = {
        sname: item.name,
      };
      this.buteList[index].valueList.push(arrList);
      this.buteList[index].name = "";
    },
    //删除属性值
    delValue(index, ind) {
      this.buteList[index].valueList.splice(ind, 1);
    },
    //关闭添加属性子弹窗
    childClose() {
      this.ruleForm.radio = 1;
      this.childDialogVisible = false;
      this.clearBute();
    },
    //穿梭框左右切换的change事件
    transferChange(row, item) {
      this.zcList = []; //初始化zcList
      row.forEach((itn) => {
        var obj = this.transferData.find((item, index) => {
          return item.sid == itn; //取出this.transferData里面符合条件的数据
        });
        this.zcList.push(obj);
      });
      //去重，防止sid重复
      this.zcList = this.zcList.reduce((accumulator, current) => {
        // 非空判断
        if(!current || !current.sid){
          return accumulator
        }
        const existingItem = accumulator.find(
          (item) => item.sid == current.sid
        );
        if (!existingItem) {
          return accumulator.concat([current]);
        } else {
          return accumulator;
        }
      }, []);
    },
    //生成sid的随机数
    genNumber() {
      const min = 1000;
      const max = 9999;
      const randomNumber = Math.floor(Math.random() * (max - min + 1)) + min;
      return randomNumber;
    },
    //子弹窗关闭方法
    submitChild() {
      if (this.buteList.length == 0) {
        this.errorMsg(this.$t("transferDialog.qsrsxjsxz"));
        return;
      }
      let rule = this.buteList.map((item) => item.valueList.length);
      if (rule.includes(0)) {
        this.errorMsg(this.$t("transferDialog.qsrsxz"));
        return;
      }
      let result = this.buteList.reduce((result, item) => {
        item.valueList.forEach((valueItem) => {
          let sid = this.genNumber();
          result.push({
            sid: Number(sid),
            label: item.label + ":" + valueItem.sname,
            name: item.label,
            sname: valueItem.sname,
            status: false,
          });
        });
        return result;
      }, []);
      this.transferData.push(...result);
      this.zcList.push(...result);
      result.forEach((item) => {
        this.selectedList.push(item.sid);
      });
      this.childClose();
    },
    //弹窗最终提交方法
    submit() {
      // 处理确定事件：发送选中的数据
      let arrList = [];
      this.selectedList.forEach((itn) => {
        var obj = this.transferData.find((item, index) => {
          return item.sid == itn; //取出this.transferData里面符合条件的数据
        });
        arrList.push(obj);
      });
      //reduce方法用于累计结果，find方法用于检查是否已经存在具有相同name的组，如果存在则添加到该组，不存在则创建新组
      const groupedData = arrList.reduce((acc, current) => {
        const existingGroup = acc.find(
          (item) => item.attr_group_name === current.name
        );
        if (existingGroup) {
          existingGroup.attr_list.push({
            attr_name: current.sname,
            status: current.status ?? false,
          });
        } else {
          acc.push({
            attr_group_name: current.name,
            attr_list: [
              {
                attr_name: current.sname,
                status: current.status ?? false,
              },
            ],
          });
        }
        return acc;
      }, []);

      console.log("groupedData", groupedData);
      console.log("zcList", this.zcList);
      // 规格去重
      const dataSource1 = Array.from(
              new Map(this.zcList.map((item) => [item.sid, item])).values()
            );

      this.$emit("submit", groupedData,dataSource1);
      this.dialogVisible = false;
    },
  },
};
</script>

<style scoped lang="less">
//穿梭框样式
::v-deep.Transfer {
  .el-dialog__wrapper {
    display: flex;
    justify-content: center;
    align-items: center;
  }
  .el-dialog {
    // width: 640px;
    width: 724px;
    border-radius: 4px;
    margin-top: 0 !important;
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
    }
    .el-dialog__body {
      display: flex;
      justify-content: center;
      // align-items: center;
      flex-direction: column;
      padding-right: 3px;
      position: relative;
      .filter {
        width: 266px !important;
        margin-bottom: 10px;
        .el-input__clear {
          line-height: 0 !important;
        }
        input {
          width: 266px !important;
        }
      }
      .el-transfer {
        width: 100%;
      }
      .el-transfer-panel {
        width: 38%;
        height: 350px;
      }
      .el-transfer-panel__list {
        height: 300px;
        padding-bottom: 40px;
      }
      .left-footer {
        display: flex;
        align-items: center;
        justify-content: center;
        border-top: none;
        font-size: 12px;
        height: 20px;
        position: absolute;
        bottom: 30px;
        left: 130px;
      }
      .el-transfer__buttons {
        padding: 0 15px !important;
      }
      .transfer-footer {
        margin-left: 10px;
      }
      .el-input {
        width: 235px;
        height: 32px;
        input {
          width: 235px;
          height: 32px;
        }
      }
      .el-pagination {
        height: 100%;
        display: flex;
        align-items: center;
        justify-content: flex-end;
      }
    }
    .el-dialog__footer {
      padding-top: 10px;
      padding-bottom: 10px;
      border-top: 1px solid #e9ecef;
      .cancel:hover {
        color: #2890ff !important;
        background-color: #ffffff !important;
        border: 1px solid #2890ff !important;
      }
    }
  }
}
//子弹窗样式
::v-deep.child {
  display: flex;
  justify-content: center;
  align-items: center;
  .el-dialog {
    width: 680px;
    border-radius: 4px;
    margin-top: 0 !important;
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
    }
    .el-dialog__body {
      display: flex;
      // justify-content: center;
      align-items: center;
      .ruleForm {
        width: 100%;
      }
      .el-form-item {
        margin-bottom: 0 !important;
      }
      .el-form-item__label {
        white-space: nowrap;
        padding-right: 0;
      }
      // .el-input {
      //   width: 310px;
      //   height: 40px;
      //   input {
      //     width: 310px;
      //     height: 40px;
      //   }
      // }
    }
    .box_center {
      display: flex;
      // justify-content: center;
      align-items: center;
      .buteInput {
        width: 310px;
        height: 40px;
        input {
          width: 310px;
          height: 40px;
        }
      }
      .buteSelect {
        width: 240px;
        input {
          width: 240px;
        }
        .el-tag__close.el-icon-close::before {
          color: #fff;
        }
      }
    }
    .add_bt {
      width: 88px;
      height: 38px;
      display: flex;
      justify-content: center;
      align-items: center;
      margin-left: 10px;
      color: #2890ff !important;
      background-color: #ffffff !important;
      border: 1px solid #2890ff !important;
    }
    .add_bt:hover {
      color: #2890ff !important;
      background-color: #ffffff !important;
      border: 1px solid #2890ff !important;
    }
    .delete_bt {
      width: 88px;
      height: 38px;
      display: flex;
      justify-content: center;
      align-items: center;
      margin-left: 10px;
      color: #828b97 !important;
      background-color: #ffffff !important;
      border: 1px solid #828b97 !important;
    }
    .delete_bt:hover {
      color: #828b97 !important;
      background-color: #ffffff !important;
      border: 1px solid #828b97 !important;
    }
    .el-dialog__footer {
      padding-top: 10px;
      padding-bottom: 10px;
      border-top: 1px solid #e9ecef;
      .cancel:hover {
        color: #2890ff !important;
        background-color: #ffffff !important;
        border: 1px solid #2890ff !important;
      }
    }
  }
  .box_add {
    background: #f4f7f9;
    overflow-x: hidden;
    overflow-y: auto;
    height: 252px;
    padding: 10px;
    width: 100%;
    box-sizing: border-box;
    .el-form-item {
      padding-bottom: 10px;
    }
    .add_input {
      width: 212px !important;
      height: 40px !important;
      input {
        width: 212px !important;
        height: 40px !important;
      }
    }
    .attrValue {
      display: flex;
      align-items: center;
      flex-wrap: wrap;
      margin-top: 10px;
      span {
        display: flex;
        align-items: center;
        height: 2.25rem;
        padding: 0 0.625rem;
        background: #fff;
        color: #414658;
        border: 0.0625rem dashed #b2bcd1;
        margin-bottom: 0.625rem;
        margin-right: 0.625rem;
      }
      img {
        width: 12px;
        height: 12px;
        margin-left: 10px;
        cursor: pointer;
      }
    }
  }
}
</style>
