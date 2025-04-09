-- Script to clean up duplicate practice tests in the practice_assessments table
-- This script will identify and remove duplicate entries while keeping one entry for each unique student-assessment pair

-- First, let's see how many duplicates we have
SELECT 
    student_id, 
    assessment_id,
    COUNT(*) as count
FROM 
    practice_assessments
GROUP BY 
    student_id, assessment_id
HAVING 
    COUNT(*) > 1;

-- Create a temporary table to store the practice_ids we want to keep
CREATE TEMPORARY TABLE practice_ids_to_keep AS
SELECT MIN(practice_id) as practice_id
FROM practice_assessments
GROUP BY student_id, assessment_id;

-- Delete all practice tests that aren't in our "to keep" list
DELETE FROM practice_assessments
WHERE practice_id NOT IN (SELECT practice_id FROM practice_ids_to_keep);

-- Drop our temporary table
DROP TEMPORARY TABLE practice_ids_to_keep;

-- Verify the cleanup - this should return no rows if all duplicates were removed
SELECT 
    student_id, 
    assessment_id, 
    COUNT(*) as count
FROM 
    practice_assessments
GROUP BY 
    student_id, assessment_id
HAVING 
    COUNT(*) > 1;
