import java.util.List;
import java.util.Random;

public class Agent {
    public Vektor2D position;
    public Vektor2D velocity;
    public Vektor2D acceleration;
    public int id;
    private static final double MAX_SPEED = 0.0000008; // Lower value for slower movement
    private static final double MAX_FORCE = 0.05; // Lower value for slower movement
    private static final Random random = new Random();
    private static final PerlinNoise perlin = new PerlinNoise();
    private double timeOffset;

    public Agent(int id, Vektor2D position, Vektor2D velocity) {
        this.id = id;
        this.position = position;
        this.velocity = velocity;
        this.acceleration = new Vektor2D();
        this.timeOffset = random.nextDouble() * 1000; // Random offset for each agent
    }

    public void applyForce(Vektor2D force) {
        acceleration.add(force);
    }

    public void update(double t) {
        velocity.add(acceleration);
        velocity.truncate(MAX_SPEED); // Limit velocity to MAX_SPEED
        position.add(velocity);
        acceleration.mult(0);

        // Apply Perlin noise to simulate smooth random vertical movement
        double noise = perlin.noise(position.x, position.y, t + timeOffset);
        double verticalOffset = (noise) * 0.00009; // Adjust the scale of vertical movement
        double horizontalOffset = (noise) * 0.00009; // Adjust the scale of horizontal movement
        
        // Apply sine wave to create oscillation
        double sineWave = Math.sin((t + timeOffset) * 0.8) * 0.0001; // Adjust the amplitude and frequency of the sine wave
        double sineWaveHorizontal = Math.sin((t + timeOffset) * 1.2) * 0.0001; // Adjust the amplitude and frequency of the sine wave

        position.y += verticalOffset + sineWave;
        position.x += horizontalOffset + sineWaveHorizontal;
        
        applyBoundarySteering();

        // Wrap around screen boundaries

    }
    
    private void applyBoundarySteering() {
        double boundaryDistanceY = 0.0; // Distance from boundary to start steering
        double boundaryDistanceX = 0.0;
        double turnStrength = 4.05; // Strength of the turn force

        Vektor2D steer = new Vektor2D();

        if (position.x > 3 - boundaryDistanceX) {
            steer.x = -turnStrength;
        } else if (position.x < -3 + boundaryDistanceX) {
            steer.x = turnStrength;
        }

        if (position.y > 3 - boundaryDistanceY) {
            steer.y = -turnStrength;
        } else if (position.y < -3 + boundaryDistanceY) {
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
        Vektor2D sep = separation(agents, 0.2).mult(0.4); // Increase separation weight
        Vektor2D ali = alignment(agents, 1.0).mult(1.0);
        Vektor2D coh = cohesion(agents, 50.5).mult(1.9); // Reduce cohesion weight

        applyForce(sep);
        applyForce(ali);
        applyForce(coh);

        // Add a small random force to introduce randomness
        applyForce(new Vektor2D(random.nextDouble() * 0.01 - 0.005, random.nextDouble() * 0.01 - 0.005));
    }
    
    public void moveToTarget(Vektor2D target) {
        Vektor2D seekForce = seek(target);
        applyForce(seekForce);
    }
}