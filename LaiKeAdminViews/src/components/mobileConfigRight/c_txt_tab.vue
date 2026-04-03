<template>
  <div class="txt_tab" v-if="configData">
    <div class="c_row-item">
      <Col class="c_label">
      {{configData.title}}
      <span>{{ displayVal }}</span>
      </Col>
      <Col class="color-box">
      <RadioGroup v-model="configData.type" type="button" @on-change="radioChange($event)">
        <Radio :label="key" v-for="(radio,key) in configData.list" :key="key">
          <span class="iconfont-diy" :class="radio.icon" v-if="radio.icon"></span>
          <span v-else>{{radio.val}}</span>
        </Radio>
      </RadioGroup>
      </Col>
    </div>
  </div>

</template>

<script>
export default {
  name: 'c_txt_tab',
  props: {
    configObj: {
      type: Object
    },
    configNme: {
      type: String
    }
  },
  data() {
    return {
      defaults: {},
      configData: {}
    }
  },
  created() {
    this.defaults = this.configObj

    if (!this.configObj || Object.keys(this.configObj).length === 0) return

    if (this.configObj[this.configNme]) {
      this.configData = this.configObj[this.configNme]
    } else {
      this.configData = this.defaults[this.defaults.link_key][this.configNme]
    }

  },
  computed: {
    displayVal() {
      const { configData } = this;
      if (!configData || !configData.list) return '';
      const item = configData.list[configData.type];
      return item && item.val !== undefined ? item.val : '';
    }
  },
  watch: {
    configObj: {
      handler(nVal, oVal) {

        try {
          if (!nVal || Object.keys(nVal).length == 0) return
          console.log('c_txt_tab', nVal, nVal.link_key, this.configNme)
          this.defaults = nVal
          if (nVal[this.configNme]) {
            this.configData = nVal[this.configNme]
          } else {
            this.configData = nVal[nVal.link_key]?.[this.configNme]
          }
        } catch (error) {
          console.error(error)
        }

      },
      immediate: true,
      deep: true
    }
  },
  methods: {
    radioChange(e) {
      // this.$emit('getConfig', { name: 'radio', values: e })
    }
  }
}
</script>

<style scoped lang="stylus">
.txt_tab {
  margin-top: 20px;
}

.c_row-item {
  margin-bottom: 20px;
}

.row-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.iconfont {
  font-size: 18px;
}
</style>
