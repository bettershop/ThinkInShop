<template>
    <div class="icon-switch">
      <!-- 过渡动画容器 --> 
        <div class="model-cut-flower" @click="iconSwitch">
            <div :class="iconType=='ios'&& 'choose'" data-type="ios" >
                <img :src="getImageUrl(iosw)"  />
            </div>
            <div :class="iconType=='applet'&& 'choose'" data-type="applet">
                <img :src="getImageUrl(xcxw)"  />
            </div>
            <div :class="iconType=='andoid'&& 'choose'" data-type="andoid">
                <img :src="getImageUrl(azw)"/>
            </div>
        </div> 
    </div>
  </template>
  
  <script>
  export default {
    data() {
      return {
        azw: 'images/diy/az-w.png',
            iosw: 'images/diy/ios-w.png',
            xcxw: 'images/diy/xcx-w.png',
        iconType:"ios"
      };
    },
    computed: {
      displayIcons() {
        return this.icons.filter(icon => icon.active);
      }
    },
    methods: {
      getImageUrl(path) {
            try {
                return require(`@/assets/${path}`) // 根据你的项目结构调整前缀
            } catch (e) {
                console.error('图片加载失败:', path, e)
                // 可增加默认的图片
                return ''
            }
        },
      iconSwitch(e) {
            const target = e.target
            // 查找最接近的包含 data-type 属性的父元素（因为点击的可能是 img 标签）
            const dom = target.closest('[data-type]');
            if (dom) {
                const type = dom.getAttribute('data-type');
                this.iconType = type
            }
        },
    }
  };
  </script>
  
  <style scoped lang="less">
  .model-cut-flower{
        width: 224px;
        height: 48px;
        background: #F4F5F6;
        border-radius: 23px 23px 23px 23px;

        position: absolute;
        bottom: -82px;
        left: 50%;
        transform: translateX(-50%);

        display: flex;
        align-items: center;
        user-select: none;
        justify-content: center;
        div{
            width: 72px;
            height: 40px;
            border-radius: 0px 0px 0px 0px;
            text-align: center;
            line-height: 50px;
            cursor: pointer;
            img {
                width: 21px;
                height: 20px; 
            }
        }
        .choose{
            background: #fff;
            border-radius: 50px;
            box-shadow: 1px 4px 3px #ccc; 
        }
    }
  </style>