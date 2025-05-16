#!/bin/bash

# Make sure Newman is installed
if ! command -v newman &> /dev/null; then
  echo "Newman is not installed. Installing..."
  npm install -g newman
fi

# Define colors for better output
GREEN='\033[0;32m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}Running UserAuth Service API Tests...${NC}"

# Run the tests using Newman
newman run userauth-postman-collection.json -e userauth-environment.json --insecure

# Check the exit code
if [ $? -eq 0 ]; then
  echo -e "${GREEN}Tests completed successfully!${NC}"
else
  echo -e "${RED}Tests failed!${NC}"
  exit 1
fi 