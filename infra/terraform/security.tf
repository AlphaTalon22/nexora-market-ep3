resource "aws_security_group" "infra" {
  name        = "nexora-infra-sg"
  description = "Kafka, Kafka UI y RabbitMQ"
  vpc_id      = data.aws_vpc.default.id
}

resource "aws_security_group" "apps" {
  name        = "nexora-apps-sg"
  description = "Microservicios de Nexora Market"
  vpc_id      = data.aws_vpc.default.id
}

resource "aws_security_group" "rds" {
  name        = "nexora-rds-sg"
  description = "MySQL de Nexora Market"
  vpc_id      = data.aws_vpc.default.id
}

# ---------- Salida a Internet (Docker, Java, git clone) ----------

resource "aws_vpc_security_group_egress_rule" "infra" {
  security_group_id = aws_security_group.infra.id
  ip_protocol       = "-1"
  cidr_ipv4         = "0.0.0.0/0"
}

resource "aws_vpc_security_group_egress_rule" "apps" {
  security_group_id = aws_security_group.apps.id
  ip_protocol       = "-1"
  cidr_ipv4         = "0.0.0.0/0"
}

resource "aws_vpc_security_group_egress_rule" "rds" {
  security_group_id = aws_security_group.rds.id
  ip_protocol       = "-1"
  cidr_ipv4         = "0.0.0.0/0"
}

# ---------- SG infra ----------

resource "aws_vpc_security_group_ingress_rule" "infra_ssh" {
  security_group_id = aws_security_group.infra.id
  description       = "SSH desde mi IP"
  ip_protocol       = "tcp"
  from_port         = 22
  to_port           = 22
  cidr_ipv4         = var.mi_ip
}

resource "aws_vpc_security_group_ingress_rule" "infra_kafka_ui" {
  security_group_id = aws_security_group.infra.id
  description       = "Kafka UI desde mi IP"
  ip_protocol       = "tcp"
  from_port         = 8080
  to_port           = 8080
  cidr_ipv4         = var.mi_ip
}

resource "aws_vpc_security_group_ingress_rule" "infra_rabbit_ui" {
  security_group_id = aws_security_group.infra.id
  description       = "Panel RabbitMQ desde mi IP"
  ip_protocol       = "tcp"
  from_port         = 15672
  to_port           = 15673
  cidr_ipv4         = var.mi_ip
}

resource "aws_vpc_security_group_ingress_rule" "infra_kafka" {
  security_group_id            = aws_security_group.infra.id
  description                  = "Brokers Kafka desde los microservicios"
  ip_protocol                  = "tcp"
  from_port                    = 9092
  to_port                      = 9094
  referenced_security_group_id = aws_security_group.apps.id
}

resource "aws_vpc_security_group_ingress_rule" "infra_rabbit" {
  security_group_id            = aws_security_group.infra.id
  description                  = "RabbitMQ desde los microservicios"
  ip_protocol                  = "tcp"
  from_port                    = 5672
  to_port                      = 5673
  referenced_security_group_id = aws_security_group.apps.id
}

# ---------- SG apps ----------

resource "aws_vpc_security_group_ingress_rule" "apps_ssh" {
  security_group_id = aws_security_group.apps.id
  description       = "SSH desde mi IP"
  ip_protocol       = "tcp"
  from_port         = 22
  to_port           = 22
  cidr_ipv4         = var.mi_ip
}

resource "aws_vpc_security_group_ingress_rule" "apps_servicios" {
  security_group_id = aws_security_group.apps.id
  description       = "APIs 8081-8087 (el API Gateway no tiene IP fija)"
  ip_protocol       = "tcp"
  from_port         = 8081
  to_port           = 8087
  cidr_ipv4         = "0.0.0.0/0"
}

# ---------- SG rds ----------

resource "aws_vpc_security_group_ingress_rule" "rds_mysql" {
  security_group_id            = aws_security_group.rds.id
  description                  = "MySQL solo desde los microservicios"
  ip_protocol                  = "tcp"
  from_port                    = 3306
  to_port                      = 3306
  referenced_security_group_id = aws_security_group.apps.id
}
