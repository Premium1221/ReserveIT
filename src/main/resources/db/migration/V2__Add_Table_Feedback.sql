CREATE TABLE feedback (
                          id INT PRIMARY KEY,
                          reservation_id INT,
                          feedback_text TEXT,
                          FOREIGN KEY (reservation_id) REFERENCES reservations(id)
);
