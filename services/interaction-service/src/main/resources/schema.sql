CREATE TABLE IF NOT EXISTS community_topic (
    topic_id BIGINT NOT NULL AUTO_INCREMENT,
    creator_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (topic_id)
);
CREATE TABLE IF NOT EXISTS topic_post (
    post_id BIGINT NOT NULL AUTO_INCREMENT, topic_id BIGINT NOT NULL, author_id BIGINT NOT NULL,
    content VARCHAR(2000) NOT NULL, PRIMARY KEY (post_id)
);
CREATE TABLE IF NOT EXISTS topic_comment (
    comment_id BIGINT NOT NULL AUTO_INCREMENT, post_id BIGINT NOT NULL, author_id BIGINT NOT NULL,
    content VARCHAR(1000) NOT NULL, PRIMARY KEY (comment_id)
);
CREATE TABLE IF NOT EXISTS topic_post_like (
    id BIGINT NOT NULL AUTO_INCREMENT, post_id BIGINT NOT NULL, user_id BIGINT NOT NULL,
    PRIMARY KEY (id), UNIQUE (post_id, user_id)
);
CREATE TABLE IF NOT EXISTS topic_post_action (
    id BIGINT NOT NULL AUTO_INCREMENT, post_id BIGINT NOT NULL, user_id BIGINT NOT NULL,
    action_type VARCHAR(20) NOT NULL, PRIMARY KEY (id)
);
CREATE TABLE IF NOT EXISTS follow_topic (
    id BIGINT NOT NULL AUTO_INCREMENT, topic_id BIGINT NOT NULL, user_id BIGINT NOT NULL,
    PRIMARY KEY (id), UNIQUE (topic_id, user_id)
);
CREATE TABLE IF NOT EXISTS conversation (
    conversation_id BIGINT NOT NULL AUTO_INCREMENT, buyer_id BIGINT NOT NULL, seller_id BIGINT NOT NULL,
    product_id BIGINT, status VARCHAR(20) NOT NULL, PRIMARY KEY (conversation_id)
);
CREATE TABLE IF NOT EXISTS chat_message (
    message_id BIGINT NOT NULL AUTO_INCREMENT, conversation_id BIGINT NOT NULL, sender_id BIGINT NOT NULL,
    content VARCHAR(2000) NOT NULL, message_type VARCHAR(20) NOT NULL, PRIMARY KEY (message_id)
);
