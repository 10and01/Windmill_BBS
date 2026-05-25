#!/bin/bash
set -e

# 如果提供了数据库环境变量，动态生成 db.properties
if [ -n "$DATABASE_URL" ]; then
    echo "使用环境变量配置数据库..."
    CONFIG_PATH="/usr/local/tomcat/webapps/ROOT/WEB-INF/classes/db.properties"
    
    cat > "$CONFIG_PATH" <<EOF
# MySQL数据库配置
driverClassName=com.mysql.cj.jdbc.Driver
url=${DATABASE_URL}
username=${DB_USERNAME:-root}
password=${DB_PASSWORD:-}

# Druid连接池配置（Render 免费版内存有限，调小连接池）
initialSize=2
minIdle=1
maxActive=5
maxWait=10000
validationQuery=SELECT 1
testWhileIdle=true
timeBetweenEvictionRunsMillis=60000
removeAbandoned=true
removeAbandonedTimeout=1800
logAbandoned=true
EOF
    echo "数据库配置已写入 $CONFIG_PATH"
else
    echo "警告: 未设置 DATABASE_URL 环境变量，将使用打包时的默认配置"
fi

# 启动 Tomcat
exec /usr/local/tomcat/bin/catalina.sh run
