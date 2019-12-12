package de.op.pob.simulation;

import java.util.HashSet;
import java.util.Set;

public class ToxicRainHitRateSimulation extends AbstractSimulation<Double> {

    private class Point {
        final double x;
        final double y;

        public Point(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public String toString() {
            return "point(" + x + "," + y + ")";
        }
    }

    private class Circle {
        final Point center;
        final double radius;

        public Circle(Point center, double radius) {
            this.center = center;
            this.radius = radius;
        }

        public boolean containsPoint(Point p) {
            return (Math.pow(p.x - center.x, 2) + Math.pow(p.y - center.y, 2)) < radius;
        }

        @Override
        public String toString() {
            return "circle(" + center + "," + radius + ")";
        }
    }

    // Base projectiles include the one base of the bow and the additional arrows of
    // the gem
    private static final int TOXIC_RAIN_GEM_BASE_PROJECTILES = 5;

    private final Circle targetArea;
    private final Circle hitArea;
    private final double toxicRainArrowExclusionRadius;
    private final double toxicRainHitableAreaRadius;
    private final int projectileCount;

    public ToxicRainHitRateSimulation(double targetHitboxRadius,
                                      double toxicRainBaseRadius,
                                      double toxicRainArrowExclusionRadius,
                                      double toxicRainPodBaseRadius,
                                      double increasedAreaOfEffect,
                                      int projectileCount,
                                      double toxicRainBaseRadiusIncPerProjectile) {
        int additionalProjectiles = projectileCount - TOXIC_RAIN_GEM_BASE_PROJECTILES;
        if (additionalProjectiles < 0) {
            throw new IllegalArgumentException("ProjectileCount needs to be more then TOXIC_RAIN_GEM_BASE_PROJECTILES("
                    + TOXIC_RAIN_GEM_BASE_PROJECTILES + ")");
        }

        this.toxicRainHitableAreaRadius = toxicRainBaseRadius
                + (additionalProjectiles * toxicRainBaseRadiusIncPerProjectile);
        Point simulationCenter = new Point(toxicRainHitableAreaRadius, toxicRainHitableAreaRadius);
        this.targetArea = new Circle(simulationCenter, toxicRainHitableAreaRadius);
        this.hitArea = new Circle(simulationCenter,
                                  targetHitboxRadius + (toxicRainPodBaseRadius * increasedAreaOfEffect));
        this.toxicRainArrowExclusionRadius = toxicRainArrowExclusionRadius;
        this.projectileCount = projectileCount;
    }

    @Override
    protected Double doSimulation() {
        Set<Circle> prevHitArrowsInclExclusionRadius = new HashSet<>();
        for (int i = 0; i < this.projectileCount; i++) {
            Point nextPossibleHit;
            do {
                nextPossibleHit = new Point(Math.random() * this.toxicRainHitableAreaRadius * 2,
                                            Math.random() * this.toxicRainHitableAreaRadius * 2);
            } while (doesSetOfCirclesContainPoint(prevHitArrowsInclExclusionRadius, nextPossibleHit)
                    || !this.targetArea.containsPoint(nextPossibleHit));

            // found a valid / not excluded point
            prevHitArrowsInclExclusionRadius.add(new Circle(nextPossibleHit, this.toxicRainArrowExclusionRadius));
        }
        return (double) prevHitArrowsInclExclusionRadius.stream()
                                                        .filter(c -> this.hitArea.containsPoint(c.center))
                                                        .count();
    }

    private boolean doesSetOfCirclesContainPoint(Set<Circle> prevHitArrowsInclExclusionRadius, Point nextPossibleHit) {
        return prevHitArrowsInclExclusionRadius.stream()
                                               .anyMatch(c -> c.containsPoint(nextPossibleHit));
    }

    @Override
    protected Double aggregateResults(Set<Double> results) {
        Double sumOfResults = results.stream()
                                     .reduce(0.0, Double::sum);
        return sumOfResults / results.size();
    }

    public final static double targetHitboxRadius = 1;                 
    public final static double toxicRainBaseRadius = 8;                
    public final static double toxicRainArrowExclusionRadius = 4;      
    public final static double toxicRainPodBaseRadius = 4;             
    public final static double increasedAreaOfEffect = 1;              
    public final static int projectileCount = 5;                       
    public final static double toxicRainBaseRadiusIncPerProjectile = 1;
    
    public static void main(String[] args) throws InterruptedException {
        ToxicRainHitRateSimulation sim = new ToxicRainHitRateSimulation(targetHitboxRadius,
                                                                        toxicRainBaseRadius,
                                                                        toxicRainArrowExclusionRadius,
                                                                        toxicRainPodBaseRadius,
                                                                        increasedAreaOfEffect,
                                                                        projectileCount,
                                                                        toxicRainBaseRadiusIncPerProjectile);
        System.out.println(sim.simulateNTimes(100000));
    }
}
