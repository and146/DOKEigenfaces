/**
 * 
 */
package and146.projects.eigenfaces.gui;

import java.awt.ScrollPane;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import and146.projects.eigenfaces.opencv.InMemoryImage;

/**
 * @author neonards
 *
 * Container (GUI frame) for image.
 */
public class ImageFrame extends JFrame {
	
	private static final long serialVersionUID = 1L;

	public ImageFrame(InMemoryImage image) {
		JFrame frame = new JFrame("Image file: " + image.getFilePath());
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		frame.setResizable(true);
		frame.setLocationRelativeTo(null);
		
		// Inserts the image icon
		ImageIcon icon = new ImageIcon(image.getFilePath());
		frame.setSize(icon.getIconWidth()+10,icon.getIconHeight()+35);
		// Draw the Image data into the BufferedImage
		JLabel label1 = new JLabel(" ", icon, JLabel.CENTER);
		frame.setContentPane(new ScrollPane());
		frame.getContentPane().add(label1);
		
		frame.validate();
		frame.setVisible(true);
	}
}
