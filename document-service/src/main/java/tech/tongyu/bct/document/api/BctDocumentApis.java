package tech.tongyu.bct.document.api;

import com.google.common.io.CharStreams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tech.tongyu.bct.common.api.annotation.BctMethodArg;
import tech.tongyu.bct.common.api.annotation.BctMethodInfo;
import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.common.util.FileUtils;
import tech.tongyu.bct.document.dto.DocTypeEnum;
import tech.tongyu.bct.document.dto.TemplateDTO;
import tech.tongyu.bct.document.dto.TemplateDirectoryDTO;
import tech.tongyu.bct.document.ext.dto.BctTemplateDTO;
import tech.tongyu.bct.document.ext.dto.CategoryEnum;
import tech.tongyu.bct.document.ext.service.BctDocumentService;
import tech.tongyu.bct.document.poi.PoiTemplateDTO;
import tech.tongyu.bct.document.poi.TradeTypeEnum;
import tech.tongyu.bct.document.service.DocumentService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

@Service
public class BctDocumentApis {

    @Value("${spring.upload.location}")
    private String FILE_PATH;

    private final BctDocumentService bctDocumentService;
    private final DocumentService documentService;

    @Autowired
    public BctDocumentApis(BctDocumentService bctDocumentService, DocumentService documentService) {
        this.bctDocumentService = bctDocumentService;
        this.documentService = documentService;
    }

    @Transactional
    @BctMethodInfo(
            description = "创建bct模板",
            retDescription = "bct模板",
            retName = "BctTemplateDTO",
            returnClass = BctTemplateDTO.class,
            service = "document-service"
    )
    public BctTemplateDTO docBctTemplateCreate(
            @BctMethodArg(name = "directoryId", description = "目录ID") String directoryId,
            @BctMethodArg(name = "category", description = "类别. enum value: 交易文档(TRADE_TEMPLATE)/客户文档(CLIENT_TEMPLATE)") String category,
            @BctMethodArg(name = "transactType", description = "交易类型. enum value:EUROPE, AMERICA", required = false) String transactType,
            @BctMethodArg(name = "docType", description = "文档类型.") String docType,
            @BctMethodArg(name = "fileType", description = "文件类型. value:UNKNOWN, WORD_2003, EXCEL_2003") String fileType,
            @BctMethodArg(name = "typeSuffix", description = "类型后缀. e.g. doc, xls, pdf etc") String typeSuffix,
            @BctMethodArg(name = "groupName", description = "字典组名称") String groupName,
            @BctMethodArg(name = "description", description = "描述", required = false) String description,
            @BctMethodArg(name = "createdBy", description = "创建人", required = false) String createdBy
    ) {
        UUID dirUuid = UUID.fromString(directoryId);

        BctTemplateDTO bctTemplateDTO = new BctTemplateDTO(dirUuid, CategoryEnum.valueOf(category), transactType,
                tech.tongyu.bct.document.ext.dto.DocTypeEnum.valueOf(docType), fileType, typeSuffix, description, createdBy);
        bctTemplateDTO.setGroupName(groupName);

        return bctDocumentService.docTemplateCreate(bctTemplateDTO);
    }

    @Transactional
    @BctMethodInfo(
            description = "创建poi模板",
            retDescription = "bct模板",
            retName = "PoiTemplateDTO",
            returnClass = BctTemplateDTO.class,
            service = "document-service"
    )
    public PoiTemplateDTO docPoiTemplateCreate(
            @BctMethodArg(name = "tradeType", description = "交易类型") String tradeType,
            @BctMethodArg(name = "docType", description = "文档类型") String docType) {

        return bctDocumentService.docPoiTemplateCreate(TradeTypeEnum.valueOf(tradeType), tech.tongyu.bct.document.ext.dto.DocTypeEnum.valueOf(docType));
    }

    @BctMethodInfo(
            description = "根据交易类别查询模板",
            retDescription = "bct模板列表",
            retName = "the list of docBctTemplate",
            returnClass = BctTemplateDTO.class,
            service = "document-service"
    )
    public List<BctTemplateDTO> docBctTemplateList(
            @BctMethodArg(description = "类别.enum value: 交易模板(TRADE_TEMPLATE)/客户模板(CLIENT_TEMPLATE)") String category
    ) {
        return bctDocumentService.docBctTemplateList(CategoryEnum.valueOf(category));
    }

    @BctMethodInfo(
            description = "查询poi模板",
            retDescription = "poi模板列表",
            retName = "the list of PoiTemplateDTO",
            returnClass = PoiTemplateDTO.class,
            service = "document-service"
    )
    public List<PoiTemplateDTO> docPoiTemplateList() {
        return bctDocumentService.docPoiTemplateList();
    }

    @Transactional
    @BctMethodInfo(
            description = "创建或更新bct模板",
            retDescription = "bct模板",
            retName = "bctTemplateDTO",
            returnClass = BctTemplateDTO.class,
            service = "document-service"
    )
    public BctTemplateDTO docBctTemplateCreateOrUpdate(
            @BctMethodArg(name = "uuid", description = "bct模板ID") String uuid,
            @BctMethodArg(name = "file", description = "文件") MultipartFile file
    ) {
        UUID bctTemplateId = UUID.fromString(uuid);

        BctTemplateDTO bctTemplate = bctDocumentService.getBctTemplateDTO(bctTemplateId).orElseThrow(() -> new IllegalArgumentException("invalid uuid"));

        if (bctTemplate.getFileName() != null) {
            List<TemplateDTO> templates = documentService.getDirectory(bctTemplate.getDirectoryId())
                    .orElseThrow(() -> new IllegalArgumentException("invalid uuid")).getTemplates();
            templates.forEach(templateDTO -> documentService.deleteTemplate(templateDTO.getUuid()));
        }

        TemplateDTO template = new TemplateDTO(bctTemplate.getDirectoryId(), file.getOriginalFilename(), DocTypeEnum.valueOf(bctTemplate.getFileType()), bctTemplate.getTypeSuffix());
        template.setCreatedBy(template.getCreatedBy());
        template.setFileName(file.getOriginalFilename());
        template.setDescription(template.getDescription());
        template.setEnabled(true);

        String templateContent;
        try {
            // reading all content of file will consume much memory.
            templateContent = CharStreams.toString(new InputStreamReader(file.getInputStream()));
        } catch (IOException e) {
            throw new CustomException("failed to read file as string");
        }

        documentService.createTemplate(template, templateContent);

        bctTemplate.setFileName(file.getOriginalFilename());
        return bctDocumentService.updateBctTemplateDTO(bctTemplate);
    }

    @Transactional
    @BctMethodInfo(
            description = "创建或更新poi模板",
            retDescription = "poi模板",
            retName = "poiTemplateDTO",
            returnClass = PoiTemplateDTO.class,
            service = "document-service"
    )
    public PoiTemplateDTO docPoiTemplateCreateOrUpdate(
            @BctMethodArg(name = "uuid", description = "bct模板ID") String uuid,
            @BctMethodArg(name = "file", description = "文件") MultipartFile file){
        UUID poiTemplateId = UUID.fromString(uuid);

        String suffix = file.getOriginalFilename().split("\\.")[1];

        if (!suffix.equals("docx") && !suffix.equals("doc")){
            throw new CustomException("只支持docx和doc格式");
        }

        PoiTemplateDTO poiTemplateDTO = bctDocumentService.getPoiTemplateDTO(poiTemplateId).orElseThrow(() -> new IllegalArgumentException("invalid uuid"));

        String path = FILE_PATH + File.separator + UUID.randomUUID() + "." + suffix;
        try {
            if(!new File(path).exists()) {
                FileUtils.createFile(path);
            }
            FileOutputStream writer = new FileOutputStream(path);
            writer.write(file.getBytes());
        } catch (Exception e) {
            throw new CustomException("文件上传失败");
        }

        poiTemplateDTO.setFileName(file.getOriginalFilename());
        poiTemplateDTO.setTypeSuffix(suffix);
        poiTemplateDTO.setFilePath(path);
        return bctDocumentService.updatePoiTemplateDTO(poiTemplateDTO);
    }

    @Transactional
    @BctMethodInfo(
            description = "删除bct文档模板",
            retDescription = "被删除的文档模板ID",
            retName = "the id of deleted one",
            service = "document-service"
    )
    public UUID docBctTemplateDelete(
            @BctMethodArg(name = "uuid", description = "需要删除的模板ID") String uuid
    ) {

        UUID bctTemplateId = UUID.fromString(uuid);

        BctTemplateDTO bctTemplate = bctDocumentService.getBctTemplateDTO(bctTemplateId).orElseThrow(() -> new IllegalArgumentException("invalid uuid"));

        List<TemplateDTO> templates = documentService.getDirectory(bctTemplate.getDirectoryId())
                .orElseThrow(() -> new IllegalArgumentException("invalid uuid")).getTemplates();

        templates.forEach(templateDTO -> documentService.deleteTemplate(templateDTO.getUuid()));

        bctTemplate.setFileName(null);
        bctDocumentService.updateBctTemplateDTO(bctTemplate);

        return bctTemplateId;
    }

    @Transactional
    @BctMethodInfo(
            description = "删除poi文档模板",
            retDescription = "被删除的文档模板ID",
            retName = "the id of deleted one",
            service = "document-service"
    )
    public String docPoiTemplateDelete(
            @BctMethodArg(name = "uuid", description = "需要删除的模板ID") String uuid
    ){
        UUID poiTemplateId = UUID.fromString(uuid);
        return bctDocumentService.deletePoiTemplate(poiTemplateId);
    }

    @Transactional
    @BctMethodInfo(
            description = "创建或更新客户文档",
            retDescription = "模板文件夹",
            retName = "templateDirectoryDTO",
            returnClass = TemplateDirectoryDTO.class,
            service = "document-service"
    )
    public TemplateDirectoryDTO partyDocCreateOrUpdate(
            @BctMethodArg(name = "uuid", description = "客户doc模板目录ID", required = false) String uuid,
            @BctMethodArg(name = "name", description = "客户doc模板目录名") String name,
            @BctMethodArg(name = "file", description = "文件") MultipartFile file
    ) {
        TemplateDirectoryDTO directoryDTO = new TemplateDirectoryDTO();
        directoryDTO.setName(name);
        if (Objects.isNull(uuid)) {
            directoryDTO = documentService.createDirectory(directoryDTO);
        } else {
            directoryDTO.setUuid(UUID.fromString(uuid));
            directoryDTO = documentService.updateDirectory(directoryDTO);
        }

        TemplateDTO template = new TemplateDTO();

        template.setDirectoryId(directoryDTO.getUuid());
        template.setFileName(file.getOriginalFilename());
        template.setEnabled(true);
        template.setName(name);
        template.setType(DocTypeEnum.UNKNOWN);
        template.setTypeSuffix(" ");

        byte[] data;
        try {
            // reading all content of file will consume much memory.
            data = file.getBytes();
        } catch (IOException e) {
            throw new CustomException("failed to read file as byte");
        }

        if (directoryDTO.getTemplates().isEmpty()) {
            List<TemplateDTO> templates = directoryDTO.getTemplates();
            templates.forEach(templateDTO -> documentService.deleteTemplate(templateDTO.getUuid()));
        }

        template = documentService.createTemplateWithByte(template, data);

        directoryDTO.setTemplates(Collections.singletonList(template));

        return directoryDTO;
    }

    @Transactional
    @BctMethodInfo(
            description = "删除客户文档",
            retDescription = "被删除的文档ID",
            retName = "the id of deleted one",
            service = "document-service"
    )
    public UUID partyDocDelete(
            @BctMethodArg(description = "需要删除的文件ID") String uuid
    ) {

        UUID docId = UUID.fromString(uuid);

        List<TemplateDTO> templates = documentService.getDirectory(docId).orElseThrow(() -> new IllegalArgumentException("invalid uuid")).getTemplates();
        templates.forEach(templateDTO -> documentService.deleteTemplate(templateDTO.getUuid()));

        return docId;
    }

    @BctMethodInfo(
            description = "根据文件ID查找客户文档",
            retDescription = "客户文档",
            retName = "templateDirectoryDTO",
            returnClass = TemplateDirectoryDTO.class,
            service = "document-service"
    )
    public TemplateDirectoryDTO getPartyDoc(
            @BctMethodArg(description = "文件ID") String uuid
    ) {
        return documentService.getDirectory(UUID.fromString(uuid)).orElseThrow(() -> new IllegalArgumentException("invalid uuid"));
    }

}
