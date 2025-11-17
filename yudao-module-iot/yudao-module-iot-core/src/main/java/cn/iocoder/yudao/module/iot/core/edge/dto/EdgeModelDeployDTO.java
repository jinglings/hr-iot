package cn.iocoder.yudao.module.iot.core.edge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 边缘AI模型下发消息 DTO
 *
 * @author AI Assistant
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EdgeModelDeployDTO implements Serializable {

    /**
     * 操作类型: deploy=部署, undeploy=取消部署, start=启动, stop=停止
     */
    private String action;

    /**
     * 模型ID
     */
    private Long modelId;

    /**
     * 模型名称
     */
    private String modelName;

    /**
     * 模型版本
     */
    private String modelVersion;

    /**
     * 网关标识
     */
    private String gatewayKey;

    /**
     * 模型类型: 1=图像分类, 2=目标检测, 3=异常检测
     */
    private String modelType;

    /**
     * 模型格式: ONNX, TensorFlow Lite, PyTorch Mobile
     */
    private String modelFormat;

    /**
     * 模型文件URL
     */
    private String modelUrl;

    /**
     * 配置JSON
     */
    private String config;

}
