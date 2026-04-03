<template>
  <div :class="classObj" class="app-wrapper" v-loading="$store.getters.loading">
    <div
      v-if="device === 'mobile' && sidebar.opened"
      class="drawer-bg"
      @click="handleClickOutside"
    />
    <div class="big-header" :class="{ 'fixed-header': fixedHeader }">
      <navbar />
    </div>
    <div class="main-container">
      <div class="main-nav">
        <SideBar />
      </div>
      <app-main />
    </div>
    <div class="footer">
      <!-- <div class="left-location">
        <span >联系地址：{{websiteInformation.contact_address}} 联系电话：{{ websiteInformation.contact_number }}</span>
      </div> -->
      <div class="left-location">
<!--        <span v-if="websiteInformation.contact_address"
          >联系地址：{{ websiteInformation.contact_address }}</span
        >
        &nbsp;
        <span v-if="websiteInformation.contact_number"
          >联系电话：{{ websiteInformation.contact_number }}</span
        >-->
      </div>
      <div class="right-company">
        <div class="company">
          <a href="javascript:;">{{
            copyright_information
          }}</a>
        </div>
        <div class="archival-information">
          <a href="javascript:;">{{record_information }}</a>
        </div>
      </div>
    </div>

    <!-- <div class="mask" v-if="showVersion && versionUpdate">
      <div class="mask-content">
        <img src="../assets/imgs/update_top.png" class="mask_bg" alt="">
        <img src="../assets/imgs/close.png" class="close-img" @click="closeVersion" alt="">
        <h1>版本升级通知</h1>
        <div class="title">版本升级</div>
        <div class="content">
          <p v-html="content"></p>
        </div>
      </div>
    </div> -->

    <div class="mask" v-if="showVersion && versionUpdate">
      <div class="mask-content">
        <!-- <img src="../assets/imgs/update_top.png" class="mask_bg" alt="">
        <img src="../assets/imgs/close.png" class="close-img" @click="closeVersion" alt="">
        <h1>版本升级通知</h1>
        <div class="title">版本升级</div>
        <div class="content">
          <p v-html="content"></p>
        </div> -->
        <div class="margin-left">
          <img src="../assets/imgs/bg_1314.png" class="mask_bg" alt="" />
        </div>
        <div class="margin-right">
          <h1>{{ systemMsgTitle }}</h1>
          <div class="title-content">
            <h2>公告内容</h2>
            <div class="content">
              <p v-html="content"></p>
            </div>
          </div>
          <div
          v-if="systemMsgType!=1"
            style="
              width: 312px;
              display: flex;
              justify-content: space-between;
              align-items: center;
            "
          >
            <el-checkbox v-model="checked">不再提醒</el-checkbox>
            <el-button @click="closeVersion" type="primary" style="width: 113px"
              >我知道了</el-button
            >
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import  { Navbar, AppMain } from "./components";
import SideBar from "./components/SideBar/index";
import ResizeMixin from "./mixin/ResizeHandler";
import { getStorage, removeStorage, setStorage } from "@/utils/storage";
import { notGG } from "@/api/layout/information";
import request from '@/api/https'
export default {
  name: "Layout",
  data() {
    return {
      checked: false,
      showVersion: true,
      versionUpdate: false,
      content: "",
      systemMsgTitle: "",
      systemMsgType: 0,
      copyright_information: '',//版权信息
      record_information: '',//备案信息
    };
  },
  components: {
    Navbar,
    SideBar,
    AppMain,
  },
  mixins: [ResizeMixin],
  watch: {
    $route: {
      handler(){
        notGG({
        api: "admin.saas.user.getUserTell",
      }).then((res) => {
        console.log("查看是否在那个维护公告中", res);
        if (res.data.code == 200 && res.data.data.systemMsgType == 1) {
          this.versionUpdate = true;
          this.showVersion = true
          this.content = res.data.data.systemMsg;
          this.systemMsgTitle = res.data.data.systemMsgTitle;
          this.systemMsgType = res.data.data.systemMsgType;
        } else {
          this.versionUpdate = getStorage("versionUpdate");
          this.content = getStorage("laike_admin_userInfo").systemMsg;
          this.systemMsgTitle = getStorage(
            "laike_admin_userInfo"
          ).systemMsgTitle;
          this.systemMsgType = getStorage(
            "laike_admin_userInfo"
          ).systemMsgType;
        }
      });
        console.log("当前路由发生了改变");
      },
      immediate : true
    },
  },
  computed: {
    sidebar() {
      return this.$store.state.app.sidebar;
    },
    device() {
      return this.$store.state.app.device;
    },
    fixedHeader() {
      return this.$store.state.settings.fixedHeader;
    },
    classObj() {
      return {
        hideSidebar: !this.sidebar.opened,
        openSidebar: this.sidebar.opened,
        withoutAnimation: this.sidebar.withoutAnimation,
        mobile: this.device === "mobile",
      };
    },
    websiteInformation() {
      return getStorage("website_information") || {};
    },
    // versionUpdate() {
    //   return getStorage("versionUpdate");
    // },
    // content() {
    //   return getStorage("laike_admin_userInfo").systemMsg;
    // },
    // systemMsgTitle() {
    //   return getStorage("laike_admin_userInfo").systemMsgTitle;
    // },
  },
  created() {
    this.handleGG();
    this.config();
  },
  methods: {
    // 查看是否在那个维护公告中
    // admin.saas.user.getUserTell
    handleGG() {
      // admin.saas.user.getUserTell
      notGG({
        api: "admin.saas.user.getUserTell",
      }).then((res) => {
        console.log("查看是否在那个维护公告中", res);
      });
    },
    config(){
      return request({
        method: "post",
        params: {
          api: "admin.system.GetBasicConfiguration",
        },
      }).then((res) => {
        if (res.data.code == "200") {
          let info = res.data.data.list
          this.copyright_information = info.copyright_information
          this.record_information = info.record_information
        }
      });
    },
    handleClickOutside() {
      this.$store.dispatch("app/closeSideBar", { withoutAnimation: false });
    },
    closeVersion() {
      this.showVersion = false;
      // removeStorage('versionUpdate')
      if (this.checked) {
        console.log("调用接口");
        notGG({
          api: "admin.saas.user.markToRead",
          tell_id: getStorage("laike_admin_userInfo").tell_id,
        });
      }
      setStorage("versionUpdate", false);
    },
  },
};
</script>

<style lang="scss" scoped>
@import "~@/styles/mixin.scss";
@import "~@/styles/variables.scss";

.app-main {
  padding-bottom: 20px !important;
}
.sidebar-container {
  padding-top: 60px;
  padding-bottom: 40px;
}
.big-header {
  position: fixed;
  top: 0;
  left: 0;
  z-index: 99 !important;
  width: 100% !important;
}

.main-nav {
  // width: 220px;
  width: 70px;
  height: calc(100vh - 100px);
  position: fixed;
  left: 0;
  top: 60px;
  // z-index: 100;
  z-index: 1999;
}
.app-wrapper {
  @include clearfix;
  position: relative;
  height: 100%;
  width: 100%;
  background-color: #edf1f5;
  &.mobile.openSidebar {
    position: fixed;
    top: 0;
  }

  .footer {
    position: fixed;
    bottom: 0;
    left: 0;
    width: 100%;
    height: 40px;
    background-color: #222;
    display: flex;
    justify-content: space-between;
    align-items: center;
    z-index: 3010;
    padding: 0 20px;
    color: #ddd;
    font-size: 14px;
    span {
      color: rgba(255, 255, 255, 0.7);
    }
    .right-company {
      display: flex;
      font-size: 14px;
      .company {
        margin-right: 30px;
        cursor: pointer;
        a {
          color: rgba(255, 255, 255, 0.7);
          &:hover {
            color: #2d8cf0;
          }
        }
      }

      .archival-information {
        cursor: pointer;
        a {
          color: #dddddd;
          &:hover {
            color: #2d8cf0;
          }
        }
      }
    }
  }
}
.drawer-bg {
  background: #000;
  opacity: 0.3;
  width: 100%;
  top: 0;
  height: 100%;
  position: absolute;
  z-index: 999;
}

// .fixed-header {
//   position: fixed;
//   top: 0;
//   right: 0;
//   z-index: 9;
//   width: calc(100% - #{$sideBarWidth});
//   transition: width 0.28s;
// }

.hideSidebar .fixed-header {
  width: calc(100% - 70px);
}

.fixed-header {
  padding: 0 !important;
}

.mobile .fixed-header {
  width: 100%;
}

.mask {
  position: fixed;
  z-index: 99999;
  top: 0;
  bottom: 0;
  left: 0;
  right: 0;
  background: rgba(0, 0, 0, 0.6);
  display: block;
  .mask-content {
    display: flex;
    top: 50% !important;
    background: none;
    width: 1140px;
    height: auto !important;
    margin: 0 auto;
    position: absolute;
    left: 50%;
    transform: translate(-50%, -50%);
    border-radius: 12px;
    .margin-left {
      width: 667px;
      height: 636px;
      padding: 0;
      // background-color: pink;
      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
        margin: 0;
      }
    }
    .margin-right {
      flex: 1;
      background-color: #fff;
      padding-left: 75px;
      border-radius: 0 12px 12px 0;
      h1 {
        margin-top: 85px;
        // text-align: center;
        color: #333333;
        font-size: 32px;
      }
      .title-content {
        height: 350px;
        width: 322px;
        padding-top: 56px;
        margin-bottom: 40px;

        .content {
          height: 267px;
          overflow-y: scroll;
          p > p > img {
            width: 100% !important;
            // object-fit: cover;
          }
        }
        h2 {
          font-size: 18px;
          margin-bottom: 12px;
          color: #333;
        }
        p {
          color: #666;
          font-size: 16px;
          word-wrap: break-word;
        }
      }
    }
  }
}
</style>
