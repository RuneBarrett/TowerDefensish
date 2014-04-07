/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package towerDefensish;

import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.PhysicsTickListener;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import java.util.ArrayList;
import towerDefensish.GamePlayAppState;

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

    public CreepControl(GamePlayAppState GPAState, BulletAppState BAState) {
        this.GPAState = GPAState;
        this.BAState = BAState;
        reachable = new ArrayList<TowerControl>();
        reaching = new ArrayList<Spatial>();
        BAState.getPhysicsSpace().addCollisionListener(this);
        maxHealth = this.GPAState.getCreepHealth();
    }

    @Override
    protected void controlUpdate(float tpf) {
        towers = GPAState.getTowers();
        creepPos = spatial.getLocalTranslation();

        //Check if tower in range, and move towards it
        moveTowardsTower = false;
        if (getHealth() > 0) {
            //Only move if not frozen
            if (!frozen) {
                //only attackt towers if not at full health
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
                if (!moveTowardsTower) {
                    spatial.lookAt(basePos, Vector3f.UNIT_Y);
                    moveTowards(creepPos, basePos, tpf);
                }
            } else {
                //freeze
            }
            //if health are not bigger than 0, die
        } else {
            GPAState.incrementCreepsKilled();
            GPAState.setBudget(GPAState.getBudget() + 1);
            GPAState.removeCreep(spatial);
            GPAState.setChargeAdded(true);
            BAState.getPhysicsSpace().remove(spatial.getControl(BetterCharacterControl.class));
            spatial.removeFromParent();
        }
        //replace this
        if (spatial.getWorldTranslation().z >= -1) {
            GPAState.setHealth(GPAState.getHealth() - 1);
            System.out.println("Player health: " + GPAState.getHealth());
            spatial.removeFromParent();
        }

        attackTimer += tpf;
        AttackTowersInRange(tpf);
        reachable.clear();




    }

    public void moveTowards(Vector3f from, Vector3f to, float tpf) {
        direction = new Vector3f(to.x - from.x, to.y - from.y, to.z - from.z);
        //spatial.getControl(RigidBodyControl.class).setLinearVelocity(direction.normalizeLocal().mult(tpf * moveSpeed));
        spatial.getControl(BetterCharacterControl.class).setWalkDirection(direction.normalizeLocal().mult(speed));
        spatial.getControl(BetterCharacterControl.class).setViewDirection(direction.normalizeLocal());
    }

    public void prePhysicsTick(PhysicsSpace space, float tpf) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void physicsTick(PhysicsSpace space, float tpf) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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

    public int getIndex() {
        return (Integer) spatial.getUserData("index");
    }

    public int getHealth() {
        return (Integer) spatial.getUserData("health");
    }

    public int getDamage() {
        return (Integer) spatial.getUserData("damage");
    }

    public void setHealth(int newHealth) {
        spatial.setUserData("health", newHealth);
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
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
}
