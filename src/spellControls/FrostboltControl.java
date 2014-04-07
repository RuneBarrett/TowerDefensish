/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package spellControls;

import com.jme3.asset.AssetManager;
import com.jme3.asset.TextureKey;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.PhysicsTickListener;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;
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
import com.jme3.scene.control.AbstractControl;
import towerDefensish.GamePlayAppState;

/**
 *
 * @author Rune Barrett
 */
public class FrostboltControl extends AbstractControl implements PhysicsCollisionListener, PhysicsTickListener {

    AssetManager assetManager;
    Node ballNode;
    ParticleEmitter frostEmitter;
    private final Geometry ballGeom;
    private boolean explode = false;
    private final GamePlayAppState GPAState;
    private final BulletAppState BAState;
    private boolean alreadyExploded = false;

    public FrostboltControl(GamePlayAppState GPAState, AssetManager assetManager, Node ballNode, Geometry ballGeom, BulletAppState BAState) {
        this.assetManager = assetManager;
        this.ballNode = ballNode;
        this.ballGeom = ballGeom;
        this.GPAState = GPAState;
        this.BAState = BAState;

        initFrostEffect();
    }

    @Override
    protected void controlUpdate(float tpf) {
        frostEmitter.setLocalTranslation(ballGeom.getLocalTranslation());
        if (explode && !alreadyExploded) {
            spatial.addControl(new FrostboltExplosionControl(GPAState, BAState, assetManager, spatial.getLocalTranslation(), ballNode, ballGeom));
            spatial.getControl(RigidBodyControl.class).setLinearVelocity(Vector3f.ZERO);
            alreadyExploded = true;
        }
        explode = false;
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }

    private void initFrostEffect() {
        BAState.getPhysicsSpace().addCollisionListener(this);
        frostEmitter = new ParticleEmitter(
                "Frost Emitter", ParticleMesh.Type.Triangle, 30);
        Material frostMat = new Material(assetManager,
                "Common/MatDefs/Misc/Particle.j3md");
        frostMat.setTexture("Texture",
                assetManager.loadTexture("Effects/flame.png"));
        frostEmitter.setMaterial(frostMat);
        frostEmitter.setImagesX(2);
        frostEmitter.setImagesY(2);
        frostEmitter.setRandomAngle(true);
        frostEmitter.setSelectRandomImage(true);

        frostEmitter.setStartColor(new ColorRGBA(0f, 0.3f, 1f, 1f));
        frostEmitter.setEndColor(new ColorRGBA(0f, 1f, 1f, 0f));
        frostEmitter.setGravity(0, 0, 0);
        frostEmitter.setStartSize(4.6f);
        frostEmitter.setEndSize(0.25f);
        frostEmitter.setLowLife(0.5f);
        frostEmitter.setHighLife(2f);
        frostEmitter.getParticleInfluencer().
                setVelocityVariation(0.2f);
        frostEmitter.getParticleInfluencer().
                setInitialVelocity(new Vector3f(0, 10f, 0));
        ballNode.attachChild(frostEmitter);
    }

    public void collision(PhysicsCollisionEvent event) {
        if (event.getNodeA().getName().equals("Frostbolt") || event.getNodeB().getName().equals("Frostbolt")) {
            explode = true;
        }
    }

    public void prePhysicsTick(PhysicsSpace space, float tpf) {
    }

    public void physicsTick(PhysicsSpace space, float tpf) {
    }
}
