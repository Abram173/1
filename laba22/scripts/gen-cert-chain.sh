#!/bin/bash
# Генерация цепочки сертификатов (3 звена) для Лабы 6.
# В сертификатах присутствует идентификатор студента (номер студенческого билета).
# Имена: BankRootCA, BankInterCA, BankServerCert (не из примера).

set -e

# Номер студенческого билета — задай одним из способов:
#   1) Первый аргумент:  ./gen-cert-chain.sh 12345678
#   2) Переменная:      export STUDENT_ID=12345678 && ./gen-cert-chain.sh
# Второй аргумент — каталог для сертификатов (по умолчанию ./certs).
STUDENT_ID="${STUDENT_ID:-$1}"
STUDENT_ID="${STUDENT_ID:-STUDENT_ID_PLACEHOLDER}"
OUT_DIR="${2:-./certs}"
[ -n "$1" ] && [ -z "$2" ] && OUT_DIR="./certs"
DAYS=3650

mkdir -p "$OUT_DIR"
cd "$OUT_DIR"

# 1) Корневой CA (первое звено)
openssl genrsa -out BankRootCA.key 4096
openssl req -new -x509 -days $DAYS -key BankRootCA.key -out BankRootCA.crt \
  -subj "/C=RU/ST=Moscow/L=Moscow/O=BankLab-${STUDENT_ID}/OU=Root/CN=BankRootCA"

# 2) Промежуточный CA (второе звено)
openssl genrsa -out BankInterCA.key 4096
openssl req -new -key BankInterCA.key -out BankInterCA.csr \
  -subj "/C=RU/ST=Moscow/L=Moscow/O=BankLab-${STUDENT_ID}/OU=Intermediate/CN=BankInterCA"
openssl x509 -req -in BankInterCA.csr -CA BankRootCA.crt -CAkey BankRootCA.key \
  -CAcreateserial -out BankInterCA.crt -days $DAYS

# 3) Серверный сертификат (третье звено)
openssl genrsa -out BankServerCert.key 2048
openssl req -new -key BankServerCert.key -out BankServerCert.csr \
  -subj "/C=RU/ST=Moscow/L=Moscow/O=BankLab-${STUDENT_ID}/OU=Server/CN=localhost"
# SAN для localhost и 127.0.0.1
cat > BankServerCert.ext << EOF
authorityKeyIdentifier=keyid,issuer
basicConstraints=CA:FALSE
keyUsage=digitalSignature,keyEncipherment
subjectAltName=@alt
[alt]
DNS.1=localhost
IP.1=127.0.0.1
EOF
openssl x509 -req -in BankServerCert.csr -CA BankInterCA.crt -CAkey BankInterCA.key \
  -CAcreateserial -out BankServerCert.crt -days $DAYS -extfile BankServerCert.ext

# PKCS12 keystore для Spring Boot (серверный сертификат + цепочка)
openssl pkcs12 -export -out bank-server.p12 -inkey BankServerCert.key -in BankServerCert.crt \
  -certfile BankInterCA.crt -chain -name bank-server -passout pass:changeit

echo "Done. Certificates in $OUT_DIR"
echo "Root CA (добавь в доверенные): $OUT_DIR/BankRootCA.crt"
echo "Keystore for app: $OUT_DIR/bank-server.p12 (password: changeit)"
