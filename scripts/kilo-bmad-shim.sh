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
# The shim is also where the completion-marker guard plugin
# (kilo-bmad-marker-guard.js) gets loaded: KILO_CONFIG_CONTENT REPLACES the whole
# config, so a `plugin:` list in .kilo/kilo.json would never apply, and the config
# arrives as content with no declaring directory — hence an absolute file:// URL
# rather than a relative path. See MARKER GUARD below.
#
# exec (not a plain call) is required: the adapter tracks and kills the server
# by the PID it spawned, so the shim must not stay in the process chain.
set -euo pipefail

[[ -n "${OPENCODE_SERVER_PASSWORD:-}" ]] && export KILO_SERVER_PASSWORD="$OPENCODE_SERVER_PASSWORD"
[[ -n "${OPENCODE_CONFIG_CONTENT:-}" ]] && export KILO_CONFIG_CONTENT="$OPENCODE_CONFIG_CONTENT"
[[ -n "${OPENCODE_DISABLE_EXTERNAL_SKILLS:-}" ]] && export KILO_DISABLE_EXTERNAL_SKILLS="$OPENCODE_DISABLE_EXTERNAL_SKILLS"
export KILO_SERVER_USERNAME="opencode"

# --- MARKER GUARD -----------------------------------------------------------
# Append the guard to the config's `plugin` array, preserving any entry already
# there. Every step degrades to "launch unguarded" rather than failing the run:
# a broken guard must never cost a story. Set BMAD_KILO_MARKER_GUARD=0 to skip.
guard_js="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/kilo-bmad-marker-guard.js"
if [[ "${BMAD_KILO_MARKER_GUARD:-1}" != "0" && -n "${KILO_CONFIG_CONTENT:-}" && -f "$guard_js" ]] \
  && command -v jq >/dev/null 2>&1; then
  if patched=$(printf '%s' "$KILO_CONFIG_CONTENT" \
      | jq -c --arg p "file://$guard_js" '.plugin = ((.plugin // []) + [$p] | unique)' 2>/dev/null) \
     && [[ -n "$patched" ]]; then
    export KILO_CONFIG_CONTENT="$patched"
  else
    echo "kilo-bmad-shim: could not inject the marker guard; launching unguarded" >&2
  fi
fi

exec kilo "$@"
