# 构建阶段
FROM maven:3.9-eclipse-temurin-11-alpine AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# 运行阶段
FROM tomcat:9.0-jdk11-temurin

# 设置容器时区为中国东八区
ENV TZ=Asia/Shanghai
RUN apt-get update && apt-get install -y tzdata && rm -rf /var/lib/apt/lists/* \
    && ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 删除 Tomcat 默认应用
RUN rm -rf /usr/local/tomcat/webapps/*

# 解压 WAR 包到 ROOT 目录（这样可以直接修改配置文件）
COPY --from=builder /app/target/enterprise-bbs.war /tmp/enterprise-bbs.war
RUN mkdir -p /usr/local/tomcat/webapps/ROOT && \
    cd /usr/local/tomcat/webapps/ROOT && \
    jar xf /tmp/enterprise-bbs.war && \
    rm /tmp/enterprise-bbs.war

# 复制启动脚本
COPY docker-entrypoint.sh /usr/local/bin/
RUN chmod +x /usr/local/bin/docker-entrypoint.sh

# 暴露端口
EXPOSE 8080

# 使用自定义启动脚本
ENTRYPOINT ["docker-entrypoint.sh"]
