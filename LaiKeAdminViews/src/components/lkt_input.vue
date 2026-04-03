<!-- 
    插件简介：
    slotName: 前后，元素和内容的展示文案
    isSlotApp:
    isSlotPre:
    isPrefix:
    isPuffix:

    使用1： 纯文案前后置元素类型
    <LktInput
        size="large"
        v-model="inputInfo.uname"
        :isSlotApp="true"
        :slotName="`元/%`"
        :isPrefix="false"
        placeholder="xxxxxx"
        style="width: 200px"
      ></LktInput>

      使用2：图标前后置元素类型
      <LktInput
        size="large"
        v-model="inputInfo.uname"
        :isSlotApp="true"
        :isPrefix="false"
        placeholder="xxxxxx"
        style="width: 200px"
      >
        // 使用插槽搭配图标
        <el-button slot="append" icon="el-icon-search"></el-button> 
      </LktInput>
 -->
<template>
  <el-input v-bind="$attrs" v-model="valueRef" @input="inputChange">
    <!-- 使用插槽配置的 -->
    <template #append v-if="isSlotApp">
      {{  slotName}}
      <slot name="append">
        <span >{{ slotName?slotName:'' }}</span>
      </slot>
    </template>
    <template #prepend v-if="isSlotPre">
      <slot name="prepend">
        <span >{{ slotName?slotName:'' }}</span>
      </slot>
    </template>
    <!-- 前后图标 -->
    <template #prefix v-if="isPrefix">
      <slot name="prefix">
        <span >{{ slotName?slotName:'' }}</span>
      </slot>
    </template>
    <template #suffix v-if="isPuffix">
      <slot name="suffix">
        <span >{{ slotName?slotName:'' }}</span>
      </slot>
    </template>
  </el-input>
</template>
<script>
export default {
  name: "LInput",
  props: {
    value: {
      type: [String, Number],
      default() {
        return "";
      },
    },
    slotName: {
      type: String,
      default: "",
    },
    //   是否开启插槽传参
    isSlotApp: {
      type: Boolean,// 是否开启：后置元素，一般为标签或按钮
      default: false,
    },
    isSlotPre: {
      type: Boolean,// 是否开启：前置元素，一般为标签或按钮
      default: false,
    },
    isPrefix: {  // 是否开启： 输入框头部内容
      type: Boolean,
      default: false,
    },
    isPuffix: { // 是否开启： 输入框尾部内容
      type: Boolean,
      default: false,
    },
    regular: {
      type: String,
      default: "",
    },
  },
  components: {},
  data() {
    return {
      valueRef:""
    };
  },
  computed: {},
  watch: {
    valueRef(val) {
      this.$emit("modelValue", val);
    },
    value: {
      handler(val) {
        this.valueRef = val;
      },
      deep: true,
    },
  },
  created() {},
  mounted() {},
  methods: {
    inputChange(value) {
      this.$emit("input", this.valueRef);
    },
  },
};
</script>
<style scoped lang="less"></style>
