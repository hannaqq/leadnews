USE leadnews_wemedia;

-- This query must return no rows before adding the constraint. If it returns
-- data, reconcile duplicate application-user accounts first.
SELECT ap_user_id, COUNT(*) AS account_count
FROM wm_user
WHERE ap_user_id IS NOT NULL
GROUP BY ap_user_id
HAVING COUNT(*) > 1;

ALTER TABLE wm_user
    ADD CONSTRAINT uk_wm_user_ap_user_id UNIQUE (ap_user_id);
