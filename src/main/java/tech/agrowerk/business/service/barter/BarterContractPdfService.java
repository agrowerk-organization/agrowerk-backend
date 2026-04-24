package tech.agrowerk.business.service.barter;

import com.lowagie.text.DocumentException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.xhtmlrenderer.pdf.ITextRenderer;
import tech.agrowerk.infrastructure.exception.local.PdfGenerationException;
import tech.agrowerk.infrastructure.model.barter.BarterContract;
import tech.agrowerk.infrastructure.model.barter.BarterPriceSnapshot;
import tech.agrowerk.infrastructure.model.barter.BarterTransactionItem;
import tech.agrowerk.infrastructure.model.core.User;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
@Slf4j
public class BarterContractPdfService {

    private static final DateTimeFormatter BR_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter BR_DATETIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Value("${app.base-url}")
    private String baseUrl;

    public byte[] generate(BarterContract contract,
                           BarterPriceSnapshot snapshot,
                           List<BarterTransactionItem> items) {
        try {
            String html = buildContractHtml(contract, snapshot, items);

            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(html);
            renderer.layout();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            renderer.createPDF(out);

            log.info("Generated PDF contract={} size={}bytes",
                    contract.getContractNumber(), out.size());

            return out.toByteArray();

        } catch (DocumentException e) {
            throw new PdfGenerationException("Failed to generatr contract PDF: " + contract.getContractNumber() + " : " + e);
        }
    }

    private String buildContractHtml(BarterContract contract,
                                     BarterPriceSnapshot snapshot,
                                     List<BarterTransactionItem> items) {

        User offeror  = contract.getTransaction().getOfferor();
        User acceptor = contract.getTransaction().getAcceptor();

        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
                "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
            <html xmlns="http://www.w3.org/1999/xhtml" lang="pt-BR">
            <head>
              <meta charset="UTF-8"/>
              <title>Contrato de Barter #%s</title>
              <style>
                body       { font-family: Arial, sans-serif; font-size: 11pt;
                             color: #1a1a1a; margin: 0; padding: 40px; }
                .header    { text-align: center; border-bottom: 2px solid #4CAF50;
                             padding-bottom: 16px; margin-bottom: 24px; }
                .logo      { color: #4CAF50; font-size: 22pt; font-weight: bold; }
                .contract-title { font-size: 14pt; font-weight: bold; margin-top: 6px; }
                .contract-number { color: #666; font-size: 10pt; }
                h3         { color: #2e7d32; border-bottom: 1px solid #c8e6c9;
                             padding-bottom: 4px; margin-top: 24px; }
                table      { width: 100%%; border-collapse: collapse; margin-top: 10px; }
                th         { background-color: #4CAF50; color: white; padding: 8px;
                             text-align: left; font-size: 10pt; }
                td         { padding: 7px 8px; border-bottom: 1px solid #e0e0e0;
                             font-size: 10pt; }
                tr:nth-child(even) td { background-color: #f9fbe7; }
                .total-row td  { font-weight: bold; background-color: #e8f5e9; }
                .snapshot-box  { background-color: #f1f8e9; border: 1px solid #a5d6a7;
                                 border-radius: 8px; padding: 16px; margin-top: 10px; }
                .snapshot-grid { display: block; }
                .snap-item     { margin-bottom: 6px; font-size: 10pt; }
                .snap-label    { color: #555; }
                .snap-value    { font-weight: bold; }
                .parties-grid  { width: 100%%; }
                .parties-grid td { border: none; vertical-align: top; padding: 0 16px 0 0; }
                .party-box     { background-color: #f5f5f5; border-radius: 6px;
                                 padding: 12px; margin-top: 6px; }
                .footer        { margin-top: 40px; border-top: 1px solid #e0e0e0;
                                 padding-top: 16px; color: #999; font-size: 9pt;
                                 text-align: center; }
                .signature-area { margin-top: 48px; }
                .sig-line      { border-top: 1px solid #333; width: 240px;
                                 margin-top: 40px; padding-top: 4px;
                                 font-size: 9pt; color: #555; }
              </style>
            </head>
            <body>

              <div class="header">
                <div class="logo">AgroWerk</div>
                <div class="contract-title">CONTRATO DE TROCA (BARTER AGRÍCOLA)</div>
                <div class="contract-number">Nº %s · Emitido em %s</div>
              </div>

              <h3>1. PARTES</h3>
              <table class="parties-grid">
                <tr>
                  <td>
                    <strong>OFERTANTE</strong>
                    <div class="party-box">
                      <div>%s</div>
                      <div style="color:#555; font-size:9pt;">%s</div>
                    </div>
                  </td>
                  <td>
                    <strong>ACEITANTE (Fornecedor)</strong>
                    <div class="party-box">
                      <div>%s</div>
                      <div style="color:#555; font-size:9pt;">%s</div>
                    </div>
                  </td>
                </tr>
              </table>

              <h3>2. INSUMOS FORNECIDOS</h3>
              <table>
                <thead>
                  <tr>
                    <th>Insumo</th>
                    <th>Qtd.</th>
                    <th>Unid.</th>
                    <th>Preço Unit. (BRL)</th>
                    <th>Total (BRL)</th>
                  </tr>
                </thead>
                <tbody>
                  %s
                  <tr class="total-row">
                    <td colspan="4">TOTAL DOS INSUMOS</td>
                    <td>R$ %s</td>
                  </tr>
                </tbody>
              </table>

              <h3>3. PRECIFICAÇÃO — SNAPSHOT DE MERCADO</h3>
              <div class="snapshot-box">
                <div class="snapshot-grid">
                  <div class="snap-item">
                    <span class="snap-label">Commodity: </span>
                    <span class="snap-value">%s</span>
                  </div>
                  <div class="snap-item">
                    <span class="snap-label">Cotação CBOT (FRED): </span>
                    <span class="snap-value">USD %s / saca</span>
                  </div>
                  <div class="snap-item">
                    <span class="snap-label">Basis local: </span>
                    <span class="snap-value">USD %s</span>
                  </div>
                  <div class="snap-item">
                    <span class="snap-label">PTAX BCB (%s): </span>
                    <span class="snap-value">R$ %s</span>
                  </div>
                  <div class="snap-item">
                    <span class="snap-label">Preço efetivo da saca (60kg): </span>
                    <span class="snap-value">R$ %s</span>
                  </div>
                  <div class="snap-item" style="margin-top:10px; font-size:11pt;">
                    <span class="snap-label">Total de sacas a entregar: </span>
                    <span class="snap-value" style="color:#2e7d32; font-size:13pt;">%s sc</span>
                  </div>
                </div>
              </div>

              <h3>4. PRAZOS</h3>
              <table>
                <tr>
                  <td><strong>Vigência do contrato</strong></td>
                  <td>%s até %s</td>
                </tr>
                <tr>
                  <td><strong>Entrega dos insumos (aceitante)</strong></td>
                  <td>%s</td>
                </tr>
                <tr>
                  <td><strong>Entrega das sacas (ofertante)</strong></td>
                  <td>%s</td>
                </tr>
              </table>

              <h3>5. TERMOS E CONDIÇÕES</h3>
              <p style="font-size:10pt; color:#333; line-height:1.6;">%s</p>

              <h3>6. GERAÇÃO E AUDITABILIDADE</h3>
              <p style="font-size:9pt; color:#666;">
                Snapshot capturado em: <strong>%s</strong> (UTC) ·
                Contrato gerado pela plataforma AgroWerk ·
                Dados de mercado: FRED (Federal Reserve) + PTAX BCB
              </p>

              <div class="signature-area">
                <table style="width:100%%;">
                  <tr>
                    <td style="width:50%%; border:none; vertical-align:bottom;">
                      <div class="sig-line">
                        %s<br/>Ofertante
                      </div>
                    </td>
                    <td style="width:50%%; border:none; vertical-align:bottom;">
                      <div class="sig-line">
                        %s<br/>Aceitante / Fornecedor
                      </div>
                    </td>
                  </tr>
                </table>
              </div>

              <div class="footer">
                AgroWerk · Contrato Nº %s · Plataforma de Gestão Agrícola ·
                Documento gerado eletronicamente — LGPD compliant
              </div>

            </body>
            </html>
            """.formatted(
                contract.getContractNumber(),
                contract.getContractNumber(),
                contract.getStartDate().format(BR_DATE),
                offeror.getName(),  offeror.getEmail(),
                acceptor.getName(), acceptor.getEmail(),
                buildItemRows(items),
                formatBrl(snapshot.getTotalValueBrl()),
                snapshot.getCommodity(),
                snapshot.getCbotPriceUsd(),
                snapshot.getBasisUsd(),
                snapshot.getPtaxReferenceDate().format(BR_DATE),
                snapshot.getPtaxRate(),
                formatBrl(snapshot.getBagPriceBrl()),
                snapshot.getTotalBagsDue().toPlainString(),
                contract.getStartDate().format(BR_DATE),
                contract.getEndDate().format(BR_DATE),
                contract.getTransaction().getAcceptorDeliveryDate().format(BR_DATE),
                contract.getTransaction().getOfferorDeliveryDate().format(BR_DATE),
                contract.getTermsAndConditions() != null
                        ? contract.getTermsAndConditions()
                        : "Conforme legislação agrícola brasileira vigente.",
                snapshot.getSnapshotAt().format(BR_DATETIME),
                offeror.getName(),
                acceptor.getName(),
                contract.getContractNumber()
        );
    }

    private String buildItemRows(List<BarterTransactionItem> items) {
        StringBuilder sb = new StringBuilder();
        for (BarterTransactionItem item : items) {
            sb.append("""
                <tr>
                  <td>%s</td>
                  <td>%s</td>
                  <td>%s</td>
                  <td>R$ %s</td>
                  <td>R$ %s</td>
                </tr>
            """.formatted(
                    item.getInput().getName(),
                    item.getQuantity().toPlainString(),
                    item.getUnitOfMeasure(),
                    formatBrl(item.getUnitPriceBrl()),
                    formatBrl(item.getTotalPriceBrl())
            ));
        }
        return sb.toString();
    }

    private String formatBrl(BigDecimal value) {
        return String.format(new Locale("pt", "BR"), "%,.2f", value);
    }
}