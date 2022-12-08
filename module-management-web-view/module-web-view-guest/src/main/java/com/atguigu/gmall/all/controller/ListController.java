package com.atguigu.gmall.all.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.list.client.ListFeignClient;
import com.atguigu.gmall.model.list.SearchParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Controller
public class ListController {
    @Autowired
    private ListFeignClient listFeignClient;

    @GetMapping("/list.html")
    public String search(SearchParam searchParam, Model model, HttpServletRequest request) throws Throwable {
        Enumeration<String> userId = request.getHeaders("userId");
        System.out.println();
        //调用接口方法
        Result<Map> list = listFeignClient.list(searchParam);
        //返回值设置到 请求域中
        model.addAllAttributes(list.getData());
        //通过searchParam 对象来确定用户的检索条件是什么？
        String urlParam = makeUrlParam(searchParam);
        // 页面需要 trademarkParam
        String trademark = makeTrademark(searchParam.getTrademark());
        // 存储平台属性 ${propsParamList} 页面需要
        List<Map<String, String>> propsList = makeProps(searchParam.getProps());
        // 页面需要一个 ${orderMap.type} 指按照哪个字段进行排序
        // ${orderMap.sort} 指按照哪个排序规则进行排序
        /*
        class OrderMap{
            Long type;
            String sort;
        }
        看作一个Map
        map.put("type",1);
        map.put("sort","desc");
        */
        Map<String, Object> map = orderDetail(searchParam.getOrder());
        // 保存排序规则！
        model.addAttribute("orderMap",map);
        // 存储平台属性
        model.addAttribute("propsParamList",propsList);
        // 条件拼接记录者 ${urlParam}
        model.addAttribute("urlParam",urlParam);

        // 前端需要存储一个searchParam
        model.addAttribute("searchParam",searchParam);
        // 存储品牌的
        model.addAttribute("trademarkParam",trademark);
        return "list/index";
    }
    // order=1:asc
    private Map<String, Object> orderDetail(String order) {
        Map<String,Object> map = new HashMap<>();
        // 判断传递的数据不能为空！
        if (!StringUtils.isEmpty(order)){
            // order=1:asc 拆分数据
            String[] split = order.split(":");
            if (split!=null && split.length==2){
                map.put("type",split[0]);
                map.put("sort",split[1]);
            }
        }else {
            // 给一个默认的排序规则
            map.put("type","1");
            map.put("sort","asc");
        }
        return map;
    }

    //prop = 23:4G:运行内存
    private List<Map<String, String>> makeProps(String[] props) {
        List<Map<String, String>> list = new ArrayList<>();
        if (props != null && props.length > 0) {
            for (String prop : props) {
                String[] split = prop.split(":");
                Map<String, String> map = new HashMap<>();
                map.put("attrId", split[0]);
                map.put("attrValue", split[1]);
                map.put("attrName", split[2]);
                list.add(map);
            }
        }
        return list;
    }

    private String makeTrademark(String trademark) {
        if (trademark != null) {
            String[] split = trademark.split(":");
            if (split != null && split.length == 2) {
                return "品牌：" + split[1];
            }
        }
        return null;
    }

    private String makeUrlParam(SearchParam searchParam) {
        //StringBuffer线程安全 加了同步锁
        StringBuilder urlParam = new StringBuilder();

        if (searchParam.getKeyword() != null) {
            urlParam.append("keyWord=").append(searchParam.getKeyword());
        }
        // 追加分类信息 因为分类只能点击一个 所以不需要用&连接
        if (searchParam.getCategory1Id() != null) {
            urlParam.append("category1Id=").append(searchParam.getCategory1Id());
        }
        if (searchParam.getCategory2Id() != null) {
            urlParam.append("category2Id=").append(searchParam.getCategory2Id());
        }
        if (searchParam.getCategory3Id() != null) {
            urlParam.append("category3Id=").append(searchParam.getCategory3Id());
        }
        //追加品牌信息
        if (searchParam.getProps() != null) {
            for (String prop : searchParam.getProps()) {
                if (urlParam.length() > 0) {
                    urlParam.append("&props=").append(prop);
                }
            }
        }
        return "list.html?" + urlParam.toString();
    }

}
