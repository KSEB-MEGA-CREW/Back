-- Users 테스트 데이터
INSERT INTO users (email, password, username, hearing_status, role, auth_provider, provider_id, created_date, modified_date) VALUES
('admin@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', '최고관리자', 'NORMAL', 'ADMIN', 'LOCAL', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', '홍길동', 'NORMAL', 'USER', 'LOCAL', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('kim@test.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', '김철수', 'DEAF', 'USER', 'LOCAL', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('park@test.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', '박지성', 'NORMAL', 'USER', 'LOCAL', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('lee@test.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', '이영희', 'DEAF', 'USER', 'LOCAL', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Translation Histories 테스트 데이터
INSERT INTO translation_histories (user_id, work_type, input_content, output_content, processing_status, processing_time, error_message, user_agent, input_length, expires_at, is_expired, created_date, modified_date) VALUES
(2, 'TEXTTO3D', '안녕하세요', '수어 애니메이션 데이터', 'COMPLETED', 1200, NULL, 'Mozilla/5.0', 5, DATE_ADD(NOW(), INTERVAL 30 DAY), 0, NOW(), NOW()),
(2, 'IMAGETOTEXT', NULL, '감사합니다', 'COMPLETED', 2500, NULL, 'Mozilla/5.0', 0, DATE_ADD(NOW(), INTERVAL 30 DAY), 0, NOW(), NOW()),
(3, 'TEXTTO3D', '만나서 반갑습니다', '수어 애니메이션 데이터', 'COMPLETED', 1800, NULL, 'Mozilla/5.0', 8, DATE_ADD(NOW(), INTERVAL 30 DAY), 0, NOW(), NOW()),
(4, 'IMAGETOTEXT', NULL, '안녕히 가세요', 'FAILED', 0, '분석 실패: 이미지 품질 불량', 'Mozilla/5.0', 0, DATE_ADD(NOW(), INTERVAL 30 DAY), 0, NOW(), NOW()),
(5, 'TEXTTO3D', '좋은 하루 되세요', '수어 애니메이션 데이터', 'PROCESSING', NULL, NULL, 'Mozilla/5.0', 8, DATE_ADD(NOW(), INTERVAL 30 DAY), 0, NOW(), NOW());

-- Support Tickets 테스트 데이터
INSERT INTO support_ticket (user_id, user_name, category, subject, content, is_public, status, admin_response, last_edited_date, admin_response_date, responded_by, created_date, modified_date) VALUES
(2, '홍길동', 'LEARNING', '학습 진행률이 업데이트되지 않습니다', '어제 React 기초 강의를 100% 수강 완료했는데 진행률이 반영되지 않습니다.', 0, 'PENDING', NULL, NOW(), NULL, NULL, NOW(), NOW()),
(3, '김철수', 'FEATURE', '새로운 기능 제안: 코드 에디터 테마 변경', '실습 환경의 코드 에디터가 너무 밝아서 눈이 아픕니다. 다크 모드를 추가해주세요.', 1, 'PENDING', NULL, NOW(), NULL, NULL, NOW(), NOW()),
(4, '박지성', 'TECHNICAL', '모바일 화면에서 UI가 깨지는 현상', '갤럭시 폴드 기종에서 게시판을 보면 일부 버튼이 겹쳐 보입니다.', 0, 'ANSWERED', '해당 문제를 확인하였으며, 다음 업데이트에서 개선될 예정입니다.', NOW(), NOW(), 1, NOW(), NOW()),
(5, '이영희', 'ACCOUNT', '비밀번호를 변경하고 싶은데 메뉴를 못 찾겠어요', '안녕하세요. 보안을 위해 주기적으로 비밀번호를 변경하고 싶은데, 내 정보 페이지 어디에도 비밀번호 변경 메뉴가 보이지 않습니다. 확인 부탁드립니다.', 1, 'ANSWERED', '설정 메뉴에서 비밀번호 변경이 가능합니다. 우측 상단 프로필 아이콘을 클릭해보세요.', NOW(), NOW(), 1, NOW(), NOW()),
(3, '김철수', 'OTHER', '결제 영수증은 어디서 받을 수 있나요?', '안녕하세요. 이번 달 구독료 결제에 대한 영수증을 받고 싶은데 어디서 다운로드할 수 있나요?', 0, 'ANSWERED', '마이페이지 > 결제 내역에서 영수증 다운로드가 가능합니다.', NOW(), NOW(), 1, NOW(), NOW());

-- Quiz Records 테스트 데이터 (월별 통계 반영)
INSERT INTO quiz_records (user_id, correct_count, created_date, modified_date) VALUES
(2, 0, '2025-08-01', '2025-08-01'),
(2, 10, '2025-08-02', '2025-08-02'),
(2, 25, '2025-08-03', '2025-08-03'),
(2, 90, '2025-08-04', '2025-08-04'),
(2, 20, '2025-08-05', '2025-08-05'),
(2, 85, '2025-08-06', '2025-08-06'),
(2, 35, '2025-08-07', '2025-08-07'),
(2, 30, '2025-08-08', '2025-08-08'),
(2, 56, '2025-08-09', '2025-08-09'),
(2, 90, '2025-08-10', '2025-08-10'),
(2, 60, '2025-08-14', '2025-08-14'),
(2, 64, '2025-08-31', '2025-08-31'),
(3, 15, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 22, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 18, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 30, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 12, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Quiz Category Records 테스트 데이터
INSERT INTO quiz_category_records (user_id, category, correct_count, created_date, modified_date) VALUES
(2, '사회생활', 15, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, '기타', 12, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, '식생활', 18, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, '삶', 20, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, '사회생활', 8, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, '기타', 10, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, '식생활', 6, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, '사회생활', 22, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, '기타', 18, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, '식생활', 25, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, '삶', 12, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, '사회생활', 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, '기타', 8, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Incorrect Quiz Records 테스트 데이터
INSERT INTO incorrect_quiz_records (user_id, word, category, sign_description, sub_description, created_date, modified_date) VALUES
(2, '사과', '식생활', '오른손을 동그랗게 말아 입에 가져다 댄다.', 'https://sldict.korean.go.kr/multimedia/multimedia_files/convert/20200825/735416/MOV000246252_700X466.mp4', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, '은혜,은이', '사회생활', '왼 주먹 등에 오른 손바닥을 대고 어루만지듯이 왼쪽으로 두 바퀴 돌린다.', 'https://sldict.korean.go.kr/multimedia/multimedia_files/convert/20200825/735416/MOV000246252_700X466.mp4', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, '여관', '기타', '손바닥이 뒤로 향하여 세운 오른손의 4지 옆면을 머리 오른쪽에 댔다가 앞으로 내리며 손가락 끝을 모으는 동작을 두 번 한다.', 'https://sldict.korean.go.kr/multimedia/multimedia_files/convert/20200825/735416/MOV000246252_700X466.mp4', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, '거처', '기타', '1·5지를 펴서 바닥이 밖으로 향하게 쥔 두 주먹을 동시에 오른쪽으로 한 바퀴 돌린 다음, 손바닥이 아래로 향하게 반쯤 구부린 오른손을 가슴 앞에서 약간 내리다가 멈춘다.', 'https://sldict.korean.go.kr/multimedia/multimedia_files/convert/20200825/735416/MOV000246252_700X466.mp4', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, '주전자', '식생활', '오른 주먹의 4·5지를 펴서 끝이 아래로 향하게 하였다가 5지 끝을 아래로 향하게 기울인다.', 'https://sldict.korean.go.kr/multimedia/multimedia_files/convert/20200825/735416/MOV000246252_700X466.mp4', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, '큰언니', '삶', '오른 주먹의 1·5지를 펴서 등이 밖으로 향하게 하여 왼쪽에서 오른쪽으로 이동한 다음, 두 주먹의 4지를 펴서 등이 밖으로 향하게 세워 맞댔다가 오른손만 위로 올린다.', 'https://sldict.korean.go.kr/multimedia/multimedia_files/convert/20200825/735416/MOV000246252_700X466.mp4', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, '감사합니다', '사회생활', '양손을 합장하고 고개를 숙인다.', 'https://sldict.korean.go.kr/multimedia/multimedia_files/convert/20200825/735416/MOV000246252_700X466.mp4', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, '학교', '기타', '지붕 모양을 만든 두 손을 위아래로 움직인다.', 'https://sldict.korean.go.kr/multimedia/multimedia_files/convert/20200825/735416/MOV000246252_700X466.mp4', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, '안녕하세요', '사회생활', '오른손을 가슴에 대고 앞으로 내밀면서 고개를 살짝 숙인다.', 'https://sldict.korean.go.kr/multimedia/multimedia_files/convert/20200825/735416/MOV000246252_700X466.mp4', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);