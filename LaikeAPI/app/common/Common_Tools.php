<?php

namespace app\common;
use think\facade\Db;
use think\facade\lang;
use think\facade\Cache;
use app\common\LaiKeLogUtils;
use app\common\Tools;

/**
 * 功能：公共应用类
 */
class Common_Tools
{
    // 获取语言
    public static function get_lang($store_id)
    {
        $list = array();
        $store_langs = "";
        $sql0 = "select store_langs from lkt_customer where id = '$store_id' ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $store_langs = $r0[0]['store_langs'];
        }

        if($store_langs != '')
        {
            $sql = "select * from lkt_lang where id in ($store_langs) ";
            $r = Db::query($sql);
            if($r)
            {
                $list = $r;
            }
        }

        return $list;
    }

    // 获取商城默认语种
    public static function get_store_lang($array)
    {
        $store_id = $array['store_id']; // 商城ID

        $list = array();

        $sql0 = "select default_lang_code from lkt_config where store_id = '$store_id' ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $default_lang_code = $r0[0]['default_lang_code'];

            $sql = "select * from lkt_lang where lang_code = '$default_lang_code' ";
            $r = Db::query($sql);
            if($r)
            {
                $list = $r;
            }
        }

        return $list;
    }

    // 获取商城币种
    public static function get_store_currency($array)
    {
        $store_id = $array['store_id']; // 商城ID
        $type = $array['type']; // 1.获取默认币种
        $id = $array['id']; // 用户使用货币ID

        $list = array();
        $list = array();
        $con = " a.store_id = '$store_id' and a.recycle = 0 ";
        if($type == 1)
        {
            $con .= " and a.default_currency = 1 ";
        }

        if($id != 0)
        {
            $con .= " and a.currency_id = '$id' ";
        }

        $str_a = "a.store_id,a.currency_id,a.is_show,a.default_currency,a.exchange_rate,a.recycle,a.update_time";
        $str_b = "b.id,b.currency_code,b.currency_name,b.currency_symbol";
        $str = $str_a . "," . $str_b;

        $sql0 = "select $str from lkt_currency_store as a left join lkt_currency as b on a.currency_id = b.id where $con ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $r0[0]['exchange_rate'] = round($r0[0]['exchange_rate'],2);
            $list = $r0;
        }

        return $list;
    }

    // 获取国家列表
    public static function get_country()
    {
        $list = array();

        $sql = "select * from lkt_ds_country ";
        $r0 = Db::query($sql);
        if($r0)
        {
            $list = $r0;
        }

        return $list;
    }
}
