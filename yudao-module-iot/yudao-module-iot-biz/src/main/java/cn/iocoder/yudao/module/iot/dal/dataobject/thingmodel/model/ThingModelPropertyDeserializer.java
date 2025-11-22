package cn.iocoder.yudao.module.iot.dal.dataobject.thingmodel.model;

import cn.iocoder.yudao.module.iot.dal.dataobject.thingmodel.model.dataType.ThingModelDataSpecs;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * ThingModelProperty 自定义反序列化器
 *
 * 用于处理 dataSpecs 和 dataSpecsList 的反序列化，将外层的 dataType 注入到内层对象中
 *
 * 解决问题：数据库中存储的 JSON 格式是 {"dataType":"double","dataSpecs":{...}}，
 * 但 Jackson 的 @JsonTypeInfo 需要在 dataSpecs 对象内部找到 dataType 字段来确定子类型。
 * 此反序列化器会自动将外层的 dataType 注入到 dataSpecs 对象中。
 *
 * @author HUIHUI
 */
public class ThingModelPropertyDeserializer extends JsonDeserializer<ThingModelProperty> {

    @Override
    public ThingModelProperty deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        ObjectMapper mapper = (ObjectMapper) p.getCodec();
        JsonNode node = mapper.readTree(p);

        ThingModelProperty property = new ThingModelProperty();

        // 基本字段反序列化
        if (node.has("identifier")) {
            property.setIdentifier(node.get("identifier").asText());
        }
        if (node.has("name")) {
            property.setName(node.get("name").asText());
        }
        if (node.has("accessMode")) {
            property.setAccessMode(node.get("accessMode").asText());
        }
        if (node.has("required")) {
            property.setRequired(node.get("required").asBoolean());
        }
        if (node.has("dataType")) {
            property.setDataType(node.get("dataType").asText());
        }

        String dataType = property.getDataType();

        // 处理 dataSpecs（单个对象）
        if (node.has("dataSpecs") && !node.get("dataSpecs").isNull()) {
            JsonNode dataSpecsNode = node.get("dataSpecs");
            if (dataSpecsNode.isObject()) {
                // 将 dataType 注入到 dataSpecs 对象中
                ObjectNode dataSpecsObject = (ObjectNode) dataSpecsNode;
                if (!dataSpecsObject.has("dataType") && dataType != null) {
                    dataSpecsObject.put("dataType", dataType);
                }
                ThingModelDataSpecs dataSpecs = mapper.treeToValue(dataSpecsObject, ThingModelDataSpecs.class);
                property.setDataSpecs(dataSpecs);
            }
        }

        // 处理 dataSpecsList（对象数组）
        if (node.has("dataSpecsList") && !node.get("dataSpecsList").isNull()) {
            JsonNode dataSpecsListNode = node.get("dataSpecsList");
            if (dataSpecsListNode.isArray()) {
                List<ThingModelDataSpecs> dataSpecsList = new ArrayList<>();
                Iterator<JsonNode> elements = dataSpecsListNode.elements();
                while (elements.hasNext()) {
                    JsonNode element = elements.next();
                    if (element.isObject()) {
                        ObjectNode dataSpecsObject = (ObjectNode) element;
                        // 将 dataType 注入到每个 dataSpecs 对象中
                        if (!dataSpecsObject.has("dataType") && dataType != null) {
                            dataSpecsObject.put("dataType", dataType);
                        }
                        ThingModelDataSpecs dataSpecs = mapper.treeToValue(dataSpecsObject, ThingModelDataSpecs.class);
                        dataSpecsList.add(dataSpecs);
                    }
                }
                property.setDataSpecsList(dataSpecsList);
            }
        }

        return property;
    }
}
