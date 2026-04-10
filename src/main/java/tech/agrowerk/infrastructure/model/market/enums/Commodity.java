package tech.agrowerk.infrastructure.model.market.enums;

import lombok.Getter;

@Getter
public enum Commodity {
    SOJA    ("Soja",     "PSOYBUSDM"),
    MILHO   ("Milho",    "PMAIZMTUSDM"),
    CAFE    ("Café",     "PCOFFOTMUSDM"),
    TRIGO   ("Trigo",    "PWHEAMTUSDM"),
    ALGODAO ("Algodão",  "PCOTTINDUSDM"),
    ACUCAR  ("Açúcar",   "PSUGAISAUSDM"),
    BOI_GORDO("Boi gordo", null);

    private final String description;
    private final String fredSeriesId;

    Commodity(String description, String fredSeriesId) {
        this.description = description;
        this.fredSeriesId = fredSeriesId;
    }

    public boolean hasFredSource() {
        return fredSeriesId != null;
    }
}