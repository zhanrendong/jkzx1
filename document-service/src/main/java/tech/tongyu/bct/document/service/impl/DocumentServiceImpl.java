package tech.tongyu.bct.document.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import freemarker.template.TemplateException;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.common.exception.ErrorCode;
import tech.tongyu.bct.common.util.JsonUtils;
import tech.tongyu.bct.document.dao.dbo.DictionaryEntry;
import tech.tongyu.bct.document.dao.dbo.Template;
import tech.tongyu.bct.document.dao.dbo.TemplateContent;
import tech.tongyu.bct.document.dao.dbo.TemplateDirectory;
import tech.tongyu.bct.document.dao.repl.intel.DictionaryEntryRepo;
import tech.tongyu.bct.document.dao.repl.intel.TemplateContentRepo;
import tech.tongyu.bct.document.dao.repl.intel.TemplateDirectoryRepo;
import tech.tongyu.bct.document.dao.repl.intel.TemplateRepo;
import tech.tongyu.bct.document.dto.DictionaryEntryDTO;
import tech.tongyu.bct.document.dto.TemplateDTO;
import tech.tongyu.bct.document.dto.TemplateDirectoryDTO;
import tech.tongyu.bct.document.dto.ValueTypeEnum;
import tech.tongyu.bct.document.service.DocumentService;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 文档服务。该服务提供提供模板管理，字典管理和文档那个生成三个核心功能。
 * １．一个＂模板文件夹＂有多个＂模板＂;
 * ２．一个＂模板＂关联一个＂模板文件＂;
 * ３．某一时刻，只有一个＂模板＂是enable = true 状态，
 * 并且该版本所属的模板文件将用于＂文件生成＂．
 *
 * @author hangzhi
 */
@Service
public class DocumentServiceImpl implements DocumentService {

    private final String GROUP_GLOBAL = "GLOBAL";

    private final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ISO_DATE_TIME;

    private final TypeReference<List<Object>> TYPE_REF = new TypeReference<List<Object>>() {
    };

    private final ObjectMapper mapper = new ObjectMapper();

    private final TemplateContentRepo templateContentRepo;

    private final TemplateRepo templateRepo;

    private final TemplateDirectoryRepo templateDirectoryRepo;

    private final DictionaryEntryRepo dictionaryEntryRepo;

    @Autowired
    public DocumentServiceImpl(TemplateContentRepo templateContentRepo,
                               TemplateDirectoryRepo templateDirectoryRepo,
                               TemplateRepo templateRepo,
                               DictionaryEntryRepo dictionaryEntryRepo) {
        this.templateContentRepo = templateContentRepo;
        this.templateRepo = templateRepo;
        this.templateDirectoryRepo = templateDirectoryRepo;
        this.dictionaryEntryRepo = dictionaryEntryRepo;
    }

    /**
     * 创建一个模板文件夹
     *
     * @param directory 创建文件夹所使用的参数。
     * @return 返回系统存储的文件夹。
     */
    @Override
    public TemplateDirectoryDTO createDirectory(TemplateDirectoryDTO directory) {
        if (null == directory || StringUtils.isEmpty(directory.getName())) {
            throw new CustomException("name cannot be empty.");
        }

        TemplateDirectory entity = toDbo(directory);
        // 避免更新操作, 由系统生成。
        entity.setUuid(null);
        entity.setCreatedAt(null);
        entity.setUpdatedAt(null);

        entity = templateDirectoryRepo.saveAndFlush(entity);

        TemplateDirectoryDTO result = toDto(entity);
        return result;
    }

    /**
     * 删除模板文件夹。
     * 确保 Id 有效。
     *
     * @param directoryId
     * @throws CustomException Id 无效时，抛出该异常。
     */
    @Override
    public void deleteDirectory(UUID directoryId) {
        if (null == directoryId) {
            throw new CustomException("directoryId cannot be null.");
        }

        // 检查Id是否有效。
        templateDirectoryRepo.findById(directoryId).orElseThrow(() -> new CustomException("directoryId is invalid."));

        templateDirectoryRepo.deleteById(directoryId);
    }

    /**
     * 获取模板文件夹.
     *
     * @param directoryId
     * @return 如果d directoryId 无效，则返回Optional.of(null).
     */
    @Override
    public Optional<TemplateDirectoryDTO> getDirectory(UUID directoryId) {
        if (null == directoryId) {
            throw new CustomException("directoryId cannot be null.");
        }

        return templateDirectoryRepo.findById(directoryId).map(dir -> toDto(dir));
    }

    /**
     * 检索文模板文件夹。 多个tag 是“或”关系。
     *
     * @param tags
     * @return
     */
    @Override
    public List<TemplateDirectoryDTO> getDirectories(List<String> tags) {
        if (null == tags || tags.size() < 1) {
            return new ArrayList<>();
        }

        return templateDirectoryRepo.findDirectoriesByTags(tags)
                .stream().map(dir -> toDto(dir)).collect(Collectors.toList());
    }

    /**
     * 更新一个模板文件夹。
     * uuid 必须指定。 文件夹下的“模板”增删需要通过 模板相关API操作。
     *
     * @param directory
     * @return
     */
    @Override
    public TemplateDirectoryDTO updateDirectory(TemplateDirectoryDTO directory) {
        if (null == directory || null == directory.getUuid()) {
            throw new CustomException("Id cannot be empty.");
        }

        TemplateDirectory dbo = templateDirectoryRepo.findById(directory.getUuid())
                .orElseThrow(() -> new CustomException("cannot find this directory."));

        if (null != directory.getName()) {
            dbo.setName(directory.getName());
        }

        if (null != directory.getTags()) {
            dbo.setTags(directory.getTags());
        }

        if (null != directory.getDescription()) {
            dbo.setDescription(directory.getDescription());
        }

        dbo = templateDirectoryRepo.saveAndFlush(dbo);

        return toDto(dbo);
    }

    /**
     * 创建模板实例。
     * 一个模板必须关联一个 模板内容。该模板内容必须为符合FreeMarker规范的模板字符串。
     *
     * @param template
     * @param templateContent
     * @return
     * @see <a href="https://freemarker.apache.org/">FreeMarker</a>
     */
    @Override
    public TemplateDTO createTemplate(TemplateDTO template, String templateContent) {
        if (null == template || null == template.getDirectoryId() || null == template.getType() ||
                StringUtils.isEmpty(template.getTypeSuffix()) || StringUtils.isEmpty(templateContent)) {
            throw new CustomException("parameter(directory/type/typeSuffix/content) is invalid.");
        }

        // check if the directory is valid
        TemplateDirectory dirEntity = templateDirectoryRepo.findById(template.getDirectoryId())
                .orElseThrow(() -> new CustomException("directory id is invalid."));

        // create content
        TemplateContent contentEntity = new TemplateContent(templateContent);
        contentEntity.setContent(templateContent);
        contentEntity = templateContentRepo.save(contentEntity);

        // create template
        Template templateDbo = new Template(template.getDirectoryId(), template.getName(),
                template.getType(), template.getTypeSuffix(), contentEntity);
        templateDbo.setFileName(template.getFileName());
        templateDbo.setDescription(template.getDescription());
        // the first template in directory should be enabled. so that at least one template is available for dir.
        List<Template> templates = dirEntity.getTemplates();
        if (null == templates || templates.size() < 1) {
            templateDbo.setEnabled(true);
        } else {
            templateDbo.setEnabled(false);
        }
        templateDbo.setCreatedBy(template.getCreatedBy());
        templateDbo = templateRepo.save(templateDbo);

        // add template into corresponding directory
        if (null == templates) {
            templates = new ArrayList<>();
        }

        templates.add(templateDbo);
        dirEntity.setTemplates(templates);
        templateDirectoryRepo.saveAndFlush(dirEntity);

        // prepare result
        TemplateDTO result = toDto(templateDbo);

        return result;
    }

    /**
     * 创建模板实例。
     * 一个模板必须关联一个 模板内容。
     *
     * @param template
     * @param data
     * @return
     * @see <a href="https://freemarker.apache.org/">FreeMarker</a>
     */
    @Override
    public TemplateDTO createTemplateWithByte(TemplateDTO template, byte[] data) {
        if (null == template || null == template.getDirectoryId() || null == template.getType() ||
                StringUtils.isEmpty(template.getTypeSuffix()) || ArrayUtils.isEmpty(data)) {
            throw new CustomException("parameter(directory/type/typeSuffix/content) is invalid.");
        }

        // check if the directory is valid
        TemplateDirectory dirEntity = templateDirectoryRepo.findById(template.getDirectoryId())
                .orElseThrow(() -> new CustomException("directory id is invalid."));

        // create content
        TemplateContent contentEntity = new TemplateContent();
        contentEntity.setDoc(data);
        contentEntity = templateContentRepo.save(contentEntity);

        // create template
        Template templateDbo = new Template(template.getDirectoryId(), template.getName(),
                template.getType(), template.getTypeSuffix(), contentEntity);
        templateDbo.setFileName(template.getFileName());
        templateDbo.setDescription(template.getDescription());
        // the first template in directory should be enabled. so that at least one template is available for dir.
        List<Template> templates = dirEntity.getTemplates();
        if (null == templates || templates.size() < 1) {
            templateDbo.setEnabled(true);
        } else {
            templateDbo.setEnabled(false);
        }
        templateDbo.setCreatedBy(template.getCreatedBy());
        templateDbo = templateRepo.save(templateDbo);

        // add template into corresponding directory
        if (null == templates) {
            templates = new ArrayList<>();
        }

        templates.add(templateDbo);
        dirEntity.setTemplates(templates);
        templateDirectoryRepo.saveAndFlush(dirEntity);

        // prepare result
        TemplateDTO result = toDto(templateDbo);

        return result;
    }

    /**
     * 删除指定模板
     *
     * @param templateId
     */
    @Override
    public void deleteTemplate(UUID templateId) {
        if (null == templateId) {
            throw new CustomException("tmeplateId cannot be null.");
        }

        Template template = templateRepo.findById(templateId)
                .orElseThrow(() -> new CustomException("invalid templateId."));
        TemplateDirectory directory = templateDirectoryRepo.findById(template.getDirectoryId())
                .orElseThrow(() -> new CustomException("invalid directory id."));

        // remove this template from directory.
        // if the deleting one is enabled and template >= 2, enable the last one.
        List<Template> templates = directory.getTemplates();
        if (null != templates) {
            templates.removeIf(t -> t.getUuid().equals(template.getUuid()));
        }

        if (template.isEnabled() && templates.size() > 0) {
            Template lastOne = templates.get(templates.size() - 1);
            lastOne.setEnabled(true);
            templateRepo.save(lastOne);
        }

        templateDirectoryRepo.save(directory);
        templateRepo.deleteById(template.getUuid());
        templateRepo.flush();
    }

    /**
     * 获取指定的模板
     *
     * @param templateId
     * @return
     */
    @Override
    public Optional<TemplateDTO> getTemplate(UUID templateId) {
        if (null == templateId) {
            throw new CustomException("tmeplateId cannot be null.");
        }

        return templateRepo.findById(templateId).map(t -> toDto(t));
    }

    /**
     * 获取指定模板的内容。 目前该内容是 FreeMarker模板。
     *
     * @param templateId
     * @return
     * @see <a herf="https://freemarker.apache.org/">FreeMarker</a>
     */
    @Override
    public String getTemplateContent(UUID templateId) {
        if (null == templateId) {
            throw new CustomException("templateId cannot be null.");
        }

        Template template = templateRepo.findById(templateId)
                .orElseThrow(() -> new CustomException("invalid templateId"));

        TemplateContent content = template.getContent();
        assert null != content;

        return content.getContent();
    }

    /**
     * 获取指定模板的内容。 目前该内容是 FreeMarker模板。
     *
     * @param templateId
     * @return
     * @see <a herf="https://freemarker.apache.org/">FreeMarker</a>
     */
    @Override
    public byte[] getTemplateDoc(UUID templateId) {
        if (null == templateId) {
            throw new CustomException("templateId cannot be null.");
        }

        Template template = templateRepo.findById(templateId)
                .orElseThrow(() -> new CustomException("invalid templateId"));

        TemplateContent content = template.getContent();
        assert null != content;

        return content.getDoc();
    }

    /**
     * 更新指定的模板。
     *
     * @param template
     * @return
     */
    @Override
    public TemplateDTO updateTemplate(TemplateDTO template) {
        if (null == template || null == template.getUuid()) {
            throw new CustomException("templateId cannot be null.");
        }

        Template dbo = templateRepo.findById(template.getUuid())
                .orElseThrow(() -> new CustomException("invalid templateId"));

        if (null != template.getName()) {
            dbo.setName(dbo.getName());
        }

        if (null != template.getType()) {
            dbo.setType(template.getType());
        }

        if (null != template.getTypeSuffix()) {
            dbo.setTypeSuffix(template.getTypeSuffix());
        }

        if (null != template.getFileName()) {
            dbo.setFileName(template.getFileName());
        }

        if (null != template.getDescription()) {
            dbo.setDescription(template.getDescription());
        }

        if (null != template.isEnabled() && (boolean) template.isEnabled()) {
            TemplateDirectory directory = templateDirectoryRepo.findById(dbo.getDirectoryId())
                    .orElseThrow(() -> new CustomException("invalid directoryId."));

            Template enabledOne = directory.findEnabledTemplate();
            if (null != enabledOne) {
                enabledOne.setEnabled(false);
                templateRepo.save(enabledOne);
            }

            dbo.setEnabled(true);
        }

        dbo = templateRepo.saveAndFlush(dbo);
        return toDto(dbo);
    }

    /**
     * 创建限定的字典项，该字典项必须指定组(dicGroup)并且源路经（sourcePath)不可为空。
     * dicGroup 不可与全局 Group 名称冲突 ("GLOBAL").
     *
     * @param entry 原始参数
     * @return
     */
    @Override
    public DictionaryEntryDTO createSpecificDicEntry(DictionaryEntryDTO entry) {
        if (null == entry ||
                StringUtils.isEmpty(entry.getDicGroup()) ||
                StringUtils.isEmpty(entry.getSourcePath()) ||
                StringUtils.isEmpty(entry.getDestinationPath()) ||
                null == entry.getType()) {
            throw new CustomException("invalid dicGroup / sourcePath / destinationPath /t type");
        }

        if (entry.getDicGroup().equals(GROUP_GLOBAL)) {
            throw new CustomException("group 名称不可以为：" + GROUP_GLOBAL);
        }

        DictionaryEntry dbo = toDbo(entry);
        // these fields will be filled by system.
        dbo.setUuid(null);
        dbo.setCreatedAt(null);
        dbo.setUpdatedAt(null);

        dbo = dictionaryEntryRepo.saveAndFlush(dbo);

        return toDto(dbo);
    }

    /**
     * 创建全局的字典项．该字典项目的默认值不可为空，且DicGroup为指定的值．
     *
     * @param entry
     * @return
     */
    @Override
    public DictionaryEntryDTO createGlobalDicEntry(DictionaryEntryDTO entry) {
        if (null == entry ||
                StringUtils.isEmpty(entry.getDestinationPath()) ||
                StringUtils.isEmpty(entry.getDefaultValue()) ||
                null == entry.getType()) {
            throw new CustomException("destinationPath /t type");
        }

        if (GROUP_GLOBAL.equals(entry.getDicGroup())) {
            throw new CustomException("group 名称不可以为：" + GROUP_GLOBAL);
        }

        DictionaryEntry dbo = toDbo(entry);
        // these fields will be filled by system.
        dbo.setUuid(null);
        dbo.setDicGroup(GROUP_GLOBAL);
        dbo.setSourcePath(null);
        dbo.setCreatedAt(null);
        dbo.setUpdatedAt(null);

        dbo = dictionaryEntryRepo.saveAndFlush(dbo);

        return toDto(dbo);
    }

    /**
     * 删除字典项。
     *
     * @param entryId
     */
    @Override
    public void deleteDicEntry(UUID entryId) {
        if (null == entryId) {
            throw new CustomException("invalid entryId");
        }

        dictionaryEntryRepo.findById(entryId).orElseThrow(() -> new CustomException("invalid entryId."));
        dictionaryEntryRepo.deleteById(entryId);
    }

    /**
     * 获取指定的字典项。
     *
     * @param entryId
     * @return
     */
    @Override
    public Optional<DictionaryEntryDTO> getDicEntry(UUID entryId) {
        if (null == entryId) {
            throw new CustomException("invalid entryId");
        }

        return dictionaryEntryRepo.findById(entryId).map(entry -> toDto(entry));
    }

    /**
     * 依据标签查询字典项。 多个标签之间是“或“关系。
     *
     * @param tags
     * @return
     */
    @Override
    public List<DictionaryEntryDTO> getDicEntries(List<String> tags) {
        if (null == tags || tags.size() < 1) {
            return new ArrayList<>();
        }

        return dictionaryEntryRepo.findDictionaryEntriesByTags(tags)
                .stream().map(dic -> toDto(dic)).collect(Collectors.toList());
    }

    /**
     * 更新字典项.
     * 如果该字典为全局，　则dicGroup和SourcePath不可改变．
     *
     * @param entry 参数列表
     * @return
     */
    @Override
    public DictionaryEntryDTO updateDicEntry(DictionaryEntryDTO entry) {
        if (null == entry || null == entry.getUuid()) {
            throw new CustomException("uuid cannot be null.");
        }

        DictionaryEntry dbo = dictionaryEntryRepo.findById(entry.getUuid())
                .orElseThrow(() -> new CustomException("invalid dic entry id."));

        if (!GROUP_GLOBAL.equals(dbo.getDicGroup()) && null != entry.getDicGroup()) {
            dbo.setDicGroup(entry.getDicGroup());
        }

        if (!GROUP_GLOBAL.equals(dbo.getDicGroup()) && null != entry.getSourcePath()) {
            dbo.setSourcePath(entry.getSourcePath());
        }

        if (null != entry.getDescription()) {
            dbo.setDescription(entry.getDescription());
        }

        if (null != entry.getDestinationPath()) {
            dbo.setDestinationPath(entry.getDestinationPath());
        }

        if (null != entry.getType()) {
            dbo.setType(entry.getType());
        }

        if (null != entry.getTags()) {
            dbo.setTags(entry.getTags());
        }

        if (null != entry.getDefaultValue()) {
            dbo.setDefaultValue(entry.getDefaultValue());
        }

        dbo = dictionaryEntryRepo.saveAndFlush(dbo);

        return toDto(dbo);
    }

    @Override
    public byte[] genByteDocument(Object data, UUID directoryId, String dicGroup) {
        StringWriter writer = new StringWriter();
        genDocument(data, directoryId, dicGroup, writer);
        return writer.getBuffer().toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public StringWriter genDocumentWriter(Object data, UUID directoryId, String dicGroup){
        StringWriter writer = new StringWriter();
        genDocument(data, directoryId, dicGroup, writer);
        return writer;
    }

    /**
     * 生成文档。
     *
     * @param data
     * @param directoryId
     * @param dicGroup
     * @param out
     */
    @Override
    public void genDocument(Object data, UUID directoryId, String dicGroup, Writer out) {
        String content = getContent(directoryId);
        freemarker.template.Configuration config = new freemarker.template.Configuration();
        freemarker.template.Template fmTemplate = null;

        try {
            fmTemplate = new freemarker.template.Template("genDoc", content, config);
        } catch (IOException e) {
            throw new CustomException("failed to initialize FreeMarker template." + e.getMessage());
        }

        Map<String, Object> dataDic = genDataDic(data, dicGroup);
        try {
            fmTemplate.process(dataDic, out);
        } catch (TemplateException e) {
            throw new CustomException("failed to generate document for directory: " + directoryId.toString() + e.getMessage());
        } catch (IOException e) {
            throw new CustomException("failed to write document to writer." + e.getMessage());
        }
    }

    private String getContent(UUID directoryId) {
        TemplateDirectory directory = templateDirectoryRepo.findById(directoryId)
                .orElseThrow(() -> new CustomException("invalid directoryId."));

        Template template = directory.findEnabledTemplate();
        if (null == template) {
            throw new CustomException("There is no valid template in this directory." + directory.toString());
        }

        TemplateContent content = template.getContent();
        assert null != content;

        return content.getContent();
    }

    private Map<String, Object> genDataDic(Object rawData, String dicGroup) {
        final Map<String, Object> dataDic = new HashMap<>();

        // put global entries into dataDic
        {
            List<DictionaryEntry> globalEntries = dictionaryEntryRepo.findByDicGroup(GROUP_GLOBAL);
            globalEntries.forEach(entry -> {
                Object value = convertToStrongType(entry.getDefaultValue(), entry.getType());
                insertToDataDic(entry.getDestinationPath(), value, dataDic);
            });
        }

        // convert rawData object to Json style(Map / List).
        if (null == rawData) {
            return dataDic;
        }

        String jsonString = JsonUtils.objectToJsonString(rawData);
        Map<String, Object> mappedRawData = JsonUtils.mapFromJsonString(jsonString);

        // merge mappedRawData into dataDic. override global dic entries.
        mergeMap(mappedRawData, dataDic);

        List<DictionaryEntry> specificEntries = dictionaryEntryRepo.findByDicGroup(dicGroup);
        specificEntries.forEach(entry -> {
            Object value = findInPath(entry.getSourcePath(), mappedRawData);
            // fetch default value if not finding data in raw data.
            if (null == value && !StringUtils.isEmpty(entry.getDefaultValue())) {
                value = entry.getDefaultValue();
            }

            // skip this entry.
            if (null == value) {
                return;
            }

            String valueStr = value instanceof String ? (String)value : JsonUtils.objectToJsonString(value);
            Object typedValue = convertToStrongType(valueStr, entry.getType());
            insertToDataDic(entry.getDestinationPath(), typedValue, dataDic);
        });

        return dataDic;
    }

    private void mergeMap(Map<String, Object> source, Map<String, Object> target) {
        assert null != source;
        assert null != target;

        source.forEach((key, value) -> {
            if (!target.containsKey(key)) {
                target.put(key, value);
                return;
            }

            // recursion point
            if (value instanceof Map && target.get(key) instanceof Map) {
                mergeMap((Map<String, Object>) value, (Map<String, Object>) target.get(key));
                return;
            }

            //override
            target.put(key, value);
        });
    } // end merge map

    private Object convertToStrongType(String valueStr, ValueTypeEnum type) {
        switch (type) {
            case NUMBER:
                return Double.valueOf(valueStr);
            case DATE_TIME: {
                // FreeMarker require Date and cannot recognize LocalDateTime.
                // But our system treate LocalDateTime as default struct.
                LocalDateTime temp = LocalDateTime.parse(valueStr, DATETIME_FORMAT);
                ZoneId zoneId = ZoneId.systemDefault();
                ZonedDateTime zdt = temp.atZone(zoneId);
                return Date.from(zdt.toInstant());
            }
            case OBJECT:
                return JsonUtils.mapFromJsonString(valueStr);
            case ARRAY: {
                try {
                    return mapper.readValue(valueStr, TYPE_REF);
                } catch (IOException e) {
                    throw new CustomException(ErrorCode.PARSING_ERROR,
                            "failed to decode: " + e.getMessage());
                }
            }
            case UNKNOWN:
            case STRING:
            default:
                return valueStr;
        }

    } // end convertToStrongType

    private void insertToDataDic(String path, Object value, Map<String, Object> dataDic) {
        assert !StringUtils.isEmpty(path);
        assert null != dataDic;

        String[] tokens = path.split("\\.");

        Map<String, Object> currentNode = dataDic;
        for (int ii = 0; ii < tokens.length; ii++) {
            if (ii == tokens.length - 1) {
                currentNode.put(tokens[ii], value);
                break;
            }

            // override the map.
            String token = tokens[ii];
            if (!currentNode.containsKey(token) || !(currentNode.get(token) instanceof Map)) {
                Map<String, Object> newNode = new HashMap<>();
                currentNode.put(token, newNode);
            }

            currentNode = (Map<String, Object>)currentNode.get(token);
        } // end for
    } // end insertToDataDic

    private Object findInPath(String path, Map<String, Object> dataDic) {
        assert !StringUtils.isEmpty(path);
        assert null != dataDic;

        String[] tokens = path.split("\\.");
        Map<String, Object> parentNode = dataDic;
        for (int ii = 0; ii < tokens.length; ii++) {
            String currentToken = tokens[ii];
            // check whether last node exists.
            if (ii == (tokens.length - 1)) {
                return parentNode.getOrDefault(currentToken, null);
            }

            // middle node exists and is Map.
            if (parentNode.containsKey(currentToken) && parentNode.get(currentToken) instanceof Map) {
                parentNode = (Map<String, Object>) parentNode.get(currentToken);
                continue;
            }

            // if not finding node, return null.
            return null;
        } // end for

        return null;
    } // end findInPath

    private TemplateDirectoryDTO toDto(TemplateDirectory dbo) {
        TemplateDirectoryDTO result = new TemplateDirectoryDTO(dbo.getName(), dbo.getTags(), dbo.getDescription());
        result.setUuid(dbo.getUuid());
        result.setName(dbo.getName());
        List<TemplateDTO> templates = null == dbo.getTemplates() ? new ArrayList<>() : dbo.getTemplates().stream().map(t -> toDto(t)).collect(Collectors.toList());
        result.setTags(dbo.getTags());
        result.setTemplates(templates);
        result.setCreatedAt(dbo.getCreatedAt());
        result.setUpdatedAt(dbo.getUpdatedAt());
        result.setCreatedBy(dbo.getCreatedBy());

        return result;
    }

    /**
     * directory dto to directory dbo .the content of templates cannot be translated in this method.
     *
     * @param dto directory dto
     * @return
     */
    private TemplateDirectory toDbo(TemplateDirectoryDTO dto) {
        TemplateDirectory result = new TemplateDirectory(dto.getName(), dto.getTags(), dto.getDescription());
        result.setUuid(dto.getUuid());
        result.setName(dto.getName());
        List<Template> templates = null == dto.getTemplates() ? new ArrayList<>() : dto.getTemplates().stream().map(t -> toDbo(t, null)).collect(Collectors.toList());
        result.setTags(dto.getTags());
        result.setTemplates(templates);
        result.setCreatedAt(dto.getCreatedAt());
        result.setUpdatedAt(dto.getUpdatedAt());
        result.setCreatedBy(dto.getCreatedBy());

        return result;
    }

    private TemplateDTO toDto(Template dbo) {
        assert null != dbo;

        TemplateDTO result = new TemplateDTO(dbo.getDirectoryId(), dbo.getName(), dbo.getType(), dbo.getTypeSuffix());
        result.setUuid(dbo.getUuid());
        result.setDescription(dbo.getDescription());
        result.setEnabled(dbo.isEnabled());
        result.setCreatedAt(dbo.getCreatedAt());
        result.setUpdatedAt(dbo.getUpdatedAt());
        result.setCreatedBy(dbo.getCreatedBy());
        result.setFileName(dbo.getFileName());
        return result;
    }

    private Template toDbo(TemplateDTO dto, TemplateContent content) {
        assert null != dto;

        Template result = new Template(dto.getDirectoryId(), dto.getName(), dto.getType(), dto.getTypeSuffix(), content);
        result.setUuid(dto.getUuid());
        result.setDescription(dto.getDescription());
        result.setEnabled(dto.isEnabled());
        result.setCreatedAt(dto.getCreatedAt());
        result.setUpdatedAt(dto.getUpdatedAt());
        result.setCreatedBy(dto.getCreatedBy());
        result.setFileName(dto.getFileName());

        return result;
    }

    private DictionaryEntryDTO toDto(DictionaryEntry dbo) {
        assert null != dbo;

        DictionaryEntryDTO result = new DictionaryEntryDTO(dbo.getDicGroup(),
                dbo.getSourcePath(),
                dbo.getDestinationPath(),
                dbo.getType(),
                dbo.getTags(),
                dbo.getDefaultValue());
        result.setUuid(dbo.getUuid());
        result.setDescription(dbo.getDescription());
        result.setDefaultValue(dbo.getDefaultValue());
        result.setCreatedAt(dbo.getCreatedAt());
        result.setUpdatedAt(dbo.getUpdatedAt());
        result.setCreatedBy(dbo.getCreatedBy());

        return result;
    }

    private DictionaryEntry toDbo(DictionaryEntryDTO dto) {
        assert null != dto;

        DictionaryEntry result = new DictionaryEntry(dto.getDicGroup(),
                dto.getSourcePath(),
                dto.getDestinationPath(),
                dto.getType(),
                dto.getTags(),
                dto.getDefaultValue());
        result.setDescription(dto.getDescription());
        result.setDefaultValue(dto.getDefaultValue());
        result.setCreatedAt(dto.getCreatedAt());
        result.setUpdatedAt(dto.getUpdatedAt());
        result.setCreatedBy(dto.getCreatedBy());

        return result;
    }
}
