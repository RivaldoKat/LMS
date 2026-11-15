ALTER TABLE announcements
    ADD COLUMN course_id BIGINT;

ALTER TABLE announcements
    ADD CONSTRAINT fk_announcements_course
        FOREIGN KEY (course_id) REFERENCES courses (id);

CREATE INDEX idx_announcements_course_id ON announcements (course_id);
