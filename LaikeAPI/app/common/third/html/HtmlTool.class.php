<?php

include_once 'simple_html_dom.php';

class HtmlTool
{

    //每行td个数
    const each_tr_td_num = 4;

    public function __construct($url)
    {
        $this->url = $url;
    }

    /**
     * 解析特定的html文档（linexsolutions），获取以下界面的追踪信息
     * http://eexpress.linexsolutions.com/awb/awbTracking?sno=EK284048439HK
     */
    public function getAwbTracingResult()
    {
        $html = new simple_html_dom();
        $html->load_file($this->url);
        $count = 0; //统计 Description 次数
        $rowData = new stdClass();
        $fetchDataFlag = false; //开始取数据标记
        $colnum = 0; //
        $abwTracingDataArrays = array();
        $pos = 0;
        foreach ($html->find('table tr td') as $element)
        {
            $Description = trim($element->innertext);
            if (trim($Description) == "Description")
            {
                $count = $count + 1;
            }

            if ($count == 2 && trim($Description) == "Entry By")
            {
                $fetchDataFlag = true;
                continue;
            }

            if ($fetchDataFlag)
            {
                $colnum = $colnum + 1;
                //便于取值
                $mod = $colnum % 10;
                switch ($mod)
                {
                    case 1:
                        $rowData->context = $Description;
                        break; //取第一列的值
                    case 2:
                        $rowData->Reference = $Description;
                        break; //取第二列的值
                    case 3:
                        $rowData->ftime = $Description;
                        break; //取第三列的值
                    case 4:
                        $rowData->entryBy = $Description;
                        break; //取第四列的值
                }
                if ($mod == HtmlTool::each_tr_td_num)
                {
                    //每四次处理一次
                    $abwTracingDataArrays[$pos++] = $rowData;
                    $colnum = 0;
                    $rowData = null;
                    $rowData = new stdClass();
                }
            }
        }
        return $abwTracingDataArrays;
    }
}
