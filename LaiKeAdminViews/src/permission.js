import router from "./router";
import store from "./store";
import { Message } from "element-ui";
import NProgress from "nprogress"; // 进度条
import "nprogress/nprogress.css"; // 进度条样式
import getPageTitle from "@/utils/get-page-title";
import { getStorage, setStorage } from "@/utils/storage";

NProgress.configure({ showSpinner: false });

const whiteList = ["/login", "/404", "/mallLogin"]; // 没有重定向白名单
// 校验跳转没有权限页面白名单
const AutomaticList = [
  "/mall/fastBoot/index",
  "/plug_ins/stores/increaseStore",
];
router.beforeEach(async (to, from, next) => {
  // 开始进度条
  // debugger
  NProgress.start();
  // debugger
  // 设置页面标题
  document.title = getPageTitle(to.meta.title);
  if (getStorage("laike_admin_userInfo")) {
    if (to.path === "/login"||to.path === '/mallLogin') {
      // 如果已经登录, 重定向到首页
      next({ path: "/" });
      NProgress.done();
    } else {
      const name = store.getters.name;
      if (getStorage("rolesInfo") && name) {
        if (
          !to.name &&
          to.path != "/print" && to.path != "/printSheet" &&
          to.path != "/systemMaintenance"
        ) {
          // next('/404')
          // /order/orderList/orderLists
          // 登录更改权限退出登录，页面404问题
          const goRouterList = getStorage("route");
          if (goRouterList) {
            if (goRouterList.some((item) => item.redirect === to.path)) {
              next(to.path);
            } else {
              console.log(
                "35353535353535",
                to,
                store.state.permission.addRoutes
              );
              if (
                AutomaticList.indexOf(to.path) !== -1 &&
                !goRouterList.some((item) => item.redirect === to.path)
              ) {
                let path = store.state.permission.addRoutes[0].redirect;
                next(path);
              } else {
                next("/404");
              }
            }
            // next(to.path)
          } else {
            if (store.state.permission.addRoutes.length <= 0) {
              console.log("40404040404040", to);
              next("/404");
            } else {
              let path = store.state.permission.addRoutes[0].redirect;
              next(path);
            }
          }
        } else {
          await store.dispatch("user/getAuthorizationList");
          next();
        }
      } else {
        try {
          if (
            getStorage("laike_admin_userInfo").systemMsgType == 1 &&
            getStorage("laike_admin_userInfo").type != 0
          ) {
            const userInfo = getStorage("laike_admin_userInfo");
            const { name } = userInfo;
            store.commit("user/SET_NAME", name);
            // 系统维护去除跳转界面（49151）
            // next('/systemMaintenance')
            next();
          } else {
            await store.dispatch("user/getAuthorizationList");
            if (getStorage("laike_head_img")) {
              store.commit(
                "user/SET_MERCHANTSLOGO",
                getStorage("laike_head_img")
              );
            }
            const userInfo = getStorage("laike_admin_userInfo");
            const { name } = userInfo;
            store.commit("user/SET_NAME", name);

            const accessRoutes = await store.dispatch(
              "permission/getAsyncRoutes"
            );
            router.addRoutes(accessRoutes);
            store.dispatch("source/getSource");
            store.dispatch("orderNum/getOrderCount");
            console.log(store.state.permission.addRoutes);
            if (getStorage("versionUpdate") == false) {
            } else {
              if (
                (getStorage("laike_admin_userInfo").systemMsgType == 2 ||
                  getStorage("laike_admin_userInfo").systemMsgType == 3) &&
                getStorage("laike_admin_userInfo").type != 0
              ) {
                setStorage("versionUpdate", true);
              }
            }
            if (
              store.state.permission.addRoutes.some((item) => {
                return item.name == "home";
              })
            ) {
              next({ ...to, replace: true });
            } else {
              let path = store.state.permission.addRoutes[0].redirect;
              next(path);
            }
          }
        } catch (error) {
          console.log("异常");
          console.log(error);
          // 删除token并转到登录页面重新登录
          await store.dispatch("user/resetToken");
          // Message.error(error || 'Has Error')
          next(`/login?redirect=${to.path}`);
          NProgress.done();
        }
      }
    }
  } else {
    /* 没有token*/
    if (whiteList.indexOf(to.path) !== -1) {
      // 直接去免登录白名单
      next();
    } else {
      // 其他没有访问权限的页面被重定向到登录页面。
      next(`/login?redirect=${to.path}`);
      NProgress.done();
    }
  }
});

router.afterEach(() => {
  NProgress.done();
});
