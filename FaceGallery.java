import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Hashtable;

import javax.imageio.ImageIO;

public class FaceGallery extends ImageGallery {
	public static enum ID {
		SheetF0Person,
		SheetF0Document,
		SheetF0Mask,
		SheetF1Person,
		SheetF1Document,
		SheetF1Mask,
		SheetF2Person,
		SheetF2Document,
		SheetF2Mask,
		SheetM0Person,
		SheetM0Document,
		SheetM0Mask,
		SheetM1Person,
		SheetM1Document,
		SheetM1Mask,
		SheetM2Person,
		SheetM2Document,
		SheetM2Mask,
		SheetM3Person,
		SheetM3Document,
		SheetM3Mask;
	}
	private Hashtable<ID, BufferedImage> gallery;
	
	public FaceGallery() {
		this(1, 1);
	}
	
	public FaceGallery(float preferredScaleX, float preferredScaleY) {
		gallery = new Hashtable<ID, BufferedImage>();
		this.preferredScaleX = preferredScaleX;
		this.preferredScaleY = preferredScaleY;
	}
	
	/**
	 * Stores an image to the gallery
	 * 
	 * @param id	ID of the object type
	 * @param file	File location of the image
	 */
	public void addImage(ID id, String file) {
		try {
			BufferedImage image = ImageIO.read(new File(file));
			image = scale(image, BufferedImage.TYPE_INT_ARGB,
						  (int) (image.getWidth() * preferredScaleX),
						  (int) (image.getHeight() * preferredScaleY),
						  preferredScaleX, preferredScaleY);
			gallery.put(id, image);
		} catch (Exception ex){
			ex.printStackTrace();
		}
	}
	
	/**
	 * Stores an image to the gallery
	 * 
	 * @param id	ID of the object type
	 * @param file	File location of the image
	 */
	public void addImage(ID id, String file, float scaleX, float scaleY) {
		try {
			BufferedImage image = ImageIO.read(new File(file));
			image = scale(image, BufferedImage.TYPE_INT_ARGB,
						  (int) (image.getWidth() * scaleX),
						  (int) (image.getHeight() * scaleY),
						  scaleX, scaleY);
			gallery.put(id, image);
		} catch (Exception ex){
			ex.printStackTrace();
		}
	}
	
	/**
	 * Returns the image given the ID key
	 * 
	 * @param key	ID of the object type
	 * @return		The image if the key exists, null otherwise
	 */
	public BufferedImage getImage(ID key) {
		return gallery.get(key);
	}
}
