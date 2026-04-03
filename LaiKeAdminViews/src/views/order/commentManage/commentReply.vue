<template>
  <div class="container">
    <div class="Search">
      

      <div class="Search-condition">
        <div class="query-input">
          <el-input
            size="medium"
            v-model="dataForm.orderNo"
            @keyup.enter.native="demand"
            class="Search-input"
            :placeholder="$t('commentReply.qsryh')"
          ></el-input>
        </div>
        <div class="select-date">
          <el-date-picker
            v-model="dataForm.choseDate"
            type="datetimerange"
            :range-separator="$t('reportManagement.businessReport.zhi')"
            :start-placeholder="$t('reportManagement.businessReport.ksrq')"
            :end-placeholder="$t('reportManagement.businessReport.jsrq')"
            value-format="yyyy-MM-dd HH:mm:ss"
            :editable="false"
          >
          </el-date-picker>
        </div>

        <div class="btn-list">
          <el-button class="fontColor" @click="reset">{{
            $t("DemoPage.tableExamplePage.reset")
          }}</el-button>
          <el-button class="bgColor" type="primary" @click="demand"
            >{{ $t("DemoPage.tableExamplePage.demand") }}
          </el-button>
        </div>
      </div>
    </div>
    <div class="merchants-list" ref="tableFather">
      <el-table
        :element-loading-text="$t('DemoPage.tableExamplePage.loading_text')"
        v-loading="page.loading"
        :data="page.tableData"
        ref="table"
        class="el-table"
        style="width: 100%"
        :height="tableHeight"
      >
      <template slot="empty">
          <div class="empty">
              <img src="../../../assets/imgs/empty.png" alt="">
              <p style="color: #414658 ;">{{$t('zdata.zwsj') }}</p>
          </div>
      </template>
        <el-table-column :label="$t('commentReply.xh')" width="88">
          <template slot-scope="scope">
            {{ scope.$index + page.currpage }}
          </template>
        </el-table-column>
        <el-table-column prop="replyUserId" :label="$t('commentReply.yhid')"> </el-table-column>

        <el-table-column prop="replyName" :label="$t('commentReply.yhmc')"> </el-table-column>


       
        <el-table-column prop="content" :label="$t('commentReply.hfnr')" width="328">
        </el-table-column>
        <el-table-column :label="$t('commentReply.hfsj')">
          <template slot-scope="scope">
            {{ scope.row.add_time | dateFormat }}
          </template>
        </el-table-column>
        
        <el-table-column fixed="right" :label="$t('commentReply.cz')" width="100">
          <template slot-scope="scope">
            <div class="OP-button">
              <div class="OP-button-top">
                
                <el-button
                  v-if="detectionBtn(button_list, '删除评论')"
                  icon="el-icon-delete"
                  @click="Delete(scope.row.id)"
                  >{{$t('commentReply.shanchu')}}</el-button
                >
              </div>
              
            </div>
          </template>
        </el-table-column>
      </el-table>
      <div class="pageBox" ref="pageBox" v-if="page.showPagebox">
        <div class="pageLeftText">{{$t('DemoPage.tableExamplePage.show')}}</div>
        <el-pagination
          layout="sizes, slot, prev, pager, next"
          :prev-text="$t('DemoPage.tableExamplePage.prev_text')"
          :next-text="$t('DemoPage.tableExamplePage.next_text')"
          @size-change="handleSizeChange"
          :page-sizes="pagesizes"
          :current-page="pagination.page"
          @current-change="handleCurrentChange"
          :total="total"
        >
          <div class="pageRightText">{{$t('DemoPage.tableExamplePage.on_show')}}{{currpage}}-{{current_num}}{{$t('DemoPage.tableExamplePage.twig')}}{{total}}{{ $t('DemoPage.tableExamplePage.twig_notes') }}</div>
        </el-pagination>
      </div>
    </div>

  </div>
  

</template>

<script>
import pageData from "@/api/constant/page";
import { del, save, index } from "@/api/order/comment";
import { isEmpty } from "element-ui/src/utils/util";
import { mixinstest } from '@/mixins/index'
import { getStorage } from '@/utils/storage'
import { getButton } from '@/api/layout/information'

export default {
  name: 'list',
  mixins: [mixinstest],
  //初始化数据
  data() {
    return {
      page: pageData.data(),
      dialogVisible: false,

      dataForm: {
        orderNo: null,
        choseDate: [],
      },

      reid:'',
      // table高度
      tableHeight: null,
      button_list: [],
      dialogVisible:false,
      
    }
  },
  //组装模板
  created() {
    if (this.$route.params.pageSize) {
      this.pagination.page = this.$route.params.dictionaryNum
      this.dictionaryNum = this.$route.params.dictionaryNum
      this.pageSize = this.$route.params.pageSize
    }
    this.loadData()
  },

  mounted() {
    this.$nextTick(function () {
      this.getHeight()
    })
    window.addEventListener('resize', this.getHeight(), false)
  },

  methods: {

    // 获取table高度
    getHeight() {
      this.tableHeight = this.$refs.tableFather.clientHeight - this.$refs.pageBox.clientHeight
      
    },
    async loadData() {
      await this.getList().then()
      await this.getButtons()
    },
    //获取按纽权限
    async getButtons() {
      let route = getStorage('route')
      route.forEach(item => {
        if (item.path == 'commentManage') {
          return this.menuId = item.id
        }
      });
      let buttonList = await getButton({
        api: 'saas.role.getButton',
        menuId: this.menuId,
      })
      this.button_list = buttonList.data.data
      this.button_list = buttonList.data.data.map(item => {
        return item.title
      })
    },
    // 获取列表
    async getList() {
      const res = await index({
        api: 'admin.order.getCommentsDetailInfoById',
        pageNo: this.dictionaryNum,
        pageSize: this.pageSize,
        // orderType: 'GM',
        cid:this.$route.params.id,
        key: this.dataForm.orderNo,
        startDate: isEmpty(this.dataForm.choseDate) ? null : this.dataForm.choseDate[0],
        endDate: isEmpty(this.dataForm.choseDate) ? null : this.dataForm.choseDate[1],
      });
      this.total = res.data.data.total
      this.page.tableData = res.data.data.list
      this.page.loading = false
      if (res.data.data.total < 10) {
        this.current_num = this.total
      }
      if (this.total < this.current_num) {
        this.current_num = this.total
      }
    },

    // 重置
    reset() {

      this.dataForm.orderNo = null
      this.dataForm.choseDate = []
    },


    // 查询
    demand() {
      this.currpage = 1
      this.current_num = 10
      this.showPagebox = false
      this.page.loading = true
      this.dictionaryNum = 1
      this.loadData().then(() => {
        this.page.loading = false
        if (this.page.tableData.length > 5) {
          this.showPagebox = true
        }
      })
    },

    //选择一页多少条
    handleSizeChange(e) {
      this.page.loading = true
      // this.page.current_num = e
      this.pageSize = e
      this.loadData().then(() => {
        this.currpage = ((this.dictionaryNum - 1) * this.pageSize) + 1
        this.current_num = this.page.tableData.length === this.pageSize ? this.dictionaryNum * this.pageSize : this.total
        this.page.loading = false
      })
    },

    //点击上一页，下一页
    handleCurrentChange(e) {
      this.page.loading = true
      this.dictionaryNum = e
      this.currpage = ((e - 1) * this.pageSize) + 1
      this.loadData().then(() => {
        this.current_num = this.page.tableData.length === this.pageSize ? e * this.pageSize : this.total
        this.page.loading = false
      })

    },


    // 删除
    Delete(id) {
      this.$confirm(this.$t('commentReply.scts'), this.$t('commentReply.ts'), {
        confirmButtonText: this.$t('commentReply.okk'),
        cancelButtonText: this.$t('commentReply.ccel'),
        type: 'warning'
      }).then(() => {
        del({
          api: 'admin.order.delCommentReply',
          id: id
        }).then(res => {
          this.demand();
          this.$message({
            type: 'success',
            message: this.$t('commentReply.cg')
          })
        })
      }).catch(() => {
        // this.$message({
        //   type: 'info',
        //   message: '已取消删除'
        // })
      })
    }

  }

}

</script>


<style scoped lang="less">
.query-input{
  margin-right: 0!important;
}
@import "../../../common/commonStyle/form";
@import "../../../webManage/css/order/commentManage/commentList";
</style>
