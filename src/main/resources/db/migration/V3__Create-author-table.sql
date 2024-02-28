CREATE TABLE author
(
    id         serial PRIMARY KEY,
    full_name  text NOT NULL,
    created_at timestamptz DEFAULT now()
);

COMMENT
ON TABLE author IS 'Автор внесения записи';

COMMENT
ON COLUMN author.id IS 'ID автора';
COMMENT
ON COLUMN author.full_name IS 'ФИО автора';
COMMENT
ON COLUMN author.created_at IS 'Дата и время создания записи';

