package tech.tongyu.core.processor;

import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.*;
import com.sun.tools.javac.code.Symbol;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import tech.tongyu.core.annotation.*;
import tech.tongyu.core.postgres.BaseRelationalEntity;
import tech.tongyu.core.postgres.BaseRelationalRepository;
import tech.tongyu.core.postgres.type.PGJson;
import tech.tongyu.core.postgres.type.PGUuidArray;

import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.*;
import javax.persistence.*;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

@AutoService(Processor.class)
public class RelationalProcessor extends BaseProcessor {

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> supported = new LinkedHashSet<>();
        supported.add(Relational.class.getCanonicalName());
        return supported;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnvironment) {
        try {
            ImportScanner scanner = new ImportScanner();
            for (Element element : roundEnvironment.getElementsAnnotatedWith(Relational.class)) {
                if (element.getKind() != ElementKind.CLASS)
                    throw new ProcessingException(element,
                            "Only class can be annotated with %s", Relational.class.getSimpleName());
                TypeElement annotatedType = (TypeElement) element;
                scanner.scan(annotatedType);
                PackageElement pkg = this.getElements().getPackageOf(annotatedType);

                //if the annotation of Bitemporal exists, Relational processor will only generate the Relational schema of the entity
                boolean hasBitemporal = Optional.ofNullable(element.getAnnotation(Bitemporal.class)).isPresent();
                String pkgName = hasBitemporal ? createPackageName(pkg, Schema) : createPackageName(pkg, Model);
                String relationalClassName = hasBitemporal ? annotatedType.getSimpleName() + RelationalTypeTag : annotatedType.getSimpleName().toString();
                TypeSpec.Builder typeSpecBuilder = TypeSpec.classBuilder(relationalClassName);

                List<FieldSpec> fieldSpecs = processColumn(annotatedType.getEnclosedElements());
                typeSpecBuilder.addFields(fieldSpecs);

                List<MethodSpec> methodSpecs = generateGetterAndSetter(annotatedType.getEnclosedElements());
                typeSpecBuilder.addMethods(methodSpecs);

                String superClassFullName = ((TypeElement) element).getSuperclass().toString();
                if ("java.lang.Object".equals(superClassFullName)) {
                    typeSpecBuilder.superclass(BaseRelationalEntity.class);
                } else {
                    String simpleClassName = getSimpleClassName(superClassFullName);
                    ClassName superClass = ClassName.get(pkgName, hasBitemporal ? simpleClassName + RelationalTypeTag : simpleClassName);
                    typeSpecBuilder.superclass(superClass);
                }

                typeSpecBuilder.addModifiers(Modifier.PUBLIC);
                typeSpecBuilder.addAnnotation(Entity.class);

                StringBuffer uniqueConstraints = new StringBuffer("{");
                uniqueConstraints.append(createUniqueConstraintList(annotatedType).stream().collect(Collectors.joining(",")));
                uniqueConstraints.append("\n}");

                String tableName = element.getAnnotation(Relational.class).table();
                if ("".equals(tableName)) {
                    tableName = camelCaseToUnderScore(annotatedType.getSimpleName().toString());
                }

                AnnotationSpec.Builder table = AnnotationSpec.builder(Table.class)
                        .addMember("schema", "$S", element.getAnnotation(Relational.class).schema())
                        .addMember("name", "$S", tableName + RelationalTableTag);
                if (!"{\n}".equals(uniqueConstraints.toString())) {
                    table.addMember("uniqueConstraints", uniqueConstraints.toString().replaceFirst("UniqueConstraint", Matcher.quoteReplacement("$T")), UniqueConstraint.class);
                }
                typeSpecBuilder.addAnnotation(table.build());

                AnnotationSpec.Builder inheritance = AnnotationSpec.builder(Inheritance.class);
                inheritance.addMember("strategy", "$T.TABLE_PER_CLASS", InheritanceType.class);
                typeSpecBuilder.addAnnotation(inheritance.build());

                AnnotationSpec.Builder typeDefs = AnnotationSpec.builder(TypeDefs.class);
                typeDefs.addMember(
                        "value",
                        "{\n\t@$T(name = $S, typeClass = $T.class),\n\t@$T(name = $S, typeClass = $T.class)\n}",
                        TypeDef.class, "PGJson", PGJson.class, TypeDef.class, "PGUuidArray", PGUuidArray.class);
                typeSpecBuilder.addAnnotation(typeDefs.build());

                TypeSpec entitySpec = typeSpecBuilder.build();
                JavaFile.builder(pkgName, entitySpec).build().writeTo(this.getFiler());
                ClassName relationalEntityClsName = ClassName.get(pkgName, relationalClassName);
                generateRepository(relationalEntityClsName, annotatedType);
            }
        } catch (ProcessingException e) {
            error(e.getElement(), e.getMessage());
        } catch (IOException e) {
            error(null, e.getMessage());
        }
        return true;
    }

    private boolean generateRepository(ClassName entityClsName, TypeElement typeElement) {
        try {
            ImportScanner scanner = new ImportScanner();
            scanner.scan(typeElement);
            String entityClassName = typeElement.getSimpleName().toString();
            PackageElement pkg = this.getElements().getPackageOf(typeElement);
            String pkgName = createPackageName(pkg, Repository);
            TypeSpec.Builder typeSpecBuilder = TypeSpec.interfaceBuilder(entityClassName + RelationalRepositoryTypeTag);
            typeSpecBuilder.addAnnotation(Repository.class);
            typeSpecBuilder.addModifiers(Modifier.PUBLIC);
            ParameterizedTypeName superRepoInterface = ParameterizedTypeName.get(
                    ClassName.get(BaseRelationalRepository.class), entityClsName, TypeName.get(UUID.class));
            typeSpecBuilder.addSuperinterface(superRepoInterface);
            List<MethodSpec> queryMethods = typeElement.getEnclosedElements().stream()
                    .filter(elem -> Optional.ofNullable(elem.getAnnotation(QueryTemplate.class)).isPresent())
                    .map(e -> generateMethodSpecFromMethodSymbol(entityClsName, (Symbol.MethodSymbol) e))
                    .collect(Collectors.toList());
            queryMethods.add(generateFindByEntityIdMethodSpec(entityClsName));
            typeSpecBuilder.addMethods(queryMethods);
            TypeSpec repoType = typeSpecBuilder.build();
            TypeSpec serviceType = generateService(typeElement, queryMethods, repoType);
            String serviceTypePkg = createPackageName(pkg, Service);
            JavaFile.builder(pkgName, repoType).build().writeTo(this.getFiler());
            JavaFile.builder(serviceTypePkg, serviceType).build().writeTo(this.getFiler());
        } catch (Exception e) {
            e.printStackTrace();
            error(null, e.getMessage());
        }
        return true;
    }

    private MethodSpec generateMethodSpecFromMethodSymbol(ClassName entityType, Symbol.MethodSymbol method) {
        String methodName = method.name.toString();
        TypeName returnType;
        String retTypeStr = method.getReturnType().toString();
        if (retTypeStr.equals(List.class.getTypeName())) {
            returnType = ParameterizedTypeName.get(ClassName.get(List.class), entityType);
        } else if (retTypeStr.equals(Optional.class.getTypeName())) {
            returnType = ParameterizedTypeName.get(ClassName.get(Optional.class), entityType);
        } else {
            returnType = entityType;
        }
        QueryTemplate annot = method.getAnnotation(QueryTemplate.class);
        AnnotationSpec annotationSpec = AnnotationSpec.builder(QueryTemplate.class)
                .addMember("description", "$S", annot.description())
                .build();
        List<ParameterSpec> params = method.getParameters().stream().map(param -> {
            TypeName paramType = TypeName.get(param.asType());
            String parameterName = param.name.toString();
            AnnotationSpec paramAnnotSpec = Optional.ofNullable(param.getAnnotation(QueryParam.class))
                    .map(paramAnnot -> AnnotationSpec.builder(QueryParam.class)
                            .addMember("description", "$S", paramAnnot.description())
                            .addMember("translator", "$S", paramAnnot.translator())
                            .addMember("required", "$L", paramAnnot.required())
                            .build()
                    ).orElse(AnnotationSpec.builder(QueryParam.class).build());
            return ParameterSpec.builder(paramType, parameterName, Modifier.FINAL)
                    .addAnnotation(paramAnnotSpec).build();
        }).collect(Collectors.toList());
        return MethodSpec.methodBuilder(methodName)
                .addAnnotation(annotationSpec)
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .returns(returnType)
                .addParameters(params)
                .build();
    }

    private MethodSpec generateFindByEntityIdMethodSpec(ClassName entityType) {
        String methodName = "findByEntityId";
        TypeName returnType = ParameterizedTypeName.get(ClassName.get(Optional.class), entityType);
        AnnotationSpec annotationSpec = AnnotationSpec.builder(QueryTemplate.class)
                .addMember("description", "$S", "通过ID获取相应实例")
                .build();
        AnnotationSpec paramAnnotSpec = AnnotationSpec.builder(QueryParam.class)
                .addMember("description", "$S", "实例ID")
                .addMember("required", "$L", true)
                .build();
        ParameterSpec parameterSpec = ParameterSpec.builder(UUID.class, "entityId", Modifier.FINAL)
                    .addAnnotation(paramAnnotSpec).build();
        return MethodSpec.methodBuilder(methodName)
                .addAnnotation(annotationSpec)
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .returns(returnType)
                .addParameter(parameterSpec)
                .build();
    }


    private TypeSpec generateService(TypeElement typeElement, List<MethodSpec> queries, TypeSpec repoType) {
        PackageElement pkg = this.getElements().getPackageOf(typeElement);
        String repoPkg = createPackageName(pkg, Repository);
        String entityClassName = typeElement.getSimpleName().toString();
        String repoServiceName = entityClassName + RepositoryServiceTypeTag;
        TypeSpec.Builder typeSpecBuilder = TypeSpec.classBuilder(repoServiceName).addModifiers(Modifier.PUBLIC);
        typeSpecBuilder.addAnnotation(org.springframework.stereotype.Service.class);
        ClassName repoServiceClsName = ClassName.get(repoPkg, repoType.name);
        String relationalRepoName = firstLetterLowerCase(repoServiceClsName.simpleName());
        FieldSpec fieldSpec = FieldSpec.builder(repoServiceClsName, relationalRepoName, Modifier.PUBLIC)
                .addAnnotation(Autowired.class).build();
        typeSpecBuilder.addField(fieldSpec);
        List<MethodSpec> apis = queries.stream()
                .map(query -> generateApiMethod(entityClassName, query, relationalRepoName))
                .collect(Collectors.toList());
        typeSpecBuilder.addMethods(apis);
        return typeSpecBuilder.build();
    }

    private MethodSpec generateApiMethod(String entityClassName, MethodSpec query, String repoParamName) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder(query.name + "From" + firstLetterUpperCase(entityClassName));
        MethodCallExpr call = new MethodCallExpr(new NameExpr(repoParamName), query.name);
        query.parameters.forEach(p -> call.addArgument(p.name));
        ReturnStmt st = new ReturnStmt(call);
        List<AnnotationSpec> annotationSpecs = query.annotations.stream()
                .filter(a -> a.type.equals(ClassName.get(QueryTemplate.class)))
                .map(a -> {
                    AnnotationSpec.Builder ab = AnnotationSpec.builder(QueryApi.class);
                    a.members.entrySet().forEach(e -> e.getValue().forEach(v -> ab.addMember(e.getKey(), v)));
                    return ab.build();
                }).collect(Collectors.toList());
        return builder
                .addAnnotations(annotationSpecs)
                .addModifiers(Modifier.PUBLIC)
                .returns(query.returnType)
                .addParameters(query.parameters)
                .addCode(st.toString() + "\n").build();
    }
}
