<!-- 
    插件简介：
    使用方法和el-select一致
    v-model:双向绑定数据
    options:传递数据源
 -->
 <template>
  <el-select
    v-model="selected"
    v-bind="$attrs"
    w-full
    @change="change ? change($event) : false"
    @visible-change="visibleChange ? visibleChange($event) : false"
    @remove-tag="removeTag ? removeTag($event) : false"
    @clear="clear ? clear() : false"
    @blur="blur ? blur($event) : false"
    @focus="focus ? focus($event) : false"
  >
    <!-- 插槽是否要保留？ -->
    <!-- <template #header>xxxx</template> -->
    <el-option
      v-for="item in options"
      :key="item[valueFiled]"
      :label="item[labelFiled]"
      :value="item[valueFiled]"
    ></el-option>
  </el-select>
</template>
<script>
export default {
  name: "LSelect",
  props: {
    value: {
      type: [String, Array],
      default:  "",
    },
    options: {
      type: Array,
      default: () => [],
    },
    valueFiled: {
      type: String,
      default: "value",
    },
    labelFiled: {
      type: String,
      default: "label",
    },
    change: {
      type: Function,
      default: () => () => {},
    },
    visibleChange: {
      type: Function,
      default: () => () => {},
    },
    removeTag: {
      type: Function,
      default: () => () => {},
    },
    clear: {
      type: Function,
      default: () => () => {},
    },
    blur: {
      type: Function,
      default: () => () => {},
    },
    focus: {
      type: Function,
      default: () => () => {},
    },
  },
  components: {},
  data() {
    return {
      selected: "",
    };
  },
  computed: {
  },
  watch: {
    selected(newValue) {
      console.log('xxxxxx848484');
      this.$emit('input', newValue);
    },
    value: {
      handler(val) {
      console.log('xxxxxx848484');
        this.selected = val;
      },
      deep: true,
    },
  },
  created() {},
  mounted() {},
  methods: {},
};
</script>
<style scoped lang="less"></style>
