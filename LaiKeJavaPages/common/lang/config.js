// === 1. 引入语言包 ===
import zh_CN from './zh_CN.js'
import en_US from './en_US.js'     
import zh_TW from './zh_TW.js'

// #ifndef MP
import id_ID from './id_ID.js'
import ms_MY from './ms_MY.js'
import ja_JP from './ja_JP.js'
import ru_RU from './ru_RU.js'
import fil_PH from './fil_PH.js'
// #endif

// === 2. 标准语言模块映射（key 为标准语言 code）===
export const langModules = {
    zh_CN,
    zh_TW,
    en_US,
    // #ifndef MP
    id_ID,
    ms_MY,
    ja_JP,
    ru_RU,
    fil_PH,
    // #endif
}

// === 3. 扩展映射：支持别名（如 zh_HK → zh_TW）===
export const langAliasMap = {
    zh_HK: 'zh_TW',
    zh_SG: 'zh_CN',
    // 可继续扩展
}

// === 4. 获取最终语言模块（支持别名）===
export function getLangModule(langCode) {
    if (!langCode) return langModules.zh_CN

    // 先查别名
    const realCode = langAliasMap[langCode] || langCode

    // 再查模块
    return langModules[realCode] || langModules.zh_CN
}

// === 5. 标准化语言 code（用于缓存/URL 统一格式）===
export function normalizeLangCode(raw) {
    if (!raw) return null
    let code = String(raw).trim().toLowerCase()

    // 支持 zh-cn, zh_cn, en-us 等 → 转为 zh_CN, en_US
    code = code.replace(/[-_]/g, '_')
    const parts = code.split('_')
    if (parts.length === 2) {
        code = `${parts[0]}_${parts[1].toUpperCase()}`
    } else if (parts.length === 1) {
        // 单段语言如 'zh' → 默认简体
        const singleMap = {
            zh: 'zh_CN',
            en: 'en_US',
            us: 'en_US',
            ja: 'ja_JP',
            jp: 'ja_JP',
            tw: 'zh_TW',
            hk: 'zh_TW',
            ru: 'ru_RU',
            id: 'id_ID',
            ms: 'ms_MY',
            fil: 'fil_PH'
        }
        code = singleMap[code] || code
    }

    return code
}

// === 6. 设备语言映射表（用于 getDeviceLanguage）===
export const deviceLangToCode = {
    // 中文
    'zh': 'zh_CN', 'zh-cn': 'zh_CN', 'zh-hans': 'zh_CN', 'zh_sg': 'zh_CN',
    'zhtw': 'zh_TW', 'zhhk': 'zh_TW', 'tw': 'zh_TW', 'hk': 'zh_TW',
    // 英语
    'en': 'en_US', 'en-us': 'en_US', 'en-gb': 'en_US', 'us': 'en_US',
    // 日语
    'ja': 'ja_JP', 'ja-jp': 'ja_JP', 'jp': 'ja_JP',
    // 俄语
    'ru': 'ru_RU', 'ru-ru': 'ru_RU',
    // 印尼
    'id': 'id_ID', 'in': 'id_ID', 'id-id': 'id_ID',
    // 马来
    'ms': 'ms_MY', 'ms-my': 'ms_MY', 'my': 'ms_MY',
    // 菲律宾
    'fil': 'fil_PH', 'tl': 'fil_PH', 'ph': 'fil_PH',
    // 其他
    'th': 'th_TH',
    'vi': 'vi_VN'
}

// === 7. 外部翻译系统使用的语言标识映射（如 chinese_simplified）===
export const externalLangMap = {
    'zh_TW': 'chinese_traditional',
    'zh_CN': 'chinese_simplified',
    'ms_MY': 'malay',
    'ja_JP': 'japanese',
    'ru_RU': 'russian',
    'fil_PH': 'filipino',
    'id_ID': 'indonesian',
    'en_US': 'english',
};

// 提供一个安全获取函数
export function getExternalLangCode(langCode) {
    return externalLangMap[langCode] || 'chinese_simplified';
}
