package com.worldcuptypes.data;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public enum Stage {
    GROUP_A("A"),
    GROUP_B("B"),
    GROUP_C("C"),
    GROUP_D("D"),
    GROUP_E("E"),
    GROUP_F("F"),
    GROUP_G("G"),
    GROUP_H("H"),
    OCTOFINALS("1/8"),
    QUARTERFINALS("1/4"),
    SEMIFINALS("1/2"),
    FINAL("final");

    public static final String GROUP_KEYWORD = "GRUPA";

    private String value;

    Stage(String name) {
        this.value = name;
    }

    private static final Map<String, Stage> lookup = new HashMap<>();

    static {
        for (Stage stage : Stage.values()) {
            lookup.put(stage.getValue(), stage);
        }
    }

    public static Stage fromValue(String value) {
        return lookup.get(value);
    }
}
