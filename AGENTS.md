# AGENTS.md

## Agents

### User (Project Architect)
- Reviews all outputs
- Assigns tasks to Codex
- Directs architecture and goals
- Maintains repo state

### ChatGPT
- Maintains project memory and context
- Generates/updates documentation
- Defines or clarifies technical direction
- Assists in prompt writing for Codex

### Codex
- Generates all source code under direction
- Follows prompts from ChatGPT or the user
- Creates, refactors, or completes implementation files

## Workflow Rules
- All code changes are reviewed before commit
- Codex tasks initiated via structured prompts
- Task tracking and decisions logged in TASKS.md and DEVLOG.md
- Repo state may only change via reviewed PRs or Git commits
