<?php

namespace app\admin\controller\app;

use think\facade\Db;
use app\common\OSSCommon;
use app\common\Tools;
use app\common\PC_Tools;
use app\common\LaiKeLogUtils;
use app\common\LKTConfigInfo;
use app\common\ReceiveGoodsUtils;
use app\common\Plugin\PluginUtils;
use app\common\Plugin\CouponPublicMethod;
use app\common\Plugin\MchPublicMethod;
use app\common\Plugin\FlashSalePublic;
use app\common\wxpayv2\wxpay;

use app\admin\model\GuideModel;
use app\admin\model\CustomerModel;
use app\admin\model\OrderConfigModel;
use app\admin\model\OrderModel;
use app\admin\model\OrderDetailsModel;
use app\admin\model\ProductListModel;
use app\admin\model\ConfigureModel;
use app\admin\model\StockModel;
use app\admin\model\ConfigModel;
use app\admin\model\TaobaoModel;
use app\admin\model\TaobaoWorkModel;
use app\admin\model\ReturnOrderModel;
use app\admin\model\PluginsModel;
use app\admin\model\SupplierModel;
use app\admin\model\SupplierOrderFrightModel;
use app\admin\model\SupplierAccountLogModel;
use app\admin\model\MchModel;
use app\admin\model\IntegralConfigModel;

class MenuTest
{
    // 后台菜单
    public function index()
    {
        print_r('后台菜单');echo "<br>";
        die;
        // 0-1000      zh_CN   186   中文简体
        // 1000-2000   en_US   840   英语
        // 2000-3000   zh_TW   852   中文繁体
        // 3000-4000   ja_JP   81    日语
        // 4000-5000   ru_RU   7     俄罗斯语
        // 5000-6000   ms_MY   60    马来西亚语
        // 6000-7000   id_ID   62    印度尼西亚语
        // 7000-8000   fil_PH   63    菲律宾语
        $num = 7000;
        $lang_code = 'en_ph';
        $country_num = 63;
        // 把value值翻译成菲律宾语
        $list = array();
        $list1 = array();
        $list2 = array();
        // $sql0 = "select * from lkt_core_menu where recycle = 0 and lang_code = 'zh_CN' and country_num = '156' limit 0,310 ";
        $sql0 = "select * from lkt_core_menu where recycle = 0 and lang_code = 'zh_CN' and country_num = '156' limit 310,620 ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            foreach($r0 as $k0 => $v0)
            {
                $v0['id'] = $v0['id'] + $num;
                if($v0['s_id'] != 0)
                {
                    $v0['s_id'] = $v0['s_id'] + $num;
                }
                $v0['lang_code'] = $lang_code;
                $v0['country_num'] = $country_num;
                $list[] = $v0;
                $list1[] = array('0'=>$v0['title'],'1'=>$v0['briefintroduction'],'2'=>$v0['guide_name']);
            }
        }

        print_r($list1);echo "<br>";
        die;

        $list2 = array(
            array("UI Navigation Bar", "UI Navigation Bar", "UI Navigation Bar"), // 行业通用术语保留英文更易理解
            array("Magdagdag ng UI Navigation Bar", "Magdagdag ng UI Navigation Bar", "Magdagdag ng UI Navigation Bar"),
            array("I-edit", "I-edit", "I-edit"),
            array("Tanggalin", "Tanggalin", "Tanggalin"),
            array("Ibaon pababa", "Ibaon pababa", "Ibaon pababa"),
            array("Itaas pataas", "Itaas pataas", "Itaas pataas"),
            array("Itaas sa Pinakamataas", "Itaas sa Pinakamataas", "Itaas sa Pinakamataas"),
            array("Pamamahala ng Aktibidad", "Pamamahala ng Aktibidad", "Pamamahala ng Aktibidad"),
            array("Magdagdag ng Aktibidad", "Magdagdag ng Aktibidad", "Magdagdag ng Aktibidad"),
            array("I-edit", "I-edit", "I-edit"),
            array("Tanggalin", "Tanggalin", "Tanggalin"),
            array("Ibaon pababa", "Ibaon pababa", "Ibaon pababa"),
            array("Itaas pataas", "Itaas pataas", "Itaas pataas"),
            array("Produkto ng Aktibidad", "Produkto ng Aktibidad", "Produkto ng Aktibidad"),
            array("Magdagdag ng Aktibidad", "Magdagdag ng Aktibidad", "Magdagdag ng Aktibidad"),
            array("I-edit ang Aktibidad", "I-edit ang Aktibidad", "I-edit ang Aktibidad"),
            array("Produkto ng Aktibidad", "Produkto ng Aktibidad", "Produkto ng Aktibidad"),
            array("Listahan ng Carousel", "Listahan ng Carousel", "Listahan ng Carousel"),
            array("I-edit ang Carousel", "I-edit ang Carousel", "I-edit ang Carousel"),
            array("Magdagdag ng Carousel", "Magdagdag ng Carousel", "Magdagdag ng Carousel"),
            array("I-edit", "I-edit", "I-edit"),
            array("Tanggalin", "Tanggalin", "Tanggalin"),
            array("Ibaon pababa", "Ibaon pababa", "Ibaon pababa"),
            array("Itaas pataas", "Itaas pataas", "Itaas pataas"),
            array("Itaas sa Pinakamataas", "Itaas sa Pinakamataas", "Itaas sa Pinakamataas"),
            array("Magdagdag ng Template", "Magdagdag ng Template", "Magdagdag ng Template"),
            array("Flash Sale", "Flash Sale", "Flash Sale"), // 秒杀在菲电商场景通用Flash Sale
            array("Tag ng Flash Sale", "Tag ng Flash Sale", "Tag ng Flash Sale"),
            array("Magdagdag ng Tag", "Magdagdag ng Tag", "Magdagdag ng Tag"),
            array("Magdagdag ng Produkto", "Magdagdag ng Produkto", "Magdagdag ng Produkto"),
            array("I-edit", "I-edit", "I-edit"),
            array("Tanggalin", "Tanggalin", "Tanggalin"),
            array("Listahan ng Produkto", "Listahan ng Produkto", "Listahan ng Produkto"),
            array("Itaas pataas", "Itaas pataas", "Itaas pataas"),
            array("Ibaon pababa", "Ibaon pababa", "Ibaon pababa"),
            array("Itaas sa Pinakamataas", "Itaas sa Pinakamataas", "Itaas sa Pinakamataas"),
            array("Talaan ng Flash Sale", "Talaan ng Flash Sale", "Talaan ng Flash Sale"),
            array("Tanggalin", "Tanggalin", "Tanggalin"),
            array("Order ng Flash Sale", "Order ng Flash Sale", "Order ng Flash Sale"),
            array("I-export", "I-export", "I-export"),
            array("Pamamahala ng After-sales", "Pamamahala ng After-sales", "Pamamahala ng After-sales"),
            array("Pamamahala ng Rating", "Pamamahala ng Rating", "Pamamahala ng Rating"),
            array("Settlement ng Order", "Settlement ng Order", "Settlement ng Order"),
            array("Pag-print ng Order", "Pag-print ng Order", "Pag-print ng Order"),
            array("Bulk na Pagtanggal", "Bulk na Pagtanggal", "Bulk na Pagtanggal"),
            array("Detalyadong Impormasyon ng Order", "Detalyadong Impormasyon ng Order", "Detalyadong Impormasyon ng Order"),
            array("I-edit ang Order", "I-edit ang Order", "I-edit ang Order"),
            array("Isara ang Order", "Isara ang Order", "Isara ang Order"),
            array("Tingnan ang Logistik", "Tingnan ang Logistik", "Tingnan ang Logistik"),
            array("Magpadala ng Produkto", "Magpadala ng Produkto", "Magpadala ng Produkto"),
            array("Magdagdag ng Produkto", "Magdagdag ng Produkto", "Magdagdag ng Produkto"),
            array("Listahan ng Produkto", "Listahan ng Produkto", "Listahan ng Produkto"),
            array("Mga Setting ng Flash Sale", "Mga Setting ng Flash Sale", "Mga Setting ng Flash Sale"),
            array("I-edit", "I-edit", "I-edit"),
            array("Pamamahala ng After-sales", "Pamamahala ng After-sales", "Pamamahala ng After-sales"),
            array("Detalyadong Impormasyon ng After-sales", "Detalyadong Impormasyon ng After-sales", "Detalyadong Impormasyon ng After-sales"),
            array("Settlement ng Order", "Settlement ng Order", "Settlement ng Order"),
            array("Tingnan", "Tingnan", "Tingnan"),
            array("Pamamahala ng Rating", "Pamamahala ng Rating", "Pamamahala ng Rating"),
            array("Baguhin", "Baguhin", "Baguhin"),
            array("I-edit ang Order", "I-edit ang Order", "I-edit ang Order"),
            array("Magpadala ng Produkto", "Magpadala ng Produkto", "Magpadala ng Produkto"),
            array("Detalyadong Impormasyon ng Order", "Detalyadong Impormasyon ng Order", "Detalyadong Impormasyon ng Order"),
            array("Tingnan ang Produkto", "Tingnan ang Produkto", "Tingnan ang Produkto"),
            array("Detalye ng Komento", "Detalye ng Komento", "Detalye ng Komento"),
            array("Points Mall", "Points Mall", "Points Mall"),
            array("Produkto ng Points", "Produkto ng Points", "Produkto ng Points"),
            array("Magdagdag", "Magdagdag", "Magdagdag"),
            array("Bulk na Pagtanggal", "Bulk na Pagtanggal", "Bulk na Pagtanggal"),
            array("I-edit", "I-edit", "I-edit"),
            array("Tanggalin", "Tanggalin", "Tanggalin"),
            array("Itaas sa Pinakamataas", "Itaas sa Pinakamataas", "Itaas sa Pinakamataas"),
            array("Mga Setting ng Points", "Mga Setting ng Points", "Mga Setting ng Points"),
            array("Magdagdag ng Produkto", "Magdagdag ng Produkto", "Magdagdag ng Produkto"),
            array("I-edit", "I-edit", "I-edit"),
            array("Order ng Exchange", "Order ng Exchange", "Order ng Exchange"),
            array("I-export", "I-export", "I-export"),
            array("Pamamahala ng Rating", "Pamamahala ng Rating", "Pamamahala ng Rating"),
            array("Settlement ng Order", "Settlement ng Order", "Settlement ng Order"),
            array("Pag-print ng Order", "Pag-print ng Order", "Pag-print ng Order"),
            array("Bulk na Pagtanggal", "Bulk na Pagtanggal", "Bulk na Pagtanggal"),
            array("Detalyadong Impormasyon ng Order", "Detalyadong Impormasyon ng Order", "Detalyadong Impormasyon ng Order"),
            array("I-edit ang Order", "I-edit ang Order", "I-edit ang Order"),
            array("Magpadala ng Produkto", "Magpadala ng Produkto", "Magpadala ng Produkto"),
            array("Isara ang Order", "Isara ang Order", "Isara ang Order"),
            array("Tingnan ang Logistik", "Tingnan ang Logistik", "Tingnan ang Logistik"),
            array("I-edit ang Order", "I-edit ang Order", "I-edit ang Order"),
            array("Magpadala ng Produkto", "Magpadala ng Produkto", "Magpadala ng Produkto"),
            array("Detalyadong Impormasyon ng Order", "Detalyadong Impormasyon ng Order", "Detalyadong Impormasyon ng Order"),
            array("Pamamahala ng After-sales", "Pamamahala ng After-sales", "Pamamahala ng After-sales"),
            array("Detalyadong Impormasyon ng After-sales", "Detalyadong Impormasyon ng After-sales", "Detalyadong Impormasyon ng After-sales"),
            array("Pamamahala ng Rating", "Pamamahala ng Rating", "Pamamahala ng Rating"),
            array("Baguhin", "Baguhin", "Baguhin"),
            array("Settlement ng Order", "Settlement ng Order", "Settlement ng Order"),
            array("Tingnan", "Tingnan", "Tingnan"),
            array("Detalye ng Rating", "", ""),
            array("Talaan ng Exchange", "Talaan ng Exchange", "Talaan ng Exchange"),
            array("Distribution", "Distribution", "Distribution"),
            array("Pamamahala ng Distributor", "Listahan ng Distributor", "Listahan ng Distributor"),
            array("Produkto ng Distribution", "Produkto ng Distribution", "Produkto ng Distribution"),
            array("Antas ng Distribution", "Antas ng Distribution", "Antas ng Distribution"),
            array("Ranking ng Commission", "Ranking ng Commission", "Ranking ng Commission"),
            array("Talaan ng Commission", "Talaan ng Commission", "Talaan ng Commission"),
            array("Ugnayan ng Rekomendasyon", "Ugnayan ng Rekomendasyon", "Ugnayan ng Rekomendasyon"),
            array("Talaan ng Withdrawal", "Talaan ng Withdrawal", "Talaan ng Withdrawal"),
            array("Mga Setting ng Distribution", "Mga Setting ng Distribution", "Mga Setting ng Distribution"),
            array("I-edit ang Antas ng Distribution", "I-edit ang Antas ng Distribution", "I-edit ang Antas ng Distribution"),
            array("Magdagdag ng Antas ng Distribution", "Magdagdag ng Antas ng Distribution", "Magdagdag ng Antas ng Distribution"),
            array("I-edit", "I-edit", "I-edit"),
            array("I-edit ang Produkto", "I-edit ang Produkto", "I-edit ang Produkto"),
            array("Magdagdag ng Produkto", "Magdagdag ng Produkto", "Magdagdag ng Produkto"),
            array("Tingnan ang Mga Nasa Ibaba", "Tingnan ang Mga Nasa Ibaba", "Tingnan ang Mga Nasa Ibaba"),
            array("Order ng Distribution", "", "Order ng Distribution"),
            array("Detalye", "", "Detalye"),
            array("Tingnan", "", "Tingnan"),
            array("Baguhin", "", "Baguhin"),
            array("Tanggalin ang Komento", "", "Tanggalin ang Komento"),
            array("Sumagot", "", "Sumagot"),
            array("Magpadala ng Produkto", "Magpadala ng Produkto", "Magpadala ng Produkto"),
            array("Listahan ng Rating", "Listahan ng Rating", "Listahan ng Rating"),
            array("Detalyadong Impormasyon ng Order", "Detalyadong Impormasyon ng Order", "Detalyadong Impormasyon ng Order"),
            array("Settlement ng Order", "Settlement ng Order", "Settlement ng Order"),
            array("Detalye ng Rating", "Detalye ng Rating", "Detalye ng Rating"),
            array("Baguhin ang Rating", "Baguhin ang Rating", "Baguhin ang Rating"),
            array("I-edit ang Order", "I-edit ang Order", "I-edit ang Order"),
            array("Magdagdag ng Distributor", "Magdagdag ng Distributor", "Magdagdag ng Distributor"),
            array("Pamamahala ng After-sales", "", "Pamamahala ng After-sales"),
            array("Detalyadong Impormasyon ng After-sales", "", "Detalyadong Impormasyon ng After-sales"),
            array("Pre-order ng Produkto", "Pre-order ng Produkto", "Pre-order ng Produkto"),
            array("Listahan ng Produkto", "Listahan ng Produkto", "Listahan ng Produkto"),
            array("I-export", "I-export", "I-export"),
            array("Mag-publish ng Produkto", "Mag-publish ng Produkto", "Mag-publish ng Produkto"),
            array("Bulk na Pag-upload ng Produkto", "Bulk na Pag-upload ng Produkto", "Bulk na Pag-upload ng Produkto"),
            array("Bulk na Pagtanggal", "Bulk na Pagtanggal", "Bulk na Pagtanggal"),
            array("I-edit", "I-edit", "I-edit"),
            array("Tanggalin", "Tanggalin", "Tanggalin"),
            array("Tingnan ang Detalyadong Impormasyon", "Tingnan ang Detalyadong Impormasyon", "Tingnan ang Detalyadong Impormasyon"),
            array("Detalyadong Impormasyon ng Pre-order", "Detalyadong Impormasyon ng Pre-order", "Detalyadong Impormasyon ng Pre-order"),
            array("Detalyadong Impormasyon ng Benta", "Detalyadong Impormasyon ng Benta", "Detalyadong Impormasyon ng Benta"),
            array("I-upload", "I-upload", "I-upload"),
            array("I-down", "I-down", "I-down"),
            array("Bulk na Pag-down", "Bulk na Pag-down", "Bulk na Pag-down"),
            array("Order ng Pre-order", "Order ng Pre-order", "Order ng Pre-order"),
            array("I-export", "I-export", "I-export"),
            array("Pamamahala ng After-sales", "Pamamahala ng After-sales", "Pamamahala ng After-sales"),
            array("Pamamahala ng Rating", "Pamamahala ng Rating", "Pamamahala ng Rating"),
            array("Settlement ng Order", "Settlement ng Order", "Settlement ng Order"),
            array("Pag-print ng Order", "Pag-print ng Order", "Pag-print ng Order"),
            array("Bulk na Pagtanggal", "Bulk na Pagtanggal", "Bulk na Pagtanggal"),
            array("Detalyadong Impormasyon ng Order", "Detalyadong Impormasyon ng Order", "Detalyadong Impormasyon ng Order"),
            array("I-edit ang Order", "I-edit ang Order", "I-edit ang Order"),
            array("Tanggalin ang Order", "Tanggalin ang Order", "Tanggalin ang Order"),
            array("Tingnan ang Logistik", "Tingnan ang Logistik", "Tingnan ang Logistik"),
            array("Magpadala ng Produkto", "Magpadala ng Produkto", "Magpadala ng Produkto"),
            array("Detalye", "", ""),
            array("Mag-publish ng Produkto", "Mag-publish ng Produkto", "Mag-publish ng Produkto"),
            array("I-edit ang Order", "I-edit ang Order", "I-edit ang Order"),
            array("Detalyadong Impormasyon ng Order", "Detalyadong Impormasyon ng Order", "Detalyadong Impormasyon ng Order"),
            array("Tingnan", "Tingnan", "Tingnan"),
            array("Detalyadong Impormasyon ng After-sales", "Detalyadong Impormasyon ng After-sales", "Detalyadong Impormasyon ng After-sales"),
            array("Settlement ng Order", "Settlement ng Order", "Settlement ng Order"),
            array("Pamamahala ng After-sales", "Pamamahala ng After-sales", "Pamamahala ng After-sales"),
            array("Magpadala ng Produkto", "Magpadala ng Produkto", "Magpadala ng Produkto"),
            array("Baguhin", "Baguhin", "Baguhin"),
            array("Pamamahala ng Rating", "Pamamahala ng Rating", "Pamamahala ng Rating"),
            array("Mga Setting ng Pre-order", "Mga Setting ng Pre-order", "Mga Setting ng Pre-order"),
            array("Talaan ng Benta", "Talaan ng Benta", "Talaan ng Benta"),
            array("Talaan ng Pre-order", "Talaan ng Pre-order", "Talaan ng Pre-order"),
            array("Detalye ng Rating", "", "Detalye ng Rating"),
            array("Miyembro", "Miyembro", "Miyembro"),
            array("Magdagdag ng Produkto", "Magdagdag ng Produkto", "Magdagdag ng Produkto"),
            array("Produkto ng Miyembro", "Produkto ng Miyembro", "Produkto ng Miyembro"),
            array("Mga Setting ng Miyembro", "Mga Setting ng Miyembro", "Mga Setting ng Miyembro"),
            array("I-edit ang Miyembro", "I-edit ang Miyembro", "I-edit ang Miyembro"),
            array("Magdagdag ng Miyembro", "Magdagdag ng Miyembro", "Magdagdag ng Miyembro"),
            array("Talaan ng Pagbili", "Talaan ng Pagbili", "Talaan ng Pagbili"),
            array("Listahan ng Miyembro", "Listahan ng Miyembro", "Listahan ng Miyembro"),
            array("Auction", "Auction", "Auction"),
            array("I-edit ang Order", "I-edit ang Order", "I-edit ang Order"),
            array("Magpadala ng Produkto", "Magpadala ng Produkto", "Magpadala ng Produkto"),
            array("Tingnan", "Tingnan", "Tingnan"),
            array("Settlement ng Order", "Settlement ng Order", "Settlement ng Order"),
            array("Detalyadong Impormasyon ng Bid", "Detalyadong Impormasyon ng Bid", "Detalyadong Impormasyon ng Bid"),
            array("Tingnan ang Auction Item", "Tingnan ang Auction Item", "Tingnan ang Auction Item"),
            array("Talaan ng Pagbabayad", "Talaan ng Pagbabayad", "Talaan ng Pagbabayad"),
            array("Detalyadong Impormasyon ng Order", "Detalyadong Impormasyon ng Order", "Detalyadong Impormasyon ng Order"),
            array("Order ng Auction", "Order ng Auction", "Order ng Auction"),
            array("Mag-publish ng Session", "Mag-publish ng Session", "Mag-publish ng Session"),
            array("Listahan ng Special Session", "Listahan ng Special Session", "Listahan ng Special Session"),
            array("Mga Setting ng Auction", "Mga Setting ng Auction", "Mga Setting ng Auction"),
            array("Mag-publish ng Special Session", "Mag-publish ng Special Session", "Mag-publish ng Special Session"),
            array("Talaan ng Security Deposit", "Talaan ng Security Deposit", "Talaan ng Security Deposit"),
            array("Listahan ng Auction Product", "Listahan ng Auction Product", "Listahan ng Auction Product"),
            array("Tingnan ang Session", "Tingnan ang Session", "Tingnan ang Session"),
            array("Pamamahala ng Rating", "Pamamahala ng Rating", "Pamamahala ng Rating"),
            array("Detalyadong Impormasyon ng Rating", "", "Detalyadong Impormasyon ng Rating"),
            array("Detalye ng Rating", "", "Detalye ng Rating"),
            array("Detalyadong Impormasyon ng Settlement", "", "Detalyadong Impormasyon ng Settlement"),
            array("Supplier", "Pamamahala ng Supplier", "Supplier"),
            array("Pamamahala ng Supplier", "Pamamahala ng Supplier", "Pamamahala ng Supplier"),
            array("Magdagdag ng Supplier", "Magdagdag ng Supplier", "Magdagdag ng Supplier"),
            array("Listahan ng Produkto", "Listahan ng Produkto", "Listahan ng Produkto"),
            array("Produkto na Nakaantala sa Pagsusuri", "Produkto na Nakaantala sa Pagsusuri", "Produkto na Nakaantala sa Pagsusuri"),
            array("Produkto na I-down dahil sa Labag sa Batas", "Produkto na I-down dahil sa Labag sa Batas", "Produkto na I-down dahil sa Labag sa Batas"),
            array("Listahan ng Produkto na Walang Supply", "Listahan ng Produkto na Walang Supply", "Listahan ng Produkto na Walang Supply"),
            array("Order ng Supplier", "Order ng Supplier", "Order ng Supplier"),
            array("Kitang sa Pagpapatakbo", "Kitang sa Pagpapatakbo", "Kitang sa Pagpapatakbo"),
            array("Pagsusuri ng Withdrawal", "Pagsusuri ng Withdrawal", "Pagsusuri ng Withdrawal"),
            array("Talaan ng Withdrawal", "Talaan ng Withdrawal", "Talaan ng Withdrawal"),
            array("Mga Setting ng Withdrawal", "Mga Setting ng Withdrawal", "Mga Setting ng Withdrawal"),
            array("Tingnan ang Detalyadong Impormasyon", "Tingnan ang Detalyadong Impormasyon", "Tingnan ang Detalyadong Impormasyon"),
            array("Detalyadong Impormasyon ng Order", "Detalyadong Impormasyon ng Order", "Detalyadong Impormasyon ng Order"),
            array("Settlement ng Order", "Settlement ng Order", "Settlement ng Order"),
            array("Detalyadong Impormasyon ng Settlement", "Detalyadong Impormasyon ng Settlement", "Detalyadong Impormasyon ng Settlement"),
            array("Group Buying", "Group Buying", "Group Buying"),
            array("Tingnan", "Tingnan", "Tingnan"),
            array("Settlement ng Order", "Settlement ng Order", "Settlement ng Order"),
            array("Detalye ng Rating", "Detalye ng Rating", "Detalye ng Rating"),
            array("Detalyadong Impormasyon", "Detalyadong Impormasyon", "Detalyadong Impormasyon"),
            array("Pamamahala ng Rating", "Pamamahala ng Rating", "Pamamahala ng Rating"),
            array("Detalyadong Impormasyon ng After-sales", "Detalyadong Impormasyon ng After-sales", "Detalyadong Impormasyon ng After-sales"),
            array("Detalyadong Impormasyon ng Order", "Detalyadong Impormasyon ng Order", "Detalyadong Impormasyon ng Order"),
            array("I-edit ang Order", "I-edit ang Order", "I-edit ang Order"),
            array("Order ng Group Buying", "Order ng Group Buying", "Order ng Group Buying"),
            array("Mga Setting ng Group Buying", "Mga Setting ng Group Buying", "Mga Setting ng Group Buying"),
            array("Magdagdag ng Aktibidad ng Group Buying", "Magdagdag ng Aktibidad ng Group Buying", "Magdagdag ng Aktibidad ng Group Buying"),
            array("Talaan ng Paglahok sa Group Buying", "Talaan ng Paglahok sa Group Buying", "Talaan ng Paglahok sa Group Buying"),
            array("Detalyadong Impormasyon ng Pagbubukas ng Group", "Detalyadong Impormasyon ng Pagbubukas ng Group", "Detalyadong Impormasyon ng Pagbubukas ng Group"),
            array("Talaan ng Pagbubukas ng Group", "Talaan ng Pagbubukas ng Group", "Talaan ng Pagbubukas ng Group"),
            array("Aktibidad ng Group Buying", "Aktibidad ng Group Buying", "Aktibidad ng Group Buying"),
            array("Pamamahala ng After-sales", "Pamamahala ng After-sales", "Pamamahala ng After-sales"),
            array("Magpadala ng Produkto", "Magpadala ng Produkto", "Magpadala ng Produkto"),
            array("Live Streaming", "", ""),
            array("Pamamahala ng Streamer", null, null),
            array("Produkto ng Live Streaming", "", ""),
            array("Session ng Live Streaming", "", ""),
            array("Order ng Live Streaming", "", ""),
            array("Talaan ng Paglipat", "", ""),
            array("Tingnan ang Mga Tagasunod", "", ""),
            array("Detalyadong Impormasyon ng Produkto", "", ""),
            array("Tingnan ang Mga Manonood", "", ""),
            array("Tingnan ang Order", "", ""),
            array("Detalyadong Impormasyon ng Order", "", ""),
            array("I-edit ang Order", "", ""),
            array("After-sales ng Order", "", ""),
            array("Pamamahala ng Rating", "", ""),
            array("Settlement ng Order", "", ""),
            array("Detalye ng Komento", "", ""),
            array("Baguhin ang Komento", "", ""),
            array("Refund at After-sales", "", ""),
            array("Detalyadong Impormasyon ng After-sales", "", ""),
            array("Tingnan ang Produkto", "", ""),
            array("Data", "", "Data"),
            array("Ulat ng User", "Ulat ng Bagong User", "Ulat ng Bagong User"),
            array("Ulat ng Order", "Ulat ng Order", "Ulat ng Order"),
            array("Ulat ng Produkto", "Ulat ng Produkto", "Ulat ng Produkto"),
            array("Permiso", "", "Permiso"),
            array("Listahan ng Administrator", "Listahan ng Administrator", "Listahan ng Administrator"),
            array("Listahan ng Administrator", "Listahan ng Administrator", "Listahan ng Administrator"),
            array("Magdagdag ng Administrator", "Magdagdag ng Administrator", "Magdagdag ng Administrator"),
            array("I-edit ang Administrator", "I-edit ang Administrator", "I-edit ang Administrator"),
            array("I-enable", "I-enable", "I-enable"),
            array("I-disable", "I-disable", "I-disable"),
            array("Tanggalin", "Tanggalin", "Tanggalin"),
            array("Pamamahala ng Papel", "Pamamahala ng Papel", "Pamamahala ng Papel"),
            array("Pamamahala ng Papel", "Listahan ng Papel", "Listahan ng Papel"),
            array("Magdagdag ng Papel", "Magdagdag ng Papel", "Magdagdag ng Papel"),
            array("I-edit ang Papel", "I-edit ang Papel", "I-edit ang Papel"),
            array("Tingnan ang Papel", "Tingnan ang Papel", "Tingnan ang Papel"),
            array("Tanggalin ang Papel", "Tanggalin ang Papel", "Tanggalin ang Papel"),
            array("Log ng Administrator", "Log ng Administrator", "Log ng Administrator"),
            array("Bulk na Pagtanggal", "Bulk na Pagtanggal", "Bulk na Pagtanggal"),
            array("I-export", "I-export", "I-export"),
            array("Log ng Administrator", "Listahan ng Log", "Listahan ng Log"),
            array("Yaman", "", "Yaman"),
            array("Pamamahala ng Larawan", "Pamamahala ng Larawan", "Pamamahala ng Larawan"),
            array("Bulk na Pagtanggal", "Bulk na Pagtanggal", "Bulk na Pagtanggal"),
            array("Pamamahala ng Larawan", "Listahan ng Larawan", "Listahan ng Larawan"),
            array("Pamamahala ng Wika", "", "Pamamahala ng Wika"),
            array("Listahan", "", "Listahan"),
            array("Magdagdag ng Wika", "", "Magdagdag ng Wika"),
            array("Mga Setting ng Internationalization", "", "Mga Setting ng Internationalization"),
            array("Mga Setting ng Internationalization", "", "Mga Setting ng Internationalization"),
            array("Pamamahala ng Rehiyon", "", ""),
            array("Magdagdag ng Bagong Rehiyon", "", ""),
            array("Listahan ng Rehiyon", "", ""),
            array("Pamamahala ng Bansa", "", ""),
            array("Listahan ng Bansa", "", ""),
            array("Magdagdag ng Bagong Bansa", "", ""),
            array("Pamamahala ng Perang Barya", "", ""),
            array("Listahan ng Perang Barya", "", ""),
            array("Magdagdag ng Bagong Perang Barya", "", ""),
            array("Magdagdag ng Waybill", "", ""),
            array("Kanselahin ang Order", "", ""),
            array("Manual na Pagproseso", "", ""),
            array("I-export", "", ""),
            array("Pagtingin sa Order", "", "Pagtingin sa Order"),
            array("I-export", "", ""),
            array("Aprubahan", "", ""),
            array("Tanggihan", "", ""),
            array("Mabilis na Configuration", "", "Mabilis na Configuration"),
            array("Mabilis na Configuration", "", "Mabilis na Configuration"),
            array("Mga Setting ng Email", "", "Mga Setting ng Email"),
            array("Mga Setting ng Email", "", "Mga Setting ng Email"),
        );

        foreach($list as $k => $v)
        {
            $list[$k]['title'] = $list2[$k]['0'];
            $list[$k]['briefintroduction'] = $list2[$k]['1'];
            $list[$k]['guide_name'] = $list2[$k]['2'];
        }

        $r3 = Db::name('core_menu')->insertAll($list);
        print_r('``````');echo "<br>";die;
        print_r($list);die;
    }

    // PC店铺菜单
    public function pcindex()
    {
        print_r('PC店铺菜单');echo "<br>";
        die;
        // 0-1000      zh_CN   186   中文简体
        // 1000-2000   en_US   840   英语
        // 2000-3000   zh_TW   852   中文繁体
        // 3000-4000   ja_JP   81    日语
        // 4000-5000   ru_RU   7     俄罗斯语
        // 5000-6000   ms_MY   60    马来西亚语
        // 6000-7000   id_ID   62    印度尼西亚语
        // 7000-8000   fil_PH   63    菲律宾语
        $num = 7000;
        $lang_code = 'en_PH';
        $country_num = 63;
        // 把value值翻译成菲律宾语并转成PHP数组格式
        $list = array();
        $list1 = array();
        $list2 = array();
        $sql0 = "select * from lkt_menu where is_display = 0 and lang_code = 'zh_CN' limit 0,200";
        // $sql0 = "select * from lkt_menu where is_display = 0 and lang_code = 'zh_CN' limit 200,400";
        $r0 = Db::query($sql0);
        if($r0)
        {
            foreach($r0 as $k0 => $v0)
            {
                $v0['id'] = $v0['id'] + $num;
                if($v0['sid'] != 0)
                {
                    $v0['sid'] = $v0['sid'] + $num;
                }
                $v0['lang_code'] = $lang_code;
                $v0['country_num'] = $country_num;
                $list[] = $v0;
                $list1[] = array('0'=>$v0['name'],'1'=>$v0['text']);
            }
        }

        print_r($list1);die;

        $list2 = array(
            0 => array(
                0 => 'Pamamahala sa Mga Pagsusuri',
                1 => 'Pamamahala sa Mga Pagsusuri'
            ),
            1 => array(
                0 => 'Baguhin',
                1 => 'Baguhin'
            ),
            2 => array(
                0 => 'I-edit ang Order',
                1 => 'I-edit ang Order'
            ),
            3 => array(
                0 => 'Pagpapadala ng Produkto',
                1 => 'Pagpapadala ng Produkto'
            ),
            4 => array(
                0 => 'Detalyadong Impormasyon ng Order',
                1 => 'Detalyadong Impormasyon ng Order'
            ),
            5 => array(
                0 => 'Tingnan ang Produkto',
                1 => 'Tingnan ang Produkto'
            ),
            6 => array(
                0 => 'Tag ng Flash Sale',
                1 => 'Tag ng Flash Sale'
            ),
            7 => array(
                0 => 'Talaan ng Flash Sale',
                1 => 'Talaan ng Flash Sale'
            ),
            8 => array(
                0 => 'Order ng Flash Sale',
                1 => 'Order ng Flash Sale'
            ),
            9 => array(
                0 => 'Group Buy',
                1 => 'Group Buy'
            ),
            10 => array(
                0 => 'Tingnan ang Logistik',
                1 => 'Tingnan ang Logistik'
            ),
            11 => array(
                0 => 'Pagpapadala ng Produkto',
                1 => 'Pagpapadala ng Produkto'
            ),
            12 => array(
                0 => 'Isara ang Order',
                1 => 'Isara ang Order'
            ),
            13 => array(
                0 => 'I-export',
                1 => 'I-export'
            ),
            14 => array(
                0 => 'Tanggalin',
                1 => 'Tanggalin'
            ),
            15 => array(
                0 => 'Simulan',
                1 => 'Simulan'
            ),
            16 => array(
                0 => 'I-edit',
                1 => 'I-edit'
            ),
            17 => array(
                0 => 'Tingnan ang Detalyadong Impormasyon',
                1 => 'Tingnan ang Detalyadong Impormasyon'
            ),
            18 => array(
                0 => 'Tapusin',
                1 => 'Tapusin'
            ),
            19 => array(
                0 => 'Bulk na Pagtanggal',
                1 => 'Bulk na Pagtanggal'
            ),
            20 => array(
                0 => 'Mga Setting ng Group Buy',
                1 => 'Mga Setting ng Group Buy'
            ),
            21 => array(
                0 => 'Kampanya ng Group Buy',
                1 => 'Kampanya ng Group Buy'
            ),
            22 => array(
                0 => 'Talaan ng Pagbuo ng Group',
                1 => 'Talaan ng Pagbuo ng Group'
            ),
            23 => array(
                0 => 'Detalyadong Impormasyon ng Pagbuo ng Group',
                1 => 'Detalyadong Impormasyon ng Pagbuo ng Group'
            ),
            24 => array(
                0 => 'Talaan ng Pagsali sa Group',
                1 => 'Talaan ng Pagsali sa Group'
            ),
            25 => array(
                0 => 'Magdagdag ng Kampanya ng Group Buy',
                1 => 'Magdagdag ng Kampanya ng Group Buy'
            ),
            26 => array(
                0 => 'Order ng Group Buy',
                1 => 'Order ng Group Buy'
            ),
            27 => array(
                0 => 'I-edit ang Order',
                1 => 'I-edit ang Order'
            ),
            28 => array(
                0 => 'Detalyadong Impormasyon ng Order',
                1 => 'Detalyadong Impormasyon ng Order'
            ),
            29 => array(
                0 => 'Pamamahala sa Serbisyo pagkatapos ng Benta',
                1 => 'Pamamahala sa Serbisyo pagkatapos ng Benta'
            ),
            30 => array(
                0 => 'Detalyadong Impormasyon ng Serbisyo pagkatapos ng Benta',
                1 => 'Detalyadong Impormasyon ng Serbisyo pagkatapos ng Benta'
            ),
            31 => array(
                0 => 'Pamamahala sa Mga Pagsusuri',
                1 => 'Pamamahala sa Mga Pagsusuri'
            ),
            32 => array(
                0 => 'Detalyadong Impormasyon',
                1 => 'Detalyadong Impormasyon'
            ),
            33 => array(
                0 => 'Detalyadong Listahan ng Pagsusuri',
                1 => 'Detalyadong Listahan ng Pagsusuri'
            ),
            34 => array(
                0 => 'Pagkakasundo ng Order',
                1 => 'Pagkakasundo ng Order'
            ),
            35 => array(
                0 => 'Tingnan',
                1 => 'Tingnan'
            ),
            36 => array(
                0 => 'Supplier',
                1 => 'Supplier'
            ),
            37 => array(
                0 => 'Detalyadong Impormasyon ng Serbisyo pagkatapos ng Benta',
                1 => 'Detalyadong Impormasyon ng Serbisyo pagkatapos ng Benta'
            ),
            38 => array(
                0 => 'Detalyadong Impormasyon ng Pagkakasundo',
                1 => 'Detalyadong Impormasyon ng Pagkakasundo'
            ),
            39 => array(
                0 => 'Bulk na Pagtanggal',
                1 => 'Bulk na Pagtanggal'
            ),
            40 => array(
                0 => 'I-export',
                1 => 'I-export'
            ),
            41 => array(
                0 => 'Pumili ng Produkto',
                1 => 'Pumili ng Produkto'
            ),
            42 => array(
                0 => 'Ipasok ang Produkto para sa Pagbebenta',
                1 => 'Ipasok ang Produkto para sa Pagbebenta'
            ),
            43 => array(
                0 => 'Ilabas at Ipasok ang Produkto',
                1 => 'Ilabas at Ipasok ang Produkto'
            ),
            44 => array(
                0 => 'I-edit',
                1 => 'I-edit'
            ),
            45 => array(
                0 => 'Tanggalin',
                1 => 'Tanggalin'
            ),
            46 => array(
                0 => 'Dropshipping sa Isang Klik',
                1 => 'Dropshipping sa Isang Klik'
            ),
            47 => array(
                0 => 'Tingnan ang Logistik',
                1 => 'Tingnan ang Logistik'
            ),
            48 => array(
                0 => 'Isara ang Order',
                1 => 'Isara ang Order'
            ),
            49 => array(
                0 => 'Tanggalin ang Order',
                1 => 'Tanggalin ang Order'
            ),
            50 => array(
                0 => 'Pagsusuri',
                1 => 'Pagsusuri'
            ),
            51 => array(
                0 => 'Tingnan',
                1 => 'Tingnan'
            ),
            52 => array(
                0 => 'Manwal na Pagproseso',
                1 => 'Manwal na Pagproseso'
            ),
            53 => array(
                0 => 'I-edit ang Order',
                1 => 'I-edit ang Order'
            ),
            54 => array(
                0 => 'Detalyadong Impormasyon ng Order',
                1 => 'Detalyadong Impormasyon ng Order'
            ),
            55 => array(
                0 => 'Tingnan ang Detalyadong Impormasyon',
                1 => 'Tingnan ang Detalyadong Impormasyon'
            ),
            56 => array(
                0 => 'Pagkakasundo ng Order',
                1 => 'Pagkakasundo ng Order'
            ),
            57 => array(
                0 => 'Pagbabalik ng Pera at Serbisyo pagkatapos ng Benta',
                1 => 'Pagbabalik ng Pera at Serbisyo pagkatapos ng Benta'
            ),
            58 => array(
                0 => 'Order ng Supplier',
                1 => 'Order ng Supplier'
            ),
            59 => array(
                0 => 'Produkto ng Supplier',
                1 => 'Produkto ng Supplier'
            ),
            60 => array(
                0 => 'Diskonto sa Limitadong Oras',
                1 => 'Diskonto sa Limitadong Oras'
            ),
            61 => array(
                0 => 'Magpadala ng Produkto',
                1 => ''
            ),
            62 => array(
                0 => 'Detalyadong Impormasyon ng Order',
                1 => 'Detalyadong Impormasyon ng Order'
            ),
            63 => array(
                0 => 'I-edit ang Order',
                1 => 'I-edit ang Order'
            ),
            64 => array(
                0 => 'Tingnan',
                1 => 'Tingnan'
            ),
            65 => array(
                0 => 'Pagkakasundo ng Order',
                1 => 'Pagkakasundo ng Order'
            ),
            66 => array(
                0 => 'Detalyadong Listahan ng Pagsusuri',
                1 => 'Detalyadong Listahan ng Pagsusuri'
            ),
            67 => array(
                0 => 'Tingnan ang Pagsusuri',
                1 => 'Tingnan ang Pagsusuri'
            ),
            68 => array(
                0 => 'Pamamahala sa Mga Pagsusuri',
                1 => 'Pamamahala sa Mga Pagsusuri'
            ),
            69 => array(
                0 => 'Detalyadong Impormasyon ng Serbisyo pagkatapos ng Benta',
                1 => 'Detalyadong Impormasyon ng Serbisyo pagkatapos ng Benta'
            ),
            70 => array(
                0 => 'Listahan ng Kampanya',
                1 => 'Listahan ng Kampanya'
            ),
            71 => array(
                0 => 'Mga Setting ng Kampanya',
                1 => 'Mga Setting ng Kampanya'
            ),
            72 => array(
                0 => 'Magdagdag ng Kampanya',
                1 => 'Magdagdag ng Kampanya'
            ),
            73 => array(
                0 => 'Order ng Kampanya',
                1 => 'Order ng Kampanya'
            ),
            74 => array(
                0 => 'Pamamahala sa Serbisyo pagkatapos ng Benta',
                1 => 'Pamamahala sa Serbisyo pagkatapos ng Benta'
            ),
            75 => array(
                0 => 'Mall ng Puntos',
                1 => 'Mall ng Puntos'
            ),
            76 => array(
                0 => 'Mga Setting ng Mall',
                1 => 'Mga Setting ng Mall'
            ),
            77 => array(
                0 => 'Bulk na Pagtanggal',
                1 => 'Bulk na Pagtanggal'
            ),
            78 => array(
                0 => 'Itaas sa Pinakamataas',
                1 => 'Itaas sa Pinakamataas'
            ),
            79 => array(
                0 => 'Tanggalin',
                1 => 'Tanggalin'
            ),
            80 => array(
                0 => 'I-export',
                1 => 'I-export'
            ),
            81 => array(
                0 => 'I-print ang Order',
                1 => 'I-print ang Order'
            ),
            82 => array(
                0 => 'Tingnan ang Logistik',
                1 => 'Tingnan ang Logistik'
            ),
            83 => array(
                0 => 'Isara ang Order',
                1 => 'Isara ang Order'
            ),
            84 => array(
                0 => 'Detalyadong Listahan ng Pagsusuri',
                1 => ''
            ),
            85 => array(
                0 => 'Talaan ng Pagpapalit ng Puntos',
                1 => 'Talaan ng Pagpapalit ng Puntos'
            ),
            86 => array(
                0 => 'Magdagdag ng Produkto',
                1 => 'Magdagdag ng Produkto'
            ),
            87 => array(
                0 => 'I-edit',
                1 => 'I-edit'
            ),
            88 => array(
                0 => 'I-edit ang Order',
                1 => 'I-edit ang Order'
            ),
            89 => array(
                0 => 'Pagpapadala ng Produkto',
                1 => 'Pagpapadala ng Produkto'
            ),
            90 => array(
                0 => 'Detalyadong Impormasyon ng Order',
                1 => 'Detalyadong Impormasyon ng Order'
            ),
            91 => array(
                0 => 'Pamamahala sa Serbisyo pagkatapos ng Benta',
                1 => 'Pamamahala sa Serbisyo pagkatapos ng Benta'
            ),
            92 => array(
                0 => 'Detalyadong Impormasyon ng Serbisyo pagkatapos ng Benta',
                1 => 'Detalyadong Impormasyon ng Serbisyo pagkatapos ng Benta'
            ),
            93 => array(
                0 => 'Pamamahala sa Mga Pagsusuri',
                1 => 'Pamamahala sa Mga Pagsusuri'
            ),
            94 => array(
                0 => 'Baguhin',
                1 => 'Baguhin'
            ),
            95 => array(
                0 => 'Pagkakasundo ng Order',
                1 => 'Pagkakasundo ng Order'
            ),
            96 => array(
                0 => 'Tingnan',
                1 => 'Tingnan'
            ),
            97 => array(
                0 => 'Order ng Pagpapalit ng Puntos',
                1 => 'Order ng Pagpapalit ng Puntos'
            ),
            98 => array(
                0 => 'Produkto ng Puntos',
                1 => 'Produkto ng Puntos'
            ),
            99 => array(
                0 => 'Pamamahala sa Template',
                1 => 'Pamamahala sa Template'
            ),
            100 => array(
                0 => 'Magdagdag ng Template',
                1 => 'Magdagdag ng Template'
            ),
            101 => array(
                0 => 'Listahan ng Slider ng Larawan',
                1 => 'Listahan ng Slider ng Larawan'
            ),
            102 => array(
                0 => 'Produkto ng Kampanya',
                1 => 'Produkto ng Kampanya'
            ),
            103 => array(
                0 => 'I-edit ang Kampanya',
                1 => 'I-edit ang Kampanya'
            ),
            104 => array(
                0 => 'Magdagdag ng Kampanya',
                1 => 'Magdagdag ng Kampanya'
            ),
            105 => array(
                0 => 'Pamamahala sa Kampanya',
                1 => 'Pamamahala sa Kampanya'
            ),
            106 => array(
                0 => 'Navbar ng UI',
                1 => 'Navbar ng UI'
            ),
            107 => array(
                0 => 'Pamamahala sa Kategorya',
                1 => 'Pamamahala sa Kategorya'
            ),
            108 => array(
                0 => 'Magdagdag ng Slider ng Larawan',
                1 => 'Magdagdag ng Slider ng Larawan'
            ),
            109 => array(
                0 => 'Pamamahala sa Template',
                1 => 'Pamamahala sa Template'
            ),
            110 => array(
                0 => 'I-edit ang Slider ng Larawan',
                1 => 'I-edit ang Slider ng Larawan'
            ),
            111 => array(
                0 => 'Auction',
                1 => 'Auction'
            ),
            112 => array(
                0 => 'Order ng Auction',
                1 => 'Order ng Auction'
            ),
            113 => array(
                0 => 'Listahan ng Espesyal na Bida',
                1 => 'Listahan ng Espesyal na Bida'
            ),
            114 => array(
                0 => 'Isara ang Order',
                1 => 'Isara ang Order'
            ),
            115 => array(
                0 => 'Tingnan ang Logistik',
                1 => 'Tingnan ang Logistik'
            ),
            116 => array(
                0 => 'Listahan ng Espesyal na Bida ng Tindahan',
                1 => 'Listahan ng Espesyal na Bida ng Tindahan'
            ),
            117 => array(
                0 => 'Bulk na Pagtanggal',
                1 => 'Bulk na Pagtanggal'
            ),
            118 => array(
                0 => 'Tingnan ang Produkto ng Auction',
                1 => 'Tingnan ang Produkto ng Auction'
            ),
            119 => array(
                0 => 'I-print ang Order',
                1 => 'I-print ang Order'
            ),
            120 => array(
                0 => 'I-edit',
                1 => 'I-edit'
            ),
            121 => array(
                0 => 'Mag-publish ng Espesyal na Bida',
                1 => 'Mag-publish ng Espesyal na Bida'
            ),
            122 => array(
                0 => 'Produkto ng Auction ng Tindahan',
                1 => 'Produkto ng Auction ng Tindahan'
            ),
            123 => array(
                0 => 'Tanggalin ang Espesyal na Bida',
                1 => 'Tanggalin ang Espesyal na Bida'
            ),
            124 => array(
                0 => 'Detalyadong Impormasyon ng Offer',
                1 => 'Detalyadong Impormasyon ng Offer'
            ),
            125 => array(
                0 => 'Kanselahin ang Pagpaparehistro',
                1 => 'Kanselahin ang Pagpaparehistro'
            ),
            126 => array(
                0 => 'Pagkakasundo ng Order',
                1 => 'Pagkakasundo ng Order'
            ),
            127 => array(
                0 => 'Detalyadong Impormasyon',
                1 => 'Detalyadong Impormasyon'
            ),
            128 => array(
                0 => 'Detalyadong Impormasyon ng Order',
                1 => 'Detalyadong Impormasyon ng Order'
            ),
            129 => array(
                0 => 'Talaan ng Pagbabayad',
                1 => 'Talaan ng Pagbabayad'
            ),
            130 => array(
                0 => 'Detalyadong Listahan ng Pagsusuri',
                1 => 'Detalyadong Listahan ng Pagsusuri'
            ),
            131 => array(
                0 => 'Tingnan',
                1 => 'Tingnan'
            ),
            132 => array(
                0 => 'I-edit ang Order',
                1 => 'I-edit ang Order'
            ),
            133 => array(
                0 => 'Pamamahala sa Mga Pagsusuri',
                1 => 'Pamamahala sa Mga Pagsusuri'
            ),
            134 => array(
                0 => 'Pagpapadala ng Produkto',
                1 => 'Pagpapadala ng Produkto'
            ),
            135 => array(
                0 => 'Branch ng Tindahan',
                1 => ''
            ),
            136 => array(
                0 => 'Listahan ng Branch ng Tindahan',
                1 => 'Listahan ng Branch ng Tindahan'
            ),
            137 => array(
                0 => 'I-edit',
                1 => 'I-edit'
            ),
            138 => array(
                0 => 'Tanggalin',
                1 => 'Tanggalin'
            ),
            139 => array(
                0 => 'Bulk na Pagtanggal',
                1 => 'Bulk na Pagtanggal'
            ),
            140 => array(
                0 => 'Administrator',
                1 => 'Administrator'
            ),
            141 => array(
                0 => 'Magdagdag ng Branch ng Tindahan',
                1 => 'Magdagdag ng Branch ng Tindahan'
            ),
            142 => array(
                0 => 'Listahan',
                1 => 'Listahan'
            ),
            143 => array(
                0 => 'Mga Setting',
                1 => ''
            ),
            144 => array(
                0 => 'Impormasyon ng Tindahan',
                1 => 'Impormasyon ng Tindahan'
            ),
            145 => array(
                0 => 'Listahan',
                1 => 'Impormasyon'
            ),
            146 => array(
                0 => 'I-edit ang Impormasyon',
                1 => 'I-edit ang Impormasyon'
            ),
            147 => array(
                0 => 'Baguhin ang Password',
                1 => 'Baguhin ang Password'
            ),
            148 => array(
                0 => 'Mga Setting ng Pag-print',
                1 => 'Mga Setting ng Pag-print'
            ),
            149 => array(
                0 => 'Mga Setting ng Slider ng Larawan',
                1 => 'Mga Setting ng Slider ng Larawan'
            ),
            150 => array(
                0 => 'Listahan',
                1 => 'Listahan ng Slider ng Larawan'
            ),
            151 => array(
                0 => 'I-edit',
                1 => 'I-edit'
            ),
            152 => array(
                0 => 'Magdagdag ng Slider ng Larawan',
                1 => 'Magdagdag ng Slider ng Larawan'
            ),
            153 => array(
                0 => 'Mga Karapatan',
                1 => ''
            ),
            154 => array(
                0 => 'Listahan ng Menu',
                1 => 'Listahan ng Menu'
            ),
            155 => array(
                0 => 'I-edit ang Menu',
                1 => 'I-edit ang Menu'
            ),
            156 => array(
                0 => 'Tingnan ang Mas Mababang Antas',
                1 => 'Tingnan ang Mas Mababang Antas'
            ),
            157 => array(
                0 => 'Magdagdag ng Menu',
                1 => 'Magdagdag ng Menu'
            ),
            158 => array(
                0 => 'Listahan ng Menu',
                1 => 'Listahan ng Menu'
            ),
            159 => array(
                0 => 'Listahan ng Administrator',
                1 => 'Listahan ng Administrator'
            ),
            160 => array(
                0 => 'I-edit ang Administrator',
                1 => 'I-edit ang Administrator'
            ),
            161 => array(
                0 => 'Magdagdag ng Administrator',
                1 => 'Magdagdag ng Administrator'
            ),
            162 => array(
                0 => 'Listahan',
                1 => 'Listahan'
            ),
            163 => array(
                0 => 'Pamamahala sa Mga Papel',
                1 => 'Pamamahala sa Mga Papel'
            ),
            164 => array(
                0 => 'Listahan',
                1 => 'Listahan ng Mga Papel'
            ),
            165 => array(
                0 => 'Tingnan ang Papel',
                1 => 'Tingnan ang Papel'
            ),
            166 => array(
                0 => 'I-edit ang Papel',
                1 => 'I-edit ang Papel'
            ),
            167 => array(
                0 => 'Magdagdag ng Papel',
                1 => 'Magdagdag ng Papel'
            ),
            168 => array(
                0 => 'Log ng Administrator',
                1 => 'Log ng Administrator'
            )
        );

        foreach($list as $k => $v)
        {
            $list[$k]['name'] = $list2[$k]['0'];
            $list[$k]['text'] = $list2[$k]['1'];
        }

        $r3 = Db::name('menu')->insertAll($list);
        print_r('``````');echo "<br>";die;

        // $list0 = array();
        // $id = 0;
        // $sql0 = "select * from lkt_menu where is_display = 0 and sid = 0 and lang_code = 'zh_CN' order by sort desc";
        // $r0 = Db::query($sql0);
        // if($r0)
        // {
        //     foreach($r0 as $k0 => $v0)
        //     {
        //         $id0 = $v0['id'];
        //         $id++;
        //         $id_0 = $id;
        //         $v0['id'] = $id_0;
        //         $list0[] = $v0;
        //         $sql1 = "select * from lkt_menu where is_display = 0 and sid = '$id0' and lang_code = 'zh_CN' order by sort desc";
        //         $r1 = Db::query($sql1);
        //         if($r1)
        //         {
        //             foreach($r1 as $k1 => $v1)
        //             {
        //                 $id1 = $v1['id'];
        //                 $id++;
        //                 $id_1 = $id;
        //                 $v1['id'] = $id_1;
        //                 $v1['sid'] = $v0['id'];
        //                 $list0[] = $v1;
        //                 $sql2 = "select * from lkt_menu where is_display = 0 and sid = '$id1' and lang_code = 'zh_CN' order by sort desc";
        //                 $r2 = Db::query($sql2);
        //                 if($r2)
        //                 {
        //                     foreach($r2 as $k2 => $v2)
        //                     {
        //                         $id2 = $v2['id'];
        //                         $id++;
        //                         $id_2 = $id;
        //                         $v2['id'] = $id_2;
        //                         $v2['sid'] = $v1['id'];
        //                         $list0[] = $v2;
        //                         $sql3 = "select * from lkt_menu where is_display = 0 and sid = '$id2' and lang_code = 'zh_CN' order by sort desc";
        //                         $r3 = Db::query($sql3);
        //                         if($r3)
        //                         {
        //                             foreach($r3 as $k3 => $v3)
        //                             {
        //                                 $id++;
        //                                 $id_3 = $id;
        //                                 $v3['id'] = $id_3;
        //                                 $v3['sid'] = $v2['id'];
        //                                 $list0[] = $v3;
        //                             }
        //                         }
        //                     }
        //                 }
        //             }
        //         }
        //     }
        // }

        // print_r(count($list0));echo "<br>";
        // print_r($list0);die;

        // $r3 = Db::name('menu')->insertAll($list0);
        print_r($r3);echo "<br>";
        print_r('``````');die;
    }
}

