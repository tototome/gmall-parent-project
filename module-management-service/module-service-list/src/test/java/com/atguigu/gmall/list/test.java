package com.atguigu.gmall.list;

import com.atguigu.gmall.list.service.SearchService;
import com.atguigu.gmall.model.list.SearchParam;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
public class test {
    @Autowired
    SearchService searchService;
    @Test
    public void  test01(){
        SearchParam searchParam = new SearchParam();

        searchParam.setTrademark("2:华为");
        searchParam.setProps(new String[]{"3:8GB:运行内存","5:骁龙710:CPU型号"});
        searchParam.setPageNo(1);
        searchParam.setPageSize(3);
        searchParam.setOrder("1:asc");
        searchParam.setKeyword("魅海星蓝");
        searchParam.setCategory1Id(2L);
        searchParam.setCategory2Id(13L);
        searchParam.setCategory3Id(61L);

        searchService.doSearch(searchParam);
    }
    @Test
    public void  test02(){
        SearchParam searchParam = new SearchParam();

        searchParam.setTrademark("2:华为");
        searchParam.setProps(new String[]{"3:8GB:运行内存","5:骁龙710:CPU型号"});
        searchParam.setPageNo(1);
        searchParam.setPageSize(3);
        searchParam.setOrder("1:asc");
        searchParam.setKeyword("魅海星蓝");
        searchParam.setCategory1Id(2L);
        searchParam.setCategory2Id(13L);
        searchParam.setCategory3Id(61L);

        searchService.doSearch(searchParam);
    }
}
