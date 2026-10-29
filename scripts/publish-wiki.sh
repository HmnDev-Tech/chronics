#!/usr/bin/env bash
# Publish docs/wiki/*.md to the GitHub Wiki of the Chronics repository.
#
# Requires: git and an authenticated GitHub CLI (gh auth login) so that the
# credential helper can clone and push the private wiki repository.
set -euo pipefail

REPO="${CHRONICS_REPO:-HmnDev-Tech/chronics}"
SOURCE_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/docs/wiki"
WORK_DIR="$(mktemp -d)"
trap 'rm -rf "$WORK_DIR"' EXIT

if [ ! -d "$SOURCE_DIR" ]; then
    echo "Wiki source not found: $SOURCE_DIR" >&2
    exit 1
fi

echo "Cloning https://github.com/${REPO}.wiki.git"
git clone "https://github.com/${REPO}.wiki.git" "$WORK_DIR/wiki"

for page in "$SOURCE_DIR"/*.md; do
    name="$(basename "$page")"
    [ "$name" = "README.md" ] && continue
    cp "$page" "$WORK_DIR/wiki/$name"
done

cd "$WORK_DIR/wiki"
git add -A
if git diff --cached --quiet; then
    echo "Wiki is already up to date."
    exit 0
fi
git -c user.name="Chronics Wiki" -c user.email="noreply@github.com" \
    commit -m "docs: sync wiki from docs/wiki"
git push origin HEAD
echo "Wiki published."
