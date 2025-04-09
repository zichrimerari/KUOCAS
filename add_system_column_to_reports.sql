-- Add system column to reports table
ALTER TABLE reports ADD COLUMN system BOOLEAN DEFAULT FALSE;

-- Update existing reports to mark system reports
UPDATE reports SET system = TRUE WHERE report_name IN ('Student Performance Report', 'Assessment Statistics Report', 'Practice Test Performance Report');
