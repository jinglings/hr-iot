package cn.iocoder.yudao.module.iot.dal.mysql.backup;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.dal.dataobject.backup.IotBackupConfigDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * IoT TDengine 备份配置 Mapper
 *
 * @author claude
 */
@Mapper
public interface IotBackupConfigMapper extends BaseMapperX<IotBackupConfigDO> {

    /**
     * 查询所有启用的备份配置
     *
     * @return 备份配置列表
     */
    default List<IotBackupConfigDO> selectEnabledList() {
        return selectList(IotBackupConfigDO::getEnabled, true);
    }

    /**
     * 根据配置名称查询
     *
     * @param configName 配置名称
     * @return 备份配置
     */
    default IotBackupConfigDO selectByConfigName(String configName) {
        return selectOne(IotBackupConfigDO::getConfigName, configName);
    }

    /**
     * 查询所有备份配置列表
     *
     * @return 备份配置列表
     */
    default List<IotBackupConfigDO> selectAllList() {
        return selectList(new LambdaQueryWrapperX<IotBackupConfigDO>()
                .orderByDesc(IotBackupConfigDO::getId));
    }

}
