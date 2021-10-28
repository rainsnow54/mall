package priv.jesse.mall.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import priv.jesse.mall.entity.OrderItem;
import priv.jesse.mall.entity.Product;
import priv.jesse.mall.entity.User;
import priv.jesse.mall.service.ProductService;
import priv.jesse.mall.service.ShopCartService;
import priv.jesse.mall.service.exception.LoginException;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author hfb
 * @date 2017/11/21
 */
@Service
public class ShopCartServiceImpl implements ShopCartService {

    @Autowired
    private ProductService productService;

    /**
     * 加购物车
     * 将商品id保存到Session中List<Integer>中
     *
     * @param productId
     * @param request
     */
    @Override
    public void addCart(int productId, HttpServletRequest request) throws Exception {//将商品id加入到用户对应的session中
        User loginUser = (User) request.getSession().getAttribute("user");
        if (loginUser == null)
            throw new Exception("未登录！请重新登录");
        List<Integer> productIds = (List<Integer>) request.getSession().getAttribute(NAME_PREFIX + loginUser.getId());//拼接字符串，String NAME_PREFIX = "shop_cart_";
        if (productIds == null) {
            productIds = new ArrayList<>();//数组
            request.getSession().setAttribute(NAME_PREFIX + loginUser.getId(), productIds);//键值对  键为"shop_cart_+该用户的id" 值为“商品id”
        }
        productIds.add(productId); //把商品id加入该用户对应的商品数组里
    }

    /**
     * 移除
     *
     * 移除session List中对应的商品Id
     *
     * @param productId
     * @param request
     */
    @Override
    public void remove(int productId, HttpServletRequest request) throws Exception {//根据商品id删除
        User loginUser = (User) request.getSession().getAttribute("user");
        if (loginUser == null)
            throw new Exception("未登录！请重新登录");
        List<Integer> productIds = (List<Integer>) request.getSession().getAttribute(NAME_PREFIX + loginUser.getId());
        Iterator<Integer> iterator = productIds.iterator();
        while (iterator.hasNext()) {
            if (productId == iterator.next()) {
                iterator.remove();
            }
        }
    }

    /**
     * 查看购物车
     *
     * 查询出session的List中所有的商品Id,并封装成List<OrderItem>返回
     *
     * @param request
     * @return
     */
    @Override
    public List<OrderItem> listCart(HttpServletRequest request) throws Exception {
        User loginUser = (User) request.getSession().getAttribute("user");
        if (loginUser == null)
            throw new Exception("未登录！请重新登录");
        //得到该用户选择的所有商品的id数组 为productIds
        List<Integer> productIds = (List<Integer>) request.getSession().getAttribute(NAME_PREFIX + loginUser.getId());
        // key: productId value:OrderItem
        Map<Integer, OrderItem> productMap = new HashMap<>();//每个商品id对应一个OrderItem
        if (productIds == null){
            return new ArrayList<>();
        }
            // 遍历List中的商品id，每个商品Id对应一个OrderItem对象
        for (Integer productId : productIds) {
            if (productMap.get(productId) == null) {//添加第一个商品时productMap为空
                Product product = productService.findById(productId);
                OrderItem orderItem = new OrderItem();
                orderItem.setProduct(product);
                orderItem.setProductId(productId);
                orderItem.setCount(1);
                orderItem.setSubTotal(product.getShopPrice());
                productMap.put(productId, orderItem);//每个商品对应一个订单
            } else {//productMap中有该商品id对应的OrderItem
                OrderItem orderItem = productMap.get(productId);
                int count = orderItem.getCount();
                orderItem.setCount(++count);
                Double subTotal = orderItem.getSubTotal();   //总价逻辑错误
                orderItem.setSubTotal(orderItem.getSubTotal()+subTotal);
                productMap.put(productId, orderItem);
            }
        }
        List<OrderItem> orderItems = new ArrayList<>(productMap.values());
        return orderItems;
    }
}
