package tech.agrowerk.business.service.law;

import com.vladsch.flexmark.ext.yaml.front.matter.YamlFrontMatterExtension;
import com.vladsch.flexmark.ext.yaml.front.matter.YamlFrontMatterVisitor;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataSet;
import org.springframework.stereotype.Service;
import tech.agrowerk.application.dto.crud.get.LawResponse;

@Service
public class LawService {

    public LawResponse getLawContent(String fileName) {
        // 1. Configurar extensões
        MutableDataSet options = new MutableDataSet();
        options.set(Parser.EXTENSIONS, Collections.singletonList(YamlFrontMatterExtension.create()));

        Parser parser = Parser.builder(options).build();
        HtmlRenderer renderer = HtmlRenderer.builder(options).build();

        // 2. Ler arquivo do resources
        // Use ResourceLoader para garantir que funcione dentro do JAR/WAR
        String mdContent = readMarkdownFile(fileName);

        // 3. Parsear
        var document = parser.parse(mdContent);

        // 4. Extrair Metadados (Front Matter)
        YamlFrontMatterVisitor visitor = new YamlFrontMatterVisitor();
        visitor.visit(document);
        Map<String, List<String>> data = visitor.getData();

        // 5. Renderizar HTML do corpo
        String html = renderer.render(document);

        return new LawResponse(data, html);
    }
}