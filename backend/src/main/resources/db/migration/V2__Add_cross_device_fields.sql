-- Add cross-device support fields to progress table
ALTER TABLE progress ADD COLUMN device_type VARCHAR(50);
ALTER TABLE progress ADD COLUMN current_step VARCHAR(100);

-- Add index for device type queries
CREATE INDEX idx_progress_device_type ON progress(device_type);

-- Add index for user progress queries with updated_at ordering
CREATE INDEX idx_progress_user_updated ON progress(user_id, updated_at DESC);