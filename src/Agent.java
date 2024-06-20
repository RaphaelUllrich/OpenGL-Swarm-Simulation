import java.util.List;
import java.util.Random;

public class Agent {
    public Vektor2D position;
    public Vektor2D velocity;
    public Vektor2D acceleration;
    public int id;
    private static final double MAX_SPEED = 0.005;
    private static final double MAX_FORCE = 0.0004;
    private static final Random random = new Random();
    private static final PerlinNoise perlin = new PerlinNoise();
    private double timeOffset;
    private Vektor2D targetPosition; // Zielposition
    private boolean reachedTarget; // Flag, um zu überprüfen, ob das Ziel erreicht wurde
    
    private static final double MIN_X = -1.0; // Minimum X-Grenze
    private static final double MAX_X = 1.0;  // Maximum X-Grenze
    private static final double MIN_Y = -1.0; // Minimum Y-Grenze
    private static final double MAX_Y = 1.0;  // Maximum Y-Grenze

    public Agent(int id, Vektor2D position, Vektor2D velocity) {
        this.id = id;
        this.position = position;
        this.velocity = velocity;
        this.acceleration = new Vektor2D();
        this.timeOffset = random.nextDouble() * 1000; // random offset
        this.targetPosition = null; // Initialisiere Zielposition als null
    }

    public void applyForce(Vektor2D force) {
        acceleration.add(force);
    }

    public void update(double t, boolean shock) {
        if (targetPosition != null) {
            Vektor2D seekForce = (seek(targetPosition).mult(70));
            applyForce(seekForce);
        }

        velocity.add(acceleration);
        velocity.truncate(MAX_SPEED);
        position.add(velocity);
        acceleration.mult(0);

        // smooth random vertical movement
        double noise = perlin.noise(position.x, position.y, t + timeOffset);
        double verticalOffset = (noise) * 0.000009;
        double horizontalOffset = (noise) * 0.000009;
        
        if (shock) {
        	double sineWave = Math.sin((t + timeOffset) * 10.8) * 0.01;
            double sineWaveHorizontal = Math.sin((t + timeOffset) * 10.2) * 0.01;
            
            position.y += verticalOffset + sineWave;
            position.x += horizontalOffset + sineWaveHorizontal;
        }
        else {
        	double sineWave = Math.sin((t + timeOffset) * 0.8) * 0.001;
            double sineWaveHorizontal = Math.sin((t + timeOffset) * 2.2) * 0.001;
            
            position.y += verticalOffset + sineWave;
            position.x += horizontalOffset + sineWaveHorizontal;
        }        

        applyBoundarySteering();
        slowDownNearTarget();
        
        keepWithinBounds();

        // reset target wenn erreicht
        if (targetPosition != null && position.distanceTo(targetPosition) < 0.01) {
            targetPosition = null;
            reachedTarget = true;
        }
    }
    
    private void keepWithinBounds() {
        if (position.x < MIN_X) {
            position.x = MIN_X;
            velocity.x = 0;
        } else if (position.x > MAX_X) {
            position.x = MAX_X;
            velocity.x = 0;
        }

        if (position.y < MIN_Y) {
            position.y = MIN_Y;
            velocity.y = 0;
        } else if (position.y > MAX_Y) {
            position.y = MAX_Y;
            velocity.y = 0;
        }
    }

    private void applyBoundarySteering() {
        double boundaryDistanceY = 1.0; //Distanz ab wann gestartet wird entgegenzulenken
        double boundaryDistanceX = 1.0;
        double turnStrength = 1.05;

        Vektor2D steer = new Vektor2D();

        if (position.x > 2 - boundaryDistanceX) {
            steer.x = -turnStrength;
        } else if (position.x < -2 + boundaryDistanceX) {
            steer.x = turnStrength;
        }

        if (position.y > 3 - boundaryDistanceY) {
            steer.y = -turnStrength;
        } else if (position.y < -2 + boundaryDistanceY) {
            steer.y = turnStrength;
        }

        applyForce(steer);
    }

    public Vektor2D separation(List<Agent> agents, double desiredSeparation) {
        Vektor2D steer = new Vektor2D();
        int count = 0;
        for (Agent other : agents) {
            double d = new Vektor2D(position, other.position).length();
            if (d > 0 && d < desiredSeparation) {
                Vektor2D diff = new Vektor2D(position, other.position);
                diff.normalize();
                diff.div(d);
                steer.add(diff);
                count++;
            }
        }
        if (count > 0) {
            steer.div(count);
        }
        if (steer.length() > 0) {
            steer.normalize();
            steer.mult(MAX_SPEED);
            steer.sub(velocity);
            steer.truncate(MAX_FORCE);
        }
        return steer;
    }

    public Vektor2D alignment(List<Agent> agents, double neighborDist) {
        Vektor2D sum = new Vektor2D();
        int count = 0;
        for (Agent other : agents) {
            double d = new Vektor2D(position, other.position).length();
            if (d > 0 && d < neighborDist) {
                sum.add(other.velocity);
                count++;
            }
        }
        if (count > 0) {
            sum.div(count);
            sum.normalize();
            Vektor2D steer = new Vektor2D(sum);
            steer.sub(velocity);
            steer.truncate(MAX_FORCE);
            return steer;
        }
        return new Vektor2D();
    }

    public Vektor2D cohesion(List<Agent> agents, double neighborDist) {
        Vektor2D sum = new Vektor2D();
        int count = 0;
        for (Agent other : agents) {
            double d = new Vektor2D(position, other.position).length();
            if (d > 0 && d < neighborDist) {
                sum.add(other.position);
                count++;
            }
        }
        if (count > 0) {
            sum.div(count);
            return seek(sum);
        }
        return new Vektor2D();
    }

    public Vektor2D seek(Vektor2D target) {
        Vektor2D desired = new Vektor2D(target);
        desired.sub(position);
        desired.normalize();
        desired.mult(MAX_SPEED);
        Vektor2D steer = new Vektor2D(desired);
        steer.sub(velocity);
        steer.truncate(MAX_FORCE);
        return steer;
    }

    public void flock(List<Agent> agents) {

    		Vektor2D sep = separation(agents, 0.2).mult(0.4);
    		Vektor2D ali = alignment(agents, 1.0).mult(1.0);
    		Vektor2D coh = cohesion(agents, 1.5).mult(1.5);
    		
    		applyForce(sep);
    	    applyForce(ali);
    	    applyForce(coh);

        // kleine random force
        applyForce(new Vektor2D(random.nextDouble() * 0.01 - 0.005, random.nextDouble() * 0.01 - 0.005));
    }
    
    public void moveToTarget(Vektor2D target) {
        this.targetPosition = target;
        this.reachedTarget = false;
    }
    
    public void resetTarget() {
        this.targetPosition = null;
        this.reachedTarget = true;
    }
    
    public void shock() {
    	this.targetPosition = null;
        this.reachedTarget = true;
    }
    
    private void slowDownNearTarget() {
        if (targetPosition != null) {
            double distance = position.distanceTo(targetPosition);
            if (distance < 0.1) { // In der Nähe des Ziels
                double speedFactor = distance / 0.01; // Verlangsamen in der Nähe des Ziels
                velocity.mult(speedFactor);
            }
        }
    }
}