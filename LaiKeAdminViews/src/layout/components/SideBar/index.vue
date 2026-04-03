<template>
  <div class="mainSideBar">
    <div class="left-nav">
      <ul class="side-nav">
        <li
          class="side-nav-item"

          v-for="(route, index) in routes"
          :key="index"
          @mouseenter="mouseenter(route)"
          @mouseleave="mouseleave"
        >
          <div
            class="nav-maininfo"
         :class="[{ active: activeName === route.name }]"
            @click="change(route)"
          >
            <div
              class="icon-font"
              @click="handleSelect(route)"
            >
              <img
                :src="
                  activeName === route.name || toggle === route.name
                    ? route.meta.icon[1]?route.meta.icon[1]:ErrorImg
                    : route.meta.icon[0]?route.meta.icon[0]:ErrorImg
                "
                alt=""
                @error="handleErrorImg"
              />
              <!-- <img :src="ErrorImg" alt=""> -->
              <span :class="'parentNode'+index">{{ route.meta.title }}</span>
            </div>
            <i v-show="toggle === route.name" class="icon"></i>
            <!-- <span v-if="route.meta.title === '订单'" class="red-dot"></span> -->
          </div>

          <ul class="nav-info">
            <li
              v-for="(child, index) in route.children"
              :key="index"
              @click="
                handleSelect(`${route.name}/${child.path}`, child);
                change(route);
              "
              v-show="child.meta.is_core == 1"
            >
              <span
                :class="{ active: child.name === $route.path.split('/')[2] }"
              >
                {{ generateTitle(child.meta.title) }}
                <i class="orderListNum" v-if="child.meta.title === '订单列表' && Number(orderListNum || 0) >0"
                  >({{ orderListNum }})</i
                >
                <i
                  class="salesReturnListNum"
                  v-if="child.meta.title === '退货列表'"
                  Number(refundListNum || 0) >0
                  >({{ refundListNum }})</i
                >
              </span>
            </li>
          </ul>
        </li>
      </ul>
    </div>
    <div class="right-nav" :class="classObj">
      <SideItem
        class="itemBar"
        :mainNav="mainNav"
        :class="classobj"
        :item="navList"
      />
    </div>
  </div>
</template>
<script>
import SideItem from "./sideItem";
import { mapGetters } from "@/store";
import { getStorage, setStorage, removeStorage } from "@/utils/storage";
import request from "@/api/https";
import { isEmpty } from "element-ui/src/utils/util";
import { isExternal } from "@/utils/validate";
import Layout from "@/layout/index";
import router from "@/router";
import { orderCount } from "@/api/plug_ins/preSale";
import ErrorImg from "@/assets/images/menu_icon.png";

export default {
  name: "mainSideBar",
  components: {
    SideItem,
  },
  data() {
    return {
      activeName: "home",
      toggle: "home",
      navList: [
        {
          path: "homeReport",
          name: "homeReport",
          meta: { title: "商城首页", is_core: 1 },
        },
      ],
      mainNav: "home",
      zcList: [],
      zcPath: "",
      ErrorImg:ErrorImg
    };
  },
  created() {

    if (
      this.$store.state.permission.addRoutes.some((item) => {
        return item.name == "home";
      })
    ) {
      // debugger
      if (this.$route.path.split("/")[1] !== "home") {
        this.$router.replace("/home/homeReport");
      }
      this.$store.state.permission.addRoutes.map((item) => {
        if (item.name == "home") {
          this.navList = item.children;
        }
      });
    } else {
      let routes = this.$store.state.permission.addRoutes[0];
      this.activeName = routes.name;
      this.toggle = routes.name;
      this.navList = routes.children;
      this.mainNav = routes.name;
      if (this.$route.path.split("/")[1] !== routes.name) {
        this.$router.replace(routes.redirect);
      }
    }
  },

  computed: {
    item() {
      return this.routes[0].children;
    },
    routes() {
      return this.$store.getters.permission_routes.filter((item) => {
        if (item.meta) {
          return item;
        }
      });
    },
    activeMenu() {
      const route = this.$route;
      const { meta, path } = route;
      // if set path, the sidebar will highlight the path you set
      if (meta.activeMenu) {
        return meta.activeMenu;
      }
      return path;
    },
    sidebar() {
      return this.$store.state.app.sidebar;
    },
    classObj() {
      return {
        active: this.sidebar.opened,
        actives: !this.sidebar.opened,
      };
    },
    classobj() {
      return {
        active: this.sidebar.opened,
        actives: !this.sidebar.opened,
      };
    },
    opened() {
      return this.sidebar.opened;
    },

    orderListNum() {
      return this.$store.getters.orderListNum;
    },

    refundListNum() {
      return this.$store.getters.refundListNum;
    },
  },

  watch: {
    $route(to, from) {
      this.zcPath = to.path;
    },
    opened() {
      if (this.opened) {
        this.toggle = this.activeName;
      } else {
        this.toggle = "";
      }
    },

    "$route.path"() {
      this.activeName = this.$route.path.split("/")[1];
      this.toggle = this.$route.path.split("/")[1];
    },
  },

  methods: {
    // 破图
    handleErrorImg(e) {
      console.log("图片报错了xxxx2777", e.target.src);
      e.target.src = ErrorImg;
    },
    //发货订单数量
    async orderCounts() {
      const list = await orderCount({
        api: "plugin.presell.AdminPreSell.orderCount",
      });
      if (list.data.code == 50786) {
        this.$router.push("/mall/fastBoot/index");
      } else {
        setStorage("inOrderNum", list.data.data.inOrderNum);
      }
    },
    // 切换菜单栏
    handleSelect(routeOrPath, child) {
      // 兼容两种调用方式：
      // 1. 点击一级菜单图标：handleSelect(route)
      // 2. 点击子菜单：handleSelect(`${route.name}/${child.path}`, child)
      let targetPath;
      let index;

      if (typeof routeOrPath === 'object') {
        // 是 route 对象（点击一级菜单图标）
        targetPath = routeOrPath.redirect || routeOrPath.path;
        index = routeOrPath.name;
      } else {
        // 是字符串路径（点击子菜单）
        targetPath = '/' + routeOrPath;
        index = routeOrPath.split('/')[1];
      }

      console.log('handleSelect', targetPath, index);

      if (isExternal(targetPath)) {
        window.open(targetPath, "_blank");
        return;
      }
      this.$router.push(targetPath);
      // setStorage('menuId', child ? child.id : null)
      if (index == "plug_ins/seckill") {
        this.$store.dispatch("orderNum/getOrderSecCount");
      }
      if (index == "plug_ins/integralMall") {
        this.$store.dispatch("orderNum/getOrderInCount");
      }
      if (index == "plug_ins/preSale") {
        this.orderCounts();
      }
    },

    getAsyncRoutes() {
      const res = [];
      return request({
        method: "post",
        params: {
          api: "saas.role.getAsyncRoutesByRoutes",
        },
      }).then((routes) => {
        if (routes.data.code == "200") {
          let route = routes.data.data;
          console.log(route, "菜单权限");
          if (route.menu.length !== 0) {
            const list = [];
            route.menu.forEach((menu, index) => {
              let icons = [];
              if (!isEmpty(menu.image)) {
                icons.push(menu.image);
              }
              if (!isEmpty(menu.image1)) {
                icons.push(menu.image1);
              }
              //一级菜单
              let topMenu = {
                path: "/" + menu.module,
                component: Layout,
                redirect: "/" + menu.module + "/" + menu.children[0].module,
                name: menu.module,
                meta: { title: menu.title, icon: icons },
              };
              //递归子菜单
              topMenu.children = this.getMenus(menu.children);

              res.push(topMenu);
              menu.children.forEach((item, i) => {
                list.push(item);
              });
            });
            setStorage("route", list);
          }
          return res;
        }
      });
    },
    //菜单递归
    getMenus(menuList) {
      if (isEmpty(menuList)) {
        return [];
      }
      menuList.forEach((currentMenu, index) => {
        let childrenMenu = {
          path: currentMenu.module,
          name: currentMenu.module,
          meta: { title: currentMenu.title, is_core: currentMenu.is_core, is_external: isExternal(currentMenu.module) },
          id: currentMenu.id,
        };
        //是否有子菜单
        if (!currentMenu.isChildren) {
          if (isExternal(currentMenu.module)) {
            childrenMenu.component = { render(c) { return c("div"); } };
          } else {
            childrenMenu.component = (resolve) =>
              require([`@/views${currentMenu.url}`], resolve);
          }
        } else {
          childrenMenu.redirect = currentMenu.url;
          childrenMenu.component = {
            render(c) {
              return c("router-view");
            },
          };
          //继续递归
          childrenMenu.children = this.getMenus(currentMenu.children);
        }
        menuList[index] = childrenMenu;
      });
      return menuList;
    },

    mouseenter(value) {
      this.toggle = value.name;
    },

    mouseleave() {
      this.toggle = this.activeName;
      if (!this.opened) {
        this.toggle = "";
      }
    },

    change(value) {
        this.navList = value.children;
        this.mainNav = value.name || '';
        this.toggle = value.name;
    },

    generateTitle(title) {
      const hasKey = this.$te("route." + title);
      if (hasKey) {
        const translatedTitle = this.$t("route." + title);
        return translatedTitle;
      }
      return title;
    },
  },
};
</script>

<style scoped lang="scss">
.mainSideBar {
  height: 100%;
  display: flex;
  justify-content: space-between;
  cursor: pointer;
  z-index: 101;
  .left-nav {
    width: 70px;
    height: 100%;
    background-color: #343e4c;
    overflow-y: auto;
    z-index: 100;
    &::-webkit-scrollbar {
      display: none;
    }
    .side-nav {
      background-color: #343e4d;
      border-bottom: none !important;
      margin: 0;
      padding: 0;
      .side-nav-item {
        width: 70px;
        height: 80px;
        margin: 0;
        padding: 0;
        color: #b2bcd1;
        font-size: 14px;
        &:hover .nav-info {
          display: block;
        }
        .nav-maininfo {
          width: 70px;
          height: 80px;
          display: flex;
          justify-content: center;
          align-items: center;
          position: relative;
          &:hover {
            color: #fff;
          }
          &.active {
            background-color: #161c24;
            color: #fff;
          }
          .icon-font {
            width: 70px;
            height: 80px;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            img {
              width: 22px;
              height: 22px;
              margin-bottom: 5px;
            }
            span {
              font-size: 16px;
              font-weight: bold;
            }
          }
          i {
            content: "";
            position: absolute;
            right: 0;
            top: 50%;
            transform: translateY(-50%);
            width: 0;
            height: 0;
            border: 8px solid transparent;
            border-right-color: #ffffff;
          }

          .red-dot {
            display: inline-block;
            width: 10px;
            height: 10px;
            background-color: red;
            border: 1px solid red;
            border-radius: 50%;
            position: absolute;
            top: 17px;
            right: 7px;
          }
        }

        .nav-info {
          width: 150px;
          height: calc(100vh - 100px);
          position: fixed;
          left: 70px;
          top: 60px;
          display: none;
          background-color: #fff;
          color: #414658;
          margin: 0;
          padding: 10px;
          box-shadow: 3px 0px 3px rgba(185, 183, 183, 0.1);
          z-index: 10000;
          li:first-child {
            margin-top: 0;
          }
          li {
            width: 130px;
            height: 40px;
            line-height: 40px;
            margin: 8px 0;
            margin: 8px 0;
            border-radius: 2px;
            &.active span {
              background-color: #e9f4ff;
              color: #2890ff;
            }
            &:hover {
              color: #2890ff !important;
            }
            span {
              display: block;
              width: 100%;
              height: 100%;
              padding-left: 6px;
              font-size: 16px;
              &.active {
                background-color: #e9f4ff;
                color: #2890ff;
              }
            }

            .orderListNum,
            .salesReturnListNum {
              font-style: normal;
              color: #ff453d;
              /* margin-left: 8px; */
            }
          }
        }
      }
    }
  }

  .right-nav {
    width: 150px;
    height: calc(100vh - 100px);
    position: fixed;
    left: 70px;
    transition: left 0.28s ease;
    z-index: 1;
    &.active {
      left: 70px;
    }
    &.actives {
      left: -80px;
      height: calc(100vh - 100px);
    }
    .itemBar {
      &.active {
        display: block;
      }
      &.actives {
        display: none;
      }
    }
  }
}
</style>
