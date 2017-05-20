import java.awt.Point;
import java.awt.image.BufferedImage;

/**
 * public DecompiledFace - Holds 4 images: eyes, nose, shoulders, and blank face
 * of a person, as well as the eye, nose, chin, and shoulder locations
 * 
 * @author Matt Preston
 */
public class DecompiledFace {
	public Point leftEye, rightEye, nose, mouth, chin,
	             leftShoulder, rightShoulder;
	public BufferedImage eyes, noseAndMouth, shoulders, face;
	
	/**
	 * An initializing constructor
	 * 
	 * @param width		Width of the facial photo
	 * @param height	Height of the facial photo
	 */
	public DecompiledFace(int width, int height) {
		leftEye       = new Point(0,0);
		rightEye      = new Point(0,0);
		nose          = new Point(0,0);
		mouth         = new Point(0,0);
		chin          = new Point(0,0);
		leftShoulder  = new Point(0,0);
		rightShoulder = new Point(0,0);
		eyes 		 = new BufferedImage(width, height,
										 BufferedImage.TYPE_INT_ARGB);
		noseAndMouth = new BufferedImage(width, height,
										 BufferedImage.TYPE_INT_ARGB);
		shoulders 	 = new BufferedImage(width, height,
										 BufferedImage.TYPE_INT_ARGB);
		face 		 = new BufferedImage(width, height,
										 BufferedImage.TYPE_INT_ARGB);
	}
	
	/**
	 * A copy constructor
	 * 
	 * @param source	The source to copy from
	 */
	public DecompiledFace(DecompiledFace source) {
		leftEye       = new Point(source.leftEye);
		rightEye      = new Point(source.rightEye);
		nose          = new Point(source.nose);
		mouth         = new Point(source.mouth);
		chin          = new Point(source.chin);
		leftShoulder  = new Point(source.leftShoulder);
		rightShoulder = new Point(source.rightShoulder);
		eyes         = ImageGallery.deepCopy(source.eyes);
		noseAndMouth = ImageGallery.deepCopy(source.noseAndMouth);
		shoulders    = ImageGallery.deepCopy(source.shoulders);
		face         = ImageGallery.deepCopy(source.face);
	}
	
	/**
	 * Returns the midpoint between eyes
	 * 
	 * @return	Interocular midpoint
	 */
	public Point getInterocularMidPoint() {
		int midX = (int) Math.round(((float) (leftEye.x+rightEye.x))/2);
		int midY = (int) Math.round(((float) (leftEye.y+rightEye.y))/2);
		return new Point(midX, midY);
	}
	
	/**
	 * Returns the midpoint between nose and mouth
	 * 
	 * @return Nose and mouth midpoint
	 */
	public Point getNoseMouthMidPoint(){
		int midX = (int) Math.round(((float) (nose.x+mouth.x))/2);
		int midY = (int) Math.round(((float) (nose.y+mouth.y))/2);
		return new Point(midX, midY);
	}
	
	/**
	 * Returns the midpoint between shoulders
	 * 
	 * @return	Shoulder midpoint
	 */
	public Point getShoulderMidPoint() {
		int midX = (int)Math.round(((float)(leftShoulder.x+rightShoulder.x))/2);
		int midY = (int)Math.round(((float)(leftShoulder.y+rightShoulder.y))/2);
		return new Point(midX, midY);
	}
	
	/**
	 * Returns the interocular distance
	 * 
	 * @return	Interocular distance
	 */
	public double getInterocularDistance() {
		return Math.sqrt(Math.pow(leftEye.x-rightEye.x, 2)
				+ Math.pow((leftEye.y-rightEye.y), 2));
	}
	
	/**
	 * Returns the nose mouth distance
	 * 
	 * @return	Nose mouth distance
	 */
	public double getNoseMouthDistance() {
		return Math.sqrt(Math.pow(nose.x-mouth.x, 2)
				+ Math.pow((nose.y-mouth.y), 2));
	}
	
	/**
	 * Returns the shoulder distance
	 * 
	 * @return	Shoulder distance
	 */
	public double getShoulderDistance() {
		return Math.sqrt(Math.pow(leftShoulder.x-rightShoulder.x, 2)
				+ Math.pow((leftShoulder.y-rightShoulder.y), 2));
	}
}
