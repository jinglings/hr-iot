package cn.iocoder.yudao.module.iot.convert.backup;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.backup.vo.restore.IotRestoreRecordCreateReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.backup.vo.restore.IotRestoreRecordRespVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.backup.IotRestoreRecordDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * IoT TDengine 恢复记录 Convert
 *
 * @author claude
 */
@Mapper
public interface IotRestoreRecordConvert {

    IotRestoreRecordConvert INSTANCE = Mappers.getMapper(IotRestoreRecordConvert.class);

    IotRestoreRecordDO convert(IotRestoreRecordCreateReqVO bean);

    IotRestoreRecordRespVO convert(IotRestoreRecordDO bean);

    List<IotRestoreRecordRespVO> convertList(List<IotRestoreRecordDO> list);

    PageResult<IotRestoreRecordRespVO> convertPage(PageResult<IotRestoreRecordDO> page);

}
