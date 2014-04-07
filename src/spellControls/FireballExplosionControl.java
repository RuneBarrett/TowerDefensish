package spellControls;

import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh;
import com.jme3.effect.shapes.EmitterSphereShape;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.control.AbstractControl;
import towerDefensish.GamePlayAppState;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Rune Barrett
 */
public class FireballExplosionControl extends AbstractControl {

    AssetManager assetManager;
    GamePlayAppState GPAState;
    BulletAppState BAState;
    Node explosionNode;
    private float time = 0;
    private int state = 0;
    private Node explosionEffect = new Node("mega explosion FX");
    private Node ballNode;
    private Geometry ballGeo;
    private final Vector3f pos;
    private ParticleEmitter sparksEmitter, burstEmitter,
            shockwaveEmitter, debrisEmitter,
            fireEmitter, smokeEmitter, embersEmitter;
    private boolean alreadyExploded = false;

    public FireballExplosionControl(GamePlayAppState GPAState, BulletAppState BAState, AssetManager assetManager, Vector3f pos, Node ballNode, Geometry ballGeo) {
        this.assetManager = assetManager;
        this.GPAState = GPAState;
        this.pos = pos;
        this.explosionNode = this.GPAState.getExplosionNode();
        this.ballNode = ballNode;
        this.ballGeo = ballGeo;
        this.BAState = BAState;

        initFire();
        initBurst();
        initEmbers();
        initSparks();
        initSmoke();
        initDebris();
        initShockwave();

    }

    @Override
    protected void controlUpdate(float tpf) {
        if (!alreadyExploded) {
            explosionTimer(tpf);
        }
    }

    private void initFire() {
        fireEmitter = new ParticleEmitter("Emitter", ParticleMesh.Type.Triangle, 100);
        Material fireMat = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
        fireMat.setTexture("Texture", assetManager.loadTexture("Effects/flame.png"));
        fireEmitter.setMaterial(fireMat);
        fireEmitter.setImagesX(2);
        fireEmitter.setImagesY(2);
        fireEmitter.setRandomAngle(true);
        fireEmitter.setSelectRandomImage(true);
        fireEmitter.setShape(new EmitterSphereShape(Vector3f.ZERO, 2f));
        explosionNode.attachChild(fireEmitter);

        fireEmitter.setStartColor(new ColorRGBA(1f, 1f, .5f, 1f));
        fireEmitter.setEndColor(new ColorRGBA(1f, 0f, 0f, 0f));
        fireEmitter.setGravity(0, -.5f, 0);
        fireEmitter.setStartSize(8f);
        fireEmitter.setEndSize(0.02f);
        fireEmitter.setLowLife(.5f);
        fireEmitter.setHighLife(2f);
        fireEmitter.getParticleInfluencer().setVelocityVariation(0.3f);
        fireEmitter.getParticleInfluencer().setInitialVelocity(new Vector3f(0, 3f, 0));
        fireEmitter.setParticlesPerSec(0);
        fireEmitter.setLocalTranslation(pos);
    }

    private void initBurst() {
        burstEmitter = new ParticleEmitter("Flash", ParticleMesh.Type.Triangle, 5);
        Material burstMat = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
        burstMat.setTexture("Texture", assetManager.loadTexture("Effects/flash.png"));
        burstEmitter.setMaterial(burstMat);
        burstEmitter.setImagesX(2);
        burstEmitter.setImagesY(2);
        burstEmitter.setSelectRandomImage(true);
        burstEmitter.setRandomAngle(true);
        explosionNode.attachChild(burstEmitter);

        burstEmitter.setStartColor(new ColorRGBA(1f, 0.8f, 0.36f, 1f));
        burstEmitter.setEndColor(new ColorRGBA(1f, 0.8f, 0.36f, .25f));
        burstEmitter.setStartSize(2f);
        burstEmitter.setEndSize(50.0f);
        burstEmitter.setGravity(0, 0, 0);
        burstEmitter.setLowLife(.75f);
        burstEmitter.setHighLife(.75f);
        burstEmitter.getParticleInfluencer().setInitialVelocity(new Vector3f(0, 2f, 0));
        burstEmitter.getParticleInfluencer().setVelocityVariation(1);
        burstEmitter.setShape(new EmitterSphereShape(Vector3f.ZERO, .5f));
        burstEmitter.setParticlesPerSec(0);
        burstEmitter.setLocalTranslation(pos);

    }

    private void initEmbers() {
        embersEmitter = new ParticleEmitter("embers", ParticleMesh.Type.Triangle, 50);
        Material embersMat = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
        embersMat.setTexture("Texture", assetManager.loadTexture("Effects/embers.png"));
        embersEmitter.setMaterial(embersMat);
        embersEmitter.setImagesX(1);
        embersEmitter.setImagesY(1);
        explosionNode.attachChild(embersEmitter);

        embersEmitter.setStartColor(new ColorRGBA(1f, 0.29f, 0.34f, 1.0f));
        embersEmitter.setEndColor(new ColorRGBA(0, 0, 0, 0.5f));
        embersEmitter.setStartSize(12f);
        embersEmitter.setEndSize(18f);
        embersEmitter.setGravity(0, -.5f, 0);
        embersEmitter.setLowLife(1.8f);
        embersEmitter.setHighLife(5f);
        embersEmitter.getParticleInfluencer().setInitialVelocity(new Vector3f(0, 3, 0));
        embersEmitter.getParticleInfluencer().setVelocityVariation(.5f);
        embersEmitter.setShape(new EmitterSphereShape(Vector3f.ZERO, 2f));
        embersEmitter.setParticlesPerSec(0);
        embersEmitter.setLocalTranslation(pos);

    }

    private void initSparks() {
        sparksEmitter = new ParticleEmitter("Spark", ParticleMesh.Type.Triangle, 20);
        Material sparkMat = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
        sparkMat.setTexture("Texture", assetManager.loadTexture("Effects/spark.png"));
        sparksEmitter.setMaterial(sparkMat);
        sparksEmitter.setImagesX(1);
        sparksEmitter.setImagesY(1);
        explosionNode.attachChild(sparksEmitter);

        sparksEmitter.setStartColor(new ColorRGBA(1f, 0.8f, 0.36f, 1.0f)); // orange
        sparksEmitter.setEndColor(new ColorRGBA(1f, 0.8f, 0.36f, .5f));
        sparksEmitter.getParticleInfluencer().setInitialVelocity(new Vector3f(0, 25, 0));
        sparksEmitter.getParticleInfluencer().setVelocityVariation(1);
        sparksEmitter.setFacingVelocity(true);
        sparksEmitter.setGravity(0, 15, 0);
        sparksEmitter.setStartSize(5f);
        sparksEmitter.setEndSize(5f);
        sparksEmitter.setLowLife(.9f);
        sparksEmitter.setHighLife(1.1f);
        sparksEmitter.setParticlesPerSec(0);
        sparksEmitter.setLocalTranslation(pos);

    }

    private void initSmoke() {
        smokeEmitter = new ParticleEmitter("Smoke emitter", ParticleMesh.Type.Triangle, 20);
        Material smokeMat = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
        smokeMat.setTexture("Texture", assetManager.loadTexture("Effects/smoketrail.png"));
        smokeEmitter.setMaterial(smokeMat);
        smokeEmitter.setImagesX(1);
        smokeEmitter.setImagesY(3);
        smokeEmitter.setSelectRandomImage(true);
        explosionNode.attachChild(smokeEmitter);

        smokeEmitter.setStartColor(new ColorRGBA(0.5f, 0.5f, 0.5f, 1f));
        smokeEmitter.setEndColor(new ColorRGBA(.1f, 0.1f, 0.1f, .5f));
        smokeEmitter.setLowLife(4f);
        smokeEmitter.setHighLife(4f);
        smokeEmitter.setGravity(0, 2, 0);
        smokeEmitter.setFacingVelocity(true);
        smokeEmitter.getParticleInfluencer().setInitialVelocity(new Vector3f(0, 6f, 0));
        smokeEmitter.getParticleInfluencer().setVelocityVariation(1);
        smokeEmitter.setStartSize(5f);
        smokeEmitter.setEndSize(30f);
        smokeEmitter.setParticlesPerSec(0);
        smokeEmitter.setLocalTranslation(pos);
    }

    private void initDebris() {
        debrisEmitter = new ParticleEmitter("Debris", ParticleMesh.Type.Triangle, 10);
        Material debrisMat = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
        debrisMat.setTexture("Texture", assetManager.loadTexture("Effects/debris.png"));
        debrisEmitter.setMaterial(debrisMat);
        debrisEmitter.setImagesX(3);
        debrisEmitter.setImagesY(3);
        debrisEmitter.setSelectRandomImage(true);
        debrisEmitter.setRandomAngle(true);
        explosionNode.attachChild(debrisEmitter);

        debrisEmitter.setRotateSpeed(FastMath.TWO_PI * 2);
        debrisEmitter.setStartColor(new ColorRGBA(0.4f, 0.4f, 0.4f, 1.0f));
        debrisEmitter.setEndColor(new ColorRGBA(0.4f, 0.4f, 0.4f, 1.0f));
        debrisEmitter.setStartSize(.3f);
        debrisEmitter.setEndSize(2f);
        debrisEmitter.setGravity(0, 10f, 0);
        debrisEmitter.setLowLife(2.7f);
        debrisEmitter.setHighLife(3.1f);
        debrisEmitter.getParticleInfluencer().setInitialVelocity(new Vector3f(0, 45, 0));
        debrisEmitter.getParticleInfluencer().setVelocityVariation(.60f);
        debrisEmitter.setParticlesPerSec(0);
        debrisEmitter.setLocalTranslation(pos);

    }

    private void initShockwave() {
        shockwaveEmitter = new ParticleEmitter("Shockwave", ParticleMesh.Type.Triangle, 2);
        shockwaveEmitter.setLocalTranslation(0, 15, 0);
        Material shockwaveMat = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
        shockwaveMat.setTexture("Texture", assetManager.loadTexture("Effects/shockwave.png"));
        shockwaveEmitter.setImagesX(1);
        shockwaveEmitter.setImagesY(1);
        shockwaveEmitter.setMaterial(shockwaveMat);
        explosionEffect.attachChild(shockwaveEmitter);

        shockwaveEmitter.setFaceNormal(Vector3f.UNIT_Y);
        shockwaveEmitter.setStartColor(new ColorRGBA(.68f, 0.77f, 0.61f, 1f));
        shockwaveEmitter.setEndColor(new ColorRGBA(.68f, 0.77f, 0.61f, 0f));
        shockwaveEmitter.setStartSize(5f);
        shockwaveEmitter.setEndSize(20f);
        shockwaveEmitter.setGravity(0, 0, 0);
        shockwaveEmitter.setLowLife(1f);
        shockwaveEmitter.setHighLife(1f);
        shockwaveEmitter.getParticleInfluencer().setInitialVelocity(new Vector3f(0, 0, 0));
        shockwaveEmitter.getParticleInfluencer().setVelocityVariation(0f);
        shockwaveEmitter.setParticlesPerSec(0);
        shockwaveEmitter.setLocalTranslation(pos);
    }
    
        private void explosionTimer(float tpf) {
        // this is a timer that triggers a series of effects in the right order
        time += tpf / 1f;
        if (time > .2f && state == 0) {
            sparksEmitter.emitAllParticles();
            state++;
        }
        if (time > .5f && state == 1) {
            burstEmitter.emitAllParticles();
            debrisEmitter.emitAllParticles();
            state++;
        }
        if (time > 1.0f && state == 2) {
            fireEmitter.emitAllParticles();
            embersEmitter.emitAllParticles();
            smokeEmitter.emitAllParticles();
            shockwaveEmitter.emitAllParticles();
            ballNode.detachChild(ballGeo);
            BAState.getPhysicsSpace().remove(ballGeo.getControl(RigidBodyControl.class));
            ballNode.detachChild(ballNode.getChild("FireballTail"));
            state++;
        }
//        if (time > 3.5f && state == 3) {
//            // rewind the effect
//
//            burstEmitter.killAllParticles();
//            sparksEmitter.killAllParticles();
//            debrisEmitter.killAllParticles();
//            shockwaveEmitter.killAllParticles();
//            state++;
//        }
//        if (time > 4.5f && state == 4) {
//            // rewind the effect
//            smokeEmitter.killAllParticles();
//            embersEmitter.killAllParticles();
//            fireEmitter.killAllParticles();
//            state++;
//        }
//        if (time > 5f && state == 5) {
//            alreadyExploded = true;            
//        }
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }
}
