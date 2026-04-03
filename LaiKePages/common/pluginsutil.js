/**
 * 插件常量定义
 */
import { normalizeLangCode } from '@/common/lang/config.js';

export const PluginCodes = {
    
    /**
     * 优惠卷
     */
    COUPON: "coupon", // 

    /**
     * 店铺
     */
    MCH: "mch",//

    /**
     * 会员
     */
    MEMBER: "member", // 

    /**
     * 秒杀
     */
    SECONDS: "seconds",

    /**
     * 预售 
     */
    PRESELL: "presell",

    /**
     * 拼团
     */
    GOGROUP: "go_group",

    /**
     * 分销
     */
    DISTRIBUTION: "distribution",

    /**
     * 积分商城
     */
    INTEGRAL: "integral",

    /**
     * diy
     */
    DIY: "diy",

    /**
     * 竞拍
     */
    AUCTION: "auction",

    /**
     * 限时折扣
     */
    FLASHSALE: "flashsale",

    /**
     * 直播
     */
    LIVING: "living",

    /**
     * 种草
     */
    ZC: "zc",
    
};

/**
 * 插件名称国际化
 * 语言码：zh_CN, zh_TW, en_US, ja_JP, ru_RU, id_ID, ms_MY, fil_PH
 */
export const PluginCodeI18n = {
    zh_CN: {
        [PluginCodes.COUPON]: "优惠券",
        [PluginCodes.MCH]: "店铺",
        [PluginCodes.MEMBER]: "会员",
        [PluginCodes.SECONDS]: "秒杀",
        [PluginCodes.PRESELL]: "预售",
        [PluginCodes.GOGROUP]: "拼团",
        [PluginCodes.DISTRIBUTION]: "分销",
        [PluginCodes.INTEGRAL]: "积分商城",
        [PluginCodes.DIY]: "DIY",
        [PluginCodes.AUCTION]: "竞拍",
        [PluginCodes.FLASHSALE]: "限时折扣",
        [PluginCodes.LIVING]: "直播",
        [PluginCodes.ZC]: "种草",
    },
    zh_TW: {
        [PluginCodes.COUPON]: "優惠券",
        [PluginCodes.MCH]: "店鋪",
        [PluginCodes.MEMBER]: "會員",
        [PluginCodes.SECONDS]: "秒殺",
        [PluginCodes.PRESELL]: "預售",
        [PluginCodes.GOGROUP]: "拼團",
        [PluginCodes.DISTRIBUTION]: "分銷",
        [PluginCodes.INTEGRAL]: "積分商城",
        [PluginCodes.DIY]: "DIY",
        [PluginCodes.AUCTION]: "競拍",
        [PluginCodes.FLASHSALE]: "限時折扣",
        [PluginCodes.LIVING]: "直播",
        [PluginCodes.ZC]: "種草",
    },
    en_US: {
        [PluginCodes.COUPON]: "Coupon",
        [PluginCodes.MCH]: "Store",
        [PluginCodes.MEMBER]: "Member",
        [PluginCodes.SECONDS]: "Seckill",
        [PluginCodes.PRESELL]: "Pre-sale",
        [PluginCodes.GOGROUP]: "Group Buying",
        [PluginCodes.DISTRIBUTION]: "Distribution",
        [PluginCodes.INTEGRAL]: "Points Mall",
        [PluginCodes.DIY]: "DIY",
        [PluginCodes.AUCTION]: "Auction",
        [PluginCodes.FLASHSALE]: "Limited-time Discount",
        [PluginCodes.LIVING]: "Live Streaming",
        [PluginCodes.ZC]: "Discover",
    },
    ja_JP: {
        [PluginCodes.COUPON]: "クーポン",
        [PluginCodes.MCH]: "店舗",
        [PluginCodes.MEMBER]: "会員",
        [PluginCodes.SECONDS]: "秒殺",
        [PluginCodes.PRESELL]: "予約販売",
        [PluginCodes.GOGROUP]: "共同購入",
        [PluginCodes.DISTRIBUTION]: "ディストリビューション",
        [PluginCodes.INTEGRAL]: "ポイントモール",
        [PluginCodes.DIY]: "DIY",
        [PluginCodes.AUCTION]: "オークション",
        [PluginCodes.FLASHSALE]: "期間限定割引",
        [PluginCodes.LIVING]: "ライブ配信",
        [PluginCodes.ZC]: "おすすめ",
    },
    ru_RU: {
        [PluginCodes.COUPON]: "Купон",
        [PluginCodes.MCH]: "Магазин",
        [PluginCodes.MEMBER]: "Членство",
        [PluginCodes.SECONDS]: "Молниеносная распродажа",
        [PluginCodes.PRESELL]: "Предпродажа",
        [PluginCodes.GOGROUP]: "Групповая покупка",
        [PluginCodes.DISTRIBUTION]: "Дистрибуция",
        [PluginCodes.INTEGRAL]: "Магазин баллов",
        [PluginCodes.DIY]: "DIY",
        [PluginCodes.AUCTION]: "Аукцион",
        [PluginCodes.FLASHSALE]: "Ограниченная скидка",
        [PluginCodes.LIVING]: "Прямая трансляция",
        [PluginCodes.ZC]: "Рекомендации",
    },
    id_ID: {
        [PluginCodes.COUPON]: "Kupon",
        [PluginCodes.MCH]: "Toko",
        [PluginCodes.MEMBER]: "Anggota",
        [PluginCodes.SECONDS]: "Seckill",
        [PluginCodes.PRESELL]: "Prajual",
        [PluginCodes.GOGROUP]: "Beli Bareng",
        [PluginCodes.DISTRIBUTION]: "Distribusi",
        [PluginCodes.INTEGRAL]: "Toko Poin",
        [PluginCodes.DIY]: "DIY",
        [PluginCodes.AUCTION]: "Lelang",
        [PluginCodes.FLASHSALE]: "Diskon Waktu Terbatas",
        [PluginCodes.LIVING]: "Siaran Langsung",
        [PluginCodes.ZC]: "Rekomendasi",
    },
    ms_MY: {
        [PluginCodes.COUPON]: "Kupon",
        [PluginCodes.MCH]: "Kedai",
        [PluginCodes.MEMBER]: "Ahli",
        [PluginCodes.SECONDS]: "Jualan Kilat",
        [PluginCodes.PRESELL]: "Pra-jualan",
        [PluginCodes.GOGROUP]: "Pembelian Berkumpulan",
        [PluginCodes.DISTRIBUTION]: "Pengedaran",
        [PluginCodes.INTEGRAL]: "Kedai Mata",
        [PluginCodes.DIY]: "DIY",
        [PluginCodes.AUCTION]: "Lelong",
        [PluginCodes.FLASHSALE]: "Diskaun Masa Terhad",
        [PluginCodes.LIVING]: "Penstriman Langsung",
        [PluginCodes.ZC]: "Cadangan",
    },
    fil_PH: {
        [PluginCodes.COUPON]: "Kupon",
        [PluginCodes.MCH]: "Tindahan",
        [PluginCodes.MEMBER]: "Miyembro",
        [PluginCodes.SECONDS]: "Seckill",
        [PluginCodes.PRESELL]: "Pre-sale",
        [PluginCodes.GOGROUP]: "Group Buying",
        [PluginCodes.DISTRIBUTION]: "Distribusyon",
        [PluginCodes.INTEGRAL]: "Tindahan ng Puntos",
        [PluginCodes.DIY]: "DIY",
        [PluginCodes.AUCTION]: "Auction",
        [PluginCodes.FLASHSALE]: "Limitadong Diskwento",
        [PluginCodes.LIVING]: "Live Streaming",
        [PluginCodes.ZC]: "Mga Rekomendasyon",
    },
};

/**
 * 根据插件 code 获取名称（默认回退到中文）
 * @param {string} code 插件code
 * @param {string} langCode 语言码
 */
export function getPluginNameByCode(code, langCode = 'zh_CN') {
    if (!code) return '';
    const normalized = normalizeLangCode(langCode) || 'zh_CN';
    const langMap = PluginCodeI18n[normalized] || PluginCodeI18n.zh_CN;
    return langMap[code] || PluginCodeI18n.zh_CN[code] || code;
}

/**
 * 通过 PluginCodes 键名或 code 获取名称
 * @param {string} codeOrKey PluginCodes 的键名或 code
 * @param {string} langCode 语言码
 */
export function getPluginName(codeOrKey, langCode = 'zh_CN') {
    if (!codeOrKey) return '';
    const code = PluginCodes[codeOrKey] || codeOrKey;
    return getPluginNameByCode(code, langCode);
}

/**
 * 
 * @type {{}}
 */
export const PluginRouter = {
    DISTRIBUTION: "pagesC/goods/goodsDetailed",
    SECONDS: "pagesB/seckill/seckill_detail",
    GOGROUP: "pagesA/group/groupDetailed",
    PRESELL: "pagesC/preSale/goods/goodsDetailed",
    ZC: "pagesE/forumPost/postDetail/index",
    AUCTION: "pagesD/OrderBidding/ProductDetails",
    FLASHSALE: "pagesC/discount/discount_detail",
    INTEGRAL: "pagesB/integral/integral_detail",
    MEMBER: "pagesC/goods/goodsDetailed"
}

/**
 * Resolve a plugin key from either PluginCodes key or code value.
 * @param {string} codeOrKey
 * @returns {string}
 */
export function resolvePluginKey(codeOrKey) {
    if (!codeOrKey) return "";
    if (PluginRouter[codeOrKey]) return codeOrKey;
    if (PluginCodes[codeOrKey]) return codeOrKey;
    const keys = Object.keys(PluginCodes);
    for (let i = 0; i < keys.length; i += 1) {
        const key = keys[i];
        if (PluginCodes[key] === codeOrKey) {
            return key;
        }
    }
    return "";
}

export function buildUrlFromBackend(plugin, paramStr) {
    const key = resolvePluginKey(plugin)
    const page = PluginRouter[key]
    if (!page) return ""
    const normalizedPage = page.startsWith("/") ? page : `/${page}`
    if (!paramStr) return normalizedPage
    const query = String(paramStr).trim().replace(/^\?/, "")
    return query ? `${normalizedPage}?${query}` : normalizedPage
}

Object.freeze(PluginCodes);
Object.keys(PluginCodeI18n).forEach((lang) => {
    Object.freeze(PluginCodeI18n[lang]);
});
Object.freeze(PluginCodeI18n);
Object.freeze(PluginRouter);
