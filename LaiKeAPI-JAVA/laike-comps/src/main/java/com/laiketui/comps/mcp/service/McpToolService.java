package com.laiketui.comps.mcp.service;

import com.alibaba.fastjson2.JSON;
import com.laiketui.common.mapper.OrderModelMapper;
import com.laiketui.common.mapper.ProductListModelMapper;
import com.laiketui.common.mapper.UserMapper;
import com.laiketui.domain.order.OrderModel;
import com.laiketui.domain.product.ProductListModel;
import com.laiketui.domain.user.User;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.util.StringUtil;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * MCP工具服务
 * 处理用户、订单、商品数据的CRUD操作
 *
 * @author wangxian
 * @date 2026-03-18
 */
@Service
public class McpToolService {

    private static final Logger logger = LoggerFactory.getLogger(McpToolService.class);

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private OrderModelMapper orderModelMapper;

    @Autowired
    private ProductListModelMapper productListModelMapper;

    /**
     * 执行工具调用
     *
     * @param toolName 工具名称
     * @param arguments 参数
     * @param request HTTP请求
     * @return 返回结果
     */
    public String executeTool(String toolName, Map<String, Object> arguments, HttpServletRequest request) {
        try {
            switch (toolName) {
                case "get_user":
                    return getUser(arguments);
                case "list_users":
                    return listUsers(arguments);
                case "get_order":
                    return getOrder(arguments);
                case "list_orders":
                    return listOrders(arguments);
                case "get_product":
                    return getProduct(arguments);
                case "list_products":
                    return listProducts(arguments);
                default:
                    return JSON.toJSONString(buildErrorResult("Tool not found: " + toolName));
            }
        } catch (Exception e) {
            logger.error("执行工具失败: {}", toolName, e);
            return JSON.toJSONString(buildErrorResult("Execution error: " + e.getMessage()));
        }
    }

    /**
     * 获取用户信息
     *
     * @param arguments 参数
     * @return JSON字符串
     */
    private String getUser(Map<String, Object> arguments) {
        try {
            String userId = getStringArgument(arguments, "user_id");
            Integer storeId = getIntArgument(arguments, "store_id");

            if (StringUtil.isEmpty(userId)) {
                return JSON.toJSONString(buildErrorResult("user_id is required"));
            }

            User example = new User();
            example.setUser_id(userId);
            if (storeId != null) {
                example.setStore_id(storeId);
            }

            User user = userMapper.selectOne(example);

            if (user == null) {
                return JSON.toJSONString(buildErrorResult("User not found"));
            }

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", convertUserToMap(user));
            return JSON.toJSONString(result);

        } catch (Exception e) {
            logger.error("获取用户信息失败", e);
            return JSON.toJSONString(buildErrorResult("Failed to get user: " + e.getMessage()));
        }
    }

    /**
     * 查询用户列表
     *
     * @param arguments 参数
     * @return JSON字符串
     */
    private String listUsers(Map<String, Object> arguments) {
        try {
            Integer storeId = getIntArgument(arguments, "store_id");
            Integer pageNum = getIntArgument(arguments, "page", 1);
            Integer pageSize = getIntArgument(arguments, "page_size", 10);

            User example = new User();
            if (storeId != null) {
                example.setStore_id(storeId);
            }
            List<User> users = userMapper.selectByRowBounds(example, buildRowBounds(pageNum, pageSize));

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", users.stream().map(this::convertUserToMap).toArray());
            result.put("total", users.size());
            result.put("page", pageNum);
            result.put("page_size", pageSize);
            return JSON.toJSONString(result);

        } catch (Exception e) {
            logger.error("查询用户列表失败", e);
            return JSON.toJSONString(buildErrorResult("Failed to list users: " + e.getMessage()));
        }
    }

    /**
     * 获取订单信息
     *
     * @param arguments 参数
     * @return JSON字符串
     */
    private String getOrder(Map<String, Object> arguments) {
        try {
            String orderNo = getStringArgument(arguments, "order_no");
            Integer storeId = getIntArgument(arguments, "store_id");

            if (StringUtil.isEmpty(orderNo)) {
                return JSON.toJSONString(buildErrorResult("order_no is required"));
            }

            OrderModel example = new OrderModel();
            example.setsNo(orderNo);
            if (storeId != null) {
                example.setStore_id(storeId);
            }

            OrderModel order = orderModelMapper.selectOne(example);

            if (order == null) {
                return JSON.toJSONString(buildErrorResult("Order not found"));
            }

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", convertOrderToMap(order));
            return JSON.toJSONString(result);

        } catch (Exception e) {
            logger.error("获取订单信息失败", e);
            return JSON.toJSONString(buildErrorResult("Failed to get order: " + e.getMessage()));
        }
    }

    /**
     * 查询订单列表
     *
     * @param arguments 参数
     * @return JSON字符串
     */
    private String listOrders(Map<String, Object> arguments) {
        try {
            Integer storeId = getIntArgument(arguments, "store_id");
            Integer pageNum = getIntArgument(arguments, "page", 1);
            Integer pageSize = getIntArgument(arguments, "page_size", 10);
            String status = getStringArgument(arguments, "status");
            String userId = getStringArgument(arguments, "user_id");

            OrderModel example = new OrderModel();
            if (storeId != null) {
                example.setStore_id(storeId);
            }
            if (StringUtil.isNotEmpty(userId)) {
                example.setUser_id(userId);
            }
            if (StringUtil.isNotEmpty(status)) {
                try {
                    example.setStatus(Integer.parseInt(status));
                } catch (NumberFormatException e) {
                    // ignore
                }
            }
            List<OrderModel> orders = orderModelMapper.selectByRowBounds(example, buildRowBounds(pageNum, pageSize));

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", orders.stream().map(this::convertOrderToMap).toArray());
            result.put("total", orders.size());
            result.put("page", pageNum);
            result.put("page_size", pageSize);
            return JSON.toJSONString(result);

        } catch (Exception e) {
            logger.error("查询订单列表失败", e);
            return JSON.toJSONString(buildErrorResult("Failed to list orders: " + e.getMessage()));
        }
    }

    /**
     * 获取商品信息
     *
     * @param arguments 参数
     * @return JSON字符串
     */
    private String getProduct(Map<String, Object> arguments) {
        try {
            Integer productId = getIntArgument(arguments, "product_id");
            Integer storeId = getIntArgument(arguments, "store_id");

            if (productId == null) {
                return JSON.toJSONString(buildErrorResult("product_id is required"));
            }

            ProductListModel example = new ProductListModel();
            example.setId(productId);
            if (storeId != null) {
                example.setStore_id(storeId);
            }

            ProductListModel product = productListModelMapper.selectOne(example);

            if (product == null) {
                return JSON.toJSONString(buildErrorResult("Product not found"));
            }

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", convertProductToMap(product));
            return JSON.toJSONString(result);

        } catch (Exception e) {
            logger.error("获取商品信息失败", e);
            return JSON.toJSONString(buildErrorResult("Failed to get product: " + e.getMessage()));
        }
    }

    /**
     * 查询商品列表
     *
     * @param arguments 参数
     * @return JSON字符串
     */
    private String listProducts(Map<String, Object> arguments) {
        try {
            Integer storeId = getIntArgument(arguments, "store_id");
            Integer pageNum = getIntArgument(arguments, "page", 1);
            Integer pageSize = getIntArgument(arguments, "page_size", 10);
            String status = getStringArgument(arguments, "status");

            ProductListModel example = new ProductListModel();
            if (storeId != null) {
                example.setStore_id(storeId);
            }
            if (StringUtil.isNotEmpty(status)) {
                example.setStatus(status);
            }
            List<ProductListModel> products = productListModelMapper.selectByRowBounds(example, buildRowBounds(pageNum, pageSize));

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", products.stream().map(this::convertProductToMap).toArray());
            result.put("total", products.size());
            result.put("page", pageNum);
            result.put("page_size", pageSize);
            return JSON.toJSONString(result);

        } catch (Exception e) {
            logger.error("查询商品列表失败", e);
            return JSON.toJSONString(buildErrorResult("Failed to list products: " + e.getMessage()));
        }
    }

    /**
     * 转换User对象为Map
     */
    private Map<String, Object> convertUserToMap(User user) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", user.getId());
        map.put("user_id", user.getUser_id());
        map.put("user_name", user.getUser_name());
        map.put("store_id", user.getStore_id());
        map.put("mobile", user.getMobile());
        map.put("email", user.getE_mail());
        map.put("real_name", user.getReal_name());
        map.put("money", user.getMoney());
        map.put("score", user.getScore());
        map.put("grade", user.getGrade());
        map.put("sex", user.getSex());
        map.put("headimgurl", user.getHeadimgurl());
        map.put("register_data", user.getRegister_data());
        map.put("last_time", user.getLast_time());
        map.put("is_lock", user.getIs_lock());
        return map;
    }

    /**
     * 转换OrderModel对象为Map
     */
    private Map<String, Object> convertOrderToMap(OrderModel order) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", order.getId());
        map.put("order_no", order.getsNo());
        map.put("store_id", order.getStore_id());
        map.put("user_id", order.getUser_id());
        map.put("name", order.getName());
        map.put("mobile", order.getMobile());
        map.put("total_price", order.getZ_price());
        map.put("status", order.getStatus());
        map.put("pay", order.getPay());
        map.put("add_time", order.getAdd_time());
        map.put("pay_time", order.getPay_time());
        map.put("arrive_time", order.getArrive_time());
        map.put("address", order.getAddress());
        map.put("remark", order.getRemark());
        map.put("otype", order.getOtype());
        map.put("ptcode", order.getPtcode());
        return map;
    }

    /**
     * 转换ProductListModel对象为Map
     */
    private Map<String, Object> convertProductToMap(ProductListModel product) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", product.getId());
        map.put("product_title", product.getProduct_title());
        map.put("store_id", product.getStore_id());
        map.put("product_number", product.getProduct_number());
        map.put("imgurl", product.getImgurl());
        map.put("price", "0");
        map.put("num", product.getNum());
        map.put("status", product.getStatus());
        map.put("volume", product.getVolume());
        map.put("add_date", product.getAdd_date());
        map.put("commodity_type", product.getCommodity_type());
        return map;
    }

    /**
     * 构建错误结果
     */
    private Map<String, Object> buildErrorResult(String message) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("error", message);
        return result;
    }

    /**
     * 获取String类型参数
     */
    private String getStringArgument(Map<String, Object> arguments, String key) {
        if (arguments == null || !arguments.containsKey(key)) {
            return null;
        }
        Object value = arguments.get(key);
        return value != null ? value.toString() : null;
    }

    /**
     * 获取Integer类型参数
     */
    private Integer getIntArgument(Map<String, Object> arguments, String key) {
        if (arguments == null || !arguments.containsKey(key)) {
            return null;
        }
        Object value = arguments.get(key);
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        } else if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return null;
    }

    /**
     * 获取Integer类型参数，带默认值
     */
    private Integer getIntArgument(Map<String, Object> arguments, String key, int defaultValue) {
        Integer value = getIntArgument(arguments, key);
        return value != null ? value : defaultValue;
    }

    private RowBounds buildRowBounds(Integer pageNum, Integer pageSize) {
        int safePageNum = pageNum == null || pageNum < 1 ? 1 : pageNum;
        int safePageSize = pageSize == null || pageSize < 1 ? 10 : pageSize;
        int offset = (safePageNum - 1) * safePageSize;
        return new RowBounds(offset, safePageSize);
    }
}
