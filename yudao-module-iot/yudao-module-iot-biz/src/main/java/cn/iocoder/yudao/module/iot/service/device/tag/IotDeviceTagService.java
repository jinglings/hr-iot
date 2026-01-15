package cn.iocoder.yudao.module.iot.service.device.tag;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.tag.*;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceTagDO;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

/**
 * IoT 设备标签 Service 接口
 *
 * @author AI
 */
public interface IotDeviceTagService {

    /**
     * 创建设备标签
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createTag(@Valid IotDeviceTagCreateReqVO createReqVO);

    /**
     * 更新设备标签
     *
     * @param updateReqVO 更新信息
     */
    void updateTag(@Valid IotDeviceTagUpdateReqVO updateReqVO);

    /**
     * 删除设备标签
     *
     * @param id 编号
     */
    void deleteTag(Long id);

    /**
     * 获得设备标签
     *
     * @param id 编号
     * @return 设备标签
     */
    IotDeviceTagDO getTag(Long id);

    /**
     * 获得设备标签分页
     *
     * @param pageReqVO 分页查询
     * @return 设备标签分页
     */
    PageResult<IotDeviceTagDO> getTagPage(IotDeviceTagPageReqVO pageReqVO);

    /**
     * 获得设备标签列表
     *
     * @return 设备标签列表
     */
    List<IotDeviceTagDO> getTagList();

    /**
     * 为设备绑定标签
     *
     * @param bindReqVO 绑定信息
     */
    void bindTagsToDevice(@Valid IotDeviceTagBindReqVO bindReqVO);

    /**
     * 为设备解绑标签
     *
     * @param bindReqVO 解绑信息
     */
    void unbindTagsFromDevice(@Valid IotDeviceTagBindReqVO bindReqVO);

    /**
     * 批量为设备绑定标签
     *
     * @param batchBindReqVO 批量绑定信息
     */
    void batchBindTags(@Valid IotDeviceTagBatchBindReqVO batchBindReqVO);

    /**
     * 批量解绑设备标签
     *
     * @param batchBindReqVO 批量解绑信息
     */
    void batchUnbindTags(@Valid IotDeviceTagBatchBindReqVO batchBindReqVO);

    /**
     * 获取设备的所有标签
     *
     * @param deviceId 设备ID
     * @return 标签列表
     */
    List<IotDeviceTagDO> getTagsByDeviceId(Long deviceId);

    /**
     * 根据标签ID列表查询关联的设备ID列表
     *
     * @param tagIds 标签ID列表
     * @return 设备ID列表
     */
    List<Long> getDeviceIdsByTagIds(Collection<Long> tagIds);

}
