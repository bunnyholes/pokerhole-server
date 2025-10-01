#!/bin/bash
# PokerHole Terminal Client
# Connects to localhost:7777 with proper terminal settings

# Save current terminal settings
old_settings=$(stty -g)

# Set raw mode for proper key handling
stty raw -echo

# Cleanup function
cleanup() {
    stty "$old_settings"
    echo ""
    echo "연결 종료"
}
trap cleanup EXIT INT TERM

# Connect to server
nc localhost 7777
