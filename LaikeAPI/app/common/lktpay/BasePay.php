<?php
abstract class BasePay{

    /**
     * 支付方法
     * @param $params
     * @return mixed
     */
    abstract function pay($params);


    /**
     * 支付前
     * @param $params
     * @return mixed
     */
    abstract function beforPay($params);

    /**
     * 支付后
     * @param $params
     * @return mixed
     */
    abstract function afterPay($params);

}