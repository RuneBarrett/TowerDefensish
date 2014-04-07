/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package towerDefensish;

/**
 *
 * @author Rune Barrett
 */
public class Charges {
    private int damage;
    private int bullets;

    public Charges(int damage, int bullets) {
        this.damage = damage;
        this.bullets = bullets;
    }

    public int getDamage() {
        return damage;
    }

    public int getBullets() {
        return bullets;
    }

    public void setBullets(int bullets) {
        this.bullets = bullets;
    }
    
    
}
