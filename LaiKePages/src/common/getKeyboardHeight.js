function onKeyboardHeightChange(callback) {
  // #ifdef APP-PLUS
  uni.onKeyboardHeightChange(callback);
  // #endif

  // #ifdef H5
  let initialHeight = window.visualViewport ? window.visualViewport.height : window.innerHeight;
  const handler = () => {
    const currentHeight = window.visualViewport ? window.visualViewport.height : window.innerHeight;
    const height = initialHeight - currentHeight;
    callback({ height });
  };
  window.visualViewport?.addEventListener('resize', handler);
  window.addEventListener('resize', handler);
  // #endif
}
// 导出工具函数
export { 
  onKeyboardHeightChange, 
};
