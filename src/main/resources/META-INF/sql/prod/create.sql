CREATE TABLE account(
    account_id BIGINT NOT NULL AUTO_INCREMENT,
    balance DECIMAL NOT NULL,
    version BIGINT NOT NULL,
    PRIMARY KEY(account_id)
);

CREATE TABLE transfer_order(
    transfer_id CHAR(36),
    ts BIGINT NOT NULL,
    source_account_id BIGINT NOT NULL,
    target_account_id BIGINT NOT NULL,
    transfer_amount DECIMAL NOT NULL,
    PRIMARY KEY (transfer_id),
    FOREIGN KEY (source_account_id) REFERENCES account(account_id),
    FOREIGN KEY (target_account_id) REFERENCES account(account_id)
);