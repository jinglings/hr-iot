# [Feature Name] - Backend Implementation Specification

> **Status**: [In Progress / Completed]
> **Created**: [Date]
> **Last Updated**: [Date]
> **Developer**: [Name]

---

## Overview

[Brief description of what this backend feature does and why it's needed]

**Key Capabilities**:
- [Capability 1]
- [Capability 2]
- [Capability 3]

---

## Requirements

### Functional Requirements

**FR1**: [Requirement description]
**FR2**: [Requirement description]
**FR3**: [Requirement description]

### Non-Functional Requirements

**NFR1 - Performance**: [e.g., Response time < 200ms for list queries]
**NFR2 - Security**: [e.g., All endpoints require iot:module:action permission]
**NFR3 - Multi-Tenancy**: [e.g., Data must be isolated by tenant]

### Out of Scope

- [What this feature explicitly does NOT include]
- [Future enhancements that are not part of this iteration]

---

## Database Schema

### New Tables

```sql
CREATE TABLE `iot_module_entity` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(255) NOT NULL COMMENT '名称',
  `description` varchar(500) DEFAULT NULL COMMENT '描述',
  `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '状态: 1-启用, 0-禁用',

  -- Standard fields
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '租户编号',

  PRIMARY KEY (`id`),
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='[Table purpose]';
```

### Modified Tables

[List any schema changes to existing tables, if applicable]

```sql
-- ALTER TABLE statements if modifying existing tables
```

### Indexes

- `idx_tenant_id`: For tenant filtering (auto-applied by framework)
- `idx_status`: For common status-based queries
- [Additional indexes and their purposes]

---

## API Endpoints

### Base Path

`/admin-api/iot/{module}`

### Endpoints

#### 1. Create Entity

- **Method**: `POST`
- **Path**: `/create`
- **Permission**: `iot:module:create`
- **Request Body**: `IotModuleEntityCreateReqVO`
  ```json
  {
    "name": "string (required, max 255)",
    "description": "string (optional, max 500)",
    "status": "integer (required, 0 or 1)"
  }
  ```
- **Response**: `CommonResult<Long>` (returns new entity ID)
- **Business Rules**:
  - Name must be unique within tenant
  - [Additional validation rules]

#### 2. Update Entity

- **Method**: `PUT`
- **Path**: `/update`
- **Permission**: `iot:module:update`
- **Request Body**: `IotModuleEntityUpdateReqVO`
  ```json
  {
    "id": "long (required)",
    "name": "string (required, max 255)",
    "description": "string (optional, max 500)",
    "status": "integer (required, 0 or 1)"
  }
  ```
- **Response**: `CommonResult<Boolean>`
- **Business Rules**:
  - Entity must exist and belong to current tenant
  - Name must be unique within tenant (excluding current entity)

#### 3. Delete Entity

- **Method**: `DELETE`
- **Path**: `/delete`
- **Permission**: `iot:module:delete`
- **Query Parameters**: `id` (required, long)
- **Response**: `CommonResult<Boolean>`
- **Business Rules**:
  - Soft delete (sets deleted = 1)
  - [Any cascade delete rules or restrictions]

#### 4. Get Entity Detail

- **Method**: `GET`
- **Path**: `/get`
- **Permission**: `iot:module:query`
- **Query Parameters**: `id` (required, long)
- **Response**: `CommonResult<IotModuleEntityRespVO>`

#### 5. Get Paginated List

- **Method**: `GET`
- **Path**: `/page`
- **Permission**: `iot:module:query`
- **Query Parameters**: `IotModuleEntityPageReqVO`
  - `pageNo` (integer, default 1)
  - `pageSize` (integer, default 10)
  - `name` (string, optional, fuzzy search)
  - `status` (integer, optional, exact match)
  - `createTime` (datetime range, optional)
- **Response**: `CommonResult<PageResult<IotModuleEntityRespVO>>`

#### 6. Get Simple List

- **Method**: `GET`
- **Path**: `/simple-list`
- **Permission**: `iot:module:query`
- **Response**: `CommonResult<List<IotModuleEntityRespVO>>` (simplified VOs for dropdowns)
- **Purpose**: For use in select boxes, returns only id and name

---

## Data Objects & Value Objects

### Data Object (DO)

**File**: `IotModuleEntityDO.java`
**Package**: `cn.iocoder.yudao.module.iot.dal.dataobject.module`

```java
@TableName("iot_module_entity")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotModuleEntityDO extends BaseDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String description;

    private Integer status;

    // BaseDO provides: creator, createTime, updater, updateTime, deleted, tenantId
}
```

### Request VOs

#### Create Request VO

**File**: `IotModuleEntityCreateReqVO.java`
**Package**: `cn.iocoder.yudao.module.iot.controller.admin.module.vo`

```java
@Schema(description = "管理后台 - [Entity] 创建 Request VO")
@Data
public class IotModuleEntityCreateReqVO {

    @Schema(description = "名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "示例名称")
    @NotEmpty(message = "名称不能为空")
    @Size(max = 255, message = "名称长度不能超过 255 个字符")
    private String name;

    @Schema(description = "描述", example = "这是一个示例描述")
    @Size(max = 500, message = "描述长度不能超过 500 个字符")
    private String description;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "状态不能为空")
    @InEnum(CommonStatusEnum.class, message = "状态必须是 {value}")
    private Integer status;
}
```

#### Update Request VO

**File**: `IotModuleEntityUpdateReqVO.java`

```java
@Schema(description = "管理后台 - [Entity] 更新 Request VO")
@Data
public class IotModuleEntityUpdateReqVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "编号不能为空")
    private Long id;

    @Schema(description = "名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "示例名称")
    @NotEmpty(message = "名称不能为空")
    @Size(max = 255, message = "名称长度不能超过 255 个字符")
    private String name;

    @Schema(description = "描述", example = "这是一个示例描述")
    @Size(max = 500, message = "描述长度不能超过 500 个字符")
    private String description;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "状态不能为空")
    @InEnum(CommonStatusEnum.class, message = "状态必须是 {value}")
    private Integer status;
}
```

#### Page Request VO

**File**: `IotModuleEntityPageReqVO.java`

```java
@Schema(description = "管理后台 - [Entity] 分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class IotModuleEntityPageReqVO extends PageParam {

    @Schema(description = "名称", example = "示例")
    private String name;

    @Schema(description = "状态", example = "1")
    private Integer status;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;
}
```

### Response VO

**File**: `IotModuleEntityRespVO.java`

```java
@Schema(description = "管理后台 - [Entity] Response VO")
@Data
public class IotModuleEntityRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "示例名称")
    private String name;

    @Schema(description = "描述", example = "这是一个示例描述")
    private String description;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer status;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    @Schema(description = "更新时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime updateTime;
}
```

---

## Service Layer

### Interface

**File**: `IotModuleEntityService.java`
**Package**: `cn.iocoder.yudao.module.iot.service.module`

```java
public interface IotModuleEntityService {

    /**
     * 创建 [Entity]
     */
    Long createEntity(IotModuleEntityCreateReqVO createReqVO);

    /**
     * 更新 [Entity]
     */
    void updateEntity(IotModuleEntityUpdateReqVO updateReqVO);

    /**
     * 删除 [Entity]
     */
    void deleteEntity(Long id);

    /**
     * 获得 [Entity]
     */
    IotModuleEntityDO getEntity(Long id);

    /**
     * 获得 [Entity] 分页
     */
    PageResult<IotModuleEntityDO> getEntityPage(IotModuleEntityPageReqVO pageReqVO);

    /**
     * 获得 [Entity] 简单列表
     */
    List<IotModuleEntityDO> getEntitySimpleList();
}
```

### Implementation

**File**: `IotModuleEntityServiceImpl.java`
**Package**: `cn.iocoder.yudao.module.iot.service.module`

```java
@Service
@Validated
public class IotModuleEntityServiceImpl implements IotModuleEntityService {

    @Resource
    private IotModuleEntityMapper entityMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createEntity(IotModuleEntityCreateReqVO createReqVO) {
        // 1. Validate uniqueness
        validateEntityNameUnique(null, createReqVO.getName());

        // 2. Convert and insert
        IotModuleEntityDO entity = IotModuleEntityConvert.INSTANCE.convert(createReqVO);
        entityMapper.insert(entity);

        // 3. Return ID
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateEntity(IotModuleEntityUpdateReqVO updateReqVO) {
        // 1. Validate exists
        validateEntityExists(updateReqVO.getId());

        // 2. Validate uniqueness
        validateEntityNameUnique(updateReqVO.getId(), updateReqVO.getName());

        // 3. Convert and update
        IotModuleEntityDO updateObj = IotModuleEntityConvert.INSTANCE.convert(updateReqVO);
        entityMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteEntity(Long id) {
        // 1. Validate exists
        validateEntityExists(id);

        // 2. Check for dependencies (if any)
        // validateEntityCanBeDeleted(id);

        // 3. Delete
        entityMapper.deleteById(id);
    }

    @Override
    public IotModuleEntityDO getEntity(Long id) {
        return entityMapper.selectById(id);
    }

    @Override
    public PageResult<IotModuleEntityDO> getEntityPage(IotModuleEntityPageReqVO pageReqVO) {
        return entityMapper.selectPage(pageReqVO);
    }

    @Override
    public List<IotModuleEntityDO> getEntitySimpleList() {
        return entityMapper.selectList(new LambdaQueryWrapperX<IotModuleEntityDO>()
                .eq(IotModuleEntityDO::getStatus, CommonStatusEnum.ENABLE.getStatus())
                .orderByDesc(IotModuleEntityDO::getId));
    }

    // ==================== Validation Methods ====================

    private void validateEntityExists(Long id) {
        if (entityMapper.selectById(id) == null) {
            throw exception(ENTITY_NOT_EXISTS);
        }
    }

    private void validateEntityNameUnique(Long id, String name) {
        IotModuleEntityDO entity = entityMapper.selectByName(name);
        if (entity == null) {
            return;
        }
        if (id == null || !id.equals(entity.getId())) {
            throw exception(ENTITY_NAME_EXISTS);
        }
    }
}
```

### Business Rules

1. **Uniqueness**: Entity name must be unique within the same tenant
2. **Status Validation**: Status must be either 0 (disabled) or 1 (enabled)
3. **Deletion Rules**: [Specify any rules about when entities can/cannot be deleted]
4. **[Additional business rules]**

---

## Data Layer (Mapper)

**File**: `IotModuleEntityMapper.java`
**Package**: `cn.iocoder.yudao.module.iot.dal.mysql.module`

```java
@Mapper
public interface IotModuleEntityMapper extends BaseMapperX<IotModuleEntityDO> {

    default PageResult<IotModuleEntityDO> selectPage(IotModuleEntityPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<IotModuleEntityDO>()
                .likeIfPresent(IotModuleEntityDO::getName, reqVO.getName())
                .eqIfPresent(IotModuleEntityDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(IotModuleEntityDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(IotModuleEntityDO::getId));
    }

    default IotModuleEntityDO selectByName(String name) {
        return selectOne(IotModuleEntityDO::getName, name);
    }
}
```

---

## Controller Layer

**File**: `IotModuleEntityController.java`
**Package**: `cn.iocoder.yudao.module.iot.controller.admin.module`

```java
@Tag(name = "管理后台 - [Entity Management]")
@RestController
@RequestMapping("/iot/module/entity")
@Validated
public class IotModuleEntityController {

    @Resource
    private IotModuleEntityService entityService;

    @PostMapping("/create")
    @Operation(summary = "创建 [Entity]")
    @PreAuthorize("@ss.hasPermission('iot:module:create')")
    public CommonResult<Long> createEntity(@Valid @RequestBody IotModuleEntityCreateReqVO createReqVO) {
        return success(entityService.createEntity(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新 [Entity]")
    @PreAuthorize("@ss.hasPermission('iot:module:update')")
    public CommonResult<Boolean> updateEntity(@Valid @RequestBody IotModuleEntityUpdateReqVO updateReqVO) {
        entityService.updateEntity(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除 [Entity]")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:module:delete')")
    public CommonResult<Boolean> deleteEntity(@RequestParam("id") Long id) {
        entityService.deleteEntity(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得 [Entity]")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:module:query')")
    public CommonResult<IotModuleEntityRespVO> getEntity(@RequestParam("id") Long id) {
        IotModuleEntityDO entity = entityService.getEntity(id);
        return success(IotModuleEntityConvert.INSTANCE.convert(entity));
    }

    @GetMapping("/page")
    @Operation(summary = "获得 [Entity] 分页")
    @PreAuthorize("@ss.hasPermission('iot:module:query')")
    public CommonResult<PageResult<IotModuleEntityRespVO>> getEntityPage(
            @Valid IotModuleEntityPageReqVO pageReqVO) {
        PageResult<IotModuleEntityDO> pageResult = entityService.getEntityPage(pageReqVO);
        return success(IotModuleEntityConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获得 [Entity] 精简列表")
    @PreAuthorize("@ss.hasPermission('iot:module:query')")
    public CommonResult<List<IotModuleEntityRespVO>> getEntitySimpleList() {
        List<IotModuleEntityDO> list = entityService.getEntitySimpleList();
        return success(IotModuleEntityConvert.INSTANCE.convertList(list));
    }
}
```

---

## Converter

**File**: `IotModuleEntityConvert.java`
**Package**: `cn.iocoder.yudao.module.iot.convert.module`

```java
@Mapper
public interface IotModuleEntityConvert {

    IotModuleEntityConvert INSTANCE = Mappers.getMapper(IotModuleEntityConvert.class);

    IotModuleEntityDO convert(IotModuleEntityCreateReqVO bean);

    IotModuleEntityDO convert(IotModuleEntityUpdateReqVO bean);

    IotModuleEntityRespVO convert(IotModuleEntityDO bean);

    List<IotModuleEntityRespVO> convertList(List<IotModuleEntityDO> list);

    PageResult<IotModuleEntityRespVO> convertPage(PageResult<IotModuleEntityDO> page);
}
```

---

## Error Codes

Add to `ErrorCodeConstants.java` in the iot module:

```java
// ========== [Entity Module] 相关错误码 ==========
ErrorCode ENTITY_NOT_EXISTS = new ErrorCode(1_004_XXX_000, "[Entity] 不存在");
ErrorCode ENTITY_NAME_EXISTS = new ErrorCode(1_004_XXX_001, "[Entity] 名称已存在");
```

---

## Testing Strategy

### Unit Tests

**File**: `IotModuleEntityServiceImplTest.java`
**Package**: `cn.iocoder.yudao.module.iot.service.module`

**Test Cases**:
1. `testCreateEntity_success()` - Successful creation
2. `testCreateEntity_nameDuplicate()` - Fails when name exists
3. `testUpdateEntity_success()` - Successful update
4. `testUpdateEntity_notExists()` - Fails when entity doesn't exist
5. `testDeleteEntity_success()` - Successful deletion
6. `testGetEntityPage()` - Pagination works correctly

### Integration Tests

**Test Scenarios**:
- Multi-tenant isolation (ensure tenant A cannot see tenant B's data)
- Permission checks (unauthorized users cannot access)
- Complex queries with multiple filter criteria

### Manual Testing Checklist

- [ ] Create entity via Swagger UI
- [ ] Update entity via Swagger UI
- [ ] Delete entity via Swagger UI
- [ ] Query entity list with filters
- [ ] Verify multi-tenant isolation
- [ ] Test permission checks

---

## Files to Create/Modify

### New Files

**Backend**:
- [ ] `sql/mysql/iot/module/iot_module_entity.sql` - Database migration
- [ ] `IotModuleEntityDO.java` - Data object
- [ ] `IotModuleEntityMapper.java` - Mapper interface
- [ ] `IotModuleEntityService.java` - Service interface
- [ ] `IotModuleEntityServiceImpl.java` - Service implementation
- [ ] `IotModuleEntityController.java` - REST controller
- [ ] `IotModuleEntityCreateReqVO.java` - Create request VO
- [ ] `IotModuleEntityUpdateReqVO.java` - Update request VO
- [ ] `IotModuleEntityPageReqVO.java` - Page request VO
- [ ] `IotModuleEntityRespVO.java` - Response VO
- [ ] `IotModuleEntityConvert.java` - MapStruct converter
- [ ] `IotModuleEntityServiceImplTest.java` - Unit tests

### Modified Files

**Backend**:
- [ ] `ErrorCodeConstants.java` - Add error codes
- [ ] `application.yaml` - Add any new configuration (if needed)

---

## Reference Implementation

**Similar existing features to review**:
- Device Management (`IotDeviceController`, `IotDeviceService`)
- Product Management (`IotProductController`, `IotProductService`)
- Energy Building Management (`IotEnergyBuildingController`, `IotEnergyBuildingService`)

**Patterns to follow**:
- Controller structure and annotations
- Service validation approach
- Mapper query patterns
- VO naming and structure

---

## Completion Checklist

- [ ] Database schema created and tested
- [ ] All backend files created
- [ ] Unit tests written and passing
- [ ] Manual testing completed via Swagger UI
- [ ] Error handling verified
- [ ] Permission checks in place
- [ ] Multi-tenant isolation tested
- [ ] Code reviewed
- [ ] Documentation updated (if needed)
- [ ] Specification archived to completed/

---

## Notes & Lessons Learned

[Space for documenting any challenges, decisions, or lessons learned during implementation]
