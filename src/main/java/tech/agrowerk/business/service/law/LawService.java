package tech.agrowerk.business.service.law;

import com.vladsch.flexmark.ext.yaml.front.matter.YamlFrontMatterExtension;
import com.vladsch.flexmark.ext.yaml.front.matter.AbstractYamlFrontMatterVisitor;
 import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataSet;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import tech.agrowerk.application.dto.response.LawResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class LawService {

    public LawResponse getLawContent(String slug) {
        MutableDataSet options = new MutableDataSet();
        options.set(Parser.EXTENSIONS, Collections.singletonList(YamlFrontMatterExtension.create()));

        Parser parser = Parser.builder(options).build();
        HtmlRenderer renderer = HtmlRenderer.builder(options).build();

        String mdContent = readMarkdownFile(slug);

        var document = parser.parse(mdContent);

        AbstractYamlFrontMatterVisitor visitor = new AbstractYamlFrontMatterVisitor();
        visitor.visit(document);

        Map<String, List<String>> rawData = visitor.getData();
        Map<String, String> metadata = rawData.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> String.join(", ", e.getValue())
                ));

        String htmlContent  = renderer.render(document);

        return new LawResponse(slug, metadata, htmlContent);
    }

    private String readMarkdownFile(String slug) {
        try {
            var resource = new ClassPathResource("content/laws/" + slug + ".md");
            return Files.readString(resource.getFile().toPath(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Error reading markdonw file: " + slug, e);
        }
    }
}