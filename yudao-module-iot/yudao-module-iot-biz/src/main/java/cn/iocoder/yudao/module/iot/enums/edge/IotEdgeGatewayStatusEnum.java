package cn.iocoder.yudao.module.iot.enums.edge;

import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * IoT 边缘网关状态枚举
 *
 * @author AI Assistant
 */
@AllArgsConstructor
@Getter
public enum IotEdgeGatewayStatusEnum implements IntArrayValuable {

    INACTIVE(0, "未激活"),
    ONLINE(1, "在线"),
    OFFLINE(2, "离线");

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(IotEdgeGatewayStatusEnum::getStatus).toArray();

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
