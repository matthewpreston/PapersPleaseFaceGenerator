import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
//import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import javax.swing.JFrame;

public class FaceDisplayer extends Canvas implements Runnable {
	private static final long serialVersionUID = 4725291127449226263L;
	private static final int NUM_BUFFERS = 3;
	private static final double NUM_TICKS = 60.0;
	private static int width, height;
	//private static float scaleX, scaleY;
	private static ImageGallery imageGallery;
	private static FaceGallery faceGallery;
	private static List<List<DecompiledFace>> facialData, frankenstein;
	private static int[] randomSheets, randomShifts;
	private static List<BufferedImage> newFace, frankenFace;
	//private static int randomSheet, randomShift;
	private static boolean debug;
	public JFrame frame;
	private Thread thread;
	private boolean running;
	private BufferStrategy bufferStrategy;
	
	public FaceDisplayer(int width, int height) {
		this(width, height, 1, 1, false);
	}
	
	public FaceDisplayer(int width, int height, float preferredScaleX,
			float preferredScaleY, boolean debug)
	{
		FaceDisplayer.width = width;
		FaceDisplayer.height = height;
		//FaceDisplayer.scaleX = preferredScaleX;
		//FaceDisplayer.scaleY = preferredScaleY;
		imageGallery = new ImageGallery(preferredScaleX, preferredScaleY);
		faceGallery = new FaceGallery(preferredScaleX, preferredScaleY);
		facialData = new ArrayList<List<DecompiledFace>>();
		frankenstein = new ArrayList<List<DecompiledFace>>();
		randomSheets = new int[4];
		randomShifts = new int[4];
		newFace = null;
		frankenFace = null;
		FaceDisplayer.debug = debug;
		
		// Create window
		frame = new JFrame("Papers Please Face Generator");
		frame.getContentPane().setPreferredSize(new Dimension(width, height));
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(this);
		frame.setVisible(true);
		
		this.createBufferStrategy(NUM_BUFFERS);
		bufferStrategy = getBufferStrategy(); 
		
		addKeyListener(new KeyInput()); // Listen for keyboard input
		
		// Load images into memory
		imageGallery.addImage(ImageGallery.ID.BoothWall,
				"Assets/BoothWall.png");
		
		// Load faces into memory
		faceGallery.addImage(FaceGallery.ID.SheetF0Person,
				"Assets/faces/SheetF0.png");
		faceGallery.addImage(FaceGallery.ID.SheetF0Document,
				"Assets/faces/SheetF0d.png");
		faceGallery.addImage(FaceGallery.ID.SheetF0Mask,
				"Assets/faces/SheetF0p.png");
		faceGallery.addImage(FaceGallery.ID.SheetF1Person,
				"Assets/faces/SheetF1.png");
		faceGallery.addImage(FaceGallery.ID.SheetF1Document,
				"Assets/faces/SheetF1d.png");
		faceGallery.addImage(FaceGallery.ID.SheetF1Mask,
				"Assets/faces/SheetF1p.png");
		faceGallery.addImage(FaceGallery.ID.SheetF2Person,
				"Assets/faces/SheetF2.png");
		faceGallery.addImage(FaceGallery.ID.SheetF2Document,
				"Assets/faces/SheetF2d.png");
		faceGallery.addImage(FaceGallery.ID.SheetF2Mask,
				"Assets/faces/SheetF2p.png");
		faceGallery.addImage(FaceGallery.ID.SheetM0Person,
				"Assets/faces/SheetM0.png");
		faceGallery.addImage(FaceGallery.ID.SheetM0Document,
				"Assets/faces/SheetM0d.png");
		faceGallery.addImage(FaceGallery.ID.SheetM0Mask,
				"Assets/faces/SheetM0p.png");
		faceGallery.addImage(FaceGallery.ID.SheetM1Person,
				"Assets/faces/SheetM1.png");
		faceGallery.addImage(FaceGallery.ID.SheetM1Document,
				"Assets/faces/SheetM1d.png");
		faceGallery.addImage(FaceGallery.ID.SheetM1Mask,
				"Assets/faces/SheetM1p.png");
		faceGallery.addImage(FaceGallery.ID.SheetM2Person,
				"Assets/faces/SheetM2.png");
		faceGallery.addImage(FaceGallery.ID.SheetM2Document,
				"Assets/faces/SheetM2d.png");
		faceGallery.addImage(FaceGallery.ID.SheetM2Mask,
				"Assets/faces/SheetM2p.png");
		faceGallery.addImage(FaceGallery.ID.SheetM3Person,
				"Assets/faces/SheetM3.png");
		faceGallery.addImage(FaceGallery.ID.SheetM3Document,
				"Assets/faces/SheetM3d.png");
		faceGallery.addImage(FaceGallery.ID.SheetM3Mask,
				"Assets/faces/SheetM3p.png");
		
		generateNewFace(); // Generate a new face
		start(); 		   // Starts a new thread
	}
	
	/**
	 * Currently only a single thread (may upgrade later when I figure it out)
	 */
	public synchronized void start() {
		thread = new Thread(this);
		thread.start();
		running = true;
	}
	
	@Override
	public void run() {
		long lastTime = System.nanoTime();
		double ns = 1000000000 / NUM_TICKS;
		double delta = 0;
		long timer = System.currentTimeMillis();
		//int frames = 0;
		
		while (running) {
			long now = System.nanoTime();
			delta += (now - lastTime) / ns;
			lastTime = now;
			while (delta >= 1) {
				tick();
				delta--;
			}
			if (running) render();
			//frames++;
			if (System.currentTimeMillis() - timer > 1000) {
				timer += 1000;
				//System.out.println("FPS: " + frames);
				//frames = 0;
			}
		}
		stop();
	}
	
	public void stop() {
		try {
			thread.join();
			running = false;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void tick() {
		
	}
	
	public void render() {
		while (running) {
			// Render single frame
			do {
				// The following loop ensures that the contents of the drawing
		        // buffer are consistent in case the underlying surface was
				// recreated
				do {
					// Get a new graphics context every time through the loop
		            // to make sure the strategy is validated
					Graphics graphics = bufferStrategy.getDrawGraphics();
					
					// Render to graphics
					graphics.setColor(new Color(0xA2, 0x94, 0x90, 0xFF));
					graphics.fillRect(0, 0, width, height);
					
					// Draw booth wall
					int scaledW = (int) (width / 5);
					int scaledH = (int) (height / 5);
					BufferedImage boothWall = imageGallery.getImage(
							ImageGallery.ID.BoothWall);
					int deltaX = (boothWall.getWidth() - scaledW) / 2;
					int deltaY = boothWall.getHeight() - scaledH;
					boothWall = boothWall.getSubimage(deltaX, deltaY,
							scaledW, scaledH);
					for (int i = 0; i < 5; i++) {
						for (int j = 0; j < 2; j++) {
							graphics.drawImage(boothWall, i*scaledW, j*scaledH,
									null);
						}
						graphics.drawImage(boothWall, i*scaledW, 3*scaledH,
								null);
					}
					
					// Draw original person, document, and mask
					FaceGallery.ID[] IDs = FaceGallery.ID.values();
					for (int i = 0; i < facialData.size(); i++) {
						int randomShift = randomShifts[i],
							randomSheet = randomSheets[i]; 
						int x = (randomShift % 2 == 0) ? 0 : scaledW,
						    y = (randomShift % 4 <= 1) ? 0 : scaledH;
						graphics.drawImage(faceGallery.getImage(
								IDs[randomSheet]).getSubimage(
										x, y, scaledW, scaledH),
								i*scaledW, 0, null);
						
						// Draw decompiled faces and scaled faces
						List<DecompiledFace> randomFace = facialData.get(i);
						List<DecompiledFace> scaledFace = frankenstein.get(i);
						for (int j = 0; j < randomFace.size(); j++) {
							DecompiledFace randomDF = randomFace.get(j);
							DecompiledFace scaledDF = scaledFace.get(j);
							switch (i) {
							case 0:
								graphics.drawImage(randomDF.shoulders,
										i*scaledW, (j+1)*scaledH, null);
								graphics.drawImage(scaledDF.shoulders,
										i*scaledW, (j+3)*scaledH, null);
								break;
							case 1:
								graphics.drawImage(randomDF.face,
										i*scaledW, (j+1)*scaledH, null);
								graphics.drawImage(scaledDF.face,
										i*scaledW, (j+3)*scaledH, null);
								break;
							case 2:
								graphics.drawImage(randomDF.eyes,
										i*scaledW, (j+1)*scaledH, null);
								graphics.drawImage(scaledDF.eyes,
										i*scaledW, (j+3)*scaledH, null);
								break;
							case 3:
								graphics.drawImage(randomDF.noseAndMouth,
										i*scaledW, (j+1)*scaledH, null);
								graphics.drawImage(scaledDF.noseAndMouth,
										i*scaledW, (j+3)*scaledH, null);
								break;
							}
						}
					}
					
					// Draw new assembled / frankenstein face
					for (int i = 0; i < newFace.size(); i++) {
						graphics.drawImage(newFace.get(i), 4*scaledW, (i+1)*scaledH, null);
						graphics.drawImage(frankenFace.get(i), 4*scaledW, (i+3)*scaledH, null);
					}
					
					// Dispose the graphics
					graphics.dispose();
					
					// Repeat the rendering if the drawing buffer contents
		            // were restored
				} while (bufferStrategy.contentsRestored());
				
				// Display the buffer
				bufferStrategy.show();
				
				// Repeat the rendering if the drawing buffer was lost
			} while (bufferStrategy.contentsLost());
		}
	}
	
	/**
	 * Creates a new face on the fly
	 */
	public static synchronized void generateNewFace() {
		List<List<DecompiledFace>> temp = new ArrayList<List<DecompiledFace>>();
		FaceGallery.ID[] IDs = FaceGallery.ID.values();
		
		// Create a face
		if (debug) { // Chooses a specific face to craft
			randomSheets[0] =   9; randomShifts[0] = 0;
			randomSheets[1] =   9; randomShifts[1] = 1;
			randomSheets[2] =  12; randomShifts[2] = 3;
			randomSheets[3] =  12; randomShifts[3] = 2;
			try {
				temp.add(FaceGenerator.decompileFaces(
						faceGallery.getImage(IDs[randomSheets[0]]),
						faceGallery.getImage(IDs[randomSheets[0]+1]),
						faceGallery.getImage(IDs[randomSheets[0]+2]),
						randomShifts[0]));
				temp.add(FaceGenerator.decompileFaces(
						faceGallery.getImage(IDs[randomSheets[1]]),
						faceGallery.getImage(IDs[randomSheets[1]+1]),
						faceGallery.getImage(IDs[randomSheets[1]+2]),
						randomShifts[1]));
				temp.add(FaceGenerator.decompileFaces(
						faceGallery.getImage(IDs[randomSheets[2]]),
						faceGallery.getImage(IDs[randomSheets[2]+1]),
						faceGallery.getImage(IDs[randomSheets[2]+2]),
						randomShifts[2]));
				temp.add(FaceGenerator.decompileFaces(
						faceGallery.getImage(IDs[randomSheets[3]]),
						faceGallery.getImage(IDs[randomSheets[3]+1]),
						faceGallery.getImage(IDs[randomSheets[3]+2]),
						randomShifts[3]));
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		} else { // Create a random face
			// Decompile 4 random faces
			int gender = ThreadLocalRandom.current().nextInt(0, 2);
			int tries = 0;
			for (int i = 0; i < 4; i++) {
				int randomSheet;
				if (gender == 0) // Female
					randomSheet = ThreadLocalRandom.current().nextInt(
							0, 9);
				else // Male
					randomSheet = ThreadLocalRandom.current().nextInt(
							9,IDs.length);
				randomSheet -= randomSheet % 3;
				int randomShift = ThreadLocalRandom.current().nextInt(0, 4);
				randomSheets[i] = randomSheet;
				randomShifts[i] = randomShift;
				try {
					temp.add(FaceGenerator.decompileFaces(
							faceGallery.getImage(IDs[randomSheet]),
							faceGallery.getImage(IDs[randomSheet+1]),
							faceGallery.getImage(IDs[randomSheet+2]),
							randomShift));
				} catch (Exception e) {
					if (tries < 3) {
						System.err.println("Failed to generate face,"
								+ " trying again;");
						e.printStackTrace();
						tries++;
						i--;	// To ensure that we get 4 faces
					} else {
						System.err.println("Creating face failed after "
								+ tries + " times. Exiting");
						e.printStackTrace();
						System.exit(1);
					}
				}
			}
		}
		
		// Generate the new face from these decompiled faces
		facialData = temp;
		newFace = FaceGenerator.generateFace(facialData);
		
		// Generate a scaled version of this face
		// Stupid copying
		List<List<DecompiledFace>> temp2 = new ArrayList<List<DecompiledFace>>(temp.size());
		for (int i = 0; i < temp.size(); i++) {
			List<DecompiledFace> temp3 = new ArrayList<DecompiledFace>(temp.get(i).size());
			for (int j = 0; j < temp.get(i).size(); j++)
				temp3.add(new DecompiledFace(temp.get(i).get(j)));
			temp2.add(temp3);
		}
		frankenstein = FaceGenerator.scaleFacialFeatures(temp2);
		frankenFace = FaceGenerator.generateFace(temp2);
	}
	
	public static void main(String args[]) {
		float scaleX, scaleY;
		scaleX = scaleY = 1;
		new FaceDisplayer((int) (300*scaleX*5/2), (int) (240*scaleY*5/2),
				scaleX, scaleY, false);
	}
}
