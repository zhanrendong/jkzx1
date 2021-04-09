package tech.tongyu.core.processor;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.*;
import com.sun.tools.javac.code.Symbol;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.core.annotation.*;
import tech.tongyu.core.postgres.*;
import tech.tongyu.core.postgres.type.PGJson;
import tech.tongyu.core.postgres.type.PGUuidArray;

import javax.annotation.PostConstruct;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.*;
import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.io.IOException;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@AutoService(Processor.class)
public class BitemporalProcessor extends BaseProcessor {
    private static final String findByKeyCodeTemplate =
            ("return findAll((org.springframework.data.jpa.domain.Specification<%s" +
                    ">) (root, query, cb) -> cb.equal(root.get(\"%s\").as(%s.class), %s))");
    private static final String findEntityByNestedEntityId = "SELECT t.* FROM %s t " +
            "WHERE t.%s %s " +
            "AND t.valid_range @> ? \\:\\:timestamptz " +
            "AND t.system_range @> ? \\:\\:timestamptz";
    private static Logger logger = LoggerFactory.getLogger(BitemporalProcessor.class);

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> supported = new LinkedHashSet<>();
        supported.add(Bitemporal.class.getCanonicalName());
        return supported;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnvironment) {
        try {
            ImportScanner scanner = new ImportScanner();
            for (Element element : roundEnvironment.getElementsAnnotatedWith(Bitemporal.class)) {
                if (element.getKind() != ElementKind.CLASS)
                    throw new ProcessingException(element,
                            "Only class can be annotated with %s", Bitemporal.class.getSimpleName());
                TypeElement annotatedType = (TypeElement) element;
                scanner.scan(annotatedType);

                generateKey(annotatedType);
                generateKeyBean(annotatedType);
                generateKeyRepository(annotatedType);

                PackageElement pkg = this.getElements().getPackageOf(annotatedType);
                String pkgName = createPackageName(pkg, Model);
                String bitemporalClassName = annotatedType.getSimpleName() + BitemporalTypeTag;
                TypeSpec.Builder typeSpecBuilder = TypeSpec.classBuilder(bitemporalClassName);

                boolean hasBitemporalKey = !checkBitemporalKey(annotatedType.getEnclosedElements());

                //Set the super class for the entity, if the entity extends another entity
                String superClassFullName = ((TypeElement) element).getSuperclass().toString();
                if ("java.lang.Object".equals(superClassFullName)) {
                    if (!hasBitemporalKey && ((TypeElement) element).getInterfaces().size() != 0) // TODO only check the entity which implements a scala interface
                        throw new ProcessingException(element,
                                "Non sub class must have %s.class", BitemporalKey.class.getSimpleName());
                    typeSpecBuilder.superclass(BaseBitemporalEntity.class);
                } else {
                    if (hasBitemporalKey)
                        throw new ProcessingException(element,
                                "Sub class cannot have %s.class", BitemporalKey.class.getSimpleName());
                    typeSpecBuilder.superclass(ClassName.get(pkgName, getSimpleClassName(superClassFullName)));
                }

                if (!checkBitemporalMapping(annotatedType.getEnclosedElements()))
                    throw new ProcessingException(element,
                            "The name of %s.class can not be empty", BitemporalMapping.class.getSimpleName());

                List<FieldSpec> fieldSpecs = processColumn(annotatedType.getEnclosedElements());
                typeSpecBuilder.addFields(fieldSpecs);

                List<MethodSpec> methodSpecs = generateGetterAndSetter(annotatedType.getEnclosedElements());
                typeSpecBuilder.addMethods(methodSpecs);

                TypeName entityKeyClass = ClassName.get(pkgName, annotatedType.getSimpleName() + KeyTypeTag);
                FieldSpec.Builder entityKeyField = FieldSpec.builder(entityKeyClass, firstLetterLowerCase(annotatedType.getSimpleName().toString()) + KeyTypeTag).addModifiers(Modifier.PRIVATE);
                AnnotationSpec.Builder manyToOne = AnnotationSpec.builder(ManyToOne.class);
                AnnotationSpec.Builder joinColumn = AnnotationSpec.builder(JoinColumn.class);
                joinColumn.addMember("name", "$S", "entity_id");
                joinColumn.addMember("referencedColumnName", "$S", "entity_id");
                joinColumn.addMember("insertable", "$N", "false");
                joinColumn.addMember("updatable", "$N", "false");
                entityKeyField.addAnnotation(manyToOne.build());
                entityKeyField.addAnnotation(joinColumn.build());
                typeSpecBuilder.addField(entityKeyField.build());

                MethodSpec.Builder getEntityKeyMethod = MethodSpec.methodBuilder("get" + firstLetterUpperCase(annotatedType.getSimpleName().toString()) + KeyTypeTag)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(entityKeyClass)
                        .addCode("return $N;\n", firstLetterLowerCase(annotatedType.getSimpleName().toString()) + KeyTypeTag);
                typeSpecBuilder.addMethod(getEntityKeyMethod.build());

                MethodSpec.Builder setEntityKeyMethod = MethodSpec.methodBuilder("set" + firstLetterUpperCase(annotatedType.getSimpleName().toString()) + KeyTypeTag)
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(entityKeyClass, firstLetterLowerCase(annotatedType.getSimpleName().toString()) + KeyTypeTag)
                        .addCode("this.$N = $N;\n", firstLetterLowerCase(annotatedType.getSimpleName().toString()) + KeyTypeTag, firstLetterLowerCase(annotatedType.getSimpleName().toString()) + KeyTypeTag);
                typeSpecBuilder.addMethod(setEntityKeyMethod.build());

                typeSpecBuilder.addModifiers(Modifier.PUBLIC);
                typeSpecBuilder.addSuperinterface(Serializable.class);
                typeSpecBuilder.addAnnotation(Entity.class);

                AnnotationSpec.Builder inheritance = AnnotationSpec.builder(Inheritance.class);
                inheritance.addMember("strategy", "$T.TABLE_PER_CLASS", InheritanceType.class);
                typeSpecBuilder.addAnnotation(inheritance.build());

                AnnotationSpec.Builder typeDefs = AnnotationSpec.builder(TypeDefs.class);
                typeDefs.addMember("value", "{\n\t@$T(name = $S, typeClass = $T.class),\n\t@$T(name = $S, typeClass = $T.class)\n}", TypeDef.class, "PGJson", PGJson.class,TypeDef.class, "PGUuidArray", PGUuidArray.class);
                typeSpecBuilder.addAnnotation(typeDefs.build());

                String table = element.getAnnotation(Bitemporal.class).table();
                if ("".equals(table)) {
                    table = camelCaseToUnderScore(annotatedType.getSimpleName().toString());
                }

                typeSpecBuilder.addAnnotation(AnnotationSpec.builder(Table.class)
                        .addMember("schema", "$S", element.getAnnotation(Bitemporal.class).schema())
                        .addMember("name", "$S", table + BitemporalTableTag)
                        .addMember("indexes", "{\n\t@$T(columnList = \"entity_id\"),\n\t@Index(columnList = \"valid_range\"),\n\t@Index(columnList = \"system_range\")\n}", Index.class)
                        .build());
                JavaFile.builder(pkgName, typeSpecBuilder.build()).build().writeTo(this.getFiler());

                generateBean(annotatedType);
                generateRepository(annotatedType);
                generateService(annotatedType);
            }
        } catch (ProcessingException e) {
            error(e.getElement(), e.getMessage());
        } catch (IOException e) {
            error(null, e.getMessage());
        }
        return true;
    }

    private boolean generateKey(TypeElement typeElement) {
        try {
            ImportScanner scanner = new ImportScanner();
            scanner.scan(typeElement);
            String entityClassName = typeElement.getSimpleName().toString();
            PackageElement pkg = this.getElements().getPackageOf(typeElement);
            String pkgName = createPackageName(pkg, Model);
            TypeSpec.Builder typeSpecBuilder = TypeSpec.classBuilder(entityClassName + KeyTypeTag);
            typeSpecBuilder.addModifiers(Modifier.PUBLIC);

            String superClassFullName = typeElement.getSuperclass().toString();
            if ("java.lang.Object".equals(superClassFullName)) {
                typeSpecBuilder.superclass(BaseRelationalEntity.class);
            } else {
                typeSpecBuilder.superclass(ClassName.get(pkgName, getSimpleClassName(superClassFullName) + KeyTypeTag));
            }

            typeSpecBuilder.addSuperinterface(Serializable.class);
            typeSpecBuilder.addAnnotation(Entity.class);

            StringBuffer uniqueConstraints = new StringBuffer("{");
            uniqueConstraints.append(createUniqueConstraintList(typeElement).stream().collect(Collectors.joining(",")));
            uniqueConstraints.append("\n}");

            StringBuffer nonUniqueIndexList = new StringBuffer("{");
            nonUniqueIndexList.append(createNonUniqueIndexList(typeElement));
            nonUniqueIndexList.append("\n}");

            String tableName = typeElement.getAnnotation(Bitemporal.class).table();
            if ("".equals(tableName)) {
                tableName = camelCaseToUnderScore(typeElement.getSimpleName().toString());
            }

            AnnotationSpec.Builder table = AnnotationSpec.builder(Table.class)
                    .addMember("schema", "$S", typeElement.getAnnotation(Bitemporal.class).schema())
                    .addMember("name", "$S",tableName + KeyTableTag);
            if (!"{\n}".equals(uniqueConstraints.toString())) {
                table.addMember("uniqueConstraints", uniqueConstraints.toString().replaceFirst("UniqueConstraint", Matcher.quoteReplacement("$T")), UniqueConstraint.class);
            }
            if (!"{\n}".equals(nonUniqueIndexList.toString())) {
                table.addMember("indexes", nonUniqueIndexList.toString().replaceFirst("Index", Matcher.quoteReplacement("$T")), Index.class);
            }
            typeSpecBuilder.addAnnotation(table.build());

            AnnotationSpec.Builder inheritance = AnnotationSpec.builder(Inheritance.class);
            inheritance.addMember("strategy", "$T.TABLE_PER_CLASS", InheritanceType.class);
            typeSpecBuilder.addAnnotation(inheritance.build());

            typeElement.getEnclosedElements().stream()
                    .filter(e ->
                            e.getKind().equals(ElementKind.FIELD) &&
                                    (e.getAnnotation(BitemporalKey.class) != null || e.getAnnotation(UniqueIndex.class) != null || e.getAnnotation(UniqueIndexes.class) != null || e.getAnnotation(NonUniqueIndex.class) != null)) //Move the fields annotated with Bitemporal or UniqueIndex or UniqueIndexes into the Key class
                    .forEach(e -> {
                        TypeName typeName = TypeName.get(e.asType());
                        List<? extends Annotation> annotations =
                                supportedJPAAnnotations.stream()
                                        .map(annoType -> Optional.ofNullable(e.getAnnotation(annoType)))
                                        .flatMap(o -> o.map(Stream::of).orElseGet(Stream::empty))
                                        .collect(Collectors.toList());
                        if (!annotations.isEmpty()) {
                            FieldSpec.Builder fieldSpecBuilder = FieldSpec.builder(typeName, e.getSimpleName().toString());
                            annotations.forEach(a -> {
                                //If Column exists, set the member of 'unique' as the value of 'true, and keep the others same
                                if (AnnotationSpec.get(a).type.equals(TypeName.get(Column.class))) {
                                    AnnotationSpec.Builder annotationSpecBuilder = AnnotationSpec.builder(Column.class);
                                    AnnotationSpec.get(a).members.forEach((m, b) -> {
                                        if (!m.equals("unique") && !m.equals("nullable")) {
                                            b.stream().forEach(cb -> annotationSpecBuilder.addMember(m, cb));
                                        }
                                    });
                                    if (AnnotationSpec.get(a).members.get("name")==null) {
                                        String name = camelCaseToUnderScore(e.getSimpleName().toString());
                                        annotationSpecBuilder.addMember("name", CodeBlock.of("$S", name));
                                    }
                                    annotationSpecBuilder.addMember("nullable", CodeBlock.of("false"));
                                    fieldSpecBuilder.addAnnotation(annotationSpecBuilder.build());
                                } else {
                                    fieldSpecBuilder.addAnnotation(AnnotationSpec.get(a));
                                }
                            });
                            e.getModifiers().forEach(m -> fieldSpecBuilder.addModifiers(m));
                            typeSpecBuilder.addField(fieldSpecBuilder.build());

                            typeSpecBuilder.addMethod(MethodSpec.methodBuilder("set" + firstLetterUpperCase(e.getSimpleName().toString()))
                                    .addModifiers(Modifier.PUBLIC)
                                    .addParameter(TypeName.get(e.asType()), e.getSimpleName().toString())
                                    .addCode("this.$N = $N;\n", e.getSimpleName(), e.getSimpleName())
                                    .build());

                            typeSpecBuilder.addMethod(MethodSpec.methodBuilder("get" + firstLetterUpperCase(e.getSimpleName().toString()))
                                    .addModifiers(Modifier.PUBLIC)
                                    .returns(TypeName.get(e.asType()))
                                    .addCode("return $N;\n", e.getSimpleName())
                                    .build());
                        } else {
                            logger.warn(String.format(
                                    "field %s does not have supported persistance annotation, will be ignored", e.toString()));
                        }
                    });
            JavaFile.builder(pkgName, typeSpecBuilder.build()).build().writeTo(this.getFiler());
        } catch (Exception e) {
            e.printStackTrace();
            error(null, e.getMessage());
        }
        return true;
    }

    private boolean generateKeyBean(TypeElement typeElement) {
        try {
            ImportScanner scanner = new ImportScanner();
            scanner.scan(typeElement);
            String entityClassName = typeElement.getSimpleName().toString();
            PackageElement pkg = this.getElements().getPackageOf(typeElement);
            String pkgName = createPackageName(pkg, Bean);
            TypeSpec.Builder typeSpecBuilder = TypeSpec.classBuilder(entityClassName + KeyTypeTag + BeanTypeTag);
            typeSpecBuilder.addAnnotation(Configuration.class);
            typeSpecBuilder.addModifiers(Modifier.PUBLIC);
            ClassName clazz = ClassName.get("java.lang", "Class");
            ClassName bitemporal = ClassName.get(createPackageName(pkg, Model), entityClassName + KeyTypeTag);
            TypeName returnType = ParameterizedTypeName.get(clazz, bitemporal);

            String table = typeElement.getAnnotation(Bitemporal.class).table();
            if ("".equals(table)) {
                table = camelCaseToUnderScore(entityClassName);
            }

            MethodSpec entityClass = MethodSpec.methodBuilder(entityClassName.substring(0, 1).toLowerCase() + entityClassName.substring(1) + KeyTypeTag + "Class")
                    .addAnnotation(AnnotationSpec.builder(Bean.class).addMember("name", "$S", table + KeyTableTag).build())
                    .addModifiers(Modifier.PUBLIC)
                    .returns(returnType)
                    .addCode("return $T.class;\n", bitemporal)
                    .build();
            typeSpecBuilder.addMethod(entityClass);
            JavaFile.builder(pkgName, typeSpecBuilder.build()).build().writeTo(this.getFiler());
        } catch (Exception e) {
            e.printStackTrace();
            error(null, e.getMessage());
        }
        return true;
    }

    private boolean generateKeyRepository(TypeElement typeElement) {
        try {
            ImportScanner scanner = new ImportScanner();
            scanner.scan(typeElement);
            String entityClassName = typeElement.getSimpleName().toString();
            PackageElement pkg = this.getElements().getPackageOf(typeElement);
            String pkgName = createPackageName(pkg, Repository);
            TypeSpec.Builder typeSpecBuilder = TypeSpec.classBuilder(entityClassName + KeyTypeTag + RepositoryTypeTag);
            typeSpecBuilder.addAnnotation(Repository.class);
            typeSpecBuilder.addModifiers(Modifier.PUBLIC);
            String biTempModelPackageName = createPackageName(pkg, Model);
            String biTempKeyTypeNameStr = entityClassName + KeyTypeTag;

            TypeName biTempKeyTypeName = ClassName.get(biTempModelPackageName, biTempKeyTypeNameStr);
            ParameterizedTypeName superClass = ParameterizedTypeName.get(
                    ClassName.get(RelationalRepository.class), biTempKeyTypeName, ClassName.get(UUID.class));
            typeSpecBuilder.superclass(superClass);

            String table = typeElement.getAnnotation(Bitemporal.class).table();
            if ("".equals(table)) {
                table = camelCaseToUnderScore(entityClassName);
            }

            ParameterSpec domainClassParameterSpec = ParameterSpec.builder(Class.class, "domainClass")
                    .addAnnotation(AnnotationSpec.builder(Qualifier.class)
                            .addMember("value", "$S", table + KeyTableTag)
                            .build())
                    .build();

            ParameterSpec entityManagerParameterSpec = ParameterSpec.builder(EntityManager.class, "entityManager")
                    .build();

            MethodSpec constructor = MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(domainClassParameterSpec)
                    .addParameter(entityManagerParameterSpec)
                    .addStatement("super($N, $N)", "domainClass", "entityManager").build();
            typeSpecBuilder.addMethod(constructor);

            typeSpecBuilder.addMethods(generateKeyRepositoryMethods(typeElement));

            JavaFile.builder(pkgName, typeSpecBuilder.build()).build().writeTo(this.getFiler());
        } catch (Exception e) {
            e.printStackTrace();
            error(null, e.getMessage());
        }
        return true;
    }

    private boolean generateBean(TypeElement typeElement) {
        try {
            ImportScanner scanner = new ImportScanner();
            scanner.scan(typeElement);
            String entityClassName = typeElement.getSimpleName().toString();
            PackageElement pkg = this.getElements().getPackageOf(typeElement);
            String pkgName = createPackageName(pkg, Bean);
            TypeSpec.Builder typeSpecBuilder = TypeSpec.classBuilder(entityClassName + BitemporalTypeTag + BeanTypeTag);
            typeSpecBuilder.addAnnotation(Configuration.class);
            typeSpecBuilder.addModifiers(Modifier.PUBLIC);
            ClassName clazz = ClassName.get("java.lang", "Class");
            ClassName bitemporal = ClassName.get(createPackageName(pkg, Model), entityClassName + BitemporalTypeTag);
            TypeName returnType = ParameterizedTypeName.get(clazz, bitemporal);

            String table = typeElement.getAnnotation(Bitemporal.class).table();
            if ("".equals(table)) {
                table = camelCaseToUnderScore(entityClassName);
            }

            MethodSpec entityClass = MethodSpec.methodBuilder(entityClassName.substring(0, 1).toLowerCase() + entityClassName.substring(1) + BitemporalTypeTag + "Class")
                    .addAnnotation(AnnotationSpec.builder(Bean.class).addMember("name", "$S", table).build())
                    .addModifiers(Modifier.PUBLIC)
                    .returns(returnType)
                    .addCode("return $T.class;\n", bitemporal)
                    .build();
            typeSpecBuilder.addMethod(entityClass);
            JavaFile.builder(pkgName, typeSpecBuilder.build()).build().writeTo(this.getFiler());
        } catch (Exception e) {
            e.printStackTrace();
            error(null, e.getMessage());
        }
        return true;
    }

    private boolean generateRepository(TypeElement typeElement) {
        try {
            ImportScanner scanner = new ImportScanner();
            scanner.scan(typeElement);
            String entityClassName = typeElement.getSimpleName().toString();
            PackageElement pkg = this.getElements().getPackageOf(typeElement);
            String pkgName = createPackageName(pkg, Repository);
            TypeSpec.Builder typeSpecBuilder = TypeSpec.classBuilder(entityClassName + BitemporalTypeTag + RepositoryTypeTag);
            typeSpecBuilder.addAnnotation(Repository.class);
            typeSpecBuilder.addModifiers(Modifier.PUBLIC);

            String biTempModelPackageName = createPackageName(pkg, Model);
            String biTempEntityTypeNameStr = entityClassName + BitemporalTypeTag;
            TypeName biTempEntityTypeName = ClassName.get(biTempModelPackageName, biTempEntityTypeNameStr);
            ParameterizedTypeName superClass = ParameterizedTypeName.get(
                    ClassName.get(BitemporalRepository.class), biTempEntityTypeName, ClassName.get(UUID.class));
            typeSpecBuilder.superclass(superClass);

            FieldSpec dynamicProxy = FieldSpec.builder(ClassName.get(pkgName, entityClassName + BitemporalTypeTag + RepositoryTypeTag), "dynamicProxy")
                    .addModifiers(Modifier.PRIVATE)
                    .addModifiers(Modifier.STATIC).build();
            typeSpecBuilder.addField(dynamicProxy);

            MethodSpec init = MethodSpec.methodBuilder("init")
                    .addModifiers(Modifier.PUBLIC)
                    .addStatement("dynamicProxy = this")
                    .addAnnotation(PostConstruct.class).build();
            typeSpecBuilder.addMethod(init);

            String table = typeElement.getAnnotation(Bitemporal.class).table();
            if ("".equals(table)) {
                table = camelCaseToUnderScore(entityClassName);
            }

            ParameterSpec domainClassParameterSpec = ParameterSpec.builder(Class.class, "domainClass")
                    .addAnnotation(AnnotationSpec.builder(Qualifier.class)
                            .addMember("value", "$S", table)
                            .build())
                    .build();

            ParameterSpec entityManagerParameterSpec = ParameterSpec.builder(EntityManager.class, "entityManager")
                    .build();

            MethodSpec constructor = MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(domainClassParameterSpec)
                    .addParameter(entityManagerParameterSpec)
                    .addStatement("super($N, $N)", "domainClass", "entityManager").build();
            typeSpecBuilder.addMethod(constructor);

            //Autowire all necessary Type, Key & Mapping Repositories
            String keyRepositoryClassName = entityClassName + KeyTypeTag + RepositoryTypeTag;
            FieldSpec.Builder keyRepository = FieldSpec.builder(ClassName.get(pkgName, keyRepositoryClassName), firstLetterLowerCase(keyRepositoryClassName)).addModifiers(Modifier.PRIVATE);
            AnnotationSpec autowired = AnnotationSpec.builder(Autowired.class).build();
            keyRepository.addAnnotation(autowired);
            typeSpecBuilder.addField(keyRepository.build());

            typeElement.getEnclosedElements().stream()
                    .filter(e -> {
                        if (!e.getKind().equals(ElementKind.FIELD))
                            return false;
                        BitemporalMapping bm = e.getAnnotation(BitemporalMapping.class);
                        if (bm != null && bm.isEntity() && !getSimpleClassName(bm.isList() ? getListElementType(e) : e.asType().toString()).equals(entityClassName)) {
                            return true;
                        } else {
                            return false;
                        }
                    })
                    .map(e -> {
                        BitemporalMapping bm = e.getAnnotation(BitemporalMapping.class);
                        return getSimpleClassName(bm.isList() ? getListElementType(e) : e.asType().toString());
                    })
                    .distinct()
                    .forEach(e -> {
                        String typeRepositoryName = e + RepositoryTypeTag;
                        FieldSpec.Builder typeRepository = FieldSpec.builder(ClassName.get(pkgName, typeRepositoryName), firstLetterLowerCase(typeRepositoryName)).addModifiers(Modifier.PRIVATE);
                        typeRepository.addAnnotation(autowired);
                        typeSpecBuilder.addField(typeRepository.build());
                        String typeKeyRepositoryName = e + KeyTypeTag + RepositoryTypeTag;
                        FieldSpec.Builder typeKeyRepository = FieldSpec.builder(ClassName.get(pkgName, typeKeyRepositoryName), firstLetterLowerCase(typeKeyRepositoryName)).addModifiers(Modifier.PRIVATE);
                        typeKeyRepository.addAnnotation(autowired);
                        typeSpecBuilder.addField(typeKeyRepository.build());
                    });

            //Add the method "upsert" to save the whole entity
            MethodSpec.Builder upsert = MethodSpec.methodBuilder("upsert")
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(ParameterSpec.builder(ClassName.get(biTempModelPackageName, entityClassName), "entity").build())
                    .addParameter(int.class, "status")
                    .addParameter(UUID.class, "applicationEventId")
                    .addParameter(OffsetDateTime.class, "valid");

            //Set the super class for the entity, if the entity extends another entity
            String superClassFullName = typeElement.getSuperclass().toString();
            if (!"java.lang.Object".equals(superClassFullName)) {
                String superClassSimpleClassName = getSimpleClassName(superClassFullName);

                FieldSpec.Builder superClassRepository = FieldSpec.builder(ClassName.get(pkgName, superClassSimpleClassName + RepositoryTypeTag), "superClassRepository").addModifiers(Modifier.PRIVATE);
                superClassRepository.addAnnotation(autowired);
                typeSpecBuilder.addField(superClassRepository.build());

                upsert.addCode("Class superClass = $T.class;\n", ClassName.get(biTempModelPackageName, superClassSimpleClassName))
                        .addCode("$T superClassEntity = new $T();\n", ClassName.get(biTempModelPackageName, superClassSimpleClassName), ClassName.get(biTempModelPackageName, superClassSimpleClassName))
                        .addCode("$T[] superClassFields = Class.forName($S).getDeclaredFields();\n", Field.class, biTempModelPackageName + "." + superClassSimpleClassName)
                        .addCode("Class clazz = $T.class;\n", ClassName.get(biTempModelPackageName, entityClassName))
                        .addCode("for(Field field: superClassFields) {\n")
                        .addCode("\tField f1 = clazz.getDeclaredField(field.getName());\n")
                        .addCode("\tf1.setAccessible(true);\n")
                        .addCode("\tField f2 = superClass.getDeclaredField(field.getName());\n")
                        .addCode("\tf2.setAccessible(true);\n")
                        .addCode("\tf2.set(superClassEntity, f1.get(entity));\n")
                        .addCode("}\n")
                        .addCode("superClassRepository.upsert(superClassEntity, status, applicationEventId, valid);\n");
            }

            upsert.addCode("\n$T now = $T.now();\n\n", OffsetDateTime.class, OffsetDateTime.class)
                    .addCode("$T $N = new $T();\n", ClassName.get(biTempModelPackageName, entityClassName + KeyTypeTag), firstLetterLowerCase(entityClassName + KeyTypeTag), ClassName.get(biTempModelPackageName, entityClassName + KeyTypeTag))
                    .addCode("$T<$T> $NList = dynamicProxy.$N.key(entity);\n", List.class, ClassName.get(biTempModelPackageName, entityClassName + KeyTypeTag), firstLetterLowerCase(entityClassName + KeyTypeTag), firstLetterLowerCase(entityClassName + KeyTypeTag + RepositoryTypeTag))
                    .addCode("if ($NList.isEmpty()) {\n", firstLetterLowerCase(entityClassName + KeyTypeTag))
                    .addCode("\t$N.setEntityId(UUID.randomUUID());\n", firstLetterLowerCase(entityClassName + KeyTypeTag));
            typeElement.getEnclosedElements().stream()
                    .filter(e -> e.getKind().equals(ElementKind.FIELD) && (e.getAnnotation(BitemporalKey.class) != null || e.getAnnotation(UniqueIndex.class) != null || e.getAnnotation(UniqueIndexes.class) != null))
                    .forEach(e ->
                            upsert.addCode("\t$N.set$N(entity.get$N());\n", firstLetterLowerCase(entityClassName + KeyTypeTag), firstLetterUpperCase(e.getSimpleName().toString()), firstLetterUpperCase(e.getSimpleName().toString()))
                    );
            upsert.addCode("} else {\n")
                    .addCode("\t$N = $NList.get(0);\n", firstLetterLowerCase(entityClassName + KeyTypeTag), firstLetterLowerCase(entityClassName + KeyTypeTag));
            typeElement.getEnclosedElements().stream()
                    .filter(e -> e.getKind().equals(ElementKind.FIELD) && (e.getAnnotation(UniqueIndex.class) != null || e.getAnnotation(UniqueIndexes.class) != null))
                    .forEach(e ->
                            upsert.addCode("\t$N.set$N(entity.get$N());\n", firstLetterLowerCase(entityClassName + KeyTypeTag), firstLetterUpperCase(e.getSimpleName().toString()), firstLetterUpperCase(e.getSimpleName().toString()))
                    );
            upsert.addCode("}\n");
            upsert.addCode("dynamicProxy.$NRepository.save($N);\n", firstLetterLowerCase(entityClassName + KeyTypeTag), firstLetterLowerCase(entityClassName + KeyTypeTag));

            typeElement.getEnclosedElements().stream()
                    .filter(e -> {
                        if (!e.getKind().equals(ElementKind.FIELD))
                            return false;
                        BitemporalMapping bm = e.getAnnotation(BitemporalMapping.class);
                        if (bm != null) {
                            if (bm.isEntity() && !getSimpleClassName(e.asType().toString()).equals(entityClassName)) {
                                return true;
                            } else {
                                return false;
                            }
                        } else {
                            return false;
                        }
                    })
                    .forEach(e -> {
                        BitemporalMapping a = e.getAnnotation(BitemporalMapping.class);

                        String typeName = getSimpleClassName(a.isList() ? getListElementType(e) : e.asType().toString());
                        String fieldName = a.name();
                        String columnName = underlineToCamel(a.column());

                        upsert.addCode("\n$T[] $NFields = Class.forName($S).getDeclaredFields();\n", Field.class, firstLetterLowerCase(fieldName), pkg.getQualifiedName().toString() + "." + typeName)
                                .addCode("Class $NClass = $T.class;\n", firstLetterLowerCase(fieldName), ClassName.get(biTempModelPackageName, typeName))
                                .addCode("Class $NClass = $T.class;\n", firstLetterLowerCase("_" + fieldName), ClassName.get(biTempModelPackageName, typeName + KeyTypeTag));

                        if (a.isList() && a.isEntity()) {
                            upsert.addCode("\nif (entity.get$N() == null && entity.get$N() != null) {\n", firstLetterUpperCase(columnName), firstLetterUpperCase(fieldName))
                                    .addCode("\t$T<$T> $N = new $T();\n", List.class, UUID.class, firstLetterLowerCase(underlineToCamel(a.column())), ArrayList.class)
                                    .addCode("\tfor($T $N: entity.get$N()) {\n", ClassName.get(biTempModelPackageName, typeName), firstLetterLowerCase(typeName), firstLetterUpperCase(fieldName))
                                    .addCode("\t\t$T $N = new $T();\n", ClassName.get(biTempModelPackageName, typeName + KeyTypeTag), firstLetterLowerCase(typeName + KeyTypeTag), ClassName.get(biTempModelPackageName, typeName + KeyTypeTag))
                                    .addCode("\t\t$T<$T> $NList = dynamicProxy.$N.key($N);\n", List.class, ClassName.get(biTempModelPackageName, typeName + KeyTypeTag), firstLetterLowerCase(typeName + KeyTypeTag), firstLetterLowerCase(typeName + KeyTypeTag + RepositoryTypeTag), firstLetterLowerCase(typeName))
                                    .addCode("\t\tif ($NList.isEmpty()) {\n", firstLetterLowerCase(typeName + KeyTypeTag))
                                    .addCode("\t\t\t$N.setEntityId(UUID.randomUUID());\n", firstLetterLowerCase(typeName + KeyTypeTag))
                                    .addCode("\t\t\tfor(Field field: $NFields) {\n", firstLetterLowerCase(fieldName))
                                    .addCode("\t\t\t\tif (field.getAnnotation($T.class) != null || field.getAnnotation($T.class) != null || field.getAnnotation($T.class) != null) {\n", BitemporalKey.class, UniqueIndex.class, UniqueIndexes.class)
                                    .addCode("\t\t\t\t\tField f1 = $NClass.getDeclaredField(field.getName());\n", firstLetterLowerCase(fieldName))
                                    .addCode("\t\t\t\t\tf1.setAccessible(true);\n")
                                    .addCode("\t\t\t\t\tField f2 = $NClass.getDeclaredField(field.getName());\n", firstLetterLowerCase("_" + fieldName))
                                    .addCode("\t\t\t\t\tf2.setAccessible(true);\n")
                                    .addCode("\t\t\t\t\tf2.set($N, f1.get($N));\n", firstLetterLowerCase(typeName + KeyTypeTag), firstLetterLowerCase(typeName))
                                    .addCode("\t\t\t\t}\n")
                                    .addCode("\t\t\t}\n")
                                    .addCode("\t\t} else {\n")
                                    .addCode("\t\t\t$N = $NList.get(0);\n", firstLetterLowerCase(typeName + KeyTypeTag), firstLetterLowerCase(typeName + KeyTypeTag))
                                    .addCode("\t\t\tfor(Field field: $NFields) {\n", firstLetterLowerCase(fieldName))
                                    .addCode("\t\t\t\tif (field.getAnnotation($T.class) != null || field.getAnnotation($T.class) != null) {\n", UniqueIndex.class, UniqueIndexes.class)
                                    .addCode("\t\t\t\t\tField f1 = $NClass.getDeclaredField(field.getName());\n", firstLetterLowerCase(fieldName))
                                    .addCode("\t\t\t\t\tf1.setAccessible(true);\n")
                                    .addCode("\t\t\t\t\tField f2 = $NClass.getDeclaredField(field.getName());\n", firstLetterLowerCase("_" + fieldName))
                                    .addCode("\t\t\t\t\tf2.setAccessible(true);\n")
                                    .addCode("\t\t\t\t\tf2.set($N, f1.get($N));\n", firstLetterLowerCase(typeName + KeyTypeTag), firstLetterLowerCase(typeName))
                                    .addCode("\t\t\t\t}\n")
                                    .addCode("\t\t\t}\n")
                                    .addCode("\t\t}\n")
                                    .addCode("\t\tdynamicProxy.$N.save($N);\n", firstLetterLowerCase(typeName + KeyTypeTag + RepositoryTypeTag), firstLetterLowerCase(typeName + KeyTypeTag))
                                    .addCode("\n\t\t$N.setEntityId($N.getEntityId());\n", firstLetterLowerCase(typeName), firstLetterLowerCase(typeName + KeyTypeTag))
                                    .addCode("\t\tdynamicProxy.$N.upsert($N, status, applicationEventId, valid);\n", firstLetterLowerCase(typeName + RepositoryTypeTag), firstLetterLowerCase(typeName))
                                    .addCode("\t\t$N.add($N.getEntityId());\n", firstLetterLowerCase(underlineToCamel(a.column())), firstLetterLowerCase(typeName + KeyTypeTag))
                                    .addCode("\t}\n")
                                    .addCode("\tentity.set$N($N.toArray((new $T[$N.size()])));\n", firstLetterUpperCase(underlineToCamel(a.column())), firstLetterLowerCase(underlineToCamel(a.column())), UUID.class, firstLetterLowerCase(underlineToCamel(a.column())))
                                    .addCode("}\n");
                        } else {
                            upsert.addCode("\nif (entity.get$N() == null && entity.get$N() != null) {\n", firstLetterUpperCase(columnName), firstLetterUpperCase(fieldName))
                                    .addCode("\t$T $N = entity.get$N();\n", ClassName.get(biTempModelPackageName, typeName), firstLetterLowerCase(typeName), firstLetterUpperCase(fieldName))
                                    .addCode("\t$T $N = new $T();\n", ClassName.get(biTempModelPackageName, typeName + KeyTypeTag), firstLetterLowerCase(typeName + KeyTypeTag), ClassName.get(biTempModelPackageName, typeName + KeyTypeTag))
                                    .addCode("\t$T<$T> $NList = dynamicProxy.$N.key(entity.get$N());\n", List.class, ClassName.get(biTempModelPackageName, typeName + KeyTypeTag), firstLetterLowerCase(typeName + KeyTypeTag), firstLetterLowerCase(typeName + KeyTypeTag + RepositoryTypeTag), firstLetterUpperCase(fieldName))
                                    .addCode("\tif ($NList.isEmpty()) {\n", firstLetterLowerCase(typeName + KeyTypeTag))
                                    .addCode("\t\t$N.setEntityId(UUID.randomUUID());\n", firstLetterLowerCase(typeName + KeyTypeTag))
                                    .addCode("\t\tfor(Field field: $NFields) {\n", firstLetterLowerCase(fieldName))
                                    .addCode("\t\t\tif (field.getAnnotation($T.class) != null || field.getAnnotation($T.class) != null || field.getAnnotation($T.class) != null) {\n", BitemporalKey.class, UniqueIndex.class, UniqueIndexes.class)
                                    .addCode("\t\t\t\tField f1 = $NClass.getDeclaredField(field.getName());\n", firstLetterLowerCase(fieldName))
                                    .addCode("\t\t\t\tf1.setAccessible(true);\n")
                                    .addCode("\t\t\t\tField f2 = $NClass.getDeclaredField(field.getName());\n", firstLetterLowerCase("_" + fieldName))
                                    .addCode("\t\t\t\tf2.setAccessible(true);\n")
                                    .addCode("\t\t\t\tf2.set($N, f1.get($N));\n", firstLetterLowerCase(typeName + KeyTypeTag), firstLetterLowerCase(typeName))
                                    .addCode("\t\t\t}\n")
                                    .addCode("\t\t}\n")
                                    .addCode("\t} else {\n")
                                    .addCode("\t\t$N = $NList.get(0);\n", firstLetterLowerCase(typeName + KeyTypeTag), firstLetterLowerCase(typeName + KeyTypeTag))
                                    .addCode("\t\tfor(Field field: $NFields) {\n", firstLetterLowerCase(fieldName))
                                    .addCode("\t\t\tif (field.getAnnotation($T.class) != null || field.getAnnotation($T.class) != null) {\n", UniqueIndex.class, UniqueIndexes.class)
                                    .addCode("\t\t\t\tField f1 = $NClass.getDeclaredField(field.getName());\n", firstLetterLowerCase(fieldName))
                                    .addCode("\t\t\t\tf1.setAccessible(true);\n")
                                    .addCode("\t\t\t\tField f2 = $NClass.getDeclaredField(field.getName());\n", firstLetterLowerCase("_" + fieldName))
                                    .addCode("\t\t\t\tf2.setAccessible(true);\n")
                                    .addCode("\t\t\t\tf2.set($N, f1.get($N));\n", firstLetterLowerCase(typeName + KeyTypeTag), firstLetterLowerCase(typeName))
                                    .addCode("\t\t\t}\n")
                                    .addCode("\t\t}\n")
                                    .addCode("\t}\n")
                                    .addCode("\tdynamicProxy.$N.save($N);\n", firstLetterLowerCase(typeName + KeyTypeTag + RepositoryTypeTag), firstLetterLowerCase(typeName + KeyTypeTag))
                                    .addCode("\n\t$N.setEntityId($N.getEntityId());\n", firstLetterLowerCase(typeName), firstLetterLowerCase(typeName + KeyTypeTag));
                            if (entityClassName.equals(typeName)) {
                                upsert.addCode("\tupsert($N, status, applicationEventId, valid);\n", firstLetterLowerCase(typeName));
                            } else {
                                upsert.addCode("\tdynamicProxy.$N.upsert($N, status, applicationEventId, valid);\n", firstLetterLowerCase(typeName + RepositoryTypeTag), firstLetterLowerCase(typeName));
                            }
                            upsert.addCode("\tentity.set$N($N.getEntityId());\n", firstLetterUpperCase(underlineToCamel(a.column())), firstLetterLowerCase(typeName + KeyTypeTag))
                                    .addCode("}\n");
                        }
                    });

            upsert.addCode("\nentity.setEntityId($N.getEntityId());\n", firstLetterLowerCase(entityClassName + KeyTypeTag));
            upsert.addCode("insert(entity, status, applicationEventId, valid);\n");
            upsert.addAnnotation(AnnotationSpec.builder(Transactional.class).build());
            upsert.addException(TypeName.get(Exception.class));
            typeSpecBuilder.addMethod(upsert.build());

            String finalTable = table;
            typeElement.getEnclosedElements().stream()
                    .filter(e -> {
                        if (!e.getKind().equals(ElementKind.FIELD))
                            return false;
                        BitemporalMapping bm = e.getAnnotation(BitemporalMapping.class);
                        return bm != null && bm.isEntity();
                    })
                    .forEach(e -> {
                        BitemporalMapping bm = e.getAnnotation(BitemporalMapping.class);
                        MethodSpec.Builder find = MethodSpec.methodBuilder("find" + entityClassName + "By" + firstLetterUpperCase(underlineToCamel(bm.column())))
                                .addModifiers(Modifier.PUBLIC)
                                .returns(ParameterizedTypeName.get(ClassName.get(List.class), biTempEntityTypeName))
                                .addParameter(UUID.class, "uuid")
                                .addParameter(Timestamp.class, "valid")
                                .addParameter(Timestamp.class, "system")
                                .addCode("$T sql = $S;\n", String.class, String.format(findEntityByNestedEntityId, finalTable + BitemporalTableTag, bm.column(), (bm.isList() ? "@> ARRAY[?]" : "= ?")))
                                .addCode("$T query = entityManager.createNativeQuery(sql, domainClass);\n", Query.class)
                                .addCode("query.setParameter(1, uuid);\n")
                                .addCode("query.setParameter(2, valid);\n")
                                .addCode("query.setParameter(3, system);\n")
                                .addCode("return query.getResultList();\n");

                        find.addAnnotation(AnnotationSpec.builder(Transactional.class).build());
                        typeSpecBuilder.addMethod(find.build());
                    });

            JavaFile.builder(pkgName, typeSpecBuilder.build()).build().writeTo(this.getFiler());
        } catch (Exception e) {
            e.printStackTrace();
            error(null, e.getMessage());
        }
        return true;
    }

    private boolean generateService(TypeElement typeElement) {
        try {
            ImportScanner scanner = new ImportScanner();
            scanner.scan(typeElement);
            String entityClassName = typeElement.getSimpleName().toString();
            PackageElement pkg = this.getElements().getPackageOf(typeElement);

            String biTempRepoPackageName = createPackageName(pkg, Repository);
            String biTempModelPackageName = createPackageName(pkg, Model);

            String biTempEntityTypeNameStr = entityClassName + BitemporalTypeTag;
            String biTempKeyTypeNameStr = entityClassName + KeyTypeTag;
            String biTempRepoTypeNameStr = entityClassName + BitemporalTypeTag + RepositoryTypeTag;
            String biTempKeyRepoTypeNameStr = entityClassName + KeyTypeTag + RepositoryTypeTag;

            TypeName biTempRepoTypeName = ClassName.get(biTempRepoPackageName, biTempRepoTypeNameStr);
            TypeName biTempKeyRepoTypeName = ClassName.get(biTempRepoPackageName, biTempKeyRepoTypeNameStr);
            TypeName biTempKeyTypeName = ClassName.get(biTempModelPackageName, biTempKeyTypeNameStr);
            TypeName biTempEntityTypeName = ClassName.get(biTempModelPackageName, biTempEntityTypeNameStr);

            String pkgName = createPackageName(pkg, Service);
            TypeSpec.Builder typeSpecBuilder = TypeSpec.classBuilder(entityClassName + BitemporalTypeTag + "Service");
            typeSpecBuilder.addAnnotation(Service.class);
            typeSpecBuilder.addModifiers(Modifier.PUBLIC);

            ParameterizedTypeName interfaceType = ParameterizedTypeName.get(
                    ClassName.get(BaseBitemporalService.class),
                    biTempEntityTypeName, biTempKeyTypeName, biTempKeyRepoTypeName, biTempRepoTypeName);
            typeSpecBuilder.addSuperinterface(interfaceType);

            String biTempRepoFieldName = toCamelCase(entityClassName, BitemporalTypeTag + RepositoryTypeTag);
            FieldSpec biTempFieldSpec = FieldSpec
                    .builder(biTempRepoTypeName, biTempRepoFieldName)
                    .addModifiers(Modifier.PUBLIC)
                    .addAnnotation(Autowired.class).build();
            typeSpecBuilder.addField(biTempFieldSpec);

            String biTempKeyRepoFieldName = toCamelCase(entityClassName, KeyTypeTag + RepositoryTypeTag);
            FieldSpec biTempKeyFieldSpec = FieldSpec
                    .builder(biTempKeyRepoTypeName, biTempKeyRepoFieldName)
                    .addModifiers(Modifier.PUBLIC)
                    .addAnnotation(Autowired.class).build();
            typeSpecBuilder.addField(biTempKeyFieldSpec);

            MethodSpec biTempKeyReopMethodSpec = MethodSpec.methodBuilder("keyRepo")
                    .addModifiers(Modifier.PUBLIC)
                    .returns(biTempKeyRepoTypeName)
                    .addStatement("return " + biTempKeyRepoFieldName).build();
            typeSpecBuilder.addMethod(biTempKeyReopMethodSpec);

            MethodSpec biTempReopMethodSpec = MethodSpec.methodBuilder("bitempRepo")
                    .addModifiers(Modifier.PUBLIC)
                    .returns(biTempRepoTypeName)
                    .addStatement("return " + biTempRepoFieldName).build();
            typeSpecBuilder.addMethod(biTempReopMethodSpec);

            JavaFile.builder(pkgName, typeSpecBuilder.build()).build().writeTo(this.getFiler());
        } catch (Exception e) {
            e.printStackTrace();
            error(null, e.getMessage());
        }
        return true;
    }

    private Map<String, List<List<Element>>> getKeyGroupedFieldList(TypeElement typeElement) {
        Map<String, List<List<Element>>> fieldList = new HashMap<>();

        List<Element> bk = new ArrayList<>();
        typeElement.getEnclosedElements().stream()
                .filter(e -> e.getKind().equals(ElementKind.FIELD) && Optional.ofNullable(e.getAnnotation(BitemporalKey.class)).isPresent())
                .forEach(e -> bk.add(e));
        List<List<Element>> bkList = new ArrayList<>();
        bkList.add(bk);
        fieldList.put(BitemporalKey.class.getSimpleName(), bkList);

        List<Element> ui = new ArrayList<>();
        typeElement.getEnclosedElements().stream()
                .filter(e -> e.getKind().equals(ElementKind.FIELD) && Optional.ofNullable(e.getAnnotation(UniqueIndex.class)).isPresent())
                .forEach(e -> ui.add(e));
        List<List<Element>> uiList = new ArrayList<>();
        uiList.add(ui);
        fieldList.put(UniqueIndex.class.getSimpleName(), uiList);

        List<Element> uis = new ArrayList<>();
        typeElement.getEnclosedElements().stream()
                .filter(e -> e.getKind().equals(ElementKind.FIELD) && Optional.ofNullable(e.getAnnotation(UniqueIndexes.class)).isPresent())
                .forEach(e -> uis.add(e));
        List<List<Element>> uisList = new ArrayList<>();
        uisList.add(uis);
        fieldList.put(UniqueIndexes.class.getSimpleName(), uisList);

        String superClassFullName = typeElement.getSuperclass().toString();
        if (!"java.lang.Object".equals(superClassFullName)) {
            TypeElement superClassTypeElement = (TypeElement) ((Symbol.ClassSymbol) typeElement).getSuperclass().tsym;
            Map<String, List<List<Element>>> superFieldList = getKeyGroupedFieldList(superClassTypeElement);
            fieldList.get(BitemporalKey.class.getSimpleName()).addAll(superFieldList.get(BitemporalKey.class.getSimpleName()));
            fieldList.get(UniqueIndex.class.getSimpleName()).addAll(superFieldList.get(UniqueIndex.class.getSimpleName()));
            fieldList.get(UniqueIndexes.class.getSimpleName()).addAll(superFieldList.get(UniqueIndexes.class.getSimpleName()));
        }

        return fieldList;
    }

    private List<MethodSpec> generateKeyRepositoryMethods(TypeElement typeElement) {
        List<MethodSpec> methodSpecList = new ArrayList<>();

        Map<String, List<List<Element>>> fieldList = getKeyGroupedFieldList(typeElement);

        ImportScanner scanner = new ImportScanner();
        scanner.scan(typeElement);
        String entityClassName = typeElement.getSimpleName().toString();
        PackageElement pkg = this.getElements().getPackageOf(typeElement);
        String pkgName = createPackageName(pkg, Repository);
        TypeSpec.Builder typeSpecBuilder = TypeSpec.classBuilder(entityClassName + KeyTypeTag + RepositoryTypeTag);
        typeSpecBuilder.addAnnotation(Repository.class);
        typeSpecBuilder.addModifiers(Modifier.PUBLIC);

        String biTempModelPackageName = createPackageName(pkg, Model);
        String biTempKeyTypeNameStr = entityClassName + KeyTypeTag;

        TypeName biTempKeyTypeName = ClassName.get(biTempModelPackageName, biTempKeyTypeNameStr);
        TypeName biTempTypeName = ClassName.get(biTempModelPackageName, entityClassName);

        ParameterizedTypeName queryReturnTypeName = ParameterizedTypeName.get(ClassName.get(List.class), biTempKeyTypeName);
        ParameterizedTypeName queryInputTypeName = ParameterizedTypeName.get(ClassName.get(List.class), biTempTypeName);

        fieldList.forEach((k, v) -> {
            if (BitemporalKey.class.getSimpleName().equals(k)) {
                MethodSpec.Builder key = MethodSpec.methodBuilder("key")
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(ParameterSpec.builder(biTempTypeName, "entity").build())
                        .returns(queryReturnTypeName)
                        .addCode("return findAll(new $T<$T>() {\n", Specification.class, biTempKeyTypeName)
                        .addCode("\t@$T\n", Override.class)
                        .addCode("\tpublic $T toPredicate($T<$T> root, $T<?> query, $T cb) {\n", Predicate.class, Root.class, biTempKeyTypeName, CriteriaQuery.class, CriteriaBuilder.class)
                        .addCode("\t\t$T<$T> list = new $T();\n", List.class, Predicate.class, ArrayList.class);
                v.stream().filter(e -> !e.isEmpty()).forEach(e ->
                        e.stream().forEach(element -> key.addCode("\t\tlist.add(cb.equal(root.get($S).as($T.class), entity.get$N()));\n", element.getSimpleName(), element.asType(), firstLetterUpperCase((element.getSimpleName().toString()))))
                );
                key.addCode("\t\t$T[] p = new $T[list.size()];\n", Predicate.class, Predicate.class)
                        .addCode("\t\tquery.where(cb.and(list.toArray(p)));\n")
                        .addCode("\t\treturn query.getRestriction();\n")
                        .addCode("\t}\n")
                        .addCode("});\n");
                methodSpecList.add(key.build());

                MethodSpec.Builder method = MethodSpec.methodBuilder("keys")
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(ParameterSpec.builder(queryInputTypeName, "entities").build())
                        .returns(queryReturnTypeName)
                        .addCode("return findAll(new $T<$T>() {\n", Specification.class, biTempKeyTypeName)
                        .addCode("\t@$T\n", Override.class)
                        .addCode("\tpublic $T toPredicate($T<$T> root, $T<?> query, $T cb) {\n", Predicate.class, Root.class, biTempKeyTypeName, CriteriaQuery.class, CriteriaBuilder.class)
                        .addCode("\t\t$T<$T> predicateList = entities.stream().map(entity -> {\n", List.class, Predicate.class)
                        .addCode("\t\t\t$T<$T> list = new $T();\n", List.class, Predicate.class, ArrayList.class);
                v.stream().filter(e -> !e.isEmpty()).forEach(e ->
                        e.stream().forEach(element -> method.addCode("\t\t\tlist.add(cb.equal(root.get($S).as($T.class), entity.get$N()));\n", element.getSimpleName(), element.asType(), firstLetterUpperCase((element.getSimpleName().toString()))))
                );
                method.addCode("\t\t\t$T[] predicates = new $T[list.size()];\n", Predicate.class, Predicate.class)
                        .addCode("\t\t\treturn cb.and(list.toArray(predicates));\n")
                        .addCode("\t\t}).collect($T.toList());\n", Collectors.class)
                        .addCode("\t\t$T[] predicateArray = new $T[predicateList.size()];\n", Predicate.class, Predicate.class)
                        .addCode("\t\tquery.where(cb.or(predicateList.toArray(predicateArray)));\n")
                        .addCode("\t\treturn query.getRestriction();\n")
                        .addCode("\t}\n")
                        .addCode("});\n");
                methodSpecList.add(method.build());
            }
            if (UniqueIndex.class.getSimpleName().equals(k)) {
                v.stream().filter(e -> !e.isEmpty()).forEach(e ->
                        e.stream().forEach(element -> {
                            String methodName = "findBy" + firstLetterUpperCase(element.getSimpleName().toString());
                            TypeName keyType = TypeName.get(element.asType());
                            String codeBlock = String.format(findByKeyCodeTemplate, ((ClassName) biTempKeyTypeName).simpleName(), element.getSimpleName(), element.asType(), element.getSimpleName());
                            MethodSpec methodSpec = MethodSpec.methodBuilder(methodName)
                                    .addModifiers(Modifier.PUBLIC)
                                    .returns(queryReturnTypeName)
                                    .addParameter(keyType, element.getSimpleName().toString())
                                    .addStatement(codeBlock)
                                    .build();
                            methodSpecList.add(methodSpec);
                        })
                );

                v.stream().filter(e -> !e.isEmpty()).forEach(e ->
                        e.stream().forEach(element -> {
                            String methodName = "findBy" + firstLetterUpperCase(element.getSimpleName().toString());
                            MethodSpec.Builder method = MethodSpec.methodBuilder(methodName)
                                    .addModifiers(Modifier.PUBLIC)
                                    .addParameter(ParameterSpec.builder(queryInputTypeName, "entities").build())
                                    .returns(queryReturnTypeName)
                                    .addCode("return findAll(new $T<$T>() {\n", Specification.class, biTempKeyTypeName)
                                    .addCode("\t@$T\n", Override.class)
                                    .addCode("\tpublic $T toPredicate($T<$T> root, $T<?> query, $T cb) {\n", Predicate.class, Root.class, biTempKeyTypeName, CriteriaQuery.class, CriteriaBuilder.class)
                                    .addCode("\t\t$T<$T> list = new $T();\n", List.class, Predicate.class, ArrayList.class)
                                    .addCode("\t\tentities.stream().forEach(entity -> list.add(cb.equal(root.get($S).as($T.class), entity.get$N())));\n", element.getSimpleName(), element.asType(), firstLetterUpperCase((element.getSimpleName().toString())))
                                    .addCode("\t\t$T[] p = new $T[list.size()];\n", Predicate.class, Predicate.class)
                                    .addCode("\t\tquery.where(cb.or(list.toArray(p)));\n")
                                    .addCode("\t\treturn query.getRestriction();\n")
                                    .addCode("\t}\n")
                                    .addCode("});\n");
                            methodSpecList.add(method.build());
                        })
                );
            }
            if (UniqueIndexes.class.getSimpleName().equals(k)) {
                v.stream().filter(e -> !e.isEmpty()).forEach(e -> {
                    String methodName = "findBy" + e.stream().map(element -> firstLetterUpperCase(element.getSimpleName().toString())).collect(Collectors.joining("And"));
                    List<ParameterSpec> parameterSpecs = e.stream().map(element -> ParameterSpec.builder(TypeName.get(element.asType()), element.getSimpleName().toString()).build()).collect(Collectors.toList());
                    MethodSpec.Builder method = MethodSpec.methodBuilder(methodName)
                            .addModifiers(Modifier.PUBLIC)
                            .addParameters(parameterSpecs)
                            .returns(queryReturnTypeName)
                            .addCode("return findAll(new $T<$T>() {\n", Specification.class, biTempKeyTypeName)
                            .addCode("\t@$T\n", Override.class)
                            .addCode("\tpublic $T toPredicate($T<$T> root, $T<?> query, $T cb) {\n", Predicate.class, Root.class, biTempKeyTypeName, CriteriaQuery.class, CriteriaBuilder.class)
                            .addCode("\t\t$T<$T> list = new $T();\n", List.class, Predicate.class, ArrayList.class);
                    e.stream().forEach(element -> method.addCode("\t\tlist.add(cb.equal(root.get($S).as($T.class), $N));\n", element.getSimpleName(), element.asType(), element.getSimpleName()));
                    method.addCode("\t\t$T[] p = new $T[list.size()];\n", Predicate.class, Predicate.class)
                            .addCode("\t\tquery.where(cb.and(list.toArray(p)));\n")
                            .addCode("\t\treturn query.getRestriction();\n")
                            .addCode("\t}\n")
                            .addCode("});\n");
                    methodSpecList.add(method.build());
                });


                v.stream().filter(e -> !e.isEmpty()).forEach(e -> {
                    String methodName = "findBy" + e.stream().map(element -> firstLetterUpperCase(element.getSimpleName().toString())).collect(Collectors.joining("And"));
                    MethodSpec.Builder method = MethodSpec.methodBuilder(methodName)
                            .addModifiers(Modifier.PUBLIC)
                            .addParameter(ParameterSpec.builder(queryInputTypeName, "entities").build())
                            .returns(queryReturnTypeName)
                            .addCode("return findAll(new $T<$T>() {\n", Specification.class, biTempKeyTypeName)
                            .addCode("\t@$T\n", Override.class)
                            .addCode("\tpublic $T toPredicate($T<$T> root, $T<?> query, $T cb) {\n", Predicate.class, Root.class, biTempKeyTypeName, CriteriaQuery.class, CriteriaBuilder.class)
                            .addCode("\t\t$T<$T> predicateList = entities.stream().map(entity -> {\n", List.class, Predicate.class)
                            .addCode("\t\t\t$T<$T> list = new $T();\n", List.class, Predicate.class, ArrayList.class);
                    e.stream().forEach(element -> method.addCode("\t\t\tlist.add(cb.equal(root.get($S).as($T.class), $N));\n", element.getSimpleName(), element.asType(), element.getSimpleName()));
                    method.addCode("\t\t\t$T[] predicates = new $T[list.size()];\n", Predicate.class, Predicate.class)
                            .addCode("\t\t\treturn cb.and(list.toArray(predicates));\n")
                            .addCode("\t\t}).collect($T.toList());\n", Collectors.class)
                            .addCode("\t\t$T[] predicateArray = new $T[predicateList.size()];\n", Predicate.class, Predicate.class)
                            .addCode("\t\tquery.where(cb.or(predicateList.toArray(predicateArray)));\n")
                            .addCode("\t\treturn query.getRestriction();\n")
                            .addCode("\t}\n")
                            .addCode("});\n");
                    methodSpecList.add(method.build());
                });
            }
        });

        return methodSpecList;
    }

    private String toCamelCase(String first, String second) {
        List<Character> chars = new ArrayList<>();
        char[] str1 = first.toCharArray();
        char c1 = str1[0];
        chars.add(Character.toLowerCase(c1));
        for (int i = 1; i < str1.length; i++) {
            chars.add(str1[i]);
        }
        char[] str2 = second.toCharArray();
        char c2 = str2[0];
        chars.add(Character.toUpperCase(c2));
        for (int i = 1; i < str2.length; i++) {
            chars.add(str2[i]);
        }
        String str = chars.stream().map(e -> e.toString()).reduce((acc, e) -> acc + e).get();
        return str;
    }

    private boolean checkBitemporalKey(List<? extends Element> enclosedElements) {
        return enclosedElements.stream().filter(e -> e.getKind().equals(ElementKind.FIELD) && Optional.ofNullable(e.getAnnotation(BitemporalKey.class)).isPresent()).count() == 0;
    }

    private boolean checkBitemporalMapping(List<? extends Element> enclosedElements) {
        return enclosedElements.stream().filter(e -> e.getKind().equals(ElementKind.FIELD) && e.getAnnotation(BitemporalMapping.class) != null && "".equals(e.getAnnotation(BitemporalMapping.class).name())).count() == 0;
    }
}