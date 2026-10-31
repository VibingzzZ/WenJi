package com.example.service.impl;

import com.example.exception.BusinessException;
import com.example.mapper.PointRecordMapper;
import com.example.mapper.UserMapper;
import com.example.po.pojo.PointRecord;
import com.example.service.PointService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class PointServiceImpl implements PointService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PointRecordMapper pointRecordMapper;

    /**
     * 发积分并记流水。
     * <p>
     * 幂等做法：以流水表唯一索引 user_id + source + biz_id 作为闸门。
     * 先插流水，插成功才动余额；重复请求会在 insert 处被数据库挡下，
     * 既不会重复加阅历，也不会产生第二条流水。
     * <p>
     * 顺序不能反：若先加余额再插流水，一旦流水插入重复被跳过，
     * 阅历已经加上去了，账就平不了。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addPoints(Integer userId, Integer points, String source, String description, String bizId) {
        if (bizId == null || bizId.isBlank()) {
            throw new BusinessException(400, "积分流水缺少业务幂等键");
        }

        // 1. 先写流水，唯一索引即幂等闸门
        PointRecord record = PointRecord.builder()
                .bizId(bizId)
                .userId(userId)
                .type(1) // 获得
                .source(source)
                .experience(points)
                .description(description)
                .build();
        try {
            pointRecordMapper.insert(record);
        } catch (DuplicateKeyException e) {
            // 该笔业务已发过积分，静默跳过（重复提交是正常情况，不算错误）
            log.info("积分已发放过，跳过 bizId={}", bizId);
            return;
        }

        // 2. 流水落库成功后再改余额，数据库侧原子自增
        int affected = userMapper.addExperience(userId, points);
        if (affected == 0) {
            // 用户不存在：抛异常把刚插入的流水一起回滚，避免孤儿记录
            throw new BusinessException(404, "用户不存在，阅历无法入账");
        }
    }
}
