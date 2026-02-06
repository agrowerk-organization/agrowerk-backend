package tech.agrowerk.business.service.law;

import com.vladsch.flexmark.ext.yaml.front.matter.YamlFrontMatterExtension;
import com.vladsch.flexmark.ext.yaml.front.matter.AbstractYamlFrontMatterVisitor;
 import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataSet;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import tech.agrowerk.application.dto.crud.get.LawResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class LawService {

    public LawResponse getLawContent(String fileName) {
        MutableDataSet options = new MutableDataSet();
        options.set(Parser.EXTENSIONS, Collections.singletonList(YamlFrontMatterExtension.create()));

        Parser parser = Parser.builder(options).build();
        HtmlRenderer renderer = HtmlRenderer.builder(options).build();

        String mdContent = readMarkdownFile(fileName);

        var document = parser.parse(mdContent);

        AbstractYamlFrontMatterVisitor visitor = new AbstractYamlFrontMatterVisitor();
        visitor.visit(document);

        Map<String, List<String>> rawData = visitor.getData();
        Map<String, String> data = rawData.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> String.join(", ", e.getValue())
                ));

        String html = renderer.render(document);

        return new LawResponse(fileName, data, html);
    }

    private String readMarkdownFile(String fileName) {
        try {
            var resource = new ClassPathResource("laws/" + fileName);
            return Files.readString(resource.getFile().toPath(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Error reading markdonw file: " + fileName, e);
        }
    }
}