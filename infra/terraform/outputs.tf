output "infra_ip_publica" {
  description = "IP elástica de la EC2 de infraestructura"
  value       = aws_eip.infra.public_ip
}

output "apps_ip_publica" {
  description = "IP elástica de la EC2 de microservicios"
  value       = aws_eip.apps.public_ip
}

output "infra_ip_privada" {
  description = "IP privada que las apps usan para Kafka y RabbitMQ"
  value       = aws_instance.infra.private_ip
}

output "rds_endpoint" {
  description = "Host:puerto de la RDS MySQL"
  value       = aws_db_instance.mysql.endpoint
}

output "rds_address" {
  description = "Host de la RDS MySQL"
  value       = aws_db_instance.mysql.address
}

output "kafka_bootstrap" {
  description = "Bootstrap de Kafka para los microservicios"
  value       = "${aws_instance.infra.private_ip}:9092,${aws_instance.infra.private_ip}:9093,${aws_instance.infra.private_ip}:9094"
}

output "ssh_infra" {
  description = "Comando para entrar por SSH a la EC2 de infraestructura"
  value       = "ssh -i labsuser.pem ec2-user@${aws_eip.infra.public_ip}"
}

output "ssh_apps" {
  description = "Comando para entrar por SSH a la EC2 de microservicios"
  value       = "ssh -i labsuser.pem ec2-user@${aws_eip.apps.public_ip}"
}
