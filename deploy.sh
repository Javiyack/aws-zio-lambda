#!/bin/bash
set -e
# ./deploy.sh
echo "Starting Local Deployment..."

# 1. Build (Simulating CodeBuild)
echo "Building Artifact..."
# We run the build command directly here. 
# In a real CI/CD, CodeBuild would pick up buildspec.yml.
# Locally, we just execute the equivalent.
export JAVA_HOME="C:\Users\javil\.jdks\corretto-1.8.0_472" 
sbt assembly

# 2. Deploy Infrastructure (Simulating CodePipeline/Deploy)
echo "Deploying Infrastructure..."
cd terraform
terraform init
terraform apply -auto-approve

echo "Deployment Complete!"
