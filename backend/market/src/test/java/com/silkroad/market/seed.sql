-- Seed Test Accounts
-- Run this after creating the database

INSERT INTO users (email, full_name, password, phone, role, status, username)
VALUES 
    ('student@test.com', 'Test User', '$2a$10$/mzrzoLqdXztTxVG.ojbZOwcHkyd9d814Spcab7rYRvl7w.u6uI7q', '+1234567890', 'USER', 'ACTIVE', 'user'),

    ('admin@test.com', 'Test Admin', '$2a$10$q/C4dyOXLfZV/NVCXz68uOv3yEuwkG4B/mzZ6V5tDjeTCxz8QYQ0i', '+0987654321', 'ADMIN', 'ACTIVE', 'admin'),

    ('user2@test.com', 'Test User Two', '$2a$10$wZhbJ5KnnIPRRF3W5sUpJu9cmvLwYunqg5nApR.LHWTNWDjTmP/Wq', '+1122334455', 'USER', 'ACTIVE', 'user2');
