package cn.iocoder.yudao.module.iot.enums.backup;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * IoT TDengine 备份模式枚举
 *
 * @author claude
 */
@RequiredArgsConstructor
@Getter
public enum BackupModeEnum implements ArrayValuable<Integer> {

    MANUAL(1, "手动"),
    AUTO(2, "自动定时");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(BackupModeEnum::getMode).toArray(Integer[]::new);

    /**
     * 模式
     */
    private final Integer mode;
    /**
     * 模式名
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
