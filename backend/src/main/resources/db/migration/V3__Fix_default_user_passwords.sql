-- Correct default account password hashes to match the documented credentials.
UPDATE users
SET password = '$2a$10$2QrAVzIk0bIQ7WEHznEqF.D2.Oc7ejF0aIfkKFSQWNcdSz/DgLaJ.'
WHERE username = 'admin';

UPDATE users
SET password = '$2a$10$b6YTdX1HfJGV3Dl0RO3gVenhDKnul4SZ2OlGqU/kzMPzqjRfegzvq'
WHERE username = 'manager';

UPDATE users
SET password = '$2a$10$ceKRFbOAUFYvP8symEvbdeHBtt9gbX4wbACvYUOGk8KeaQzfYDhHC'
WHERE username = 'sales';
