<template>
  <div class="hot_imgs">
    <div class="title" v-if="configData.title">
      <!-- 轮播图组件 新布局-->
        <template v-if="defaults.name == 'swiperBg'">
          <div class="swiperbg-title">
            <span>图片({{configData.list.length}}/6)</span>
            <span> {{ configData.title }}</span>
          </div>
        </template>
         <!--使用原来的 显示布局 -->
        <template v-else>
          {{ configData.title }}
        </template>
    </div>
    <div class="list-box">
      <draggable
        class="dragArea list-group"
        :list="configData.list"
        group="peoples"
        handle=".move-icon"
      >
        <div class="item" v-for="(item, index) in configData.list" :key="index">
          <div class="move-icon" v-if="canMove">
            <span class="iconfont-diy icondrag"></span>
          </div>
          <div
            class="img-box"
            @click="modalPicTap('单选', index)"
            :style="{ height: `${imgHeight}px`, width: `${imgWidth}px` }"
          >
            <img :src="item.img" alt="" v-if="item.img" />
            <div class="upload-box" v-else>
              <Icon type="ios-camera-outline" size="22" />
            </div>
            <div
              class="delect-btn"
              @click.stop="bindDelete(item, index)"
              v-if="canDelete && configData.list.length > 1"
            >
              <span class="iconfont-diy icondel_1"></span>
            </div>
          </div>
          <div class="info">
            <div class="info-item" v-for="(infos, key) in item.info" :key="key">
              <!-- 自定义 -->
              <span>{{ infos.title }}</span>
              <div class="input-box">
                <div v-if="infos.title === '链接'" class="input-box-select">
                  <Select class="select-link-type" v-model="infos.link_type" @on-change="getList">
                    <Option value="product" key="product">商品</Option>
                    <Option value="fenlei" key="fenlei">分类</Option>
                    <Option value="dianpu" key="dianpu">店铺</Option>
                    <Option value="diy" key="diy">自定义</Option>
                  </Select>

                  <Input
                    v-model="infos.value"
                    v-if="infos.link_type === 'diy'"
                    @on-change="bindCustomLink(infos,index,key)"
                  ></Input>

                  <Select
                    class="select-box"
                    v-model="infos.value"
                    filterable
                    clearable
                    v-if="infos.link_type === 'product'"
                  >
                    <Option
                      v-for="item in product_links"
                      :value="`/pagesC/goods/goodsDetailed?toback=true&pro_id=${item.sid}`"
                      :key="item.id"
                      >{{ item.name }}</Option
                    >
                  </Select>

                  <Select
                    class="select-box"
                    v-model="infos.value"
                    filterable
                    clearable
                    v-if="infos.link_type === 'fenlei'"
                  >
                    <Option
                      v-for="item in feilei_links"
                      :value="`/pagesC/goods/goods?cid=${item.sid}`"
                      :key="item.id"
                    >{{ item.name }}</Option
                    >
                  </Select>

                  <Select
                    class="select-box"
                    v-model="infos.value"
                    filterable
                    clearable
                    v-if="infos.link_type === 'dianpu'"
                  >
                    <Option
                      v-for="item in dianpu_links"
                      :value="`/pagesB/store/store?shop_id=${item.sid}`"
                      :key="item.id"
                    >{{ item.name }}</Option
                    >
                  </Select>
                </div>

                <Input
                  v-else
                  v-model="infos.value"
                  :placeholder="infos.tips"
                  :maxlength="infos.max"
                />
              </div>
            </div>
          </div>
        </div>
      </draggable>
      <div>
        <Modal
          v-model="modalPic"
          :width="modalWidth"
          scrollable
          footer-hide
          closable
          title="上传商品图"
          :mask-closable="false"
          :z-index="1"
        >
          <uploadPictures
            :isChoice="isChoice"
            @getPic="getPic"
            :gridBtn="gridBtn"
            :gridPic="gridPic"
            v-if="modalPic"
          ></uploadPictures>
        </Modal>
      </div>
    </div>
    <template v-if="configData.list && configData.list.length<6">
      <div class="add-btn" v-if="configData.list.length < configData.maxList">
        <Button
          type="primary"
          ghost
          style="width: 100%; height: 40px; border-color: #D8D8D8; color: #414658;"
          @click="addBox"
          ><i class="el-icon-plus"></i> 添加
        </Button>
      </div>
    </template>
  </div>
</template>

<script>
import vuedraggable from "vuedraggable";
import uploadPictures from "@/components/uploadPictures";
import { mapState,mapActions, mapMutations} from "vuex";
import { bannerPathList } from '@/api/plug_ins/template'
export default {
  name: "c_menu_list",
  props: {
    configObj: {
      type: Object
    },
    configNme: {
      type: String
    },
    index: {
      type: null
    },
    canDelete: {
      type: Boolean,
      default: true
    },
    canMove: {
      type: Boolean,
      default: true
    },
    imgWidth: {
      type: Number,
      default: 80
    },
    imgHeight: {
      type: Number,
      default: 80
    }
  },
  components: {
    draggable: vuedraggable,
    uploadPictures
  },
  data() {
    return {
      defaults: {},
      configData: {},
      menus: [],
      list: [
        {
          title: "aa",
          val: ""
        }
      ],
      modalPic: false,
      isChoice: "单选",
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
      activeIndex: 0,
      lastObj: {}
    };
  },
  computed: {
    ...mapState({
      product_links: state => state.admin.mobildConfig.product_links,
      feilei_links: state => state.admin.mobildConfig.feilei_links,
      dianpu_links: state => state.admin.mobildConfig.dianpu_links,
      delLinkList: state => state.admin.mobildConfig.delLinkList,
    }),
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
  mounted() {

    this.$nextTick(() => {
      console.log("mounted",this.configObj);
      this.defaults = this.configObj;
      if(Object.keys(this.configObj).length ==0 )return
      // this.configData = Object.assign({}, this.configObj[this.configNme]);
      if(this.configObj[this.configNme]){
        this.configData = this.configObj[this.configNme]
      }else{
        this.configData = this.defaults[this.defaults.link_key][this.configNme]
      }

      // if (this.configData && this.configData.list) {
      //   for (let [index, item] of this.configData.list.entries()) {
      //     for (let [key, infos] of item.info.entries()) {
      //       if (infos.title === "链接") {
      //         if (!infos.hasOwnProperty("link_type")) {
      //           infos.link_type = "system";
      //         }
      //       }
      //     }
      //   }
      // }
    });

  },
  watch: {
    configObj: {
      handler(nVal, oVal) {
        this.defaults = nVal;
        // this.configData = nVal[this.configNme];
         if( this.configObj[this.configNme]){
            this.configData = nVal[this.configNme]
          }else{
            this.configData = nVal[nVal.link_key][this.configNme]
          }


      },
      deep: true
    },
    configData: {
      handler(nVal, oVal) {
        console.log("configData", nVal);
        this.configData.list.forEach(v=>{
            this.$set(v,'is_img',true)
            v.info.forEach(item=>{
              if(!item.lodValue){
                this.$set(item,'lodValue',item.value)
              }
            })
          })
      },
      deep: true
    }

  },
  methods: {
    ...mapActions('admin/mobildConfig',[
      'bindPages',
    ]),
    ...mapMutations('admin/mobildConfig',['SET_DELLINKLIST']),
    async getList (e) {
      console.log(e);
      if(e != 'diy') {
        const res = await bannerPathList({
          // api: 'admin.diy.bannerPathList',
          api: 'plugin.template.AdminDiy.BannerPathList',
          type: e == 'product' ? 2 : e == 'fenlei' ? 1 : 3
        })
        console.log(res);
        if(e == 'product') {
          this.$store.state.admin.mobildConfig.product_links = res.data.data.list
        } else if(e == 'fenlei') {
          this.$store.state.admin.mobildConfig.feilei_links = res.data.data.list
        } else if(e == 'dianpu') {
          this.$store.state.admin.mobildConfig.dianpu_links = res.data.data.list
        }
      }

    },
    addBox() {
      console.log('我添加了',this.configData.list)
      if (this.configData.list.length == 0) {
        const time = new Date().getTime()
        this.$set(this.lastObj,'unit',`unit-${time}`)
        this.configData.list.push(this.lastObj);

      } else {
        const obj = JSON.parse(
          JSON.stringify(this.configData.list[this.configData.list.length - 1])
        );
        console.log(obj)
        const time = new Date().getTime()
        this.$set(obj,'unit',`unit-${time}`)
        obj.info.forEach((v,index)=>{
          v.value = ''
          v.lodValue = ''
          if(index == 1){
            v.unit = obj.unit
          }
          })
        this.configData.list.push(obj);
        console.log(this.configData.list)
      }
    },
    // 点击图文封面
    modalPicTap(title, index) {
      this.activeIndex = index;
      this.modalPic = true;
    },
    // 添加自定义弹窗
    addCustomDialog(editorId) {
      window.UE.registerUI(
        "test-dialog",
        function(editor, uiName) {
          const dialog = new window.UE.ui.Dialog({
            iframeUrl: "/admin/widget.images/index.html?fodder=dialog",
            editor: editor,
            name: uiName,
            title: "上传图片",
            cssRules: "width:1200px;height:500px;padding:20px;"
          });
          this.dialog = dialog;
          // 参考上面的自定义按钮
          var btn = new window.UE.ui.Button({
            name: "dialog-button",
            title: "上传图片",
            cssRules:
              "background-image: url(../../../assets/images/icons.png);background-position: -726px -77px;",
            onclick: function() {
              // 渲染dialog
              dialog.render();
              dialog.open();
            }
          });

          return btn;
        },
        37
      );
    },
    // 获取图片信息
    getPic(pc) {
      this.$nextTick(() => {
        this.configData.list[this.activeIndex].img = pc.att_dir;

        this.$set(this.configData.list[this.activeIndex],'relative_img',pc.image_name)
        console.log('this.configData.list[this.activeIndex]',this.configData.list[this.activeIndex])
        this.modalPic = false;
      });
    },
    // 删除
    bindDelete(item, index) {
      console.log('bindDelete',index)
      const defaultArray = JSON.parse(JSON.stringify(this.$store.state.admin.mobildConfig.defaultArray))
      const currentAttr  = defaultArray[this.configObj.link_key]
      let targetConfig;
      if (currentAttr.swiperConfig) {
        targetConfig = currentAttr.swiperConfig;
      } else if (currentAttr.menuConfig) {
        targetConfig = currentAttr.menuConfig;
      } else if (currentAttr.advConfig) {
        targetConfig = currentAttr.advConfig;
      }

        const obj = targetConfig.list[index]
           obj.info.forEach(v=>{
              if(v.unit){
                  this.delLinkList.push({
                    isDelete:true,
                    link:currentAttr.link_key,
                    unit:v.unit,
                    lodValue:v.lodValue,
                    value:v.value
                  })
                  console.log(this.delLinkList)
                  this.SET_DELLINKLIST(this.delLinkList)
                }
              })

      console.log('我被删除了',this.configData,defaultArray)
      if (this.configData.list.length == 1) {
        this.lastObj = this.configData.list[0];
      }
      this.configData.list.splice(index, 1);



    },
    bindCustomLink(item){
      const time = new Date().getTime()
      console.log(item)
      console.log('configData',this.configData)
      console.log('configNme',this.configNme)
      if(!item.unit){
        this.$set(item,'unit',`unit-${time}`)
      }

      this.bindPages({
        diyId:this.$route.query.id,// 主題 diy
        link:item.value,
        lodValue:item.lodValue, // 原始数据
        linkKey:this.defaults.link_key, //组件id
        unit: item.unit, // 组件唯一标识
        typeIndex:this.$route.query.typeIndex
      })
    }
  }
};
</script>

<style scoped lang="stylus">
.hot_imgs
  margin-bottom 20px
  border-top 1px solid rgba(0, 0, 0, 0.05)
  border-bottom: 1px solid #E9ECEF
  padding-bottom: 16px

  .title
    padding 13px 0
    color #999
    font-size 12px

  .list-box
    .item
      position relative
      display flex
      margin-top 20px

      .move-icon
        display flex
        align-items center
        justify-content center
        width 30px
        height 80px
        cursor move

      .img-box
        position relative
        width 80px
        height 80px

        img
          width 100%
          height 100%

      .info
        flex 1
        margin-left 22px

        .info-item
          display flex
          align-items center
          margin-bottom 10px

          span
            width 40px
            font-size 13px

          .input-box
            flex 1
            .input-box-select
              display flex
              .select-link-type
                width 100px
                margin-right: 6px
                >>> .ivu-select-dropdown {
                  top 74px !important
                  left  172px !important;
                }
              .select-box {
                >>> .ivu-select-dropdown {
                  top 74px !important
                  left  247px !important;
                  width 152px !important;
                }
              }


      .delect-btn
        position absolute
        right: -10px;
        top: -15px;

        .iconfont-diy
          font-size 22px
          color #999

  .add-btn
    margin-top 10px

.upload-box
  display flex
  align-items center
  justify-content center
  width 100%
  height 100%
  border-radius: 4px
  background: #f2f2f2

.iconfont-diy
  color #DDDDDD
  font-size 28px

.swiperbg-title
  display flex
  align-items center
  justify-content space-between

  span:first-child {
    font-family: MicrosoftYaHei, MicrosoftYaHei
    font-size: 14px
    color: #7D869C
    line-height: 14px
    text-align: left
  }
  span:last-child {
    font-family: MicrosoftYaHei, MicrosoftYaHei
    font-weight: normal
    font-size: 12px
    color: #C4CAD8
    line-height: 12px
  }
</style>
