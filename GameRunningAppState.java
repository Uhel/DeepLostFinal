/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package myGame;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.audio.AudioData;
import com.jme3.audio.AudioNode;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.input.InputManager;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.light.AmbientLight;
import com.jme3.light.SpotLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.CameraNode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.CameraControl;
import com.jme3.scene.shape.Box;
import com.jme3.system.AppSettings;
import com.jme3.texture.Texture;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author Jan
 */
public class GameRunningAppState extends AbstractAppState implements ScreenController, AnalogListener, ActionListener {

    private Node rootNode;
    private Node guiNode;
    private AppSettings settings;
    private AssetManager assetManager;
    BulletAppState bulletAppState;
    private RigidBodyControl floorPhy, wallPhy; // fyzicke controllery srazky objektu
    private Node playerNode = new Node("the player");
    private BetterCharacterControl playerControl;
    private CameraNode camNode;
    private Vector3f walkDirection = new Vector3f(0, 0, 0), viewDirection = new Vector3f(0, 0, 1);
    private boolean left = false, right = false, forward = false,
            backward = false, mouseRotateLeft = false, mouseRotateRight = false, run = false;
    Texture wall;
    Geometry floorGeo;
    //float w = -4, h = -10, speed = 10f;
    float w = 0, h = 0, speed = 10f;
    private boolean hardcore = false;
    private boolean ghostMode = false;
    private SpotLight spot = new SpotLight();
    private DeepLost app;
    Spatial monster;
    private InputManager inputManager;
    private AppStateManager stateManager;
    Nifty nifty;
    AudioNode natureAudio;
    
    static int level = 1;
    int[][] map;
    int plus;
    int between;
    Quaternion quat = new Quaternion();
    Node walls = new Node("Walls");
    
    GameStates gs = new GameStates();
    
    // monster
    int stary_smer = 0;

    @Override
    public void cleanup() {
        super.cleanup(); //To change body of generated methods, choose Tools | Templates.
       // System.out.println(stateManager.hasState(this));
       // System.out.println(stateManager.detach(this));
        stateManager.detach(this);
       // nifty.gotoScreen("start");
      //  System.out.println("go");
       // System.out.println(stateManager.attach(new MyStartScreen(app, nifty, settings, stateManager)));
        stateManager.attach(new MyStartScreen(app, nifty, settings, stateManager));
      //  nifty.fromXml("Interface/screen.xml", "start", new MyStartScreen(DeepLost.app, nifty, settings));
        nifty.gotoScreen("start");
        app.getFlyByCamera().setDragToRotate(true);
        rootNode.detachAllChildren();
        rootNode.getLocalLightList().clear();
        guiNode.getChildren().clear();
        natureAudio.stop();
    }
    
    public GameRunningAppState(AppSettings settings, AppStateManager stateManager, boolean hardcore, DeepLost app, Nifty nifty) {
        this.app = app;
        this.rootNode = app.getRootNode();
        this.guiNode = app.getGuiNode();
        this.assetManager = app.getAssetManager();
        this.inputManager = app.getInputManager();
        this.settings = settings;
        this.hardcore = hardcore;
        this.nifty = nifty;
        this.stateManager = stateManager;
    }
    
    public GameRunningAppState(DeepLost app, AppStateManager stateManager, AppSettings settings, boolean hardcore, boolean ghostMode, Nifty nifty) {
        this.app = app;
        this.rootNode = app.getRootNode();
        this.guiNode = app.getGuiNode();
        this.assetManager = app.getAssetManager();
        this.inputManager = app.getInputManager();
        this.settings = settings;
        this.hardcore = hardcore;
        this.ghostMode = ghostMode;
        this.nifty = nifty;
        this.stateManager = stateManager;
    }

    @Override
    public void bind(Nifty nifty, Screen screen) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void onStartScreen() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void onEndScreen() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void update(float tpf) {
        super.update(tpf); //To change body of generated methods, choose Tools | Templates.
        if (hardcore) {
            spot.setDirection(app.getCamera().getDirection());
            spot.setPosition(app.getCamera().getLocation());
        }
        if (!ghostMode) {
            Vector3f modelForwardDir = playerNode.getWorldRotation().mult(Vector3f.UNIT_Z);
            walkDirection.set(0, 0, 0);

            float xr = playerNode.getLocalTranslation().x - monster.getLocalTranslation().x;
            float zr = playerNode.getLocalTranslation().z - monster.getLocalTranslation().z;
            if ((xr >= -2 && xr <= 2) && (zr >= -2 && zr <= 2)) {
                //app.stop(); 
                System.out.println("You were killed by an monster");
                cleanup();
            }
            app.getInputManager().setCursorVisible(false);
            if (playerNode.getLocalTranslation().z <= -1) {
                //app.stop();
                System.out.println("You reached a new level");
                level++;
                cleanup();
            }
            if (playerNode.getLocalTranslation().y < -15) {
                //app.stop();
                System.out.println("You DIED!!!!!!!!!!!");
                cleanup();
            }
            if (level > 9) {
                System.out.println("You have just winnen this game!!!!");
            }
            Vector3f modelLeftDir = playerNode.getWorldRotation().mult(Vector3f.UNIT_X);
            if (forward) {
                walkDirection.addLocal(modelForwardDir.mult(speed));
            } else if (run) {
                walkDirection.addLocal(modelForwardDir.mult(speed * 2));
            } else if (backward) {
                walkDirection.addLocal(modelForwardDir.mult(speed).negate());           
            } else if (left) {
//                walkDirection.addLocal(modelForwardDir.crossLocal(Vector3f.UNIT_Y).multLocal(-speed * tpf).mult(2 * speed));
                walkDirection.addLocal(modelForwardDir.crossLocal(Vector3f.UNIT_Y).multLocal(-speed * tpf).mult(2 * speed));
            } else if (right) {
//                walkDirection.addLocal(modelForwardDir.crossLocal(Vector3f.UNIT_Y).multLocal(speed * tpf).mult(2 * speed));
                walkDirection.addLocal(modelForwardDir.crossLocal(Vector3f.UNIT_Y).multLocal(speed * tpf).mult(2 * speed));
            }
            playerControl.setWalkDirection(walkDirection); // walk!
            if (mouseRotateRight) {
              //  Quaternion rotateR = new Quaternion().fromRotationMatrix(matrix).mult(speed * tpf);
//               Quaternion rotateR = new Quaternion().fromAngleAxis(FastMath.PI * tpf, Vector3f.UNIT_XYZ); - pokrok
               Quaternion rotateR = new Quaternion().fromAngleAxis(FastMath.HALF_PI * tpf, Vector3f.UNIT_Y);
//                Quaternion rotateR = new Quaternion().fromAngleAxis(FastMath.PI * tpf, new Vector3f(1.0f, 1.0f, 0.0f));
               // Quaternion rotateR = new Quaternion(viewDirection.x, viewDirection.y++, viewDirection.z+10, 1.0f);
//                viewDirection.y++;
//                rotateR.mult(viewDirection);
                rotateR.multLocal(viewDirection);
                mouseRotateLeft = false;
                mouseRotateRight = false;
//                playerNode.setLocalRotation(rotateR);
            } else if (mouseRotateLeft) {
               // Quaternion rotateL = new Quaternion().fromRotationMatrix(matrix).mult(speed * tpf);
                Quaternion rotateL = new Quaternion().fromAngleAxis(-FastMath.HALF_PI * tpf, Vector3f.UNIT_Y);
////                Quaternion rotateL = new Quaternion().fromAngleAxis(-FastMath.PI * tpf, Vector3f.UNIT_XYZ); - pokrok
//                Quaternion rotateL = new Quaternion().fromAngleAxis(-FastMath.PI * tpf, new Vector3f(1.0f, 1.0f, 0.0f));
               // Quaternion rotateL = new Quaternion(viewDirection.x, viewDirection.y--, viewDirection.z-10, 1.0f);
//                viewDirection.y--;
                rotateL.multLocal(viewDirection);
               // playerNode.setLocalRotation(rotateL);
                mouseRotateLeft = false;
                mouseRotateRight = false;
            }
            playerControl.setViewDirection(viewDirection); // turn  
            //monster
//            double X = round((double)monster.getLocalTranslation().x, 1);
//            double Z = round((double)monster.getLocalTranslation().z, 1);  
//            if(control_points.contains(Z) && control_points2.contains(X) && switcher == 0){
//               // if(!jeCesta(smer, (int)X, (int)Z)){
//                //System.out.println("Splnena");
//                smer = novaCesta((int)X, (int)Z);
//                monster.rotate(0f, 5f, 0f);
//                // }
//                switcher = 1;
//            }else{
//                monster.move(smer().x, smer().y, smer().z);
//                switcher = 0;
//            }
            double X = round((double)monster.getLocalTranslation().x, 1);
            double Z = round((double)monster.getLocalTranslation().z, 1);  
            //if(control_points.contains(Z) && control_points2.contains(X) && switcher == 0){
            if(control_points.contains(Z) && control_points.contains(X) && switcher == 0){
                stary_smer = smer;

                smer = novaCesta((int)X, (int)Z, stary_smer);
               if(rotate_monster(smer, stary_smer) != -1){
                    monster.rotate(0f, rotate_monster(smer, stary_smer), 0f);
               }

                switcher = 1;
            }else{
                monster.move(smer().x, smer().y, smer().z);
                switcher = 0;
            }
        } else {
//            double X = round((double)monster.getLocalTranslation().x, 1);
//            double Z = round((double)monster.getLocalTranslation().z, 1);  
//            //if(control_points.contains(Z) && control_points.contains(X) && switcher == 0){
//            if(control_points.contains(Z) && control_points2.contains(X) && switcher == 0){
//               // if(!jeCesta(smer, (int)X, (int)Z)){
//                //System.out.println("Splnena");
//                smer = novaCesta((int)X, (int)Z);
//                monster.rotate(0f, 5f, 0f);
//                // }
//                switcher = 1;
//            }else{
//                monster.move(smer().x, smer().y, smer().z);
//                switcher = 0;
//            }
            double X = round((double)monster.getLocalTranslation().x, 1);
            double Z = round((double)monster.getLocalTranslation().z, 1);  
            if(control_points.contains(Z) && control_points.contains(X) && switcher == 0){
            //if(control_points.contains(Z) && control_points2.contains(X) && switcher == 0){
                stary_smer = smer;

                smer = novaCesta((int)X, (int)Z, stary_smer);
                if(rotate_monster(smer, stary_smer) != -1){
                    monster.rotate(0f, rotate_monster(smer, stary_smer), 0f);
                }

                switcher = 1;
            }else{
                monster.move(smer().x, smer().y, smer().z);
                switcher = 0;
            }
        }
    }

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app); //To change body of generated methods, choose Tools | Templates.

        this.app.getFlyByCamera().setDragToRotate(false);
     //   System.out.println("Welcome");
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);
        switch (level) {
            case 1: 
                map = gs.getLevelOne().get_labyrint();
             //   System.out.println("level1");
                plus = 2; between = 9;
                break;
            case 2: 
                map = gs.getLevelTwo().get_labyrint();
              //  System.out.println("level2");
                plus = 6; between = 8;
                break;
            case 3: 
                map = gs.getLevelThree().get_labyrint();
             //   System.out.println("level3");
                plus = 10; between = 7;
                break;
            case 4: 
                map = gs.getLevelFour().get_labyrint();
             //   System.out.println("level4");
                plus = 2; between = 9;
                break;
            case 5: 
                map = gs.getLevelFive().get_labyrint();
             //   System.out.println("level5");
                plus = 6; between = 8;
                break;
            case 6: 
                map = gs.getLevelSix().get_labyrint();
             //   System.out.println("level6");
                plus = 10; between = 7;
                break;
            case 7: 
                map = gs.getLevelSeven().get_labyrint();
             //   System.out.println("level7");
                plus = 2; between = 9;
                break;
            case 8: 
                map = gs.getLevelEight().get_labyrint();
             //   System.out.println("level8");
                plus = 6; between = 8;
                break;
            case 9: 
                map = gs.getLevelNine().get_labyrint();
             //   System.out.println("level9");
                plus = 10; between = 7;
                break;
            default:
                //map = gs.getColumMode().get_labyrint(); // chyba
                plus = 10; between = 7;
        }
        
        gs.attachCenterMark(guiNode, assetManager, settings);
        rootNode.attachChild(walls);

        paintWalls(map);
        if (hardcore) {
            spot = new SpotLight();
            spot.setSpotRange(100);
            spot.setSpotOuterAngle(30 * FastMath.DEG_TO_RAD);
            spot.setSpotInnerAngle(5 * FastMath.DEG_TO_RAD);
            rootNode.addLight(spot);
            //natureAudio = new AudioNode(assetManager, "Sounds/tune1.ogg", AudioData.DataType.Stream); // streaming=true
        } else {
            rootNode.addLight(new AmbientLight(ColorRGBA.White));
            //natureAudio = new AudioNode(assetManager, "Sounds/tune1.ogg", AudioData.DataType.Stream); // streaming=true
        }
        natureAudio = new AudioNode(assetManager, "Sounds/River.ogg", AudioData.DataType.Stream); // streaming=true
        natureAudio.setVolume(5);
        natureAudio.setLooping(true);
        natureAudio.play();
        
        if (!ghostMode) {
            //------------
            //playerNode.setLocalTranslation(new Vector3f(4, 0, 4));
            playerNode.setLocalTranslation(new Vector3f(75, 5, 90));
            rootNode.attachChild(playerNode);        
            playerControl = new BetterCharacterControl(0.5f, 1f, 30f);
            playerControl.setJumpForce(new Vector3f(0, 200, 0));
            playerControl.setGravity(new Vector3f(0, -10, 0));        
            playerNode.addControl(playerControl);
            bulletAppState.getPhysicsSpace().add(playerControl);
            // 1. nastavovani firstperson navigation
            camNode = new CameraNode("CamNode", app.getCamera());
            //Setting the direction to Spatial to camera, this means the camera will copy the movements of the Node
            camNode.setControlDir(CameraControl.ControlDirection.SpatialToCamera);
            //attaching the camNode to the teaNode
            playerNode.attachChild(camNode);            
            camNode.setQueueBucket(RenderQueue.Bucket.Gui);
            camNode.setLocalTranslation(new Vector3f(0f, 3f, -0.8f));
            quat.lookAt(Vector3f.UNIT_Z, Vector3f.UNIT_Y); // muze byt pokrok
            
            camNode.setLocalRotation(quat);
            camNode.setEnabled(true);           
            camNode.setEnabled(true);
          //  flyCam.setEnabled(false);     
            this.app.getFlyByCamera().setEnabled(true);     
            // zareistrovani listeneru
            gs.registerInputs(inputManager);
            inputManager.addListener(this, "Left", "Right", "Forward", "Back", "Jump", "RunForward");
            inputManager.addListener(this, "mouseRotateRight", "mouseRotateLeft");

            monster = assetManager.loadModel("Models/monsterV1.j3o");
            Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
            Texture text = assetManager.loadTexture("Textures/grass.jpg");
            mat.setTexture("DiffuseMap", text);
            monster.setMaterial(mat);
            //5.5f, -4f, 8f
            //monster.setLocalTranslation(80f, 6f, 90f);
            //Quaternion q = new Quaternion().fromAngleAxis(FastMath.HALF_PI, Vector3f.UNIT_X);
         //   monster.setLocalRotation(q);
            monster.scale(0.65f);
            //monster.setLocalTranslation( 2f, 2.5f, 4f);
            monster.setLocalTranslation( 4f, 2.5f, 4f);
            rootNode.attachChild(monster);
            inicialize();

        } else {
            app.getCamera().setLocation(new Vector3f(0f, 5f, -40f));
            monster = assetManager.loadModel("Models/monster.j3o");
            Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
            Texture text = assetManager.loadTexture("Textures/grass.jpg");
            mat.setTexture("DiffuseMap", text);
            monster.setMaterial(mat);

            monster.scale(0.65f);
            monster.setLocalTranslation( 4f, 2.5f, 4f);
//            monster.setLocalTranslation( 2f, 2.5f, 4f);
            rootNode.attachChild(monster);
            inicialize();
        }
        
        System.out.println(walls.getChildren().toString());
    }

    @Override
    public void postRender() {
        super.postRender(); 
        // vyzkouset
    }
    
    public void makeWallTexture(float x, float z, String texture, boolean inWalls) {
        Box wallMesh = new Box(2f, 10f, 2f);
        Geometry wallGeo = new Geometry("Wall", wallMesh);
        Material wallMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        //Texture wall = assetManager.loadTexture("Textures/god.png");
        wall = assetManager.loadTexture("Textures/" + texture);
        wall.setWrap(Texture.WrapMode.Repeat);
        wallMat.setTexture("DiffuseMap", wall);
        wallGeo.setMaterial(wallMat);
        wallGeo.setLocalTranslation(x, 11.5f, z);
        if (!inWalls) {
            rootNode.attachChild(wallGeo);
        } else {
            walls.attachChild(wallGeo);
        }
        wallPhy = new RigidBodyControl(50000f); // v (kg), kdyz vetsi nez nula, tak jsou tzv. dynamicke, to znamena, ze na ne pusobi gravitace, sily, atd, cim bliz nule, tak tim lehci
        wallGeo.addControl(wallPhy);    
        bulletAppState.getPhysicsSpace().add(wallPhy);
    }
   
   public void makeSquire(float x, float z, float y) {
        Box mesh = new Box(2f, 2f, 2f);
        Geometry geo = new Geometry("Mesh", mesh);
        Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        Texture floor = assetManager.loadTexture("Textures/f.JPG");
        mat.setTexture("DiffuseMap", floor);
        geo.setMaterial(mat);
        geo.setLocalTranslation(x, y, z);
        rootNode.attachChild(geo);
        floorPhy = new RigidBodyControl(0.0f); // kdyz dam vic jak 0.0, tak me to odnasi nahoru(efekt) protoze podlaha pada za vlivu gravitace
        geo.addControl(floorPhy);
        bulletAppState.getPhysicsSpace().add(floorPhy);
    }
   
    public void paintWalls(int[][] map) {
        //w -= plus;
        System.out.println(w);
        for (byte y = 0; y < map.length; y++) {
            for (byte x = 0; x < map[y].length; x++) {
                if ((y == 0 && x == 1) || (y == map.length - 1 && x == map.length-2)) {
                } else if (map[y][x] == 1) {
                    makeWallTexture(w, h, "ff.jpg", true);
                }   
                w += 4;
            }
            //w = -plus;
            w = 0;
            h +=4;
        }
        w = -42;
        h = -40;
//        w = -46;
//        h = -50;
        for (int i = 0; i < 40; i++) {
            for (int j = 0; j < 39; j++) {
              //  makeSquire(w, h, 23.5f);
                makeSquire(w, h, -0.5f);
                w += 4;
            }  
           // w = -46;
            w = -42;
            h += 4;
        }
        w = -42;
//        w = -46;
        float w2 = w + 152;
        //h = -50;
        h = -40;
        float h2 = h;
        float w3 = w + 4;
        float h3 = h2 + 156;
        for (int i = 0; i < 40; i++) {
            if (i < 37) {
                makeWallTexture(w3, h2, "future.jpg", false);
                makeWallTexture(w3, h3, "future.jpg", false);
            }
            w3 += 4;
            makeWallTexture(w, h, "future.jpg", false);
            makeWallTexture(w2, h, "future.jpg", false);
            h += 4;
        }
        w = -38;
        w2 = 106;
        h = 28;
        h2 = 32;
        
//        w = -42;
//        w2 = 102;
//        h = 18;
//        h2 = 22;

//        for (int i = 0; i < between; i++) {
//            makeWallTexture(w, h, "future.jpg");
//            makeWallTexture(w2, h, "future.jpg");
//            makeWallTexture(w, h2, "future.jpg");
//            makeWallTexture(w2, h2, "future.jpg");
//            w += 4; w2 -= 4;
//        }
   }
   
   
    public void onAnalog(String name, float value, float tpf) {
        if (name.equals("mouseRotateLeft")) {
            mouseRotateLeft = true;
            mouseRotateRight = false;
        } else if (name.equals("mouseRotateRight")) {
            mouseRotateRight = true;
            mouseRotateLeft = false;  
        } 
    }

    public void onAction(String name, boolean keyPressed, float tpf) {
        if (name.equals("Left")) {
            left = keyPressed;
        } else if (name.equals("Right")) {
            right = keyPressed;
        } else if (name.equals("Forward")) {
            forward = keyPressed;
        } else if (name.equals("Back")) {
            backward = keyPressed;
        } else if (name.equals("RunForward")) {
            run = keyPressed;
        } else if (name.equals("Jump")) {
            playerControl.jump(); 
        } 
    }
    
    
    // monster
    float zet = 5.5f;
    float xi = 0;
    //x = 0, z = 5.5 pocatek, z=9.5 posunuti
    private float y = 0.0f;
    boolean p = true;
    boolean jeCesta(int smer,float x, float z){
        switch(smer){
            case DOWN:
                return iswall(x, z - 4);
            case UP:
                return iswall(x, z + 4);
            case RIGHT: 
                return iswall(x - 4, z);
            case LEFT:
                return iswall(x + 4, z);
        }
        return false;
    }
    final int UP = 0;
    final int DOWN = 1;
    final int RIGHT = 2;
    final int LEFT = 3;
    ////http://stackoverflow.com/questions/2808535/round-a-double-to-2-decimal-places
    public static double round(double value, int places) {
    if (places < 0) throw new IllegalArgumentException();

    BigDecimal bd = new BigDecimal(value);
    bd = bd.setScale(places, RoundingMode.HALF_UP);
    return bd.doubleValue();
    }
    
    
    int novaCesta(float x, float z, int stary_smer){
            Random r = new Random();
            int s = -1;
            int random = r.nextInt(4);
            ArrayList<Integer> l = new ArrayList<Integer>();
            if(iswall(x + 4, z)){
                l.add(LEFT);
              //  System.out.println("LEFT");
            }else{
                l.add(-1);
            }
            if(iswall(x - 4, z)){
                l.add(RIGHT);
               // System.out.println("Right");
            }else{
                l.add(-1);
            }
            if(iswall(x, z + 4)){
               // System.out.println("up");
                l.add(UP);
            }else{
                l.add(-1);
            }
            if(iswall(x, z - 4)){
               /// System.out.println("down");
                l.add(DOWN);
            }else{
                l.add(-1);
            }
            while(s == -1){
                s = l.get(r.nextInt(4));
                
            }
         return s;
        
    }

    float rotate_monster(int new_smer, int stary_smer){
       
        float angle = -1;
        if(new_smer != stary_smer){
            
            if(new_smer + stary_smer == 1 || new_smer + stary_smer == 5){
                angle = (float)(Math.PI);
            }else{
                if((stary_smer == UP && new_smer == LEFT) ||
                        (stary_smer == DOWN && new_smer == RIGHT) ||
                            (stary_smer == RIGHT && new_smer == UP) ||
                                (stary_smer == LEFT && new_smer == DOWN)){

                    angle = (float)Math.PI/2;
                }else{
                    angle = (float) -Math.PI/2;
                }
            }
        }
        return angle;
    }

    int smer = UP;
    float kon_x = 0;
    float kon_z = 0;
    void body(int smer, float x, float z){
        switch(smer){
            case DOWN:
                kon_z = z - 4;
            case UP:
                kon_z = z + 4;
            case RIGHT: 
                kon_x = x - 4;
            case LEFT:
                kon_x = x + 4;
        }
    }
    
    Vector3f smer(){
        switch(smer){
            case DOWN:
                return new Vector3f(0f,0f,-speed2);
            case UP:
                return new Vector3f(0f,0f,speed2);
            case RIGHT: 
                return new Vector3f(-speed2,0f,0f);
            case LEFT:
                return new Vector3f(speed2,0f,0f);
        }
        return new Vector3f();
    }
    static ArrayList<Double> control_points = new ArrayList<Double>();
   // static ArrayList<Double> control_points2 = new ArrayList<Double>();
    
    private static void inicialize() {
        for (int i = 4; i < 100; i += 8) {
            control_points.add((double)i);
           // control_points2.add((double)(i - 2));
        }
    }
    float speed2 = 0.05f;
    int switcher;

    boolean iswall(float x, float z) {
        //List<Spatial> children = rootNode.getChildren();
        List<Spatial> children = walls.getChildren();
        for ( Spatial child : children ) {
            if (child.getLocalTranslation().x == x && child.getLocalTranslation().z == z) {
                return false;
            }
        }
        return true;
    }
    
    public boolean isWall(float x, float z) {
        List<Spatial> children = rootNode.getChildren();
        for ( Spatial child : children ) {
            float f1 = child.getLocalTranslation().x + 2;
            float f2 = child.getLocalTranslation().x - 2;
            float s2 = child.getLocalTranslation().z - 2;
            float s1 = child.getLocalTranslation().z + 2;
            if ((f1 >= x) && (f2 <= x) && (s1 >= z) && (s1 <= z)) {
                return true; 
            }
        }
        return false;
    }
}
