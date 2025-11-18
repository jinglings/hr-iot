package cn.iocoder.yudao.module.iot.enums.edge;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * IoT 边缘模型部署状态枚举
 *
 * @author AI Assistant
 */
@AllArgsConstructor
@Getter
public enum IotEdgeModelDeployStatusEnum implements ArrayValuable<Integer> {

    NOT_DEPLOYED(0, "未部署"),
    DEPLOYED(1, "已部署"),
    DEPLOY_FAILED(2, "部署失败"),
    RUNNING(3, "运行中"),
    STOPPED(4, "已停止");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(IotEdgeModelDeployStatusEnum::getStatus).toArray(Integer[]::new);

    /**
     * 状态值
     */
    private final Integer status;
    /**
     * 状态名
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
