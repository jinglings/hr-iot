package cn.iocoder.yudao.module.iot.convert.edge;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.edge.vo.aimodel.IotEdgeAiModelCreateReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.edge.vo.aimodel.IotEdgeAiModelRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.edge.vo.aimodel.IotEdgeAiModelUpdateReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.edge.IotEdgeAiModelDO;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-18T13:59:21+0800",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.2 (Oracle Corporation)"
)
public class IotEdgeAiModelConvertImpl implements IotEdgeAiModelConvert {

    @Override
    public IotEdgeAiModelDO convert(IotEdgeAiModelCreateReqVO bean) {
        if ( bean == null ) {
            return null;
        }

        IotEdgeAiModelDO.IotEdgeAiModelDOBuilder iotEdgeAiModelDO = IotEdgeAiModelDO.builder();

        iotEdgeAiModelDO.name( bean.getName() );
        iotEdgeAiModelDO.version( bean.getVersion() );
        iotEdgeAiModelDO.modelType( bean.getModelType() );
        iotEdgeAiModelDO.modelFormat( bean.getModelFormat() );
        iotEdgeAiModelDO.fileUrl( bean.getFileUrl() );
        iotEdgeAiModelDO.fileSize( bean.getFileSize() );
        iotEdgeAiModelDO.fileMd5( bean.getFileMd5() );
        iotEdgeAiModelDO.minMemory( bean.getMinMemory() );
        iotEdgeAiModelDO.minCpuCores( bean.getMinCpuCores() );
        iotEdgeAiModelDO.gpuRequired( bean.getGpuRequired() );
        iotEdgeAiModelDO.applicationScene( bean.getApplicationScene() );
        iotEdgeAiModelDO.inputFormat( bean.getInputFormat() );
        iotEdgeAiModelDO.outputFormat( bean.getOutputFormat() );
        iotEdgeAiModelDO.description( bean.getDescription() );

        return iotEdgeAiModelDO.build();
    }

    @Override
    public IotEdgeAiModelDO convert(IotEdgeAiModelUpdateReqVO bean) {
        if ( bean == null ) {
            return null;
        }

        IotEdgeAiModelDO.IotEdgeAiModelDOBuilder iotEdgeAiModelDO = IotEdgeAiModelDO.builder();

        iotEdgeAiModelDO.id( bean.getId() );
        iotEdgeAiModelDO.name( bean.getName() );
        iotEdgeAiModelDO.fileUrl( bean.getFileUrl() );
        iotEdgeAiModelDO.fileSize( bean.getFileSize() );
        iotEdgeAiModelDO.fileMd5( bean.getFileMd5() );
        iotEdgeAiModelDO.applicationScene( bean.getApplicationScene() );
        iotEdgeAiModelDO.inputFormat( bean.getInputFormat() );
        iotEdgeAiModelDO.outputFormat( bean.getOutputFormat() );
        iotEdgeAiModelDO.status( bean.getStatus() );
        iotEdgeAiModelDO.description( bean.getDescription() );

        return iotEdgeAiModelDO.build();
    }

    @Override
    public IotEdgeAiModelRespVO convert(IotEdgeAiModelDO bean) {
        if ( bean == null ) {
            return null;
        }

        IotEdgeAiModelRespVO iotEdgeAiModelRespVO = new IotEdgeAiModelRespVO();

        iotEdgeAiModelRespVO.setId( bean.getId() );
        iotEdgeAiModelRespVO.setName( bean.getName() );
        iotEdgeAiModelRespVO.setVersion( bean.getVersion() );
        iotEdgeAiModelRespVO.setModelType( bean.getModelType() );
        iotEdgeAiModelRespVO.setModelFormat( bean.getModelFormat() );
        iotEdgeAiModelRespVO.setFileUrl( bean.getFileUrl() );
        iotEdgeAiModelRespVO.setFileSize( bean.getFileSize() );
        iotEdgeAiModelRespVO.setFileMd5( bean.getFileMd5() );
        iotEdgeAiModelRespVO.setMinMemory( bean.getMinMemory() );
        iotEdgeAiModelRespVO.setMinCpuCores( bean.getMinCpuCores() );
        iotEdgeAiModelRespVO.setGpuRequired( bean.getGpuRequired() );
        iotEdgeAiModelRespVO.setApplicationScene( bean.getApplicationScene() );
        iotEdgeAiModelRespVO.setInputFormat( bean.getInputFormat() );
        iotEdgeAiModelRespVO.setOutputFormat( bean.getOutputFormat() );
        iotEdgeAiModelRespVO.setStatus( bean.getStatus() );
        iotEdgeAiModelRespVO.setDescription( bean.getDescription() );
        iotEdgeAiModelRespVO.setCreateTime( bean.getCreateTime() );

        return iotEdgeAiModelRespVO;
    }

    @Override
    public List<IotEdgeAiModelRespVO> convertList(List<IotEdgeAiModelDO> list) {
        if ( list == null ) {
            return null;
        }

        List<IotEdgeAiModelRespVO> list1 = new ArrayList<IotEdgeAiModelRespVO>( list.size() );
        for ( IotEdgeAiModelDO iotEdgeAiModelDO : list ) {
            list1.add( convert( iotEdgeAiModelDO ) );
        }

        return list1;
    }

    @Override
    public PageResult<IotEdgeAiModelRespVO> convertPage(PageResult<IotEdgeAiModelDO> page) {
        if ( page == null ) {
            return null;
        }

        PageResult<IotEdgeAiModelRespVO> pageResult = new PageResult<IotEdgeAiModelRespVO>();

        pageResult.setTotal( page.getTotal() );
        pageResult.setList( convertList( page.getList() ) );

        return pageResult;
    }
}
