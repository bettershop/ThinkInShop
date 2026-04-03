const VUE_DIR_NAME = process.env.VUE_APP_DIR_NAME;


// let defaultUrl = window.location.protocol + '//' + window.location.host + VUE_DIR_NAME;

let defaultUrl = "https://xiaochengxu.houjiemeishi.com/V3/";

window.UEDITOR_HOME_URL = window.location.protocol + '//' + window.location.host + VUE_DIR_NAME

const Setting = {
  // 接口请求地址
  apiBaseURL: defaultUrl
}

export default Setting;
