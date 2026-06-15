-- ============================================================
-- PostgreSQL-compatible seed data (production profile)
-- Uses string_agg() instead of H2's LISTAGG()
-- ============================================================

-- LESSONS
INSERT INTO lessons (title, difficulty_level, content_text, display_order, min_wpm, min_accuracy)
SELECT * FROM (VALUES
  ('Home Row Keys',       'BASIC', 'asdfjkl; asdfjkl; fdsa ;lkj asdf jkl; fjdk slaf fall lake mask desk glad half jack sale talk walk hall ball tall wall', 1, 20, 85.0),
  ('Left Hand Practice',  'BASIC', 'asdf asdf aaaa ssss dddd ffff asdf fdsa asd fds cafe fade safe face base calf lad dad sad bag tab asdf fade cafe base', 2, 20, 85.0),
  ('Right Hand Practice', 'BASIC', 'jkl; jkl; jjjj kkkk llll ;;;; jkl; ;lkj look pool hook moon noon loom mojo poll hook loll kook jkl; look pool moon noon', 3, 20, 85.0),
  ('Both Hands Together', 'BASIC', 'flag lake disk fall glad half jack mask sale talk desk dash flask black slack clack flack glass flask glad flag lake disk', 4, 20, 85.0),
  ('Adding E and I',      'BASIC', 'slide filed ideal aisle faked eased fails ledge like file side hide seek life tie aide isle elite file slide filed ideal', 5, 20, 85.0),
  ('Adding T and Y',      'BASIC', 'style theft still tasks tilts fifty dusty eighty deft tasty testy sixty nasty hasty pasty style dusty sixty tasty theft', 6, 20, 85.0),
  ('Adding R and U',      'BASIC', 'rules ruler rusty tired flair rural reads fruit suits runs turns hurls blurs flurry rural fruit ruler rules rusty tired', 7, 20, 85.0),
  ('Top Row Practice',    'BASIC', 'quit your west tier rope pure query wrote outer tower power quite wrote rope tower power outer quest your quite west tier', 8, 20, 85.0)
) AS v(title, difficulty_level, content_text, display_order, min_wpm, min_accuracy)
WHERE NOT EXISTS (SELECT 1 FROM lessons WHERE difficulty_level = 'BASIC');

INSERT INTO lessons (title, difficulty_level, content_text, display_order, min_wpm, min_accuracy)
SELECT * FROM (VALUES
  ('Common Words',       'INTERMEDIATE', 'the quick brown fox jumps over the lazy dog. the big black cat sat on the mat. she sells sea shells by the seashore. how much wood would a woodchuck chuck if a woodchuck could chuck wood?',                                                                    9,  35, 85.0),
  ('Short Sentences',    'INTERMEDIATE', 'She sells sea shells by the seashore. Peter Piper picked a peck of pickled peppers. How much wood would a woodchuck chuck? The rain in Spain stays mainly in the plain. Betty Botter bought some butter.',                                                     10, 35, 85.0),
  ('Capitalization',     'INTERMEDIATE', 'John and Mary traveled to New York on Monday morning. The Eiffel Tower stands in Paris, France. Alice met Bob at London Bridge last summer. The Amazon River flows through South America into the Atlantic Ocean.',                                              11, 35, 85.0),
  ('Punctuation Basics', 'INTERMEDIATE', 'Hello, world! How are you today? I am doing very well, thank you! Please come to the meeting at 3:00 PM in room 204. Do not forget to bring your laptop, charger, and notebook with you.',                                                                     12, 35, 85.0),
  ('Common Phrases',     'INTERMEDIATE', 'To be or not to be, that is the question. All that glitters is not gold. Actions speak louder than words. Time flies when you are having fun. Every cloud has a silver lining. Look before you leap.',                                                          13, 35, 85.0),
  ('Paragraph Typing',   'INTERMEDIATE', 'Practice typing every single day to improve your speed and accuracy steadily. Focus on accuracy before chasing speed, because speed will naturally follow good habits. Always keep your fingers on the home row keys and use the correct finger for each key.', 14, 35, 85.0),
  ('Mixed Case',         'INTERMEDIATE', 'The Sun rises in the East and sets in the West every single day. Stars shine brightly in the Night sky. Jupiter is the largest Planet in our Solar System. Saturn has beautiful rings made of ice and rock orbiting around it.',                                15, 35, 85.0),
  ('Business Writing',   'INTERMEDIATE', 'Dear Mr. Johnson, I am writing to inquire about the Software Engineer position advertised on your website. I have five years of experience working with Java, Spring Boot, and AWS cloud services. Please find my attached resume for your consideration. Best regards, Sarah Williams.', 16, 35, 85.0)
) AS v(title, difficulty_level, content_text, display_order, min_wpm, min_accuracy)
WHERE NOT EXISTS (SELECT 1 FROM lessons WHERE difficulty_level = 'INTERMEDIATE');

INSERT INTO lessons (title, difficulty_level, content_text, display_order, min_wpm, min_accuracy)
SELECT * FROM (VALUES
  ('Numbers and Data',        'ADVANCED', 'In 2024, the top 10 tech startups raised $1.2 billion in Series B funding rounds. The average valuation reached $450 million per company across all sectors. With 3,847 employees across 26 global offices, these companies serve over 2.5 million active users. Revenue grew 78% year-over-year, reaching a combined $890 million total across the entire portfolio.',                                                                                                                                                            17, 50, 85.0),
  ('Special Characters',      'ADVANCED', 'user@example.com; config["port"] = 8080; SELECT * FROM users WHERE id = 42; curl -X POST https://api.example.com/v2/users -H "Content-Type: application/json"; URL: https://app.example.com/dashboard?tab=analytics&page=1; path: /usr/local/bin/node; env: NODE_ENV=production',                                                                                                                                                                                                                                              18, 50, 85.0),
  ('Java Code',               'ADVANCED', 'public class UserService { private final UserRepository repository; public UserService(UserRepository repository) { this.repository = repository; } public User findById(Long id) { return repository.findById(id).orElseThrow(() -> new RuntimeException("User not found: " + id)); } public List<User> findAll() { return repository.findAll(); } }',                                                                                                                                                                           19, 50, 85.0),
  ('Python Code',             'ADVANCED', 'def quicksort(arr): if len(arr) <= 1: return arr pivot = arr[len(arr) // 2] left = [x for x in arr if x < pivot] middle = [x for x in arr if x == pivot] right = [x for x in arr if x > pivot] return quicksort(left) + middle + quicksort(right) print(quicksort([3, 6, 8, 10, 1, 2, 1]))',                                                                                                                                                                                                                              20, 50, 85.0),
  ('Cloud Computing',         'ADVANCED', 'Cloud computing has fundamentally transformed how modern software systems are designed and deployed at scale. Developers now provision infrastructure within minutes using platforms like AWS, Azure, and Google Cloud. Kubernetes orchestrates containers across distributed clusters, while service meshes manage traffic routing and observability. The architectural shift from monolithic applications to microservices enables independent deployment and scaling of each service component.',                                   21, 50, 85.0),
  ('Clean Code Principles',   'ADVANCED', 'Clean code is not written by following a rigid set of rules alone. It is crafted by a programmer who genuinely cares about their work and its long-term maintainability. Writing truly readable code means choosing meaningful variable names, keeping functions short and focused, and cleanly separating concerns across well-defined module boundaries. A well-designed function should perform exactly one responsibility and perform it exceptionally well every time.',                                                       22, 50, 85.0),
  ('Speed Test Pangrams',     'ADVANCED', 'The quick brown fox jumps over the lazy dog. Pack my box with five dozen liquor jugs immediately. Sphinx of black quartz, judge my vow right now. How vexingly quick daft zebras jump over the fence! The five boxing wizards jump quickly past the giant hexagonal pool. Crazy Fredrick bought many exquisite opal jewels for his beloved girlfriend last Tuesday evening.',                                                                                                                                                    23, 50, 85.0),
  ('Artificial Intelligence', 'ADVANCED', 'Artificial intelligence is fundamentally reshaping every major industry across the planet at an unprecedented pace. Machine learning algorithms process vast datasets to discover meaningful patterns completely invisible to human analysts. Natural language processing now enables computers to understand, generate, and translate human speech with remarkable fluency and accuracy. Deep neural networks trained on millions of carefully labeled examples can recognize objects in images with superhuman precision, revolutionizing fields from medical diagnosis to fully autonomous vehicles navigating complex urban environments.', 24, 50, 85.0)
) AS v(title, difficulty_level, content_text, display_order, min_wpm, min_accuracy)
WHERE NOT EXISTS (SELECT 1 FROM lessons WHERE difficulty_level = 'ADVANCED');

-- Fix min_wpm for existing lessons seeded with wrong default
UPDATE lessons SET min_wpm = 35 WHERE difficulty_level = 'INTERMEDIATE' AND min_wpm = 20;
UPDATE lessons SET min_wpm = 50 WHERE difficulty_level = 'ADVANCED'     AND min_wpm = 20 AND is_ai_generated = FALSE;

-- ============================================================
-- ADMIN USER
-- IMPORTANT: Change this password before going live!
--   1. Generate hash: use BCrypt online tool or Spring's BCryptPasswordEncoder
--   2. Set ADMIN_PASSWORD_HASH env var OR replace hash directly
-- ============================================================
INSERT INTO app_users (username, user_password, role, email, email_verified, password_changed)
SELECT 'admin', '$2b$10$N95qoF8RcUKtDcUTFh1Of.EJiFblJPz50JJ/PRg/vSplbJp4hrvBq', 'ADMIN', 'admin@typemaster.com', TRUE, TRUE
WHERE NOT EXISTS (SELECT 1 FROM app_users WHERE username = 'admin');

UPDATE app_users
SET role             = 'ADMIN',
    email_verified   = TRUE,
    password_changed = TRUE
WHERE username = 'admin';

-- ============================================================
-- EXAMS — PostgreSQL uses string_agg() instead of LISTAGG()
-- ============================================================
INSERT INTO exams (difficulty_level, duration_minutes, min_wpm, min_accuracy, content_text, is_active)
SELECT 'BASIC', 15, 25, 85.0,
    (SELECT string_agg(content_text, ' | ' ORDER BY display_order)
     FROM lessons WHERE difficulty_level = 'BASIC' AND is_ai_generated = FALSE),
    TRUE
WHERE NOT EXISTS (SELECT 1 FROM exams WHERE difficulty_level = 'BASIC');

INSERT INTO exams (difficulty_level, duration_minutes, min_wpm, min_accuracy, content_text, is_active)
SELECT 'INTERMEDIATE', 30, 40, 87.0,
    (SELECT string_agg(content_text, ' | ' ORDER BY display_order)
     FROM lessons WHERE difficulty_level = 'INTERMEDIATE' AND is_ai_generated = FALSE),
    TRUE
WHERE NOT EXISTS (SELECT 1 FROM exams WHERE difficulty_level = 'INTERMEDIATE');

INSERT INTO exams (difficulty_level, duration_minutes, min_wpm, min_accuracy, content_text, is_active)
SELECT 'ADVANCED', 60, 55, 90.0,
    (SELECT string_agg(content_text, ' | ' ORDER BY display_order)
     FROM lessons WHERE difficulty_level = 'ADVANCED' AND is_ai_generated = FALSE),
    TRUE
WHERE NOT EXISTS (SELECT 1 FROM exams WHERE difficulty_level = 'ADVANCED');

-- ============================================================
-- H2 → POSTGRESQL DATA MIGRATION
-- Restores: admin profile, sampatk, alkasampat, yukthaasealu
-- All statements idempotent — safe on every startup restart
-- ============================================================

-- Update admin profile (skipped if already set)
UPDATE app_users
SET full_name     = 'Admin Test User',
    date_of_birth = '1984-03-12',
    occupation    = 'System Administrator'
WHERE username = 'admin' AND (full_name IS NULL OR full_name = '');

-- ── Users ─────────────────────────────────────────────────────────────────────
INSERT INTO app_users (username, user_password, role, email, email_verified, password_changed,
                       placement_completed, placement_wpm, recommended_tier, is_active,
                       full_name, is_student, school_name, class_year, course_specialization)
SELECT 'sampatk', '$2a$10$CukuNbfTXGjuJGhz.rMR4OKpn5MH9yNIE4sNj5NiBgWvHqX3QmNS6', 'USER',
       NULL, FALSE, FALSE, TRUE, 0, 'BASIC', TRUE, '', FALSE, NULL, NULL, NULL
WHERE NOT EXISTS (SELECT 1 FROM app_users WHERE username = 'sampatk');

INSERT INTO app_users (username, user_password, role, email, email_verified, password_changed,
                       placement_completed, is_active, full_name, date_of_birth,
                       occupation, is_student, school_name, class_year, course_specialization)
SELECT 'alkasampat', '$2a$10$YpSxth0HHStF4SQtxLIeBOLxY0jokPfsKoQdxZW3Bcj9Ct/A3qTxi', 'USER',
       'neelu742@gmail.com', TRUE, TRUE, FALSE, TRUE,
       'Neelima Asealu', '1989-06-10', 'home maker', FALSE, NULL, NULL, NULL
WHERE NOT EXISTS (SELECT 1 FROM app_users WHERE username = 'alkasampat');

INSERT INTO app_users (username, user_password, role, email, email_verified, password_changed,
                       placement_completed, is_active, full_name, date_of_birth,
                       is_student, school_name, class_year, course_specialization)
SELECT 'yukthaasealu', '$2a$10$f0iSsmN5JZUgP5l2fWehSuaep9zt3Q6rVBNshjvV718XtEDMUd5jS', 'USER',
       NULL, FALSE, TRUE, FALSE, TRUE,
       'Yuktha Asealu', '2012-04-11', TRUE,
       'Vikas The Concept School', 'IX', 'CBSE'
WHERE NOT EXISTS (SELECT 1 FROM app_users WHERE username = 'yukthaasealu');

-- ── Performance: admin (lessons 1-16) ────────────────────────────────────────
INSERT INTO user_performance (accuracy_percentage, completed_at, wpm, lesson_id, user_id)
SELECT v.acc, v.ts::timestamp, v.wpm, v.lid, (SELECT id FROM app_users WHERE username = 'admin')
FROM (VALUES
  (100.0,'2026-06-11 15:15:23', 45,  1),(100.0,'2026-06-11 15:16:10', 42,  2),
  (100.0,'2026-06-11 16:41:51', 43,  3),(100.0,'2026-06-11 16:42:35', 60,  4),
  (100.0,'2026-06-11 16:43:05', 58,  5),(100.0,'2026-06-11 16:43:40', 50,  6),
  (100.0,'2026-06-11 16:44:15', 48,  7),(100.0,'2026-06-11 16:44:52', 47,  8),
  (100.0,'2026-06-12 00:41:15', 61,  9),(100.0,'2026-06-12 00:42:05', 55, 10),
  (100.0,'2026-06-12 00:43:08', 44, 11),(100.0,'2026-06-12 00:44:13', 38, 12),
  ( 99.0,'2026-06-12 22:10:41', 58, 13),(100.0,'2026-06-12 22:11:37', 63, 14),
  ( 97.0,'2026-06-12 22:12:37', 58, 15),( 99.0,'2026-06-12 22:13:40', 60, 16)
) AS v(acc, ts, wpm, lid)
WHERE NOT EXISTS (
  SELECT 1 FROM user_performance
  WHERE user_id = (SELECT id FROM app_users WHERE username = 'admin')
    AND lesson_id = v.lid AND completed_at = v.ts::timestamp
);

-- ── Performance: sampatk (lessons 1-17) ──────────────────────────────────────
INSERT INTO user_performance (accuracy_percentage, completed_at, wpm, lesson_id, user_id)
SELECT v.acc, v.ts::timestamp, v.wpm, v.lid, (SELECT id FROM app_users WHERE username = 'sampatk')
FROM (VALUES
  (100.0,'2026-06-09 16:53:31', 40,  1),(100.0,'2026-06-09 16:54:22', 40,  2),
  (100.0,'2026-06-09 16:55:08', 37,  3),(100.0,'2026-06-09 16:55:44', 52,  4),
  (100.0,'2026-06-09 16:56:22', 52,  5),(100.0,'2026-06-09 16:57:01', 45,  6),
  (100.0,'2026-06-09 16:58:06', 25,  7),(100.0,'2026-06-09 16:58:53', 40,  8),
  (100.0,'2026-06-09 17:00:09', 48,  9),(100.0,'2026-06-09 17:01:20', 43, 10),
  (100.0,'2026-06-09 17:02:29', 43, 11),( 95.0,'2026-06-09 17:03:50', 35, 12),
  (100.0,'2026-06-10 17:37:41', 56, 13),(100.0,'2026-06-10 17:38:42', 64, 14),
  (100.0,'2026-06-10 17:39:43', 52, 15),(100.0,'2026-06-10 17:40:55', 52, 16),
  (100.0,'2026-06-10 17:42:41', 45, 17)
) AS v(acc, ts, wpm, lid)
WHERE NOT EXISTS (
  SELECT 1 FROM user_performance
  WHERE user_id = (SELECT id FROM app_users WHERE username = 'sampatk')
    AND lesson_id = v.lid AND completed_at = v.ts::timestamp
);

-- ── Performance: alkasampat (lessons 1-6, multiple runs on some) ──────────────
INSERT INTO user_performance (accuracy_percentage, completed_at, wpm, lesson_id, user_id)
SELECT v.acc, v.ts::timestamp, v.wpm, v.lid, (SELECT id FROM app_users WHERE username = 'alkasampat')
FROM (VALUES
  (91.0,'2026-06-10 14:53:59',  6, 1),(88.0,'2026-06-10 14:57:09',  8, 1),
  (95.0,'2026-06-10 15:00:20',  9, 2),(94.0,'2026-06-10 15:03:19',  9, 3),
  (98.0,'2026-06-10 15:06:09', 10, 4),(98.0,'2026-06-10 15:08:51', 10, 5),
  (90.0,'2026-06-10 15:12:53',  6, 6)
) AS v(acc, ts, wpm, lid)
WHERE NOT EXISTS (
  SELECT 1 FROM user_performance
  WHERE user_id = (SELECT id FROM app_users WHERE username = 'alkasampat')
    AND lesson_id = v.lid AND completed_at = v.ts::timestamp
);

-- ── Performance: yukthaasealu (lessons 1-20 + repeat runs on lesson 1) ───────
INSERT INTO user_performance (accuracy_percentage, completed_at, wpm, lesson_id, user_id)
SELECT v.acc, v.ts::timestamp, v.wpm, v.lid, (SELECT id FROM app_users WHERE username = 'yukthaasealu')
FROM (VALUES
  ( 99.0,'2026-06-10 15:21:30',  6,  1),(100.0,'2026-06-10 15:24:07', 10,  2),
  (100.0,'2026-06-10 15:29:33',  9,  3),( 93.0,'2026-06-10 15:33:48',  6,  4),
  ( 98.0,'2026-06-10 15:38:47',  6,  5),(100.0,'2026-06-10 15:41:47',  9,  6),
  (100.0,'2026-06-10 15:46:39',  8,  7),(100.0,'2026-06-10 15:48:42', 14,  8),
  (100.0,'2026-06-10 15:52:37', 10,  9),( 96.0,'2026-06-10 15:57:40',  8, 10),
  ( 97.0,'2026-06-10 16:02:16', 10, 11),( 99.0,'2026-06-10 16:07:56',  7, 12),
  (100.0,'2026-06-11 09:01:34',  9, 13),(100.0,'2026-06-11 09:06:07', 12, 14),
  (100.0,'2026-06-11 09:10:35', 11, 15),(100.0,'2026-06-11 09:16:24', 10, 16),
  (100.0,'2026-06-11 09:23:22', 11, 17),(100.0,'2026-06-11 09:31:10',  8, 18),
  ( 98.0,'2026-06-11 09:42:36',  6, 19),( 98.0,'2026-06-11 09:53:32',  5, 20),
  (100.0,'2026-06-12 09:33:40',  9,  1),(100.0,'2026-06-12 09:35:44', 14,  1),
  (100.0,'2026-06-12 09:39:19', 10,  1),( 97.0,'2026-06-14 10:26:21', 13,  1),
  ( 97.0,'2026-06-14 10:29:47', 16,  1),( 93.0,'2026-06-14 10:34:03', 17,  1),
  ( 97.0,'2026-06-14 10:35:39', 17,  1)
) AS v(acc, ts, wpm, lid)
WHERE NOT EXISTS (
  SELECT 1 FROM user_performance
  WHERE user_id = (SELECT id FROM app_users WHERE username = 'yukthaasealu')
    AND lesson_id = v.lid AND completed_at = v.ts::timestamp
);

-- ── Exam attempts ─────────────────────────────────────────────────────────────
INSERT INTO exam_attempts (accuracy, completed_at, passed, started_at, wpm, exam_id, user_id)
SELECT 100.0, '2026-06-11 16:52:40.972091'::timestamp, TRUE, '2026-06-11 16:47:51.000000'::timestamp, 41,
  (SELECT id FROM exams WHERE difficulty_level = 'BASIC'),
  (SELECT id FROM app_users WHERE username = 'admin')
WHERE NOT EXISTS (
  SELECT 1 FROM exam_attempts
  WHERE user_id = (SELECT id FROM app_users WHERE username = 'admin')
    AND exam_id = (SELECT id FROM exams WHERE difficulty_level = 'BASIC')
    AND completed_at = '2026-06-11 16:52:40.972091'::timestamp
);

INSERT INTO exam_attempts (accuracy, completed_at, passed, started_at, wpm, exam_id, user_id)
SELECT 99.0, '2026-06-12 11:38:43.438667'::timestamp, TRUE, '2026-06-12 11:34:01.000000'::timestamp, 42,
  (SELECT id FROM exams WHERE difficulty_level = 'BASIC'),
  (SELECT id FROM app_users WHERE username = 'sampatk')
WHERE NOT EXISTS (
  SELECT 1 FROM exam_attempts
  WHERE user_id = (SELECT id FROM app_users WHERE username = 'sampatk')
    AND exam_id = (SELECT id FROM exams WHERE difficulty_level = 'BASIC')
    AND completed_at = '2026-06-12 11:38:43.438667'::timestamp
);

INSERT INTO exam_attempts (accuracy, completed_at, passed, started_at, wpm, exam_id, user_id)
SELECT 99.0, '2026-06-12 11:47:21.710666'::timestamp, TRUE, '2026-06-12 11:40:40.000000'::timestamp, 53,
  (SELECT id FROM exams WHERE difficulty_level = 'INTERMEDIATE'),
  (SELECT id FROM app_users WHERE username = 'sampatk')
WHERE NOT EXISTS (
  SELECT 1 FROM exam_attempts
  WHERE user_id = (SELECT id FROM app_users WHERE username = 'sampatk')
    AND exam_id = (SELECT id FROM exams WHERE difficulty_level = 'INTERMEDIATE')
    AND completed_at = '2026-06-12 11:47:21.710666'::timestamp
);

INSERT INTO exam_attempts (accuracy, completed_at, passed, started_at, wpm, exam_id, user_id)
SELECT 99.0, '2026-06-12 22:20:16.881180'::timestamp, TRUE, '2026-06-12 22:13:53.000000'::timestamp, 55,
  (SELECT id FROM exams WHERE difficulty_level = 'INTERMEDIATE'),
  (SELECT id FROM app_users WHERE username = 'admin')
WHERE NOT EXISTS (
  SELECT 1 FROM exam_attempts
  WHERE user_id = (SELECT id FROM app_users WHERE username = 'admin')
    AND exam_id = (SELECT id FROM exams WHERE difficulty_level = 'INTERMEDIATE')
    AND completed_at = '2026-06-12 22:20:16.881180'::timestamp
);

-- ── Certificates ──────────────────────────────────────────────────────────────
INSERT INTO certificates (certificate_id, difficulty_level, issued_at, pdf_data, exam_attempt_id, user_id)
SELECT 'a2831ebf-c964-403f-8890-04d682ce11c2', 'BASIC', '2026-06-11 16:52:40.985977'::timestamp,
  NULL,
  (SELECT id FROM exam_attempts
   WHERE user_id = (SELECT id FROM app_users WHERE username = 'admin')
     AND exam_id = (SELECT id FROM exams WHERE difficulty_level = 'BASIC')
     AND completed_at = '2026-06-11 16:52:40.972091'::timestamp),
  (SELECT id FROM app_users WHERE username = 'admin')
WHERE NOT EXISTS (SELECT 1 FROM certificates WHERE certificate_id = 'a2831ebf-c964-403f-8890-04d682ce11c2');

INSERT INTO certificates (certificate_id, difficulty_level, issued_at, pdf_data, exam_attempt_id, user_id)
SELECT '9817fb8f-d86b-4bfb-ae92-bba6ea50d1b3', 'BASIC', '2026-06-12 11:38:43.588463'::timestamp,
  NULL,
  (SELECT id FROM exam_attempts
   WHERE user_id = (SELECT id FROM app_users WHERE username = 'sampatk')
     AND exam_id = (SELECT id FROM exams WHERE difficulty_level = 'BASIC')
     AND completed_at = '2026-06-12 11:38:43.438667'::timestamp),
  (SELECT id FROM app_users WHERE username = 'sampatk')
WHERE NOT EXISTS (SELECT 1 FROM certificates WHERE certificate_id = '9817fb8f-d86b-4bfb-ae92-bba6ea50d1b3');

INSERT INTO certificates (certificate_id, difficulty_level, issued_at, pdf_data, exam_attempt_id, user_id)
SELECT 'ddc426dc-7483-46c0-9e57-798ccc75a51b', 'INTERMEDIATE', '2026-06-12 11:47:21.779914'::timestamp,
  NULL,
  (SELECT id FROM exam_attempts
   WHERE user_id = (SELECT id FROM app_users WHERE username = 'sampatk')
     AND exam_id = (SELECT id FROM exams WHERE difficulty_level = 'INTERMEDIATE')
     AND completed_at = '2026-06-12 11:47:21.710666'::timestamp),
  (SELECT id FROM app_users WHERE username = 'sampatk')
WHERE NOT EXISTS (SELECT 1 FROM certificates WHERE certificate_id = 'ddc426dc-7483-46c0-9e57-798ccc75a51b');

INSERT INTO certificates (certificate_id, difficulty_level, issued_at, pdf_data, exam_attempt_id, user_id)
SELECT '6618b803-9397-45a6-a245-f51143811860', 'INTERMEDIATE', '2026-06-12 22:20:17.194923'::timestamp,
  NULL,
  (SELECT id FROM exam_attempts
   WHERE user_id = (SELECT id FROM app_users WHERE username = 'admin')
     AND exam_id = (SELECT id FROM exams WHERE difficulty_level = 'INTERMEDIATE')
     AND completed_at = '2026-06-12 22:20:16.881180'::timestamp),
  (SELECT id FROM app_users WHERE username = 'admin')
WHERE NOT EXISTS (SELECT 1 FROM certificates WHERE certificate_id = '6618b803-9397-45a6-a245-f51143811860');

-- ── Inquiries ─────────────────────────────────────────────────────────────────
INSERT INTO inquiries (message, status, subject, user_id, admin_response, created_at, reopen_count)
SELECT 'hello there', 'RESOLVED', 'this is a test question',
  (SELECT id FROM app_users WHERE username = 'sampatk'),
  'thank you for raising this inquiry',
  '2026-06-09 17:04:36.554039'::timestamp, 0
WHERE NOT EXISTS (
  SELECT 1 FROM inquiries
  WHERE user_id = (SELECT id FROM app_users WHERE username = 'sampatk')
    AND created_at = '2026-06-09 17:04:36.554039'::timestamp
);

INSERT INTO inquiries (message, status, subject, user_id, admin_response, created_at, reopen_count)
SELECT 'djfsdkljfskldfjsdlfjsdkljslf', 'RESOLVED', 'regarding lesson 5',
  (SELECT id FROM app_users WHERE username = 'sampatk'),
  'please send proper request. Avoid sending spam. thank you!',
  '2026-06-10 12:05:14.340767'::timestamp, 0
WHERE NOT EXISTS (
  SELECT 1 FROM inquiries
  WHERE user_id = (SELECT id FROM app_users WHERE username = 'sampatk')
    AND created_at = '2026-06-10 12:05:14.340767'::timestamp
);

INSERT INTO inquiries (message, status, subject, user_id, admin_response, created_at, reopen_count)
SELECT 'my avg speed isn''t visible', 'RESOLVED', 'about my avg speed',
  (SELECT id FROM app_users WHERE username = 'yukthaasealu'),
  'please use FAQ for help navigating the dashboards. Use My Progress module to see your progress. Let us know if the problem still exists.',
  '2026-06-10 15:26:17.166617'::timestamp, 0
WHERE NOT EXISTS (
  SELECT 1 FROM inquiries
  WHERE user_id = (SELECT id FROM app_users WHERE username = 'yukthaasealu')
    AND created_at = '2026-06-10 15:26:17.166617'::timestamp
);

-- Password reset for sampatk (idempotent: skips if already set to target hash)
UPDATE app_users
SET user_password = '$2a$10$VLQTELV.Wy4nCe46dGNSDe.YhVHmKvAGpZH7QtPOLKkXgS.7uoVku',
    password_changed = TRUE
WHERE username = 'sampatk'
  AND user_password != '$2a$10$VLQTELV.Wy4nCe46dGNSDe.YhVHmKvAGpZH7QtPOLKkXgS.7uoVku';
