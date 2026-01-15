import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { getAccessToken, getRefreshToken, getTenantId } from '@/utils/auth'
import { useUserStore } from '@/store/modules/user'

/**
 * FUXA 认证 Composable
 * 
 * 用于管理 FUXA 嵌入的认证逻辑，包括：
 * - JWT Token 获取和刷新
 * - FUXA URL 生成（带认证参数）
 * - 租户 ID 注入
 * - 用户信息传递
 * 
 * Part of SCADA-022: Implement Authentication Composable
 *
 * @author HR-IoT Team
 */
export function useFuxaAuth(options: UseFuxaAuthOptions = {}) {
    const {
        baseUrl = '/fuxa',
        autoRefresh = true,
        refreshInterval = 5 * 60 * 1000, // 5 分钟
        embedMode = true
    } = options

    // User store
    const userStore = useUserStore()

    // State
    const isAuthenticated = ref(false)
    const tokenExpired = ref(false)
    const lastRefreshTime = ref<Date | null>(null)

    // Token 刷新定时器
    let refreshTimer: ReturnType<typeof setInterval> | null = null

    // 计算属性

    /** 当前 Access Token */
    const accessToken = computed(() => getAccessToken())

    /** 当前 Refresh Token */
    const refreshToken = computed(() => getRefreshToken())

    /** 当前租户 ID */
    const tenantId = computed(() => getTenantId())

    /** 当前用户 ID */
    const userId = computed(() => userStore.getUser?.id)

    /** 当前用户名 */
    const userName = computed(() => userStore.getUser?.nickname || userStore.getUser?.username)

    /** 是否有有效 Token */
    const hasValidToken = computed(() => !!accessToken.value)

    // 方法

    /**
     * 生成 FUXA 基础 URL
     */
    const generateFuxaBaseUrl = (): string => {
        return baseUrl
    }

    /**
     * 生成带认证的 FUXA URL
     * 
     * @param dashboardId 仪表板 ID（可选）
     * @param viewPath 视图路径（可选）
     * @returns 带认证参数的完整 URL
     */
    const generateFuxaUrl = (dashboardId?: string, viewPath?: string): string => {
        const token = accessToken.value
        const tenant = tenantId.value

        // 构建路径
        let path = baseUrl
        if (viewPath) {
            path = `${baseUrl}/${viewPath}`
        } else if (dashboardId) {
            path = `${baseUrl}/view/${dashboardId}`
        }

        // 构建参数
        const params = new URLSearchParams()

        if (token) {
            params.set('token', token)
        }

        if (tenant) {
            params.set('tenantId', String(tenant))
        }

        if (embedMode) {
            params.set('embed', 'true')
        }

        // 添加时间戳防止缓存
        params.set('t', Date.now().toString())

        const queryString = params.toString()
        return queryString ? `${path}?${queryString}` : path
    }

    /**
     * 生成 FUXA 编辑器 URL
     */
    const generateEditorUrl = (): string => {
        return generateFuxaUrl(undefined, 'editor')
    }

    /**
     * 生成 FUXA 实验室 URL
     */
    const generateLabUrl = (): string => {
        return generateFuxaUrl(undefined, 'lab')
    }

    /**
     * 获取认证头
     */
    const getAuthHeaders = (): Record<string, string> => {
        const headers: Record<string, string> = {}

        const token = accessToken.value
        if (token) {
            headers['Authorization'] = `Bearer ${token}`
        }

        const tenant = tenantId.value
        if (tenant) {
            headers['tenant-id'] = String(tenant)
        }

        return headers
    }

    /**
     * 获取 postMessage 认证数据
     */
    const getPostMessageAuthData = (): FuxaAuthMessage => {
        return {
            type: 'hr-iot:auth',
            payload: {
                token: accessToken.value || '',
                tenantId: tenantId.value || null,
                userId: userId.value || null,
                userName: userName.value || null,
                timestamp: Date.now()
            }
        }
    }

    /**
     * 发送认证消息到 FUXA iframe
     */
    const sendAuthToFuxa = (iframe: HTMLIFrameElement | null): void => {
        if (!iframe?.contentWindow) {
            console.warn('[useFuxaAuth] iframe 不存在或无法访问')
            return
        }

        const authData = getPostMessageAuthData()
        iframe.contentWindow.postMessage(authData, '*')
    }

    /**
     * 刷新认证状态
     */
    const refreshAuth = async (): Promise<boolean> => {
        try {
            // 检查 token 是否存在
            if (!accessToken.value) {
                isAuthenticated.value = false
                tokenExpired.value = true
                return false
            }

            isAuthenticated.value = true
            tokenExpired.value = false
            lastRefreshTime.value = new Date()

            return true
        } catch (error) {
            console.error('[useFuxaAuth] 刷新认证失败:', error)
            isAuthenticated.value = false
            return false
        }
    }

    /**
     * 清除认证
     */
    const clearAuth = (): void => {
        isAuthenticated.value = false
        tokenExpired.value = true
        lastRefreshTime.value = null

        if (refreshTimer) {
            clearInterval(refreshTimer)
            refreshTimer = null
        }
    }

    /**
     * 启动自动刷新
     */
    const startAutoRefresh = (): void => {
        if (!autoRefresh) return

        stopAutoRefresh()

        refreshTimer = setInterval(() => {
            refreshAuth()
        }, refreshInterval)
    }

    /**
     * 停止自动刷新
     */
    const stopAutoRefresh = (): void => {
        if (refreshTimer) {
            clearInterval(refreshTimer)
            refreshTimer = null
        }
    }

    // 监听 token 变化
    watch(accessToken, (newToken) => {
        if (newToken) {
            isAuthenticated.value = true
            tokenExpired.value = false
        } else {
            isAuthenticated.value = false
            tokenExpired.value = true
        }
    }, { immediate: true })

    // 生命周期
    onMounted(() => {
        refreshAuth()
        startAutoRefresh()
    })

    onUnmounted(() => {
        stopAutoRefresh()
    })

    return {
        // State
        isAuthenticated,
        tokenExpired,
        lastRefreshTime,

        // Computed
        accessToken,
        refreshToken,
        tenantId,
        userId,
        userName,
        hasValidToken,

        // Methods
        generateFuxaBaseUrl,
        generateFuxaUrl,
        generateEditorUrl,
        generateLabUrl,
        getAuthHeaders,
        getPostMessageAuthData,
        sendAuthToFuxa,
        refreshAuth,
        clearAuth,
        startAutoRefresh,
        stopAutoRefresh
    }
}

// ==========================================
// 类型定义
// ==========================================

/** useFuxaAuth 选项 */
export interface UseFuxaAuthOptions {
    /** FUXA 基础 URL */
    baseUrl?: string
    /** 是否自动刷新 */
    autoRefresh?: boolean
    /** 刷新间隔 (毫秒) */
    refreshInterval?: number
    /** 是否嵌入模式 */
    embedMode?: boolean
}

/** FUXA 认证消息 */
export interface FuxaAuthMessage {
    type: 'hr-iot:auth'
    payload: {
        token: string
        tenantId: number | null
        userId: number | null
        userName: string | null
        timestamp: number
    }
}

/** FUXA 认证返回类型 */
export type UseFuxaAuthReturn = ReturnType<typeof useFuxaAuth>
