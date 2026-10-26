package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.po.pojo.Region;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface RegionMapper extends BaseMapper<Region> {
    /**
     * 根据经纬度查询所在的城市信息
     */
    Region getCityByLocation(@Param("lng") Double lng, @Param("lat") Double lat);

    /**
     * 根据编码获取区域信息
     */
    Region selectByCode(@Param("regionCode") String regionCode);
}
