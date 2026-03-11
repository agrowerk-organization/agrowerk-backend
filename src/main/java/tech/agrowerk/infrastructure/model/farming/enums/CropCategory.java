package tech.agrowerk.infrastructure.model.farming.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
public enum CropCategory {
    CEREAL("Cereal"),
    GRAIN("Grão"),
    OILSEED("Oleaginosa"),
    FIBER("Fibra"),
    VEGETABLE("Hortaliça"),
    FRUIT("Frutífera"),
    LEGUME("Leguminosa"),
    TUBER("Tubérculo"),
    SUGARCANE("Cana-de-Açúcar"),
    STIMULANT("Estimulante"),
    FORAGE("Forrageira"),
    OTHER("Outros");

    private final String description;

    CropCategory(String description) {
        this.description = description;
    }
}
