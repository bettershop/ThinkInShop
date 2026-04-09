package com.laiketui.common.utils.weixin;

/**
 * 微信v3支付bean
 *
 * @author sunH
 */
public class WxV3PayConfig
{
    //平台证书序列号
    public static String mchSerialNo = "67F0079C7CDE2527484B69BD9249D739ACF1EDCB";

    //appID
    public static String APP_ID = "wx5ae2bb641565e4a3";

    //服务商商户id
    public static String Mch_ID = "1516978921";

    //子商户号
    public static String Son_Mch_ID = "1644691870";

    // API V3密钥
    public static String apiV3Key = "laiketuilaiketuilaiketuilaiketui";

    // 商户API V3私钥
    public static String privateKey = "-----BEGIN PRIVATE KEY-----\n" +
            "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCxGjeoFn6XeVRe\n" +
            "jUyV5VUUL1tRmfYADrzniV8TeRk/xsGUQqlY7AxhJBWTf8RIxZi50ZFJCatAk4cm\n" +
            "IG6KPh/sw+YzGhWOW4oT7QAUvI+jL6Ql50OXxip7lscvKlrAwiyI1hUjmdQZN8rD\n" +
            "p2HyQ0KvgksqiZ+TAfEvMa0KGn2GZ0v8h4z0ni1ik0wHg01FHolQiGjuLxO9hbjC\n" +
            "6r5bXyI2btbNzVB53CjWIWz6P6eil5MvrE1SuBY10P/xw4oSShsflnttTMbi/2fM\n" +
            "+RmThzgjUtqQ1T4MU3X8zr39wDD0q05Uz6fZvfVe+3OZK0avPppPvuFwyc1j8Kd4\n" +
            "3Koep/ZBAgMBAAECggEAf1FDtbkGJMr6lkaqShBmC27IDW/LY9s0fpdJHGywdCLk\n" +
            "wzrxF5YKiEMyp6jwaQh0dRFtir5CuNfWDti93vAubz2d3hrgaEjyiMSWjFHKSRxU\n" +
            "u1L4SsIRhGuuof+m4nA1J+IPrFpIGiD7yZApX8puxFG8y8XITBzXUIA7z8mfd+DU\n" +
            "CMre/i8hlV1xX+YiUArexpohnE1j+hfa2t4ld9wIWlicOgcXXkBHDdzLBtwSJ/bY\n" +
            "Te+NAuvxMGEWcRjr9XyrR+JV0MOKWUWcjvKrnwCGXgmcb5WxubIm8guRcUhZWX5x\n" +
            "ctcFkYh4sm9YtopDA9Fq9xGSaRtkiKsKnsshhBgBgQKBgQDijZ2bLpY4fLMGQBep\n" +
            "kTDTaMLr/DUzoQR56jDuvtl7m46NlOhnYzdXeRcdGv2paGHvdf9FvoJFQEcLuCky\n" +
            "FfCpJlLVJEf8S3FGM+Ii0yzAMu6P/tdkMBEuliD7+R6o4jiaN6j33un9KgBlvBMW\n" +
            "k0iIMYKMlZUhyXCunoGtAZTLGQKBgQDIHyo8fbg8Viw4RkmaILmhViUvGccD4Ngz\n" +
            "PIZ45AJ1yYYmXhMkojxVOdpZlt1xlUhXNGaPp/vDQ7/A2r7+Q3CIsbHQBTitLkjG\n" +
            "/qBufRWS0lJ6LyGmyZ9JzTrytautmPmX/kkaEB3jLIPNLq45RRdoQp3H79U22zHq\n" +
            "m1v52MQRaQKBgQDQMnqLONXeVWKkNNRKBV7MebLf8Wfm6WtWqoyLayIsQPlRTW4C\n" +
            "TSB3p5VBLyO7wmx2BocwTfIjt/1SXMym9Zizqb9nlq78wUv4ywdfH5g5jNTFydmh\n" +
            "fbbcjCUFypuNck+4EbybBeUcvbOdyqf0ECgQ2QbzMSfa4+dE4e+j0RcvSQKBgAqB\n" +
            "GEZ0kucX3tpVView8A136BvAzqF1e+O0mZpe3YIxQclhxr91hJU0pDFvqc/8df+d\n" +
            "0IRZ3O0y8S2+TagrAg0qxpqUq5TriQZo9yLVjfxW7d1b+/g1PRcynpV+07QZA4Dm\n" +
            "6A9mKStzereaa6q15bSjSoTkIdzvRH98CCflKOWBAoGAFU84HpXLd3iQzUm4/flc\n" +
            "mE5WcvHSVteXrM4yrztGVgtqZcEheIZPfW0dp671ijBdwadHCKPoEV9q5qNPCb/c\n" +
            "fVmvdO7eLhmyHld2uYBCP9+xLHJtKLL9m92pINa+hYS4z4zogF2SJbz5S5U2U8rd\n" +
            "IzqgdLViR5QWLFJ0eoJ6TDk=\n" +
            "-----END PRIVATE KEY-----";

    public static void main(String[] args)
    {
        String privateKeys = privateKey.replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");
        System.out.println(privateKeys);
    }
}
