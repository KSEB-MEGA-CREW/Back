#!/bin/bash

# ==========================================================
# ECR 이미지 빌드 및 푸시 자동화 스크립트
#
# AWS ECR에 DOCKER 이미지를 빌드하고 푸시
# ==========================================================

set -e  # 에러 발생 시 즉시 종료

# ----------------------------------------------------------
# 1. 환경 변수 설정
# ----------------------------------------------------------

# 보안을 위해 CI/CD 파이프라인에서 환경 변수로 주입할 것

# AWS 계정 ID
AWS_ACCOUNT_ID=${AWS_ACCOUNT_ID}
# ECR 리전 (예: ap-northeast-2)
AWS_REGION=${AWS_REGION:-ap-northeast-2}
# ECR 리포지토리 이름 (환경변수 템플릿과 일치)
ECR_REPOSITORY=${ECR_REPOSITORY:-mega-crew-backend}
# 이미지에 적용할 태그 (예: latest, 또는 git commit hash)
IMAGE_TAG=${IMAGE_TAG:-latest}

# 환경 변수가 올바르게 설정되었는지 확인
if [[ -z "$AWS_ACCOUNT_ID" ]]; then
  echo "❌ 오류: AWS_ACCOUNT_ID 환경 변수가 설정되지 않았습니다."
  echo "💡 해결방법: export AWS_ACCOUNT_ID=your-account-id"
  exit 1
fi

if [[ -z "$AWS_REGION" ]]; then
  echo "❌ 오류: AWS_REGION 환경 변수가 설정되지 않았습니다."
  echo "💡 해결방법: export AWS_REGION=ap-northeast-2"
  exit 1
fi

if [[ -z "$ECR_REPOSITORY" ]]; then
  echo "❌ 오류: ECR_REPOSITORY 환경 변수가 설정되지 않았습니다."
  echo "💡 해결방법: export ECR_REPOSITORY=mega-crew-backend"
  exit 1
fi

# ECR 리포지토리 URI 생성
ECR_REPOSITORY_URI="${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/${ECR_REPOSITORY}"

echo "🚀 ===== ECR 빌드 및 푸시 시작 ====="
echo "📍 AWS 계정 ID: ${AWS_ACCOUNT_ID}"
echo "🌏 AWS 리전: ${AWS_REGION}"
echo "📦 ECR 리포지토리: ${ECR_REPOSITORY}"
echo "🏷️  이미지 태그: ${IMAGE_TAG}"
echo "🔗 ECR URI: ${ECR_REPOSITORY_URI}"
echo "==============================================="

# ----------------------------------------------------------
# 2. AWS CLI 설치 확인
# ----------------------------------------------------------

echo "🔍 AWS CLI 설치 상태 확인 중..."
if ! command -v aws &> /dev/null; then
    echo "❌ AWS CLI가 설치되지 않았습니다."
    echo "💡 설치 방법: https://docs.aws.amazon.com/cli/latest/userguide/getting-started-install.html"
    exit 1
fi

echo "✅ AWS CLI 버전: $(aws --version)"

# ----------------------------------------------------------
# 3. Docker 설치 확인
# ----------------------------------------------------------

echo "🔍 Docker 설치 상태 확인 중..."
if ! command -v docker &> /dev/null; then
    echo "❌ Docker가 설치되지 않았습니다."
    exit 1
fi

echo "✅ Docker 버전: $(docker --version)"

# ----------------------------------------------------------
# 4. AWS 자격 증명 확인
# ----------------------------------------------------------

echo "🔍 AWS 자격 증명 확인 중..."
if ! aws sts get-caller-identity &> /dev/null; then
    echo "❌ AWS 자격 증명이 설정되지 않았습니다."
    echo "💡 해결방법: aws configure 또는 IAM Role 설정"
    exit 1
fi

echo "✅ AWS 자격 증명 확인 완료"

# ----------------------------------------------------------
# 5. ECR 리포지토리 존재 확인 및 생성
# ----------------------------------------------------------

echo "🔍 ECR 리포지토리 존재 확인 중..."
if ! aws ecr describe-repositories --repository-names ${ECR_REPOSITORY} --region ${AWS_REGION} &> /dev/null; then
    echo "⚠️  ECR 리포지토리가 존재하지 않습니다. 생성 중..."
    aws ecr create-repository \
        --repository-name ${ECR_REPOSITORY} \
        --region ${AWS_REGION} \
        --image-scanning-configuration scanOnPush=true
    echo "✅ ECR 리포지토리 생성 완료"
else
    echo "✅ ECR 리포지토리 존재 확인"
fi

# ----------------------------------------------------------
# 6. AWS ECR 로그인
# ----------------------------------------------------------

echo "🔐 AWS ECR에 로그인 중..."
aws ecr get-login-password --region ${AWS_REGION} | \
    docker login --username AWS --password-stdin ${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com

if [ $? -ne 0 ]; then
  echo "❌ 오류: AWS ECR 로그인에 실패했습니다."
  echo "💡 IAM 권한 확인: ecr:GetAuthorizationToken, ecr:BatchCheckLayerAvailability, ecr:GetDownloadUrlForLayer, ecr:BatchGetImage"
  exit 1
fi
echo "✅ AWS ECR 로그인 성공"

# ----------------------------------------------------------
# 7. Docker 이미지 빌드
# ----------------------------------------------------------

echo "🏗️  Docker 이미지 빌드 시작 (태그: ${IMAGE_TAG})"

# Dockerfile 존재 확인
if [ ! -f "Dockerfile" ]; then
    echo "❌ 오류: Dockerfile이 현재 디렉터리에 존재하지 않습니다."
    exit 1
fi

# 현재 디렉터리의 Dockerfile을 사용해 이미지 빌드
# --platform linux/amd64 옵션을 사용해 EC2 환경에 맞게 빌드
docker build \
    --platform linux/amd64 \
    --tag ${ECR_REPOSITORY}:${IMAGE_TAG} \
    --tag ${ECR_REPOSITORY_URI}:${IMAGE_TAG} \
    .

if [ $? -ne 0 ]; then
  echo "❌ 오류: Docker 이미지 빌드에 실패했습니다."
  exit 1
fi
echo "✅ Docker 이미지 빌드 성공"

# ----------------------------------------------------------
# 8. ECR에 이미지 업로드
# ----------------------------------------------------------

echo "📤 Docker 이미지를 ECR에 업로드 중..."
docker push ${ECR_REPOSITORY_URI}:${IMAGE_TAG}

if [ $? -ne 0 ]; then
  echo "❌ 오류: Docker 이미지 업로드에 실패했습니다."
  exit 1
fi
echo "✅ Docker 이미지 업로드 성공"

# ----------------------------------------------------------
# 9. 이미지 정보 출력
# ----------------------------------------------------------

echo ""
echo "🎉 ===== 빌드 및 업로드 완료 ====="
echo "📦 이미지 URI: ${ECR_REPOSITORY_URI}:${IMAGE_TAG}"
echo "🔗 ECR 콘솔: https://console.aws.amazon.com/ecr/repositories/${ECR_REPOSITORY}/?region=${AWS_REGION}"

# 이미지 크기 정보
IMAGE_SIZE=$(docker images ${ECR_REPOSITORY_URI}:${IMAGE_TAG} --format "table {{.Size}}" | tail -n +2)
echo "📏 이미지 크기: ${IMAGE_SIZE}"

# 로컬 이미지 정리 (선택사항)
#read -p "🗑️  로컬 이미지를 삭제하시겠습니까? (y/N): " -n 1 -r
#echo
#if [[ $REPLY =~ ^[Yy]$ ]]; then
#    docker rmi ${ECR_REPOSITORY}:${IMAGE_TAG}
#    docker rmi ${ECR_REPOSITORY_URI}:${IMAGE_TAG}
#    echo "✅ 로컬 이미지 삭제 완료"
#fi

echo "🚀 모든 작업이 완료되었습니다!"