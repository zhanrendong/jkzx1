package tech.tongyu.bct.servers.document.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import tech.tongyu.bct.common.api.request.JsonRpcRequest;
import tech.tongyu.bct.document.dto.TemplateDTO;
import tech.tongyu.bct.document.dto.TemplateDirectoryDTO;
import tech.tongyu.bct.document.service.DocumentService;

import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/download")
public class DocumentController {
    private final DocumentService documentService;

    @Autowired
    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping("/gen-doc")
    @ResponseBody
    ResponseEntity<?> genDoc(@RequestBody JsonRpcRequest request) {
        if (null == request || null == request.getParams()) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }

        Map<String, Object> params = request.getParams();

        UUID dirId = UUID.fromString((String) params.getOrDefault("directoryId", null));
        String group = (String) params.getOrDefault("dicGroup", null);
        Map<String, Object> data = (Map<String, Object>) params.get("data");
        String fileName = (String) params.getOrDefault("fileName", null);

        if (null == dirId || null == group || null == data) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }

        //fetch enabled template.
        TemplateDTO template = null;
        {
            TemplateDirectoryDTO dir = documentService.getDirectory(dirId)
                    .orElseThrow(() -> new IllegalArgumentException("invalid directory id."));

            List<TemplateDTO> templates = dir.getTemplates();
            if (null == templates || templates.size() < 1) {
                throw new IllegalArgumentException("templates in this directory is empty.");
            }

            template = templates.stream().filter(t -> t.isEnabled()).findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("no template is enabled."));
        }

        String fullFileName = StringUtils.isEmpty(fileName) ?
                template.getName() + "." + template.getTypeSuffix():
                fileName + "." + template.getTypeSuffix();

        StringWriter out = new StringWriter();
        documentService.genDocument(data, dirId, group, out);

        // prepare for header
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", fullFileName);

        return new ResponseEntity<>(out.getBuffer(), headers, HttpStatus.OK);
    }

    @GetMapping("/template")
    @ResponseBody
    @Transactional
    ResponseEntity<?> downloadTemplateFile(@RequestParam("templateId") String templateId) {
        UUID id = UUID.fromString(templateId);

        TemplateDTO template = documentService.getTemplate(id)
                .orElseThrow(() -> new IllegalArgumentException("invalid templateId."));

        // reading all file content will consume much memory.
        String fileContent = documentService.getTemplateContent(id);

        // preparing for header
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", template.getFileName());

        return new ResponseEntity<>(fileContent, headers, HttpStatus.OK);
    }
}

