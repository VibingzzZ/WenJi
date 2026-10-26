package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.po.pojo.TravelBlog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TravelBlogMapper extends BaseMapper<TravelBlog> {
}
