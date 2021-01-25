CREATE TABLE likes(
    liked_user_id TEXT NOT NULL REFERENCES users(id),
    liked_by_user_id TEXT NOT NULL REFERENCES users(id),
    liked_at TIMESTAMP NOT NULL,
    PRIMARY KEY(liked_user_id, liked_by_user_id)
)