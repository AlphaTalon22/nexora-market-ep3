#!/bin/bash
set -euxo pipefail

# =====================================================================
# EC2 de infraestructura: Kafka (3 ZK + 3 brokers), Kafka UI y RabbitMQ
# =====================================================================

dnf install -y docker git
systemctl enable --now docker

mkdir -p /usr/local/lib/docker/cli-plugins
curl -fsSL https://github.com/docker/compose/releases/latest/download/docker-compose-linux-x86_64 \
  -o /usr/local/lib/docker/cli-plugins/docker-compose
chmod +x /usr/local/lib/docker/cli-plugins/docker-compose

# 2 GB de swap para que los contenedores no se queden sin memoria
dd if=/dev/zero of=/swapfile bs=1M count=2048
chmod 600 /swapfile
mkswap /swapfile
swapon /swapfile
echo '/swapfile swap swap defaults 0 0' >> /etc/fstab

cd /home/ec2-user
git clone --branch ${repo_branch} ${repo_url} nexora-market-ep3
chown -R ec2-user:ec2-user nexora-market-ep3
cd nexora-market-ep3

# IP privada propia (IMDSv2): es la IP que Kafka anuncia a los clientes
TOKEN=$(curl -s -X PUT "http://169.254.169.254/latest/api/token" \
  -H "X-aws-ec2-metadata-token-ttl-seconds: 300")
IP_PRIVADA=$(curl -s -H "X-aws-ec2-metadata-token: $TOKEN" \
  http://169.254.169.254/latest/meta-data/local-ipv4)

cat > infra/kafka/.env <<EOF
KAFKA_HOST=$IP_PRIVADA
EOF

cat > infra/rabbitmq/.env <<EOF
RABBITMQ_USER=${rabbit_user}
RABBITMQ_PASS=${rabbit_pass}
RABBITMQ_ERLANG_COOKIE=${rabbit_cookie}
EOF

cat > infra/kafka-ui/.env <<EOF
KAFKA_UI_USER=admin
KAFKA_UI_PASS=${kafka_ui_pass}
EOF

# Orden obligatorio: Kafka crea la red nexora-net; UI y RabbitMQ la usan
docker compose --env-file infra/kafka/.env -f infra/kafka/docker-compose.yml up -d
docker compose --env-file infra/kafka-ui/.env -f infra/kafka-ui/docker-compose.yml up -d
docker compose --env-file infra/rabbitmq/.env -f infra/rabbitmq/docker-compose.yml up -d

echo ">> Infraestructura lista: Kafka en $IP_PRIVADA:9092-9094"
