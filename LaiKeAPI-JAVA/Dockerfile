FROM openjdk:8-jre-alpine

ENV TZ=Asia/Shanghai
WORKDIR /app
COPY output/*.jar /app/

# 默认启动 comps.jar（可被 docker-compose command 覆盖）
CMD ["java", "-jar", "/app/comps.jar"]