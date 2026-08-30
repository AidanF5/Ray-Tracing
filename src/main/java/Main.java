import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL43;
import org.lwjgl.system.MemoryUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.reflect.WildcardType;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.File;


public class Main {
    private static final int WIDTH = 1920;
    private static final int HEIGHT = 1080;
    private static final int SAMPLES_PER_PIXEL = 256;

    private static final Vec3 camPos = new Vec3(0.0f, 0.0f, 1.5f);
    private static final Vec3 camFor = VectorOperations.normalise(new Vec3(0.0f, 0.0f, -1.0f));
    private static final Vec3 camUp = new Vec3(0.0f, 1.0f, 0.0f);
    private static final Vec3 camRight = VectorOperations.normalise(VectorOperations.cross(camFor, camUp));
    private static final float fov = 45.0f;

    public static void main(String[] args) {
        //Initalise and create window
        if(!GLFW.glfwInit()){
            throw new IllegalStateException("Failed to initalise GLFW");
        }
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 4);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);

        long window = GLFW.glfwCreateWindow(WIDTH, HEIGHT, "GPU Ray Tracer", MemoryUtil.NULL, MemoryUtil.NULL);
        if(window == MemoryUtil.NULL) throw new RuntimeException("Failed to create the rendering window");

        GLFW.glfwMakeContextCurrent(window);
        GL.createCapabilities();

        //High precision RGBA32F Output Texture
        int outputTexture = GL43.glGenTextures();
        GL43.glBindTexture(GL43.GL_TEXTURE_2D, outputTexture);
        GL43.glTexStorage2D(GL43.GL_TEXTURE_2D, 1, GL43.GL_RGBA32F, WIDTH, HEIGHT);

        //Get the scene data
        int numObjects = 7;
        ByteBuffer buffer = MemoryUtil.memAlloc(numObjects * 80);  //int and floats are 4 bytes

        /*addObject(buffer, 0, 0, 0.0f, 0.0f,
                1.0f, 0.1f,  0.5f,  0.0f,
                0.0f, 0.0f, -1.0f, 0.0f,
                0.0f, 0.0f, 0.0f, 0.0f,
                0.5f, 0.0f, 0.0f, 0.0f);
        */
        // 1. Ground Plane (Type: 1=Plane, Material: 0=Lambertian, Color: Checker Gray)
        addObject(buffer,
                0, 0, 0.0f, 0.0f,               // Type=1 (Plane), Mat=0 (Lambertian)
                0.4f, 0.4f, 0.4f, 0.0f,         // Gray Albedo
                0.0f, -0.5f, 0.0f, 0.0f,        // Plane Point: Y = -0.5
                0.0f, 1.0f, 0.0f, 0.0f,         // Normal: Facing straight UP (0, 1, 0)
                0.0f, 0.0f, 0.0f, 0.0f);        // Radius/Height unused

        // 2. Center Glass Sphere (Type: 0=Sphere, Material: 2=Dielectric, IOR: 1.5)
        addObject(buffer,
                0, 2, 1.5f, 0.0f,               // Type=0 (Sphere), Mat=2 (Dielectric), IOR=1.5
                1.0f, 1.0f, 1.0f, 0.0f,         // Pure White (Transparent)
                0.0f, 0.0f, -1.2f, 0.0f,        // Center: (0, 0, -1.2)
                0.0f, 0.0f, 0.0f, 0.0f,         // Normal unused
                0.5f, 0.0f, 0.0f, 0.0f);        // Radius = 0.5

        // 3. Left Diffuse Sphere (Type: 0=Sphere, Material: 0=Lambertian, Color: Red/Orange)
        addObject(buffer,
                0, 0, 0.0f, 0.0f,               // Type=0 (Sphere), Mat=0 (Lambertian)
                0.9f, 0.2f, 0.1f, 0.0f,         // Warm Red Albedo
                -1.2f, 0.0f, -1.0f, 0.0f,       // Center: (-1.2, 0, -1.0)
                0.0f, 0.0f, 0.0f, 0.0f,         // Normal unused
                0.5f, 0.0f, 0.0f, 0.0f);        // Radius = 0.5

        // 4. Right Polished Metal Sphere (Type: 0=Sphere, Material: 1=Metal, Fuzz: 0.05)
        addObject(buffer,
                0, 1, 0.05f, 0.0f,              // Type=0 (Sphere), Mat=1 (Metal), Fuzziness=0.05 (Smooth mirror)
                0.8f, 0.85f, 0.88f, 0.0f,       // Silver Chrome Albedo
                1.2f, 0.0f, -1.0f, 0.0f,        // Center: (1.2, 0, -1.0)
                0.0f, 0.0f, 0.0f, 0.0f,         // Normal unused
                0.5f, 0.0f, 0.0f, 0.0f);        // Radius = 0.5

        // 5. Back Metal Cylinder (Type: 2=Cylinder, Material: 1=Metal, Fuzz: 0.1)
        addObject(buffer,
                0, 1, 0.1f, 0.0f,               // Type=2 (Cylinder), Mat=1 (Metal), Fuzziness=0.1
                0.9f, 0.75f, 0.3f, 0.0f,        // Gold Metal Albedo
                -0.6f, -0.5f, -2.0f, 0.0f,      // Base Center: (-0.6, -0.5, -2.0)
                0.0f, 1.0f, 0.0f, 0.0f,         // Axis vector: Pointing UP along Y
                0.35f, 1.5f, 0.0f, 0.0f);       // Radius = 0.35, Height = 1.5

        // 6. Small Emerald Glass Bubble (Type: 0=Sphere, Material: 2=Dielectric, IOR: 1.5)
        addObject(buffer,
                0, 2, 1.5f, 0.0f,               // Type=0 (Sphere), Mat=2 (Dielectric)
                0.1f, 0.9f, 0.3f, 0.0f,         // Tinted Emerald Green
                0.5f, -0.25f, -0.6f, 0.0f,      // Positioned close to camera
                0.0f, 0.0f, 0.0f, 0.0f,
                0.25f, 0.0f, 0.0f, 0.0f);       // Radius = 0.25

        // Ground Sphere (Radius = 100, positioned far down at Y = -100.5)
        addObject(buffer, 0, 0, 0.0f, 0.0f,
                0.5f, 0.5f, 0.5f, 0.0f,     // Gray color
                0.0f, -100.5f, -1.0f, 0.0f, // Position
                0.0f, 0.0f, 0.0f, 0.0f,
                100.0f, 0.0f, 0.0f, 0.0f);  // Radius = 100
        buffer.flip();

        int ssbo = GL43.glGenBuffers();
        GL43.glBindBuffer(GL43.GL_SHADER_STORAGE_BUFFER, ssbo);
        GL43.glBufferData(GL43.GL_SHADER_STORAGE_BUFFER, buffer, GL43.GL_STATIC_DRAW);
        GL43.glBindBufferBase(GL43.GL_SHADER_STORAGE_BUFFER, 1, ssbo);
        MemoryUtil.memFree(buffer);

        //Compile GLSL Shader
        String shaderSource = "";
        try {
            shaderSource = Files.readString(Path.of("src/main/resources/shaders/raytracer.comp"));
        }
        catch (Exception e){
            System.out.println("AHHHHHHH?");
        }
        int computeShader = GL43.glCreateShader(GL43.GL_COMPUTE_SHADER);
        GL43.glShaderSource(computeShader, shaderSource);
        GL43.glCompileShader(computeShader);

        if (GL43.glGetShaderi(computeShader, GL43.GL_COMPILE_STATUS) == GL43.GL_FALSE) {
            throw new RuntimeException("Compute Shader Error:\n" + GL43.glGetShaderInfoLog(computeShader));
        }

        int program = GL43.glCreateProgram();
        GL43.glAttachShader(program, computeShader);
        GL43.glLinkProgram(program);

        //Set Uniforms
        GL43.glUseProgram(program);
        GL43.glUniform1i(GL43.glGetUniformLocation(program, "u_Samples"), SAMPLES_PER_PIXEL);
        GL43.glUniform3f(GL43.glGetUniformLocation(program, "u_CamPos"), camPos.x(), camPos.y(), camPos.z());
        GL43.glUniform3f(GL43.glGetUniformLocation(program, "u_CamFor"), camFor.x(), camFor.y(), camFor.z());
        GL43.glUniform3f(GL43.glGetUniformLocation(program, "u_CamRight"), camRight.x(), camRight.y(), camRight.z());
        GL43.glUniform3f(GL43.glGetUniformLocation(program, "u_CamUp"), camUp.x(), camUp.y(), camUp.z());
        GL43.glUniform1f(GL43.glGetUniformLocation(program, "u_Fov"), fov);

        System.out.println("Rendering " + WIDTH + "x" + HEIGHT + " image at " + SAMPLES_PER_PIXEL + " samples per pixel on GPU...");
        long startTime = System.currentTimeMillis();

        //Render the Image
        GL43.glBindImageTexture(0, outputTexture, 0, false, 0, GL43.GL_WRITE_ONLY, GL43.GL_RGBA32F);
        GL43.glDispatchCompute((WIDTH + 15)/16, (HEIGHT + 15)/16, 1);
        GL43.glMemoryBarrier(GL43.GL_SHADER_IMAGE_ACCESS_BARRIER_BIT);
        GL43.glFinish();

        long duration = System.currentTimeMillis() - startTime;
        System.out.println("Completed in " + (duration) + " milliseconds");

        //Move image from GPU to PNG
        saveTexturePNG(outputTexture, WIDTH, HEIGHT, "renderImage.png");
        System.out.println("Render saved to png file");

        //GL Cleanup
        GL43.glDeleteTextures(outputTexture);
        GL43.glDeleteProgram(program);
        GLFW.glfwDestroyWindow(window);
        GLFW.glfwTerminate();
    }

    private static void addObject(ByteBuffer buf, int type, int material, float matParam1, float matParam2,
                           float r, float g, float b, float matParam3,
                           float x, float y, float z, float matParam4,
                           float nx, float ny, float nz, float matParam5,
                           float radius, float height, float matParam6, float additionalParam){
        buf.putInt(type).putInt(material).putFloat(matParam1).putFloat(matParam2);
        buf.putFloat(r).putFloat(g).putFloat(b).putFloat(matParam3);
        buf.putFloat(x).putFloat(y).putFloat(z).putFloat(matParam4);
        buf.putFloat(nx).putFloat(ny).putFloat(nz).putFloat(matParam5);
        buf.putFloat(radius).putFloat(height).putFloat(matParam6).putFloat(additionalParam);
    }

    private static void saveTexturePNG(int textID, int width, int height, String filepath){
        GL43.glBindTexture(GL43.GL_TEXTURE_2D, textID);
        FloatBuffer pixels = BufferUtils.createFloatBuffer(width * height * 4);
        GL43.glGetTexImage(GL43.GL_TEXTURE_2D, 0, GL43.GL_RGBA, GL43.GL_FLOAT, pixels);

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int index = ((height - 1 - y) * width + x) * 4;

                //take gamma as 2
                float r = (float) Math.sqrt(Math.max(0.0f, Math.min(1.0f, pixels.get(index))));
                float g = (float) Math.sqrt(Math.max(0.0f, Math.min(1.0f, pixels.get(index + 1))));
                float b = (float) Math.sqrt(Math.max(0.0f, Math.min(1.0f, pixels.get(index + 2))));

                int rgb = ((int)(r * 255) << 16) | ((int)(g * 255) << 8) | (int)(b * 255);
                image.setRGB(x, y, rgb);
            }
        }
        try {
            ImageIO.write(image, "png", new File(filepath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
}
