/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package towerDefensish;

import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.PhysicsTickListener;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh;
import com.jme3.light.PointLight;
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
public class CreepControl extends AbstractControl implements PhysicsTickListener, PhysicsCollisionListener {

    private GamePlayAppState GPAState;
    private BulletAppState BAState;
    private ArrayList<Spatial> towers;
    private ArrayList<TowerControl> reachable;
    private Vector3f basePos = new Vector3f(0, 2.0f, 0);
    private Vector3f creepPos;
    private Vector3f direction;
    private boolean moveTowardsTower;
    private Vector3f towerPos;
    float speed = 2.8f;
    private float attackTimer = 0;
    private boolean frozen = false;
    private boolean baseInRange = false;
    private ArrayList<Spatial> reaching;
    private int maxHealth;
    private ParticleEmitter fireEmitter;
    private final AssetManager assetManager;
    private Node creepNode;
    private float fireSize = 1;
    //private int temp;
    private float frozenTime = 3;
    private float unfreezeTimer = 0;
    private PointLight FrozenLight;

    public CreepControl(GamePlayAppState GPAState, BulletAppState BAState, AssetManager assetManager, Node creepNode) {
        this.GPAState = GPAState;
        this.BAState = BAState;
        this.assetManager = assetManager;
        this.creepNode = creepNode;
        reachable = new ArrayList<TowerControl>();
        reaching = new ArrayList<Spatial>();
        BAState.getPhysicsSpace().addCollisionListener(this);
        maxHealth = this.GPAState.getCreepHealth();
        initFireEffect();
        FrozenLight = new PointLight();
        FrozenLight.setColor(new ColorRGBA(0f, 0.3f, 1f, 1f).mult(7));
        FrozenLight.setPosition(new Vector3f(0, 6, 0));
    }

    @Override
    protected void controlUpdate(float tpf) {
        towers = GPAState.getTowers();
        creepPos = spatial.getLocalTranslation();
        fireEmitter.setLocalTranslation(creepPos);
        moveTowardsTower = false;
        fireSize = maxHealth - getHealth();

        //Check if tower in range, and move towards it
        if (getHealth() > 0) {
            //Only move if not frozen
            if (!frozen) {
                //only attack towers if not at full health
                if (getHealth() < maxHealth) {
                    for (Spatial tower : towers) {
                        towerPos = tower.getWorldTranslation();
                        if (spatial.getWorldTranslation().distance(towerPos) < 27f) {
                            moveTowards(creepPos, towerPos, tpf);
                            moveTowardsTower = true;
                            break;
                        }
                    }
                }
                //if no towers are in range, or if at full health, move towards base
                if (!moveTowardsTower && spatial.getWorldTranslation().y < 40f) {
                    spatial.lookAt(basePos, Vector3f.UNIT_Y);
                    moveTowards(creepPos, basePos, tpf);
                }
            } else {

                //System.out.println("freeze");
                    spatial.getControl(BetterCharacterControl.class).setWalkDirection(Vector3f.ZERO);
                if (unfreezeTimer == 0) {
                    spatial.addLight(FrozenLight);
                    //moveTowards(creepPos, basePos, tpf);
                }
                
                unfreezeTimer += tpf;
                if (unfreezeTimer >= frozenTime) {
                    spatial.removeLight(FrozenLight);
                    frozen = false;
                    System.out.println("unfreeze");
                    unfreezeTimer = 0;
                }
            }
            //if health are not bigger than 0, die
        } else {
            GPAState.incrementCreepsKilled(getXpWorth());
            GPAState.setBudget(GPAState.getBudget() + 1);
            GPAState.removeCreep(spatial);
            GPAState.setChargeAdded(true);
            //fireSize = 4;
            BAState.getPhysicsSpace().remove(spatial.getControl(BetterCharacterControl.class));
            spatial.removeFromParent();
            creepNode.detachChild(fireEmitter);
        }

        //NOT USED ANYMORE
        if (spatial.getWorldTranslation().z >= -1) {
            GPAState.setHealth(GPAState.getHealth() - 1);
            System.out.println("Player health: " + GPAState.getHealth());
            //spatial.removeFromParent();
            //fireEmitter.killAllParticles();
            fireSize = 0;
        }

        attackTimer += tpf;
        AttackTowersInRange(tpf);
        reachable.clear();

        fireEmitter.setStartSize(0.4f * fireSize);
        fireEmitter.setEndSize(0.1f * fireSize);

    }

    private void AttackTowersInRange(float tpf) {

        for (Spatial tower : towers) {
            if ((spatial.getWorldTranslation().distance(tower.getWorldTranslation()) < 8)) {//spatial.getWorldTranslation().z-creep.getWorldTranslation().z  < 10
                reachable.add(tower.getControl(TowerControl.class));
            }
        }
        if (attackTimer > 2f) {
            attackTimer = 0;
            for (TowerControl tc : reachable) {
                if (tc.getHealth() > 0) {
                    tc.setHealth(tc.getHealth() - getDamage());
                }
            }

            if (baseInRange && reaching.contains(spatial)) {
                baseInRange = false;
                GPAState.setHealth(GPAState.getHealth() - getDamage());
            }
        }
    }

    public void moveTowards(Vector3f from, Vector3f to, float tpf) {
        direction = new Vector3f(to.x - from.x, to.y - from.y, to.z - from.z);
        //spatial.getControl(RigidBodyControl.class).setLinearVelocity(direction.normalizeLocal().mult(tpf * moveSpeed));
        spatial.getControl(BetterCharacterControl.class).setWalkDirection(direction.normalizeLocal().mult(speed));
        spatial.getControl(BetterCharacterControl.class).setViewDirection(direction.normalizeLocal());
    }

    public void collision(PhysicsCollisionEvent event) {
        //no reason to loop when colliding with scene
        if (!event.getNodeB().getName().equals("New Scene")) {
            if (event.getNodeA().getName().equals("player") || event.getNodeB().getName().equals("player")) {
                for (Spatial spatial1 : GPAState.getCreeps()) {
                    if (event.getNodeA().getName().equals(spatial1.getName()) || event.getNodeB().getName().equals(spatial1.getName())) {
                        reaching.add(spatial1);

                    }
                    baseInRange = true;
                }

            }
        }
    }

    private void initFireEffect() {
        //BAState.getPhysicsSpace().addCollisionListener(this);
        fireEmitter = new ParticleEmitter("FireballTail", ParticleMesh.Type.Triangle, 30);
        Material fireMat = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
        fireMat.setTexture("Texture", assetManager.loadTexture("Effects/flame.png"));
        fireEmitter.setMaterial(fireMat);

        fireEmitter.setImagesX(2);
        fireEmitter.setImagesY(2);
        fireEmitter.setRandomAngle(true);
        fireEmitter.setSelectRandomImage(true);
        fireEmitter.setStartColor(new ColorRGBA(1f, 1f, .5f, 1f));
        fireEmitter.setEndColor(new ColorRGBA(1f, 0f, 0f, 0f));
        fireEmitter.setGravity(0, 0, 0);
        fireEmitter.setStartSize(0.4f * fireSize);
        fireEmitter.setEndSize(0.1f * fireSize);
        fireEmitter.setLowLife(0.5f);
        fireEmitter.setHighLife(2f);
        fireEmitter.getParticleInfluencer().setVelocityVariation(0.2f);
        fireEmitter.getParticleInfluencer().setInitialVelocity(new Vector3f(0, 10f, 0));

        creepNode.attachChild(fireEmitter);
    }

    public void prePhysicsTick(PhysicsSpace space, float tpf) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void physicsTick(PhysicsSpace space, float tpf) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public int getIndex() {
        return (Integer) spatial.getUserData("index");
    }

    public int getHealth() {
        return (Integer) spatial.getUserData("health");
    }

    public int getDamage() {
        return (Integer) spatial.getUserData("damage");
    }

    public int getXpWorth() {
        return (Integer) spatial.getUserData("xpWorth");
    }

    public void setHealth(int newHealth) {
        spatial.setUserData("health", newHealth);
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }

    public void freeze(float time) {
        frozen = true;
        frozenTime = time;
    }
}
