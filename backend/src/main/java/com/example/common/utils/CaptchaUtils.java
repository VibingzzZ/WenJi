package com.example.common.utils;

import lombok.Getter;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Random;

public class CaptchaUtils {

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int WIDTH = 120;
    private static final int HEIGHT = 40;
    private static final int CODE_LENGTH = 4;

    @Getter
    public static class CaptchaResult {
        // getters...
        private String code;
        private String base64Image;

        public CaptchaResult(String code, String base64Image) {
            this.code = code;
            this.base64Image = base64Image;
        }

    }

    /**
     * 生成验证实体类
     *
     * @return 验证码结果
     */
    public static CaptchaResult generateCaptcha() {
        // 生成随机验证码
        String code = generateRandomCode();

        // 创建图片
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        // 设置背景
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, WIDTH, HEIGHT);

        // 添加干扰线
        drawInterferenceLines(g);

        // 绘制验证码
        drawCaptchaCode(g, code);

        // 释放资源
        g.dispose();

        // 转换为Base64
        String base64Image = imageToBase64(image);

        return new CaptchaResult(code, base64Image);
    }

    /**
     * 生成随机验证码
     *
     * @return 验证码
     */
    private static String generateRandomCode() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }

    /**
     * 绘制干扰线
     * @param g Graphics2D对象
     */
    private static void drawInterferenceLines(Graphics2D g) {
        g.setColor(Color.LIGHT_GRAY);
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            int x1 = random.nextInt(WIDTH);
            int y1 = random.nextInt(HEIGHT);
            int x2 = random.nextInt(WIDTH);
            int y2 = random.nextInt(HEIGHT);
            g.drawLine(x1, y1, x2, y2);
        }
    }

    /**
     * 绘制验证码
     * @param g Graphics2D对象
     * @param code 验证码
     */
    private static void drawCaptchaCode(Graphics2D g, String code) {
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 24));
        Random random = new Random();

        for (int i = 0; i < code.length(); i++) {
            int x = 20 + i * 20;
            int y = 25 + random.nextInt(10);
            g.drawString(String.valueOf(code.charAt(i)), x, y);
        }
    }

    /**
     * 将图片转换为Base64
     * @param image 图片
     * @return Base64字符串
     */
    private static String imageToBase64(BufferedImage image) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            byte[] bytes = baos.toByteArray();
            return Base64.getEncoder().encodeToString(bytes);
        } catch (IOException e) {
            throw new RuntimeException("验证码生成失败", e);
        }
    }
}
