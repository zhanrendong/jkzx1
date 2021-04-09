package tech.tongyu.bct.common.util;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class FileUtils {

    public static void delete(String path){
        File file = new File(path);
        if(!file.delete())
            throw new FileUtilsException(String.format("删除文件[%s]失败", path));
    }

    public static String readClassPathFile(String classPath){
        StringBuilder sb = new StringBuilder();
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(FileUtils.class.getClassLoader().getResourceAsStream(classPath), StandardCharsets.UTF_8))){
            String temp;
            while((temp = reader.readLine()) != null){
                sb.append(temp);
            }
            return sb.toString();
        } catch (IOException e){
            throw new FileUtilsException(String.format("无法读取目标文件[%s]", classPath));
        }
    }

    public static List<List<String>> readClassPathCsvFile(String classPath){
        List<List<String>> result = Lists.newArrayList();
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(FileUtils.class.getClassLoader().getResourceAsStream(classPath)))){
            String temp;
            while((temp = reader.readLine()) != null){
                result.add(Arrays.stream(temp.split(","))
                        .map(StringUtils::trim)
                        .collect(Collectors.toList()));
            }
            return result;
        } catch (IOException e){
            throw new FileUtilsException(String.format("无法读取目标文件[%s]", classPath));
        }
    }

    public static File getFile(String location, String filename){
        File file = new File(String.format("%s" + File.separator + "%s", location, filename));
        if(!file.exists()) {
            File fileDir = new File(location);
            if(fileDir.exists()){
                try {
                    if(!file.createNewFile())
                        throw new FileUtilsException("fail to create file");
                } catch (IOException e){
                    throw new RuntimeException(e);
                }
            }
            fileDir.mkdirs();
            try {
                file.createNewFile();
            } catch (IOException e){
                throw new RuntimeException(e);
            }
        }

        return file;
    }

    public static PrintStream getPrintStream(File file){
        try {
            return new PrintStream(file, Charset.forName("utf-8").name());
        } catch (FileNotFoundException e){
            // because file exists, just throw wrapped runtime exception
            throw new RuntimeException(e);
        } catch (UnsupportedEncodingException e){
            // utf-8 will be supported, just throw out runtime exception
            throw new RuntimeException("utf-8 is not supported, please check...");
        }
    }

    public static PrintStream getPrintStream(String location, String filename){
        return getPrintStream(getFile(location, filename));
    }

    public static class FileUtilsException extends RuntimeException{
        FileUtilsException(String message){
            super(message);
        }
    }

    public static String getRelativePath(String... paths){
        if(Objects.isNull(paths) || paths.length == 0)
            throw new FileUtilsException("empty paths");
        StringBuilder sb = new StringBuilder(".");
        for(String pathTemp : paths){
            if(StringUtils.isBlank(pathTemp))
                throw new FileUtilsException("blank path");
            sb.append(String.format("%s%s", File.separator, pathTemp));
        }

        return sb.toString();
    }

    public static List<File> readDirectory(String path){
        if(StringUtils.isBlank(path))
            throw new FileUtilsException("path is blank");
        File file = new File(path);
        List<File> retList = Lists.newArrayList();
        if(!file.isDirectory())
            throw new FileUtilsException(String.format("%s is not a valid directory", path));
        String[] fileList = file.list();
        if(Objects.isNull(fileList))
            return retList;
        for (String tempFileName : fileList){
            File tempFile = new File(path + File.separator + tempFileName);
            if(!tempFile.isDirectory()){
                retList.add(tempFile);
            }
            else{
                List<File> childFileList = readDirectory(tempFile.getPath());
                if(childFileList.size() > 0)
                    retList.addAll(childFileList);
            }
        }

        return retList;
    }

    public static List<String> readDirectoryAsStringList(String path){
        List<File> files = readDirectory(path);
        if(Objects.isNull(files) || files.size() == 0)
            return Lists.newArrayList();
        List<String> retList = Lists.newArrayListWithExpectedSize(files.size());
        files.forEach(file -> {
            StringBuilder sb = new StringBuilder();
            try(BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)))){
                String temp;
                while((temp = reader.readLine()) != null){
                    sb.append(temp);
                }
                temp = sb.toString();
                if(StringUtils.isNotBlank(temp))
                    retList.add(temp);
            } catch (IOException e){
                throw new FileUtilsException(e.getMessage());
            }
        });

        return retList;
    }

    public static String readFileAsString(String path){
        File file = new File(path);
        if(!file.isFile())
            throw new FileUtilsException(String.format("%s is not a normal file!", path));
        StringBuilder sb = new StringBuilder();
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)))){
            String temp;
            while((temp = reader.readLine()) != null){
                sb.append(temp);
            }
            return sb.toString();
        } catch (IOException e){
            throw new FileUtilsException(e.getMessage());
        }
    }

    public static void createFile(String destFileName) {
        File file = new File(destFileName);
        if(file.exists()) {
            if(!file.delete()){
                throw new FileUtilsException("file is exists and delete fail");
            }
        }
        if (destFileName.endsWith(File.separator)) {
            if(!file.mkdirs()){
                throw new FileUtilsException("upper folder create fail");
            }
            return;
        }
        //判断目标文件所在的目录是否存在
        if(!file.getParentFile().exists()) {
            //如果目标文件所在的目录不存在，则创建父目录
            if(!file.getParentFile().mkdirs()) {
                throw new FileUtilsException("upper folder create fail");
            }
        }
        //创建目标文件
        try {
            if (!file.createNewFile()) {
                throw new FileUtilsException("file is create fail");
            }
        } catch (IOException e) {
            throw new FileUtilsException(e.getMessage());
        }
    }
}
