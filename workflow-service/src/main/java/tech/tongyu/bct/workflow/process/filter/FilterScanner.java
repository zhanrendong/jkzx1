package tech.tongyu.bct.workflow.process.filter;

import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.tongyu.bct.common.util.CollectionUtils;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class FilterScanner{

    private ProcessStartableFilterScanner processStartableFilterScanner;
    private TaskCompletableFilterScanner taskCompletableFilterScanner;
    private TaskReadableFilterScanner taskReadableFilterScanner;

    @Autowired
    public FilterScanner(
            ProcessStartableFilterScanner processStartableFilterScanner
            , TaskCompletableFilterScanner taskCompletableFilterScanner
            , TaskReadableFilterScanner taskReadableFilterScanner){
        this.processStartableFilterScanner = processStartableFilterScanner;
        this.taskCompletableFilterScanner = taskCompletableFilterScanner;
        this.taskReadableFilterScanner = taskReadableFilterScanner;
    }

    public ProcessStartableFilter getProcessStartableFilter(String className){
        return processStartableFilterScanner.getProcessStartableFilter(className);
    }

    public TaskReadableFilter getTaskReadableFilter(String className){
        return taskReadableFilterScanner.getTaskReadableFilter(className);
    }

    public TaskCompletableFilter getTaskCompletableFilter(String className){
        return taskCompletableFilterScanner.getTaskCompletableFilter(className);
    }

    public Collection<TaskReadableFilter> getTaskReadableFilters(Collection<String> className){
        if(CollectionUtils.isEmpty(className)) {
            return Sets.newHashSet();
        }
        return className.stream()
                .map(this::getTaskReadableFilter)
                .collect(Collectors.toSet());
    }

    public Collection<TaskCompletableFilter> getTaskCompletableFilters(Collection<String> className){
        if(CollectionUtils.isEmpty(className)) {
            return Sets.newHashSet();
        }
        return className.stream()
                .map(this::getTaskCompletableFilter)
                .collect(Collectors.toSet());
    }

    public Collection<ProcessStartableFilter> getProcessStartableFilters(Collection<String> className){
        if(CollectionUtils.isEmpty(className)) {
            return Sets.newHashSet();
        }
        return className.stream()
                .map(this::getProcessStartableFilter)
                .collect(Collectors.toSet());
    }

    public <T extends Filter> T getFilter(Class<T> clazz, String className){
        if(TaskReadableFilter.class.isAssignableFrom(clazz)) {
            return (T) getTaskReadableFilter(className);
        }
        else if(TaskCompletableFilter.class.isAssignableFrom(clazz)){
            return (T) getTaskCompletableFilter(className);
        } else if(ProcessStartableFilter.class.isAssignableFrom(clazz)){
            return (T) getProcessStartableFilter(className);
        }
        else {
            throw new UnsupportedOperationException("only three filter supported: taskReadableFilter, taskCompletableFilter and processStartableFilter");
        }
    }

}
