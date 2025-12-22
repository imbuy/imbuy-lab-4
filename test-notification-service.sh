#!/bin/bash

# Скрипт для тестирования микросервиса уведомлений
# Использование: ./test-notification-service.sh

set -e

API_GATEWAY="http://localhost:8080"
USER_ID=1

echo "=========================================="
echo "Тестирование Notification Service"
echo "=========================================="
echo ""

# Цвета для вывода
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Функция для проверки статуса сервиса
check_service() {
    echo -e "${YELLOW}Проверка доступности API Gateway...${NC}"
    if curl -s -f "${API_GATEWAY}/actuator/health" > /dev/null 2>&1; then
        echo -e "${GREEN}✓ API Gateway доступен${NC}"
        return 0
    else
        echo -e "${RED}✗ API Gateway недоступен${NC}"
        echo "Убедитесь, что сервисы запущены: docker-compose up -d"
        return 1
    fi
}

# Функция для получения уведомлений
get_notifications() {
    echo ""
    echo -e "${YELLOW}Получение уведомлений для пользователя ${USER_ID}...${NC}"
    response=$(curl -s -X GET "${API_GATEWAY}/notifications/users/${USER_ID}?page=0&size=10")
    echo "$response" | jq '.' 2>/dev/null || echo "$response"
}

# Функция для получения количества непрочитанных уведомлений
get_unread_count() {
    echo ""
    echo -e "${YELLOW}Получение количества непрочитанных уведомлений...${NC}"
    count=$(curl -s -X GET "${API_GATEWAY}/notifications/users/${USER_ID}/unread-count")
    echo -e "${GREEN}Непрочитанных уведомлений: ${count}${NC}"
}

# Функция для отметки уведомления как прочитанного
mark_as_read() {
    local notification_id=$1
    echo ""
    echo -e "${YELLOW}Отметка уведомления ${notification_id} как прочитанного...${NC}"
    response=$(curl -s -w "\nHTTP Status: %{http_code}\n" -X PUT "${API_GATEWAY}/notifications/${notification_id}/read")
    if echo "$response" | grep -q "HTTP Status: 204"; then
        echo -e "${GREEN}✓ Уведомление отмечено как прочитанное${NC}"
    else
        echo -e "${RED}✗ Ошибка при отметке уведомления${NC}"
        echo "$response"
    fi
}

# Функция для отметки всех уведомлений как прочитанных
mark_all_as_read() {
    echo ""
    echo -e "${YELLOW}Отметка всех уведомлений пользователя ${USER_ID} как прочитанных...${NC}"
    response=$(curl -s -w "\nHTTP Status: %{http_code}\n" -X PUT "${API_GATEWAY}/notifications/users/${USER_ID}/read-all")
    if echo "$response" | grep -q "HTTP Status: 204"; then
        echo -e "${GREEN}✓ Все уведомления отмечены как прочитанные${NC}"
    else
        echo -e "${RED}✗ Ошибка при отметке уведомлений${NC}"
        echo "$response"
    fi
}

# Функция для проверки Kafka топиков
check_kafka_topics() {
    echo ""
    echo -e "${YELLOW}Проверка Kafka топиков...${NC}"
    if docker ps | grep -q kafka-1; then
        echo "Доступные топики:"
        docker exec kafka-1 kafka-topics --bootstrap-server localhost:29092 --list 2>/dev/null || echo "Ошибка при получении списка топиков"
    else
        echo -e "${RED}Kafka контейнер не запущен${NC}"
    fi
}

# Функция для отправки тестового события в Kafka
send_test_notification() {
    echo ""
    echo -e "${YELLOW}Отправка тестового уведомления в Kafka...${NC}"
    
    # Создаем JSON событие
    cat > /tmp/test-notification.json << EOF
{
  "sourceService": "test-script",
  "userId": ${USER_ID},
  "type": "WEBSOCKET",
  "title": "Test Notification from Script",
  "message": "This is a test notification sent via Kafka",
  "metadata": {
    "test": "true",
    "timestamp": "$(date -u +%Y-%m-%dT%H:%M:%SZ)"
  }
}
EOF

    if docker ps | grep -q kafka-1; then
        docker cp /tmp/test-notification.json kafka-1:/tmp/test-notification.json
        docker exec kafka-1 kafka-console-producer \
            --bootstrap-server localhost:29092 \
            --topic notifications < /tmp/test-notification.json 2>/dev/null
        
        if [ $? -eq 0 ]; then
            echo -e "${GREEN}✓ Тестовое уведомление отправлено в Kafka${NC}"
            echo "Подождите несколько секунд для обработки..."
            sleep 3
        else
            echo -e "${RED}✗ Ошибка при отправке уведомления${NC}"
        fi
        rm -f /tmp/test-notification.json
    else
        echo -e "${RED}Kafka контейнер не запущен${NC}"
    fi
}

# Функция для проверки логов notification-service
check_logs() {
    echo ""
    echo -e "${YELLOW}Последние логи notification-service (последние 20 строк):${NC}"
    if docker ps | grep -q notification-service; then
        docker logs --tail 20 notification-service 2>&1 | grep -i "notification\|websocket\|kafka" || docker logs --tail 20 notification-service
    else
        echo -e "${RED}Notification service контейнер не запущен${NC}"
    fi
}

# Главное меню
main() {
    if ! check_service; then
        exit 1
    fi

    echo ""
    echo "Выберите действие:"
    echo "1) Получить уведомления пользователя"
    echo "2) Получить количество непрочитанных уведомлений"
    echo "3) Отметить уведомление как прочитанное (нужен ID)"
    echo "4) Отметить все уведомления как прочитанные"
    echo "5) Проверить Kafka топики"
    echo "6) Отправить тестовое уведомление в Kafka"
    echo "7) Просмотреть логи notification-service"
    echo "8) Выполнить все проверки"
    echo "0) Выход"
    echo ""
    read -p "Ваш выбор: " choice

    case $choice in
        1)
            get_notifications
            ;;
        2)
            get_unread_count
            ;;
        3)
            read -p "Введите ID уведомления: " notification_id
            mark_as_read $notification_id
            ;;
        4)
            mark_all_as_read
            ;;
        5)
            check_kafka_topics
            ;;
        6)
            send_test_notification
            get_notifications
            ;;
        7)
            check_logs
            ;;
        8)
            get_notifications
            get_unread_count
            check_kafka_topics
            check_logs
            ;;
        0)
            echo "Выход..."
            exit 0
            ;;
        *)
            echo -e "${RED}Неверный выбор${NC}"
            ;;
    esac
}

# Запуск скрипта
main

