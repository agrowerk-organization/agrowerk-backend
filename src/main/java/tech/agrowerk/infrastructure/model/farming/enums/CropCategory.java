package tech.agrowerk.infrastructure.model.farming.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
public enum CropCategory {
    GRAIN("Grão"),
    SUGAR_CROP("Cultura Açucareira"),
    FRUIT("Frutífera"),
    FIBER("Fibra"),
    VEGETABLE("Hortaliça"),
    TUBER("Tubérculo"),
    OILSEED("Oleaginosa"),
    LEGUME("Leguminosa"),
    INDUSTRIAL("Industrial"),
    FORESTRY("Florestal"),
    OTHER("Outros");

    private final String description;

    CropCategory(String description) {
        this.description = description;
    }
}
