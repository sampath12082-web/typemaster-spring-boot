---
name: doc-writer
description: Update, maintain, audit, and sync all project documentation with actual app functionality. Use when asked to update docs, maintain documentation, sync docs with code, ensure docs are current, write missing documentation, or document a new feature/bug/enhancement.
---

# Document Writer & Maintainer

Audits the entire documentation set against the running application, identifies gaps and stale content, then updates all docs to match reality.

## Documentation Inventory

| File | Purpose | When to Update |
|------|---------|----------------|
| `CLAUDE.md` | Claude Code guidance — build commands, architecture | When project structure or build process changes |
| `docs/quality/BUGS.md` | Bug tracking with severity, root cause, resolution | When bugs are found, investigated, or fixed |
| `docs/quality/ENHANCEMENTS.md` | Feature/enhancement tracking with priority and status | Before starting new features, after completing them |
| `docs/quality/MARKET_COMPARISON.md` | Competitive analysis and roadmap | After major feature additions or market research |
| `docs/quality/SECURITY_AUDIT.md` | Security findings tracker | After security reviews |
| `docs/quality/TEST_PLAN.md` | Test strategy and coverage | When test approach changes |
| `docs/architecture/HLD.md` / `LLD.md` | High/low-level design | When architecture changes |
| `docs/standards/CODING_STANDARDS.md` | Naming, layering, conventions | When conventions evolve |

## Rules

### Before Any Code Change
1. **New feature?** → Add to `docs/quality/ENHANCEMENTS.md` with number, priority, status "Open"
2. **Bug found?** → Add to `docs/quality/BUGS.md` with severity, root cause, status "Open"

### After Code Changes
1. **Bug fixed?** → Move to Resolved in `BUGS.md` with resolution and date
2. **Enhancement done?** → Update status in `ENHANCEMENTS.md`
3. **New API endpoint?** → Update `CLAUDE.md` if architecturally significant
4. **Auth/security changed?** → Update `CLAUDE.md` auth section
5. **Build/structure changed?** → Update `CLAUDE.md` commands section

### Audit Process

1. **Inventory current state** — count entities, controllers, services, tests
2. **Compare docs against code** — find stale references, wrong counts, missing features
3. **Fix every gap** — use live data, not memory
4. **Verify** — ensure consistency across all docs

## Quality Standards

- **Numbers come from live queries**, not from memory
- **Every command in docs must work** — run it before writing it
- **Status in ENHANCEMENTS.md must reflect reality** — check the code
- **Test inventory in CLAUDE.md must list all test files** — run `find`
