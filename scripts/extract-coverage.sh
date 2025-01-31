#!/bin/bash -e

# SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
#
# SPDX-License-Identifier: Apache-2.0

for COUNTER in INSTRUCTION BRANCH LINE COMPLEXITY METHOD CLASS; do
  grep -o "<counter type=\"$COUNTER\" missed=\"[[:digit:]]*\" covered=\"[[:digit:]]*\"/>" "$1" > tmp.coverage
  tail -n1 tmp.coverage | grep -o '[[:digit:]]*' > tmp.coverage.nums
  MISSED="$(head -n1 tmp.coverage.nums)"
  COVERED="$(tail -n1 tmp.coverage.nums)"

  if [ $((MISSED + COVERED)) -gt 0 ]; then
    echo "$COUNTER coverage: `echo $((COVERED * 100000 / (MISSED + COVERED))) | sed 's/\(...\)$/.\1/'`%"
  else
    echo "$COUNTER coverage: 0%"
  fi
done