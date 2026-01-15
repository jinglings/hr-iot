# Spec-Kit Usage Guide for HR-IoT Project

This directory contains specifications for features developed using the **spec-kit** methodology - a specification-driven development approach that emphasizes clear requirements, thoughtful planning, and systematic implementation.

---

## Table of Contents

1. [What is Spec-Kit?](#what-is-spec-kit)
2. [When to Use Spec-Kit](#when-to-use-spec-kit)
3. [Workflow Overview](#workflow-overview)
4. [Directory Structure](#directory-structure)
5. [Getting Started](#getting-started)
6. [Spec-Kit Commands](#spec-kit-commands)
7. [Example Walkthrough](#example-walkthrough)
8. [Best Practices](#best-practices)
9. [Integration with Existing Docs](#integration-with-existing-docs)

---

## What is Spec-Kit?

Spec-kit is a development methodology that prioritizes **specifications as executable artifacts**. Instead of jumping directly into coding, you:

1. **Define requirements** clearly (what needs to be built and why)
2. **Design architecture** thoughtfully (how it should be built)
3. **Break down tasks** systematically (steps to get there)
4. **Implement with context** (following the plan)

**Benefits**:
- Clearer requirements reduce rework
- Thoughtful architecture prevents technical debt
- Systematic implementation improves code quality
- Documentation is a natural byproduct
- Easier onboarding (specs explain features comprehensively)

---

## When to Use Spec-Kit

### ✅ Use Spec-Kit For:

- **New Major Features**: Device grouping, alert management, reporting modules
- **Complex Enhancements**: Multi-step workflows, integrations with external systems
- **Cross-Module Features**: Features spanning backend + frontend + database
- **Architectural Changes**: Introducing new patterns or significant refactoring

### ❌ Do NOT Use Spec-Kit For:

- **Simple Bug Fixes**: One-file changes, typo corrections
- **Trivial UI Tweaks**: Color changes, minor text updates
- **Dependency Updates**: Upgrading libraries or frameworks
- **Refactoring**: Code cleanup without functional changes

**Rule of Thumb**: If the feature requires more than 4 hours of work or touches more than 3 files, consider using spec-kit.

---

## Workflow Overview

```
┌──────────────────────────────────────────────────────────────┐
│  PHASE 1: INITIALIZATION                                      │
│  Command: /speckit.constitution                               │
│  Output: Review project principles (see constitution.md)      │
│  Purpose: Refresh your understanding of project conventions   │
└──────────────────────────────────────────────────────────────┘
                              ↓
┌──────────────────────────────────────────────────────────────┐
│  PHASE 2: SPECIFICATION                                       │
│  Command: /speckit.specify                                    │
│  Input: User requirements, business goals                     │
│  Output: features/in-progress/{feature}/01-specification.md   │
│  Purpose: Define WHAT needs to be built and WHY              │
└──────────────────────────────────────────────────────────────┘
                              ↓
┌──────────────────────────────────────────────────────────────┐
│  PHASE 3: PLANNING                                            │
│  Command: /speckit.plan                                       │
│  Input: Specification + existing codebase patterns            │
│  Output: features/in-progress/{feature}/02-plan.md            │
│  Purpose: Design HOW it will be built                         │
└──────────────────────────────────────────────────────────────┘
                              ↓
┌──────────────────────────────────────────────────────────────┐
│  PHASE 4: TASK BREAKDOWN                                      │
│  Command: /speckit.tasks                                      │
│  Input: Architecture plan                                     │
│  Output: features/in-progress/{feature}/03-tasks.md           │
│  Purpose: Break work into ordered, actionable tasks           │
└──────────────────────────────────────────────────────────────┘
                              ↓
┌──────────────────────────────────────────────────────────────┐
│  PHASE 5: IMPLEMENTATION                                      │
│  Command: /speckit.implement (per task)                       │
│  Process: Work through tasks sequentially                     │
│  Output: Actual code + tests                                  │
│  Purpose: Build the feature following the plan                │
└──────────────────────────────────────────────────────────────┘
                              ↓
┌──────────────────────────────────────────────────────────────┐
│  PHASE 6: COMPLETION                                          │
│  Actions:                                                     │
│  - Move spec to features/completed/{feature}/                 │
│  - Update module guides if needed                             │
│  - Document lessons learned                                   │
└──────────────────────────────────────────────────────────────┘
```

---

## Directory Structure

```
specs/
├── .speckit/
│   └── constitution.md              # Project principles and conventions
│
├── features/
│   ├── backlog/                     # Planned features (not started)
│   │   └── {feature-name}/
│   │       └── 01-specification.md
│   │
│   ├── in-progress/                 # Active development
│   │   └── {feature-name}/
│   │       ├── 01-specification.md  # Requirements
│   │       ├── 02-plan.md           # Architecture design
│   │       ├── 03-tasks.md          # Task breakdown
│   │       ├── 04-notes.md          # Implementation notes (optional)
│   │       └── 05-completion.md     # Final summary (optional)
│   │
│   └── completed/                   # Finished and deployed
│       └── {feature-name}/
│           └── [all spec files from in-progress]
│
├── templates/
│   ├── backend-feature.md           # Template for backend-only features
│   ├── frontend-feature.md          # Template for frontend-only features
│   └── fullstack-feature.md         # Template for full-stack features
│
└── README.md                        # This file
```

---

## Getting Started

### Step 1: Choose a Template

Select the appropriate template based on your feature scope:

- **Backend Only**: API endpoints, business logic, database changes
  - Use: `templates/backend-feature.md`

- **Frontend Only**: UI components, pages without new APIs
  - Use: `templates/frontend-feature.md`

- **Full-Stack**: Both backend and frontend work
  - Use: `templates/fullstack-feature.md`

### Step 2: Create Feature Directory

Create a directory for your feature in `features/in-progress/`:

```bash
mkdir -p specs/features/in-progress/device-grouping
```

### Step 3: Start Specification Process

Use spec-kit commands (if using an AI assistant like Claude Code) or manually fill out the chosen template.

**With AI Assistant**:
```
Claude, let's use spec-kit to design a new feature for device grouping.
First, please review /speckit.constitution.
Then, create a specification using /speckit.specify.
```

**Manual**:
Copy the template and fill it out section by section.

---

## Spec-Kit Commands

### `/speckit.constitution`

**Purpose**: Review project principles before starting
**When**: At the beginning of any new feature
**Output**: None (just awareness refresh)
**Details**: Reviews `specs/.speckit/constitution.md` to ensure alignment with project conventions

---

### `/speckit.specify`

**Purpose**: Create feature specification (requirements)
**When**: Starting a new feature
**Input**: User requirements, business goals
**Output**: `01-specification.md`

**Content Includes**:
- Feature overview and goals
- User stories
- Functional requirements (FRs)
- Non-functional requirements (NFRs)
- Success criteria
- Out of scope items

**Tips**:
- Be specific about what success looks like
- Include both happy path and error scenarios
- Reference existing similar features
- Consider multi-tenancy implications

---

### `/speckit.plan`

**Purpose**: Design architecture and implementation approach
**When**: After specification is approved
**Input**: Specification + existing codebase patterns
**Output**: `02-plan.md`

**Content Includes**:
- Component architecture
- Database schema changes
- API endpoint designs
- Frontend component structure
- Integration points
- Technology choices with justification
- Risk assessment

**Tips**:
- Review similar existing features first
- Follow established patterns from CLAUDE.md
- Document WHY for each architectural decision
- Consider edge cases and error handling

---

### `/speckit.tasks`

**Purpose**: Break work into ordered, actionable tasks
**When**: After architecture plan is approved
**Input**: Architecture plan
**Output**: `03-tasks.md`

**Content Includes**:
- Ordered task list with dependencies
- Files to create/modify per task
- Testing strategy per task
- Critical path identification

**Tips**:
- Start with database/backend, then frontend
- Each task should be completable in < 4 hours
- Mark dependencies clearly
- Include testing as separate tasks

---

### `/speckit.implement`

**Purpose**: Implement a specific task
**When**: During development, per task
**Input**: Task details from task list
**Output**: Actual code, tests, documentation

**Tips**:
- Work through tasks sequentially
- Reference existing code for patterns
- Test each task before moving to next
- Update specification if requirements change

---

### Optional Commands

#### `/speckit.clarify`
**Purpose**: Ask clarifying questions about vague requirements
**When**: During specification phase if requirements are unclear

#### `/speckit.analyze`
**Purpose**: Analyze existing codebase for context
**When**: Before planning, to understand existing patterns

#### `/speckit.checklist`
**Purpose**: Verify completeness before committing
**When**: Before finalizing implementation

---

## Example Walkthrough

Let's walk through creating a "Device Grouping" feature using spec-kit.

### Phase 1: Initialization

```
Developer: We need to add device grouping functionality.

Claude: Let me review the project constitution first.
[Reviews specs/.speckit/constitution.md]

I see the project follows:
- Three-layer architecture (Controller → Service → Mapper)
- Multi-tenancy with automatic filtering
- OpenAPI annotations for all endpoints
- MapStruct for conversions

Ready to proceed with specification.
```

### Phase 2: Specification

```
Claude: [Running /speckit.specify]

Creates: specs/features/in-progress/device-grouping/01-specification.md

Key sections:
- Overview: Hierarchical device grouping for organization
- User Stories:
  - As an admin, I want to create device groups
  - As an admin, I want to assign devices to groups
  - As a user, I want to filter devices by group
- Functional Requirements:
  FR1: Support nested groups (tree structure)
  FR2: One device can belong to multiple groups
  FR3: Groups are tenant-specific
- Database: Need iot_device_group and iot_device_group_relation tables
- APIs: CRUD for groups, assign/remove device endpoints
- Frontend: Tree view, drag-drop, filtering
```

### Phase 3: Planning

```
Claude: [Running /speckit.plan]

Creates: specs/features/in-progress/device-grouping/02-plan.md

Key decisions:
- Backend Architecture:
  IotDeviceGroupController
  IotDeviceGroupService + ServiceImpl
  IotDeviceGroupMapper (extends BaseMapperX)
  Standard VO pattern (CreateReqVO, UpdateReqVO, RespVO)

- Database Schema:
  iot_device_group (id, name, parent_id, tenant_id, ...)
  iot_device_group_relation (group_id, device_id, tenant_id)

- Frontend Architecture:
  src/views/iot/deviceGroup/index.vue
  src/views/iot/deviceGroup/GroupTree.vue (tree component)
  src/views/iot/deviceGroup/GroupForm.vue (create/edit dialog)
  src/api/iot/deviceGroup.ts

- Reference: Similar to IotEnergyBuildingController (tree structure)
```

### Phase 4: Task Breakdown

```
Claude: [Running /speckit.tasks]

Creates: specs/features/in-progress/device-grouping/03-tasks.md

Tasks:
1. Database Schema (30 min)
   - Create SQL migration script
   - Add to sql/mysql/iot/

2. Backend - Data Layer (1 hour)
   - IotDeviceGroupDO.java
   - IotDeviceGroupRelationDO.java
   - Mappers

3. Backend - Service Layer (2 hours)
   - Service interface and implementation
   - Business logic and validation

4. Backend - Controller Layer (1.5 hours)
   - All VOs
   - Controller with OpenAPI annotations
   - Permission checks

5. Backend - Testing (1 hour)
   - Service unit tests
   - Permission tests

6. Frontend - API Layer (30 min)
   - TypeScript interfaces
   - API service functions

7. Frontend - Components (3 hours)
   - GroupTree component
   - GroupForm dialog
   - Main index page

8. Frontend - Integration (1 hour)
   - Add group filter to device list
   - Update device table

9. End-to-End Testing (1 hour)
   - Test all workflows

10. Documentation (30 min)
    - Update IOT_MODULE_GUIDE.md

Total: 11-12 hours
```

### Phase 5: Implementation

```
Developer: [Works through tasks]

Task 1: Database Schema ✅
- Created sql/mysql/iot/device_group.sql
- Includes both iot_device_group and relation tables

Task 2: Data Layer ✅
- Created IotDeviceGroupDO with proper annotations
- Created IotDeviceGroupMapper extending BaseMapperX
- Followed pattern from IotDeviceDO

[... continues through all tasks ...]

Task 10: Documentation ✅
- Added "Device Grouping" section to IOT_MODULE_GUIDE.md
- Included API reference and usage examples
```

### Phase 6: Completion

```
Developer:
1. Moved spec to completed:
   mv specs/features/in-progress/device-grouping \
      specs/features/completed/

2. Updated completion notes:
   - Feature took 12 hours (within estimate)
   - Challenge: Optimizing tree queries for large hierarchies
   - Solution: Added recursive CTE for efficient tree loading
   - Lesson: Consider performance testing for tree structures early

3. Feature deployed to production ✅
```

---

## Best Practices

### 1. Start with Constitution

Always review `specs/.speckit/constitution.md` before starting a new feature. It reminds you of:
- Project conventions
- Architecture patterns
- Security requirements
- Testing expectations

### 2. Be Specific in Requirements

**Bad**:
- "Add device management"

**Good**:
- "FR1: Users can create devices with name, type, and product association"
- "FR2: Device names must be unique within a tenant"
- "FR3: Users can filter devices by product and status"

### 3. Reference Existing Code

Don't reinvent the wheel. Always identify similar existing features and reference them:

```markdown
## Reference Implementation

Similar to:
- IotDeviceController (CRUD patterns)
- IotEnergyBuildingController (tree structure)
- IotProductController (simple list endpoint)
```

### 4. Document Decisions

Capture the "WHY" behind architectural choices:

```markdown
### Technology Choice: Use TDengine for Energy Metrics

**Decision**: Store energy consumption data in TDengine instead of MySQL

**Rationale**:
- Time-series data grows rapidly (millions of rows)
- TDengine optimized for time-based queries
- Supports downsampling and aggregation efficiently
- Reduces MySQL storage pressure

**Trade-offs**:
- Additional database dependency
- Team needs to learn TDengine query syntax
```

### 5. Update Specs During Implementation

If requirements change mid-development:

1. Update the specification first
2. Mark changes with "UPDATED [Date]"
3. Explain what changed and why
4. Continue implementation

**Example**:
```markdown
**UPDATED 2024-01-20**: Changed from single-group to multi-group assignment
per user feedback. This allows devices to appear in multiple groups
simultaneously for flexible organization.
```

### 6. Keep Specs Concise

Specifications should be:
- **Scannable**: Use headers, lists, tables
- **Clear**: Avoid ambiguous language
- **Complete**: Cover all requirements, but not over-specify implementation

**Avoid**:
- Pasting entire code files
- Over-documenting trivial details
- Repeating information from CLAUDE.md

### 7. Archive Completed Specs

Once deployed, move specifications to `features/completed/`. This:
- Keeps `in-progress/` clean
- Provides historical documentation
- Helps onboarding (new devs can read past features)

---

## Integration with Existing Docs

Spec-kit **complements** existing documentation:

```
Documentation Hierarchy:

┌─────────────────────────────────────────────┐
│  CLAUDE.md (Project-Wide)                   │
│  - Architecture principles                  │
│  - Development conventions                  │
│  - Tech stack reference                     │
└─────────────────────────────────────────────┘
              │
              ├──▶ Module Guides (Module-Specific)
              │    - IOT_MODULE_GUIDE.md
              │    - 能源管理模块需求计划.md
              │    - Feature capabilities overview
              │
              ├──▶ Spec-Kit Specs (Feature-Specific)
              │    - Detailed requirements
              │    - Implementation plan
              │    - Task tracking
              │
              └──▶ OpenAPI/Swagger (API Documentation)
                   - Live API reference
                   - Auto-generated from annotations
```

### When to Update Which Documentation

| Change | CLAUDE.md | Module Guide | Spec-Kit | OpenAPI |
|--------|-----------|--------------|----------|---------|
| New architectural pattern | ✅ Update | Maybe | Reference | N/A |
| New module capability | No | ✅ Update | Create new spec | N/A |
| New feature | No | Maybe | ✅ Create spec | Auto-generated |
| API endpoint change | No | No | Update spec | ✅ Update annotations |
| Bug fix | No | No | No need | N/A |

---

## Tips for Success

### For AI-Assisted Development (Claude Code, Copilot, etc.)

1. **Provide Context**: Reference the constitution and existing code
2. **Use Commands**: Explicitly use spec-kit commands for clarity
3. **Iterate**: Don't expect perfection on first pass
4. **Review**: Always review generated specs before implementing

### For Manual Specification

1. **Use Templates**: Don't start from blank - copy templates
2. **Fill Incrementally**: Complete one section at a time
3. **Get Feedback**: Review specs with team before implementing
4. **Keep Updated**: Update specs as you learn during implementation

### Common Pitfalls to Avoid

❌ **Skipping Constitution Review**: Leads to inconsistent implementations
❌ **Over-Specifying**: Don't write implementation code in specs
❌ **Under-Specifying**: "Add device feature" is too vague
❌ **Ignoring Existing Patterns**: Reinventing the wheel
❌ **Not Updating Specs**: Specs diverge from actual implementation
❌ **Using Spec-Kit for Trivial Changes**: Overkill for small bugs

---

## FAQ

### Q: Do I need to use spec-kit for every change?

**A**: No! Use it for new features and complex changes. Simple bug fixes, minor UI tweaks, and trivial refactoring don't need formal specifications.

### Q: Can I skip the planning phase and go straight to implementation?

**A**: Not recommended. The planning phase catches architectural issues early, when they're cheap to fix. Skipping it often leads to rework.

### Q: What if requirements change during implementation?

**A**: Update the specification! Mark changes with dates and explain why. The spec should reflect the final implementation (or document why it differs).

### Q: How detailed should task breakdowns be?

**A**: Each task should be completable in under 4 hours. If a task feels too big, break it down further.

### Q: Should specs include code samples?

**A**: Yes, for illustrative purposes (showing API structure, data models). No, don't paste entire implementation files - that's what the actual code is for.

### Q: Can I use spec-kit with existing features?

**A**: Yes! Retroactively documenting a complex existing feature with a spec helps future developers understand it. Place in `completed/`.

### Q: How do I handle multi-developer features?

**A**: Create the spec collaboratively, assign tasks to different developers, use the spec as coordination document. Update as you go.

---

## Getting Help

- **Project Conventions**: See `CLAUDE.md`
- **Module Capabilities**: See module-specific guides (e.g., `IOT_MODULE_GUIDE.md`)
- **Spec-Kit Methodology**: See [GitHub spec-kit repository](https://github.com/github/spec-kit)
- **Similar Features**: Browse `specs/features/completed/` for examples

---

## Conclusion

Spec-kit is a **framework for thoughtful development**. It doesn't replace coding - it makes coding more effective by ensuring you build the right thing, the right way, the first time.

**Remember**:
- Specifications are living documents (update as you learn)
- Planning prevents rework (invest time upfront)
- Documentation is a byproduct (specs serve as feature docs)
- Consistency matters (follow project conventions)

Happy spec-driven development! 🚀

---

**Last Updated**: 2026-01-03
**Version**: 1.0
