public abstract class Material {
    abstract boolean scatter(Ray r, RayHit hitty, Vec3 colour, Ray scattered);
}
