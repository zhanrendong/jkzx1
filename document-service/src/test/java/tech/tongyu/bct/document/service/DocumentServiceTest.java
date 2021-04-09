package tech.tongyu.bct.document.service;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import tech.tongyu.bct.common.util.JsonUtils;
import tech.tongyu.bct.document.dao.dbo.DictionaryEntry;
import tech.tongyu.bct.document.dao.dbo.Template;
import tech.tongyu.bct.document.dao.dbo.TemplateContent;
import tech.tongyu.bct.document.dao.dbo.TemplateDirectory;
import tech.tongyu.bct.document.dao.repl.intel.DictionaryEntryRepo;
import tech.tongyu.bct.document.dao.repl.intel.TemplateContentRepo;
import tech.tongyu.bct.document.dao.repl.intel.TemplateDirectoryRepo;
import tech.tongyu.bct.document.dao.repl.intel.TemplateRepo;
import tech.tongyu.bct.document.dto.*;
import tech.tongyu.bct.document.service.impl.DocumentServiceImpl;

import java.io.StringWriter;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;

@RunWith(MockitoJUnitRunner.class)
public class DocumentServiceTest {

    @Mock
    private TemplateContentRepo templateContentRepo;

    @Mock
    private TemplateRepo templateRepo;

    @Mock
    private TemplateDirectoryRepo templateDirectoryRepo;

    @Mock
    private DictionaryEntryRepo dictionaryEntryRepo;

    @Test
    public void CreateDirectoryTest_Success() {
        DocumentService service = getServiceInstance();

        TemplateDirectory dbo = getDirectoryDBO();
        Mockito.when(templateDirectoryRepo.saveAndFlush(any())).thenReturn(dbo);

        TemplateDirectoryDTO directory = getDirectoryDTO();
        directory = service.createDirectory(directory);

        Assert.assertNotNull(directory.getUuid());
    }

    @Test
    public void CreateTemplateTest_Success() {
        Mockito.when(templateDirectoryRepo.findById(any())).thenReturn(Optional.of(getDirectoryDBO()));
        Mockito.when(templateContentRepo.save(any())).thenReturn(getContentDBO());
        Mockito.when(templateRepo.save(any())).thenReturn(getTemplateDBO());
        Mockito.when(templateDirectoryRepo.save(any())).thenReturn(getDirectoryDBO());
        DocumentService service = getServiceInstance();
        TemplateDTO template = getTemplateDTO();
        template = service.createTemplate(template, sharedTemplateContent);

        Assert.assertNotNull(template.getUuid());
    }

    @Test
    public void CreateSpecificDicEntryTest_Success() {
        DocumentService service = getServiceInstance();
        DictionaryEntryDTO entry = getSpecificDictEntryDtoStr();
        Mockito.when(dictionaryEntryRepo.saveAndFlush(any()))
                .thenReturn(getSpecificDicEntryDboStr());

        entry = service.createSpecificDicEntry(entry);

        Assert.assertNotNull(entry.getUuid());
    }

    @Test
    public void CreateGlobalDicEntryTest_Success() {
        DocumentService service = getServiceInstance();
        DictionaryEntryDTO entry = getGlobalDicEntryDtoStr();
        entry.setDicGroup(null);
        Mockito.when(dictionaryEntryRepo.saveAndFlush(any()))
                .thenReturn(getSpecificDicEntryDboStr());

        entry = service.createGlobalDicEntry(entry);

        Assert.assertNotNull(entry.getUuid());
    }

    @Test
    public void GenDocumentTest_SuccessBasic() {
        // prepare
        TemplateDirectory dicDbo = getDirectoryDBO();
        Template template = getTemplateDBO();
        template.getContent().setContent("<xml> ${myNum?string('#.##')}  ${myStr} ${globalStr} ${defV} ${noHandle} ${myDateTime?string('yyyy年MM月dd日 hh时mm分ss秒')}</xml>");
        List<Template> templates = new ArrayList<>();
        templates.add(template);
        dicDbo.setTemplates(templates);
        Mockito.when(templateDirectoryRepo.findById(any())).thenReturn(Optional.of(dicDbo));

        // prepare global dictionary
        DictionaryEntry globalEntry = getGlobalDicEntryDboStr();
        globalEntry.setDestinationPath("globalStr");
        globalEntry.setDefaultValue("globalValue");
        List<DictionaryEntry> globalEntries = new ArrayList<>();
        globalEntries.add(globalEntry);
        Mockito.when(dictionaryEntryRepo.findByDicGroup(eq("GLOBAL"))).thenReturn(globalEntries);

        // prepare specific dictionary
        DictionaryEntry speEntry1 = getSpecificDicEntryDboStr();
        speEntry1.setSourcePath("node1");
        speEntry1.setDestinationPath("myStr");
        DictionaryEntry speEntry2 = getSpecificDicEntryDboNum();
        speEntry2.setSourcePath("node2");
        speEntry2.setDestinationPath("myNum");
        DictionaryEntry speEntry3 = getSpecificDicEntryDboStr();
        speEntry3.setSourcePath("invalid");
        speEntry3.setDestinationPath("defV");
        speEntry3.setDefaultValue("defaultValue");
        DictionaryEntry speEntry4 = getSpecificDicEntryDboNum();
        speEntry4.setSourcePath("node3");
        speEntry4.setDestinationPath("myDateTime");
        speEntry4.setType(ValueTypeEnum.DATE_TIME);

        List<DictionaryEntry> speEntries = new ArrayList<>();
        speEntries.add(speEntry1);
        speEntries.add(speEntry2);
        speEntries.add(speEntry3);
        speEntries.add(speEntry4);
        Mockito.when(dictionaryEntryRepo.findByDicGroup(eq("myGroup"))).thenReturn(speEntries);

        // act
        DocumentService service = getServiceInstance();
        Map<String, Object> data = new HashMap<>();
        data.put("node1", "myStr1");
        data.put("node2", 1234.567);
        data.put("node3", LocalDateTime.now().toString());
        data.put("noHandle", "myNoHandle1");

        StringWriter writer = new StringWriter();
        service.genDocument(data, UUID.randomUUID(), "myGroup", writer);
        String output = writer.toString();

        // validate
        Assert.assertTrue(output.contains("myStr1"));
        Assert.assertTrue(output.contains("1234.57"));
        Assert.assertTrue(output.contains("myNoHandle1"));
        Assert.assertTrue(output.contains("defaultValue"));
        Assert.assertTrue(output.contains("globalValue"));
        Assert.assertTrue(output.contains("年"));
    }

    @Test
    public void GenDocumentTest_SuccessComplexPath() {
        // prepare
        TemplateDirectory dicDbo = getDirectoryDBO();
        Template template = getTemplateDBO();
        template.getContent().setContent("<xml> ${parent.subNode1.subNode2}, ${parent.subNode2.subNode3}$</xml>");
        List<Template> templates = new ArrayList<>();
        templates.add(template);
        dicDbo.setTemplates(templates);
        Mockito.when(templateDirectoryRepo.findById(any())).thenReturn(Optional.of(dicDbo));

        // prepare specific dictionary
        DictionaryEntry speEntry1 = getSpecificDicEntryDboStr();
        speEntry1.setSourcePath("node1");
        speEntry1.setDestinationPath("parent.subNode1.subNode2");

        DictionaryEntry speEntry2 = getSpecificDicEntryDboStr();
        speEntry2.setSourcePath("node2.node2");
        speEntry2.setDestinationPath("parent.subNode2.subNode3");

        List<DictionaryEntry> speEntries = new ArrayList<>();
        speEntries.add(speEntry1);
        speEntries.add(speEntry2);
        Mockito.when(dictionaryEntryRepo.findByDicGroup(eq("myGroup"))).thenReturn(speEntries);

        // act
        DocumentService service = getServiceInstance();
        MyObject1 obj1 = new MyObject1();
        obj1.setNode1("obj1_node1"); // node2.node1.
        obj1.setNode2("obj1_node2"); // node2.node2

        MyObject2 obj2 = new MyObject2();
        obj2.setNode1("obj2_node1"); // node1
        obj2.setNode2(obj1); // node2

        StringWriter writer = new StringWriter();
        service.genDocument(obj2, UUID.randomUUID(), "myGroup", writer);
        String output = writer.toString();

        // validate
        Assert.assertTrue(output.contains("obj2_node1"));
        Assert.assertTrue(output.contains("obj1_node2"));
    }

    @Test
    public void GenDocumentTest_SuccessArray() {
        // prepare
        TemplateDirectory dicDbo = getDirectoryDBO();
        Template template = getTemplateDBO();
        template.getContent().setContent("<xml> <#list items as item> ${item.node1} & ${item.node2} ;</#list> </xml>");
        List<Template> templates = new ArrayList<>();
        templates.add(template);
        dicDbo.setTemplates(templates);
        Mockito.when(templateDirectoryRepo.findById(any())).thenReturn(Optional.of(dicDbo));

        // prepare specific dictionary
        DictionaryEntry speEntry1 = getSpecificDicEntryDboStr();
        speEntry1.setSourcePath("rawArray");
        speEntry1.setDestinationPath("items");
        speEntry1.setType(ValueTypeEnum.ARRAY);

        List<DictionaryEntry> speEntries = new ArrayList<>();
        speEntries.add(speEntry1);
        Mockito.when(dictionaryEntryRepo.findByDicGroup(eq("myGroup"))).thenReturn(speEntries);

        // act
        DocumentService service = getServiceInstance();
        List<MyObject1> array = new ArrayList<>();
        MyObject1 obj1 = new MyObject1();
        obj1.setNode1("n11"); // node2.node1
        obj1.setNode2("n12"); // node2.node2
        array.add(obj1);

        MyObject1 obj2 = new MyObject1();
        obj2.setNode1("n21"); // node2.node1
        obj2.setNode2("n22"); // node2.node2
        array.add(obj2);

        Map<String, Object> dataDic = new HashMap<>();
        dataDic.put("rawArray", array);

        StringWriter writer = new StringWriter();
        service.genDocument(dataDic, UUID.randomUUID(), "myGroup", writer);
        String output = writer.toString();

        // validate
        Assert.assertTrue(output.contains("n11"));
        Assert.assertTrue(output.contains("n12"));
        Assert.assertTrue(output.contains("n21"));
        Assert.assertTrue(output.contains("n22"));
    }

    @Test
    public void GenDocumentTest_SuccessObject() {
        // prepare
        TemplateDirectory dicDbo = getDirectoryDBO();
        Template template = getTemplateDBO();
        template.getContent().setContent("<xml> ${obj.node1} & ${obj.node2}  </xml>");
        List<Template> templates = new ArrayList<>();
        templates.add(template);
        dicDbo.setTemplates(templates);
        Mockito.when(templateDirectoryRepo.findById(any())).thenReturn(Optional.of(dicDbo));

        // prepare specific dictionary
        DictionaryEntry speEntry1 = getSpecificDicEntryDboStr();
        speEntry1.setSourcePath("rawObject");
        speEntry1.setDestinationPath("obj");
        speEntry1.setType(ValueTypeEnum.OBJECT);

        List<DictionaryEntry> speEntries = new ArrayList<>();
        speEntries.add(speEntry1);
        Mockito.when(dictionaryEntryRepo.findByDicGroup(eq("myGroup"))).thenReturn(speEntries);

        // act
        DocumentService service = getServiceInstance();
        MyObject1 obj1 = new MyObject1();
        obj1.setNode1("n11"); // node2.node1.
        obj1.setNode2("n12"); // node2.node2

        Map<String, Object> dataDic = new HashMap<>();
        dataDic.put("rawObject", JsonUtils.objectToJsonString(obj1));

        StringWriter writer = new StringWriter();
        service.genDocument(dataDic, UUID.randomUUID(), "myGroup", writer);
        String output = writer.toString();

        // validate
        Assert.assertTrue(output.contains("n11"));
        Assert.assertTrue(output.contains("n12"));
    }

    // assistant code.
    private static class MyObject1 {
        private String node1;

        private String node2;

        public String getNode1() {
            return node1;
        }

        public void setNode1(String node1) {
            this.node1 = node1;
        }

        public String getNode2() {
            return node2;
        }

        public void setNode2(String node2) {
            this.node2 = node2;
        }
    }

    private static class MyObject2 {
        private String node1;

        private MyObject1 node2;


        public String getNode1() {
            return node1;
        }

        public void setNode1(String node1) {
            this.node1 = node1;
        }

        public MyObject1 getNode2() {
            return node2;
        }

        public void setNode2(MyObject1 node2) {
            this.node2 = node2;
        }
    }
    private TemplateDirectoryDTO getDirectoryDTO() {
        Set<String> tags = new HashSet<>();
        tags.add("a1");
        tags.add("a2");
        final String name = "myTemplateDirectory";
        final String description = "description";

        TemplateDirectoryDTO directory = new TemplateDirectoryDTO(name, tags, description);
        directory.setUuid(UUID.randomUUID());
        directory.setCreatedBy("Tom");

        return directory;
    }

    private TemplateDirectory getDirectoryDBO() {
        Set<String> tags = new HashSet<>();
        tags.add("a1");
        tags.add("a2");
        TemplateDirectory dbo = new TemplateDirectory("myTemplateDirectory", tags, "my description");
        dbo.setUuid(UUID.randomUUID());
        Template template = getTemplateDBO();
        List<Template> templates = new ArrayList<>();
        templates.add(template);
        dbo.setTemplates(templates);
        dbo.setUuid(UUID.randomUUID());
        dbo.setCreatedAt(Instant.now());
        dbo.setUpdatedAt(Instant.now());
        dbo.setCreatedBy("Tom");

        return dbo;
    }

    private TemplateDTO getTemplateDTO() {
        TemplateDTO result = new TemplateDTO(UUID.randomUUID(), "myTemplate",
                DocTypeEnum.WORD_2003, ".doc");
        result.setUuid(UUID.randomUUID());
        result.setFileName("myfile");
        result.setEnabled(true);
        result.setDescription("this is template description.");
        result.setCreatedAt(Instant.now());
        result.setUpdatedAt(Instant.now());
        result.setCreatedBy("Jerry");

        return result;
    }

    private final String sharedTemplateContent = "<xml> ${myStr}, ${myNum} </xml>";
    private TemplateContent getContentDBO() {
        TemplateContent content = new TemplateContent(sharedTemplateContent);
        content.setUuid(UUID.randomUUID());
        return content;
    }

    private Template getTemplateDBO() {
        TemplateContent content = getContentDBO();
        Template result = new Template(UUID.randomUUID(), "myTemplate",
                DocTypeEnum.WORD_2003, "doc", content);
        result.setUuid(UUID.randomUUID());
        result.setFileName("myfile");
        result.setEnabled(true);
        result.setDescription("this is template description.");
        result.setCreatedAt(Instant.now());
        result.setUpdatedAt(Instant.now());
        result.setCreatedBy("Jerry");
        return result;
    }

    private DictionaryEntryDTO getSpecificDictEntryDtoStr() {
        Set<String> tags = new HashSet<>();
        tags.add("t1");
        tags.add("t2");
        DictionaryEntryDTO result = new DictionaryEntryDTO("myGroup",
                "sNode1",
                "tNode1",
                ValueTypeEnum.STRING,
                tags,
                "Tongyu");
        result.setUuid(UUID.randomUUID());
        result.setDescription("my description");
        result.setDefaultValue("defaultV1");
        result.setCreatedBy("Lily");

        return result;
    }

    private DictionaryEntryDTO getSpecificDictEntryDtoNum() {
        Set<String> tags = new HashSet<>();
        tags.add("t1");
        tags.add("t2");
        DictionaryEntryDTO result = new DictionaryEntryDTO("myGroup",
                "sNode1",
                "tNode1",
                ValueTypeEnum.NUMBER,
                tags,
                "Tongyu");
        result.setUuid(UUID.randomUUID());
        result.setDescription("my description");
        result.setDefaultValue("10.123");
        result.setCreatedBy("Lily");

        return result;
    }

    private DictionaryEntry getSpecificDicEntryDboStr() {
        Set<String> tags = new HashSet<>();
        tags.add("t3");
        tags.add("t4");
        DictionaryEntry result = new DictionaryEntry("myGroup",
                "sNode1",
                "tNode1",
                ValueTypeEnum.STRING,
                tags,
                "Tongyu");
        result.setUuid(UUID.randomUUID());
        result.setDescription("my description");
        result.setCreatedBy("Lily");

        result.setCreatedAt(Instant.now());
        result.setUpdatedAt(Instant.now());

        return result;
    }

    private DictionaryEntry getSpecificDicEntryDboNum() {
        Set<String> tags = new HashSet<>();
        tags.add("t3");
        tags.add("t4");
        DictionaryEntry result = new DictionaryEntry("myGroup",
                "sNode1",
                "tNode1",
                ValueTypeEnum.NUMBER,
                tags,
                "1234.123");
        result.setUuid(UUID.randomUUID());
        result.setDescription("my description");
        result.setCreatedBy("Lily");

        result.setCreatedAt(Instant.now());
        result.setUpdatedAt(Instant.now());

        return result;
    }

    private DictionaryEntryDTO getGlobalDicEntryDtoStr() {
        Set<String> tags = new HashSet<>();
        tags.add("t1");
        tags.add("t2");
        DictionaryEntryDTO result = new DictionaryEntryDTO("GLOBAL",
                null,
                "tNode1",
                ValueTypeEnum.STRING,
                tags,
                "Tongyu");
        result.setUuid(UUID.randomUUID());
        result.setDescription("my description");
        result.setDefaultValue("defaultV1");
        result.setCreatedBy("Lily");

        return result;
    }

    private DictionaryEntry getGlobalDicEntryDboStr() {
        Set<String> tags = new HashSet<>();
        tags.add("t1");
        tags.add("t2");
        DictionaryEntry result = new DictionaryEntry("GLOBAL",
                null,
                "tNode1",
                ValueTypeEnum.STRING,
                tags,
                "Tongyu");
        result.setUuid(UUID.randomUUID());
        result.setDescription("my description");
        result.setDefaultValue("defaultV1");
        result.setCreatedBy("Lily");

        result.setCreatedAt(Instant.now());
        result.setUpdatedAt(Instant.now());

        return result;
    }

    private DocumentService getServiceInstance() {
        return new DocumentServiceImpl(templateContentRepo,
                templateDirectoryRepo,
                templateRepo,
                dictionaryEntryRepo);
    }
}
