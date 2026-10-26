package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.po.pojo.SiteImages;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SiteImageMapper extends BaseMapper<SiteImages> {
    /**
     * 根据站点ID查询所有图片
     * @param siteId 站点ID
     * @return 图片列表
     */
    List<SiteImages> findBySiteId(String siteId);
}
