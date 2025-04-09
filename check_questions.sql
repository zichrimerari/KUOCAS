-- Check questions for SCO100 unit
USE kenyatta_cat_system;

-- Check what difficulty values are actually stored in the database
SELECT DISTINCT difficulty FROM questions WHERE unit_code = 'SCO100';

-- Count questions by difficulty
SELECT difficulty, COUNT(*) as count FROM questions WHERE unit_code = 'SCO100' GROUP BY difficulty;

-- Check what question types are available
SELECT DISTINCT question_type FROM questions WHERE unit_code = 'SCO100';

-- Count questions by question type
SELECT question_type, COUNT(*) as count FROM questions WHERE unit_code = 'SCO100' GROUP BY question_type;

-- Check what topics are available
SELECT DISTINCT topic FROM questions WHERE unit_code = 'SCO100';

-- Count questions by topic
SELECT topic, COUNT(*) as count FROM questions WHERE unit_code = 'SCO100' GROUP BY topic;

-- Sample of actual questions
SELECT question_id, question_text, question_type, difficulty, topic FROM questions WHERE unit_code = 'SCO100' LIMIT 10;
