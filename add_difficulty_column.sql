USE kenyatta_cat_system;

-- Add difficulty column to questions table
ALTER TABLE questions ADD COLUMN difficulty VARCHAR(20) DEFAULT 'MEDIUM';

-- Update the difficulty values for existing questions based on their marks
-- Easy: marks <= 2
-- Medium: marks > 2 AND marks <= 4
-- Hard: marks > 4
UPDATE questions SET difficulty = 'EASY' WHERE marks <= 2;
UPDATE questions SET difficulty = 'MEDIUM' WHERE marks > 2 AND marks <= 4;
UPDATE questions SET difficulty = 'HARD' WHERE marks > 4;
