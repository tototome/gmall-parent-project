package com.atguigu.gmall.product.aopcache;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Documented
@Retention(value = RetentionPolicy.RUNTIME)
public @interface GmallCache {

    String prefix() default  "cache";
}
