package com.example._7920Assignment2;

import android.graphics.Path;

import java.util.List;

/* stores history of paths drawn so far */
public class PathData {

    Path Path ;
    List<PathPoint> PathPointList ;
    int SelectedColor;
    boolean IsFill;

    //constructor
    public PathData(Path path,List<PathPoint> pathPointList,  int selectedColor, boolean isFill) {
        this.Path = path;
        this.PathPointList = pathPointList;
        this.SelectedColor = selectedColor;
        this.IsFill = isFill;
    }

    // getters and setter for Path data class fields
    public Path getPath() {
        return Path;
    }


    public List<PathPoint> getPathPointList() {
        return PathPointList;
    }

    public int getSelectedColor() {
        return SelectedColor;
    }

    public void setSelectedColor(int selectedColor) {
        SelectedColor = selectedColor;
    }

    public boolean getIsFill() {
        return IsFill;
    }
}
