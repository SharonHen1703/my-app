-- V3__auctions.sql (PostgreSQL compatible)

CREATE TABLE auctions (
  id                SERIAL PRIMARY KEY,
  title             VARCHAR(255) NOT NULL,
  description       TEXT         NOT NULL,
  condition         VARCHAR(50)  NOT NULL CHECK (condition IN ('new','like_new','used','refurbished','damaged_or_parts')),

  categories        VARCHAR(2000) NOT NULL DEFAULT '[]',
  image_urls        VARCHAR(2000) NOT NULL DEFAULT '[]',

  min_price         DECIMAL(12,2) NOT NULL CHECK (min_price >= 0),
  buy_now_price     DECIMAL(12,2) NULL CHECK (buy_now_price IS NULL OR buy_now_price >= min_price),

  bid_increment     DECIMAL(12,2) NOT NULL CHECK (bid_increment > 0),

  current_bid_amount DECIMAL(12,2) NULL,
  highest_max_bid    DECIMAL(12,2) NULL,
  highest_user_id    BIGINT        NULL,
  bids_count         INTEGER       NOT NULL DEFAULT 0 CHECK (bids_count >= 0),

  start_date        TIMESTAMP NOT NULL,
  end_date          TIMESTAMP NOT NULL CHECK (end_date > start_date),

  seller_id         BIGINT      NOT NULL,
  status            VARCHAR(50) NOT NULL CHECK (status IN ('active','sold','unsold')),

  created_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

  CHECK (current_bid_amount IS NULL OR current_bid_amount >= min_price),
  CHECK (highest_max_bid  IS NULL OR current_bid_amount IS NULL OR current_bid_amount <= highest_max_bid),
  CHECK ((highest_user_id IS NULL) = (highest_max_bid IS NULL)),
  
  FOREIGN KEY (seller_id) REFERENCES users(id),
  FOREIGN KEY (highest_user_id) REFERENCES users(id)
);

CREATE INDEX idx_auctions_seller_id ON auctions (seller_id);
