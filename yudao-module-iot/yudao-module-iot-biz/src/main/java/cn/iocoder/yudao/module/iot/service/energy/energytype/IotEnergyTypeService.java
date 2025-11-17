package cn.iocoder.yudao.module.iot.service.energy.energytype;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.energy.energytype.vo.IotEnergyTypePageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.energy.energytype.vo.IotEnergyTypeRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.energy.energytype.vo.IotEnergyTypeSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.energy.IotEnergyTypeDO;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

/**
 * IoT 能源类型 Service 接口
 *
 * @author 芋道源码
 */
public interface IotEnergyTypeService {

    /**
     * 创建能源类型
     */
    Long createEnergyType(@Valid IotEnergyTypeSaveReqVO createReqVO);

    /**
     * 更新能源类型
     */
    void updateEnergyType(@Valid IotEnergyTypeSaveReqVO updateReqVO);

    /**
     * 删除能源类型
     */
    void deleteEnergyType(Long id);

    /**
     * 获得能源类型
     */
    IotEnergyTypeDO getEnergyType(Long id);

    /**
     * 获得能源类型列表
     */
    List<IotEnergyTypeDO> getEnergyTypeList(Collection<Long> ids);

    /**
     * 获得能源类型分页
     */
    PageResult<IotEnergyTypeDO> getEnergyTypePage(IotEnergyTypePageReqVO pageReqVO);

    /**
     * 获得能源类型树形结构
     */
    List<IotEnergyTypeRespVO> getEnergyTypeTree();

    /**
     * 获得指定父级的能源类型列表
     */
    List<IotEnergyTypeDO> getEnergyTypeListByParentId(Long parentId);

    /**
     * 获得指定状态的能源类型列表
     */
    List<IotEnergyTypeDO> getEnergyTypeListByStatus(Integer status);

}
