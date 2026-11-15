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

output "api_url" {
  description = "URL base de la HTTP API en el stage $default"
  value       = aws_apigatewayv2_api.nexora.api_endpoint
}

output "cloudfront_url" {
  description = "URL publica del frontend en CloudFront"
  value       = "https://${aws_cloudfront_distribution.frontend.domain_name}"
}

output "frontend_bucket" {
  description = "Bucket S3 privado del frontend"
  value       = aws_s3_bucket.frontend.bucket
}

output "cloudfront_distribution_id" {
  description = "ID de la distribucion CloudFront para invalidaciones"
  value       = aws_cloudfront_distribution.frontend.id
}

output "comando_subir_frontend" {
  description = "Comando para subir el build y invalidar CloudFront"
  value       = "aws s3 sync frontend/dist/frontend/browser s3://${aws_s3_bucket.frontend.bucket} --delete && aws cloudfront create-invalidation --distribution-id ${aws_cloudfront_distribution.frontend.id} --paths \"/*\""
}
