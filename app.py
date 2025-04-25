from flask import Flask, request, jsonify
from flask_cors import CORS
import psycopg2
import random
import string
import os
import logging
from datetime import datetime, timedelta
from dotenv import load_dotenv

load_dotenv()

app = Flask(__name__)
CORS(app, resources={
    r"/api/*": {
        "origins": "*",
        "methods": ["GET", "POST", "OPTIONS"],
        "allow_headers": ["Content-Type"]
    }
})

# Настройка логирования
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


# Подключение к PostgreSQL
def get_db():
    return psycopg2.connect(
        host=os.getenv("DB_HOST"),
        database=os.getenv("DB_NAME"),
        user=os.getenv("DB_USER"),
        password=os.getenv("DB_PASSWORD")
    )


def generate_user_id():
    """Генерация уникального ID пользователя"""
    return "user#" + ''.join(random.choices(string.ascii_uppercase + string.digits, k=4))


def generate_secure_code():
    """Генерация безопасного 6-значного кода"""
    return str(random.SystemRandom().randint(100000, 999999))


@app.route('/api/send-code', methods=['POST'])
def send_code():
    try:
        data = request.get_json()
        if not data:
            return jsonify({"error": "Необходимо передать JSON данные"}), 400

        phone = data.get('phone')
        email = data.get('email')

        if not (phone or email):
            return jsonify({"error": "Укажите phone или email"}), 400

        # Валидация номера телефона
        if phone and (not phone.isdigit() or len(phone) != 11):
            return jsonify({"error": "Неверный формат телефона. Используйте 11 цифр"}), 400

        code = generate_secure_code()
        expires_at = datetime.now() + timedelta(minutes=5)

        try:
            conn = get_db()
            cur = conn.cursor()

            # Сохраняем код в базу
            cur.execute("""
                INSERT INTO verification_codes (phone, email, code, expires_at)
                VALUES (%s, %s, %s, %s)
            """, (phone, email, code, expires_at))

            conn.commit()
            logger.info(f"Код {code} отправлен для phone: {phone}, email: {email}")

            # В реальном приложении здесь должна быть отправка SMS/email
            # send_sms(phone, code) или send_email(email, code)

            return jsonify({"success": True, "message": "Код отправлен"})

        except Exception as db_error:
            conn.rollback()
            logger.error(f"Ошибка базы данных: {db_error}")
            return jsonify({"error": "Ошибка сервера"}), 500
        finally:
            if 'cur' in locals(): cur.close()
            if 'conn' in locals(): conn.close()

    except Exception as e:
        logger.error(f"Ошибка в обработке запроса: {e}")
        return jsonify({"error": "Неверный запрос"}), 400


@app.route('/api/register', methods=['POST'])
def register():
    try:
        data = request.get_json()
        if not data:
            return jsonify({"error": "Необходимо передать JSON данные"}), 400

        phone = data.get('phone')
        email = data.get('email')
        username = data.get('username')
        code = data.get('code')

        if not all([phone or email, username, code]):
            return jsonify({"error": "Необходимые поля: phone/email, username, code"}), 400

        try:
            conn = get_db()
            cur = conn.cursor()

            # Проверка кода
            cur.execute("""
                SELECT code FROM verification_codes
                WHERE (phone = %s OR email = %s)
                AND expires_at > NOW()
                ORDER BY created_at DESC LIMIT 1
            """, (phone, email))

            valid_code = cur.fetchone()

            if not valid_code or valid_code[0] != code:
                return jsonify({"error": "Неверный или просроченный код"}), 400

            # Создаем пользователя
            user_id = generate_user_id()
            cur.execute("""
                INSERT INTO users (user_id, username, phone, email, is_verified)
                VALUES (%s, %s, %s, %s, TRUE)
                RETURNING user_id
            """, (user_id, username, phone, email))

            conn.commit()
            logger.info(f"Зарегистрирован новый пользователь: {user_id}")
            return jsonify({"user_id": user_id})

        except Exception as db_error:
            conn.rollback()
            logger.error(f"Ошибка базы данных: {db_error}")
            return jsonify({"error": "Ошибка регистрации"}), 500
        finally:
            if 'cur' in locals(): cur.close()
            if 'conn' in locals(): conn.close()

    except Exception as e:
        logger.error(f"Ошибка в обработке запроса: {e}")
        return jsonify({"error": "Неверный запрос"}), 400


@app.route('/api/users', methods=['GET'])
def get_users():
    try:
        conn = get_db()
        cur = conn.cursor()
        cur.execute("SELECT user_id, username, phone, email, is_verified FROM users")
        users = cur.fetchall()

        # Преобразуем результат в список словарей
        users_list = []
        for user in users:
            users_list.append({
                "user_id": user[0],
                "username": user[1],
                "phone": user[2],
                "email": user[3],
                "is_verified": user[4]
            })

        return jsonify(users_list)

    except Exception as e:
        logger.error(f"Ошибка при получении пользователей: {e}")
        return jsonify({"error": "Ошибка сервера"}), 500
    finally:
        if 'cur' in locals(): cur.close()
        if 'conn' in locals(): conn.close()


if __name__ == '__main__':
    logger.info("Доступные эндпоинты:")
    for rule in app.url_map.iter_rules():
        logger.info(f"{rule.rule} ({', '.join(rule.methods)})")

    app.run(host='0.0.0.0', port=5000, debug=True)
