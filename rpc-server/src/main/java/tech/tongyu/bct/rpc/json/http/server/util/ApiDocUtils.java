package tech.tongyu.bct.rpc.json.http.server.util;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.util.Strings;
import tech.tongyu.bct.common.api.doc.BctFieldDto;
import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.common.util.CollectionUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class ApiDocUtils {

    public static void writeBctFieldDto(StringBuilder stringBuilder, Collection<BctFieldDto> bctFieldDtoList, String type){
        bctFieldDtoList.stream()
                .peek(bctFieldDto -> {
                    writeContent(stringBuilder
                            , bctFieldDto.getName(), bctFieldDto.getType(), bctFieldDto.getDescription()
                            , true, type, false, NON_VALID);
                    if(CollectionUtils.isNotEmpty(bctFieldDto.getSubClassFieldList())){
                        writeBctFieldDto(stringBuilder, bctFieldDto.getSubClassFieldList(), bctFieldDto.getType());
                    }
                    if(CollectionUtils.isNotEmpty(bctFieldDto.getPossibleSubClassFieldCollectionMap())){
                        bctFieldDto.getPossibleSubClassFieldCollectionMap()
                                .entrySet().stream()
                                .peek(entry -> {
                                    if(CollectionUtils.isNotEmpty(entry.getValue())){
                                        writeBctFieldDto(stringBuilder, entry.getValue(), entry.getKey());
                                    }
                                })
                                .collect(Collectors.toSet());
                    }
                })
                .collect(Collectors.toSet());
    }

    private static final String PARAM_TABLE_HEADER =
            "|参数名|参数类型|参数描述|是否子类型字段描述|关联类型|是否可选类型|可选类型关联字段|\n" +
            "|:-------|:-------|:-------|:-------|:-------|:-------|:-------|\n";

    private static final String RETURN_TABLE_HEADER =
            "|返回字段名|返回字段类型|返回字段描述|是否子类型字段描述|关联类型|是否可选类型|可选类型关联字段|\n" +
            "|:-------|:-------|:-------|:-------|:-------|:-------|:-------|\n";

    private static final String NON_VALID = "-";

    public static void writeBlankLine(StringBuilder stringBuilder){
        stringBuilder.append("\n");
    }

    public static void writeContent(StringBuilder stringBuilder
            , String name, String type, String description, Boolean isSubClass, String relatedType
            , Boolean optional, String optionalRelatedType){
        stringBuilder.append(String.format("|%s|%s|%s|%b|%s|%b|%s|\n", name, type, description, isSubClass, relatedType, optional, optionalRelatedType));
    }

    public static void writeCommonComment(StringBuilder stringBuilder, String header, String key, Map<String, Object> map){
        stringBuilder.append(String.format("- %s: %s\n", header, map.get(key).toString()));
    }

    public static void writeCommonTitle(StringBuilder stringBuilder, String key){
        stringBuilder.append(String.format("# %s\n", key));
    }

    public static void writeCommonSecondaryTitle(StringBuilder stringBuilder, String key, Map<String, Object> map){
        stringBuilder.append(String.format("## %s\n", map.get(key).toString()));
    }

    public static void createApiDoc(List<Map<String, Object>> data){
        Map<String, List<Map<String, Object>>> services = data.stream()
                .collect(Collectors.groupingBy(m -> m.get("service").toString()));
        StringBuilder buffer = new StringBuilder();
        services.forEach((service, info) -> {
            writeCommonTitle(buffer, service);
            info.forEach(method -> {
                writeCommonSecondaryTitle(buffer, "method", method);
                writeCommonComment(buffer, "服务名", "service", method);
                writeCommonComment(buffer, "接口名", "method", method);
                writeCommonComment(buffer, "描述", "description", method);
                writeCommonComment(buffer, "返回类型", "retType", method);
                writeCommonComment(buffer, "返回名称", "retName", method);
                writeBlankLine(buffer);
                buffer.append(PARAM_TABLE_HEADER);
                List<Map<String, Object>> args = (List<Map<String, Object>>) method.get("args");
                args.forEach(arg -> {
                    String name = (String) arg.get("name");
                    String type = (String) arg.get("type");
                    String description = (String) arg.get("description");
                    List<BctFieldDto> bctFieldDtoList = (List<BctFieldDto>) arg.get("class-info");
                    Boolean hasSubClassField =CollectionUtils.isNotEmpty(bctFieldDtoList);
                    if(hasSubClassField){
                        writeBctFieldDto(buffer, bctFieldDtoList, type);
                    }
                    writeContent(buffer, name, type, description, false, NON_VALID, false, NON_VALID);
                });
                writeBlankLine(buffer);
                buffer.append(RETURN_TABLE_HEADER);
                List<BctFieldDto> bctFieldDtoList = (List<BctFieldDto>) method.get("return-info");
                Boolean hasSubClassField =CollectionUtils.isNotEmpty(bctFieldDtoList);
                if(hasSubClassField){
                    writeBctFieldDto(buffer, bctFieldDtoList, method.get("retType").toString());
                }

                buffer.append("\n");
            });
        });
        try {
            InputStream content = new ByteArrayInputStream(buffer.toString().getBytes(StandardCharsets.UTF_8));
            FileUtils.copyInputStreamToFile(content,new File("api/api_doc.md"));
        } catch (Exception e) {
            throw new CustomException("生成api文档失败,错误原因：" + e.getMessage());
        }
    }
}
