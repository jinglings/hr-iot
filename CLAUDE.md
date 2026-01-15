# CLAUDE.md - AI Assistant Guide for HR-IoT Project

## Project Overview

**HR-IoT** is an enterprise-grade IoT (Internet of Things) platform built on the ruoyi-vue-pro framework. It provides comprehensive device management, data collection, rule engine, and data visualization capabilities for industrial IoT scenarios.

### Tech Stack

**Backend:**
- Spring Boot 2.7.18 (JDK 8)
- MyBatis Plus 3.5.7 (ORM)
- MySQL 5.7/8.0+ (Relational database)
- TDengine (Time-series database for IoT data)
- Redis 5.0/6.0/7.0 (Cache and real-time data)
- Spring Security (Authentication/Authorization)

**Frontend:**
- Vue 3.5.12
- Vite 5.1.4
- Element Plus 2.11.1
- TypeScript 5.3.3
- Pinia 2.1.7 (State management)
- Axios 1.9.0 (HTTP client)

**Build Tools:**
- Maven (Backend)
- pnpm >=8.6.0 (Frontend, Node.js >= 16.0.0 required)

---

## Repository Structure

```
hr-iot/
├── yudao-dependencies/          # Maven dependency version management
├── yudao-framework/             # Core framework extensions and utilities
├── yudao-server/                # Main Spring Boot application entry point
│   └── src/main/resources/
│       ├── application.yaml     # Main configuration file
│       └── application-*.yaml   # Environment-specific configs
├── yudao-module-system/         # System management module (users, roles, menus, etc.)
├── yudao-module-infra/          # Infrastructure module (code gen, file upload, etc.)
├── yudao-module-iot/            # IoT module (ACTIVE - main business module)
│   ├── yudao-module-iot-core/   # Core utilities, enums, message bus
│   ├── yudao-module-iot-biz/    # Business logic layer
│   ├── yudao-module-iot-gateway/# Device gateway (protocol handlers)
│   ├── IOT_MODULE_GUIDE.md      # Detailed IoT module documentation
│   └── 能源管理模块需求计划.md     # Energy management requirements (Chinese)
├── hr-vue3/                     # Frontend Vue 3 application
│   ├── src/
│   │   ├── api/                 # API service definitions
│   │   ├── views/               # Page components
│   │   ├── components/          # Reusable components
│   │   ├── router/              # Vue Router configuration
│   │   ├── store/               # Pinia store modules
│   │   └── utils/               # Utility functions
│   ├── package.json
│   └── vite.config.ts
├── docs/                        # Additional documentation
├── sql/                         # Database initialization scripts
├── script/                      # Utility scripts
└── pom.xml                      # Root Maven configuration

Note: The following modules are DISABLED (commented out in pom.xml):
- yudao-module-member (Member management)
- yudao-module-bpm (Business process management)
- yudao-module-report (Reporting)
- yudao-module-mp (WeChat official account)
- yudao-module-pay (Payment system)
- yudao-module-mall (E-commerce)
- yudao-module-crm (CRM system)
- yudao-module-erp (ERP system)
- yudao-module-ai (AI features)
```

---

## Development Workflows

### Backend Development

#### 1. Project Setup

```bash
# Navigate to project root
cd /home/user/hr-iot

# Build the project (skip tests for faster build)
mvn clean install -DskipTests

# Run the application
cd yudao-server
mvn spring-boot:run

# Or run with specific profile
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

#### 2. Module Structure (Three-Layer Architecture)

Each business module follows this structure:

```
yudao-module-xxx/
└── yudao-module-xxx-biz/
    ├── controller/              # REST API endpoints
    │   └── admin/               # Admin APIs
    │       └── XxxController.java
    ├── service/                 # Business logic
    │   ├── XxxService.java      # Service interface
    │   └── XxxServiceImpl.java  # Service implementation
    ├── dal/                     # Data Access Layer
    │   ├── dataobject/          # Entity classes (DO)
    │   │   └── XxxDO.java
    │   ├── mysql/               # MyBatis mappers
    │   │   └── XxxMapper.java
    │   └── redis/               # Redis operations
    │       └── XxxRedisDAO.java
    ├── convert/                 # MapStruct converters
    │   └── XxxConvert.java
    └── vo/                      # Value Objects (DTOs)
        ├── XxxRespVO.java       # Response VO
        ├── XxxCreateReqVO.java  # Create request VO
        └── XxxUpdateReqVO.java  # Update request VO
```

#### 3. Coding Conventions

**Controller Layer:**
- Use `@RestController` and `@RequestMapping` annotations
- Inject services via constructor injection (preferred) or `@Autowired`
- Return `CommonResult<T>` for all API responses
- Use `@PreAuthorize` for permission checks
- Example:

```java
@RestController
@RequestMapping("/iot/device")
@Tag(name = "管理后台 - IoT 设备")
public class IotDeviceController {

    @Autowired
    private IotDeviceService deviceService;

    @PostMapping("/create")
    @Operation(summary = "创建设备")
    @PreAuthorize("@ss.hasPermission('iot:device:create')")
    public CommonResult<Long> createDevice(@Valid @RequestBody IotDeviceCreateReqVO createReqVO) {
        return success(deviceService.createDevice(createReqVO));
    }
}
```

**Service Layer:**
- Interface + Implementation pattern
- Use `@Service` annotation on implementation
- Business logic and validation goes here
- Transaction management with `@Transactional`

**Mapper Layer:**
- Extend `BaseMapperX<DO>` from framework
- Use MyBatis Plus methods for CRUD
- Custom queries in XML or via annotations
- Example:

```java
@Mapper
public interface IotDeviceMapper extends BaseMapperX<IotDeviceDO> {

    default PageResult<IotDeviceDO> selectPage(IotDevicePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<IotDeviceDO>()
                .likeIfPresent(IotDeviceDO::getName, reqVO.getName())
                .eqIfPresent(IotDeviceDO::getProductId, reqVO.getProductId())
                .orderByDesc(IotDeviceDO::getId));
    }
}
```

**Data Object (DO) Conventions:**
- Use Lombok annotations: `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`
- Extend `BaseDO` for common fields (creator, updater, timestamps, deleted flag)
- Use `@TableName` to map to database table
- Use `@TableId` with appropriate ID strategy

**VO (Value Object) Conventions:**
- Separate VOs for different operations: Create, Update, Response, Query/Page
- Use validation annotations: `@NotNull`, `@NotEmpty`, `@Size`, etc.
- Use `@Schema` (OpenAPI) for documentation

**Converter Pattern:**
- Use MapStruct for object conversion
- Interface with `@Mapper(componentModel = "spring")` annotation
- Define conversion methods between DO, VO, and other types
- Example:

```java
@Mapper
public interface IotDeviceConvert {
    IotDeviceConvert INSTANCE = Mappers.getMapper(IotDeviceConvert.class);

    IotDeviceDO convert(IotDeviceCreateReqVO bean);
    IotDeviceRespVO convert(IotDeviceDO bean);
    List<IotDeviceRespVO> convertList(List<IotDeviceDO> list);
}
```

#### 4. Database Conventions

- **Table naming**: lowercase with underscores (e.g., `iot_device`, `iot_product`)
- **Column naming**: lowercase with underscores
- **Primary key**: `id` (BIGINT, auto-increment)
- **Common fields** (from BaseDO):
  - `creator` VARCHAR(64) - Created by user ID
  - `create_time` DATETIME - Creation timestamp
  - `updater` VARCHAR(64) - Updated by user ID
  - `update_time` DATETIME - Update timestamp
  - `deleted` BIT - Soft delete flag (0=not deleted, 1=deleted)
  - `tenant_id` BIGINT - Tenant ID for multi-tenancy

- **Time-series data**: Use TDengine for IoT sensor data, metrics, and telemetry
- **Real-time data**: Use Redis for device online status, latest values

#### 5. Testing Conventions

- Unit tests in `src/test/java`
- Use JUnit 5 + Mockito
- Test class naming: `{ClassName}Test` or `{ClassName}ServiceImplTest`
- Mock dependencies with `@MockBean`
- Example structure:

```java
@SpringBootTest
class IotDeviceServiceImplTest {

    @Autowired
    private IotDeviceService deviceService;

    @MockBean
    private IotDeviceMapper deviceMapper;

    @Test
    void testCreateDevice() {
        // Arrange
        IotDeviceCreateReqVO reqVO = new IotDeviceCreateReqVO();
        reqVO.setName("Test Device");

        // Act
        Long id = deviceService.createDevice(reqVO);

        // Assert
        assertNotNull(id);
    }
}
```

### Frontend Development

#### 1. Project Setup

```bash
# Navigate to frontend directory
cd /home/user/hr-iot/hr-vue3

# Install dependencies (use pnpm, not npm)
pnpm install

# Run development server (local mode)
pnpm dev

# Run with specific environment
pnpm dev-server  # dev environment

# Build for production
pnpm build:prod

# Build for other environments
pnpm build:dev
pnpm build:test
pnpm build:stage
```

#### 2. Directory Structure

```
hr-vue3/src/
├── api/                    # API service modules
│   ├── iot/                # IoT-related APIs
│   ├── system/             # System APIs
│   └── login/              # Login APIs
├── views/                  # Page components
│   ├── iot/                # IoT module pages
│   │   ├── device/         # Device management
│   │   ├── product/        # Product management
│   │   └── rule/           # Rule engine
│   ├── system/             # System pages
│   └── Login/              # Login page
├── components/             # Reusable components
├── router/                 # Vue Router configuration
│   └── index.ts            # Route definitions
├── store/                  # Pinia stores
│   └── modules/
├── utils/                  # Utility functions
│   ├── request.ts          # Axios instance
│   └── auth.ts             # Auth utilities
├── assets/                 # Static assets
├── styles/                 # Global styles
└── types/                  # TypeScript type definitions
```

#### 3. Coding Conventions

**Component Naming:**
- Use PascalCase for component files: `DeviceList.vue`, `ProductForm.vue`
- Use kebab-case in templates: `<device-list />`

**Script Setup (Composition API):**
```vue
<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getDeviceList, deleteDevice } from '@/api/iot/device'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const dataList = ref([])
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  name: ''
})

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getDeviceList(queryParams)
    dataList.value = res.data.list
  } catch (error) {
    ElMessage.error('获取数据失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchData()
})
</script>
```

**API Service Pattern:**
```typescript
// src/api/iot/device.ts
import request from '@/utils/request'

export interface DeviceVO {
  id: number
  name: string
  productId: number
  deviceKey: string
  status: number
}

export interface DevicePageReqVO {
  pageNo: number
  pageSize: number
  name?: string
  productId?: number
}

// Get device list
export const getDeviceList = (params: DevicePageReqVO) => {
  return request.get('/iot/device/page', { params })
}

// Get device detail
export const getDevice = (id: number) => {
  return request.get(`/iot/device/get?id=${id}`)
}

// Create device
export const createDevice = (data: Partial<DeviceVO>) => {
  return request.post('/iot/device/create', data)
}

// Update device
export const updateDevice = (data: DeviceVO) => {
  return request.put('/iot/device/update', data)
}

// Delete device
export const deleteDevice = (id: number) => {
  return request.delete(`/iot/device/delete?id=${id}`)
}
```

**Router Configuration:**
- Routes are defined in `src/router/index.ts`
- Use lazy loading for route components
- Add route meta for permissions and page info

**State Management (Pinia):**
- Store modules in `src/store/modules/`
- Use composition API style
- Export typed getters and actions

#### 4. Environment Configuration

Environment files in `hr-vue3/`:
- `.env` - Base configuration
- `.env.local` - Local development
- `.env.dev` - Development server
- `.env.test` - Test environment
- `.env.stage` - Staging environment
- `.env.prod` - Production environment

Key variables:
```bash
# API base URL
VITE_BASE_URL=http://localhost:48080

# Application title
VITE_APP_TITLE=HR-IoT 管理系统

# Upload URL
VITE_UPLOAD_URL=http://localhost:48080/admin-api/infra/file/upload
```

#### 5. Code Quality Tools

- **ESLint**: JavaScript/TypeScript linting
  ```bash
  pnpm lint:eslint
  ```
- **Prettier**: Code formatting
  ```bash
  pnpm lint:format
  ```
- **Stylelint**: CSS/SCSS linting
  ```bash
  pnpm lint:style
  ```

---

## IoT Module Specifics

The IoT module (`yudao-module-iot`) is the core business module of this project. See `IOT_MODULE_GUIDE.md` for detailed documentation.

### Key Features

1. **Product Management**: Define device categories with common properties
2. **Device Management**: Register, authenticate, and manage IoT devices
3. **Thing Model (物模型)**: Standardized property, event, and service definitions
4. **Protocol Support**: MQTT, TCP, HTTP, Modbus, EMQX
5. **Data Collection**: Real-time data from sensors and devices
6. **Rule Engine**: Scene-based automation and data flow rules
7. **OTA Updates**: Firmware management and over-the-air updates
8. **Alert Management**: Configurable alerts with multi-channel notifications

### Architecture

```
┌─────────────────────────────────────────────┐
│          Device Layer (Devices)              │
│  MQTT / TCP / HTTP / Modbus / EMQX           │
└─────────────┬───────────────────────────────┘
              │
┌─────────────▼───────────────────────────────┐
│     iot-gateway (Protocol Adapters)          │
│  - Connection management                     │
│  - Protocol encoding/decoding                │
│  - Message routing                           │
└─────────────┬───────────────────────────────┘
              │ Message Bus (Redis/Local/RocketMQ)
┌─────────────▼───────────────────────────────┐
│     iot-biz (Business Logic)                 │
│  - Product/Device CRUD                       │
│  - Thing model management                    │
│  - Scene rules engine                        │
│  - Data flow processing                      │
│  - OTA management                            │
│  - Alert processing                          │
└─────────────┬───────────────────────────────┘
              │
┌─────────────▼───────────────────────────────┐
│     iot-core (Core Utilities)                │
│  - Message bus abstraction                   │
│  - Common enums and constants                │
│  - Utility classes                           │
└──────────────────────────────────────────────┘

Data Storage:
┌──────────┐  ┌──────────┐  ┌──────────┐
│  MySQL   │  │ TDengine │  │  Redis   │
│(Metadata)│  │(TimeSeries)│ │(Realtime)│
└──────────┘  └──────────┘  └──────────┘
```

### Configuration

IoT-specific configuration in `application.yaml`:

```yaml
yudao:
  iot:
    message-bus:
      type: redis  # Options: local, redis, rocketmq
```

Multi-tenancy cache exclusions for IoT:
```yaml
yudao:
  tenant:
    ignore-caches:
      - iot:device
      - iot:thing_model_list
```

---

## Key Conventions for AI Assistants

### When Adding New Features

1. **Backend Module**:
   - Create DO (Data Object) first with proper table mapping
   - Create Mapper extending `BaseMapperX`
   - Create Service interface and implementation
   - Create Controller with proper REST endpoints
   - Create Convert interface for VO ↔ DO conversions
   - Create Request/Response VOs with validation
   - Write unit tests

2. **Frontend Module**:
   - Create API service in `src/api/{module}/`
   - Create page component in `src/views/{module}/`
   - Add route configuration
   - Use Element Plus components
   - Follow existing component patterns (list, form, detail)

### When Modifying Code

1. **Always maintain backward compatibility** unless explicitly breaking changes are needed
2. **Follow existing patterns** - check similar features for reference
3. **Update documentation** if adding new concepts or workflows
4. **Add tests** for new functionality
5. **Use TypeScript** in frontend - avoid `any` types when possible
6. **Maintain internationalization** (i18n) - use translation keys, not hardcoded Chinese text

### When Debugging

1. **Check logs**:
   - Backend: Console output or log files
   - Frontend: Browser console and Network tab

2. **Common issues**:
   - Permission denied: Check `@PreAuthorize` annotations
   - Database errors: Verify SQL syntax and table structure
   - Frontend API errors: Check CORS, API endpoint, request format
   - Module not working: Verify it's enabled in `pom.xml`

3. **Configuration files to check**:
   - Backend: `application.yaml`, `application-{env}.yaml`
   - Frontend: `.env`, `.env.{env}`

### Database Migration

When adding new tables or modifying existing ones:

1. Create SQL migration script in `sql/mysql/` directory
2. Include both schema changes and data migrations
3. Use `IF NOT EXISTS` for tables and columns when appropriate
4. Add rollback statements in comments
5. Update relevant DO classes to match schema

### Multi-Tenancy Awareness

This system supports multi-tenancy (SaaS mode):
- Most tables include `tenant_id` column
- Framework automatically filters by tenant
- Some tables/caches are tenant-agnostic (see `yudao.tenant.ignore-tables/caches`)
- When adding new features, consider if they should be tenant-specific

### Security Considerations

1. **Always validate input** - use Bean Validation annotations
2. **Use parameterized queries** - prevent SQL injection
3. **Check permissions** - use `@PreAuthorize` on sensitive operations
4. **Sanitize output** - prevent XSS attacks
5. **Use HTTPS** in production
6. **Keep dependencies updated** for security patches

### Performance Best Practices

1. **Use pagination** for list queries
2. **Cache frequently accessed data** in Redis
3. **Use async processing** for heavy operations
4. **Optimize database queries** - add indexes where needed
5. **Minimize API calls** - batch requests when possible
6. **Use lazy loading** for frontend components and routes

### Code Style

**Backend (Java):**
- Follow [Alibaba Java Coding Guidelines](https://github.com/alibaba/p3c)
- Use 4 spaces for indentation
- Maximum line length: 120 characters
- Use meaningful variable names (English preferred)

**Frontend (TypeScript/Vue):**
- Follow [Vue Style Guide](https://vuejs.org/style-guide/)
- Use 2 spaces for indentation
- Use ESLint and Prettier for formatting
- Prefer composition API over options API

---

## Using Spec-Kit for Feature Development

This project integrates **spec-kit**, a specification-driven development methodology, to improve the development workflow for new features. Spec-kit emphasizes clear requirements, thoughtful planning, and systematic implementation.

### What is Spec-Kit?

Spec-kit is a development approach that prioritizes specifications as executable artifacts. Instead of jumping directly into coding:

1. **Define requirements** clearly (what and why)
2. **Design architecture** thoughtfully (how)
3. **Break down tasks** systematically (steps)
4. **Implement with context** (following the plan)

**Benefits**:
- Clearer requirements reduce rework
- Thoughtful architecture prevents technical debt
- Systematic implementation improves code quality
- Documentation is a natural byproduct

### When to Use Spec-Kit

**✅ Use for**:
- New major features (device grouping, alert management)
- Complex enhancements (multi-step workflows, integrations)
- Cross-module features (backend + frontend + database)
- Architectural changes

**❌ Don't use for**:
- Simple bug fixes
- Trivial UI tweaks
- Dependency updates
- Minor refactoring

**Rule of Thumb**: If the feature requires more than 4 hours or touches more than 3 files, consider spec-kit.

### Spec-Kit Workflow

```
1. /speckit.constitution  → Review project principles
2. /speckit.specify       → Create feature specification
3. /speckit.plan          → Design architecture
4. /speckit.tasks         → Break down tasks
5. /speckit.implement     → Implement each task
6. Archive to completed/  → Move spec when done
```

### Directory Structure

All spec-kit artifacts are in the `specs/` directory:

```
specs/
├── .speckit/constitution.md     # Project principles
├── features/
│   ├── in-progress/             # Active features
│   ├── completed/               # Finished features
│   └── backlog/                 # Planned features
├── templates/                   # Specification templates
│   ├── backend-feature.md
│   ├── frontend-feature.md
│   └── fullstack-feature.md
└── README.md                    # Detailed usage guide
```

### Getting Started

1. **Review Constitution**: Read `specs/.speckit/constitution.md` to understand project conventions
2. **Choose Template**: Select backend/frontend/fullstack template based on feature scope
3. **Create Specification**: Use spec-kit commands or manually fill template
4. **Follow Workflow**: Specify → Plan → Tasks → Implement

**For detailed guidance**, see `specs/README.md`

### Integration with Existing Documentation

Spec-kit **complements** existing documentation:

- **CLAUDE.md** (this file): Project-wide architecture and conventions
- **Module Guides**: Module-specific capabilities (e.g., IOT_MODULE_GUIDE.md)
- **Spec-Kit Specs**: Feature-specific requirements and implementation plans
- **OpenAPI/Swagger**: Live API reference (auto-generated)

Each serves a specific purpose. Spec-kit focuses on feature-level design and implementation tracking.

### Example

See `specs/README.md` for a complete walkthrough of developing a "Device Grouping" feature using spec-kit methodology.

---

## Common Commands Reference

### Backend (Maven)

```bash
# Clean build
mvn clean install

# Skip tests
mvn clean install -DskipTests

# Run specific module
cd yudao-server && mvn spring-boot:run

# Run tests
mvn test

# Generate code (if code generator is used)
# Access: http://localhost:48080/admin-api/infra/codegen
```

### Frontend (pnpm)

```bash
# Install dependencies
pnpm install

# Development server
pnpm dev                 # Local environment
pnpm dev-server          # Dev environment

# Build
pnpm build:prod          # Production
pnpm build:dev           # Development
pnpm build:test          # Test
pnpm build:stage         # Staging

# Code quality
pnpm lint:eslint         # Lint JavaScript/TypeScript
pnpm lint:format         # Format code with Prettier
pnpm lint:style          # Lint CSS/SCSS

# Type checking
pnpm ts:check            # TypeScript type check
```

### Git Workflow

```bash
# Current branch
git branch  # You should be on: claude/claude-md-mi2rfsvug8u19ov8-01PXqMEj1v8gnzomQ6fDtsxg

# Development workflow
git add .
git commit -m "feat: description of changes"
git push -u origin <branch-name>

# Commit message conventions (follows Conventional Commits):
# feat: New feature
# fix: Bug fix
# docs: Documentation changes
# style: Code style changes (formatting, etc.)
# refactor: Code refactoring
# test: Adding or updating tests
# chore: Maintenance tasks
```

---

## Important Files to Know

### Backend Configuration
- `yudao-server/src/main/resources/application.yaml` - Main configuration
- `yudao-server/src/main/resources/application-local.yaml` - Local dev config
- `pom.xml` - Maven dependencies and build configuration

### Frontend Configuration
- `hr-vue3/package.json` - NPM dependencies and scripts
- `hr-vue3/vite.config.ts` - Vite build configuration
- `hr-vue3/.env.local` - Local environment variables
- `hr-vue3/src/router/index.ts` - Route definitions
- `hr-vue3/src/utils/request.ts` - Axios HTTP client configuration

### Documentation
- `README.md` - Project overview (Chinese)
- `yudao-module-iot/IOT_MODULE_GUIDE.md` - IoT module detailed guide
- `yudao-module-iot/能源管理模块需求计划.md` - Energy management requirements
- `docs/tdengine-connection-guide.md` - TDengine setup guide

### Database
- `sql/mysql/` - MySQL schema and initialization scripts
- SQL files for different modules and features

---

## Development Ports

Default ports (can be configured in application.yaml):
- **Backend API**: 48080
- **Frontend Dev Server**: 3000 (Vite default, configurable)
- **MySQL**: 3306
- **Redis**: 6379
- **TDengine**: 6041 (HTTP), 6030 (Native)

---

## Troubleshooting Guide

### Backend Issues

**Issue**: Application won't start
- Check if MySQL and Redis are running
- Verify database connection in `application-local.yaml`
- Check if port 48080 is already in use
- Look for error stack traces in console

**Issue**: Module not found or disabled
- Check if module is commented out in root `pom.xml`
- Run `mvn clean install` to rebuild

**Issue**: Permission denied errors
- Check user roles and permissions in database
- Verify `@PreAuthorize` annotations match granted permissions

### Frontend Issues

**Issue**: Build fails
- Clear node_modules and reinstall: `rm -rf node_modules && pnpm install`
- Check Node.js version (>= 16.0.0 required)
- Check pnpm version (>= 8.6.0 required)

**Issue**: API calls fail
- Verify backend is running on correct port
- Check `VITE_BASE_URL` in `.env.local`
- Check browser console for CORS errors
- Verify authentication token is valid

**Issue**: Components not rendering
- Check router configuration
- Verify imports are correct
- Check browser console for errors

---

## Additional Resources

- **Ruoyi-Vue-Pro Documentation**: https://doc.iocoder.cn/
- **Spring Boot Documentation**: https://docs.spring.io/spring-boot/docs/2.7.18/reference/html/
- **Vue 3 Documentation**: https://vuejs.org/guide/introduction.html
- **Element Plus Documentation**: https://element-plus.org/
- **MyBatis Plus Documentation**: https://baomidou.com/
- **Vite Documentation**: https://vitejs.dev/guide/

---

## Contact & Support

For questions about this codebase:
1. Check existing documentation in `docs/` directory
2. Review module-specific guides (e.g., `IOT_MODULE_GUIDE.md`)
3. Check the original ruoyi-vue-pro documentation: https://doc.iocoder.cn/

---

**Last Updated**: 2025-11-17
**Project Version**: 2025.10-jdk8-SNAPSHOT
**Framework Base**: ruoyi-vue-pro
