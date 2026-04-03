<!-- 
    插件简介：
    is_white:表示目前系统的按钮类型，有三种
    为1时：白色底色，黑色文字按钮
    <LktButton size="large" :is_white="1" type="primary" icon="Tickets">导入记录</LktButton>

    为2时：自定义颜色,白底文字（color得传值）
    <LktButton size="large" :is_white="2" type="primary" icon="Tickets" :color="'#2bc2c1'">导入记录</LktButton>
    
    为3时：带颜色边框，白底背景
      <LktButton size="large" :is_white="3" plain>
        导入记录
        <template #icon>
          <i style="margin-left: 8px">X</i>
        </template>
      </LktButton>
    
    插槽：#icon  图标按钮
    自提图标更改：增加组件伪类添加图标
    实例：
    <LktButton size="large" :is_white="2" type="primary" icon="Tickets" :color="'#2bc2c1'"  class="green_edge2">导入记录</LktButton>
    注意：不要和green_edge同名，因为伪类content写在外面，多个会混淆
    .green_edge2:before {
        content: '\e642' ;
        font-size: 14px;
        margin-right: 8px;
        position: relative;
        top: 1px;
      }
 -->
<template>
  <el-button
    ref="lktbutton"
    v-bind="$attrs"
    :style="is_white==2?myStyleData:''"
    :class="menue[is_white]"
    @click="handleClick"
    :size="size"
    :type="type"
    :disabled="disabled"
    :plain="plain"
    :text="text"
    :icon="icon"
  >
    <slot></slot>
    <slot name="icon"></slot>
    <slot name="loading"></slot>
  </el-button>
</template>
<script>
export default {
  name: "LButton",
  props: {
    size: {
      type: String,
      default: undefined,
    },
    type: {
      type: String,
      default: "default",
    },
    disabled: {
      type: Boolean,
      default: false,
    },
    plain: {
      type: Boolean,
      default: false,
    },
    text: {
      type: Boolean,
      default: false,
    },
    bg: {
      type: Boolean,
      default: false,
    },
    link: {
      type: Boolean,
      default: false,
    },
    round: {
      type: Boolean,
      default: false,
    },
    circle: {
      type: Boolean,
      default: false,
    },
    loading: {
      type: Boolean,
      default: false,
    },
    loadingIcon: {
      type: [String, Object],
      default: undefined,
    },
    icon: {
      type: [String, Object],
      default: undefined,
    },
    autofocus: {
      type: Boolean,
      default: false,
    },
    nativeType: {
      type: String,
      default: "button",
    },
    autoInsertSpace: {
      type: Boolean,
      default: false,
    },
    color: {
      type: String,
      default: "#2890ff",
    },
    conctentx: {
      type: String,
      default: "\e644",
    },
    darker: {
      type: Boolean,
      default: false,
    },
    tag: {
      type: [String, Object],
      default: "button",
    },
    is_white: {
      type: Number,
      default: -1,
    },
  },
  components: {},
  data() {
    return {
      myStyleData: {
        "--color": this.color,
        "--conctentx": this.conctentx,
      },
      menue: {
        0: "batchImport",
        1: "white_edge",
        2: "green_edge",
        3: "blue_edge",
      },
    };
  },
  computed: {
    className: () => {
      const menue = {
        0: "batchImport",
        1: "white_edge",
        2: "green_edge",
        3: "blue_edge",
      };
      return menue[this.is_white];
    },
  },
  watch: {
    color: {
      handler(val) {
        this.$set(this.myStyleData, "--color", this.color);
        this.$set(this.myStyleData, "--conctentx", this.conctentx);
      },
      deep: true,
      immediate: true,
    },
  },
  created() {},
  mounted() {},
  methods: {
    handleClick() {
      this.$emit("click");
    },
    setUI() {
      console.log("this.$refs.lktbutton", this.$refs.lktbutton.style);
    },
    myStyle() {
      this.myStyleData = {
        "--color": this.color,
      };
    },
  },
};
</script>
<style scoped lang="less">
// 白色边框按钮
.white_edge {
  min-width: 120px;
  height: 42px;
  color: #6a7076;
  background-color: #fff;
  border-color: #fff;
  border-radius: 4px;
  &:focus {
    color: #6a7076;
    background-color: #fff;
    border-color: #fff;
  }
  &:hover {
    color: #6a7076;
    background-color: #fff;
    border-color: #fff;
    &:not(.is-disabled) {
      opacity: 0.8;
    }
    &:active {
      opacity: 1;
    }
  }
}

// 蓝色的-默认按钮
.green_edge {
  font-family: "laiketui" !important;
  font-size: 16px;
  font-style: normal;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  background-color: var(--color);
  border-color: var(--color);
  color: #fff;
  .is-loading {
    width: 100%;
  }
  &:focus {
    background-color: var(--color);
    border-color: var(--color);
  }
  &:hover {
    background-color: var(--color);
    border-color: var(--color);
    &:not(.is-disabled) {
      opacity: 0.8;
    }
    &:active {
      opacity: 1;
    }
  }
}

.batchImport {
}
// 蓝色线条和文字
.blue_edge {
  min-width: 96px;
  height: 38px;
  border-radius: 4px;
  border: 1px solid #2890ff;
  background-color: #fff;
  color: #2890ff;
  margin: 0;
  padding: 0;
  &:focus {
    border: 1px solid #2890ff;
    background-color: #fff;
    color: #2890ff;
    opacity: 1;
  }
  &:hover {
    border: 1px solid #2890ff;
    background-color: #fff;
    color: #2890ff;
    opacity: 0.6;
  }
}
</style>
