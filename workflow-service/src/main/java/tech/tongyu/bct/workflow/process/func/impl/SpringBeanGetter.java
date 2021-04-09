package tech.tongyu.bct.workflow.process.func.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import tech.tongyu.bct.workflow.exceptions.ReturnMessageAndTemplateDef;
import tech.tongyu.bct.workflow.exceptions.WorkflowCommonException;
import tech.tongyu.bct.workflow.process.filter.*;
import tech.tongyu.bct.workflow.process.func.*;

import java.util.Objects;

@Component
public class SpringBeanGetter implements ActionScanner, ModifierScanner, ApplicationContextAware
        , TaskReadableFilterScanner, TaskCompletableFilterScanner, ProcessStartableFilterScanner
        , TaskActionScanner {

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    private static Logger logger = LoggerFactory.getLogger(SpringBeanGetter.class);

    @Override
    @SuppressWarnings("unchecked")
    public Action getAction(String className) {
        try{
            Class<? extends Action> actionClass = (Class<? extends Action>) Class.forName(className);
            if(Objects.isNull(applicationContext))
                throw new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.SPRING_EMPTY_CONTEXT);
            return applicationContext.getBean(actionClass);
        } catch (ClassNotFoundException e){
            e.printStackTrace();
            throw new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.UNKNOWN_ACTION_CLASS, className);
        } catch (NoSuchBeanDefinitionException e){
            e.printStackTrace();
            throw new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.ACTION_OBJECT_NOT_FOUND_IN_SPRING_CONTAINER, className);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Modifier getModifier(String className) {
        try{
            Class<? extends Modifier> modifierClass = (Class<? extends Modifier>) Class.forName(className);
            if(Objects.isNull(applicationContext))
                throw new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.SPRING_EMPTY_CONTEXT);
            return applicationContext.getBean(modifierClass);
        } catch (ClassNotFoundException e){
            e.printStackTrace();
            throw new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.UNKNOWN_MODIFIER_CLASS, className);
        } catch (NoSuchBeanDefinitionException e){
            e.printStackTrace();
            throw new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.MODIFIER_OBJECT_NOT_FOUND_IN_SPRING_CONTAINER, className);
        }
    }

    private <T> T getTargetObject(String className, ReturnMessageAndTemplateDef.Errors classNotFoundError, ReturnMessageAndTemplateDef.Errors noSuchBeanDefError){
        try{
            Class<? extends T> targetClass = (Class<? extends T>) Class.forName(className);
            if(Objects.isNull(applicationContext))
                throw new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.SPRING_EMPTY_CONTEXT);

            return applicationContext.getBean(targetClass);
        } catch (ClassNotFoundException e){
            e.printStackTrace();
            throw new WorkflowCommonException(classNotFoundError, className);
        } catch (NoSuchBeanDefinitionException e){
            e.printStackTrace();
            throw new WorkflowCommonException(noSuchBeanDefError, className);
        }
    }

    @Override
    public ProcessStartableFilter getProcessStartableFilter(String className) {
        return getTargetObject(className
                , ReturnMessageAndTemplateDef.Errors.UNKNOWN_PROCESS_STARTABLE_FILTER
                , ReturnMessageAndTemplateDef.Errors.PROCESS_STARTABLE_FILTER_NOT_FOUND_IN_SPRING_CONTAINER
        );
    }

    @Override
    public TaskCompletableFilter getTaskCompletableFilter(String className) {
        return getTargetObject(className
                , ReturnMessageAndTemplateDef.Errors.UNKNOWN_TASK_COMPLETABLE_FILTER
                , ReturnMessageAndTemplateDef.Errors.TASK_COMPLETABLE_FILTER_NOT_FOUND_IN_SPRING_CONTAINER
        );
    }

    @Override
    public TaskReadableFilter getTaskReadableFilter(String className) {
        return getTargetObject(className
                , ReturnMessageAndTemplateDef.Errors.UNKNOWN_TASK_READABLE_FILTER
                , ReturnMessageAndTemplateDef.Errors.TASK_READABLE_FILTER_NOT_FOUND_IN_SPRING_CONTAINER
        );
    }

    @Override
    public TaskAction getTaskAction(String className) {
        return getTargetObject(className
                , ReturnMessageAndTemplateDef.Errors.UNKNOWN_TASK_ACTION
                , ReturnMessageAndTemplateDef.Errors.TASK_ACTION_NOT_FOUND_IN_SPRING_CONTAINER
        );
    }

}
