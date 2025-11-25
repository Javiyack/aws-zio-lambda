resource "aws_s3_bucket" "my_bucket" {
  bucket = "my-app-bucket"
}

resource "aws_dynamodb_table" "my_table" {
  name         = "my-app-table"
  billing_mode = "PAY_PER_REQUEST"
  hash_key     = "id"

  attribute {
    name = "id"
    type = "S"
  }
}

resource "aws_kinesis_stream" "input_stream" {
  name             = "input-stream"
  shard_count      = 1
  retention_period = 24
}

resource "aws_kinesis_stream" "output_stream" {
  name             = "output-stream"
  shard_count      = 1
  retention_period = 24
}

resource "aws_ssm_parameter" "my_param" {
  name  = "/my-app/config/some-param"
  type  = "String"
  value = "some-value"
}

resource "aws_secretsmanager_secret" "my_secret" {
  name = "my-app-secret"
}

resource "aws_secretsmanager_secret_version" "my_secret_val" {
  secret_id     = aws_secretsmanager_secret.my_secret.id
  secret_string = "{\"username\":\"dbuser\",\"password\":\"dbpassword\"}"
}

# IAM Role for Lambda
resource "aws_iam_role" "lambda_role" {
  name = "lambda_role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "lambda.amazonaws.com"
        }
      }
    ]
  })
}

# IAM Policy for Lambda
resource "aws_iam_role_policy" "lambda_policy" {
  name = "lambda_policy"
  role = aws_iam_role.lambda_role.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "kinesis:GetRecords",
          "kinesis:GetShardIterator",
          "kinesis:DescribeStream",
          "kinesis:ListShards",
          "kinesis:PutRecord",
          "kinesis:PutRecords"
        ]
        Resource = "*"
      },
      {
        Effect = "Allow"
        Action = [
          "dynamodb:PutItem",
          "dynamodb:GetItem",
          "dynamodb:UpdateItem",
          "dynamodb:DeleteItem"
        ]
        Resource = "*"
      },
      {
        Effect = "Allow"
        Action = [
          "s3:PutObject",
          "s3:GetObject"
        ]
        Resource = "*"
      },
      {
        Effect = "Allow"
        Action = [
          "ssm:GetParameter"
        ]
        Resource = "*"
      },
      {
        Effect = "Allow"
        Action = [
          "secretsmanager:GetSecretValue"
        ]
        Resource = "*"
      },
      {
        Effect = "Allow"
        Action = [
          "logs:CreateLogGroup",
          "logs:CreateLogStream",
          "logs:PutLogEvents"
        ]
        Resource = "*"
      }
    ]
  })
}

# Lambda Function
resource "aws_lambda_function" "my_lambda" {
  function_name = "my-scala-lambda"
  role          = aws_iam_role.lambda_role.arn
  handler       = "com.example.lambda.LambdaHandler::handleRequest"
  runtime       = "java11" # Or java17/21 depending on support
  timeout       = 30
  memory_size   = 512

  # Dummy filename for Terraform validation/apply if file doesn't exist yet.
  # In a real pipeline, this would be the assembly jar.
  # For LocalStack, we can point to a placeholder or the actual build artifact if mapped.
  filename         = "../target/scala-3.3.1/aws-lambda-zio-scala-assembly-0.1.0-SNAPSHOT.jar"
  source_code_hash = filebase64sha256("../target/scala-3.3.1/aws-lambda-zio-scala-assembly-0.1.0-SNAPSHOT.jar")

  environment {
    variables = {
      # Env vars if needed
    }
  }
}

# Event Source Mapping (Kinesis Trigger)
resource "aws_lambda_event_source_mapping" "kinesis_trigger" {
  event_source_arn  = aws_kinesis_stream.input_stream.arn
  function_name     = aws_lambda_function.my_lambda.arn
  starting_position = "TRIM_HORIZON"
  batch_size        = 10
}
