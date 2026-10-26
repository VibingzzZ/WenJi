package com.example.service.impl;

import com.example.mapper.HeritageSiteMapper;
import com.example.mapper.OpeningHoursMapper;
import com.example.mapper.RegionMapper;
import com.example.mapper.SiteImageMapper;
import com.example.po.pojo.HeritageSite;
import com.example.po.pojo.OpeningHours;
import com.example.po.pojo.Region;
import com.example.service.SiteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
public class SiteServiceImpl implements SiteService {
    @Autowired
    private HeritageSiteMapper heritageSiteMapper;
    @Autowired
    private RegionMapper regionMapper;
    @Autowired
    private OpeningHoursMapper openingHoursMapper;
    @Autowired
    private SiteImageMapper siteImageMapper;

    public List<HeritageSite> getNearbyHeritageSites(Double lng, Double lat) {
        String cityCode = null;
        
        // 尝试根据经纬度获取城市
        try {
            Region currentRegion = regionMapper.getCityByLocation(lng, lat);
            if (currentRegion != null) {
                cityCode = currentRegion.getRegionCode();
                log.info("当前城市：{}", currentRegion.getName());
            } else {
                log.info("无法通过 GIS 定位城市，使用默认城市北京");
                // 如果 GIS 查询失败，使用距离排序
            }
        } catch (Exception e) {
            log.warn("GIS 空间查询失败，将按距离排序返回景点：{}", e.getMessage());
            // GIS 功能不可用时，cityCode 保持 null，SQL 会按距离排序
        }
        
        // 如果 cityCode 为 null，回退到北京
        if (cityCode == null) {
            cityCode = "110100";
            log.info("使用默认城市编码：{}", cityCode);
        }

        LocalTime now = LocalTime.now();
        int dayOfWeek = LocalDate.now().getDayOfWeek().getValue();
        
        log.info("查询景点 - 经度：{}, 纬度：{}, 城市编码：{}", lng, lat, cityCode);

        List<HeritageSite> sites = heritageSiteMapper.selectRecommendedSites(lng, lat, cityCode, 10);
        log.info("查询到 {} 个景点", sites != null ? sites.size() : 0);

        for (HeritageSite site : sites) {
            try {
                site.setImages(siteImageMapper.findBySiteId(site.getSiteId()));
            } catch (Exception e) {
                log.warn("获取景点图片失败：{}", e.getMessage());
            }
            try {
                List<OpeningHours> hours = openingHoursMapper.findBySiteId(site.getSiteId());
                site.setOpeningHours(hours);
                site.setIsOpening(checkCurrentOpeningStatus(hours, dayOfWeek, now));
            } catch (Exception e) {
                log.warn("获取开放时间失败：{}", e.getMessage());
            }
        }
        return sites;
    }

    /**
     * 检查当前时间是否在营业时间段内
     * @param hours
     * @param dayOfWeek
     * @param now
     * @return
     */
    private Boolean checkCurrentOpeningStatus(List<OpeningHours> hours, int dayOfWeek, LocalTime now) {
        if (hours == null || hours.isEmpty()) return false;
        return hours.stream()
                .filter(h -> h.getDayOfWeek() == dayOfWeek && h.getIsOpen())
                .anyMatch(h -> now.isAfter(h.getOpenTime()) && now.isBefore(h.getCloseTime()));
    }

    /**
     * 获取景点详情
     * @param siteId
     * @return
     */
    public HeritageSite getDetail(String siteId) {
        return heritageSiteMapper.getSiteDetail(siteId);
    }
}