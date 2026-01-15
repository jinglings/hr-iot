package cn.iocoder.yudao.module.iot.dal.mysql.device;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.tag.IotDeviceTagPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceTagDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * IoT 设备标签 Mapper
 *
 * @author AI
 */
@Mapper
public interface IotDeviceTagMapper extends BaseMapperX<IotDeviceTagDO> {

    /**
     * 分页查询设备标签
     */
    default PageResult<IotDeviceTagDO> selectPage(IotDeviceTagPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<IotDeviceTagDO>()
                .likeIfPresent(IotDeviceTagDO::getTagKey, reqVO.getTagKey())
                .likeIfPresent(IotDeviceTagDO::getTagValue, reqVO.getTagValue())
                .eqIfPresent(IotDeviceTagDO::getIsPreset, reqVO.getIsPreset())
                .orderByDesc(IotDeviceTagDO::getUsageCount)
                .orderByDesc(IotDeviceTagDO::getId));
    }

    /**
     * 根据标签键值查询
     */
    default IotDeviceTagDO selectByKeyAndValue(String tagKey, String tagValue) {
        return selectOne(new LambdaQueryWrapperX<IotDeviceTagDO>()
                .eq(IotDeviceTagDO::getTagKey, tagKey)
                .eq(IotDeviceTagDO::getTagValue, tagValue));
    }

    /**
     * 查询所有标签（用于下拉列表）
     */
    default List<IotDeviceTagDO> selectList() {
        return selectList(new LambdaQueryWrapperX<IotDeviceTagDO>()
                .orderByDesc(IotDeviceTagDO::getUsageCount)
                .orderByDesc(IotDeviceTagDO::getId));
    }

    /**
     * 根据标签键查询所有相关标签
     */
    default List<IotDeviceTagDO> selectListByTagKey(String tagKey) {
        return selectList(new LambdaQueryWrapperX<IotDeviceTagDO>()
                .eq(IotDeviceTagDO::getTagKey, tagKey)
                .orderByAsc(IotDeviceTagDO::getTagValue));
    }

    /**
     * 增加使用次数
     */
    default void incrementUsageCount(Long id, int count) {
        IotDeviceTagDO tag = selectById(id);
        if (tag != null) {
            tag.setUsageCount(tag.getUsageCount() + count);
            updateById(tag);
        }
    }

}
