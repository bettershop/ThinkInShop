<?php
namespace app\admin\controller\plugin\template;

use app\BaseController;
use app\common;
use think\facade\Db;
use think\facade\Request;
use app\common\Plugin\Plugin;
use app\common\ServerPath;
use app\common\LaiKeLogUtils;
use app\common\Jurisdiction;
use app\common\Tools;
use app\common\PC_Tools;

use app\admin\model\DiyModel;
use app\admin\model\ConfigModel;
use app\admin\model\BannerModel;
use app\admin\model\JumpPathModel;
use app\admin\model\ProductClassModel;
use app\admin\model\ProductListModel;
use app\admin\model\MchModel;
use app\admin\model\UiNavigationBarModel;
use app\admin\model\ActivityModel;
use app\admin\model\AdminModel;
use app\admin\model\ConfigureModel;
use app\admin\model\ActivityProModel;

/**
 * 功能：模板管理
 * 修改人：DHB
 */
class AdminDiy extends BaseController
{   
    // 获取H5域名
    public function Index()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $name  = '默认模板'; // 当前应用名称
        $H5_domain = '';
        $list = array();

        $r_config = ConfigModel::where(['store_id'=>$store_id])->field('H5_domain')->select()->toArray();
        if($r_config)
        {
            $H5_domain = $r_config[0]['H5_domain'];
        }

        $data = array('H5_domain' => $H5_domain,'list' => $list,'name' => $name,'status' => 0,'use_id' => '');
        $message = Lang('Success');
        return output(200,$message, $data);
    }

    // 获取diy数据
    public function GetDiyList()
    {
        $store_id = $this->request->param('storeId');
        $id = $this->request->param('id');
        $theme_type = $this->request->param('theme_type'); // 1.系统主题 2.自定义主题
        $theme_type_code = $this->request->param('theme_type_code');

        $details = array();
        $arr = Tools::get_data_dictionary(array('name'=>'DIY主题','lang_code'=>''));
        if($arr != array())
        {
            foreach($arr as $k => $v)
            {
                $code = $v['code'];
                $sql_0 = "select ifnull(count(id),0) as total from lkt_diy where store_id = '$store_id' and mch_id = 0 and is_del = 0 and theme_type = '$theme_type' and theme_dict_code = '$code' ";
                $r_0 = Db::query($sql_0);
                if($r_0)
                {
                    $v['count'] = $r_0[0]['total'];
                }
                $details[] = $v;
            }
        }

        $list = array();
        $total = 0;
        $where_list = array('store_id'=>$store_id,'is_del'=>0,'mch_id'=>0);
        if($id != '' && $id != 0)
        {
            $where_list['id'] = $id;
        }
        if($theme_type != '')
        {
            $where_list['theme_type'] = $theme_type;
        }
        if($theme_type_code != '')
        {
            $where_list['theme_dict_code'] = $theme_type_code;
        }

        $r0 = DiyModel::where($where_list)->field('count(id) as total')->select()->toArray();
        if($r0)
        {
            $total = $r0[0]['total'];
        }

        $r1 = DiyModel::where($where_list)->select()->toArray();
        if($r1)
        {
            $list = $r1;
        }

        $data = array('total'=>$total,'list'=>$list,'details'=>$details);
        $message = Lang("Success");
        return output("200",$message,$data);
    }

    // 获取路径
    public function BannerPathList()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $type0 = trim($this->request->param('type'));

        $where_list = array('store_id'=>$store_id,'type0'=>$type0,'status'=>1,'type'=>1);
        $list = array();
        $total = 0;

        $r0 = JumpPathModel::where($where_list)->select()->toArray();
        if($r0)
        {
            foreach($r0 as $k => $v)
            {
                $parameter = explode('=',$v['parameter'])[1];
                if($v['type0'] == 1)
                {
                    $r1 = ProductClassModel::where(['cid'=>$parameter,'recycle'=>0])->field('cid')->select()->toArray();
                }
                else if($v['type0'] == 2)
                {
                    $r1 = ProductListModel::where(['id'=>$parameter,'recycle'=>0,'mch_status'=>2,'status'=>2])->field('id')->select()->toArray();
                }
                else if($v['type0'] == 3)
                {
                    $r1 = MchModel::where(['id'=>$parameter,'recovery'=>0,'review_status'=>1])->field('id')->select()->toArray();
                }
                if($r1)
                {
                    $list[] = $v;
                }
            }
        }

        $data = array('list' => $list);
        $message = Lang('Success');
        return output(200,$message, $data);
    }

    // 获取页面数据
    public function getBindPageList()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $id = trim($this->request->param('id'));

        $list = array();
        $sql = "select a.bind_id,a.id,a.link_key,a.page_name,a.link,a.page_key,a.image,a.page_context,a.status,a.recycle,a.create_time,a.create_by,a.update_time from (select b.id AS bind_id,b.link_key,p.*,ROW_NUMBER() over ( PARTITION BY b.diy_page_id ) AS top from lkt_diy_page_bind b left join lkt_diy_page p on p.id = b.diy_page_id where b.diy_id = '$id') AS a WHERE a.top = 1";
        $r = Db::query($sql);
        if($r)
        {
            $list = $r;
        }

        $data = array('list' => $list);
        $message = Lang('Success');
        return output(200,$message, $data);
    }

    // 添加页面
    public function addOrUpdateDiyPage()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $id = trim($this->request->param('id')); // 页面ID
        $page_name = trim($this->request->param('page_name')); // 页面名称
        $link = trim($this->request->param('link')); // 链接
        $page_key = trim($this->request->param('page_key')); // 页面对应的key
        $type = trim($this->request->param('page_type')); // 类型 1：系统页面 2：自定义页面
        $image = trim($this->request->param('url')); // 
        $page_context = trim($this->request->param('page_context')); // 页面内容

        $JurisdictionAction = new Jurisdiction();
        $admin_name = $this->user_list['name'];
        $time = date("Y-m-d H:i:s");

        if($id != '')
        {
            $sql0 = "update lkt_diy_page set image = '$image',page_context = '$page_context',update_time = '$time' where id = '$id' ";
            $r0 = Db::execute($sql0);
            if($r0 == -1)
            {
                $JurisdictionAction->admin_record($store_id, $admin_name, ' 编辑页面失败！', 2);
                $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 编辑页面失败！sql: ' . $sql0;
                $this->Log($Log_content);
                $message = Lang('operation failed');
                return output(109,$message);
            }
            else
            {
                $JurisdictionAction->admin_record($store_id, $admin_name, '编辑页面成功！', 2);
                $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 编辑页面成功！';
                $this->Log($Log_content);
                $message = Lang('Success');
                return output(200,$message);
            }
        }
        else
        {
            $sql0 = "select id from lkt_diy_page where store_id = '$store_id' and type = '$type' and page_name = '$page_name' and recycle = 0 ";
            $r0 = Db::query($sql0);
            if($r0)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 页面名称已存在！';
                $this->Log($Log_content);
                $message = Lang('template.18');
                return output(109,$message);
            }
    
            $sql1 = array('store_id'=>$store_id,'page_name'=>$page_name,'type'=>$type,'link'=>$link,'page_key'=>$page_key,'create_time'=>$time,'create_by'=>$admin_name,'update_time'=>$time);
            $r1 = Db::name('diy_page')->insertGetId($sql1);
            if($r1 > 0)
            {
                $JurisdictionAction->admin_record($store_id, $admin_name, '添加页面成功！', 1);
                $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 添加页面成功！';
                $this->Log($Log_content);
                $data = array('id' => $r1);
                $message = Lang('Success');
                return output(200,$message, $data);
            }
            else
            {
                $JurisdictionAction->admin_record($store_id, $admin_name, ' 添加页面失败！', 1);
                $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 添加页面失败！参数: ' . json_encode($sql1);
                $this->Log($Log_content);
                $message = Lang('operation failed');
                return output(109,$message);
            }
        }
    }

    // 页面管理
    public function getDiyPageList()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $name = trim($this->request->param('name'));
        $status = trim($this->request->param('status'));
        $page = trim($this->request->param('pageNo'));
        $pagesize = trim($this->request->param('pageSize'));
        $pagesize = $pagesize ? $pagesize : '10';

        $start = 0;
        if ($page)
        {
            $start = ($page - 1) * $pagesize;
        }
        $list = array();
        $total = 0;

        $condition = "store_id = '$store_id' and recycle = 0 ";
        if($name != '')
        {
            $condition .= " and page_name like '%$name%' ";
        }
        if($status != '')
        {
            $condition .= " and status = '$status' ";
        }

        $sql = "select * from lkt_diy_page where $condition order by id desc limit $start,$pagesize";
        $r = Db::query($sql);
        if($r)
        {
            foreach($r as $k => $v)
            {
                if($v['image'] == '')
                {
                    unset($v['image']);
                }
                if($v['page_context'] == '')
                {
                    unset($v['page_context']);
                }
                $list[] = $v;
            }
        }

        $data = array('total' => $total,'list' => $list);
        $message = Lang('Success');
        return output(200,$message, $data);
    }

    // 编辑页面
    public function getDiyPageById()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $id = trim($this->request->param('id'));
        $link = trim($this->request->param('link'));

        $bindDiyList = array();
        $model = array();

        $sql0 = "select * from lkt_diy_page where store_id = '$store_id' and recycle = 0 and link = '$link' and id = '$id'";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $model = $r0;
        }

        $sql1 = "select * from lkt_diy_page_bind where diy_page_id = '$id' ";
        $r1 = Db::query($sql1);
        if($r1)
        {
            $bindDiyList = $r1;
        }

        $data = array('bindDiyList' => $bindDiyList,'model' => $model);
        $message = Lang('Success');
        return output(200,$message, $data);
    }

    // 删除页面
    public function delDiyPage()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $id = trim($this->request->param('id'));

        $JurisdictionAction = new Jurisdiction();
        $admin_name = $this->user_list['name'];

        $sql0 = "delete from lkt_diy_page where id = '$id' ";
        $r0 = Db::execute($sql0);
        if($r0 > 0)
        {
            $JurisdictionAction->admin_record($store_id, $admin_name, '删除页面成功！', 3);
            $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 删除页面成功！';
            $this->Log($Log_content);
            $message = Lang('Success');
            return output(200,$message);
        }
        else
        {
            $JurisdictionAction->admin_record($store_id, $admin_name, ' 删除页面失败！', 3);
            $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 删除页面失败！sql: ' . $sql0;
            $this->Log($Log_content);
            $message = Lang('operation failed');
            return output(109,$message);
        }
    }

    // 获取页面数据
    public function getPageJson()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $id = trim($this->request->param('diyId'));
        $link = trim($this->request->param('link'));

        $data = array();
        $sql0 = "select * from lkt_diy_page where link = '$link' ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $data = $r0[0];
        }

        $message = Lang('Success');
        return output(200,$message,$data);
    }

    // 关联主题
    public function getDiyPageBindList()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $id = trim($this->request->param('id'));
        $name = trim($this->request->param('name'));
        $page = trim($this->request->param('pageNo'));
        $pagesize = trim($this->request->param('pageSize'));
        $pagesize = $pagesize ? $pagesize : '10';

        $start = 0;
        if ($page)
        {
            $start = ($page - 1) * $pagesize;
        }

        $condition = " b.store_id = '$store_id' and a.diy_page_id = '$id' ";
        if ($name != '')
        {
            $condition .= " and b.name like '%$name%' ";
        }

        $list = array();
        $total = 0;
        $sql0 = "select count(a.id) as total from lkt_diy_page_bind as a left join lkt_diy as b on a.diy_id = b.id where $condition ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $total = $r0[0]['total'];
        }

        $sql0 = "select a.id as bind_id,a.diy_id,a.diy_page_id,a.bind_time,b.name,b.cover from lkt_diy_page_bind as a left join lkt_diy_page as b on a.diy_id = b.id where $condition ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $list = $r0;
        }

        $data = array('total'=>$total,'list'=>$list);
        $message = Lang('Success');
        return output(200,$message,$data);
    }

    // 取消关联
    public function delBindDiyPage()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $diy_id = trim($this->request->param('diy_id'));
        $diy_page_id = trim($this->request->param('diy_page_id'));

        $JurisdictionAction = new Jurisdiction();
        $admin_name = $this->user_list['name'];
        Db::startTrans();

        // 根据DIY主键、页面ID，查询绑定关系
        $sql0 = "select * from lkt_diy_page_bind where diy_id = '$diy_id' and diy_page_id = '$diy_page_id' ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            // 根据DIY主键不同、页面ID，查询是否有其它的绑定关系
            $sql0_0 = "select * from lkt_diy_page_bind where diy_id != '$diy_id' and diy_page_id = '$diy_page_id' ";
            $r0_0 = Db::query($sql0_0);
            if(empty($r0_0))
            { // 不存在(把该页面改成未使用)
                $sql0_1 = "update lkt_diy_page set status = 0 where id = '$diy_page_id' ";
                $r0_1 = Db::execute($sql0_1);
                if($r0_1 == -1)
                {
                    $JurisdictionAction->admin_record($store_id, $admin_name, '取消关联失败！', 3);
                    $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 取消关联失败！';
                    $this->Log($Log_content);
                    Db::rollback();
                    $message = Lang('operation failed');
                    return output(109,$message);
                }
            }

            $link = ""; // 链接
            // 根据页面ID，查询页面数据
            $sql1 = "select link from lkt_diy_page where id = '$diy_page_id' ";
            $r1 = Db::query($sql1);
            if($r1)
            {
                $link = $r1[0]['link']; // 链接
            }

            $value = "";
            // 根据页面ID，查询页面数据
            $sql1 = "select value from lkt_diy where id = '$diy_id' ";
            $r1 = Db::query($sql1);
            if($r1)
            {
                $value = $r1[0]['value']; // 页面数据

                if($link != '')
                {
                    $newvalue = str_replace($link, '', $value);

                    $sql2 = "update lkt_diy set value = '$newvalue' where id = '$diy_id' ";
                    $r2 = Db::execute($sql2);
                    if($r2 == -1)
                    {
                        $JurisdictionAction->admin_record($store_id, $admin_name, '取消关联失败！', 3);
                        $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 取消关联失败！';
                        $this->Log($Log_content);
                        Db::rollback();
                        $message = Lang('operation failed');
                        return output(109,$message);
                    }
                }
            }

            $sql3 = "delete from lkt_diy_page_bind where diy_id = '$diy_id' and diy_page_id = '$diy_page_id' ";
            $r3 = Db::execute($sql3);
            if($r3 < 1)
            {
                $JurisdictionAction->admin_record($store_id, $admin_name, ' 取消关联失败！', 3);
                $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 取消关联失败！sql: ' . $sql3;
                Db::rollback();
                $this->Log($Log_content);
                $message = Lang('operation failed');
                return output(109,$message);
            }
        }

        $JurisdictionAction->admin_record($store_id, $admin_name, '删除页面成功！', 3);
        $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 删除页面成功！';
        $this->Log($Log_content);
        Db::commit();
        $message = Lang('Success');
        return output(200,$message);
    }

    // 添加/编辑diy数据
    public function AddOrUpdateDiy()
    {
        $store_id = $this->request->param('storeId');
        $id = $this->request->param('id');
        $name = $this->request->param('name'); // 页面名称
        $remark = $this->request->param('remark'); // 备注
        $theme_type = $this->request->param('theme_type'); // 主题类型 1:系统主题 2:自定义主题
        $theme_dict_code = $this->request->param('theme_type_code'); // 主题类型code（字典code）
        $cover = $this->request->param('cover'); // 封面图
        $value = $this->request->param('value'); // 页面数据
        $tab_bar = $this->request->param('tabBar'); // 导航
        $tabber_info = $this->request->param('tabberinfo'); // 导航配置信息
        $createdFrom = $this->request->param('createdFrom'); // 
        $pageInfo = $this->request->param('pageInfo'); // 

        $JurisdictionAction = new Jurisdiction();
        $admin_name = $this->user_list['name'];

        if($id != '' && $id != 0)
        {
            $r0 = DiyModel::where(['store_id'=>$store_id,'name'=>$name])->where('id','<>',$id)->field('id')->select()->toArray();
        }
        else
        {
            $r0 = DiyModel::where(['store_id'=>$store_id,'name'=>$name])->field('id')->select()->toArray();
        }
        if($r0)
        {
            $message = Lang('template.14');
            return output(109,$message);
        }

        $current_time = time();
        $update_time = time();
        $time = date("Y-m-d H:i:s");

        if($id != '' && $id != 0)
        {
            $sql_where = array('store_id'=>$store_id,'id'=>$id);
            $sql_update = array('name'=>$name,'cover'=>$cover,'value'=>$value,'tab_bar'=>$tab_bar,'tabber_info'=>$tabber_info);
            $r = Db::name('diy')->where($sql_where)->update($sql_update);
            if ($r == -1)
            {
                $JurisdictionAction->admin_record($store_id, $admin_name, '修改diy配置失败！ID：' . $id, 2);
                $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 修改diy配置失败！条件参数：' . json_encode($sql_where) . '；修改参数：' . json_encode($sql_update);
                $lktlog->customerLog($Log_content);
                $message = Lang('template.15');
                return output(109,$message);
            }
            else
            {
                if($pageInfo != '')
                {
                    $pageInfo_list = json_decode($pageInfo,true);
                    foreach($pageInfo_list as $k => $v)
                    {
                        if(isset($v['dleLink']))
                        {
                            $dleLink = $v['dleLink']; // 删除大类

                            // 根据diy主键不是当前DIY主键、旧页面ID，查询是否有其它绑定关系
                            $sql_1 = "select diy_page_id from lkt_diy_page_bind where diy_id = '$id' and link_key = '$dleLink' ";
                            $r_1 = Db::query($sql_1);
                            if($r_1)
                            {
                                foreach($r_1 as $k_1 => $v_1)
                                {
                                    $diy_page_id = $v_1['diy_page_id']; // 页面ID
                                    // 根据key不同，页面ID，查询该页面是否绑定其它DIY
                                    $sql_1_1 = "select id from lkt_diy_page_bind where link_key != '$dleLink' and diy_page_id = '$diy_page_id' ";
                                    $r_1_1 = Db::query($sql_1_1);
                                    if($r_1_1)
                                    { // 存在其它DIY使用了该页面
                                        $status = 1;
                                    }
                                    else
                                    { // 不存在其它DIY使用了该页面
                                        $status = 0;
                                    }

                                    // 根据商城ID、页面ID，修改页面使用状态
                                    $sql_2 = "update lkt_diy_page set status = '$status' where store_id = '$store_id' and id = '$diy_page_id' ";
                                    $r_2 = Db::execute($sql_2);
                                }
                            }
                            // 根据diy主键、页面ID，删除绑定关系
                            $sql_3 = "delete from lkt_diy_page_bind where diy_id = '$id' and link_key = '$dleLink' ";
                            $r_3 = Db::execute($sql_3);
                        }
                        else
                        {
                            $link = $v['link']; // diy数据使用链接对应的json key
                            $unit = $v['unit']; // 子组件value
                            $lodValue = $v['lodValue']; // 旧链接
                            $value = $v['value']; // 新链接

                            if(isset($v['isDelete']) && $v['isDelete'])
                            {
                                if($lodValue != '')
                                { // 旧链接不为空
                                    // 根据商城ID、就链接，查询页面ID
                                    $sql_0 = "select id from lkt_diy_page where store_id = '$store_id' and link = '$lodValue' ";
                                    $r_0 = Db::query($sql_0);
                                    if($r_0)
                                    { // 旧数据存在
                                        $diy_page_id_0 = $r_0[0]['id']; // 旧页面ID

                                        // 根据diy主键不是当前DIY主键、旧页面ID，查询是否有其它绑定关系
                                        $sql_1 = "select id from lkt_diy_page_bind where diy_id != '$id' and diy_page_id = '$diy_page_id_0' ";
                                        $r_1 = Db::query($sql_1);
                                        if($r_1)
                                        { // 存在其它DIY使用了该页面
                                            $status = 1;
                                        }
                                        else
                                        { // 不存在其它DIY使用了该页面
                                            $status = 0;
                                        }

                                        // 根据商城ID、页面ID，修改页面使用状态
                                        $sql_2 = "update lkt_diy_page set status = '$status' where store_id = '$store_id' and id = '$diy_page_id_0' ";
                                        $r_2 = Db::execute($sql_2);

                                        // 根据diy主键、页面ID，删除绑定关系
                                        $sql_3 = "delete from lkt_diy_page_bind where diy_id = '$id' and diy_page_id = '$diy_page_id_0' ";
                                        $r_3 = Db::execute($sql_3);
                                    }
                                }
                            }
                            else
                            {
                                if($lodValue != $value)
                                {
                                    if($lodValue != '')
                                    { // 旧链接不为空
                                        // 根据商城ID、就链接，查询页面ID
                                        $sql_0 = "select id from lkt_diy_page where store_id = '$store_id' and link = '$lodValue' ";
                                        $r_0 = Db::query($sql_0);
                                        if($r_0)
                                        { // 旧数据存在
                                            $diy_page_id_0 = $r_0[0]['id']; // 旧页面ID

                                            // 根据diy主键不是当前DIY主键、旧页面ID，查询是否有其它绑定关系
                                            $sql_1 = "select id from lkt_diy_page_bind where diy_id != '$id' and diy_page_id = '$diy_page_id_0' ";
                                            $r_1 = Db::query($sql_1);
                                            if($r_1)
                                            { // 存在其它DIY使用了该页面
                                                $status = 1;
                                            }
                                            else
                                            { // 不存在其它DIY使用了该页面
                                                $status = 0;
                                            }

                                            // 根据商城ID、页面ID，修改页面使用状态
                                            $sql_2 = "update lkt_diy_page set status = '$status' where store_id = '$store_id' and id = '$diy_page_id_0' ";
                                            $r_2 = Db::execute($sql_2);

                                            // 根据diy主键、页面ID，删除绑定关系
                                            $sql_3 = "delete from lkt_diy_page_bind where diy_id = '$id' and diy_page_id = '$diy_page_id_0' ";
                                            $r_3 = Db::execute($sql_3);
                                        }
                                    }

                                    if($value != '')
                                    { // 新链接不为空
                                        // 根据商城ID、新链接，查询页面ID
                                        $sql_1 = "select id from lkt_diy_page where store_id = '$store_id' and link = '$value' ";
                                        $r_1 = Db::query($sql_1);
                                        if($r_1)
                                        { // 新数据存在
                                            $diy_page_id_1 = $r_1[0]['id']; // 新页面ID

                                            // 根据diy主键不是当前DIY主键、新页面ID，查询是否有其它绑定关系
                                            $sql_1 = "select id from lkt_diy_page_bind where diy_id = '$id' and diy_page_id = '$diy_page_id_1' ";
                                            $r_1 = Db::query($sql_1);
                                            if(empty($r_1))
                                            { // 不存在，添加绑定关系
                                                // 根据商城ID、页面ID，修改页面使用状态
                                                $sql_2 = "update lkt_diy_page set status = '1' where store_id = '$store_id' and id = '$diy_page_id_1' ";
                                                $r_2 = Db::execute($sql_2);

                                                // 添加绑定关系
                                                $sql_3 = array('diy_id'=>$id,'diy_page_id'=>$diy_page_id_1,'link_key'=>$link,'unit'=>$unit,'bind_time'=>$time);
                                                $r_3 = Db::name('diy_page_bind')->insertGetId($sql_3);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                $JurisdictionAction->admin_record($store_id, $admin_name, '修改diy配置成功！ID：' . $id, 2);
                $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 修改diy配置成功！';
                $this->Log($Log_content);
                $message = Lang('Success');
                return output(200,$message);
            }
        }
        else
        {
            $sql_inset = array('version'=>'1.0','name'=>$name,'value'=>'','add_time'=>$current_time,'update_time'=>$update_time,'status'=>0,'type'=>1,'is_del'=>0,'store_id'=>$store_id,'cover'=>'','remark'=>$remark,'theme_type'=>$theme_type,'theme_dict_code'=>$theme_dict_code);
            $res = Db::name('diy')->insertGetId($sql_inset);
            if ($res < 1) 
            {
                $JurisdictionAction->admin_record($store_id, $admin_name, '新增diy配置失败！', 2);
                $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 新增diy配置失败！参数：'.json_encode($sql_inset);
                $this->Log($Log_content);
                $message = Lang('template.16');
                return output(109,$message);
            } 
            else 
            {
                $data = array('id'=>$res);
                $JurisdictionAction->admin_record($store_id, $admin_name, '新增diy配置成功！', 1);
                $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 新增diy配置成功！';
                $this->Log($Log_content);
                $message = Lang("Success");
                return output("200",$message,$data);
            }
        }
    }

    // 删除diy数据
    public function DelDiy()
    {
        $store_id = $this->request->param('storeId');
        $id = $this->request->param('id');

        $JurisdictionAction = new Jurisdiction();
        $admin_name = $this->user_list['name'];

        $sql_where = array('store_id'=>$store_id,'id'=>$id);
        $sql_update = array('is_del'=>1);
        $r = Db::name('diy')->where($sql_where)->update($sql_update);
        if ($r == -1)
        {
            $JurisdictionAction->admin_record($store_id, $admin_name, '修改diy配置失败！ID：' . $id, 2);
            $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 修改diy配置失败！条件参数：' . json_encode($sql_where) . '；修改参数：' . json_encode($sql_update);
            $lktlog->customerLog($Log_content);
            $message = Lang('template.15');
            return output(109,$message);
        }
        else
        {
            $JurisdictionAction->admin_record($store_id, $admin_name, '修改diy配置成功！ID：' . $id, 2);
            $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 修改diy配置成功！';
            $this->Log($Log_content);
            $message = Lang('Success');
            return output(200,$message);
        }
    }

    // 应用diy
    public function DiyStatus()
    {
        $store_id = $this->request->param('storeId');
        $id = $this->request->param('id');

        $JurisdictionAction = new Jurisdiction();
        $admin_name = $this->user_list['name'];

        $sql_where0 = array('store_id'=>$store_id);
        $sql_update0 = array('status'=>0);
        $r0 = Db::name('diy')->where($sql_where0)->update($sql_update0);

        $sql_where = array('store_id'=>$store_id,'id'=>$id);
        $sql_update = array('status'=>1);
        $r = Db::name('diy')->where($sql_where)->update($sql_update);
        if ($r == -1)
        {
            $JurisdictionAction->admin_record($store_id, $admin_name, '修改diy配置失败！ID：' . $id, 2);
            $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 修改diy配置失败！条件参数：' . json_encode($sql_where) . '；修改参数：' . json_encode($sql_update);
            $lktlog->customerLog($Log_content);
            $message = Lang('template.15');
            return output(109,$message);
        }
        else
        {
            $JurisdictionAction->admin_record($store_id, $admin_name, '修改diy配置成功！ID：' . $id, 2);
            $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 修改diy配置成功！';
            $this->Log($Log_content);
            $message = Lang('Success');
            return output(200,$message);
        }
    }

    // 轮播图列表
    public function BannerIndex()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $page = trim($this->request->param('pageNo'));
        $pagesize = trim($this->request->param('pageSize'));
        $pagesize = $pagesize ? $pagesize : '10';

        $list = array();
        $total = 0;
        $r0 = BannerModel::where(['store_id'=>$store_id,'mch_id'=>0])->where('type','<>','4')->select()->toArray();
        if($r0)
        {
            $total = count($r0);
        }

        $r1 = BannerModel::where(['store_id'=>$store_id,'mch_id'=>0])->where('type','<>','4')->page($page,$pagesize)->order('sort','desc')->select()->toArray();
        if($r1)
        {
            foreach($r1 as $k => $v)
            {
                $v['image'] = ServerPath::getimgpath($v['image'], $store_id);
                $list[] = $v;
            }
        }

        $data = array('total' => $total,'list' => $list);
        $message = Lang('Success');
        return output(200,$message, $data);
    }

    // 添加/编辑轮播图
    public function BannerSave()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $id = trim($this->request->param('id')); // 轮播图ID
        $type0 = trim($this->request->param('type0')); // 类型 1.分类 2.商品 3.店铺 4.pc店铺 0=自定义
        $url = trim($this->request->param('url'));// 路径
        $image = trim($this->request->param('picUrl')); // 图片

        $JurisdictionAction = new Jurisdiction();
        $admin_name = $this->user_list['name'];

        if ($image)
        {
            $image = preg_replace('/.*\//', '', $image);
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 轮播图不能为空！';
            $this->Log($Log_content);
            $message = Lang('pc.0');
            return output(109,$message);
        }

        $time = date("Y-m-d H:i:s");
        if($id != '' && $id != 0)
        {
            $sql_where = array('store_id'=>$store_id,'id'=>$id);
            $sql_update = array('open_type'=>$type0,'image'=>$image,'url'=>$url,'add_date'=>$time);
            $r = Db::name('banner')->where($sql_where)->update($sql_update);
            if ($r == -1)
            {
                $JurisdictionAction->admin_record($store_id, $admin_name, ' 修改轮播图失败！ID：' . $id, 2);
                $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 修改轮播图失败！条件参数：' . json_encode($sql_where) . '；修改参数：' . json_encode($sql_update);
                $this->Log($Log_content);
                $message = Lang('pc.1');
                return output(109,$message);
            }
            else
            {
                $JurisdictionAction->admin_record($store_id, $admin_name, '修改轮播图成功！ID：' . $id, 2);
                $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 修改轮播图成功！';
                $this->Log($Log_content);
                $message = Lang('Success');
                return output(200,$message);
            }
        }
        else
        {
            // 查询最大的排序号
            $r_sort = BannerModel::where(['store_id'=>$store_id])->field('MAX(sort) as sort')->select()->toArray();
            $sort = $r_sort[0]['sort'];
            $sort = $sort + 1;

            $sql_inset = array('store_id'=>$store_id,'open_type'=>$type0,'image'=>$image,'url'=>$url,'sort'=>$sort,'type'=>0,'add_date'=>$time);
            $r = Db::name('banner')->insert($sql_inset);
            if ($r == -1)
            {
                $JurisdictionAction->admin_record($store_id, $admin_name, '添加轮播图失败！', 1);
                $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 添加轮播图失败！参数: ' . json_encode($sql_inset);
                $this->Log($Log_content);
                $message = Lang('pc.3');
                return output(109,$message);
            }
            else
            {
                $JurisdictionAction->admin_record($store_id, $admin_name, '添加轮播图成功！', 1);
                $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 添加轮播图成功！';
                $this->Log($Log_content);
                $message = Lang('Success');
                return output(200,$message);
            }
        }
    }

    // 上移/下移轮播图
    public function BannerRemove()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $id = trim($this->request->param('id')); // 互换排序的轮播图ID
        $id1 = trim($this->request->param('id1')); // 操作的轮播图ID

        $JurisdictionAction = new Jurisdiction();
        $admin_name = $this->user_list['name'];

        Db::startTrans();
        // 查询互换排序的轮播图ID的排序
        $r0 = BannerModel::where(['store_id'=>$store_id,'id'=>$id])->field('sort')->select()->toArray();
        $sort0 = $r0[0]['sort']; // 排序号

        // 操作的轮播图ID的排序
        $r1 = BannerModel::where(['store_id'=>$store_id,'id'=>$id1])->field('sort')->select()->toArray();
        $sort1 = $r1[0]['sort']; // 排序号

        if($sort0 == $sort1)
        {
            $sort0 = (int)$sort0 + 1;
        }

        // 修改当前轮播图排序
        $sql_where2 = array('store_id'=>$store_id,'id'=>$id);
        $sql_update2 = array('sort'=>$sort1);
        $r2 = Db::name('banner')->where($sql_where2)->update($sql_update2);
        
        // 修改上条数据轮播图排序
        $sql_where3 = array('store_id'=>$store_id,'id'=>$id1);
        $sql_update3 = array('sort'=>$sort0);
        $r3 = Db::name('banner')->where($sql_where3)->update($sql_update3);
        if ($r2 > -1 && $r3 > -1)
        {
            $JurisdictionAction->admin_record($store_id, $admin_name, ' 修改轮播图排序成功！参数：' . $id1, 7);
            $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 修改轮播图排序成功！';
            $this->Log($Log_content);
            Db::commit();
            $message = Lang('Success');
            return output(200,$message);
        }
        else
        {
            $JurisdictionAction->admin_record($store_id, $admin_name, '修改轮播图排序失败！参数：' . $id1, 7);
            $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 修改轮播图排序失败！参数：'.json_encode($sql_where2).'；参数：'.json_encode($sql_where3);
            $this->Log($Log_content);
            Db::rollback();
            $message = Lang('pc.1');
            return output(200,$message);
        }
    }

    // 置顶轮播图
    public function BannerMoveTop()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $id = trim($this->request->param('id')); // 轮播图ID

        $JurisdictionAction = new Jurisdiction();
        $admin_name = $this->user_list['name'];

        // 查询最大的排序号
        $r_sort = BannerModel::where(['store_id'=>$store_id])->field('MAX(sort) as sort')->select()->toArray();
        $sort = $r_sort[0]['sort'];
        $sort = $sort + 1;

        // 修改当前轮播图排序
        $sql_where = array('store_id'=>$store_id,'id'=>$id);
        $sql_update = array('sort'=>$sort);
        $r = Db::name('banner')->where($sql_where)->update($sql_update);
        if ($r > 0)
        {
            $JurisdictionAction->admin_record($store_id, $admin_name, '置顶轮播图ID为' . $id . '成功', 7);
            $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 置顶轮播图成功！';
            $this->Log($Log_content);
            $message = Lang('Success');
            return output(200,$message);
        }
        else
        {
            $JurisdictionAction->admin_record($store_id, $admin_name, '置顶轮播图ID为' . $id . '失败', 7);
            $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 置顶轮播图失败！参数：' . json_encode($sql_where);
            $this->Log($Log_content);
            $message = Lang('pc.6');
            return output(200,$message);
        }
    }

    // 删除轮播图
    public function BannerDel()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $id = trim($this->request->param('id')); // 轮播图ID

        $JurisdictionAction = new Jurisdiction();
        $admin_name = $this->user_list['name'];

        $sql_where = array('store_id'=>$store_id,'id'=>$id);
        $r = Db::table('lkt_banner')->where($sql_where)->delete();
        if ($r > 0)
        {
            $JurisdictionAction->admin_record($store_id, $admin_name, '删除轮播图id为' . $id, 3);
            $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 删除轮播图成功！';
            $this->Log($Log_content);
            $message = Lang('Success');
            return output('200',$message);
        }
        else
        {
            $JurisdictionAction->admin_record($store_id, $admin_name, '删除轮播图id为' . $id . '失败', 3);
            $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 删除轮播图失败！参数: ' . json_encode($sql_where);
            $this->Log($Log_content);
            $message = Lang('pc.7');
            return output(109,$message);
        }
    }

    // UI导航栏
    public function UiIndex()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $page = trim($this->request->param('pageNo'));
        $pagesize = trim($this->request->param('pageSize'));
        $pagesize = $pagesize ? $pagesize : '10';

        $list = array();
        $total = 0;
        
        $r0 = UiNavigationBarModel::where(['store_id'=>$store_id])->select()->toArray();
        if($r0)
        {
            $total = count($r0);
        }

        $r1 = UiNavigationBarModel::where(['store_id'=>$store_id])->page($page,$pagesize)->order('sort','desc')->select()->toArray();
        if($r1)
        {
            foreach($r1 as $k => $v)
            {
                $v['image'] = ServerPath::getimgpath($v['image'], $store_id);
                $list[] = $v;
            }
        }

        $data = array('total' => $total,'list' => $list);
        $message = Lang('Success');
        return output(200,$message, $data);
    }

    // 添加/编辑UI导航栏
    public function UiSave()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $id = trim($this->request->param('id')); // UI导航栏ID
        $name = trim($this->request->param('name')); // 名称
        $image = trim($this->request->param('picUrl')); // 图片
        $type = trim($this->request->param('type0')); // 类型
        $url = trim($this->request->param('url')); // 路径
        $isshow = trim($this->request->param('isShow')); // 是否显示
        $is_login = trim($this->request->param('isLogin')); // 是否显示
        
        $JurisdictionAction = new Jurisdiction();
        $admin_name = $this->user_list['name'];
        $time = date("Y-m-d H:i:s");

        if ($name)
        {
            if(mb_strlen($name, 'utf-8') > 4)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' UI导航栏名称不能超过4个字！';
                $this->Log($Log_content);
                $message = Lang('template.17');
                return output(109,$message);
            }
            if($id != '' && $id != 0)
            {
                $r0 = UiNavigationBarModel::where(['store_id'=>$store_id,'name'=>$name])->where('id','<>',$id)->field('id')->select()->toArray();
            }
            else
            {
                $r0 = UiNavigationBarModel::where(['store_id'=>$store_id,'name'=>$name])->field('id')->select()->toArray();
            }
            if($r0)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' UI导航栏名称已存在！';
                $this->Log($Log_content);
                $message = Lang('template.0');
                return output(109,$message);
            }
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' UI导航栏名称不能为空！';
            $this->Log($Log_content);
            $message = Lang('template.1');
            return output(109,$message);
        }

        if ($image)
        {
            $image = preg_replace('/.*\//', '', $image);
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' UI导航栏图片不能为空！';
            $this->Log($Log_content);
            $message = Lang('template.2');
            return output(109,$message);
        }

        if($id != '' && $id != 0)
        {
            $sql_where = array('store_id'=>$store_id,'id'=>$id);
            $sql_update = array('name'=>$name,'image'=>$image,'type'=>$type,'url'=>$url,'isshow'=>$isshow,'is_login'=>$is_login);
            $r = Db::name('ui_navigation_bar')->where($sql_where)->update($sql_update);
            if ($r == -1)
            {
                $JurisdictionAction->admin_record($store_id, $admin_name, '修改UI导航栏失败！ID：' . $id . '失败', 2);
                $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 修改UI导航栏失败！条件参数：' . json_encode($sql_where) . '；修改参数：' . json_encode($sql_update);
                $this->Log($Log_content);
                $message = Lang('pc.1');
                return output(109,$message);
            }
            else
            {
                $JurisdictionAction->admin_record($store_id, $admin_name, ' 修改UI导航栏成功！ID：' . $id, 2);
                $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 修改UI导航栏成功！';
                $this->Log($Log_content);
                $message = Lang('Success');
                return output(200,$message);
            }
        }
        else
        {
            // 查询最大的排序号
            $r_sort = UiNavigationBarModel::where(['store_id'=>$store_id])->field('MAX(sort) as sort')->select()->toArray();
            $sort = $r_sort[0]['sort'];
            $sort = $sort + 1;

            $sql_inset = array('store_id'=>$store_id,'name'=>$name,'image'=>$image,'type'=>$type,'url'=>$url,'sort'=>$sort,'add_date'=>$time,'isshow'=>$isshow,'is_login'=>$is_login);
            $r = Db::name('ui_navigation_bar')->insert($sql_inset);
            if ($r == -1)
            {
                $JurisdictionAction->admin_record($store_id, $admin_name, ' 添加UI导航栏失败！', 1);
                $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 添加UI导航栏失败！参数: ' . json_encode($sql_inset);
                $this->Log($Log_content);
                $message = Lang('operation failed');
                return output(109,$message);
            }
            else
            {
                $JurisdictionAction->admin_record($store_id, $admin_name, '添加UI导航栏成功！', 1);
                $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 添加UI导航栏成功！';
                $this->Log($Log_content);
                $message = Lang('Success');
                return output(200,$message);
            }
        }
    }

    // UI导航栏是否显示
    public function UiIsShowSwitch()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $id = trim($this->request->param('id')); // UI导航栏ID

        $JurisdictionAction = new Jurisdiction();
        $admin_name = $this->user_list['name'];
        $time = date("Y-m-d H:i:s");

        $r0 = UiNavigationBarModel::where(['store_id'=>$store_id,'id'=>$id])->field('isshow')->select()->toArray();
        if($r0)
        {
            $isshow = $r0[0]['isshow'];

            $sql_where = array('store_id'=>$store_id,'id'=>$id);
            if($isshow == 1)
            {
                $sql_update = array('isshow'=>0);
            }
            else
            {
                $sql_update = array('isshow'=>1);
            }
            $r = Db::name('ui_navigation_bar')->where($sql_where)->update($sql_update);
            if ($r == -1)
            {
                $JurisdictionAction->admin_record($store_id, $admin_name, '修改UI导航栏失败！ID：' . $id . '失败', 2);
                $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 修改UI导航栏失败！条件参数：' . json_encode($sql_where) . '；修改参数：' . json_encode($sql_update);
                $this->Log($Log_content);
                $message = Lang('template.3');
                return output(109,$message);
            }
            else
            {
                $JurisdictionAction->admin_record($store_id, $admin_name, ' 修改UI导航栏成功！ID：' . $id, 2);
                $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 修改UI导航栏成功！';
                $this->Log($Log_content);
                $message = Lang('Success');
                return output(200,$message);
            }
        }
    }

    // UI导航栏是否登陆
    public function UiIsLoginSwitch()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $id = trim($this->request->param('id')); // UI导航栏ID

        $JurisdictionAction = new Jurisdiction();
        $admin_name = $this->user_list['name'];
        $time = date("Y-m-d H:i:s");

        $r0 = UiNavigationBarModel::where(['store_id'=>$store_id,'id'=>$id])->field('is_login')->select()->toArray();
        if($r0)
        {
            $is_login = $r0[0]['is_login'];

            $sql_where = array('store_id'=>$store_id,'id'=>$id);
            if($is_login == 1)
            {
                $sql_update = array('is_login'=>0);
            }
            else
            {
                $sql_update = array('is_login'=>1);
            }
            $r = Db::name('ui_navigation_bar')->where($sql_where)->update($sql_update);
            if ($r == -1)
            {
                $JurisdictionAction->admin_record($store_id, $admin_name, '修改UI导航栏失败！ID：' . $id . '失败', 2);
                $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 修改UI导航栏失败！条件参数：' . json_encode($sql_where) . '；修改参数：' . json_encode($sql_update);
                $this->Log($Log_content);
                $message = Lang('template.3');
                return output(109,$message);
            }
            else
            {
                $JurisdictionAction->admin_record($store_id, $admin_name, ' 修改UI导航栏成功！ID：' . $id, 2);
                $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 修改UI导航栏成功！';
                $this->Log($Log_content);
                $message = Lang('Success');
                return output(200,$message);
            }
        }
    }

    // 上移/下移UI导航栏
    public function UiMove()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $id = trim($this->request->param('id')); // 互换排序的UI导航栏ID
        $id1 = trim($this->request->param('id1')); // 操作的UI导航栏ID

        $JurisdictionAction = new Jurisdiction();
        $admin_name = $this->user_list['name'];

        Db::startTrans();
        // 互换排序的UI导航栏ID的排序
        $r0 = UiNavigationBarModel::where(['store_id'=>$store_id,'id'=>$id])->field('sort')->select()->toArray();
        $sort0 = $r0[0]['sort']; // 排序号

        // 操作的UI导航栏ID的排序
        $r1 = UiNavigationBarModel::where(['store_id'=>$store_id,'id'=>$id1])->field('sort')->select()->toArray();
        $sort1 = $r1[0]['sort']; // 排序号

        if($sort0 == $sort1)
        {
            $sort0 = (int)$sort0 + 1;
        }

        // 修改当前轮播图排序
        $sql_where2 = array('store_id'=>$store_id,'id'=>$id);
        $sql_update2 = array('sort'=>$sort1);
        $r2 = Db::name('ui_navigation_bar')->where($sql_where2)->update($sql_update2);
        
        // 修改上条数据轮播图排序
        $sql_where3 = array('store_id'=>$store_id,'id'=>$id1);
        $sql_update3 = array('sort'=>$sort0);
        $r3 = Db::name('ui_navigation_bar')->where($sql_where3)->update($sql_update3);
        if ($r2 > -1 && $r3 > -1)
        {
            $JurisdictionAction->admin_record($store_id, $admin_name, ' 修改UI导航栏排序成功！参数：' . $id1, 7);
            $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 修改UI导航栏排序成功！';
            $this->Log($Log_content);
            Db::commit();
            $message = Lang('Success');
            return output(200,$message);
        }
        else
        {
            $JurisdictionAction->admin_record($store_id, $admin_name, '修改UI导航栏排序失败！参数：' . $id1, 7);
            $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 修改UI导航栏排序失败！参数：'.json_encode($sql_where2).'；参数：'.json_encode($sql_where3);
            $this->Log($Log_content);
            Db::rollback();
            $message = Lang('template.3');
            return output(200,$message);
        }
    }

    // 置顶UI导航栏
    public function UiTop()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $id = trim($this->request->param('id')); // UI导航栏ID

        $JurisdictionAction = new Jurisdiction();
        $admin_name = $this->user_list['name'];

        // 查询最大的排序号
        $r_sort = UiNavigationBarModel::where(['store_id'=>$store_id])->field('MAX(sort) as sort')->select()->toArray();
        $sort = $r_sort[0]['sort'];
        $sort = $sort + 1;

        // 修改当前UI导航栏排序
        $sql_where = array('store_id'=>$store_id,'id'=>$id);
        $sql_update = array('sort'=>$sort);
        $r = Db::name('ui_navigation_bar')->where($sql_where)->update($sql_update);
        if ($r > 0)
        {
            $JurisdictionAction->admin_record($store_id, $admin_name, '置顶UI导航栏成功！ID：' . $id, 7);
            $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 置顶UI导航栏成功！';
            $this->Log($Log_content);
            $message = Lang('Success');
            return output(200,$message);
        }
        else
        {
            $JurisdictionAction->admin_record($store_id, $admin_name, '置顶UI导航栏失败！ID：' . $id, 7);
            $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 置顶UI导航栏失败！参数：' . json_encode($sql_where);
            $this->Log($Log_content);
            $message = Lang('template.3');
            return output(200,$message);
        }
    }

    // 删除UI导航栏
    public function UiDel()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $id = trim($this->request->param('id')); // UI导航栏ID

        $JurisdictionAction = new Jurisdiction();
        $admin_name = $this->user_list['name'];

        $sql_where = array('store_id'=>$store_id,'id'=>$id);
        $r = Db::table('lkt_ui_navigation_bar')->where($sql_where)->delete();
        if ($r > 0)
        {
            $JurisdictionAction->admin_record($store_id, $admin_name, ' 删除UI导航栏成功！ID：' . $id, 3);
            $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 删除UI导航栏成功！';
            $this->Log($Log_content);
            $message = Lang('Success');
            return output('200',$message);
        }
        else
        {
            $JurisdictionAction->admin_record($store_id, $admin_name, ' 删除UI导航栏失败！ID：' . $id, 3);
            $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 删除UI导航栏失败！参数: ' . json_encode($sql_where);
            $this->Log($Log_content);
            $message = Lang('template.4');
            return output(200,$message);
        }
    }

    // 分类管理
    public function ClassIndex()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $page = trim($this->request->param('pageNo'));
        $pagesize = trim($this->request->param('pageSize'));
        $pagesize = $pagesize ? $pagesize : '10';

        $list = array();
        $total = 0;
        
        $r0 = ProductClassModel::where(['store_id'=>$store_id,'sid'=>0,'recycle'=>0,'examine'=>1,'notset'=>0])->field('cid')->select()->toArray();
        if($r0)
        {
            $total = count($r0);
        }

        $r1 = ProductClassModel::where(['store_id'=>$store_id,'sid'=>0,'recycle'=>0,'examine'=>1,'notset'=>0])->page($page,$pagesize)->order('sort','desc')->select()->toArray();
        if($r1)
        {
            foreach($r1 as $k => $v)
            {
                $list[] = $v;
            }
        }

        $data = array('total' => $total,'list' => $list);
        $message = Lang('Success');
        return output(200,$message, $data);
    }

    // 上移/下移分类
    public function ClassMove()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $id = trim($this->request->param('id')); // 互换排序的分类ID
        $id1 = trim($this->request->param('id1')); // 操作的分类ID

        $JurisdictionAction = new Jurisdiction();
        $admin_name = $this->user_list['name'];

        Db::startTrans();
        // 互换排序的分类ID的排序
        $r0 = ProductClassModel::where(['store_id'=>$store_id,'cid'=>$id])->field('sort')->select()->toArray();
        $sort0 = $r0[0]['sort']; // 排序号

        // 操作的分类ID的排序
        $r1 = ProductClassModel::where(['store_id'=>$store_id,'cid'=>$id1])->field('sort')->select()->toArray();
        $sort1 = $r1[0]['sort']; // 排序号

        if($sort0 == $sort1)
        {
            $sort0 = (int)$sort0 + 1;
        }

        // 修改当前轮播图排序
        $sql_where2 = array('store_id'=>$store_id,'cid'=>$id);
        $sql_update2 = array('sort'=>$sort1);
        $r2 = Db::name('product_class')->where($sql_where2)->update($sql_update2);
        
        // 修改上条数据轮播图排序
        $sql_where3 = array('store_id'=>$store_id,'cid'=>$id1);
        $sql_update3 = array('sort'=>$sort0);
        $r3 = Db::name('product_class')->where($sql_where3)->update($sql_update3);
        if ($r2 > -1 && $r3 > -1)
        {
            $JurisdictionAction->admin_record($store_id, $admin_name, ' 修改分类排序成功！参数：' . $id1, 7);
            $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 修改分类排序成功！';
            $this->Log($Log_content);
            Db::commit();
            $message = Lang('Success');
            return output(200,$message);
        }
        else
        {
            $JurisdictionAction->admin_record($store_id, $admin_name, '修改分类排序失败！参数：' . $id1, 7);
            $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 修改分类排序失败！参数：'.json_encode($sql_where2).'；参数：'.json_encode($sql_where3);
            $this->Log($Log_content);
            Db::rollback();
            $message = Lang('template.5');
            return output(200,$message);
        }
    }

    // 置顶分类
    public function ClassTop()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $id = trim($this->request->param('id')); // 分类ID

        $JurisdictionAction = new Jurisdiction();
        $admin_name = $this->user_list['name'];

        // 查询最大的排序号
        $r_sort = ProductClassModel::where(['store_id'=>$store_id])->field('MAX(sort) as sort')->select()->toArray();
        $sort = $r_sort[0]['sort'];
        $sort = $sort + 1;

        // 修改当前UI导航栏排序
        $sql_where = array('store_id'=>$store_id,'cid'=>$id);
        $sql_update = array('sort'=>$sort);
        $r = Db::name('product_class')->where($sql_where)->update($sql_update);
        if ($r > 0)
        {
            $JurisdictionAction->admin_record($store_id, $admin_name, '置顶分类成功！ID：' . $id, 7);
            $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 置顶分类成功！';
            $this->Log($Log_content);
            $message = Lang('Success');
            return output(200,$message);
        }
        else
        {
            $JurisdictionAction->admin_record($store_id, $admin_name, '置顶分类失败！ID：' . $id, 7);
            $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 置顶分类失败！参数：' . json_encode($sql_where);
            $this->Log($Log_content);
            $message = Lang('template.5');
            return output(200,$message);
        }
    }

    // 是否显示-分类
    public function ClassSwitch()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $id = trim($this->request->param('id')); // 分类ID

        $JurisdictionAction = new Jurisdiction();
        $admin_name = $this->user_list['name'];

        $is_display = 1;
        $r0 = ProductClassModel::where(['store_id'=>$store_id,'cid'=>$id])->field('is_display')->select()->toArray();
        if($r0)
        {
            if($r0[0]['is_display'] == 1)
            {
                $is_display = 0;
            }
        }

        // 修改当前UI导航栏排序
        $sql_where = array('store_id'=>$store_id,'cid'=>$id);
        $sql_update = array('is_display'=>$is_display);
        $r = Db::name('product_class')->where($sql_where)->update($sql_update);
        if ($r > 0)
        {
            $JurisdictionAction->admin_record($store_id, $admin_name, '修改分类成功！ID：' . $id, 7);
            $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 修改分类成功！';
            $this->Log($Log_content);
            $message = Lang('Success');
            return output(200,$message);
        }
        else
        {
            $JurisdictionAction->admin_record($store_id, $admin_name, '修改分类失败！ID：' . $id, 7);
            $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 修改分类失败！参数：' . json_encode($sql_where);
            $this->Log($Log_content);
            $message = Lang('template.5');
            return output(200,$message);
        }
    }

    // 活动管理
    public function ActivityList()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $id = trim($this->request->param('id'));
        $page = trim($this->request->param('pageNo'));
        $pagesize = trim($this->request->param('pageSize'));
        $page = $page ? $page : '1';
        $pagesize = $pagesize ? $pagesize : '10';

        $list = array();
        $total = 0;
        
        $where_list = array('store_id'=>$store_id);
        if($id != '' && $id != 0)
        {
            $where_list['id'] = $id;
        }
        $r0 = ActivityModel::where($where_list)->field('id')->select()->toArray();
        if($r0)
        {
            $total = count($r0);
        }

        $r1 = ActivityModel::where($where_list)->page($page,$pagesize)->order('sort','desc')->select()->toArray();
        if($r1)
        {
            foreach($r1 as $k => $v)
            {
                $list[] = $v;
            }
        }

        $data = array('total' => $total,'list' => $list);
        $message = Lang('Success');
        return output(200,$message, $data);
    }

    // 获取商品-添加商品
    public function GetGoodsList()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $id = trim($this->request->param('id')); // 活动ID
        $goodsIdList = trim($this->request->param('goodsIdList')); // 已选中的活动商品ID
        $page = trim($this->request->param('pageNo'));
        $pagesize = trim($this->request->param('pageSize'));
        $pagesize = $pagesize ? $pagesize : '10';

        $list = array();
        $total = 0;
        $start = 0;
        if($page)
        {
            $start = ($page - 1) * $pagesize;
        }

        $mch_id = PC_Tools::SelfOperatedStore($store_id);
        
        $condition = "a.store_id = '$store_id' and a.recycle = 0 and a.is_presell = 0 and a.commodity_type = '0' and a.mch_id = '$mch_id' ";

        $p_id_list = array();
        if($id != '')
        {
            $sql_0 = "select p_id from lkt_activity_pro where store_id = '$store_id' and activity_id = '$id' ";
            $r_0 = Db::query($sql_0);
            if($r_0)
            {   
                foreach($r_0 as $k_0 =>$v_0)
                {
                    $p_id_list[] = $v_0['p_id'];
                }
            }
        }

        if($p_id_list != array())
        {
            $p_id_str = implode(',',$p_id_list);

            $condition .= " and a.id not in ($p_id_str) ";
        }

        $sql0 = "select count(a.id) as total from lkt_product_list as a left join lkt_mch as b on a.mch_id = b.id where $condition ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $total = $r0[0]['total'];
        }

        $str = "a.id,a.product_title,a.imgurl,a.volume,a.s_type,a.num,a.status,a.cover_map,a.product_class as classId,a.brand_id,b.name as mch_name,b.logo";

        $sql1 = "select $str from lkt_product_list as a left join lkt_mch as b on a.mch_id = b.id where $condition order by a.sort asc limit $start,$pagesize ";
        $r1 = Db::query($sql1);
        if($r1)
        {
            foreach ($r1 as $k => $v)
            {
                $v['imgurl'] = ServerPath::getimgpath($v['imgurl'],$store_id);

                $pid = $v['id'];
                $r_s = ConfigureModel::where(['pid'=>$pid,'recycle'=>0])->field('min(price) as price')->select()->toArray();
                if ($r_s)
                {
                    $v['price'] = round($r_s[0]['price'],2);
                }
                $list[] = $v;
            }
        }

        $data = array('total' => $total,'list' => $list);
        $message = Lang('Success');
        return output(200,$message, $data);
    }

    // 获取插件数据
    public function GetPluginTypeList()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $Plugin_list = array();
        $Plugin = new Plugin();
        $Plugin_arr1 = $Plugin->front_Plugin($store_id);

        foreach ($Plugin_arr1 as $k => $v)
        {
            if ($k == 'SecondsPublic' && $v == 1)
            {
                $Plugin_list[] = array('value'=>8,'name'=>'秒杀','status'=>false);
            }
            else if ($k == 'Auction' && $v == 1)
            {
                $Plugin_list[] = array('value'=>4,'name'=>'竞拍','status'=>false);
            }
            else if ($k == 'IntegralPublicMethod' && $v == 1)
            {
                $Plugin_list[] = array('value'=>7,'name'=>'积分','status'=>false);
            }
        }

        $data = array('list' => $Plugin_list);
        $message = Lang('Success');
        return output(200,$message, $data);
    }

    // 添加/编辑活动
    public function ActivitySave()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $id = trim($this->request->param('id')); // UI导航栏ID
        $activity_type = trim($this->request->param('activityType')); // 类型 0.活动专题 1.营销插件
        $plug_type = trim($this->request->param('plugType')); // 插件类型
        $name = trim($this->request->param('name')); // 活动名称
        $p_id = trim($this->request->param('pid')); // 商品ID

        $JurisdictionAction = new Jurisdiction();
        $admin_name = $this->user_list['name'];
        $time = date("Y-m-d H:i:s");
        Db::startTrans();
        if($activity_type == 1)
        {
            if($plug_type == 0 || $plug_type == '')
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 请选择活动类型！';
                $this->Log($Log_content);
                $message = Lang('template.6');
                return output(109,$message);
            }
        }
        else
        {
            $plug_type = 0;
        }

        if ($name)
        {
            if($id != '' && $id != 0)
            {
                $r0 = ActivityModel::where(['store_id'=>$store_id,'name'=>$name])->where('id','<>',$id)->field('id')->select()->toArray();
            }
            else
            {
                $r0 = ActivityModel::where(['store_id'=>$store_id,'name'=>$name])->field('id')->select()->toArray();
            }
            if($r0)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 活动标题已存在！';
                $this->Log($Log_content);
                $message = Lang('template.7');
                return output(109,$message);
            }
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 活动标题不能为空！';
            $this->Log($Log_content);
            $message = Lang('template.8');
            return output(109,$message);
        }

        if($id != '' && $id != 0)
        {
            $sql_where = array('store_id'=>$store_id,'id'=>$id);
            $sql_update = array('activity_type'=>$activity_type,'plug_type'=>$plug_type,'name'=>$name,'p_id'=>$p_id);
            $r = Db::name('activity')->where($sql_where)->update($sql_update);
            if ($r == -1)
            {
                $JurisdictionAction->admin_record($store_id, $admin_name, '修改活动失败！ID：' . $id, 2);
                $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 修改活动失败！条件参数：' . json_encode($sql_where) . '；修改参数：' . json_encode($sql_update);
                $lktlog->customerLog($Log_content);
                Db::rollback();
                $message = Lang('template.9');
                return output(109,$message);
            }
            else
            {
                $r_1 = Db::table('lkt_activity_pro')->where(['store_id'=>$store_id,'activity_id'=>$id])->delete();

                if($p_id != '')
                {
                    $p_list = explode(',',$p_id);
                    $z_sort = count($p_list);
                    foreach ($p_list as $k => $v)
                    {
                        $sql_inset_pro = array('store_id'=>$store_id,'activity_id'=>$id,'p_id'=>$v,'is_display'=>0,'sort'=>$z_sort,'add_date'=>$time,'is_display'=>1);
                        $r_activity_pro = Db::name('activity_pro')->insertGetId($sql_inset_pro);
                        if($r_activity_pro <= 0)
                        {
                            $JurisdictionAction->admin_record($store_id, $admin_name, ' 添加活动商品失败！', 1);
                            $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 添加活动商品失败！参数: ' . json_encode($sql_inset_pro);
                            $this->Log($Log_content);
                            Db::rollback();
                            $message = Lang('template.10');
                            return output(109,$message);
                        }
                        $z_sort = $z_sort - 1;
                    }
                }

                $JurisdictionAction->admin_record($store_id, $admin_name, '修改活动成功！ID：' . $id, 2);
                $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 修改活动成功！';
                $this->Log($Log_content);
                Db::commit();
                $message = Lang('Success');
                return output(200,$message);
            }
        }
        else
        {
            // 查询最大的排序号
            $r_sort = ActivityModel::where(['store_id'=>$store_id])->field('MAX(sort) as sort')->select()->toArray();
            $sort = $r_sort[0]['sort'];
            $sort = $sort + 1;

            $sql_inset = array('store_id'=>$store_id,'activity_type'=>$activity_type,'plug_type'=>$plug_type,'name'=>$name,'p_id'=>$p_id,'sort'=>$sort,'add_date'=>$time,'is_display'=>1);
            $r = Db::name('activity')->insertGetId($sql_inset);
            if ($r > 0)
            {
                if($p_id != '')
                {
                    $p_list = explode(',',$p_id);
                    $z_sort = count($p_list);
                    foreach ($p_list as $k => $v)
                    {
                        $sql_inset_pro = array('store_id'=>$store_id,'activity_id'=>$r,'p_id'=>$v,'is_display'=>0,'sort'=>$z_sort,'add_date'=>$time,'is_display'=>1);
                        $r_activity_pro = Db::name('activity_pro')->insertGetId($sql_inset_pro);
                        if($r_activity_pro <= 0)
                        {
                            $JurisdictionAction->admin_record($store_id, $admin_name, ' 添加活动商品失败！', 1);
                            $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 添加活动商品失败！参数: ' . json_encode($sql_inset_pro);
                            $this->Log($Log_content);
                            Db::rollback();
                            $message = Lang('template.10');
                            return output(109,$message);
                        }
                        $z_sort = $z_sort - 1;
                    }
                }
                $JurisdictionAction->admin_record($store_id, $admin_name, '添加活动成功！', 1);
                $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 添加活动成功！';
                $this->Log($Log_content);
                Db::commit();
                $message = Lang('Success');
                return output(200,$message);
            }
            else
            {
                $JurisdictionAction->admin_record($store_id, $admin_name, '添加活动失败', 1);
                $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 添加活动失败！参数: ' . json_encode($sql_inset);
                $this->Log($Log_content);
                Db::rollback();
                $message = Lang('template.11');
                return output(109,$message);
            }
        }
    }

    // 获取商品-编辑页面
    public function GetActGoodsList()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $id = trim($this->request->param('id'));
        $page = trim($this->request->param('pageNo'));
        $pagesize = trim($this->request->param('pageSize'));
        $page = $page ? $page : '1';
        $pagesize = $pagesize ? $pagesize : '10';
        
        $list = array();
        $total = 0;

        $r0 = ActivityProModel::where(['store_id'=>$store_id,'activity_id'=>$id])->field('count(id) as total')->select()->toArray();
        if($r0)
        {
            $total = $r0[0]['total'];
        }

        $r1 = ActivityProModel::where(['store_id'=>$store_id,'activity_id'=>$id])->page($page,$pagesize)->order('sort','desc')->select()->toArray();
        if($r1)
        {
            foreach ($r1 as $k => $v)
            {
                $pid = $v['p_id'];
                $v['imgurl'] = '';
                $v['product_title'] = '';
                $v['num'] = 0;
                $v['price'] = 0;
                $r2 = ProductListModel::where(['id'=>$pid])->field('product_title,imgurl,num')->select()->toArray();
                if($r2)
                {
                    $v['imgurl'] = ServerPath::getimgpath($r2[0]['imgurl'],$store_id);
                    $v['product_title'] = $r2[0]['product_title'];
                    $v['num'] = $r2[0]['num'];
                }

                $r3 = ConfigureModel::where(['pid'=>$pid,'recycle'=>0])->field('min(price) as price')->select()->toArray();
                if ($r3)
                {
                    $v['price'] = round($r3[0]['price'],2);
                }
                $list[] = $v;
            }
        }

        $data = array('total' => $total,'list' => $list);
        $message = Lang('Success');
        return output(200,$message, $data);
    }

    // 活动是否显示
    public function ActivitySwitch()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $id = trim($this->request->param('id')); // 活动商品ID

        $JurisdictionAction = new Jurisdiction();
        $admin_name = $this->user_list['name'];

        $r0 = ActivityModel::where(['store_id'=>$store_id,'id'=>$id])->field('is_display')->select()->toArray();
        if($r0)
        {
            $is_display = $r0[0]['is_display'];

            $sql_where = array('store_id'=>$store_id,'id'=>$id);
            if($is_display == 1)
            {
                $sql_update = array('is_display'=>0);
            }
            else
            {
                $sql_update = array('is_display'=>1);
            }
            $r = Db::name('activity')->where($sql_where)->update($sql_update);
            if ($r == -1)
            {
                $JurisdictionAction->admin_record($store_id, $admin_name, '修改活动商品失败！ID：' . $id . '失败', 2);
                $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 修改活动商品失败！条件参数：' . json_encode($sql_where) . '；修改参数：' . json_encode($sql_update);
                $this->Log($Log_content);
                $message = Lang('template.12');
                return output(109,$message);
            }
            else
            {
                $JurisdictionAction->admin_record($store_id, $admin_name, ' 修改活动商品成功！ID：' . $id, 2);
                $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 修改活动商品成功！';
                $this->Log($Log_content);
                $message = Lang('Success');
                return output(200,$message);
            }
        }
        else
        {
            $message = Lang('Parameter error');
            return output(109,$message);
        }
    }

    // 商品是否显示
    public function ActGoodsSwitch()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $id = trim($this->request->param('id')); // 活动商品ID

        $JurisdictionAction = new Jurisdiction();
        $admin_name = $this->user_list['name'];

        $r0 = ActivityProModel::where(['store_id'=>$store_id,'id'=>$id])->field('is_display')->select()->toArray();
        if($r0)
        {
            $is_display = $r0[0]['is_display'];

            $sql_where = array('store_id'=>$store_id,'id'=>$id);
            if($is_display == 1)
            {
                $sql_update = array('is_display'=>0);
            }
            else
            {
                $sql_update = array('is_display'=>1);
            }
            $r = Db::name('activity_pro')->where($sql_where)->update($sql_update);
            if ($r == -1)
            {
                $JurisdictionAction->admin_record($store_id, $admin_name, '修改活动商品失败！ID：' . $id . '失败', 2);
                $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 修改活动商品失败！条件参数：' . json_encode($sql_where) . '；修改参数：' . json_encode($sql_update);
                $this->Log($Log_content);
                $message = Lang('template.12');
                return output(109,$message);
            }
            else
            {
                $JurisdictionAction->admin_record($store_id, $admin_name, ' 修改活动商品成功！ID：' . $id, 2);
                $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 修改活动商品成功！';
                $this->Log($Log_content);
                $message = Lang('Success');
                return output(200,$message);
            }
        }
        else
        {
            $message = Lang('Parameter error');
            return output(109,$message);
        }
    }

    // 上移/下移活动商品
    public function ActGoodsMove()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $id = trim($this->request->param('id')); // 活动ID
        $goodsId = trim($this->request->param('goodsId')); // 互换排序的活动商品ID
        $goodsId1 = trim($this->request->param('goodsId1')); // 操作的活动商品ID

        $JurisdictionAction = new Jurisdiction();
        $admin_name = $this->user_list['name'];

        Db::startTrans();
        // 互换排序的分类ID的排序
        $r0 = ActivityProModel::where(['store_id'=>$store_id,'activity_id'=>$id,'id'=>$goodsId])->field('sort')->select()->toArray();
        $sort0 = $r0[0]['sort']; // 排序号

        // 操作的分类ID的排序
        $r1 = ActivityProModel::where(['store_id'=>$store_id,'activity_id'=>$id,'id'=>$goodsId1])->field('sort')->select()->toArray();
        $sort1 = $r1[0]['sort']; // 排序号

        if($sort0 == $sort1)
        {
            $sort0 = (int)$sort0 + 1;
        }

        // 修改当前轮播图排序
        $sql_where2 = array('store_id'=>$store_id,'activity_id'=>$id,'id'=>$goodsId);
        $sql_update2 = array('sort'=>$sort1);
        $r2 = Db::name('activity_pro')->where($sql_where2)->update($sql_update2);
        
        // 修改上条数据轮播图排序
        $sql_where3 = array('store_id'=>$store_id,'activity_id'=>$id,'id'=>$goodsId1);
        $sql_update3 = array('sort'=>$sort0);
        $r3 = Db::name('activity_pro')->where($sql_where3)->update($sql_update3);
        if ($r2 > -1 && $r3 > -1)
        {
            $JurisdictionAction->admin_record($store_id, $admin_name, ' 修改活动商品排序成功！参数：' . $goodsId1, 7);
            $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 修改活动商品排序成功！';
            $this->Log($Log_content);
            Db::commit();
            $message = Lang('Success');
            return output(200,$message);
        }
        else
        {
            $JurisdictionAction->admin_record($store_id, $admin_name, '修改活动商品排序失败！参数：' . $goodsId1, 7);
            $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 修改活动商品排序失败！参数：'.json_encode($sql_where2).'；参数：'.json_encode($sql_where3);
            $this->Log($Log_content);
            Db::rollback();
            $message = Lang('template.12');
            return output(200,$message);
        }
    }
    
    // 置顶活动商品
    public function ActGoodsTop()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $id = trim($this->request->param('id')); // 活动ID
        $goodsId = trim($this->request->param('goodsId')); // 活动商品ID

        $JurisdictionAction = new Jurisdiction();
        $admin_name = $this->user_list['name'];

        // 查询最大的排序号
        $r_sort = ActivityProModel::where(['store_id'=>$store_id,'activity_id'=>$id])->field('MAX(sort) as sort')->select()->toArray();
        $sort = $r_sort[0]['sort'];
        $sort = $sort + 1;

        // 修改当前UI导航栏排序
        $sql_where = array('store_id'=>$store_id,'activity_id'=>$id,'id'=>$goodsId);
        $sql_update = array('sort'=>$sort);
        $r = Db::name('activity_pro')->where($sql_where)->update($sql_update);
        if ($r > 0)
        {
            $JurisdictionAction->admin_record($store_id, $admin_name, '置顶活动商品成功！ID：' . $goodsId, 7);
            $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 置顶活动商品成功！';
            $this->Log($Log_content);
            $message = Lang('Success');
            return output(200,$message);
        }
        else
        {
            $JurisdictionAction->admin_record($store_id, $admin_name, '置顶活动商品失败！ID：' . $goodsId, 7);
            $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 置顶活动商品失败！参数：' . json_encode($sql_where);
            $this->Log($Log_content);
            $message = Lang('template.12');
            return output(200,$message);
        }
    }

    // 上移/下移活动
    public function ActivityMove()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $moveId = trim($this->request->param('moveId')); // 互换排序的活动ID
        $moveId2 = trim($this->request->param('moveId2')); // 操作的活动ID

        $JurisdictionAction = new Jurisdiction();
        $admin_name = $this->user_list['name'];

        Db::startTrans();
        // 互换排序的分类ID的排序
        $r0 = ActivityModel::where(['store_id'=>$store_id,'id'=>$moveId])->field('sort')->select()->toArray();
        $sort0 = $r0[0]['sort']; // 排序号

        // 操作的分类ID的排序
        $r1 = ActivityModel::where(['store_id'=>$store_id,'id'=>$moveId2])->field('sort')->select()->toArray();
        $sort1 = $r1[0]['sort']; // 排序号

        if($sort0 == $sort1)
        {
            $sort0 = (int)$sort0 + 1;
        }

        // 修改当前轮播图排序
        $sql_where2 = array('store_id'=>$store_id,'id'=>$moveId);
        $sql_update2 = array('sort'=>$sort1);
        $r2 = Db::name('activity')->where($sql_where2)->update($sql_update2);
        
        // 修改上条数据轮播图排序
        $sql_where3 = array('store_id'=>$store_id,'id'=>$moveId2);
        $sql_update3 = array('sort'=>$sort0);
        $r3 = Db::name('activity')->where($sql_where3)->update($sql_update3);
        if ($r2 > -1 && $r3 > -1)
        {
            $JurisdictionAction->admin_record($store_id, $admin_name, ' 修改活动排序成功！参数：' . $moveId2, 7);
            $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 修改活动排序成功！';
            $this->Log($Log_content);
            Db::commit();
            $message = Lang('Success');
            return output(200,$message);
        }
        else
        {
            $JurisdictionAction->admin_record($store_id, $admin_name, '修改活动排序失败！参数：' . $moveId2, 7);
            $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 修改活动排序失败！参数：'.json_encode($sql_where2).'；参数：'.json_encode($sql_where3);
            $this->Log($Log_content);
            Db::rollback();
            $message = Lang('template.9');
            return output(200,$message);
        }
    }

    // 删除活动
    public function ActivityDel()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $id = trim($this->request->param('id')); // 活动ID

        $JurisdictionAction = new Jurisdiction();
        $admin_name = $this->user_list['name'];

        Db::startTrans();

        $where_list0 = array('store_id'=>$store_id,'id'=>$id);
        $r0 = Db::table('lkt_activity')->where($where_list0)->delete();
        if ($r0 > 0)
        {
            $r1 = Db::table('lkt_activity_pro')->where(['store_id'=>$store_id,'activity_id'=>$id])->delete();

            $JurisdictionAction->admin_record($store_id, $admin_name, '删除活动成功！ID：' . $id, 3);
            $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 删除活动成功！';
            $this->Log($Log_content);
            Db::commit();
            $message = Lang('Success');
            return output(200,$message);
        }
        else
        {
            $JurisdictionAction->admin_record($store_id, $admin_name, ' 删除活动失败！ID：' . $id, 3);
            $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 删除活动失败！参数：' . json_encode($where_list0);
            $this->Log($Log_content);
            Db::rollback();
            $message = Lang('template.13');
            return output(109,$message);
        }
    }

    // 日志
    public function Log($Log_content)
    {
        $lktlog = new LaiKeLogUtils();
        $lktlog->log("template/AdminDiy.log",$Log_content);
        return;
    }
}
