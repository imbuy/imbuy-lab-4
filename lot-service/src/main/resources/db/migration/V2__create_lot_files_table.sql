-- Создание таблицы для файлов лотов
CREATE TABLE lot_files (
    id BIGSERIAL PRIMARY KEY,
    lot_id BIGINT NOT NULL,
    file_id BIGINT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    file_size BIGINT NOT NULL,
    uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Внешний ключ на таблицу lots
    CONSTRAINT fk_lot_files_lot FOREIGN KEY (lot_id) REFERENCES lots(id) ON DELETE CASCADE,
    
    -- Проверки
    CONSTRAINT chk_file_size_positive CHECK (file_size > 0),
    CONSTRAINT chk_file_path_length CHECK (LENGTH(file_path) <= 500),
    
    -- Уникальность: один файл может быть связан с одним лотом только один раз
    CONSTRAINT uk_lot_files_file_lot UNIQUE (lot_id, file_id)
);

-- Создание индексов для оптимизации запросов
CREATE INDEX idx_lot_files_lot_id ON lot_files (lot_id);
CREATE INDEX idx_lot_files_file_id ON lot_files (file_id);
CREATE INDEX idx_lot_files_uploaded_at ON lot_files (uploaded_at);

