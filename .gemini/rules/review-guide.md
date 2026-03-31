# Code Reviewer Guide

**Goal:** Maximize code health (Quality vs Speed balance).

---

## Mandatory (Logic & Arch)

### Architecture

- Fit existing system
- Apply SOLID principles
- Block overengineering (YAGNI)

### Functionality

- Verify intent
- Handle edge cases
- Prevent race conditions
- Prevent deadlocks

### Security

- Block SQLi/XSS
- Validate inputs
- Verify authentication
- **ZERO** sensitive data leaks (passwords/PII)

### Performance

- Block N+1 queries
- Prevent memory leaks
- Prevent load degradation

### Tests

- Require automated tests
- Must fail on broken code

### Complexity

- Enforce simplicity & readability (lines/funcs/classes)
- Block tangles

---

## Critical (Logging & Errors)

### Log Security (LOG-SEC)

- **ZERO** sensitive data in logs (PII, tokens, DB paths, cards)
- Block raw stack traces / internal state exposure

### Log Info (LOG-INFO)

- Require meaningful context for Root Cause Analysis (RCA)

### Log Spam (LOG-SPAM)

- Block useless/spam logs
- Prevent logging performance hits

### Log Errors (LOG-ERR)

- Require try-catch graceful degradation
- Block swallowed/ignored exceptions

### Log Store (LOG-STORE)

- Ensure architecture supports 6+ months log retention

---

## Optional (Style & Meta)

### Style

- Defer to linters (ESLint/Prettier)
- Ignore subjective spacing

### Naming

- Require clear, concise, intent-revealing names

### Documentation

- Update README/API

### Nits

- Prefix non-blocking formatting/polish feedback with "Nit:" or "Optional:"
