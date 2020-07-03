package com.kotori316.marker.render;

import java.util.Objects;

class ColorBox {
    public final int red;
    public final int green;
    public final int blue;
    public final int alpha;

    public ColorBox(int red, int green, int blue, int alpha) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
    }

    public static final ColorBox redColor = new ColorBox(0xFF, 0x3D, 0x63, 0xFF);
    public static final ColorBox blueColor = new ColorBox(0x75, 0xCC, 0xFF, 0xFF);
    public static final ColorBox white = new ColorBox(0xFF, 0xFF, 0xFF, 0xFF);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ColorBox colorBox = (ColorBox) o;
        return red == colorBox.red &&
            green == colorBox.green &&
            blue == colorBox.blue &&
            alpha == colorBox.alpha;
    }

    @Override
    public int hashCode() {
        return Objects.hash(red, green, blue, alpha);
    }

    @Override
    public String toString() {
        return "ColorBox{" +
            "red=" + red +
            ", green=" + green +
            ", blue=" + blue +
            ", alpha=" + alpha +
            '}';
    }
}
