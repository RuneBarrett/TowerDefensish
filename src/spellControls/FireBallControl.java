/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package spellControls;

import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.PhysicsTickListener;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import java.util.ArrayList;
import towerDefensish.CreepControl;
import towerDefensish.GamePlayAppState;

/**
 *
 * @author Rune Barrett
 */
public class FireBallControl extends AbstractControl implements PhysicsCollisionListener, PhysicsTickListener {

    AssetManager assetManager;
    Node ballNode;
    private final Geometry ballGeom;
    private final BulletAppState BAState;
    private boolean explode = false;
    private ParticleEmitter fireEmitter;
    private final GamePlayAppState GPAState;
    private boolean alreadyExploded = false;
    private ArrayList<Spatial> influencedCreeps;
    private Vector3f explosionPos;
    private int damage = 4;
    private boolean alreadyDamaged = false;
    private float damageTimer = 5;

    public FireBallControl(GamePlayAppState GPAState, AssetManager assetManager, Node ballNode, Geometry ballGeom, BulletAppState BAState) {
        this.assetManager = assetManager;
        this.ballNode = ballNode;
        this.ballGeom = ballGeom;
        this.BAState = BAState;
        this.GPAState = GPAState;
        influencedCreeps = new ArrayList<Spatial>();

        initFireEffect();
    }

    @Override
    protected void controlUpdate(float tpf) {
        fireEmitter.setLocalTranslation(ballGeom.getLocalTranslation());
        if (explode && !alreadyExploded) {
            spatial.addControl(new FireballExplosionControl(GPAState, BAState, assetManager, spatial.getLocalTranslation(), ballNode, ballGeom));
            spatial.getControl(RigidBodyControl.class).setLinearVelocity(Vector3f.ZERO);
            alreadyExploded = true;
        }
        explode = false;
        damageTimer+=tpf;
        System.out.println(damageTimer);
        if (!influencedCreeps.isEmpty()) {
            System.out.println(influencedCreeps.size());
            for (Spatial influencedCreep : influencedCreeps) {
                //influencedCreep.setJumpForce(new Vector3f(0, 10, 0));
                if (damageTimer>5f) {
                    influencedCreep.getControl(CreepControl.class).setHealth(influencedCreep.getControl(CreepControl.class).getHealth() - getDamage());
                    //alreadyDamaged = true;
                    damageTimer = 0f;
                }
                if (influencedCreep.getWorldTranslation().y - 5 < explosionPos.y) {
                    System.out.println("back");
                    influencedCreep.getControl(BetterCharacterControl.class).setWalkDirection(explosionPos.negate().negate());
                } else if (influencedCreep.getWorldTranslation().y > explosionPos.y) {
                    System.out.println("forward");
                    influencedCreep.getControl(BetterCharacterControl.class).setWalkDirection(explosionPos.negate());

                }
                influencedCreep.getControl(BetterCharacterControl.class).jump();
            }

        }
    }

    private void initFireEffect() {
        BAState.getPhysicsSpace().addCollisionListener(this);
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
        fireEmitter.setStartSize(4.6f);
        fireEmitter.setEndSize(0.25f);
        fireEmitter.setLowLife(0.5f);
        fireEmitter.setHighLife(2f);
        fireEmitter.getParticleInfluencer().setVelocityVariation(0.2f);
        fireEmitter.getParticleInfluencer().setInitialVelocity(new Vector3f(0, 10f, 0));
        ballNode.attachChild(fireEmitter);
    }

    public void collision(PhysicsCollisionEvent event) {
        influencedCreeps.clear();
        if (event.getNodeA().getName().equals("Fireball") || event.getNodeB().getName().equals("Fireball")) {
            explode = true;
            for (Spatial creep : GPAState.getCreeps()) {
                if (event.getPositionWorldOnA().distance(creep.getLocalTranslation()) < 40f) {
                    influencedCreeps.add(creep);
                    explosionPos = event.getPositionWorldOnA();
                }
                //event.getLocalPointA().distance(creep.getLocalTranslation());


            }

        }
    }

    public int getDamage() {
        return damage;
    }

    public void prePhysicsTick(PhysicsSpace space, float tpf) {
    }

    public void physicsTick(PhysicsSpace space, float tpf) {
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }
}
