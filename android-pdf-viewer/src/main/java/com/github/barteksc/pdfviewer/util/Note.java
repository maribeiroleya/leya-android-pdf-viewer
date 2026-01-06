package com.github.barteksc.pdfviewer.util;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

public class Note {

    private double xPos;
    private double yPos;
    private String color;

    private Drawable drawable;

    public Note(double xPos, double yPos, String color) {
        this.xPos = xPos;
        this.yPos = yPos;
        this.color = color;
    }


    public double getXpos() {
        return xPos;
    }

    public double getYpos() {
        return yPos;
    }

    public String getColor() {
        return color;
    }

    public void setDrawable(Drawable drawable) {
        this.drawable = drawable;
    }

    public Drawable getDrawable() {
        return drawable;
    }
}
