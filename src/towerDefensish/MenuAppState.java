/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package towerDefensish;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.collision.CollisionResults;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.input.controls.Trigger;
import com.jme3.light.DirectionalLight;
import com.jme3.light.SpotLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.FadeFilter;
import com.jme3.renderer.Camera;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.shadow.DirectionalLightShadowFilter;
import com.jme3.shadow.DirectionalLightShadowRenderer;
import java.util.ArrayList;

/**
 *
 * @author Rune Barrett
 */
public class MenuAppState extends AbstractAppState {

    //private final static Trigger TRIGGER_STARTGAME = new KeyTrigger(KeyInput.KEY_SPACE);
    private final static Trigger TRIGGER_STARTGAME = new MouseButtonTrigger(MouseInput.BUTTON_LEFT);
    private final static String MAPPING_STARTGAME = "Charge";
    private SimpleApplication app;
    private Camera cam;
    private AssetManager assetManager;
    private Node menuPlayerNode;
    private Node menuTowerNode;
    private Node rootNode;
    private Node menuFloorNode;
    private ArrayList<Spatial> towers;
    private AppStateManager stateManager;
    private FadeFilter fade;
    //private FilterPostProcessor fpp;
    DirectionalLight menuSun;
    private boolean startGame = false;
    private boolean GameHasStarted = false;
    SpotLight spot;
    SpotLight spot1;
    float timer = 6;

    @Override
    public void initialize(AppStateManager stateManager, Application app) {

        super.initialize(stateManager, app);
        this.app = (SimpleApplication) app;
        cam = this.app.getCamera();
        rootNode = this.app.getRootNode();
        assetManager = this.app.getAssetManager();
        this.stateManager = stateManager;

        //bulletAppState = new BulletAppState();
        mappingsAndTriggers(app);
        //stateManager.attach(bulletAppState);
        rootNode.setShadowMode(ShadowMode.Off);
        createNodes();
        createFloor();
        createBase();
        createTowers();

        lightCamShadowEffects();
        //fadeFilter(app);

    }
    private ActionListener actionListenerMenu = new ActionListener() {
        public void onAction(String name, boolean isPressed, float tpf) {
            timer = 0;
        }
    };
    boolean alreadyFaded = false;

    @Override
    public void update(float tpf) {

        if (!GameHasStarted && timer < 5) {
            timer += tpf;
            fade.fadeOut();
            if (timer > 4) {
                fade.fadeIn();
                startGame = true;
                GameHasStarted = true;
            }
        }

//        CollisionResults results = clickRayCollission();
//        if (results.size() > 0) {
//            Geometry target = results.getClosestCollision().getGeometry();
//            if (target.getName().equals("player") && !alreadyFaded) {
//                System.out.println("triggered");
//                //fade.fadeOut();
//                alreadyFaded = true;
//            }
//        }
    }

    private void createNodes() {
        menuPlayerNode = new Node("playerNode");
        menuTowerNode = new Node("towerNode");
        menuFloorNode = new Node("floorNode");
        rootNode.attachChild(menuPlayerNode);
        rootNode.attachChild(menuFloorNode);
        rootNode.attachChild(menuTowerNode);
    }

    private void createFloor() {
        Spatial floor = assetManager.loadModel("Scenes/TowerDefenseTerrain2.j3o");
        floor.setLocalTranslation(0, 0, -140f);
        floor.scale(1.4f);
        menuFloorNode.attachChild(floor);

        floor.setShadowMode(ShadowMode.CastAndReceive);

//        floorPhy = new RigidBodyControl(0.0f);
//        floor.addControl(floorPhy);
//        bulletAppState.getPhysicsSpace().add(floorPhy);
    }

    private void createBase() {
        Vector3f basePos = new Vector3f(0, 2.0f, 0);
        Spatial base = assetManager.loadModel("Textures/Base/base.obj");
        base.rotate(0, -FastMath.DEG_TO_RAD * 90, 0);
        base.scale(4f);
        base.setLocalTranslation(basePos);
        base.setName("player");
        base.setShadowMode(ShadowMode.CastAndReceive);
        menuPlayerNode.attachChild(base);
    }

    private void createTowers() {
        ArrayList<Vector3f> vList = new ArrayList<Vector3f>();
        vList.add(new Vector3f(43.0f, 1f, -35.0f));
        vList.add(new Vector3f(15.0f, 0.0f, -20.0f));
        vList.add(new Vector3f(-43.0f, 0.0f, -35.0f));
        vList.add(new Vector3f(-15.0f, 0.0f, -20.0f));
        vList.add(new Vector3f(20.0f, 0.0f, 0.0f));
        vList.add(new Vector3f(-20.0f, 0.0f, 0.0f));

        towers = new ArrayList<Spatial>();
        for (int i = 0; i < vList.size(); i++) {
            Spatial tower = assetManager.loadModel("Textures/Turret/turret.obj");
            tower.setName("Tower " + i);
            tower.setLocalTranslation(vList.get(i));
            tower.setUserData("index", i);
            tower.setUserData("chargesNum", 20);
            tower.setUserData("height", 13.3f);
            tower.setUserData("health", 5);
            tower.scale(3.0f);
            tower.setShadowMode(ShadowMode.Cast);

            //pl.setRadius(4);
            menuTowerNode.attachChild(tower);
            towers.add(tower);
        }
    }

    private void lightCamShadowEffects() {

//        cam.setLocation(new Vector3f(-36.95293f, 10.859799f, 24.602526f));
//        cam.setRotation(new Quaternion(0.01599581f, 0.90922946f, -0.03508482f, 0.4145058f));

        cam.setLocation(new Vector3f(-36.95293f, 10.859799f, 24.602526f));
        cam.setRotation(new Quaternion(-0.0013211549f, 0.92241716f, 0.0031582424f, 0.3861797f));

        menuSun = new DirectionalLight();
        menuSun.setDirection(new Vector3f(-0.7f, -0.5f, 0.3f));
        rootNode.addLight(menuSun);

        spot = new SpotLight();
        spot.setSpotRange(100f);                           // distance
        spot.setSpotInnerAngle(45f * FastMath.DEG_TO_RAD); // inner light cone (central beam)
        spot.setSpotOuterAngle(20f * FastMath.DEG_TO_RAD); // outer light cone (edge of the light)
        spot.setColor(ColorRGBA.White.mult(2.3f));         // light color
        spot.setPosition(new Vector3f(-5.186699f, 10.38011f, 29.306236f));
        spot.setDirection(new Vector3f(0.43758437f, -0.3816585f, -0.8141602f).negate());             // shine forward from camera loc
        rootNode.addLight(spot);

        spot1 = new SpotLight();
        spot1.setSpotRange(100f);                           // distance
        spot1.setSpotInnerAngle(45f * FastMath.DEG_TO_RAD); // inner light cone (central beam)
        spot1.setSpotOuterAngle(20f * FastMath.DEG_TO_RAD); // outer light cone (edge of the light)
        spot1.setColor(ColorRGBA.White.mult(2.3f));         // light color
        spot1.setPosition(new Vector3f(-25.186699f, 19.38011f, 29.306236f));
        spot1.setDirection(new Vector3f(0.43758437f, -0.3816585f, -0.8141602f).negate());             // shine forward from camera loc
        rootNode.addLight(spot1);

        /* two ways to cast drop shadows */
        DirectionalLightShadowRenderer dlsr =
                new DirectionalLightShadowRenderer(assetManager, 1024, 2);
        dlsr.setLight(menuSun);
        app.getViewPort().addProcessor(dlsr);

        FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
        fade = new FadeFilter(2);
        fpp.addFilter(fade);

        DirectionalLightShadowFilter dlsf =
                new DirectionalLightShadowFilter(assetManager, 1024, 2);
        dlsf.setLight(menuSun);
        dlsf.setEnabled(true); // try true or false
        fpp.addFilter(dlsf);
        app.getViewPort().addProcessor(fpp);
    }

//    private CollisionResults clickRayCollission() {
//        CollisionResults results = new CollisionResults();
//        Vector2f click2d = app.getInputManager().getCursorPosition();
//        Vector3f click3d = cam.getWorldCoordinates(new Vector2f(click2d.getX(), click2d.getY()), 0f);
//        Vector3f dir = cam.getWorldCoordinates(new Vector2f(click2d.getX(), click2d.getY()), 1f).subtractLocal(click3d);
//        Ray ray = new Ray(click3d, dir);
//        rootNode.collideWith(ray, results);
//        return results;
//    }

    @Override
    public void cleanup() {
        //System.out.println("cleanup");
        //
        menuPlayerNode.removeFromParent();
        menuTowerNode.removeFromParent();
        menuFloorNode.removeFromParent();
        rootNode.removeLight(menuSun);
        rootNode.removeLight(spot);
        rootNode.removeLight(spot1);
        app.getInputManager().removeListener(actionListenerMenu);
        app.getInputManager().deleteMapping(MAPPING_STARTGAME);
        //startGame = true;
    }

    public ArrayList<Spatial> getTowers() {
        return towers;
    }

    private void mappingsAndTriggers(Application app) {
        app.getInputManager().addMapping(MAPPING_STARTGAME, TRIGGER_STARTGAME);
        app.getInputManager().addListener(actionListenerMenu, new String[]{MAPPING_STARTGAME});

    }

    public boolean getStartGame() {
        return startGame;
    }

    public void setStartGame(boolean bool) {
        startGame = bool;
    }
}
