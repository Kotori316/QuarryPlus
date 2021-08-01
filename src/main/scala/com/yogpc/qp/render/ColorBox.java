package com.yogpc.qp.render;

record ColorBox(int red, int green, int blue, int alpha) {

    public static final ColorBox redColor = new ColorBox(0xFF, 0x3D, 0x63, 0xFF);
    public static final ColorBox blueColor = new ColorBox(0x75, 0xCC, 0xFF, 0xFF);
    public static final ColorBox white = new ColorBox(0xFF, 0xFF, 0xFF, 0xFF);
}
