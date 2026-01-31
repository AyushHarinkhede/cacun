#!/usr/bin/env bash
# Run from repo root. This performs conservative replacements; review each file after.
find . -type f -name "*.css" -print0 | xargs -0 sed -i \
  -e 's/linear-gradient([^)]*)/var(--light-blue)/g' \
  -e 's/#\([0-9A-Fa-f]\)\{3,6\}/REVIEW_HEX/g' \
  -e 's/rgb([^)]*)/REVIEW_RGB/g'
echo "Done. Search for REVIEW_HEX/REVIEW_RGB and manually replace with var(--light-blue) or var(--light-yellow) as appropriate."

