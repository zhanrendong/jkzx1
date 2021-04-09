package tech.tongyu.bct.document.api;

import com.google.common.io.CharStreams;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tech.tongyu.bct.common.api.annotation.BctMethodArg;
import tech.tongyu.bct.common.api.annotation.BctMethodInfo;
import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.document.dto.*;
import tech.tongyu.bct.document.service.DocumentService;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

/**
 * Apis for document generation.
 * @see <a href="http://confluence.tongyu.tech:8090/pages/viewpage.action?pageId=14910603">the design of document generation. </a>
 */
@Service
public class DocumentApis {
    private final DocumentService documentService;

    @Autowired
    public DocumentApis(DocumentService documentService) {
        this.documentService = documentService;
    }

    @Transactional
    @BctMethodInfo(
            description = "创建模板文件夹",
            retDescription = "模板文件夹",
            retName = "templateDirectoryDTO",
            returnClass = TemplateDirectoryDTO.class,
            service = "document-service"
    )
    public TemplateDirectoryDTO docTemplateDirectoryCreate(
            @BctMethodArg(description = "文件夹名称") String name,
            @BctMethodArg(description = "标签") List<String> tags,
            @BctMethodArg(description = "描述") String description,
            @BctMethodArg(description = "创建人") String createdBy
    ) {
        if (StringUtils.isEmpty(name)) {
            throw new IllegalArgumentException("name is mandatory");
        }

        TemplateDirectoryDTO dto = new TemplateDirectoryDTO(name, new HashSet<>(tags), description);
        dto.setCreatedBy(createdBy);

        return documentService.createDirectory(dto);
    }

    @Transactional
    @BctMethodInfo(
            description = "删除模板文件夹",
            retDescription = "被删除的文件夹ID",
            retName = "the id of deleted directory",
            service = "document-service"
    )
    public UUID docTemplateDirectoryDelete(
            @BctMethodArg(description = "需要删除的文件夹ID") String id
    ) {

        UUID uuid = UUID.fromString(id);
        documentService.deleteDirectory(uuid);
        return uuid;
    }

    @BctMethodInfo(
            description = "根据标记查询模板文件夹",
            retDescription = "模板文件夹列表",
            returnClass = TemplateDirectoryDTO.class,
            retName = "the list of template directory.",
            service = "document-service"
    )
    public List<TemplateDirectoryDTO> docTemplateDirectoryList(
            @BctMethodArg(description = "标记列表") List<String> tags
    ) {
        if (null == tags || tags.size() < 1) {
            throw new IllegalArgumentException("tags is required");
        }

        return documentService.getDirectories(tags);
    }

    @BctMethodInfo(
            description = "根据ID获取模板文件夹",
            retDescription = "模板文件夹列表",
            returnClass = TemplateDirectoryDTO.class,
            retName = "the list of template directory.",
            service = "document-service"
    )
    public TemplateDirectoryDTO docTemplateDirectoryGet(
            @BctMethodArg(description = "文件夹ID") String id
    ) {

        UUID uuid = UUID.fromString(id);
        return documentService.getDirectory(uuid).orElseThrow(() -> new IllegalArgumentException("invalid uuid"));
    }

    @Transactional
    @BctMethodInfo(
            description = "更新模板文件夹",
            retDescription = "模板文件夹",
            returnClass = TemplateDirectoryDTO.class,
            retName = "TemplateDirectoryDTO",
            service = "document-service"
    )
    public TemplateDirectoryDTO docTemplateDirectoryUpdate(
            @BctMethodArg(description = "模板文件夹ID") String id,
            @BctMethodArg(description = "文件夹名称") String name,
            @BctMethodArg(description = "标识") List<String> tags,
            @BctMethodArg(description = "描述") String description
    ) {
        UUID uuid = UUID.fromString(id);
        TemplateDirectoryDTO dto = new TemplateDirectoryDTO(name, new HashSet<>(tags), description);
        dto.setUuid(uuid);

        return documentService.updateDirectory(dto);
    }

    @Transactional
    @BctMethodInfo(
            description = "创建模板",
            retDescription = "模板信息",
            returnClass = TemplateDTO.class,
            retName = "TemplateDTO",
            service = "document-service"
    )
    public TemplateDTO docTemplateCreate(
            @BctMethodArg(description = "模板文件ID") String directoryId,
            @BctMethodArg(description = "模板名称") String name,
            @BctMethodArg(description = "文档类型. enum value:UNKNOWN, WORD_2003, EXCEL_2003.") String type,
            @BctMethodArg(description = "文档类型对应的后缀名. e.g. doc, xls, pdf etc") String typeSuffix,
            @BctMethodArg(description = "描述") String description,
            @BctMethodArg(description = "是否可用") Boolean enabled,
            @BctMethodArg(description = "创建人") String createdBy,
            @BctMethodArg(description = "模板文件") MultipartFile file
    ) {
        UUID dirUuid = UUID.fromString(directoryId);
        TemplateDTO template = new TemplateDTO(dirUuid, name, DocTypeEnum.valueOf(type), typeSuffix);
        template.setCreatedBy(createdBy);
        template.setFileName(file.getOriginalFilename());
        template.setDescription(description);
        template.setEnabled(enabled);
        template.setCreatedBy(createdBy);

        String templateContent;
        try {
            // reading all content of file will consume much memory.
            templateContent = CharStreams.toString(new InputStreamReader(file.getInputStream()));
        } catch (IOException e) {
           throw new CustomException("failed to read file as string");
        }

        return documentService.createTemplate(template, templateContent);
    }

    @Transactional
    @BctMethodInfo(
            description = "删除模板文件",
            retDescription = "模板文件ID",
            service = "document-service"
    )
    public UUID docTemplateDelete(
            @BctMethodArg(description = "模板文件ID") String id
    ) {

        UUID uuid = UUID.fromString(id);
        documentService.deleteTemplate(uuid);
        return uuid;
    }

    @BctMethodInfo(
            description = "根据模板ID获取模板",
            retDescription = "模板信息",
            returnClass = TemplateDTO.class,
            retName = "TemplateDTO",
            service = "document-service"
    )
    public TemplateDTO docTemplateGet(
            @BctMethodArg(description = "模板文件ID") String id
    ) {

        UUID uuid = UUID.fromString(id);
        return documentService.getTemplate(uuid).orElseThrow(() -> new IllegalArgumentException("invalid uuid"));
    }

    @Transactional
    @BctMethodInfo(
            description = "更新模板",
            retDescription = "模板信息",
            returnClass = TemplateDTO.class,
            retName = "TemplateDTO",
            service = "document-service"
    )
    public TemplateDTO docTemplateUpdate(
            @BctMethodArg(description = "模板文件ID") String id,
            @BctMethodArg(description = "模板名称") String name,
            @BctMethodArg(description = "文档类型. enum value:UNKNOWN, WORD_2003, EXCEL_2003.") String type,
            @BctMethodArg(description = "文档类型对应的后缀名. e.g. doc, xls, pdf etc") String typeSuffix,
            @BctMethodArg(description = "描述") String description,
            @BctMethodArg(description = "是否可用") Boolean enabled
    ) {
        UUID templateId = UUID.fromString(id);
        TemplateDTO template = new TemplateDTO(templateId, name, DocTypeEnum.valueOf(type), typeSuffix);
        template.setUuid(templateId);
        template.setDescription(description);
        template.setEnabled(enabled);

        return documentService.updateTemplate(template);
    }

    @Transactional
    @BctMethodInfo(
            description = "创建共享字典项",
            retDescription = "字典项",
            returnClass = DictionaryEntryDTO.class,
            retName = "DictionaryEntryDTO",
            service = "document-service"
    )
    public DictionaryEntryDTO docGlobalDictionaryEntryCreate(
            @BctMethodArg(description = "目的路径") String destinationPath,
            @BctMethodArg(description = "字段类型. enum value:String, Number, Date Time, Object, List.") String type,
            @BctMethodArg(description = "标识") List<String> tags,
            @BctMethodArg(description = "描述") String description,
            @BctMethodArg(description = "默认值") String defaultValue,
            @BctMethodArg(description = "创建人") String createdBy
    ) {
        DictionaryEntryDTO dicEntry = new DictionaryEntryDTO(null,
                null,
                destinationPath,
                ValueTypeEnum.valueOf(type),
                new HashSet<>(tags),
                defaultValue);
        dicEntry.setCreatedBy(createdBy);
        dicEntry.setDescription(description);

        return documentService.createGlobalDicEntry(dicEntry);
    }

    @BctMethodInfo(
            description = "创建特定的字典条目",
            retDescription = "字典项",
            returnClass = DictionaryEntryDTO.class,
            retName = "DictionaryEntryDTO",
            service = "document-service"
    )
    public DictionaryEntryDTO docSpecificDictionaryEntryCreate(
            @BctMethodArg(description = "字典组") String dicGroup,
            @BctMethodArg(description = "源路径") String sourcePath,
            @BctMethodArg(description = "目的路径") String destinationPath,
            @BctMethodArg(description = "字段类型. enum value:String, Number, Date Time, Object, List.") String type,
            @BctMethodArg(description = "标识") List<String> tags,
            @BctMethodArg(description = "描述") String description,
            @BctMethodArg(description = "默认值") String defaultValue,
            @BctMethodArg(description = "创建人") String createdBy
    ) {
        DictionaryEntryDTO dicEntry = new DictionaryEntryDTO(dicGroup,
                sourcePath,
                destinationPath,
                ValueTypeEnum.valueOf(type),
                new HashSet<>(tags),
                defaultValue);
        dicEntry.setCreatedBy(createdBy);
        dicEntry.setDescription(description);

        return documentService.createSpecificDicEntry(dicEntry);
    }

    @BctMethodInfo(
            description = "删除特定字典条目",
            retDescription = "字典ID",
            service = "document-service"
    )
    public UUID docDictionaryEntryDelete(
            @BctMethodArg(description = "字典ID") String id
    ) {

        UUID uuid = UUID.fromString(id);
        documentService.deleteDicEntry(uuid);
        return uuid;
    }

    @BctMethodInfo(
            description = "根据字典ID查询特定的字典条目",
            retDescription = "字典项",
            returnClass = DictionaryEntryDTO.class,
            retName = "DictionaryEntryDTO",
            service = "document-service"
    )
    public DictionaryEntryDTO docDictionaryEntryGet(
            @BctMethodArg(description = "字典ID") String id
    ) {

        UUID uuid = UUID.fromString(id);
        return documentService.getDicEntry(uuid).orElseThrow(() -> new IllegalArgumentException("invalid uuid"));
    }

    @BctMethodInfo(
            description = "根据标识查找字典项",
            retDescription = "字典项列表",
            returnClass = DictionaryEntryDTO.class,
            retName = "List<DictionaryEntryDTO>",
            service = "document-service"
    )
    public List<DictionaryEntryDTO> docDictionaryEntryList(
            @BctMethodArg(description = "tags.") List<String> tags
    ) {
        if (null == tags || tags.size() < 1) {
            throw new IllegalArgumentException("tags is required");
        }

        return documentService.getDicEntries(tags);
    }

    @Transactional
    @BctMethodInfo(
            description = "更新特定的字典条目",
            retDescription = "字典项",
            returnClass = DictionaryEntryDTO.class,
            retName = "DictionaryEntryDTO",
            service = "document-service"
    )
    public DictionaryEntryDTO docDictionaryEntryUpdate(
            @BctMethodArg(description = "id") String id,
            @BctMethodArg(description = "字典组") String dicGroup,
            @BctMethodArg(description = "源路径") String sourcePath,
            @BctMethodArg(description = "目的路径") String destinationPath,
            @BctMethodArg(description = "类型. enum value:String, Number, Date Time, Object, List.") String type,
            @BctMethodArg(description = "标记") List<String> tags,
            @BctMethodArg(description = "描述") String description,
            @BctMethodArg(description = "默认值") String defaultValue
    ) {
        UUID uuid = UUID.fromString(id);
        DictionaryEntryDTO dicEntry = new DictionaryEntryDTO(dicGroup,
                sourcePath,
                destinationPath,
                ValueTypeEnum.valueOf(type),
                new HashSet<>(tags),
                defaultValue);
        dicEntry.setUuid(uuid);
        dicEntry.setDescription(description);

        return documentService.updateDicEntry(dicEntry);
    }
}
