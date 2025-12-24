-- Добавление поля lot_id в таблицу file_metadata
ALTER TABLE file_metadata ADD COLUMN lot_id BIGINT;

-- Создание индекса для оптимизации запросов по lot_id
CREATE INDEX idx_file_metadata_lot_id ON file_metadata (lot_id);

