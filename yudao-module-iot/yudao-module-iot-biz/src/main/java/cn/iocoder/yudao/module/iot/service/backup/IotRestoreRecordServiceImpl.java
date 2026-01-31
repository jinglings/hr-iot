package cn.iocoder.yudao.module.iot.service.backup;

import cn.hutool.core.io.FileUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.backup.vo.restore.IotRestoreRecordCreateReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.backup.vo.restore.IotRestoreRecordPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.backup.vo.restore.IotRestoreRecordRespVO;
import cn.iocoder.yudao.module.iot.convert.backup.IotRestoreRecordConvert;
import cn.iocoder.yudao.module.iot.dal.dataobject.backup.IotBackupRecordDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.backup.IotRestoreRecordDO;
import cn.iocoder.yudao.module.iot.dal.mysql.backup.IotBackupRecordMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.backup.IotRestoreRecordMapper;
import cn.iocoder.yudao.module.iot.enums.backup.RestoreStatusEnum;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.opencsv.CSVReader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.Statement;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.BACKUP_FILE_NOT_EXISTS;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.BACKUP_RECORD_NOT_EXISTS;

/**
 * IoT TDengine 恢复记录 Service 实现类
 *
 * @author claude
 */
@Service
@Slf4j
@DS("tdengine") // 使用TDengine数据源
public class IotRestoreRecordServiceImpl implements IotRestoreRecordService {

    @Resource
    private IotRestoreRecordMapper restoreRecordMapper;

    @Resource
    private IotBackupRecordMapper backupRecordMapper;

    @Resource
    private IotBackupExecutor backupExecutor;

    @Resource
    private DataSource dataSource;

    @Override
    @Async("backupExecutor")
    @Transactional(rollbackFor = Exception.class)
    public Long executeRestore(IotRestoreRecordCreateReqVO createReqVO) {
        log.info("[executeRestore][开始恢复数据] req={}", createReqVO);

        // 1. 校验备份记录存在
        IotBackupRecordDO backup = backupRecordMapper.selectById(createReqVO.getBackupId());
        if (backup == null) {
            throw exception(BACKUP_RECORD_NOT_EXISTS);
        }

        // 2. 校验备份文件存在
        if (backup.getFilePath() == null || !FileUtil.exist(backup.getFilePath())) {
            throw exception(BACKUP_FILE_NOT_EXISTS);
        }

        // 3. 创建恢复记录
        IotRestoreRecordDO record = IotRestoreRecordConvert.INSTANCE.convert(createReqVO);
        record.setRestoreStatus(RestoreStatusEnum.IN_PROGRESS.getStatus());
        record.setStartTime(LocalDateTime.now());
        restoreRecordMapper.insert(record);
        Long recordId = record.getId();

        String extractPath = null;

        try {
            // 4. 解压备份文件
            extractPath = backupExecutor.decompressBackup(backup.getFilePath());

            // 5. 读取元数据
            IotBackupExecutor.BackupMetadata metadata = backupExecutor.readMetadata(extractPath);

            // 6. 执行数据导入
            long totalRecordCount = importData(extractPath, metadata, createReqVO);

            // 7. 更新恢复记录为成功
            record.setRestoreStatus(RestoreStatusEnum.SUCCESS.getStatus());
            record.setEndTime(LocalDateTime.now());
            record.setDuration((int) Duration.between(record.getStartTime(), record.getEndTime()).getSeconds());
            record.setRecordCount(totalRecordCount);
            restoreRecordMapper.updateById(record);

            log.info("[executeRestore][恢复成功] recordId={}, records={}", recordId, totalRecordCount);

            return recordId;

        } catch (Exception e) {
            log.error("[executeRestore][恢复失败] recordId={}", recordId, e);

            // 更新恢复记录为失败状态
            record.setRestoreStatus(RestoreStatusEnum.FAILED.getStatus());
            record.setEndTime(LocalDateTime.now());
            record.setDuration((int) Duration.between(record.getStartTime(), record.getEndTime()).getSeconds());
            record.setErrorMessage(e.getMessage());
            restoreRecordMapper.updateById(record);

            throw new ServiceException(500, "恢复失败：" + e.getMessage());

        } finally {
            // 清理临时文件
            if (extractPath != null) {
                FileUtil.del(extractPath);
            }
        }
    }

    @Override
    public IotRestoreRecordDO getRestoreRecord(Long id) {
        return restoreRecordMapper.selectById(id);
    }

    @Override
    public PageResult<IotRestoreRecordRespVO> getRestoreRecordPage(IotRestoreRecordPageReqVO pageReqVO) {
        PageResult<IotRestoreRecordDO> pageResult = restoreRecordMapper.selectPage(pageReqVO);

        // 填充备份名称
        PageResult<IotRestoreRecordRespVO> result = IotRestoreRecordConvert.INSTANCE.convertPage(pageResult);
        result.getList().forEach(vo -> {
            IotBackupRecordDO backup = backupRecordMapper.selectById(vo.getBackupId());
            if (backup != null) {
                vo.setBackupName(backup.getBackupName());
            }
        });

        return result;
    }

    /**
     * 导入数据
     *
     * @param extractPath 解压目录
     * @param metadata 备份元数据
     * @param createReqVO 恢复请求
     * @return 导入的记录数
     */
    private long importData(String extractPath, IotBackupExecutor.BackupMetadata metadata,
                           IotRestoreRecordCreateReqVO createReqVO) {
        long totalCount = 0;

        for (IotBackupExecutor.BackupTableInfo tableInfo : metadata.getTables()) {
            // 检查是否需要恢复该表
            if (!shouldRestoreTable(tableInfo, createReqVO)) {
                log.info("[importData][跳过表] db={}, table={}", tableInfo.getDatabase(), tableInfo.getStable());
                continue;
            }

            log.info("[importData][开始导入] db={}, table={}", tableInfo.getDatabase(), tableInfo.getStable());

            // CSV文件路径
            String csvFile = extractPath + "/" + tableInfo.getDatabase() + "/" + tableInfo.getFileName();

            try (CSVReader reader = new CSVReader(new FileReader(csvFile))) {
                // 读取表头
                String[] headers = reader.readNext();
                if (headers == null) {
                    log.warn("[importData][CSV文件为空] file={}", csvFile);
                    continue;
                }

                // 批量插入数据
                List<String[]> batch = new ArrayList<>();
                String[] row;
                while ((row = reader.readNext()) != null) {
                    batch.add(row);

                    // 每1000条记录插入一次
                    if (batch.size() >= 1000) {
                        batchInsert(tableInfo, headers, batch);
                        totalCount += batch.size();
                        batch.clear();
                    }
                }

                // 插入剩余数据
                if (!batch.isEmpty()) {
                    batchInsert(tableInfo, headers, batch);
                    totalCount += batch.size();
                }

                log.info("[importData][导入完成] db={}, table={}, records={}",
                        tableInfo.getDatabase(), tableInfo.getStable(), totalCount);

            } catch (Exception e) {
                log.error("[importData][导入失败] table={}", tableInfo.getStable(), e);
                throw new ServiceException(500, "导入数据失败：" + e.getMessage());
            }
        }

        return totalCount;
    }

    /**
     * 判断是否需要恢复该表
     *
     * @param tableInfo 表信息
     * @param createReqVO 恢复请求
     * @return 是否恢复
     */
    private boolean shouldRestoreTable(IotBackupExecutor.BackupTableInfo tableInfo,
                                      IotRestoreRecordCreateReqVO createReqVO) {
        // 完整恢复：恢复所有表
        if (createReqVO.getRestoreType() == 1) {
            return true;
        }

        // 选择性恢复：检查是否在恢复目标中
        if (createReqVO.getRestoreTarget() != null) {
            return createReqVO.getRestoreTarget().contains(tableInfo.getStable());
        }

        return true;
    }

    /**
     * 批量插入数据
     *
     * @param tableInfo 表信息
     * @param headers 表头
     * @param batch 批量数据
     */
    private void batchInsert(IotBackupExecutor.BackupTableInfo tableInfo,
                            String[] headers, List<String[]> batch) {
        // 构建批量插入SQL
        StringBuilder sql = new StringBuilder();
        sql.append(String.format("INSERT INTO %s.%s (", tableInfo.getDatabase(), tableInfo.getStable()));

        // 添加列名
        for (int i = 0; i < headers.length; i++) {
            if (i > 0) sql.append(",");
            sql.append(headers[i]);
        }
        sql.append(") VALUES ");

        // 添加数据行
        for (int i = 0; i < batch.size(); i++) {
            if (i > 0) sql.append(",");
            sql.append("(");

            String[] row = batch.get(i);
            for (int j = 0; j < row.length; j++) {
                if (j > 0) sql.append(",");

                // 处理不同数据类型
                String value = row[j];
                if (value == null || value.isEmpty() || "null".equals(value)) {
                    sql.append("NULL");
                } else if (isNumeric(value)) {
                    sql.append(value);
                } else {
                    // 字符串需要转义
                    sql.append("'").append(value.replace("'", "''")).append("'");
                }
            }
            sql.append(")");
        }

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql.toString());
            log.debug("[batchInsert][批量插入成功] table={}, count={}", tableInfo.getStable(), batch.size());
        } catch (Exception e) {
            log.error("[batchInsert][批量插入失败] table={}", tableInfo.getStable(), e);
            throw new ServiceException(500, "批量插入失败：" + e.getMessage());
        }
    }

    /**
     * 判断字符串是否为数字
     *
     * @param str 字符串
     * @return 是否为数字
     */
    private boolean isNumeric(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

}
