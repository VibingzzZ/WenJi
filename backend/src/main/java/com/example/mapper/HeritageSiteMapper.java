package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.po.pojo.HeritageSite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface HeritageSiteMapper extends BaseMapper<HeritageSite> {
    /**
     * 根据用户当前位置和城市筛选推荐景点
     */
    List<HeritageSite> selectRecommendedSites(@Param("lng") Double lng,
                                              @Param("lat") Double lat,
                                              @Param("cityCode") String cityCode,
                                              @Param("limit") Integer limit);

    /**
     * 根据景点ID获取完整详情（包含图片和时间列表）
     */
    HeritageSite getSiteDetail(@Param("siteId") String siteId);
}
