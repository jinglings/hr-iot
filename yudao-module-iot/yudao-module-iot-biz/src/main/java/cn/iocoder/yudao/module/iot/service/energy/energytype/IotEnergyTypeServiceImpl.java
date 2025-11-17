package cn.iocoder.yudao.module.iot.service.energy.energytype;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.energy.energytype.vo.IotEnergyTypePageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.energy.energytype.vo.IotEnergyTypeRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.energy.energytype.vo.IotEnergyTypeSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.energy.IotEnergyTypeDO;
import cn.iocoder.yudao.module.iot.dal.mysql.energy.IotEnergyTypeMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.*;

/**
 * IoT 能源类型 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class IotEnergyTypeServiceImpl implements IotEnergyTypeService {

    @Resource
    private IotEnergyTypeMapper energyTypeMapper;

    @Override
    public Long createEnergyType(IotEnergyTypeSaveReqVO createReqVO) {
        // 校验父级能源类型是否存在（如果指定了父级）
        if (createReqVO.getParentId() != null && createReqVO.getParentId() > 0) {
            validateEnergyTypeExists(createReqVO.getParentId());
        }

        // 校验能源编码唯一性
        validateEnergyCodeUnique(null, createReqVO.getEnergyCode());

        // 插入
        IotEnergyTypeDO energyType = BeanUtils.toBean(createReqVO, IotEnergyTypeDO.class);
        energyTypeMapper.insert(energyType);

        // 返回
        return energyType.getId();
    }

    @Override
    public void updateEnergyType(IotEnergyTypeSaveReqVO updateReqVO) {
        // 校验存在
        validateEnergyTypeExists(updateReqVO.getId());

        // 校验父级能源类型是否存在（如果指定了父级）
        if (updateReqVO.getParentId() != null && updateReqVO.getParentId() > 0) {
            validateEnergyTypeExists(updateReqVO.getParentId());

            // 校验不能设置自己为父级
            if (updateReqVO.getId().equals(updateReqVO.getParentId())) {
                throw exception(ENERGY_TYPE_PARENT_ERROR);
            }

            // 校验不能设置自己的子级为父级
            if (isChildOf(updateReqVO.getId(), updateReqVO.getParentId())) {
                throw exception(ENERGY_TYPE_PARENT_ERROR);
            }
        }

        // 校验能源编码唯一性
        validateEnergyCodeUnique(updateReqVO.getId(), updateReqVO.getEnergyCode());

        // 更新
        IotEnergyTypeDO updateObj = BeanUtils.toBean(updateReqVO, IotEnergyTypeDO.class);
        energyTypeMapper.updateById(updateObj);
    }

    @Override
    public void deleteEnergyType(Long id) {
        // 校验存在
        validateEnergyTypeExists(id);

        // 校验是否存在子能源类型
        List<IotEnergyTypeDO> children = energyTypeMapper.selectListByParentId(id);
        if (CollUtil.isNotEmpty(children)) {
            throw exception(ENERGY_TYPE_EXITS_CHILDREN);
        }

        // TODO 校验是否存在计量点等关联数据

        // 删除
        energyTypeMapper.deleteById(id);
    }

    /**
     * 校验能源类型是否存在
     */
    private void validateEnergyTypeExists(Long id) {
        if (energyTypeMapper.selectById(id) == null) {
            throw exception(ENERGY_TYPE_NOT_EXISTS);
        }
    }

    /**
     * 校验能源编码唯一性
     */
    private void validateEnergyCodeUnique(Long id, String energyCode) {
        IotEnergyTypeDO energyType = energyTypeMapper.selectByEnergyCode(energyCode);
        if (energyType == null) {
            return;
        }
        // 如果 id 为空，说明不用比较是否为相同 id 的能源类型
        if (id == null) {
            throw exception(ENERGY_TYPE_CODE_EXISTS);
        }
        if (!energyType.getId().equals(id)) {
            throw exception(ENERGY_TYPE_CODE_EXISTS);
        }
    }

    /**
     * 判断 childId 是否是 parentId 的子孙节点
     */
    private boolean isChildOf(Long parentId, Long childId) {
        List<IotEnergyTypeDO> children = energyTypeMapper.selectListByParentId(parentId);
        for (IotEnergyTypeDO child : children) {
            if (child.getId().equals(childId)) {
                return true;
            }
            if (isChildOf(child.getId(), childId)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public IotEnergyTypeDO getEnergyType(Long id) {
        return energyTypeMapper.selectById(id);
    }

    @Override
    public List<IotEnergyTypeDO> getEnergyTypeList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return CollUtil.newArrayList();
        }
        return energyTypeMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<IotEnergyTypeDO> getEnergyTypePage(IotEnergyTypePageReqVO pageReqVO) {
        return energyTypeMapper.selectPage(pageReqVO);
    }

    @Override
    public List<IotEnergyTypeRespVO> getEnergyTypeTree() {
        // 获取所有能源类型
        List<IotEnergyTypeDO> allEnergyTypes = energyTypeMapper.selectList();

        // 转换为 VO
        List<IotEnergyTypeRespVO> allNodes = BeanUtils.toBean(allEnergyTypes, IotEnergyTypeRespVO.class);

        // 构建树形结构
        return buildTree(allNodes, 0L);
    }

    /**
     * 递归构建树形结构
     */
    private List<IotEnergyTypeRespVO> buildTree(List<IotEnergyTypeRespVO> allNodes, Long parentId) {
        return allNodes.stream()
                .filter(node -> {
                    Long nodeParentId = node.getParentId() != null ? node.getParentId() : 0L;
                    return nodeParentId.equals(parentId);
                })
                .peek(node -> {
                    List<IotEnergyTypeRespVO> children = buildTree(allNodes, node.getId());
                    if (CollUtil.isNotEmpty(children)) {
                        node.setChildren(children);
                    }
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<IotEnergyTypeDO> getEnergyTypeListByParentId(Long parentId) {
        return energyTypeMapper.selectListByParentId(parentId);
    }

    @Override
    public List<IotEnergyTypeDO> getEnergyTypeListByStatus(Integer status) {
        return energyTypeMapper.selectListByStatus(status);
    }

}
