package tech.tongyu.bct.document.service;

import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.document.dto.DictionaryEntryDTO;
import tech.tongyu.bct.document.dto.TemplateDTO;
import tech.tongyu.bct.document.dto.TemplateDirectoryDTO;

import java.io.StringWriter;
import java.io.Writer;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 文档服务。该服务提供提供模板管理，字典管理和文档那个生成三个核心功能。
 * １．一个＂模板文件夹＂有多个＂模板＂;
 * ２．一个＂模板＂关联一个＂模板文件＂;
 * ３．某一时刻，只有一个＂模板＂是enable = true 状态，
 * 并且该版本所属的模板文件将用于＂文件生成＂．
 *
 * @author hangzhi
 */
public interface DocumentService {
    String SCHEMA = "documentService";


    /**
     * 创建一个模板文件夹
     *
     * @param directory 创建文件夹所使用的参数。
     * @return 返回系统存储的文件夹。
     */
    TemplateDirectoryDTO createDirectory(TemplateDirectoryDTO directory);

    /**
     * 删除模板文件夹。
     * 确保 Id 有效。
     *
     * @param directoryId
     * @throws CustomException Id 无效时，抛出该异常。
     */
    void deleteDirectory(UUID directoryId);

    /**
     * 获取模板文件夹.
     *
     * @param directoryId
     * @return 如果d directoryId 无效，则返回Optional.of(null).
     */
    Optional<TemplateDirectoryDTO> getDirectory(UUID directoryId);

    /**
     * 检索文模板文件夹。 多个tag 是“或”关系。
     *
     * @param tags
     * @return
     */
    List<TemplateDirectoryDTO> getDirectories(List<String> tags);

    /**
     * 更新一个模板文件夹。
     * uuid 必须指定。 文件夹下的“模板”增删需要通过 模板相关API操作。
     *
     * @param directory
     * @return
     */
    TemplateDirectoryDTO updateDirectory(TemplateDirectoryDTO directory);

    /**
     * 创建模板实例。
     * 一个模板必须关联一个 模板内容。该模板内容必须为符合FreeMarker规范的模板字符串。
     *
     * @param template
     * @param templateContent
     * @return
     * @see <a href="https://freemarker.apache.org/">FreeMarker</a>
     */
    TemplateDTO createTemplate(TemplateDTO template, String templateContent);

    /**
     * 创建模板实例。
     * 一个模板必须关联一个 模板内容。
     *
     * @param template
     * @param data
     * @return
     * @see <a href="https://freemarker.apache.org/">FreeMarker</a>
     */
    TemplateDTO createTemplateWithByte(TemplateDTO template, byte[] data);

    /**
     * 删除指定模板
     *
     * @param templateId
     */
    void deleteTemplate(UUID templateId);

    /**
     * 获取指定的模板
     *
     * @param templateId
     * @return
     */
    Optional<TemplateDTO> getTemplate(UUID templateId);

    /**
     * 获取指定模板的内容。 目前该内容是 FreeMarker模板。
     *
     * @param templateId
     * @return
     * @see <a herf="https://freemarker.apache.org/">FreeMarker</a>
     */
    String getTemplateContent(UUID templateId);

    byte[] getTemplateDoc(UUID templateId);
    /**
     * 更新指定的模板。
     *
     * @param template
     * @return
     */
    TemplateDTO updateTemplate(TemplateDTO template);

    /**
     * 创建限定的字典项，该字典项必须指定组(dicGroup)并且源路经（sourcePath)不可为空。
     * dicGroup 不可与全局 Group 名称冲突 ("GLOBAL").
     * @param entry 原始参数
     * @return
     */
    DictionaryEntryDTO createSpecificDicEntry(DictionaryEntryDTO entry);

    /**
     * 创建全局的字典项．该字典项目的默认值不可为空，且DicGroup为指定的值．
     * @param entry
     * @return
     */
    public DictionaryEntryDTO createGlobalDicEntry(DictionaryEntryDTO entry);

    /**
     * 删除字典项。
     * @param entryId
     */
    void deleteDicEntry(UUID entryId);

    /**
     * 获取指定的字典项。
     * @param entryId
     * @return
     */
    Optional<DictionaryEntryDTO> getDicEntry(UUID entryId);

    /**
     * 依据标签查询字典项。 多个标签之间是“或“关系。
     * @param tags
     * @return
     */
    List<DictionaryEntryDTO> getDicEntries(List<String> tags);

    /**
     * 更新字典项.
     * 如果该字典为全局，　则dicGroup和SourcePath不可改变．
     * @param entry 参数列表
     * @return
     */
    DictionaryEntryDTO updateDicEntry(DictionaryEntryDTO entry);

    /**
     *  生成文档。
     * @param data
     * @param directoryId
     * @param dicGroup
     * @param out
     */
    void genDocument(Object data, UUID directoryId, String dicGroup, Writer out);

    byte[] genByteDocument(Object data, UUID directoryId, String dicGroup);

    StringWriter genDocumentWriter(Object data, UUID directoryId, String dicGroup);
}
