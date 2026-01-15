# HR-IoT Project Constitution

> This constitution captures the core principles and conventions of the HR-IoT project. These guidelines should inform all feature specifications and implementations created using spec-kit.

---

## Core Principles

### 1. Architecture Adherence

**Three-Layer Architecture**:
- **Controller Layer**: REST API endpoints, request validation, response formatting
- **Service Layer**: Business logic, transaction management, data validation
- **Mapper Layer**: Data access, database operations (MyBatis Plus)

**Clear Separation**:
- Backend (Spring Boot) and Frontend (Vue 3) are independent modules
- Backend provides REST APIs, frontend consumes them
- No direct database access from frontend

**Pattern Reuse**:
- Before implementing new features, review similar existing features
- Follow established patterns for consistency
- Document new patterns in CLAUDE.md if they represent architectural evolution

---

### 2. Naming & Code Conventions

#### Backend (Java)

**Entity Classes (Data Objects)**:
- Suffix: `DO` (e.g., `IotDeviceDO`, `IotEnergyBuildingDO`)
- Extend `BaseDO` for common fields (creator, create_time, updater, update_time, deleted, tenant_id)
- Use Lombok: `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`
- Annotate with `@TableName("table_name")`

**Value Objects (DTOs)**:
- Request VOs: `XxxCreateReqVO`, `XxxUpdateReqVO`, `XxxPageReqVO`
- Response VOs: `XxxRespVO`
- Use `@Schema` for OpenAPI documentation
- Use Bean Validation: `@NotNull`, `@NotEmpty`, `@Size`, etc.

**Service Layer**:
- Interface: `XxxService`
- Implementation: `XxxServiceImpl`
- Annotate implementation with `@Service`
- Use `@Transactional` for operations that modify data

**Mapper Layer**:
- Interface: `XxxMapper extends BaseMapperX<XxxDO>`
- Annotate with `@Mapper`
- Use `LambdaQueryWrapperX` for complex queries
- Custom queries via default methods or XML

**Converters**:
- Interface: `XxxConvert`
- Annotate with `@Mapper(componentModel = "spring")`
- Define conversion methods between DO, VO, and other types
- Use instance pattern: `XxxConvert.INSTANCE.convert(...)`

#### Frontend (TypeScript/Vue)

**Component Files**:
- PascalCase: `DeviceList.vue`, `ProductForm.vue`
- Use `<script setup lang="ts">` (Composition API)
- Props interface: `defineProps<Props>()`

**API Services**:
- Location: `src/api/{module}/{resource}.ts`
- Define TypeScript interfaces for all DTOs
- Export functions for API calls (get, post, put, delete)

**State Management**:
- Prefer local reactive state (`ref()`, `reactive()`) for component-specific state
- Use Pinia stores for shared cross-component state

---

### 3. Data Storage Strategy

**MySQL** (Relational Database):
- Business metadata (products, devices, groups)
- User data (users, roles, permissions)
- Configuration data
- Reference data (energy types, device categories)

**TDengine** (Time-Series Database):
- IoT sensor data and telemetry
- Energy consumption metrics
- Device property history
- Time-series analytics

**Redis** (Cache & Real-Time):
- Device online/offline status
- Latest device property values
- Session data
- Frequently accessed reference data

---

### 4. Multi-Tenancy Requirements

**Table Design**:
- All business tables MUST include `tenant_id bigint(20) NOT NULL DEFAULT '0'`
- Framework automatically filters queries by current user's tenant
- Use `@TenantIgnore` annotation ONLY for truly cross-tenant tables

**Service Layer**:
- No manual tenant filtering needed (framework handles it)
- Be aware of tenant context when caching data
- Document any cross-tenant operations clearly

**Testing**:
- Test tenant isolation for all new features
- Verify data cannot leak between tenants

---

### 5. API Development Standards

**REST Conventions**:
- `POST /api/{module}/{resource}/create` - Create resource
- `PUT /api/{module}/{resource}/update` - Update resource
- `DELETE /api/{module}/{resource}/delete` - Delete single resource (id parameter)
- `GET /api/{module}/{resource}/get` - Get single resource (id parameter)
- `GET /api/{module}/{resource}/page` - Paginated list with filtering
- `GET /api/{module}/{resource}/simple-list` - Simplified list (for dropdowns)
- `GET /api/{module}/{resource}/export-excel` - Export to Excel

**OpenAPI Annotations** (Required on ALL endpoints):
- `@Tag(name = "管理后台 - {Module Name}")` on controller class
- `@Operation(summary = "{Action description}")` on each method
- `@Parameter` for individual parameters with descriptions and examples
- `@Schema` on all VO fields with meaningful descriptions

**Response Format**:
- Always return `CommonResult<T>` from controllers
- Use helper methods: `success(data)`, `success()`
- Framework handles error responses automatically

**Permission Checks** (Required):
- Use `@PreAuthorize("@ss.hasPermission('module:resource:action')")`
- Define permissions in system menu configuration
- Check permissions before any data access

**Validation**:
- Use `@Valid` on request body parameters
- Use `@Validated` on controller class
- Define validation rules in ReqVO classes using Bean Validation

---

### 6. Security Requirements

**Input Validation**:
- Validate ALL user inputs at controller level
- Use Bean Validation annotations (`@NotNull`, `@NotEmpty`, `@Size`, `@Pattern`, etc.)
- Validate business rules in service layer

**SQL Injection Prevention**:
- Use MyBatis Plus parameterized queries (framework handles this)
- Never concatenate user input into SQL strings
- Use `LambdaQueryWrapperX` for dynamic queries

**Data Exposure**:
- Never log sensitive data (passwords, tokens, API keys)
- Exclude sensitive fields from response VOs
- Use `@JsonIgnore` for fields that should never be serialized

**Authentication & Authorization**:
- All API endpoints require authentication (except explicitly public ones)
- Use `@PreAuthorize` for permission checks
- Never trust client-side validation alone

---

### 7. Testing Requirements

**Unit Tests** (Service Layer):
- Test business logic with mocked dependencies
- Use `@SpringBootTest` and `@MockBean`
- Cover edge cases and error scenarios
- Naming: `{ClassName}ServiceImplTest`

**Integration Tests** (for complex workflows):
- Test end-to-end scenarios
- Use actual database (H2 or test MySQL)
- Verify data integrity

**Frontend Testing**:
- Component rendering tests for reusable components
- Form validation tests
- API integration tests (mocked APIs)

---

### 8. Documentation Standards

**Code Documentation**:
- Use JSDoc/JavaDoc for complex algorithms
- Comment WHY, not WHAT (code should be self-explanatory)
- Update OpenAPI annotations when API changes

**Project Documentation**:
- **CLAUDE.md**: Update for architectural changes or new patterns
- **Module Guides**: Update when adding significant module capabilities
- **Spec-Kit Specifications**: Feature-specific documentation
- **README files**: Keep up-to-date for each module

**API Documentation**:
- OpenAPI/Swagger annotations must be complete and accurate
- Test endpoints in Swagger UI before committing
- Accessible at: `http://localhost:48080/swagger-ui`

---

## Technology Stack Constraints

### Backend

**Core Framework**:
- Spring Boot 2.7.18 (JDK 8 compatibility required)
- MyBatis Plus 3.5.7 for ORM
- Spring Security for authentication/authorization

**Databases**:
- MySQL 5.7 or 8.0+ for relational data
- TDengine for time-series data
- Redis 5.0/6.0/7.0 for caching

**Build Tool**:
- Maven 3.6+
- Must work with existing yudao-framework extensions

### Frontend

**Core Framework**:
- Vue 3.5.12 with Composition API
- TypeScript 5.3.3 (strict mode)
- Element Plus 2.11.1 for UI components

**Build Tool**:
- Vite 5.1.4
- Node.js >= 16.0.0
- pnpm >= 8.6.0 (NOT npm or yarn)

**Development**:
- ESLint + Prettier for code quality
- Stylelint for CSS
- TypeScript type checking required

---

## Integration Points with Existing IoT Platform

### Core Capabilities

**Device Management**:
- Product definitions (iot_product)
- Device instances (iot_device)
- Device authentication
- Device lifecycle management

**Thing Model**:
- Property definitions (writable/read-only)
- Event definitions
- Service definitions (commands)
- Thing Specification Language (TSL) format

**Protocol Support**:
- MQTT (primary)
- Modbus (industrial devices)
- TCP (custom protocols)
- HTTP (web-based devices)
- EMQX integration

**Message Bus**:
- Abstraction layer for message routing
- Supports: Local, Redis, RocketMQ
- Configured in application.yaml

### Energy Management Module (In Development)

**Space Hierarchy**:
- Buildings (top level)
- Areas (within buildings)
- Floors (within areas)
- Rooms (within floors)

**Energy Monitoring**:
- Energy types (electricity, water, gas, steam, etc.)
- Energy meters associated with spaces
- Real-time data collection
- Statistical analysis and reporting

**Integration**:
- Energy meters are IoT devices
- Use thing model for property definitions
- Store time-series data in TDengine

---

## Quality Gates

Before considering a feature complete, verify:

1. ✅ **Functionality**: All CRUD operations work as specified
2. ✅ **Validation**: Frontend validation matches backend validation
3. ✅ **Permissions**: `@PreAuthorize` annotations in place and tested
4. ✅ **Multi-Tenancy**: Data isolation verified (cannot access other tenant's data)
5. ✅ **Documentation**: OpenAPI annotations complete and accurate
6. ✅ **Error Handling**: User-friendly error messages, proper exception handling
7. ✅ **Testing**: Unit tests pass, integration tests pass (if applicable)
8. ✅ **Performance**: Acceptable response times for expected load
9. ✅ **Code Review**: Follows established patterns, no code smells
10. ✅ **Specification Alignment**: Implementation matches the specification or changes are documented

---

## Development Best Practices

### Before Starting Implementation

1. **Review Similar Features**: Find and study existing code for similar functionality
2. **Understand the Data Flow**: Trace through Controller → Service → Mapper → Database
3. **Check Module Guide**: Read relevant module documentation (e.g., IOT_MODULE_GUIDE.md)
4. **Plan Database Changes**: Design schema changes carefully (hard to change later)

### During Implementation

1. **Follow Existing Patterns**: Don't reinvent the wheel
2. **Test As You Go**: Don't wait until the end to test
3. **Update Specification**: Document any deviations from the original spec
4. **Commit Frequently**: Small, logical commits with meaningful messages

### After Implementation

1. **Self Code Review**: Review your own code before requesting peer review
2. **Test All Scenarios**: Happy path, error cases, edge cases
3. **Update Documentation**: Keep CLAUDE.md and module guides current
4. **Archive Specification**: Move completed spec to `specs/features/completed/`

---

## Continuous Improvement

This constitution is a living document:

- Update when new architectural patterns emerge
- Add lessons learned from completed features
- Remove obsolete constraints
- Keep aligned with CLAUDE.md and actual practice

**Last Updated**: 2026-01-03
**Version**: 1.0
