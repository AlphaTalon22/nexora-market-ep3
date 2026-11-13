resource "aws_instance" "infra" {
  ami                    = data.aws_ami.amazon_linux.id
  instance_type          = var.instance_type_infra
  key_name               = var.key_name
  subnet_id              = data.aws_subnets.default.ids[0]
  vpc_security_group_ids = [aws_security_group.infra.id]
  iam_instance_profile   = "LabInstanceProfile"

  metadata_options {
    http_tokens = "required"
  }

  root_block_device {
    volume_size = 30
    volume_type = "gp3"
  }

  user_data = templatefile("${path.module}/scripts/infra.sh.tpl", {
    repo_url      = var.repo_url
    repo_branch   = var.repo_branch
    rabbit_user   = var.rabbit_user
    rabbit_pass   = var.rabbit_pass
    rabbit_cookie = var.rabbit_cookie
    kafka_ui_pass = var.kafka_ui_pass
  })

  tags = {
    Name = "nexora-infra"
  }
}

resource "aws_instance" "apps" {
  ami                    = data.aws_ami.amazon_linux.id
  instance_type          = var.instance_type_apps
  key_name               = var.key_name
  subnet_id              = data.aws_subnets.default.ids[0]
  vpc_security_group_ids = [aws_security_group.apps.id]
  iam_instance_profile   = "LabInstanceProfile"

  metadata_options {
    http_tokens = "required"
  }

  root_block_device {
    volume_size = 20
    volume_type = "gp3"
  }

  user_data = templatefile("${path.module}/scripts/apps.sh.tpl", {
    repo_url        = var.repo_url
    repo_branch     = var.repo_branch
    infra_ip        = aws_instance.infra.private_ip
    db_address      = aws_db_instance.mysql.address
    db_username     = var.db_username
    db_password     = var.db_password
    rabbit_user     = var.rabbit_user
    rabbit_pass     = var.rabbit_pass
    entra_tenant_id = var.entra_tenant_id
    entra_audience  = var.entra_audience
  })

  depends_on = [aws_db_instance.mysql]

  tags = {
    Name = "nexora-apps"
  }
}

resource "aws_eip" "infra" {
  domain   = "vpc"
  instance = aws_instance.infra.id

  tags = {
    Name = "nexora-infra-eip"
  }
}

resource "aws_eip" "apps" {
  domain   = "vpc"
  instance = aws_instance.apps.id

  tags = {
    Name = "nexora-apps-eip"
  }
}
