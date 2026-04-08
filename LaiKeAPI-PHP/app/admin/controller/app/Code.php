<?php
namespace app\admin\controller\app;

use app\BaseController;
use app\common;
use think\facade\Db;
use app\common\ServerPath;

require_once "../app/common/phpqrcode.php";

use app\admin\model\ConfigModel;
use app\admin\model\PaymentConfigModel;
use app\admin\model\ThirdModel;
/**
 * 分享
 */
class Code extends BaseController
{
    public $appid; // 小程序唯一标识
    public $appsecret; // 小程序的 app secret

    // 竞拍分享
    public function shareQrCode()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $access_id = trim($this->request->param('access_id'));

        $id = trim($this->request->param('id'));
        $parameter = trim($this->request->param('parameter'));
        $parameter = json_decode($parameter,true);
        $apiUrl = $parameter['apiUrl'];
        $path = $parameter['path'];

        $user_id = $this->user_list['user_id'];
        //获取根目录
        $end_res = ThirdModel::select()->toArray();
        if($end_res)
        {
            $endurl = $end_res[0]['endurl'];
        }
        else
        {
            $message = Lang('getcode.0');
            return output(ERROR_CODE_SPBCZ, $message);
        }

        $name = '';
        $img = '';
        $isfx = true;
        $url = $path;
        if($apiUrl == 'plugin.auction.app.shareSpecial')
        {
            $sql0 = "select name,img from lkt_auction_special where id = '$id' ";
            $r0 = Db::query($sql0);
            if($r0)
            {
                $name = $r0[0]['name'];
                $img = ServerPath::getimgpath($r0[0]['img']); // 获取图片地址
                $goods_img = $this->downImgUrl($img);//下载商品图片
                $goods_img_rand = rand(100000, 999999);
            }
            if ($path && !empty($path)) 
            {
                $url = $path;
            }
            else
            {
                $url = "pagesA/OrderBidding/StartBidding?specialId=" . $id;
            }
            $url = $url . "&isfx=" . $isfx . "&fatherId=" . $user_id;
        }

        file_put_contents('../public/image/product_share_img/1log.txt', '开始', FILE_APPEND);

        $pic_size = 3;
        if ($store_type == 1)
        {
            //微信小程序
            $r = PaymentConfigModel::where(['pid'=>5,'store_id'=>$store_id])->select()->toArray();
            $value = $r ? $r[0] : [];
            if ($value)
            {
                $list = json_decode($value['config_data']);
                $appid = $list->appid;
                $appsecret = $list->appsecret;
            }
            $qrcode_path = $this->get_mini_code($url, $appid, $appsecret);
        }
        // else if ($store_type == 3)
        // {
        //     //支付宝小程序
        //     $qrcode_path = $this->get_qrcode2($store_id, $url, $query_param, '../public/image/product_share_img', 5);
        //     $qrcode_path = $this->upimg($qrcode_path, 200);
        // }
        else
        {
            $r = ConfigModel::where('store_id',$store_id)->select()->toArray();
            if (!empty($r[0]['H5_domain']))
            {
                $url = $r[0]['H5_domain'] . $url;
            }
            //生成二维码
            $qrcode_path = $this->get_qrcode1($url, '../public/image/product_share_img', $pic_size);
        }

        // 底图
        $path_1 = '../public/image/product_share_img/jpshare.jpg';
        // 二维码
        $path_2 = $qrcode_path;
        $path_2 = $this->upimg($path_2, 69);

        $goods_img_obj = \think\Image::open($goods_img);
        $goods_img1 = "../public/image/product_share_img/goods_img_$goods_img_rand.jpg";
        list($width, $height) = getimagesize($goods_img);
        if ($width >= $height)
        {
            $goods_img_nw = 291 / $height * $width;
            $goods_img_nh = 291;
        }
        else
        {
            $goods_img_nh = 291 / $width * $height;
            $goods_img_nw = 291;
        }

        $goods_img_obj
            ->thumb($goods_img_nw, $goods_img_nh, \think\Image::THUMB_FIXED)
            ->save($goods_img1, 'jpg');
        unlink($goods_img);
        $goods_img = $goods_img1;
        list($width, $height) = getimagesize($goods_img);

        //字体
        $font2 = '../public/image/product_share_img/PINGFANG MEDIUM.TTF';//字体,字体文件需保存到相应文件夹下
        $font1 = '../public/image/product_share_img/PINGFANG BOLD.TTF';//字体,字体文件需保存到相应文件夹下
        $goods_local = array(15, 15);
        $qrcode_local = array(235, 325);
        $text1_local = array(15, 340);
        $black = "#000000";

        $versin = 2;//版本号

        $share_img_name = time() . $id . $versin . '_share.jpg';

        $share_img_path = 'image/product_share_img/' . $share_img_name;

        $image = \think\Image::open($path_1);
        $image
            ->water($goods_img, $goods_local)//商品图片
            ->water($path_2, $qrcode_local)//二维码
            ->text($name, $font2, 14, $black, $text1_local);
        $image->save($share_img_path, 'jpg');

        $qrCode = preg_replace('/.*\//', '', $path_2);
        unlink($qrcode_path);
        $resArr = array('doMain'=>$endurl,'qrCode' => $qrCode,'imgUrl'=>$share_img_path);
        $message = Lang('Success');
        return output("200", $message,$resArr);
    }

    public function get_mini_code($url, $APPID, $APPSECRET, $code_width = 165)
    {
        header('content-type:text/html;charset=utf-8');
        //      获取access_token
        $access_token = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=$APPID&secret=$APPSECRET";
        //      缓存access_token
        $_SESSION['access_token'] = "";
        $_SESSION['expires_in'] = 0;
        $ACCESS_TOKEN = "";
        if (!isset($_SESSION['access_token']) || (isset($_SESSION['expires_in']) && time() > $_SESSION['expires_in']))
        {

            $json = $this->httpRequest($access_token);
            $json = json_decode($json, true);
            $_SESSION['access_token'] = $json['access_token'];
            $_SESSION['expires_in'] = time() + 7200;
            $ACCESS_TOKEN = $json["access_token"];
        }
        else
        {
            $ACCESS_TOKEN = $_SESSION["access_token"];
        }
        //      构建请求二维码参数
        //      path是扫描二维码跳转的小程序路径，可以带参数?id=xxx
        //      width是二维码宽度
        $qcode = "https://api.weixin.qq.com/wxa/getwxacode?access_token=$ACCESS_TOKEN";
        $param = json_encode(array("path" => $url, "width" => 50));

        //      POST参数
        $result = $this->httpRequest($qcode, $param, "POST");
        //      生成二维码
        $file_path = '../public/images/product_share_img/';
        $qrcode_name = md5(time() . rand(1000, 9999));
        $filename = $file_path . $qrcode_name . '.jpg';
        $qrcode_name = file_put_contents($filename, $result);

        $imgPath = "../public/images/product_share_img/" . uniqid() . mt_rand(1, 200) . ".jpg";

        list($width, $height) = getimagesize($filename);
        $per = round($code_width / $height, 3);
        $n_w = $width * $per;
        $n_h = $height * $per;

        $new = imagecreatetruecolor($n_w, $n_h);
        $img = imagecreatefromjpeg($filename);
        //copy部分图像并调整
        imagecopyresized($new, $img, 0, 0, 0, 0, $n_w, $n_h, $width, $height);
        //图像输出新图片、另存为
        imagejpeg($new, $imgPath);
        imagedestroy($new);
        imagedestroy($img);
        // unlink($filename);
        $filename = $imgPath;
        return $filename;
    }

    public function upimg($filename, $code_width = 200)
    {      
        $type = explode('.', $filename);
        if ($type[1] == 'png' || $type[1] == 'PNG')
        {
            $imgPath = "../public/image/product_share_img/" . uniqid() . mt_rand(1, 200) . ".png";
        }
        else
        {
            $imgPath = "../public/image/product_share_img/" . uniqid() . mt_rand(1, 200) . ".jpg";
        }
        
        list($width, $height) = getimagesize($filename);
        $per = round($code_width / $height, 3);
        $n_w = $width * $per;
        $n_h = $height * $per; 
        $new = imagecreatetruecolor($n_w, $n_h);

        $filename = base64_encode(file_get_contents($filename));
        $filename = base64_decode($filename);

        $img = imagecreatefromstring($filename);
        //copy部分图像并调整
        imagecopyresized($new, $img, 0, 0, 0, 0, $n_w, $n_h, $width, $height);
        //图像输出新图片、另存为
        imagejpeg($new, $imgPath);
        // var_dump($imgPath);die;
        imagedestroy($new);
        imagedestroy($img);
        // unlink($filename);
        $filename = $imgPath;
        return $filename;
    }

    // 获得二维码，并储存到本地
    public function get_qrcode1($url, $uploadImg, $size = 5)
    {
        $qrcode_name = md5(time() . rand(1000, 9999));
        $value = str_replace("&amp;", "&", $url);                  //二维码内容
        $errorCorrectionLevel = 'L';    //容错级别
        $matrixPointSize = $size;           //生成图片大小
        $filename = $uploadImg . '/' . $qrcode_name . '.png';

        app('QRcode')::png($value, $filename, $errorCorrectionLevel, $matrixPointSize, 2);

        return $filename;
    }

    // 获得二维码，并储存到本地
    public function get_qrcode2($store_id, $url, $query_param, $uploadImg, $size = 5)
    {
        $appid = $this->config_data->appid;
        $data['$store_id'] = $store_id;
        $data['$url'] = $url;
        $data['$query_param'] = $query_param;
        $data['$uploadImg'] = $uploadImg;
        $code_url = TestImage::getcode($url, $query_param, $uploadImg, $store_id, '分销推广二维码');

        $img = $this->downImgUrl($code_url . 'jpg');

        return $img;
    }

    /**
     * 下载网络图片
     * @param $imgurl
     * @return string
     */
    function downImgUrl($imgurl, $type = "png")
    {
        // 获取头像
        $imgPath = "../public/image/product_share_img/" . uniqid() . mt_rand(1, 200) . ".$type";

        //合成头像
        $ch = curl_init($imgurl);
        curl_setopt($ch, CURLOPT_HEADER, 0);
        curl_setopt($ch, CURLOPT_REFERER, '');
        curl_setopt($ch, CURLOPT_NOBODY, 0);
        curl_setopt($ch, CURLOPT_TIMEOUT, 10);   //只需要设置一个秒的数量就可以
        curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
        curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, false);
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
        $img = curl_exec($ch);
        curl_close($ch);
        file_put_contents($imgPath, $img);
        return $imgPath;
        exit;
    }

    //把请求发送到微信服务器换取二维码
    function httpRequest($url, $data = '', $method = 'GET')
    {
        $curl = curl_init();
        curl_setopt($curl, CURLOPT_URL, $url);
        curl_setopt($curl, CURLOPT_SSL_VERIFYPEER, 0);
        curl_setopt($curl, CURLOPT_SSL_VERIFYHOST, 0);
        curl_setopt($curl, CURLOPT_USERAGENT, $_SERVER['HTTP_USER_AGENT']);
        curl_setopt($curl, CURLOPT_FOLLOWLOCATION, 1);
        curl_setopt($curl, CURLOPT_AUTOREFERER, 1);
        if ($method == 'POST')
        {
            curl_setopt($curl, CURLOPT_POST, 1);
            if ($data != '')
            {
                curl_setopt($curl, CURLOPT_POSTFIELDS, $data);
            }
        }

        curl_setopt($curl, CURLOPT_TIMEOUT, 30);
        curl_setopt($curl, CURLOPT_HEADER, 0);
        curl_setopt($curl, CURLOPT_RETURNTRANSFER, 1);
        $result = curl_exec($curl);
        curl_close($curl);
        return $result;
    }
}