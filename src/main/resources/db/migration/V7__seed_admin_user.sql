-- the very first admin, nobody exists yet to enable one through the app
-- password is 'ChangeMe123!' hashed with BCrypt, log in once and change it for real
INSERT INTO users (id, username, email, password_hash, enabled, role)
VALUES (
           gen_random_uuid(),
           'admin',
           'admin@financeapp.local',
           '$2a$10$EbY0wCg8w1yV0LKzM2rG4uK6vN3H7dGZ1QhP4y5r6sT8uV9wX0yZ2',
           true,
           'ADMIN'
       );