void main() {

    Lambertian l = new Lambertian();

    ObjectList world = new ObjectList();
    world.add(new Sphere(0, -100.5, -1, 100, new Vec3(0.8,0.8,0.0), l));
    world.add(new Sphere(0, 0, -1.2, 0.5, new Vec3(0.1, 0.2, 0.5), l));
    world.add(new Sphere(-1.0, 0.0, -1.0, 0.5, new Vec3(1.0, 1.0, 1.0), new Dielectric(1.5)));
    world.add(new Sphere(-1.0, 0.0, -1.0, 0.4, new Vec3(1.0, 1.0, 1.0), new Dielectric(1.0/1.5)));
    world.add(new Sphere(1.0, 0.0, -1.0, 0.5, new Vec3(0.8, 0.6, 0.2), new Metal(1.0)));

    double aspectRatio = (double)16/9;
    int imageWidth = 400;

    //int imageWidth = 256;
    //int imageHeight = 256;
    Camera cam = new Camera(aspectRatio, imageWidth);

    cam.render(world);
}
