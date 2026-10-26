package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.po.pojo.OpeningHours;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface OpeningHoursMapper extends BaseMapper<OpeningHours> {
    /**
     * 根据站点ID查询营业时间
     * @param siteId 站点ID
     * @return 营业时间列表
     */
    List<OpeningHours> findBySiteId(String siteId);
}
