class Config {}

if (process.env.NODE_ENV === "development") {
  // 本地
  Config.baseUrl = 'api'
} else {
  // 线上
  Config.baseUrl = process.env.VUE_APP_BASE_API
}

export default Config;
