package cn.iocoder.yudao.module.iot.controller.admin.edge.vo.aimodel;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - IoT 边缘AI模型更新 Request VO")
@Data
public class IotEdgeAiModelUpdateReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "主键不能为空")
    private Long id;

    @Schema(description = "模型名称", example = "质量检测模型")
    private String name;

    @Schema(description = "模型文件URL")
    private String fileUrl;

    @Schema(description = "文件大小(字节)", example = "10485760")
    private Long fileSize;

    @Schema(description = "文件MD5")
    private String fileMd5;

    @Schema(description = "应用场景", example = "产品质量检测")
    private String applicationScene;

    @Schema(description = "输入格式", example = "图像(224x224)")
    private String inputFormat;

    @Schema(description = "输出格式", example = "分类结果+置信度")
    private String outputFormat;

    @Schema(description = "描述")
    private String description;

}
