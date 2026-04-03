<?php

namespace app\admin\model;

use think\Model;

class ImgGroupModel extends Model
{ 

    // 设置当前模型对应的完整数据表名称
    protected $table = 'lkt_img_group';
    
    // 设置当前模型的数据库连接
    protected $connection = 'laiketui';

	// 模型初始化
    protected static function init()
    {
        //TODO:初始化内容
    }
}
