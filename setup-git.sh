#!/bin/sh
# setup-git.sh
#
# Run this once after extracting the archive.
# It initialises a git repo, makes the first commit, and (optionally)
# pushes to GitHub.
#
# Usage:
#   chmod +x setup-git.sh
#   ./setup-git.sh
#
# With a remote URL (pushes straight to GitHub):
#   ./setup-git.sh https://github.com/YOUR_USERNAME/songlink-share.git

set -e

REMOTE_URL="$1"

echo ""
echo "==> Initialising git repository..."
git init
git add .
git commit -m "Initial commit: Songlink Share Android app"

echo ""
echo "==> Renaming branch to 'main'..."
git branch -M main

if [ -n "$REMOTE_URL" ]; then
  echo ""
  echo "==> Adding remote: $REMOTE_URL"
  git remote add origin "$REMOTE_URL"
  echo ""
  echo "==> Pushing to GitHub..."
  git push -u origin main
  echo ""
  echo "Done! Your repo is live at: $REMOTE_URL"
else
  echo ""
  echo "Done! Git repo created locally."
  echo ""
  echo "To push to GitHub, run:"
  echo "  git remote add origin https://github.com/YOUR_USERNAME/songlink-share.git"
  echo "  git push -u origin main"
fi
