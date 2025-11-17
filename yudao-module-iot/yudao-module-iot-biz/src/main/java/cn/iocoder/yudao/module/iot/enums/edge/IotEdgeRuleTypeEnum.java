package cn.iocoder.yudao.module.iot.enums.edge;

import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * IoT 边缘规则类型枚举
 *
 * @author AI Assistant
 */
@AllArgsConstructor
@Getter
public enum IotEdgeRuleTypeEnum implements IntArrayValuable {

    SCENE(1, "场景规则"),
    DATA_FLOW(2, "数据流转"),
    AI_INFERENCE(3, "AI推理");

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(IotEdgeRuleTypeEnum::getType).toArray();

    /**
     * 类型值
     */
    private final Integer type;
    /**
     * 类型名
     */
    private final String name;

    @Override
    public int[] array() {
        return ARRAYS;
    }

}
