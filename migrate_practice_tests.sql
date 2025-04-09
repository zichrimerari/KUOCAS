-- SQL script to migrate practice tests to the practice_assessments table
-- This ensures that practice tests created by students are properly linked to them

-- Insert practice tests from assessments table to practice_assessments table
INSERT INTO practice_assessments (
    practice_id,
    assessment_id,
    student_id,
    title,
    unit_code,
    score,
    total_possible,
    percentage,
    grade,
    status
)
SELECT 
    UUID(), -- Generate a unique practice_id
    a.assessment_id,
    a.created_by,
    a.title,
    a.unit_code,
    0, -- Initial score is 0
    a.total_marks,
    0, -- Initial percentage is 0
    'N/A', -- Initial grade is N/A
    'CREATED' -- Status is CREATED
FROM 
    assessments a
WHERE 
    a.is_practice = 1
    AND NOT EXISTS (
        SELECT 1 
        FROM practice_assessments pa 
        WHERE pa.assessment_id = a.assessment_id 
        AND pa.student_id = a.created_by
    );

-- Show the count of migrated practice tests
SELECT COUNT(*) AS migrated_tests FROM practice_assessments;
