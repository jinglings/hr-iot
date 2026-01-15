package cn.iocoder.yudao.module.iot.dal.mysql.scada;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.dal.dataobject.scada.IotScadaTagMappingDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * SCADA Tag 映射 Mapper
 *
 * Part of SCADA-011: Create SCADA Mappers
 *
 * @author HR-IoT Team
 */
@Mapper
public interface IotScadaTagMappingMapper extends BaseMapperX<IotScadaTagMappingDO> {

    /**
     * 根据 Tag 名称查询
     *
     * @param tagName SCADA Tag 名称
     * @return Tag 映射配置
     */
    default IotScadaTagMappingDO selectByTagName(String tagName) {
        return selectOne(IotScadaTagMappingDO::getTagName, tagName);
    }

    /**
     * 根据 Tag ID 查询
     *
     * @param tagId FUXA 内部 Tag ID
     * @return Tag 映射配置
     */
    default IotScadaTagMappingDO selectByTagId(String tagId) {
        return selectOne(IotScadaTagMappingDO::getTagId, tagId);
    }

    /**
     * 根据设备 ID 查询所有 Tag 映射
     *
     * @param deviceId IoT 设备 ID
     * @return Tag 映射列表
     */
    default List<IotScadaTagMappingDO> selectListByDeviceId(Long deviceId) {
        return selectList(new LambdaQueryWrapperX<IotScadaTagMappingDO>()
                .eq(IotScadaTagMappingDO::getDeviceId, deviceId)
                .orderByAsc(IotScadaTagMappingDO::getTagName));
    }

    /**
     * 根据设备 ID 和属性标识符查询
     *
     * @param deviceId           设备 ID
     * @param propertyIdentifier 属性标识符
     * @return Tag 映射配置
     */
    default IotScadaTagMappingDO selectByDeviceAndProperty(Long deviceId, String propertyIdentifier) {
        return selectOne(new LambdaQueryWrapperX<IotScadaTagMappingDO>()
                .eq(IotScadaTagMappingDO::getDeviceId, deviceId)
                .eq(IotScadaTagMappingDO::getPropertyIdentifier, propertyIdentifier));
    }

    /**
     * 查询指定数据类型的 Tag 列表
     *
     * @param dataType 数据类型
     * @return Tag 映射列表
     */
    default List<IotScadaTagMappingDO> selectListByDataType(String dataType) {
        return selectList(new LambdaQueryWrapperX<IotScadaTagMappingDO>()
                .eq(IotScadaTagMappingDO::getDataType, dataType));
    }

    /**
     * 模糊查询 Tag
     *
     * @param tagNamePattern Tag 名称模式
     * @return Tag 映射列表
     */
    default List<IotScadaTagMappingDO> selectListByTagNameLike(String tagNamePattern) {
        return selectList(new LambdaQueryWrapperX<IotScadaTagMappingDO>()
                .like(IotScadaTagMappingDO::getTagName, tagNamePattern)
                .orderByAsc(IotScadaTagMappingDO::getTagName));
    }

    /**
     * 统计设备的 Tag 数量
     *
     * @param deviceId 设备 ID
     * @return Tag 数量
     */
    default Long selectCountByDeviceId(Long deviceId) {
        return selectCount(IotScadaTagMappingDO::getDeviceId, deviceId);
    }

    /**
     * 删除设备的所有 Tag 映射
     *
     * @param deviceId 设备 ID
     * @return 删除数量
     */
    default int deleteByDeviceId(Long deviceId) {
        return delete(new LambdaQueryWrapperX<IotScadaTagMappingDO>()
                .eq(IotScadaTagMappingDO::getDeviceId, deviceId));
    }

    /**
     * 批量查询 Tag
     *
     * @param tagNames Tag 名称列表
     * @return Tag 映射列表
     */
    default List<IotScadaTagMappingDO> selectListByTagNames(List<String> tagNames) {
        return selectList(new LambdaQueryWrapperX<IotScadaTagMappingDO>()
                .in(IotScadaTagMappingDO::getTagName, tagNames));
    }

}
