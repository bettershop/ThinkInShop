import Vue from 'vue'

import 'normalize.css/normalize.css' // A modern alternative to CSS resets

import ElementUI from 'element-ui'
import 'element-ui/lib/theme-chalk/index.css'
import '../theme/index.css'
// import locale from 'element-ui/lib/locale/lang/en' // lang i18n
import '@/styles/index.scss' // global css
import '@/styles/common.scss'
import '@/styles/icon.scss'

import App from './App'
import store from './store'
import router from './router'


import '@/icons' // icon
import '@/permission' // permission control

import locale from 'element-ui/lib/locale';
import enLocale from 'element-ui/lib/locale/lang/en'
import zhLocale from 'element-ui/lib/locale/lang/zh-CN'


import LktTransfer from './components/lkt_transfer'

import LKtNavLabel from './components/lkt_navLabel'
import lktColorPicker from './components/lkt_colorPicker'

import LKtInput from './components/lkt_input'
import Lktinstruction from './components/lkt_instruction'

import LKtTable from './components/lkt_table'

import LKtButton from './components/lkt_button'

import LKtDialog from './components/lkt_dialog'

if (process.env.NODE_ENV === 'production') {
  const { mockXHR } = require('../mock')
  mockXHR()
}
// 引入echarts图表
import * as echarts from "echarts"
Vue.prototype.$echarts = echarts

import i18n from './lang'

//引入moment
import moment from 'moment'
Vue.prototype.$moment = moment

// Vue.use(ElementUI,{
//   locale.i18n: (key, value) => i18n.t(key, value)
// })
locale.i18n((key, value) => i18n.t(key, value)) //兼容element
// Vue.use(ElementUI, { locale })
import AsyncComputed from "vue-async-computed";
Vue.use(AsyncComputed);
// 导入组件库
import lUpload from './packages/index'
import lTransfer from "./packages/index";
import LPagination from "./packages/index";
import LButton from "./packages/index";
import LInput from "./packages/index";
import LSelect from "./packages/index";
// 全局基础组件

// 枚举工具
import enums from '@/enums';
Vue.prototype.$enums = enums;

// 注册组件库
Vue.use(lUpload)
Vue.use(lTransfer)
Vue.use(LPagination)
Vue.use(LButton)
Vue.use(LInput)
Vue.use(LSelect)

// 查看图片
import Viewer from 'v-viewer'
import 'viewerjs/dist/viewer.css'
Vue.use(Viewer, {
  defaultOptions: {
    zIndex: 9999,
  }
})

// 全局前置守卫
router.beforeEach((to, from, next) => {
  if (from.path !== to.path) {
    // 这段代码 用于切换页面时，获取第一个 类名以'container' 命名的元素并将改元素滚动到可视区域的顶部
    // 解决 部分页面切换时 因为浏览器滚动条 的原因，新页面没有显示在顶部
    Vue.nextTick(() => {
      const element = document.getElementsByClassName('container');
      console.log(element)
      if (element.length > 0) {
        // 将该元素滚动到可视区域的顶部
        element[0].scrollIntoView({ behavior: 'smooth', block: 'start' });
      }
    });
  }
  next()
})
/**
 * 自定义复制指令
 * 使用方式
 *  <i
      class="el-icon-document-copy"
      v-copy="textValue"
    ></i>
    textValue : 被复制的字符串
 */
Vue.directive('copy', {
  bind(el, { value, modifiers }) {
    el.$value = value;
    console.log(value)

    const useCapture = modifiers.capture;
    const stopPropagation = modifiers.stop;

    el.handler = (event) => {
      if (stopPropagation) {
        event.stopPropagation();
      }

      try {
        // 获取绑定的值
        if (!el.$value) {
          ElementUI.Message({
            type: 'warning',
            dangerouslyUseHTMLString: true,
            message: '内容为空',
            offset: 102
          })
          return
        }

        // 创建一个临时输入框来存放要复制的内容
        const input = document.createElement('input');
        input.value = el.$value;
        document.body.appendChild(input); // 将输入框添加到 body 中

        input.select(); // 选中文本
        document.execCommand('copy'); // 执行复制命令

        document.body.removeChild(input); // 移除输入框

        ElementUI.Message({
          type: 'success',
          dangerouslyUseHTMLString: true,
          message: '复制成功',
          offset: 102
        })
        // 绑定点击事件（支持捕获阶段） 
      } catch (err) {
        ElementUI.Message({
          type: 'error',
          dangerouslyUseHTMLString: true,
          message: '复制失败',
          offset: 102
        })
      }
    };
    // 将点击事件绑定到元素上
    el.addEventListener('click', el.handler, useCapture);
  },
  update(el, { value }) {
    el.$value = value; // 更新指令的绑定值
  },
  unbind(el) {
    const useCapture = modifiers.capture;
    el.removeEventListener('click', el.handler, useCapture);
    delete el.handler;
  }
});

import '@/common/commonStyle/common.scss'
import publicFunction from '@/common/commonFunction/common'
Vue.use(publicFunction)

Vue.filter('dateFormat', function (val) {
  try {
    if (val === null || val === '' || !val) {
      return '暂无时间';
    }
    const d = new Date(val); // val 为表格内取到的后台时间
    const month = d.getMonth() + 1 < 10 ? `0${d.getMonth() + 1}` : d.getMonth() + 1;
    const day = d.getDate() < 10 ? `0${d.getDate()}` : d.getDate();
    const hours = d.getHours() < 10 ? `0${d.getHours()}` : d.getHours();
    const min = d.getMinutes() < 10 ? `0${d.getMinutes()}` : d.getMinutes();
    const sec = d.getSeconds() < 10 ? `0${d.getSeconds()}` : d.getSeconds();
    const times = `${d.getFullYear()}-${month}-${day} ${hours}:${min}:${sec}`;
    return times;
  } catch (error) {
    console.error(error)
    return '格式异常！';
  }
})

Vue.filter('dateFormat1', function (val) {
  if (val === null || val === '') {
    return '暂无时间';
  }
  const d = new Date(val); // val 为表格内取到的后台时间
  const month = d.getMonth() + 1 < 10 ? `0${d.getMonth() + 1}` : d.getMonth() + 1;
  const day = d.getDate() < 10 ? `0${d.getDate()}` : d.getDate();
  const times = `${d.getFullYear()}-${month}-${day}`;
  return times;
})



import './utils/rem'
import directives from './utils/directives.js'
import hasPermi from './utils/hasPermi.js'

import enter from './utils/enter.js'

Vue.use(directives)
Vue.use(hasPermi)
Vue.use(enter)



// iView 和 iView Pro
import ViewUI from 'view-design';
import iViewPro from '@/libs/iview-pro/iview-pro.min.js';
import { modalSure } from '@/utils/public'
import modalForm from '@/utils/modalForm'
import formCreate from '@form-create/iview'
import 'iview/dist/styles/iview.css'
// 路由


import './styles/DIY/index.less';
import './libs/iview-pro/iview-pro.css';

import './assets/iconfont/iconfont.css'
import './assets/iconfont/iconfont2.css'
import './assets/iconfont/iconfont3.css'
//订单来源
import { myGetSource } from './store/modules/myGetSource'
Vue.prototype.$myGetSource = myGetSource;
// 懒加载
import VueLazyload from 'vue-lazyload';

Vue.config.productionTip = false

Vue.prototype.$modalForm = modalForm;
Vue.prototype.$modalSure = modalSure;
Vue.use(formCreate);

Vue.use(ViewUI)
Vue.use(iViewPro)
Vue.use(VueLazyload)


Vue.config.productionTip = false

Vue.component('l-Transfer', LktTransfer);

Vue.component('l-tab', LKtNavLabel);
Vue.component('l-colorPicker', lktColorPicker);

Vue.component('l-input', LKtInput);

Vue.component('l-instruction', Lktinstruction);

Vue.component('l-table', LKtTable);

Vue.component('l-button', LKtButton);

Vue.component('l-dialog', LKtDialog);

import commonGlobal from '@/api/common.js'
Vue.prototype.LaiKeCommon = commonGlobal


new Vue({
  el: '#app',
  i18n,
  router,
  store,
  render: h => h(App)
})
