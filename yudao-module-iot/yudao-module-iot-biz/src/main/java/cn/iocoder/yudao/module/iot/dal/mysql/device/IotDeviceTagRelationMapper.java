package cn.iocoder.yudao.module.iot.dal.mysql.device;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceTagRelationDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Collection;
import java.util.List;

/**
 * IoT 设备标签关联 Mapper
 *
 * @author AI
 */
@Mapper
public interface IotDeviceTagRelationMapper extends BaseMapperX<IotDeviceTagRelationDO> {

    /**
     * 根据设备ID查询关联的标签ID列表
     */
    default List<IotDeviceTagRelationDO> selectListByDeviceId(Long deviceId) {
        return selectList(IotDeviceTagRelationDO::getDeviceId, deviceId);
    }

    /**
     * 根据标签ID查询关联的设备ID列表
     */
    default List<IotDeviceTagRelationDO> selectListByTagId(Long tagId) {
        return selectList(IotDeviceTagRelationDO::getTagId, tagId);
    }

    /**
     * 查询设备是否已绑定指定标签
     */
    default IotDeviceTagRelationDO selectByDeviceIdAndTagId(Long deviceId, Long tagId) {
        return selectOne(new LambdaQueryWrapperX<IotDeviceTagRelationDO>()
                .eq(IotDeviceTagRelationDO::getDeviceId, deviceId)
                .eq(IotDeviceTagRelationDO::getTagId, tagId));
    }

    /**
     * 删除设备的所有标签关联
     */
    default int deleteByDeviceId(Long deviceId) {
        return delete(IotDeviceTagRelationDO::getDeviceId, deviceId);
    }

    /**
     * 删除标签的所有设备关联
     */
    default int deleteByTagId(Long tagId) {
        return delete(IotDeviceTagRelationDO::getTagId, tagId);
    }

    /**
     * 删除指定设备与标签的关联
     */
    default int deleteByDeviceIdAndTagId(Long deviceId, Long tagId) {
        return delete(new LambdaQueryWrapperX<IotDeviceTagRelationDO>()
                .eq(IotDeviceTagRelationDO::getDeviceId, deviceId)
                .eq(IotDeviceTagRelationDO::getTagId, tagId));
    }

    /**
     * 批量删除指定设备与标签的关联
     */
    default int deleteByDeviceIdAndTagIds(Long deviceId, Collection<Long> tagIds) {
        return delete(new LambdaQueryWrapperX<IotDeviceTagRelationDO>()
                .eq(IotDeviceTagRelationDO::getDeviceId, deviceId)
                .in(IotDeviceTagRelationDO::getTagId, tagIds));
    }

    /**
     * 统计设备绑定的标签数量
     */
    default Long selectCountByDeviceId(Long deviceId) {
        return selectCount(IotDeviceTagRelationDO::getDeviceId, deviceId);
    }

    /**
     * 统计标签关联的设备数量
     */
    default Long selectCountByTagId(Long tagId) {
        return selectCount(IotDeviceTagRelationDO::getTagId, tagId);
    }

    /**
     * 查询绑定了指定标签的设备ID列表
     */
    @Select("<script>" +
            "SELECT DISTINCT device_id FROM iot_device_tag_relation WHERE tag_id IN " +
            "<foreach collection='tagIds' item='tagId' open='(' separator=',' close=')'>" +
            "#{tagId}" +
            "</foreach>" +
            "</script>")
    List<Long> selectDeviceIdsByTagIds(@Param("tagIds") Collection<Long> tagIds);

}
