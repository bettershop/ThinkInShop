// utils/loadTcPlayer.js
let tcPlayerPromise = null;

function getGlobalTcPlayer() {
    const tcPlayerCtor = window.TCPlayer || window.TcPlayer;
    if (typeof tcPlayerCtor === 'function') {
        return tcPlayerCtor;
    }
    if (tcPlayerCtor && typeof tcPlayerCtor.default === 'function') {
        return tcPlayerCtor.default;
    }
    return null;
}

export function loadTcPlayer() {
    if (!tcPlayerPromise) {
        tcPlayerPromise = new Promise((resolve, reject) => {
            // 防止重复加载
            const existed = getGlobalTcPlayer();
            if (existed) {
                return resolve(existed);
            }

            const cssUrl = '/static/tcplayer/tcplayer.min.css';
            const jsUrl = '/static/tcplayer/tcplayer.v5.1.0.min.js';

            let cssLoaded = false;
            let jsLoaded = false;

            // 加载 CSS
            const link = document.createElement('link');
            link.rel = 'stylesheet';
            link.href = cssUrl;
            link.onload = () => {
                cssLoaded = true;
                checkComplete();
            };
            link.onerror = () => {
                console.warn('TcPlayer CSS 加载失败，但继续加载 JS');
                cssLoaded = true; // 即使 CSS 失败也不阻塞
                checkComplete();
            };
            document.head.appendChild(link);

            // 加载 JS
            const script = document.createElement('script');
            script.src = jsUrl;
            script.async = true;
            script.onload = () => {
                jsLoaded = true;
                checkComplete();
            };
            script.onerror = (err) => {
                reject(new Error('TcPlayer JS 加载失败'));
            };
            document.head.appendChild(script);

            function checkComplete() {
                // 等待 JS 加载完成即可（CSS 可选）
                const tcPlayer = getGlobalTcPlayer();
                if (jsLoaded && tcPlayer) {
                    resolve(tcPlayer);
                }
            }
        });
    }

    return tcPlayerPromise;
}
