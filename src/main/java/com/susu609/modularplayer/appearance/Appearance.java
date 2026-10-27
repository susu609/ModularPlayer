package com.susu609.modularplayer.appearance;

/** Independent body shape per anatomical group and one style per clothing slot. */
public record Appearance(
        Shape head, Shape torso, Shape arms, Shape legs,
        Headwear headwear, Top top, Bottom bottom, Footwear feet) {

    public enum Shape { STEVE, WIDE }
    public enum Headwear { NONE, STEVE_HAT }
    public enum Top { NONE, STEVE_BLUE, STEVE_RED }
    public enum Bottom { NONE, STEVE_PANTS }
    public enum Footwear { NONE, STEVE_BOOTS }

    public Appearance withTorso(Shape next) {
        return new Appearance(head, next, arms, legs, headwear, top, bottom, feet);
    }

    public Appearance withTop(Top next) {
        return new Appearance(head, torso, arms, legs, headwear, next, bottom, feet);
    }

    public Appearance withBottom(Bottom next) {
        return new Appearance(head, torso, arms, legs, headwear, top, next, feet);
    }

    public Appearance withFeet(Footwear next) {
        return new Appearance(head, torso, arms, legs, headwear, top, bottom, next);
    }
}
