import qs from "qs";
import request from "../https";
import { error } from "jquery";
let { entries } = Object;


//添加至草稿箱
export const addDrafts = request.throttle(params => {

    let formData = new FormData()
    for (let [key, value] of entries(params)) {
        if (value !== null && value !== undefined) {
            formData.append(key, value)
        }
    }
    return request({
        method: "post",
        data: formData,
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    });
});


// 草稿箱列表
export const getList = params => {
    return request({
        method: "post",
        params
    });
};

// 草稿箱删除
export const delListItemById = request.throttle(params => {
    return request({
        method: "post",
        params
    });
});

// 草稿箱编辑回显
export const queryDraftsInfo = params => {
    return request({
        method: "post",
        params
    });
};

