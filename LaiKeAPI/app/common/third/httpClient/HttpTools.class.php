<?php

class HttpTools
{
    public static function httpsRequest($url, $data = null)
    {
        // 1.初始化会话
        $ch = curl_init();
        // 设置请求的url
        curl_setopt($ch, CURLOPT_URL, $url);

        curl_setopt($ch, CURLOPT_HTTPHEADER, array(
            "User-Agent: {Mozilla/5.0 (Windows NT 6.1; WOW64; rv:26.0) Gecko/20100101 Firefox/26.0}",
            "Accept: {text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8}",
            "Accept-Language: {zh-cn,zh;q=0.8,en-us;q=0.5,en;q=0.3}",
            "Cookie:{cq=ccp%3D1; cna=a7suCzOmSTECAXgg9iCf4AtX; t=671b2069c7e8ac444da66d664a397a5f; tracknick=%5Cu4F0D%5Cu6653%5Cu8F8901;
        _tb_token_=nDiU1vCuzFd0; cookie2=c54709ffbe04a5ccb80283c34d6b00fa;
        pnm_cku822=128WsMPac%2FFS4KgNn%2BYfhzduo4U2NC0zh9cAS4%3D%
        7CWUCLjKhqr873bOIFQcMecSw%3D%7CWMEKRlV%
        2B3D9a6XWaidNWNQOSWXwaXugvQHzhxALh%7CX0
        YLbX78NUR2b2DHoxnIqZENQqR35TBZbfQ5vooI0b6GHZA3U1kr%7CXkdILog
        Cr878ZK9I%2B%2FE3QjAD3lFJJaAZRA%3D%3D%7CXUeMwMR2s%
        2BTUQk8IPP5TNgWfUjQwonccMCxihTa0fRYgtjgfa4j6%7CXMY
        K7F8liOvH3hMUpzXkiaU%2FJw%3D%3D}",
        ));
        // 保证返回成功的结果是服务器的结果
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
        if (!empty($data)) {
            // 发送post请求
            curl_setopt($ch, CURLOPT_POST, 1);
            // 设置发送post请求参数数据
            curl_setopt($ch, CURLOPT_POSTFIELDS, $data);
        }
        // 3.执行会话; $result是微信服务器返回的JSON字符串
        $result = curl_exec($ch);
        // 4.关闭会话
        curl_close($ch);
        return $result;

    }

}
