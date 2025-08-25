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

set -e  # 에러 발생시 스크립트 중단

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# 로그 함수
log_info() { echo -e "${BLUE}[INFO]${NC} $1"; }
log_success() { echo -e "${GREEN}[SUCCESS]${NC} $1"; }
log_warning() { echo -e "${YELLOW}[WARNING]${NC} $1"; }
log_error() { echo -e "${RED}[ERROR]${NC} $1"; }

# ----------------------------------------------------------
# 1. 환경 변수 확인 및 로드
# ----------------------------------------------------------

log_info "환경 설정 확인 중..."

if [ ! -f ".env" ]; then
    log_error ".env 파일이 없습니다. .env.example을 참고하여 생성하세요."
    exit 1
fi

# .env 파일 로드
source .env

# 필수 환경 변수 확인
REQUIRED_VARS=(
    "AWS_ACCOUNT_ID"
    "AWS_REGION"
    "ECR_REPOSITORY"
    "DB_PASSWORD"
    "JWT_SECRET"
)

for var in "${REQUIRED_VARS[@]}"; do
    if [ -z "${!var}" ]; then
        log_error "필수 환경 변수 $var가 설정되지 않았습니다."
        exit 1
    fi
done

# 변수 설정
IMAGE_TAG=${IMAGE_TAG:-latest}
ECR_URI="${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/${ECR_REPOSITORY}:${IMAGE_TAG}"

log_success "환경 설정 확인 완료"
log_info "배포할 이미지: $ECR_URI"

# ----------------------------------------------------------
# 2. 필수 도구 확인
# ----------------------------------------------------------

log_info "필수 도구 확인 중..."

if ! command -v docker &> /dev/null; then
    log_error "Docker가 설치되지 않았습니다."
    exit 1
fi

if ! command -v docker-compose &> /dev/null; then
    log_error "Docker Compose가 설치되지 않았습니다."
    exit 1
fi

if ! command -v aws &> /dev/null; then
    log_error "AWS CLI가 설치되지 않았습니다."
    exit 1
fi

log_success "필수 도구 확인 완료"

# ----------------------------------------------------------
# 3. 디렉토리 생성
# ----------------------------------------------------------

log_info "필요한 디렉토리 생성 중..."

DIRECTORIES=(
    "/opt/mega-crew/logs"
    "/opt/mega-crew/mysql-data"
    "/opt/mega-crew/redis-data"
)

for dir in "${DIRECTORIES[@]}"; do
    if [ ! -d "$dir" ]; then
        sudo mkdir -p "$dir"
        sudo chown 1000:1000 "$dir" 2>/dev/null || true
        log_info "디렉토리 생성: $dir"
    fi
done

# ----------------------------------------------------------
# 4. AWS ECR 로그인
# ----------------------------------------------------------

log_info "AWS ECR 로그인 중..."

aws ecr get-login-password --region ${AWS_REGION} | \
    docker login --username AWS --password-stdin \
    ${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com

if [ $? -eq 0 ]; then
    log_success "ECR 로그인 성공"
else
    log_error "ECR 로그인 실패. IAM 권한을 확인하세요."
    exit 1
fi

# ----------------------------------------------------------
# 5. 최신 이미지 풀
# ----------------------------------------------------------

log_info "최신 이미지 다운로드 중..."

docker pull $ECR_URI

if [ $? -eq 0 ]; then
    log_success "이미지 다운로드 성공: $ECR_URI"
else
    log_error "이미지 다운로드 실패: $ECR_URI"
    exit 1
fi

# ----------------------------------------------------------
# 6. 기존 서비스 중지
# ----------------------------------------------------------

log_info "기존 서비스 중지 중..."

if [ -f "docker-compose.prod.yml" ]; then
    if docker-compose -f docker-compose.prod.yml ps -q 2>/dev/null | grep -q .; then
        docker-compose -f docker-compose.prod.yml down --remove-orphans
        log_success "기존 서비스 중지 완료"
    else
        log_info "실행 중인 서비스가 없습니다"
    fi
else
    log_error "docker-compose.prod.yml 파일이 없습니다."
    exit 1
fi

# ----------------------------------------------------------
# 7. 새 서비스 시작
# ----------------------------------------------------------

log_info "새 서비스 시작 중..."

docker-compose -f docker-compose.prod.yml up -d

if [ $? -eq 0 ]; then
    log_success "서비스 시작 성공"
else
    log_error "서비스 시작 실패"
    # 로그 출력
    docker-compose -f docker-compose.prod.yml logs
    exit 1
fi

# ----------------------------------------------------------
# 8. 헬스체크
# ----------------------------------------------------------

log_info "애플리케이션 헬스체크 대기 중..."

MAX_ATTEMPTS=30
ATTEMPT=0

while [ $ATTEMPT -lt $MAX_ATTEMPTS ]; do
    if curl -f http://localhost:8080/actuator/health 2>/dev/null; then
        log_success "애플리케이션이 정상적으로 시작되었습니다!"
        break
    fi

    ATTEMPT=$((ATTEMPT + 1))
    log_info "헬스체크 시도 $ATTEMPT/$MAX_ATTEMPTS..."
    sleep 10
done

if [ $ATTEMPT -eq $MAX_ATTEMPTS ]; then
    log_error "애플리케이션이 정상적으로 시작되지 않았습니다."
    log_info "로그 확인: docker-compose -f docker-compose.prod.yml logs app"
    exit 1
fi

# ----------------------------------------------------------
# 9. 정리 작업
# ----------------------------------------------------------

log_info "사용하지 않는 Docker 이미지 정리 중..."
docker image prune -f

# ----------------------------------------------------------
# 10. 배포 완료
# ----------------------------------------------------------

log_success "========================================="
log_success "MEGA CREW 배포 완료!"
log_success "========================================="
log_info "애플리케이션 URL: http://$(curl -s ifconfig.me):8080"
log_info "헬스체크 URL: http://localhost:8080/actuator/health"
log_info ""
log_info "유용한 명령어:"
log_info "  로그 확인: docker-compose -f docker-compose.prod.yml logs -f app"
log_info "  상태 확인: docker-compose -f docker-compose.prod.yml ps"
log_info "  중지: docker-compose -f docker-compose.prod.yml down"
log_info ""
log_warning "배포 완료 후 모니터링을 확인하세요."