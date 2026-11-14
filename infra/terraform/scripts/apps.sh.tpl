#!/bin/bash
set -euxo pipefail

# =====================================================================
# EC2 de microservicios: Java 17, build Maven y un systemd por servicio
# =====================================================================

dnf install -y java-17-amazon-corretto-devel git

# 2 GB de swap para el build y los 7 servicios
dd if=/dev/zero of=/swapfile bs=1M count=2048
chmod 600 /swapfile
mkswap /swapfile
swapon /swapfile
echo '/swapfile swap swap defaults 0 0' >> /etc/fstab

cd /home/ec2-user
git clone --branch ${repo_branch} ${repo_url} nexora-market-ep3
chown -R ec2-user:ec2-user nexora-market-ep3

cd nexora-market-ep3/backend
chmod +x mvnw
sudo -u ec2-user ./mvnw -DskipTests package

crear_servicio() {
  local nombre=$1
  local puerto=$2
  local schema=$3
  local jar="/home/ec2-user/nexora-market-ep3/backend/$nombre/target/$nombre-1.0.0.jar"

  if [ ! -f "$jar" ]; then
    echo ">> $nombre todavia no tiene jar; se omite"
    return
  fi

  cat > "/etc/systemd/system/$nombre.service" <<EOF
[Unit]
Description=$nombre (Nexora Market EP3)
After=network.target

[Service]
User=ec2-user
WorkingDirectory=/home/ec2-user/nexora-market-ep3/backend
Environment="DB_URL=jdbc:mysql://${db_address}:3306/$schema?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Santiago"
Environment="DB_USER=${db_username}"
Environment="DB_PASS=${db_password}"
Environment="RABBITMQ_ADDRESSES=${infra_ip}:5672,${infra_ip}:5673"
Environment="RABBITMQ_USER=${rabbit_user}"
Environment="RABBITMQ_PASS=${rabbit_pass}"
Environment="KAFKA_BOOTSTRAP=${infra_ip}:9092,${infra_ip}:9093,${infra_ip}:9094"
Environment="NEXORA_JWT_ISSUER_URI=https://login.microsoftonline.com/${entra_tenant_id}/v2.0"
Environment="NEXORA_JWT_AUDIENCE=${entra_audience}"
Environment="PORT=$puerto"
ExecStart=/usr/bin/java -Xms128m -Xmx384m -jar $jar
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
EOF

  systemctl daemon-reload
  systemctl enable --now "$nombre"
}

crear_servicio ms-productos 8081 nexora_productos
crear_servicio ms-pedidos 8082 nexora_pedidos
crear_servicio ms-pagos 8083 nexora_pagos
crear_servicio ms-notificaciones 8084 nexora_notificaciones
crear_servicio ms-analitica 8085 nexora_analitica
crear_servicio ms-kafka-admin 8086 nexora_kafka_admin
crear_servicio ms-rabbit-admin 8087 nexora_rabbit_admin

echo ">> Microservicios desplegados en los puertos 8081-8087"
