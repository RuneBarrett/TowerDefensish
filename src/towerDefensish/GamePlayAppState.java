/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package towerDefensish;

import spellControls.FireBallControl;
import spellControls.FrostboltControl;
import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.LoopMode;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.asset.TextureKey;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.collision.shapes.MeshCollisionShape;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.bullet.control.GhostControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.collision.CollisionResults;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.light.PointLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.FogFilter;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Line;
import com.jme3.scene.shape.Sphere;
import java.util.ArrayList;
import spellControls.NormalSpellControl;

/**
 *
 * @author Rune Barrett
 */
public class GamePlayAppState extends AbstractAppState {

    private SimpleApplication app;
    private Camera cam;
    private AssetManager assetManager;
    private AppStateManager stateManager;
    private AnimControl control;
    private AnimChannel channel;
    private PointLight ChargedLight;
    private PlayerControl playerControl;
    private Node playerNode;
    private Node towerNode;
    private Node creepNode;
    private Node beamNode;
    private Node rootNode;
    private Node ballNode;
    private Node explosionNode;
    private BulletAppState bulletAppState;
    private RigidBodyControl towerPhy;
    private RigidBodyControl floorPhy;
    private RigidBodyControl ballPhy;
    private RigidBodyControl basePhy;
    private BetterCharacterControl creepPhy;
    private ArrayList<Spatial> creeps;
    private ArrayList<Spatial> towers;
    private Mesh spellMesh;
    private static final String ANI_FLY = "my_animation";
    //Player settings - should be refactored into the playerControl
    private int level = 0;
    private int score = 0;
    private int health = 20;
    private int budget = 5;
    private float mana;
    private float maxMana = 50;
    private int creepsKilled = 0;
    private boolean fireballOn;
    private boolean frostBoltOn;
    private boolean frostNovaOn;
    private boolean bigSpellOn;
    private boolean lastGameWon = false;
    //GUI Settings
    private BitmapText statsTitle;
    private BitmapText playerHealth;
    private BitmapText playerExperience;
    private BitmapText playerLevel;
    private BitmapText towerHealth;
    private BitmapText playerCharges;
    private BitmapText towerCharges;
    private BitmapText towerName;
    private BitmapText towerBullets;
    private BitmapText playerCreepCount;
    private BitmapText smallPlayerInfo;
    private BitmapText playerMana;
    private BitmapText infoMessage;
    private BitmapText cooldownText;
    private int screenHeight;
    private int screenWidth;
    private String info;
    private float chargeTimer;
    private float infoTimer;
    private float budgetTimer = 0;
    private float beamTimer = 0;
    private int numOfCreeps = 20;
    private int creepHealth = 12;
    private int bNum = 0;
    private boolean chargeAdded = false;
    private float baseShapeScale;
    private boolean isNewInfo;
    private float cooldown;
    private boolean wavecleared = false;
    private boolean waveAlreadyCleared = false;
    private FilterPostProcessor fpp;
    private FogFilter fogFilter;
    private DirectionalLight sun;
    private Node collisionNode;
    private boolean gameLost = false;
    ;
    private float gameLostTimer = 0;

    @Override
    public void initialize(AppStateManager stateManager, Application app) {

        super.initialize(stateManager, app);
        this.app = (SimpleApplication) app;
        cam = this.app.getCamera();
        rootNode = this.app.getRootNode();
        assetManager = this.app.getAssetManager();
        this.stateManager = stateManager;
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);

        spellMesh = new Sphere(32, 32, 2.50f, true, false);
        mana = maxMana;

        lightsAndCam();
        createNodes();
        createFloor();
        createBase();
        createCreeps(numOfCreeps);
        createTowers();

        fpp = new FilterPostProcessor(assetManager);
        app.getViewPort().addProcessor(fpp);

        fogFilter = new FogFilter();
        fogFilter.setFogDistance(300);
        fogFilter.setFogDensity(0.7f);
        //fogFilter.setFogColor(ColorRGBA.Black);
        fpp.addFilter(fogFilter);

        initGui();
        playerControl = playerNode.getChild("player").getControl(PlayerControl.class);
        smallPlayerInfo.setText("      Start killing those ugly things!");
        //bulletAppState.setDebugEnabled(true);
    }

    @Override
    public void update(float tpf) {
        collisionNode.setLocalTranslation(ballNode.getLocalTranslation());
        update2DGui(tpf);
        if (health <= 0) {
            if (gameLostTimer == 0) {
                System.out.println("The player lost.");
                lastGameWon = false;
                playerControl.setDead(true);
            }
            gameLostTimer += tpf;
            if (gameLostTimer > 8f) {
                gameLost = true;
            }

        } else if (creeps.isEmpty()) {
            if (!waveAlreadyCleared) {
                wavecleared = true;
                waveAlreadyCleared = true;
            }
            //stateManager.detach(this);
        }
        budgetTimer += tpf;
        if (budgetTimer >= 15.0f) {
            budgetTimer = 0.0f;
            budget++;
        }
        beamTimer += tpf;
        if (beamTimer >= 0.3f) {
            beamNode.detachAllChildren();
            beamTimer = 0;
        }
        if (mana < maxMana) {
            mana += tpf;
        }
        cooldown -= tpf;
    }

    public void shoot() {
        if (cooldown < 0) {
            cooldown = 8;
            Geometry ballGeo = new Geometry("NormalSpell", spellMesh);

            Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
            ballGeo.setMaterial(mat);
            ballGeo.setLocalTranslation(cam.getLocation());
            Geometry ballGhost = new Geometry("BallGhost");
            ballGhost.setLocalTranslation(ballGeo.getLocalTranslation());

            TextureKey wood = new TextureKey("Interface/Pics/wood.png", false);
            mat.setTexture("DiffuseMap", assetManager.loadTexture(wood));

            if (fireballOn) {
                if (mana > 18) {
                    ballGeo.setName("Fireball");
                    TextureKey fire = new TextureKey("Interface/Pics/flames.png", false);
                    mat.setTexture("DiffuseMap", assetManager.loadTexture(fire));
                    ballGeo.addControl(new FireBallControl(this, assetManager, ballNode, ballGeo, bulletAppState));
                    mana = mana - 18;
                } else {
                    info = "Mana too low to cast a Fireball!";
                    isNewInfo = true;
                }
            } else if (frostBoltOn) {
                if (mana > 12) {
                    ballGeo.setName("Frostbolt");
                    TextureKey ice = new TextureKey("Interface/Pics/chrislinder_ice_6.png", false);
                    mat.setTexture("DiffuseMap", assetManager.loadTexture(ice));
                    ballGeo.addControl(new FrostboltControl(this, assetManager, ballNode, ballGeo, bulletAppState));
                    mana = mana - 12;
                } else {
                    info = "Mana too low to cast a Frostbolt!";
                    isNewInfo = true;
                }
            } else if (frostNovaOn) {
                TextureKey ice = new TextureKey("Interface/Pics/ice-block.png", false);
                mat.setTexture("DiffuseMap", assetManager.loadTexture(ice));
                //ballGeo.addControl(new FrostBoltControl(assetManager, ballNode, ballGeo));
            } else {
                ballGeo.addControl(new NormalSpellControl(this, assetManager, ballNode, ballGeo, bulletAppState));
                mana = mana - 5;
            }
            mat.setColor("Specular", ColorRGBA.White.mult(ColorRGBA.Red));
            mat.setFloat("Shininess", 200f);


            //ballGhost.addControl(ghost);
            //ballNode.addControl(ghost);
            //ghost.removeCollideWithGroup(PhysicsCollisionObject.COLLISION_GROUP_01);
            //ghost.addCollideWithGroup(PhysicsCollisionObject.COLLISION_GROUP_NONE);
            //collisionNode.addControl(ghost);
            ballNode.attachChild(ballGeo);

            //GhostControl ghost = new GhostControl(new SphereCollisionShape(10));
            ballPhy = new RigidBodyControl(50.5f);
            ballGeo.addControl(ballPhy);
            //ballGeo.addControl(ghost);
            bulletAppState.getPhysicsSpace().add(ballPhy);
            //bulletAppState.getPhysicsSpace().add(ghost);
            ballPhy.setCcdSweptSphereRadius(.1f);
            ballPhy.setCcdMotionThreshold(0.001f);
            ballPhy.setAngularVelocity(new Vector3f(-FastMath.nextRandomFloat() * 25, FastMath.nextRandomFloat() * 5 - 5, FastMath.nextRandomFloat() * 5 - 5));



            ballPhy.setLinearVelocity(cam.getDirection().mult(65));
        } else {
            info = "Spells are on cooldown.";
            isNewInfo = true;
        }
    }

    private void createNodes() {
        playerNode = new Node("playerNode");
        towerNode = new Node("towerNode");
        creepNode = new Node("creepNode");
        beamNode = new Node("beamNode");
        ballNode = new Node("ballNode");
        collisionNode = new Node("collisionNode");
        explosionNode = new Node("explosionNode");
        rootNode.attachChild(collisionNode);
        rootNode.attachChild(playerNode);
        rootNode.attachChild(towerNode);
        rootNode.attachChild(creepNode);
        rootNode.attachChild(beamNode);
        rootNode.attachChild(ballNode);
        rootNode.attachChild(explosionNode);
    }

    private void createFloor() {
        Spatial floor = assetManager.loadModel("Scenes/TowerDefenseTerrain2.j3o");
        floor.setLocalTranslation(0, 0, -140f);
        floor.scale(1.4f);
        rootNode.attachChild(floor);

        floorPhy = new RigidBodyControl(0.0f);
        floor.addControl(floorPhy);
        bulletAppState.getPhysicsSpace().add(floorPhy);
    }

    private void createBase() {
        Vector3f basePos = new Vector3f(0, 2.0f, 0);
        Spatial base = assetManager.loadModel("Textures/Base/base.obj");
        base.rotate(0, -FastMath.DEG_TO_RAD * 90, 0);
        base.scale(4f);
        base.setLocalTranslation(basePos);
        base.addControl(new PlayerControl(this, playerNode, assetManager));
        base.setName("player");
        base.setUserData("xp", 0);
        base.setUserData("level", 1);
        playerNode.attachChild(base);

        basePhy = new RigidBodyControl(0f);
        base.addControl(basePhy);
        bulletAppState.getPhysicsSpace().add(basePhy);
        baseShapeScale = 1.98f;
        basePhy.getCollisionShape().setScale(new Vector3f(baseShapeScale, baseShapeScale, baseShapeScale));
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
            tower.addControl(new TowerControl(this, bulletAppState, assetManager, towerNode));
            towerNode.attachChild(tower);

            towerPhy = new RigidBodyControl(0f);
            tower.addControl(towerPhy);
            bulletAppState.getPhysicsSpace().add(towerPhy);
            float towerShapeScale = 1.73f;
            towerPhy.getCollisionShape().setScale(new Vector3f(towerShapeScale, towerShapeScale, towerShapeScale));

            towers.add(tower);
        }
    }

    public void createCreeps(int num) {

        Vector3f v;
        int creepX = 160;
        int creepZ = 200;
        creeps = new ArrayList<Spatial>();
        for (int i = 0; i < num; i++) {

            v = new Vector3f(FastMath.nextRandomFloat() * creepX - creepX / 2,//X
                    FastMath.nextRandomFloat() * 400.0f + 5,//Y
                    FastMath.nextRandomFloat() * creepZ - (creepZ + 50));//Z

            Node creep = (Node) assetManager.loadModel("Textures/Creeps/FlySnakeCar/FlySnakeCar.mesh.xml");

            control = creep.getControl(AnimControl.class);
            channel = control.createChannel();
            channel.setAnim(ANI_FLY);
            channel.setTime(FastMath.nextRandomFloat() * channel.getAnimMaxTime());
            channel.setLoopMode(LoopMode.Cycle);

            Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
            TextureKey flameLook = new TextureKey("Interface/Pics/flames.png", false);
            mat.setTexture("DiffuseMap", assetManager.loadTexture(flameLook));

            creep.setMaterial(mat);
            creep.scale(2.8f);
            creep.setName("Creep" + i);
            creep.setLocalTranslation(v);
            creep.setUserData("index", i);
            creep.setUserData("health", creepHealth);
            creep.setUserData("damage", 1);
            creep.setUserData("xpWorth", 5);
            creep.addControl(new CreepControl(this, bulletAppState, assetManager, creepNode));

            creepPhy = new BetterCharacterControl(2.5f, 0.1f, 20f);
            creep.addControl(creepPhy);
            bulletAppState.getPhysicsSpace().add(creepPhy);

            creeps.add(creep);
            creepNode.attachChild(creep);
        }
    }

    public void createBeam(Vector3f towerPos, Vector3f creepPos, int num, float tHeight) {
        Vector3f tp = new Vector3f(towerPos.x, towerPos.y + tHeight, towerPos.z);
        Vector3f cp = new Vector3f(creepPos.x, creepPos.y + 1.0f, creepPos.z);
        Line beam = new Line(tp, cp);
        beam.setLineWidth(3);
        Geometry beamGeom = new Geometry("Beam" + bNum, beam);

        Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        TextureKey laser = new TextureKey("Interface/Pics/laser.png", false);
        mat.setTexture("DiffuseMap", assetManager.loadTexture(laser));
        mat.setColor("Specular", ColorRGBA.White.mult(ColorRGBA.Red));
        mat.setFloat("Shininess", 100f);

        beamGeom.setMaterial(mat);
        beamGeom.setUserData("index", num);
        bNum++;
        beamNode.attachChild(beamGeom);
    }

    private void lightsAndCam() {
        //Light if charged
        ChargedLight = new PointLight();
        ChargedLight.setColor(ColorRGBA.Yellow);

        //Sun
        sun = new DirectionalLight();
        sun.setDirection((new Vector3f(-0.8f, -0.5f, -0.5f)));
        sun.setColor(ColorRGBA.White.mult(1.6f));
        AmbientLight al = new AmbientLight();
        al.setColor(ColorRGBA.White.mult(2.0f));
        rootNode.addLight(sun);
        rootNode.addLight(al);

        //Set cam location
        Vector3f c = new Vector3f(0.0f, 28.0f, 75.0f);
        cam.setLocation(c);
        cam.setRotation(new Quaternion(0.0f, 1.0f, 0.0f, 0));
    }

    private void update2DGui(float tpf) {
        //Set 2d GUI
        statsTitle.setText("Current Statistics");
        playerHealth.setText("     Health:         " + getHealth());
        playerCharges.setText("     Charges:     " + getBudget());
        playerCreepCount.setText("     Creeps Killed:   " + getCreepsKilled());
        playerMana.setText("     Mana:           " + getMana());
        chargeTimer += tpf;
        if (getChargeAdded()) {
            smallPlayerInfo.setText("     Charge added! Keep killing!");
            setChargeAdded(false);
        }
        if (chargeTimer > 5) {
            chargeTimer = 0;
            smallPlayerInfo.setText("     Kill more.. Now..");
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
        if (isNewInfo()) {
            infoTimer = 0;
            infoMessage.setText(getInfoMessage());
            setIsNewInfo(false);
        }
        if (infoTimer > 5) {
            infoMessage.setText("");
        }
        if (getCooldown() > 0) {
            cooldownText.setText("Cooldown: " + getCooldown());
        } else {
            cooldownText.setText("");
        }
        ///__________________________________________________________------------------------
//        int a = playerControl.getExperience();
//        int b = playerControl.getXpToNextLevel();

        playerExperience.setText("      XP: " + playerControl.getExperience() + " / " + playerControl.getXpToNextLevel());

        playerLevel.setText("      Level:     " + playerControl.getLevel());

    }

    private void initGui() {
        //Selection light
        //lamp.setColor(ColorRGBA.Cyan);
        //lamp.setPosition(new Vector3f(0, 3, 0));
        Node guiNode = app.getGuiNode();

        //Stat title
        BitmapFont guiFont = assetManager.loadFont("Interface/Fonts/Cracked.fnt");

        statsTitle = new BitmapText(guiFont);
        statsTitle.setSize(guiFont.getCharSet().getRenderedSize());
        statsTitle.move(1, // X
                screenHeight, // Y
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
                screenWidth / 2 - 120, // X
                screenHeight / 5, // Y
                0); // Z (depth layer)
        guiNode.attachChild(infoMessage);

//------------------------------------------------------------------------------
        guiFont = assetManager.loadFont("Interface/Fonts/Cracked28.fnt");

        //Small player info
        smallPlayerInfo = new BitmapText(guiFont);
        smallPlayerInfo.setSize(guiFont.getCharSet().getRenderedSize());
        smallPlayerInfo.move(1, // X
                screenHeight - smallPlayerInfo.getHeight() * 1 - 10, // Y
                0); // Z (depth layer)
        guiNode.attachChild(smallPlayerInfo);

        //Player health
        playerHealth = new BitmapText(guiFont);
        playerHealth.setSize(guiFont.getCharSet().getRenderedSize());
        playerHealth.move(1, // X
                screenHeight - playerHealth.getHeight() * 6 - 10, // Y
                0); // Z (depth layer)
        guiNode.attachChild(playerHealth);

        //Player Mana
        playerMana = new BitmapText(guiFont);
        playerMana.setSize(guiFont.getCharSet().getRenderedSize());
        playerMana.move(1, // X
                screenHeight - playerMana.getHeight() * 7 - 10, // Y
                0); // Z (depth layer)
        guiNode.attachChild(playerMana);

        //Player charges
        playerCharges = new BitmapText(guiFont);
        playerCharges.setSize(guiFont.getCharSet().getRenderedSize());
        playerCharges.move(1, // X
                screenHeight - playerHealth.getHeight() * 5 - 10, // Y
                0); // Z (depth layer)
        guiNode.attachChild(playerCharges);

        //Player creep count
        playerCreepCount = new BitmapText(guiFont);
        playerCreepCount.setSize(guiFont.getCharSet().getRenderedSize());
        playerCreepCount.move(1, // X
                screenHeight - playerCreepCount.getHeight() * 4 - 10, // Y
                0); // Z (depth layer)
        guiNode.attachChild(playerCreepCount);

        //Player level
        playerLevel = new BitmapText(guiFont);
        playerLevel.setSize(guiFont.getCharSet().getRenderedSize());
        playerLevel.move(1, // X
                screenHeight - playerLevel.getHeight() * 2 - 10, // Y
                0); // Z (depth layer)
        guiNode.attachChild(playerLevel);

        //Player experience
        playerExperience = new BitmapText(guiFont);
        playerExperience.setSize(guiFont.getCharSet().getRenderedSize());
        playerExperience.move(1, // X
                screenHeight - playerExperience.getHeight() * 3 - 10, // Y
                0); // Z (depth layer)
        guiNode.attachChild(playerExperience);

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
        cooldownText.move(screenWidth - 200, // X
                screenHeight - cooldownText.getHeight(), // Y
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

    private CollisionResults clickRayCollission() {
        CollisionResults results = new CollisionResults();
        Vector2f click2d = app.getInputManager().getCursorPosition();
        Vector3f click3d = cam.getWorldCoordinates(new Vector2f(click2d.getX(), click2d.getY()), 0f);
        Vector3f dir = cam.getWorldCoordinates(new Vector2f(click2d.getX(), click2d.getY()), 1f).subtractLocal(click3d);
        Ray ray = new Ray(click3d, dir);
        rootNode.collideWith(ray, results);
        return results;
    }

    @Override
    public void cleanup() {
        //setSmallPlayerInfoText("     Nice job defending your base!");
        //rootNode.detachChild(creepNode);
        rootNode.removeLight(sun);
        playerHealth.setText("");
        playerCharges.setText("");
        playerMana.setText("");
    }

    // Only accessors and mutators below
    public int getLevel() {
        return level;
    }

    public int getScore() {
        return score;
    }

    public int getBudget() {
        return budget;
    }

    public int getHealth() {
        return health;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public void setBudget(int budget) {
        this.budget = budget;
    }

    public ArrayList<Spatial> getCreeps() {
        return creeps;
    }

    public ArrayList<Spatial> getTowers() {
        return towers;
    }

    public void removeCreep(Spatial creep) {
        creeps.remove(creep);
    }

    public boolean isLastGameWon() {
        return lastGameWon;
    }

    public PointLight getChargedLight() {
        return ChargedLight;
    }

    public int getCreepsKilled() {
        return creepsKilled;
    }

    public int getNumOfCreeps() {
        return numOfCreeps;
    }

    public void incrementCreepsKilled(int xpWorth) {
        creepsKilled++;
        int temp = playerControl.getExperience() + xpWorth;
        System.out.println(temp);
        playerControl.setExperience(temp);
    }

    public void setChargeAdded(boolean bool) {
        chargeAdded = bool;
    }

    public boolean getChargeAdded() {
        return chargeAdded;
    }

    public void setFireball(boolean fireBool) {
        fireballOn = fireBool;
    }

    public void setFrostBolt(boolean frostBool) {
        frostBoltOn = frostBool;
    }

    public void setFrostNova(boolean novaBool) {
        frostNovaOn = novaBool;
    }

    public void setBigSpell(boolean bigSpellBool) {
        bigSpellOn = bigSpellBool;
    }

    public Node getExplosionNode() {
        return explosionNode;
    }

    public int getCreepHealth() {
        return creepHealth;
    }

    public float getMana() {
        return mana;
    }

    boolean isNewInfo() {
        return isNewInfo;
    }

    public String getInfoMessage() {
        return info;
    }

    public void setIsNewInfo(boolean bool) {
        isNewInfo = bool;
    }

    public float getCooldown() {
        return cooldown;
    }

    public void setScreenSize(int height, int width) {
        screenHeight = height;
        screenWidth = width;
    }

    public boolean getWaveCleared() {
        return wavecleared;
    }

    public void setWaveCleared(boolean b) {
        wavecleared = b;
    }

    public void maxMana() {
        mana = maxMana;
    }

    public void setSmallPlayerInfoText(String str) {
        smallPlayerInfo.setText(str);
    }

    public void fogOff(float tpf) {
        while (fogFilter.getFogDensity() > 0.01) {
            System.out.println(tpf + " - tpf");
            System.out.println(fogFilter.getFogDensity());
            fogFilter.setFogDensity(fogFilter.getFogDensity() - (0.1f * tpf));
        }
    }

    boolean getGameLost() {
        return gameLost;
    }

    void setGameLost(boolean b) {
        gameLost = b;
    }
}
