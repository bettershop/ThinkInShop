// 当前文件用于初始化 diy增加 系统页面 默认信息（分类页面 购物车页面 我的页面 ）内容

const path = require('path')
const files = require.context('./', false, /\.js$/)

const modules = {}
files.keys().forEach(key => {
  const name = path.basename(key, '.js')
  modules[name] = files(key).default || files(key)
})

export default modules