/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package towerDefensish;

import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
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

    public TowerControl(GamePlayAppState GPAState, BulletAppState BAState) {
        this.GPAState = GPAState;
        this.reachable = new ArrayList<CreepControl>();
        this.charges = new ArrayList<Charges>();
        this.BAState = BAState;
        //charges.add(new Charges(1, 100));     //for testing   
    }

    @Override
    protected void controlUpdate(float tpf) {
        creeps = GPAState.getCreeps();
        beamTimer += tpf;
        shootAtCreepsInRange(tpf);
        reachable.clear();
        damageTimer += tpf;
        if(getHealth()<=0){
        BAState.getPhysicsSpace().remove(spatial.getControl(RigidBodyControl.class));
        spatial.removeFromParent();
        GPAState.getTowers().remove(spatial);
        }
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

    public void reduceHealth(int damage) {
        if (damageTimer > 5F) {
            int oldHp = (Integer) spatial.getUserData("health");
            int newHp = oldHp - damage;
            spatial.setUserData("health", newHp);
            damageTimer = 0;
        }
    }
    
    public void setHealth(int newHealth){
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
