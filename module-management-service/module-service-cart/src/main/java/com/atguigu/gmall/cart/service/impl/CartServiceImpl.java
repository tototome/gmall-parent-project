package com.atguigu.gmall.cart.service.impl;

import com.atguigu.gmall.cart.mapper.CartInfoMapper;
import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.cart.CartInfo;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.client.ProductFeignClient;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {
    @Autowired
    CartInfoMapper cartInfoMapper;
    @Autowired
    ProductFeignClient productFeignClient;
    @Autowired
    RedisTemplate redisTemplate;

    @Override
    public void addCart(Long skuId, Integer skuNum, String userId) {
        // 查询数据库看 添加的商品在购物车中没有

        CartInfo  cartInfo = cartInfoMapper.selectOne(new QueryWrapper<CartInfo>().
                eq("sku_Id", skuId).
                eq("user_Id", userId));
        if (cartInfo != null) {
            //原先在购物车中 num+1
            cartInfo.setSkuNum(cartInfo.getSkuNum() + skuNum);
            //更新价格
            cartInfo.setSkuPrice(productFeignClient.getSkuPrice(skuId).getData());
            //更新数据库
            cartInfoMapper.updateById(cartInfo);
        } else {
            cartInfo=new CartInfo();
            SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId).getData();
            // 3、根据远程接口查询到的 SkuInfo 对象设置 CartInfo 对象的属性
            cartInfo.setUserId(userId);
            cartInfo.setSkuId(skuId);
            cartInfo.setCartPrice(skuInfo.getPrice());

            // 当前 cartInfo 在数据库不存在，所以 skuNum 就是初始数量值
            cartInfo.setSkuNum(skuNum);
            cartInfo.setImgUrl(skuInfo.getSkuDefaultImg());
            cartInfo.setSkuName(skuInfo.getSkuName());
            cartInfo.setSkuPrice(skuInfo.getPrice());
            cartInfoMapper.insert(cartInfo);
        }
        //最终都要更新redis
        String cartKey = getCartKey(userId);
        HashOperations hashOperations = redisTemplate.opsForHash();
        //直接覆盖
        hashOperations.put(cartKey, skuId.toString(), cartInfo);
        //设置过期时间
        //hashOperations.getOperations().expire(cartKey, RedisConst.USER_CART_EXPIRE, TimeUnit.SECONDS);
        setCartKeyExpire(cartKey);
    }

    @Override
    public List<CartInfo> selectCart(String userId, String userTempId) {
        //数据先存缓存查
        //if (StringUtils.isEmpty(userId)) {
        //    return cartListFromCache(userTempId);
        //}else {
        //    return cartListFromCache(userId);
        //}
        //完善 考虑合并问题
        // 当临时购物车有数据 且登陆的状态下 合并
       if (StringUtils.isEmpty(userId)){
           return cartListFromCache(userTempId);
       }
       //下面都是登陆的状态 考虑是否合并
        return  getMergedCartList(userId,userTempId);


    }

    @Override
    public void modifyCartCheckStatus(String userId, Long skuId, Integer isChecked) {
        CartInfo cartInfo = cartInfoMapper.selectOne(new QueryWrapper<CartInfo>().eq("user_id", userId).eq("sku_id", skuId));
        cartInfo.setIsChecked(isChecked);
        cartInfoMapper.updateById(cartInfo);
        BoundHashOperations boundHashOperations = redisTemplate.boundHashOps(getCartKey(userId));
        CartInfo cartInfoRedis = (CartInfo) boundHashOperations.get(skuId.toString());
        if (cartInfoRedis==null){
            return;
        }
        cartInfoRedis.setIsChecked(isChecked);
        boundHashOperations.put(skuId.toString(),cartInfoRedis);

    }

    @Override
    public void removeCartItem(String userId, Long skuId) {
        // 1、修改数据库
        cartInfoMapper
                .delete(
                        new QueryWrapper<CartInfo>()
                                .eq("user_id", userId)
                                .eq("sku_id", skuId));

        // 2、修改 Redis 缓存
        String cartKey = getCartKey(userId);

        BoundHashOperations operator = redisTemplate.boundHashOps(cartKey);

        operator.delete(skuId.toString());
    }

    @Override
    public List<CartInfo> getCheckedCart(Long userId) {
        //List<CartInfo> cartInfoList = cartInfoMapper.selectList(new QueryWrapper<CartInfo>().eq("user_id", userId));
        //我们对购物车做的有缓存 从redis中拿数据 拿不到从数据库拿 缓存可能会过期
        BoundHashOperations boundHashOperations = redisTemplate.boundHashOps(getCartKey(userId.toString()));
        List<CartInfo> cartInfoList = boundHashOperations.values();
        if (CollectionUtils.isEmpty(cartInfoList)) {
            cartInfoList = cartInfoMapper.selectList(new QueryWrapper<CartInfo>().eq("user_id", userId));
        }

        List<CartInfo> cartCheckedInfo = cartInfoList.stream().filter(cartInfo -> {
            return cartInfo.getIsChecked() == 1;
        }).collect(Collectors.toList());
        return cartCheckedInfo;

    }

    private List<CartInfo> getMergedCartList(String userId, String userTempId) {
        //   代码走到这里 所处 登陆状态
        List<CartInfo> cartFormalList = cartListFromCache(userId);
        List<CartInfo> cartTempList = cartListFromCache(userTempId);
        if (CollectionUtils.isEmpty(cartFormalList) && CollectionUtils.isEmpty(cartTempList)) {
            return new ArrayList<>();
        }
        if (!CollectionUtils.isEmpty(cartTempList)) {
            if (CollectionUtils.isEmpty(cartFormalList)) {
                for (CartInfo cartInfo : cartTempList) {
                    cartInfo.setUserId(userId);
                    cartFormalList.add(cartInfo);
                }
                //return cartTempList
                //这里可以写个异步 更新数据库
            } else {
                // 判断两个购物车是否有相同商品
                // 可以将formal转成map 使用的map.contain 判断key在不在里面
                Map<String, CartInfo> cartFormalMap = cartFormalList.stream().collect(Collectors.toMap(cartInfo -> cartInfo.getSkuId().toString()
                        , cartInfo -> cartInfo));
                for (CartInfo cartInfo:cartTempList){
                    String skuId = cartInfo.getSkuId().toString();
                    if (cartFormalMap.containsKey(skuId)){
                        Integer skuNum = cartInfo.getSkuNum();
                        Integer skuNumFormal = cartFormalMap.get(skuId).getSkuNum();
                        cartFormalMap.get(skuId).setSkuNum(skuNumFormal+skuNum);
                        //合并检查状态
                        if (cartInfo.getIsChecked()==1){
                            cartFormalMap.get(skuId).setIsChecked(cartInfo.getIsChecked());
                        }
                    }else{
                        cartInfo.setUserId(userId);
                        cartFormalMap.put(cartInfo.getUserId().toString(),cartInfo);
                    }
                }
                //Map还原成List
                cartFormalList = cartFormalMap.values().stream().collect(Collectors.toList());
            }
            //更新数据库
            //删除旧数据
            cartInfoMapper.delete(new QueryWrapper<CartInfo>().eq("user_id",userId));
            cartInfoMapper.delete(new QueryWrapper<CartInfo>().eq("user_id",userTempId));
            //新数据保存数据库
            for (CartInfo cartInfo : cartFormalList) {
                cartInfoMapper.insert(cartInfo);
            }
            //删除redis 临时购物车数据
            redisTemplate.delete(getCartKey(userTempId));
            //覆盖redis旧的正式购物车数据
            Map<String, CartInfo> cartFormalMap = cartFormalList.stream().collect(Collectors.toMap(cartInfo -> cartInfo.getSkuId().toString()
                    , cartInfo -> cartInfo));
            redisTemplate.boundHashOps(getCartKey(userId)).putAll(cartFormalMap);
            setCartKeyExpire(userId);
        }

        //走到这里说明 temp为空 返回正式的就好了
        return cartFormalList;
    }


    private List<CartInfo> cartListFromCache(String userId) {
        if (StringUtils.isEmpty(userId)) {
            return new ArrayList<>();
        }
        //从redis 中拿数据
        List<CartInfo> cartList = redisTemplate.boundHashOps(getCartKey(userId)).values();


        //判断有没有数据没有数据 可能只是缓存数据到期 从数据库中查数据 有数据就返回
        if (CollectionUtils.isEmpty(cartList)) {
            //数据库查不到就返回null了
            cartList = cartListFromDB(userId);
        }
        return cartList;

    }

    private List<CartInfo> cartListFromDB(String userId) {
        List<CartInfo> cartList = new ArrayList<>();

        cartList = cartInfoMapper.selectList(new QueryWrapper<CartInfo>().eq("user_id", userId));
        if (!CollectionUtils.isEmpty(cartList)) {
            HashOperations hashOperations = redisTemplate.opsForHash();
            //数据库有数据设置到redis中
            for (CartInfo cartInfo :
                    cartList) {
                BigDecimal skuPrice = productFeignClient.getSkuPrice(cartInfo.getSkuId()).getData();
                cartInfo.setSkuPrice(skuPrice);
                hashOperations.put(getCartKey(userId).toString(), cartInfo.getSkuId().toString(), cartInfo);
            }
        }
        return cartList;
    }

    private String getCartKey(String userId) {
        return RedisConst.USER_KEY_PREFIX + userId + RedisConst.USER_CART_KEY_SUFFIX;
    }

    private void setCartKeyExpire(String cartKey) {
        redisTemplate.expire(cartKey, RedisConst.USER_CART_EXPIRE, TimeUnit.SECONDS);
    }
}
