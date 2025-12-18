#!/bin/bash

# Tmux development environment setup for auction-marketplace
# Creates 4 windows in the current tmux session

REPO_DIR=$(pwd)

# Configure tmux to hide directory names in window status
tmux set-option -g window-status-format "#I:#W"
tmux set-option -g window-status-current-format "#I:#W"

# Rename current window to IDE (window 0)
tmux rename-window "IDE"
tmux send-keys "cd $REPO_DIR && nvim" C-m

# Window 1: Claude Code
tmux new-window -n "claude" -c "$REPO_DIR"
tmux send-keys "claude" C-m

# Window 2: Spring Boot App
tmux new-window -n "app" -c "$REPO_DIR"
tmux send-keys "mvn spring-boot:run -Dspring-boot.run.profiles=dev" C-m

# Window 3: Docker Compose
tmux new-window -n "docker" -c "$REPO_DIR"
tmux send-keys "docker compose up" C-m

# Select the first window (IDE)
tmux select-window -t 0
