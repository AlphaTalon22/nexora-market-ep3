resource "aws_apigatewayv2_api" "nexora" {
  name          = "nexora-api"
  protocol_type = "HTTP"

  cors_configuration {
    allow_origins = [
      "https://${aws_cloudfront_distribution.frontend.domain_name}",
      "http://localhost:4200",
    ]
    allow_methods = ["GET", "POST", "PUT", "DELETE", "OPTIONS"]
    allow_headers = ["authorization", "content-type"]
    max_age       = 3600
  }
}

resource "aws_apigatewayv2_authorizer" "entra" {
  api_id           = aws_apigatewayv2_api.nexora.id
  name             = "entra"
  authorizer_type  = "JWT"
  identity_sources = ["$request.header.Authorization"]

  jwt_configuration {
    issuer   = "https://login.microsoftonline.com/${var.entra_tenant_id}/v2.0"
    audience = [var.entra_audience, "api://${var.entra_audience}"]
  }
}

locals {
  servicios = {
    "/api/productos"      = "8081"
    "/api/pedidos"        = "8082"
    "/api/pagos"          = "8083"
    "/api/notificaciones" = "8084"
    "/api/boletas"        = "8084"
    "/api/analitica"      = "8085"
    "/api/kafka-admin"    = "8086"
    "/api/rabbit-admin"   = "8087"
  }

  metodos = ["GET", "POST", "PUT", "DELETE"]
  puertos = toset(values(local.servicios))

  rutas = flatten([
    for prefijo, puerto in local.servicios : [
      for metodo in local.metodos : [
        {
          clave   = "${metodo} ${prefijo}"
          metodo  = metodo
          prefijo = prefijo
          puerto  = puerto
          publica = metodo == "GET" && prefijo == "/api/productos"
        },
        {
          clave   = "${metodo} ${prefijo}/{proxy+}"
          metodo  = metodo
          prefijo = prefijo
          puerto  = puerto
          publica = metodo == "GET" && prefijo == "/api/productos"
        },
      ]
    ]
  ])
}

resource "aws_apigatewayv2_integration" "servicios" {
  for_each = local.puertos

  api_id                 = aws_apigatewayv2_api.nexora.id
  integration_type       = "HTTP_PROXY"
  integration_method     = "ANY"
  integration_uri        = "http://${aws_eip.apps.public_ip}:${each.value}"
  payload_format_version = "1.0"

  request_parameters = {
    "overwrite:path" = "$request.path"
  }
}

resource "aws_apigatewayv2_route" "api" {
  for_each = { for ruta in local.rutas : ruta.clave => ruta }

  api_id    = aws_apigatewayv2_api.nexora.id
  route_key = each.value.clave
  target    = "integrations/${aws_apigatewayv2_integration.servicios[each.value.puerto].id}"

  authorization_type = each.value.publica ? "NONE" : "JWT"
  authorizer_id      = each.value.publica ? null : aws_apigatewayv2_authorizer.entra.id
}

resource "aws_apigatewayv2_stage" "default" {
  api_id      = aws_apigatewayv2_api.nexora.id
  name        = "$default"
  auto_deploy = true

  depends_on = [
    aws_apigatewayv2_integration.servicios,
    aws_apigatewayv2_route.api,
  ]
}
