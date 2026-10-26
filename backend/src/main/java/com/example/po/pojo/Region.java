package com.example.po.pojo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@TableName("region")
public class Region {
    @TableId
    private String regionCode;//区域编码
    private String parentCode;//父级区域编码
    private String name;//区域名称
    private Integer level;//区域级别
    private BigDecimal centerLat;//区域中心点纬度
    private BigDecimal centerLng;//区域中心点经度
    private String polygon; // 存储多边形 WKT 文本
    private Integer sortOrder;
    private Boolean isActive;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}