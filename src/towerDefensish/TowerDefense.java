package towerDefensish;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.collision.CollisionResults;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.input.controls.Trigger;
import com.jme3.renderer.RenderManager;
import com.jme3.system.AppSettings;
import com.jme3.input.controls.ActionListener;
import com.jme3.light.PointLight;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.texture.Texture;
import com.jme3.util.SkyFactory;

/**
 *
 * @author Rune Barrett
 */
public class TowerDefense extends SimpleApplication {

    private final static Trigger TRIGGER_CHARGE = new KeyTrigger(KeyInput.KEY_C);
    private final static Trigger TRIGGER_CHARGE2 = new MouseButtonTrigger(MouseInput.BUTTON_RIGHT);
    private final static Trigger TRIGGER_SELECT = new MouseButtonTrigger(MouseInput.BUTTON_LEFT);
    private final static Trigger TRIGGER_SHOOT = new MouseButtonTrigger(MouseInput.BUTTON_LEFT);
    private final static Trigger TRIGGER_SHIFT = new KeyTrigger((KeyInput.KEY_LSHIFT));
    private final static Trigger TRIGGER_SELECTSPELL1 = new KeyTrigger((KeyInput.KEY_Q));
    private final static Trigger TRIGGER_SELECTSPELL2 = new KeyTrigger((KeyInput.KEY_W));
    private final static Trigger TRIGGER_SELECTSPELL3 = new KeyTrigger((KeyInput.KEY_E));
    private final static Trigger TRIGGER_SELECTSPELL4 = new KeyTrigger((KeyInput.KEY_R));
    private final static String MAPPING_CHARGE = "Charge";
    private final static String MAPPING_SELECT = "Select";
    private final static String MAPPING_SHOOT = "Shoot";
    private final static String MAPPING_SHIFT = "Shift";
    private final static String MAPPING_SELECTSPELL1 = "SelectSpell 1";
    private final static String MAPPING_SELECTSPELL2 = "SelectSpell 2";
    private final static String MAPPING_SELECTSPELL3 = "SelectSpell 3";
    private final static String MAPPING_SELECTSPELL4 = "SelectSpell 4";
    private int selected = -1;
    private int oldSelected = -1;
    private GamePlayAppState GPAState;
    private MenuAppState menuState;
    private PointLight lamp = new PointLight();
    private boolean shiftHeld;

    public static void main(String[] args) {
        AppSettings settings = new AppSettings(true);
        settings.setTitle("Tower Defensish");
        //settings.setSettingsDialogImage("Interface/towerDefense.png");
        //app.setShowSettings(false);

        TowerDefense app = new TowerDefense();
        app.setSettings(settings);
        app.start();

    }

    @Override
    public void simpleInitApp() {

        GPAState = new GamePlayAppState();
        GPAState.setScreenSize(settings.getHeight(), settings.getWidth());
        GPAState.setEnabled(false);

        menuState = new MenuAppState();
        stateManager.attach(menuState);


        inGameSettings();
        initSky();
    }

    @Override
    public void simpleUpdate(float tpf) {
        if (menuState.getStartGame()) {
            disableWASDandAddMappingsAndListeners();
            stateManager.detach(menuState);
            //initSky();
            GPAState.setEnabled(true);
            stateManager.attach(GPAState);
            menuState.setStartGame(false);
        }
        if(GPAState.getWaveCleared()){
            System.out.println("Time for Act I!");
            GPAState.setWaveCleared(false);
            GPAState.setEnabled(false);
            GPAState.setSmallPlayerInfoText("     Nice job defending your base!");
            GPAState.cleanup();
            flyCam.setEnabled(false);
            GPAState.fogOff(tpf);
            stateManager.attach(menuState);
        }else if(GPAState.getGameLost()){
                        GPAState.setGameLost(false);
            GPAState.setEnabled(false);
            GPAState.setSmallPlayerInfoText("     Well, that was not the kind of base defending you were supposed to do..");
            GPAState.cleanup();
            flyCam.setEnabled(false);
            GPAState.fogOff(tpf);
            stateManager.attach(menuState);
        }
    }

    private ActionListener actionListener = new ActionListener() {
        public void onAction(String name, boolean isPressed, float tpf) {
            if (GPAState.isEnabled()) {
                if (name.equals(MAPPING_SHIFT)) {
                    shiftHeld = true;
                    inputManager.setCursorVisible(false);
                    flyCam.setEnabled(true);

                }
                if (name.equals(MAPPING_SHIFT) && !isPressed) {
                    shiftHeld = false;
                    inputManager.setCursorVisible(false);
                    flyCam.setEnabled(false);
                    Vector3f c = new Vector3f(0.0f, 28.0f, 75.0f);
                    cam.setLocation(c);
                    cam.setRotation(new Quaternion(0.0f, 1.0f, 0.0f, 0));
                }
                if (name.equals(MAPPING_SHOOT) && !isPressed && shiftHeld) {
                    GPAState.shoot();
                }
                if (name.equals(MAPPING_SELECT) && !isPressed && !shiftHeld) {
                    CollisionResults results = clickRayCollission();

                    if (results.size() > 0) {
                        Geometry target = results.getClosestCollision().getGeometry();
                        if (target.getControl(TowerControl.class) instanceof TowerControl) {
                            selected = target.getControl(TowerControl.class).getIndex();
                            try {
                                rootNode.getChild("Tower " + oldSelected).getControl(TowerControl.class).getSpatial().removeLight(lamp);//do something
                            } catch (NullPointerException e) {
                            }
                            rootNode.getChild("Tower " + selected).getControl(TowerControl.class).getSpatial().addLight(lamp);//doSomething
                        }
                    } else {
                    }
                }
                if (name.equals(MAPPING_CHARGE) && !isPressed) {
                    int budget = stateManager.getState(GamePlayAppState.class).getBudget();
                    if (budget > 0) {
                        GPAState.setBudget(budget - 1);
                        try {
                            rootNode.getChild("Tower " + selected).getControl(TowerControl.class).addCharge();
                        } catch (NullPointerException e) {
                        }
                    }
                }

                if (name.equals(MAPPING_SELECTSPELL1) && !isPressed) {
                    clearSpellSelection();
                    GPAState.setFireball(true);
                }

                if (name.equals(MAPPING_SELECTSPELL2) && !isPressed) {
                    clearSpellSelection();
                    GPAState.setFrostBolt(true);
                }
                if (name.equals(MAPPING_SELECTSPELL3) && !isPressed) {
                    clearSpellSelection();
                    GPAState.setFrostNova(true);
                }
                if (name.equals(MAPPING_SELECTSPELL4) && !isPressed) {
                    clearSpellSelection();
                    GPAState.setBigSpell(true);
                }


                if (oldSelected != selected) {
                    try {
                        rootNode.getChild("Tower " + oldSelected).getControl(TowerControl.class).getSpatial().removeLight(lamp);//do something
                    } catch (NullPointerException npe) {
                    }
                }
                oldSelected = selected;

            }
        }
    };

    private void initSky() {
        Texture west = assetManager.loadTexture("Textures/Sky/Lagoon/lagoon_west.jpg");
        Texture east = assetManager.loadTexture("Textures/Sky/Lagoon/lagoon_east.jpg");
        Texture north = assetManager.loadTexture("Textures/Sky/Lagoon/lagoon_north.jpg");
        Texture south = assetManager.loadTexture("Textures/Sky/Lagoon/lagoon_south.jpg");
        Texture up = assetManager.loadTexture("Textures/Sky/Lagoon/lagoon_up.jpg");
        Texture down = assetManager.loadTexture("Textures/Sky/Lagoon/lagoon_down.jpg");
        Spatial sky = SkyFactory.createSky(assetManager, west, east, north, south, up, down);
        sky.rotate(0, FastMath.DEG_TO_RAD * 100, 0);
        rootNode.attachChild(sky);
    }

    private void disableWASDandAddMappingsAndListeners() {
        //anonymyous Appstate for disabling WASD controls, because the flycam is initialized after simpleInitApp() 
        stateManager.attach(new AbstractAppState() {
            @Override
            public void initialize(AppStateManager stateManager, Application app) {
                super.initialize(stateManager, app);
                addMappingsAndListeners();
                stateManager.detach(this);
            }
        });
    }

    private CollisionResults clickRayCollission() {
        CollisionResults results = new CollisionResults();
        Vector2f click2d = inputManager.getCursorPosition();
        Vector3f click3d = cam.getWorldCoordinates(new Vector2f(click2d.getX(), click2d.getY()), 0f);
        Vector3f dir = cam.getWorldCoordinates(new Vector2f(click2d.getX(), click2d.getY()), 1f).subtractLocal(click3d);
        Ray ray = new Ray(click3d, dir);
        rootNode.collideWith(ray, results);
        return results;
    }

    private void clearSpellSelection() {
        GPAState.setFireball(false);
        GPAState.setFrostBolt(false);
        GPAState.setFrostNova(false);
        GPAState.setBigSpell(false);
    }

    private void addMappingsAndListeners() {
        inputManager.deleteMapping("FLYCAM_Forward");
        inputManager.deleteMapping("FLYCAM_Lower");
        inputManager.deleteMapping("FLYCAM_StrafeLeft");
        inputManager.deleteMapping("FLYCAM_Rise");

        inputManager.addMapping(MAPPING_CHARGE, TRIGGER_CHARGE, TRIGGER_CHARGE2);
        inputManager.addMapping(MAPPING_SELECT, TRIGGER_SELECT);
        inputManager.addMapping(MAPPING_SHOOT, TRIGGER_SHOOT);
        inputManager.addMapping(MAPPING_SHIFT, TRIGGER_SHIFT);
        inputManager.addMapping(MAPPING_SELECTSPELL1, TRIGGER_SELECTSPELL1);
        inputManager.addMapping(MAPPING_SELECTSPELL2, TRIGGER_SELECTSPELL2);
        inputManager.addMapping(MAPPING_SELECTSPELL3, TRIGGER_SELECTSPELL3);
        inputManager.addMapping(MAPPING_SELECTSPELL4, TRIGGER_SELECTSPELL4);

        inputManager.addListener(actionListener, new String[]{MAPPING_CHARGE});
        inputManager.addListener(actionListener, new String[]{MAPPING_SELECT});
        inputManager.addListener(actionListener, new String[]{MAPPING_SHOOT});
        inputManager.addListener(actionListener, new String[]{MAPPING_SHIFT});
        inputManager.addListener(actionListener, new String[]{MAPPING_SELECTSPELL1});
        inputManager.addListener(actionListener, new String[]{MAPPING_SELECTSPELL2});
        inputManager.addListener(actionListener, new String[]{MAPPING_SELECTSPELL3});
        inputManager.addListener(actionListener, new String[]{MAPPING_SELECTSPELL4});
    }

    private void inGameSettings() {
        setDisplayFps(false);
        setDisplayStatView(false);

        //flyCam.setDragToRotate(true);
        flyCam.setEnabled(false);
        flyCam.setMoveSpeed(0);
        inputManager.setCursorVisible(true);

        flyCam.setMoveSpeed(50.0f);
    }

    public int getSelected() {
        return selected;
    }

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }
}
