package cn.iocoder.yudao.module.iot.enums.edge;

import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * IoT 边缘 AI 模型类型枚举
 *
 * @author AI Assistant
 */
@AllArgsConstructor
@Getter
public enum IotEdgeAiModelTypeEnum implements IntArrayValuable {

    IMAGE_CLASSIFICATION(1, "图像分类"),
    OBJECT_DETECTION(2, "目标检测"),
    ANOMALY_DETECTION(3, "异常检测"),
    FACE_RECOGNITION(4, "人脸识别"),
    QUALITY_INSPECTION(5, "质量检测");

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(IotEdgeAiModelTypeEnum::getType).toArray();

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
