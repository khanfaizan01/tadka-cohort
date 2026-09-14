# Diagram Style Guide

Conventions for all Mermaid diagrams in this repo.

## Tools

- **Mermaid** syntax (rendered by GitHub, VS Code, or [mermaid.live](https://mermaid.live))
- Use `sequenceDiagram` for request/response flows
- Use `erDiagram` for entity-relationship diagrams
- Use `graph TD` or `graph LR` for architecture/flow diagrams
- Use `timeline` for chronological progressions

## Conventions

| Element | Convention |
|---------|------------|
| Participants | `participant X as "Label"` |
| HTTP requests | `Client->>Controller: METHOD /path` |
| Database calls | `Repository->>DB: SQL statement` |
| Responses | `Controller-->>Client: HTTP status` |
| Notes | `Note over X,Y: text` |
| Groups | `subgraph Name["Display Name"] ... end` |

## Color coding (optional)

| Color | Meaning |
|-------|---------|
| Green (#4CAF50) | Success / read path |
| Red (#F44336) | Error / failure path |
| Orange (#FF9800) | Side effect / async |
| Blue (#2196F3) | Database / persistence |

## Rules

1. Every diagram must have a caption explaining what it shows
2. Every diagram must reference the ADR(s) it illustrates
3. Keep diagrams to < 15 participants
4. Use `break` markers to indicate where a failure occurs
5. Sequence diagrams should show the *break* and the *fix*
