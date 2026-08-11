public class Main {
    public static void main(String[] args) {
        /*Lambertian l = new Lambertian();

        ObjectList world = new ObjectList();

        world.add(new Sphere(0, -1000, 0, 1000, new Vec3(0.5, 0.5, 0.5), l));

        for (int a = -11; a < 11; a++) {
            for (int b = -11; b < 11; b++) {
                double chooseMat = Math.random();
                Vec3 centre = new Vec3(a + 0.9 * Math.random(), 0.2, b + 0.9 * Math.random());

                if (VectorOperations.subtract(centre, new Vec3(4, 0.2, 0)).getMag() > 0.9) {
                    Material sphereMaterial;
                    Vec3 sphereColor;

                    if (chooseMat < 0.8) {
                        sphereColor = VectorOperations.multiplyComponents(Vec3.random(), Vec3.random());
                        world.add(new Sphere(centre.x(), centre.y(), centre.z(), 0.2, sphereColor, l));

                    } else if (chooseMat < 0.95) {
                        Vec3 albedo = Vec3.random(0.5, 1.0);
                        double fuzz = Math.random() / 2.0;
                        sphereMaterial = new Metal(fuzz);
                        world.add(new Sphere(centre.x(), centre.y(), centre.z(), 0.2, albedo, sphereMaterial));

                    } else {
                        sphereMaterial = new Dielectric(1.5);
                        sphereColor = new Vec3(1.0, 1.0, 1.0);
                        //Basic white colour
                        world.add(new Sphere(centre.x(), centre.y(), centre.z(), 0.2, sphereColor, sphereMaterial));
                    }
                }
            }
        }

        Material material1 = new Dielectric(1.5);
        world.add(new Sphere(0, 1, 0, 1.0, new Vec3(1.0, 1.0, 1.0), material1));

        Material material2 = new Lambertian();
        world.add(new Sphere(-4, 1, 0, 1.0, new Vec3(0.4, 0.2, 0.1), material2));

        Material material3 = new Metal(0.0);
        world.add(new Sphere(4, 1, 0, 1.0, new Vec3(0.7, 0.6, 0.5), material3));
        double aspectRatio = (double) 16 / 9;
        int imageWidth = 1200;

        Camera cam = new Camera(aspectRatio, imageWidth);

        cam.render(world);
         */

    }
}
