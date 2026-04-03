<?php

namespace app\common\Plugin;
use think\facade\Db;
use app\common\LaiKeLogUtils;

use app\admin\model\OrderModel;
require_once('FileUtil.php');

/**
 * 功能：订单公共类
 */
class PluginUtils
{

    /**
     * 订单号
     * @param $type
     * @param $text
     * @return string
     */
    public static function order_number($type, $text)
    {
        if (empty($text))
        {
            $text = 'sNo';
        }
        if ($type == 'PT')
        {
            $orderType = 'PT';
        }
        else if ($type == 'JP')
        {
            $orderType = 'JP';
        }
        else if ($type == 'MS')
        {
            $orderType = 'MS';
        }
        else if ($type == 'FX')
        {
            $orderType = 'FX';
        }
        else if ($type == 'ZB')
        {
            $orderType = 'ZB';
        }
        else
        {
            $orderType = 'GM';
        }
        $sNo = $orderType . date("ymdhis") . rand(0, 9) . rand(0, 9) . rand(0, 9) . rand(0, 9) . rand(0, 9) . rand(0, 9);
        $res = OrderModel::where($text,$sNo)->select()->toArray();
        if ($res)
        {
            self::order_number($orderType, $text);
        }
        else
        {
            return $sNo;
        }
    }

    /**
     * 根据订单类型获取插件类名称
     * 存在订单类型和订单前缀不一致 一个插件多种订单类型如竞拍：竞拍保证金订单、竞拍商品订单
     * @param $otype
     * @return string
     */
    public static function getPluginClassnameByOtype($otype)
    {
        if($otype == 'PS' || $otype == 'presell')
        { // 预售商品
            $pluginName = 'Presell_order';
        }
        else if ($otype == 'FX' || $otype == 'distribution')
        { // 分销
            $pluginName = 'Distribution';
        }
        else if ($otype == 'MC')
        { // 会员卡
            $pluginName = 'members_card';
        }
        else if ($otype == 'member' || $otype == 'CZ')
        { // 会员商品
            $pluginName = 'Members';
        }
        else if ($otype == 'normal_order_pc')
        { // 普通订单（PC商城）
            $pluginName = 'Normal_order_pc';
        }
        else if ($otype == 'MS' || $otype == 'seconds')
        { //秒杀订单
            $pluginName = 'SecondsPublic';
        }
        else if ($otype == 'JP' || $otype == 'auction')
        { // 竞拍订单
            $pluginName = 'Auction';
        }
        else if ($otype == 'IN' || $otype == 'integral')
        {
            $pluginName = 'IntegralPublicMethod';
        }
        else if ($otype == 'DJ')
        {
            $pluginName = 'Members_card';
        }
        else if ($otype == 'CZ')
        { // 充值
            $pluginName = 'Members';
        }
        else if ($otype == 'PR')
        { // 店铺保证金
            $pluginName = 'Margin_order';
        }
        else if ($otype == 'PT' || $otype == 'KT' || $otype == 'go_group')
        { // 拼团
            $pluginName = 'Go_groupPublicMethod';
        }
        else if ($otype == 'FS' || $otype == 'flashsale')
        { // 限时折扣
            $pluginName = 'FlashSalePublic';
        }
        else if ($otype == 'VI')
        { // 虚拟订单
            $pluginName = 'Virtual_order';
        }
        else if ($otype == 'ZB' || $otype == 'living')
        { // 直播订单
            $pluginName = 'LivingPublic';
        }
        else
        { // 普通订单（移动端）
            $pluginName = 'NormalOrder';
        }


        return $pluginName;
    }


    /**
     * @param $plugin_classType
     * @return mixed
     */
    public static function getPlugin($plugin_classType)
    {   
        $file_path = '../app/common/Plugin/' . $plugin_classType . '.php';
        if(!file_exists($file_path))
        {
            // throw new Exception('找不到引入文件'. $file_path );
            return false;
        }

        switch ($plugin_classType) {
            case 'Distribution':
                $plugin_classType = new Distribution();
                break;

            case 'Presell_order': // 预售商品
                $plugin_classType = new Presell_order();
                break;

            case 'Members': // 会员商品
                $plugin_classType = new Members();
                break;

            case 'Members_card': // 开通会员
                $plugin_classType = new Members_card();
                break;

            case 'Auction': // 竞拍
                $plugin_classType = new Auction();
                break;

            case 'normal_order_pc': // 普通订单（PC商城）
                $plugin_classType = new Normal_order_pc();
                break;

            case 'SecondsPublic': // 秒杀订单
                $plugin_classType = new SecondsPublic();
              break;

            case 'IntegralPublicMethod':
                $plugin_classType = new IntegralPublicMethod();
                break;

            case 'Normal_order_pc':
                $plugin_classType = new Normal_order_pc();
                break;
            
            case 'Margin_order':
                $plugin_classType = new Margin_order();
                break;

            case 'Go_groupPublicMethod': // 拼团
                $plugin_classType = new Go_groupPublicMethod();
                break;

            case 'FlashSalePublic': // 限时折扣
                $plugin_classType = new FlashSalePublic();
                break;
                
            case 'Virtual_order': // 虚拟订单
                $plugin_classType = new Virtual_order();
                break;
            case 'LivingPublic': // 直播订单
                $plugin_classType = new LivingPublic();
                break;

            default: // 普通订单（移动端）
                $plugin_classType = new NormalOrder();
                break;
        }
        return $plugin_classType;
    }


    /**
     * 执行对象的方法
     * @param $otype        订单号
     * @param $fun          方法名
     * @param $args         参数
     * @throws ReflectionException
     */
    public static function invokeMethod($otype, $fun, $args)
    {   
        $plugin_class_type = self::getPluginClassnameByOtype($otype);
        return  self::handle($plugin_class_type,$fun, $args);
    }

    /**
     * @param $plugin_class_type    插件入口类
     * @param $fun                  方法名
     * @param $args                 参数
     * @return mixed
     * @throws ReflectionException
     */
    private static function handle($plugin_class_type, $fun, $args)
    {
        $plugin_log_path = 'common/plugin_execute.log';
        
        LaiKeLogUtils::log($plugin_log_path,"插件 [ $plugin_class_type ] 方法 $fun 开始执行. ");
        $plugin = self::getPlugin($plugin_class_type);

        if($plugin == false)
        {
            return array('code'=>'502','message'=>'插件文件不存在');
        }
        if (method_exists($plugin, $fun))
        {
            // $method = new ReflectionMethod($plugin, $fun);
            // if ($method->ispublic())
            // {
                if (count((array)$args) > 0)
                {
                    LaiKeLogUtils::log($plugin_log_path,"插件 [ $plugin_class_type ]方法 $fun 执行结束. ");
                    return $plugin->$fun($args);
                }
                else
                {
                    LaiKeLogUtils::log($plugin_log_path,"插件 [ $plugin_class_type ]方法 $fun 执行结束. ");
                    return $plugin->$fun();
                }
            // }
            // else
            // {
            //     LaiKeLogUtils::log($plugin_log_path,"插件 [ $plugin_class_type ] 方法 $fun 不是公共方法。 ");
            //     echo 'error';
            //     exit();
            // }
        }
        else
        {
            LaiKeLogUtils::log($plugin_log_path,"插件 [ $plugin_class_type ] 方法 $fun 不存在 ");
            echo 'error';
            exit();
        }
    }

    /**
     * 安装插件 分发文件和执行数据库脚本
     * @param $data
     * @return array
     * @throws ReflectionException
     */
    public static function install($data)
    {
        $plugin_name = $data->plugin_name;

        if(empty($plugin_name))
        {
            throw new Exception("安装插件失败，插件名为空！");
        }

        $zip = new ZipArchive;
        $savePath = MO_PLUGINS_ZIP_DIR."/".$plugin_name.".zip";
        if ( $zip->open($savePath) === TRUE )
        {
            $zip->extractTo(MO_PLUGINS_DIR.'/');
            $zip->close();
                //$a = array_diff(scandir(MO_PLUGINS_DIR.'/'.$plugin_name.'/webapp/pluginsdb'),array('..','.'));
                if(!file_exists(MO_PLUGINS_DIR.'/'.$plugin_name.'/app/pluginsdb'))
            {
                    return array('code'=>'502','msg'=>'插件压缩包异常');
                }
            //0。数据库脚本文件
            $plugin_db_from_dir = MO_PLUGINS_DIR.'/'.$plugin_name.'/app/pluginsdb';
            $plugin_db_to_dir = MO_PLUGINSDB_DIR.'/';
            app('FileUtil')::copyDir($plugin_db_from_dir,$plugin_db_to_dir);
            //1。插件入口文件
            $plugin_from_entryfile = MO_PLUGINS_DIR.'/'.$plugin_name.'/app/common/Plugin/'.$plugin_name.'.php';
            $plugin_to_entryfile =  MO_LIB_DIR.'/Plugin/'.$plugin_name.'.php';
            app('FileUtil')::copyFile($plugin_from_entryfile,$plugin_to_entryfile);

            if($plugin_name == 'platform_activities')
            {
              $plugin_lib_from_dir = MO_PLUGINS_DIR.'/'.$plugin_name.'/app/common/Plugin';
                  if(!file_exists($plugin_lib_from_dir))
              {
                        return array('code'=>'502','msg'=>'插件压缩包异常');
                    }
              $plugin_lib_to_dir = MO_LIB_DIR.'/Plugin';
              app('FileUtil')::copyDir($plugin_lib_from_dir,$plugin_lib_to_dir);
            }

            //2。插件后台文件
            $plugin_admin_from_dir = MO_PLUGINS_DIR.'/'.$plugin_name.'/app/admin/controller/plugin';
                if(!file_exists($plugin_admin_from_dir))
            {
                    return array('code'=>'502','msg'=>'插件压缩包异常');
                }
            $plugin_admin_to_dir = MO_WEBAPP_DIR.'/admin/controller/plugin';
            app('FileUtil')::copyDir($plugin_admin_from_dir,$plugin_admin_to_dir);

            //3。插件的接口文件 app actions
            $plugin_api_from_dir = MO_PLUGINS_DIR.'/'.$plugin_name.'/app/admin/controller/app';
            if(file_exists($plugin_api_from_dir))
            {
                $plugin_api_to_dir = MO_WEBAPP_DIR.'/admin/controller/app';
                app('FileUtil')::copyDir($plugin_api_from_dir,$plugin_api_to_dir);
            }

            // 4.PC端
            $plugin_api_from_dir = MO_PLUGINS_DIR.'/'.$plugin_name.'/app/admin/controller/app_pc';
            if(file_exists($plugin_api_from_dir))
            {
                $plugin_api_to_dir = MO_WEBAPP_DIR.'/app/admin/controller/app_pc';
                app('FileUtil')::copyDir($plugin_api_from_dir,$plugin_api_to_dir);
            }


            //安装文件处理
                app('FileUtil')::unlinkDir(MO_PLUGINS_DIR.'/'.$plugin_name);
                app('FileUtil')::unlinkFile($savePath);
                
                //垃圾文件 __MACOSX 删除
                if(file_exists(MO_PLUGINS_DIR.'/__MACOSX'))
            {
                    app('FileUtil')::unlinkDir(MO_PLUGINS_DIR.'/__MACOSX');
                }
                if(file_exists(MO_PLUGINS_ZIP_DIR.'/__MACOSX'))
            {
                    app('FileUtil')::unlinkDir(MO_PLUGINS_ZIP_DIR.'/__MACOSX');
                }

            //4。数据库操作
            $ret = PluginUtils::invokeMethod($plugin_name,'install',$data);
            if(isset($ret['code']) && $ret['code'] == '502')
            {
                return $ret;
            }
            else
            { 
                $message = Lang('Success');
                return array('code'=>'200','msg'=>'成功！');
            }
        }

        return array('code'=>'501','msg'=>'插件压缩包不存在');
    }

    /**
     * 插件卸载
     * @param $data
     * @return array
     * @throws ReflectionException
     */
    public static function uninstall($data)
    {
        $plugin_name = $data->plugin_name;
        if(empty($plugin_name))
        {
            throw new Exception('卸载的插件为空！');
        }

        $uninstalllog= "common/uninstall.log";
        $ret = PluginUtils::invokeMethod($data->plugin_class_name,'uninstall',$data);

        LaiKeLogUtils::log($uninstalllog,'开始卸载插件'.$plugin_name);

        LaiKeLogUtils::log($uninstalllog,'开始卸载插件数据脚本文件');
        $plugin_db_to_dir = MO_PLUGINSDB_DIR.'/'.$plugin_name.'/';
        app('FileUtil')::unlinkDir($plugin_db_to_dir);
        LaiKeLogUtils::log($uninstalllog,'卸载插件数据脚本文件结束'.$plugin_db_to_dir);

        LaiKeLogUtils::log($uninstalllog,'开始卸载插件入口文件');
        $plugin_to_entryfile =  MO_LIB_DIR.'/Plugin/'.$plugin_name.'.php';
        app('FileUtil')::unlinkFile($plugin_to_entryfile);

        if($plugin_name == 'platform_activities'){

            $plugin_to_entryfile =  MO_LIB_DIR.'/Plugin/pthd_go_group.php';
            app('FileUtil')::unlinkFile($plugin_to_entryfile);

            $plugin_to_entryfile =  MO_LIB_DIR.'/Plugin/pthd_seconds.php';
            app('FileUtil')::unlinkFile($plugin_to_entryfile);
        }

        LaiKeLogUtils::log($uninstalllog,'开始卸载插件入口文件'.$plugin_to_entryfile);

        LaiKeLogUtils::log($uninstalllog,'开始卸载插件后台管理文件');
        $plugin_admin_to_dir = MO_WEBAPP_DIR.'/admin/controller/plugin/'.$plugin_name.'.php';
        app('FileUtil')::unlinkFile($plugin_admin_to_dir);
        LaiKeLogUtils::log($uninstalllog,'结束卸载插件后台管理文件'.$plugin_admin_to_dir);

        //删除插件的移动端api接口文件
        LaiKeLogUtils::log($uninstalllog,'开始删除插件的移动端api接口文件');
        $plugin_api_from_dir = MO_PLUGINS_DIR.'/'.$plugin_name.'/app/admin/controller/app';
        app('FileUtil')::delPluginApiFiles($plugin_api_from_dir,$plugin_name);
        LaiKeLogUtils::log($uninstalllog,'删除插件的移动端api接口文件结束');

        //删除插件的PC端api接口文件
        LaiKeLogUtils::log($uninstalllog,'开始删除插件的PC端api接口文件');
        $plugin_api_from_dir = MO_PLUGINS_DIR.'/'.$plugin_name.'/app/admin/controller/app_pc';
        app('FileUtil')::delPluginApiFiles($plugin_api_from_dir,$plugin_name);
        LaiKeLogUtils::log($uninstalllog,'删除插件的PC端api接口文件结束');

        LaiKeLogUtils::log($uninstalllog,'==卸载结束==');

        return array('code'=>'200','msg'=>'卸载成功');


    }

}