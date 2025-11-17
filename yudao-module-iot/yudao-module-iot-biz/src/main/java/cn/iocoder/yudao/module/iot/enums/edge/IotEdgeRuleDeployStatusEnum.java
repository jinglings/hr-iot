package cn.iocoder.yudao.module.iot.enums.edge;

import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * IoT 边缘规则部署状态枚举
 *
 * @author AI Assistant
 */
@AllArgsConstructor
@Getter
public enum IotEdgeRuleDeployStatusEnum implements IntArrayValuable {

    NOT_DEPLOYED(0, "未部署"),
    DEPLOYED(1, "已部署"),
    DEPLOY_FAILED(2, "部署失败");

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(IotEdgeRuleDeployStatusEnum::getStatus).toArray();

    /**
     * 状态值
     */
    private final Integer status;
    /**
     * 状态名
     */
    private final String name;

    @Override
    public int[] array() {
        return ARRAYS;
    }

}
