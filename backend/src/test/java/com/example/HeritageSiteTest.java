package com.example;

import com.example.common.Result;
import com.example.po.pojo.HeritageSite;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 遗址景点测试类
 * 使用Spring Boot测试框架进行集成测试
 * 配置自动配置MockMvc和自动装配测试组件
 */
@SpringBootTest
@AutoConfigureMockMvc
public class HeritageSiteTest {

    /**
     * 自动装配MockMvc对象，用于模拟HTTP请求
     */
    @Autowired
    private MockMvc mockMvc;

    /**
     * 自动装配ObjectMapper对象，用于JSON序列化和反序列化
     */
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 测试获取附近景点的方法
     * @throws Exception 可能抛出的异常
     */
    @Test  // 标记这是一个JUnit测试方法
    void testGetNearbySites() throws Exception {  // 测试获取附近景点的方法，声明可能抛出异常
        // 景德镇经纬度 - 定义测试用的地理位置坐标
        Double lng = 117.17;  // 经度值
        Double lat = 29.27;   // 纬度值



    // 执行HTTP GET请求并验证响应
        MvcResult result = mockMvc.perform(get("/map/initial")  // 发送GET请求到/map/initial接口
                .param("lng", lng.toString())  // 添加经度参数
                .param("lat", lat.toString()))  // 添加纬度参数
                .andExpect(status().isOk())  // 期望HTTP状态码为200(OK)
                .andReturn();  // 获取执行结果



    // 处理响应结果
        String content = result.getResponse().getContentAsString();  // 获取响应内容字符串
        Result<List<HeritageSite>> response = objectMapper.readValue(content, new TypeReference<>() {});  // 将响应内容转换为Result对象



    // 验证测试结果
        assertEquals(1, response.getCode());  // 验证响应码是否为1(成功)
        assertNotNull(response.getData());  // 验证响应数据是否不为空
        System.out.println("测试结果：成功获取附近景点，数量：" + response.getData().size());  // 输出测试结果信息
    }

    /**
     * 测试获取景点详情的方法
     * @throws Exception 可能抛出的异常
     */
    @Test
    void testGetSiteDetail() throws Exception {
        // 假设数据库中存在 ID 为 "1" 的景点 (实际测试时需确保数据存在)
        String siteId = "1"; // 定义测试用的景点ID



        // 执行GET请求并验证响应状态码
        MvcResult result = mockMvc.perform(get("/map/" + siteId))  // 发送GET请求到指定URL
                .andExpect(status().isOk())  // 预期响应状态码为200
                .andReturn();  // 获取请求结果



        // 获取响应内容并解析为Result对象
        String content = result.getResponse().getContentAsString();  // 获取响应内容字符串
        Result<HeritageSite> response = objectMapper.readValue(content, new TypeReference<>() {});  // 将JSON字符串转换为Result对象



        // 根据响应码判断测试结果
        if (response.getCode() == 1) {  // 如果响应码为1表示成功
            assertNotNull(response.getData());  // 验证响应数据不为空
            assertEquals(siteId, response.getData().getSiteId());  // 验证返回的景点ID与请求的一致
            System.out.println("测试结果：成功获取景点详情：" + response.getData().getName());  // 打印成功信息
        } else {  // 如果响应码不为1
            System.out.println("测试提示：未找到 ID 为 " + siteId + " 的景点数据，请检查数据库。");  // 打印提示信息
        }
    }
}
