package cn.iocoder.yudao.module.iot.enums.edge;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * IoT 边缘 AI 模型格式枚举
 *
 * @author AI Assistant
 */
@AllArgsConstructor
@Getter
public enum IotEdgeAiModelFormatEnum implements ArrayValuable<Integer> {

    ONNX(1, "ONNX"),
    TENSORFLOW_LITE(2, "TensorFlow Lite"),
    PYTORCH_MOBILE(3, "PyTorch Mobile"),
    CAFFE(4, "Caffe"),
    OPENVINO(5, "OpenVINO");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(IotEdgeAiModelFormatEnum::getFormat).toArray(Integer[]::new);

    /**
     * 格式值
     */
    private final Integer format;
    /**
     * 格式名
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
