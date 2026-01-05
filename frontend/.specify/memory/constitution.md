<!--
============================================================================
SYNC IMPACT REPORT
============================================================================
Version change: 1.0.0 → 1.1.0 (MINOR - new principle added)

Modified principles: None

Added sections:
- Principle VIII: Frontend-Only Scope

Removed sections: None

Templates requiring updates:
- ✅ .specify/templates/plan-template.md - Updated project structure
- ✅ .specify/templates/tasks-template.md - Updated path conventions

Follow-up TODOs: None
============================================================================
-->

# Auction Frontend Constitution

## Core Principles

### I. Locked Technology Stack

The project uses a fixed set of technologies. No additional component libraries,
styling frameworks, data-fetching libraries, state management solutions, or
schema validation libraries may be introduced.

**Approved stack:**
- **UI Components**: shadcn/ui
- **Styling**: Tailwind CSS
- **Data Fetching**: TanStack Query with Axios
- **Global State**: Zustand
- **Schema Validation**: Zod
- **Build/Runtime**: React + TypeScript + Vite

Any proposal to add new dependencies in these categories MUST be rejected.
Use existing patterns and the approved libraries exclusively.

### II. Styling Hierarchy

All styling MUST follow this strict precedence:

1. **shadcn components** - Use pre-built shadcn components first
2. **Tailwind CSS** - Apply utility classes for customization
3. **Raw CSS** - Only as an absolute last resort when Tailwind cannot achieve
   the required effect

Raw CSS usage requires explicit justification in code comments explaining why
Tailwind was insufficient.

### III. Schema Validation with Zod

All object schemas, API response validation, and form validation MUST use Zod.
No alternative validation libraries (yup, joi, io-ts, etc.) may be added.

When implementing new validation:
- Look for existing Zod patterns in the codebase and adapt them
- Create reusable schema compositions where possible
- Co-locate schemas with their related types

### IV. Data Fetching Architecture

All remote data operations MUST use TanStack Query with Axios:

- **TanStack Query**: Manages caching, loading states, error states, and refetching
- **Axios**: Handles HTTP transport layer

Direct `fetch()` calls or other HTTP clients are prohibited. All API calls
MUST flow through the established TanStack Query + Axios pattern.

### V. State Management with Zustand

Global application state MUST be managed through Zustand stores:

- Use Zustand for cross-component state that doesn't belong to server state
- Server state (API data) belongs in TanStack Query, not Zustand
- Keep stores focused and small; prefer multiple stores over one monolithic store
- Avoid duplicating TanStack Query cache data in Zustand

### VI. Component Architecture

React components MUST follow these conventions:

- **One component per file** - Each component lives in its own file
- **PascalCase naming** - Component files and exports use PascalCase (e.g., `UserProfile.tsx`)
- **Shared components** - Reusable components live in `/shared` directory
- **Page components** - Page-level components live in `/pages` directory
- **No deep nesting** - Avoid component nesting beyond 2-3 levels without
  explicit justification
- **Composition over hierarchy** - Pages compose shared components; avoid
  creating deep component trees

### VII. Design Fidelity

UI implementation MUST match designs in `/design` directory with the following rules:

- **Architectural fidelity is NON-NEGOTIABLE** - Pages defined in designs MUST exist;
  no pages may be added or removed without design updates
- **Component-level flexibility** - Within a page, components may be:
  - Rearranged for better UX
  - Restyled while maintaining visual intent
  - Grouped or separated as implementation requires

Changes at the page architecture level require design document updates first.

### VIII. Frontend-Only Scope

This project is exclusively a frontend/UI application. All work MUST be confined
to the `frontend/` directory.

**Absolute boundaries:**
- All source code changes MUST occur within `frontend/`
- All specifications and plans live in `frontend/specs/`
- The backend directory (`../`) MUST NEVER be modified
- No changes to parent directories or sibling projects are permitted

**Rationale**: The backend is a separate concern with its own repository/workflow.
This frontend consumes backend APIs via REST and WebSocket but has no authority
to modify backend code. Speckit workflows operate exclusively on frontend assets.

## Technology Constraints

**Prohibited additions:**
- Component libraries (Material-UI, Chakra, Ant Design, etc.)
- CSS frameworks (Bootstrap, Bulma, etc.)
- CSS-in-JS libraries (styled-components, emotion, etc.)
- Alternative state managers (Redux, MobX, Jotai, Recoil, etc.)
- Alternative data-fetching (SWR, RTK Query, etc.)
- Alternative validation (yup, joi, valibot, etc.)

**Permitted additions:**
- Utility libraries that don't overlap with core stack (date-fns, lodash, etc.)
- Development tools (testing libraries, linters, etc.)
- shadcn component additions from the official registry

## Component Structure

```
frontend/
├── src/
│   ├── pages/           # Page-level components (route destinations)
│   ├── shared/          # Reusable components shared across pages
│   ├── hooks/           # Custom React hooks
│   ├── stores/          # Zustand stores
│   ├── api/             # API layer (Axios + TanStack Query)
│   ├── schemas/         # Zod schemas
│   └── types/           # TypeScript type definitions
├── specs/               # Feature specifications and plans
│   └── [###-feature]/   # Per-feature documentation
├── design/              # Design assets and mockups
└── .specify/            # Speckit configuration and templates
```

## Governance

This constitution establishes non-negotiable rules for the Auction Frontend project.

**Amendment procedure:**
1. Propose change with rationale and impact analysis
2. Document what existing code would need modification
3. Update constitution version following semantic versioning
4. Update all affected code to comply

**Versioning policy:**
- MAJOR: Removing or fundamentally changing a principle
- MINOR: Adding new principles or expanding existing guidance
- PATCH: Clarifications and wording improvements

**Compliance review:**
- All PRs MUST be verified against these principles
- Constitution violations block merge
- Justified exceptions MUST be documented in code and PR description

**Version**: 1.1.0 | **Ratified**: 2026-01-05 | **Last Amended**: 2026-01-05
