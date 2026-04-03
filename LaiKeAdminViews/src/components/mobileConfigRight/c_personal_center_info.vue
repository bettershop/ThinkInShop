<template>
  <div class="goods-box">
    <div style="margin-top: 10px;">
      <div>
        <span class="title">内容</span>
      </div>
      <div class="hied">
        <div class="flex">
          <span class="title">个人信息</span>
          <div @click="queryInfoFalg">
            <!-- <i class="el-icon-arrow-up" v-if="queryFalg"></i>
            <i class="el-icon-arrow-down" v-else></i> -->
          </div>
        </div>
        <template>
          <div class="personal-info-box">

            <p>
              <span>个人资产</span>
            </p>
            <div>
              <span>展示内容</span>
              <span>最多显示5个内容</span>
            </div>
            <div>
              <el-checkbox-group v-model="iconConfig.checkList">
                <el-checkbox :label="value.type" v-for="(value,index) in checkList" :key="index">
                  {{ value.name }}
                </el-checkbox>
              </el-checkbox-group>
            </div>

          </div>
        </template>

        <div class="flex" v-if="false">
          <span class="title">广告</span>
          <div @click="queryInfoFalg">
            <!-- <i class="el-icon-arrow-up" v-if="queryFalg"></i>
            <i class="el-icon-arrow-down" v-else></i> -->
          </div>
        </div>
        <template v-if="false">
          <div class='adv-box'>
            <!-- <div  class="flex">
                                <span>会员中心</span>
                                <ASwitch v-model="advConfig.vipShow" ></ASwitch>
                            </div>  -->
            <!-- <div  class="flex">
                                <span>店铺/银行卡</span>
                                <ASwitch v-model="iconConfig.kjrk" ></ASwitch>
                            </div> -->
            <!-- <p>
                                <span>添加图片</span>
                                <span>建议尺寸48px * 48px,拖拽可调整图标位置</span>
                            </p> -->
            <!-- 图片 -->
            <!-- <div>
                                <draggable>

                                </draggable>
                                <div>+ 添加</div>
                            </div>  -->
          </div>
          <div>
            <p>
              <span>直角/圆角</span>
              <Col class="color-box">
              <RadioGroup v-model="advConfig.boxType" type="button">
                <Radio :label="key" v-for="(radio,key) in butTylelist" :key="key">
                  <span class="iconfont-diy" :class="radio.icon" v-if="radio.icon"></span>
                  <span v-else>{{radio.val}}</span>
                </Radio>
              </RadioGroup>
              </Col>
            </p>
            <p>
              <span>圆角值</span>
              <Col span="18" class="slider-box">
              <Slider show-input :max='15' v-model="advConfig.boxRoundedVal" :disabled="advConfig.boxType == 1 "></Slider>
              </Col>
            </p>
            <div style="display: flex;justify-content: space-between;">
              <span>背景颜色</span>
              <div>
                <div class="but-list color-list" @click="upBoxColor2">
                  <p data-type="0" :class="{ 'avt':advConfig.backgType == 0 }">纯色</p>
                  <p data-type="1" :class="{ 'avt':advConfig.backgType == 1 }">渐变</p>
                </div>
                <div style="display: flex;margin-top: 10px;">
                  <div class="color-item-title" v-if="advConfig.backgType == 1">
                    <div class="color-item-title-text" style="width: 75px;">{{ advConfig.backgColor1 }}</div>
                    <el-color-picker format="hex" size="mini" v-model="advConfig.backgColor1" alpha class="colorPicker-box" style="margin: 0 2px;"></el-color-picker>
                    <div class="reset-btn" style="margin: 0;" @click="advConfig.backgColor1 = '#FFF'">重置</div>
                  </div>
                  <div class="color-item-title">
                    <div class="color-item-title-text" style="width: 75px;">{{ advConfig.backgColor2 }}</div>
                    <el-color-picker format="hex" size="mini" v-model="advConfig.backgColor2" alpha class="colorPicker-box" style="margin: 0 2px;"></el-color-picker>
                    <div class="reset-btn" style="margin: 0;" @click="advConfig.backgColor2 = '#FFF'">重置</div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </template>

        <div class="flex">
          <span class="title">订单模块</span>
          <div @click="queryInfoFalg">
            <!-- <i class="el-icon-arrow-up" v-if="queryFalg"></i>
            <i class="el-icon-arrow-down" v-else></i> -->
          </div>
        </div>
        <template>
          <div class='adv-box'>
            <div class="flex">
              <span>显示/隐藏</span>
              <ASwitch v-model="orderCondfig.isShow"></ASwitch>
            </div>
            <div class="flex">
              <span> 标题名称</span>
              <Input v-model="orderCondfig.orderTitle" class="my-input" placeholder="请输入标题名称" />
            </div>
            <!-- <p>
              <span>图标 (共<span style="color: #000;" v-if="orderCondfig.orderList">{{  orderCondfig.orderList.length  }}</span>个)</span>
              <span>建议尺寸48px * 48px,拖拽可调整图标位置</span>
            </p> -->
            <!-- 图片 -->
            <!-- <div>
              <draggable v-model="orderCondfig.orderList" :options="{animation:200}">
                <div v-for="(item,index) in orderCondfig.orderList" :key="index" class="draggable-item">
                  <div style="display: flex;align-items: center;" class="order-item">
                    <div>
                      <img src="../../assets/imgs/sixdian.png" alt="">
                    </div>
                    <div class="item-info">
                      <div style="margin-left: 10px;">
                        <el-select placeholder="请选择订单类型" v-model="item.type" size="small">
                          <el-option :label="item.label" :value="item.value" v-for="(item ) in  orderTyleList" :key="item.value"></el-option>
                        </el-select>
                      </div>
                      <div>
                        <div class="img-box" @click="modalPicTap('单选', index)" :style="{ height: `50px`, width: `50px` }">
                          <img :src="item.imgUrl" alt="" v-if="item.imgUrl" />
                          <div class="upload-box" v-else> 
                            <img src='../../assets/images/diy/Default_picture.png' alt="商品图片" class="product-image" />
                          </div>
                        </div>
                        <Input v-model="item.name" style="margin-left: 10px;width: 100%;" />
                      </div>
                      <i class="el-icon-circle-close dle-but" @click="delOrderType(index)" v-if="orderCondfig.orderList && orderCondfig.orderList.length !=1"></i>
                    </div>

                  </div>
                </div>
              </draggable>
              <div class="add-but" @click="addOrderItem" v-if="orderCondfig.orderList && orderCondfig.orderList.length<5">+ 添加</div>
            </div> -->
          </div>
          <!-- <div>
            <p>
              <span>直角/圆角</span>
              <Col class="color-box">
              <RadioGroup v-model="orderCondfig.boxType" type="button">
                <Radio :label="key" v-for="(radio,key) in butTylelist" :key="key">
                  <span class="iconfont-diy" :class="radio.icon" v-if="radio.icon"></span>
                  <span v-else>{{radio.val}}</span>
                </Radio>
              </RadioGroup>
              </Col>
            </p>
            <p>
              <span>圆角值</span>
              <Col span="18" class="slider-box">
              <Slider show-input :max='15' v-model="orderCondfig.boxRoundedVal" :disabled="orderCondfig.boxType == 1 "></Slider>
              </Col>
            </p>
            <div style="display: flex;justify-content: space-between;">
              <span>背景颜色</span>
              <div>
                <div class="but-list color-list" @click="orderUpBoxColor">
                  <p data-type="0" :class="{ 'avt':orderCondfig.backgType == 0 }">纯色</p>
                  <p data-type="1" :class="{ 'avt':orderCondfig.backgType == 1 }">渐变</p>
                </div>
                <div style="display: flex;margin-top: 10px;">
                  <div class="color-item-title" v-if="orderCondfig.backgType == 1">
                    <div class="color-item-title-text" style="width: 75px;">{{ orderCondfig.backgColor1 }}</div>
                    <el-color-picker format="hex" size="mini" v-model="orderCondfig.backgColor1" alpha class="colorPicker-box" style="margin: 0 2px;"></el-color-picker>
                    <div class="reset-btn" style="margin: 0;" @click="orderCondfig.backgColor1 ='#fff'">重置</div>
                  </div>
                  <div class="color-item-title">
                    <div class="color-item-title-text" style="width: 75px;">{{ orderCondfig.backgColor2 }}</div>
                    <el-color-picker format="hex" size="mini" v-model="orderCondfig.backgColor2" alpha class="colorPicker-box" style="margin: 0 2px;"></el-color-picker>
                    <div class="reset-btn" style="margin: 0;" @click="orderCondfig.backgColor2 ='#fff'">重置</div>
                  </div>
                </div>
              </div>
            </div>
          </div> -->
        </template>

        <div class="flex">
          <span class="title">功能模块</span>
          <div @click="queryInfoFalg">
            <!-- <i class="el-icon-arrow-up" v-if="queryFalg"></i>
            <i class="el-icon-arrow-down" v-else></i> -->
          </div>
        </div>
        <template>
          <div class='adv-box'>
            <div class="flex">
              <span>显示/隐藏</span>
              <ASwitch v-model="functionConfig.isShow"></ASwitch>
            </div>
            <div class="flex">
              <span> 标题名称</span>
              <Input v-model="functionConfig.titleName" class="my-input" placeholder="请输入标题名称" />
            </div>
             <div class="flex">
              <span>显示方案</span>
              <div class="flex-cent">
                <div @click="functionConfig.option = 1" class="but" :class="{'but-select': functionConfig.option == 1}">方案一</div>
                <div @click="functionConfig.option = 2" class="but" :class="{'but-select': functionConfig.option == 2}">方案二</div>
              </div>
            </div>
            <!-- <p>
              <span>图标</span>
              <span>建议尺寸48px * 48px,拖拽可调整图标位置</span>
            </p> -->
            <!-- 图片 -->
            <!-- <div>
              <draggable v-model="functionConfig.list" :options="{animation:200}">
                <div v-for="(item,index) in functionConfig.list" :key="index" class="draggable-item">
                  <div style="display: flex;align-items: center;" class="order-item">
                    <div>
                      <img src="../../assets/imgs/sixdian.png" alt="">
                    </div>
                    <div class="item-info">
                      <div style="margin-left: 10px;">
                        <el-select placeholder="请选择功能类型" v-model="item.type" size="small">
                          <el-option :label="item.label" :value="item.value" v-for="(item ) in  typeList" :key="item.value"></el-option>
                        </el-select>
                      </div>
                      <div>
                        <div class="img-box" @click="modalPicTap1('单选', index)" :style="{ height: `50px`, width: `50px` }">
                          <img :src="item.imgUrl" alt="" v-if="item.imgUrl" />
                          <div class="upload-box" v-else> 
                            <img src='../../assets/images/diy/Default_picture.png' alt="商品图片" class="product-image" />
                          </div>
                        </div>
                        <Input v-model="item.name" style="margin-left: 10px;width: 100%;" />
                      </div>
                      <i class="el-icon-circle-close dle-but" @click="delList(index)"></i>
                    </div>

                  </div>
                </div>
              </draggable>
              <div class="add-but" @click="addGList">+ 添加</div>
            </div> -->
          </div>
          <!-- <div>
            <p>
              <span>直角/圆角</span>
              <Col class="color-box">
              <RadioGroup v-model="functionConfig.boxType" type="button">
                <Radio :label="key" v-for="(radio,key) in butTylelist" :key="key">
                  <span class="iconfont-diy" :class="radio.icon" v-if="radio.icon"></span>
                  <span v-else>{{radio.val}}</span>
                </Radio>
              </RadioGroup>
              </Col>
            </p>
            <p>
              <span>圆角值</span>
              <Col span="18" class="slider-box">
              <Slider show-input :max='15' v-model="functionConfig.boxRoundedVal" :disabled="functionConfig.boxType == 1 "></Slider>
              </Col>
            </p>
            <div style="display: flex;justify-content: space-between;">
              <span>背景颜色</span>
              <div>
                <div class="but-list color-list" @click="functionUpBoxColor">
                  <p data-type="0" :class="{ 'avt':functionConfig.backgType == 0 }">纯色</p>
                  <p data-type="1" :class="{ 'avt':functionConfig.backgType == 1 }">渐变</p>
                </div>
                <div style="display: flex;margin-top: 10px;">
                  <div class="color-item-title" v-if="functionConfig.backgType == 1">
                    <div class="color-item-title-text" style="width: 75px;">{{ functionConfig.backgColor1 }}</div>
                    <el-color-picker format="hex" size="mini" v-model="functionConfig.backgColor1" alpha class="colorPicker-box" style="margin: 0 2px;"></el-color-picker>
                    <div class="reset-btn" style="margin: 0;" @click="functionConfig.backgColor1 = '#fff'">重置</div>
                  </div>
                  <div class="color-item-title">
                    <div class="color-item-title-text" style="width: 75px;">{{ functionConfig.backgColor2 }}</div>
                    <el-color-picker format="hex" size="mini" v-model="functionConfig.backgColor2" alpha class="colorPicker-box" style="margin: 0 2px;"></el-color-picker>
                    <div class="reset-btn" style="margin: 0;" @click="functionConfig.backgColor2 = '#fff'">重置</div>
                  </div>
                </div>
              </div>
            </div>
          </div> -->
        </template>

        <div>
        </div>
        <!-- <span class="title">推荐商品</span>  -->
        <!-- <div class="hied">
                <p>
                    <span>商品显示/隐藏</span>
                    <ASwitch @on-change="change" v-model="goodsConfig.goodsShow"></ASwitch> 
                </p>
                <div class="flex-buju" >
                    <span>商品布局</span>
                    <div class="but-list" @click="upType">
                        <p :class="{'avt':layoutType == 1}" data-type="1">一排两个</p>
                        <p :class="{'avt':layoutType == 2}" data-type="2">一排一个</p>
                        <p :class="{'avt':layoutType == 3}" data-type="3">纵向显示</p>
                    </div>
                </div>
            </div> -->
      </div>
    </div>

    <Modal v-model="modalPic" :width="modalWidth" scrollable footer-hide closable title="上传商品图" :mask-closable="false" :z-index="1">
      <uploadPictures :isChoice="isChoice" @getPic="getPic" :gridBtn="gridBtn" :gridPic="gridPic" v-if="modalPic"></uploadPictures>
    </Modal>
  </div>
</template>
  
  <script>
import uploadPictures from "@/components/uploadPictures";
import vuedraggable from 'vuedraggable'
import goodsList from '@/components/goodsList'
import { Switch, Input } from 'iview'
export default {
  name: 'c_personal_center_info',
  props: {
    configObj: {
      type: Object
    }
  },
  components: {
    uploadPictures,
    goodsList,
    'ASwitch': Switch,
    'Input': Input,
    draggable: vuedraggable,
  },
  computed: {
    modalWidth() {
      console.log("window.outerWidth", window.outerWidth);
      if (window.outerWidth <= 1440) {
        console.log(1);
        return "70%";
      }
      console.log(1);
      return "60%";
    }
  },
  watch: {
    configObj: {
      handler(nVal, oVal) {
        this.defaults = nVal
        if (this.defaults && Object.keys(this.defaults).length) {
          this.iconConfig = this.defaults.iconConfig
          this.advConfig = this.defaults.advConfig
          this.orderCondfig = this.defaults.orderCondfig
          this.goodsConfig = this.defaults.goodsConfig
          this.functionConfig = this.defaults.functionConfig
        }
        console.log(this.defaults, '个人信息')
      },
      immediate: true,
      deep: true
    },
    'advConfig.boxType': {
      handler(nVal, oVal) {
        if (nVal == 1) {
          this.advConfig.boxRoundedVal = 0
        } else {
          this.advConfig.boxRoundedVal = 15
        }

      }
    },
    // 订单模块
    "orderCondfig.boxType": {
      handler(nVal, oVal) {
        if (nVal == 1) {
          this.orderCondfig.boxRoundedVal = 0
        } else {
          this.orderCondfig.boxRoundedVal = 15
        }

      }
    },
    'functionConfig.boxType': {
      handler(nVal, oVal) {
        if (nVal == 1) {
          this.functionConfig.boxRoundedVal = 0
        } else {
          this.functionConfig.boxRoundedVal = 15
        }

      }
    },
  },
  data() {
    return {
      queryFalg: true,
      layoutType: 1,
      iconConfig: {},
      advConfig: {},
      orderCondfig: {},
      functionConfig: {},
      goodsList: [],
      defaults: {},
      orderTyleList: [], // 订单类型
      typeList: [],// 功能中心列表
      pageConfig: {},
      goodsConfig: {},
      butTylelist: [
        {
          val: "圆角",
          icon: "iconPic_fillet"
        },
        {
          val: "直角",
          icon: "iconPic_square"
        }
      ],

      isChoice: "单选",
      modalPic: false,
      gridBtn: {
        xl: 4,
        lg: 8,
        md: 8,
        sm: 8,
        xs: 8
      },
      gridPic: {
        xl: 6,
        lg: 8,
        md: 12,
        sm: 12,
        xs: 12
      },
      index: null,
      orderListIndex: '',
      functionIndex: '',
      checkList: [
        { type: 'kq', name: '卡券' },
        { type: 'ye', name: '余额' },
        { type: 'jf', name: '积分' },
        { type: 'sc', name: '收藏' }
      ],
    }
  },
  created() {
    this.defaults = this.configObj
    const orderTyleList = this.$enums.order.orderStatus.ORDER_TYPES
    this.orderTyleList = []
    for (let key in orderTyleList) {
      const item = orderTyleList[key]
      this.orderTyleList.push({
        label: this.$t(item.label),
        value: item.value
      })
    }
    this.getTypeList()
  },
  methods: {
    delOrderType(index) {
      this.orderCondfig.orderList.splice(index, 1)
    },
    delList(index) {
      this.functionConfig.list.splice(index, 1)
    },
    addOrderItem() {
      this.orderCondfig.orderList.push({
        type: '',
        imgUrl: '',//图标
        name: '',
      })
    },
    modalPicTap(type, index) {
      console.log('index', index)
      this.modalPic = true;
      this.orderListIndex = index
      this.functionIndex = ''
    },
    modalPicTap1(type, index) {
      console.log('index11', index)

      this.modalPic = true;
      this.orderListIndex = ''
      this.functionIndex = index
    },
    // 获取图片信息
    getPic(pc) {
      this.$nextTick(() => {
        console.log('pcpcpc', pc.att_dir)
        if (typeof this.orderListIndex == 'number') {
          this.orderCondfig.orderList[this.orderListIndex].imgUrl = pc.att_dir
        } else {
          this.functionConfig.list[this.functionIndex].imgUrl = pc.att_dir
        }
        this.modalPic = false;
      });
    },
    resetBgB() {
      this.iconConfig.backgColor1 = '#ffffff'
    },
    resetBgC() {
      this.iconConfig.backgColor2 = '#ffffff'
    },
    queryInfoFalg() {
      this.queryFalg = !this.queryFalg
    },
    UpPersonalInfoType(event) {
      const type = event.target.dataset.type

      if ([1, 2, 3].includes(Number(type))) {
        this.iconConfig.styleType = Number(type)
      }
    },
    upBoxColor(event) {
      const type = event.target.dataset.type
      if ([0, 1].includes(Number(type))) {
        this.iconConfig.backgType = Number(type)
      }
    },
    orderUpBoxColor(event) {
      const type = event.target.dataset.type
      if ([0, 1].includes(Number(type))) {
        this.orderCondfig.backgType = Number(type)
      }
    },
    functionUpBoxColor(event) {
      const type = event.target.dataset.type
      if ([0, 1].includes(Number(type))) {
        this.functionConfig.backgType = Number(type)
      }
    },
    upBoxColor2(event) {
      const type = event.target.dataset.type
      // 验证类型有效性（防止点击到父元素其他区域）
      if ([0, 1].includes(Number(type))) {
        this.advConfig.backgType = Number(type)
      }
    },
    upType(event) {
      const type = Number(event.target.dataset.type);

      // 验证类型有效性（防止点击到父元素其他区域）
      if ([1, 2, 3].includes(type)) {
        this.layoutType = type;
        console.log("当前布局类型：", this.layoutType);
        this.goodsConfig.layoutType = type
      }
    },
    addGList() {
      this.functionConfig.list.push({
        type: '',
        imgUrl: '',//图标
        name: '',
      })
    },
    // 获取插件
    async getTypeList() {
      const list = this.$enums.DIY.DIYfunctZone.funCenterByType(this)
      list.forEach((item, key) => {
        this.typeList.push({
          label: item.value,
          value: item.type
        })
      })
      // await this.LaiKeCommon.select({
      //     api: 'plugin.template.Admin.getPluginStatus', 
      // }) 

    },
    change() {

    }

  }
}
  </script>
  
  <style scoped lang="less">
.add-but {
  width: 100%;
  height: 38px;
  background: #fcfcfc;
  border-radius: 4px 4px 4px 4px;
  border: 1px solid #d5dbe8;
  text-align: center;

  color: #7d869c;
  font-weight: normal;
  line-height: 14px;

  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}
.order-item {
  background-color: #f4f7f9;
  border-radius: 6px;
  margin: 10px 0;
  padding: 10px;

  .item-info {
    position: relative;
    flex: 1;
    > div:nth-child(2) {
      margin: 10px;
      padding: 10px;
      margin-right: 0px;
      margin-top: 0px;
      display: flex;
      align-items: center;
      background: #fff;
      border-radius: 6px;
    }
    /deep/ .el-select {
      width: 100%;
      margin-bottom: 10px;
    }
  }
}
.flex-cent{
  display: flex;
  justify-content: center;
  align-items: center;
}
.flex,
.flex-buju {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
i {
  cursor: pointer;
}
.title {
  font-family: MicrosoftYaHei;
  font-size: 14px;
  font-weight: normal;
  line-height: 14px;
  letter-spacing: 0em;

  color: #414658 !important;
}
.avt,
.avitive {
  border: 1px solid #2d8cf0;
  color: #2d8cf0;
}
.personal-info-box {
  background-color: #f4f7f9;
  padding: 10px;
  border-radius: 4px;
}
.adv-box {
  > div {
    margin: 10px 0;
  }
}
.hied {
  p {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin: 10px 0;
  }
  > div {
    margin: 10px 0;
  }
  span {
    user-select: none;
    font-family: MicrosoftYaHei;
    font-size: 14px;
    font-weight: normal;
    line-height: 14px;
    letter-spacing: 0em;

    color: #7d869c;
  }
  .my-input {
    width: 176px;
    height: 32px;
    border-radius: 4px;
    opacity: 1;
  }
}
.but{
  border:1px solid #d5dbe8;
  border-radius:4px;
  padding:3px 9px;
  font-size: 12px;
  margin-left:10px;
  cursor:pointer;
}
.but-select{
  border:1px solid #2d8cf0 !important;
  color:#2d8cf0 !important;
}
.color-item-title {
  display: flex;
  align-items: center;
  .colorPicker-box {
    margin: 0 6px;
  }
  .color-item-title-text {
    width: 98px;
    height: 32px;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 8px 10px;
    background: #fcfcfc;
    border-radius: 4px 4px 4px 4px;
    border: 1px solid #d5dbe8;
  }
}
.dle-but {
  position: absolute;
  top: -15px;
  right: -15px;
  font-size: 20px;
  color: #333;
  cursor: pointer;
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
  