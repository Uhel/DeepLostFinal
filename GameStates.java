/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package myGame;

import com.jme3.asset.AssetManager;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.system.AppSettings;

/**
 *
 * @author Jan
 */
public class GameStates {
    
    private final int WIDTH = 2;
    private final Labyrint levelOne   = new Labyrint(19, 19, 3);
    private final Labyrint levelTwo   = new Labyrint(21, 21, 3);
    private final Labyrint levelThree = new Labyrint(23, 23, 3);
    private final Labyrint levelFour  = new Labyrint(19, 19, 2);
    private final Labyrint levelFive  = new Labyrint(21, 21, 2);
    private final Labyrint levelSix   = new Labyrint(23, 23, 2);
    private final Labyrint levelSeven = new Labyrint(19, 19, 1);
    private final Labyrint levelEight = new Labyrint(21, 21, 1);
    private final Labyrint levelNine  = new Labyrint(23, 23, 1);
    private final Labyrint columMode  = new Labyrint(23, 23, 0);
    private final DeepLost app = new DeepLost();
    
    public GameStates() {}

    /**
     * @return polovinu sirky steny
     */
    public int getWIDTH() {
        return WIDTH;
    }

    /**
     * @return labyrint level-1 s rozmistenymi sloupy
     */
    public Labyrint getLevelOne() {
        return levelOne;
    }

    /**
     * @return labyrint level-2 s rozmistenymi sloupy
     */
    public Labyrint getLevelTwo() {
        return levelTwo;
    }

    /**
     * @return labyrint level-3 s rozmistenymi sloupy
     */
    public Labyrint getLevelThree() {
        return levelThree;
    }

    /**
     * @return labyrint level-4 s rozmistenymi sloupy
     */
    public Labyrint getLevelFour() {
        return levelFour;
    }

    /**
     * @return labyrint level-5 s rozmistenymi sloupy
     */
    public Labyrint getLevelFive() {
        return levelFive;
    }

    /**
     * @return labyrint level-6 s rozmistenymi sloupy
     */
    public Labyrint getLevelSix() {
        return levelSix;
    }

    /**
     * @return labyrint level-7 s rozmistenymi sloupy
     */
    public Labyrint getLevelSeven() {
        return levelSeven;
    }

    /**
     * @return labyrint level-8 s rozmistenymi sloupy
     */
    public Labyrint getLevelEight() {
        return levelEight;
    }

    /**
     * @return labyrint level-9 s rozmistenymi sloupy
     */
    public Labyrint getLevelNine() {
        return levelNine;
    }

    /**
     * @return labyrint colum-mode s rozmistenymi sloupy
     */
    public Labyrint getColumMode() {
        return columMode;
    }

    public DeepLost getApp() {
        return app;
    }
    
            /**
     * metoda vytvori objekt typu Box a vrati jej
     * @param name - jmeno Boxu 
     * @param loc - souradnice, kde se ma nachazet
     * @param color - barva
     * @param x - x-osova velikost Boxu
     * @param y - y-onova velikost Boxu
     * @return objekt typu Box
     */
    public Geometry myBox(String name, Vector3f loc, ColorRGBA color, float x, float y, AssetManager assetManager) {
        Box mesh = new Box(Vector3f.ZERO,x ,y ,1);
        Geometry geom = new Geometry(name, mesh);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", color);
        geom.setMaterial(mat);
        geom.setLocalTranslation(loc);
        return geom;
    } 
    
    /**
     * pomoci metody myBox vytvori 2 Boxy, zmensi je, nastavi soradnice na obrazovce a prida do guiNode 
     */
    public void attachCenterMark(Node guiNode, AssetManager assetManager, AppSettings settings) {
        Geometry c = myBox("center mark", Vector3f.ZERO, ColorRGBA.White, 3, 0.5f, assetManager);
        c.scale(4);
        c.setLocalTranslation(settings.getWidth() / 2, settings.getHeight() / 2, 0);
        guiNode.attachChild(c);
        Geometry d = myBox("center mark", Vector3f.ZERO, ColorRGBA.White, 0.5f, 3, assetManager);
        d.scale(4);
        d.setLocalTranslation(settings.getWidth() / 2, settings.getHeight() / 2, 0);
        guiNode.attachChild(d);
    }
    
    public void registerInputs(InputManager inputManager) {
        inputManager.addMapping("RunForward", new KeyTrigger(KeyInput.KEY_Q));
            inputManager.addMapping("Forward", new KeyTrigger(KeyInput.KEY_W), new KeyTrigger(KeyInput.KEY_UP));
            inputManager.addMapping("Back", new KeyTrigger(KeyInput.KEY_S), new KeyTrigger(KeyInput.KEY_DOWN));
            inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_A), new KeyTrigger(KeyInput.KEY_LEFT));
            inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_D), new KeyTrigger(KeyInput.KEY_RIGHT));
            inputManager.addMapping("map", new KeyTrigger(KeyInput.KEY_M));
            inputManager.addMapping("Jump", new KeyTrigger(KeyInput.KEY_SPACE));
            inputManager.addMapping("mouseRotateRight", new MouseAxisTrigger(MouseInput.AXIS_X, true));
            inputManager.addMapping("mouseRotateLeft", new MouseAxisTrigger(MouseInput.AXIS_X, false));
    }
}
