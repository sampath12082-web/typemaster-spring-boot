-- ============================================================
-- LESSONS — seeded once per tier; includes min_wpm & min_accuracy
-- ============================================================
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

-- Fix min_wpm for INTERMEDIATE and ADVANCED lessons that already existed with default value 20
UPDATE lessons SET min_wpm = 35 WHERE difficulty_level = 'INTERMEDIATE' AND min_wpm = 20;
UPDATE lessons SET min_wpm = 50 WHERE difficulty_level = 'ADVANCED'     AND min_wpm = 20 AND is_ai_generated = FALSE;

-- ============================================================
-- ADMIN USER
-- ============================================================
INSERT INTO app_users (username, user_password, role, email, email_verified, password_changed)
SELECT 'admin', '$2b$10$N95qoF8RcUKtDcUTFh1Of.EJiFblJPz50JJ/PRg/vSplbJp4hrvBq', 'ADMIN', 'admin@typemaster.com', TRUE, TRUE
WHERE NOT EXISTS (SELECT 1 FROM app_users WHERE username = 'admin');

-- Ensure existing admin always has verified email and correct role flags
UPDATE app_users
SET role             = 'ADMIN',
    email_verified   = TRUE,
    password_changed = TRUE
WHERE username = 'admin';

-- ============================================================
-- EXAMS — one per tier, content built from lesson text
-- ============================================================
INSERT INTO exams (difficulty_level, duration_minutes, min_wpm, min_accuracy, content_text, is_active)
SELECT 'BASIC', 15, 25, 85.0,
    (SELECT LISTAGG(content_text, ' | ') WITHIN GROUP (ORDER BY display_order)
     FROM lessons WHERE difficulty_level = 'BASIC' AND is_ai_generated = FALSE),
    TRUE
WHERE NOT EXISTS (SELECT 1 FROM exams WHERE difficulty_level = 'BASIC');

INSERT INTO exams (difficulty_level, duration_minutes, min_wpm, min_accuracy, content_text, is_active)
SELECT 'INTERMEDIATE', 30, 40, 87.0,
    (SELECT LISTAGG(content_text, ' | ') WITHIN GROUP (ORDER BY display_order)
     FROM lessons WHERE difficulty_level = 'INTERMEDIATE' AND is_ai_generated = FALSE),
    TRUE
WHERE NOT EXISTS (SELECT 1 FROM exams WHERE difficulty_level = 'INTERMEDIATE');

INSERT INTO exams (difficulty_level, duration_minutes, min_wpm, min_accuracy, content_text, is_active)
SELECT 'ADVANCED', 60, 55, 90.0,
    (SELECT LISTAGG(content_text, ' | ') WITHIN GROUP (ORDER BY display_order)
     FROM lessons WHERE difficulty_level = 'ADVANCED' AND is_ai_generated = FALSE),
    TRUE
WHERE NOT EXISTS (SELECT 1 FROM exams WHERE difficulty_level = 'ADVANCED');
