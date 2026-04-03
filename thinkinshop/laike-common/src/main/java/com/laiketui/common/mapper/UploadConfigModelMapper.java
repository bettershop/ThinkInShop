package com.laiketui.common.mapper;


import com.laiketui.domain.upload.UploadConfigModel;
import tk.mybatis.mapper.common.BaseMapper;

import java.util.List;

public interface UploadConfigModelMapper extends BaseMapper<UploadConfigModel>
{
    List<UploadConfigModel> batchGetByUpservers(List<String> uploadModes);
}