-- Create wallets table
CREATE TABLE IF NOT EXISTS public.wallets (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
  balance DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
  UNIQUE(user_id)
);

-- Add RLS policies
ALTER TABLE public.wallets ENABLE ROW LEVEL SECURITY;

-- Policy for users to view their own wallet
CREATE POLICY "Users can view their own wallet" 
  ON public.wallets
  FOR SELECT 
  USING (auth.uid() = user_id);

-- Policy for users to update their own wallet (this will be through functions only)
CREATE POLICY "System can update any wallet" 
  ON public.wallets
  FOR UPDATE 
  USING (true)
  WITH CHECK (true);
  
-- Policy for users to create their wallet
CREATE POLICY "Users can create their own wallet" 
  ON public.wallets
  FOR INSERT 
  WITH CHECK (auth.uid() = user_id);

-- Function to add funds to wallet
CREATE OR REPLACE FUNCTION public.add_funds(user_id UUID, amount DECIMAL)
RETURNS public.wallets AS $$
DECLARE
  wallet public.wallets;
BEGIN
  -- Ensure amount is positive
  IF amount <= 0 THEN
    RAISE EXCEPTION 'Amount must be positive';
  END IF;

  -- Update the wallet
  UPDATE public.wallets
  SET balance = balance + amount,
      updated_at = now()
  WHERE wallets.user_id = $1
  RETURNING * INTO wallet;

  -- If no wallet found, create one
  IF wallet IS NULL THEN
    INSERT INTO public.wallets (user_id, balance)
    VALUES ($1, amount)
    RETURNING * INTO wallet;
  END IF;

  RETURN wallet;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Function to deduct funds from wallet
CREATE OR REPLACE FUNCTION public.deduct_funds(user_id UUID, amount DECIMAL)
RETURNS public.wallets AS $$
DECLARE
  wallet public.wallets;
BEGIN
  -- Ensure amount is positive
  IF amount <= 0 THEN
    RAISE EXCEPTION 'Amount must be positive';
  END IF;

  -- Check if wallet exists
  SELECT * INTO wallet FROM public.wallets WHERE wallets.user_id = $1;
  
  IF wallet IS NULL THEN
    RAISE EXCEPTION 'Wallet not found';
  END IF;
  
  -- Check if sufficient funds
  IF wallet.balance < amount THEN
    RAISE EXCEPTION 'Insufficient funds';
  END IF;

  -- Update the wallet
  UPDATE public.wallets
  SET balance = balance - amount,
      updated_at = now()
  WHERE wallets.user_id = $1
  RETURNING * INTO wallet;

  RETURN wallet;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Create transactions table to track history
CREATE TABLE IF NOT EXISTS public.transactions (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
  amount DECIMAL(10, 2) NOT NULL,
  type VARCHAR(10) NOT NULL CHECK (type IN ('credit', 'debit')),
  description TEXT,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

-- Add RLS policies for transactions
ALTER TABLE public.transactions ENABLE ROW LEVEL SECURITY;

-- Policy for users to view their own transactions
CREATE POLICY "Users can view their own transactions" 
  ON public.transactions
  FOR SELECT 
  USING (auth.uid() = user_id);

-- Trigger to record transactions when funds are added or deducted
CREATE OR REPLACE FUNCTION public.record_wallet_transaction()
RETURNS TRIGGER AS $$
BEGIN
  IF (TG_OP = 'UPDATE') THEN
    -- Calculate the difference
    IF (NEW.balance > OLD.balance) THEN
      -- Credit transaction
      INSERT INTO public.transactions (user_id, amount, type, description)
      VALUES (NEW.user_id, (NEW.balance - OLD.balance), 'credit', 'Funds added');
    ELSIF (NEW.balance < OLD.balance) THEN
      -- Debit transaction
      INSERT INTO public.transactions (user_id, amount, type, description)
      VALUES (NEW.user_id, (OLD.balance - NEW.balance), 'debit', 'Funds deducted');
    END IF;
  ELSIF (TG_OP = 'INSERT') THEN
    -- New wallet created with initial balance
    IF (NEW.balance > 0) THEN
      INSERT INTO public.transactions (user_id, amount, type, description)
      VALUES (NEW.user_id, NEW.balance, 'credit', 'Initial deposit');
    END IF;
  END IF;
  
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Add trigger to wallets table
CREATE TRIGGER wallet_transaction_trigger
AFTER INSERT OR UPDATE ON public.wallets
FOR EACH ROW
EXECUTE FUNCTION public.record_wallet_transaction(); 