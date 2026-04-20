// utils/share.js
export class UniAppShareHelper {
    
    // 动态更新页面标题和OG标签
    static updatePageMeta(shareData) { 
        // 不改 document.title，避免覆盖系统配置中的商城标题
        // 动态更新或创建OG标签
        this.updateOGTag('og:title', shareData.title || '我的宜都-宜都市综合服务平台帖吧');
        this.updateOGTag('og:description', shareData.desc || '发现精彩内容');
        this.updateOGTag('og:image', shareData.imgUrl || 'https://hbdg.houjiemeishi.com/pic/images/share_img.png');
        this.updateOGTag('og:url', window.location.href.split('#')[0]);
         
    } 
    // 更新单个OG标签
    static updateOGTag(property, content) {
        let meta = document.querySelector(`meta[property="${property}"]`);
        
        if (!meta) {
            meta = document.createElement('meta');
            meta.setAttribute('property', property);
            document.head.appendChild(meta);
            console.log(`📝 创建新的OG标签: ${property}`);
        }
        
        meta.setAttribute('content', content);
        console.log(`✏️ 更新OG标签 ${property}: ${content}`);
    }
    
    // UniApp页面生命周期中调用
    static onPageShow(shareData) {
        // 延迟执行，确保DOM已更新
        setTimeout(() => {
            this.updatePageMeta(shareData);
        }, 100);
    }
    
    // 检查当前OG标签状态（调试用）
    static checkOGTags() {
        const tags = ['og:title', 'og:description', 'og:image', 'og:url'];
        tags.forEach(tag => {
            const meta = document.querySelector(`meta[property="${tag}"]`);
            console.log(`🔍 ${tag}:`, meta ? meta.getAttribute('content') : '未找到');
        });
    }
}
