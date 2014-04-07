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
import spellControls.NormalSpellControl;

/**
 *
 * @author Rune Barrett
 */
public class StartScreenAppState extends AbstractAppState {

    private SimpleApplication app;
    private Camera cam;
    private AssetManager assetManager;
    private PointLight ChargedLight;
    private Node playerNode;
    private Node towerNode;
    private Node rootNode;
    private RigidBodyControl floorPhy;
    private ArrayList<Spatial> towers;
    private BulletAppState bulletAppState;
    private AppStateManager stateManager;

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

        lightsAndCam();
        createNodes();
        createFloor();
        createBase();
        createTowers();
    }

    @Override
    public void update(float tpf) {
    }

    private void createNodes() {
        playerNode = new Node("playerNode");
        towerNode = new Node("towerNode");
        rootNode.attachChild(playerNode);
        rootNode.attachChild(towerNode);
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
        base.setName("player");
        playerNode.attachChild(base);
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
            towerNode.attachChild(tower);
            towers.add(tower);
        }
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
        Vector3f c = new Vector3f(0.0f, 28.0f, 75.0f);
        cam.setLocation(c);
        cam.setRotation(new Quaternion(0.0f, 1.0f, 0.0f, 0));
    }

    @Override
    public void cleanup() {
    }

    public ArrayList<Spatial> getTowers() {
        return towers;
    }
}
