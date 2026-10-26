package com.example.config;

public class LevelConfig {
    //根据经验值获取等级
    public static String getLevelByExp(int exp) {
        // 处理负数经验的情况
        if (exp < 0) {
            return "见习学徒"; // 或抛出异常，根据业务需求决定
        }

        // 按照经验从高到低排列，便于顺序比较
        if (exp >= 10000) return "晶钻贤者";
        if (exp >= 5000) return "翠玉守护者";
        if (exp >= 2000) return "鎏金匠人";
        if (exp >= 500) return "银辉行者";
        if (exp >= 100) return "青铜学者";
        return "见习学徒";
    }
}
