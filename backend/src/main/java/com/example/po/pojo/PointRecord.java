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
    private Integer userId;
    private Integer type; // 1-获得，2-消耗
    private String source; // blog/badge/mall_exchange
    private Integer experience;
    private String description;
    private LocalDateTime createTime;
}
