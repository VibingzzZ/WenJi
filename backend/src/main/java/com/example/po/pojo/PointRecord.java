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
@TableName("point_record")
public class PointRecord {
    @TableId(type = IdType.AUTO)
    private Long recordId;
    /**
     * 业务幂等键，形如 blog:123 / badge:5
     * 与 user_id + source 组成唯一索引，重复发放会被数据库挡住
     */
    private String bizId;
    private Integer userId;
    private Integer type; // 1-获得，2-消耗
    private String source; // blog/badge/mall_exchange
    private Integer experience;
    private String description;
    private LocalDateTime createTime;
}
