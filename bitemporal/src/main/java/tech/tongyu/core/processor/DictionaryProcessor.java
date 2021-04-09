package tech.tongyu.core.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auto.service.AutoService;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.tongyu.core.annotation.*;

import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.persistence.Column;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@AutoService(Processor.class)
public class DictionaryProcessor extends BaseProcessor {

    private static Logger logger = LoggerFactory.getLogger(DictionaryProcessor.class);

    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> supported = new LinkedHashSet<>();
        supported.add(DictionaryTitle.class.getCanonicalName());
        return supported;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        try {
            for (Element element : roundEnv.getElementsAnnotatedWith(DictionaryTitle.class)) {
                if (element.getKind() != ElementKind.CLASS)
                    throw new ProcessingException(element,
                            "Only class can be annotated with %s", DictionaryTitle.class.getSimpleName());
                DictionaryTitle dt = element.getAnnotation(DictionaryTitle.class);

                String entityClassName = element.getSimpleName().toString();

                String newSchema = addComma(generateSchema((Type) (element.asType()), Arrays.asList("\t")));

                File mdFile = new File("." + File.separator + "deploy" + File.separator + "dictionary" + File.separator + dt.tableName().toUpperCase() + ".md");
                File parent = mdFile.getParentFile();
                if (parent != null && !parent.exists()) {
                    parent.mkdirs();
                }
                mdFile.createNewFile();
                BufferedWriter mdWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(mdFile)));
                mdWriter.write("##数据字典");
                mdWriter.newLine();
                mdWriter.newLine();
                mdWriter.write("表名:```" + dt.tableName() + "/" + dt.tableName() + "_bitemporal```");
                mdWriter.newLine();
                mdWriter.newLine();
                mdWriter.write("说明:```" + dt.tableMemo() + "(_bitemporal代表双时序)```");
                mdWriter.newLine();
                mdWriter.newLine();

                mdWriter.write("|字段名|说明|类型|备注|");
                mdWriter.newLine();
                mdWriter.write("|---|---|---|---|");
                mdWriter.newLine();
                mdWriter.write("|entity_uuid|版本ID|uuid|仅在双时序表中存在|");
                mdWriter.newLine();
                mdWriter.write("|entity_state|实例状态|uuid|仅在双时序表中存在|");
                mdWriter.newLine();
                mdWriter.write("|valid_range|业务时间段|tstzrange|仅在双时序表中存在|");
                mdWriter.newLine();
                mdWriter.write("|system_range|系统时间段|tstzrange|仅在双时序表中存在|");
                mdWriter.newLine();
                mdWriter.write("|entity_id|实例ID|uuid||");
                mdWriter.newLine();
                mdWriter.write("|entity_doc|实例JSON|jsonb||");
                mdWriter.newLine();
                List<String> drs = element.getEnclosedElements().stream()
                        .filter(e -> e.getAnnotation(DictionaryRow.class) != null)
                        .map(e -> {
                            StringBuffer sb = new StringBuffer();
                            DictionaryRow dr = e.getAnnotation(DictionaryRow.class);
                            Column column = e.getAnnotation(Column.class);
                            BitemporalMapping bm = e.getAnnotation(BitemporalMapping.class);
                            String cName = ("".equals(dr.columnName()) ? bm != null ? bm.column() : column.name() : dr.columnName()).replace("_", "\\_");
                            String cMemo = dr.columnMemo();
                            String cType = "".equals(dr.columnType()) ? bm != null ? bm.isEntity() ? bm.isList() ? "uuid[]" : "uuid" : "jsonb" : "".equals(column.columnDefinition()) ? firstLetterLowerCase(getSimpleClassName(e.asType().toString())) : column.columnDefinition() : dr.columnType();
                            String cRemark = dr.remark().replace("_", "\\_");
                            sb.append("|").append(cName).append("|").append(cMemo).append("|").append(cType).append("|").append(cRemark);
                            return sb.toString();
                        }).collect(Collectors.toList());
                for (String dr : drs) {
                    mdWriter.write(dr);
                    mdWriter.newLine();
                }
                mdWriter.newLine();
                mdWriter.write("父表:```" + ("".equals(dt.parentTableName()) ? "无" : dt.parentTableName()) + "```");
                mdWriter.newLine();
                mdWriter.newLine();
                mdWriter.write("ID表:```" + dt.tableName() + "_key```");
                mdWriter.newLine();
                mdWriter.newLine();
                mdWriter.write("|字段名|说明|类型|备注|");
                mdWriter.newLine();
                mdWriter.write("|---|---|---|---|");
                mdWriter.newLine();
                mdWriter.write("|entity_id|版本ID|uuid||");
                mdWriter.newLine();
                mdWriter.write("|entity_doc|实例JSON|jsonb|无用|");
                mdWriter.newLine();

                List<String> keyDrs = element.getEnclosedElements().stream()
                        .filter(e -> e.getAnnotation(DictionaryRow.class) != null &&
                                (e.getAnnotation(BitemporalKey.class) != null ||
                                        e.getAnnotation(UniqueIndex.class) != null ||
                                        e.getAnnotation(UniqueIndexes.class) != null))
                        .map(e -> {
                            StringBuffer sb = new StringBuffer();
                            DictionaryRow dr = e.getAnnotation(DictionaryRow.class);
                            Column column = e.getAnnotation(Column.class);
                            String cName = ("".equals(dr.columnName()) ? column.name() : dr.columnName()).replace("_", "\\_");
                            String cMemo = dr.columnMemo();
                            String cType = "".equals(dr.columnType()) ? "".equals(column.columnDefinition()) ? firstLetterLowerCase(getSimpleClassName(e.asType().toString())) : column.columnDefinition() : dr.columnType();
                            String cRemark = dr.remark();
                            sb.append("|").append(cName).append("|").append(cMemo).append("|").append(cType).append("|").append(cRemark);
                            return sb.toString();
                        }).collect(Collectors.toList());

                for (String dr : keyDrs) {
                    mdWriter.write(dr);
                    mdWriter.newLine();
                }

                mdWriter.write("##关系型查询接口");
                mdWriter.newLine();
                mdWriter.newLine();

                mdWriter.write("1、 通过ID获取相应实例");
                mdWriter.newLine();
                mdWriter.newLine();
                mdWriter.write("```");
                mdWriter.newLine();
                mdWriter.write("请求格式:");
                mdWriter.newLine();
                mdWriter.write("{");
                mdWriter.newLine();
                mdWriter.write("\t\"method\": \"findByEntityIdFrom" + entityClassName + "\",");
                mdWriter.newLine();
                mdWriter.write("\t\"params\": {");
                mdWriter.newLine();
                mdWriter.write("\t\t\"entityId\": \"实例ID\" //必填");
                mdWriter.newLine();
                mdWriter.write("\t}");
                mdWriter.newLine();
                mdWriter.write("}");
                mdWriter.newLine();
                mdWriter.newLine();
                mdWriter.write("返回格式:");
                mdWriter.newLine();
                mdWriter.write("{");
                mdWriter.newLine();
                mdWriter.write("\t\"jsonrpc\": \"2.0\",");
                mdWriter.newLine();
                mdWriter.write("\t\"id\": \"1\",");
                mdWriter.newLine();
                mdWriter.write("\t\"result\": ");
                mdWriter.newLine();

                for (String str : newSchema.split("\n")) {
                    mdWriter.write("\t" + str);
                    mdWriter.newLine();
                }

                mdWriter.write("}");
                mdWriter.newLine();
                mdWriter.write("```");
                mdWriter.newLine();
                mdWriter.newLine();

                List<Element> apiList = element.getEnclosedElements().stream()
                        .filter(e -> e.getAnnotation(QueryTemplate.class) != null)
                        .collect(Collectors.toList());

                for (int i = 0; i < apiList.size(); i++) {
                    mdWriter.write((i + 2) + "、 " + apiList.get(i).getAnnotation(QueryTemplate.class).description());
                    mdWriter.newLine();
                    mdWriter.newLine();
                    mdWriter.write("```");
                    mdWriter.newLine();
                    mdWriter.write("请求格式:");
                    mdWriter.newLine();
                    mdWriter.write("{");
                    mdWriter.newLine();
                    mdWriter.write("\t\"method\": \"" + apiList.get(i).getSimpleName() + "From" + entityClassName + "\",");
                    mdWriter.newLine();
                    mdWriter.write("\t\"params\": {");
                    mdWriter.newLine();

                    List<Symbol.VarSymbol> varSymbolList = ((Symbol.MethodSymbol) apiList.get(i)).getParameters();
                    for (int j = 0; j < varSymbolList.size(); j++) {
                        if (varSymbolList.get(j).getAnnotation(QueryParam.class) != null) {
                            QueryParam queryParam = varSymbolList.get(j).getAnnotation(QueryParam.class);
                            mdWriter.write("\t\t\"" + varSymbolList.get(j).name + "\": \"" + queryParam.description() + "\"" + (j != varSymbolList.size() - 1 ? "," : "") + " //" + (queryParam.required() ? "必填" : "选填"));
                            mdWriter.newLine();
                        }
                    }

                    mdWriter.write("\t}");
                    mdWriter.newLine();
                    mdWriter.write("}");
                    mdWriter.newLine();
                    mdWriter.newLine();
                    mdWriter.write("返回格式:");
                    mdWriter.newLine();
                    mdWriter.write("{");
                    mdWriter.newLine();
                    mdWriter.write("\t\"jsonrpc\": \"2.0\",");
                    mdWriter.newLine();
                    mdWriter.write("\t\"id\": \"1\",");
                    mdWriter.newLine();
                    mdWriter.write("\t\"result\": [");
                    mdWriter.newLine();

                    for (String str : newSchema.split("\n")) {
                        mdWriter.write("\t\t" + str);
                        mdWriter.newLine();
                    }

                    mdWriter.write("\t]");
                    mdWriter.newLine();
                    mdWriter.write("}");
                    mdWriter.newLine();
                    mdWriter.write("```");
                    mdWriter.newLine();
                    mdWriter.newLine();
                }

                mdWriter.flush();
                mdWriter.close();
            }
        } catch (ProcessingException e) {
            error(e.getElement(), e.getMessage());
        } catch (Exception e) {
            error(null, e.getMessage());
        }
        return true;
    }

    private String generateSchema(Type type, List<String> tabs) {
        StringBuffer sb = new StringBuffer();
        sb.append("{\n");
        ArrayList<String> newTabs = new ArrayList<>();
        newTabs.addAll(tabs);
        newTabs.add("\t");

        final int[] count = {0};
        type.asElement().getEnclosedElements().stream()
                .filter(e -> e.getKind().isField() && shouldGenerateField(type, e))
                .forEach(e -> {
                    tabs.forEach(t -> sb.append("\t"));
                    sb.append("\"" + e.getSimpleName() + "\"").append(": ");
                    if (Optional.ofNullable(e.getAnnotation(BitemporalMapping.class)).isPresent()) {
                        BitemporalMapping bm = e.getAnnotation(BitemporalMapping.class);
                        if (bm.isList() && bm.isEntity()) {
                            sb.append("[\n");
                            sb.append("\t\t\"" + UUID.randomUUID().toString() + "\"\n");
                            sb.append("\t]\n");
                        } else if (bm.isEntity()) {
                            sb.append("\"" + UUID.randomUUID().toString() + "\"\n");
                        } else {
                            if (e.type.tsym.name.toString().equals("List")) {
                                sb.append("[\n");
                                Type pType = ((Type.ClassType) e.type).typarams_field.get(0).tsym.type;
                                sb.append("\t\t" + returnDefaultValue(pType, newTabs) + "\n");
                                sb.append("\t]\n");
                            } else {
                                sb.append(generateSchema(e.asType(), newTabs));
                            }
                        }
                    } else {
                        if (e.type.tsym.name.toString().equals("List")) {
                            sb.append("[\n");
                            Type pType = ((Type.ClassType) e.type).typarams_field.get(0).tsym.type;
                            newTabs.add("\t");
                            sb.append("\t\t\t" + generateSchema(pType, newTabs).replace("},", "}"));
                            sb.append("\t\t]\n");
                        } else {
                            sb.append(returnDefaultValue(e.asType(), newTabs) + "\n");
                        }
                    }
                    count[0]++;
                });

        if (count[0]==0) {
            for (int i = 1; i < tabs.size(); i++) {
                sb.append("\t");
            }
            sb.append("\t<???>\n");
        }

        for (int i = 1; i < tabs.size(); i++) {
            sb.append("\t");
        }
        sb.append("}\n");
        return sb.toString();
    }

    private boolean shouldGenerateField(Type type, Element field) {
        if (Optional.ofNullable(type.getAnnotation(Bitemporal.class)).isPresent()) {
            return (Optional.ofNullable(field.getAnnotation(DictionaryRow.class)).isPresent());
        } else {
            return true;
        }
    }

    private String returnDefaultValue(Type type, List<String> tabs) {
        try {
            if (((Type.ClassType) type.tsym.type).supertype_field.tsym.toString().equals(Enum.class.getTypeName())) {
                return "<string>";
            } else if (type.toString().equals(BigDecimal.class.getTypeName())) {
                return "<double>";
            } else if (type.toString().equals(BigInteger.class.getTypeName())) {
                return "<int>";
            } else if (type.isPrimitive() || type.toString().contains("java.lang")) {
                return "<" + getSimpleClassName(type.toString()).toLowerCase() + ">";
            } else if (type.toString().equals(Date.class.getTypeName())) {
                return objectMapper.writeValueAsString(Date.valueOf(LocalDate.now()));
            } else if (type.toString().equals(LocalDate.class.getTypeName())) {
                return formatOneLineJson(objectMapper.writeValueAsString(LocalDate.now()), tabs);
            } else {
                return formatOneLineJson(objectMapper.writeValueAsString(type.getClass().newInstance()), tabs);
            }
        } catch (Exception e) {
            return "<???>";
        }
    }

    private String formatOneLineJson(String json, List<String> tabs) {
        String[] lines = json.replace("{", "{\n")
                .replace("}", "\n}")
                .replace(":", ": ")
                .replace(",", "\n")
                .split("\n");
        final int[] count = {-1};
        StringBuffer result = new StringBuffer();
        Arrays.stream(lines).forEach(e -> {
            if (e.endsWith("{")) {
                if (count[0]>-1) {
                    tabs.stream().forEach(t -> result.append(t));
                    if (count[0]>0)
                        IntStream.range(0, count[0]).forEach(i -> result.append("\t"));
                }
                count[0]++;
            } else if (e.endsWith("}")) {
                tabs.stream().limit(tabs.size()-1).forEach(t -> result.append(t));
                if (count[0]>0)
                    IntStream.range(0, count[0]).forEach(i -> result.append("\t"));
                count[0]--;
            } else {
                tabs.stream().forEach(t -> result.append(t));
                if (count[0]>0)
                    IntStream.range(0, count[0]).forEach(i -> result.append("\t"));
            }
            result.append(e + "\n");
        });
        return result.toString().substring(0, result.toString().lastIndexOf("\n"));
    }



    private String addComma(String string) {
        String[] lines = string.split("\n");

        StringBuffer result = new StringBuffer();

        for (int i=0; i<lines.length; i++) {
            result.append(lines[i]);
            if (lines.length>1 && i<lines.length-2
                    && !lines[i].endsWith("[") && !lines[i].endsWith("{")
                    && !"}".equals(lines[i+1].replaceAll("\t", ""))
                    && !"]".equals(lines[i+1].replaceAll("\t", ""))) {
                result.append(",\n");
            } else {
                result.append("\n");
            }
        }

        return result.toString();
    }
}
