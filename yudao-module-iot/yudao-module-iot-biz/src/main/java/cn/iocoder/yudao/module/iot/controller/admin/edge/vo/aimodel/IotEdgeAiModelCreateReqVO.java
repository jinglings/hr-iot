package cn.iocoder.yudao.module.iot.controller.admin.edge.vo.aimodel;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Schema(description = "管理后台 - IoT 边缘AI模型创建 Request VO")
@Data
public class IotEdgeAiModelCreateReqVO {

    @Schema(description = "模型名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "质量检测模型")
    @NotBlank(message = "模型名称不能为空")
    private String name;

    @Schema(description = "模型版本", requiredMode = Schema.RequiredMode.REQUIRED, example = "v1.0.0")
    @NotBlank(message = "模型版本不能为空")
    private String version;

    @Schema(description = "模型类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "模型类型不能为空")
    private String modelType;

    @Schema(description = "模型格式", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "模型格式不能为空")
    private String modelFormat;

    @Schema(description = "模型文件URL", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "模型文件URL不能为空")
    private String fileUrl;

    @Schema(description = "文件大小(字节)", example = "10485760")
    private Long fileSize;

    @Schema(description = "文件MD5")
    private String fileMd5;

    @Schema(description = "最小内存要求(MB)", example = "512")
    private Integer minMemory;

    @Schema(description = "最小CPU核心数", example = "2")
    private Integer minCpuCores;

    @Schema(description = "是否需要GPU: 0=否, 1=是", example = "0")
    private Integer gpuRequired;

    @Schema(description = "应用场景", example = "产品质量检测")
    private String applicationScene;

    @Schema(description = "输入格式", example = "图像(224x224)")
    private String inputFormat;

    @Schema(description = "输出格式", example = "分类结果+置信度")
    private String outputFormat;

    @Schema(description = "描述")
    private String description;

}
