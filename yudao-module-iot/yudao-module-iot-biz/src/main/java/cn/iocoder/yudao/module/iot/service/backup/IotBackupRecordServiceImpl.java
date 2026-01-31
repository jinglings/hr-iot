package cn.iocoder.yudao.module.iot.service.backup;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.QueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.backup.vo.record.IotBackupRecordCreateReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.backup.vo.record.IotBackupRecordPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.backup.vo.record.IotBackupRecordRespVO;
import cn.iocoder.yudao.module.iot.convert.backup.IotBackupRecordConvert;
import cn.iocoder.yudao.module.iot.dal.dataobject.backup.IotBackupRecordDO;
import cn.iocoder.yudao.module.iot.dal.mysql.backup.IotBackupRecordMapper;
import cn.iocoder.yudao.module.iot.enums.backup.BackupModeEnum;
import cn.iocoder.yudao.module.iot.enums.backup.BackupStatusEnum;
import cn.iocoder.yudao.module.iot.enums.backup.BackupTypeEnum;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.BACKUP_RECORD_NOT_EXISTS;

/**
 * IoT TDengine 备份记录 Service 实现类
 *
 * @author claude
 */
@Service
@Slf4j
public class IotBackupRecordServiceImpl implements IotBackupRecordService {

    @Resource
    private IotBackupRecordMapper backupRecordMapper;

    @Resource
    private IotBackupExecutor backupExecutor;

    /**
     * 备份存储路径
     */
    @Value("${yudao.iot.backup.storage-path:/data/iot-backup}")
    private String backupStoragePath;

    @Override
    @Async("backupExecutor")
    @Transactional(rollbackFor = Exception.class)
    public Long createBackup(IotBackupRecordCreateReqVO createReqVO) {
        log.info("[createBackup][开始创建备份] req={}", createReqVO);

        // 1. 创建备份记录
        IotBackupRecordDO record = IotBackupRecordConvert.INSTANCE.convert(createReqVO);
        record.setBackupStatus(BackupStatusEnum.IN_PROGRESS.getStatus());
        record.setStartTime(LocalDateTime.now());
        record.setFileFormat("zip");

        // 设置默认备份模式
        if (record.getBackupMode() == null) {
            record.setBackupMode(BackupModeEnum.MANUAL.getMode());
        }

        backupRecordMapper.insert(record);
        Long recordId = record.getId();

        try {
            // 2. 生成备份路径
            String backupPath = generateBackupPath(record);

            // 3. 执行备份
            IotBackupExecutor.BackupResult result;
            if (BackupTypeEnum.FULL.getType().equals(createReqVO.getBackupType())) {
                // 全量备份
                result = backupExecutor.executeFullBackup(backupPath);
            } else if (BackupTypeEnum.DATABASE.getType().equals(createReqVO.getBackupType())) {
                // 数据库备份
                List<String> databases = JSONUtil.toList(createReqVO.getBackupScope(), String.class);
                result = backupExecutor.executeDatabaseBackup(backupPath, databases);
            } else {
                throw new ServiceException(400, "暂不支持的备份类型");
            }

            // 4. 更新备份记录
            record.setBackupStatus(BackupStatusEnum.SUCCESS.getStatus());
            record.setFilePath(result.getFilePath());
            record.setFileSize(result.getFileSize());
            record.setRecordCount(result.getRecordCount());
            record.setEndTime(LocalDateTime.now());
            record.setDuration((int) Duration.between(record.getStartTime(), record.getEndTime()).getSeconds());

            backupRecordMapper.updateById(record);

            log.info("[createBackup][备份成功] recordId={}, filePath={}", recordId, result.getFilePath());

            return recordId;

        } catch (Exception e) {
            log.error("[createBackup][备份失败] recordId={}", recordId, e);

            // 更新备份记录为失败状态
            record.setBackupStatus(BackupStatusEnum.FAILED.getStatus());
            record.setEndTime(LocalDateTime.now());
            record.setErrorMessage(e.getMessage());
            record.setDuration((int) Duration.between(record.getStartTime(), record.getEndTime()).getSeconds());

            backupRecordMapper.updateById(record);

            throw new ServiceException(500, "备份失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBackup(Long id) {
        // 1. 校验存在
        IotBackupRecordDO record = validateBackupRecordExists(id);

        // 2. 删除备份文件
        if (record.getFilePath() != null) {
            try {
                FileUtil.del(record.getFilePath());
                log.info("[deleteBackup][删除备份文件成功] id={}, path={}", id, record.getFilePath());
            } catch (Exception e) {
                log.error("[deleteBackup][删除备份文件失败] id={}, path={}", id, record.getFilePath(), e);
                // 文件删除失败不影响记录删除
            }
        }

        // 3. 删除记录
        backupRecordMapper.deleteById(id);
        log.info("[deleteBackup][删除备份记录成功] id={}", id);
    }

    @Override
    public IotBackupRecordDO getBackupRecord(Long id) {
        return backupRecordMapper.selectById(id);
    }

    @Override
    public PageResult<IotBackupRecordRespVO> getBackupRecordPage(IotBackupRecordPageReqVO pageReqVO) {
        PageResult<IotBackupRecordDO> pageResult = backupRecordMapper.selectPage(pageReqVO);
        return IotBackupRecordConvert.INSTANCE.convertPage(pageResult);
    }

    @Override
    public String downloadBackup(Long id) {
        // 校验存在
        IotBackupRecordDO record = validateBackupRecordExists(id);

        // 校验文件存在
        if (record.getFilePath() == null || !FileUtil.exist(record.getFilePath())) {
            throw new ServiceException(400, "备份文件不存在");
        }

        return record.getFilePath();
    }

    @Override
    public IotBackupExecutor.BackupMetadata validateBackup(Long id) {
        // 校验存在
        IotBackupRecordDO record = validateBackupRecordExists(id);

        // 校验文件存在
        if (record.getFilePath() == null || !FileUtil.exist(record.getFilePath())) {
            throw new ServiceException(400, "备份文件不存在");
        }

        try {
            // 解压备份文件
            String extractPath = backupExecutor.decompressBackup(record.getFilePath());

            // 读取元数据
            IotBackupExecutor.BackupMetadata metadata = backupExecutor.readMetadata(extractPath);

            // 清理临时文件
            FileUtil.del(extractPath);

            return metadata;

        } catch (Exception e) {
            log.error("[validateBackup][验证备份失败] id={}", id, e);
            throw new ServiceException(500, "验证备份失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cleanExpiredBackups(Integer retentionDays, Integer maxBackupCount) {
        log.info("[cleanExpiredBackups][开始清理过期备份] retentionDays={}, maxBackupCount={}",
                retentionDays, maxBackupCount);

        // 1. 按时间清理：删除超过保留天数的备份
        if (retentionDays != null && retentionDays > 0) {
            LocalDateTime beforeTime = LocalDateTime.now().minusDays(retentionDays);
            List<IotBackupRecordDO> expiredRecords = backupRecordMapper.selectListBeforeTime(beforeTime);

            for (IotBackupRecordDO record : expiredRecords) {
                deleteBackup(record.getId());
            }

            log.info("[cleanExpiredBackups][按时间清理完成] count={}", expiredRecords.size());
        }

        // 2. 按数量清理：保留最新的N个备份，删除其余的
        if (maxBackupCount != null && maxBackupCount > 0) {

            Wrapper<IotBackupRecordDO> pageWrapper = new QueryWrapperX<>();
            List<IotBackupRecordDO> allRecords = backupRecordMapper.selectList(pageWrapper);
            if (allRecords.size() > maxBackupCount) {
                // 删除超出数量的旧备份
                List<IotBackupRecordDO> toDelete = allRecords.subList(maxBackupCount, allRecords.size());
                for (IotBackupRecordDO record : toDelete) {
                    deleteBackup(record.getId());
                }

                log.info("[cleanExpiredBackups][按数量清理完成] count={}", toDelete.size());
            }
        }
    }

    /**
     * 校验备份记录是否存在
     *
     * @param id 编号
     * @return 备份记录
     */
    private IotBackupRecordDO validateBackupRecordExists(Long id) {
        IotBackupRecordDO record = backupRecordMapper.selectById(id);
        if (record == null) {
            throw exception(BACKUP_RECORD_NOT_EXISTS);
        }
        return record;
    }

    /**
     * 生成备份路径
     *
     * @param record 备份记录
     * @return 备份路径
     */
    private String generateBackupPath(IotBackupRecordDO record) {
        // 按日期分类：/data/iot-backup/backups/20250121/
        String dateDir = DateUtil.format(record.getStartTime(), "yyyyMMdd");
        String backupDir = backupStoragePath + File.separator + "backups" + File.separator + dateDir;

        // 创建目录
        FileUtil.mkdir(backupDir);

        // 备份路径：/data/iot-backup/backups/20250121/backup_{id}_{timestamp}
        String timestamp = DateUtil.format(record.getStartTime(), "HHmmss");
        return backupDir + File.separator + "backup_" + record.getId() + "_" + timestamp;
    }

}
