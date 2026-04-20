let path = require('path'); 
const TerserPlugin = require('terser-webpack-plugin'); 
module.exports = {
   
    configureWebpack:{ 
       // 核心：排除指定依赖，仅微信小程序端生效
          externals: process.env.UNI_PLATFORM === 'mp-weixin' 
            ? {
                // 排除 jweixin-module
                'jweixin-module': 'jweixin-module',
                // 排除 vue-clipboard2
                'vue-clipboard2': 'vue-clipboard2',
                'vconsole': 'vconsole',
              } 
            : {}, // 其他端（H5/App）不排除，正常打包
            module: {
                rules: [
               //         {
               //           test: /\.(js|ts)$/,
               //           use: {
               //             loader: 'babel-loader',
               //             options: {
               //               cacheDirectory: true,
               //             },
               //           },
               //           exclude(resource) {
               //             // 所有端都排除 node_modules
               //             if (/node_modules/.test(resource)) return true;
               
               //             // 微信小程序端额外排除
               //             if (process.env.UNI_PLATFORM === 'mp-weixin') {
               //               const packagesPath = path.resolve(__dirname, 'packages');
               //               const iconfontPath = path.resolve(__dirname, 'static/iconfont1');
               
               //               if (resource.startsWith(packagesPath)) return true;
               //               if (resource.startsWith(iconfontPath)) return true;
               //             }
               
               //             return false;
               //           },
               //         },
                     ],
            },
         optimization: {
              usedExports: true, //构建时移除未使用的代码。
              minimizer: [
                new TerserPlugin({
                  terserOptions: {
                      compress: {
                          // drop_console: true,
                          drop_debugger: true
                      },
                      mangle: true,
                      output: {
                          comments: false
                      }
                  },
                  parallel: true,
                  sourceMap: false,
                  extractComments: false
                })
              ]
          }
    }
    // css: {
//         loaderOptions: {
//             less: {
//                 globalVars: {
//                     "hack": `true;@import "${stylePath}"`
//                 }
//             }
//         }
//     },
    // pluginOptions: {
//         'style-resources-loader': {
//             preProcessor: 'less',
//             // 这三种 patterns 写法都是可以的
//             // patterns: ["./src/assets/reset1.less", "./src/assets/reset2.less"]
//             // patterns: "./src/assets/reset.less"
//             patterns: [
//             // 两种路径写法都可以，这里的路径不能使用 @ 符号，否则会报错
//             // path.resolve(__dirname, './src/assets/reset.less')
//             path.resolve(__dirname, 'src/assets/reset.less')
//             ]
//         }
//     }

}
