package cn.iocoder.yudao.module.iot.dal.dataobject.edge;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import cn.iocoder.yudao.module.iot.enums.edge.IotEdgeAiModelFormatEnum;
import cn.iocoder.yudao.module.iot.enums.edge.IotEdgeAiModelTypeEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * IoT 边缘 AI 模型 DO
 *
 * @author AI Assistant
 */
@TableName(value = "iot_edge_ai_model", autoResultMap = true)
@KeySequence("iot_edge_ai_model_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotEdgeAiModelDO extends TenantBaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;

    /**
     * 模型名称
     */
    private String name;

    /**
     * 模型版本
     */
    private String version;

    // ========== 模型信息 ==========

    /**
     * 模型类型
     *
     * 枚举 {@link IotEdgeAiModelTypeEnum}
     */
    private String modelType;

    /**
     * 模型格式
     *
     * 枚举 {@link IotEdgeAiModelFormatEnum}
     */
    private String modelFormat;

    /**
     * 模型文件URL
     */
    private String fileUrl;

    /**
     * 文件大小(字节)
     */
    private Long fileSize;

    /**
     * 文件MD5
     */
    private String fileMd5;

    // ========== 运行要求 ==========

    /**
     * 最小内存要求(MB)
     */
    private Integer minMemory;

    /**
     * 最小CPU核心数
     */
    private Integer minCpuCores;

    /**
     * 是否需要GPU
     * 0=否, 1=是
     */
    private Integer gpuRequired;

    // ========== 应用场景 ==========

    /**
     * 应用场景
     */
    private String applicationScene;

    /**
     * 输入格式
     */
    private String inputFormat;

    /**
     * 输出格式
     */
    private String outputFormat;

    // ========== 状态信息 ==========

    /**
     * 状态
     * 0=禁用, 1=启用
     */
    private Integer status;

    /**
     * 描述
     */
    private String description;

}
