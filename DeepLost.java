/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package myGame;

import com.jme3.app.SimpleApplication;
import com.jme3.niftygui.NiftyJmeDisplay;
import de.lessvoid.nifty.Nifty;

/**
 *
 * @author Jan
 */
public class DeepLost extends SimpleApplication {

    static DeepLost app;
    
    @Override
    public void simpleInitApp() {
//        GameRunningAppState state = new GameRunningAppState(rootNode, guiNode, settings, assetManager);
//        stateManager.attach(state);

        NiftyJmeDisplay niftyDisplay = NiftyJmeDisplay.newNiftyJmeDisplay(assetManager, inputManager, audioRenderer, guiViewPort);
        Nifty nifty = niftyDisplay.getNifty();
        nifty.fromXml("Interface/screen.xml", "start", new MyStartScreen(DeepLost.this, nifty, settings));
        guiViewPort.addProcessor(niftyDisplay);
        flyCam.setDragToRotate(true);
        flyCam.setMoveSpeed(10f);

//        MyStartScreen state = new MyStartScreen(DeepLost.this, nifty, settings);
//        stateManager.attach(state);
    }
    
    public static void main(String[] args) {
        app = new DeepLost();
        app.start();
    }
}
