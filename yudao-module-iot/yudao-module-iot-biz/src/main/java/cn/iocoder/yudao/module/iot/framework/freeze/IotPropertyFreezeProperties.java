package cn.iocoder.yudao.module.iot.framework.freeze;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * IoT 设备属性【数据冻结检测】配置
 *
 * 用于检测"设备通信正常、但采集到的属性值长时间保持不变"的场景，
 * 即从某个时间点起就没有再获取到新的值（传感器卡死、寄存器未刷新、网关回旧值等）。
 *
 * 配置前缀：{@code yudao.iot.property-freeze}
 *
 * @author HR-IoT Team
 */
@ConfigurationProperties(prefix = "yudao.iot.property-freeze")
@Data
public class IotPropertyFreezeProperties {

    /**
     * 是否启用数据冻结检测（全局开关），默认启用
     */
    private boolean enabled = true;

    /**
     * 全局冻结阈值：值持续未变化超过该时长，标记为冻结。默认 30 分钟。
     *
     * 支持 {@code 30m}、{@code 1h}、{@code PT30M} 等写法。
     */
    private Duration threshold = Duration.ofMinutes(30);

    /**
     * 属性级冻结开关：禁用检测的属性标识符集合。
     *
     * 对于天生长期不变的属性（如设定值、额定功率、开关量等），在此声明 identifier 即可关闭检测，避免误报。
     */
    private Set<String> disabledIdentifiers = Collections.emptySet();

    /**
     * 属性级阈值覆盖：按属性标识符单独配置冻结阈值，覆盖 {@link #threshold} 全局值。
     *
     * key 为属性 identifier，value 为时长（如 {@code 2h}）。
     */
    private Map<String, Duration> identifierThresholds = Collections.emptyMap();

    /**
     * 判断指定属性是否启用冻结检测（全局开 且 不在禁用名单中）
     *
     * @param identifier 属性标识符
     * @return 是否启用检测
     */
    public boolean isDetectEnabled(String identifier) {
        return enabled && !disabledIdentifiers.contains(identifier);
    }

    /**
     * 获取指定属性的冻结阈值（毫秒）：优先属性级覆盖，否则取全局阈值
     *
     * @param identifier 属性标识符
     * @return 阈值毫秒数
     */
    public long getThresholdMillis(String identifier) {
        Duration duration = identifierThresholds.getOrDefault(identifier, threshold);
        return duration.toMillis();
    }

}
