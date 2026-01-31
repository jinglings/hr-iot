package cn.iocoder.yudao.module.iot.service.backup;

import cn.iocoder.yudao.module.iot.controller.admin.backup.vo.config.IotBackupConfigRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.backup.vo.config.IotBackupConfigSaveReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.backup.vo.record.IotBackupRecordCreateReqVO;
import cn.iocoder.yudao.module.iot.convert.backup.IotBackupConfigConvert;
import cn.iocoder.yudao.module.iot.dal.dataobject.backup.IotBackupConfigDO;
import cn.iocoder.yudao.module.iot.dal.mysql.backup.IotBackupConfigMapper;
import cn.iocoder.yudao.module.iot.enums.backup.BackupModeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.BACKUP_CONFIG_NAME_EXISTS;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.BACKUP_CONFIG_NOT_EXISTS;

/**
 * IoT TDengine 备份配置 Service 实现类
 *
 * @author claude
 */
@Service
@Slf4j
public class IotBackupConfigServiceImpl implements IotBackupConfigService {

    @Resource
    private IotBackupConfigMapper backupConfigMapper;

    @Resource
    private IotBackupRecordService backupRecordService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createBackupConfig(IotBackupConfigSaveReqVO createReqVO) {
        log.info("[createBackupConfig][开始创建备份配置] req={}", createReqVO);

        // 校验配置名称唯一性
        validateConfigNameUnique(null, createReqVO.getConfigName());

        // 插入配置
        IotBackupConfigDO config = IotBackupConfigConvert.INSTANCE.convert(createReqVO);
        backupConfigMapper.insert(config);

        log.info("[createBackupConfig][创建成功] configId={}", config.getId());
        return config.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBackupConfig(IotBackupConfigSaveReqVO updateReqVO) {
        log.info("[updateBackupConfig][开始更新备份配置] req={}", updateReqVO);

        // 校验存在
        validateBackupConfigExists(updateReqVO.getId());

        // 校验配置名称唯一性
        validateConfigNameUnique(updateReqVO.getId(), updateReqVO.getConfigName());

        // 更新配置
        IotBackupConfigDO updateObj = IotBackupConfigConvert.INSTANCE.convert(updateReqVO);
        backupConfigMapper.updateById(updateObj);

        log.info("[updateBackupConfig][更新成功] configId={}", updateReqVO.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBackupConfig(Long id) {
        log.info("[deleteBackupConfig][开始删除备份配置] id={}", id);

        // 校验存在
        validateBackupConfigExists(id);

        // 删除配置
        backupConfigMapper.deleteById(id);

        log.info("[deleteBackupConfig][删除成功] id={}", id);
    }

    @Override
    public IotBackupConfigDO getBackupConfig(Long id) {
        return backupConfigMapper.selectById(id);
    }

    @Override
    public List<IotBackupConfigRespVO> getBackupConfigList() {
        List<IotBackupConfigDO> list = backupConfigMapper.selectAllList();
        return IotBackupConfigConvert.INSTANCE.convertList(list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBackupConfigEnabled(Long id, Boolean enabled) {
        log.info("[updateBackupConfigEnabled][更新配置状态] id={}, enabled={}", id, enabled);

        // 校验存在
        IotBackupConfigDO config = validateBackupConfigExists(id);

        // 更新状态
        IotBackupConfigDO updateObj = new IotBackupConfigDO();
        updateObj.setId(id);
        updateObj.setEnabled(enabled);
        backupConfigMapper.updateById(updateObj);

        log.info("[updateBackupConfigEnabled][更新成功] id={}, enabled={}", id, enabled);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long executeBackupConfig(Long id) {
        log.info("[executeBackupConfig][立即执行备份配置] id={}", id);

        // 校验存在
        IotBackupConfigDO config = validateBackupConfigExists(id);

        // 构建备份请求
        IotBackupRecordCreateReqVO backupReq = new IotBackupRecordCreateReqVO();
        backupReq.setBackupName(generateBackupName(config));
        backupReq.setBackupType(config.getBackupType());
        backupReq.setBackupScope(config.getBackupScope());
        backupReq.setBackupMode(BackupModeEnum.MANUAL.getMode()); // 手动触发
        backupReq.setRemark("手动执行配置：" + config.getConfigName());

        // 执行备份
        Long backupId = backupRecordService.createBackup(backupReq);

        log.info("[executeBackupConfig][执行成功] configId={}, backupId={}", id, backupId);
        return backupId;
    }

    /**
     * 校验备份配置是否存在
     *
     * @param id 编号
     * @return 备份配置
     */
    private IotBackupConfigDO validateBackupConfigExists(Long id) {
        IotBackupConfigDO config = backupConfigMapper.selectById(id);
        if (config == null) {
            throw exception(BACKUP_CONFIG_NOT_EXISTS);
        }
        return config;
    }

    /**
     * 校验配置名称唯一性
     *
     * @param id 编号（更新时传入，创建时为null）
     * @param configName 配置名称
     */
    private void validateConfigNameUnique(Long id, String configName) {
        IotBackupConfigDO config = backupConfigMapper.selectByConfigName(configName);
        if (config == null) {
            return;
        }

        // 如果是更新，排除自己
        if (id != null && config.getId().equals(id)) {
            return;
        }

        throw exception(BACKUP_CONFIG_NAME_EXISTS);
    }

    /**
     * 生成备份名称
     *
     * @param config 配置
     * @return 备份名称
     */
    private String generateBackupName(IotBackupConfigDO config) {
        return String.format("auto_%s_%d", config.getConfigName(), System.currentTimeMillis());
    }

}
