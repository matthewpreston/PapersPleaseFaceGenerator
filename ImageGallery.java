import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.util.Hashtable;

import javax.imageio.ImageIO;

public class ImageGallery {
	public static enum ID {
		BoothWall;
	}
	public float preferredScaleX, preferredScaleY;
	private Hashtable<ID, BufferedImage> gallery;
	
	public ImageGallery() {
		this(1, 1);
	}
	
	public ImageGallery(float preferredScaleX, float preferredScaleY) {
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
	
	/**
	 * Returns a scaled image
	 * 
	 * @param sbi image to scale
	 * @param imageType type of image
	 * @param dWidth width of destination image
	 * @param dHeight height of destination image
	 * @param fWidth x-factor for transformation / scaling
	 * @param fHeight y-factor for transformation / scaling
	 * @return scaled image
	 */
	public static BufferedImage scale(BufferedImage sbi, int imageType, 
									  int dWidth, int dHeight,
									  double fWidth, double fHeight) {
	    BufferedImage dbi = null;
	    if(sbi != null) {
	        dbi = new BufferedImage(dWidth, dHeight, imageType);
	        Graphics2D g = dbi.createGraphics();
	        AffineTransform at = AffineTransform.getScaleInstance(fWidth, fHeight);
	        g.drawRenderedImage(sbi, at);
	    }
	    return dbi;
	}
	
	/**
	 * Returns a scaled image
	 * 
	 * @param sbi			Image to scale
	 * @param imageType 	Type of image
	 * @param dWidth		Width of destination image
	 * @param dHeight 		Height of destination image
	 * @param fWidth 		X-factor for transformation / scaling
	 * @param fHeight 		Y-factor for transformation / scaling
	 * @param x				X starting position to draw on new image
	 * @param y				Y starting position to draw on new image
	 * @return 				Scaled image
	 */
	public static BufferedImage scale(BufferedImage sbi, int imageType,
			int dWidth, int dHeight, double fWidth, double fHeight,
			int x, int y)
	{
	    BufferedImage dbi = null;
	    if(sbi != null) {
	        dbi = new BufferedImage(dWidth, dHeight, imageType);
	        Graphics2D g = dbi.createGraphics();
	        AffineTransform at = AffineTransform.getScaleInstance(fWidth, fHeight);
	        g.transform(at);
	        g.drawImage(sbi, x, y, sbi.getWidth(), sbi.getHeight(), null);
	        g.dispose();
	    }
	    return dbi;
	}
	
	/**
	 * Creates an identical copy of a given buffered image
	 * 
	 * @param bi	The source
	 * @return		The copy
	 */
	public static BufferedImage deepCopy(BufferedImage bi) {
	    ColorModel cm = bi.getColorModel();
	    boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
	    WritableRaster raster = bi.copyData(
	    		bi.getRaster().createCompatibleWritableRaster());
	    return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
	}
}
