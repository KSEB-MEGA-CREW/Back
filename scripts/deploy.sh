#!/bin/bash

# ==========================================================
# Docker를 이용한 EC2 배포 자동화 스크립트 => EC2 환경에서 사용하기
#
# 이 스크립트는 다음 작업을 수행합니다.
# 1. AWS ECR에 로그인
# 2. ECR에서 최신 Docker 이미지를 풀(pull)
# 3. 현재 실행 중인 컨테이너 중지 및 삭제
# 4. 새로운 이미지로 컨테이너 실행
# ==========================================================

# ----------------------------------------------------------
# 1. 환경 변수 설정
# ----------------------------------------------------------

# AWS 계정 ID, ECR 리전, 리포지토리 이름 등을 환경 변수로 정의
# ECR 이미지 URI는 빌드/푸시 스크립트와 동일해야 합니다.
AWS_ACCOUNT_ID=${AWS_ACCOUNT_ID}
AWS_REGION=${AWS_REGION}
ECR_REPOSITORY_NAME=${ECR_REPOSITORY_NAME}
IMAGE_TAG=${IMAGE_TAG:-latest} # 기본값으로 latest 사용

# 환경 변수가 올바르게 설정되었는지 확인
if [[ -z "$AWS_ACCOUNT_ID" || -z "$AWS_REGION" || -z "$ECR_REPOSITORY_NAME" ]]; then
  echo "오류: 필수 환경 변수(AWS_ACCOUNT_ID, AWS_REGION, ECR_REPOSITORY_NAME)가 설정되지 않았습니다."
  exit 1
fi

# ECR 이미지 URI 생성
ECR_REPOSITORY_URI="${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/${ECR_REPOSITORY_NAME}:${IMAGE_TAG}"

# 컨테이너 이름 (이름이 고정되어야 중지/삭제가 용이)
CONTAINER_NAME="mega-crew-app"

# ----------------------------------------------------------
# 2. AWS ECR 로그인
# ----------------------------------------------------------

echo "AWS ECR에 로그인 중..."
# EC2 인스턴스의 IAM Role을 사용하여 ECR에 로그인
aws ecr get-login-password --region ${AWS_REGION} | docker login --username AWS --password-stdin ${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com
if [ $? -ne 0 ]; then
  echo "오류: AWS ECR 로그인에 실패했습니다. EC2 인스턴스의 IAM 역할 및 권한을 확인하세요."
  exit 1
fi
echo "AWS ECR 로그인 성공."

# ----------------------------------------------------------
# 3. ECR에서 최신 이미지 풀(pull)
# ----------------------------------------------------------

echo "ECR에서 최신 이미지(${ECR_REPOSITORY_URI})를 풀(pull)합니다."
docker pull ${ECR_REPOSITORY_URI}
if [ $? -ne 0 ]; then
  echo "오류: Docker 이미지 풀(pull)에 실패했습니다. 이미지 태그 또는 ECR 설정을 확인하세요."
  exit 1
fi
echo "Docker 이미지 풀(pull) 성공."


# ----------------------------------------------------------
# 4. 기존 컨테이너 중지 및 삭제
# ----------------------------------------------------------

echo "기존 컨테이너를 중지하고 삭제합니다."
# 현재 실행 중인 컨테이너가 있는지 확인
if [ "$(docker ps -q -f name=${CONTAINER_NAME})" ]; then
  echo "기존 컨테이너(${CONTAINER_NAME})를 중지합니다."
  docker stop ${CONTAINER_NAME}
  echo "기존 컨테이너를 삭제합니다."
  docker rm ${CONTAINER_NAME}
else
  echo "실행 중인 기존 컨테이너가 없습니다."
fi


# ----------------------------------------------------------
# 5. 새로운 컨테이너 실행
# ----------------------------------------------------------

echo "새로운 이미지로 컨테이너를 실행합니다."
# 새로운 Docker 컨테이너 실행
docker run -d \
  --name ${CONTAINER_NAME} \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e JDBC_URL="jdbc:mysql://rds-endpoint:3306/db_name" \
  -e JDBC_USERNAME="username" \
  -e JDBC_PASSWORD="password" \
  ${ECR_REPOSITORY_URI}
if [ $? -ne 0 ]; then
  echo "오류: 새로운 컨테이너 실행에 실패했습니다."
  exit 1
fi
echo "새로운 컨테이너(${CONTAINER_NAME})가 성공적으로 실행되었습니다."
