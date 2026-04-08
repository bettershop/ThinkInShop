<?php
namespace app\admin\controller\admin;

use app\BaseController;
use app\common;
use think\facade\Db;
use think\facade\Session;
use think\facade\Request;

use app\common\LaiKeLogUtils;
use app\common\ServerPath;
/**
 * 功能：后台报表类
 * 修改人：PJY
 */

class Report extends BaseController
{   
    //商城首页
    public function storeIndex()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        //状态查询
        $mon = date("Y-m");//当前月份
        //得到系统的年月
        $tmp_date = date("Ym");
        //切割出年份
        $tmp_year = substr($tmp_date, 0, 4);
        //切割出月份
        $tmp_mon = substr($tmp_date, 4, 2);
        $tmp_forwardmonth = mktime(0, 0, 0, $tmp_mon - 1, 1, $tmp_year);
        //得到当前月的上一个月
        $lastmon = date("Y-m", $tmp_forwardmonth);
        //今天
        $today = date("Y-m-d");
        $day_1 = date("Y-m-d", strtotime("-1 day"));//昨天
        // 获取当前日期是星期几
        $dayOfWeek = date('N', strtotime($today));
        // 计算距离本周一的天数差
        $daysToMonday = $dayOfWeek - 1;
        // 获取本周星期一的日期
        $monday = date('Y-m-d', strtotime("-$daysToMonday days", strtotime($today)));
        //获取上周一日期
        $lastWeekMonday = date('Y-m-d', strtotime("-7 days", strtotime($monday)));
        //获取本周是当月第几周
        $first_day_of_month = date('Y-m-01');
        $first_day_of_month_week = date('N', strtotime($first_day_of_month)); // 获取当月1号时间戳 + 星期数
        $targetDay = date('j'); // 获取目标日期是当月第几天
        $w = floor(($targetDay + $first_day_of_month_week - 2) / 7) + 1; // 核心计算：当月第几周（适配周一为起始的公式）
        // $w = date('w') - $first_day_of_month_week + 1;

        $goodsData = array();//商品数据块
        //出售商品数
        $goodsData['onSaledAmount'] = 0;
        $sql0 = "select ifnull(count(id),0) as num from lkt_product_list where store_id = '$store_id' and recycle = 0 and mch_status = 2 and status = 2 ";
        $res0 = Db::query($sql0);
        if($res0)
        {
            $goodsData['onSaledAmount'] = $res0[0]['num'];
        }
        //商品总数
        $goodsData['allGoodsAmount'] = 0;
        $sql1 = "select ifnull(count(id),0) as num from lkt_product_list where store_id = '$store_id' and recycle = 0 and mch_status = 2 ";
        $res1 = Db::query($sql1);
        if($res1)
        {
            $goodsData['allGoodsAmount'] = $res1[0]['num'];
        }
        //下架商品数
        $goodsData['underSaledAmount'] = 0;
        $sql2 = "select ifnull(count(id),0) as num from lkt_product_list where store_id = '$store_id' and recycle = 0 and status = 3 ";
        $res2 = Db::query($sql2);
        if($res2)
        {
            $goodsData['underSaledAmount'] = $res2[0]['num'];
        }
        //预警商品数
        $goodsData['warnenAnmun'] = 0;
        $sql3 = "SELECT count( DISTINCT c.id ) as num FROM lkt_stock AS b
                    RIGHT JOIN lkt_product_list AS a ON b.product_id = a.id
                    RIGHT JOIN lkt_configure AS c ON b.attribute_id = c.id
                    LEFT JOIN lkt_mch AS m ON a.mch_id = m.id WHERE a.store_id = '$store_id' AND a.store_id = b.store_id AND a.recycle = 0 AND a.mch_status = 2 AND b.type = 2 AND a.gongyingshang = 0 ";
        $res3 = Db::query($sql3);
        if($res3)
        {
            $goodsData['warnenAnmun'] = $res3[0]['num'];
        }

        $volumeData = array();//营业额块
        $volumeData['currentVolume'] = 0;//实时营业额
        $sql4 = "select ifnull(sum(a.z_price),0) as total from lkt_order a where a.store_id = '$store_id' and DATE_FORMAT(a.pay_time, '%Y-%m-%d') = '$today'";
        $res4 = Db::query($sql4);
        if($res4)
        {
            $volumeData['currentVolume'] = $res4[0]['total'];
        }
        $volumeData['allVolume'] = 0;//累计营业额
        // $sql5 = "SELECT (IFNULL(SUM(a.z_price), 0) - (SELECT IFNULL(SUM(real_money), 0) FROM lkt_return_order WHERE r_type IN (4, 9) AND store_id = '$store_id' AND sNo IN (SELECT sNo FROM lkt_order WHERE store_id = '$store_id'))) total FROM lkt_order a WHERE store_id = '$store_id'";
        $sql5 = "select ifnull(sum(a.z_price),0) as total from lkt_order a where a.store_id = '$store_id' and a.status not in (0,7) ";
        $res5 = Db::query($sql5);
        if($res5)
        {
            $volumeData['allVolume'] = round($res5[0]['total'],2);
        }
        $volumeData['monthtVolume'] = 0;//本月营业额
        $sql6 = "select ifnull(sum(a.z_price),0) as total from lkt_order a where a.store_id = '$store_id' and DATE_FORMAT(a.pay_time, '%Y-%m') = '$mon'";
        $res6 = Db::query($sql6);
        if($res6)
        {
            $volumeData['monthtVolume'] = $res6[0]['total'];
        }
        $volumeData['yesterdayVolume'] = 0;//昨日营业额
        $sql7 = "select ifnull(sum(a.z_price),0) as total from lkt_order a where a.store_id = '$store_id' and DATE_FORMAT(a.pay_time, '%Y-%m-%d') = '$day_1'";
        $res7 = Db::query($sql7);
        if($res7)
        {
            $volumeData['yesterdayVolume'] = $res7[0]['total'];
        }

        $volumeData['flag'] = "up";//标识
        $volumeData['rate'] = "100%";//营业额百分比(当日减去前一天的值除以前一日的营业额)
        if($volumeData['yesterdayVolume'] >0)
        {
            $rate = round(($volumeData['currentVolume'] - $volumeData['yesterdayVolume'])/$volumeData['yesterdayVolume'] * 100,2);
            if($rate > 0)
            {
                $volumeData['rate'] = $rate."%";
            }
            else
            {
                $volumeData['flag'] = "down";
                $volumeData['rate'] = abs($rate)."%";
            }
        }
        $volumeData['tbflag'] = $volumeData['flag'];//标识

        $mchData = array();//开通店铺模块
        $mchData['total'] = 0;//本周开通店铺数
        $sql8 = "select ifnull(count(id),0) as num from lkt_mch where store_id = '$store_id' and add_time >= '$monday' and review_status = 1 and recovery = 0 ";
        $res8 = Db::query($sql8);
        if($res8)
        {
            $mchData['total'] = $res8[0]['num'];
        }
        //上周开通店铺数
        $lastWeek_num = 0;
        $sql_lastWeek = "select ifnull(count(id),0) as num from lkt_mch where store_id = '$store_id' and add_time >= '$lastWeekMonday' and add_time < '$monday' and review_status = 1 ";
        $res_lastWeek = Db::query($sql_lastWeek);
        if($res_lastWeek)
        {
            $lastWeek_num = $res_lastWeek[0]['num'];
        }
        //上月本周开通店铺数
        //上月本周时间
        $monthPerWeek = $this->monthPerWeek(date("Y-m-d", strtotime("-1 month")),$w);
        $monthWeek_num = 0;
        $sql_monthWeek = "select ifnull(count(id),0) as num from lkt_mch where store_id = '$store_id' and add_time >= '".$monthPerWeek[0]."' and add_time <= '".$monthPerWeek[1]."' and review_status = 1 ";
        $res_monthWeek = Db::query($sql_monthWeek);
        if($res_monthWeek)
        {
            $monthWeek_num = $res_monthWeek[0]['num'];
        }
        $mchData['tbflag'] = "up";//周同比
        $mchData['tbflag'] = "100%";
        if($monthWeek_num > 0)
        {
            $rate0 = round(($mchData['total'] - $monthWeek_num)/$monthWeek_num * 100,2);
            if($rate0 > 0)
            {
                $mchData['tbflag'] = $rate0."%";
            }
            else
            {   
                $mchData['tbflag'] = "down";
                $mchData['tbflag'] = abs($rate0)."%";
            }
        }
        $mchData['hbflag'] = "up";//周环比
        $mchData['hbrate'] = "100%";
        if($lastWeek_num > 0)
        {
            $rate1 = round(($mchData['total'] - $lastWeek_num)/$lastWeek_num * 100,2);
            if($rate1 > 0)
            {
                $mchData['hbrate'] = $rate1."%";
            }
            else
            {   
                $mchData['tbflag'] = "down";
                $mchData['hbrate'] = abs($rate1)."%";
            }
        }

        $supplierCountData = array();//入驻供应商模块
        $supplierCountData['total'] = 0;//本周入驻供应商数
        $sql9 = "select ifnull(count(id),0) as num from lkt_supplier where store_id = '$store_id' and add_date >= '$monday' ";
        $res9 = Db::query($sql9);
        if($res9)
        {
            $supplierCountData['total'] = $res9[0]['num'];
        }
        //上周开通供应商数
        $lastWeek_nums = 0;
        $sql_lastWeeks = "select ifnull(count(id),0) as num from lkt_mch where store_id = '$store_id' and add_time >= '$lastWeekMonday' and add_time < '$monday' and review_status = 1 ";
        $res_lastWeeks = Db::query($sql_lastWeeks);
        if($res_lastWeeks)
        {
            $lastWeek_nums = $res_lastWeeks[0]['num'];
        }
        //上月本周开通店铺数
        //上月本周时间
        $monthPerWeeks = $this->monthPerWeek(date("Y-m-d", strtotime("-1 month")),$w);
        $monthWeek_nums = 0;
        $sql_monthWeeks = "select ifnull(count(id),0) as num from lkt_mch where store_id = '$store_id' and add_time >= '".$monthPerWeek[0]."' and add_time <= '".$monthPerWeek[1]."' and review_status = 1 ";
        $res_monthWeeks = Db::query($sql_monthWeeks);
        if($res_monthWeeks)
        {
            $monthWeek_nums = $res_monthWeeks[0]['num'];
        }
        $supplierCountData['tbflag'] = "up";//周环比
        $supplierCountData['tbflag'] = "100%";
        if($monthWeek_nums > 0)
        {
            $rate2 = round(($supplierCountData['total'] - $monthWeek_nums)/$monthWeek_nums * 100,2);
            if($rate2 > 0)
            {
                $supplierCountData['tbflag'] = $rate2."%";
            }
            else
            {   
                $supplierCountData['tbflag'] = "down";
                $supplierCountData['tbflag'] = abs($rate2)."%";
            }
        }
        $supplierCountData['hbflag'] = "up";//周环比
        $supplierCountData['hbrate'] = "100%";
        if($lastWeek_nums > 0)
        {
            $rate3 = round(($supplierCountData['total'] - $lastWeek_nums)/$lastWeek_nums * 100,2);
            if($rate3 > 0)
            {
                $supplierCountData['hbrate'] = $rate3."%";
            }
            else
            {   
                $supplierCountData['tbflag'] = "down";
                $supplierCountData['hbrate'] = abs($rate3)."%";
            }
        }

        //收益汇总
        $moneyInfo = array();
        //近7天
        $date_week = array();
        for ($i = 6; $i >=0; $i--)
        {
            $date_week[] = date('Y-m-d', strtotime("$today -$i days"));
        }
        foreach ($date_week as $key => $value)
        {
            $moneyInfo[0][0][$key] = $value;
            $sql10 = "SELECT ifnull( sum( a.s_charge ), 0 ) as total FROM ( SELECT DISTINCT w.id,s_charge FROM lkt_withdraw w
                                                                    LEFT JOIN lkt_record r ON w.store_id = r.store_id AND w.user_id = r.user_id WHERE w.store_id ='$store_id' 
                                                                    AND DATE_FORMAT( r.add_date, '%Y-%m-%d' )='$value'
                                                                    AND DATE_FORMAT( w.examine_date, '%Y-%m-%d' ) = '$value' 
                                                                    AND w.STATUS = 1 
                                                                    AND r.type = 21 
                                                                    ) AS a";
            $res10 = Db::query($sql10);
            $moneyInfo[0][1][$key] = $res10 && $res10[0]['total'] ? (float)$res10[0]['total'] : 0;
        }

        //近一个月
        $date_month = array();
        for ($i = 30; $i >= 0; $i--)
        {
            $date_month[] = date('Y-m-d', strtotime("$today -$i days")); //每隔一天赋值给数组
        }
        foreach ($date_month as $key => $value)
        {
            $moneyInfo[1][0][$key] = $value;
            $sql11 = "SELECT ifnull( sum( a.s_charge ), 0 ) as total FROM ( SELECT DISTINCT w.id,s_charge FROM lkt_withdraw w
                                                                    LEFT JOIN lkt_record r ON w.store_id = r.store_id AND w.user_id = r.user_id WHERE w.store_id ='$store_id' 
                                                                    AND DATE_FORMAT( r.add_date, '%Y-%m-%d' )='$value'
                                                                    AND DATE_FORMAT( w.examine_date, '%Y-%m-%d' ) = '$value' 
                                                                    AND w.STATUS = 1 
                                                                    AND r.type = 21 
                                                                    ) AS a";
            $res11 = Db::query($sql11);
            $moneyInfo[1][1][$key] = $res11 && $res11[0]['total'] ? (float)$res11[0]['total'] : 0;
        }

        //近一年
        $date_year = array();
        for ($i = 12; $i >= 0; $i--)
        {
            $date_year[] = date('Y-m', strtotime("$mon -$i month")); //每隔一天赋值给数组
        }
        foreach ($date_year as $key => $value)
        {
            $moneyInfo[2][0][$key] = $value;
            $sql12 = "SELECT ifnull( sum( a.s_charge ), 0 ) as total FROM ( SELECT DISTINCT w.id,s_charge FROM lkt_withdraw w
                                                                    LEFT JOIN lkt_record r ON w.store_id = r.store_id AND w.user_id = r.user_id WHERE w.store_id ='$store_id' 
                                                                    AND DATE_FORMAT( r.add_date, '%Y-%m' )='$value'
                                                                    AND DATE_FORMAT( w.examine_date, '%Y-%m' ) = '$value' 
                                                                    AND w.STATUS = 1 
                                                                    AND r.type = 21 
                                                                    ) AS a";
            $res12 = Db::query($sql12);
            $moneyInfo[2][1][$key] = $res12 && $res12[0]['total'] ? (float)$res12[0]['total'] : 0;
        }

        //商品排行
        $topGoodsData = array();
        $topGoodsData['total'] = 0;//总销售数量
        $sql13 = "SELECT sum( volume + real_volume ) as total FROM lkt_product_list WHERE store_id ='$store_id' AND recycle = 0";
        $res13 = Db::query($sql13);
        if($res13)
        {
            $topGoodsData['total'] = $res13[0]['total'];
        }
        $topGoodsData['topGoods'] = array();//top10
        $sql14 = "SELECT product_title name,(volume + real_volume) value FROM lkt_product_list WHERE store_id = '$store_id' AND recycle = 0 ORDER BY (volume + real_volume) DESC LIMIT 0,10";
        $res14 = Db::query($sql14);
        if($res14)
        {
            $topGoodsData['topGoods'] = $res14;
        }

        //店铺成交额
        $chargeMoneyData = array();
        //全部
        $chargeMoneyData['allData'][0] = array();
        $chargeMoneyData['allData'][1] = array();
        $chargeMoneyData['allDataList'] = array();
        $sql15 = "SELECT ifnull( sum( a.z_price ), 0 ) amount,m.name FROM lkt_order a
                    LEFT JOIN lkt_order_details d ON a.sNo = d.r_sNo
                    LEFT JOIN lkt_product_list b ON d.p_id = b.id
                    INNER JOIN lkt_mch m ON b.mch_id = m.id WHERE a.store_id ='$store_id' AND a.settlement_status = 1 GROUP BY b.mch_id ORDER BY amount DESC LIMIT 0,10";
        $res15 = Db::query($sql15);
        if($res15)
        {   
            $chargeMoneyData['allDataList'] = $res15;
            foreach ($res15 as $key => $value) 
            {
                $chargeMoneyData['allData'][0][$key] = $value['name'];
                $chargeMoneyData['allData'][1][$key] = $value['amount'];
            }
        }
        //近七天
        $star_time = date('Y-m-d', strtotime("$today -6 days"));
        $chargeMoneyData['weekData'][0] = array();
        $chargeMoneyData['weekData'][1] = array();
        $chargeMoneyData['weekDataList'] = array();
        $sql16 = "SELECT ifnull( sum( a.z_price ), 0 ) amount,m.name FROM lkt_order a
                    LEFT JOIN lkt_order_details d ON a.sNo = d.r_sNo
                    LEFT JOIN lkt_product_list b ON d.p_id = b.id
                    INNER JOIN lkt_mch m ON b.mch_id = m.id WHERE a.store_id ='$store_id' and a.add_time >= '$star_time' AND a.settlement_status = 1 GROUP BY b.mch_id ORDER BY amount DESC LIMIT 0,10";
        $res16 = Db::query($sql16);
        if($res16)
        {
            $chargeMoneyData['weekDataList'] = $res16;
            foreach ($res16 as $key => $value) 
            {
                $chargeMoneyData['weekData'][0][$key] = $value['name'];
                $chargeMoneyData['weekData'][1][$key] = $value['amount'];
            }
        }
        //近一月
        $star_time = date('Y-m-d', strtotime("$today -1 month"));
        $chargeMoneyData['monthData'][0] = array();
        $chargeMoneyData['monthData'][1] = array();
        $chargeMoneyData['monthDataList'] = array();
        $sql17 = "SELECT ifnull( sum( a.z_price ), 0 ) amount,m.name FROM lkt_order a
                    LEFT JOIN lkt_order_details d ON a.sNo = d.r_sNo
                    LEFT JOIN lkt_product_list b ON d.p_id = b.id
                    INNER JOIN lkt_mch m ON b.mch_id = m.id WHERE a.store_id ='$store_id' and a.add_time >= '$star_time' AND a.settlement_status = 1 GROUP BY b.mch_id ORDER BY amount DESC LIMIT 0,10";
        $res17 = Db::query($sql17);
        if($res17)
        {
            $chargeMoneyData['monthDataList'] = $res17;
            foreach ($res17 as $key => $value) 
            {
                $chargeMoneyData['monthData'][0][$key] = $value['name'];
                $chargeMoneyData['monthData'][1][$key] = $value['amount'];
            }
        }
        //近一年
        $star_time = date('Y-m-d', strtotime("$today -1 year"));
        $chargeMoneyData['yearData'][0] = array();
        $chargeMoneyData['yearData'][1] = array();
        $chargeMoneyData['yearDataList'] = array();
        $sql18 = "SELECT ifnull( sum( a.z_price ), 0 ) amount,m.name FROM lkt_order a
                    LEFT JOIN lkt_order_details d ON a.sNo = d.r_sNo
                    LEFT JOIN lkt_product_list b ON d.p_id = b.id
                    INNER JOIN lkt_mch m ON b.mch_id = m.id WHERE a.store_id ='$store_id' and a.add_time >= '$star_time' AND a.settlement_status = 1 GROUP BY b.mch_id ORDER BY amount DESC LIMIT 0,10";
        $res18 = Db::query($sql18);
        if($res18)
        {
            $chargeMoneyData['yearDataList'] = $res18;
            foreach ($res18 as $key => $value) 
            {
                $chargeMoneyData['yearData'][0][$key] = $value['name'];
                $chargeMoneyData['yearData'][1][$key] = $value['amount'];
            }
        }

        //供应商排行
        $supplierData = array();
        //全部
        $supplierData['allSupplierData'][0] = array();
        $supplierData['allSupplierData'][1] = array();
        $supplierData['allSupplierDataList'] = array();
        $sql19 = "SELECT ifnull( sum( o.z_price ), 0 ) num,c.supplier_name FROM lkt_order o
                    LEFT JOIN lkt_order_details a ON o.sNo = a.r_sNo
                    LEFT JOIN lkt_product_list b ON a.p_id = b.id
                    LEFT JOIN lkt_supplier c ON b.gongyingshang = c.id WHERE o.STATUS = 5 
                    AND gongyingshang != 0 
                    AND c.supplier_name IS NOT NULL 
                    AND a.store_id ='$store_id' GROUP BY gongyingshang ORDER BY num DESC LIMIT 0,10";
        $res19 = Db::query($sql19);
        if($res19)
        {   
            $supplierData['allSupplierDataList'] = $res19;
            foreach ($res19 as $key => $value) 
            {
                $supplierData['allSupplierData'][0][$key] = $value['supplier_name'];
                $supplierData['allSupplierData'][1][$key] = $value['num'];
            }
        }
        //近七天
        $star_time = date('Y-m-d', strtotime("$today -6 days"));
        $supplierData['weekSupplierData'][0] = array();
        $supplierData['weekSupplierData'][1] = array();
        $supplierData['weekSupplierDataList'] = array();
        $sql20 = "SELECT ifnull( sum( o.z_price ), 0 ) num,c.supplier_name FROM lkt_order o
                    LEFT JOIN lkt_order_details a ON o.sNo = a.r_sNo
                    LEFT JOIN lkt_product_list b ON a.p_id = b.id
                    LEFT JOIN lkt_supplier c ON b.gongyingshang = c.id WHERE o.STATUS = 5 
                    AND gongyingshang != 0 
                    AND a.add_time >= '$star_time'
                    AND c.supplier_name IS NOT NULL 
                    AND a.store_id ='$store_id' GROUP BY gongyingshang ORDER BY num DESC LIMIT 0,10";
        $res20 = Db::query($sql20);
        if($res20)
        {   
            $supplierData['weekSupplierDataList'] = $res20;
            foreach ($res20 as $key => $value) 
            {
                $supplierData['weekSupplierData'][0][$key] = $value['supplier_name'];
                $supplierData['weekSupplierData'][1][$key] = $value['num'];
            }
        }
        //近一月
        $star_time = date('Y-m-d', strtotime("$today -1 month"));
        $supplierData['monthSupplierData'][0] = array();
        $supplierData['monthSupplierData'][1] = array();
        $supplierData['monthSupplierDataList'] = array();
        $sql21 = "SELECT ifnull( sum( o.z_price ), 0 ) num,c.supplier_name FROM lkt_order o
                    LEFT JOIN lkt_order_details a ON o.sNo = a.r_sNo
                    LEFT JOIN lkt_product_list b ON a.p_id = b.id
                    LEFT JOIN lkt_supplier c ON b.gongyingshang = c.id WHERE o.STATUS = 5 
                    AND gongyingshang != 0 
                    AND a.add_time >= '$star_time'
                    AND c.supplier_name IS NOT NULL 
                    AND a.store_id ='$store_id' GROUP BY gongyingshang ORDER BY num DESC LIMIT 0,10";
        $res21 = Db::query($sql21);
        if($res21)
        {   
            $supplierData['monthSupplierDataList'] = $res21;
            foreach ($res21 as $key => $value) 
            {
                $supplierData['monthSupplierData'][0][$key] = $value['supplier_name'];
                $supplierData['monthSupplierData'][1][$key] = $value['num'];
            }
        }
        //近一年
        $star_time = date('Y-m-d', strtotime("$today -1 year"));
        $supplierData['yearSupplierData'][0] = array();
        $supplierData['yearSupplierData'][1] = array();
        $supplierData['yearSupplierDataList'] = array();
        $sql22 = "SELECT ifnull( sum( o.z_price ), 0 ) num,c.supplier_name FROM lkt_order o
                    LEFT JOIN lkt_order_details a ON o.sNo = a.r_sNo
                    LEFT JOIN lkt_product_list b ON a.p_id = b.id
                    LEFT JOIN lkt_supplier c ON b.gongyingshang = c.id WHERE o.STATUS = 5 
                    AND gongyingshang != 0 
                    AND a.add_time >= '$star_time'
                    AND c.supplier_name IS NOT NULL 
                    AND a.store_id ='$store_id' GROUP BY gongyingshang ORDER BY num DESC LIMIT 0,10";
        $res22 = Db::query($sql22);
        if($res22)
        {   
            $supplierData['yearSupplierDataList'] = $res22;
            foreach ($res22 as $key => $value) 
            {
                $supplierData['yearSupplierData'][0][$key] = $value['supplier_name'];
                $supplierData['yearSupplierData'][1][$key] = $value['num'];
            }
        }
        $reportData = array('chargeMoneyData'=>$chargeMoneyData,'goodsData'=>$goodsData,'mchData'=>$mchData,'moneyInfo'=>$moneyInfo,'supplierCountData'=>$supplierCountData,'supplierData'=>$supplierData,'topGoodsData'=>$topGoodsData,'volumeData'=>$volumeData);
        $message = Lang("Success");
        return output(200,$message,array('reportData'=>$reportData));
    }

    //用户报表
    public function userIndex()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));
        //状态查询
        $mon = date("Y-m");//当前月份
        //得到系统的年月
        $tmp_date = date("Ym");
        //切割出年份
        $tmp_year = substr($tmp_date, 0, 4);
        //切割出月份
        $tmp_mon = substr($tmp_date, 4, 2);
        $tmp_forwardmonth = mktime(0, 0, 0, $tmp_mon - 1, 1, $tmp_year);
        //得到当前月的上一个月
        $lastmon = date("Y-m", $tmp_forwardmonth);
        //今天
        $today = date("Y-m-d");
        $day_1 = date("Y-m-d", strtotime("-1 day"));//昨天

        //24小时模块
        $hour = array();
        for ($i=0; $i < 24; $i++) 
        { 
            if($i < 10)
            {
                $hour[0][$i] = "0".$i.":00";
            }
            else
            {
                $hour[0][$i] = $i.":00";
            }
            $hour[1][$i] = 0;
            $hour[2][$i] = 0;
        }
        //活跃用户模块
        $activeuserData = array();
        //今日
        $activeuserData['today'][0]=array();
        $activeuserData['today'][1]=array();
        
        $sql0 = "SELECT tt.* FROM(SELECT DATE_FORMAT(a.TIME, '%H:00') AS HOUR,COUNT(a.TIME) over (PARTITION BY DATE_FORMAT(a.TIME, '%Y-%m-%d %H')) AS num,row_number () over (PARTITION BY DATE_FORMAT(a.TIME, '%Y-%m-%d %H')) AS top FROM (SELECT DATE_FORMAT(CONCAT(DATE(last_time),' ',IF((FLOOR(MINUTE (last_time) / 30)) >= 1,HOUR (last_time) + 1,HOUR (last_time))),'%Y-%m-%d %H') AS TIME FROM lkt_user WHERE DATE_FORMAT(last_time, '%Y-%m-%d') = '$today' AND store_id = '$store_id') a ) AS tt WHERE tt.top<2 ORDER BY  tt.HOUR";
        $res0 = Db::query($sql0);
        if($res0)
        {   
            foreach ($hour[0] as $key => $value) 
            {   
                foreach ($res0 as $k => $v) 
                {
                    if($value == $v['HOUR']) 
                    {
                        $hour[1][$key] = $v['num'];
                    }
                }
            }
            $activeuserData['today'][0]=$hour[0];
            $activeuserData['today'][1]=$hour[1];
        }
        //昨日
        $activeuserData['yesterday'][0]=array();
        $activeuserData['yesterday'][1]=array();
        $sql1 = "SELECT tt.* FROM(SELECT DATE_FORMAT(a.TIME, '%H:00') AS HOUR,COUNT(a.TIME) over (PARTITION BY DATE_FORMAT(a.TIME, '%Y-%m-%d %H')) AS num,row_number () over (PARTITION BY DATE_FORMAT(a.TIME, '%Y-%m-%d %H')) AS top FROM (SELECT DATE_FORMAT(CONCAT(DATE(last_time),' ',IF((FLOOR(MINUTE (last_time) / 30)) >= 1,HOUR (last_time) + 1,HOUR (last_time))),'%Y-%m-%d %H') AS TIME FROM lkt_user WHERE DATE_FORMAT(last_time, '%Y-%m-%d') = '$day_1' AND store_id = '$store_id') a ) AS tt WHERE tt.top<2 ORDER BY  tt.HOUR";
        $res1 = Db::query($sql1);
        if($res1)
        {   
            foreach ($hour[0] as $key => $value) 
            {   
                foreach ($res1 as $k => $v) 
                {
                    if($value == $v['HOUR']) 
                    {
                        $hour[2][$key] = $v['num'];
                    }
                }
            }
            $activeuserData['yesterday'][0]=$hour[0];
            $activeuserData['yesterday'][1]=$hour[2];
        }
        //近七天
        $week = date('Y-m-d', strtotime("$today -6 days"));
        $activeuserData['thisweek'][0]=array();
        $activeuserData['thisweek'][1]=array();
        $sql2 = "SELECT COUNT( 1 ) num,DATE_FORMAT( last_time, '%y-%m-%d' ) AS DAY FROM lkt_user WHERE DATE_FORMAT( last_time, '%Y-%m-%d' ) >= '$week'AND store_id = '$store_id' GROUP BY DAY";
        $res2 = Db::query($sql2);
        if($res2)
        {
            foreach ($res2 as $key => $value) 
            {
                $activeuserData['thisweek'][0][$key]= $value['DAY'];
                $activeuserData['thisweek'][1][$key]= $value['num'];
            }
        }
        //近一月
        $month = date('Y-m-d', strtotime("$today -29 days"));
        $activeuserData['thismonth'][0]=array();
        $activeuserData['thismonth'][1]=array();
        $sql3 = "SELECT COUNT( 1 ) num,DATE_FORMAT( last_time, '%y-%m-%d' ) AS DAY FROM lkt_user WHERE DATE_FORMAT( last_time, '%Y-%m-%d' ) >= '$month'AND store_id = '$store_id' GROUP BY DAY order by DAY asc";
        $res3 = Db::query($sql3);
        if($res3)
        {
            foreach ($res3 as $key => $value) 
            {
                $activeuserData['thismonth'][0][$key]= $value['DAY'];
                $activeuserData['thismonth'][1][$key]= $value['num'];
            }
        }

        //用户统计
        $userCount = array();
        $sql4 = "SELECT count( 1 ) value,CASE source WHEN 1 THEN '小程序端' WHEN 2 THEN 'H5端' WHEN 3 THEN '支付宝小程序' WHEN 4 THEN '头条小程序' WHEN 5 THEN '百度小程序' WHEN 6 THEN 'PC端' WHEN 11 THEN 'APP端' ELSE '' END AS name FROM lkt_user WHERE source IN (1,2,3,4,5,6,11) AND store_id ='$store_id' GROUP BY source order by source asc";
        $res4 = Db::query($sql4);
        if($res4)
        {
            $userCount = $res4;
        }

        //新增用户
        $additiondata = array();
        $sql5 = "select data from lkt_order_report where store_id = '$store_id' and type = '5'";
        $res5 = Db::query($sql5);
        if($res5)
        {
            $additiondata = json_decode($res5[0]['data']);
        }

        //会员统计
        $Membership = array();
        //近7天
        $date_week = array();
        for ($i = 6; $i >=0; $i--)
        {
            $date_week[] = date('Y-m-d', strtotime("$today -$i days"));
        }
        foreach ($date_week as $key => $value) 
        {   

            $Membership['week'][0][$key] = $value;
            $Membership['week'][1][$key] = 0;
            $Membership['week'][2][$key] = 0;
            $Membership['week'][3][$key] = 0;
            //开通
            $sql6 = "SELECT COUNT( 1 ) num FROM lkt_user WHERE grade_end IS NOT NULL AND store_id = '$store_id' AND grade = 1 AND is_out = 0 AND DATE_FORMAT( grade_add, '%Y-%m-%d' ) = '$value' ";
            $res6 = Db::query($sql6);
            if($res6)
            {
                $Membership['week'][1][$key] = $res6[0]['num'];
            }
            //过期
            $sql7 = "SELECT COUNT( 1 ) num FROM lkt_user WHERE grade_end IS NOT NULL AND store_id = '$store_id' AND DATE_FORMAT( grade_end, '%Y-%m-%d' ) = '$value'";
            $res7 = Db::query($sql7);
            if($res7)
            {
               $Membership['week'][2][$key] = $res7[0]['num'];
            }
            //续费
            $user_arr = array();
            $sql8 = "SELECT data FROM lkt_order_data  WHERE order_type = 'DJ' AND DATE_FORMAT( addtime, '%Y-%m-%d' ) = '$value' AND STATUS = '$store_id'";
            $res8 = Db::query($sql8);
            if($res8)
            {
                foreach ($res8 as $k => $v) 
                {
                    $data = json_decode($v['data'],true);
                    if($data['storeId']== $store_id && !in_array($data['userId'],$user_arr))
                    {
                        array_push($user_arr, $data['userId']);
                    }
                }
                $Membership['week'][3][$key] = count($user_arr);
            }
        }
        //近月
        $date_month = array();
        for ($i = 29; $i >=0; $i--)
        {
            $date_month[] = date('Y-m-d', strtotime("$today -$i days"));
        }
        foreach ($date_month as $key => $value) 
        {
            $Membership['month'][0][$key] = $value;
            $Membership['month'][1][$key] = 0;
            $Membership['month'][2][$key] = 0;
            $Membership['month'][3][$key] = 0;
            //开通
            $sql9 = "SELECT COUNT( 1 ) num FROM lkt_user WHERE grade_end IS NOT NULL AND store_id = '$store_id' AND grade = 1 AND is_out = 0 AND DATE_FORMAT( grade_add, '%Y-%m-%d' ) = '$value' ";
            $res9 = Db::query($sql9);
            if($res9)
            {
                $Membership['month'][1][$key] = $res9[0]['num'];
            }
            //过期
            $sql10 = "SELECT COUNT( 1 ) num FROM lkt_user WHERE grade_end IS NOT NULL AND store_id = '$store_id' AND DATE_FORMAT( grade_end, '%Y-%m-%d' ) = '$value'";
            $res10 = Db::query($sql10);
            if($res10)
            {
               $Membership['month'][2][$key] = $res10[0]['num'];
            }
            //续费
            $user_arr = array();
            $sql11 = "SELECT data FROM lkt_order_data  WHERE order_type = 'DJ' AND DATE_FORMAT( addtime, '%Y-%m-%d' ) = '$value' AND STATUS = '$store_id'";
            $res11 = Db::query($sql11);
            if($res11)
            {
                foreach ($res11 as $k => $v) 
                {
                    $data = json_decode($v['data'],true);
                    if($data['storeId']== $store_id && !in_array($data['userId'],$user_arr))
                    {
                        array_push($user_arr, $data['userId']);
                    }
                }
                $Membership['month'][3][$key] = count($user_arr);
            }
        }
        //近一年
        $date_year = array();
        for ($i = 12; $i >= 0; $i--)
        {
            $date_year[] = date('Y-m', strtotime("$mon -$i month")); //每隔一天赋值给数组
        }
        foreach ($date_year as $key => $value) 
        {
            $Membership['year'][0][$key] = $value;
            $Membership['year'][1][$key] = 0;
            $Membership['year'][2][$key] = 0;
            $Membership['year'][3][$key] = 0;
            //开通
            $sql12 = "SELECT COUNT( 1 ) num FROM lkt_user WHERE grade_end IS NOT NULL AND store_id = '$store_id' AND grade = 1 AND is_out = 0 AND DATE_FORMAT( grade_add, '%Y-%m-%d' ) = '$value' ";
            $res12 = Db::query($sql12);
            if($res12)
            {
                $Membership['year'][1][$key] = $res12[0]['num'];
            }
            //过期
            $sql13 = "SELECT COUNT( 1 ) num FROM lkt_user WHERE grade_end IS NOT NULL AND store_id = '$store_id' AND DATE_FORMAT( grade_end, '%Y-%m' ) = '$value'";
            $res13 = Db::query($sql13);
            if($res13)
            {
               $Membership['year'][2][$key] = $res13[0]['num'];
            }
            //续费
            $user_arr = array();
            $sql14 = "SELECT data FROM lkt_order_data  WHERE order_type = 'DJ' AND DATE_FORMAT( addtime, '%Y-%m' ) = '$value' AND STATUS = '$store_id'";
            $res14 = Db::query($sql14);
            if($res14)
            {
                foreach ($res14 as $k => $v) 
                {
                    $data = json_decode($v['data'],true);
                    if($data['storeId']== $store_id && !in_array($data['userId'],$user_arr))
                    {
                        array_push($user_arr, $data['userId']);
                    }
                }
                $Membership['year'][3][$key] = count($user_arr);
            }
        }

        //用户消费排行
        $moneyTop = array();
        //全部
        $moneyTop['all'] = array();
        $moneyTop['alldata'] = array();
        $sql15 = "SELECT * FROM (SELECT SUM( a.z_price ) over (PARTITION BY a.user_id ) amount,b.user_name name,row_number () over (PARTITION BY a.user_id ) AS top FROM lkt_order a left join lkt_user b on a.user_id = b.user_id WHERE a.store_id ='$store_id' AND STATUS = 5 AND recycle = 0 ) AS tt WHERE top < 2 ORDER BY amount DESC LIMIT 0,10";
        $res15 = Db::query($sql15);
        if($res15)
        {
            foreach ($res15 as $key => $value) 
            {
                $moneyTop['all'][0][$key] = $value['name'];
                $moneyTop['all'][1][$key] = $value['amount'];
                $moneyTop['alldata'][$key]['amount'] = $value['amount'];
                $moneyTop['alldata'][$key]['name'] = $value['name'];
            }
        }
        //近一周
        $moneyTop['week'] = array();
        $moneyTop['weekdata'] = array();
        $sql16 = "SELECT * FROM (SELECT SUM( a.z_price ) over (PARTITION BY a.user_id ) amount,b.user_name name,row_number () over (PARTITION BY a.user_id ) AS top FROM lkt_order a left join lkt_user b on a.user_id = b.user_id WHERE a.store_id ='$store_id' and a.add_time >= '$week' AND STATUS = 5 AND recycle = 0 ) AS tt WHERE top < 2 ORDER BY amount DESC LIMIT 0,10";
        $res16 = Db::query($sql16);
        if($res16)
        {
            foreach ($res16 as $key => $value) 
            {
                $moneyTop['week'][0][$key] = $value['name'];
                $moneyTop['week'][1][$key] = $value['amount'];
                $moneyTop['weekdata'][$key]['amount'] = $value['amount'];
                $moneyTop['weekdata'][$key]['name'] = $value['name'];
            }
        }
        //近一月
        $moneyTop['month'] = array();
        $moneyTop['monthdata'] = array();
        $sql17 = "SELECT * FROM (SELECT SUM( a.z_price ) over (PARTITION BY a.user_id ) amount,b.user_name name,row_number () over (PARTITION BY a.user_id ) AS top FROM lkt_order a left join lkt_user b on a.user_id = b.user_id WHERE a.store_id ='$store_id' and a.add_time >= '$month' AND STATUS = 5 AND recycle = 0 ) AS tt WHERE top < 2 ORDER BY amount DESC LIMIT 0,10";
        $res17 = Db::query($sql17);
        if($res17)
        {
            foreach ($res17 as $key => $value) 
            {
                $moneyTop['month'][0][$key] = $value['name'];
                $moneyTop['month'][1][$key] = $value['amount'];
                $moneyTop['monthdata'][$key]['amount'] = $value['amount'];
                $moneyTop['monthdata'][$key]['name'] = $value['name'];
            }
        }
        //近一年
        $year = date('Y-m-d', strtotime("$today -364 days"));
        $moneyTop['year'] = array();
        $moneyTop['yeardata'] = array();
        $sql18 = "SELECT * FROM (SELECT SUM( a.z_price ) over (PARTITION BY a.user_id ) amount,b.user_name name,row_number () over (PARTITION BY a.user_id ) AS top FROM lkt_order a left join lkt_user b on a.user_id = b.user_id WHERE a.store_id ='$store_id' and a.add_time >= '$year' AND STATUS = 5 AND recycle = 0 ) AS tt WHERE top < 2 ORDER BY amount DESC LIMIT 0,10";
        $res18 = Db::query($sql18);
        if($res18)
        {
            foreach ($res18 as $key => $value) 
            {
                $moneyTop['year'][0][$key] = $value['name'];
                $moneyTop['year'][1][$key] = $value['amount'];
                $moneyTop['yeardata'][$key]['amount'] = $value['amount'];
                $moneyTop['yeardata'][$key]['name'] = $value['name'];
            }
        }
        $data = array('Membership'=>$Membership,'activeuserData'=>$activeuserData,'additiondata'=>$additiondata,'moneyTop'=>$moneyTop,'userCount'=>$userCount);
        $message = Lang("Success");
        return output(200,$message,$data);
    }

    //订单报表
    public function orderIndex()
    {
            $store_id = addslashes(trim($this->request->param('storeId')));
            $store_type = addslashes(trim($this->request->param('storeType')));
            $access_id = addslashes(trim($this->request->param('accessId')));
            //状态查询
            $mon = date("Y-m");//当前月份
            //得到系统的年月
            $tmp_date = date("Ym");
            //切割出年份
            $tmp_year = substr($tmp_date, 0, 4);
            //切割出月份
            $tmp_mon = substr($tmp_date, 4, 2);
            $tmp_forwardmonth = mktime(0, 0, 0, $tmp_mon - 1, 1, $tmp_year);
            //得到当前月的上一个月
            $lastmon = date("Y-m", $tmp_forwardmonth);
            //今天
            $today = date("Y-m-d");
            //昨天
            $day_1 = date("Y-m-d", strtotime("-1 day"));
            // 获取当前日期是星期几
            $dayOfWeek = date('N', strtotime($today));
            // 计算距离本周一的天数差
            $daysToMonday = $dayOfWeek - 1;
            // 获取本周星期一的日期
            $monday = date('Y-m-d', strtotime("-$daysToMonday days", strtotime($today)));
            //获取上周一日期
            $lastWeekMonday = date('Y-m-d', strtotime("-7 days", strtotime($monday)));
            //获取本周是当月第几周
            $first_day_of_month = date('Y-m-01');
            $first_day_of_month_week = date('W', strtotime($first_day_of_month));
            $w = date('W') - $first_day_of_month_week + 1;
            //订单状态-订单数量
            $orderStatus = array();
            $orderStatus[0][0]['num'] = 0;
            $orderStatus[0][0]['hbflag'] = "up";
            $orderStatus[0][0]['hbrate'] = "100%";//日环比 昨天对比
            $orderStatus[0][0]['tbflag'] = "up";
            $orderStatus[0][0]['tbrate'] = "100%";//日同比 上周今日对比
            //今日待付款
            $sql0 = "SELECT count( 1 ) num FROM lkt_order a WHERE DATE_FORMAT( add_time, '%Y-%m-%d' ) >= '$today' AND DATE_FORMAT( add_time, '%Y-%m-%d' ) <= '$today' AND store_id ='$store_id' AND recycle = 0 AND otype = 'GM' AND STATUS =0";
            $res0 = Db::query($sql0);
            if($res0)
            {
                $orderStatus[0][0]['num'] = $res0[0]['num'];
            }
            //昨日待付款
            $yesterday_dd = 0;
            $sql_0 = "SELECT count( 1 ) num FROM lkt_order a WHERE DATE_FORMAT( add_time, '%Y-%m-%d' ) >= '$day_1' AND DATE_FORMAT( add_time, '%Y-%m-%d' ) <= '$day_1' AND store_id ='$store_id' AND recycle = 0 AND otype = 'GM' AND STATUS =0";
            $res_0 = Db::query($sql_0);
            if($res_0)
            {
                $yesterday_dd = $res_0[0]['num'];
            }
            //上周今日待付款
            $week_d = date("Y-m-d", strtotime("-7 day"));
            $week_dd = 0;
            $sql_1 = "SELECT count( 1 ) num FROM lkt_order a WHERE DATE_FORMAT( add_time, '%Y-%m-%d' ) >= '$week_d' AND DATE_FORMAT( add_time, '%Y-%m-%d' ) <= '$week_d' AND store_id ='$store_id' AND recycle = 0 AND otype = 'GM' AND STATUS =0";
            $res_1 = Db::query($sql_1);
            if($res_1)
            {
                $week_dd = $res_1[0]['num'];
            }
            if($yesterday_dd > 0)
            {
                $rete_d = round(($orderStatus[0][0]['num'] - $yesterday_dd)/$yesterday_dd * 100,2);
                if($rete_d > 0)
                {
                    $orderStatus[0][0]['hbrate'] = $rete_d."%";
                }
                else
                {
                    $orderStatus[0][0]['hbflag'] = "down";
                    $orderStatus[0][0]['hbrate'] = abs($rete_d)."%";
                }
            }
            if($week_dd > 0)
            {
                $rete_w = round(($orderStatus[0][0]['num'] - $week_dd)/$week_dd * 100,2);
                if($rete_w > 0)
                {
                    $orderStatus[0][0]['tbrate'] = $rete_w."%";
                }
                else
                {
                    $orderStatus[0][0]['tbflag'] = "down";
                    $orderStatus[0][0]['tbrate'] = abs($rete_w)."%";
                }
            }
            $orderStatus[0][1]['num'] = 0;
            $orderStatus[0][1]['hbflag'] = "up";
            $orderStatus[0][1]['hbrate'] = "100%";//周环比 上周对比
            $orderStatus[0][1]['tbflag'] = "up";
            $orderStatus[0][1]['tbrate'] = "100%";//周同比 上月这周对比
            //本周待付款
            $sql1 = "SELECT count( 1 ) num FROM lkt_order a WHERE DATE_FORMAT( add_time, '%Y-%m-%d' ) >= '$monday'  AND store_id ='$store_id' AND recycle = 0 AND otype = 'GM' AND STATUS =0";
            $res1 = Db::query($sql1);
            if($res1)
            {
                $orderStatus[0][1]['num'] = $res1[0]['num'];
            }
            //上周待付款
            $yesterday_wd = 0;
            $sql_2 = "SELECT count( 1 ) num FROM lkt_order a WHERE DATE_FORMAT( add_time, '%Y-%m-%d' ) >= '$lastWeekMonday' and DATE_FORMAT( add_time, '%Y-%m-%d' ) < '$monday'  AND store_id ='$store_id' AND recycle = 0 AND otype = 'GM' AND STATUS =0 ";
            $res_2 = Db::query($sql_2);
            if($res_2)
            {
                $yesterday_wd = $res_2[0]['num'];
            }
            //上月本周待付款
            $monthPerWeeks = $this->monthPerWeek(date("Y-m-d", strtotime("-1 month")),$w);//上月本周时间段
            $week_wd = 0;
            $sql_3 = "SELECT count( 1 ) num FROM lkt_order a WHERE DATE_FORMAT( add_time, '%Y-%m-%d' ) >= '".$monthPerWeeks[0]."' and DATE_FORMAT( add_time, '%Y-%m-%d' ) <= '".$monthPerWeeks[1]."'  AND store_id ='$store_id' AND recycle = 0 AND otype = 'GM' AND STATUS =0";
            $res_3 = Db::query($sql_3);
            if($res_3)
            {
                $week_wd = $res_3[0]['num'];
            }
            if($yesterday_wd > 0)
            {
                $rete_wd = round(($orderStatus[0][1]['num'] - $yesterday_wd)/$yesterday_wd * 100,2);
                if($rete_wd > 0)
                {
                    $orderStatus[0][1]['hbrate'] = $rete_wd."%";
                }
                else
                {
                    $orderStatus[0][1]['hbflag'] = "down";
                    $orderStatus[0][1]['hbrate'] = abs($rete_wd)."%";
                }
            }
            if($week_wd > 0)
            {
                $rete_md = round(($orderStatus[0][1]['num'] - $week_wd)/$week_wd * 100,2);
                if($rete_md > 0)
                {
                    $orderStatus[0][1]['tbrate'] = $rete_md."%";
                }
                else
                {
                    $orderStatus[0][1]['tbflag'] = "down";
                    $orderStatus[0][1]['tbrate'] = abs($rete_md)."%";
                }
            }

            //待发货
            $orderStatus[1][0]['num'] = 0;
            $orderStatus[1][0]['hbflag'] = "up";
            $orderStatus[1][0]['hbrate'] = "100%";//日环比 昨天对比
            $orderStatus[1][0]['tbflag'] = "up";
            $orderStatus[1][0]['tbrate'] = "100%";//日同比 上周今日对比
            //今日待发货
            $sql2 = "SELECT count( 1 ) num FROM lkt_order a WHERE DATE_FORMAT( add_time, '%Y-%m-%d' ) >= '$today' AND DATE_FORMAT( add_time, '%Y-%m-%d' ) <= '$today' AND store_id ='$store_id' AND recycle = 0 AND otype = 'GM' AND STATUS =1";
            $res2 = Db::query($sql2);
            if($res2)
            {
                $orderStatus[1][0]['num'] = $res2[0]['num'];
            }
            //昨日待发货
            $yesterday_df = 0;
            $sql_4 = "SELECT count( 1 ) num FROM lkt_order a WHERE DATE_FORMAT( add_time, '%Y-%m-%d' ) >= '$day_1' AND DATE_FORMAT( add_time, '%Y-%m-%d' ) <= '$day_1' AND store_id ='$store_id' AND recycle = 0 AND otype = 'GM' AND STATUS =1";
            $res_4 = Db::query($sql_4);
            if($res_4)
            {
                $yesterday_df = $res_4[0]['num'];
            }
            //上周今日待发货
            $week_df = 0;
            $sql_5 = "SELECT count( 1 ) num FROM lkt_order a WHERE DATE_FORMAT( add_time, '%Y-%m-%d' ) >= '$week_d' AND DATE_FORMAT( add_time, '%Y-%m-%d' ) <= '$week_d' AND store_id ='$store_id' AND recycle = 0 AND otype = 'GM' AND STATUS =1";
            $res_5 = Db::query($sql_5);
            if($res_5)
            {
                $week_df = $res_5[0]['num'];
            }
            if($yesterday_df > 0)
            {
                $rete_d = round(($orderStatus[1][0]['num'] - $yesterday_df)/$yesterday_df * 100,2);
                if($rete_d > 0)
                {
                    $orderStatus[1][0]['hbrate'] = $rete_d."%";
                }
                else
                {
                    $orderStatus[1][0]['hbflag'] = "down";
                    $orderStatus[1][0]['hbrate'] = abs($rete_d)."%";
                }
            }
            if($week_df > 0)
            {
                $rete_w = round(($orderStatus[1][0]['num'] - $week_df)/$week_df * 100,2);
                if($rete_w > 0)
                {
                    $orderStatus[1][0]['tbrate'] = $rete_w."%";
                }
                else
                {
                    $orderStatus[1][0]['tbflag'] = "down";
                    $orderStatus[1][0]['tbrate'] = abs($rete_w)."%";
                }
            }
            $orderStatus[1][1]['num'] = 0;
            $orderStatus[1][1]['hbflag'] = "up";
            $orderStatus[1][1]['hbrate'] = "100%";//周环比 上周对比
            $orderStatus[1][1]['tbflag'] = "up";
            $orderStatus[1][1]['tbrate'] = "100%";//周同比 上月这周对比
            //本周待发货
            $sql3 = "SELECT count( 1 ) num FROM lkt_order a WHERE DATE_FORMAT( add_time, '%Y-%m-%d' ) >= '$monday'  AND store_id ='$store_id' AND recycle = 0 AND otype = 'GM' AND STATUS =1";
            $res3 = Db::query($sql3);
            if($res3)
            {
                $orderStatus[1][1]['num'] = $res3[0]['num'];
            }
            //上周待发货
            $yesterday_wf = 0;
            $sql_6 = "SELECT count( 1 ) num FROM lkt_order a WHERE DATE_FORMAT( add_time, '%Y-%m-%d' ) >= '$lastWeekMonday' and DATE_FORMAT( add_time, '%Y-%m-%d' ) < '$monday'  AND store_id ='$store_id' AND recycle = 0 AND otype = 'GM' AND STATUS =1 ";
            $res_6 = Db::query($sql_6);
            if($res_6)
            {
                $yesterday_wf = $res_6[0]['num'];
            }
            //上月本周待发货
            $monthPerWeeks = $this->monthPerWeek(date("Y-m-d", strtotime("-1 month")),$w);//上月本周时间段
            $week_wf = 0;
            $sql_7 = "SELECT count( 1 ) num FROM lkt_order a WHERE DATE_FORMAT( add_time, '%Y-%m-%d' ) >= '".$monthPerWeeks[0]."' and DATE_FORMAT( add_time, '%Y-%m-%d' ) <= '".$monthPerWeeks[1]."'  AND store_id ='$store_id' AND recycle = 0 AND otype = 'GM' AND STATUS =1 ";
            $res_7 = Db::query($sql_7);
            if($res_7)
            {
                $week_wf = $res_7[0]['num'];
            }
            if($yesterday_wf > 0)
            {
                $rete_wd = round(($orderStatus[1][1]['num'] - $yesterday_wf)/$yesterday_wf * 100,2);
                if($rete_wd > 0)
                {
                    $orderStatus[1][1]['hbrate'] = $rete_wd."%";
                }
                else
                {
                    $orderStatus[1][1]['hbflag'] = "down";
                    $orderStatus[1][1]['hbrate'] = abs($rete_wd)."%";
                }
            }
            if($week_wf > 0)
            {
                $rete_md = round(($orderStatus[1][1]['num'] - $week_wf)/$week_wf * 100,2);
                if($rete_md > 0)
                {
                    $orderStatus[1][1]['tbrate'] = $rete_md."%";
                }
                else
                {
                    $orderStatus[1][1]['tbflag'] = "down";
                    $orderStatus[1][1]['tbrate'] = abs($rete_md)."%";
                }
            }

            //待收货
            $orderStatus[2][0]['num'] = 0;
            $orderStatus[2][0]['hbflag'] = "up";
            $orderStatus[2][0]['hbrate'] = "100%";//日环比 昨天对比
            $orderStatus[2][0]['tbflag'] = "up";
            $orderStatus[2][0]['tbrate'] = "100%";//日同比 上周今日对比
            //今日待收货
            $sql4 = "SELECT count( 1 ) num FROM lkt_order a WHERE DATE_FORMAT( add_time, '%Y-%m-%d' ) >= '$today' AND DATE_FORMAT( add_time, '%Y-%m-%d' ) <= '$today' AND store_id ='$store_id' AND recycle = 0 AND otype = 'GM' AND STATUS =2";
            $res4 = Db::query($sql4);
            if($res4)
            {
                $orderStatus[2][0]['num'] = $res4[0]['num'];
            }
            //昨日待收货
            $yesterday_ds = 0;
            $sql_8 = "SELECT count( 1 ) num FROM lkt_order a WHERE DATE_FORMAT( add_time, '%Y-%m-%d' ) >= '$day_1' AND DATE_FORMAT( add_time, '%Y-%m-%d' ) <= '$day_1' AND store_id ='$store_id' AND recycle = 0 AND otype = 'GM' AND STATUS =2";
            $res_8 = Db::query($sql_8);
            if($res_8)
            {
                $yesterday_ds = $res_8[0]['num'];
            }
            //上周今日待收货
            $week_ds = 0;
            $sql_9 = "SELECT count( 1 ) num FROM lkt_order a WHERE DATE_FORMAT( add_time, '%Y-%m-%d' ) >= '$week_d' AND DATE_FORMAT( add_time, '%Y-%m-%d' ) <= '$week_d' AND store_id ='$store_id' AND recycle = 0 AND otype = 'GM' AND STATUS =2";
            $res_9 = Db::query($sql_9);
            if($res_9)
            {
                $week_ds = $res_9[0]['num'];
            }
            if($yesterday_ds > 0)
            {
                $rete_d = round(($orderStatus[2][0]['num'] - $yesterday_ds)/$yesterday_ds * 100,2);
                if($rete_d > 0)
                {
                    $orderStatus[2][0]['hbrate'] = $rete_d."%";
                }
                else
                {
                    $orderStatus[2][0]['hbflag'] = "down";
                    $orderStatus[2][0]['hbrate'] = abs($rete_d)."%";
                }
            }
            if($week_ds > 0)
            {
                $rete_w = round(($orderStatus[2][0]['num'] - $week_ds)/$week_ds * 100,2);
                if($rete_w > 0)
                {
                    $orderStatus[2][0]['tbrate'] = $rete_w."%";
                }
                else
                {
                    $orderStatus[2][0]['tbflag'] = "down";
                    $orderStatus[2][0]['tbrate'] = abs($rete_w)."%";
                }
            }
            $orderStatus[2][1]['num'] = 0;
            $orderStatus[2][1]['hbflag'] = "up";
            $orderStatus[2][1]['hbrate'] = "100%";//周环比 上周对比
            $orderStatus[2][1]['tbflag'] = "up";
            $orderStatus[2][1]['tbrate'] = "100%";//周同比 上月这周对比
            //本周待收货
            $sql5 = "SELECT count( 1 ) num FROM lkt_order a WHERE DATE_FORMAT( add_time, '%Y-%m-%d' ) >= '$monday'  AND store_id ='$store_id' AND recycle = 0 AND otype = 'GM' AND STATUS =2";
            $res5 = Db::query($sql5);
            if($res5)
            {
                $orderStatus[2][1]['num'] = $res5[0]['num'];
            }
            //上周待收货
            $yesterday_ws = 0;
            $sql_10 = "SELECT count( 1 ) num FROM lkt_order a WHERE DATE_FORMAT( add_time, '%Y-%m-%d' ) >= '$lastWeekMonday' and DATE_FORMAT( add_time, '%Y-%m-%d' ) < '$monday'  AND store_id ='$store_id' AND recycle = 0 AND otype = 'GM' AND STATUS =2 ";
            $res_10 = Db::query($sql_10);
            if($res_10)
            {
                $yesterday_ws = $res_10[0]['num'];
            }
            //上月本周待收货
            $monthPerWeeks = $this->monthPerWeek(date("Y-m-d", strtotime("-1 month")),$w);//上月本周时间段
            $week_ws = 0;
            $sql_11 = "SELECT count( 1 ) num FROM lkt_order a WHERE DATE_FORMAT( add_time, '%Y-%m-%d' ) >= '".$monthPerWeeks[0]."' and DATE_FORMAT( add_time, '%Y-%m-%d' ) <= '".$monthPerWeeks[1]."'  AND store_id ='$store_id' AND recycle = 0 AND otype = 'GM' AND STATUS =2 ";
            $res_11 = Db::query($sql_11);
            if($res_11)
            {
                $week_ws = $res_11[0]['num'];
            }
            if($yesterday_ws > 0)
            {
                $rete_wd = round(($orderStatus[2][1]['num'] - $yesterday_ws)/$yesterday_ws * 100,2);
                if($rete_wd > 0)
                {
                    $orderStatus[2][1]['hbrate'] = $rete_wd."%";
                }
                else
                {
                    $orderStatus[2][1]['hbflag'] = "down";
                    $orderStatus[2][1]['hbrate'] = abs($rete_wd)."%";
                }
            }
            if($week_ws > 0)
            {
                $rete_md = round(($orderStatus[2][1]['num'] - $week_ws)/$week_ws * 100,2);
                if($rete_md > 0)
                {
                    $orderStatus[2][1]['tbrate'] = $rete_md."%";
                }
                else
                {
                    $orderStatus[2][1]['tbflag'] = "down";
                    $orderStatus[2][1]['tbrate'] = abs($rete_md)."%";
                }
            }

            //待评价
            $orderStatus[3][0]['num'] = 0;
            $orderStatus[3][0]['hbflag'] = "up";
            $orderStatus[3][0]['hbrate'] = "100%";//日环比 昨天对比
            $orderStatus[3][0]['tbflag'] = "up";
            $orderStatus[3][0]['tbrate'] = "100%";//日同比 上周今日对比
            //今日待评价
            $sql6 = "SELECT count( 1 ) num FROM lkt_order a WHERE DATE_FORMAT( add_time, '%Y-%m-%d' ) >= '$today' AND DATE_FORMAT( add_time, '%Y-%m-%d' ) <= '$today' AND store_id ='$store_id' AND recycle = 0 AND otype = 'GM' AND STATUS =5 AND NOT EXISTS ( SELECT * FROM lkt_comments c WHERE a.id = c.oid )";
            $res6 = Db::query($sql6);
            if($res6)
            {
                $orderStatus[3][0]['num'] = $res6[0]['num'];
            }
            //昨日待评价
            $yesterday_dp = 0;
            $sql_12 = "SELECT count( 1 ) num FROM lkt_order a WHERE DATE_FORMAT( add_time, '%Y-%m-%d' ) >= '$day_1' AND DATE_FORMAT( add_time, '%Y-%m-%d' ) <= '$day_1' AND store_id ='$store_id' AND recycle = 0 AND otype = 'GM' AND STATUS =5 AND NOT EXISTS ( SELECT * FROM lkt_comments c WHERE a.id = c.oid )";
            $res_12 = Db::query($sql_12);
            if($res_12)
            {
                $yesterday_dp = $res_12[0]['num'];
            }
            //上周今日待评价
            $week_dp = 0;
            $sql_13 = "SELECT count( 1 ) num FROM lkt_order a WHERE DATE_FORMAT( add_time, '%Y-%m-%d' ) >= '$week_d' AND DATE_FORMAT( add_time, '%Y-%m-%d' ) <= '$week_d' AND store_id ='$store_id' AND recycle = 0 AND otype = 'GM' AND STATUS =5 AND NOT EXISTS ( SELECT * FROM lkt_comments c WHERE a.id = c.oid )";
            $res_13 = Db::query($sql_13);
            if($res_13)
            {
                $week_dp = $res_13[0]['num'];
            }
            if($yesterday_dp > 0)
            {
                $rete_d = round(($orderStatus[3][0]['num'] - $yesterday_dp)/$yesterday_dp * 100,2);
                if($rete_d > 0)
                {
                    $orderStatus[3][0]['hbrate'] = $rete_d."%";
                }
                else
                {
                    $orderStatus[3][0]['hbflag'] = "down";
                    $orderStatus[3][0]['hbrate'] = abs($rete_d)."%";
                }
            }
            if($week_dp > 0)
            {
                $rete_w = round(($orderStatus[3][0]['num'] - $week_dp)/$week_dp * 100,2);
                if($rete_w > 0)
                {
                    $orderStatus[3][0]['tbrate'] = $rete_w."%";
                }
                else
                {
                    $orderStatus[3][0]['tbflag'] = "down";
                    $orderStatus[3][0]['tbrate'] = abs($rete_w)."%";
                }
            }
            $orderStatus[3][1]['num'] = 0;
            $orderStatus[3][1]['hbflag'] = "up";
            $orderStatus[3][1]['hbrate'] = "100%";//周环比 上周对比
            $orderStatus[3][1]['tbflag'] = "up";
            $orderStatus[3][1]['tbrate'] = "100%";//周同比 上月这周对比
            //本周待评价
            $sql7 = "SELECT count( 1 ) num FROM lkt_order a WHERE DATE_FORMAT( add_time, '%Y-%m-%d' ) >= '$monday'  AND store_id ='$store_id' AND recycle = 0 AND otype = 'GM' AND STATUS =5 AND NOT EXISTS ( SELECT * FROM lkt_comments c WHERE a.id = c.oid )";
            $res7 = Db::query($sql7);
            if($res7)
            {
                $orderStatus[3][1]['num'] = $res7[0]['num'];
            }
            //上周待评价
            $yesterday_wp = 0;
            $sql_14 = "SELECT count( 1 ) num FROM lkt_order a WHERE DATE_FORMAT( add_time, '%Y-%m-%d' ) >= '$lastWeekMonday' and DATE_FORMAT( add_time, '%Y-%m-%d' ) < '$monday'  AND store_id ='$store_id' AND recycle = 0 AND otype = 'GM' AND STATUS =5 AND NOT EXISTS ( SELECT * FROM lkt_comments c WHERE a.id = c.oid ) ";
            $res_14 = Db::query($sql_14);
            if($res_14)
            {
                $yesterday_wp = $res_14[0]['num'];
            }
            //上月本周待评价
            $monthPerWeeks = $this->monthPerWeek(date("Y-m-d", strtotime("-1 month")),$w);//上月本周时间段
            $week_wp = 0;
            $sql_15 = "SELECT count( 1 ) num FROM lkt_order a WHERE DATE_FORMAT( add_time, '%Y-%m-%d' ) >= '".$monthPerWeeks[0]."' and DATE_FORMAT( add_time, '%Y-%m-%d' ) <= '".$monthPerWeeks[1]."'  AND store_id ='$store_id' AND recycle = 0 AND otype = 'GM' AND STATUS =5 AND NOT EXISTS ( SELECT * FROM lkt_comments c WHERE a.id = c.oid ) ";
            $res_15 = Db::query($sql_15);
            if($res_15)
            {
                $week_wp = $res_15[0]['num'];
            }
            if($yesterday_wp > 0)
            {
                $rete_wd = round(($orderStatus[3][1]['num'] - $yesterday_wp)/$yesterday_wp * 100,2);
                if($rete_wd > 0)
                {
                    $orderStatus[3][1]['hbrate'] = $rete_wd."%";
                }
                else
                {
                    $orderStatus[3][1]['hbflag'] = "down";
                    $orderStatus[3][1]['hbrate'] = abs($rete_wd)."%";
                }
            }
            if($week_wp > 0)
            {
                $rete_md = round(($orderStatus[2][1]['num'] - $week_wp)/$week_wp * 100,2);
                if($rete_md > 0)
                {
                    $orderStatus[3][1]['tbrate'] = $rete_md."%";
                }
                else
                {
                    $orderStatus[3][1]['tbflag'] = "down";
                    $orderStatus[3][1]['tbrate'] = abs($rete_md)."%";
                }
            }

            //待处理
            $orderStatus[4][0]['num'] = 0;
            $orderStatus[4][0]['hbflag'] = "up";
            $orderStatus[4][0]['hbrate'] = "100%";//日环比 昨天对比
            $orderStatus[4][0]['tbflag'] = "up";
            $orderStatus[4][0]['tbrate'] = "100%";//日同比 上周今日对比
            //今日待处理
            $sql8 = "SELECT COUNT( DISTINCT b.sNo ) num FROM lkt_return_order a LEFT JOIN lkt_order b ON a.sNo = b.sNo WHERE DATE_FORMAT( a.re_time, '%Y-%m-%d' ) >='$today' AND DATE_FORMAT( a.re_time, '%Y-%m-%d' ) <='$today' AND a.store_id ='$store_id' AND b.otype = 'GM' AND a.r_type = 0";
            $res8 = Db::query($sql8);
            if($res8)
            {
                $orderStatus[4][0]['num'] = $res8[0]['num'];
            }
            //昨日待处理
            $yesterday_dc = 0;
            $sql_16 = "SELECT COUNT( DISTINCT b.sNo ) num FROM lkt_return_order a LEFT JOIN lkt_order b ON a.sNo = b.sNo WHERE DATE_FORMAT( a.re_time, '%Y-%m-%d' ) >='$day_1' AND DATE_FORMAT( a.re_time, '%Y-%m-%d' ) <='$day_1' AND a.store_id ='$store_id' AND b.otype = 'GM' AND a.r_type = 0";
            $res_16 = Db::query($sql_16);
            if($res_16)
            {
                $yesterday_dc = $res_16[0]['num'];
            }
            //上周今日待处理
            $week_dc = 0;
            $sql_17 = "SELECT COUNT( DISTINCT b.sNo ) num FROM lkt_return_order a LEFT JOIN lkt_order b ON a.sNo = b.sNo WHERE DATE_FORMAT( a.re_time, '%Y-%m-%d' ) >='$week_d' AND DATE_FORMAT( a.re_time, '%Y-%m-%d' ) <='$week_d' AND a.store_id ='$store_id' AND b.otype = 'GM' AND a.r_type = 0";
            $res_17 = Db::query($sql_17);
            if($res_17)
            {
                $week_dc = $res_17[0]['num'];
            }
            if($yesterday_dc > 0)
            {
                $rete_d = round(($orderStatus[4][0]['num'] - $yesterday_dc)/$yesterday_dc * 100,2);
                if($rete_d > 0)
                {
                    $orderStatus[4][0]['hbrate'] = $rete_d."%";
                }
                else
                {
                    $orderStatus[4][0]['hbflag'] = "down";
                    $orderStatus[4][0]['hbrate'] = abs($rete_d)."%";
                }
            }
            if($week_dc > 0)
            {
                $rete_w = round(($orderStatus[4][0]['num'] - $week_dc)/$week_dc * 100,2);
                if($rete_w > 0)
                {
                    $orderStatus[4][0]['tbrate'] = $rete_w."%";
                }
                else
                {
                    $orderStatus[4][0]['tbflag'] = "down";
                    $orderStatus[4][0]['tbrate'] = abs($rete_w)."%";
                }
            }
            $orderStatus[4][1]['num'] = 0;
            $orderStatus[4][1]['hbflag'] = "up";
            $orderStatus[4][1]['hbrate'] = "100%";//周环比 上周对比
            $orderStatus[4][1]['tbflag'] = "up";
            $orderStatus[4][1]['tbrate'] = "100%";//周同比 上月这周对比
            //本周待处理
            $sql9 = "SELECT COUNT( DISTINCT b.sNo ) num FROM lkt_return_order a LEFT JOIN lkt_order b ON a.sNo = b.sNo WHERE DATE_FORMAT( a.re_time, '%Y-%m-%d' ) >='$monday' AND a.store_id ='$store_id' AND b.otype = 'GM' AND a.r_type = 0";
            $res9 = Db::query($sql9);
            if($res9)
            {
                $orderStatus[4][1]['num'] = $res9[0]['num'];
            }
            //上周待处理
            $yesterday_wc = 0;
            $sql_18 = "SELECT COUNT( DISTINCT b.sNo ) num FROM lkt_return_order a LEFT JOIN lkt_order b ON a.sNo = b.sNo WHERE DATE_FORMAT( a.re_time, '%Y-%m-%d' ) >='$lastWeekMonday' AND DATE_FORMAT( a.re_time, '%Y-%m-%d' ) < '$monday' AND a.store_id ='$store_id' AND b.otype = 'GM' AND a.r_type = 0 ";
            $res_18 = Db::query($sql_18);
            if($res_18)
            {
                $yesterday_wc = $res_18[0]['num'];
            }
            //上月本周待处理
            $monthPerWeeks = $this->monthPerWeek(date("Y-m-d", strtotime("-1 month")),$w);//上月本周时间段
            $week_wc = 0;
            $sql_19 = "SELECT COUNT( DISTINCT b.sNo ) num FROM lkt_return_order a LEFT JOIN lkt_order b ON a.sNo = b.sNo WHERE DATE_FORMAT( a.re_time, '%Y-%m-%d' ) >='".$monthPerWeeks[0]."' AND DATE_FORMAT( a.re_time, '%Y-%m-%d' ) <= '".$monthPerWeeks[1]."' AND a.store_id ='$store_id' AND b.otype = 'GM' AND a.r_type = 0 ";
            $res_19 = Db::query($sql_19);
            if($res_19)
            {
                $week_wc = $res_19[0]['num'];
            }
            if($yesterday_wc > 0)
            {
                $rete_wd = round(($orderStatus[4][1]['num'] - $yesterday_wc)/$yesterday_wc * 100,2);
                if($rete_wd > 0)
                {
                    $orderStatus[4][1]['hbrate'] = $rete_wd."%";
                }
                else
                {
                    $orderStatus[4][1]['hbflag'] = "down";
                    $orderStatus[4][1]['hbrate'] = abs($rete_wd)."%";
                }
            }
            if($week_wc > 0)
            {
                $rete_md = round(($orderStatus[2][1]['num'] - $week_wc)/$week_wc * 100,2);
                if($rete_md > 0)
                {
                    $orderStatus[4][1]['tbrate'] = $rete_md."%";
                }
                else
                {
                    $orderStatus[4][1]['tbflag'] = "down";
                    $orderStatus[4][1]['tbrate'] = abs($rete_md)."%";
                }
            }

            //订单数据层
            $orderAmount = array();
            //今日订单数
            $orderAmount[0]['today_num'] = 0;
            $sql10 = "SELECT COUNT( DISTINCT o.sNo ) num FROM lkt_order AS o
                        LEFT JOIN lkt_user AS lu ON o.user_id = lu.user_id
                        RIGHT JOIN lkt_order_details AS d ON o.sNo = d.r_sNo
                        RIGHT JOIN lkt_configure attr ON attr.id = d.sid
                        RIGHT JOIN lkt_product_list AS p ON p.id = d.p_id 
                        RIGHT JOIN lkt_mch AS m ON p.mch_id = m.id
                        WHERE o.store_id ='$store_id' AND lu.store_id = '$store_id' AND ( p.gongyingshang IS NULL OR p.gongyingshang = 0 )
                        AND DATE_FORMAT( o.add_time, '%Y-%m-%d' )='$today' AND o.otype = 'GM' AND o.recycle != 1 ";
            $res10 = Db::query($sql10);
            if($res10)
            {
                $orderAmount[0]['today_num'] = $res10[0]['num'];
            }
            //昨日订单数
            $orderAmount[0]['yesterday_num'] = 0;
            $sql11 = "SELECT COUNT( DISTINCT o.sNo ) num FROM lkt_order AS o
                        LEFT JOIN lkt_user AS lu ON o.user_id = lu.user_id
                        RIGHT JOIN lkt_order_details AS d ON o.sNo = d.r_sNo
                        RIGHT JOIN lkt_configure attr ON attr.id = d.sid
                        RIGHT JOIN lkt_product_list AS p ON p.id = d.p_id 
                        RIGHT JOIN lkt_mch AS m ON p.mch_id = m.id 
                        WHERE o.store_id ='$store_id' AND lu.store_id = '$store_id' AND ( p.gongyingshang IS NULL OR p.gongyingshang = 0 )
                        AND DATE_FORMAT( o.add_time, '%Y-%m-%d' )='$day_1' AND o.otype = 'GM' AND o.recycle != 1 ";
            $res11 = Db::query($sql11);
            if($res11)
            {
                $orderAmount[0]['yesterday_num'] = $res11[0]['num'];
            }
            //本月订单数
            $orderAmount[0]['month_num'] = 0;
            $sql12 = "SELECT COUNT( DISTINCT o.sNo ) num FROM lkt_order AS o
                        LEFT JOIN lkt_user AS lu ON o.user_id = lu.user_id
                        RIGHT JOIN lkt_order_details AS d ON o.sNo = d.r_sNo
                        RIGHT JOIN lkt_configure attr ON attr.id = d.sid
                        RIGHT JOIN lkt_product_list AS p ON p.id = d.p_id 
                        RIGHT JOIN lkt_mch AS m ON p.mch_id = m.id 
                        WHERE o.store_id ='$store_id' AND lu.store_id = '$store_id' AND ( p.gongyingshang IS NULL OR p.gongyingshang = 0 )
                        AND DATE_FORMAT( o.add_time, '%Y-%m' )='$mon' AND o.otype = 'GM' AND o.recycle != 1 ";
            $res12 = Db::query($sql12);
            if($res12)
            {
                $orderAmount[0]['month_num'] = $res12[0]['num'];
            }
            //累计订单数
            $orderAmount[0]['all_num'] = 0;
            $sql13 = "SELECT COUNT( DISTINCT o.sNo ) num FROM lkt_order AS o
                        LEFT JOIN lkt_user AS lu ON o.user_id = lu.user_id
                        RIGHT JOIN lkt_order_details AS d ON o.sNo = d.r_sNo
                        RIGHT JOIN lkt_configure attr ON attr.id = d.sid
                        RIGHT JOIN lkt_product_list AS p ON p.id = d.p_id 
                        RIGHT JOIN lkt_mch AS m ON p.mch_id = m.id
                        WHERE o.store_id ='$store_id' AND lu.store_id = '$store_id' AND ( p.gongyingshang IS NULL OR p.gongyingshang = 0 )
                        AND o.otype = 'GM' AND o.recycle != 1 ";
            $res13 = Db::query($sql13);
            if($res13)
            {
                $orderAmount[0]['all_num'] = $res13[0]['num'];
            }
            $orderAmount[0]['flag'] = 'up';
            $orderAmount[0]['add_rate'] = '100%';
            if($orderAmount[0]['yesterday_num'] > 0)
            {
                $rete = round(($orderAmount[0]['today_num'] - $orderAmount[0]['yesterday_num'])/$orderAmount[0]['yesterday_num'] * 100,2);
                if($rete > 0)
                {
                    $orderAmount[0]['add_rate'] = $rete."%";
                }
                else
                {
                    $orderAmount[0]['flag']= "down";
                    $orderAmount[0]['add_rate'] = abs($rete)."%";
                }
            }

            //今日订单总额
            $orderAmount[1]['today_money'] = 0;
            $sql14 = "SELECT IFNULL( SUM( o.z_price ), 0 ) num FROM lkt_order o WHERE o.sNo IN (SELECT DISTINCT o.sNo FROM lkt_order AS o
                        LEFT JOIN lkt_user AS lu ON o.user_id = lu.user_id
                        RIGHT JOIN lkt_order_details AS d ON o.sNo = d.r_sNo
                        RIGHT JOIN lkt_configure attr ON attr.id = d.sid
                        RIGHT JOIN lkt_product_list AS p ON p.id = attr.pid 
                        WHERE
                            o.store_id = '$store_id' 
                            AND lu.store_id = '$store_id' 
                            AND o.recycle != 1 
                            AND o.otype IN ( 'GM' ) 
                            AND ( p.gongyingshang IS NULL OR p.gongyingshang = 0 ) 
                        ) 
                        AND STATUS != 0 
                        AND DATE_FORMAT( add_time, '%Y-%m-%d' )= '$today'";
            $res14 = Db::query($sql14);
            if($res14)
            {
                $orderAmount[1]['today_money'] = $res14[0]['num'];
            }
            //昨日订单总额
            $orderAmount[1]['yesterday_money'] = 0;
            $sql15 = "SELECT IFNULL( SUM( o.z_price ), 0 ) num FROM lkt_order o WHERE o.sNo IN (SELECT DISTINCT o.sNo FROM lkt_order AS o
                        LEFT JOIN lkt_user AS lu ON o.user_id = lu.user_id
                        RIGHT JOIN lkt_order_details AS d ON o.sNo = d.r_sNo
                        RIGHT JOIN lkt_configure attr ON attr.id = d.sid
                        RIGHT JOIN lkt_product_list AS p ON p.id = attr.pid 
                        WHERE
                            o.store_id = '$store_id' 
                            AND lu.store_id = '$store_id' 
                            AND o.recycle != 1 
                            AND o.otype IN ( 'GM' ) 
                            AND ( p.gongyingshang IS NULL OR p.gongyingshang = 0 ) 
                        ) 
                        AND STATUS != 0 
                        AND DATE_FORMAT( add_time, '%Y-%m-%d' )= '$day_1'";
            $res15 = Db::query($sql15);
            if($res15)
            {
                $orderAmount[1]['yesterday_money'] = $res15[0]['num'];
            }
            //本月订单总额
            $orderAmount[1]['month_money'] = 0;
            $sql16 = "SELECT IFNULL( SUM( o.z_price ), 0 ) num FROM lkt_order o WHERE o.sNo IN (SELECT DISTINCT o.sNo FROM lkt_order AS o
                        LEFT JOIN lkt_user AS lu ON o.user_id = lu.user_id
                        RIGHT JOIN lkt_order_details AS d ON o.sNo = d.r_sNo
                        RIGHT JOIN lkt_configure attr ON attr.id = d.sid
                        RIGHT JOIN lkt_product_list AS p ON p.id = attr.pid 
                        WHERE
                            o.store_id = '$store_id' 
                            AND lu.store_id = '$store_id' 
                            AND o.recycle != 1 
                            AND o.otype IN ( 'GM' ) 
                            AND ( p.gongyingshang IS NULL OR p.gongyingshang = 0 ) 
                        ) 
                        AND STATUS != 0 
                        AND DATE_FORMAT( add_time, '%Y-%m' )= '$mon'";
            $res16 = Db::query($sql16);
            if($res16)
            {
                $orderAmount[1]['month_money'] = $res16[0]['num'];
            }
            //累计订单总额
            $orderAmount[1]['all_money'] = 0;
            $sql17 = "SELECT IFNULL( SUM( o.z_price ), 0 ) num FROM lkt_order o WHERE o.sNo IN (SELECT DISTINCT o.sNo FROM lkt_order AS o
                        LEFT JOIN lkt_user AS lu ON o.user_id = lu.user_id
                        RIGHT JOIN lkt_order_details AS d ON o.sNo = d.r_sNo
                        RIGHT JOIN lkt_configure attr ON attr.id = d.sid
                        RIGHT JOIN lkt_product_list AS p ON p.id = attr.pid 
                        WHERE
                            o.store_id = '$store_id' 
                            AND lu.store_id = '$store_id' 
                            AND o.recycle != 1 
                            AND o.otype IN ( 'GM' ) 
                            AND ( p.gongyingshang IS NULL OR p.gongyingshang = 0 ) 
                        ) 
                        AND STATUS not in (0,7) ";
            $res17 = Db::query($sql17);
            if($res17)
            {
                $orderAmount[1]['all_money'] = $res17[0]['num'];
            }
            $orderAmount[1]['add_money_rate'] = "100%";
            $orderAmount[1]['flag'] = "up";
            if($orderAmount[1]['yesterday_money'] > 0)
            {
                $rete0 = round(($orderAmount[1]['today_money'] - $orderAmount[1]['yesterday_money'])/$orderAmount[1]['yesterday_money'] * 100,2);
                if($rete0 > 0)
                {
                    $orderAmount[1]['add_money_rate'] = $rete0."%";
                }
                else
                {
                    $orderAmount[1]['flag']= "down";
                    $orderAmount[1]['add_money_rate'] = abs($rete0)."%";
                }
            }

            //今日退款金额
            $orderAmount[2]['today_return'] = 0;
            $sql18 = "SELECT ifnull( sum( real_money ), 0 ) num FROM lkt_return_order a
                        LEFT JOIN lkt_order b ON a.sNo = b.sNo
                        LEFT JOIN lkt_product_list AS p ON p.id = a.pid 
                    WHERE
                        a.store_id ='$store_id' 
                        AND a.r_type IN ( 4, 9 ) 
                        AND DATE_FORMAT( a.re_time, '%Y-%m-%d' )='$today' 
                        AND b.otype = 'GM' 
                        AND ( p.gongyingshang IS NULL OR p.gongyingshang = 0 )";
            $res18 = Db::query($sql18);
            if($res18)
            {
                $orderAmount[2]['today_return'] = round($res18[0]['num'],2);
            }
            //昨日退款金额
            $orderAmount[2]['yesterday_return'] = 0;
            $sql19 = "SELECT ifnull( sum( real_money ), 0 ) num FROM lkt_return_order a
                        LEFT JOIN lkt_order b ON a.sNo = b.sNo
                        LEFT JOIN lkt_product_list AS p ON p.id = a.pid 
                    WHERE
                        a.store_id ='$store_id' 
                        AND a.r_type IN ( 4, 9 ) 
                        AND DATE_FORMAT( a.re_time, '%Y-%m-%d' )='$day_1' 
                        AND b.otype = 'GM' 
                        AND ( p.gongyingshang IS NULL OR p.gongyingshang = 0 )";
            $res19 = Db::query($sql19);
            if($res19)
            {
                $orderAmount[2]['yesterday_return'] = round($res19[0]['num'],2);
            }
            //本月退款金额
            $orderAmount[2]['month_return'] = 0;
            $sql20 = "SELECT ifnull( sum( real_money ), 0 ) num FROM lkt_return_order a
                        LEFT JOIN lkt_order b ON a.sNo = b.sNo
                        LEFT JOIN lkt_product_list AS p ON p.id = a.pid 
                    WHERE
                        a.store_id ='$store_id' 
                        AND a.r_type IN ( 4, 9 ) 
                        AND DATE_FORMAT( a.re_time, '%Y-%m' )='$mon' 
                        AND b.otype = 'GM' 
                        AND ( p.gongyingshang IS NULL OR p.gongyingshang = 0 )";
            $res20 = Db::query($sql20);
            if($res20)
            {
                $orderAmount[2]['month_return'] = round($res20[0]['num'],2);
            }
            //全部退款金额
            $orderAmount[2]['all_return'] = 0;
            $sql21 = "SELECT ifnull( sum( real_money ), 0 ) num FROM lkt_return_order a
                        LEFT JOIN lkt_order b ON a.sNo = b.sNo
                        LEFT JOIN lkt_product_list AS p ON p.id = a.pid 
                    WHERE
                        a.store_id ='$store_id' 
                        AND a.r_type IN ( 4, 9 )  
                        AND b.otype = 'GM' 
                        AND ( p.gongyingshang IS NULL OR p.gongyingshang = 0 )";
            $res21 = Db::query($sql21);
            if($res21)
            {
                $orderAmount[2]['all_return'] =round($res21[0]['num'],2);
            }
            $orderAmount[2]['all_return_rate'] = "100%";
            $orderAmount[2]['flag'] = "up";
            if($orderAmount[2]['yesterday_return'] > 0)
            {
                $rete1 = round(($orderAmount[2]['today_return'] - $orderAmount[2]['yesterday_return'])/$orderAmount[2]['yesterday_return'] * 100,2);
                if($rete1 > 0)
                {
                    $orderAmount[1]['all_return_rate'] = $rete1."%";
                }
                else
                {
                    $orderAmount[1]['flag']= "down";
                    $orderAmount[1]['all_return_rate'] = abs($rete1)."%";
                }
            }

            //订单总数
            $totalAmount = array();
            $sql22 = "select data from lkt_order_report where store_id = '$store_id' and type = 7 ";
            $res22 = Db::query($sql22);
            if($res22)
            {
                $totalAmount = json_decode($res22[0]['data']);
            }

            //退付款订单总数
            $refundOrderData = array();
            $sql23 = "select data from lkt_order_report where store_id = '$store_id' and type = 6";
            $res23 = Db::query($sql23);
            if($res23)
            {
                $refundOrderData = json_decode($res23[0]['data']);
            }
            $data = array('orderAmount'=>$orderAmount,'orderStatus'=>$orderStatus,'refundOrderData'=>$refundOrderData,'totalAmount'=>$totalAmount);
            $message = Lang("Success");
            return output(200,$message,$data);
    }

    //商品报表
    public function goodsIndex()
    {
            $store_id = addslashes(trim($this->request->param('storeId')));
            $store_type = addslashes(trim($this->request->param('storeType')));
            $access_id = addslashes(trim($this->request->param('accessId')));
            $mch_id = cache($access_id.'_'.$store_type);
            //状态查询
            $mon = date("Y-m");//当前月份
            //得到系统的年月
            $tmp_date = date("Ym");
            //切割出年份
            $tmp_year = substr($tmp_date, 0, 4);
            //切割出月份
            $tmp_mon = substr($tmp_date, 4, 2);
            $tmp_forwardmonth = mktime(0, 0, 0, $tmp_mon - 1, 1, $tmp_year);
            //得到当前月的上一个月
            $lastmon = date("Y-m", $tmp_forwardmonth);
            //今天
            $today = date("Y-m-d");
            $day_1 = date("Y-m-d", strtotime("-1 day"));//昨天

            $countList = array();
            //对接商品数
            $countList['userAmount'] = 0;
            $sql0 = "select count(1) num from lkt_product_list where recycle = 0 and mch_status = 2 and store_id='$store_id' ";
            $res0 = Db::query($sql0);
            if($res0)
            {
                $countList['userAmount'] = $res0[0]['num'];
            }
            //对接店铺数
            $countList['mchAmount'] = 0;
            $sql1 = "select count(1) num from lkt_mch where store_id='$store_id' and review_status = 1 and recovery = 0";
            $res1 = Db::query($sql1);
            if($res1)
            {
                $countList['mchAmount'] = $res1[0]['num'];
            }
            //商品分类数
            $countList['productClassAmount'] = 0;
            $sql2 = "select count(1) num from lkt_product_class WHERE store_id = '$store_id' and recycle = 0";
            $res2 = Db::query($sql2);
            if($res2)
            {
                $countList['productClassAmount'] = $res2[0]['num'];
            }
            //商品品牌数
            $countList['brandAmount'] = 0;
            $sql3 = "select count(1) num from lkt_brand_class where store_id='$store_id' and recycle=0";
            $res3 = Db::query($sql3);
            if($res3)
            {
                $countList['brandAmount'] = $res3[0]['num'];
            }

            //商品状态
            $goodsSalesInfoWithStatus = array();
            //在售
            $goodsSalesInfoWithStatus[0][0]=array();//平台
            $goodsSalesInfoWithStatus[0][1]=array();//其他
            $goodsSalesInfoWithStatus[0][2]=array();//供应商
            $sql4 = "SELECT COUNT( 1 ) value,CASE TRUE WHEN TRUE THEN '平台自营店' WHEN FALSE THEN '其他店铺' END name,CASE 2 WHEN 2 THEN '在售' WHEN 3 THEN '已下架' ELSE '所有' END status FROM lkt_product_list WHERE mch_id = '$mch_id' 
                    AND store_id ='$store_id' 
                    AND status =2 
                    AND recycle = 0 
                    AND mch_status = 2 
                    AND ( gongyingshang = 0 OR gongyingshang IS NULL )";
            $res4 = Db::query($sql4);
            if($res4)
            {
                $goodsSalesInfoWithStatus[0][0]=$res4[0];
            }
            $sql5 = "SELECT COUNT( 1 ) value,CASE false WHEN TRUE THEN '平台自营店' WHEN FALSE THEN '其他店铺' END name,CASE 2 WHEN 2 THEN '在售' WHEN 3 THEN '已下架' ELSE '所有' END status FROM lkt_product_list WHERE mch_id != '$mch_id' 
                    AND store_id ='$store_id'  
                    AND status =2 
                    AND recycle = 0 
                    AND mch_status = 2 
                    AND ( gongyingshang = 0 OR gongyingshang IS NULL )";
            $res5 = Db::query($sql5);
            if($res5)
            {
                $goodsSalesInfoWithStatus[0][1]=$res5[0];
            }
            $sql6 = "SELECT COUNT( 1 ) value,'供应商商城' name,CASE 2 WHEN 2 THEN '在售' WHEN 3 THEN '已下架' WHEN 4 THEN '已下架' ELSE '所有' END status FROM lkt_product_list WHERE mch_id = 0 
                    AND store_id ='$store_id'  
                    AND status =2 
                    AND recycle = 0 
                    AND mch_status = 2 
                    AND gongyingshang IS NOT NULL 
                    AND gongyingshang !=0";
            $res6 = Db::query($sql6);
            if($res6)
            {
                $goodsSalesInfoWithStatus[0][2]=$res6[0];
            }
            //已下架
            $goodsSalesInfoWithStatus[1][0]=array();//平台
            $goodsSalesInfoWithStatus[1][1]=array();//其他
            $goodsSalesInfoWithStatus[1][2]=array();//供应商
            $sql7 = "SELECT COUNT( 1 ) value,CASE TRUE WHEN TRUE THEN '平台自营店' WHEN FALSE THEN '其他店铺' END name,CASE 3 WHEN 2 THEN '在售' WHEN 3 THEN '已下架' ELSE '所有' END status FROM lkt_product_list WHERE mch_id = '$mch_id' 
                    AND store_id ='$store_id' 
                    AND status =3 
                    AND recycle = 0 
                    AND mch_status = 2 
                    AND ( gongyingshang = 0 OR gongyingshang IS NULL )";
            $res7 = Db::query($sql7);
            if($res7)
            {
                $goodsSalesInfoWithStatus[1][0]=$res7[0];
            }
            $sql8 = "SELECT COUNT( 1 ) value,CASE false WHEN TRUE THEN '平台自营店' WHEN FALSE THEN '其他店铺' END name,CASE 3 WHEN 2 THEN '在售' WHEN 3 THEN '已下架' ELSE '所有' END status FROM lkt_product_list WHERE mch_id != '$mch_id' 
                    AND store_id ='$store_id'  
                    AND status =3 
                    AND recycle = 0 
                    AND mch_status = 2 
                    AND ( gongyingshang = 0 OR gongyingshang IS NULL )";
            $res8 = Db::query($sql8);
            if($res8)
            {
                $goodsSalesInfoWithStatus[1][1]=$res8[0];
            }
            $sql9 = "SELECT COUNT( 1 ) value,'供应商商城' name,CASE 4 WHEN 2 THEN '在售' WHEN 3 THEN '已下架' WHEN 4 THEN '已下架' ELSE '所有' END status FROM lkt_product_list WHERE mch_id = 0   AND store_id ='$store_id'  
                    AND status =4 
                    AND recycle = 0 
                    AND mch_status = 2 
                    AND gongyingshang IS NOT NULL 
                    AND gongyingshang !=0";
            $res9 = Db::query($sql9);
            if($res9)
            {
                $goodsSalesInfoWithStatus[1][2]=$res9[0];
            }

            //全部
            $goodsSalesInfoWithStatus[2][0]=array();//平台
            $goodsSalesInfoWithStatus[2][1]=array();//其他
            $goodsSalesInfoWithStatus[2][2]=array();//供应商
            $sql10 = "SELECT COUNT( 1 ) value,CASE TRUE WHEN TRUE THEN '平台自营店' WHEN FALSE THEN '其他店铺' END name,CASE null WHEN 2 THEN '在售' WHEN 3 THEN '已下架' ELSE '所有' END status FROM lkt_product_list WHERE mch_id = '$mch_id' 
                    AND store_id ='$store_id'  
                    AND recycle = 0 
                    AND mch_status = 2 
                    AND ( gongyingshang = 0 OR gongyingshang IS NULL )";
            $res10 = Db::query($sql10);
            if($res10)
            {
                $goodsSalesInfoWithStatus[2][0]=$res10[0];
            }
            $sql11 = "SELECT COUNT( 1 ) value,CASE false WHEN TRUE THEN '平台自营店' WHEN FALSE THEN '其他店铺' END name,CASE null WHEN 2 THEN '在售' WHEN 3 THEN '已下架' ELSE '所有' END status FROM lkt_product_list WHERE mch_id != '$mch_id' 
                    AND store_id ='$store_id'  
                    AND recycle = 0 
                    AND mch_status = 2 
                    AND ( gongyingshang = 0 OR gongyingshang IS NULL )";
            $res11 = Db::query($sql11);
            if($res11)
            {
                $goodsSalesInfoWithStatus[2][1]=$res11[0];
            }
            $sql12 = "SELECT COUNT( 1 ) value,'供应商商城' name,CASE null WHEN 2 THEN '在售' WHEN 3 THEN '已下架' WHEN 4 THEN '已下架' ELSE '所有' END status FROM lkt_product_list WHERE mch_id = 0   AND store_id ='$store_id'  
                    AND recycle = 0 
                    AND mch_status = 2 
                    AND gongyingshang IS NOT NULL 
                    AND gongyingshang !=0";
            $res12 = Db::query($sql12);
            if($res12)
            {
                $goodsSalesInfoWithStatus[2][2]=$res12[0];
            }

            //商品销量汇总
            $goodsSalesInfo = array();
            //近7天
            $date_week = array();
            for ($i = 6; $i >=0; $i--)
            {
                $date_week[] = date('Y-m-d', strtotime("$today -$i days"));
            }
            foreach ($date_week as $key => $value) 
            {   
                $goodsSalesInfo[0][0][$key] = $value;
                $goodsSalesInfo[0][1][$key] = 0;
                $goodsSalesInfo[0][2][$key] = 0;
                //自营
                $sql13 = "SELECT ifnull( sum( a.num ), 0 ) num FROM lkt_order_details a
                            LEFT JOIN lkt_order b ON a.r_sNo = b.sNo
                            LEFT JOIN lkt_product_list p ON a.p_id = p.id WHERE b.STATUS IN ( 1, 2, 5, 7 ) 
                            AND a.store_id ='$store_id'
                            AND DATE_FORMAT( a.add_time, '%Y-%m-%d' )='$value' 
                            AND p.mch_id ='$mch_id'";
                $res13 = Db::query($sql13);
                if($res13)
                {
                    $goodsSalesInfo[0][1][$key] = $res13[0]['num'];
                }
                //非自营
                $sql14 = "SELECT ifnull( sum( a.num ), 0 ) num FROM lkt_order_details a
                            LEFT JOIN lkt_order b ON a.r_sNo = b.sNo
                            LEFT JOIN lkt_product_list p ON a.p_id = p.id WHERE b.STATUS IN ( 1, 2, 5, 7 ) 
                            AND a.store_id ='$store_id'
                            AND DATE_FORMAT( a.add_time, '%Y-%m-%d' )='$value' 
                            AND p.mch_id !='$mch_id'";
                $res14 = Db::query($sql14);
                if($res14)
                {
                    $goodsSalesInfo[0][2][$key] = $res14[0]['num'];
                }
            }
            //近一月
            $date_month = array();
            for ($i = 30; $i >= 0; $i--)
            {
                $date_month[] = date('Y-m-d', strtotime("$today -$i days")); //每隔一天赋值给数组
            }
            foreach ($date_month as $key => $value) 
            {
                $goodsSalesInfo[1][0][$key] = $value;
                $goodsSalesInfo[1][1][$key] = 0;
                $goodsSalesInfo[1][2][$key] = 0;
                //自营
                $sql15 = "SELECT ifnull( sum( a.num ), 0 ) num FROM lkt_order_details a
                            LEFT JOIN lkt_order b ON a.r_sNo = b.sNo
                            LEFT JOIN lkt_product_list p ON a.p_id = p.id WHERE b.STATUS IN ( 1, 2, 5, 7 ) 
                            AND a.store_id ='$store_id'
                            AND DATE_FORMAT( a.add_time, '%Y-%m-%d' )='$value' 
                            AND p.mch_id ='$mch_id'";
                $res15 = Db::query($sql15);
                if($res15)
                {
                    $goodsSalesInfo[1][1][$key] = $res15[0]['num'];
                }
                //非自营
                $sql16 = "SELECT ifnull( sum( a.num ), 0 ) num FROM lkt_order_details a
                            LEFT JOIN lkt_order b ON a.r_sNo = b.sNo
                            LEFT JOIN lkt_product_list p ON a.p_id = p.id WHERE b.STATUS IN ( 1, 2, 5, 7 ) 
                            AND a.store_id ='$store_id'
                            AND DATE_FORMAT( a.add_time, '%Y-%m-%d' )='$value' 
                            AND p.mch_id !='$mch_id'";
                $res16 = Db::query($sql16);
                if($res16)
                {
                    $goodsSalesInfo[1][2][$key] = $res16[0]['num'];
                }
            }
            //近一年
            $date_year = array();
            for ($i = 12; $i >= 0; $i--)
            {
                $date_year[] = date('Y-m', strtotime("$mon -$i month")); //每隔一天赋值给数组
            }
            foreach ($date_year as $key => $value) 
            {
                $goodsSalesInfo[2][0][$key] = $value;
                $goodsSalesInfo[2][1][$key] = 0;
                $goodsSalesInfo[2][2][$key] = 0;
                //自营
                $sql17 = "SELECT ifnull( sum( a.num ), 0 ) num FROM lkt_order_details a
                            LEFT JOIN lkt_order b ON a.r_sNo = b.sNo
                            LEFT JOIN lkt_product_list p ON a.p_id = p.id WHERE b.STATUS IN ( 1, 2, 5, 7 ) 
                            AND a.store_id ='$store_id'
                            AND DATE_FORMAT( a.add_time, '%Y-%m' )='$value' 
                            AND p.mch_id ='$mch_id'";
                $res17 = Db::query($sql17);
                if($res17)
                {
                    $goodsSalesInfo[2][1][$key] = $res17[0]['num'];
                }
                //非自营
                $sql18 = "SELECT ifnull( sum( a.num ), 0 ) num FROM lkt_order_details a
                            LEFT JOIN lkt_order b ON a.r_sNo = b.sNo
                            LEFT JOIN lkt_product_list p ON a.p_id = p.id WHERE b.STATUS IN ( 1, 2, 5, 7 ) 
                            AND a.store_id ='$store_id'
                            AND DATE_FORMAT( a.add_time, '%Y-%m' )='$value' 
                            AND p.mch_id !='$mch_id'";
                $res18 = Db::query($sql18);
                if($res18)
                {
                    $goodsSalesInfo[2][2][$key] = $res18[0]['num'];
                }
            }

            //商品规格
            $skuInfo = array();
            //商城
            $skuInfo[0][0] = array();
            $skuInfo[0][1] = array();
            //店铺
            $skuInfo[1][0] = array();
            $skuInfo[1][1] = array();
            //供应商
            $skuInfo[2][0] = array();
            $skuInfo[2][1] = array();
            //查询所有一级分类
            $sql_c = "select cid,pname from lkt_product_class where store_id = '$store_id' and level = 0 and sid = 0 and recycle = 0";
            $res_c = Db::query($sql_c);
            if($res_c)
            {   
                foreach ($res_c as $key => $value) 
                {   
                    $cid = $value['cid'];
                    //自营商品
                    $sql19 = "SELECT ifnull( count( 1 ), 0 ) num FROM lkt_configure a
                                LEFT JOIN lkt_product_list b ON a.pid = b.id 
                                WHERE b.store_id ='$store_id' AND b.mch_id ='$mch_id' AND b.product_class like '%-$cid-%' AND ( b.gongyingshang = 0 OR b.gongyingshang IS NULL ) AND a.recycle = 0 AND b.recycle = 0 ";
                    $res19 = Db::query($sql19);
                    if($res19 && $res19[0]['num'] > 0)
                    {
                        array_push($skuInfo[0][0], $value['pname']);
                        array_push($skuInfo[0][1], $res19[0]['num']);
                    }
                    //其他店铺
                    $sql20 = "SELECT ifnull( count( 1 ), 0 ) num FROM lkt_configure a
                                LEFT JOIN lkt_product_list b ON a.pid = b.id 
                                WHERE b.store_id ='$store_id' AND b.mch_id !='$mch_id' AND b.product_class like '%-$cid-%' AND ( b.gongyingshang = 0 OR b.gongyingshang IS NULL ) AND a.recycle = 0 AND b.recycle = 0";
                    $res20 = Db::query($sql20);
                    if($res20 && $res20[0]['num']>0)
                    {
                        array_push($skuInfo[1][0], $value['pname']);
                        array_push($skuInfo[1][1], $res20[0]['num']);
                    }
                    //供应商
                    $sql21 = "SELECT ifnull( count( 1 ), 0 ) num FROM lkt_configure a
                                LEFT JOIN lkt_product_list b ON a.pid = b.id 
                                WHERE b.gongyingshang != '' AND b.gongyingshang != 0 AND a.recycle = 0 AND b.recycle = 0 AND b.store_id ='$store_id' AND b.mch_id = 0 AND b.product_class like '%-$cid-%' ";
                    $res21 = Db::query($sql21);
                    if($res21 && $res21[0]['num'])
                    {
                        array_push($skuInfo[2][0], $value['pname']);
                        array_push($skuInfo[2][1], $res21[0]['num']);
                    }
                }
            }

            //商品数量
            $goodsNumInfo = array();

            //近7天
            foreach ($date_week as $key => $value) 
            {   

                $goodsNumInfo[0][0][0][$key] = $value;
                $goodsNumInfo[0][0][1][$key] = 0;//商城已售
                $goodsNumInfo[0][0][2][$key] = 0;//商城退货
                $goodsNumInfo[0][0][3][$key] = 0;//商城新增
                $sql22 = "SELECT ifnull( sum( a.num ), 0 ) num FROM lkt_order_details a
                            INNER JOIN lkt_product_list b ON a.p_id = b.id 
                        WHERE a.r_status IN ( 1, 2, 5, 7 ) AND b.mch_id ='$mch_id' AND ( b.gongyingshang = 0 OR b.gongyingshang IS NULL ) AND b.store_id ='$store_id' AND DATE_FORMAT( a.add_time, '%Y-%m-%d' ) = '$value'";
                $res22 = Db::query($sql22);
                if($res22)
                {
                    $goodsNumInfo[0][0][1][$key] = $res22[0]['num'];
                }
                $sql23 = "SELECT ifnull( sum( o.num ), 0 ) num FROM lkt_return_order a
                            LEFT JOIN lkt_product_list b ON a.pid = b.id
                            INNER JOIN lkt_order o ON a.sNo = o.sNo 
                        WHERE a.store_id ='$store_id' AND DATE_FORMAT( a.re_time, '%Y-%m-%d' )='$value' AND b.mch_id ='$mch_id' AND ( b.gongyingshang = 0 OR b.gongyingshang IS NULL )";
                $res23 = Db::query($sql23);
                if($res23)
                {
                    $goodsNumInfo[0][0][2][$key] = $res23[0]['num'];
                }
                $sql24 = "SELECT ifnull( count( 1 ), 0 ) num FROM lkt_product_list WHERE DATE_FORMAT( add_date, '%Y-%m-%d' )='$value' AND store_id ='$store_id' AND mch_id ='$mch_id' AND ( gongyingshang = 0 OR gongyingshang IS NULL )";
                $res24 = Db::query($sql24);
                if($res24)
                {
                    $goodsNumInfo[0][0][3][$key] = $res24[0]['num'];
                }

                $goodsNumInfo[0][1][0][$key] = $value;
                $goodsNumInfo[0][1][1][$key] = 0;//店铺已售
                $goodsNumInfo[0][1][2][$key] = 0;//店铺退货
                $goodsNumInfo[0][1][3][$key] = 0;//店铺新增
                $sql25 = "SELECT ifnull( sum( a.num ), 0 ) num FROM lkt_order_details a
                            INNER JOIN lkt_product_list b ON a.p_id = b.id 
                        WHERE a.r_status IN ( 1, 2, 5, 7 ) AND b.mch_id !='$mch_id' AND ( b.gongyingshang = 0 OR b.gongyingshang IS NULL ) AND b.store_id ='$store_id' AND DATE_FORMAT( a.add_time, '%Y-%m-%d' ) = '$value'";
                $res25 = Db::query($sql25);
                if($res25)
                {
                    $goodsNumInfo[0][1][1][$key] = $res25[0]['num'];
                }
                $sql26 = "SELECT ifnull( sum( o.num ), 0 ) num FROM lkt_return_order a
                            LEFT JOIN lkt_product_list b ON a.pid = b.id
                            INNER JOIN lkt_order o ON a.sNo = o.sNo 
                        WHERE a.store_id ='$store_id' AND DATE_FORMAT( a.re_time, '%Y-%m-%d' )='$value' AND b.mch_id !='$mch_id' AND ( b.gongyingshang = 0 OR b.gongyingshang IS NULL )";
                $res26 = Db::query($sql26);
                if($res26)
                {
                    $goodsNumInfo[0][1][2][$key] = $res26[0]['num'];
                }
                $sql27 = "SELECT ifnull( count( 1 ), 0 ) num FROM lkt_product_list WHERE DATE_FORMAT( add_date, '%Y-%m-%d' )='$value' AND store_id ='$store_id' AND mch_id !='$mch_id' AND ( gongyingshang = 0 OR gongyingshang IS NULL )";
                $res27 = Db::query($sql27);
                if($res27)
                {
                    $goodsNumInfo[0][1][3][$key] = $res27[0]['num'];
                }

                $goodsNumInfo[0][2][0][$key] = $value;
                $goodsNumInfo[0][2][1][$key] = 0;//供应商已售
                $goodsNumInfo[0][2][2][$key] = 0;//供应商退货
                $goodsNumInfo[0][2][3][$key] = 0;//供应商新增
                $sql28 = "SELECT ifnull( sum( a.num ), 0 ) num  FROM lkt_order_details a INNER JOIN lkt_product_list b ON a.p_id = b.id WHERE a.r_status IN ( 1, 2, 5, 7 ) AND b.gongyingshang != 0 AND b.store_id = '$store_id' AND b.mch_id = 0 AND DATE_FORMAT( a.add_time, '%Y-%m-%d' ) = '$value' ";
                $res28 =Db::query($sql28);
                if($res28)
                {
                    $goodsNumInfo[0][2][1][$key] = $res28[0]['num'];
                }
                $sql29 = "SELECT ifnull( sum( o.num ), 0 ) num FROM lkt_return_order a
                            LEFT JOIN lkt_product_list b ON a.pid = b.id
                            INNER JOIN lkt_order o ON a.sNo = o.sNo 
                            WHERE a.store_id ='$store_id' AND DATE_FORMAT( a.re_time, '%Y-%m-%d' )='$value' AND b.gongyingshang != 0 AND b.mch_id = 0";
                $res29 = Db::query($sql29);
                if($res29)
                {
                    $goodsNumInfo[0][2][2][$key] = $res29[0]['num'];
                }
                $sql30 = "SELECT ifnull( count( 1 ), 0 ) num FROM lkt_product_list WHERE DATE_FORMAT( add_date, '%Y-%m-%d' )='$value' AND store_id ='$store_id' AND mch_id = 0 AND gongyingshang != 0";
                $res30 = Db::query($sql30);
                if($res30)
                {
                    $goodsNumInfo[0][2][3][$key] = $res30[0]['num'];
                }
            }

            //近一月
            foreach ($date_month as $key => $value) 
            {
                $goodsNumInfo[1][0][0][$key] = $value;
                $goodsNumInfo[1][0][1][$key] = 0;//商城已售
                $goodsNumInfo[1][0][2][$key] = 0;//商城退货
                $goodsNumInfo[1][0][3][$key] = 0;//商城新增
                $sql31 = "SELECT ifnull( sum( a.num ), 0 ) num FROM lkt_order_details a
                            INNER JOIN lkt_product_list b ON a.p_id = b.id 
                        WHERE a.r_status IN ( 1, 2, 5, 7 ) AND b.mch_id ='$mch_id' AND ( b.gongyingshang = 0 OR b.gongyingshang IS NULL ) AND b.store_id ='$store_id' AND DATE_FORMAT( a.add_time, '%Y-%m-%d' ) = '$value'";
                $res31 = Db::query($sql31);
                if($res31)
                {
                    $goodsNumInfo[1][0][1][$key] = $res31[0]['num'];
                }
                $sql32 = "SELECT ifnull( sum( o.num ), 0 ) num FROM lkt_return_order a
                            LEFT JOIN lkt_product_list b ON a.pid = b.id
                            INNER JOIN lkt_order o ON a.sNo = o.sNo 
                        WHERE a.store_id ='$store_id' AND DATE_FORMAT( a.re_time, '%Y-%m-%d' )='$value' AND b.mch_id ='$mch_id' AND ( b.gongyingshang = 0 OR b.gongyingshang IS NULL )";
                $res32 = Db::query($sql32);
                if($res32)
                {
                    $goodsNumInfo[1][0][2][$key] = $res32[0]['num'];
                }
                $sql33 = "SELECT ifnull( count( 1 ), 0 ) num FROM lkt_product_list WHERE DATE_FORMAT( add_date, '%Y-%m-%d' )='$value' AND store_id ='$store_id' AND mch_id ='$mch_id' AND ( gongyingshang = 0 OR gongyingshang IS NULL )";
                $res33 = Db::query($sql33);
                if($res33)
                {
                    $goodsNumInfo[1][0][3][$key] = $res33[0]['num'];
                }

                $goodsNumInfo[1][1][0][$key] = $value;
                $goodsNumInfo[1][1][1][$key] = 0;//店铺已售
                $goodsNumInfo[1][1][2][$key] = 0;//店铺退货
                $goodsNumInfo[1][1][3][$key] = 0;//店铺新增
                $sql34 = "SELECT ifnull( sum( a.num ), 0 ) num FROM lkt_order_details a
                            INNER JOIN lkt_product_list b ON a.p_id = b.id 
                        WHERE a.r_status IN ( 1, 2, 5, 7 ) AND b.mch_id !='$mch_id' AND ( b.gongyingshang = 0 OR b.gongyingshang IS NULL ) AND b.store_id ='$store_id' AND DATE_FORMAT( a.add_time, '%Y-%m-%d' ) = '$value'";
                $res34 = Db::query($sql34);
                if($res34)
                {
                    $goodsNumInfo[1][1][1][$key] = $res34[0]['num'];
                }
                $sql35 = "SELECT ifnull( sum( o.num ), 0 ) num FROM lkt_return_order a
                            LEFT JOIN lkt_product_list b ON a.pid = b.id
                            INNER JOIN lkt_order o ON a.sNo = o.sNo 
                        WHERE a.store_id ='$store_id' AND DATE_FORMAT( a.re_time, '%Y-%m-%d' )='$value' AND b.mch_id !='$mch_id' AND ( b.gongyingshang = 0 OR b.gongyingshang IS NULL )";
                $res35 = Db::query($sql35);
                if($res35)
                {
                    $goodsNumInfo[1][1][2][$key] = $res35[0]['num'];
                }
                $sql36 = "SELECT ifnull( count( 1 ), 0 ) num FROM lkt_product_list WHERE DATE_FORMAT( add_date, '%Y-%m-%d' )='$value' AND store_id ='$store_id' AND mch_id !='$mch_id' AND ( gongyingshang = 0 OR gongyingshang IS NULL )";
                $res36 = Db::query($sql36);
                if($res36)
                {
                    $goodsNumInfo[1][1][3][$key] = $res36[0]['num'];
                }

                $goodsNumInfo[1][2][0][$key] = $value;
                $goodsNumInfo[1][2][1][$key] = 0;//供应商已售
                $goodsNumInfo[1][2][2][$key] = 0;//供应商退货
                $goodsNumInfo[1][2][3][$key] = 0;//供应商新增
                $sql37 = "SELECT ifnull( sum( a.num ), 0 ) num  FROM lkt_order_details a INNER JOIN lkt_product_list b ON a.p_id = b.id WHERE a.r_status IN ( 1, 2, 5, 7 ) AND b.gongyingshang != 0 AND b.store_id = '$store_id' AND b.mch_id = 0 AND DATE_FORMAT( a.add_time, '%Y-%m-%d' ) = '$value' ";
                $res37 =Db::query($sql37);
                if($res37)
                {
                    $goodsNumInfo[1][2][1][$key] = $res37[0]['num'];
                }
                $sql38 = "SELECT ifnull( sum( o.num ), 0 ) num FROM lkt_return_order a
                            LEFT JOIN lkt_product_list b ON a.pid = b.id
                            INNER JOIN lkt_order o ON a.sNo = o.sNo 
                            WHERE a.store_id ='$store_id' AND DATE_FORMAT( a.re_time, '%Y-%m-%d' )='$value' AND b.gongyingshang != 0 AND b.mch_id = 0";
                $res38 = Db::query($sql38);
                if($res38)
                {
                    $goodsNumInfo[1][2][2][$key] = $res38[0]['num'];
                }
                $sql39 = "SELECT ifnull( count( 1 ), 0 ) num FROM lkt_product_list WHERE DATE_FORMAT( add_date, '%Y-%m-%d' )='$value' AND store_id ='$store_id' AND mch_id = 0 AND gongyingshang != 0";
                $res39 = Db::query($sql39);
                if($res39)
                {
                    $goodsNumInfo[1][2][3][$key] = $res39[0]['num'];
                }
            }

            //近一年
            foreach ($date_year as $key => $value) 
            {
                $goodsNumInfo[2][0][0][$key] = $value;
                $goodsNumInfo[2][0][1][$key] = 0;//商城已售
                $goodsNumInfo[2][0][2][$key] = 0;//商城退货
                $goodsNumInfo[2][0][3][$key] = 0;//商城新增
                $sql40 = "SELECT ifnull( sum( a.num ), 0 ) num FROM lkt_order_details a
                            INNER JOIN lkt_product_list b ON a.p_id = b.id 
                        WHERE a.r_status IN ( 1, 2, 5, 7 ) AND b.mch_id ='$mch_id' AND ( b.gongyingshang = 0 OR b.gongyingshang IS NULL ) AND b.store_id ='$store_id' AND DATE_FORMAT( a.add_time, '%Y-%m' ) = '$value'";
                $res40 = Db::query($sql40);
                if($res40)
                {
                    $goodsNumInfo[2][0][1][$key] = $res40[0]['num'];
                }
                $sql41 = "SELECT ifnull( sum( o.num ), 0 ) num FROM lkt_return_order a
                            LEFT JOIN lkt_product_list b ON a.pid = b.id
                            INNER JOIN lkt_order o ON a.sNo = o.sNo 
                        WHERE a.store_id ='$store_id' AND DATE_FORMAT( a.re_time, '%Y-%m' )='$value' AND b.mch_id ='$mch_id' AND ( b.gongyingshang = 0 OR b.gongyingshang IS NULL )";
                $res41 = Db::query($sql41);
                if($res41)
                {
                    $goodsNumInfo[2][0][2][$key] = $res41[0]['num'];
                }
                $sql42 = "SELECT ifnull( count( 1 ), 0 ) num FROM lkt_product_list WHERE DATE_FORMAT( add_date, '%Y-%m' )='$value' AND store_id ='$store_id' AND mch_id ='$mch_id' AND ( gongyingshang = 0 OR gongyingshang IS NULL )";
                $res42 = Db::query($sql42);
                if($res42)
                {
                    $goodsNumInfo[2][0][3][$key] = $res42[0]['num'];
                }

                $goodsNumInfo[2][1][0][$key] = $value;
                $goodsNumInfo[2][1][1][$key] = 0;//店铺已售
                $goodsNumInfo[2][1][2][$key] = 0;//店铺退货
                $goodsNumInfo[2][1][3][$key] = 0;//店铺新增
                $sql43 = "SELECT ifnull( sum( a.num ), 0 ) num FROM lkt_order_details a
                            INNER JOIN lkt_product_list b ON a.p_id = b.id 
                        WHERE a.r_status IN ( 1, 2, 5, 7 ) AND b.mch_id !='$mch_id' AND ( b.gongyingshang = 0 OR b.gongyingshang IS NULL ) AND b.store_id ='$store_id' AND DATE_FORMAT( a.add_time, '%Y-%m' ) = '$value'";
                $res43 = Db::query($sql43);
                if($res43)
                {
                    $goodsNumInfo[2][1][1][$key] = $res43[0]['num'];
                }
                $sql44 = "SELECT ifnull( sum( o.num ), 0 ) num FROM lkt_return_order a
                            LEFT JOIN lkt_product_list b ON a.pid = b.id
                            INNER JOIN lkt_order o ON a.sNo = o.sNo 
                        WHERE a.store_id ='$store_id' AND DATE_FORMAT( a.re_time, '%Y-%m-%d' )='$value' AND b.mch_id !='$mch_id' AND ( b.gongyingshang = 0 OR b.gongyingshang IS NULL )";
                $res44 = Db::query($sql44);
                if($res44)
                {
                    $goodsNumInfo[2][1][2][$key] = $res44[0]['num'];
                }
                $sql45 = "SELECT ifnull( count( 1 ), 0 ) num FROM lkt_product_list WHERE DATE_FORMAT( add_date, '%Y-%m' )='$value' AND store_id ='$store_id' AND mch_id !='$mch_id' AND ( gongyingshang = 0 OR gongyingshang IS NULL )";
                $res45 = Db::query($sql45);
                if($res45)
                {
                    $goodsNumInfo[2][1][3][$key] = $res45[0]['num'];
                }

                $goodsNumInfo[2][2][0][$key] = $value;
                $goodsNumInfo[2][2][1][$key] = 0;//供应商已售
                $goodsNumInfo[2][2][2][$key] = 0;//供应商退货
                $goodsNumInfo[2][2][3][$key] = 0;//供应商新增
                $sql46 = "SELECT ifnull( sum( a.num ), 0 ) num  FROM lkt_order_details a INNER JOIN lkt_product_list b ON a.p_id = b.id WHERE a.r_status IN ( 1, 2, 5, 7 ) AND b.gongyingshang != 0 AND b.store_id = '$store_id' AND b.mch_id = 0 AND DATE_FORMAT( a.add_time, '%Y-%m' ) = '$value' ";
                $res46 =Db::query($sql46);
                if($res46)
                {
                    $goodsNumInfo[2][2][1][$key] = $res46[0]['num'];
                }
                $sql47 = "SELECT ifnull( sum( o.num ), 0 ) num FROM lkt_return_order a
                            LEFT JOIN lkt_product_list b ON a.pid = b.id
                            INNER JOIN lkt_order o ON a.sNo = o.sNo 
                            WHERE a.store_id ='$store_id' AND DATE_FORMAT( a.re_time, '%Y-%m' )='$value' AND b.gongyingshang != 0 AND b.mch_id = 0";
                $res47 = Db::query($sql47);
                if($res47)
                {
                    $goodsNumInfo[2][2][2][$key] = $res47[0]['num'];
                }
                $sql48 = "SELECT ifnull( count( 1 ), 0 ) num FROM lkt_product_list WHERE DATE_FORMAT( add_date, '%Y-%m' )='$value' AND store_id ='$store_id' AND mch_id = 0 AND gongyingshang != 0";
                $res48 = Db::query($sql48);
                if($res48)
                {
                    $goodsNumInfo[2][2][3][$key] = $res48[0]['num'];
                }
            }

            $data = array('countList'=>$countList,'goodsNumInfo'=>$goodsNumInfo,'goodsSalesInfo'=>$goodsSalesInfo,'goodsSalesInfoWithStatus'=>$goodsSalesInfoWithStatus,'skuInfo'=>$skuInfo);
            $message = Lang("Success");
            return output(200,$message,$data);
    }

    //库存
    public function stockRecord()
    {
            $store_id = addslashes(trim($this->request->param('storeId')));
            $store_type = addslashes(trim($this->request->param('storeType')));
            $access_id = addslashes(trim($this->request->param('accessId')));
            $mch_id = cache($access_id.'_'.$store_type);

            $type = addslashes(trim($this->request->param('type')));//week 周 month 月 year 年
            $page = addslashes(trim($this->request->param('pageNo')));
            $pagesize = addslashes(trim($this->request->param('pageSize')));
            if ($page)
            {
                $page = $page;
                $start = ($page - 1) * $pagesize;
            }
            else
            {
                $page = 1;
                $start = 0;
            }
            //状态查询
            $mon = date("Y-m");//当前月份
            //得到系统的年月
            $tmp_date = date("Ym");
            //切割出年份
            $tmp_year = substr($tmp_date, 0, 4);
            //切割出月份
            $tmp_mon = substr($tmp_date, 4, 2);
            $tmp_forwardmonth = mktime(0, 0, 0, $tmp_mon - 1, 1, $tmp_year);
            //得到当前月的上一个月
            $lastmon = date("Y-m", $tmp_forwardmonth);
            //今天
            $today = date("Y-m-d");
            $day_1 = date("Y-m-d", strtotime("-1 day"));//昨天
            $week = date("Y-m-d", strtotime("-6 day"));
            $month = date("Y-m-d", strtotime("-29 day"));
            $year = date("Y-m-d", strtotime("-364 day"));

            $stockInfo = array();
            $stockRecord = array();
            //总数
            $stockRecord['total'] = 0;
            $list = array();
            switch ($type) 
            {
                case 'week':
                    //入库数量
                    $sql0 = "SELECT ifnull( sum( flowing_num ), 0 ) num FROM lkt_stock WHERE store_id ='$store_id' AND add_date >= '$week' AND type = 0";
                    //出库数量
                    $sql1 = "SELECT ifnull( sum( flowing_num ), 0 ) num FROM lkt_stock WHERE store_id ='$store_id' AND type = 1 AND add_date >= '$week'";
                    //预警次数
                    $sql2 = "SELECT ifnull( count( 1 ), 0 ) num FROM lkt_stock WHERE store_id ='$store_id' AND add_date >='$week' AND type = 2";
                    //总数
                    $sql3 = "SELECT DISTINCT COUNT( a.id ) num FROM lkt_configure a
                                LEFT JOIN lkt_product_list b ON a.pid = b.id
                                INNER JOIN lkt_stock s ON a.id = s.attribute_id AND a.pid = product_id 
                                WHERE b.store_id = '$store_id' AND s.type = 2 AND s.add_date >= '$week' AND a.recycle = 0 AND b.recycle = 0";

                    //表单数据
                    $sql4 = "SELECT a.id,b.id product_number,b.product_title,a.img,a.total_num,s.add_date warnDate,a.num,a.attribute FROM lkt_configure a
                                LEFT JOIN lkt_product_list b ON a.pid = b.id
                                INNER JOIN lkt_stock s ON a.id = s.attribute_id AND a.pid = product_id 
                                WHERE b.store_id = '$store_id' AND s.type = 2 AND s.add_date >= '$week' AND a.recycle = 0 AND b.recycle = 0 ORDER BY a.num ASC LIMIT $start,$pagesize";

                    break;
                
                case 'year':
                    //入库数量
                    $sql0 = "SELECT ifnull( sum( flowing_num ), 0 ) num FROM lkt_stock WHERE store_id ='$store_id' AND add_date >= '$year' AND type = 0";
                    //出库数量
                    $sql1 = "SELECT ifnull( sum( flowing_num ), 0 ) num FROM lkt_stock WHERE store_id ='$store_id' AND type = 1 AND add_date >= '$year'";
                    //预警次数
                    $sql2 = "SELECT ifnull( count( 1 ), 0 ) num FROM lkt_stock WHERE store_id ='$store_id' AND add_date >='$year' AND type = 2";
                    //总数
                    $sql3 = "SELECT DISTINCT COUNT( a.id ) num FROM lkt_configure a
                                LEFT JOIN lkt_product_list b ON a.pid = b.id
                                INNER JOIN lkt_stock s ON a.id = s.attribute_id AND a.pid = product_id 
                                WHERE b.store_id = '$store_id' AND s.type = 2 AND s.add_date >= '$year' AND a.recycle = 0 AND b.recycle = 0";

                    //表单数据
                    $sql4 = "SELECT a.id,b.id product_number,b.product_title,a.img,a.total_num,s.add_date warnDate,a.num,a.attribute FROM lkt_configure a
                                LEFT JOIN lkt_product_list b ON a.pid = b.id
                                INNER JOIN lkt_stock s ON a.id = s.attribute_id AND a.pid = product_id 
                                WHERE b.store_id = '$store_id' AND s.type = 2 AND s.add_date >= '$year' AND a.recycle = 0 AND b.recycle = 0 ORDER BY a.num ASC LIMIT $start,$pagesize";

                    break;

                default :
                    //入库数量
                    $sql0 = "SELECT ifnull( sum( flowing_num ), 0 ) num FROM lkt_stock WHERE store_id ='$store_id' AND add_date >= '$month' AND type = 0";
                    //出库数量
                    $sql1 = "SELECT ifnull( sum( flowing_num ), 0 ) num FROM lkt_stock WHERE store_id ='$store_id' AND type = 1 AND add_date >= '$month'";
                    //预警次数
                    $sql2 = "SELECT ifnull( count( 1 ), 0 ) num FROM lkt_stock WHERE store_id ='$store_id' AND add_date >='$month' AND type = 2";
                    
                    $sql3 = "SELECT DISTINCT COUNT( a.id ) num FROM lkt_configure a
                                LEFT JOIN lkt_product_list b ON a.pid = b.id
                                INNER JOIN lkt_stock s ON a.id = s.attribute_id AND a.pid = product_id 
                                WHERE b.store_id = '$store_id' AND s.type = 2 AND s.add_date >= '$month' AND a.recycle = 0 AND b.recycle = 0";

                    //表单数据
                    $sql4 = "SELECT a.id,b.id product_number,b.product_title,a.img,a.total_num,s.add_date warnDate,a.num,a.attribute FROM lkt_configure a
                                LEFT JOIN lkt_product_list b ON a.pid = b.id
                                INNER JOIN lkt_stock s ON a.id = s.attribute_id AND a.pid = product_id 
                                WHERE b.store_id = '$store_id' AND s.type = 2 AND s.add_date >= '$month' AND a.recycle = 0 AND b.recycle = 0 ORDER BY a.num ASC LIMIT $start,$pagesize";
                    break;
            }
            $res0 = Db::query($sql0);
            if($res0)
            {
                $stockInfo[0] = $res0[0]['num'];//入库数量
            }
            else
            {
                $stockInfo[0] = 0;
            }
            $res1 = Db::query($sql1);
            if($res1)
            {
                $stockInfo[1] = $res1[0]['num'];//出库数量
            }
            else
            {
                $stockInfo[1] = 0;
            }
            $res2 = Db::query($sql2);
            if($res2)
            {
                $stockInfo[2] = $res2[0]['num'];//预警次数
            }
            else
            {
                $stockInfo[2] = 0;
            }

            $res3 = Db::query($sql3);
            if($res3)
            {
                $stockRecord['total'] = $res3[0]['num'];
            }

            $res4 = Db::query($sql4);
            if($res4)
            {
                foreach ($res4 as $key => $value) 
                {
                    $res4[$key]['img'] = ServerPath::getimgpath($value['img'],$store_id);
                    $attribute = unserialize($value['attribute']);
                    $attributes = array();
                    $res4[$key]['config'] = '';
                    foreach ($attribute as $k => $v)
                    {
                        if (strpos($k, '_LKT_') !== false)
                        {
                            $k = substr($k, 0, strrpos($k, "_LKT"));
                            $v = substr($v, 0, strrpos($v, "_LKT"));
                        }
                        $res4[$key]['config'] .= $k . ":" . $v . "  ";
                    }
                }
                $list = $res4;
            }
            $stockRecord['data'] = $list;
            $data = array('stockRecord'=>$stockRecord,'stockInfo'=>$stockInfo);
            $message = Lang("Success");
            return output(200,$message,$data);
    }

    // 日志
    public function Log($Log_content)
    {
        $lktlog = new LaiKeLogUtils();
        $lktlog->log("admin/express.log",$Log_content);
        return;
    }

    /**
     * 获取每个月每周开始与结束时间
    */
    public function monthPerWeek($date,$num)
    {
        $today = date('Y-m-d',strtotime($date));
        $year = date('Y', strtotime($today));  //获取年
        $month = date('m', strtotime($today));  //获取月
        $day = date('d', strtotime($today));      //获取日

        $j = date("t",strtotime($today)); //获取当前月份天数
        $a = 1; //下标从1开始取 第一周
        $weekinfo = array();
        for ($i=1; $i <= $j ; $i=$i+7) 
        {   //循环本月有多少周
            $w = date('N',strtotime($year.'-'.$month.'-'.$i));  //计算第一天是周几
            // 第一天
            if($i == 1)
            {
                $start = strtotime($year.'-'.$month.'-'.$i);
            }
            else
            {
                $start = strtotime($year.'-'.$month.'-'.$i.' -'.($w-1).' days');
            }
            // 最后一天
            if($j-$i >= 7)
            {
                $end = strtotime($year.'-'.$month.'-'.$i.' +'.(7-$w).' days');
            }
            else
            {
                $end = strtotime($year.'-'.$month.'-'.$i.' +'.($w).' days');
            }

            $weekinfo[$a] = array(date('Y-m-d',$start),date('Y-m-d 23:59:59',$end));
            $a++; 
        }
        return $weekinfo[$num];
    }
}