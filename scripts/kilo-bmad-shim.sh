#!/usr/bin/env bash
# bmad-loop <-> Kilo CLI shim.
#
# bmad-loop drives hookless profiles through its OpencodeHttpAdapter, which
# hardcodes OpenCode's env contract: OPENCODE_SERVER_PASSWORD (per-session
# basic-auth secret), OPENCODE_CONFIG_CONTENT (permission allow + hermetic
# skills path + model) and OPENCODE_DISABLE_EXTERNAL_SKILLS. Kilo is a fork of
# OpenCode with an identical HTTP API and an identical env contract renamed to
# the KILO_ prefix — it ignores OPENCODE_* entirely, so without this translation
# the spawned server would run unauthenticated, without `permission: allow`
# (sessions hang on permission prompts) and without the project skill tree.
#
# The adapter also sends basic auth as user `opencode`, while Kilo defaults to
# `kilo`; KILO_SERVER_USERNAME pins it back.
#
# exec (not a plain call) is required: the adapter tracks and kills the server
# by the PID it spawned, so the shim must not stay in the process chain.
set -euo pipefail

[[ -n "${OPENCODE_SERVER_PASSWORD:-}" ]] && export KILO_SERVER_PASSWORD="$OPENCODE_SERVER_PASSWORD"
[[ -n "${OPENCODE_CONFIG_CONTENT:-}" ]] && export KILO_CONFIG_CONTENT="$OPENCODE_CONFIG_CONTENT"
[[ -n "${OPENCODE_DISABLE_EXTERNAL_SKILLS:-}" ]] && export KILO_DISABLE_EXTERNAL_SKILLS="$OPENCODE_DISABLE_EXTERNAL_SKILLS"
export KILO_SERVER_USERNAME="opencode"

exec kilo "$@"
