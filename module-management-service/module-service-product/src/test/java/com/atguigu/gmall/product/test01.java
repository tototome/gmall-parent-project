package com.atguigu.gmall.product;


import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.InjectionConfig;
import com.baomidou.mybatisplus.generator.config.*;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;

import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;


public class test01 {
    @Test
    public void autoGenerator() {

        AutoGenerator autoGenerator = new AutoGenerator();

        GlobalConfig globalConfig = new GlobalConfig();
        globalConfig.setServiceName("%sService");
        globalConfig.setXmlName("%sMapper");
        globalConfig.setAuthor("pp");
        globalConfig.setServiceImplName("%sImplService");
        String property = System.getProperty("user.dir");
        globalConfig.setOutputDir(property+"/src/main/java");
        autoGenerator.setGlobalConfig(globalConfig);

        DataSourceConfig dataSourceConfig=new DataSourceConfig();
        dataSourceConfig.setDbType(DbType.MYSQL);
        dataSourceConfig.setUsername("root");
        dataSourceConfig.setPassword("root");
        dataSourceConfig.setDriverName("com.mysql.jdbc.Driver");
        dataSourceConfig.setUrl("jdbc:mysql://192.168.200.128:3306/gmall_product?characterEncoding=utf-8&serverTimezone=GMT%2B8");

        autoGenerator.setDataSource(dataSourceConfig);
        PackageConfig packageConfig = new PackageConfig();
        packageConfig.setParent("com.atguigu.gmall.product");
        autoGenerator.setPackageInfo(packageConfig);
        StrategyConfig strategyConfig= new StrategyConfig();
        strategyConfig.setRestControllerStyle(true);
        strategyConfig.setNaming(NamingStrategy.underline_to_camel);
        strategyConfig.setColumnNaming(NamingStrategy.underline_to_camel);

        autoGenerator.setStrategy(strategyConfig);



        InjectionConfig injectionConfig = new InjectionConfig() {
            @Override
            public void initMap() {
                Map<String, Object> map = new HashMap<>();
                map.put("paggg", "com.atguigu.gmall.product");
                this.setMap(map);
            }
        };
        autoGenerator.setCfg(injectionConfig);
        TemplateConfig templateConfig = new TemplateConfig();
        templateConfig.setEntity("");
        templateConfig.setMapper("templates/mapper2.java");
        templateConfig.setService("templates/service2.java");
        templateConfig.setServiceImpl("templates/serviceImp2l.java");
        autoGenerator.setTemplate(templateConfig);
        autoGenerator.setTemplateEngine(new FreemarkerTemplateEngine());
        autoGenerator.execute();


    }
}
