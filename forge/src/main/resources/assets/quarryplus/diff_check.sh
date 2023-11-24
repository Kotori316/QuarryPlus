#!/usr/bin/env bash
set -eu

EXECUTE_DIR=$(pwd)

if [ $# -ne 1 ]; then
  echo "Usage: $(basename $0) (relative file path)"
  exit 1
fi

cd $(dirname $0)

cd ../../../../..
search_name=${1#./}
files=($(find . -regex ".*/$search_name" -not -path "./build/*"))
if [ ${#files[@]} -gt 1 ]; then
  diff <(jq --sort-keys . ${files[0]}) <(jq --sort-keys . ${files[1]})

  if [ ${#files[@]} -gt 2 ]; then
    echo "You have ${#files[@]} files for $1."
  fi
else
  echo "No duplicated files."
fi

cd "$EXECUTE_DIR"
