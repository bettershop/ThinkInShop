<?php
namespace app\common;

require_once "../vendor/autoload.php";

use think\facade\Db;
use PaypalServerSdkLib\PaypalServerSdkClientBuilder;
use PaypalServerSdkLib\Logging\LoggingConfigurationBuilder;
use PaypalServerSdkLib\Logging\RequestLoggingConfigurationBuilder;
use PaypalServerSdkLib\Logging\ResponseLoggingConfigurationBuilder;
use Psr\Log\LogLevel;
use PaypalServerSdkLib\Models\Builders\PaymentTokenRequestBuilder;
use PaypalServerSdkLib\Models\Builders\PaymentTokenRequestPaymentSourceBuilder;
use PaypalServerSdkLib\Environment;
use PaypalServerSdkLib\Authentication\ClientCredentialsAuthCredentialsBuilder; // 用于初始化ClientCredentialsAuth安全凭据的实用程序类。
use PaypalServerSdkLib\Models\Builders\OrderRequestBuilder;
use PaypalServerSdkLib\Models\Builders\PurchaseUnitRequestBuilder;
use PaypalServerSdkLib\Models\Builders\AmountWithBreakdownBuilder;
use PaypalServerSdkLib\Models\CheckoutPaymentIntent;
use PaypalServerSdkLib\Models\Builders\OrderApplicationContextBuilder;
use PaypalServerSdkLib\Models\Builders\OAuthTokenBuilder;

class PayPalService 
{
    protected $client;
    
    public function __construct($client_id,$client_secret) 
    {
        $client = PaypalServerSdkClientBuilder::init() // Paypal服务器Sdk客户端构建器
            ->clientCredentialsAuthCredentials( // 客户端凭据身份验证凭据
                ClientCredentialsAuthCredentialsBuilder::init( // ClientCredentialsAuthCredentialsBuilder的初始化器
                    $client_id,
                    $client_secret
                )
            )
            ->environment(Environment::SANDBOX) // API可用的环境 Sandbox:沙盒 Production:生产
            ->loggingConfiguration( // 日志配置
                LoggingConfigurationBuilder::init() // 日志配置生成器
                    ->level(LogLevel::INFO) // 测井水平设置器。有关日志级别的可能值，请参阅Psr\Log\LogLevel.php
                    ->requestConfiguration( // 请求配置
                        RequestLoggingConfigurationBuilder::init()->body(true) // 请求日志配置生成器
                    )
                    ->responseConfiguration( // 响应配置
                        ResponseLoggingConfigurationBuilder::init()->headers(true) // 响应记录配置生成器的初始化器。
                    )
            )
            ->build();
    }

    // 创建订单
    public function createOrder($data) 
    {
        $order_types = $data['order_types'];
        $collect = [
            'body' => OrderRequestBuilder::init( // 初始化新的订单请求生成器对象。
                CheckoutPaymentIntent::CAPTURE, // 在订单创建后立即获取付款或授权付款的意图。
                [
                    PurchaseUnitRequestBuilder::init( // 初始化新的采购单元请求生成器对象。
                        AmountWithBreakdownBuilder::init( // 使用细分生成器对象初始化新的金额。
                            $data['currency'],
                            $data['amount']
                        )->build()
                    )
                    ->referenceId($data['real_sno']) // 支付单号
                    ->build()
                ]
            )
                ->applicationContext(
                    OrderApplicationContextBuilder::init()
                        ->returnUrl($data['return_url']) // 支付成功时跳转链接，可以支持异步
                        ->cancelUrl($data['cancel_url']) // 取消支付时跳转链接
                        ->build()
                )
                ->build(),
            'prefer' => 'return=minimal'
        ];
        
        $client = PaypalServerSdkClientBuilder::init() // Paypal服务器Sdk客户端构建器
            ->clientCredentialsAuthCredentials( // 客户端凭据身份验证凭据
                    ClientCredentialsAuthCredentialsBuilder::init( // ClientCredentialsAuthCredentialsBuilder的初始化器
                        $data['client_id'],
                        $data['client_secret']
                    )
                )
            ->environment(Environment::SANDBOX) // API可用的环境 Sandbox:沙盒 Production:生产
            ->loggingConfiguration( // 日志配置
                LoggingConfigurationBuilder::init() // 日志配置生成器
                    ->level(LogLevel::INFO) // 测井水平设置器。有关日志级别的可能值，请参阅Psr\Log\LogLevel.php
                    ->requestConfiguration( // 请求配置
                        RequestLoggingConfigurationBuilder::init()->body(true) // 请求日志配置生成器
                    )
                    ->responseConfiguration( // 响应配置
                        ResponseLoggingConfigurationBuilder::init()->headers(true) // 响应记录配置生成器的初始化器。
                    )
            )
            ->build();

        ob_start();
        $apiResponse = $client->getOrdersController()->createOrder($collect);
        ob_end_clean();

        if ($apiResponse->isSuccess()) 
        {
            $order = $apiResponse->getResult();
            $order1 = json_encode($order);
            $order2 = json_decode($order1,true);

            $paypal_id = $order2['id'];
            $order3 = $order2['links'];
            foreach ($order3 as $k => $v)
            {
                if($v['rel'] == 'approve')
                {
                    $url = $v['href'];
                }
            }

            if($order_types == 'CZ' || $order_types == 'DJ')
            {
                $real_sno = $data['real_sno'];
                $sql_d = "select data from lkt_order_data where trade_no = '$real_sno' ";
                $r_d = Db::query($sql_d);
                if($r_d)
                {
                    $data_d = unserialize($r_d[0]['data']);
                    $data_d['paypal_id'] = $paypal_id;
                    $data_d1 = serialize($data_d);

                    $data_where_1 = array('trade_no'=>$data['real_sno']);
                    $data_update_1 = array('data'=>$data_d1);
                    $r_1 = Db::name('order_data')->where($data_where_1)->update($data_update_1);
                }
            }
            else
            {
                $data_where_1 = array('real_sno'=>$data['real_sno']);
                $data_update_1 = array('paypal_id'=>$paypal_id);
                $r_1 = Db::name('order')->where($data_where_1)->update($data_update_1);
            }

            $rew = array('data'=>$url);
            return $rew;
        } 
        else 
        {
            $errors = $apiResponse->getResult();
            echo json_encode(array('code'=>109,'message'=>$errors));
            exit();
        }
    }
    
    // 捕获订单的付款
    public function capture($data) 
    {
        $collect = [
            'id' => $data['paypal_id'],
            'prefer' => 'return=minimal'
        ];
        
        $client = PaypalServerSdkClientBuilder::init() // Paypal服务器Sdk客户端构建器
            ->clientCredentialsAuthCredentials( // 客户端凭据身份验证凭据
                    ClientCredentialsAuthCredentialsBuilder::init( // ClientCredentialsAuthCredentialsBuilder的初始化器
                        $data['client_id'],
                        $data['client_secret']
                    )
                )
            ->environment(Environment::SANDBOX) // API可用的环境 Sandbox:沙盒 Production:生产
            ->loggingConfiguration( // 日志配置
                LoggingConfigurationBuilder::init()// 日志配置生成器
                    ->level(LogLevel::INFO) // 测井水平设置器。有关日志级别的可能值，请参阅Psr\Log\LogLevel.php
                    ->requestConfiguration( // 请求配置
                        RequestLoggingConfigurationBuilder::init()->body(true) // 请求日志配置生成器
                    )
                    ->responseConfiguration( // 响应配置
                        ResponseLoggingConfigurationBuilder::init()->headers(true) // 响应记录配置生成器的初始化器。
                    )
            )
            ->build();

        ob_start();
        $apiResponse = $client->getOrdersController()->captureOrder($collect);
        ob_end_clean();

        if ($apiResponse->isSuccess()) 
        {
            $order = $apiResponse->getResult();
            $order1 = json_encode($order);
            $order2 = json_decode($order1,true);

            return $order2;
        } 
        else 
        {
            $errors = $apiResponse->getResult();
            echo json_encode(array('code'=>109,'message'=>$errors));
            exit();
        }
    }
    
    // 退款
    public static function refund($data)
    {
        $collect = [
            'captureId' => $data['capture_id'],
            'prefer' => 'return=minimal'
        ];
        
        $client = PaypalServerSdkClientBuilder::init() // Paypal服务器Sdk客户端构建器
            ->clientCredentialsAuthCredentials( // 客户端凭据身份验证凭据
                ClientCredentialsAuthCredentialsBuilder::init( // ClientCredentialsAuthCredentialsBuilder的初始化器
                    $data['client_id'],
                    $data['client_secret']
                )
                ->oAuthToken(
                    OAuthTokenBuilder::init(
                        'A21AAI_pswzkPabe25F64ErOjGcF5enF0zSXkma4t3Cr3Mg55MGWW2NBmVdcyBFhBVGttSXBSw1klNxjWXO61ask3EATMqCuw',
                        'Bearer'
                    )
                    ->expiresIn(30568)
                    ->scope('https://uri.paypal.com/services/payments/futurepayments https://uri.paypal.com/services/invoicing https://uri.paypal.com/services/vault/payment-tokens/read https://uri.paypal.com/services/disputes/read-buyer https://uri.paypal.com/services/payments/realtimepayment https://uri.paypal.com/services/disputes/update-seller openid https://uri.paypal.com/services/payments/payment/authcapture https://uri.paypal.com/services/disputes/read-seller Braintree:Vault https://uri.paypal.com/services/payments/refund https://api.paypal.com/v1/vault/credit-card https://api.paypal.com/v1/payments/.* https://uri.paypal.com/payments/payouts https://uri.paypal.com/services/vault/payment-tokens/readwrite https://api.paypal.com/v1/vault/credit-card/.* https://uri.paypal.com/services/subscriptions https://uri.paypal.com/services/applications/webhooks')
                    ->build()
                )
            )

            ->environment(Environment::SANDBOX) // API可用的环境 Sandbox:沙盒 Production:生产
            ->loggingConfiguration( // 日志配置
                LoggingConfigurationBuilder::init()// 日志配置生成器
                    ->level(LogLevel::INFO) // 测井水平设置器。有关日志级别的可能值，请参阅Psr\Log\LogLevel.php
                    ->requestConfiguration( // 请求配置
                        RequestLoggingConfigurationBuilder::init()->body(true) // 请求日志配置生成器
                    )
                    ->responseConfiguration( // 响应配置
                        ResponseLoggingConfigurationBuilder::init()->headers(true) // 响应记录配置生成器的初始化器。
                    )
            )
            ->build();

        ob_start();
        $apiResponse = $client->getPaymentsController()->refundCapturedPayment($collect);
        ob_end_clean();

        if ($apiResponse->isSuccess()) 
        {
            $refund = $apiResponse->getResult();
            $order1 = json_encode($refund);
            $order2 = json_decode($order1,true);
            return $order2;
        } 
        else 
        {
            $errors = $apiResponse->getResult();
            echo json_encode(array('code'=>109,'message'=>$errors));
            exit();
        }
    }

    // 获取售后详情
    public static function GetRefund($data)
    {
        $collect = [
            'refundId' => $data['refund_Id']
        ];

        $client = PaypalServerSdkClientBuilder::init() // Paypal服务器Sdk客户端构建器
            ->clientCredentialsAuthCredentials( // 客户端凭据身份验证凭据
                ClientCredentialsAuthCredentialsBuilder::init( // ClientCredentialsAuthCredentialsBuilder的初始化器
                    $data['client_id'],
                    $data['client_secret']
                )
                ->oAuthToken(
                    OAuthTokenBuilder::init(
                        'A21AAJVb7kXnLSW_87LKbaG8t_csXkuRe4A7inmzVUv690RAWwAXkSW3gmAepy1TjIU5dFvO3YgHQzsT6NsR9GgGEgoIw68gA',
                        'Bearer'
                    )
                    ->expiresIn(32382)
                    ->scope('https://uri.paypal.com/services/payments/futurepayments https://uri.paypal.com/services/invoicing https://uri.paypal.com/services/vault/payment-tokens/read https://uri.paypal.com/services/disputes/read-buyer https://uri.paypal.com/services/payments/realtimepayment https://uri.paypal.com/services/disputes/update-seller openid https://uri.paypal.com/services/payments/payment/authcapture https://uri.paypal.com/services/disputes/read-seller Braintree:Vault https://uri.paypal.com/services/payments/refund https://api.paypal.com/v1/vault/credit-card https://api.paypal.com/v1/payments/.* https://uri.paypal.com/payments/payouts https://uri.paypal.com/services/vault/payment-tokens/readwrite https://api.paypal.com/v1/vault/credit-card/.* https://uri.paypal.com/services/subscriptions https://uri.paypal.com/services/applications/webhooks')
                    ->build()
                )
            )

            ->environment(Environment::SANDBOX) // API可用的环境 Sandbox:沙盒 Production:生产
            ->loggingConfiguration( // 日志配置
                LoggingConfigurationBuilder::init()// 日志配置生成器
                    ->level(LogLevel::INFO) // 测井水平设置器。有关日志级别的可能值，请参阅Psr\Log\LogLevel.php
                    ->requestConfiguration( // 请求配置
                        RequestLoggingConfigurationBuilder::init()->body(true) // 请求日志配置生成器
                    )
                    ->responseConfiguration( // 响应配置
                        ResponseLoggingConfigurationBuilder::init()->headers(true) // 响应记录配置生成器的初始化器。
                    )
            )
            ->build();

        ob_start();
        $apiResponse = $client->getPaymentsController()->getRefund($collect);
        ob_end_clean();

        if ($apiResponse->isSuccess()) 
        {
            $refund = $apiResponse->getResult();
            $order1 = json_encode($refund);
            $order2 = json_decode($order1,true);

            return $order2;
        } 
        else 
        {
            $errors = $apiResponse->getResult();
            echo json_encode(array('code'=>109,'message'=>$errors));
            exit();
        }
    }

    // 提现
    public function Withdrawal($data)
    {
        $Oauth_url = "https://api-m.sandbox.paypal.com/v1/oauth2/token";
        $auth = base64_encode($data['client_id'].':'.$data['client_secret']);

        $Oauth_headers = array(
            'Content-Type: application/x-www-form-urlencoded',
            'Authorization: Basic '.$auth 
        );

        $Oauth_res = self::http_request($Oauth_url,"grant_type=client_credentials",$Oauth_headers); 
        $json = json_decode($Oauth_res,true);

        $headers = array(
            'Content-Type: application/json',
            'Authorization: Bearer '.$json['access_token'],
        );

        $array = array(
            'sender_batch_header' => array(
                "sender_batch_id" => $data['txsno'], // 发件人指定的 ID 号
                "email_subject" => "You have a payout!", // 付款项目付款完成时 PayPal 发送的电子邮件的主题行
                "email_message" => "You have received a payout! Thanks for using our service!" // PayPal 在提现项目完成时发送的电子邮件
            ),
            'items' => array(
                array(
                    'recipient_type' => "EMAIL", // 收件人类型 EMAIL:未加密的电子邮件。 PHONE:未加密的电话号码。 PAYPAL_ID:加密的 PayPal 账号。 USER_HANDLE:帐户关联的用户名。
                    'amount' => array(
                        'value' => $data['amount'],
                        'currency' => $data['currency']
                    ),
                    "note" => "Thanks for your patronage!", // 发件人指定的通知注释
                    "sender_item_id" => $data['txsno'],
                    "receiver" => $data['email']
                )
            ),
        );

        $url = "https://api-m.sandbox.paypal.com/v1/payments/payouts";
        $res = self::http_request($url,json_encode($array),$headers);

        return $res;
    }

    public static function http_request($url,$data = null,$headers=array())
    {
        $curl = curl_init();
        if( count($headers) >= 1 ){
            curl_setopt($curl, CURLOPT_HTTPHEADER, $headers);
        }
        curl_setopt($curl, CURLOPT_URL, $url);

        curl_setopt($curl, CURLOPT_SSL_VERIFYPEER, FALSE);
        curl_setopt($curl, CURLOPT_SSL_VERIFYHOST, FALSE);

        if (!empty($data)){
            curl_setopt($curl, CURLOPT_POST, 1);
            curl_setopt($curl, CURLOPT_POSTFIELDS, $data);
        }
        curl_setopt($curl, CURLOPT_RETURNTRANSFER, 1);
        $output = curl_exec($curl);
        curl_close($curl);
        return $output;
    }
}


