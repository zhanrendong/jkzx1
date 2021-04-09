package tech.tongyu.bct.common.util;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CollectionUtils {

    public static boolean isEmpty(Collection<?> collection) {
        return (collection == null || collection.isEmpty());
    }

    public static boolean isNotEmpty(Collection<?> collection){
        return !isEmpty(collection);
    }

    public static boolean isEmpty(Object[] array){
        return ArrayUtils.isEmpty(array);
    }

    public static boolean isNotEmpty(Object[] array){
        return !isEmpty(array);
    }

    public static boolean isEmpty(Object obj) {
        if (Objects.isNull(obj)) return true;
        if (Collection.class.isInstance(obj)) return isEmpty((Collection) obj);
        if(Map.class.isInstance(obj)) return isEmpty((Map) obj);
        return !obj.getClass().isArray() || isEmpty(ObjectUtils.toObjectArray(obj));
    }

    public static boolean isEmpty(Map obj){
        return Objects.isNull(obj) || obj.isEmpty();
    }

    public static boolean isNotEmpty(Object obj) {
        return !isEmpty(obj);
    }

    public static List<String> strArrayToStrList(String[] array){
        if(ArrayUtils.isEmpty(array)) return null;
        return Lists.newArrayList(array);
    }

    public static String[] strListToStrArray(List<String> list){
        if(isEmpty(list)) return null;
        String[] content = new String[list.size()];
        for(int i = 0; i < list.size(); i++){
            content[i] = list.get(i);
        }
        return content;
    }

    public static <T> List<T> toList(Object obj, Class<T> clazz){
        if(Objects.isNull(obj))
            return null;
        if(clazz.isInstance(obj)){
            return Lists.newArrayList((T)obj);
        }else if(List.class.isInstance(obj)){
            return (List<T>) obj;
        }
        throw new UnsupportedOperationException("can't convert target obj to List. " +
                "please check the type.");
    }

    public static List<String> toStrList(Object obj){
        List<String> strList = null;
        if(String.class.isInstance(obj)){
            if(StringUtils.isNotBlank((String)obj)) {
                strList = Lists.newArrayList();
                strList.add((String) obj);
                return strList;
            }
        }else if(List.class.isInstance(obj)){
            List<String> target = (List<String>) obj;
            if(CollectionUtils.isNotEmpty(target)){
                List<Integer> index = Lists.newArrayList();
                for(int i = 0; i < target.size(); i++){
                    if(StringUtils.isBlank(target.get(i)))
                        index.add(i);
                }
                if(CollectionUtils.isNotEmpty(index)){
                    for(int i : index){
                        target.remove(i);
                    }
                }
                if(CollectionUtils.isNotEmpty(target))
                    return target;
            }
        }
        return null;
    }

    public static <T> boolean contains(Collection<T> collection, T element){
        if(CollectionUtils.isNotEmpty(collection)){
            for(T ele : collection){
                if(Objects.equals(ele, element))
                    return true;
            }
        }
        return false;
    }

    public static <T> boolean contains(T[] collection, T element){
        return contains(Lists.newArrayList(collection), element);
    }

    public static void checkOnlyOneInCollection(Collection collection, String id){
        if(CollectionUtils.isEmpty(collection))
            throw new CollectionUtilsException(
                    String.format("no instances found with uuid => %s ???", id));

        if(collection.size() > 1)
            throw new CollectionUtilsException(
                    String.format("multiple instances found with one uuid { %s }???", id));
    }

    public static void checkNotNullCollection(Collection collection, String id){
        if(CollectionUtils.isEmpty(collection))
            throw new CollectionUtilsException(
                    String.format("no instances found with uuid => %s ???", id));
    }

    public static <T> T getFirstElementOrNull(Collection<T> collection, String id){
        if(CollectionUtils.isEmpty(collection))
            return null;

        if(collection.size() > 1)
            throw new CollectionUtilsException(
                    String.format("multiple instances found with one uuid { %s }???", id));

        return collection.iterator().next();
    }

    public static class CollectionUtilsException extends RuntimeException{
        public CollectionUtilsException(String message){
            super(message);
        }
    }
}
