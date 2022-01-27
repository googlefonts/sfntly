package com.google.typography.font.tools.fontviewer;

import com.google.typography.font.sfntly.Font;
import com.google.typography.font.sfntly.FontFactory;
import java.awt.Dimension;
import java.io.FileInputStream;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

/**
 * The FontViewer application shows the hierarchy of some of the tables of a font.
 *
 * <p>Each node in the left tree corresponds to an {@link AbstractNode} object, starting with the
 * {@link FontNode} for a complete font. To navigate to the child nodes, examine {@link
 * FontNode#getChildAt(int)}.
 */
public class FontViewer {

  private final JFrame frame;
  private final JScrollPane contentScrollPane;
  private JSplitPane framePane;

  FontViewer(Font font) {
    JScrollPane fontPane = createFontTree(font);
    this.contentScrollPane = createContentPane();
    this.frame = createFrame(fontPane, contentScrollPane);
  }

  private JScrollPane createFontTree(Font font) {
    TreeModel model = new DefaultTreeModel(new FontNode(font));
    JTree fontTree = new JTree(model);
    fontTree.setBorder(new EmptyBorder(3, 3, 3, 3));
    fontTree.addTreeSelectionListener(
        e -> render((AbstractNode) e.getPath().getLastPathComponent()));

    JScrollPane fontPane = new JScrollPane(fontTree);
    fontPane.setPreferredSize(new Dimension(300, 500));
    return fontPane;
  }

  private static JScrollPane createContentPane() {
    JScrollPane pane = new JScrollPane();
    pane.add(new JTextArea());
    pane.setPreferredSize(new Dimension(500, 500));
    return pane;
  }

  private JFrame createFrame(JScrollPane fontPane, JScrollPane mainPane) {
    JFrame frame = new JFrame("Font Viewer");
    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    this.framePane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, fontPane, mainPane);
    frame.getContentPane().add(framePane);
    frame.pack();
    frame.setLocationRelativeTo(null);
    return frame;
  }

  private void render(AbstractNode node) {
    JComponent mainComponent = node.render();
    mainComponent.setBorder(new EmptyBorder(3, 3, 3, 3));
    if (node.renderInScrollPane()) {
      contentScrollPane.setViewportView(mainComponent);
      contentScrollPane.revalidate();
      contentScrollPane.repaint();
      framePane.setRightComponent(contentScrollPane);
    } else {
      framePane.setRightComponent(mainComponent);
    }
  }

  public static void main(String[] args) throws Exception {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

    String fileName = 0 < args.length ? args[0] : getFilenameFromDialog();
    if (fileName != null) {
      Font font = FontFactory.getInstance().loadFonts(new FileInputStream(fileName))[0];
      FontViewer viewer = new FontViewer(font);
      viewer.frame.setVisible(true);
    }
  }

  private static String getFilenameFromDialog() {
    JFileChooser chooser = new JFileChooser();
    FileNameExtensionFilter filter = new FileNameExtensionFilter("Font files", "ttf", "otf");
    chooser.setFileFilter(filter);
    int returnVal = chooser.showOpenDialog(null);
    if (returnVal == JFileChooser.APPROVE_OPTION) {
      return chooser.getSelectedFile().getPath();
    }
    return null;
  }
}
