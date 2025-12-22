-- Создание таблицы метаданных файлов
CREATE TABLE file_metadata (
                               id BIGSERIAL PRIMARY KEY,
                               file_name VARCHAR(255) NOT NULL,
                               file_path VARCHAR(500) NOT NULL,
                               content_type VARCHAR(100) NOT NULL,
                               file_size BIGINT NOT NULL,
                               uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               deleted_at TIMESTAMP,

    -- Проверки
                               CONSTRAINT chk_file_size_positive CHECK (file_size > 0),
                               CONSTRAINT chk_file_path_length CHECK (LENGTH(file_path) <= 500)
);

-- Создание индексов отдельными командами
CREATE INDEX idx_file_metadata_uploaded_at ON file_metadata (uploaded_at);

-- Дополнительные полезные индексы:
CREATE INDEX idx_file_metadata_content_type ON file_metadata (content_type);
CREATE INDEX idx_file_metadata_file_name ON file_metadata (file_name);

-- Индекс для soft-delete (если часто фильтруете по не удаленным файлам)
