ALTER TABLE budget
    ADD COLUMN author_id int
        REFERENCES author (id) DEFAULT NULL;

COMMENT
ON COLUMN budget.author_id IS 'Автор внесения записи';