package cn.iocoder.yudao.module.iot.controller.admin.energy.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - IoT 能源空间树节点 VO")
@Data
public class IotEnergySpaceTreeNodeVO {

    @Schema(description = "节点ID", example = "1")
    private Long id;

    @Schema(description = "节点名称", example = "1号办公楼")
    private String name;

    @Schema(description = "节点编码", example = "BUILDING_001")
    private String code;

    @Schema(description = "节点类型", example = "building")
    private String type;

    @Schema(description = "父节点ID", example = "0")
    private Long parentId;

    @Schema(description = "排序", example = "1")
    private Integer sort;

    @Schema(description = "状态", example = "1")
    private Integer status;

    @Schema(description = "子节点列表")
    private List<IotEnergySpaceTreeNodeVO> children;

}
