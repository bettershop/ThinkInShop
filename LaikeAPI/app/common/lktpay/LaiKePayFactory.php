<?php
/**
 *
 */
class LaiKePayFactory{

    /**
     * 获取支付类型
     * @param $type
     * @return mixed
     * @throws Exception
     */
    public static function getPayBean($type){
        if (!empty($type)){
            return new $type();
        }
        throw  new Exception("无效的支付类型：".$type,501);
    }

}