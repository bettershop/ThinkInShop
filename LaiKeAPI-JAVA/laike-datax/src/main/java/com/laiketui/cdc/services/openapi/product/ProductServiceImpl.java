package com.laiketui.cdc.services.openapi.product;

import com.laiketui.cdc.openapi.product.ProductService;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.admin.DefaultViewVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Service
public class ProductServiceImpl implements ProductService {

    public Map<String, Object> home(MainVo vo, Integer mchId) throws LaiKeAPIException {
        return null;
    }

    
    public Map<String, Object> index(DefaultViewVo vo, HttpServletResponse response) throws LaiKeAPIException {
        return null;
    }

    
    public Map<String, Object> goodsStatus(MainVo vo, String ids) throws LaiKeAPIException {
        return null;
    }

    
    public Map<String, Object> isopen(MainVo vo) throws LaiKeAPIException {
        return null;
    }

    
    public void displaySellOut(MainVo vo) throws LaiKeAPIException {

    }
}
