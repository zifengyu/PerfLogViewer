package cn.alan.perflogviewer.ui;

import java.awt.Dimension;
import java.awt.Point;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class PrefManager {
private final static PrefManager prefManager = new PrefManager();
    
    private final Preferences toolPrefs;
    
    /** Creates a new instance of PrefManager */
    private PrefManager() {
        toolPrefs = Preferences.userNodeForPackage(this.getClass());
    }
    
    public static PrefManager get() {
        return prefManager;
    }
    
    public Dimension getPreferredSize() {
        return(new Dimension(toolPrefs.getInt("windowWidth", 800),
               toolPrefs.getInt("windowHeight", 600)));
    }
    
    public void setPreferredSize(Dimension size) {
        toolPrefs.putInt("windowHeight", size.height);
        toolPrefs.putInt("windowWidth", size.width);
    }
    
    public Point getWindowPos() {
        Point point = new Point(toolPrefs.getInt("windowPosX", 0),
                toolPrefs.getInt("windowPosY", 0));
        return(point);
    }
    
    public void setWindowPos(int x, int y) {
        toolPrefs.putInt("windowPosX", x);
        toolPrefs.putInt("windowPosY", y);
    }
    
    public int getBottomDividerPos() {
        return(toolPrefs.getInt("bottom.dividerPos", 400));
    }
    
    public void setBottomDividerPos(int pos) {
        toolPrefs.putInt("bottom.dividerPos", pos);
    }

    public int getDividerPos() {
        return(toolPrefs.getInt("dividerPos", 600));
    }
    
    public void setDividerPos(int pos) {
        toolPrefs.putInt("dividerPos", pos);
    }
    
    public int getWindowState() {
        return(toolPrefs.getInt("windowState", -1));
    }
    
    public void setWindowState(int windowState) {
        toolPrefs.putInt("windowState", windowState);
    }
    
    public int getTableViewColumnSize(int column) {
    	return toolPrefs.getInt("tableViewColumnSize" + column, 100);
    }
    
    public void setTableViewColumnSize(int column, int size) {
    	toolPrefs.putInt("tableViewColumnSize" + column, size);
    }
    
    public void flush() {
        try {
            toolPrefs.flush();
        } catch (BackingStoreException ex) {
            ex.printStackTrace();
        }
    }

}
