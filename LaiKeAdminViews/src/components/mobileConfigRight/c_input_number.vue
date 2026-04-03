<template>
  <div class="numbox" v-if="configData">
    <div class="c_row-item">
      <Col class="label" span="5">
      <span>{{numName}}</span>
      </Col>
      <Col span="5" class="slider-box">
      <Input v-model="configData.val" type="number" :placeholder="placeholder" style="text-align: right;" ref="input" />
      </Col>
    </div>
  </div>
</template>

<script>
export default {
  name: 'c_input_number',
  props: {
    configObj: {
      type: Object
    },
    configNme: {
      type: String
    },
    numName: {
      type: String,
      default: '商品数量'
    },
    placeholder: {
      type: String,
      default: '请输入数量'
    },
    span: {
      type: Number,
      default: 4
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
    console.log('this.configObj c_input_name', this.configObj)
    // this.configData = this.configObj[this.configNme]
    if (Object.keys(this.configObj).length == 0) return
    if (!this.configObj) {
      this.defaults = {}
      return
    }
    if (this.configObj[this.configNme]) {
      this.configData = this.configObj[this.configNme]
    } else {
      this.configData = this.defaults[this.defaults.link_key][this.configNme]
    }
  },
  watch: {
    'configData.val'(val) {
      console.log('val', this.defaults.name)
      if (['seckill', 'VIP', 'combination', 'homeStore', 'PublicModeFX', 'liveModel'].includes(this.defaults.name)) {
        if (val > 10) {
          this.warnMsg('数量最大为10个，已重置为10个')
          this.$refs.input.currentValue = 10
          this.$set(this.configData, 'val', 10)
        }
        if (val < 1) {
          this.$refs.input.currentValue = 1
          this.$set(this.configData, 'val', 1)
        }
      }


    },
    configObj: {
      handler(nVal, oVal) {
        this.defaults = nVal
        // this.configData = nVal[this.configNme]
        if (Object.keys(this.defaults).length == 0) return
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
.numbox {
  display: flex;
  align-items: center;
  margin-bottom: 10px;

  span {
    width: 80px;
    color: #999;
  }
}

/* font-size 12px */
.c_row-item {
  width: 100%;
}
</style>
