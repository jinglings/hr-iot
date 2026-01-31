package cn.iocoder.yudao.module.iot.enums.backup;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * IoT TDengine 备份类型枚举
 *
 * @author claude
 */
@RequiredArgsConstructor
@Getter
public enum BackupTypeEnum implements ArrayValuable<Integer> {

    FULL(1, "全量备份"),
    DATABASE(2, "数据库备份"),
    TABLE(3, "表备份");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(BackupTypeEnum::getType).toArray(Integer[]::new);

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
