# AWS Lambda Scala ZIO Project

Este proyecto implementa una función AWS Lambda utilizando Scala 3 y ZIO 2, diseñada para interactuar con múltiples servicios de AWS y APIs externas. La infraestructura está definida como código (IaC) utilizando Terraform y se puede desplegar localmente usando LocalStack.

## Características

*   **Lenguaje y Frameworks:** Scala 3, ZIO 2.
*   **AWS SDK:** Última versión.
*   **Persistencia y Mensajería:**
    *   PostgreSQL (Slick)
    *   DynamoDB (Scanamo)
    *   Kinesis Streams (Productor y Consumidor)
    *   S3
*   **Configuración y Secretos:**
    *   AWS Parameter Store
    *   AWS Secrets Manager
*   **Conectividad:** Llamadas a APIs externas (sttp3).
*   **Serialización:** Circe.
*   **Calidad de Código:** Pruebas unitarias, Patrones de diseño, Programación Funcional, Lógica de Retry, Logging.
*   **Infraestructura:** Terraform.
*   **Entorno Local:** LocalStack y Docker.

## Requisitos Previos

*   Java Development Kit (JDK) 8 (Requerido para compatibilidad con el entorno de build actual).
*   sbt (Scala Build Tool).
*   Docker y Docker Compose.
*   Terraform.
*   AWS CLI (Opcional, útil para interactuar con LocalStack).

## Configuración y Despliegue Local

### 1. Iniciar Servicios Locales
Levanta los servicios de AWS simulados (LocalStack) y la base de datos PostgreSQL usando Docker Compose:

```bash
docker-compose up -d
```

### 2. Construir y Desplegar
El script `deploy.sh` se encarga de compilar el proyecto (generando un fat jar con `sbt assembly`) y aplicar la configuración de Terraform.

```bash
./deploy.sh
```

Este script realiza los siguientes pasos:
1.  Establece `JAVA_HOME` (Asegúrate de que la ruta en `deploy.sh` coincida con tu instalación de JDK 8 o ajústala según sea necesario).
2.  Ejecuta `sbt assembly` para empaquetar la Lambda.
3.  Inicializa y aplica los cambios de Terraform en el directorio `terraform/`.

## Estructura del Proyecto

*   `src/`: Código fuente Scala.
*   `terraform/`: Archivos de configuración de Terraform.
*   `docker-compose.yml`: Definición de servicios locales (LocalStack, Postgres).
*   `build.sbt`: Configuración de construcción del proyecto Scala.
*   `deploy.sh`: Script de automatización para build y deploy local.
