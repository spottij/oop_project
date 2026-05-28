package gui;

import java.util.ArrayList;
import java.util.List;

public class RobotModel {
    private volatile double m_robotPositionX = 100;
    private volatile double m_robotPositionY = 100;
    private volatile double m_robotDirection = 0;

    private volatile int m_targetPositionX = 150;
    private volatile int m_targetPositionY = 100;

    private static final double maxVelocity = 0.1;
    private static final double maxAngularVelocity = 0.001;

    private final List<RobotListener> listeners = new ArrayList<>();

    public void addListener(RobotListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }

    private void notifyListeners() {
        synchronized (listeners) {
            for (RobotListener l : listeners) {
                l.onModelUpdated();
            }
        }
    }

    public double getX() { return m_robotPositionX; }
    public double getY() { return m_robotPositionY; }
    public double getDirection() { return m_robotDirection; }
    public int getTargetX() { return m_targetPositionX; }
    public int getTargetY() { return m_targetPositionY; }

    public void setTargetPosition(int x, int y) {
        this.m_targetPositionX = x;
        this.m_targetPositionY = y;
        notifyListeners();
    }

    public void updateModel() {
        double distance = distance(m_targetPositionX, m_targetPositionY, m_robotPositionX, m_robotPositionY);
        if (distance < 0.5) {
            return;
        }

        double velocity = maxVelocity;
        double angleToTarget = angleTo(m_robotPositionX, m_robotPositionY, m_targetPositionX, m_targetPositionY);
        double angleDifference = angleToTarget - m_robotDirection;
        while (angleDifference < -Math.PI) angleDifference += 2 * Math.PI;
        while (angleDifference > Math.PI) angleDifference -= 2 * Math.PI;

        double angularVelocity = 0;
        if (Math.abs(angleDifference) > 0.00001) {
            if (angleDifference > 0) {
                angularVelocity = maxAngularVelocity;
            } else {
                angularVelocity = -maxAngularVelocity;
            }
        }
        if (Math.abs(angleDifference) < 0.05) {
            angularVelocity = angleDifference / 10.0;
        }


        moveRobot(velocity, angularVelocity, 10);
        notifyListeners();
    }

    private void moveRobot(double velocity, double angularVelocity, double duration) {
        velocity = applyLimits(velocity, 0, maxVelocity);
        angularVelocity = applyLimits(angularVelocity, -maxAngularVelocity, maxAngularVelocity);

        double newX = m_robotPositionX + velocity / angularVelocity * (Math.sin(m_robotDirection  + angularVelocity * duration) - Math.sin(m_robotDirection));
        if (!Double.isFinite(newX)) {
            newX = m_robotPositionX + velocity * duration * Math.cos(m_robotDirection);
        }

        double newY = m_robotPositionY - velocity / angularVelocity * (Math.cos(m_robotDirection  + angularVelocity * duration) - Math.cos(m_robotDirection));
        if (!Double.isFinite(newY)) {
            newY = m_robotPositionY + velocity * duration * Math.sin(m_robotDirection);
        }

        m_robotPositionX = newX;
        m_robotPositionY = newY;

        double newDirection = m_robotDirection + angularVelocity * duration;
        while (newDirection < 0) newDirection += 2 * Math.PI;
        while (newDirection >= 2 * Math.PI) newDirection -= 2 * Math.PI;
        m_robotDirection = newDirection;
    }

    private static double distance(double x1, double y1, double x2, double y2) {
        double diffX = x1 - x2;
        double diffY = y1 - y2;
        return Math.sqrt(diffX * diffX + diffY * diffY);
    }

    private static double angleTo(double fromX, double fromY, double toX, double toY) {
        double diffX = toX - fromX;
        double diffY = toY - fromY;
        return Math.atan2(diffY, diffX);
    }

    private static double applyLimits(double value, double min, double max) {
        if (value < min) return min;
        if (value > max) return max;
        return value;
    }
}