// Jiaxu Wang (24009377)  Jiaheng Liu (24009483)  Angze Song (24009333)   Xiao Wu (24009458)
public class Camera {
    double worldWidth;
    double worldHeight;
    double viewWidth;
    double viewHeight;
    double x;
    double y;

    public Camera(double worldWidth, double worldHeight, double viewWidth, double viewHeight) {
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
        this.viewWidth = viewWidth;
        this.viewHeight = viewHeight;
        this.x = 0;
        this.y = 0;
    }

    void follow(double targetX, double targetY, double dt) {
        double targetCamX = targetX - viewWidth * 0.5;
        double targetCamY = targetY - viewHeight * 0.5;

        double t = 1.0 - Math.exp(-GameConfig.CAMERA_LERP_SPEED * dt);
        x += (targetCamX - x) * t;
        y += (targetCamY - y) * t;

        x = Math.max(0, Math.min(worldWidth - viewWidth, x));
        y = Math.max(0, Math.min(worldHeight - viewHeight, y));
    }

    double screenX(double worldX) {
        return worldX - x;
    }

    double screenY(double worldY) {
        return worldY - y;
    }

    boolean isVisible(double worldX, double worldY, double margin) {
        return worldX >= x - margin && worldX <= x + viewWidth + margin
            && worldY >= y - margin && worldY <= y + viewHeight + margin;
    }
}
