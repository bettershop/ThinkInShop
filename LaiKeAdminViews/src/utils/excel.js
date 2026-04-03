/**
 * 下载excel
 * @param {blob} fileArrayBuffer 文件流
 * @param {String} filename 文件名称
 */
export const downloadXlsx = (res, filename) => {
  if (res === null || res === undefined) return false
  var blob = new Blob([res.data], {
    type: 'application/vnd.ms-excel;charset=utf-8'
  }) //application/vnd.openxmlformats-officedocument.spreadsheetml.sheet这里表示xlsx类型
  var downloadElement = document.createElement('a')
  var href = window.URL.createObjectURL(blob) //创建下载的链接
  downloadElement.href = href
  // downloadElement.download = getQueryStringChinese(res.headers['content-disposition'].split(';')[1].split('=')[1].split('.')[0])+'.xlsx'; //下载后文件名
  downloadElement.download =
    filename != null
      ? decodeURI(filename)
      : decodeURI(res.headers['content-disposition']).split('=')[1] //下载后文件名
  document.body.appendChild(downloadElement)
  downloadElement.click() //点击下载
  document.body.removeChild(downloadElement) //下载完成移除元素
  window.URL.revokeObjectURL(href) //释放掉blob对象
  return true
}
