FROM eclipse-temurin:21-jre-alpine

# 1. 安装中文字体和配置工具
# Alpine 使用 apk 命令，字体包名是 wqy-zenhei (没有 fonts- 前缀)
# musl-locales 用于支持中文语言环境
RUN apk add --no-cache \
    wqy-zenhei \
    fontconfig \
    musl-locales \
    musl-locales-lang \
    && fc-cache -fv

# 2. 设置系统级环境变量
ENV LANG=C.UTF-8
ENV LC_ALL=C.UTF-8
ENV LANGUAGE=zh_CN:zh

# 创建一个目录，用于存放应用代码
WORKDIR /app

# 复制jar包到容器内
COPY target/*.jar app.jar

# 暴露端口，用于外部访问
EXPOSE 8080

# 3. 启动参数
ENTRYPOINT ["java", "-Dfile.encoding=UTF-8", "-Dsun.jnu.encoding=UTF-8", "-jar", "app.jar"]