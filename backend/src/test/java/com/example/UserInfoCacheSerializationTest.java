package com.example;

import com.example.po.vo.UserInfoVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * 用户资料缓存序列化测试。
 *
 * <p>告警点：UserInfoVO 的 createTime / updateTime / lastLoginTime 是 String 类型，
 * 却带了 {@code @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")}。如果 Jackson 在往返时
 * 把字符串按日期格式处理，写进 Redis 的就不是原值，前端会解析失败。
 * 这个测试确认往返之后字段不丢、格式不变。
 */
class UserInfoCacheSerializationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void userInfoVoJsonRoundTrip() throws Exception {
        UserInfoVO vo = new UserInfoVO();
        vo.setUsername("abin");
        vo.setRealName("廖展滨");
        vo.setGender(1);
        vo.setPhone("13800138000");
        vo.setAvatarUrl("https://example.com/a.png");
        vo.setLocation("广州");
        vo.setLevel("登堂");
        vo.setExperience(150);
        vo.setIsRealNameVerified(1);
        vo.setLastLoginTime("2026-11-07 10:00:00");
        vo.setCreateTime("2026-01-01 00:00:00");
        vo.setUpdateTime("2026-11-07 10:00:00");

        String json = objectMapper.writeValueAsString(vo);
        UserInfoVO back = objectMapper.readValue(json, UserInfoVO.class);

        assertNotNull(back);
        // 模拟 redis 命中路径会返回的字段，必须一个不少
        assertEquals("abin", back.getUsername());
        assertEquals("廖展滨", back.getRealName());
        assertEquals(Integer.valueOf(1), back.getGender());
        assertEquals("13800138000", back.getPhone());
        assertEquals("https://example.com/a.png", back.getAvatarUrl());
        assertEquals("广州", back.getLocation());
        assertEquals("登堂", back.getLevel());
        assertEquals(Integer.valueOf(150), back.getExperience());
        assertEquals(Integer.valueOf(1), back.getIsRealNameVerified());
        // 日期字段是 String，往返后必须还是原样字符串
        assertEquals("2026-11-07 10:00:00", back.getLastLoginTime());
        assertEquals("2026-01-01 00:00:00", back.getCreateTime());
        assertEquals("2026-11-07 10:00:00", back.getUpdateTime());
    }
}
