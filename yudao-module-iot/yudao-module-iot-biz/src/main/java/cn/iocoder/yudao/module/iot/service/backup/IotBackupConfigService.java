package cn.iocoder.yudao.module.iot.service.backup;

import cn.iocoder.yudao.module.iot.controller.admin.backup.vo.config.IotBackupConfigRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.backup.vo.config.IotBackupConfigSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.backup.IotBackupConfigDO;

import javax.validation.Valid;
import java.util.List;

/**
 * IoT TDengine 备份配置 Service 接口
 *
 * @author claude
 */
public interface IotBackupConfigService {

    /**
     * 创建备份配置
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createBackupConfig(@Valid IotBackupConfigSaveReqVO createReqVO);

    /**
     * 更新备份配置
     *
     * @param updateReqVO 更新信息
     */
    void updateBackupConfig(@Valid IotBackupConfigSaveReqVO updateReqVO);

    /**
     * 删除备份配置
     *
     * @param id 编号
     */
    void deleteBackupConfig(Long id);

    /**
     * 获得备份配置
     *
     * @param id 编号
     * @return 备份配置
     */
    IotBackupConfigDO getBackupConfig(Long id);

    /**
     * 获得备份配置列表
     *
     * @return 备份配置列表
     */
    List<IotBackupConfigRespVO> getBackupConfigList();

    /**
     * 启用/禁用备份配置
     *
     * @param id 编号
     * @param enabled 是否启用
     */
    void updateBackupConfigEnabled(Long id, Boolean enabled);

    /**
     * 立即执行备份配置
     *
     * @param id 编号
     * @return 备份记录ID
     */
    Long executeBackupConfig(Long id);

}
