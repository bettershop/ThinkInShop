<?php

namespace app\admin\model;

use think\Model;

class GroupGoodsModel extends Model
{ 

    // 设置当前模型对应的完整数据表名称
    protected $table = 'lkt_group_goods';
    
    // 设置当前模型的数据库连接
    protected $connection = 'laiketui';

	// 模型初始化
    protected static function init()
    {
        //TODO:初始化内容
    }
}
