export default {
    data() {
        return {
            isTranslated: false,
            _translateDoneHandler: null,
            _translateFallbackTimer: null
        }
    },
    methods: {
        markTranslated() {
            if (this.isTranslated) {
                return
            }
            this.isTranslated = true
        },
        clearTranslateWaiter() {
            if (this._translateDoneHandler) {
                window.removeEventListener('translationDone', this._translateDoneHandler)
                this._translateDoneHandler = null
            }
            if (this._translateFallbackTimer) {
                clearTimeout(this._translateFallbackTimer)
                this._translateFallbackTimer = null
            }
        }
    },
    onLoad() {
        // H5 页面等待全局翻译完成再显示
        if (process.env.UNI_PLATFORM === 'h5') {
            if (window.__LKT_TRANSLATED__) {
                this.markTranslated()
            } else {
                this._translateDoneHandler = () => {
                    this.markTranslated()
                    this.clearTranslateWaiter()
                }
                window.addEventListener('translationDone', this._translateDoneHandler)
                if (typeof window.__LKT_REQUEST_TRANSLATION__ === 'function') {
                    window.__LKT_REQUEST_TRANSLATION__('page-load')
                }
                // 兜底：避免极端情况下页面一直不可见
                this._translateFallbackTimer = setTimeout(() => {
                    this.markTranslated()
                    this.clearTranslateWaiter()
                }, 2500)
            }
        } else {
            this.markTranslated()
        }
    },
    onUnload() {
        // #ifdef H5
        this.clearTranslateWaiter()
        // #endif
    },
    beforeDestroy() {
        // #ifdef H5
        this.clearTranslateWaiter()
        // #endif
    }
}
