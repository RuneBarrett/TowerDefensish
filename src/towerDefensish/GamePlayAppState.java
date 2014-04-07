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
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.light.PointLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Line;
import com.jme3.scene.shape.Sphere;
import java.util.ArrayList;

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
    private RigidBodyControl floorPhy;
    private ArrayList<Spatial> creeps;
    private ArrayList<Spatial> towers;
    private static final String ANI_FLY = "my_animation";
    private int level = 0;
    private int score = 0;
    private int health = 20;
    private int budget = 5;
    private float mana = 100;
    private int creepsKilled = 0;
    private float budgetTimer = 0;
    private float beamTimer = 0;
    private int numOfCreeps = 15;
    private int creepHealth = 12;
    private boolean lastGameWon = false;
    private int bNum = 0;
    private BulletAppState bulletAppState;
    private RigidBodyControl towerPhy;
    private BetterCharacterControl creepPhy;
    private boolean chargeAdded = false;
    private Mesh spellMesh;
    private RigidBodyControl ballPhy;
    private boolean fireballOn;
    private boolean frostBoltOn;
    private boolean frostNovaOn;
    private boolean bigSpellOn;
    private FireBallControl fireControl;
    private String infoMessage = "";
    private RigidBodyControl basePhy;
    private float baseShapeScale;
    private boolean isNewInfo;
    private float cooldown;

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

        //stoneMat = assetManager.loadMaterial("Textures/Turret/turret.png");
        spellMesh = new Sphere(32, 32, 2.50f, true, false);

        lightsAndCam();
        createNodes();
        createFloor();
        createBase();
        createCreeps(numOfCreeps);
        createTowers();
        //bulletAppState.setDebugEnabled(true);
    }

    @Override
    public void update(float tpf) {

        if (health <= 0) {
            System.out.println("The player lost.");
            lastGameWon = false;
            playerControl = playerNode.getChild("player").getControl(PlayerControl.class);
            playerControl.setDead(true);

        } else if (creeps.isEmpty()) {
            System.out.println("The player won.");
            lastGameWon = true;
            stateManager.detach(this);
        }
        budgetTimer += tpf;
        if (budgetTimer >= 15.0f) {
            budgetTimer = 0.0f;
            budget++;
            System.out.println("Charge added. Budget: " + budget);
        }
        beamTimer += tpf;
        if (beamTimer >= 0.3f) {
            beamNode.detachAllChildren();
            beamTimer = 0;
        }
        if (mana < 100) {
            mana += tpf * 2;
        }
        cooldown+=tpf;
    }

    public void shoot() {
        if(cooldown>5){
        System.out.println(mana);
        Geometry ballGeo = new Geometry("Spell", spellMesh);
        Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        ballGeo.setMaterial(mat);
        ballGeo.setLocalTranslation(cam.getLocation());

        TextureKey wood = new TextureKey("Interface/Pics/wood.png", false);
        mat.setTexture("DiffuseMap", assetManager.loadTexture(wood));

        if (fireballOn) {
            if (mana > 20 && cooldown > 5f) {
                cooldown = 0;
                ballGeo.setName("Fireball");
                TextureKey fire = new TextureKey("Interface/Pics/flames.png", false);
                mat.setTexture("DiffuseMap", assetManager.loadTexture(fire));
                ballGeo.addControl(new FireBallControl(this, assetManager, ballNode, ballGeo, bulletAppState));
                mana = mana - 20;
            } else {
                infoMessage = "Mana too low to cast a Fireball!";
                isNewInfo = true;
                System.out.println("Mana too low to cast a Fireball!");
            }
        }

        if (frostBoltOn) {
            if (mana > 15 && cooldown > 5f) {
                cooldown = 0;
                ballGeo.setName("Frostbolt");
                TextureKey ice = new TextureKey("Interface/Pics/chrislinder_ice_6.png", false);
                mat.setTexture("DiffuseMap", assetManager.loadTexture(ice));
                ballGeo.addControl(new FrostboltControl(this, assetManager, ballNode, ballGeo, bulletAppState));
                mana = mana - 15;
            } else {
                infoMessage = "Mana too low to cast a Frostbolt!";
                isNewInfo = true;
                System.out.println("Mana too low to cast a Frostbolt!");
            }
        }
        if (frostNovaOn) {
            TextureKey ice = new TextureKey("Interface/Pics/ice-block.png", false);
            mat.setTexture("DiffuseMap", assetManager.loadTexture(ice));
            //ballGeo.addControl(new FrostBoltControl(assetManager, ballNode, ballGeo));
        }
        mat.setColor("Specular", ColorRGBA.White.mult(ColorRGBA.Red));
        mat.setFloat("Shininess", 200f);
        ballNode.attachChild(ballGeo);


        ballPhy = new RigidBodyControl(70.5f);
        ballGeo.addControl(ballPhy);
        bulletAppState.getPhysicsSpace().add(ballPhy);
        ballPhy.setCcdSweptSphereRadius(.1f);
        ballPhy.setCcdMotionThreshold(0.001f);
        ballPhy.setAngularVelocity(new Vector3f(-FastMath.nextRandomFloat() * 25, FastMath.nextRandomFloat() * 5 - 5, FastMath.nextRandomFloat() * 5 - 5));


        ballPhy.setLinearVelocity(cam.getDirection().mult(65));
        }else{
            infoMessage = "Spells are on cooldown.";
                isNewInfo = true;
        }
    }

    private void createNodes() {
        playerNode = new Node("playerNode");
        towerNode = new Node("towerNode");
        creepNode = new Node("creepNode");
        beamNode = new Node("beamNode");
        ballNode = new Node("ballNode");
        explosionNode = new Node("explosionNode");
        rootNode.attachChild(playerNode);
        rootNode.attachChild(towerNode);
        rootNode.attachChild(creepNode);
        rootNode.attachChild(beamNode);
        rootNode.attachChild(ballNode);
        rootNode.attachChild(explosionNode);
    }

    private void createFloor() {
        Spatial floor = assetManager.loadModel("Scenes/TowerDefenseTerrain.j3o");
        floor.setLocalTranslation(0, 0, -120f);
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
            tower.addControl(new TowerControl(this, bulletAppState));
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
        int creepX = 120;
        int creepZ = 150;
        creeps = new ArrayList<Spatial>();
        for (int i = 0; i < num; i++) {

            v = new Vector3f(FastMath.nextRandomFloat() * creepX - creepX / 2,//X
                    FastMath.nextRandomFloat() * 15.0f + 5,//Y
                    FastMath.nextRandomFloat() * creepZ - (creepZ + 40));//Z

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
            creep.scale(2.0f);
            creep.setName("Creep" + i);
            creep.setLocalTranslation(v);
            creep.setUserData("index", i);
            creep.setUserData("health", creepHealth);
            creep.setUserData("damage", 1);
            creep.addControl(new CreepControl(this, bulletAppState));

            creepPhy = new BetterCharacterControl(2.5f, 0.1f, 2f);
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
        System.out.println("beam from tower" + num);

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
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection((new Vector3f(-0.8f, -0.5f, -0.5f)));
        sun.setColor(ColorRGBA.White.mult(1.6f));
        AmbientLight al = new AmbientLight();
        al.setColor(ColorRGBA.White.mult(2.0f));
        rootNode.addLight(sun);
        rootNode.addLight(al);

        //Set cam location
        Vector3f c = new Vector3f(0.0f, 30.0f, 55.0f);
        cam.setLocation(c);
        cam.setRotation(new Quaternion(0.0f, 1.0f, -0.1f, 0));
    }

    @Override
    public void cleanup() {
        app.stop();
    }

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

    public ArrayList<Spatial> getCreeps() {
        return creeps;
    }

    public ArrayList<Spatial> getTowers() {
        return towers;
    }

    public void removeCreep(Spatial creep) {
        creeps.remove(creep);
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

    public void incrementCreepsKilled() {
        creepsKilled++;
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
        return infoMessage;
    }

    public void setIsNewInfo(boolean bool) {
        isNewInfo = bool;
    }
}
