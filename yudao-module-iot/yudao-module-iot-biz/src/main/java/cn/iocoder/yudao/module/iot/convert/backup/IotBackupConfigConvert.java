package cn.iocoder.yudao.module.iot.convert.backup;

import cn.iocoder.yudao.module.iot.controller.admin.backup.vo.config.IotBackupConfigRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.backup.vo.config.IotBackupConfigSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.backup.IotBackupConfigDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * IoT TDengine 备份配置 Convert
 *
 * @author claude
 */
@Mapper
public interface IotBackupConfigConvert {

    IotBackupConfigConvert INSTANCE = Mappers.getMapper(IotBackupConfigConvert.class);

    IotBackupConfigDO convert(IotBackupConfigSaveReqVO bean);

    IotBackupConfigRespVO convert(IotBackupConfigDO bean);

    List<IotBackupConfigRespVO> convertList(List<IotBackupConfigDO> list);

}
