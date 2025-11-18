package cn.iocoder.yudao.module.iot.enums.shadow;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 设备影子变更类型枚举
 *
 * @author AI Assistant
 */
@Getter
@AllArgsConstructor
public enum ShadowChangeTypeEnum {

    DESIRED("DESIRED", "期望状态变更"),
    REPORTED("REPORTED", "实际状态上报"),
    DELTA("DELTA", "差量状态变更");

    /**
     * 类型
     */
    private final String type;

    /**
     * 描述
     */
    private final String description;

}
