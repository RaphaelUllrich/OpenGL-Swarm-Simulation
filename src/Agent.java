import java.util.List;
import java.util.Random;

public class Agent {
    public Vektor2D position;
    public Vektor2D velocity;
    public Vektor2D acceleration;
    public int id;
    private static final double MAX_SPEED = 0.0002; // Lower value for slower movement
    private static final double MAX_FORCE = 0.5; // Lower value for slower movement
    private static final Random random = new Random();

    public Agent(int id, Vektor2D position, Vektor2D velocity) {
        this.id = id;
        this.position = position;
        this.velocity = velocity;
        this.acceleration = new Vektor2D();
    }

    public void applyForce(Vektor2D force) {
        acceleration.add(force);
    }

    public void update() {
        velocity.add(acceleration);
        velocity.truncate(MAX_SPEED); // Limit velocity to MAX_SPEED
        position.add(velocity);
        acceleration.mult(0);

        // Wrap around screen boundaries
        if (position.x > 3) position.x = -3;
        if (position.x < -3) position.x = 3;
        if (position.y > 3) position.y = -3;
        if (position.y < -3) position.y = 3;
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
        Vektor2D sep = separation(agents, 25).mult(1.5); // Increase separation weight
        Vektor2D ali = alignment(agents, 50).mult(1.0);
        Vektor2D coh = cohesion(agents, 50).mult(0.5); // Reduce cohesion weight

        applyForce(sep);
        applyForce(ali);
        applyForce(coh);

        // Add a small random force to introduce randomness
        applyForce(new Vektor2D(random.nextDouble() * 0.01 - 0.005, random.nextDouble() * 0.01 - 0.005));
    }
}
