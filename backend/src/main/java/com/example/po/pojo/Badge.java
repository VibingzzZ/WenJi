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
@TableName("badge")
public class Badge {
    @TableId(type = IdType.AUTO)
    private Integer badgeId;
    private String name;
    private String description;
    private String iconUrl;
    private String type;
    private Integer threshold;
    private Integer experienceBonus;
    private LocalDateTime createTime;
}
