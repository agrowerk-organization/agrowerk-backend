package tech.agrowerk.infrastructure.model.farming.enums;

import lombok.Getter;

@Getter
public enum SoilType {
    RED_OXISOL(
            "Red Oxisol",
            "Deep, well-drained soil, very common in Cerrado. Excellent for soybeans, corn and coffee.",
            "LV"
    ),
    RED_YELLOW_OXISOL(
            "Red-Yellow Oxisol",
            "Deep, acidic soil, common in tropical areas. Good for perennial crops.",
            "LVA"
    ),
    YELLOW_OXISOL(
            "Yellow Oxisol",
            "Deep, acidic soil, typical of Amazon region. Requires liming.",
            "LA"
    ),

    RED_ULTISOL(
            "Red Ultisol",
            "Soil with clay accumulation in depth. Good for fruit cultivation.",
            "PV"
    ),
    RED_YELLOW_ULTISOL(
            "Red-Yellow Ultisol",
            "Moderately deep soil, susceptible to erosion.",
            "PVA"
    ),


    RED_ALFISOL(
            "Red Alfisol (Terra Roxa)",
            "Fertile, structured soil, excellent for coffee, soybeans and sugarcane.",
            "NV"
    ),

    QUARTZARENIC_ENTISOL(
            "Quartzarenic Entisol",
            "Sandy soil, low fertility, requires frequent irrigation.",
            "RQ"
    ),
    LITHOLIC_ENTISOL(
            "Litholic Entisol",
            "Shallow, rocky soil, limited for mechanized agriculture.",
            "RL"
    ),
    FLUVIC_ENTISOL(
            "Fluvic Entisol",
            "Alluvial, fertile soil, common in floodplains and riverbanks.",
            "RU"
    ),

    MOLLISOL(
            "Mollisol",
            "Dark, fertile soil, rich in organic matter. Excellent for grains.",
            "M"
    ),

    HISTOSOL(
            "Histosol (Organic Soil)",
            "Soil rich in organic matter, typical of wetlands and peatlands.",
            "O"
    ),

    PLANOSOL(
            "Planosol",
            "Soil with compacted layer, poor drainage, common in Rio Grande do Sul.",
            "S"
    ),

    GLEYSOL(
            "Gleysol",
            "Hydromorphic soil, permanently or periodically saturated with water.",
            "G"
    ),

    INCEPTISOL(
            "Inceptisol",
            "Developing soil, variable texture, common in hilly terrain.",
            "C"
    ),


    VERTISOL(
            "Vertisol",
            "Expansive clayey soil, forms large cracks when dry.",
            "V"
    ),

    PLINTHOSOL(
            "Plinthosol",
            "Soil with petroplinthite formation, common in transition areas.",
            "F"
    ),

    SANDY_SOIL(
            "Sandy Soil",
            "Soil with predominance of sand, low water and nutrient retention.",
            "SA"
    ),
    CLAY_SOIL(
            "Clay Soil",
            "Soil with high clay proportion, good water retention.",
            "CL"
    ),
    SILTY_SOIL(
            "Silty Soil",
            "Soil with intermediate texture, good natural fertility.",
            "SI"
    ),
    AMAZONIAN_DARK_EARTH(
            "Amazonian Dark Earth (ADE)",
            "Anthropogenic soil, extremely fertile, rich in pyrogenic carbon.",
            "TPA"
    ),
    UNIDENTIFIED(
            "Unidentified",
            "Unclassified soil or pending analysis.",
            "NI"
    );

    private final String displayName;
    private final String description;
    private final String code;

    SoilType(String displayName, String description, String code) {
        this.displayName = displayName;
        this.description = description;
        this.code = code;
    }

    public boolean isFertile() {
        return this == RED_ALFISOL ||
                this == RED_OXISOL ||
                this == MOLLISOL ||
                this == AMAZONIAN_DARK_EARTH ||
                this == FLUVIC_ENTISOL;
    }

    public boolean requiresLiming() {
        return this == YELLOW_OXISOL ||
                this == RED_YELLOW_OXISOL ||
                this == RED_YELLOW_ULTISOL;
    }


    public boolean hasGoodDrainage() {
        return this == RED_OXISOL ||
                this == RED_YELLOW_OXISOL ||
                this == RED_ALFISOL ||
                this == QUARTZARENIC_ENTISOL;
    }

    public boolean isSuitableForMechanization() {
        return this != LITHOLIC_ENTISOL &&
                this != GLEYSOL &&
                this != HISTOSOL;
    }
}
