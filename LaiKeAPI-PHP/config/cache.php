<?php

return [
    'default' => 'redis',

    'stores'  => [
        'file' => [
            'type'       => 'File',
            'path'       => root_path() . 'runtime/cache/',
            'prefix'     => 'tp8_',
            'expire'     => 0,
            'tag_prefix' => 'tag:',
            'serialize'  => [],
        ],

        'redis' => [
            'type'      => 'redis',
            'host'      => '127.0.0.1',
            'port'      => 6339,
            'password'  => 'laiketui18',
            'prefix'    => 'tp8_',
            'expire'    => 0,
            'select'    => 12,
        ],
    ],
];
