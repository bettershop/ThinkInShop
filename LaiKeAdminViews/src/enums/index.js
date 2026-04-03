// src/enums/index.js
const modules = {};
 
// 自动导入所有模块目录下的 .js 文件
const requireModule = require.context(
  '.',           // 当前目录
  true,          // 包括子目录
  /\/[^/]+\/[^/]+.js$/  // 匹配 /模块名/文件名.js 格式的文件
);

requireModule.keys().forEach(fileName => {
  // 获取模块名（目录名）
  const moduleName = fileName.split('/')[1];
  
  // 获取脚本名（不包含扩展名）
  const scriptName = fileName
    .split('/')
    .pop()
    .replace(/\.\w+$/, '');

  // 如果该模块还没有被注册，初始化它
  if (!modules[moduleName]) {
    modules[moduleName] = {};
  }

  // 导入脚本内容
  const script = requireModule(fileName);
  
  // 将脚本内容添加到对应的模块下
  modules[moduleName][scriptName] = script.default || script;
});

export default modules;