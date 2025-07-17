-- Just for reference.
-- This sql script is never used,
-- just for demonstration, and transparency purposes!

CREATE TABLE IF NOT EXISTS "users" (
  "id" SERIAL PRIMARY KEY,
  "username" VARCHAR(255) UNIQUE NOT NULL,
  "email" VARCHAR(255) UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS "ssh_keys" (
  "id" SERIAL PRIMARY KEY,
  "user_id" INTEGER NOT NULL,
  "key_type" VARCHAR(50) NOT NULL,
  "server" TEXT NOT NULL,
  "public_key" TEXT NOT NULL,
  "fingerprint" TEXT NOT NULL,
  "accepted_by" VARCHAR(255) NOT NULL,
  "issued_date" TIMESTAMP DEFAULT now(),
  "valid_until" TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS "audit_log" (
  "id" SERIAL PRIMARY KEY,
  "title" VARCHAR(255) NOT NULL,
  "message" TEXT NOT NULL,
  "timestamp" TIMESTAMP DEFAULT now()
);

CREATE TABLE IF NOT EXISTS "requests" (
  "id" SERIAL PRIMARY KEY,
  "user_id" INTEGER NOT NULL,
  "server" TEXT NOT NULL,
  "public_key" TEXT NOT NULL,
  "key_type" TEXT NOT NULL,
  "timestamp" TIMESTAMP DEFAULT now()
);

ALTER TABLE "ssh_keys" ADD CONSTRAINT fk_user FOREIGN KEY ("user_id") REFERENCES "users"("id");

ALTER TABLE "requests" ADD CONSTRAINT fk_user_request FOREIGN KEY ("user_id") REFERENCES "users"("id");
