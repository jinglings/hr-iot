# [Feature Name] - Frontend Implementation Specification

> **Status**: [In Progress / Completed]
> **Created**: [Date]
> **Last Updated**: [Date]
> **Developer**: [Name]

---

## Overview

[Brief description of what this frontend feature does and why it's needed]

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

### UI/UX Requirements

**UX1**: [e.g., Page must load within 2 seconds]
**UX2**: [e.g., Form must validate in real-time]
**UX3**: [e.g., Success feedback must be immediate]

### Out of Scope

- [What this feature explicitly does NOT include]
- [Future enhancements that are not part of this iteration]

---

## Router Configuration

### Route Definition

**File**: `hr-vue3/src/router/index.ts`

```typescript
{
  path: '/iot/module',
  component: Layout,
  redirect: '/iot/module/entity',
  name: 'IotModule',
  meta: {
    title: 'Module Management',
    icon: 'ep:document',
    alwaysShow: true
  },
  children: [
    {
      path: 'entity',
      component: () => import('@/views/iot/module/entity/index.vue'),
      name: 'IotModuleEntity',
      meta: {
        title: 'Entity Management',
        icon: 'ep:list'
      }
    }
  ]
}
```

### Route Permissions

**Permission Code**: `iot:module:query` (for viewing the page)

Additional permissions:
- `iot:module:create` - Create button visibility
- `iot:module:update` - Edit button visibility
- `iot:module:delete` - Delete button visibility

---

## Component Structure

### Page Structure

```
hr-vue3/src/views/iot/module/entity/
├── index.vue                    # Main page (list view)
├── EntityForm.vue               # Create/Edit dialog
└── components/                  # Optional sub-components
    └── EntityDetail.vue         # Detail drawer (if needed)
```

### Component Tree

```
index.vue (Main Page)
├─ Search Form (Element Plus Form)
├─ Action Buttons (Create, Export, etc.)
├─ Data Table (Element Plus Table)
│  ├─ Column: Name
│  ├─ Column: Description
│  ├─ Column: Status (with badge)
│  ├─ Column: Create Time
│  └─ Column: Actions (Edit, Delete buttons)
├─ Pagination (Element Plus Pagination)
└─ EntityForm Dialog (for Create/Edit)
   ├─ Form Fields
   └─ Form Actions (Submit, Cancel)
```

---

## API Service Layer

### API Service File

**File**: `hr-vue3/src/api/iot/module/entity.ts`

```typescript
import request from '@/utils/request'

// ==================== Type Definitions ====================

export interface EntityVO {
  id?: number
  name: string
  description?: string
  status: number
  createTime?: string
  updateTime?: string
}

export interface EntityPageReqVO {
  pageNo: number
  pageSize: number
  name?: string
  status?: number
  createTime?: [string, string] // Date range
}

// ==================== API Functions ====================

// Get paginated entity list
export const getEntityPage = (params: EntityPageReqVO) => {
  return request.get<PageResult<EntityVO>>('/iot/module/entity/page', { params })
}

// Get single entity by ID
export const getEntity = (id: number) => {
  return request.get<EntityVO>(`/iot/module/entity/get?id=${id}`)
}

// Create new entity
export const createEntity = (data: EntityVO) => {
  return request.post('/iot/module/entity/create', data)
}

// Update existing entity
export const updateEntity = (data: EntityVO) => {
  return request.put('/iot/module/entity/update', data)
}

// Delete entity
export const deleteEntity = (id: number) => {
  return request.delete(`/iot/module/entity/delete?id=${id}`)
}

// Get simple list (for dropdowns)
export const getEntitySimpleList = () => {
  return request.get<EntityVO[]>('/iot/module/entity/simple-list')
}

// Export to Excel
export const exportEntity = (params: EntityPageReqVO) => {
  return request.download('/iot/module/entity/export-excel', params)
}
```

---

## Main Page Implementation

### File: `index.vue`

```vue
<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import {
  getEntityPage,
  deleteEntity,
  exportEntity,
  type EntityVO,
  type EntityPageReqVO
} from '@/api/iot/module/entity'
import EntityForm from './EntityForm.vue'
import { formatDate } from '@/utils/formatTime'

// ==================== Data ====================

const loading = ref(false)
const dataList = ref<EntityVO[]>([])
const total = ref(0)

// Query parameters
const queryParams = reactive<EntityPageReqVO>({
  pageNo: 1,
  pageSize: 10,
  name: undefined,
  status: undefined,
  createTime: undefined
})

// Form dialog
const formDialogVisible = ref(false)
const formRef = ref<InstanceType<typeof EntityForm>>()

// Search form reference
const queryFormRef = ref<FormInstance>()

// ==================== Lifecycle ====================

onMounted(() => {
  fetchData()
})

// ==================== Methods ====================

/** Fetch entity list */
const fetchData = async () => {
  loading.value = true
  try {
    const res = await getEntityPage(queryParams)
    dataList.value = res.data.list
    total.value = res.data.total
  } catch (error) {
    ElMessage.error('Failed to load data')
  } finally {
    loading.value = false
  }
}

/** Search */
const handleQuery = () => {
  queryParams.pageNo = 1
  fetchData()
}

/** Reset search form */
const handleReset = () => {
  queryFormRef.value?.resetFields()
  handleQuery()
}

/** Open create dialog */
const handleCreate = () => {
  formDialogVisible.value = true
  formRef.value?.open('create')
}

/** Open edit dialog */
const handleUpdate = (row: EntityVO) => {
  formDialogVisible.value = true
  formRef.value?.open('update', row.id)
}

/** Delete entity */
const handleDelete = async (row: EntityVO) => {
  try {
    await ElMessageBox.confirm(
      `Are you sure you want to delete "${row.name}"?`,
      'Confirm Delete',
      {
        confirmButtonText: 'Delete',
        cancelButtonText: 'Cancel',
        type: 'warning'
      }
    )

    await deleteEntity(row.id!)
    ElMessage.success('Deleted successfully')
    await fetchData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('Failed to delete')
    }
  }
}

/** Export to Excel */
const handleExport = async () => {
  try {
    await ElMessageBox.confirm(
      'Do you want to export the data?',
      'Confirm Export',
      {
        confirmButtonText: 'Export',
        cancelButtonText: 'Cancel',
        type: 'warning'
      }
    )

    await exportEntity(queryParams)
    ElMessage.success('Export started')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('Failed to export')
    }
  }
}

/** Pagination change */
const handlePageChange = (page: number) => {
  queryParams.pageNo = page
  fetchData()
}

/** Page size change */
const handleSizeChange = (size: number) => {
  queryParams.pageSize = size
  queryParams.pageNo = 1
  fetchData()
}

/** Form submit success callback */
const handleFormSuccess = () => {
  formDialogVisible.value = false
  fetchData()
}

/** Get status tag type */
const getStatusTagType = (status: number) => {
  return status === 1 ? 'success' : 'info'
}

/** Get status text */
const getStatusText = (status: number) => {
  return status === 1 ? 'Enabled' : 'Disabled'
}
</script>

<template>
  <div class="app-container">
    <!-- Search Form -->
    <el-form
      ref="queryFormRef"
      :model="queryParams"
      :inline="true"
      label-width="80px"
    >
      <el-form-item label="Name" prop="name">
        <el-input
          v-model="queryParams.name"
          placeholder="Enter name"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="Status" prop="status">
        <el-select v-model="queryParams.status" placeholder="Select status" clearable>
          <el-option label="Enabled" :value="1" />
          <el-option label="Disabled" :value="0" />
        </el-select>
      </el-form-item>
      <el-form-item label="Create Time" prop="createTime">
        <el-date-picker
          v-model="queryParams.createTime"
          type="daterange"
          range-separator="-"
          start-placeholder="Start date"
          end-placeholder="End date"
          value-format="YYYY-MM-DD HH:mm:ss"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleQuery">Search</el-button>
        <el-button @click="handleReset">Reset</el-button>
      </el-form-item>
    </el-form>

    <!-- Action Buttons -->
    <el-row :gutter="10" class="mb-8px">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          @click="handleCreate"
          v-hasPermi="['iot:module:create']"
        >
          <Icon icon="ep:plus" class="mr-5px" />
          Create
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          @click="handleExport"
          v-hasPermi="['iot:module:export']"
        >
          <Icon icon="ep:download" class="mr-5px" />
          Export
        </el-button>
      </el-col>
    </el-row>

    <!-- Data Table -->
    <el-table v-loading="loading" :data="dataList" border>
      <el-table-column label="ID" align="center" prop="id" width="80" />
      <el-table-column label="Name" align="center" prop="name" show-overflow-tooltip />
      <el-table-column
        label="Description"
        align="center"
        prop="description"
        show-overflow-tooltip
      />
      <el-table-column label="Status" align="center" prop="status" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusTagType(row.status)">
            {{ getStatusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column
        label="Create Time"
        align="center"
        prop="createTime"
        width="180"
        :formatter="(row) => formatDate(row.createTime)"
      />
      <el-table-column label="Actions" align="center" width="180" fixed="right">
        <template #default="{ row }">
          <el-button
            link
            type="primary"
            @click="handleUpdate(row)"
            v-hasPermi="['iot:module:update']"
          >
            Edit
          </el-button>
          <el-button
            link
            type="danger"
            @click="handleDelete(row)"
            v-hasPermi="['iot:module:delete']"
          >
            Delete
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- Pagination -->
    <Pagination
      v-model:page="queryParams.pageNo"
      v-model:limit="queryParams.pageSize"
      :total="total"
      @pagination="fetchData"
    />

    <!-- Form Dialog -->
    <EntityForm
      v-if="formDialogVisible"
      ref="formRef"
      @success="handleFormSuccess"
      @close="formDialogVisible = false"
    />
  </div>
</template>

<style scoped lang="scss">
// Custom styles if needed
</style>
```

---

## Form Component Implementation

### File: `EntityForm.vue`

```vue
<script setup lang="ts">
import { ref, reactive } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import {
  getEntity,
  createEntity,
  updateEntity,
  type EntityVO
} from '@/api/iot/module/entity'

// ==================== Props & Emits ====================

const emit = defineEmits(['success', 'close'])

// ==================== Data ====================

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const formType = ref<'create' | 'update'>('create')

const formRef = ref<FormInstance>()
const formData = reactive<EntityVO>({
  id: undefined,
  name: '',
  description: '',
  status: 1
})

// Form validation rules
const formRules = reactive<FormRules<EntityVO>>({
  name: [
    { required: true, message: 'Please enter name', trigger: 'blur' },
    { max: 255, message: 'Name cannot exceed 255 characters', trigger: 'blur' }
  ],
  description: [
    { max: 500, message: 'Description cannot exceed 500 characters', trigger: 'blur' }
  ],
  status: [{ required: true, message: 'Please select status', trigger: 'change' }]
})

// ==================== Methods ====================

/** Open dialog */
const open = async (type: 'create' | 'update', id?: number) => {
  dialogVisible.value = true
  formType.value = type
  resetForm()

  if (type === 'update' && id) {
    dialogTitle.value = 'Edit Entity'
    await loadEntityData(id)
  } else {
    dialogTitle.value = 'Create Entity'
  }
}

/** Load entity data for editing */
const loadEntityData = async (id: number) => {
  try {
    formLoading.value = true
    const res = await getEntity(id)
    Object.assign(formData, res.data)
  } catch (error) {
    ElMessage.error('Failed to load entity data')
    dialogVisible.value = false
  } finally {
    formLoading.value = false
  }
}

/** Submit form */
const submitForm = async () => {
  const form = formRef.value
  if (!form) return

  await form.validate(async (valid) => {
    if (!valid) return

    try {
      formLoading.value = true

      if (formType.value === 'create') {
        await createEntity(formData)
        ElMessage.success('Created successfully')
      } else {
        await updateEntity(formData)
        ElMessage.success('Updated successfully')
      }

      dialogVisible.value = false
      emit('success')
    } catch (error) {
      ElMessage.error(formType.value === 'create' ? 'Failed to create' : 'Failed to update')
    } finally {
      formLoading.value = false
    }
  })
}

/** Reset form */
const resetForm = () => {
  formData.id = undefined
  formData.name = ''
  formData.description = ''
  formData.status = 1
  formRef.value?.resetFields()
}

/** Close dialog */
const handleClose = () => {
  dialogVisible.value = false
  emit('close')
}

// Expose methods for parent component
defineExpose({ open })
</script>

<template>
  <el-dialog
    v-model="dialogVisible"
    :title="dialogTitle"
    width="600px"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      v-loading="formLoading"
    >
      <el-form-item label="Name" prop="name">
        <el-input
          v-model="formData.name"
          placeholder="Please enter name"
          maxlength="255"
          show-word-limit
        />
      </el-form-item>
      <el-form-item label="Description" prop="description">
        <el-input
          v-model="formData.description"
          type="textarea"
          :rows="4"
          placeholder="Please enter description"
          maxlength="500"
          show-word-limit
        />
      </el-form-item>
      <el-form-item label="Status" prop="status">
        <el-radio-group v-model="formData.status">
          <el-radio :label="1">Enabled</el-radio>
          <el-radio :label="0">Disabled</el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">Cancel</el-button>
        <el-button type="primary" @click="submitForm" :loading="formLoading">
          Confirm
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<style scoped lang="scss">
// Custom styles if needed
</style>
```

---

## State Management (Optional)

### Pinia Store (if needed for shared state)

**File**: `hr-vue3/src/store/modules/iot/entity.ts`

```typescript
import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { EntityVO } from '@/api/iot/module/entity'
import { getEntitySimpleList } from '@/api/iot/module/entity'

export const useEntityStore = defineStore('iot-entity', () => {
  // State
  const entityList = ref<EntityVO[]>([])
  const loading = ref(false)

  // Actions
  const loadEntityList = async () => {
    loading.value = true
    try {
      const res = await getEntitySimpleList()
      entityList.value = res.data
    } finally {
      loading.value = false
    }
  }

  return {
    entityList,
    loading,
    loadEntityList
  }
})
```

**Usage**:
```typescript
import { useEntityStore } from '@/store/modules/iot/entity'

const entityStore = useEntityStore()
await entityStore.loadEntityList()
```

---

## UI Components Used

### Element Plus Components

- `el-form` - Search form and entity form
- `el-table` - Data table display
- `el-pagination` - Page navigation
- `el-dialog` - Create/Edit dialog
- `el-button` - Action buttons
- `el-tag` - Status display
- `el-input` - Text input fields
- `el-select` - Status dropdown
- `el-date-picker` - Date range picker
- `el-radio-group` - Status radio buttons
- `el-message` - Success/Error notifications
- `el-message-box` - Confirmation dialogs

### Custom Components

- `<Icon>` - Icon component (from framework)
- `<Pagination>` - Pagination component (from framework)
- `EntityForm` - Custom form dialog component

---

## Form Validation

### Real-Time Validation Rules

**Name Field**:
- Required field
- Maximum length: 255 characters
- Triggers on: blur

**Description Field**:
- Optional field
- Maximum length: 500 characters
- Triggers on: blur

**Status Field**:
- Required field
- Must be 0 or 1
- Triggers on: change

### Backend Validation Alignment

Frontend validation should match backend validation rules:
- Same maximum lengths
- Same required fields
- Same enum values

---

## Error Handling

### API Error Handling

```typescript
try {
  const res = await getEntityPage(queryParams)
  dataList.value = res.data.list
} catch (error) {
  ElMessage.error('Failed to load data')
  console.error('Error loading entities:', error)
}
```

### Form Submission Errors

```typescript
catch (error) {
  if (error.response?.data?.msg) {
    ElMessage.error(error.response.data.msg) // Backend error message
  } else {
    ElMessage.error('Failed to create')
  }
}
```

---

## Testing Strategy

### Component Tests

**Test File**: `entity/index.test.ts`

**Test Cases**:
1. Component renders correctly
2. Search functionality works
3. Create button opens dialog
4. Edit button opens dialog with data
5. Delete button shows confirmation
6. Pagination updates data

### Manual Testing Checklist

- [ ] Page loads without errors
- [ ] Search filters work correctly
- [ ] Create dialog opens and submits
- [ ] Edit dialog loads existing data
- [ ] Delete confirmation works
- [ ] Pagination works correctly
- [ ] Permission buttons show/hide correctly
- [ ] Form validation displays errors
- [ ] Success/error messages appear
- [ ] Export functionality works

---

## Files to Create/Modify

### New Files

**Frontend**:
- [ ] `src/api/iot/module/entity.ts` - API service
- [ ] `src/views/iot/module/entity/index.vue` - Main page
- [ ] `src/views/iot/module/entity/EntityForm.vue` - Form dialog
- [ ] `src/store/modules/iot/entity.ts` - Pinia store (if needed)

### Modified Files

**Frontend**:
- [ ] `src/router/index.ts` - Add route configuration

---

## Reference Implementation

**Similar existing pages to review**:
- Device Management (`src/views/iot/device/index.vue`)
- Product Management (`src/views/iot/product/index.vue`)
- Energy Building (`src/views/iot/energy/building/index.vue`)

**Patterns to follow**:
- Search form layout
- Table column structure
- Dialog component usage
- API service organization
- Error handling approach

---

## Accessibility & UX

### Keyboard Navigation

- Tab navigation through form fields
- Enter key to submit search
- Escape key to close dialogs

### Loading States

- Show loading spinner during API calls
- Disable form during submission
- Display skeleton screens for initial load (optional)

### User Feedback

- Success message after create/update/delete
- Error messages with clear descriptions
- Confirmation dialogs before destructive actions
- Form validation feedback

---

## Performance Considerations

- Debounce search input (if implemented)
- Lazy load table data (pagination)
- Cache simple lists in store
- Optimize table column rendering

---

## Completion Checklist

- [ ] All frontend files created
- [ ] Route configured and accessible
- [ ] API services working correctly
- [ ] Form validation implemented
- [ ] Permission directives applied
- [ ] Manual testing completed
- [ ] Responsive design verified
- [ ] Error handling verified
- [ ] Documentation updated (if needed)
- [ ] Specification archived to completed/

---

## Notes & Lessons Learned

[Space for documenting any challenges, decisions, or lessons learned during implementation]
