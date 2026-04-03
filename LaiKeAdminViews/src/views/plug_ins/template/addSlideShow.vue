<template>
  <div class="container">
    <!-- <div class="header">
        <span>添加轮播图</span>
    </div> -->
    <el-form :model="ruleForm" label-position="right" :rules="rules" ref="ruleForm" label-width="135px" class="form-search">
      <div class="notice">
        <el-form-item :label="$t('template.addSlideShow.tp')" prop="img">
          <l-upload
            :limit="1"
            v-model="ruleForm.img"
            :text="$t('template.addSlideShow.jy750')"
          >
          </l-upload>
        </el-form-item>
        <el-form-item :label="$t('template.addSlideShow.lj')">

          <el-select class="select-input type-stlect" v-model="ruleForm.class1" placeholder="">
            <el-option v-for="(item,index) in classList1" :key="index" :label="item.label" :value="item.value">
            </el-option>
          </el-select>

          <el-input v-if="ruleForm.class1 === 0" v-model="ruleForm.url"></el-input>
          <div v-else-if="ruleForm.class1 === 2" class="" style="display: inline-block;">
              <el-button class="new_xzsp" plain type="primary" @click="AddPro">{{$t('template.addSlideShow.xzsp')}}</el-button>

          </div>
          <el-select class="select-input selects" filterable v-else v-model="ruleForm.url" :placeholder="$t('template.addSlideShow.qsrgj')">
            <el-option v-for="(item,index) in classList2" :key="index" :label="item.name" :value="item.parameter">
            </el-option>
          </el-select>
          <div class="myurl" v-if="ruleForm.class1 === 2&&prochangedata" @click="delpro()">
            <img class="myurl_img" :src="prochangedata.imgurl" alt="">
            <div class="myurl_right">
              <p>{{prochangedata.product_title}}</p>
              <p>{{$t('template.addSlideShow.sj')}}：{{prochangedata.price}}</p>
              <p>{{$t('template.addSlideShow.kc')}}：{{prochangedata.num}}</p>
            </div>
            <div>
              <i class="el-icon-error" style="color:#b4b4b4"></i>
            </div>
          </div>
        </el-form-item>

        <div class="footer-button">
          <el-button type="primary" class="footer-save bgColor mgleft" @click="submitForm('ruleForm')">{{$t('template.addSlideShow.save')}}</el-button>
          <el-button plain class="footer-cancel fontColor" @click="$router.go(-1)">{{$t('template.ccel')}}</el-button>
        </div>
      </div>
    </el-form>
    <div class="dialog-block">
      <!-- 弹框组件 -->

      <el-dialog :title="$t('template.addSlideShow.xzsp')" :visible.sync="dialogVisible2" width="920px">
          <div class="">
              <div class="Search">
                  <el-input class="Search-input" v-model="search" :placeholder="$t('template.addSlideShow.qsrsp')"></el-input>
                  <el-button @click="Reset" plain>{{$t('DemoPage.tableExamplePage.reset')}}</el-button>
                  <el-button @click="query" type="primary" v-enter="query">{{$t('DemoPage.tableExamplePage.demand')}}</el-button>
              </div>
              <div class="Table">
                  <el-table height="350" :data="prodata"  style="width: 100%" @current-change="handleSelectionChange" ref="multipleTable">
                      <el-table-column :label="$t('template.addSlideShow.xz')" align="center" width="55">
                          <template slot-scope="scope">
                              <el-radio :label="scope.row" v-model="prochangedata" ><i></i></el-radio>
                          </template>
                      </el-table-column>
                      <!-- <el-table-column :reserve-selection="true" align="center" type="selection" width="55"></el-table-column> -->
                      <el-table-column prop="ProName" align="center" :label="$t('template.addSlideShow.spmc')">
                          <template slot-scope="scope">
                              <div class="Info">
                                  <img :src="scope.row.imgurl" width="40" height="40" alt="">
                                  <span>{{scope.row.product_title}}</span>
                              </div>
                          </template>
                      </el-table-column>
                      <el-table-column prop="p_name" align="center" :label="$t('template.addSlideShow.fl')">
                      </el-table-column>
                      <el-table-column prop="brand_name" align="center" :label="$t('template.addSlideShow.pp')">
                      </el-table-column>
                      <el-table-column prop="price" align="center" :label="$t('template.addSlideShow.sj')">
                      </el-table-column>
                      <el-table-column prop="num" align="center" :label="$t('template.addSlideShow.kc')">
                      </el-table-column>
                  </el-table>
                  <div class="pageBox">
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
          <span slot="footer" class="dialog-footer">
              <el-button @click="dialogVisible2 = false">{{$t('template.ccel')}}</el-button>
              <el-button type="primary" @click="confirm">{{$t('template.okk')}}</el-button>
          </span>
      </el-dialog>
    </div>
  </div>
</template>

<script>
import addSlideShow from '@/webManage/js/plug_ins/template/addSlideShow'
export default addSlideShow
</script>

<style scoped lang="less">
@import '../../../webManage/css/plug_ins/template/addSlideShow.less';
/deep/.new_xzsp{
  color: #2890ff;
  background: initial;
  border-color: #2890ff;
}
/deep/.el-dialog__body{
  border-top: 1px solid #E9ECEF;
  border-bottom: 1px solid #E9ECEF;
}
.Info{
  display: flex;
  align-items: center;
  >img{
    margin-right: 10px;
  }
  >span{
    text-align: left;
  }
}
.myurl{
  width: 323px;
  height: 108px;
  border-radius: 4px 4px 4px 4px;
  border: 1px solid #D5DBE8;
  display: flex;
  padding: 8px;
  line-height: 25px;
  margin-top: 20px;
  justify-content: space-between;
  .myurl_img{
    width: 88px;
    height: 88px;
  }
  .myurl_right{
    width: 100%;
    padding: 0 5px;
  }
}
</style>
