public class CPUMain {
    public static void main(String[] args){
        Lambertian l = new Lambertian();

        ObjectList world = new ObjectList();


        Metal m = new Metal(0);
        //world.add(new Sphere(0, -1000, 0, 1000, new Vec3(0.5f, 0.5f, 0.5f), l));
        world.add(new Plane(0, 1, 0, 0f, new Vec3(0.5f, 0.5f, 0.5f), m));

        for (int a = -11; a < 11; a++) {
            for (int b = -11; b < 11; b++) {
                double chooseMat = Math.random();
                Vec3 centre = new Vec3(a + 0.9f * (float)Math.random(), 0.2f, b + 0.9f * (float)Math.random());

                if (VectorOperations.subtract(centre, new Vec3(4, 0.2f, 0)).getMag() > 0.9) {
                    Material sphereMaterial;
                    Vec3 colour;

                    if (chooseMat < 0.8) {
                        colour = VectorOperations.multiplyComponents(Vec3.random(), Vec3.random());
                        world.add(new Sphere(centre.x(), centre.y(), centre.z(), 0.2, colour, l));

                    }
                    else if (chooseMat < 0.95) {
                        Vec3 albedo = Vec3.random(0.5, 1.0);
                        float fuzz = (float) (Math.random() / 2.0);
                        sphereMaterial = new Metal(fuzz);
                        world.add(new Sphere(centre.x(), centre.y(), centre.z(), 0.2, albedo, sphereMaterial));

                    }
                    else {
                        sphereMaterial = new Dielectric(1.5f);
                        colour = new Vec3(1.0f, 1.0f, 1.0f);
                        //Basic white colour
                        world.add(new Sphere(centre.x(), centre.y(), centre.z(), 0.2, colour, sphereMaterial));
                    }
                }
                else{
                    b--;
                }
            }
        }

        Material material1 = new Dielectric(1.5f);
        //world.add(new Sphere(0, 1, 0, 1.0, new Vec3(1.0f, 1.0f, 1.0f), material1));
        world.add(new Cylinder(0, 0.5f, 0,0, 1, 0, 1.0f,1, true, new Vec3(1.0f, 1.0f, 1.0f), material1));

        Material material2 = new Lambertian();
        //world.add(new Sphere(-4, 1, 0, 1.0, new Vec3(0.4f, 0.2f, 0.1f), material2));
        world.add(new Cylinder(-4, 0.5f, 0,0, 1, 0, 1.0f,1, false, new Vec3(0.4f, 0.2f, 0.1f), material2));

        Material material3 = new Metal(0.0f);
        //.add(new Sphere(4, 1, 0, 1.0, new Vec3(0.7f, 0.6f, 0.5f), material3));
        world.add(new Cylinder(4, 0.5f, 0,1, 0, 0, 1.0f,4, false, new Vec3(0.9f, 0.2f, 0.1f), material3));
        float aspectRatio = (float) 16 / 9;
        int imageWidth = 1200;

        Camera cam = new Camera(aspectRatio, imageWidth);


        long startTime = System.currentTimeMillis();
        cam.render(world);
        long duration = System.currentTimeMillis() - startTime;
        System.out.println("Completed in " + (duration) + " milliseconds");
    }
}
