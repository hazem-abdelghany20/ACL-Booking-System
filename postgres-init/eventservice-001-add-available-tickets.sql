-- run in the eventservice database
ALTER TABLE events
  ADD COLUMN available_tickets integer NOT NULL DEFAULT 0;

-- initialize from existing capacity
UPDATE events
  SET available_tickets = capacity;
