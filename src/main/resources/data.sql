CREATE TABLE IF NOT EXISTS company (
                                       id UUID PRIMARY KEY,
                                       name VARCHAR(255),
    address VARCHAR(255),
    phone VARCHAR(20),
    email VARCHAR(255),
    picture_url VARCHAR(255),
    rating FLOAT,
    tags VARCHAR(255)  -- Tags stored as comma-separated string
    );

-- Insert a small set of data with rating and tags
INSERT INTO company (id, name, address, phone, email, picture_url, rating, tags)
VALUES
    ('f2a9be02-4c0c-4c13-bd8b-d1e93dd56927', 'Burgerme', '123 Main St', '1234567890', 'burgerme@test.com', 'https://via.placeholder.com/300x200', 4.7, 'Fast Food,Burgers'),
    ('e8a5f102-4b1d-4c14-bdab-c3f9d35e9615', 'Domino''s', '123 Pizza St', '123-456-7890', 'info@dominos.com', 'https://via.placeholder.com/300x200', 4.1, 'Pizza,Fast Food'),
    ('b5c1d0a2-8c7e-4d1e-a4a6-df2a7b9d5b3c', 'Foodgasm', '789 Curry Blvd', '345-678-9012', 'info@foodgasm.com', 'https://via.placeholder.com/300x200', 4.7, 'Indian,Spicy'),
    ('c4a5d5b3-7b6a-482c-9e8a-bd7d9a8d8f3b', 'New York Pizza', '101 Pizza Ln', '456-789-0123', 'info@nypizza.com', 'https://via.placeholder.com/300x200', 4.2, 'Pizza,Italian');
