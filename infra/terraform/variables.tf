variable "region" {
  description = "Región de AWS del Learner Lab"
  type        = string
  default     = "us-east-1"
}

variable "mi_ip" {
  description = "IP pública del equipo, en formato CIDR (ej. 200.1.2.3/32)"
  type        = string
}

variable "repo_url" {
  description = "Repo público que clonan las EC2"
  type        = string
  default     = "https://github.com/AlphaTalon22/nexora-market-ep3.git"
}

variable "repo_branch" {
  description = "Rama que clonan las EC2"
  type        = string
  default     = "main"
}

variable "key_name" {
  description = "Key pair que ya existe en el lab"
  type        = string
  default     = "vockey"
}

variable "instance_type_infra" {
  description = "Tipo de la EC2 de infraestructura (Kafka, Kafka UI, RabbitMQ)"
  type        = string
  default     = "t3.large"
}

variable "instance_type_apps" {
  description = "Tipo de la EC2 de microservicios"
  type        = string
  default     = "t3.large"
}

variable "db_instance_class" {
  description = "Clase de la RDS del lab"
  type        = string
  default     = "db.t3.micro"
}

variable "db_username" {
  description = "Usuario administrador de la RDS"
  type        = string
  default     = "nexora"
}

variable "db_password" {
  description = "Clave administradora de la RDS"
  type        = string
  sensitive   = true
}

variable "rabbit_user" {
  description = "Usuario del clúster RabbitMQ"
  type        = string
  default     = "nexora"
}

variable "rabbit_pass" {
  description = "Clave del clúster RabbitMQ"
  type        = string
  sensitive   = true
}

variable "rabbit_cookie" {
  description = "Erlang cookie del clúster RabbitMQ"
  type        = string
  sensitive   = true
}

variable "kafka_ui_pass" {
  description = "Clave del panel web de Kafka UI"
  type        = string
  sensitive   = true
}

variable "entra_tenant_id" {
  description = "Tenant de Entra ID que valida los microservicios"
  type        = string
  default     = "5f26903b-8f3c-4181-afbb-a40c0b6e034d"
}

variable "entra_audience" {
  description = "Audiencia (Application ID) de la API en Entra ID"
  type        = string
  default     = "967df881-c709-4c72-9c7c-787c1aa5557d"
}
