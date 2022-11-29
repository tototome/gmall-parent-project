package com.atguigu.gmall.product.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.product.mapper.BaseCategoryViewMapper;
import com.atguigu.gmall.product.service.BaseCategoryViewService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * VIEW 服务实现类
 * </p>
 *
 * @author pp
 * @since 2022-11-19
 */
@Service
public class BaseCategoryViewImplService extends ServiceImpl<BaseCategoryViewMapper, BaseCategoryView> implements BaseCategoryViewService {

    @Override
    public List<JSONObject> getAllCategoryForPortal() {
        int index = 1;
        //查出全部数据
        List<BaseCategoryView> baseCategoryViews = baseMapper.selectList(null);
        //创建一个List<JSONObject> 后面将值设置进去
        List<JSONObject> categoryForPortal = new ArrayList<>();

        //数据分组 BaseCategoryView::getCategory1Id) 方法引用
        Map<Long, List<BaseCategoryView>> category1Map = baseCategoryViews.stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory1Id));
        System.out.println();
        Set<Long> category1Key = category1Map.keySet();
        for (Long key : category1Key) {
            index++;
            //设置一级分类属性
            List<BaseCategoryView> baseCategoryViews1 = category1Map.get(key);
            BaseCategoryView baseCategoryView = baseCategoryViews1.get(0);
            String category1Name = baseCategoryView.getCategory1Name();
            Long category1Id = baseCategoryView.getCategory1Id();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("index", index);
            jsonObject.put("categoryId", category1Id);
            jsonObject.put("categoryName", category1Name);
            //将二级属性设置到一级属性中
            //先将数据按照二级分类进行分组
            Map<Long, List<BaseCategoryView>> category2Map = baseCategoryViews1.stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory2Id));
            Set<Long> key2s = category2Map.keySet();
            List<JSONObject> category2List=new ArrayList<>();
            for (Long key2:key2s){
                List<BaseCategoryView> baseCategoryViews2 = category2Map.get(key2);
                System.out.println();
                BaseCategoryView baseCategoryView1 = baseCategoryViews2.get(0);
                Long category2Id = baseCategoryView1.getCategory2Id();
                String category2Name = baseCategoryView1.getCategory2Name();
                JSONObject jsonObject1 = new JSONObject();
                jsonObject1.put("categoryId",category2Id);
                jsonObject1.put("categoryName",category2Name);
                //设置三级数据
                List<JSONObject> category3List=new ArrayList<>();
                for (BaseCategoryView categoryView : baseCategoryViews2) {
                    Long category3Id = categoryView.getCategory3Id();
                    String category3Name = categoryView.getCategory3Name();
                    JSONObject jsonObject2 = new JSONObject();
                    jsonObject2.put("categoryId",category3Id);
                    jsonObject2.put("categoryName",category3Name);
                    category3List.add(jsonObject2);
                }
                jsonObject1.put("categoryChild",category3List);
                category2List.add(jsonObject1);
            }
            jsonObject.put("categoryChild",category2List);
            categoryForPortal.add(jsonObject);
        }
        return categoryForPortal;
    }
}
