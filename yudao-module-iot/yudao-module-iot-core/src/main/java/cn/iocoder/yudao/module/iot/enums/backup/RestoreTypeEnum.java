package cn.iocoder.yudao.module.iot.enums.backup;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * IoT TDengine 恢复类型枚举
 *
 * @author claude
 */
@RequiredArgsConstructor
@Getter
public enum RestoreTypeEnum implements ArrayValuable<Integer> {

    FULL(1, "完整恢复"),
    SELECTIVE(2, "选择性恢复");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(RestoreTypeEnum::getType).toArray(Integer[]::new);

    /**
     * 类型
     */
    private final Integer type;
    /**
     * 类型名
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
