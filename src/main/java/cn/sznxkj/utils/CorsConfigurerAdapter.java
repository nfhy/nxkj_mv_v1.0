package cn.sznxkj.utils;

import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * Created by wangll on 2016/10/11.
 */
public class CorsConfigurerAdapter extends WebMvcConfigurerAdapter
{
    @Override
    public void addCorsMappings(CorsRegistry registry)
    {
        registry.addMapping("/*").allowedOrigins("*");
    }
}

