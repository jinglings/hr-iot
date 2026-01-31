package cn.iocoder.yudao.module.iot.enums.backup;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * IoT TDengine 备份状态枚举
 *
 * @author claude
 */
@RequiredArgsConstructor
@Getter
public enum BackupStatusEnum implements ArrayValuable<Integer> {

    IN_PROGRESS(0, "备份中"),
    SUCCESS(1, "成功"),
    FAILED(2, "失败");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(BackupStatusEnum::getStatus).toArray(Integer[]::new);

    /**
     * 状态
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
