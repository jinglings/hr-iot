# [Feature Name] - Full-Stack Implementation Specification

> **Status**: [In Progress / Completed]
> **Created**: [Date]
> **Last Updated**: [Date]
> **Developers**: [Backend Developer Name] / [Frontend Developer Name]

---

## Overview

[Comprehensive description of the feature including both backend and frontend aspects]

**Feature Scope**:
- Backend: [What the backend provides]
- Frontend: [What the frontend displays/enables users to do]

**User Value**:
[Describe the value this feature brings to end users]

---

## Requirements

### Functional Requirements

**FR1**: [End-to-end requirement spanning both backend and frontend]
**FR2**: [End-to-end requirement]
**FR3**: [End-to-end requirement]

### Non-Functional Requirements

**NFR1 - Performance**: [e.g., Page load time < 2s, API response < 200ms]
**NFR2 - Security**: [e.g., Permission checks on both frontend and backend]
**NFR3 - Usability**: [e.g., Mobile responsive, accessible]
**NFR4 - Multi-Tenancy**: [e.g., Complete tenant isolation]

### Out of Scope

- [Features explicitly not included in this iteration]
- [Future enhancements planned for later]

---

## Architecture Overview

### System Flow

```
┌─────────────┐         ┌─────────────┐         ┌─────────────┐
│   Browser   │────────▶│   Backend   │────────▶│  Database   │
│  (Vue 3 UI) │         │ (Spring     │         │  (MySQL/    │
│             │◀────────│  Boot API)  │◀────────│   TDengine) │
└─────────────┘         └─────────────┘         └─────────────┘
      │                        │
      │                        │
      ▼                        ▼
  User Actions         API Endpoints
  - View list          POST /create
  - Create entity      PUT /update
  - Edit entity        DELETE /delete
  - Delete entity      GET /get
  - Search/Filter      GET /page
```

### Data Flow

1. **User Initiates Action** → Frontend validates input
2. **API Call** → Request sent to backend
3. **Backend Processing** → Validates, applies business rules, updates database
4. **Response** → Returns success/error to frontend
5. **UI Update** → Frontend displays result to user

---

## Part 1: Backend Implementation

> For detailed backend implementation guide, see `backend-feature.md` template.
> This section highlights key backend components for this specific feature.

### Database Schema

#### Primary Tables

**Table**: `iot_module_entity`

| Column | Type | Description | Notes |
|--------|------|-------------|-------|
| id | bigint(20) | Primary key | Auto-increment |
| name | varchar(255) | Entity name | Unique per tenant |
| description | varchar(500) | Description | Optional |
| status | tinyint(4) | Status | 1=enabled, 0=disabled |
| tenant_id | bigint(20) | Tenant ID | Auto-filtered |
| ... | ... | Standard fields | creator, create_time, etc. |

**Additional tables** (if needed):
- [List any related tables]

### API Endpoints Summary

| Method | Endpoint | Permission | Purpose |
|--------|----------|------------|---------|
| POST | `/iot/module/entity/create` | `iot:module:create` | Create new entity |
| PUT | `/iot/module/entity/update` | `iot:module:update` | Update existing entity |
| DELETE | `/iot/module/entity/delete` | `iot:module:delete` | Delete entity |
| GET | `/iot/module/entity/get` | `iot:module:query` | Get single entity |
| GET | `/iot/module/entity/page` | `iot:module:query` | Get paginated list |
| GET | `/iot/module/entity/simple-list` | `iot:module:query` | Get dropdown list |

### Key Business Rules

1. **Uniqueness**: [Describe uniqueness constraints]
2. **Validation**: [Describe validation rules]
3. **Dependencies**: [Describe any entity dependencies]
4. **Cascade Operations**: [Describe what happens on delete, etc.]

### Files to Create (Backend)

- [ ] SQL migration script
- [ ] `IotModuleEntityDO.java`
- [ ] `IotModuleEntityMapper.java`
- [ ] `IotModuleEntityService.java` + `IotModuleEntityServiceImpl.java`
- [ ] `IotModuleEntityController.java`
- [ ] VOs: `CreateReqVO`, `UpdateReqVO`, `PageReqVO`, `RespVO`
- [ ] `IotModuleEntityConvert.java`
- [ ] Unit tests

---

## Part 2: Frontend Implementation

> For detailed frontend implementation guide, see `frontend-feature.md` template.
> This section highlights key frontend components for this specific feature.

### Page Structure

**Main Page**: `src/views/iot/module/entity/index.vue`
- List view with search filters
- Create/Edit/Delete actions
- Pagination

**Form Component**: `src/views/iot/module/entity/EntityForm.vue`
- Dialog for create/edit operations
- Form validation
- Submit handling

### Route Configuration

```typescript
{
  path: '/iot/module/entity',
  component: () => import('@/views/iot/module/entity/index.vue'),
  name: 'IotModuleEntity',
  meta: {
    title: 'Entity Management',
    icon: 'ep:list',
    permission: 'iot:module:query'
  }
}
```

### API Service

**File**: `src/api/iot/module/entity.ts`

```typescript
export interface EntityVO {
  id?: number
  name: string
  description?: string
  status: number
  createTime?: string
}

export const getEntityPage = (params: EntityPageReqVO) => { ... }
export const createEntity = (data: EntityVO) => { ... }
export const updateEntity = (data: EntityVO) => { ... }
export const deleteEntity = (id: number) => { ... }
```

### Key UI Components

- Search form with filters
- Data table with sortable columns
- Create/Edit dialog with validation
- Delete confirmation dialog
- Status badges
- Action buttons with permissions

### Files to Create (Frontend)

- [ ] `src/api/iot/module/entity.ts`
- [ ] `src/views/iot/module/entity/index.vue`
- [ ] `src/views/iot/module/entity/EntityForm.vue`
- [ ] Route configuration in `src/router/index.ts`

---

## Part 3: Integration & Testing

### Integration Points

#### 1. API Contract

**Frontend → Backend Request Format**:
```json
{
  "name": "Example Entity",
  "description": "Description text",
  "status": 1
}
```

**Backend → Frontend Response Format**:
```json
{
  "code": 0,
  "msg": "",
  "data": {
    "id": 1024,
    "name": "Example Entity",
    "description": "Description text",
    "status": 1,
    "createTime": "2024-01-15 10:30:00"
  }
}
```

#### 2. Data Synchronization

- Frontend status values (0, 1) match backend enum values
- Date formats consistent (backend: `LocalDateTime`, frontend: ISO string)
- Validation rules identical on both sides

#### 3. Error Handling

**Backend Error Codes**:
- `1_004_XXX_000`: Entity not exists
- `1_004_XXX_001`: Entity name already exists

**Frontend Error Display**:
- Show backend error message if available
- Fallback to generic error messages
- Use `ElMessage.error()` for user notification

### End-to-End Testing Scenarios

#### Scenario 1: Create Entity

1. **User Action**: Click "Create" button on entity list page
2. **Frontend**: Open create dialog, show empty form
3. **User Action**: Fill in form fields, click "Confirm"
4. **Frontend**: Validate form, send POST request to `/iot/module/entity/create`
5. **Backend**: Validate request, check name uniqueness, insert to database
6. **Backend**: Return new entity ID
7. **Frontend**: Close dialog, show success message, refresh list
8. **Result**: New entity appears in the list

#### Scenario 2: Edit Entity

1. **User Action**: Click "Edit" on a list row
2. **Frontend**: Send GET request to `/iot/module/entity/get?id=xxx`
3. **Backend**: Fetch entity data, return to frontend
4. **Frontend**: Populate form dialog with existing data
5. **User Action**: Modify fields, click "Confirm"
6. **Frontend**: Validate form, send PUT request to `/iot/module/entity/update`
7. **Backend**: Validate, check uniqueness (excluding current), update database
8. **Frontend**: Close dialog, show success message, refresh list
9. **Result**: Updated entity displays new values

#### Scenario 3: Delete Entity

1. **User Action**: Click "Delete" on a list row
2. **Frontend**: Show confirmation dialog
3. **User Action**: Confirm deletion
4. **Frontend**: Send DELETE request to `/iot/module/entity/delete?id=xxx`
5. **Backend**: Validate entity exists, perform soft delete (deleted = 1)
6. **Frontend**: Show success message, refresh list
7. **Result**: Entity removed from list view

#### Scenario 4: Search & Filter

1. **User Action**: Enter search criteria, click "Search"
2. **Frontend**: Update query params, send GET request to `/iot/module/entity/page`
3. **Backend**: Build dynamic query with filters, execute pagination
4. **Backend**: Return filtered results with total count
5. **Frontend**: Display filtered data in table, update pagination
6. **Result**: Only matching entities shown

### Permission Integration

**Backend Checks** (`@PreAuthorize`):
- Prevents unauthorized API access
- Returns 403 Forbidden if permission missing

**Frontend Checks** (`v-hasPermi`):
- Hides buttons user doesn't have permission for
- Improves UX (don't show unavailable actions)
- Does NOT replace backend checks (still need backend security)

**Verification**:
- Test with different user roles
- Ensure buttons hide/show correctly
- Ensure API calls are still protected

---

## Development Workflow

### Phase 1: Backend Development

1. Create database migration script
2. Implement Data Objects (DO)
3. Implement Mapper layer
4. Implement Service layer with business logic
5. Implement Controller layer
6. Write unit tests
7. Test via Swagger UI

**Backend Complete When**:
- All endpoints working in Swagger UI
- Unit tests passing
- Multi-tenant isolation verified

### Phase 2: Frontend Development

1. Create API service layer
2. Add route configuration
3. Implement main list page
4. Implement form dialog component
5. Test all CRUD operations
6. Verify permission directives
7. Test error scenarios

**Frontend Complete When**:
- All user actions work correctly
- Form validation matches backend
- Error messages display properly
- Permission controls working

### Phase 3: Integration Testing

1. Test complete create flow
2. Test complete edit flow
3. Test complete delete flow
4. Test search and pagination
5. Test permission scenarios
6. Test multi-tenant isolation
7. Test error handling

**Integration Complete When**:
- All end-to-end scenarios pass
- Frontend and backend data formats aligned
- Error messages are user-friendly
- Performance is acceptable

---

## Testing Checklist

### Backend Testing

- [ ] Database schema created successfully
- [ ] All CRUD endpoints work via Swagger UI
- [ ] Validation rules enforced
- [ ] Uniqueness constraints work
- [ ] Multi-tenant filtering verified
- [ ] Unit tests pass
- [ ] Error codes return correctly

### Frontend Testing

- [ ] Page renders without errors
- [ ] Search filters work
- [ ] Create dialog opens and submits
- [ ] Edit dialog loads existing data
- [ ] Delete confirmation works
- [ ] Pagination works
- [ ] Form validation shows errors
- [ ] Success/error messages display
- [ ] Permission buttons show/hide

### Integration Testing

- [ ] Create flow works end-to-end
- [ ] Edit flow works end-to-end
- [ ] Delete flow works end-to-end
- [ ] Search returns correct results
- [ ] Backend validation errors display on frontend
- [ ] Permission checks work on both layers
- [ ] Tenant isolation verified (multi-tenant test)
- [ ] Performance acceptable (< 2s page load, < 200ms API)

---

## Files Summary

### Backend Files to Create

```
yudao-module-iot/yudao-module-iot-biz/
├── src/main/java/.../iot/
│   ├── controller/admin/module/
│   │   └── IotModuleEntityController.java
│   ├── convert/module/
│   │   └── IotModuleEntityConvert.java
│   ├── dal/
│   │   ├── dataobject/module/
│   │   │   └── IotModuleEntityDO.java
│   │   └── mysql/module/
│   │       └── IotModuleEntityMapper.java
│   ├── service/module/
│   │   ├── IotModuleEntityService.java
│   │   └── IotModuleEntityServiceImpl.java
│   └── controller/admin/module/vo/
│       ├── IotModuleEntityCreateReqVO.java
│       ├── IotModuleEntityUpdateReqVO.java
│       ├── IotModuleEntityPageReqVO.java
│       └── IotModuleEntityRespVO.java
└── src/test/java/.../iot/service/module/
    └── IotModuleEntityServiceImplTest.java

sql/mysql/iot/
└── iot_module_entity.sql
```

### Frontend Files to Create

```
hr-vue3/
├── src/
│   ├── api/iot/module/
│   │   └── entity.ts
│   ├── views/iot/module/entity/
│   │   ├── index.vue
│   │   └── EntityForm.vue
│   └── router/
│       └── index.ts (modify)
```

---

## Reference Implementations

### Backend References

- Device Management: `yudao-module-iot/.../controller/admin/device/IotDeviceController.java`
- Product Management: `yudao-module-iot/.../controller/admin/product/IotProductController.java`
- Energy Building: `yudao-module-iot/.../controller/admin/energy/building/IotEnergyBuildingController.java`

### Frontend References

- Device Management: `hr-vue3/src/views/iot/device/index.vue`
- Product Management: `hr-vue3/src/views/iot/product/index.vue`
- Energy Building: `hr-vue3/src/views/iot/energy/building/index.vue`

**Study these for**:
- Code structure and organization
- Naming conventions
- Error handling patterns
- Component composition
- API service patterns

---

## Deployment & Rollout

### Database Migration

1. Review SQL script for syntax errors
2. Test on development database
3. Apply to test environment
4. Verify no data loss or conflicts
5. Apply to production (during maintenance window)

### Backend Deployment

1. Merge backend code to main branch
2. Run full test suite
3. Build Maven project
4. Deploy to test environment
5. Verify via Swagger UI
6. Deploy to production

### Frontend Deployment

1. Merge frontend code to main branch
2. Run build process (`pnpm build:prod`)
3. Deploy to test environment
4. Manual testing of all features
5. Deploy to production

### Post-Deployment Verification

- [ ] Feature accessible to users with correct permissions
- [ ] All CRUD operations work correctly
- [ ] No console errors in browser
- [ ] No backend errors in logs
- [ ] Performance is acceptable
- [ ] Multi-tenant isolation working

---

## Completion Criteria

This feature is considered complete when:

1. ✅ All backend files created and tested
2. ✅ All frontend files created and tested
3. ✅ Integration testing scenarios pass
4. ✅ Code reviewed and approved
5. ✅ Documentation updated (if needed)
6. ✅ Deployed to production
7. ✅ Specification archived to `completed/`
8. ✅ No critical bugs reported within 48 hours of deployment

---

## Notes & Lessons Learned

[Space for documenting challenges, decisions, or lessons learned during implementation]

### Technical Decisions

- [Document any architectural decisions made]
- [Document any deviations from standard patterns]

### Challenges Encountered

- [Document any unexpected issues and how they were resolved]

### Future Improvements

- [Ideas for enhancements not included in this iteration]
- [Performance optimizations to consider]

---

**Last Updated**: [Date]
**Status**: [In Progress / Completed / Deployed]
