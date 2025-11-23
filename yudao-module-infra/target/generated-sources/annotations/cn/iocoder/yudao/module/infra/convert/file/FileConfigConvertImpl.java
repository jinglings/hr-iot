package cn.iocoder.yudao.module.infra.convert.file;

import cn.iocoder.yudao.module.infra.controller.admin.file.vo.config.FileConfigSaveReqVO;
import cn.iocoder.yudao.module.infra.dal.dataobject.file.FileConfigDO;
import javax.annotation.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-22T19:37:20+0800",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.2 (Oracle Corporation)"
)
public class FileConfigConvertImpl implements FileConfigConvert {

    @Override
    public FileConfigDO convert(FileConfigSaveReqVO bean) {
        if ( bean == null ) {
            return null;
        }

        FileConfigDO.FileConfigDOBuilder fileConfigDO = FileConfigDO.builder();

        fileConfigDO.id( bean.getId() );
        fileConfigDO.name( bean.getName() );
        fileConfigDO.storage( bean.getStorage() );
        fileConfigDO.remark( bean.getRemark() );

        return fileConfigDO.build();
    }
}
