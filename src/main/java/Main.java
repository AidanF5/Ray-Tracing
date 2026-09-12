import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL43;
import org.lwjgl.system.MemoryUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.File;


public class Main {
    private static final int WIDTH = 1920;
    private static final int HEIGHT = 1080;
    private static final int SAMPLES_PER_PIXEL = 256;
    private static final int BOUNCES_PER_RAY = 64;

    //private static final Vec3 camPos = new Vec3(13.0f, 2.0f, 3.0f);
    //private static final Vec3 camFor = VectorOperations.normalise(new Vec3(-13.0f, -2.0f, -3.0f));
    //private static final Vec3 camUp = new Vec3(0.0f, 1.0f, 0.0f);
    private static final Vec3 camPos = new Vec3(0, 2f, -0.2f);
    private static final Vec3 camFor = VectorOperations.normalise(new Vec3(1, 0, 0.05f));
    private static final Vec3 camUp = new Vec3(0.0f, 1.0f, 0.0f);
    private static final Vec3 camRight = VectorOperations.normalise(VectorOperations.cross(camFor, camUp));
    private static final float fov = 70.0f;

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
        //int numObjects = 490;
        int numObjects = 244;
        ByteBuffer buffer = MemoryUtil.memAlloc(numObjects * 80);  //int and floats are 4 bytes

        //createTestScene(buffer);
        createSnowglobeScene(buffer);

        buffer.flip();

        int ssbo = GL43.glGenBuffers();
        GL43.glBindBuffer(GL43.GL_SHADER_STORAGE_BUFFER, ssbo);
        GL43.glBufferData(GL43.GL_SHADER_STORAGE_BUFFER, buffer, GL43.GL_STATIC_DRAW);
        GL43.glBindBufferBase(GL43.GL_SHADER_STORAGE_BUFFER, 1, ssbo);
        MemoryUtil.memFree(buffer);

        //Compile GLSL Shader
        int program = createProgram("src/main/resources/shaders");

        //Set Uniforms
        GL43.glUseProgram(program);
        GL43.glUniform1i(GL43.glGetUniformLocation(program, "u_Samples"), SAMPLES_PER_PIXEL);
        GL43.glUniform1i(GL43.glGetUniformLocation(program, "u_maxBounces"), BOUNCES_PER_RAY);
        GL43.glUniform1i(GL43.glGetUniformLocation(program, "u_SamplesPerPass"), 4);
        GL43.glUniform3f(GL43.glGetUniformLocation(program, "u_CamPos"), camPos.x(), camPos.y(), camPos.z());
        GL43.glUniform3f(GL43.glGetUniformLocation(program, "u_CamFor"), camFor.x(), camFor.y(), camFor.z());
        GL43.glUniform3f(GL43.glGetUniformLocation(program, "u_CamRight"), camRight.x(), camRight.y(), camRight.z());
        GL43.glUniform3f(GL43.glGetUniformLocation(program, "u_CamUp"), camUp.x(), camUp.y(), camUp.z());
        GL43.glUniform1f(GL43.glGetUniformLocation(program, "u_Fov"), fov);
        //GL43.glUniform3f(GL43.glGetUniformLocation(program, "u_backgroundCol"), 0, 0, 0);
        GL43.glUniform3f(GL43.glGetUniformLocation(program, "u_backgroundCol"), 0.02f, 0.15f, 0.3f);
        //GL43.glUniform3f(GL43.glGetUniformLocation(program, "u_backgroundCol"), 1, 1, 1);

        System.out.println("Rendering " + WIDTH + "x" + HEIGHT + " image at " + SAMPLES_PER_PIXEL + " samples per pixel on GPU...");
        long startTime = System.currentTimeMillis();

        //Render the Image
        int passes = SAMPLES_PER_PIXEL/4;
        int framebuffers = GL43.glGenFramebuffers();

        for (int i = 0; i < passes; i++) {
            GLFW.glfwPollEvents();
            if(GLFW.glfwWindowShouldClose(window)){
                break;
            }

            GL43.glUniform1i(GL43.glGetUniformLocation(program, "u_CurrentSample"), i);
            GL43.glBindImageTexture(0, outputTexture, 0, false, 0, GL43.GL_READ_WRITE, GL43.GL_RGBA32F);
            GL43.glDispatchCompute((WIDTH + 15) / 16, (HEIGHT + 15) / 16, 1);
            GL43.glMemoryBarrier(GL43.GL_SHADER_IMAGE_ACCESS_BARRIER_BIT | GL43.GL_TEXTURE_FETCH_BARRIER_BIT);

            GL43.glBindFramebuffer(GL43.GL_READ_FRAMEBUFFER, framebuffers);
            GL43.glFramebufferTexture2D(GL43.GL_READ_FRAMEBUFFER, GL43.GL_COLOR_ATTACHMENT0, GL43.GL_TEXTURE_2D, outputTexture, 0);
            GL43.glBlitFramebuffer(0, 0, WIDTH, HEIGHT, 0, 0, WIDTH, HEIGHT, GL43.GL_COLOR_BUFFER_BIT, GL43.GL_NEAREST);

            GLFW.glfwSwapBuffers(window);

        }
        GL43.glFinish();

        //Move image from GPU to PNG
        saveTexturePNG(outputTexture, WIDTH, HEIGHT, "renderImage.png");
        System.out.println("Render saved to png file");

        long duration = System.currentTimeMillis() - startTime;
        System.out.println("Completed in " + (duration) + " milliseconds");

        //GL Cleanup
        GL43.glDeleteTextures(outputTexture);
        GL43.glDeleteProgram(program);
        GLFW.glfwDestroyWindow(window);
        GLFW.glfwTerminate();
    }

    private static void addObject(ByteBuffer buf, int type, int material, float matParam1, float matParam2,
                           float r, float g, float b, float matParam3,
                           float x, float y, float z, float matParam4,
                           float nx, float ny, float nz, float emitt,
                           float radius, float height, float shapeParam1, float additionalParam){
        buf.putInt(type).putInt(material).putFloat(matParam1).putFloat(matParam2);
        buf.putFloat(r).putFloat(g).putFloat(b).putFloat(matParam3);
        buf.putFloat(x).putFloat(y).putFloat(z).putFloat(matParam4);
        buf.putFloat(nx).putFloat(ny).putFloat(nz).putFloat(emitt);
        buf.putFloat(radius).putFloat(height).putFloat(shapeParam1).putFloat(additionalParam);
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

    private static void createTestScene(ByteBuffer buf){

        //addObject(buf, 0, 0, 0, 0,
        //        0.5f, 0.5f, 0.5f, 0,
        //        0, -1000, 0, 0,
        //        0, 0, 0, 0,
        //        1000, 0, 0, 0);

        //addObject(buf, 1, 2, 1.33f, 0,
        //        0.5f, 0.5f, 0.5f, 0,
        //        0, 0, 0, 0,
        //        0, 1, 0, 0,
        //        0, 0, 0, 0);

        addObject(buf, 11, 0, 0, 0,
                0.5f, 0.5f, 0.5f, 0,
                0, 0, 0, 0,
                0, 1, 0, 0,
                5, 0, 0, 0);

        for (int a = -11; a < 11; a++){
            for (int b = -11; b < 11; b++){
                double chooseMat = Math.random();
                float x = a+0.9f * (float)Math.random();
                float y = 0.2f;
                float z = b + 0.9f * (float)Math.random();

                boolean clearGlass = x*x + z*z > 0.81;
                boolean clearLamb = (x+4)*(x+4) + z*z > 0.81;
                boolean clearMetal = (x-4)*(x-4)+(z*z) > 0.81;

                if(clearGlass && clearLamb && clearMetal){
                    Vec3 colour;
                    float matParam1 = 0.0f;
                    int material;
                    if(chooseMat < 0.8){
                        colour = VectorOperations.multiplyComponents(Vec3.random(), Vec3.random());
                        material = 0;
                    }
                    else if(chooseMat < 0.95){
                        colour = Vec3.random(0.5, 1.0);
                        matParam1 = (float) (Math.random()/2);
                        material = 1;
                    }
                    else{
                        matParam1 = 1.5f;
                        colour = new Vec3(1.0f, 1.0f, 1.0f);
                        material = 2;
                    }
                    addObject(buf, 0, material, matParam1, 0.0f,
                            colour.x(), colour.y(), colour.z(), 0 ,
                            x, y, z, 0,
                            0, 0, 0, 0,
                            0.2f, 0, 0, 0);

                }
                else{
                    b--;
                }
            }
        }

        addObject(buf, 3, 1, 0.05f, 0,
                1, 1, 1, 0,
                0, 0, 0, 0,
                -0.5f, 1, 0, 0,
                (float)(Math.PI/6), 2, 1, 1);

        addObject(buf, 0, 2, 1.5f, 0,
                1, 1, 1, 0,
                -4, 1, 0, 0,
                0, 0, 0, 0,
                1, 0, 0, 0);

        addObject(buf, 2, 1, 0, 0,
                0.7f, 0.6f, 0.5f, 0,
                4, 1, 0, 0,
                1, 1, 0, 0,
                1, 1, 0, 0);


        addObject(buf, 0, 3, 0, 0,
                25, 25, 25, 0,
                3, 1.5f, 3, 0,
                0, 0, 0, 1,
                0.4f, 0, 0, 0);


        addObject(buf, 0, 3, 0, 0,
                25, 25, 25, 0,
                -3, 2, -3, 0,
                0, 0, 0, 1,
                0.5f, 0, 0, 0);


    }


    public static int createProgram(String folder){
        StringBuilder fullShader = new StringBuilder();

        File f = new File(folder);
        File[] listOfFiles = f.listFiles();
        int program = GL43.glCreateProgram();

        for (int i = 0; i < listOfFiles.length; i++) {
            try {
                fullShader.append(Files.readString(Path.of(listOfFiles[i].getPath())));
            }
            catch(Exception e){
                System.out.println("Failed to read " + Path.of(listOfFiles[i].getPath()));
                System.exit(0);
            }
        }

        int shader = GL43.glCreateShader(GL43.GL_COMPUTE_SHADER);

        GL43.glShaderSource(shader, fullShader.toString());
        GL43.glCompileShader(shader);

        if(GL43.glGetShaderi(shader, GL43.GL_COMPILE_STATUS) == GL43.GL_FALSE){
            throw new RuntimeException("Error compiling:\n" + GL43.glGetShaderInfoLog(shader));
        }

        GL43.glAttachShader(program, shader);
        GL43.glLinkProgram(program);

        if (GL43.glGetProgrami(program, GL43.GL_LINK_STATUS) == GL43.GL_FALSE) {
            throw new RuntimeException("Program Linking Error:\n" + GL43.glGetProgramInfoLog(program));
        }

        GL43.glDetachShader(program, shader);
        GL43.glDeleteShader(shader);


        return program;
    }

    private static void createSnowglobeScene(ByteBuffer buf){

        //Building
        //floor
        addObject(buf, 1, 0, 0, 0,
                1, 1, 1, 0,
                0, 0, 0, 0,
                0, 1, 0, 0,
                0, 0, 0, -1);

        //left wall
        addObject(buf, 1, 0, 0, 0,
                0.66f, 0.66f, 0.66f, 0,
                0, 0, 0, 0,
                0.7f, 0, 1, 0,
                0, 0, 0, 1);

        //right wall
        addObject(buf, 1, 0, 0, 0,
                0.66f, 0.66f, 0.66f, 0,
                0, 0, 0, 0,
                0.7f, 0, -1, 0,
                0, 0, 0, 1);

        //back glass wall
        addObject(buf, 1, 2, 1.5f, 0,
                1, 1, 1, 0,
                0, 0, 0, 0,
                -1, 0, 0, 0,
                0, 0, 0, 10);

        //back blocking wall
        addObject(buf, 0, 0, 0, 0,
                1, 1, 1, 0,
                0, 0, 0, 0,
                1, 0, 0, 0,
                0, 0, 0, 6);

        //roof disk
        addObject(buf, 11, 0, 0, 0,
                0.85f, 0.85f, 0.85f, 0,
                0, 4, 0, 0,
                0, -1, 0, 0,
                10, 0, 0, 10);

        //joining support cylinders
        addObject(buf, 2, 0, 0, 0,
                0.5f, 0.5f, 0.5f, 0,
                10, 0, 8, 0,
                0, 1, 0, 0,
                1, 20, 0, 1);

        addObject(buf, 2, 0, 0, 0,
                0.5f, 0.5f, 0.5f, 0,
                10, 0, -8f, 0,
                0, 1, 0, 0,
                1, 20, 0, 1);

        addObject(buf, 2, 0, 0, 0,
                0.5f, 0.5f, 0.5f, 0,
                10, 1, 0f, 0,
                0, 0, 1, 0,
                1, 20, 0, 1);

        //light
        addObject(buf, 0, 3, 0, 0,
                10f, 10, 10, 0,
                0f, 5, 0f, 0,
                0, 0, 0, 1,
                1, 0, 0, 0);

        //lightshade
        addObject(buf, 2, 0, 0, 0,
                0.5f, 0.5f, 0.9f, 0,
                0, 5, 0f, 0,
                0, 1, 0, 0,
                1.5f, 3, 0, 0);

        //Table
        addObject(buf, 2, 0, 0, 0,
                0.6f, 0.3f, 0, 0,
                0, 1.8f, 0, 0,
                0, 1, 0, 0,
                0.4f, 0.3f, 0, 1);

        //globe
        addObject(buf, 0, 2, 1.5f, 0,
                1, 1, 1, 0,
                0.3f, 2.049f, 0f, 0 ,
                0, 0, 0, 0,
                0.1f, 0, 0, 0);


        //globe support
        addObject(buf, 3, 1, 0.1f, 0,
                1, 0.8f, 0, 0,
                0.3f, 2.2f, 0, 0,
                0, -1, 0, 0,
                (float)Math.PI/12, 0.25f, 0.23f, 0);

        //globe light
        addObject(buf, 11, 3, 0, 0,
                4, 4, 4, 0,
                0.3f, 1.97f, 0, 0,
                0, 1, 0, 1,
                0.05f, 0, 0, -1.97f);

        //snowman
        addObject(buf, 0, 0, 0, 0,
                0.92f, 0.94f, 0.96f, 0,
                0.3f, 2.053f, 0, 0,
                0, 0, 0, 0,
                0.012f, 0, 0, 0);

        addObject(buf, 0, 0, 0, 0,
                0.92f, 0.94f, 0.96f, 0,
                0.3f, 2.03f, 0, 0,
                0, 0, 0, 0,
                0.015f, 0, 0, 0);

        addObject(buf, 0, 0, 0, 0,
                0.92f, 0.94f, 0.96f, 0,
                0.3f, 2f, 0, 0,
                0, 0, 0, 0,
                0.02f, 0, 0, 0);

        //hat
        addObject(buf, 2, 0, 0, 0,
                0.1f, 0.1f, 0.1f, 0,
                0.3f, 2.06f, 0, 0,
                0, 1, 0, 0,
                0.015f, 0.001f, 0, 1);
        addObject(buf, 2, 0, 0, 0,
                0.1f, 0.1f, 0.1f, 0,
                0.3f, 2.0612f, 0, 0,
                0, 1, 0, 0,
                0.01f, 0.014f, 0, 1);
        addObject(buf, 2, 0, 0, 0,
                0.1f, 0.1f, 0.1f, 0,
                0.3f, 2.0629f, 0, 0,
                0, 1, 0, 0,
                0.01f, 0.02f, 0, 1);

        //snowman details
        addObject(buf, 0, 0, 0, 0,
                0.02f, 0.02f, 0.02f, 0,
                0.289f, 2.056f, 0, 0,
                0, 0, 0, 0,
                0.001f, 0, 0, 0);
        addObject(buf, 0, 0, 0, 0,
                0.02f, 0.02f, 0.02f, 0,
                0.295f, 2.056f, -0.01f, 0,
                0, 0, 0, 0,
                0.001f, 0, 0, 0);

        addObject(buf, 0, 0, 0, 0,
                0.02f, 0.02f, 0.02f, 0,
                0.289f, 2.051f, 0, 0,
                0, 0, 0, 0,
                0.001f, 0, 0, 0);
        addObject(buf, 0, 0, 0, 0,
                0.02f, 0.02f, 0.02f, 0,
                0.295f, 2.051f, -0.01f, 0,
                0, 0, 0, 0,
                0.001f, 0, 0, 0);
        addObject(buf, 0, 0, 0, 0,
                0.02f, 0.02f, 0.02f, 0,
                0.2932f, 2.049f, -0.008f, 0,
                0, 0, 0, 0,
                0.001f, 0, 0, 0);
        addObject(buf, 0, 0, 0, 0,
                0.02f, 0.02f, 0.02f, 0,
                0.2898f, 2.049f, -0.0025f, 0,
                0, 0, 0, 0,
                0.001f, 0, 0, 0);
        addObject(buf, 0, 0, 0, 0,
                0.02f, 0.02f, 0.02f, 0,
                0.2914f, 2.048f, -0.00515f, 0,
                0, 0, 0, 0,
                0.001f, 0, 0, 0);

        addObject(buf, 0, 0, 0, 0,
                0.02f, 0.02f, 0.02f, 0,
                0.2895f, 2.035f, -0.008f, 0,
                0, 0, 0, 0,
                0.0012f, 0, 0, 0);
        addObject(buf, 0, 0, 0, 0,
                0.02f, 0.02f, 0.02f, 0,
                0.2895f, 2.025f, -0.008f, 0,
                0, 0, 0, 0,
                0.0012f, 0, 0, 0);

        addObject(buf, 2, 0, 0, 0,
                0.1f, 0.1f, 0.1f, 0,
                0.31f, 2.038f, -0.018f, 0,
                0.3f, 0.8f, -0.5f, 0,
                0.0003f, 0.02f, 0, 1);
        addObject(buf, 2, 0, 0, 0,
                0.1f, 0.1f, 0.1f, 0,
                0.31f, 2.038f, 0.028f, 0,
                -0.3f, 0.8f, 0.5f, 0,
                0.0003f, 0.02f, 0, 1);

        addObject(buf, 3, 0, 0, 0,
                0.926f, 0.566f, 0.129f, 0,
                0.284f, 2.052f, -0.008f, 0,
                1, 0, 0.5f, 0,
                (float)Math.PI/24, 0.01f, 0, 0);

        //snowflakes
        for (int i = 0; i < 200; i++){
            float x = (float)Math.random()/10;
            float y = (float)Math.random()/10;
            float z = (float)Math.random()/10;
            if(x*x + y*y + z*z > 0.01){
                i--;
                continue;
            }
            if(Math.random() > 0.5){
                x = -x;
            }
            if(Math.random() > 0.5){
                y = -y;
            }
            if(Math.random() > 0.5){
                z = -z;
            }
            float nx = (float)Math.random();
            float ny = (float)Math.random();
            float nz = (float)Math.random();
            addObject(buf, 11, 0, 0, 0,
                    0.92f, 0.94f, 0.96f, 0,
                    x + 0.3f, y + 2.049f, z, 0,
                    nx, ny, nz, 0,
                    0.001f, 0, 0, -((x + 0.3f)*nx+(y + 2.049f)*ny+z*nz));
        }

        addObject(buf, 2, 1, 0.05f, 0,
                0.6f, 0.5f, 0.3f, 0,
                0.1f, 1.96f, -0.28f, 0,
                -0.6f, 0, -0.3f, 0,
                0.01f, 0.02f, 0, 0);
        addObject(buf, 2, 1, 0f, 0,
                0.4f, 0.5f, 0.7f, 0,
                0.1f, 1.952f, -0.2f, 0,
                -0.7f, 0, -0.5f, 0,
                0.002f, 0.025f, 0, 0);
        addObject(buf, 2, 1, 0.25f, 0,
                0.4f, 0.6f, 0.1f, 0,
                0.13f, 1.955f, -0.25f, 0,
                -0.1f, 0, 0.3f, 0,
                0.005f, 0.02f, 0, 0);
        addObject(buf, 2, 2, 2f, 0,
                1f, 1f, 1f, 0,
                0.13f, 1.955f, -0.14f, 0,
                0, 1, 0, 0,
                0.015f, 0.1f, 0, 1);

        addObject(buf, 0, 0, 0, 0,
                0.2f, 0.4f, 0.6f, 0,
                0.13f, 1.965f, -0.05f, 0,
                0, 0, 0, 0,
                0.015f, 0, 0, 0);
        addObject(buf, 0, 1, 2f, 0,
                0.7f, 0.6f, 0.5f, 0,
                0.16f, 1.96f, -0.11f, 0,
                0, 0, 0, 0,
                0.01f, 0, 0, 0);
        addObject(buf, 0, 1, 0, 0,
                0.7f, 0.5f, 0.4f, 0,
                0.24f, 1.97f, -0.2f, 0,
                0, 0, 0, 0,
                0.02f, 0, 0, 0);

        //torch
        addObject(buf, 2, 2, 0.2f, 0,
                0.3f, 0.3f, 0.4f, 0,
                0.2f, 1.962f, -0.29f, 0,
                0.35f, 0, -0.4f, 0,
                0.012f, 0.05f, 0, 1);
        addObject(buf, 0, 2, 0.2f, 0,
                0.3f, 0.3f, 0.4f, 0,
                0.183537f, 1.962f, -0.271186f, 0,
                0, 0, 0, 0,
                0.012f, 0, 0, 0);
        addObject(buf, 11, 3, 0, 0,
                10, 10, 10, 0,
                0.216528f, 1.962f, -0.30889f, 0,
                0.35f, 0, -0.4f, 1,
                0.012f, 0.05f, 0, 0.1993408f);
        addObject(buf, 0, 2, 0.2f, 0,
                0.3f, 0.3f, 0.4f, 0,
                0.214617f, 1.970485f, -0.29382f, 0,
                0, 0, 0, 0,
                0.002f, 0, 0, 0);

        //outside
    }
}
