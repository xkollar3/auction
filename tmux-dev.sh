#!/bin/bash

# Tmux development environment setup for auction-marketplace
# Creates 5 windows in the current tmux session

REPO_DIR=$(pwd)
BE_DIR="$REPO_DIR/backend"
FE_DIR="$REPO_DIR/frontend"

# Configure tmux to hide directory names in window status
tmux set-option -g window-status-format "#I:#W"
tmux set-option -g window-status-current-format "#I:#W"

# Window 0: IDE for Backend
tmux rename-window "IDE-BE"
tmux send-keys "cd $BE_DIR && nvim" C-m

# Window 1: IDE for Frontend
tmux new-window -n "IDE-FE" -c "$FE_DIR"
tmux send-keys "nvim" C-m

# Window 2: Claude Code (split - BE left, FE right)
tmux new-window -n "claude" -c "$BE_DIR"
tmux send-keys "claude" C-m
tmux split-window -h -c "$FE_DIR"
tmux send-keys "claude" C-m
tmux select-pane -t 0

# Window 3: App (split - BE left, FE right)
tmux new-window -n "app" -c "$BE_DIR"
tmux send-keys "mvn spring-boot:run -Dspring-boot.run.profiles=dev" C-m
tmux split-window -h -c "$FE_DIR"
tmux send-keys "npm run dev" C-m
tmux select-pane -t 0

# Window 4: Docker Compose
tmux new-window -n "docker" -c "$REPO_DIR"
tmux send-keys "docker compose up" C-m

# Select the first window (IDE-BE)
tmux select-window -t 0
