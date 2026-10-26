package com.example.service.impl;

import com.example.mapper.PointRecordMapper;
import com.example.mapper.UserMapper;
import com.example.po.pojo.PointRecord;
import com.example.po.pojo.User;
import com.example.service.PointService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PointServiceImpl implements PointService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PointRecordMapper pointRecordMapper;

    @Override
    @Transactional
    public void addPoints(Integer userId, Integer points, String source, String description) {
        // 1. 更新用户阅历
        User user = userMapper.selectById(userId);
        if (user != null) {
            Integer currentExp = user.getExperience() == null ? 0 : user.getExperience();
            user.setExperience(currentExp + points);
            userMapper.updateById(user);
        }

        // 2. 记录阅历流水
        PointRecord record = PointRecord.builder()
                .userId(userId)
                .type(1) // 获得
                .source(source)
                .experience(points)
                .description(description)
                .build();
        pointRecordMapper.insert(record);
    }
}
