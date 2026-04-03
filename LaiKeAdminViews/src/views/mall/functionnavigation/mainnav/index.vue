<template>
  <div class="container">
    <div
      v-for="(item, index) in button_list"
      :key="index"
      @click="$router.push('/mall/functionnavigation/editornav')"
      style="height:20px;"
    >
      <span class="editor-nav" v-if="item.title == '编辑导览'">{{
        $t("functionnavigation.bjdl")
      }}</span>
    </div>
    <div class="nav-content">
      <div class="operation-prompt">
        <div class="logo">
          <img src="@/assets/imgs/czts.png" alt="" />
          <span>{{ $t("functionnavigation.czts") }}</span>
        </div>
        <div class="prompt-content">
          <p>① {{ $t("functionnavigation.zptqx") }}</p>
          <p>② {{ $t("functionnavigation.djysj") }}</p>
        </div>
      </div>
      <div class="nav-entrance">
        <div class="plug-in" v-for="(item, index) in navList" :key="index">
          <div class="plug-container" v-if="item.list.length !== 0">
            <h2>{{ item.title }}</h2>
            <div class="plug-nav">
              <div
                class="card"
                v-for="(items, index) in item.list"
                :key="index"
                @click="$router.push(`${items.menuPath}`)"
              >
                <div class="img">
                  <img :src="items.image" alt="" @error="handleErrorImg"/>
                </div>
                <div class="introduce">
                  <span>{{ items.title }}</span>
                  <span>{{ items.introduce }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { index } from "@/api/mall/functionNavigation";
import { getStorage } from "@/utils/storage";
import { getButton } from "@/api/layout/information";
import ErrorImg from '@/assets/images/default_picture.png'

export default {
  name: "mainnavv",

  data() {
    return {
      navList: [],
      button_list: [],
      menuId: "",
    };
  },

  created() {
    this.indexs();
    this.getButtons();
  },
  methods: {
    // 图片错误处理
    handleErrorImg(e){
      console.log('图片报错了',e.target.src);
      e.target.src=ErrorImg
    },
    //获取按纽权限
    async getButtons() {
      let route = getStorage("route");
      route.forEach((item) => {
        if (item.path == "functionnavigation") {
          return (this.menuId = item.id);
        }
      });
      let buttonList = await getButton({
        api: "saas.role.getButton",
        token: getStorage("laike_admin_userInfo").token,
        storeId: getStorage("rolesInfo").storeId,
        menuId: this.menuId,
      });
      this.button_list = buttonList.data.data;
      console.log(this.button_list, "获取按纽权限");
    },
    async indexs() {
      let { entries } = Object;
      let data = {
        api: "admin.overview.index",
      };
      let formData = new FormData();
      for (let [key, value] of entries(data)) {
        formData.append(key, value);
      }
      const res = await index(formData);
      console.log(res);
      this.navList = res.data.data.list;
    },
  },
};
</script>

<style scoped lang="less">
.container {
  width: 100%;
  height: 737px;
  overflow: hidden;
  overflow-y: auto;
  .editor-nav {
    width: 96px;
    height: 40px;
    line-height: 40px;
    text-align: center;
    border: 1px solid #2890ff;
    border-radius: 4px;
    position: fixed;
    // top: 70px;
    right: 30px;
    cursor: pointer;
    /* span { */
    font-size: 14px;
    font-weight: 400;
    color: #2890ff;
    /* } */
  }

  .nav-content {
    .operation-prompt {
      width: 100%;
      height: 114px;
      background: #e9f4ff;
      border: 1px dashed #2890ff;
      border-radius: 4px;
      padding: 17px 0 0 17px;
      .logo {
        display: flex;
        align-items: center;
        margin-bottom: 10px;
        img {
          width: 20px;
          height: 20px;
        }
        span {
          margin-left: 4px;
          font-size: 14px;
          font-weight: bold;
          color: #2890ff;
        }
      }
      .prompt-content {
        padding-left: 23px;
        p {
          color: #6a7076;
          &:not(:first-child) {
            margin-top: 8px;
          }
        }
      }
    }
    .nav-entrance {
      width: 100%;
      .plug-in {
        width: 100%;
        .plug-container {
          h2 {
            margin: 20px 0;
            font-size: 16px;
            font-weight: 400;
            color: #414658;
          }
          .plug-nav {
            width: 100%;
            display: grid;
            justify-content: space-between;
            grid-template-columns: repeat(auto-fill, 306px);
            // grid-gap: 30px;
            .card {
              width: 306px;
              height: 100px;
              background-color: #fff;
              border-radius: 4px;
              margin-bottom: 30px;
              display: flex;
              align-items: center;
              padding: 0 24px;
              &:hover {
                box-shadow: 0px 0px 24px 0px rgba(38, 41, 52, 0.14);
              }
              .img {
                width: 64px;
                height: 64px;
                margin-right: 20px;
                img {
                  width: 64px;
                  height: 64px;
                }
              }
              .introduce {
                height: 64px;
                display: flex;
                flex-direction: column;
                justify-content: space-between;
              }
            }
          }
        }
      }
    }
  }
}
</style>
