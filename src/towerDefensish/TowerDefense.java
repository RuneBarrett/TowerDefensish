package towerDefensish;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.AnimEventListener;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.collision.CollisionResults;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.input.controls.Trigger;
import com.jme3.renderer.RenderManager;
import com.jme3.system.AppSettings;
import com.jme3.input.controls.ActionListener;
import com.jme3.light.PointLight;
import com.jme3.math.ColorRGBA;
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
    private StartScreenAppState startState;
    private PointLight lamp = new PointLight();
    private BitmapText statsTitle;
    private BitmapText playerHealth;
    private BitmapText towerHealth;
    private BitmapText playerCharges;
    private BitmapText towerCharges;
    private BitmapText towerName;
    private BitmapText towerBullets;
    private BitmapText playerCreepCount;
    private BitmapText budgetIncremented;
    private BitmapText playerMana;
    private BitmapText infoMessage;
    private BitmapText cooldownText;
    private float chargeTimer;
    private float infoTimer;
    private boolean shiftHeld;

    public static void main(String[] args) {
        AppSettings settings = new AppSettings(true);
        settings.setTitle("Tower Defense");
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

        startState = new StartScreenAppState();
        stateManager.attach(startState);

        inGameSettings();
        initSky();
    }

    @Override
    public void simpleUpdate(float tpf) {
        if (startState.getStartGame()) {
            disableWASDandAddMappingsAndListeners();
            stateManager.detach(startState);
            //initSky();
            GPAState.setEnabled(true);
            stateManager.attach(GPAState);
            startState.setStartGame(false);
        }
        if(GPAState.getWaveCleared()){
            System.out.println("Time for Act I!");
            GPAState.setWaveCleared(false);
        }
    }

    private void update2DGui(float tpf) {
        //Set 2d GUI
        statsTitle.setText("Current Statistics");
        playerHealth.setText("     Health:         " + GPAState.getHealth());
        playerCharges.setText("     Charges:     " + GPAState.getBudget());
        playerCreepCount.setText("     Creeps Killed:   " + GPAState.getCreepsKilled());
        playerMana.setText("     Mana:    " + GPAState.getMana());
        chargeTimer += tpf;
        if (GPAState.getChargeAdded()) {
            budgetIncremented.setText("     Charge added! Keep killing!");
            GPAState.setChargeAdded(false);
        }
        if (chargeTimer > 5) {
            chargeTimer = 0;
            budgetIncremented.setText("     Kill more.. Now..");
        }

        CollisionResults results = clickRayCollission();
        if (results.size() > 0) {
            Geometry target = results.getClosestCollision().getGeometry();
            if (target.getControl(TowerControl.class) instanceof TowerControl) {
                towerName.setText(target.getName());
                towerCharges.setText("     Charges:     " + target.getControl(TowerControl.class).getCharges());
                towerHealth.setText("     Health:     " + target.getControl(TowerControl.class).getHealth());
                towerBullets.setText("     Bullets:     " + target.getControl(TowerControl.class).getBullets());
            }
        } else {
            towerName.setText("");
            towerCharges.setText("");
            towerHealth.setText("");
            towerBullets.setText("");
        }
        infoTimer += tpf;
        if (GPAState.isNewInfo()) {
            infoTimer = 0;
            //infoMessage.setText(state.getInfoMessage());
            GPAState.setIsNewInfo(false);
        }
        if (infoTimer > 5) {
            infoMessage.setText("");
        }
        if (GPAState.getCooldown() > 0) {
            cooldownText.setText("Cooldown: " + GPAState.getCooldown());
        } else {
            cooldownText.setText("");
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

    private void initGui() {
        //Selection light
        lamp.setColor(ColorRGBA.Cyan);
        lamp.setPosition(new Vector3f(0, 3, 0));

        //Stat title
        guiFont = assetManager.loadFont("Interface/Fonts/Cracked.fnt");

        statsTitle = new BitmapText(guiFont);
        statsTitle.setSize(guiFont.getCharSet().getRenderedSize());
        statsTitle.move(1, // X
                settings.getHeight(), // Y
                0); // Z (depth layer)
        guiNode.attachChild(statsTitle);

        //Tower title/name
        towerName = new BitmapText(guiFont);
        towerName.setSize(guiFont.getCharSet().getRenderedSize());
        towerName.move(0, // X
                0 + towerName.getLineHeight() * 3.3f, // Y
                0); // Z (depth layer)
        guiNode.attachChild(towerName);

        //Info message
        infoMessage = new BitmapText(guiFont);
        infoMessage.setSize(guiFont.getCharSet().getRenderedSize());
        infoMessage.move(
                settings.getWidth() / 2 - 120, // X
                settings.getHeight() / 5, // Y
                0); // Z (depth layer)
        guiNode.attachChild(infoMessage);

//------------------------------------------------------------------------------
        //Player health
        guiFont = assetManager.loadFont("Interface/Fonts/Cracked28.fnt");
        playerHealth = new BitmapText(guiFont);
        playerHealth.setSize(guiFont.getCharSet().getRenderedSize());
        playerHealth.move(1, // X
                settings.getHeight() - playerHealth.getHeight() - 10, // Y
                0); // Z (depth layer)
        guiNode.attachChild(playerHealth);

        //Player charges
        playerCharges = new BitmapText(guiFont);
        playerCharges.setSize(guiFont.getCharSet().getRenderedSize());
        playerCharges.move(1, // X
                settings.getHeight() - playerHealth.getHeight() * 2 - 10, // Y
                0); // Z (depth layer)
        guiNode.attachChild(playerCharges);

        //Charge added to budget
        budgetIncremented = new BitmapText(guiFont);
        budgetIncremented.setSize(guiFont.getCharSet().getRenderedSize());
        budgetIncremented.move(1, // X
                settings.getHeight() - playerHealth.getHeight() * 3 - 10, // Y
                0); // Z (depth layer)
        guiNode.attachChild(budgetIncremented);

        //Player charges
        playerCreepCount = new BitmapText(guiFont);
        playerCreepCount.setSize(guiFont.getCharSet().getRenderedSize());
        playerCreepCount.move(1, // X
                settings.getHeight() - playerCreepCount.getHeight() * 4 - 10, // Y
                0); // Z (depth layer)
        guiNode.attachChild(playerCreepCount);

        //Player Mana
        playerMana = new BitmapText(guiFont);
        playerMana.setSize(guiFont.getCharSet().getRenderedSize());
        playerMana.move(1, // X
                settings.getHeight() - playerMana.getHeight() * 5 - 10, // Y
                0); // Z (depth layer)
        guiNode.attachChild(playerMana);

        //towerHealth
        towerHealth = new BitmapText(guiFont);
        towerHealth.setSize(guiFont.getCharSet().getRenderedSize());
        towerHealth.move(1, // X
                towerHealth.getHeight() * 3.5f, // Y
                0); // Z (depth layer)
        guiNode.attachChild(towerHealth);

        //towerCharges
        towerCharges = new BitmapText(guiFont);
        towerCharges.setSize(guiFont.getCharSet().getRenderedSize());
        towerCharges.move(1, // X
                towerCharges.getHeight() * 2.5f, // Y
                0); // Z (depth layer)
        guiNode.attachChild(towerCharges);

        //towerBullets
        towerBullets = new BitmapText(guiFont);
        towerBullets.setSize(guiFont.getCharSet().getRenderedSize());
        towerBullets.move(1, // X
                towerBullets.getHeight() * 1.5f, // Y
                0); // Z (depth layer)
        guiNode.attachChild(towerBullets);

        //Colldown
        cooldownText = new BitmapText(guiFont);
        cooldownText.setSize(guiFont.getCharSet().getRenderedSize());
        cooldownText.move(settings.getWidth() - 200, // X
                settings.getHeight() - cooldownText.getHeight(), // Y
                0); // Z (depth layer)
        guiNode.attachChild(cooldownText);

//        Picture frame = new Picture("User interface frame");
//        frame.setImage(assetManager, "Interface/Pics/sword.png", false);
//        frame.move(settings.getWidth(), settings.getHeight() - 200, -2);
//        frame.rotate(0, 0, FastMath.DEG_TO_RAD * 90);
//        frame.setWidth(150);
//        frame.setHeight(220);
//        guiNode.attachChild(frame);
    }

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
