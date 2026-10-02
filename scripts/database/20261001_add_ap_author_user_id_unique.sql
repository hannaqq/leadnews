USE leadnews_article;

-- This query must return no rows before adding the constraint. If it returns
-- data, resolve the duplicate authors first instead of deleting them blindly.
SELECT user_id, COUNT(*) AS author_count
FROM ap_author
WHERE user_id IS NOT NULL
GROUP BY user_id
HAVING COUNT(*) > 1;

ALTER TABLE ap_author
    ADD CONSTRAINT uk_ap_author_user_id UNIQUE (user_id);
