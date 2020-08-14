package com.potato.apicompiler;

import com.google.auto.service.AutoService;
import com.potato.apiannotation.ApiLog;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

/**
 * @Author wangguoli
 * 使用规则：在你的接口方法上加上注解即可（本项目中，在@{com.epoch.rupeeLoan.manager.ApiManager}类中，每个方法上添加注解），注解如下：
 * @ApiLog(nameChinese = "首页信息", nameEnglish = ApiURL.GET_HOME)
 * build之后会自动生成一个Java文件：ApiMap(记录了所有接口)
 */
@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_7)
@SupportedAnnotationTypes({"com.potato.apiannotation.ApiLog"})
public class ApiLogProcessor extends AbstractProcessor {

    private static final boolean IS_DEBUG = true;

    private Filer filerUtils;
    private Map<String, String> groupMap = new TreeMap<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        filerUtils = processingEnvironment.getFiler();
    }

    //处理注解
    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        if (!Utils.isEmpty(set)) {
            Set<? extends Element> apiLogElements = roundEnvironment.getElementsAnnotatedWith(ApiLog.class);
            if (!Utils.isEmpty(apiLogElements)) {
                processApiLog(apiLogElements);
            }
            return true;
        }
        return false;
    }

    private void processApiLog(Set<? extends Element> apiLogElements) {
        for (Element element : apiLogElements) {
            ApiLog apiLog = element.getAnnotation(ApiLog.class);
            String key = apiLog.nameEnglish();
            String value = apiLog.nameChinese();
            groupMap.put(key, value);
        }
        generatedGroup();
    }

    private void generatedGroup() {
        ParameterizedTypeName map = ParameterizedTypeName.get(
                ClassName.get(Map.class),
                ClassName.get(String.class),
                ClassName.get(String.class)
        );
        //generate field
        //public static final Map<String, String> mapApi = new HashMap<>()
        FieldSpec mapApi = FieldSpec.builder(map, "mapApi")
                .addModifiers(PUBLIC, STATIC, FINAL)
                .initializer("new $T<>()", HashMap.class)
                .build();
        //generate Class like public class ApiLogMap {}
        TypeSpec.Builder clazzBuilder = TypeSpec.classBuilder("ApiLogMap");
        clazzBuilder.addModifiers(PUBLIC).addField(mapApi);
        //generate static block
        if (IS_DEBUG) {
            CodeBlock.Builder blockBuilder = CodeBlock.builder();
            //add these codes to static block just like mapApi.put("","")
            for (Map.Entry entry : groupMap.entrySet()) {
                String key = (String) entry.getKey();
                if (!key.contains("{") && !key.equals(""))
                blockBuilder.addStatement("mapApi.put($S, $S)", entry.getKey(), entry.getValue());
            }
            //generate javadoc
            clazzBuilder.addJavadoc("created by Wangguoli.don't delete it,please!!!\nTime: "
                    +DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL, Locale.CHINA).format(new Date())
                    +"\n编译期间记录了" + groupMap.size() + "个接口\n");
            CodeBlock block = blockBuilder.build();
            clazzBuilder.addStaticBlock(block);
        }
        TypeSpec clazzType = clazzBuilder.build();

        JavaFile javaFile = JavaFile.builder("com.potato.apiLogs", clazzType).build();
        try {
            javaFile.writeTo(filerUtils);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
