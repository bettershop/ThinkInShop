/**
 * 获取系统基础配置（包含商城名称）
 */
import req from "@/common/req/main.js";

const SYSTEM_CONFIG_CACHE_KEY = "lkt_system_config_cache";
const SYSTEM_CONFIG_CACHE_TS_KEY = "lkt_system_config_cache_ts";
const DEFAULT_CACHE_MAX_AGE = 1 * 60 * 1000;

let memoryCache = null;
let memoryCacheTs = 0;
let pendingPromise = null;

function normalizeSystemInfo(systemInfo = {}) {
    const mallName = (systemInfo && (
        systemInfo.name ||
        systemInfo.mall_name ||
        systemInfo.store_name ||
        systemInfo.appTitle ||
        systemInfo.title
    )) || "";

    if (mallName) {
        uni.setStorageSync("mall_name", mallName);
        uni.setStorageSync("appTitle", mallName);
    }

    return {
        ...systemInfo,
        name: mallName || systemInfo.name || ""
    };
}

function isCacheValid(ts, maxAge) {
    if (!ts || !maxAge) return false;
    return Date.now() - ts < maxAge;
}

function readStorageCache(maxAge) {
    const ts = Number(uni.getStorageSync(SYSTEM_CONFIG_CACHE_TS_KEY) || 0);
    if (!isCacheValid(ts, maxAge)) return null;

    const cache = uni.getStorageSync(SYSTEM_CONFIG_CACHE_KEY);
    if (!cache || typeof cache !== "object") return null;
    return {
        cache,
        ts
    };
}

function writeStorageCache(cache) {
    const now = Date.now();
    uni.setStorageSync(SYSTEM_CONFIG_CACHE_KEY, cache);
    uni.setStorageSync(SYSTEM_CONFIG_CACHE_TS_KEY, now);
    memoryCache = cache;
    memoryCacheTs = now;
}

/**
 * @param {Object} options
 * @param {Boolean} options.forceRefresh - 是否强制刷新
 * @param {Number} options.maxAge - 缓存有效期，单位ms
 */
export function getSystemConfig(options = {}) {
    const {
        forceRefresh = false,
        maxAge = DEFAULT_CACHE_MAX_AGE
    } = options;

    if (!forceRefresh && memoryCache && isCacheValid(memoryCacheTs, maxAge)) {
        return Promise.resolve(memoryCache);
    }

    if (!forceRefresh && pendingPromise) {
        return pendingPromise;
    }

    if (!forceRefresh) {
        const storageCache = readStorageCache(maxAge);
        if (storageCache && storageCache.cache) {
            memoryCache = storageCache.cache;
            memoryCacheTs = storageCache.ts;
            return Promise.resolve(memoryCache);
        }
    }

    pendingPromise = new Promise((resolve, reject) => {
        let data = {
            api: "app.index.GetBasicConfiguration"
        };

        req.post({ data }).then((res) => {
            if (res && res.code === 200) {
                const systemInfo = res.data && res.data.list ? res.data.list : {};
                const normalized = normalizeSystemInfo(systemInfo);
                writeStorageCache(normalized);
                resolve(normalized);
            } else {
                reject((res && (res.message || res.msg)) || "获取系统配置失败");
            }
        }).catch((err) => {
            reject(err);
        }).finally(() => {
            pendingPromise = null;
        });
    });

    return pendingPromise;
}

/**
 * 设置页面标题，优先使用动态获取的商城名称
 * @param {String} defaultTitle - 默认标题，获取不到商城名称时使用
 */
export function setPageTitle(defaultTitle = '') {
    const mallName = uni.getStorageSync('mall_name');
    const appTitle = uni.getStorageSync('appTitle');
    const title = mallName || appTitle || defaultTitle;
    if (!title) {
        // #ifdef H5
        document.title = " ";
        // #endif
        return;
    }
    
    // #ifdef MP-WEIXIN || MP-ALIPAY || MP-BAIDU || MP-TOUTIAO || MP-QQ
    uni.setNavigationBarTitle({
        title: title
    });
    // #endif
    
    // #ifdef H5
    document.title = title;
    // #endif
    
    // #ifdef APP-PLUS
    plus.navigator.setStatusBarBackground('#ffffff');
    plus.navigator.setTitle({
        title: title
    });
    // #endif
}
