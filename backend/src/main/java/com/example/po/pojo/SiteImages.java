package com.example.po.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@TableName("site_image")
public class SiteImages {
    @TableId(type = IdType.AUTO)
    private Long imageId;
    private String siteId;
    private String imageUrl;
    private String thumbnailUrl;
    private String title;
    private String description;
    private Integer sortOrder;
    private Boolean isCover;
    private Integer imageType;
    private Integer width;
    private Integer height;
    private Integer size;
    private String uploadBy;
    private Integer status;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}