package cn.iocoder.yudao.module.iot.dal.dataobject.edge;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import cn.iocoder.yudao.module.iot.enums.edge.IotEdgeModelDeployStatusEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * IoT 边缘模型部署记录 DO
 *
 * @author AI Assistant
 */
@TableName(value = "iot_edge_model_deployment", autoResultMap = true)
@KeySequence("iot_edge_model_deployment_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotEdgeModelDeploymentDO extends TenantBaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;

    /**
     * 模型ID
     *
     * 关联 {@link IotEdgeAiModelDO#getId()}
     */
    private Long modelId;

    /**
     * 网关ID
     *
     * 关联 {@link IotEdgeGatewayDO#getId()}
     */
    private Long gatewayId;

    // ========== 部署信息 ==========

    /**
     * 部署状态
     *
     * 枚举 {@link IotEdgeModelDeployStatusEnum}
     */
    private Integer deployStatus;

    /**
     * 部署时间
     */
    private LocalDateTime deployTime;

    /**
     * 部署错误信息
     */
    private String deployError;

    // ========== 运行信息 ==========

    /**
     * 状态
     * 0=停止, 1=运行中
     */
    private Integer status;

    /**
     * 启动时间
     */
    private LocalDateTime startTime;

    /**
     * 停止时间
     */
    private LocalDateTime stopTime;

    // ========== 性能统计 ==========

    /**
     * 推理次数
     */
    private Long inferenceCount;

    /**
     * 平均推理时间(ms)
     */
    private Integer avgInferenceTime;

    /**
     * 最后推理时间
     */
    private LocalDateTime lastInferenceTime;

}
