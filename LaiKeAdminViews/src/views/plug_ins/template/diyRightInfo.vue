<template>
  <div class="container">
    <div class="title-box" v-if="statusType !='pageDetail'">
      <div class="title-text">
        {{createdFrom.name || ''}}
      </div>
    </div>
    <div class="tab-box">
      <!-- diy 主题使用 -->
      <template v-if="statusType !='pageDetail'">

        <el-tabs type="card" v-model="activeName">
          <el-tab-pane label="通用设置" name="generalSett">
            <div class="general-settings-box">
              <div>
                <div>
                  <span>主题信息</span>
                  <div @click="queryInfoFalg">
                    <i class="el-icon-arrow-up" v-if="queryFalg"></i>
                    <i class="el-icon-arrow-down" v-else></i>
                  </div>
                </div>
                <div class="title-box" v-if="queryFalg" style="margin: 20px 0px;padding: 0px;">
                  <div class="diy-introduction">
                    <div>
                      <div>
                        <span>主题名称</span>
                      </div>
                      <div style="display: flex;align-items: center;height: 30px;width: 77%;">
                        <div class="diy-textinfo" style="width: 100%;">{{createdFrom.name}} </div>
                        <i v-if="typeIndex ==2" @click="editDiyinfo" class="el-icon-edit" style="color:#2890FF"></i>
                      </div>
                    </div>

                    <div v-if="typeIndex ==2">
                      <div>
                        <span>主题类型</span>
                      </div>
                      <div style="display: flex;align-items: center;height: 30px">
                        <div class="diy-textinfo">{{createdFrom.theme_type_name}} </div>
                      </div>
                    </div>

                    <div>
                      <div style="margin-bottom: auto;line-height: 23px;">
                        <span>主题描述</span>
                      </div>
                      <div class="diy-textinfo">
                        {{createdFrom.remark}}
                      </div>
                    </div>

                  </div>
                </div>
              </div>
              <div>
                <div>
                  <p>
                    <span>导航 </span>
                    <span style="color: #999;font-size: 12px;">（{{tabBarList.length}}/4）</span>
                  </p>
                  <div @click="queryTabbarModInfoFalg">
                    <i class="el-icon-arrow-down" v-if="!tabbarModFalg"></i>
                    <i class="el-icon-arrow-up" v-else></i>
                  </div>
                </div>

                <div v-show="tabbarModFalg">
                  <div class="tabber-box">
                    <div class="tabber-item">
                      <draggable @update="handleDragEnd" ref="tabbarBox">
                        <div v-for="(item,index) in tabBarList" :key="index" class="item" :data-pageKey='item.key'>
                          <div>
                            <img src="../../../assets/imgs/sixdian.png" alt="">
                          </div>
                          <div>
                            <l-upload :limit="1" v-model="item.selectedIconPath" text="" @input="selectedIconImg">
                            </l-upload>
                          </div>
                          <div>
                            <l-upload :limit="1" v-model="item.iconPath" text=" " @input="selectIconPathImg">
                            </l-upload>
                          </div>
                          <div class="name-size">
                            <div>
                              <span>名称</span>
                              <el-input v-model="item.page_name" @input="maxValuelengt(index)" />
                            </div>
                          </div>
                          <div style="margin-top: 10px;margin-left: 15px;">
                            <i class="el-icon-delete" @click='delTabber(index)' v-if="item.isDel"></i>
                          </div>
                        </div>
                      </draggable>
                      <div class="add-tabbar" @click="addTabBerItem" v-if="tabBarList.length < 4">
                        <span>+ </span>
                        <span style="margin-left: 6px;"> 添加</span>
                      </div>
                    </div>
                  </div>
                  <div>
                    <div class="tabbar-config">
                      <el-form v-model="tabberInfo" label-width="100px" label-position="left">
                        <el-form-item label="显示方式">
                          <div class="tabber-show-type">
                            <div @click="textAndIconIsShow">
                              <div :class="{ 'select-item': tabberInfo.tabbarShow }" data-type="none">不显示</div>
                              <div :class="{ 'select-item': tabberInfo.fontIsShow && !tabberInfo.iconIsShow }" data-type="font">仅文字</div>
                              <div :class="{ 'select-item': !tabberInfo.fontIsShow && tabberInfo.iconIsShow }" data-type="icon">仅图标</div>
                              <div :class="{ 'select-item': tabberInfo.fontIsShow && tabberInfo.iconIsShow }" data-type="fontAndIcon">文字/图标</div>
                            </div>
                          </div>
                        </el-form-item>
                        <el-form-item label="大小" class="font-type-size">
                          <div class="tabber-show-type">
                            <div style="margin-right: 5px;">
                              <div class="title">
                                图标
                              </div>
                              <el-select class="size" size="small" :disabled='!tabberInfo.iconIsShow' v-model="tabberInfo.ficonSize" :placeholder="$t('template.divTemplate.qxz')" @change="upTBarIcon">
                                <el-option label="24 * 24" value="small"></el-option>
                                <el-option label="32 * 32" value="medium"></el-option>
                                <el-option label="40 * 40" value="big"></el-option>
                              </el-select>
                            </div>
                            <div>
                              <div class="title">
                                文字
                              </div>
                              <el-select class="size" v-model="tabberInfo.fontSize" :disabled='!tabberInfo.fontIsShow' :placeholder="$t('template.divTemplate.qxz')" size="small">
                                <el-option :label="item.value" :value="item.value" :disabled="item.disabled" v-for="(item,index) in fontSizeList" :key="index"></el-option>
                              </el-select>
                            </div>
                          </div>
                        </el-form-item>
                        <el-form-item label="初始文字颜色">
                          <div class="spacing color-box ivu-col ivu-col-span-14">
                            <div class="color-item">
                              <el-color-picker format="hex" size="mini" v-model="tabberInfo.color" alpha class="colorPicker-box" style="margin: 0 2px;"></el-color-picker>
                              <span @click="resetBgA(tabberInfo,index,key)">重置</span>
                            </div>
                          </div>
                        </el-form-item>

                        <el-form-item label="选中文字颜色" class="opt-color">
                          <div class="spacing color-box ivu-col ivu-col-span-14" style="display: flex;justify-content: end;">
                            <div class="color-item">
                              <el-color-picker format="hex" size="mini" v-model="tabberInfo.optColor" alpha class="colorPicker-box" style="margin: 0 2px;"></el-color-picker>
                              <span @click="tabberInfo.optColor = 'rgba(11,11,11,1)'">重置</span>
                            </div>
                          </div>
                        </el-form-item>

                        <el-form-item label="行间距">
                          <Slider v-model="tabberInfo.lingHehig" show-input :max='20' :min="12"></Slider>
                        </el-form-item>
                        <el-form-item label="间距">
                          <div class="spacing">
                            <l-instruction class="locator" @orientation="orientation" />
                            <Slider v-model="marginPointTo" show-input :max='10' :min="0">
                            </Slider>
                          </div>
                        </el-form-item>

                      </el-form>
                    </div>
                  </div>
                </div>
              </div>

              <div>
                <div>
                  <span>系统设置</span>
                  <div @click="querySystemSettingsFalg">
                    <i class="el-icon-arrow-down" v-if="!systemSettingsFalg"></i>
                    <i class="el-icon-arrow-up" v-else></i>
                  </div>
                </div>
                <div class="tabbar-config system-config" v-show="systemSettingsFalg">

                  <div class="c_row-item">
                    <div class="c_label ivu-col ivu-col-span-8">
                      文字颜色
                    </div>
                    <div class="spacing color-box ivu-col ivu-col-span-14">
                      <div class="color-item">
                        <ColorPicker v-model="tabberInfo.textColor" @on-change="changeTextColor($event)" alpha></ColorPicker>
                        <span @click="resetBgB(tabberInfo,index,key)">重置</span>
                      </div>
                    </div>
                  </div>
                  <div style="display: flex;justify-content: space-between;">
                    <span>背景设置</span>
                    <div>
                      <div class="but-list color-list" @click="UpBackType">
                        <p data-type="0" :class="{ 'avt':tabberInfo.backType == 0 }">颜色</p>
                        <p data-type="1" :class="{ 'avt':tabberInfo.backType == 1 }">图片</p>
                      </div>
                      <div style="display: flex;margin-top: 10px;">
                        <div class="color-item-title" v-if="tabberInfo.backType  == 0">
                          <el-color-picker format="hex" size="mini" v-model="tabberInfo.colorTwo" @on-change="changeColor($event)" alpha class="colorPicker-box" style="margin: 0 2px;"></el-color-picker>
                          <span @click="resetBgC(tabberInfo,index,key)">重置</span>
                        </div>
                        <div class="color-item-title" v-else>
                          <l-upload :limit="1" v-model="tabberInfo.imgurl" text="">
                          </l-upload>
                        </div>
                      </div>
                    </div>
                  </div>

                </div>
              </div>
            </div>
          </el-tab-pane>
          <el-tab-pane label="页面列表" name="pageList">
            <div class="page-list-box" :style="{'overflow-y':activeName== 'generalSett'?'auto':'hidden'}">
              <div>
                <div>
                  <span>页面</span>
                  <i class="el-icon-plus" @click='addPagesItem'></i>
                </div>
              </div>
              <div>
                <div class="search-pagsname">
                  <el-input placeholder="请输入内容" v-model="input1" round @input="quyerPageItembyName">
                    <i slot="prefix" class="el-input__icon el-icon-search"></i>
                  </el-input>
                  <popupMenu @queryType="queryType" />
                </div>
              </div>
              <div>
                <div class="page-list" v-if="pageList1.length > 0">
                  <div class="page-list-item item" v-for="(item,index) in pageList1" :key='index' ref="pageListBox" @mouseenter="preSelection = index" @mouseleave="preSelection=''" @click.stop="queryPage(item)" :class="{ 'list-item-query':preSelection === index, 'list-item-active':querpageItem == item.key }">
                    <div class="page-item-info">
                      <div>
                        <p>
                          <span>{{item.page_name}}</span>
                        </p>
                        <p>
                          {{item.type ?'系统页面':'自定义页面'}}
                        </p>
                      </div>
                      <div class="page-item-btn">
                        <el-tooltip :content="$t('template.divTemplate.gllb')" effect="dark" ref="myTooltip" popper-class="but-name" placement="bottom">
                          <!-- 关联 -->
                          <el-button v-if="!item.type" @click.stop.prevent="relevance(item,index)" class="link">
                            <img v-if="relevanceIndex == index" src="../../../assets/images/diy/guanl-cur.png" class="page-but" />
                            <img v-else src="../../../assets/images/diy/guanl.png" class="page-but" />
                          </el-button>
                        </el-tooltip>
                        <el-tooltip :content="$t('template.divTemplate.fzlj')" effect="dark" popper-class="but-name" placement="bottom">
                          <!-- 复制链接 -->
                          <el-button v-if="!item.type" v-copy.stop.prevent="item.link" class="link">
                            <img v-if="relevanceHover == 2" src="../../../assets/images/diy/codelj-cur.png" class="page-but" />
                            <img v-else src="../../../assets/images/diy/code-lj.png" class="page-but" />
                          </el-button>
                        </el-tooltip>
                        <el-tooltip :content="$t('template.divTemplate.scym')" effect="dark" popper-class="but-name" placement="bottom">
                          <!-- 删除 -->
                          <el-button v-if="!item.type" @click.stop.prevent="delPageItem(item)" class="link">
                            <img v-if="relevanceHover == 2" src="../../../assets/images/diy/del-cur.png" class="page-but" />
                            <img v-else src="../../../assets/images/diy/del.png" class="page-but" />
                          </el-button>
                        </el-tooltip>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

            </div>
          </el-tab-pane>
        </el-tabs>
        <div class="footer">
          <div>
            <el-button @click="toUp">{{ $t('template.ccel')}}</el-button>
            <!-- 另存为 或是 保存  -->
            <el-button type="primary" @click='SubmitEvent'>{{ (this.typeIndex == 1 && !isLkt) ? $t('diy.lcw') : $t('material.bc')}} </el-button>
          </div>
        </div>
      </template>
      <!-- 自定义页面编辑使用 -->
      <template v-else>
        <div class="general-settings-box1">
          <div>
            <div class="title-box" style="display: flex;align-items: center;justify-content: space-between;">
              <div class="title-text">
                {{createdFrom.name || ''}}
              </div>
              <div @click="queryInfoFalg(1)">
                <i class="el-icon-arrow-up" v-if="queryFalg"></i>
                <i class="el-icon-arrow-down" v-else></i>
              </div>
            </div>

            <div class="diy-introduction" v-if="queryFalg">
              <div>页面名称 <span class="link"> {{ createdFrom.name }}</span></div>
              <div style="display: flex;">页面链接 <span class="link">{{ createdFrom.link }}</span> <span v-copy.stop.prevent="createdFrom.link" class="fz">复制</span></div>
            </div>
            <div style="border-bottom: 1px solid #E9ECEF;padding-bottom: 20px;"></div>
          </div>
          <div>
            <div class="diy-page-title" style="border: none;">
              <h2>关联主题列表<span>(共{{ bindDiyListTwo.length }})</span></h2>
            </div>
            <div>
              <div class="search-pagsname">
                <el-input placeholder="请输入关联主题名称" v-model="diyName" round @input="quyerDiyItembyName">
                  <i slot="prefix" class="el-input__icon el-icon-search"></i>
                </el-input>
              </div>
              <ul class="page-box">
                <li v-for="(item,index) in bindDiyListTwo" :key="index">
                  <div class="page-item">
                    <img :src="item.cover">
                    <span>{{ item.name }}</span>
                  </div>
                </li>
              </ul>
            </div>
            <div class="footer">

              <div>
                <el-button @click="toUp">{{ $t('template.ccel')}}</el-button>
                <el-button type="primary" @click='PageSubmitEvent'>{{ $t('material.bc')}} </el-button>
              </div>
            </div>
          </div>
        </div>

      </template>

    </div>

    <!-- 编辑主题信息 -->
    <el-dialog :title="$t('template.divTemplate.bjztxx')" :visible.sync="dialogFormVisible" width="580px" class="dit">
      <div class="dit-box">
        <el-form :model="createdFromTwo" :rules="rules" ref="createdFromRef" label-width="80px">
          <el-form-item :label="$t('template.divTemplate.ztmc')" prop="name">
            <el-input v-model="createdFromTwo.name" autocomplete="off" :placeholder="$t('template.divTemplate.qsr')" style="width: 360px;"></el-input>
          </el-form-item>
          <el-form-item :label="$t('template.divTemplate.ztlb')" prop="theme_type_code">
            <el-select v-model="createdFromTwo.theme_type_code" :placeholder="$t('template.divTemplate.qxz')" style="width: 360px;">
              <el-option :label="val.name" :value="val.id" v-for="(val) in labelList" :key="val.id"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item :label="$t('template.divTemplate.ztms')">
            <el-input style="width: 360px;" v-model="createdFromTwo.remark" autocomplete="off" type="textarea" :placeholder="$t('template.divTemplate.ztmsInfo')" :autosize="{ minRows: 3, maxRows: 4}" maxlength="255" show-word-limit></el-input>
          </el-form-item>
        </el-form>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="resetForm('createdFromRef')">{{ $t('template.ccel') }}</el-button>
        <el-button type="primary" @click="editCoupons('createdFromRef')">{{ $t('template.okk') }}</el-button>
      </div>
    </el-dialog>

    <!-- 创建自定义页面 -->
    <el-dialog title="新建页面" :visible.sync="dialogVisible" class="dit">
      <div class="dit-box">
        <el-form inline :model="newPage" :rules="rules" ref="ruleForm" label-width="80px">
          <el-form-item label="页面名称" prop="pageName">
            <el-input v-model="newPage.pageName" style="width: 300px;" placeholder="请输入页面名称"></el-input>
          </el-form-item>
        </el-form>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="cancelPage">取 消</el-button>
        <el-button type="primary" @click="addTabber('ruleForm')">确 定</el-button>
      </span>
    </el-dialog>

    <!-- 查看 自定义页面 关联信息 -->
    <el-dialog :title="$t('template.pageList.ckglxx')" :visible.sync="dialogPages" class="dit">
      <div>
        <el-form inline>
          <el-form-item>
            <el-input v-model="queryPageName" :placeholder="$t('template.pageList.qsrymcm')"></el-input>
          </el-form-item>
          <el-form-item>
            <el-button type="primary">{{ $t('template.pageList.cx') }}</el-button>
          </el-form-item>
        </el-form>
      </div>
      <div>
        <el-table :element-loading-text="$t('DemoPage.tableExamplePage.loading_text')" v-loading="loading" :data="tableData" ref="table" class="el-table" style="width: 100%" :header-cell-style="handleHeaderCellStyle" :cell-style="handleHeaderCellStyle">
          <template slot="empty">
            <div class="empty">
              <img src="../../../assets/imgs/wu.png" alt="" />
              <p style="color: #414658">{{ $t('zdata.zwsj') }}</p>
            </div>
          </template>
          <el-table-column prop="" :label="$t('template.pageList.xh')" width="90">
            <template slot-scope="scope">
              <span>{{ scope.$index + 1 }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="name" :label="$t('template.pageList.glymmc')">
          </el-table-column>
          <el-table-column prop="bdsj" :label="$t('template.pageList.bdsj')">
          </el-table-column>
          <el-table-column fixed="right" :label="$t('template.pageList.cz')" width="200">
            <template slot-scope="{row}">
              <div>
                <el-button type="text" @click="lockInfo(row)">{{ $t('template.pageList.dw') }}</el-button>

                <el-button type="text" @click="delPageById(row)">{{ $t('template.pageList.jb') }}</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div class="pageBox" ref="pageBox">
        <el-pagination layout="  slot, prev, pager, next" :prev-text="$t('DemoPage.tableExamplePage.prev_text')" :next-text="$t('DemoPage.tableExamplePage.next_text')" @size-change="handleSizeChange" :page-sizes="pagesizes" :current-page="pagination.page" @current-change="handleCurrentChange" :total="total">
          <div class="pageRightText">
            {{ $t('DemoPage.tableExamplePage.on_show') }}{{ currpage }}-{{
                    current_num
                    }}{{ $t('DemoPage.tableExamplePage.twig') }}{{ total
                    }}{{ $t('DemoPage.tableExamplePage.twig_notes') }}
          </div>
        </el-pagination>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import diyRightInfo from '@/webManage/js/plug_ins/template/diyRightInfo'
export default diyRightInfo
</script>

<style lang="less" scoped>
@import '../../../webManage/css/plug_ins/template/diyRightInfo.less';
.color-item-title {
  display: flex;
  align-items: center;
  .colorPicker-box {
    margin: 0 6px;
  }
}

.avt,
.avitive {
  border: 1px solid #2d8cf0;
  color: #2d8cf0;
}
.but-list {
  display: flex;
  align-items: center;
  justify-content: end;
  user-select: none;
  p {
    width: 80px;
    display: block;
    height: 32px;
    line-height: 32px;
    text-align: center;
    border: 1px solid #d5dbe8;
    border-radius: 4px;
    cursor: pointer;
    margin-left: 5px;
  }
}

.color-list {
  p {
    width: 50px;
    height: 24px;
    line-height: 24px;
    font-size: 12px;
    margin: 0;
    margin-left: 5px;
  }
}
.upload-box {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 50px;
  height: 100%;
  border-radius: 4px;
  background: #f2f2f2;
}
.img-box {
  position: relative;
  width: 80px;
  height: 80px;

  img {
    width: 100%;
    height: 100%;
  }
}
</style>
