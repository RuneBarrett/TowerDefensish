/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package towerDefensish;

import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import java.util.ArrayList;

/**
 *
 * @author Rune Barrett
 */
public class TowerControl extends AbstractControl {

    private GamePlayAppState GPAState;
    private ArrayList<Spatial> creeps;
    private ArrayList<CreepControl> reachable;
    private ArrayList<Charges> charges;
    private Charges charge;
    private float beamTimer = 0;
    boolean alreadyShot = false;
    private float damageTimer = 0;
    private BulletAppState BAState;
    private AssetManager assetManager;
    private ParticleEmitter blueFireEmitter;
    private float blueFireSize = 2f;
    private Node towerNode;

    public TowerControl(GamePlayAppState GPAState, BulletAppState BAState, AssetManager assetManager, Node towerNode) {
        this.GPAState = GPAState;
        this.reachable = new ArrayList<CreepControl>();
        this.charges = new ArrayList<Charges>();
        this.BAState = BAState;
        this.assetManager = assetManager;
        this.towerNode = towerNode;
        initFireEffect();
        //charges.add(new Charges(1, 100));     //for testing   
    }

    @Override
    protected void controlUpdate(float tpf) {
        creeps = GPAState.getCreeps();
        beamTimer += tpf;
        shootAtCreepsInRange(tpf);
        reachable.clear();
        damageTimer += tpf;
        if (getHealth() <= 0) {
            BAState.getPhysicsSpace().remove(spatial.getControl(RigidBodyControl.class));
            spatial.removeFromParent();
            GPAState.getTowers().remove(spatial);
        }
        Vector3f tPos = spatial.getLocalTranslation();
        Vector3f firePos = new Vector3f(tPos.x, tPos.y+13.6f, tPos.z);
        blueFireEmitter.setLocalTranslation(firePos);
    }

    private void shootAtCreepsInRange(float tpf) {
        if (charges.size() >= 0) {
            for (Spatial creep : creeps) {
                if ((spatial.getWorldTranslation().distance(creep.getWorldTranslation()) < 35)) {//spatial.getWorldTranslation().z-creep.getWorldTranslation().z  < 10
                    reachable.add(creep.getControl(CreepControl.class));
                }
            }
            if (beamTimer > 0.4f) {
                beamTimer = 0;
                if (!charges.isEmpty()) {
                    try {
                        spatial.removeLight(GPAState.getChargedLight());
                    } catch (NullPointerException npe) {
                        System.out.println("NullPointerException in tower control - remove light");
                    }
                    spatial.addLight(GPAState.getChargedLight());

                    charge = charges.get(0);
                    for (CreepControl cc : reachable) {

                        if (cc.getHealth() > 0 && charge.getBullets() > 0 && !alreadyShot) {
                            System.out.println("Shot at creep: " + cc.getIndex() + " hp: " + cc.getHealth());
                            cc.setHealth(cc.getHealth() - charge.getDamage());
                            //cc.getSpatial().lookAt(spatial.getLocalTranslation(), Vector3f.UNIT_Y);
                            charge.setBullets(charge.getBullets() - 1);
                            GPAState.createBeam(spatial.getWorldTranslation(), cc.getSpatial().getWorldTranslation(), (Integer) spatial.getUserData("index"), (Float) spatial.getUserData("height"));
                            alreadyShot = true;
                        }
                        if (charge.getBullets() <= 0 && !charges.isEmpty()) {
                            charges.remove(0);
                        }
                    }
                    alreadyShot = false;
                } else {
                    spatial.removeLight(GPAState.getChargedLight());
                }
            }
        }
    }

    private void initFireEffect() {
        //BAState.getPhysicsSpace().addCollisionListener(this);
        blueFireEmitter = new ParticleEmitter("FireballTail", ParticleMesh.Type.Triangle, 30);
        Material fireMat = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
        fireMat.setTexture("Texture", assetManager.loadTexture("Effects/flame.png"));
        blueFireEmitter.setMaterial(fireMat);

        blueFireEmitter.setImagesX(2);
        blueFireEmitter.setImagesY(2);
        blueFireEmitter.setRandomAngle(true);
        blueFireEmitter.setSelectRandomImage(true);
        blueFireEmitter.setStartColor(new ColorRGBA(0f, .5f, 1f, 1f));
        blueFireEmitter.setEndColor(new ColorRGBA(0.0f, 0.0f, 1f, 0f));
        blueFireEmitter.setGravity(0, 0, 0);
        blueFireEmitter.setStartSize(0.5f * blueFireSize);
        blueFireEmitter.setEndSize(0.1f * blueFireSize);
        blueFireEmitter.setLowLife(0.5f);
        blueFireEmitter.setHighLife(2f);
        blueFireEmitter.getParticleInfluencer().setVelocityVariation(0.2f);
        blueFireEmitter.getParticleInfluencer().setInitialVelocity(new Vector3f(0, 10f, 0));

        towerNode.attachChild(blueFireEmitter);
    }

    //not used anymore
    public void reduceHealth(int damage) {
        if (damageTimer > 5F) {
            int oldHp = (Integer) spatial.getUserData("health");
            int newHp = oldHp - damage;
            spatial.setUserData("health", newHp);
            damageTimer = 0;
        }
    }

    public void setHealth(int newHealth) {
        spatial.setUserData("health", newHealth);
    }

    public void addCharge() {
        charges.add(new Charges(1, 8));
    }

    public int getIndex() {
        return (Integer) spatial.getUserData("index");
    }

    public int getCharges() {
//        return (Integer) spatial.getUserData("chargesNum");
        return charges.size();
    }

    public int getBullets() {
//        return (Integer) spatial.getUserData("chargesNum");
        int bullets;
        if (charge == null) {
            bullets = 0;
        } else {
            bullets = charge.getBullets();
        }
        return bullets;
    }

    public int getHeight() {
        return (Integer) spatial.getUserData("height");
    }

    public int getHealth() {
        return (Integer) spatial.getUserData("health");
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }
}
