<?php
namespace app\admin\controller\app;

use app\BaseController;
use think\facade\Db;
use think\facade\Request;

use app\common\LaiKeLogUtils;
use app\common\Plugin\PluginUtils;

class Groupbuy extends BaseController
{
    // 确认订单页码
    public function payfor()
    {
        //1.列出基础数据
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $this->store_id = $store_id;
        $this->store_type = $store_type;
        
        $language = trim($this->request->param('language')); // 语言
        $access_id = trim($this->request->param('access_id')); // 授权id
        $this->language = $language;
        $this->access_id = $access_id;
        
        $product1 = addslashes($this->request->param('product'));//  商品数组--------['pid'=>66,'cid'=>88]
        $cart_id = addslashes(trim($this->request->param('cart_id')));  //购物车id-- 12,13,123,
        $vipSource = addslashes($this->request->param('vipSource')); // 1.会员
        // $coupon_id = addslashes($this->request->post('coupon_id')); // 优惠券ID
        $type = addslashes($this->request->param('type'));// PS.预售
        $product_type = addslashes($this->request->param('product_type'));//产品类型，JP-竞拍商品,KJ-砍价商品
        $order_type = addslashes($this->request->param('order_type'));//产品类型，JP-竞拍商品,KJ-砍价商品

        $pluginName = 'NormalOrder';
        if($type == 'PS')
        {
            $pluginName = 'Presell_order';
        }
        elseif($type == 'FX')
        {
            $pluginName = 'Distribution';
        }
        elseif($type == 'MS' || $product_type == 'MS')
        {
            $pluginName = 'SecondsPublic';
        }
        elseif($type == 'JP' || $product_type == 'JP' || $order_type == 'JP')
        {
            $pluginName = 'Auction';
        }
        elseif($type == 'FS' || $product_type == 'FS')
        {
            $pluginName = 'FlashSalePublic';
        }

        if($vipSource == 1)
        {
            $pluginName = 'Members';
        }

        $this->user = $this->user_list;
        $user_id = $this->user_list['user_id'];

        if (empty($pluginName))
        {
            ob_clean();
            $message = Lang('Request error');
            return output(ERROR_CODE_WLYC,$message);
        }
        
        $pluginObj = PluginUtils::getPlugin($pluginName);

        if (method_exists($pluginObj, 'settlement'))
        {
            $pluginObj->settlement($this);
            exit;
        }
        else
        {
            ob_clean();
            $message = Lang('tools.15');
            return output(ERROR_CODE_WLYC,$message);
        }
    }
}

?>