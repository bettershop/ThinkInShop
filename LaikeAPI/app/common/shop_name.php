<?php

$shop_name = array(
    '自营'
);
$buf = get_included_files();
if (count($buf) == 1) {
    header('Content-Type:text/html;charset=utf-8;');
    highlight_file(__FILE__);
    return $shop_name;
}
?>