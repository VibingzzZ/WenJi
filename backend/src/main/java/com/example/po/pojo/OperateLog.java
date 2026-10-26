package com.example.po.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("operate_log")
public class OperateLog {
    private Integer id;//ID
    private Integer operateEmpId;//操作员工ID
    private LocalDateTime operateTime;//操作时间
    private String className;//类名
    private String methodName;//方法名
    private String methodParams;//方法参数
    private String returnValue;//返回值
    private Long costTime;//执行时长
}
