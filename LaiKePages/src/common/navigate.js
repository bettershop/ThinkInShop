export const navigator = {
    async to (url, checkLogin = false, me) {
        if (checkLogin) {
            me.$refs.lktAuthorizeComp.handleAfterAuth(me, '/pagesD/login/newLogin?landing_code=1', function () {
                uni.navigateTo({
                    url,
                    success () {
                        return Promise.resolve()
                    }
                })
            })
        } else {
            uni.navigateTo({
                url,
                success () {
                    return Promise.resolve()
                }
            })    
        }
    },
    back () {
        console.log(getCurrentPages());
         
        if (getCurrentPages().length > 1) {
            uni.navigateBack({
                delta: 1
            })
        } else {
            navigator.toHome()
            
        }
    },
    switchTab (url) {
        uni.switchTab({
            url
        })
    },
    toHome () {
        uni.redirectTo({
            url: '/pages/shell/shell?pageType=home'
        })
    }
}
