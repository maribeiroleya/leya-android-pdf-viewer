package com.github.barteksc.pdfviewer.util;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

public class Hotspot {

    private double xPos;
    private double yPos;
    private String type;

    private Drawable drawable;


    public Hotspot(double xPos, double yPos, String type) {
        this.xPos = xPos;
        this.yPos = yPos;
        this.type = type;
    }


    public double getXpos() {
        return xPos;
    }

    public double getYpos() {
        return yPos;
    }

    public String getType() {
        return type;
    }

    public void setDrawable(Drawable drawable) {
        this.drawable = drawable;
    }

    public Drawable getDrawable() {
        return drawable;
    }
}
