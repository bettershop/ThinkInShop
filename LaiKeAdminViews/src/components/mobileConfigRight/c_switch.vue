<template>
  <div class="numbox" v-if="configData">
    <div class="c_row-item">
      <Col class="label" span="8">
      <span>{{fieldName || '是否显示'}}</span>
      </Col>
      <Col span="16" class="slider-box">
      <ASwitch v-model="configData.val" @on-change="change"></ASwitch>
      <!--        <Input v-model="configData.val" type="number" placeholder="请输入数量" style="text-align: right;"/>-->
      </Col>
    </div>
  </div>
</template>

<script>
import { Switch } from 'iview'
export default {
  name: 'c_switch',
  components: { ASwitch: Switch },
  props: {
    configObj: {
      type: Object
    },
    configNme: {
      type: String
    },
    fieldName: {
      type: String,
      default: '是否显示'
    }
  },
  data() {
    return {
      defaults: {},
      sliderWidth: 0,
      configData: {}
    }
  },
  created() {
    this.defaults = this.configObj
    if (!this.defaults || Object.keys(this.defaults).length === 0) return
    if (this.configObj[this.configNme]) {
      this.configData = this.configObj[this.configNme]
    } else {
      this.configData = this.defaults[this.defaults.link_key][this.configNme]
    }
    console.log(this.configData);
  },
  watch: {
    configObj: {
      handler(nVal, oVal) {
        this.defaults = nVal
        if (!nVal || Object.keys(nVal).length === 0) return
        console.log('c_switchc_switch', nVal, this.configNme);
        if (nVal[this.configNme]) {
          this.configData = nVal[this.configNme]
        } else {
          this.configData = nVal[nVal.link_key][this.configNme]
        }
      },
      immediate: true,
      deep: true
    }
  },
  methods: {
    change() {

    }
  }
}
</script>

<style scoped lang="stylus">
.numbox {
  display: flex;
  align-items: center;
  margin-bottom: 10px;

  span {
    /* width 80px */
    color: #999;
  }
}

/* font-size 12px */
.c_row-item {
  width: 100%;

  .slider-box {
    display: flex;
    flex-direction: row-reverse;
  }
}
</style>
