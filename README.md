# Hybrid Java and GLSL Ray Tracing Engine

![Example Image](https://github.com/AidanF5/Ray-Tracing/blob/master/Saved-Image.png?raw=true)

Welcome to my rendering project, a high performance ray tracing engine extending from Peter Shirley's *Ray Tracing in One Weekend*

The goal of this project is to extend it as far as possible to create a realistic renderer of many difference scenes

This project includes CPU code allowing for easy extensibility and modularity
It also includes GPU code written in GLSL to give a fast render

## Key Features: <br />
&emsp;Dual Engine Architecture<br />
&emsp;&emsp;CPU Engine: object-oriented Java design for easy extensibility and prototyping <br />
&emsp;&emsp;GPU Engine: Custom GLSL shaders for executing quick paralleled calculations for high quality renders <br />
&emsp;Configurable depth-of-field, field of view and placement <br />
&emsp;Materials including Lambertian diffuse, Metallic, Dielectrics <br />
&emsp;Shapes including Spheres, Planes and Cylinders


## RoadMap: <br />
&emsp;Native PNG for CPU renderer (replacing PPM format) <br /> 
&emsp;Exporting the rest of the camera controls into GLSL shaders <br />
&emsp;Implement an emissive light material (removing default sky gradient) <br/>
&emsp;Implement further primitives of Tori and Cones
