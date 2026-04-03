<template>
  <!--
  拷贝至 html中
  <navLabel
      ref="navLabel"
      :nav_label_type="nav_label_type"
      :nav_label_list="nav_label_list"
      @_choose="nav_choose"
  >
  </navLabel>
  -->
  <!--
  拷贝至 data中
  //导航类型 1不需要权限 2需要权限认证
  nav_label_type: 1,
  //导航数据 name:导航文字 label:绑定值 value:数量 disabled:是否禁用 routerTypes是否启用权限判断
  nav_label_list: [
    {
      id: 'id1',
      name: '标签1',
      value: 0,
      disabled: false,
    },
    {
      id: 'id2',
      name: '标签2',
      value: 0,
      disabled: false,
      routerType: false, //nav_label_type==2 时需要传
      routerName: '',    //nav_label_type==2 时需要传
      routerValue: 0,    //nav_label_type==2 时需要传
    },
  ],
  -->
  <!--
  拷贝至 methods中
  /**
   * label导航 点击事件
   * @param {Object} id ：当前选中的id
   */
  nav_choose(id){
    console.log("父组件nav_choose～", id)
    //this.$router.push('/plug_ins/preSale/goodsList')
  },
  -->
  <div class="nav-label">
    <!-- 不需要权限 -->
    <template v-if="nav_label_type == 1">
      <el-radio-group
        @input="_choose(nav_choose)"
        v-model="nav_choose"
        :fill="nav_label_style.btnColor"
        :text-color="nav_label_style.sizeColor"
      > 
        <el-radio-button
          v-for="(item, index) in nav_label_list"
          :key="item.id"
          :disabled="item.disabled"
          :label="item.id"
        >
          {{ item.name }}
          <span
            v-if="item.value && !item.kuohao"
            class="nav-label-span"
            :class="nav_choose == item.id ? 'active' : ''"
            >({{ item.value }})</span
          >
          <span
            v-if="item.value && item.kuohao"
            class="nav-label-span"
            :class="nav_choose == item.id ? 'active' : ''"
            >{{ item.value }}</span
          >
        </el-radio-button>
      </el-radio-group>
    </template>
    <!-- 需要权限（当前身份是否有此功能） -->
    <template v-if="nav_label_type == 2">
      <el-radio-group
        @input="_choose(nav_choose)"
        v-model="nav_choose"
        :fill="nav_label_style.btnColor"
        :text-color="nav_label_style.sizeColor"
      >
        <el-radio-button
          v-for="(item, index) in nav_label_list"
          :key="item.id"
          v-if="handleTabLimits(routerList, item.routerName, item.routerValue)"
          :disabled="item.disabled"
          :label="item.id"
        >
          {{ item.name }}
          <span
            v-if="item.value"
            class="nav-label-span"
            :class="nav_choose == item.id ? 'active' : ''"
            >({{ item.value }})</span
          >
        </el-radio-button>
      </el-radio-group>
    </template>
  </div>
</template>

<script>
export default {
  name: 'navLabel',
  data () {
    return {
      routerList: JSON.parse(sessionStorage.getItem('tabRouter')), //身份权限
      nav_choose: '' //当前选中导航标签
    }
  },
  computed: {},
  props: {
    //导航类型 1不需要权限 2需要权限认证
    nav_label_type: {
      type: Number,
      default: 0
    },
    //必传 导航数据 name:导航文字 label:绑定值 value:数量 disabled:是否禁用 routerTypes是否启用权限判断
    nav_label_list: {
      type: Array,
      default: () => [
        {
          id: 'id',
          name: '标签1',
          value: 0,
          ischoose: false,
          disabled: false,
          routerType: false,
          routerName: '',
          routerValue: 0
        }
      ]
    },
    //非必传
    nav_label_style: {
      type: Object,
      default: () => ({
        btnColor: '#2890ff',
        sizeColor: '#ffffff'
      })
    }
  },
  created () {
    console.log('组件创建～', this.nav_label_type, this.nav_label_list)
    //默认选中第一个导航标签 如需其他则另增参数判断,默认初始化ischoose为true的
    if (this.nav_label_list.length) {
      this.nav_choose = this.nav_label_list.find(
        item => item.ischoose == true
      ).id
    }
  },
  mounted () {
    console.log('组件挂载～')
  },
  methods: {
    _choose (id) {
      console.log('选中了标签～', id)
      this.$emit('_choose', id)
    }
  }
}
</script>

<style lang="less" scoped>
// 修改样式请在父组件用deep形式修改 此为默认公共样式
.nav-label {
  /deep/.el-radio-button__inner {
    width: 150px;
    height: 42px;
    font-size: 16px;
    line-height: 17px;
    display: flex;
    align-items: center;
    justify-content: center;
  }
  .nav-label-span {
    margin-left: 4px;
    color: #ff453d;
  }
  .active {
    color: #ffffff  
  }
}
</style>
