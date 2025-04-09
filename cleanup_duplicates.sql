-- Script to clean up duplicate practice tests
USE kenyatta_cat_system;

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

-- Keep only one entry for each student-assessment pair
DELETE p1 FROM practice_assessments p1
INNER JOIN practice_assessments p2
WHERE 
    p1.student_id = p2.student_id 
    AND p1.assessment_id = p2.assessment_id
    AND p1.practice_id > p2.practice_id;

-- Verify the cleanup
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
