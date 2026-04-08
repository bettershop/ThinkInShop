<?php
namespace app\task;

use think\facade\Db;
use yunwuxin\cron\Task;

use app\common\BackupController;

class DemoTask extends Task
{
    public function configure()
    {
        $time = date('Y-m-d H:i:s');
        // 查询数据备份设置
        $sql0 = "select * from lkt_backup_config where store_id = 0 ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $is_open = $r0[0]['is_open'];
            $query_data = $r0[0]['query_data']; // 1.每天 2.N天 3.每小时 4.N小时 5.N分钟 6.每周 7.每月
            $execute_cycle = $r0[0]['execute_cycle'];
            $url = $r0[0]['url'];
            if($is_open == 1)
            {
                $execute_cycle_list = explode(' ',$execute_cycle);

                $second = $execute_cycle_list[0]; // 秒
                $minute = $execute_cycle_list[1]; // 分
                $hour = $execute_cycle_list[2]; // 小时
                $day = $execute_cycle_list[3]; // 天
                $month = $execute_cycle_list[4]; // 月
                $week = $execute_cycle_list[5]; // 周几
                file_put_contents("./1.log",'类型：' . $query_data . '；时间：' . $time . PHP_EOL,FILE_APPEND);
                file_put_contents("./1.log",$week . '周；' . $month . '月；'  . $day . '天；' . $hour . '小时；' . $minute . '分；' . PHP_EOL,FILE_APPEND);
                if($query_data == 1)
                { // 每天
                    $segments = array($minute,$hour,$day,$month,$week);
                    $segments1 = implode(' ', $segments);
                    file_put_contents("./1.log",'表达式：' . $segments1 . PHP_EOL,FILE_APPEND);
                    $this->expression($segments1);
                }
                else if($query_data == 2)
                { // N天
                    $segments = array($minute,$hour,'*/' . $day,$month,$week);
                    $segments1 = implode(' ', $segments);
                    file_put_contents("./1.log",'表达式：' . $segments1 . PHP_EOL,FILE_APPEND);
                    $this->expression($segments1);
                }
                else if($query_data == 3)
                { // 每小时
                    $segments = array($minute,$hour,$day,$month,$week);
                    $segments1 = implode(' ', $segments);
                    file_put_contents("./1.log",'表达式：' . $segments1 . PHP_EOL,FILE_APPEND);
                    $this->expression($segments1);
                }
                else if($query_data == 4)
                { // N小时
                    $segments = array($minute,'*/' . $hour,$day,$month,$week);
                    $segments1 = implode(' ', $segments);
                    file_put_contents("./1.log",'表达式：' . $segments1 . PHP_EOL,FILE_APPEND);
                    $this->expression($segments1);
                }
                else if($query_data == 5)
                { // N分钟
                    $segments = array('*/' . $minute,$hour,$day,$month,$week);
                    $segments1 = implode(' ', $segments);
                    file_put_contents("./1.log",'表达式：' . $segments1 . PHP_EOL,FILE_APPEND);
                    $this->expression($segments1);
                }
                else if($query_data == 6)
                { // 每周
                    $time = $hour . ':' . $minute;
                    $this->weeklyOn($week,$time); // 指定每周的时间执行
                }
                else if($query_data == 7)
                { // 每月
                    $time = $hour . ':' . $minute;
                    $this->monthlyOn($day,$time); // 指定每月的执行时间
                }
            }
        }
    }
    /**
     * 执行任务
     * @return mixed
     */
    protected function execute()
    {
        //...具体的任务执行
        $time = date('Y-m-d H:i:s');
        file_put_contents("./1.log","备份开始" .$time . PHP_EOL,FILE_APPEND);
        
        $array = array('store_id'=>0,'file_type'=>'定时任务');
        $res = new BackupController();
        $res->backup($array);
        
        file_put_contents("./1.log","备份结束" .$time . PHP_EOL,FILE_APPEND);
        return true;
    }
}
