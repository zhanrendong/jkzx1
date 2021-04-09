package tech.tongyu.core.processor;

import com.squareup.javapoet.*;
import com.sun.tools.javac.code.Symbol;
import org.hibernate.annotations.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import tech.tongyu.core.annotation.*;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.persistence.Column;
import javax.persistence.Transient;
import javax.tools.Diagnostic;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

abstract class BaseProcessor extends AbstractProcessor {

    protected static final List<Class<? extends Annotation>> supportedJPAAnnotations = Arrays.asList(
            Column.class, Type.class, Transient.class);

    protected static final String Repository = "repository";
    protected static final String Model = "model";
    protected static final String Bean = "bean";
    protected static final String Service = "service";
    protected static final String Schema = "schema";
    protected static final String BeanTypeTag = "Bean";
    protected static final String RepositoryTypeTag = "Repository";
    protected static final String RelationalBeanTypeTag = "RelationalBean";
    protected static final String RelationalRepositoryTypeTag = "RelationalRepository";
    protected static final String RepositoryServiceTypeTag = "RepositoryService";
    protected static final String BitemporalTypeTag = "";
    protected static final String BitemporalTableTag = "_bitemporal";
    protected static final String KeyTypeTag = "Key";
    protected static final String KeyTableTag = "_key";
//    protected static final String TemporalTypeTag = "Temporal";
//    protected static final String TemporalTableTag = "_temporal";
    protected static final String RelationalTypeTag = "Relational";
    protected static final String RelationalTableTag = "";
//    protected static final String EntityTypePrefix = "tech.tongyu.core.entity.";
//    protected static final String ModelPackage = "tech.tongyu.core.entity.model";

    private static Logger logger = LoggerFactory.getLogger(BaseProcessor.class);

    private Types types;
    private Elements elements;
    private Filer filer;
    private Messager messager;

    public Types getTypes() {
        return types;
    }

    public void setTypes(Types types) {
        this.types = types;
    }

    public Elements getElements() {
        return elements;
    }

    public void setElements(Elements elements) {
        this.elements = elements;
    }

    public Filer getFiler() {
        return filer;
    }

    public void setFiler(Filer filer) {
        this.filer = filer;
    }

    public Messager getMessager() {
        return messager;
    }

    public void setMessager(Messager messager) {
        this.messager = messager;
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        this.types = processingEnvironment.getTypeUtils();
        this.elements = processingEnvironment.getElementUtils();
        this.messager = processingEnvironment.getMessager();
        this.filer = processingEnvironment.getFiler();
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    public void error(Element element, String message) {
        this.messager.printMessage(Diagnostic.Kind.ERROR, message, element);
    }

    public List<FieldSpec> processColumn(List<? extends Element> enclosedElements) {

        List<FieldSpec> fields = new ArrayList();

        FieldSpec.Builder objectMapper = FieldSpec.builder(ClassName.get("com.fasterxml.jackson.databind", "ObjectMapper"), firstLetterLowerCase("ObjectMapper")).addModifiers(Modifier.PRIVATE);
        AnnotationSpec _autowired = AnnotationSpec.builder(Autowired.class).build();
        objectMapper.addAnnotation(_autowired);
        AnnotationSpec _transient = AnnotationSpec.builder(Transient.class).build();
        objectMapper.addAnnotation(_transient);

        fields.add(objectMapper.build());

        List<FieldSpec> columns = enclosedElements.stream()
                .filter(e -> e.getKind().equals(ElementKind.FIELD))
                .map(e -> {
                    if (e.getAnnotation(BitemporalMapping.class) != null) {
                        BitemporalMapping bm = e.getAnnotation(BitemporalMapping.class);
                        if (bm.isList() && bm.isEntity()) {
                            FieldSpec.Builder fieldSpecBuilder = FieldSpec.builder(UUID[].class, firstLetterLowerCase(underlineToCamel(bm.column())));
                            AnnotationSpec.Builder column = AnnotationSpec.builder(Column.class);
                            column.addMember("name", "$S", bm.column());
                            column.addMember("columnDefinition", "$S", "uuid[]");
                            fieldSpecBuilder.addAnnotation(column.build());
                            AnnotationSpec.Builder type = AnnotationSpec.builder(Type.class);
                            type.addMember("type", "$S", "PGUuidArray");
                            fieldSpecBuilder.addAnnotation(type.build());
                            return fieldSpecBuilder.build();
                        } else if (bm.isEntity()) {
                            FieldSpec.Builder fieldSpecBuilder = FieldSpec.builder(UUID.class, firstLetterLowerCase(underlineToCamel(bm.column())));
                            AnnotationSpec.Builder column = AnnotationSpec.builder(Column.class);
                            column.addMember("name", "$S", bm.column());
                            fieldSpecBuilder.addAnnotation(column.build());
                            return fieldSpecBuilder.build();
                        } else {
                            FieldSpec.Builder fieldSpecBuilder = FieldSpec.builder(ClassName.get("com.fasterxml.jackson.databind", "JsonNode"), firstLetterLowerCase(underlineToCamel(bm.column())));
                            AnnotationSpec.Builder column = AnnotationSpec.builder(Column.class);
                            column.addMember("name", "$S", bm.column());
                            column.addMember("columnDefinition", "$S", "jsonb");
                            fieldSpecBuilder.addAnnotation(column.build());
                            AnnotationSpec.Builder type = AnnotationSpec.builder(Type.class);
                            type.addMember("type", "$S", "PGJson");
                            fieldSpecBuilder.addAnnotation(type.build());
                            return fieldSpecBuilder.build();
                        }
                    } else {
                        FieldSpec.Builder fieldSpecBuilder = FieldSpec.builder(TypeName.get(e.asType()), e.getSimpleName().toString());
                        List<? extends Annotation> annotations = supportedJPAAnnotations.stream()
                                .map(annoType -> Optional.ofNullable(e.getAnnotation(annoType)))
                                .flatMap(o -> o.map(Stream::of).orElseGet(Stream::empty))
                                .collect(Collectors.toList());
                        annotations.forEach(a -> {
                            if (a.annotationType()==Column.class && AnnotationSpec.get(a).members.get("name")==null) {
                                String name = camelCaseToUnderScore(e.getSimpleName().toString());
                                AnnotationSpec.Builder annotationSpecBuilder = AnnotationSpec.get(a).toBuilder().addMember("name", CodeBlock.of("$S", name));
                                fieldSpecBuilder.addAnnotation(annotationSpecBuilder.build());
                            } else {
                                fieldSpecBuilder.addAnnotation(AnnotationSpec.get(a));
                            }
                        });
                        return fieldSpecBuilder.build();
                    }
                }).collect(Collectors.toList());

        fields.addAll(columns);

        List<FieldSpec> transients = enclosedElements.stream()
                .filter(e -> {
                    if (!e.getKind().equals(ElementKind.FIELD))
                        return false;
                    BitemporalMapping bm = e.getAnnotation(BitemporalMapping.class);
                    if (bm != null && bm.isEntity()) {
                        return true;
                    } else {
                        return false;
                    }
                })
                .map(e -> {
                    BitemporalMapping bm = e.getAnnotation(BitemporalMapping.class);
                    String packageName = getClassPackageName(bm.isList() ? getListElementType(e) : e.asType().toString()) + "." + Model;
                    String className = getSimpleClassName(bm.isList() ? getListElementType(e) : e.asType().toString());
                    TypeName typeName = ClassName.get(packageName, className);
                    if (bm.isList()) {
                        ParameterizedTypeName nestedFieldTpyeName = ParameterizedTypeName.get(ClassName.get(List.class), typeName);
                        FieldSpec.Builder nestedFieldSpecBuilder = FieldSpec.builder(nestedFieldTpyeName, bm.name()).addAnnotation(AnnotationSpec.builder(Transient.class).build());
                        return nestedFieldSpecBuilder.build();
                    } else {
                        FieldSpec.Builder nestedFieldSpecBuilder = FieldSpec.builder(typeName, bm.name()).addAnnotation(AnnotationSpec.builder(Transient.class).build());
                        return nestedFieldSpecBuilder.build();
                    }
                }).collect(Collectors.toList());

        fields.addAll(transients);

        return fields;
    }



    public List<MethodSpec> generateGetterAndSetter(List<? extends Element> enclosedElements) {
        List<MethodSpec> methodSpecs = new ArrayList();
        enclosedElements.stream()
                .filter(e -> e.getKind().equals(ElementKind.FIELD))
                .forEach(e -> {
                    if (e.getAnnotation(BitemporalMapping.class) != null) {
                        BitemporalMapping bm = e.getAnnotation(BitemporalMapping.class);
                        if (bm.isList() && bm.isEntity()) {
                            String packageName = getClassPackageName(getListElementType(e)) + "." + Model;
                            String className = getSimpleClassName(getListElementType(e));
                            TypeName typeName = ClassName.get(packageName, className);
                            methodSpecs.add(MethodSpec.methodBuilder("get" + firstLetterUpperCase(bm.name()))
                                    .addModifiers(Modifier.PUBLIC)
                                    .returns(ParameterizedTypeName.get(ClassName.get(List.class), typeName))
                                    .addCode("return $N;\n", bm.name())
                                    .build());
                            methodSpecs.add(MethodSpec.methodBuilder("get" + underlineToCamel(bm.column()))
                                    .addModifiers(Modifier.PUBLIC)
                                    .returns(UUID[].class)
                                    .addCode("return $N;\n", firstLetterLowerCase(underlineToCamel(bm.column())))
                                    .build());
                        } else if (bm.isEntity()) {
                            String packageName = getClassPackageName(e.asType().toString()) + "." + Model;
                            String className = getSimpleClassName(e.asType().toString());
                            TypeName typeName = ClassName.get(packageName, className);
                            methodSpecs.add(MethodSpec.methodBuilder("get" + firstLetterUpperCase(bm.name()))
                                    .addModifiers(Modifier.PUBLIC)
                                    .returns(typeName)
                                    .addCode("return $N;\n", bm.name())
                                    .build());
                            methodSpecs.add(MethodSpec.methodBuilder("get" + underlineToCamel(bm.column()))
                                    .addModifiers(Modifier.PUBLIC)
                                    .returns(UUID.class)
                                    .addCode("return $N;\n", firstLetterLowerCase(underlineToCamel(bm.column())))
                                    .build());
                        } else {
                            methodSpecs.add(MethodSpec.methodBuilder("get" + firstLetterUpperCase(bm.name()))
                                    .addModifiers(Modifier.PUBLIC)
                                    .returns(ClassName.get("com.fasterxml.jackson.databind", "JsonNode"))
                                    .addCode("return $N;\n", firstLetterLowerCase(underlineToCamel(bm.column())))
                                    .build());
                            methodSpecs.add(MethodSpec.methodBuilder(firstLetterLowerCase(e.getSimpleName().toString()))
                                    .addModifiers(Modifier.PUBLIC)
                                    .returns(TypeName.get(e.asType()))
                                    .addException(TypeName.get(Exception.class))
                                    .addCode("return objectMapper.readValue($N.toString(), new $T<$T>(){});\n", firstLetterLowerCase(underlineToCamel(bm.column())), ClassName.get("com.fasterxml.jackson.core.type", "TypeReference"), TypeName.get(e.asType()))
                                    .build());
                        }
                    } else {
                        methodSpecs.add(MethodSpec.methodBuilder("get" + firstLetterUpperCase(e.getSimpleName().toString()))
                                .addModifiers(Modifier.PUBLIC)
                                .returns(TypeName.get(e.asType()))
                                .addCode("return $N;\n", e.getSimpleName())
                                .build());
                    }
                });

        enclosedElements.stream()
                .filter(e -> e.getKind().equals(ElementKind.FIELD))
                .forEach(e -> {
                    if (e.getAnnotation(BitemporalMapping.class) != null) {
                        BitemporalMapping bm = e.getAnnotation(BitemporalMapping.class);
                        if (bm.isList() && bm.isEntity()) {
                            String packageName = getClassPackageName(getListElementType(e)) + "." + Model;
                            String className = getSimpleClassName(getListElementType(e));
                            TypeName typeName = ClassName.get(packageName, className);
                            methodSpecs.add(MethodSpec.methodBuilder("set" + firstLetterUpperCase(bm.name()))
                                    .addModifiers(Modifier.PUBLIC)
                                    .addParameter(ParameterizedTypeName.get(ClassName.get(List.class), typeName), bm.name())
                                    .addCode("this.$N = $N;\n", bm.name(), bm.name())
                                    .build());
                            methodSpecs.add(MethodSpec.methodBuilder("set" + underlineToCamel(bm.column()))
                                    .addModifiers(Modifier.PUBLIC)
                                    .addParameter(UUID[].class, firstLetterLowerCase(underlineToCamel(bm.column())))
                                    .addCode("this.$N = $N;\n", firstLetterLowerCase(underlineToCamel(bm.column())), firstLetterLowerCase(underlineToCamel(bm.column())))
                                    .build());
                        } else if (bm.isEntity()) {
                            String packageName = getClassPackageName(e.asType().toString()) + "." + Model;
                            String className = getSimpleClassName(e.asType().toString());
                            TypeName typeName = ClassName.get(packageName, className);
                            methodSpecs.add(MethodSpec.methodBuilder("set" + firstLetterUpperCase(bm.name()))
                                    .addModifiers(Modifier.PUBLIC)
                                    .addParameter(typeName, bm.name())
                                    .addCode("this.$N = $N;\n", bm.name(), bm.name())
                                    .build());
                            methodSpecs.add(MethodSpec.methodBuilder("set" + underlineToCamel(bm.column()))
                                    .addModifiers(Modifier.PUBLIC)
                                    .addParameter(UUID.class, firstLetterLowerCase(underlineToCamel(bm.column())))
                                    .addCode("this.$N = $N;\n", firstLetterLowerCase(underlineToCamel(bm.column())), firstLetterLowerCase(underlineToCamel(bm.column())))
                                    .build());
                        } else {
                            methodSpecs.add(MethodSpec.methodBuilder("set" + firstLetterUpperCase(bm.name()))
                                    .addModifiers(Modifier.PUBLIC)
                                    .addParameter(ClassName.get("com.fasterxml.jackson.databind", "JsonNode"), bm.name())
                                    .addCode("this.$N = $N;\n", firstLetterLowerCase(underlineToCamel(bm.column())), bm.name())
                                    .build());
                            methodSpecs.add(MethodSpec.methodBuilder(firstLetterLowerCase(e.getSimpleName().toString()) + "_")
                                    .addModifiers(Modifier.PUBLIC)
                                    .addParameter(TypeName.get(e.asType()), bm.name())
                                    .addException(TypeName.get(Exception.class))
                                    .addCode("this.$N = objectMapper.readTree(objectMapper.writeValueAsString($N));\n", firstLetterLowerCase(underlineToCamel(bm.column())), bm.name())
                                    .build());
                        }
                    } else {
                        methodSpecs.add(MethodSpec.methodBuilder("set" + firstLetterUpperCase(e.getSimpleName().toString()))
                                .addModifiers(Modifier.PUBLIC)
                                .addParameter(TypeName.get(e.asType()), e.getSimpleName().toString())
                                .addCode("this.$N = $N;\n", e.getSimpleName(), e.getSimpleName())
                                .build());
                    }
                });
        return methodSpecs;
    }

    public String createPackageName(PackageElement pkg, String newPackage) {
        return pkg.isUnnamed() ? "newPackage" : pkg.getQualifiedName().toString() + "." + newPackage;
    }

    public String getSimpleClassName(String fullClassName) {
        return fullClassName.substring(fullClassName.lastIndexOf(".") + 1);
    }

    public String getClassPackageName(String fullClassName) {
        return fullClassName.substring(0, fullClassName.lastIndexOf("."));
    }

    public String getListElementType(Element e) {
        return ((com.sun.tools.javac.code.Type.ClassType) ((Symbol.VarSymbol) e).type).typarams_field.get(0).tsym.toString();
    }

    public String underlineToCamel(String s) {
        return Arrays.stream(s.split("_")).map(e -> {
            char[] chars = e.toCharArray();
            chars[0] = Character.toUpperCase(chars[0]);
            return String.valueOf(chars);
        }).reduce((acc, e) -> acc + e).get();
    }

    public String firstLetterLowerCase(String s) {
        return s.substring(0, 1).toLowerCase() + s.substring(1);
    }

    public String firstLetterUpperCase(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    public List<String> createUniqueConstraintList(TypeElement typeElement) {
        List<String> uniqueConstraintList = new ArrayList<>();

        String bk = typeElement.getEnclosedElements().stream()
                .filter(e -> e.getKind().equals(ElementKind.FIELD) && Optional.ofNullable(e.getAnnotation(BitemporalKey.class)).isPresent())
                .map(e -> "\"" + ("".equals(e.getAnnotation(Column.class).name()) ? camelCaseToUnderScore(e.getSimpleName().toString()) : e.getAnnotation(Column.class).name()) + "\"").collect(Collectors.joining(","));
        if (!bk.isEmpty())
            uniqueConstraintList.add("\n\t@UniqueConstraint(columnNames={" + bk + "})");

        String ui = typeElement.getEnclosedElements().stream()
                .filter(e -> e.getKind().equals(ElementKind.FIELD) && Optional.ofNullable(e.getAnnotation(UniqueIndex.class)).isPresent())
                .map(e -> "\"" + ("".equals(e.getAnnotation(Column.class).name()) ? camelCaseToUnderScore(e.getSimpleName().toString()) : e.getAnnotation(Column.class).name()) + "\"").collect(Collectors.joining(","));
        if (!ui.isEmpty())
            uniqueConstraintList.add("\n\t@UniqueConstraint(columnNames={" + ui + "})");

        String uci = typeElement.getEnclosedElements().stream()
                .filter(e -> e.getKind().equals(ElementKind.FIELD) && Optional.ofNullable(e.getAnnotation(UniqueIndexes.class)).isPresent())
                .map(e -> "\"" + ("".equals(e.getAnnotation(Column.class).name()) ? camelCaseToUnderScore(e.getSimpleName().toString()) : e.getAnnotation(Column.class).name()) + "\"").collect(Collectors.joining(","));
        if (!uci.isEmpty())
            uniqueConstraintList.add("\n\t@UniqueConstraint(columnNames={" + uci + "})");

        String superClassFullName = typeElement.getSuperclass().toString();
        if (!"java.lang.Object".equals(superClassFullName)) {
            TypeElement superClassTypeElement = (TypeElement) ((Symbol.ClassSymbol) typeElement).getSuperclass().tsym;
            uniqueConstraintList.addAll(createUniqueConstraintList(superClassTypeElement));
        }

        return uniqueConstraintList;
    }

    public String createNonUniqueIndexList(TypeElement typeElement) {
        String nonUniqueIndexList = typeElement.getEnclosedElements().stream()
                .filter(e -> e.getKind().equals(ElementKind.FIELD) && Optional.ofNullable(e.getAnnotation(NonUniqueIndex.class)).isPresent())
                .map(e -> "\n\t@Index(columnList=\"" + ("".equals(e.getAnnotation(Column.class).name()) ? camelCaseToUnderScore(e.getSimpleName().toString()) : e.getAnnotation(Column.class).name()) + "\")").collect(Collectors.joining(","));

        return nonUniqueIndexList;
    }

    public String camelCaseToUnderScore(String camelCase) {
        StringBuilder buf = new StringBuilder(camelCase);
        for (int i=1; i<buf.length()-1; i++) {
            if (Character.isLowerCase(buf.charAt(i-1)) &&
                    Character.isUpperCase(buf.charAt(i)) &&
                    Character.isLowerCase(buf.charAt(i+1))) {
                buf.insert(i++, '_');
            }
        }
        return buf.toString().toLowerCase();
    }
}
