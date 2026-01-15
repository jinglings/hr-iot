package cn.iocoder.yudao.module.iot.service.device.tag;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.tag.*;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceTagDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceTagRelationDO;
import cn.iocoder.yudao.module.iot.dal.mysql.device.IotDeviceTagMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.device.IotDeviceTagRelationMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.*;

/**
 * IoT 设备标签 Service 实现类
 *
 * @author AI
 */
@Service
@Validated
public class IotDeviceTagServiceImpl implements IotDeviceTagService {

    /**
     * 每个设备最多绑定的标签数量
     */
    private static final int MAX_TAGS_PER_DEVICE = 20;

    @Resource
    private IotDeviceTagMapper tagMapper;

    @Resource
    private IotDeviceTagRelationMapper tagRelationMapper;

    @Override
    public Long createTag(IotDeviceTagCreateReqVO createReqVO) {
        // 1. 校验标签键值唯一性
        validateTagKeyValueUnique(null, createReqVO.getTagKey(), createReqVO.getTagValue());

        // 2. 插入
        IotDeviceTagDO tag = BeanUtils.toBean(createReqVO, IotDeviceTagDO.class);
        tag.setIsPreset(false);
        tag.setUsageCount(0);
        if (tag.getColor() == null) {
            tag.setColor("#409EFF"); // 默认颜色
        }
        tagMapper.insert(tag);
        return tag.getId();
    }

    @Override
    public void updateTag(IotDeviceTagUpdateReqVO updateReqVO) {
        // 1. 校验存在
        IotDeviceTagDO tag = validateTagExists(updateReqVO.getId());

        // 2. 校验标签键值唯一性
        if (updateReqVO.getTagKey() != null || updateReqVO.getTagValue() != null) {
            String tagKey = updateReqVO.getTagKey() != null ? updateReqVO.getTagKey() : tag.getTagKey();
            String tagValue = updateReqVO.getTagValue() != null ? updateReqVO.getTagValue() : tag.getTagValue();
            validateTagKeyValueUnique(updateReqVO.getId(), tagKey, tagValue);
        }

        // 3. 更新
        IotDeviceTagDO updateObj = BeanUtils.toBean(updateReqVO, IotDeviceTagDO.class);
        tagMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTag(Long id) {
        // 1. 校验存在
        IotDeviceTagDO tag = validateTagExists(id);

        // 2. 预置标签不允许删除
        if (Boolean.TRUE.equals(tag.getIsPreset())) {
            throw exception(DEVICE_TAG_PRESET_CANNOT_DELETE);
        }

        // 3. 删除标签关联关系
        tagRelationMapper.deleteByTagId(id);

        // 4. 删除标签
        tagMapper.deleteById(id);
    }

    @Override
    public IotDeviceTagDO getTag(Long id) {
        return tagMapper.selectById(id);
    }

    @Override
    public PageResult<IotDeviceTagDO> getTagPage(IotDeviceTagPageReqVO pageReqVO) {
        return tagMapper.selectPage(pageReqVO);
    }

    @Override
    public List<IotDeviceTagDO> getTagList() {
        return tagMapper.selectList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bindTagsToDevice(IotDeviceTagBindReqVO bindReqVO) {
        Long deviceId = bindReqVO.getDeviceId();
        List<Long> tagIds = bindReqVO.getTagIds();

        // 1. 检查设备当前标签数量 + 待绑定数量是否超限
        Long currentCount = tagRelationMapper.selectCountByDeviceId(deviceId);
        if (currentCount + tagIds.size() > MAX_TAGS_PER_DEVICE) {
            throw exception(DEVICE_TAG_BIND_LIMIT_EXCEEDED);
        }

        // 2. 批量绑定
        for (Long tagId : tagIds) {
            // 校验标签存在
            validateTagExists(tagId);

            // 检查是否已绑定
            IotDeviceTagRelationDO existing = tagRelationMapper.selectByDeviceIdAndTagId(deviceId, tagId);
            if (existing != null) {
                continue; // 已绑定则跳过
            }

            // 创建关联
            IotDeviceTagRelationDO relation = IotDeviceTagRelationDO.builder()
                    .deviceId(deviceId)
                    .tagId(tagId)
                    .tenantId(TenantContextHolder.getTenantId())
                    .createTime(LocalDateTime.now())
                    .build();
            tagRelationMapper.insert(relation);

            // 增加使用计数
            tagMapper.incrementUsageCount(tagId, 1);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unbindTagsFromDevice(IotDeviceTagBindReqVO bindReqVO) {
        Long deviceId = bindReqVO.getDeviceId();
        List<Long> tagIds = bindReqVO.getTagIds();

        for (Long tagId : tagIds) {
            // 删除关联
            int deleted = tagRelationMapper.deleteByDeviceIdAndTagId(deviceId, tagId);
            if (deleted > 0) {
                // 减少使用计数
                tagMapper.incrementUsageCount(tagId, -1);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchBindTags(IotDeviceTagBatchBindReqVO batchBindReqVO) {
        List<Long> deviceIds = batchBindReqVO.getDeviceIds();
        List<Long> tagIds = batchBindReqVO.getTagIds();

        for (Long deviceId : deviceIds) {
            IotDeviceTagBindReqVO bindReqVO = new IotDeviceTagBindReqVO();
            bindReqVO.setDeviceId(deviceId);
            bindReqVO.setTagIds(tagIds);
            bindTagsToDevice(bindReqVO);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchUnbindTags(IotDeviceTagBatchBindReqVO batchBindReqVO) {
        List<Long> deviceIds = batchBindReqVO.getDeviceIds();
        List<Long> tagIds = batchBindReqVO.getTagIds();

        for (Long deviceId : deviceIds) {
            IotDeviceTagBindReqVO bindReqVO = new IotDeviceTagBindReqVO();
            bindReqVO.setDeviceId(deviceId);
            bindReqVO.setTagIds(tagIds);
            unbindTagsFromDevice(bindReqVO);
        }
    }

    @Override
    public List<IotDeviceTagDO> getTagsByDeviceId(Long deviceId) {
        // 1. 查询设备关联的标签ID
        List<IotDeviceTagRelationDO> relations = tagRelationMapper.selectListByDeviceId(deviceId);
        if (CollUtil.isEmpty(relations)) {
            return Collections.emptyList();
        }

        // 2. 查询标签详情
        List<Long> tagIds = relations.stream()
                .map(IotDeviceTagRelationDO::getTagId)
                .collect(Collectors.toList());
        return tagMapper.selectBatchIds(tagIds);
    }

    @Override
    public List<Long> getDeviceIdsByTagIds(Collection<Long> tagIds) {
        if (CollUtil.isEmpty(tagIds)) {
            return Collections.emptyList();
        }
        return tagRelationMapper.selectDeviceIdsByTagIds(tagIds);
    }

    /**
     * 校验标签是否存在
     */
    private IotDeviceTagDO validateTagExists(Long id) {
        IotDeviceTagDO tag = tagMapper.selectById(id);
        if (tag == null) {
            throw exception(DEVICE_TAG_NOT_EXISTS);
        }
        return tag;
    }

    /**
     * 校验标签键值唯一性
     */
    private void validateTagKeyValueUnique(Long id, String tagKey, String tagValue) {
        IotDeviceTagDO existing = tagMapper.selectByKeyAndValue(tagKey, tagValue != null ? tagValue : "");
        if (existing != null && !existing.getId().equals(id)) {
            throw exception(DEVICE_TAG_KEY_VALUE_EXISTS);
        }
    }

}
