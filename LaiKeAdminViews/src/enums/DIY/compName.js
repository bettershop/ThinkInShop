/**
 * 用于渲染 diy 组件 名称 
 */
export const COMP_NAME = {
    // 轮播图
    CAROUSEL_IMAGE: {
        value: 'swiperBg',
        label: 'diy.lbt'
    },
    // 导航组
    NAVIGATION_GROUP: {
        value: 'menus',
        label: 'diy.dhz'
    },
    // 商品分类
    PRODUCT_CATEGORY: {
        value: 'nav_bar',
        label: 'diy.spfl'
    },
    // 图片
    IMAGE: {
        value: 'swiperPicture',
        label: 'diy.tp'
    },
    // 搜索框
    SEARCH_BOX: {
        value: 'search_box',
        label: 'diy.ssk'
    },
    // 秒杀
    SECKILL: {
        value: 'home_seckill',
        label: 'diy.ms'
    },
    // 店铺
    STORE: {
        value: 'home_store',
        label: 'diy.dp'
    },
    // 广告
    ADVERTISMENT: {
        value: 'homeAdv',
        label: 'diy.gg'
    },
    // 背景
    BACKGROUND: {
        value: 'homeBg',
        label: 'diy.bj'
    },
    // 辅助空白
    BLANK_SPACE: {
        value: 'z_auxiliary_box',
        label: 'diy.fzkb'
    },
    // 辅助线
    GUIDELINE: {
        value: 'z_auxiliary_line',
        label: 'diy.fzx'
    }
}

/**
 * 根据组件标签获取 组件名称
 * @param {number} value - 组件标签
 * @returns {string} - 组件国际化名称
 */
export const getCompNameLabel = (value, _this) => {
    console.log(COMP_NAME, value)
    const status = Object.values(COMP_NAME).find(type => type.value === value.toString());
    console.log(status)
    return _this.$t(status.label) || '无效组件'
};