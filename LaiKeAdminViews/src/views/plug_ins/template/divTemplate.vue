<template>
  <div class="container">
    <el-form ref="ruleForm" class="form-search">

      <div class="div-content" id="iframe_tem" :style="{'display':typeIndex === '3' ?'block':'flex'}">
        <div class="right-template" style="flex: 1;">
          <div class="header">
            <div class="title-type" @click="chooseType">
              <span :class="typeIndex == '1' && 'select-title'" :data-type="1">{{$t('template.divTemplate.diymb')}}</span>
              <span :class="typeIndex == '2' && 'select-title'" :data-type="2">{{$t('template.divTemplate.zdyzt')}}</span>
              <span :class="typeIndex == '3' && 'select-title'" :data-type="3">{{$t('template.divTemplate.ymgl')}}</span>
              <span :class="typeIndex == '4' && 'select-title'" :data-type="4">{{$t('template.divTemplate.ydt')}}</span>
            </div>

            <div class="add" style="margin-right: 20px;">
              <!-- 新建模板 -->
              <el-button type="primary" @click="dialogFormVisible = true" v-if="(typeIndex == 2 || isLkt) && typeIndex !=3">{{$t('template.divTemplate.xjmb')}}</el-button>
            </div>
          </div>
          <!-- 自定义模板主题 列表显示方式 -->
          <div class="template-header" :style="{'justify-content': typeIndex!=2?'end':'space-between'}">
            <div ref="scrollContainer" class="scrollContainer" v-if="typeIndex==2" @mousedown="onMouseDown" @mousemove="onMouseMove" @mouseup="onMouseUp" @mouseleave="onMouseUp">
              <l-tab ref="navLabel" :nav_label_type="nav_label_type" :nav_label_list="nav_label_list" @_choose="nav_choose">
              </l-tab>
            </div>
            <!-- 排列方式 -->
            <div class="type-setting" @click="switchShowType" v-if="typeIndex!=3&&typeIndex!=4">
              <div class="item" :class="switchIndex == 0 && 'query-type'" :data-index="0">
                <img :src="getImageUrl(dt)" class="icon-img" />
                {{$t('template.divTemplate.tw')}}
              </div>
              <div class="item" :class="switchIndex == 1 && 'query-type'" :data-index="1">
                <img :src="getImageUrl(pl)" class="icon-img" />
                {{$t('template.divTemplate.lb')}}
              </div>
            </div>
          </div>
          <div v-if="typeIndex!=3&&typeIndex!=4" class="bz">
            <span style="color: red;font-weight: 700;font-size: 15px;"> *注 </span> ：恢复默认模板请取消当前应用
          </div>
          <!-- diy模板列表 -->
          <div class="template-content" ref="templateContent" v-if="typeIndex!=3&&typeIndex!=4" :style="{'height': `${tableHeight}px`}" element-loading-text="拼命加载中">
            <!-- 列表 -->
            <template v-if="switchIndex==1">
              <template v-if="imgList && imgList.length>0 ">
                <ul style="margin: 16px 10px; display: grid;grid-template-columns: repeat(5, 1fr);gap: 21px; place-items: center;" v-loading="loading">

                  <li v-for="(item,index) in imgList" :key="index" class="list-soring" :class="{'operation1': selection === index }" @mouseenter="preSelection = index" @mouseleave="preSelection=null" @click="queryItem(index)">
                    <img :src="getImageUrl(gouxuan)" class="icon-img list-icon" v-if="item.status == 1" />

                    <div class="diy-inof" :class="selection === index && 'operation2'">
                      <div class="img-box">
                        <img :src="item.cover" v-if="item.cover">
                      </div>
                    </div>
                    <div class="default-p list-name" :class="preSelection === index?'butMask':'textMask'" style=" background: linear-gradient(to bottom, rgb(255 255 255 / 100%), #ecf0f6, #f7f9fd)">
                      <span v-if="preSelection !== index" class="tw-diy-title">{{ item.name }} </span>
                      <div class="operation" v-if="preSelection === index">
                        <div class="btn-item">
                          <el-button type="primary" v-if="item.status == 0" @click="Application(item)">{{$t('template.divTemplate.yy')}}</el-button>
                          <el-button type="primary" v-if="item.status == 1" @click="Application('默认')">{{$t('template.divTemplate.qxyy')}}</el-button>
                          <el-button type="primary" @click="editor(item)">{{$t('template.bianji')}}</el-button>
                          <el-button type="primary" v-if="item.status != 1" @click="Delete(item)">{{$t('template.shanchu')}}</el-button>
                        </div>
                      </div>
                    </div>
                  </li>
                </ul>
              </template>
              <template v-else>
                <div class="empty" v-loading="loading">
                  <img src="../../../assets/imgs/wu.png" alt="" style="width: 200px;" />
                  <p style="color: #97A0B4">{{ $t('template.pageManafenebt.zwzt') }}</p>
                </div>

              </template>
            </template>
            <!-- 图文 -->
            <template v-else>
              <template v-if="imgList && imgList.length>0 ">
                <ul v-loading="loading" style="margin: 20px; display: grid;grid-template-columns: repeat(3, 1fr); gap: 21px;place-items: center;">

                  <li class="default" :class="{'operation1':selection === index,'backColor': preSelection === index}" v-for="(item,index) in imgList" :key="index" @mouseenter="preSelection = index" @mouseleave="preSelection=null" @click="queryItem(index)">
                    <div class="current-box">

                      <p class="default-p tw-diy-title">
                        <span>{{ item.name }}</span>
                      </p>
                      <div class="current-use" v-if="item.status == 1">
                        <img :src="getImageUrl(yingyong)" class="icon-img" />
                        {{$t('template.divTemplate.dqyy')}}
                      </div>
                    </div>
                    <!-- <el-divider></el-divider> -->
                    <div class="current-info">
                      <img :src="item.cover" class="wt-icon" v-if="item.cover">
                      <div style="flex: 1;">
                        <div>
                          <p class="diy-info">{{ item.remark }}</p>
                        </div>
                        <div class="btn-item" style="height: 40px;" :style="{ 'text-align': item.status === 0 && typeIndex != 1 ? 'center' : 'left' }">
                          <template v-if="preSelection === index">
                            <el-button v-if="item.status == 0" @click="Application(item)">{{$t('template.divTemplate.yy')}}</el-button>
                            <el-button v-if="item.status == 1" @click="Application('默认')">{{$t('template.divTemplate.qxyy')}}</el-button>

                            <el-button class="edit" @click="editor(item)">{{$t('template.bianji')}}</el-button>
                            <el-button class="del" v-if='(typeIndex == 2 || isLkt) && item.status !=1' @click="Delete(item)">{{$t('template.shanchu')}}</el-button>
                          </template>
                        </div>
                      </div>
                    </div>
                  </li>
                </ul>
              </template>
              <template v-else>
                <div class="empty" v-loading="loading">
                  <img src="../../../assets/imgs/wu.png" alt="" style="width: 200px;" />
                  <p style="color: #97A0B4">{{ $t('template.pageManafenebt.zwzt') }}</p>
                </div>
              </template>
            </template>
          </div>
          <page-management v-if="typeIndex==3" />
          <guideMap v-if="typeIndex==4" />
        </div>
        <div class="left-show" v-if="typeIndex!=3&&typeIndex!=4">
          <div class="show-header">
            <div class="title">
              <span> {{ template }}</span>
            </div>
            <div class="editor" @click="editors">
              <i class="el-icon-edit-outline"></i>
              <span>{{$t('template.bianji')}}</span>
            </div>
          </div>
          <div class="show-content" ref="iframeBox">
            {{ iframeHeight }}
            <div class="preview-box">
              <iframe v-if="iframeShow" :src="H5_domain" ref="mobsf" id="mobsf" scrolling="auto" frameborder="0" name="refresh_name" class="border-box" style="background-size: 100% 100%;" :style="{
                                 'height': `${iframeHeight }`
                        }"></iframe>

            </div>
          </div>
        </div>
      </div>
    </el-form>
    <!-- 新建模板弹窗 -->
    <el-dialog :title="$t('template.divTemplate.cjztxx')" :visible.sync="dialogFormVisible" width="600px" class="dit">
      <div class="dit-box">
        <el-form :model="createdFrom" :rules="rules" ref="createdFromRef" label-width="120px">
          <el-form-item :label="$t('template.divTemplate.ztmc')" prop="name">
            <el-input v-model="createdFrom.name" autocomplete="off" :placeholder="$t('template.divTemplate.qsr')" style="width: 360px;"></el-input>
          </el-form-item>
          <!-- 主题列别 只有 自定义主题创建时 才会有-->
          <el-form-item :label="$t('template.divTemplate.ztlb')" prop="theme_type_code" v-if="typeIndex == 2">
            <el-select v-model="createdFrom.theme_type_code" :placeholder="$t('template.divTemplate.qxz')" style="width: 360px;">
              <el-option :label="val.name" :value="val.id" v-for="(val) in nav_label_list1" :key="val.id"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item :label="$t('template.divTemplate.ztms')">
            <el-input style="width: 360px;" v-model="createdFrom.remark" autocomplete="off" type="textarea" :placeholder="$t('template.divTemplate.ztmsInfo')" :autosize="{ minRows: 3, maxRows: 4}" maxlength="255" show-word-limit></el-input>
          </el-form-item>
        </el-form>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="resetForm('createdFromRef')">{{ $t('template.ccel') }}</el-button>
        <el-button type="primary" @click="addCoupons('createdFromRef')">{{ $t('template.okk') }}</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import divTemplate from '@/webManage/js/plug_ins/template/divTemplate'
export default divTemplate
</script>

<style scoped lang='less'>
@import '../../../webManage/css/plug_ins/template/divTemplate.less';
</style>
