<template>
  <div class="box" v-if="configData">
    <div class="c_row-item">
      <Col class="label" span="4" :style="{color: titleTextColor}">
      {{configData.title}}
      </Col>
      <Col span="19" class="slider-box">
      <Input v-model="configData.value" placeholder="选填不超过10个字" :maxlength="configData.max" />
      </Col>
    </div>
  </div>

</template>

<script>
export default {
  name: 'c_input_item',
  props: {
    configObj: {
      type: Object
    },
    configNme: {
      type: String
    },
    titleTextColor: {
      type: String,
      default: '#515A6E'
    }
  },
  data() {
    return {
      value: '',
      defaults: {},
      configData: {}
    }
  },
  created() {
    this.defaults = this.configObj
    // this.configData = this.configObj[this.configNme]

    if (!this.configObj || Object.keys(this.configObj).length === 0) return

    if (this.configObj[this.configNme]) {
      this.configData = this.configObj[this.configNme]
    } else {
      this.configData = this.defaults[this.defaults.link_key][this.configNme]
    }
  },
  watch: {
    configObj: {
      handler(nVal, oVal) {
        this.defaults = nVal
        if (!nVal || Object.keys(nVal).length === 0) return
        if (this.configObj[this.configNme]) {
          this.configData = nVal[this.configNme]
        } else {
          this.configData = nVal[nVal.link_key][this.configNme]
        }
      },
      immediate: true,
      deep: true
    }
  }
}
</script>

<style scoped lang="stylus">
.c_row-item {
  margin-bottom: 13px;
}
</style>
