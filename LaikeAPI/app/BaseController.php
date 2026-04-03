<?php
declare (strict_types = 1);

namespace app;

use app\admin\model\ConfigModel;
use think\App;
use think\facade\Db;
use think\exception\ValidateException;
use think\Validate;
use app\common\LaiKeLogUtils;
use app\common\Tools;

/**
 * 控制器基础类
 */
abstract class BaseController
{
    /**
     * Request实例
     * @var \think\Request
     */
    protected $request;

    /**
     * 应用实例
     * @var \think\App
     */
    protected $app;

    /**
     * 日志实例
     * @var \think\App
     */
    protected $log;

     /**
     * 登录用户信息
     * @var \think\App
     */
    protected $user_list;
    /**
     * 是否批量验证
     * @var bool
     */
    protected $batchValidate = false;

    /**
     * 控制器中间件
     * @var array
     */
    protected $middleware = [];

    /**
     * 构造方法
     * @access public
     * @param  App  $app  应用对象
     */
    public function __construct(App $app)
    {
        $this->app     = $app;
        $this->request = $this->app->request;
        $this->log     = new LaiKeLogUtils();

        // 控制器初始化
        $this->initialize();
        // 权限登录状态校验
        $this->auth();
    }

    /**
     * token失效刷新缓存时间 秒
     */
    const TOKEN_REFRESH_THRESHOLD = 600;


    // 初始化
    protected function initialize()
    {}

    /**
     * 验证数据
     * @access protected
     * @param  array        $data     数据
     * @param  string|array $validate 验证器名或者验证规则数组
     * @param  array        $message  提示信息
     * @param  bool         $batch    是否批量验证
     * @return array|string|true
     * @throws ValidateException
     */
    protected function validate(array $data, $validate, array $message = [], bool $batch = false)
    {
        if (is_array($validate)) {
            $v = new Validate();
            $v->rule($validate);
        } else {
            if (strpos($validate, '.')) {
                // 支持场景
                [$validate, $scene] = explode('.', $validate);
            }
            $class = false !== strpos($validate, '\\') ? $validate : $this->app->parseClass('validate', $validate);
            $v     = new $class();
            if (!empty($scene)) {
                $v->scene($scene);
            }
        }

        $v->message($message);

        // 是否批量验证
        if ($batch || $this->batchValidate) {
            $v->batch(true);
        }

        return $v->failException(true)->check($data);
    }

    /**
     * 验证权限
     * @access protected
     * @param  array        $data     数据
     * @param  string|array $validate 验证器名或者验证规则数组
     * @param  array        $message  提示信息
     * @param  bool         $batch    是否批量验证
     * @return array|string|true
     * @throws ValidateException
     */

    protected function auth()
    {   
        $token = '';
        $store_type = null;
        $store_id = null;
        if($this->request->has('storeType'))
        {
            $store_type = $this->request->param('storeType');
        }
        elseif($this->request->has('store_type'))
        {
            $store_type = $this->request->param('store_type');
        }
        if($this->request->has('accessId'))
        {
            $token = $this->request->param('accessId');
        }
        elseif($this->request->has('access_id'))
        {
            $token = $this->request->param('access_id');
        }
        elseif ($this->request->has('token')) 
        {
            $token = $this->request->param('token');
        }

        $api = $this->request->param('api');
        $module = $this->request->param('module');
        $action = $this->request->param('action');
        $app = $this->request->param('app');
        $m = $this->request->param('m');

        if($api == '')
        {
            if($app != '')
            {
                $api = $module . '.' . $action . '.' . $app;
            }
            else
            {
                $api = $module . '.' . $action . '.' . $m;
            }
        }

        $list = array(
            /* 移动端 */ 
            'app.Order.getPayment', // 获取支付方式
            'app.common.getLangs', // 获取商城语种
            'app.common.getCurrencys', // 获取商城币种
            'app.common.getStoreDefaultCurrency', // 获取商城默认币种
            'app.common.getCountry', // 获取国家列表
            'app.common.getStoreDefaultI18n', // 获取商城默认币种和语种
            'app.user.getItuList', // 获取区号
            'app.user.index', // 个人中心
            'app.user.myRecommendation', // 个人中心-推荐商品
            /* 店铺移动端 */ 
            'mch.App.Mch.Store_homepage', // 店铺主页
            'mch.App.Mch.store_homepage_load', // 店铺主页-加载更多
            'mch.App.Mch.diy_home_page', // 店铺主页-DIY
            /* PC商城 */ 
            'mall.Order.getPayment', // 获取支付方式
            /* 插件 */ 
            'plugin.auction.AppAuction.getSpecialList', // 竞拍专场
            'plugin.distribution.AppDistribution.getGoodsInfo', // 分销商品
            'plugin.living.AppAnchor.queryLivingById', // 直播间
            'plugin.living.AppAnchor.queryCommentByRoomId', // 查询评论
            'plugin.living.AppAnchorProduct.livingPro', // 商品管理-直播页面
        );

        if(!in_array($api,$list))
        {
            if($token)
            {
                if($store_type == 9 || $store_type == 10)
                {
                    $sql0 = "select * from lkt_mch_admin where access_id = '$token' and recycle = 0 ";
                    $r0 = Db::query($sql0);
                    if(!$r0)
                    {
                        $message = Lang('expired');//登录已过期,请重新登录！
                        echo json_encode(array('code' => 203, 'message' => $message));
                        exit();
                    }
                }
                $res = Tools::verifyToken($token);
                if ($res === false) {
                    echo json_encode(['code' => 203, 'message' => Lang('expired')]);
                    exit();
                }

                $now = time();
                $exp = $res['exp'];

                // token的过期时间
                $ttl = 7200 ;
                //获取有效期
                $store_id = $this->request->param('store_id');
                $res = ConfigModel::where('store_id', $store_id)
                    ->field('exp_time')
                    ->select()
                    ->toArray();
                if($res)
                {
                    $ttl = intval($res[0]['exp_time']* 3600);
                }

                if ($exp - $now <= self::TOKEN_REFRESH_THRESHOLD) {

                    $newPayload = $res;
                    $newPayload['iat'] = $now;
                    $newPayload['exp'] = $now + $ttl;

                    $newToken = \app\common\Jwt::getToken($newPayload);

                    // 失效旧 token
                    cache($token, null);

                    // 写入新 token
                    cache($newToken, $newPayload, $ttl);

                    header('X-Refresh-Token: ' . $newToken);

                    // 使用新 token
                    $token = $newToken;
                    $res   = $newPayload;
                }

                $this->user_list = cache($token);

            }
            else
            {
                $message = Lang('Illegal invasion');//登录已过期,请重新登录！
                echo json_encode(array('code' => 203, 'message' => $message));
                exit();
            }
        }
    }

    protected function maskValue($value, $prefix = 3, $suffix = 4, $maskChar = '*')
    {
        if ($value === null) {
            return $value;
        }
        $value = (string)$value;
        $len = strlen($value);
        if ($len === 0) {
            return $value;
        }
        if ($len <= ($prefix + $suffix)) {
            if ($len <= 2) {
                return str_repeat($maskChar, $len);
            }
            return substr($value, 0, 1) . str_repeat($maskChar, $len - 2) . substr($value, -1);
        }
        return substr($value, 0, $prefix) . str_repeat($maskChar, $len - $prefix - $suffix) . substr($value, -$suffix);
    }

    protected function maskMobile($mobile)
    {
        return $this->maskValue($mobile, 3, 4);
    }

    protected function maskIdNumber($idNumber)
    {
        return $this->maskValue($idNumber, 6, 4);
    }

    protected function isMaskedValue($value)
    {
        return is_string($value) && strpos($value, '*') !== false;
    }
}
