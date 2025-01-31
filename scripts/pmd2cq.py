#!/usr/bin/env python3

# SPDX-FileCopyrightText: 2025 Contributors to the HPCToolkit Project
#
# SPDX-License-Identifier: Apache-2.0

import json
import sys
from pathlib import Path
import os
import hashlib

topdir = Path(os.environ["CI_PROJECT_DIR"]) if "CI_PROJECT_DIR" in os.environ else Path().absolute()
(infile,) = sys.argv[1:]

data = []
with open(infile, "r") as f:
    for segment in f.read().split("\0"):
        if segment and not segment.isspace():
            data.append(json.loads(segment))


def fixup_segment(seg):
    seg["location"]["path"] = Path(seg["location"]["path"]).relative_to(topdir).as_posix()
    seg["fingerprint"] = hashlib.sha1(json.dumps(seg).encode("utf-8")).hexdigest()
    return seg


json.dump([fixup_segment(s) for s in data], sys.stdout)
