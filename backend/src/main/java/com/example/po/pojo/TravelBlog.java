package com.example.po.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("travel_blog")
public class TravelBlog {
    @TableId(type = IdType.AUTO)
    private Long blogId;
    private Integer userId;
    private String siteId;
    private String title;
    private String content;
    private String images; // JSON 字符串
    private Integer experienceEarned;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
