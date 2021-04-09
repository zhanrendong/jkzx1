package tech.tongyu.bct.workflow.process.generator;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ProcessNumGenerator {

    public String getProcessSequenceNum(){
        return UUID.randomUUID().toString();
    }
}
