<template>
  <div class="container " :class="{'flex-box': mch_type}" style="padding-top: 0px;">
        <div v-if="mch_type" >
          <div style="display:flex; justify-content:center; align-items:center;  ">
          <div style="background:#fff; padding:40px 60px; border-radius:16px; box-shadow:0 4px 12px rgba(0,0,0,0.08); text-align:center;">
            <!-- 对勾图标 -->
            <div style="width:60px; height:60px; margin:0 auto 20px; background:#4caf50; border-radius:50%; display:flex; justify-content:center; align-items:center;">
              <span style="color:#fff; font-size:32px;">✔</span>
            </div>
            <!-- 标题 -->
            <h2 style="margin:0 0 10px; font-size:24px; color:#333;">{{$t('fastboot.pzwc') }}</h2>
            <!-- 副标题 -->
            <p style="margin:0; font-size:14px; color:#666;">{{$t('fastboot.cg') }}</p>
          </div>
        </div>
      </div >
      <div class="core-set  merchants-list " v-else >
        <div class="form-card" style="min-height: 680px;">
          <div class="title" >
            <el-steps :active="active" finish-status="success" space="500" style="width:530px" simple >
              <el-step :title="$t('fastboot.cjzyd')"></el-step>
              <el-step :title="$t('fastboot.yhsz')"></el-step>
              <el-step :title="$t('fastboot.ydddzpz')"></el-step>
            </el-steps>
          </div>
          <template v-if="active === 0 " >
            <div class="add-menu  " style="  justify-content: normal;" >
              <el-form
                :model="ruleForm"
                :rules="rules"
                ref="ruleForm"
                class="picture-ruleForm"
                label-position="right"
                label-width="100px"
              >
                <el-form-item class="upload-img" :label="$t('stores.viewStore.dplg')" prop="logo">
                  <l-upload
                    :limit="1"
                    v-model="ruleForm.logo"
                    text="(最多上传一张，建议上传120px*40px的图片)"
                  >
                  </l-upload>
                </el-form-item>
                <!-- <el-form-item class="upload-img" :label="$t('stores.viewStore.dptx')"  >
                  <l-upload
                    :limit="1"
                    v-model="ruleForm.headImg"
                    text="(最多上传一张，建议上传120px*120px的图片)"
                  >
                  </l-upload>
                </el-form-item>
                <el-form-item class="upload-img" :label="$t('stores.viewStore.xct')" >
                  <l-upload
                    :limit="1"
                    v-model="ruleForm.posterImg"
                    text="(图片尺寸200*200px，仅用于推荐商家功能及PC商城店铺主页呈现)"
                  >
                  </l-upload>
                </el-form-item> -->
                <el-form-item :label="$t('stores.viewStore.dpmc')" prop="name">
                  <el-input
                    maxlength="20"
                    v-model="ruleForm.name"
                    :placeholder="$t('increaseStore.qsrdpmc')"
                  ></el-input>
                  </el-form-item>
             <!--   <el-form-item :label="$t('stores.viewStore.dpxx')" >
                  <el-input
                    maxlength="200"
                    v-model="ruleForm.shop_information"
                    :placeholder="$t('increaseStore.qsrdpjjxx')"
                  ></el-input>
                </el-form-item>
                 <el-form-item :label="$t('increaseStore.dpfl')" >
                  <el-select
                    class="select-input2"
                    v-model="ruleForm.mac_choose_fl"
                    :placeholder="$t('increaseStore.qxzdpfl')"
                  >
                    <el-option
                      v-for="(item, index) in choose_fl"
                      :key="index"
                      :label="item.name"
                      :value="item.id"
                    ></el-option>
                  </el-select>
                  <el-button class="add_bt" @click="dialogVisible = true">添加店铺分类</el-button>
                </el-form-item>
                <el-form-item :label="$t('increaseStore.jyfw')"  >
                  <el-input
                    maxlength="200"
                    v-model="ruleForm.shop_range"
                    :placeholder="$t('increaseStore.qsrjyfw')"
                  ></el-input>
                </el-form-item>
                <el-form-item :label="$t('increaseStore.yhxm')"  >
                  <el-input
                    maxlength="20"
                    :placeholder="$t('increaseStore.qsrzsxm')"
                    v-model="ruleForm.realname"
                  ></el-input>
                </el-form-item>
                <el-form-item :label="$t('increaseStore.sfzh')" >
                  <el-input
                    :placeholder="$t('increaseStore.qsrsfzh')"
                    @keyup.native="ruleForm.ID_number = oninput3(ruleForm.ID_number)"
                    v-model="ruleForm.ID_number"
                  ></el-input>
                </el-form-item> -->
                <el-form-item :label="$t('increaseStore.lxdh')" prop="tel">
                  <div class="input-interval-block">
                    <el-autocomplete
                    class='auto'
                      v-model="state"
                      :fetch-suggestions="querySearchAsync"
                      :placeholder="$t('membersLists.qh')"
                      @select="handleSelect"
                    >
                    <template slot-scope="{ item }">
                      <div>
                        <span>{{ item.name }}</span>
                        <span style="color: #999; margin-left: 10px;">+({{ item.code2 }})</span>
                      </div>
                    </template>
                    </el-autocomplete>
                    <el-input
                    :placeholder="$t('increaseStore.qsrlxdh')"
                    @keyup.native="ruleForm.tel = oninput2(ruleForm.tel)"
                    v-model="ruleForm.tel"
                  ></el-input>
                  </div>

                </el-form-item>
                <el-form-item v-if="isDomestic" class="cascadeAddress" :label="$t('increaseStore.szdq')" prop="sheng">
                  <div class="cascadeAddress-block">
                    <el-select
                      class="select-input"
                      v-model="ruleForm.sheng"
                      :placeholder="$t('increaseStore.sheng')"
                    >
                      <el-option
                        v-for="(item, index) in shengList"
                        :key="index"
                        :label="item.districtName"
                        :value="item.districtName"
                      >
                        <div @click="getShi(item.id)">{{ item.districtName }}</div>
                      </el-option>
                    </el-select>
                    <el-select
                      class="select-input"
                      v-model="ruleForm.shi"
                      :placeholder="$t('increaseStore.shi')"
                    >
                      <el-option
                        v-for="(item, index) in shiList"
                        :key="index"
                        :label="item.districtName"
                        :value="item.districtName"
                      >
                        <div @click="getXian(item.id)">{{ item.districtName }}</div>
                      </el-option>
                    </el-select>
                    <el-select
                      class="select-input"
                      v-model="ruleForm.xian"
                      :placeholder="$t('increaseStore.xian')"
                    >
                      <el-option
                        v-for="(item, index) in xianList"
                        :key="index"
                        :label="item.districtName"
                        :value="item.districtName"
                      >
                        <div>{{ item.districtName }}</div>
                      </el-option>
                    </el-select>
                  </div>
                </el-form-item>
                <el-form-item :label="$t('increaseStore.xxdz')" prop="address">
                  <el-input
                    :placeholder="$t('increaseStore.qsrxxdz')"
                    v-model="ruleForm.address"
                  ></el-input>
                </el-form-item>

                <el-form-item :label="$t('aftersaleAddress.yzbm')" >
                  <el-input
                    :placeholder="$t('aftersaleAddress.add.qsryzbm')"
                    v-model="ruleForm.code"
                  ></el-input>
                </el-form-item>
                <el-form-item :label="$t('increaseStore.ssxz')">
                  <el-radio-group
                    v-model="ruleForm.shop_nature"
                    @change="natureChange($event)"
                  >
                    <el-radio
                      v-for="item in belongList"
                      :label="item.value"
                      :key="item.value"
                      >{{ item.name }}</el-radio
                    >
                  </el-radio-group>
                </el-form-item>

              </el-form>
            </div>
        </template>

        <template v-if="active === 1 ">
            <div class="user-box" style="  ">
              <el-form ref="ruleForm1" class="form-search" :rules="rule" :model="ruleForm1"
                style="height: auto;"
               label-position="right"
                label-width="130px">
                <div class="basic-info">

                  <div class="formBox" style="justify-content: normal;">
                      <div class="basic-block">
                        <!-- 默认头像设置 -->
                        <el-form-item :label="$t('membersSet.mrtx')" prop="wx_headimgurl">
                          <l-upload
                            :limit="1"
                            v-model="ruleForm1.wx_headimgurl"
                            :text="$t('membersSet.txbz')"
                          >
                          </l-upload>
                        </el-form-item>
                        <!-- 默认昵称设置 -->
                        <el-form-item :label="$t('membersSet.mrnc')" prop="wx_name">
                          <el-input maxlength="16" v-model="ruleForm1.wx_name" @keyup.native="ruleForm1.wx_name = stripscript(ruleForm1.wx_name)" :placeholder="$t('membersSet.qsrmrnc')"></el-input>
                        </el-form-item>

                        <el-form-item :label="$t('configMod.yjfwqdz')" prop="host">
                          <el-input v-model="ruleForm1.host" :placeholder="$t('fastboot.SMTP')"></el-input>
                        </el-form-item>
                        <el-form-item :label="$t('configMod.fjryxzh')" prop="username">
                          <el-input v-model="ruleForm1.username" :placeholder="$t('fastboot.yxzh')"></el-input>
                        </el-form-item>

                        <el-form-item :label="$t('configMod.yxsqm')" prop="password">
                          <el-input v-model="ruleForm1.password" :placeholder="$t('fastboot.yxsqm')"></el-input>
                        </el-form-item>
                        <el-form-item :label="$t('configMod.sfqySSL')" >
                          <el-switch v-model="ruleForm1.sslEnable"></el-switch>
                        </el-form-item>
                        <el-form-item :label="$t('mryz')" prop="default_lang">
                          <el-select
                            class="select-input"
                            filterable
                            :disabled="isSwitchFlag"
                            v-model="ruleForm1.default_lang"
                            :placeholder="$t('qxzmryz')"
                          >
                            <el-option
                              v-for="(item,index) in languages"
                              :key="index"
                              :label="item.lang_name"
                              :value="item.lang_code"
                            />
                          </el-select>
                        </el-form-item>
                        <el-form-item :label="$t('mrbz')" prop="default_currency">
                          <el-select
                            class="select-input"
                            filterable
                            :disabled="switchFlag"
                            v-model="ruleForm1.default_currency"
                            :placeholder="$t('qxzmrbz')"
                          >
                            <el-option
                              v-for="(item,index) in currencys"
                              :key="index"
                              :label="item.currency_name"
                              :value="item.id"
                            />
                          </el-select>
                        </el-form-item>
                      </div>
                  </div>
                </div>

              </el-form>
            </div>
        </template>

        <template v-if="active === 2 ">
          <div class="add-menu" style=" margin-top: 10px;" >
            <el-form
              :model="ruleForm3"
              :rules="rule"
             label-position="right"
              ref="ruleForm3"
              label-width="auto"
              class="form-search"
              style="height: 500px;"
            >
              <el-form-item
                :label="$t('systemManagement.diyyl')"
                prop="h_Address"
              >
                <el-input
                  v-model="ruleForm3.h_Address"
                ></el-input>
              </el-form-item>
            </el-form>
          </div>

        </template>
      </div>

        <div class="footerBox" >
            <el-button
              class="bgColor"
              type="primary"
              @click="submitForm('ruleForm')"
              >{{ active !== 2 ? '下一步' : $t('DemoPage.tableFromPage.save') }}</el-button
            >
            <el-button class="bdColor" @click="toHome" plain>{{
               active === 0?
              $t('DemoPage.tableFromPage.cancel') : "上一步"
            }}</el-button>
        </div>
      </div>


      <div class="dialog-brand">
      <!-- 弹框组件 -->
      <el-dialog
        :title="$t('releasephysical.tjfl')"
        :visible.sync="dialogVisible"
        :before-close="handleClose"
      >
        <el-form
          :model="ruleForm2"
          :rules="rules2"
          ref="ruleForm2"
          class="picture-ruleForm"
          label-width="auto"
        >
        <el-form-item :label="$t('stores.addStoreFl.flmc')" prop="name">
          <el-input maxlength="20" class="select-input" v-model="ruleForm2.name" :placeholder="$t('stores.addStoreFl.qsrfl')"></el-input>
        </el-form-item>
        <el-form-item :label="$t('stores.addStoreFl.fltb')">
          <l-upload
            ref="imgUpload"
            :limit="1"
            v-model="ruleForm2.img"
            :text="$t('stores.addStoreFl.zdscq')"
          >
          </l-upload>
        </el-form-item>
        <el-form-item class="input-interval" :label="$t('stores.addStoreFl.pxh')" prop="px">
          <el-input class="select-input" @keyup.native="ruleForm2.px = oninput2(ruleForm2.px)" v-model="ruleForm2.px" :placeholder="max + $t('stores.addStoreFl.mrpxh')"></el-input>
        </el-form-item>

       <el-form-item :label="$t('stores.addStoreFl.sfxs')" style="margin-bottom: 0;">
            <el-radio-group v-model="ruleForm2.ishow">
              <el-radio v-for="item in classList" :label="item.value" :key="item.value">{{item.name}}</el-radio>
            </el-radio-group>
        </el-form-item>

          <div class="form-footer">
            <el-form-item>
              <el-button class="bdColor" @click="handleClose" plain>{{
                $t('DemoPage.tableFromPage.cancel')
              }}</el-button>
              <el-button
                class="bgColor"
                type="primary"
                @click="submitForm2('ruleForm2')"
                >{{ $t('DemoPage.tableFromPage.save') }}</el-button
              >
            </el-form-item>
          </div>
        </el-form>
      </el-dialog>
    </div>
  </div>
</template>

<script>
import LaiKeCommon from '@/api/common.js'
import cascade from '@/api/publics/cascade'
import { getMchFl, modifyMchInfo } from '@/api/plug_ins/stores'
export default {
  data() {
    return {
      active:0,
      id:'',
      ruleForm: {
        logo: '',
        headImg: '',
        posterImg: '',
        name: '',
        shop_information: '',
        shop_range: '',
        // community_id: '',
        // adminid: '',
        realname: '',
        ID_number: '',
        tel: '',
        sheng: '',
        shi: '',
        xian: '',
        address: '',
        shop_nature: 0,
        // business_licens: [],
        img1: '', //身份证正面
        img2: '', //身份证反面
        img3: '', //营业执照
        mac_choose_fl: ''
      },
      ruleForm1:{
        wx_name: "",
        wx_headimgurl: "",
        host: '',
        username: '',
        password: '',
        sslEnable: true,
        default_currency: '',
        default_lang: ''
      },
      dialogVisible: false,
      choose_fl: [], // 店铺分类
      rules: {
        logo: [
          {
            required: true,
            message: '请选择店铺logo',
            trigger: 'change'
          }
        ],
        headImg: [
          {
            required: true,
            message: '请选择店铺头像',
            trigger: 'change'
          }
        ],
        posterImg: [
          {
            required: true,
            message: '请选择店铺宣传图',
            trigger: 'change'
          }
        ],
        name: [
          {
            required: true,
            message: '请输入店铺名称',
            trigger: 'blur'
          }
        ],
        shop_information: [
          {
            required: true,
            message: '请输入店铺简介',
            trigger: 'blur'
          }
        ],
        shop_range: [
          {
            required: true,
            message: '请输入店铺经营范围',
            trigger: 'blur'
          }
        ],
        mac_choose_fl: [
          {
            required: true,
            message: '请选择店铺分类',
            trigger: 'change'
          }
        ],
        realname: [
          {
            required: true,
            message: '请输入用户姓名',
            trigger: 'blur'
          }
        ],
        ID_number: [
          {
            required: true,
            message: '请输入身份证号',
            trigger: 'blur'
          }
        ],
        tel: [
          {
            required: true,
            message: '请输入联系电话',
            trigger: 'blur'
          }
        ],
        sheng: [
          {
            required: true,
            message: '请选择省或直辖市',
            trigger: 'change'
          }
        ],
        address: [
          {
            required: true,
            message: '请输入详细地址',
            trigger: 'blur'
          }
        ]
      },
      ruleForm3: {
        h_Address: '',
      },
      rule: {
          wx_headimgurl:[
              { required: true, message: this.$t('默认头像不能为空'), trigger: 'change' },
          ],
          wx_name: [
              { required: true, message: this.$t('默认昵称不能为空'), trigger: 'blur' },
          ],
          h_Address: [
          {
            required: true,
            message: this.$t('systemManagement.qtxh5'),
            trigger: 'blur'
          },
          ],
          host: [
            {
              required: true,
              message: this.$t('configMod.qsryjfwqdz'),
              trigger: 'blur'
            }
          ],
          username: [
            {
              required: true,
              message: this.$t('configMod.qsrfjryxzh'),
              trigger: 'blur'
            }
          ],
          password: [
            {
              required: true,
              message: this.$t('configMod.qsryxsqm'),
              trigger: 'blur'
            }
          ],
          default_lang:[
          {required: true, message: this.$t('qxzmryz'), trigger: 'change'}
        ],
        default_currency:[
          {required: true, message: this.$t('qxzmrbz'), trigger: 'change' }
        ]
      },
      ruleForm2: {
        name: '',
        img: '',
        px: '',
        ishow: 1
      },
      rules2: {
        name: [
          {
            required: true,
            message: this.$t('stores.addStoreFl.qsrfl'),
            trigger: 'blur'
          }
        ],
        px: [
          {
            required: true,
            message: '请输入排序号',
            trigger: 'blur'
          }
        ]
      },
      belongList: [
        {
          value: 0,
          name: this.$t('stores.viewStore.gr')
        },
        {
          value: 1,
          name: this.$t('stores.viewStore.qy')
        }
      ],
      classList: [
        {
          value: 1,
          name: this.$t('stores.addStoreFl.shi')
        },
        {
          value: 0,
          name: this.$t('stores.addStoreFl.fou')
        }
      ],
      max: '3',
      //省市级联集
      shengList: {},
      shiList: {},
      xianList: {},
      state:'',
      storeInfo: null,
      countryNum: '', // 选中的国家的num3值
      restaurants:[],
      isDomestic: true, // 是否国内
      mch_type: false, // 是否有自营店
      languages:[], // 语种集合
      currencys:[], // 币种集合
      isSwitchFlag:false, // 是否禁用语言选择
      switchFlag:false // 是否默认币种
    };
  },
  watch: {
    'ruleForm1.wx_headimgurl'() {
      this.$refs.ruleForm1.validateField('wx_headimgurl')
    },
  },
    //组装模板
  async created() {
    await this.getadminMch()

    this.getSheng()
    this.getMchFls()

    this.loadData();

  },
  mounted() {
    if(sessionStorage.getItem('restaurants')){
      this.restaurants =JSON.parse( sessionStorage.getItem('restaurants'))
    }else{
        this.queryAdd()
    }
    this.getLandingCurrency()
  },
  methods:{
    async getLandingCurrency(){
      let id = ''
      await LaiKeCommon.select({
        api: 'saas.shop.getLandingCurrency',
      }).then(res=>{
        if(res.data.code == '200'){
          const {data} = res.data
          this.ruleForm1.default_currency = data.default_currency
          this.switchFlag = !(
            this.ruleForm1.default_currency === null ||
            this.ruleForm1.default_currency === undefined ||
            this.ruleForm1.default_currency === ""
          );
          this.isSwitchFlag = !(
            data.default_lang_id === null ||
            data.default_lang_id === undefined ||
            data.default_lang_id === ""
          );
          id = data.default_lang_id
        }
      })
      const res = await this.LaiKeCommon.select({
        api: 'admin.currency.currencyList',
        pageNo: 1,
        pageSize: 300
      })
      this.currencys = res.data.data.list

      const res1 = await this.LaiKeCommon.select({
        api: 'admin.lang.index',
        pageNo: 1,
        pageSize: 300
      })
      this.languages = res1.data.data.list
      const item = this.languages.find(item=>item.id == id)
       this.ruleForm1.default_lang = item.lang_code

    },

    // 判断是否有自营店
    async getadminMch(){
       LaiKeCommon.select({
        api: 'admin.system.checkHaveStoreMchId',
      }).then(res=>{
        if(res.data.code == '200'){
           this.mch_type = res.data.data
        }
      })

    },
    handleClose () {
      this.dialogVisible = false
      this.ruleForm2.name = ''
      this.ruleForm2.img = ''
      this.$refs.imgUpload.fileList = []
      this.ruleForm2.px = ''
      this.ruleForm2.ishow = 1
      this.$refs['ruleForm2'].clearValidate()
    },
    async loadData() {
      const {data:res} = await LaiKeCommon.select({
          api: 'admin.system.getEmailConfig',
          id: this.$route.query.id,
      })
      console.log('resresres',res)
      if(res && res.code === '200'){
        const {data} =res
        this.id = data.id
         if(data.mail_config.length>0){
          this.mainForm = JSON.parse(data.mail_config)
         }
      }
    },
    queryAdd(){
      const data ={
          api:'admin.user.getItuList',
          keyword:this.keyword
      }
      getItuList(data).then(res=>{
        if(res.data.code == 200){
            this.restaurants = res.data.data
            sessionStorage.setItem('restaurants',JSON.stringify(this.restaurants))
        }
      })
    },
    // 获取省级
    async getSheng () {
      const res = await cascade.getSheng()
      this.shengList = res.data.data
    },
    toHome(){
      if(this.active !== 0){
        this.active -= 1
        return
      }

      this.$router.push({ path: '/' })
    },
    // 获取市级
    async getShi (sid, flag) {
      const res = await cascade.getShi(sid)
      this.shiList = res.data.data
      if (!flag) {
        this.ruleForm.shi = ''
        this.ruleForm.xian = ''
      }
    },

    // 获取县级
    async getXian (sid, flag) {
      const res = await cascade.getXian(sid)
      this.xianList = res.data.data
      if (!flag) {
        this.ruleForm.xian = ''
      }
    },
    submitForm2 (formName) {
      this.$refs[formName].validate(async valid => {
        if (valid) {
          try {
            modifyMchInfo({
              api: 'mch.Admin.Mch.AddMchClass',
              name: this.ruleForm2.name,
              img: this.ruleForm2.img,
              sort: this.ruleForm2.px,
              isDisplay: this.ruleForm2.ishow
            }).then(res => {
              if (res.data.code == '200') {
                this.$message({
                  message: this.$t('zdata.tjcg'),
                  type: 'success',
                  offset: 102
                })
                this.handleClose()
                this.getMchFls(true)
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
    },
     // 选择建议项时触发的方法
     handleSelect(item) {
      console.log('选中的项:', item);
      this.state = item.code2; // 可以根据需求更新输入框显示的值
      this.countryNum = item.num3;
      if (item.code2 == '86' || item.code2 == '852' || item.code2 == '853') {
        this.isDomestic = true;
      }else {
        this.isDomestic = false;
      }
    },
    // 异步查询建议列表的方法
    querySearchAsync(queryString, cb) {
      // 模拟异步请求
      setTimeout(() => {
      const results = queryString
          ? this.restaurants.filter(this.createFilter(queryString))
          : this.restaurants;
      // 调用回调函数，将查询结果传递给组件
      cb(results);
      }, 300);
    },
    createFilter(queryString) {
      return (country) => {
          const lowerCaseQuery = queryString.toLowerCase();
          return (
              country.name.toLowerCase().indexOf(lowerCaseQuery) > -1 ||
              country.code2.toLowerCase().indexOf(lowerCaseQuery) > -1 ||
              country.zh_name.toLowerCase().indexOf(lowerCaseQuery) > -1
          );
      };
  },
     //获取店铺分类
     async getMchFls (isAdd) {
      const res = await getMchFl({
        api: 'mch.Admin.Mch.MchClassList',
        isDisPlay: 1
      })
      console.log('店铺分类res', res)
      if (res.data.code == 200) {
        this.choose_fl = res.data.data.list
         if (isAdd) {
          this.ruleForm.mac_choose_fl = this.choose_fl[0].id
        }
      } else {
        console.log(res.data.message)
      }
    },
    submitForm(){
      let formName = ''
          formName = this.active === 0 && 'ruleForm'
      if(!formName){
          formName = this.active === 1 && 'ruleForm1'
      }
      if(!formName){
          formName = this.active === 2 && 'ruleForm3'
      }
      console.log(formName)

        this.$refs[formName].validate((valid) => {
          console.log(valid)
          if (typeof valid && valid) {
            if(this.active !== 2){
              this.active += 1
            }else{
              if (this.ruleForm.shop_nature == 1) {
              var imgUrls = this.ruleForm.img3
            } else if (this.ruleForm.shop_nature == 0) {
              var imgUrls = this.ruleForm.img1 + ',' + this.ruleForm.img2
            }
              LaiKeCommon.edit({
                api:'admin.system.quickProfile',
                mail_config:JSON.stringify({
                  host: this.ruleForm1.host,
                  username: this.ruleForm1.username,
                  password: this.ruleForm1.password,
                  sslEnable: this.ruleForm1.sslEnable
                }),
                // imgUrls:imgUrls,
                cid: this.ruleForm.mac_choose_fl,
                ...this.ruleForm,
                ...this.ruleForm1,
                ...this.ruleForm3
              }).then((res)=>{
                if(res.data.code == '200'){
                  this.$message.success('保存成功')
                  setTimeout(()=>{
                    this.$router.push({ path: '/' })
                  },1500)
                }
              })
            }
          } else {
            return false;
          }
        });
    }
  }
}
</script>

<style scoped lang="less">
@import "../../../webManage/css/mall/sms/smsList.less";
@import '../../../webManage/css/plug_ins/stores/addStore.less';
@import '../../../webManage/css/members/membersList/membersSet.less';
@import "../../../webManage/css/mall/systemManagement/basicConfiguration.less";
/deep/ .el-steps--simple {
    padding: 0px;
    background: transparent;
    width: 520px;
}
/deep/ .el-step__icon{
  width: 20px !important;
  height: 20px !important;
}
.flex-box {
  background:#f5f6f8;
  display:flex;
  justify-content:center;
  align-items:center;
}
.core-set {
  display: block;
  padding: 0;
}
.form-search{
  padding: 0px !important;
  margin: 0px !important;
}
.add-menu{
  height: auto !important;
  /deep/ .el-form-item__content{
    margin: 0px !important;
  }
}
.basic-block {
 margin: 0px !important;
  p {
    color: #b2bcd1;
  }
}
/deep/ .is-success,.is-text{
  .el-step__title{

    color:#2dcc35 !important;
  }
  border-color:#2dcc35;

}
/deep/ .is-process  {
  .is-text{
    color: #2d6dcc;
    border-color:#2d6dcc;
  }
}
/deep/ .is-process {
  color: #2d6dcc !important;
}

/deep/ .el-step__icon-inner{
  font-size: 20px;
}


  /deep/ .el-step{
    flex: 1;
  }

  /deep/ .merchants-list{
    el-input {
      width: auto;
    }
  }
  .footerBox {
      position: fixed;
      right: 0;
      bottom: 40px;
      display: flex;
      align-items: center;
      justify-content: flex-end;
      padding: 15px 20px;
      border-top: 1px solid #E9ECEF;
      background: #FFFFFF;
      width: 300%;
      z-index: 10;
      button {
        height: 40px;
      }
    }
</style>
