const fs = require('fs-extra');
const path = require('path');
const Terser = require('terser'); // JS 压缩
const CleanCSS = require('clean-css'); // CSS/WXSS 压缩
const jsonminify = require('jsonminify'); // JSON 压缩
const htmlMinifier = require('html-minifier-terser'); // Template 压缩
const compiler = require('vue-template-compiler'); // Vue 文件解析

// 1. 定义路径
const rootDir = path.resolve(__dirname, '../'); // 项目根目录
const sourceDir = path.join(rootDir, 'packages'); // 源目录：专属打包文件夹
const targetDir = path.join(rootDir, 'node_modules/@my-miniprogram'); // 目标目录

// 2. 压缩配置（可按需调整）
const compressConfig = {
  // HTML/WXML/Template 压缩配置
  html: {
    collapseWhitespace: true, // 折叠空格
    removeComments: true, // 移除注释
    removeEmptyAttributes: true, // 移除空属性
    minifyJS: false, // 模板内的JS不单独压缩（脚本部分会单独处理）
    minifyCSS: false // 模板内的CSS不单独压缩（样式部分会单独处理）
  },
  // JS 压缩配置
  js: {
    compress: {
      drop_console: true, // 移除console（开发时可改为false）
      drop_debugger: true, // 移除debugger
      pure_funcs: ['console.log', 'console.warn'] // 移除指定函数
    },
    mangle: true, // 混淆变量名（开发时可改为false）
    format: { comments: false } // 移除注释
  },
  // CSS/WXSS 压缩配置
  css: { level: 2, removeComments: true }
};

// 3. 单一文件压缩函数
const compressors = {
  // JS/WXS 文件压缩
  '.js': async (filePath) => {
    const code = await fs.readFile(filePath, 'utf8');
    const result = await Terser.minify(code, compressConfig.js);
    if (result.error) throw result.error;
    await fs.writeFile(filePath, result.code, 'utf8');
  },
  '.wxs': async (filePath) => compressors['.js'](filePath), // 复用JS压缩逻辑

  // CSS/WXSS 文件压缩
  '.wxss': async (filePath) => {
    const code = await fs.readFile(filePath, 'utf8');
    const minified = new CleanCSS(compressConfig.css).minify(code).styles;
    await fs.writeFile(filePath, minified, 'utf8');
  },
  '.css': async (filePath) => compressors['.wxss'](filePath), // 复用WXSS压缩逻辑

  // JSON 文件压缩
  '.json': async (filePath) => {
    const code = await fs.readFile(filePath, 'utf8');
    const minified = jsonminify(code);
    await fs.writeFile(filePath, minified, 'utf8');
  },

  // Vue 文件压缩（核心新增）
  '.vue': async (filePath) => {
    const vueCode = await fs.readFile(filePath, 'utf8');
    // 解析Vue文件为模板、脚本、样式三部分
    const parsed = compiler.parseComponent(vueCode, { preserveWhitespace: false });

    let newVueCode = '';
    // 处理 <template> 部分
    if (parsed.template) {
      const templateContent = parsed.template.content;
      const minifiedTemplate = await htmlMinifier.minify(templateContent, compressConfig.html);
      newVueCode += `<template${parsed.template.attrs ? ' ' + Object.entries(parsed.template.attrs).map(([k, v]) => `${k}="${v}"`).join(' ') : ''}>\n${minifiedTemplate}\n</template>\n`;
    }

    // 处理 <script> 部分
    if (parsed.script) {
      const scriptContent = parsed.script.content;
      const minifiedScript = await Terser.minify(scriptContent, compressConfig.js);
      if (minifiedScript.error) throw minifiedScript.error;
      newVueCode += `<script${parsed.script.attrs ? ' ' + Object.entries(parsed.script.attrs).map(([k, v]) => `${k}="${v}"`).join(' ') : ''}>\n${minifiedScript.code}\n</script>\n`;
    }

    // 处理 <style> 部分（支持多style标签）
    if (parsed.styles && parsed.styles.length > 0) {
      for (const style of parsed.styles) {
        const styleContent = style.content;
        const minifiedStyle = new CleanCSS(compressConfig.css).minify(styleContent).styles;
        newVueCode += `<style${style.attrs ? ' ' + Object.entries(style.attrs).map(([k, v]) => `${k}="${v}"`).join(' ') : ''}>\n${minifiedStyle}\n</style>\n`;
      }
    }

    // 保留自定义块（如 <i18n>）（可选）
    if (parsed.customBlocks && parsed.customBlocks.length > 0) {
      for (const block of parsed.customBlocks) {
        newVueCode += `<${block.type}${block.attrs ? ' ' + Object.entries(block.attrs).map(([k, v]) => `${k}="${v}"`).join(' ') : ''}>\n${block.content}\n</${block.type}>\n`;
      }
    }

    // 写入压缩后的Vue文件
    await fs.writeFile(filePath, newVueCode.trim(), 'utf8');
  }
};

// 4. 递归遍历目录并压缩文件
async function compressFiles(dir) {
  const files = await fs.readdir(dir);
  for (const file of files) {
    const filePath = path.join(dir, file);
    const stat = await fs.stat(filePath);

    if (stat.isDirectory()) {
      await compressFiles(filePath); // 递归处理子目录
    } else {
      const ext = path.extname(filePath).toLowerCase();
      if (compressors[ext]) {
        try {
          await compressors[ext](filePath);
          console.log(`🔧 已压缩：${filePath}`);
        } catch (err) {
          console.error(`❌ 压缩失败：${filePath}`, err.message);
        }
      }
    }
  }
}

// 5. 核心构建流程：清空 → 拷贝 → 压缩
async function buildPackages() {
  try {
    // 清空旧文件
    await fs.emptyDir(targetDir);
    console.log('✅ 已清空旧编译文件');

    // 拷贝packages到node_modules
    await fs.copy(sourceDir, targetDir);
    console.log('✅ 公共文件拷贝完成');

    // 压缩所有文件（含Vue）
    await compressFiles(targetDir);
    console.log('✅ 所有文件压缩完成（含Vue文件）');

  } catch (err) {
    console.error('❌ 构建失败：', err);
    process.exit(1);
  }
}

// 执行构建
buildPackages();
