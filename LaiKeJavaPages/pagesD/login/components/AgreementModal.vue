<template>
 <div></div>
</template>
  
  <script>
  export default {
    props: {
      modelValue: {
        type: Boolean,
        default: false,
      },
    },
    computed: {
      show: {
        get() {
          return this.modelValue;
        },
        set(value) {
          this.$emit('update:modelValue', value);
        },
      },
    },
    methods: {
      openAgreement(type) {
        // 打开协议页面
      },
    },
  };
  </script>
  
  <style scoped lang="less">
  
  </style>