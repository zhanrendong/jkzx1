//package tech.tongyu.core.processor;
//
//import com.google.auto.service.AutoService;
//import com.squareup.javapoet.*;
//import org.hibernate.annotations.TypeDef;
//import org.hibernate.annotations.TypeDefs;
//import tech.tongyu.core.annotation.Temporal;
//import tech.tongyu.core.postgres.BaseTemporalEntity;
//import tech.tongyu.core.postgres.type.PGJson;
//import tech.tongyu.core.postgres.type.PGUuidArray;
//
//import javax.annotation.processing.Processor;
//import javax.annotation.processing.RoundEnvironment;
//import javax.lang.model.element.*;
//import javax.persistence.Entity;
//import javax.persistence.Inheritance;
//import javax.persistence.InheritanceType;
//import javax.persistence.Table;
//import java.io.IOException;
//import java.io.Serializable;
//import java.util.LinkedHashSet;
//import java.util.List;
//import java.util.Set;
//
//@AutoService(Processor.class)
//public class TemporalProcessor extends BaseProcessor {
//
//    @Override
//    public Set<String> getSupportedAnnotationTypes() {
//        Set<String> supported = new LinkedHashSet<>();
//        supported.add(Temporal.class.getCanonicalName());
//        return supported;
//    }
//
//    @Override
//    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnvironment) {
//        try {
//            ImportScanner scanner = new ImportScanner();
//            for (Element element : roundEnvironment.getElementsAnnotatedWith(Temporal.class)) {
//                if (element.getKind() != ElementKind.CLASS)
//                    throw new ProcessingException(element,
//                            "Only class can be annotated with %s", Temporal.class.getSimpleName());
//                TypeElement annotatedType = (TypeElement) element;
//                scanner.scan(annotatedType);
//                PackageElement pkg = this.getElements().getPackageOf(annotatedType);
//                String pkgName = createPackageName(pkg, Schema);
//                String temporalClassName = annotatedType.getSimpleName() + TemporalTypeTag;
//                TypeSpec.Builder typeSpecBuilder = TypeSpec.classBuilder(temporalClassName);
//
//                List<FieldSpec> fieldSpecs = processColumn(annotatedType.getEnclosedElements());
//                typeSpecBuilder.addFields(fieldSpecs);
//
//                List<MethodSpec> methodSpecs = generateGetterAndSetter(annotatedType.getEnclosedElements());
//                typeSpecBuilder.addMethods(methodSpecs);
//
//                String superClassFullName = ((TypeElement) element).getSuperclass().toString();
//                if ("java.lang.Object".equals(superClassFullName)) {
//                    typeSpecBuilder.superclass(BaseTemporalEntity.class);
//                } else {
//                    typeSpecBuilder.superclass(ClassName.get(pkgName, getSimpleClassName(superClassFullName) + TemporalTypeTag));
//                }
//
//                typeSpecBuilder.addModifiers(Modifier.PUBLIC);
//                typeSpecBuilder.addSuperinterface(Serializable.class);
//                typeSpecBuilder.addAnnotation(Entity.class);
//
//                AnnotationSpec.Builder inheritance = AnnotationSpec.builder(Inheritance.class);
//                inheritance.addMember("strategy", "$T.TABLE_PER_CLASS", InheritanceType.class);
//                typeSpecBuilder.addAnnotation(inheritance.build());
//
//                AnnotationSpec.Builder typeDefs = AnnotationSpec.builder(TypeDefs.class);
//                typeDefs.addMember("value", "{\n\t@$T(name = $S, typeClass = $T.class),\n\t@$T(name = $S, typeClass = $T.class)\n}", TypeDef.class, "PGJson", PGJson.class,TypeDef.class, "PGUuidArray", PGUuidArray.class);
//                typeSpecBuilder.addAnnotation(typeDefs.build());
//
//                typeSpecBuilder.addAnnotation(AnnotationSpec.builder(Table.class).addMember("name", "$S", element.getAnnotation(Temporal.class).table() + TemporalTableTag).build());
//                JavaFile.builder(pkgName, typeSpecBuilder.build()).build().writeTo(this.getFiler());
//            }
//        } catch (ProcessingException e) {
//            error(e.getElement(), e.getMessage());
//        } catch (IOException e) {
//            error(null, e.getMessage());
//        }
//        return true;
//    }
//}
