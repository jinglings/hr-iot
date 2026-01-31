package cn.iocoder.yudao.module.iot.convert.backup;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.backup.vo.record.IotBackupRecordCreateReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.backup.vo.record.IotBackupRecordRespVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.backup.IotBackupRecordDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * IoT TDengine 备份记录 Convert
 *
 * @author claude
 */
@Mapper
public interface IotBackupRecordConvert {

    IotBackupRecordConvert INSTANCE = Mappers.getMapper(IotBackupRecordConvert.class);

    IotBackupRecordDO convert(IotBackupRecordCreateReqVO bean);

    IotBackupRecordRespVO convert(IotBackupRecordDO bean);

    List<IotBackupRecordRespVO> convertList(List<IotBackupRecordDO> list);

    PageResult<IotBackupRecordRespVO> convertPage(PageResult<IotBackupRecordDO> page);

}
