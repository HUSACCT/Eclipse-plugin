package plugin.views.internalframes;

import java.awt.Color;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JTextPane;

public class JInternalHusacctNotAvailableScreen extends JInternalFrame {
	private static final long serialVersionUID = 1L;
	private JTextPane textPaneInformation;
	private JPanel jPanel;
	
	public JInternalHusacctNotAvailableScreen() {
		textPaneInformation = new JTextPane();
		jPanel = new JPanel();
		setBounds(50, 50, 200, 400);
		setResizable(true);
		setClosable(true);
		setMaximizable(true);
		setIconifiable(true);
		setTitle("Husacct");
		setVisible(true);
		
		textPaneInformation.setContentType("text/html");
		textPaneInformation.setText(
				"<font face='verdana' color='black'>" +
        		"<span style='font-size: 14pt'>" +
				"<B>THIS VIEW IS NOT AVAILABLE YET</B><BR><BR>" +
				"<span style='font-size: 10pt'>" +
				"In the �StateView� you can see when it is available" +
				"</span>" 
		);
		jPanel.add(textPaneInformation);
		jPanel.setBackground(Color.WHITE);
		getContentPane().add(jPanel);
	}
}