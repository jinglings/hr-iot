#!/bin/bash

################################################################################
# TDengine 数据库初始化脚本
#
# 功能: 自动创建数据库并初始化所有表结构
# 使用方式: ./init_database.sh [database_name]
# 示例: ./init_database.sh iot_energy
################################################################################

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 配置参数
DB_NAME=${1:-iot_energy}
TDENGINE_HOST=${TDENGINE_HOST:-localhost}
TDENGINE_PORT=${TDENGINE_PORT:-6041}
TDENGINE_USER=${TDENGINE_USER:-root}
TDENGINE_PASSWORD=${TDENGINE_PASSWORD:-taosdata}

# 获取脚本所在目录
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

################################################################################
# 函数定义
################################################################################

# 打印信息
print_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

# 打印警告
print_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

# 打印错误
print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 检查 TDengine 是否安装
check_tdengine() {
    print_info "检查 TDengine 是否安装..."

    if command -v taos &> /dev/null; then
        print_info "TDengine 客户端已安装"
        taos -V
        return 0
    else
        print_error "未找到 TDengine 客户端(taos)"
        print_error "请先安装 TDengine: https://www.taosdata.com/cn/getting-started/"
        return 1
    fi
}

# 检查 TDengine 服务是否运行
check_tdengine_service() {
    print_info "检查 TDengine 服务状态..."

    # 尝试连接 TDengine
    if taos -h ${TDENGINE_HOST} -P ${TDENGINE_PORT} -u ${TDENGINE_USER} -p${TDENGINE_PASSWORD} -s "SHOW DATABASES;" &> /dev/null; then
        print_info "TDengine 服务运行正常"
        return 0
    else
        print_error "无法连接到 TDengine 服务"
        print_error "主机: ${TDENGINE_HOST}:${TDENGINE_PORT}"
        print_error "请检查服务是否启动: sudo systemctl status taosd"
        return 1
    fi
}

# 创建数据库
create_database() {
    print_info "创建数据库: ${DB_NAME}"

    # 数据库参数说明:
    # KEEP 3650: 数据保留3650天(10年)
    # DURATION 10: 单个数据文件存储10天的数据
    # BUFFER 16: 写入缓存16MB
    # PAGES 256: 元数据缓存页数

    SQL="CREATE DATABASE IF NOT EXISTS ${DB_NAME}
         KEEP 3650
         DURATION 10
         BUFFER 16
         PAGES 256;"

    if taos -h ${TDENGINE_HOST} -P ${TDENGINE_PORT} -u ${TDENGINE_USER} -p${TDENGINE_PASSWORD} -s "${SQL}"; then
        print_info "数据库 ${DB_NAME} 创建成功"
        return 0
    else
        print_error "数据库创建失败"
        return 1
    fi
}

# 执行 SQL 文件
execute_sql_file() {
    local sql_file=$1
    local file_name=$(basename "$sql_file")

    print_info "执行 SQL 文件: ${file_name}"

    if [ ! -f "${sql_file}" ]; then
        print_error "SQL 文件不存在: ${sql_file}"
        return 1
    fi

    # 使用 taos 客户端执行 SQL 文件
    if taos -h ${TDENGINE_HOST} -P ${TDENGINE_PORT} -u ${TDENGINE_USER} -p${TDENGINE_PASSWORD} -d ${DB_NAME} < "${sql_file}"; then
        print_info "${file_name} 执行成功"
        return 0
    else
        print_error "${file_name} 执行失败"
        return 1
    fi
}

# 验证表是否创建成功
verify_tables() {
    print_info "验证表结构..."

    SQL="USE ${DB_NAME}; SHOW STABLES;"

    print_info "当前超级表列表:"
    taos -h ${TDENGINE_HOST} -P ${TDENGINE_PORT} -u ${TDENGINE_USER} -p${TDENGINE_PASSWORD} -s "${SQL}"

    # 检查 energy_realtime_data 表
    SQL="USE ${DB_NAME}; SHOW STABLES LIKE 'energy_realtime_data';"
    if taos -h ${TDENGINE_HOST} -P ${TDENGINE_PORT} -u ${TDENGINE_USER} -p${TDENGINE_PASSWORD} -s "${SQL}" | grep -q "energy_realtime_data"; then
        print_info "✓ energy_realtime_data 表已创建"
    else
        print_warn "✗ energy_realtime_data 表未找到"
    fi
}

# 显示使用说明
show_usage() {
    cat << EOF

${GREEN}TDengine 数据库初始化完成!${NC}

数据库配置:
  数据库名称: ${DB_NAME}
  主机地址:   ${TDENGINE_HOST}:${TDENGINE_PORT}
  用户名:     ${TDENGINE_USER}

连接方式:
  1. 命令行连接:
     taos -h ${TDENGINE_HOST} -P ${TDENGINE_PORT} -u ${TDENGINE_USER} -p ${TDENGINE_PASSWORD}

  2. 查看数据库:
     taos -h ${TDENGINE_HOST} -s "USE ${DB_NAME}; SHOW STABLES;"

  3. 应用配置(application.yaml):
     spring:
       datasource:
         tdengine:
           driver-class-name: com.taosdata.jdbc.rs.RestfulDriver
           url: jdbc:TAOS-RS://${TDENGINE_HOST}:${TDENGINE_PORT}/${DB_NAME}?timezone=UTC-8
           username: ${TDENGINE_USER}
           password: ${TDENGINE_PASSWORD}

后续操作:
  - 查看详细文档: cat ${SCRIPT_DIR}/README.md
  - 测试插入数据: 参考 README.md 中的示例

EOF
}

################################################################################
# 主流程
################################################################################

main() {
    echo "================================"
    echo "TDengine 数据库初始化工具"
    echo "================================"
    echo ""

    # 1. 检查环境
    check_tdengine || exit 1
    echo ""

    check_tdengine_service || exit 1
    echo ""

    # 2. 创建数据库
    create_database || exit 1
    echo ""

    # 3. 执行 SQL 文件
    print_info "开始执行建表脚本..."

    # 执行能源实时数据表脚本
    execute_sql_file "${SCRIPT_DIR}/energy_realtime_data.sql" || exit 1

    # 如果有其他 SQL 文件，在此添加
    # execute_sql_file "${SCRIPT_DIR}/other_table.sql" || exit 1

    echo ""

    # 4. 验证表结构
    verify_tables
    echo ""

    # 5. 显示使用说明
    show_usage

    print_info "初始化完成!"
}

# 执行主流程
main
