import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class FaceGenerator {
	public static final int PERSON_FACE_COLOUR   = 0xFF848A6B;	// Peat green
	public static final int PERSON_DARK_COLOUR   = 0xFF3D5043;	// Pine green
	public static final int PERSON_LIGHT_COLOUR  = 0xFFDED6AC;	// Egg shell
	public static final int COAT_DARK_COLOUR   	 = 0xFF551B18;	// Blood red
	public static final int COAT_LIGHT_COLOUR    = 0xFF892C27;	// Rose red
	public static final int DOCUMENT_FACE_COLOUR = 0xFFA29490;	// Grey
	public static final int DOCUMENT_DARK_COLOUR = 0xFF574848;	// Charcoal
	
	/**
	 * Generates a random face and returns the image of the person and their
	 * document photo
	 * 
	 * @param imageGallery	Holds the person and document photos
	 * @return				List of BufferedImage's [0] = person, [1] = document
	 */
	public static List<BufferedImage> generateFace(FaceGallery imageGallery) {
		List<List<DecompiledFace>> frankenstein
			= new ArrayList<List<DecompiledFace>>();
		FaceGallery.ID[] IDs = FaceGallery.ID.values();
		
		// Decompile 4 random faces
		int tries = 0;
		for (int i = 0; i < 4; i++) {
			int randomSheet = ThreadLocalRandom.current().nextInt(0, IDs.length);
			randomSheet -= randomSheet % 3;
			int randomShift = ThreadLocalRandom.current().nextInt(0, 4);
			try {
				frankenstein.add(decompileFaces(
						imageGallery.getImage(IDs[randomSheet]),
						imageGallery.getImage(IDs[randomSheet+1]),
						imageGallery.getImage(IDs[randomSheet+2]),
						randomShift));
			} catch (Exception e) {
				if (tries < 3) {
					System.err.println("Failed to generate face, trying again;");
					e.printStackTrace();
					tries++;
					i--;	// To ensure that we get 4 faces
				} else {
					System.err.println("Failed to generate face after 3 times");
					e.printStackTrace();
					System.exit(1);
				}
			}
		}
		
		// Generate new face
		return generateFace(frankenstein);
	}
	
	/**
	 * Generates a random face and returns the image of the person and their
	 * document photo
	 * 
	 * @param frankenstein	4 person and document decompiled faces
	 * @return				List of BufferedImage's [0] = person, [1] = document
	 */
	public static List<BufferedImage> generateFace(
			List<List<DecompiledFace>> frankenstein)
	{
		// Scale the blank face, eyes, and nose & mouth
		//frankenstein = scaleFacialFeatures(frankenstein);
		
		// Calculate facial feature points (overlap chins to place face
		// initially, overlap interocular midpoints, and overlap nose and mouth
		// midpoints (the groove under one's nose is the philtrum))
		Point oldChin = frankenstein.get(0).get(0).chin,
			  newChin = frankenstein.get(1).get(0).chin;
		int deltaXChin = oldChin.x - newChin.x,
			deltaYChin = oldChin.y - newChin.y;
		Point oldEyes = frankenstein.get(1).get(0).getInterocularMidPoint(),
			  newEyes = frankenstein.get(2).get(0).getInterocularMidPoint();
		int deltaXEyes = oldEyes.x - newEyes.x,
			deltaYEyes = oldEyes.y - newEyes.y;
		Point oldPhiltrum = frankenstein.get(1).get(0).getNoseMouthMidPoint(),
			  newPhiltrum = frankenstein.get(3).get(0).getNoseMouthMidPoint();
		int deltaXPhiltrum = oldPhiltrum.x - newPhiltrum.x,
			deltaYPhiltrum = oldPhiltrum.y - newPhiltrum.y;
		
		// Overlay face, eyes, and nose & mouth over shoulders for both person
		// and document
		List<BufferedImage> newFace = new ArrayList<BufferedImage>();
		BufferedImage tempFace = frankenstein.get(1).get(0).face; // For W & H
		int width = tempFace.getWidth(), height = tempFace.getHeight();
		for (int i = 0; i < 2; i++) {
			// Create a new BufferedImage with a full colour space
			tempFace = new BufferedImage(width, height,
					BufferedImage.TYPE_INT_ARGB);
			Graphics2D tempGraphics = (Graphics2D) tempFace.createGraphics();
			
			// Draw shoulders on temporary graphical output
			tempGraphics.drawImage(frankenstein.get(0).get(i).shoulders,
					null, 0, 0);
			
			// Fill in a blank face
			BufferedImage filledFace = createFilledFace(frankenstein, i,
					deltaXEyes, deltaYEyes, deltaXPhiltrum, deltaYPhiltrum);
			
			// Draw this face on temporary graphical output
			tempGraphics.drawImage(filledFace, null, deltaXChin, deltaYChin);

			tempGraphics.dispose();
			
			// Add this to the list to return
			newFace.add(tempFace);
		}
		return newFace;
	}
	
	/**
	 * Using an image mask, separates the eyes, nose & mouth, shoulders, and
	 * face of a person in both their normal image and on their document.
	 * Assumes that the dimensions of all images are the same (cough)
	 * 
	 * @param person		Image of person/entrant
	 * @param document		Image of person on document
	 * @param mask			Mask to separate eyes, nose & mouth, shoulders, face
	 * @param shift			Which person out of the 4 to choose, 0 <= int <= 3;
	 * 						Top left: 0, top right: 1, bot left: 2, bot right: 3
	 * @return				List of DecompiledFace's; [0] = person, [1] = doc
	 * @throws Exception	Image dimension mismatch / shift out of bounds
	 */
	public static List<DecompiledFace> decompileFaces(BufferedImage person,
			BufferedImage document, BufferedImage mask, int shift)
			throws Exception
	{
		int imageWidth = person.getWidth();
		int imageHeight = person.getHeight();
		if (imageWidth != document.getWidth() 
				|| imageHeight != document.getHeight())
		{
			throw new Exception("Person & document image dimensions mismatch");
		} else if (imageWidth != mask.getWidth() 
				|| imageHeight != mask.getHeight())
		{
			throw new Exception("Person & mask image dimensions mismatch");
		}
		if (shift < 0 || shift > 3) {
			throw new Exception("Shift out of bounds (0 <= shift <= 3), given: "
								+ shift);
		}
		
		// Get subimages
		imageWidth /= 2;
		imageHeight /= 2;
		int x = (shift % 2 == 0) ? 0 : imageWidth,	// shift == 0 or 2 ? 0 : w
			y = (shift % 4 <= 1) ? 0 : imageHeight;	// shift == 0 or 1 ? 0 : h
		person = person.getSubimage(x, y, imageWidth, imageHeight);
		document = document.getSubimage(x, y, imageWidth, imageHeight);
		mask = mask.getSubimage(x, y, imageWidth, imageHeight);
		
		// Get the pixels of each image
		int[] personPixels = person.getRGB(0, 0, imageWidth, imageHeight, null, 0,
					 					   imageWidth);
		int[] documentPixels = document.getRGB(0, 0, imageWidth, imageHeight, null, 0,
							   			       imageWidth);
		int[] maskPixels = mask.getRGB(0, 0, imageWidth, imageHeight, null, 0,
					   				   imageWidth);
		
		// Create arrays to hold pixel data (Java initializes all entries to 0)
		int[] eyesPerson           = new int[imageWidth*imageHeight],
			  noseAndMouthPerson   = new int[imageWidth*imageHeight],
			  shouldersPerson      = new int[imageWidth*imageHeight],
			  facePerson           = new int[imageWidth*imageHeight],
			  eyesDocument         = new int[imageWidth*imageHeight],
			  noseAndMouthDocument = new int[imageWidth*imageHeight],
			  shouldersDocument    = new int[imageWidth*imageHeight],
			  faceDocument         = new int[imageWidth*imageHeight];
		
		// Extract facial elements and record them in the above arrays
		int previousColour = 0x00000000, maskColour = 0x00000000;
		int featureCounter = 0;
		Point[] facialFeatures = new Point[7];
		for (int i = 0; i < maskPixels.length; i++) {
			maskColour = maskPixels[i];
			// White = facial feature (iris/nose/mouth/chin/shoulders)
			if (maskColour == 0xFFFFFFFF) {
				x = i % imageWidth;
				y = (i - x) / imageWidth;
				facialFeatures[featureCounter++] = new Point(x, y);
				maskColour = previousColour;
			}
			switch (maskColour) { // Compare the ARGB value of the mask
			case 0x00000000: // Void = record face
				facePerson[i] = personPixels[i];
				faceDocument[i] = documentPixels[i];
				break;
			case 0xFF008000: // Forest green = record eyes (maintain on face)
				facePerson[i] = personPixels[i];
				eyesPerson[i] = personPixels[i];
				eyesDocument[i] = faceDocument[i] = documentPixels[i];
				break;
			case 0xFF00FF00: // Green = extract eyes from face
				eyesPerson[i] = personPixels[i];
				eyesDocument[i] = documentPixels[i];
				facePerson[i] = PERSON_FACE_COLOUR;
				faceDocument[i] = DOCUMENT_FACE_COLOUR;
				break;
			case 0xFF00FFFF: // Aqua = extract eyes, nose & mouth from face
				eyesPerson[i] = noseAndMouthPerson[i] = personPixels[i];
				eyesDocument[i] = noseAndMouthDocument[i] = documentPixels[i];
				facePerson[i] = PERSON_FACE_COLOUR;
				faceDocument[i] = DOCUMENT_FACE_COLOUR;
				break;
			case 0xFF000080: // Navy = record nose & mouth (maintain on face)
				noseAndMouthPerson[i] = facePerson[i] = personPixels[i];
				noseAndMouthDocument[i] = faceDocument[i] = documentPixels[i];
				break;
			case 0xFF0000FF: // Blue = extract nose & mouth from face
				noseAndMouthPerson[i] = personPixels[i];
				noseAndMouthDocument[i] = documentPixels[i];
				facePerson[i] = PERSON_FACE_COLOUR;
				faceDocument[i] = DOCUMENT_FACE_COLOUR;
				break;
			case 0xFFFF0000: // Red = draw coat for shoulders
				facePerson[i] = personPixels[i];
				faceDocument[i] = documentPixels[i];
				shouldersPerson[i] = COAT_DARK_COLOUR;
				//shouldersDocument[i] = DOCUMENT_FACE_COLOUR;
				break;
			case 0xFF000000: // Black = extract shoulders from face
				shouldersPerson[i] = personPixels[i];
				shouldersDocument[i] = documentPixels[i];
				break;
			}
			previousColour = maskColour;
		}
		
		// Store person facial data
		DecompiledFace personFacialData = new DecompiledFace(imageWidth,
				imageHeight);
		personFacialData.eyes.setRGB(
				0, 0, imageWidth, imageHeight, eyesPerson, 0, imageWidth);
		personFacialData.noseAndMouth.setRGB(
				0, 0, imageWidth, imageHeight, noseAndMouthPerson, 0, imageWidth);
		personFacialData.shoulders.setRGB(
				0, 0, imageWidth, imageHeight, shouldersPerson, 0, imageWidth);
		personFacialData.face.setRGB(
				0, 0, imageWidth, imageHeight, facePerson, 0, imageWidth);
		
		// Store document facial data
		DecompiledFace documentFacialData = new DecompiledFace(imageWidth,
				imageHeight);
		documentFacialData.eyes.setRGB(
				0, 0, imageWidth, imageHeight, eyesDocument, 0, imageWidth);
		documentFacialData.noseAndMouth.setRGB(
				0, 0, imageWidth, imageHeight, noseAndMouthDocument, 0, imageWidth);
		documentFacialData.shoulders.setRGB(
				0, 0, imageWidth, imageHeight, shouldersDocument, 0, imageWidth);
		documentFacialData.face.setRGB(
				0, 0, imageWidth, imageHeight, faceDocument, 0, imageWidth);
		
		// Store person and document facial features
		personFacialData.leftEye         = new Point(facialFeatures[0]);
		documentFacialData.leftEye       = new Point(facialFeatures[0]);
		personFacialData.rightEye        = new Point(facialFeatures[1]);
		documentFacialData.rightEye      = new Point(facialFeatures[1]);
		personFacialData.nose            = new Point(facialFeatures[2]);
		documentFacialData.nose          = new Point(facialFeatures[2]);
		personFacialData.mouth           = new Point(facialFeatures[3]);
		documentFacialData.mouth         = new Point(facialFeatures[3]);
		personFacialData.chin            = new Point(facialFeatures[4]);
		documentFacialData.chin          = new Point(facialFeatures[4]);
		personFacialData.leftShoulder    = new Point(facialFeatures[5]);
		documentFacialData.leftShoulder  = new Point(facialFeatures[5]);
		personFacialData.rightShoulder   = new Point(facialFeatures[6]);
		documentFacialData.rightShoulder = new Point(facialFeatures[6]);
		
		// Create a list to return
		List<DecompiledFace> facialData = new ArrayList<DecompiledFace>();
		facialData.add(personFacialData);
		facialData.add(documentFacialData);
		return facialData;
	}
		
	/**
	 * Fills in the face of the entrant. Will maintain the dark areas of the
	 * face and void the eyes / nose and mouth images that overflow off the face
	 * 
	 * @param frankenstein		Contains decompiled faces
	 * @param deltaXEyes		X shift of the new eyes
	 * @param deltaYEyes		Y shift of the new eyes
	 * @param deltaXPhiltrum	X shift of the new nose and mouth
	 * @param deltaYPhiltrum	Y shift of the new nose and mouth
	 * @return
	 */
	private static BufferedImage createFilledFace(
			List<List<DecompiledFace>> frankenstein, int index,
			int deltaXEyes, int deltaYEyes,
			int deltaXPhiltrum, int deltaYPhiltrum)
	{	
		// Copy the blank face
		BufferedImage originalFace = ImageGallery.deepCopy(
				frankenstein.get(1).get(index).face);
		BufferedImage tempFace = ImageGallery.deepCopy(originalFace);
		
		// Overlay eyes and nose
		Graphics2D tempGraphics = (Graphics2D) tempFace.getGraphics();
		tempGraphics.drawImage(frankenstein.get(2).get(index).eyes,
				null,deltaXEyes, deltaYEyes);		  // Draw eyes
		tempGraphics.drawImage(frankenstein.get(3).get(index).noseAndMouth,
				null,deltaXPhiltrum, deltaYPhiltrum); // Draw nose and mouth
		tempGraphics.dispose();
		
		// Iterate through all pixels of the face, maintaining
		int w = originalFace.getWidth(), h = originalFace.getHeight();
		int[] originalPixels = originalFace.getRGB(0, 0, w, h, null, 0, w);
		int[] tempPixels = tempFace.getRGB(0, 0, w, h, null, 0, w);
		for (int i = 0; i < originalPixels.length; i++) {
			switch (originalPixels[i]) {
			case 0x00000000: 		 	// Void = remove overflow if it exists
			case PERSON_DARK_COLOUR: 	// Pine = draw face contour over overlap
			case DOCUMENT_DARK_COLOUR:	// Charcoal = same as above
				tempPixels[i] = originalPixels[i];
			}
		}
		
		// Return the newly filled face
		tempFace.setRGB(0, 0, w, h, tempPixels, 0, w);
		return tempFace;
	}
	
	/**TODO
	 * Scales face, eyes, and nose & mouth so that they match those of the donor
	 * of the shoulders
	 * 
	 * @param frankenstein
	 */
	public static List<List<DecompiledFace>> scaleFacialFeatures(
			List<List<DecompiledFace>> frankenstein)
	{
		for (int i = 0; i < 2; i++) {
			DecompiledFace donorShoulders = frankenstein.get(0).get(i);
			DecompiledFace donorFace = frankenstein.get(1).get(i);
			DecompiledFace tempDF;
			int width = donorFace.face.getWidth();
			int height = donorFace.face.getHeight();
			double scaleFactor;
			
			// Scale the blank face based off of interocular distance of the
			// entrant who is donating their shoulders
			tempDF = frankenstein.get(1).get(i);
			// Scale (geometric average)
			scaleFactor = Math.sqrt(donorShoulders.getShoulderDistance()
					* tempDF.getShoulderDistance()) / tempDF.getShoulderDistance();
			//System.out.println("Scale factor face: " + scaleFactor);
			tempDF.face = ImageGallery.scale(tempDF.face,
					BufferedImage.TYPE_INT_ARGB, width, height, scaleFactor, 1);
			// Re-adjust facial data coordinates
			tempDF.leftEye.x = (int) Math.round(tempDF.leftEye.x * scaleFactor);
			tempDF.rightEye.x =	(int) Math.round(tempDF.rightEye.x*scaleFactor);
			tempDF.nose.x = (int) Math.round(tempDF.nose.x * scaleFactor);
			tempDF.mouth.x = (int) Math.round(tempDF.mouth.x * scaleFactor);
			tempDF.chin.x = (int) Math.round(tempDF.chin.x * scaleFactor);
			
			// Scale the eyes based off of interocular distance of the
			// entrant who is donating their face
			tempDF = frankenstein.get(2).get(i);
			// Scale
			scaleFactor = donorFace.getInterocularDistance()
					/ tempDF.getInterocularDistance();
			//System.out.println("Scale factor eyes: " + scaleFactor);
			tempDF.eyes = ImageGallery.scale(tempDF.eyes,
					BufferedImage.TYPE_INT_ARGB, width, height, scaleFactor, 1);
			// Re-adjust facial data coordinates
			tempDF.leftEye.x = (int) Math.round(tempDF.leftEye.x * scaleFactor);
			tempDF.rightEye.x =	(int) Math.round(tempDF.rightEye.x*scaleFactor);
			
			// Scale the nose & mouth based off of interocular distance of the
			// entrant who is donating their face
			tempDF = frankenstein.get(3).get(i);
			// Scale
			scaleFactor = Math.sqrt(donorFace.getNoseMouthDistance()
					* tempDF.getNoseMouthDistance()) / tempDF.getNoseMouthDistance();
			//System.out.println("Scale factor nose: " + scaleFactor);
			//int dy = (int) (height * (1 - scaleFactor) / 2);
			tempDF.noseAndMouth = ImageGallery.scale(tempDF.noseAndMouth,
					BufferedImage.TYPE_INT_ARGB, width, height, 1, scaleFactor);
			// Re-adjust facial data coordinates
			tempDF.nose.y = (int) Math.round(tempDF.nose.y * scaleFactor);
			tempDF.mouth.y = (int) Math.round(tempDF.mouth.y * scaleFactor);
		}
		return frankenstein;
	}
}
