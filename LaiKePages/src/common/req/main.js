import Request from './request.js'

class methods extends Request {

    constructor() {
        super()
    }

    post(Parma) {
        return super.request({
            method: 'POST',
            ...Parma
        })
    }

    get(Parma) {
        return super.request({
            ...Parma
        })
    }

    /**
     * @description 图片上传
     * @param { String } filePath 选中图片路径
     * */
    upLoad(filePath, extData = {}) {
        return super.uploadimg(filePath, extData)
    }
    
    upLoadl(filePath, extData = {}) {
        return super.uploadimgl(filePath, extData)
    }
}

const REQ = new methods()

export default REQ
