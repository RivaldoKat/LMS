ALTER TABLE submission
    ADD COLUMN graded_at TIMESTAMP;

UPDATE submission
SET graded_at = submitted_at
WHERE status = 'GRADED'
  AND graded_at IS NULL;

CREATE INDEX idx_submission_graded_at ON submission (graded_at);
