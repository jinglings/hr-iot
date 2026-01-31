package cn.iocoder.yudao.module.iot.service.backup;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ZipUtil;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.opencsv.CSVWriter;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.io.File;
import java.io.FileWriter;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * IoT TDengine 备份执行引擎
 *
 * 负责实际的备份和恢复操作：
 * - 导出TDengine数据到CSV文件
 * - 压缩备份文件
 * - 解压备份文件
 * - 生成和读取备份元数据
 *
 * @author claude
 */
@Service
@Slf4j
@DS("tdengine") // 使用TDengine数据源
public class IotBackupExecutor {

    @Resource
    private DataSource dataSource;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    /**
     * 执行全量备份
     *
     * @param backupPath 备份临时目录路径
     * @return 备份结果
     */
    public BackupResult executeFullBackup(String backupPath) {
        log.info("[executeFullBackup][开始全量备份] path={}", backupPath);

        try {
            // 1. 创建临时目录
            File tempDir = new File(backupPath);
            FileUtil.mkdir(tempDir);

            // 2. 获取所有数据库
            List<String> databases = queryDatabases();
            log.info("[executeFullBackup][查询到数据库] count={}", databases.size());

            long totalRecordCount = 0;
            List<BackupTableInfo> tableInfos = new ArrayList<>();

            // 3. 遍历每个数据库
            for (String db : databases) {
                // 创建数据库目录
                String dbDir = backupPath + File.separator + db;
                FileUtil.mkdir(dbDir);

                // 获取数据库中的所有超级表
                List<String> stables = queryStables(db);
                log.info("[executeFullBackup][数据库 {} 的超级表] count={}", db, stables.size());

                // 导出每个超级表的数据
                for (String stable : stables) {
                    long count = exportStableData(db, stable, dbDir);
                    totalRecordCount += count;

                    // 记录表信息
                    tableInfos.add(BackupTableInfo.builder()
                            .database(db)
                            .stable(stable)
                            .recordCount(count)
                            .fileName(stable + ".csv")
                            .build());
                }
            }

            // 4. 创建元数据文件
            BackupMetadata metadata = BackupMetadata.builder()
                    .backupTime(LocalDateTime.now())
                    .databases(databases)
                    .tables(tableInfos)
                    .totalRecordCount(totalRecordCount)
                    .build();
            saveMetadata(backupPath, metadata);

            // 5. 压缩备份文件
            String zipPath = compressBackup(backupPath);
            long fileSize = FileUtil.size(new File(zipPath));

            // 6. 清理临时目录
            FileUtil.del(tempDir);

            log.info("[executeFullBackup][全量备份完成] zipPath={}, size={}, records={}",
                    zipPath, fileSize, totalRecordCount);

            return BackupResult.builder()
                    .filePath(zipPath)
                    .fileSize(fileSize)
                    .recordCount(totalRecordCount)
                    .success(true)
                    .build();

        } catch (Exception e) {
            log.error("[executeFullBackup][全量备份失败]", e);
            throw new ServiceException(500, "备份失败：" + e.getMessage());
        }
    }

    /**
     * 执行数据库备份
     *
     * @param backupPath 备份临时目录路径
     * @param databases 要备份的数据库列表
     * @return 备份结果
     */
    public BackupResult executeDatabaseBackup(String backupPath, List<String> databases) {
        log.info("[executeDatabaseBackup][开始数据库备份] path={}, databases={}", backupPath, databases);

        try {
            File tempDir = new File(backupPath);
            FileUtil.mkdir(tempDir);

            long totalRecordCount = 0;
            List<BackupTableInfo> tableInfos = new ArrayList<>();

            for (String db : databases) {
                String dbDir = backupPath + File.separator + db;
                FileUtil.mkdir(dbDir);

                List<String> stables = queryStables(db);
                for (String stable : stables) {
                    long count = exportStableData(db, stable, dbDir);
                    totalRecordCount += count;

                    tableInfos.add(BackupTableInfo.builder()
                            .database(db)
                            .stable(stable)
                            .recordCount(count)
                            .fileName(stable + ".csv")
                            .build());
                }
            }

            BackupMetadata metadata = BackupMetadata.builder()
                    .backupTime(LocalDateTime.now())
                    .databases(databases)
                    .tables(tableInfos)
                    .totalRecordCount(totalRecordCount)
                    .build();
            saveMetadata(backupPath, metadata);

            String zipPath = compressBackup(backupPath);
            long fileSize = FileUtil.size(new File(zipPath));
            FileUtil.del(tempDir);

            log.info("[executeDatabaseBackup][数据库备份完成] zipPath={}, size={}, records={}",
                    zipPath, fileSize, totalRecordCount);

            return BackupResult.builder()
                    .filePath(zipPath)
                    .fileSize(fileSize)
                    .recordCount(totalRecordCount)
                    .success(true)
                    .build();

        } catch (Exception e) {
            log.error("[executeDatabaseBackup][数据库备份失败]", e);
            throw new ServiceException(500, "备份失败：" + e.getMessage());
        }
    }

    /**
     * 导出超级表数据到CSV文件
     *
     * @param database 数据库名
     * @param stable 超级表名
     * @param outputDir 输出目录
     * @return 导出的记录数
     */
    private long exportStableData(String database, String stable, String outputDir) {
        String sql = String.format("SELECT * FROM %s.%s", database, stable);
        String csvFile = outputDir + File.separator + stable + ".csv";

        log.info("[exportStableData][开始导出] db={}, stable={}", database, stable);

        long recordCount = 0;

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql);
             CSVWriter writer = new CSVWriter(new FileWriter(csvFile))) {

            // 写入表头
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            String[] headers = new String[columnCount];
            for (int i = 0; i < columnCount; i++) {
                headers[i] = metaData.getColumnName(i + 1);
            }
            writer.writeNext(headers);

            // 写入数据（分批刷新，避免内存溢出）
            while (rs.next()) {
                String[] row = new String[columnCount];
                for (int i = 0; i < columnCount; i++) {
                    Object value = rs.getObject(i + 1);
                    row[i] = value != null ? value.toString() : "";
                }
                writer.writeNext(row);
                recordCount++;

                // 每10000条记录刷新一次
                if (recordCount % 10000 == 0) {
                    writer.flush();
                }
            }

            log.info("[exportStableData][导出完成] db={}, stable={}, records={}",
                    database, stable, recordCount);

        } catch (Exception e) {
            log.error("[exportStableData][导出失败] db={}, stable={}", database, stable, e);
            throw new ServiceException(500, "导出数据失败：" + e.getMessage());
        }

        return recordCount;
    }

    /**
     * 查询所有数据库
     *
     * @return 数据库列表
     */
    private List<String> queryDatabases() {
        List<String> databases = new ArrayList<>();
        String sql = "SHOW DATABASES";

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String dbName = rs.getString(1);
                // 过滤系统数据库
                if (!"information_schema".equals(dbName) &&
                    !"performance_schema".equals(dbName) &&
                    !"log".equals(dbName)) {
                    databases.add(dbName);
                }
            }

        } catch (Exception e) {
            log.error("[queryDatabases][查询数据库失败]", e);
            throw new ServiceException(500, "查询数据库失败：" + e.getMessage());
        }

        return databases;
    }

    /**
     * 查询指定数据库的所有超级表
     *
     * @param database 数据库名
     * @return 超级表列表
     */
    private List<String> queryStables(String database) {
        List<String> stables = new ArrayList<>();
        String sql = String.format("SHOW %s.STABLES", database);

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                stables.add(rs.getString(1));
            }

        } catch (Exception e) {
            log.error("[queryStables][查询超级表失败] db={}", database, e);
            throw new ServiceException(500, "查询超级表失败：" + e.getMessage());
        }

        return stables;
    }

    /**
     * 保存备份元数据
     *
     * @param backupPath 备份目录
     * @param metadata 元数据
     */
    private void saveMetadata(String backupPath, BackupMetadata metadata) {
        String metadataFile = backupPath + File.separator + "metadata.json";
        try {
            String json = JSONUtil.toJsonPrettyStr(metadata);
            FileUtil.writeUtf8String(json, metadataFile);
            log.info("[saveMetadata][元数据已保存] file={}", metadataFile);
        } catch (Exception e) {
            log.error("[saveMetadata][保存元数据失败]", e);
            throw new ServiceException(500, "保存元数据失败：" + e.getMessage());
        }
    }

    /**
     * 压缩备份文件
     *
     * @param sourcePath 源目录路径
     * @return 压缩文件路径
     */
    private String compressBackup(String sourcePath) {
        String zipPath = sourcePath + ".zip";

        try {
            ZipUtil.zip(sourcePath, zipPath);
            log.info("[compressBackup][压缩完成] zipPath={}", zipPath);
        } catch (Exception e) {
            log.error("[compressBackup][压缩失败]", e);
            throw new ServiceException(500, "压缩备份文件失败：" + e.getMessage());
        }

        return zipPath;
    }

    /**
     * 解压备份文件
     *
     * @param zipPath 压缩文件路径
     * @return 解压目录路径
     */
    public String decompressBackup(String zipPath) {
        String extractPath = zipPath.replace(".zip", "_extract");

        try {
            ZipUtil.unzip(zipPath, extractPath);
            log.info("[decompressBackup][解压完成] extractPath={}", extractPath);
        } catch (Exception e) {
            log.error("[decompressBackup][解压失败]", e);
            throw new ServiceException(500, "解压备份文件失败：" + e.getMessage());
        }

        return extractPath;
    }

    /**
     * 读取备份元数据
     *
     * @param extractPath 解压目录
     * @return 备份元数据
     */
    public BackupMetadata readMetadata(String extractPath) {
        String metadataFile = extractPath + File.separator + "metadata.json";

        try {
            String json = FileUtil.readUtf8String(metadataFile);
            BackupMetadata metadata = JSONUtil.toBean(json, BackupMetadata.class);
            log.info("[readMetadata][元数据已读取] file={}", metadataFile);
            return metadata;
        } catch (Exception e) {
            log.error("[readMetadata][读取元数据失败]", e);
            throw new ServiceException(500, "读取元数据失败：" + e.getMessage());
        }
    }

    /**
     * 备份结果
     */
    @Data
    @Builder
    public static class BackupResult {
        /** 备份文件路径 */
        private String filePath;
        /** 文件大小（字节） */
        private Long fileSize;
        /** 备份记录数 */
        private Long recordCount;
        /** 是否成功 */
        private Boolean success;
        /** 错误信息 */
        private String errorMessage;
    }

    /**
     * 备份元数据
     */
    @Data
    @Builder
    public static class BackupMetadata {
        /** 备份时间 */
        private LocalDateTime backupTime;
        /** 数据库列表 */
        private List<String> databases;
        /** 表信息列表 */
        private List<BackupTableInfo> tables;
        /** 总记录数 */
        private Long totalRecordCount;
    }

    /**
     * 备份表信息
     */
    @Data
    @Builder
    public static class BackupTableInfo {
        /** 数据库名 */
        private String database;
        /** 超级表名 */
        private String stable;
        /** 记录数 */
        private Long recordCount;
        /** 文件名 */
        private String fileName;
    }

}
